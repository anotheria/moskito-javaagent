package org.moskito.javaagent.request;

import net.anotheria.moskito.core.dynamic.OnDemandStatsProducer;
import net.anotheria.moskito.core.dynamic.OnDemandStatsProducerException;
import net.anotheria.moskito.core.predefined.Constants;
import net.anotheria.moskito.core.predefined.FilterStats;
import net.anotheria.moskito.core.predefined.FilterStatsFactory;
import net.anotheria.moskito.core.registry.ProducerRegistryFactory;
import net.anotheria.moskito.core.stats.Interval;

public class RequestURIProducerService {

    private static final RequestURIProducerService INSTANCE = new RequestURIProducerService();

    private final OnDemandStatsProducer<FilterStats> requestUriProducer;

    public RequestURIProducerService() {
        requestUriProducer = new OnDemandStatsProducer<>(
                "RequestURI", "filter", "default",
                new FilterStatsFactory(getMonitoringIntervals())
        );
        ProducerRegistryFactory.getProducerRegistryInstance().registerProducer(requestUriProducer);
    }

    public static RequestURIProducerService getInstance() {
        return INSTANCE;
    }

    public void notifyRequestStarted(RequestStartDTO requestStartDTO) {

        try {

            FilterStats requestStats = requestUriProducer.getStats(requestStartDTO.getUri());
            FilterStats defaultStats = requestUriProducer.getDefaultStats();

            requestStats.addRequest();
            defaultStats.addRequest();

        } catch (OnDemandStatsProducerException e) {
            System.err.println("BAD_DAY");
            e.printStackTrace();
        }

    }

    public void notifyRequestFinished(RequestFinishDTO requestFinishDTO) {

        try {

            FilterStats requestStats = requestUriProducer.getStats(requestFinishDTO.getUri());
            FilterStats defaultStats = requestUriProducer.getDefaultStats();

            requestStats.addExecutionTime(requestFinishDTO.getDuration());
            defaultStats.addExecutionTime(requestFinishDTO.getDuration());

            if(requestFinishDTO.getIoException() != null) {
                requestStats.notifyIOException(requestFinishDTO.getIoException());
                defaultStats.notifyIOException(requestFinishDTO.getIoException());
            }
            if(requestFinishDTO.getServletException() != null) {
                requestStats.notifyServletException(requestFinishDTO.getServletException());
                defaultStats.notifyServletException(requestFinishDTO.getServletException());
            }
            if(requestFinishDTO.getRuntimeException() != null) {
                requestStats.notifyRuntimeException(requestFinishDTO.getRuntimeException());
                defaultStats.notifyRuntimeException(requestFinishDTO.getRuntimeException());
            }
            if(requestFinishDTO.getOtherException() != null) {
                requestStats.notifyError(requestFinishDTO.getRuntimeException());
                defaultStats.notifyError(requestFinishDTO.getRuntimeException());
            }

            requestStats.notifyRequestFinished();
            defaultStats.notifyRequestFinished();

        } catch (OnDemandStatsProducerException e) {
            System.err.println("BAD_DAY");
            e.printStackTrace();
        }

    }

    private Interval[] getMonitoringIntervals(){
        return Constants.getDefaultIntervals();
    }

}
