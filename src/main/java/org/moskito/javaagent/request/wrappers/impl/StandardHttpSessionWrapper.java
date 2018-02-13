package org.moskito.javaagent.request.wrappers.impl;

import org.moskito.javaagent.request.wrappers.HttpSessionWrapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class StandardHttpSessionWrapper implements HttpSessionWrapper {

    private Object session;

    private Method getAttributeMethod;
    private Method setAttributeMethod;

    public StandardHttpSessionWrapper(Object session) throws NoSuchMethodException {

        this.session = session;

        this.getAttributeMethod = session.getClass().getMethod("getAttribute", String.class);
        this.setAttributeMethod = session.getClass().getMethod("setAttribute", String.class, Object.class);

    }

    @Override
    public void setAttribute(String name, Object value) {
        try {
            setAttributeMethod.invoke(session, name, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            // TODO : LOG IT
        }
    }

    @Override
    public Object getAttribute(String name) {
        try {
            return getAttributeMethod.invoke(session, name);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
            // TODO : LOG IT
        }
    }

}
