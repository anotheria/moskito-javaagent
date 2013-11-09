package org.moskito.javaagent;

import net.anotheria.moskito.aop.aspect.AbstractMoskitoAspect;
import net.anotheria.moskito.core.calltrace.CurrentlyTracedCall;
import net.anotheria.moskito.core.calltrace.RunningTraceContainer;
import net.anotheria.moskito.core.calltrace.TraceStep;
import net.anotheria.moskito.core.calltrace.TracedCall;
import net.anotheria.moskito.core.dynamic.OnDemandStatsProducer;
import net.anotheria.moskito.core.predefined.ServiceStats;
import net.anotheria.moskito.core.predefined.ServiceStatsFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.configureme.ConfigurationManager;
import org.moskito.javaagent.config.LoadTimeMonitoringConfig;
import org.moskito.javaagent.config.MonitoringClassConfig;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by IntelliJ IDEA.
 *
 * @author <a href="mailto:vzhovtiuk@anotheria.net">Vitaliy Zhovtiuk</a>
 *         Date: 10/27/13
 *         Time: 11:50 AM
 *         To change this template use File | Settings | File Templates.
 */
public
@Aspect
abstract class LoadTimeMonitoringAspect extends AbstractMoskitoAspect {

    /**
     * Factory constant is needed to prevent continuous reinstantiation of ServiceStatsFactory objects.
     */
    private static final ServiceStatsFactory FACTORY = new ServiceStatsFactory();

    private LoadTimeMonitoringConfig loadTimeMonitoringConfig = new LoadTimeMonitoringConfig();

    // abstract pointcut: no expression is defined
    @Pointcut
    abstract void monitoredMethod();

    protected LoadTimeMonitoringAspect() {
        ConfigurationManager.INSTANCE.configure(loadTimeMonitoringConfig);
    }

    @Around(value = "monitoredMethod()")
    public Object doProfilingMethod(ProceedingJoinPoint pjp) throws Throwable {
        MonitoringClassConfig method = getMonitoringConfig(pjp.getSignature().getDeclaringTypeName());
        return doProfiling(pjp, method.getProducerId(), method.getSubsystem(), method.getCategory());
    }

    /*  */
    private Object doProfiling(ProceedingJoinPoint pjp, String aProducerId, String aSubsystem, String aCategory) throws Throwable {

        OnDemandStatsProducer<ServiceStats> producer = getProducer(pjp, aProducerId, aCategory, aSubsystem, false, FACTORY);
        String producerId = producer.getProducerId();

        String caseName = pjp.getSignature().getDeclaringType() + "." + pjp.getSignature().getName();
        ServiceStats defaultStats = producer.getDefaultStats();
        ServiceStats methodStats = producer.getStats(caseName);

        final Object[] args = pjp.getArgs();
        final String method = pjp.getSignature().getName();
        defaultStats.addRequest();
        if (methodStats != null) {
            methodStats.addRequest();
        }
        TracedCall aRunningTrace = RunningTraceContainer.getCurrentlyTracedCall();
        TraceStep currentStep = null;
        CurrentlyTracedCall currentTrace = aRunningTrace.callTraced() ? (CurrentlyTracedCall) aRunningTrace : null;
        if (currentTrace != null) {
            StringBuilder call = new StringBuilder(producerId).append('.').append(method).append("(");
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
        Object ret = null;
        try {
            ret = pjp.proceed();
            return ret;
        } catch (InvocationTargetException e) {
            defaultStats.notifyError();
            if (methodStats != null) {
                methodStats.notifyError();
            }
            //System.out.println("exception of class: "+e.getCause()+" is thrown");
            if (currentStep != null) {
                currentStep.setAborted();
            }
            throw e.getCause();
        } catch (Throwable t) {
            defaultStats.notifyError();
            if (methodStats != null) {
                methodStats.notifyError();
            }
            if (currentStep != null) {
                currentStep.setAborted();
            }
            throw t;
        } finally {
            long exTime = System.nanoTime() - startTime;
            defaultStats.addExecutionTime(exTime);
            if (methodStats != null) {
                methodStats.addExecutionTime(exTime);
            }
            defaultStats.notifyRequestFinished();
            if (methodStats != null) {
                methodStats.notifyRequestFinished();
            }
            if (currentStep != null) {
                currentStep.setDuration(exTime);
                try {
                    currentStep.appendToCall(" = " + ret);
                } catch (Throwable t) {
                    currentStep.appendToCall(" = ERR: " + t.getMessage() + " (" + t.getClass() + ")");
                }
            }
            if (currentTrace != null) {
                currentTrace.endStep();
            }
        }


    }

    private MonitoringClassConfig getMonitoringConfig(String declaringTypeName) {
        for (MonitoringClassConfig monitoringClassConfig : loadTimeMonitoringConfig.getMonitoringClassConfig()) {
            if (monitoringClassConfig.patternMatch(declaringTypeName)) {
                return monitoringClassConfig;
            }
        }
        return new MonitoringClassConfig();
    }
}
