package com.thenewcircle.yamba.client;

import java.util.List;

/**
 * Public API for YambaClient
 */
public interface YambaClientInterface {

    /**
     * Post status without location.
     *
     * @param status
     * @throws YambaClientException
     */
    void postStatus(String status) throws YambaClientException;

    /**
     * Post status at location.
     *
     * @param status
     * @param latitude
     * @param longitude
     * @throws YambaClientException
     */
    void postStatus(String status, double latitude, double longitude)
            throws YambaClientException;

    /**
     * Convenience method to get a list of recent statuses.
     *
     * @param maxPosts max on length of the timeline
     * @return a list of YambaStatus objects
     * @throws YambaClientException
     */
    List<YambaStatus> getTimeline(final int maxPosts) throws YambaClientException;
}
