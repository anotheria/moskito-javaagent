package org.moskito.javaagent.request;

public class RequestFinishDTO {

    private String uri;
    private long duration;

    private Throwable ioException;
    private Throwable servletException;
    private Throwable runtimeException;
    private Throwable otherException;


    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public Throwable getIoException() {
        return ioException;
    }

    public void setIoException(Throwable ioException) {
        this.ioException = ioException;
    }

    public Throwable getServletException() {
        return servletException;
    }

    public void setServletException(Throwable servletException) {
        this.servletException = servletException;
    }

    public Throwable getRuntimeException() {
        return runtimeException;
    }

    public void setRuntimeException(Throwable runtimeException) {
        this.runtimeException = runtimeException;
    }

    public Throwable getOtherException() {
        return otherException;
    }

    public void setOtherException(Throwable otherException) {
        this.otherException = otherException;
    }

}