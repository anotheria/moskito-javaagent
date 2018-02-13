package org.moskito.javaagent.request.wrappers;

public interface HttpRequestWrapper {

    String getUri();
    String getMethod();
    String getDomain();
    HttpSessionWrapper getSession();
    String getHeader(String headerName);

}
