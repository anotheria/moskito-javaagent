package org.moskito.javaagent.request.wrappers;

/**
 * Interface for interaction with http session
 */
public interface HttpSessionWrapper {

    /**
     * Sets http session attribute
     * @param name attribute name to set
     * @param value attribute value to set
     */
    void setAttribute(String name, Object value);

    /**
     * Returns session attribute with given name.
     *
     * May return null in case of wrapper initialization error
     *
     * @param name attribute name to return
     * @return attribute with given name. Null if attribute with such name not exists
     */
    Object getAttribute(String name);

    /**
     * @return id of http session
     */
    String getId();

}
