package org.moskito.javaagent.request.producers;

import org.moskito.javaagent.request.dto.RequestDTO;

/**
 * Listener for http methods producer with http methods as statistics unit
 */
public class MethodListener extends AbstractProducerListener {

    public MethodListener() {
        super("Method", "filter", "default");
    }

    @Override
    protected String getStatsNameFromRequest(RequestDTO requestDTO) {
        return requestDTO.getMethod();
    }

}
