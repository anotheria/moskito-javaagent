package org.moskito.javaagent.request.producers;

import org.moskito.javaagent.request.wrappers.HttpRequestWrapper;

/**
 * Listener for request uri producer with requested uri as statistics unit
 */
public class RequestUriListener extends AbstractProducerListener {

    /**
     * Limit for the url length.
     */
    private static final int URI_LIMIT = 80;

    public RequestUriListener() {
        super("RequestURI", "filter", "default");
    }

    @Override
    protected String getStatsNameFromRequest(HttpRequestWrapper httpRequestWrapper) {

        String uri = httpRequestWrapper.getUri();

        if (uri.length()>URI_LIMIT){
            uri = uri.substring(0, URI_LIMIT-3)+"...";
        }

        return uri;

    }

}
