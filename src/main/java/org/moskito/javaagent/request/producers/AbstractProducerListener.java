package org.moskito.javaagent.request.producers;

import net.anotheria.moskito.core.dynamic.OnDemandStatsProducer;
import net.anotheria.moskito.core.dynamic.OnDemandStatsProducerException;
import net.anotheria.moskito.core.predefined.Constants;
import net.anotheria.moskito.core.predefined.FilterStats;
import net.anotheria.moskito.core.predefined.FilterStatsFactory;
import net.anotheria.moskito.core.registry.ProducerRegistryFactory;
import org.moskito.javaagent.request.RequestListener;
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

    /**
     * Creates and registers producer with given arguments
     * @param producerId id of producer
     * @param category producer category
     * @param subsystem producer subsystem
     */
    protected AbstractProducerListener(String producerId, String category, String subsystem) {

        this.producer = new OnDemandStatsProducer<>(
                producerId, category, subsystem,
                new FilterStatsFactory(Constants.getDefaultIntervals())
        );

        ProducerRegistryFactory.getProducerRegistryInstance().registerProducer(this.producer);

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
     * Adds request to default and request-specific stats
     * @param requestDTO http request data
     */
    public void onRequestStart(RequestDTO requestDTO) {

        String statsName = getStatsNameFromRequest(requestDTO);

        if(statsName == null) {
            log.warn("Failed to obtain stats name for " + producer.getProducerId() + " producer.");
            return;
        }

        try {

            FilterStats requestStats = producer.getStats(statsName);
            FilterStats defaultStats = producer.getDefaultStats();

            requestStats.addRequest();
            defaultStats.addRequest();

        } catch (OnDemandStatsProducerException e) {
            log.warn("Failed to process data for " + producer.getProducerId() + " producer", e);
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

        try {

            FilterStats requestStats = producer.getStats(statsName);
            FilterStats defaultStats = producer.getDefaultStats();

            requestStats.addExecutionTime(resultDTO.getDuration());
            defaultStats.addExecutionTime(resultDTO.getDuration());

            switch (resultDTO.getExceptionKind()) {

                case IO:
                    requestStats.notifyIOException(resultDTO.getException());
                    defaultStats.notifyIOException(resultDTO.getException());
                    break;
                case SERVLET:
                    requestStats.notifyServletException(resultDTO.getException());
                    defaultStats.notifyServletException(resultDTO.getException());
                    break;
                case RUNTIME:
                    requestStats.notifyRuntimeException(resultDTO.getException());
                    defaultStats.notifyRuntimeException(resultDTO.getException());
                    break;
                case OTHER:
                    requestStats.notifyError(resultDTO.getException());
                    defaultStats.notifyError(resultDTO.getException());
                    break;
                case NONE:

            }

            requestStats.notifyRequestFinished();
            defaultStats.notifyRequestFinished();

        } catch (OnDemandStatsProducerException e) {
            log.warn("Failed to process data for " + producer.getProducerId() + " producer", e);
        }

    }

}
