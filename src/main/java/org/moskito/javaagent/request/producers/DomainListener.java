package org.moskito.javaagent.request.producers;

import org.moskito.javaagent.request.dto.RequestDTO;

/**
 * Listener for domain producer with domain name as statistics unit
 */
public class DomainListener extends AbstractProducerListener {

    public DomainListener() {
        super("Domain", "filter", "default");
    }

    @Override
    protected String getStatsNameFromRequest(RequestDTO requestDTO) {
        return requestDTO.getDomain();
    }

}
