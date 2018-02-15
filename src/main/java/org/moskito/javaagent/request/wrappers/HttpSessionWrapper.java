package org.moskito.javaagent.request.wrappers;


public interface HttpSessionWrapper {

    void setAttribute(String name, Object value);
    Object getAttribute(String name);
    String getId();

}
