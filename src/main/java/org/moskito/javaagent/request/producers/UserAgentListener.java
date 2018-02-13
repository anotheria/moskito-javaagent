package org.moskito.javaagent.request.producers;

import org.moskito.javaagent.request.wrappers.HttpRequestWrapper;

/**
 * Listener for user agent producer with client user agent as statistics unit
 */
public class UserAgentListener extends AbstractProducerListener {

    public UserAgentListener() {
        super("UserAgent", "filter", "default");
    }

    @Override
    protected String getStatsNameFromRequest(HttpRequestWrapper httpRequestWrapper) {

        String userAgent = httpRequestWrapper.getHeader("User-Agent");

        if(userAgent == null)
            userAgent = "no-user-agent";

        return userAgent;

    }

}
