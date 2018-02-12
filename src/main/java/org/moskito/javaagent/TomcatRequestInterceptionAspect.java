package org.moskito.javaagent;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.moskito.javaagent.request.RequestProcessingService;
import org.moskito.javaagent.request.dto.RequestDTO;
import org.moskito.javaagent.request.dto.RequestExecutionResultDTO;
import org.moskito.javaagent.util.HttpRequestParsingUtil;

import java.io.IOException;

/**
 * Aspect for collecting tomcat http requests statistics
 */
@Aspect
public abstract class TomcatRequestInterceptionAspect {

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
     * Cuts around org.apache.catalina.core.StandardContextValve.invoke(Request, Response)
     * method to extract request statistics.
     *
     * @param joinPoint join point of required method
     *
     */
    @Around(value = "configurationInjectionMethods()")
    public Object editTomcatConfiguration(ProceedingJoinPoint joinPoint) throws Throwable {

        // Instance of org.apache.catalina.connector.Request
        // that implements javax.servlet.http.HttpServletRequest
        Object req =  joinPoint.getArgs()[0];

        long startTime = System.nanoTime();

        RequestDTO requestDTO = HttpRequestParsingUtil.parseHttpRequest(req);
        RequestExecutionResultDTO resultDTO = new RequestExecutionResultDTO(requestDTO);

        Object ret;

        RequestProcessingService.getInstance().notifyRequestStarted(requestDTO);

        try {
            ret = joinPoint.proceed();
        }
        catch (RuntimeException e) {
            resultDTO.setRuntimeException(e);
            throw e;
        } catch (IOException e) {
            resultDTO.setIOException(e);
            throw e;
        } catch (Exception e) {

            if(HttpRequestParsingUtil.isServletException(e.getClass().getCanonicalName())) {
                resultDTO.setServletException(e);
            }
            else
                resultDTO.setOtherException(e);

            throw e;

        }
        finally {
            resultDTO.setDuration(System.nanoTime() - startTime);
            RequestProcessingService.getInstance().notifyRequestFinished(resultDTO);
        }

        return ret;

    }

}
