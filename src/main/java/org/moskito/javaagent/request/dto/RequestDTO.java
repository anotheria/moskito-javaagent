package org.moskito.javaagent.request.dto;

/**
 * Represents http request
 * data required to collect server http requests statistics
 */
public class RequestDTO {

    /**
     * Requested uri
     */
    private String uri;
    /**
     * Requested domain
     */
    private String domain;
    /**
     * Http method of request
     */
    private String method;
    /**
     * Referer of http request
     */
    private String referrer;
    /**
     * User agent of http request
     */
    private String userAgent;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getReferrer() {
        return referrer;
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

}
