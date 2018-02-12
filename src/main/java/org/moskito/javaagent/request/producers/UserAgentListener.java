package org.moskito.javaagent.request.producers;

import org.moskito.javaagent.request.dto.RequestDTO;

/**
 * Listener for user agent producer with client user agent as statistics unit
 */
public class UserAgentListener extends AbstractProducerListener {

    public UserAgentListener() {
        super("UserAgent", "filter", "default");
    }

    @Override
    protected String getStatsNameFromRequest(RequestDTO requestDTO) {
        return requestDTO.getUserAgent();
    }

}
