package org.moskito.javaagent.instrumentations.sample;

import net.anotheria.moskito.core.dynamic.OnDemandStatsProducer;
import net.anotheria.moskito.core.dynamic.OnDemandStatsProducerException;
import net.anotheria.moskito.core.predefined.ServiceStats;
import net.anotheria.moskito.core.predefined.ServiceStatsFactory;

/**
 * TODO comment this class
 *
 * @author lrosenberg
 * @since 22.04.13 11:37
 */
public class EchoTestWithMonitoring {
	private static OnDemandStatsProducer<ServiceStats> onDemandStatsProducer;

	static{
		onDemandStatsProducer = new OnDemandStatsProducer<ServiceStats>(EchoTestWithMonitoring.class.getSimpleName(), "agent", "default", new ServiceStatsFactory());
	}

	public long echo(long echo) throws Throwable{
		ServiceStats defaultStats = onDemandStatsProducer.getDefaultStats();
		ServiceStats methodStats = null;
		try{
			methodStats = onDemandStatsProducer.getStats("echo");
		}catch(OnDemandStatsProducerException e){}

		defaultStats.addRequest();
		if (methodStats!=null)
			methodStats.addRequest();
/*
		TracedCall aRunningTrace = RunningTraceContainer.getCurrentlyTracedCall();
		TraceStep currentStep = null;
		CurrentlyTracedCall currentTrace = aRunningTrace.callTraced() ?
				(CurrentlyTracedCall)aRunningTrace : null;
		if (currentTrace !=null){
			StringBuilder call = new StringBuilder(producer.getProducerId()).append('.').append(method.getName()).append("(");
			if (args!=null && args.length>0){
				for (int i=0; i<args.length; i++){
					call.append(args[i]);
					if (i<args.length-1)
						call.append(", ");
				}
			}
			call.append(")");
			currentStep = currentTrace.startStep(call.toString(), producer);
		}
*/
		long startTime = System.nanoTime();

		try{
			return echo;
		}catch(Throwable t){
			defaultStats.notifyError();
			methodStats.notifyError();
/*
			if (currentStep!=null)
				currentStep.setAborted();
*/
			throw t;
		}finally{
			long exTime = System.nanoTime() - startTime;
			defaultStats.addExecutionTime(exTime);
			defaultStats.notifyRequestFinished();
			if (methodStats!=null){
				methodStats.addExecutionTime(exTime);
				methodStats.notifyRequestFinished();
			}
/*
			if (currentStep!=null){
				currentStep.setDuration(exTime);
				try{
					currentStep.appendToCall(" = "+ret);
				}catch(Throwable t){
					currentStep.appendToCall(" = ERR: "+t.getMessage()+" ("+t.getClass()+")");
				}
			}
			if (currentTrace !=null)
				currentTrace.endStep();
*/
		}
	}

}
