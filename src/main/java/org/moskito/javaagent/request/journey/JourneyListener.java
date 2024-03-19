package org.moskito.javaagent.request.journey;

import net.anotheria.moskito.core.calltrace.CurrentlyTracedCall;
import net.anotheria.moskito.core.calltrace.NoTracedCall;
import net.anotheria.moskito.core.calltrace.RunningTraceContainer;
import net.anotheria.moskito.core.calltrace.TracedCall;
import net.anotheria.moskito.core.context.MoSKitoContext;
import net.anotheria.moskito.core.journey.Journey;
import net.anotheria.moskito.core.journey.JourneyManager;
import net.anotheria.moskito.core.journey.JourneyManagerFactory;
import net.anotheria.moskito.core.journey.NoSuchJourneyException;
import org.moskito.javaagent.request.RequestListener;
import org.moskito.javaagent.request.RequestResultData;
import org.moskito.javaagent.request.config.RequestListenerConfiguration;
import org.moskito.javaagent.request.wrappers.HttpRequestWrapper;
import org.moskito.javaagent.request.wrappers.HttpSessionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener for setting up and recording journeys
 */
public class JourneyListener implements RequestListener {

    private static final Logger log = LoggerFactory.getLogger(JourneyListener.class);

    private static final String SA_JOURNEY_RECORD = "mskJourneyRecord";
    private static final String HEADER_JOURNEY_NAME = "JourneyName";
    private static final String PARAM_JOURNEY_NAME = "mskJourneyName";
    private static final String PARAM_JOURNEY_RECORDING = "mskJourney";

    /**
     * JourneyManager instance.
     */
    private JourneyManager journeyManager = JourneyManagerFactory.getJourneyManager();

    /**
     * The value of the parameter for the session monitoring start.
     */
    private static final String PARAM_VALUE_START = "start";
    /**
     * The value of the parameter for the session monitoring stop.
     */
    private static final String PARAM_VALUE_STOP = "stop";

    /**
     * Returns current journey record from given request session
     * @param request request to extract journey record
     * @return record of currently running journey or
     *         null if there is no active journey present
     */
    private JourneyRecord getCurrentJourneyRecord(HttpRequestWrapper request) {

        HttpSessionWrapper session = request.getSession(false);

        if(session == null)
            return null;

        return ((JourneyRecord) request.getSession().getAttribute(SA_JOURNEY_RECORD));

    }

    /**
     * Stops tracing and returns resulting traced call
     * @return traced call of current request
     */
    private CurrentlyTracedCall endJourneyRecording() {

        TracedCall last = RunningTraceContainer.endTrace();
        RunningTraceContainer.cleanup();

        if (last instanceof NoTracedCall) {
            log.warn("Unexpectedly last is a NoTracedCall instead of CurrentlyTracedCall");
            return null;
        } else {
            CurrentlyTracedCall finishedCall = (CurrentlyTracedCall) last;
            finishedCall.setEnded();
            return finishedCall;
        }

    }


    private void startJourney(JourneyRecord record) {

        Journey journey = journeyManager.getOrCreateJourney(record.getName());
        journey.setActive(true);

    }

    @Override
    public void onRequestStarted(HttpRequestWrapper request) {

        String journeyNameFromHeader = request.getHeader(HEADER_JOURNEY_NAME);
        String journeyNameFromParameter = request.getParameter(PARAM_JOURNEY_NAME);
        String journeyCommandFromParameter = request.getParameter(PARAM_JOURNEY_RECORDING);

        boolean journeyStartedFromParams = false;

        if(journeyCommandFromParameter != null)
            switch (journeyCommandFromParameter) {

                case PARAM_VALUE_STOP:

                    try {
                        journeyManager.getJourney(journeyNameFromParameter).setActive(false);
                    } catch (NoSuchJourneyException ignore) {}

                    HttpSessionWrapper session = request.getSession(false);
                    if(session != null) {
                        JourneyRecord currentRecord = ((JourneyRecord) session.getAttribute(SA_JOURNEY_RECORD));
                        if(currentRecord != null && currentRecord.getName().equals(journeyNameFromParameter))
                        session.setAttribute(SA_JOURNEY_RECORD, null);
                    }
                    break;


                case PARAM_VALUE_START:

                    journeyStartedFromParams = true;
                    JourneyRecord record = new JourneyRecord(journeyNameFromParameter);
                    Journey journey = journeyManager.getOrCreateJourney(record.getName());
                    journey.setActive(true);
                    request.getSession().setAttribute(SA_JOURNEY_RECORD, record);

            }

        if(!journeyStartedFromParams && journeyNameFromHeader != null) {
            JourneyRecord record = new JourneyRecord(journeyNameFromHeader);
            startJourney(record);
            request.getSession().setAttribute(SA_JOURNEY_RECORD, record);
        }

        JourneyRecord currentJourneyRecord = getCurrentJourneyRecord(request);

        if(currentJourneyRecord != null) {
            String url = request.getServletPath();
            if (request.getPathInfo() != null)
                url += request.getPathInfo();
            if (request.getQueryString() != null)
                url += '?' + request.getQueryString();
            RunningTraceContainer.startTracedCall(currentJourneyRecord.getName() + "-" + url);
        }

    }

    @Override
    public void onRequestFinished(HttpRequestWrapper request, RequestResultData resultData) {

        JourneyRecord record = getCurrentJourneyRecord(request);

        if(record != null) {

            CurrentlyTracedCall tracedCall = endJourneyRecording();

            if(tracedCall != null) {
                journeyManager.getOrCreateJourney(record.getName()).addCall(tracedCall);
            }

        }

        MoSKitoContext.cleanup();

    }

    @Override
    public void configure(RequestListenerConfiguration conf) {

    }

}
