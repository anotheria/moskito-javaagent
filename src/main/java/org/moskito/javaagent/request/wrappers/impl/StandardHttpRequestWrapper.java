package org.moskito.javaagent.request.wrappers.impl;

import org.moskito.javaagent.request.wrappers.HttpRequestWrapper;
import org.moskito.javaagent.request.wrappers.HttpSessionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Wrapper implementation that takes instance of {@link javax.servlet.http.HttpServletRequest}
 * as constructor argument and provides interface methods implementations using reflection.
 */
public class StandardHttpRequestWrapper implements HttpRequestWrapper {

    private static final Logger log = LoggerFactory.getLogger(StandardHttpRequestWrapper.class);

    /**
     * Instance of {@link javax.servlet.http.HttpServletRequest} to wrap
     */
    private Object httpRequest;

    /**
     * {@link javax.servlet.http.HttpServletRequest#getRequestURI()}
     */
    private Method getUriMethod;
    /**
     * {@link javax.servlet.http.HttpServletRequest#getServerName()}
     */
    private Method getDomainMethod;
    /**
     * {@link javax.servlet.http.HttpServletRequest#getMethod()}
     */
    private Method getHttpMethodMethod;
    /**
     * {@link javax.servlet.http.HttpServletRequest#getHeader(String)}
     */
    private Method getHeaderMethod;
    /**
     * {@link javax.servlet.http.HttpServletRequest#getParameter(String)}
     */
    private Method getParameterMethod;
    /**
     * {@link javax.servlet.http.HttpServletRequest#getSession(boolean)}
     */
    private Method getSessionMethod;
    /**
     * {@link javax.servlet.http.HttpServletRequest#getRemoteAddr()}
     */
    private Method getRemoteAddrMethod;
    /**
     * {@link javax.servlet.http.HttpServletRequest#getAttribute(String)}
     */
    private Method getAttributeMethod;
    /**
     * Instance of session wrapper associated with this request
     */
    private StandardHttpSessionWrapper session;

    /**
     * Takes {@link javax.servlet.http.HttpServletRequest} instance
     * as source of provided data.
     * @param httpRequest instance {@link javax.servlet.http.HttpServletRequest} to wrap
     * @throws NoSuchMethodException if given argument is not valid http request instance
     * @throws InvocationTargetException if given argument is not valid http request instance
     * @throws IllegalAccessException if given argument is not valid http request instance
     */
    public StandardHttpRequestWrapper(Object httpRequest)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        this.httpRequest = httpRequest;

        Class<?> httpRequestClass = httpRequest.getClass();

        this.getUriMethod = httpRequestClass.getMethod("getRequestURI");
        this.getDomainMethod = httpRequestClass.getMethod("getServerName");
        this.getHttpMethodMethod = httpRequestClass.getMethod("getMethod");
        this.getHeaderMethod = httpRequestClass.getMethod("getHeader", String.class);
        this.getParameterMethod = httpRequestClass.getMethod("getParameter", String.class);
        this.getSessionMethod = httpRequestClass.getMethod("getSession", boolean.class);
        this.getRemoteAddrMethod = httpRequestClass.getMethod("getRemoteAddr");
        this.getAttributeMethod = httpRequestClass.getMethod("getAttribute", String.class);

    }

    @Override
    public String getUri() {
        try {
            return (String) getUriMethod.invoke(httpRequest);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.warn("Failed to receive data from http request", e);
            return null;
        }
    }

    @Override
    public String getMethod() {
        try {
            return (String) getHttpMethodMethod.invoke(httpRequest);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.warn("Failed to receive data from http request", e);
            return null;
        }
    }

    @Override
    public String getDomain() {
        try {
            return (String) getDomainMethod.invoke(httpRequest);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.warn("Failed to receive data from http request", e);
            return null;
        }
    }

    @Override
    public HttpSessionWrapper getSession() {
        return getSession(true);
    }

    @Override
    public HttpSessionWrapper getSession(boolean create) {

        if(session == null) {
            try {

                Object sessionObject = getSessionMethod.invoke(httpRequest, create);
                if(sessionObject != null)
                    session = new StandardHttpSessionWrapper(sessionObject);

            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                log.warn("Failed to receive data from http request", e);
            }
        }

        return session;

    }

    @Override
    public String getHeader(String headerName) {
        try {
            return (String) getHeaderMethod.invoke(httpRequest, headerName);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.warn("Failed to receive data from http request", e);
            return null;
        }
    }

    @Override
    public String getParameter(String name) {
        try {
            return (String) getParameterMethod.invoke(httpRequest, name);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.warn("Failed to receive data from http request", e);
            return null;
        }
    }

    @Override
    public String getRemoteAddr() {
        try {
            return (String) getRemoteAddrMethod.invoke(httpRequest);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.warn("Failed to receive data from http request", e);
            return null;
        }
    }

    @Override
    public Object getAttribute(String name) {
        try {
            return getAttributeMethod.invoke(httpRequest, name);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.warn("Failed to receive data from http request", e);
            return null;
        }
    }

}
