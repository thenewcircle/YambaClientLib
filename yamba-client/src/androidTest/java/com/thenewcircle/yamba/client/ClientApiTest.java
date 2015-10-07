package com.thenewcircle.yamba.client;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.MediumTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void clientAcceptsCredentialsChange() throws YambaClientException {
        YambaClientInterface client = YambaClient.getClient(
                TEST_USER, TEST_PASS);
        //This version should succeed
        client.getTimeline(COUNT);

        client = YambaClient.getClient("fake", "fake");
        //This version should fail
        exceptionRule.expect(YambaClientException.class);
        client.getTimeline(COUNT);
    }

    @Test
    public void postTimelineStatus() {
        YambaClientInterface client = YambaClient.getClient(
                TEST_USER, TEST_PASS);
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
        YambaClientInterface client = YambaClient.getClient(
                TEST_USER, TEST_PASS);
        try {
            List<YambaStatus> list = client.getTimeline(COUNT);
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
        YambaClientInterface client = YambaClient.getClient(
                TEST_USER, TEST_PASS);
        try {
            client.getTimeline(MAX);

            //Finishing with no exceptions means we passed

        } catch (YambaClientException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
