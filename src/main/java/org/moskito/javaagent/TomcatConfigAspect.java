package org.moskito.javaagent;

import net.anotheria.moskito.webui.util.StartStopListener;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import java.util.HashMap;
import java.util.Map;

/**
 * Aspect for editing tomcat webapps configuration
 */
@Aspect
public abstract class TomcatConfigAspect {

    public static final String CONFIG_INJECTION_CLASS = "org/apache/catalina/startup/ContextConfig";

    private static boolean listenerAdded = false;

    /**
     * Abstract pointcut: no expression is defined.
     * Expression will be provided to some generated @Aspect via 'aop.xml'.
     */
    @Pointcut()
    abstract void configurationInjectionMethods();

    /**
     * Adds filters and listeners definitions to tomcat
     * configuration by editing method argument that
     * contains parsed xml webapp configuration before it pass
     * to configuration applying method.
     *
     * @param joinPoint join point of required method
     *
     */
    @Before(value = "configurationInjectionMethods()")
    public void editTomcatConfiguration(JoinPoint joinPoint) {

        if(!listenerAdded) {

            TomcatWebappConfigBuilder builder;

            try {
                builder = new TomcatWebappConfigBuilder(joinPoint.getArgs()[0]);
            }
            catch (ClassNotFoundException e) {
                return;
            }

            Map<String, String> moskitoUiFilterInitParams = new HashMap<>();
            moskitoUiFilterInitParams.put("path", "/moskito-inspect/");

            Map<String, String> genericMonitoringFilterParams = new HashMap<>();
            genericMonitoringFilterParams.put("limit", "100");

            Map<String, String> jSTalkBackFilterFilterParams = new HashMap<>();
            jSTalkBackFilterFilterParams.put("limit", "100");

            builder.addListener(StartStopListener.class.getCanonicalName())
                    .addListener("net.anotheria.moskito.webui.util.SetupPreconfiguredAccumulators")
                  //  .addListener("net.anotheria.moskito.web.session.SessionCountProducer")
                    .addFilter(
                            "net.anotheria.moskito.web.filters.MoskitoCommandFilter",
                            "MoskitoCommandFilter",
                            new String[] {"/*"}
                    )
                    .addFilter(
                            "net.anotheria.moskito.web.filters.JourneyFilter",
                            "JourneyFilter",
                            new String[] {"/*"}
                    )
                    .addFilter(
                            "net.anotheria.moskito.web.filters.GenericMonitoringFilter",
                            "GenericMonitoringFilter",
                            new String[] {"/*"},
                            genericMonitoringFilterParams
                    )
                    .addFilter(
                            "net.anotheria.moskito.web.filters.JourneyStarterFilter",
                            "JourneyStarterFilter",
                            new String[] {"/*"}
                    )
                    .addFilter(
                            "net.anotheria.moskito.web.filters.JSTalkBackFilter",
                            "JSTalkBackFilter",
                            new String[] {"/jstalkbackfilter/*"},
                            jSTalkBackFilterFilterParams
                    )
                    .addFilter(
                            "net.anotheria.anoplass.api.filter.APIFilter",
                            "APIFilter",
                            new String[]{"/*"})
                    .addFilter(
                            "net.anotheria.moskito.webui.MoskitoUIFilter",
                            "MoskitoUIFilter",
                            new String[]{
                                    "/moskito-inspect/*",
                                    "/moskito-inspect/"
                            },
                            moskitoUiFilterInitParams);

            listenerAdded = true;

        }

    }

}
