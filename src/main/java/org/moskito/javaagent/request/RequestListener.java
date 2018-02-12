package org.moskito.javaagent.request;

import org.moskito.javaagent.request.dto.RequestDTO;
import org.moskito.javaagent.request.dto.RequestExecutionResultDTO;

/**
 * Interface for listeners that will monitor
 * incoming http request in case javaagent
 * started on tomcat server
 */
public interface RequestListener {

    /**
     * Called on incoming http request
     * @param startDTO incoming request data
     */
    void onRequestStart(RequestDTO startDTO);

    /**
     * Called when http request is finished
     * @param finishDTO finished request data
     */
    void onRequestFinished(RequestExecutionResultDTO finishDTO);

}
