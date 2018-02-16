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

    /**
     * Called on incoming http request before it was executed
     * @param request wrapper for request to extract data
     */
    void onRequestStarted(HttpRequestWrapper request);

    /**
     * Called after http request execution
     * @param request wrapper for request to extract data
     * @param resultData request execution result data
     */
    void onRequestFinished(HttpRequestWrapper request, RequestResultData resultData);

    /**
     * Configure listener.
     * Called on listener instantiation.
     *
     * @param conf listener configuration
     */
    void configure(RequestListenerConfiguration conf);

}
