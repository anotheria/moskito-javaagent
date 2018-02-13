package org.moskito.javaagent.request;

import java.io.IOException;

public class RequestResultData {

    private long duration;
    private Throwable exception;
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

    public enum ExceptionKind {

        SERVLET,
        IO,
        RUNTIME,
        ERROR,
        NONE

    }

}
