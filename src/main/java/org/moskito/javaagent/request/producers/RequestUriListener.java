package org.moskito.javaagent.request.producers;

import org.moskito.javaagent.request.dto.RequestDTO;

/**
 * Listener for request uri producer with requested uri as statistics unit
 */
public class RequestUriListener extends AbstractProducerListener {

    public RequestUriListener() {
        super("RequestURI", "filter", "default");
    }

    @Override
    protected String getStatsNameFromRequest(RequestDTO requestDTO) {
        return requestDTO.getUri();
    }

}
