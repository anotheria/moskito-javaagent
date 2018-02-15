package org.moskito.javaagent.request;

import java.io.IOException;

/**
 * Represents http request execution
 * statistics data.
 *
 * Holds execution time and
 * request execution error, if they present.
 */
public class RequestResultData {

    /**
     * Request duration in nanoseconds
     */
    private long duration;

    /**
     * Exception that has been thrown out
     * while executing the request
     */
    private Throwable exception;

    /**
     * Type of thrown exception
     * {@see {@link ExceptionKind}}
     */
    private ExceptionKind exceptionKind = ExceptionKind.NONE;

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public Throwable getException() {
        return exception;
    }

    public ExceptionKind getExceptionKind() {
        return exceptionKind;
    }

    public void setIOException(IOException e) {
        exception = e;
        exceptionKind = ExceptionKind.IO;
    }

    public void setServletException(Exception e) {
        exception = e;
        exceptionKind = ExceptionKind.SERVLET;
    }

    public void setRuntimeException(RuntimeException e) {
        exception = e;
        exceptionKind = ExceptionKind.RUNTIME;
    }

    public void setError(Error e) {
        exception = e;
        exceptionKind = ExceptionKind.ERROR;
    }

    /**
     * Represents type of exception
     * that been thrown while processing request
     */
    public enum ExceptionKind {

        /**
         * javax.servlet.ServletException and it subclasses
         */
        SERVLET,
        /**
         * {@link java.io.IOException} and it subclasses
         */
        IO,
        /**
         * {@link java.lang.RuntimeException} and it subclasses
         */
        RUNTIME,
        /**
         * {@link java.lang.RuntimeException} and it subclasses
         */
        ERROR,
        /**
         * No any exception
         */
        NONE

    }

}
