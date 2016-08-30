package org.moskito.javaagent;

import java.lang.reflect.InvocationTargetException;

import org.moskito.javaagent.config.JavaAgentConfig;
import net.anotheria.moskito.aop.aspect.AbstractMoskitoAspect;
import net.anotheria.moskito.core.calltrace.CurrentlyTracedCall;
import net.anotheria.moskito.core.calltrace.RunningTraceContainer;
import net.anotheria.moskito.core.calltrace.TraceStep;
import net.anotheria.moskito.core.calltrace.TracedCall;
import net.anotheria.moskito.core.dynamic.IOnDemandStatsFactory;
import net.anotheria.moskito.core.dynamic.OnDemandStatsProducer;
import net.anotheria.moskito.core.predefined.ServiceStats;
import net.anotheria.moskito.core.predefined.ServiceStatsFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.moskito.javaagent.config.JavaAgentConfig.MonitoringClassConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	/**
	 * Abstract pointcut: no expression is defined.
	 * Expression will be provided to some generated @Aspect via 'aop.xml'.
	 */
	@Pointcut
	abstract void monitoredMethod();


	@Around (value = "monitoredMethod()")
	public Object doProfilingMethod(final ProceedingJoinPoint pjp) throws Throwable {
		switch (agentConfig.getMode()) {
			case LOG_ONLY:
				return log(pjp);
			case PROFILING:
				final MonitoringClassConfig configuration = agentConfig.getMonitoringConfig(pjp.getSignature().getDeclaringTypeName());
				return configuration.isDefaultConfig() ? pjp.proceed() :
						doProfiling(pjp, pjp.getSignature().getDeclaringTypeName(), configuration.getSubsystem(), configuration.getCategory());
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

		final OnDemandStatsProducer<ServiceStats> producer = getProducer(pjp, aProducerId, aCategory, aSubsystem, false, FACTORY, false);

		final String producerId = producer.getProducerId();

		final String methodName = pjp.getSignature().getName();
		final ServiceStats defaultStats = producer.getDefaultStats();
		final ServiceStats methodStats = producer.getStats(methodName);

		final Object[] args = pjp.getArgs();
		defaultStats.addRequest();
		if (methodStats != null)
			methodStats.addRequest();

		final TracedCall aRunningTrace = RunningTraceContainer.getCurrentlyTracedCall();
		TraceStep currentStep = null;
		final CurrentlyTracedCall currentTrace = aRunningTrace.callTraced() ? (CurrentlyTracedCall) aRunningTrace : null;
		if (currentTrace != null) {
			final StringBuilder call = new StringBuilder(producerId).append('.').append(methodName).append("(");
			if (args != null && args.length > 0) {
				for (int i = 0; i < args.length; i++) {
					call.append(args[i]);
					if (i < args.length - 1) {
						call.append(", ");
					}
				}
			}
			call.append(")");
			currentStep = currentTrace.startStep(call.toString(), producer);
		}
		long startTime = System.nanoTime();
		Object result = null;
		try {
			result = pjp.proceed();
			return result;
		} catch (final Throwable t) {
			defaultStats.notifyError();
			if (methodStats != null)
				methodStats.notifyError();

			if (currentStep != null)
				currentStep.setAborted();

			if (t instanceof InvocationTargetException)
				throw t.getCause();
			throw t;
		} finally {
			long exTime = System.nanoTime() - startTime;
			defaultStats.addExecutionTime(exTime);
			if (methodStats != null)
				methodStats.addExecutionTime(exTime);

			defaultStats.notifyRequestFinished();
			if (methodStats != null)
				methodStats.notifyRequestFinished();

			if (currentStep != null) {
				currentStep.setDuration(exTime);
				try {
					currentStep.appendToCall(" = " + result);
				} catch (final Throwable t) {
					currentStep.appendToCall(" = ERR: " + t.getMessage() + " (" + t.getClass() + ")");
				}
			}
			if (currentTrace != null)
				currentTrace.endStep();

		}
	}

}
