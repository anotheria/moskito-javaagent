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

    
    private static boolean isServletException(String exceptionClassName) {

        return exceptionClassName.equals("javax.servlet.ServletException") ||
                exceptionClassName.equals("javax.servlet.UnavailableException") ||
                exceptionClassName.equals("org.apache.jasper.JasperException");

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

            if(isServletException(e.getClass().getCanonicalName())) {
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
