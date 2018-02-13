package org.moskito.javaagent.request;

import org.moskito.javaagent.request.config.RequestListenerConfiguration;
import org.moskito.javaagent.request.wrappers.HttpRequestWrapper;

/**
 * Interface for listeners that will monitor
 * incoming http request in case javaagent
 * started on tomcat server.
 * Should have public default constructor.
 */
public interface RequestListener {


    void onRequestStart(HttpRequestWrapper request);


    void onRequestFinished(HttpRequestWrapper request, RequestResultData resultData);

    /**
     * Configure listener.
     * Called on listener instantiation.
     *
     * @param conf listener configuration
     */
    void configure(RequestListenerConfiguration conf);

}
