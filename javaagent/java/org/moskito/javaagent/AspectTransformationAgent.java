package org.moskito.javaagent;

import org.aspectj.weaver.loadtime.ClassPreProcessorAgentAdapter;
import org.configureme.ConfigurationManager;
import org.moskito.javaagent.config.LoadTimeMonitoringConfig;

import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * TODO comment this class
 *
 * @author lrosenberg
 * @since 07.04.13 22:25
 */
public class AspectTransformationAgent implements java.lang.instrument.ClassFileTransformer {
    private org.aspectj.weaver.loadtime.ClassPreProcessorAgentAdapter classPreProcessorAgentAdapter = new ClassPreProcessorAgentAdapter();
    private LoadTimeMonitoringConfig loadTimeMonitoringConfig = new LoadTimeMonitoringConfig();


    /**
     * JVM hook to statically load the javaagent at startup.
     * <p/>
     * After the Java Virtual Machine (JVM) has initialized, the premain method
     * will be called. Then the real application main method will be called.
     *
     * @param args
     * @param inst
     * @throws Exception
     */
    public static void premain(String args, Instrumentation inst) {

        AspectTransformationAgent aspectTransformationAgent = new AspectTransformationAgent();
        ConfigurationManager.INSTANCE.configure(aspectTransformationAgent.loadTimeMonitoringConfig);
        inst.addTransformer(aspectTransformationAgent);
    }


    /**
     * JVM hook to dynamically load javaagent at runtime.
     * <p/>
     * The agent class may have an agentmain method for use when the agent is
     * started after VM startup.
     *
     * @param args
     * @param inst
     * @throws Exception
     */
    public static void agentmain(String args, Instrumentation inst) throws Exception {
        inst.addTransformer(new org.moskito.javaagent.AspectTransformationAgent());
    }


    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!containsClassToInclude(className)) return classfileBuffer;
        return classPreProcessorAgentAdapter.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
    }

    private boolean containsClassToInclude(String className) {
        String[] classesToExclude = loadTimeMonitoringConfig.getClassesToInclude();
        for(String classToExclude : classesToExclude) {
            if(className.matches(classToExclude.replace("/", "."))) {
                return true;
            }
        }
        return false;
    }
}
