
package com.thenewcircle.yamba.client;

public class YambaClientTimeoutException extends YambaClientIOException {

    private static final long serialVersionUID = -3792023133642909075L;

    public YambaClientTimeoutException(String detailMessage) {
        super(detailMessage);
    }

    public YambaClientTimeoutException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
