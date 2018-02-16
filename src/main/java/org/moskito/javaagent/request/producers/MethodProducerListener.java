package org.moskito.javaagent.request.producers;

import org.moskito.javaagent.request.wrappers.HttpRequestWrapper;

/**
 * Listener for http methods producer with http methods as statistics unit
 */
public class MethodProducerListener extends AbstractProducerListener {

    public MethodProducerListener() {
        super("Method", "filter", "default");
    }

    @Override
    protected String getStatsNameFromRequest(HttpRequestWrapper httpRequestWrapper) {
        return httpRequestWrapper.getMethod();
    }

}
