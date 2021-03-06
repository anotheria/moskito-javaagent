package org.moskito.javaagent.request.wrappers.impl;

import org.moskito.javaagent.request.wrappers.HttpSessionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Wrapper implementation that takes instance of {@link javax.servlet.http.HttpSession}
 * as constructor argument and provides interface methods implementations using reflection.
 */
public class StandardHttpSessionWrapper implements HttpSessionWrapper {

    private static final Logger log = LoggerFactory.getLogger(StandardHttpSessionWrapper.class);

    /**
     * Instance of {@link javax.servlet.http.HttpSession} to wrap
     */
    private Object session;

    /**
     * {@link javax.servlet.http.HttpSession#getAttribute(String)}
     */
    private Method getAttributeMethod;
    /**
     * {@link javax.servlet.http.HttpSession#setAttribute(String, Object)}
     */
    private Method setAttributeMethod;
    /**
     * {@link javax.servlet.http.HttpSession#getId()}
     */
    private Method getIdMethod;

    /**
     * Takes instance of {@link javax.servlet.http.HttpSession} as argument
     *
     * @param session instance of http session
     * @throws NoSuchMethodException if given object is not instance of http session
     */
    public StandardHttpSessionWrapper(Object session) throws NoSuchMethodException {

        this.session = session;

        this.getAttributeMethod = session.getClass().getMethod("getAttribute", String.class);
        this.setAttributeMethod = session.getClass().getMethod("setAttribute", String.class, Object.class);
        this.getIdMethod = session.getClass().getMethod("getId");

    }

    @Override
    public void setAttribute(String name, Object value) {
        try {
            setAttributeMethod.invoke(session, name, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.warn("Failed to access to http session", e);
        }
    }

    @Override
    public Object getAttribute(String name) {
        try {
            return getAttributeMethod.invoke(session, name);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.warn("Failed to access to http session", e);
            return null;
        }
    }

    @Override
    public String getId() {
        try {
            return (String) getIdMethod.invoke(session);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.warn("Failed to access to http session", e);
            return null;
        }
    }

}
