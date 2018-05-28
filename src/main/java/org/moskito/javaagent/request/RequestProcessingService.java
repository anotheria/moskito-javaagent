package org.moskito.javaagent.request;

import net.anotheria.moskito.core.config.MoskitoConfigurationHolder;
import net.anotheria.moskito.core.config.filter.FilterConfig;
import org.moskito.javaagent.config.JavaAgentConfig;
import org.moskito.javaagent.request.config.RequestListenerConfiguration;
import org.moskito.javaagent.request.journey.JourneyListener;
import org.moskito.javaagent.request.journey.TagsListener;
import org.moskito.javaagent.request.producers.*;
import org.moskito.javaagent.request.wrappers.HttpRequestWrapper;
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
                RequestUriProducerListener.class
        );
        caseExtractorsAndRequestListenersAliases.put(
                "net.anotheria.moskito.web.filters.caseextractor.RefererCaseExtractor",
                ReferrerProducerListener.class
        );
        caseExtractorsAndRequestListenersAliases.put(
                "net.anotheria.moskito.web.filters.caseextractor.MethodCaseExtractor",
                MethodProducerListener.class
        );
        caseExtractorsAndRequestListenersAliases.put(
                "net.anotheria.moskito.web.filters.caseextractor.UserAgentCaseExtractor",
                UserAgentProducerListener.class
        );
        caseExtractorsAndRequestListenersAliases.put(
                "net.anotheria.moskito.web.filters.caseextractor.DomainCaseExtractor",
                DomainProducerListener.class
        );
    }

    /**
     * Configures listener and adds it to listeners list
     * @param listener instance of listener to add
     * @param configuration listener configuration
     */
    private void initListener(RequestListener listener, RequestListenerConfiguration configuration) {
        listener.configure(configuration);
        interceptionListeners.add(listener);
    }

    /**
     * Configures listeners for filter producers
     * using filter configuration.
     */
    public RequestProcessingService() {

        initCaseExtractorsAliases();

        FilterConfig filterConfig = MoskitoConfigurationHolder.getConfiguration().getFilterConfig();

        RequestListenerConfiguration conf = new RequestListenerConfiguration();
        conf.setProducersStatsLimit(JavaAgentConfig.getInstance().getRequestStatsLimit());

        initListener(new JourneyListener(), conf);
        initListener(new TagsListener(), conf);

        // Adding listeners according to case extractors configuration in filter config
        for (String caseExtractorName : filterConfig.getCaseExtractors()) {

            if(caseExtractorsAndRequestListenersAliases.containsKey(caseExtractorName)) {
                try {
                    RequestListener listener = caseExtractorsAndRequestListenersAliases.get(caseExtractorName)
                            .newInstance();
                    initListener(listener, conf);
                } catch (InstantiationException | IllegalAccessException e) {
                    log.error("Failed to instantiate listener. Case extractor aliases is corrupted", e);
                }
            }

        }

    }

    public static RequestProcessingService getInstance() {
        return INSTANCE;
    }

    /**
     * Notifies listeners that new http request started
     * @param httpRequestWrapper http request data
     */
    public void notifyRequestStarted(HttpRequestWrapper httpRequestWrapper) {
        for (RequestListener listener : interceptionListeners)
            listener.onRequestStarted(httpRequestWrapper);
    }

    /**
     * Notifies listeners that http request been finished
     * @param httpRequestWrapper http request data
     * @param resultData resulting data of request
     */
    public void notifyRequestFinished(HttpRequestWrapper httpRequestWrapper, RequestResultData resultData) {
        for (RequestListener listener : interceptionListeners)
            listener.onRequestFinished(httpRequestWrapper, resultData);
    }

}
