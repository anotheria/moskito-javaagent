package org.moskito.javaagent;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Helper class for injecting filters and listeners to
 * tomcat web application configuration.
 */
public class TomcatWebappConfigBuilder {

    /**
     * Filter definition class
     * used by tomcat configurator.
     *
     * Should be
     * <a href="https://tomcat.apache.org/tomcat-8.0-doc/api/org/apache/tomcat/util/descriptor/web/FilterDef.html">
     *     org.apache.tomcat.util.descriptor.web.FilterDef
     * </a>
     */
    private Class<?> filterDefClass;

    /**
     * Filter mapping class
     * used by tomcat configurator.
     *
     * Should be
     * <a href="https://tomcat.apache.org/tomcat-8.0-doc/api/org/apache/tomcat/util/descriptor/web/FilterMap.html">
     *     org.apache.tomcat.util.descriptor.web.FilterMap
     * </a>
     */
    private Class<?> filterMapClass;

    /**
     * Tomcat webapp configuration
     * object to edit.
     *
     * Should be instance of
     * <a href="https://tomcat.apache.org/tomcat-8.0-doc/api/org/apache/tomcat/util/descriptor/web/WebXml.html">
     *     org.apache.tomcat.util.descriptor.web.WebXml
     * </a>
     */
    private Object webXml;

    /**
     * Constructor that takes instance
     * of WebXml configuration class to
     * edit.
     *
     * Takes required classes from classloader
     * obtained from given constructor parameter
     *
     * @param webXml tomcat configuration to edit
     * @throws ClassNotFoundException is required configuration classes not found
     */
    public TomcatWebappConfigBuilder(Object webXml) throws ClassNotFoundException {
        this.webXml = webXml;
        filterDefClass = Class.forName(
                "org.apache.tomcat.util.descriptor.web.FilterDef",
                true,
                webXml.getClass().getClassLoader()
        );
        filterMapClass = Class.forName(
                "org.apache.tomcat.util.descriptor.web.FilterMap",
                true,
                webXml.getClass().getClassLoader()
        );
    }

    /**
     * Adds filter to tomcat configuration
     *
     * @param className filter class name
     * @param filterName filter name
     * @param urlMappings array of url mappings for new filter
     *
     * @return this
     */
    public TomcatWebappConfigBuilder addFilter(
            String className, String filterName, String[] urlMappings
    ) {
        return addFilter(className, filterName, urlMappings, new HashMap<String, String>());
    }

    /**
     * Adds filter to tomcat configuration
     *
     * @param className filter class name
     * @param filterName filter name
     * @param urlMappings array of url mappings for new filter
     * @param initParams filter init parameters
     *
     * @return this
     */
    public TomcatWebappConfigBuilder addFilter(
            String className, String filterName, String[] urlMappings, Map<String, String> initParams
    ) {

        try {
            if(((Map) webXml.getClass().getMethod("getFilters").invoke(webXml)).containsKey(filterName))
                return this;
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }

        try {

            Object filterDef = filterDefClass.newInstance();
            List<Object> filterMappings = new LinkedList<>();

            filterDefClass.getMethod("setFilterClass", String.class)
                    .invoke(filterDef, className);

            filterDefClass.getMethod("setFilterName", String.class)
                    .invoke(filterDef, filterName);

            for(Map.Entry<String, String> initParam : initParams.entrySet()) {

                filterDefClass.getMethod("addInitParameter", String.class, String.class)
                        .invoke(filterDef, initParam.getKey(), initParam.getValue());

            }

            for(String urlMapping : urlMappings) {

                Object filterMapping = filterMapClass.newInstance();

                filterMapClass.getMethod("setFilterName", String.class)
                    .invoke(filterMapping, filterName);

                filterMapClass.getMethod("addURLPattern", String.class)
                        .invoke(filterMapping, urlMapping);

                filterMappings.add(filterMapping);

            }

            webXml.getClass().getMethod("addFilter", filterDefClass)
                    .invoke(webXml, filterDef);

            for(Object filterMapping : filterMappings)
                webXml.getClass().getMethod("addFilterMapping", filterMapClass)
                        .invoke(webXml, filterMapping);

        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return this;

    }

    /**
     * Adds listener to tomcat configuration
     *
     * @param className listener class name
     * @return this
     */
    public TomcatWebappConfigBuilder addListener(String className) {
        try {
            webXml.getClass().getMethod("addListener", String.class).invoke(
                webXml, className
            );
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        return this;

    }

}
