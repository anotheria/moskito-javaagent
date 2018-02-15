package org.moskito.javaagent.request.producers;

import net.anotheria.moskito.core.dynamic.EntryCountLimitedOnDemandStatsProducer;
import net.anotheria.moskito.core.dynamic.OnDemandStatsProducer;
import net.anotheria.moskito.core.dynamic.OnDemandStatsProducerException;
import net.anotheria.moskito.core.predefined.Constants;
import net.anotheria.moskito.core.predefined.FilterStats;
import net.anotheria.moskito.core.predefined.FilterStatsFactory;
import net.anotheria.moskito.core.registry.ProducerRegistryFactory;
import org.moskito.javaagent.request.RequestListener;
import org.moskito.javaagent.request.RequestResultData;
import org.moskito.javaagent.request.config.RequestListenerConfiguration;
import org.moskito.javaagent.request.wrappers.HttpRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for filter producers
 * that monitor incoming requests statistics.
 *
 * Has protected constructor to fill producer data (id, category, subsystem).
 * Contains defined methods of {@link RequestListener} that
 * fills {@link FilterStats} metrics.
 * Has abstract method {@link AbstractProducerListener#getStatsNameFromRequest(HttpRequestWrapper)}
 * to obtain statistic for given request.
 */
public abstract class AbstractProducerListener implements RequestListener {

    private static final Logger log = LoggerFactory.getLogger(AbstractProducerListener.class);

    /**
     * Producer of this listener
     */
    private OnDemandStatsProducer<FilterStats> producer;

    private String producerId;
    private String producerCategory;
    private String producerSubsystem;

    /**
     * Creates and registers producer associated with listener.
     * @param limit producer stats limit
     */
    private void init(int limit) {

        this.producer = limit < 0 ?
                new OnDemandStatsProducer<>(
                        producerId, producerCategory, producerSubsystem,
                        new FilterStatsFactory(Constants.getDefaultIntervals())
                ) :
                new EntryCountLimitedOnDemandStatsProducer<>(
                        producerId, producerCategory, producerSubsystem,
                        new FilterStatsFactory(Constants.getDefaultIntervals()),
                        limit
                );

        ProducerRegistryFactory.getProducerRegistryInstance().registerProducer(this.producer);

    }

    /**
     * Saves given producer credentials to
     * later create it on {@link AbstractProducerListener#configure(RequestListenerConfiguration)} call.
     *
     * @param producerId id of producer
     * @param category producer category
     * @param subsystem producer subsystem
     */
    protected AbstractProducerListener(String producerId, String category, String subsystem) {
        this.producerId = producerId;
        this.producerCategory = category;
        this.producerSubsystem = subsystem;
    }

    /**
     * Should return name for stats object
     * associated with given request.
     *
     * @param httpRequestWrapper request data to get stats name
     * @return name of statistics for given request
     */
    protected abstract String getStatsNameFromRequest(HttpRequestWrapper httpRequestWrapper);

    private void fillStatisticsAfterRequest(FilterStats stats, RequestResultData resultData) {

        stats.addExecutionTime(resultData.getDuration());

        switch (resultData.getExceptionKind()) {

            case IO:
                stats.notifyIOException(resultData.getException());
                break;
            case SERVLET:
                stats.notifyServletException(resultData.getException());
                break;
            case RUNTIME:
                stats.notifyRuntimeException(resultData.getException());
                break;
            case ERROR:
                stats.notifyError(resultData.getException());
                break;
            case NONE:

        }

        stats.notifyRequestFinished();

    }

    /**
     * Adds request to default and request-specific stats
     * @param httpRequestWrapper http request data
     */
    public void onRequestStarted(HttpRequestWrapper httpRequestWrapper) {

        String statsName = getStatsNameFromRequest(httpRequestWrapper);

        if(statsName == null) {
            log.warn("Failed to obtain stats name for " + producer.getProducerId() + " producer.");
            return;
        }

        producer.getDefaultStats().addRequest();

        try {
            producer.getStats(statsName).addRequest();
        } catch (OnDemandStatsProducerException e) {
            log.debug(
                    "Failed to process data for " + producer.getProducerId() + " producer. Stats amount limit reached"
            );
        }

    }


    public void onRequestFinished(HttpRequestWrapper httpRequestWrapper, RequestResultData resultData) {

        String statsName = getStatsNameFromRequest(httpRequestWrapper);

        if(statsName == null) {
            log.warn("Failed to obtain stats name for " + producer.getProducerId() + " producer.");
            return;
        }

        fillStatisticsAfterRequest(producer.getDefaultStats(), resultData);

        try {
            fillStatisticsAfterRequest(producer.getStats(statsName), resultData);
        } catch (OnDemandStatsProducerException e) {
            log.debug(
                    "Failed to process data for " + producer.getProducerId() + " producer. Stats amount limit reached"
            );
        }

    }


    /**
     * Configure listener.
     * Called on listener instantiation.
     *
     * @param conf listener configuration
     */
    public void configure(RequestListenerConfiguration conf){
        init(conf.getProducersStatsLimit());
    }

}
