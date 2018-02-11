package org.moskito.javaagent;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.moskito.javaagent.request.RequestFinishDTO;
import org.moskito.javaagent.request.RequestStartDTO;
import org.moskito.javaagent.request.RequestURIProducerService;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Aspect for editing tomcat webapps configuration
 */
@Aspect
public abstract class TomcatRequestInterceptionAspect {

    public static final String REQUEST_INTERCEPTION_CLASS = "org/apache/catalina/core/StandardContextValve";

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
    @Around(value = "configurationInjectionMethods()")
    public Object editTomcatConfiguration(ProceedingJoinPoint joinPoint) throws Throwable {

        // Implements HttpServletRequest
        Object req =  joinPoint.getArgs()[0];

        long startTime = System.nanoTime();
        RequestStartDTO requestStartDTO = new RequestStartDTO();
        RequestFinishDTO requestFinishDTO = new RequestFinishDTO();

        String uri = null;

        try {
            uri = (String) req.getClass().getMethod("getRequestURI").invoke(req);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            System.err.println("Failed to extract uri from request");
            e.printStackTrace();
        }

        requestStartDTO.setUri(uri);

        RequestURIProducerService.getInstance().notifyRequestStarted(requestStartDTO);

        Object ret;

        try {
            ret = joinPoint.proceed();
        }
        catch (RuntimeException e) {
            requestFinishDTO.setRuntimeException(e);
            throw e;
        }
        catch (IOException e) {
            requestFinishDTO.setIoException(e);
            throw e;
        }
        catch (Exception e) {

            // TODO : ADD SERVLET EXCEPTION INHERITED CLASSES
            if(e.getClass().getCanonicalName().equals("javax.servlet.ServletException")) {
                requestFinishDTO.setServletException(e);
            }
            else
                requestFinishDTO.setOtherException(e);

            throw e;

        }
        finally {
            requestFinishDTO.setUri(uri);
            requestFinishDTO.setDuration(System.nanoTime() - startTime);
            RequestURIProducerService.getInstance().notifyRequestFinished(requestFinishDTO);
        }

        return ret;

    }

}
