package org.moskito.javaagent.request.producers;

import org.moskito.javaagent.request.wrappers.HttpRequestWrapper;

/**
 * Listener for domain producer with domain name as statistics unit
 */
public class DomainProducerListener extends AbstractProducerListener {

    public DomainProducerListener() {
        super("Domain", "filter", "default");
    }

    @Override
    protected String getStatsNameFromRequest(HttpRequestWrapper httpRequestWrapper) {
        return httpRequestWrapper.getDomain();
    }

}
