package org.moskito.javaagent.request.producers;

import net.anotheria.moskito.core.dynamic.EntryCountLimitedOnDemandStatsProducer;
import net.anotheria.moskito.core.dynamic.OnDemandStatsProducer;
import net.anotheria.moskito.core.dynamic.OnDemandStatsProducerException;
import net.anotheria.moskito.core.predefined.Constants;
import net.anotheria.moskito.core.predefined.FilterStats;
import net.anotheria.moskito.core.predefined.FilterStatsFactory;
import net.anotheria.moskito.core.registry.ProducerRegistryFactory;
import org.moskito.javaagent.request.RequestListener;
import org.moskito.javaagent.request.config.RequestListenerConfiguration;
import org.moskito.javaagent.request.dto.RequestDTO;
import org.moskito.javaagent.request.dto.RequestExecutionResultDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for filter producers
 * that monitor incoming requests statistics.
 *
 * Has protected constructor to fill producer data (id, category, subsystem).
 * Contains defined methods of {@link RequestListener} that
 * fills {@link FilterStats} metrics.
 * Has abstract method {@link AbstractProducerListener#getStatsNameFromRequest(RequestDTO)}
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
     * @param requestDTO request data to get stats name
     * @return name of statistics for given request
     */
    protected abstract String getStatsNameFromRequest(RequestDTO requestDTO);

    /**
     * Fills given stats object
     * with data from given request data object
     * @param stats stats to fill
     * @param resultDTO source of data
     */
    private void fillStatisticsAfterRequest(FilterStats stats, RequestExecutionResultDTO resultDTO) {

        stats.addExecutionTime(resultDTO.getDuration());

        switch (resultDTO.getExceptionKind()) {

            case IO:
                stats.notifyIOException(resultDTO.getException());
                break;
            case SERVLET:
                stats.notifyServletException(resultDTO.getException());
                break;
            case RUNTIME:
                stats.notifyRuntimeException(resultDTO.getException());
                break;
            case OTHER:
                stats.notifyError(resultDTO.getException());
                break;
            case NONE:

        }

        stats.notifyRequestFinished();

    }

    /**
     * Adds request to default and request-specific stats
     * @param requestDTO http request data
     */
    public void onRequestStart(RequestDTO requestDTO) {

        String statsName = getStatsNameFromRequest(requestDTO);

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

    /**
     * Writes execution time and error statistics to producer
     * using given request data.
     * @param resultDTO http request resulting data
     */
    public void onRequestFinished(RequestExecutionResultDTO resultDTO) {

        String statsName = getStatsNameFromRequest(resultDTO);

        if(statsName == null) {
            log.warn("Failed to obtain stats name for " + producer.getProducerId() + " producer.");
            return;
        }

        fillStatisticsAfterRequest(producer.getDefaultStats(), resultDTO);

        try {
            fillStatisticsAfterRequest(producer.getStats(statsName), resultDTO);
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
