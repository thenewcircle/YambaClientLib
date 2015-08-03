package com.thenewcircle.yamba.client;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.MediumTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class ClientApiTest {

    private static final String TEST_USER = "student";
    private static final String TEST_PASS = "password";


    private static final int COUNT = 1;
    private static final int MAX = 25;

    @Before
    public void initTargetContext() {
        //TODO: Initialize client for multiple tests?
    }

    @Test
    public void postTimelineStatus() {
        YambaClient client = new YambaClient(TEST_USER, TEST_PASS);
        try {
            client.postStatus("Yamba Automated Test");

            client.postStatus("Yamba Automated Location Test",
                    37.789529, -122.394193);
        } catch (YambaClientException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void getTimelineCount() {
        YambaClient client = new YambaClient(TEST_USER, TEST_PASS);
        try {
            List<YambaStatus> list = client.getTimeline(COUNT);
            assertEquals("List size should equal " + COUNT,
                    list.size(), COUNT);

        } catch (YambaClientException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void getTimelineComplete() {
        YambaClient client = new YambaClient(TEST_USER, TEST_PASS);
        try {
            client.getTimeline(MAX);

            //Finishing with no exceptions means we passed

        } catch (YambaClientException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
