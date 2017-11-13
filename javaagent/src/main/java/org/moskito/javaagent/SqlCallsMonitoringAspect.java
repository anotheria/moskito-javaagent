package org.moskito.javaagent;

import net.anotheria.moskito.core.calltrace.CurrentlyTracedCall;
import net.anotheria.moskito.core.calltrace.RunningTraceContainer;
import net.anotheria.moskito.core.calltrace.TraceStep;
import net.anotheria.moskito.core.calltrace.TracedCall;
import net.anotheria.moskito.core.dynamic.OnDemandStatsProducer;
import net.anotheria.moskito.core.registry.ProducerRegistryFactory;
import net.anotheria.moskito.sql.stats.QueryStats;
import net.anotheria.moskito.sql.stats.QueryStatsFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.moskito.javaagent.config.JavaAgentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aspect used to intercept SQL query calls, for further profiling/logging.
 *
 * @author esmakula
 */
@Aspect
public abstract class SqlCallsMonitoringAspect {
	/**
	 * Logging util.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(SqlCallsMonitoringAspect.class);

	/**
	 * Agent config instance.
	 */
	private final JavaAgentConfig agentConfig = JavaAgentConfig.getInstance();

	/**
	 * Query failed.
	 */
	private static final String SQL_QUERY_FAILED = " FAILED!!! ";
	/**
	 * Empty string .
	 */
	private static final String EMPTY = "";

	/**
	 * Empty string .
	 */
	private static final String SPACE = " ";

	/**
	 * Query stats producer.
	 */
	private OnDemandStatsProducer<QueryStats> producer;


	public SqlCallsMonitoringAspect() {
		producer = new OnDemandStatsProducer<>("SQLQueries", "sql", "sql", QueryStatsFactory.DEFAULT_INSTANCE);
		ProducerRegistryFactory.getProducerRegistryInstance().registerProducer(producer);
	}

	/**
	 * Abstract pointcut: no expression is defined.
	 * Expression will be provided to some generated @Aspect via 'aop.xml'.
	 */
	@Pointcut()
	abstract void monitoredStatementMethods();

	/**
	 * Abstract pointcut: no expression is defined.
	 * Expression will be provided to some generated @Aspect via 'aop.xml'.
	 */
	@Pointcut()
	abstract void monitoredPreparedStatementMethods();

	/**
	 * Pointcut to get argument which is sql statement.
	 */
	@Pointcut("args(statement)")
	void monitoredArg(String statement) {
	}

	@Pointcut("monitoredStatementMethods() && monitoredArg(statement)")
	void monitoredMethodWithArg(String statement) {
	}

	@Around(value = "monitoredMethodWithArg(statement)", argNames = "pjp,statement")
	public Object doProfilingMethod(final ProceedingJoinPoint pjp, final String statement) throws Throwable {
		return doProfiling(pjp, statement);
	}


	@Around(value = "monitoredPreparedStatementMethods()", argNames = "pjp")
	public Object doProfilingMethod(final ProceedingJoinPoint pjp) throws Throwable {
		String prepatedStatement = pjp.getTarget().toString();
		String statement = prepatedStatement.substring(prepatedStatement.indexOf(SPACE) + 1);
		return doProfiling(pjp, statement);
	}

	/**
	 * Perform profiling.
	 *
	 * @param pjp       {@link ProceedingJoinPoint}
	 * @param statement sql statement
	 * @return invocation result - both with profiling
	 * @throws Throwable on errors
	 */
	private Object doProfiling(final ProceedingJoinPoint pjp, final String statement) throws Throwable {
		switch (agentConfig.getMode()) {
			case LOG_ONLY:
				return log(pjp, statement);
			case PROFILING:
				return doMoskitoProfiling(pjp, statement);
			default:
				throw new AssertionError(agentConfig.getMode() + " not supported ");
		}
	}

	/**
	 * Perform log entry creation for log mode.
	 *
	 * @param pjp {@link ProceedingJoinPoint}
	 * @return invocation result
	 * @throws Throwable on errors
	 */
	private Object log(final ProceedingJoinPoint pjp, String statement) throws Throwable {
		try {
			return pjp.proceed();
		} finally {
			LOG.info("SQL Call Entry - " + statement);
		}
	}

	/**
	 * Perform moskito profiling.
	 *
	 * @param pjp       {@link ProceedingJoinPoint}
	 * @param statement sql statement
	 * @return invocation result - both with profiling
	 * @throws Throwable on errors
	 */
	public Object doMoskitoProfiling(ProceedingJoinPoint pjp, String statement) throws Throwable {
		long callTime = System.nanoTime();
		QueryStats cumulatedStats = producer.getDefaultStats();
		QueryStats statementStats = producer.getStats(statement);
		//add Request Count, increase CR,MCR
		cumulatedStats.addRequest();
		if (statementStats != null)
			statementStats.addRequest();
		// start stopwatch
		//System.out.println(smt);
		boolean success = true;
		try {
			Object retVal = pjp.proceed();
			// stop stopwatch
			return retVal;
		} catch (Throwable t) {
			success = false;
			cumulatedStats.notifyError(t);
			if (statementStats != null)
				statementStats.notifyError();
			throw t;
		} finally {
			final long callDurationTime = System.nanoTime() - callTime;
			//add execution time
			cumulatedStats.addExecutionTime(callDurationTime);
			if (statementStats != null)
				statementStats.addExecutionTime(callDurationTime);
			//notify request finished / decrease CR/MCR
			cumulatedStats.notifyRequestFinished();
			if (statementStats != null) {
				statementStats.notifyRequestFinished();
			}

			addTrace(statement, success, callDurationTime);

		}
	}

	/**
	 * Perform additional profiling - for Journey stuff.
	 *
	 * @param statement prepared statement
	 * @param isSuccess is success
	 */
	private void addTrace(String statement, final boolean isSuccess, final long duration) {
		TracedCall aRunningTrace = RunningTraceContainer.getCurrentlyTracedCall();
		CurrentlyTracedCall currentTrace = aRunningTrace.callTraced() ? (CurrentlyTracedCall) aRunningTrace : null;
		if (currentTrace != null) {
			TraceStep currentStep = currentTrace.startStep((isSuccess ? EMPTY : SQL_QUERY_FAILED) + "SQL : (' " + statement + "')", producer);
			if (!isSuccess)
				currentStep.setAborted();
			currentStep.setDuration(duration);
			currentTrace.endStep();
		}
	}


}
