package org.moskito.javaagent.request.dto;

/**
 * Contains http request data
 * with additional request execution statistics
 */
public class RequestExecutionResultDTO extends RequestDTO{

    /**
     * Request execution duration in nanoseconds
     */
    private long duration;

    /**
     * Exception that been thrown due request
     * execution. null if no exceptions been thrown.
     */
    private Throwable exception;
    /**
     * Type of an exception that been thrown due request
     * execution.
     */
    private ExceptionKind exceptionKind = ExceptionKind.NONE;

    /**
     * Copies request data from given dto object
     * @param source source of request data
     */
    public RequestExecutionResultDTO(RequestDTO source) {
        this.setUri(source.getUri());
        this.setDomain(source.getDomain());
        this.setMethod(source.getMethod());
        this.setReferrer(source.getReferrer());
        this.setUserAgent(source.getUserAgent());
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public Throwable getException() {
        return exception;
    }

    public void setRuntimeException(Throwable e) {
        this.exception = e;
        this.exceptionKind = ExceptionKind.RUNTIME;
    }

    public void setIOException(Throwable e) {
        this.exception = e;
        this.exceptionKind = ExceptionKind.IO;
    }

    public void setServletException(Throwable e) {
        this.exception = e;
        this.exceptionKind = ExceptionKind.SERVLET;
    }

    public void setOtherException(Throwable e) {
        this.exception = e;
        this.exceptionKind = ExceptionKind.OTHER;
    }

    public ExceptionKind getExceptionKind() {
        return exceptionKind;
    }

    public enum ExceptionKind {
        /**
         * Represents {@link java.io.IOException}
         */
        IO,
        /**
         * Represents {@link javax.servlet.ServletException}
         */
        SERVLET,
        /**
         * Represents {@link RuntimeException}
         */
        RUNTIME,
        /**
         * Represent other (than any types above) kind of exception
         */
        OTHER,
        /**
         * No exceptions been throw due request execution
         */
        NONE
    }

}