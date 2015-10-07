package com.thenewcircle.yamba.client;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.MediumTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static com.google.common.truth.Truth.assert_;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class ClientApiTest {

    private static final String TEST_USER = "student";
    private static final String TEST_PASS = "password";

    private static final int COUNT = 1;
    private static final int MAX = 25;

    YambaClientInterface mClient;

    @Before
    public void initTargetContext() {
        mClient = YambaClient.getClient(TEST_USER, TEST_PASS);
    }

    @Test
    public void postTimelineStatus() {
        try {
            mClient.postStatus("Yamba Automated Test");

            mClient.postStatus("Yamba Automated Location Test",
                    37.789529, -122.394193);
        } catch (YambaClientException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void getTimelineCount() {
        try {
            List<YambaStatus> list = mClient.getTimeline(COUNT);
            assert_().withFailureMessage("List size should equal " + COUNT)
                    .that(list.size())
                    .isEqualTo(COUNT);

        } catch (YambaClientException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void getTimelineComplete() {
        try {
            mClient.getTimeline(MAX);

            //Finishing with no exceptions means we passed

        } catch (YambaClientException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
