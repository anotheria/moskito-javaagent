package org.moskito.javaagent.request.wrappers;

public interface HttpRequestWrapper {

    String getUri();
    String getMethod();
    String getDomain();
    HttpSessionWrapper getSession();
    HttpSessionWrapper getSession(boolean create);
    String getHeader(String headerName);
    String getParameter(String name);
    String getRemoteAddr();
    Object getAttribute(String name);

}
