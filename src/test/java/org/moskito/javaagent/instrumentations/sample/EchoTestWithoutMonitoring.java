package org.moskito.javaagent.instrumentations.sample;

/**
 * TODO comment this class
 *
 * @author lrosenberg
 * @since 22.04.13 11:37
 */
public class EchoTestWithoutMonitoring {
	public long echo(long echo) throws Throwable{
	    return echo +6;
	}

}
