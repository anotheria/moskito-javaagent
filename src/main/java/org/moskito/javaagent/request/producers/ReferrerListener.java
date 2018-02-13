package org.moskito.javaagent.request.producers;

import org.moskito.javaagent.request.wrappers.HttpRequestWrapper;

/**
 * Listener for referrer producer with http client referrer uri as statistics unit
 */
public class ReferrerListener extends AbstractProducerListener {

    /**
     * Constant for http.
     */
    private static final String HTTP_PROTOCOL = "http://";
    /**
     * Constant for https.
     */
    private static final String HTTPS_PROTOCOL = "https://";

    /**
     * Limit for the url length.
     */
    private static final int URI_LIMIT = 80;

    public ReferrerListener() {
        super("Referrer", "filter", "default");
    }

    private static String extractServerName(String referer){
        int end;
        end = referer.indexOf(':');
        if (end==-1)
            end = referer.indexOf('/');
        if (end==-1)
            return null;
        return referer.substring(0,end);
    }

    @Override
    protected String getStatsNameFromRequest(HttpRequestWrapper httpRequestWrapper) {

        String referer = httpRequestWrapper.getHeader("referer");

        if (referer==null || referer.length()==0)
            return "no-referer";
        if (referer.startsWith(HTTP_PROTOCOL))
            referer = referer.substring(HTTP_PROTOCOL.length());
        if (referer.startsWith(HTTPS_PROTOCOL))
            referer = referer.substring(HTTPS_PROTOCOL.length());

        String currentServerName = httpRequestWrapper.getDomain();
        String refererServerName = extractServerName(referer);

        if (currentServerName.equals(refererServerName))
            return "_this_server_";

        if (referer.length()>URI_LIMIT){
            referer = referer.substring(0, URI_LIMIT-3)+"...";
        }

        return referer;

    }

}
