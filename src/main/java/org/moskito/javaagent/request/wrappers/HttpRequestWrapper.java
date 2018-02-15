package org.moskito.javaagent.request.wrappers;

/**
 * Interface for interaction with http request.
 */
public interface HttpRequestWrapper {

    /**
     * @return uri of request
     * May return null in case of wrapper initialization error
     */
    String getUri();

    /**
     * @return request HTTP method
     * May return null in case of wrapper initialization error
     */
    String getMethod();

    /**
     * @return domain of request
     * May return null in case of wrapper initialization error
     */
    String getDomain();

    /**
     * Returns wrapped http session of request
     * associated with this wrapper.
     * Creates session if it was not created yet.
     *
     * May return null in case of wrapper initialization error
     *
     * @return wrapped http session
     */
    HttpSessionWrapper getSession();

    /**
     * Returns wrapped http session of request
     * associated with this wrapper.
     *
     * May return null in case of wrapper initialization error
     *
     * @param create is require to create session if it not present
     * @return wrapped http session
     *
     */
    HttpSessionWrapper getSession(boolean create);

    /**
     * Returns http request header.
     *
     * May return null in case of wrapper initialization error
     *
     * @param headerName name of header to return
     * @return http request header record
     */
    String getHeader(String headerName);

    /**
     * Returns request parameter
     * @param name parameter name
     * @return request parameter with given name
     * May return null in case of wrapper initialization error
     */
    String getParameter(String name);

    /**
     * @return IP address of client
     * May return null in case of wrapper initialization error
     */
    String getRemoteAddr();

    /**
     * Returns request attribute or null if
     * attribute with given name is not exists.
     *
     * May return null in case of wrapper initialization error
     *
     * @param name name of attribute
     * @return attribute object with given name
     */
    Object getAttribute(String name);

}
