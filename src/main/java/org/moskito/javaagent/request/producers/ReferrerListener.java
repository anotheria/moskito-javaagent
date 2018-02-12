package org.moskito.javaagent.request.producers;

import org.moskito.javaagent.request.dto.RequestDTO;

/**
 * Listener for referrer producer with http client referrer uri as statistics unit
 */
public class ReferrerListener extends AbstractProducerListener {

    public ReferrerListener() {
        super("Referrer", "filter", "default");
    }

    @Override
    protected String getStatsNameFromRequest(RequestDTO requestDTO) {
        return requestDTO.getReferrer();
    }

}
