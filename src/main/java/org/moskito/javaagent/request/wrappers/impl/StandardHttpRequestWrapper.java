package org.moskito.javaagent.request.wrappers.impl;

import org.moskito.javaagent.request.wrappers.HttpRequestWrapper;
import org.moskito.javaagent.request.wrappers.HttpSessionWrapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class StandardHttpRequestWrapper implements HttpRequestWrapper {

    private Object httpRequest;

    private Method getUriMethod;
    private Method getDomainMethod;
    private Method getHttpMethodMethod;
    private Method getHeaderMethod;
    private StandardHttpSessionWrapper session;

    public StandardHttpRequestWrapper(Object httpRequest)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        this.httpRequest = httpRequest;

        Class<?> httpRequestClass = httpRequest.getClass();

        this.getUriMethod = httpRequestClass.getMethod("getRequestURI");
        this.getDomainMethod = httpRequestClass.getMethod("getServerName");
        this.getHttpMethodMethod = httpRequestClass.getMethod("getMethod");
        this.getHeaderMethod = httpRequestClass.getMethod("getHeader", String.class);

        this.session =
                new StandardHttpSessionWrapper(httpRequestClass.getMethod("getSession").invoke(httpRequest));

    }

    @Override
    public String getUri() {
        try {
            return (String) getUriMethod.invoke(httpRequest);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
            // TODO : LOG IT
        }
    }

    @Override
    public String getMethod() {
        try {
            return (String) getHttpMethodMethod.invoke(httpRequest);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
            // TODO : LOG IT
        }
    }

    @Override
    public String getDomain() {
        try {
            return (String) getDomainMethod.invoke(httpRequest);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
            // TODO : LOG IT
        }
    }

    @Override
    public HttpSessionWrapper getSession() {
        return session;
    }

    @Override
    public String getHeader(String headerName) {
        try {
            return (String) getHeaderMethod.invoke(httpRequest, headerName);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
            // TODO : LOG IT
        }
    }

}
