package org.moskito.javaagent.util;

import org.moskito.javaagent.request.dto.RequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

/**
 * Helper class for retrieving data from
 * http requests.
 */
public class HttpRequestParsingUtil {

    private static final Logger log = LoggerFactory.getLogger(HttpRequestParsingUtil.class);

    /**
     * Constant for http.
     */
    public static final String HTTP_PROTOCOL = "http://";
    /**
     * Constant for https.
     */
    public static final String HTTPS_PROTOCOL = "https://";

    /**
     * Limit for the url length.
     */
    public static final int URI_LIMIT = 80;

    public static boolean isServletException(String exceptionClassName) {

        return exceptionClassName.equals("javax.servlet.ServletException") ||
                exceptionClassName.equals("javax.servlet.UnavailableException") ||
                exceptionClassName.equals("org.apache.jasper.JasperException");

    }

    public static RequestDTO parseHttpRequest(Object httpRequest) {

        RequestDTO requestDTO = new RequestDTO();

        String domain = null;
        String method = null;
        String referer = null;
        String userAgent = null;
        String uri = null;

        try {

            domain    =  (String) httpRequest.getClass().getMethod("getServerName").invoke(httpRequest);
            method    =  ((String) httpRequest.getClass().getMethod("getMethod").invoke(httpRequest));
            referer   =  extractReferer(httpRequest);
            userAgent =  extractUserAgent(httpRequest);
            uri       =  extractUri(httpRequest);

        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.warn("Failed to extract data from http request", e);
        }

        requestDTO.setDomain(domain);
        requestDTO.setMethod(method);
        requestDTO.setReferrer(referer);
        requestDTO.setUserAgent(userAgent);
        requestDTO.setUri(uri);

        return requestDTO;

    }

    private static String extractUserAgent(Object req)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        String userAgent = ((String)
                req.getClass().getMethod("getHeader", String.class).invoke(req, "User-Agent")
        );

        if(userAgent == null)
            userAgent = "no-user-agent";

        return userAgent;

    }

    private static String extractUri(Object req)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        String ret = (String) req.getClass().getMethod("getRequestURI").invoke(req);

        if (ret.length()>URI_LIMIT){
            ret = ret.substring(0, URI_LIMIT-3)+"...";
        }

        return ret;

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

    private static String extractReferer(Object req) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        String referer =  ((String)
                req.getClass().getMethod("getHeader", String.class).invoke(req, "referer")
        );

        if (referer==null || referer.length()==0)
            return "no-referer";
        if (referer.startsWith(HTTP_PROTOCOL))
            referer = referer.substring(HTTP_PROTOCOL.length());
        if (referer.startsWith(HTTPS_PROTOCOL))
            referer = referer.substring(HTTPS_PROTOCOL.length());

        String currentServerName = (String) req.getClass().getMethod("getServerName").invoke(req);
        String refererServerName = extractServerName(referer);

        if (currentServerName.equals(refererServerName))
            return "_this_server_";

        if (referer.length()>URI_LIMIT){
            referer = referer.substring(0, URI_LIMIT-3)+"...";
        }

        return referer;

    }

}
