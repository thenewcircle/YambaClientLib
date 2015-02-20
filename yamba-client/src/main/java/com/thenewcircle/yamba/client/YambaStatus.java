package com.thenewcircle.yamba.client;


import java.util.Date;

/**
 * YambaStatus
 */
public class YambaStatus {
    private final long id;
    private final Date createdAt;
    private final String user;
    private final String message;

    YambaStatus(long id, Date createdAt, String user, String message) {
        this.id = id;
        this.createdAt = createdAt;
        this.user = user;
        this.message = message;
    }

    /** @return the record id */
    public long getId() { return id; }

    /** @return the record creation date */
    public Date getCreatedAt() { return createdAt; }

    /** @return the record owner */
    public String getUser() { return user; }

    /** @return the message */
    public String getMessage() { return message; }
}
