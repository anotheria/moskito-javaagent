package org.moskito.javaagent;

import net.anotheria.moskito.aop.aspect.AbstractMoskitoAspect;
import net.anotheria.moskito.core.calltrace.*;
import net.anotheria.moskito.core.context.MoSKitoContext;
import net.anotheria.moskito.core.dynamic.IOnDemandStatsFactory;
import net.anotheria.moskito.core.dynamic.OnDemandStatsProducer;
import net.anotheria.moskito.core.journey.Journey;
import net.anotheria.moskito.core.journey.JourneyManagerFactory;
import net.anotheria.moskito.core.predefined.ServiceStats;
import net.anotheria.moskito.core.predefined.ServiceStatsFactory;
import net.anotheria.moskito.core.tracer.Trace;
import net.anotheria.moskito.core.tracer.TracerRepository;
import net.anotheria.moskito.core.tracer.Tracers;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.moskito.javaagent.config.JavaAgentConfig;
import org.moskito.javaagent.config.JavaAgentConfig.MonitoringClassConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

/**
 * Abstract aspect into which desired classes will be weaved, for further profiling/logging.
 *
 * @author <a href="mailto:vzhovtiuk@anotheria.net">Vitaliy Zhovtiuk</a>
 *         Date: 10/27/13
 *         Time: 11:50 AM
 *         To change this template use File | Settings | File Templates.
 */
@Aspect
public abstract class LoadTimeMonitoringAspect extends AbstractMoskitoAspect<ServiceStats> {
	/**
	 * Logging util.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(LoadTimeMonitoringAspect.class);

	/**
	 * Factory constant is needed to prevent continuous re-instantiation of ServiceStatsFactory objects.
	 */
	private static final IOnDemandStatsFactory<ServiceStats> FACTORY = ServiceStatsFactory.DEFAULT_INSTANCE;
	/**
	 * LoadTimeMonitoringConfig instance.
	 */
	private final JavaAgentConfig agentConfig = JavaAgentConfig.getInstance();

	private static final ThreadLocal<String> lastProducerId = new ThreadLocal<>();

	/**
	 * Abstract pointcut: no expression is defined.
	 * Expression will be provided to some generated @Aspect via 'aop.xml'.
	 */
	@Pointcut
	abstract void monitoredMethod();

	@Around (value = "monitoredMethod()")
	public Object doProfilingMethod(final ProceedingJoinPoint pjp) throws Throwable {

		final MonitoringClassConfig configuration = agentConfig.getMonitoringConfig(pjp.getSignature().getDeclaringTypeName());
		if (configuration.isDefaultConfig()) {
			return pjp.proceed();
		}
		switch (agentConfig.getMode()) {
			case LOG_ONLY:
				return log(pjp);
			case PROFILING:
				return doProfiling(pjp, pjp.getSignature().getDeclaringType().getSimpleName(), configuration.getSubsystem(), configuration.getCategory());
			default:
				throw new AssertionError(agentConfig.getMode() + " not supported ");
		}

	}

	/**
	 * Perform log entry creation for log mode.
	 *
	 * @param pjp
	 * 		{@link ProceedingJoinPoint}
	 * @return invocation result
	 * @throws Throwable
	 * 		on errors
	 */
	private Object log(final ProceedingJoinPoint pjp) throws Throwable {
		try {
			return pjp.proceed();
		} finally {
			LOG.info("Entry - " + pjp.getSignature().getDeclaringType() + "." + pjp.getSignature().getName());
		}
	}

	/**
	 * Perform moskito profiling.
	 *
	 * @param pjp
	 * 		{@link ProceedingJoinPoint}
	 * @param aProducerId
	 * 		id of the moskito producer to be used
	 * @param aSubsystem
	 * 		moskito subsystem to be used
	 * @param aCategory
	 * 		moskito category to be used
	 * @return invocation result - both with profiling
	 * @throws Throwable
	 * 		on errors
	 */
	private Object doProfiling(final ProceedingJoinPoint pjp, final String aProducerId, final String aSubsystem, final String aCategory) throws Throwable {

		final OnDemandStatsProducer<ServiceStats> producer = getProducer(
				pjp, aProducerId, aCategory, aSubsystem, false, FACTORY, true
		);

		final String producerId = producer.getProducerId();
		final String prevProducerId = lastProducerId.get();

		final String methodName = pjp.getSignature().getName();
		final Object[] args = pjp.getArgs();

		final ServiceStats defaultStats = producer.getDefaultStats();
		final ServiceStats methodStats = producer.getStats(methodName);

		lastProducerId.set(producerId);

		defaultStats.addRequest();
		methodStats.addRequest();

		TracedCall aRunningTrace = RunningTraceContainer.getCurrentlyTracedCall();
		TraceStep currentStep = null;
		CurrentlyTracedCall currentTrace = aRunningTrace.callTraced() ? (CurrentlyTracedCall) aRunningTrace : null;

		MoSKitoContext context = MoSKitoContext.get();
		TracerRepository tracerRepository = TracerRepository.getInstance();

		boolean tracingRequired =
				!context.hasTracerFired() && tracerRepository.isTracingEnabledForProducer(producerId);
		Trace trace = null;
		boolean journeyStartedByCurrentStep = false;
		StringBuilder call = null;

		if (tracingRequired) {

			trace = new Trace();
			context.setTracerFired();

			if (currentTrace == null) {

				String journeyCallName = Tracers.getCallName(trace);
				RunningTraceContainer.startTracedCall(journeyCallName);
				journeyStartedByCurrentStep = true;

				currentTrace = (CurrentlyTracedCall) RunningTraceContainer.getCurrentlyTracedCall();

			}

			call = TracingUtil.buildCall(producerId, methodName, args, Tracers.getCallName(trace));
			currentStep = currentTrace.startStep(call.toString(), producer);

		}

		long startTime = System.nanoTime();
		Object ret = null;

		try {
			ret = pjp.proceed();
			return ret;
		}
		catch (Throwable t) {

			// InvocationTargetException may wrap real exception.
			// Unwrapping it if necessary
			final Throwable realCause = (t instanceof InvocationTargetException) ?
					t.getCause() : t;

			defaultStats.notifyError(realCause);
			methodStats.notifyError();

			if (tracingRequired) {

				currentStep.setAborted();

				if (t instanceof InvocationTargetException) {
					call.append(" ERR ").append(realCause.getMessage());
				}

			}

			throw realCause;

		} finally {

			long exTime = System.nanoTime() - startTime;

			if (!producerId.equals(prevProducerId)) {
				defaultStats.addExecutionTime(exTime);
			}

			methodStats.addExecutionTime(exTime);
			lastProducerId.set(prevProducerId);

			defaultStats.notifyRequestFinished();
			methodStats.notifyRequestFinished();

			if (tracingRequired) {

				currentStep.setDuration(exTime);
				currentStep.appendToCall(" = " + TracingUtil.parameter2string(ret));

				currentTrace.endStep();

				call.append(" = ").append(TracingUtil.parameter2string(ret));
				trace.setCall(call.toString());
				trace.setDuration(exTime);
				trace.setElements(Thread.currentThread().getStackTrace());

				if (journeyStartedByCurrentStep) {
					//now finish the journey.
					Journey myJourney = JourneyManagerFactory.getJourneyManager().getOrCreateJourney(Tracers.getJourneyNameForTracers(producerId));
					myJourney.addUseCase((CurrentlyTracedCall) RunningTraceContainer.endTrace());
					RunningTraceContainer.cleanup();
				}

				tracerRepository.addTracedExecution(producerId, trace);

			}

		}

	}

}
