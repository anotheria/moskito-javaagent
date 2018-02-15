package org.moskito.javaagent;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.moskito.javaagent.request.RequestProcessingService;
import org.moskito.javaagent.request.RequestResultData;
import org.moskito.javaagent.request.wrappers.HttpRequestWrapper;
import org.moskito.javaagent.request.wrappers.impl.StandardHttpRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Aspect for collecting tomcat http requests statistics
 */
@Aspect
public abstract class TomcatRequestInterceptionAspect {

    private static final Logger log = LoggerFactory.getLogger(TomcatRequestInterceptionAspect.class);

    /**
     * Name of class that contain current interception point
     */
    public static final String REQUEST_INTERCEPTION_CLASS = "org/apache/catalina/core/StandardContextValve";

    /**
     * Abstract pointcut: no expression is defined.
     * Expression will be provided to some generated @Aspect via 'aop.xml'.
     */
    @Pointcut()
    abstract void configurationInjectionMethods();

    /**
     * Loads javax.servlet.ServletException class
     * using given exception class loader to check
     * is given exception is instance of servlet exception.
     *
     * @param exception exception to check
     * @return true - given exception is instance of javax.servlet.ServletException
     */
    private static boolean isServletException(Exception exception) {

        try {
            // javax.servlet.ServletException class should be available
            // by using class loader of tomcat app instantiated exceptions
            Class<?> servletExceptionClass = exception.getClass().getClassLoader()
                    .loadClass("javax.servlet.ServletException");
            return servletExceptionClass.isInstance(exception);
        } catch (ClassNotFoundException e) {
            log.warn("Failed to find servlet exception class using given exception class loader.", e);
            return false;
        }

    }

    /**
     * Cuts around org.apache.catalina.core.StandardContextValve.invoke(Request, Response)
     * method to extract request statistics.
     *
     * @param joinPoint join point of required method
     *
     */
    @Around(value = "configurationInjectionMethods()")
    public Object editTomcatConfiguration(ProceedingJoinPoint joinPoint) throws Throwable {

        // Should be instance of org.apache.catalina.connector.Request
        // that implements javax.servlet.http.HttpServletRequest
        Object httpRequest =  joinPoint.getArgs()[0];
        long startTime = System.nanoTime();
        HttpRequestWrapper requestWrapper;
        RequestResultData resultData = new RequestResultData();
        Object ret;

        try {
            requestWrapper = new StandardHttpRequestWrapper(httpRequest);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException  e) {
            log.warn("Failed to intercept http request", e);
            return joinPoint.proceed();
        }

        RequestProcessingService.getInstance().notifyRequestStarted(requestWrapper);

        try {
            ret = joinPoint.proceed();
        }
        catch (RuntimeException e) {
            resultData.setRuntimeException(e);
            throw e;
        } catch (IOException e) {
            resultData.setIOException(e);
            throw e;
        }
        catch (Error err) {
            resultData.setError(err);
            throw err;
        } catch (Exception e) {

            // ServletException direct catching is impossible due to servlet api is not available here
            if(isServletException(e)) {
                resultData.setServletException(e);
            }

            throw e;

        }
        finally {
            resultData.setDuration(System.nanoTime() - startTime);
            RequestProcessingService.getInstance().notifyRequestFinished(requestWrapper, resultData);
        }

        return ret;

    }

}
