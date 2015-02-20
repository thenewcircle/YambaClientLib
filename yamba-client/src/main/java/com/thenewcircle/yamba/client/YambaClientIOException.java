
package com.thenewcircle.yamba.client;

public class YambaClientIOException extends YambaClientException {

    private static final long serialVersionUID = -3792023133642909075L;

    public YambaClientIOException(String detailMessage) {
        super(detailMessage);
    }

    public YambaClientIOException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
