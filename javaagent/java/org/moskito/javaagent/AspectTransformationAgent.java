package org.moskito.javaagent;

import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import net.anotheria.moskito.webui.embedded.StartMoSKitoInspectBackendForRemote;
import org.aspectj.weaver.loadtime.ClassPreProcessorAgentAdapter;
import org.moskito.javaagent.config.LoadTimeMonitoringConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main moskito - agent entry-point.
 *
 * @author lrosenberg
 * @since 07.04.13 22:25
 */
public class AspectTransformationAgent implements java.lang.instrument.ClassFileTransformer {
	/**
	 * Logging util.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(AspectTransformationAgent.class);
	/**
	 * {@link ClassPreProcessorAgentAdapter} instance.
	 */
	private ClassPreProcessorAgentAdapter classPreProcessorAgentAdapter = new ClassPreProcessorAgentAdapter();
	/**
	 * {@link LoadTimeMonitoringConfig} instance.
	 */
	private static final LoadTimeMonitoringConfig CONFIGURATION = LoadTimeMonitoringConfig.getInstance();


	/**
	 * JVM hook to statically load the javaagent at startup.
	 * <p/>
	 * After the Java Virtual Machine (JVM) has initialized, the premain method
	 * will be called. Then the real application main method will be called.
	 *
	 * @param args
	 * 		arguments
	 * @param inst
	 * 		{@link Instrumentation}
	 */
	public static void premain(String args, Instrumentation inst) {
		LOG.info("premain method invoked with args: {} and inst: {}", args, inst);
		AspectTransformationAgent aspectTransformationAgent = new AspectTransformationAgent();
		inst.addTransformer(aspectTransformationAgent);
		startMoskitoBackend();
	}


	/**
	 * JVM hook to dynamically load javaagent at runtime.
	 * <p/>
	 * The agent class may have an agentmain method for use when the agent is
	 * started after VM startup.
	 *
	 * @param args
	 * 		arguments
	 * @param inst
	 * 		{@link Instrumentation} instance
	 * @throws Exception
	 */
	public static void agentmain(String args, Instrumentation inst) throws Exception {
		LOG.info("agentmain method invoked with args: {} and inst: {}", args, inst);
		inst.addTransformer(new org.moskito.javaagent.AspectTransformationAgent());
		startMoskitoBackend();
	}

	/**
	 * Perform moskito-backend start, in case if enabled by configuration!.
	 *
	 */
	private static void startMoskitoBackend() {

		if (CONFIGURATION.startMoskitoBacked())
			try {
				LOG.info("Starting Moskito backend on " + CONFIGURATION.getMoskitoBackendPort() + " port! !");
				StartMoSKitoInspectBackendForRemote.startMoSKitoInspectBackend(CONFIGURATION.getMoskitoBackendPort());
				LOG.info("Starting Moskito backend on " + CONFIGURATION.getMoskitoBackendPort() + " port! Performed successfully!");
			} catch (final Throwable mise) {
				LOG.error("Failed to start moskitoInspect backend. [" + mise.getMessage() + "]", mise);
			}
	}


	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		if (!containsClassToInclude(className))
			return classfileBuffer;
		return classPreProcessorAgentAdapter.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
	}


	/**
	 * Return {@code false} in case if nothing should be included, {@code true} otherwise.
	 *
	 * @param className
	 * 		name of the class
	 * @return booean condition
	 */
	private boolean containsClassToInclude(final String className) {
		final String[] toInclude = CONFIGURATION.getClassesToInclude();
		if (toInclude == null)
			return false;
		for (String classToExclude : toInclude)
			if (className.matches(classToExclude.replace("/", ".")))
				return true;


		return false;
	}
}
