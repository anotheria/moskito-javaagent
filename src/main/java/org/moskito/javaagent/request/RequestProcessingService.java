package org.moskito.javaagent.request;

import net.anotheria.moskito.core.config.MoskitoConfigurationHolder;
import net.anotheria.moskito.core.config.filter.FilterConfig;
import org.moskito.javaagent.request.dto.RequestDTO;
import org.moskito.javaagent.request.dto.RequestExecutionResultDTO;
import org.moskito.javaagent.request.producers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Service to process incoming http requests data.
 * Used to register data for http request statistics producers.
 */
public class RequestProcessingService {

    private static final RequestProcessingService INSTANCE = new RequestProcessingService();
    private static final Logger log = LoggerFactory.getLogger(AbstractProducerListener.class);

    /**
     * Aliases between case extractors and request listeners for filter category producers.
     * Needed to configure service using old-style configuration
     */
    /*
     * NOTE : Case extractors can not be used instead listeners due they contain servlet api classes
     *        witch are not available in javaagent.
     */
    private final Map<String, Class<? extends RequestListener>>
            caseExtractorsAndRequestListenersAliases = new HashMap<>();

    /**
     * List of registered listeners to notify about incoming http requests
     */
    private List<RequestListener> interceptionListeners = new LinkedList<>();

    /**
     * Fills aliases of case extractors and producer listeners
     */
    private void initCaseExtractorsAliases() {
        caseExtractorsAndRequestListenersAliases.put(
                "net.anotheria.moskito.web.filters.caseextractor.RequestURICaseExtractor",
                RequestUriListener.class
        );
        caseExtractorsAndRequestListenersAliases.put(
                "net.anotheria.moskito.web.filters.caseextractor.RefererCaseExtractor",
                ReferrerListener.class
        );
        caseExtractorsAndRequestListenersAliases.put(
                "net.anotheria.moskito.web.filters.caseextractor.MethodCaseExtractor",
                MethodListener.class
        );
        caseExtractorsAndRequestListenersAliases.put(
                "net.anotheria.moskito.web.filters.caseextractor.UserAgentCaseExtractor",
                UserAgentListener.class
        );
        caseExtractorsAndRequestListenersAliases.put(
                "net.anotheria.moskito.web.filters.caseextractor.DomainCaseExtractor",
                DomainListener.class
        );
    }

    /**
     * Configures listeners for filter producers
     * using filter configuration
     * {@see {@link FilterConfig}}
     */
    public RequestProcessingService() {

        initCaseExtractorsAliases();

        FilterConfig filterConfig = MoskitoConfigurationHolder.getConfiguration().getFilterConfig();

        for (String caseExtractorName : filterConfig.getCaseExtractors()) {

            if(caseExtractorsAndRequestListenersAliases.containsKey(caseExtractorName)) {
                try {
                    interceptionListeners.add(
                            caseExtractorsAndRequestListenersAliases.get(caseExtractorName).newInstance()
                    );
                } catch (InstantiationException | IllegalAccessException e) {
                    log.error("Failed to instantiate listener. Case extractor aliases is corrupted");
                }
            }

        }

    }

    public static RequestProcessingService getInstance() {
        return INSTANCE;
    }

    /**
     * Notifies listeners that new request started
     *
     * @param requestDTO new request data
     */
    public void notifyRequestStarted(RequestDTO requestDTO) {
        for (RequestListener listener : interceptionListeners)
            listener.onRequestStart(requestDTO);
    }

    /**
     * Notifies listener that request finished
     *
     * @param resultDTO finished request data
     */
    public void notifyRequestFinished(RequestExecutionResultDTO resultDTO) {
        for (RequestListener listener : interceptionListeners)
            listener.onRequestFinished(resultDTO);
    }

}
