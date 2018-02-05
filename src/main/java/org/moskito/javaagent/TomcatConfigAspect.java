package org.moskito.javaagent;

import net.anotheria.moskito.webui.util.StartStopListener;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.util.HashMap;
import java.util.Map;

@Aspect
public class TomcatConfigAspect {

    public static final String CONFIG_INJECTION_CLASS = "org/apache/catalina/startup/ContextConfig";


}
