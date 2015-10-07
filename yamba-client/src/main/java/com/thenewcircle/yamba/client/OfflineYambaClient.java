package com.thenewcircle.yamba.client;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Stubbed client implementation for use in hermetic test cases
 */
public class OfflineYambaClient implements YambaClientInterface {

    private final AtomicInteger idCounter = new AtomicInteger(1);
    private final List<YambaStatus> statuses = new ArrayList<>();

    public static YambaClientInterface newClient() {
        return new OfflineYambaClient();
    }

    private OfflineYambaClient() {
        //Pre-load dummy entries
        try {
            postStatus("NewCircle Android");
            postStatus("Yamba Test Message");
        } catch (YambaClientException e) {
            e.printStackTrace();
        }
    }

    /* All posts are added to the internal in-memory list */

    @Override
    public void postStatus(String status) throws YambaClientException {
        postStatus(status, 0.0, 0.0);
    }

    @Override
    public void postStatus(String status, double latitude, double longitude)
            throws YambaClientException {
        synchronized (this) {
            statuses.add(new YambaStatus(idCounter.getAndIncrement(),
                    Calendar.getInstance().getTime(),"Offline Test User", status));
        }
    }

    /* Timeline requests simply return the last N items */

    @Override
    public List<YambaStatus> getTimeline(int maxPosts)
            throws YambaClientException {
        ArrayList<YambaStatus> result = new ArrayList<>(maxPosts);

        synchronized (this) {
            if (statuses.size() > maxPosts) {
                //Return the requested amount
                result.addAll(statuses.subList(
                        statuses.size() - maxPosts, statuses.size()));
            } else {
                //For a small list, return the whole thing
                result.addAll(statuses);
            }
        }

        return result;
    }
}
