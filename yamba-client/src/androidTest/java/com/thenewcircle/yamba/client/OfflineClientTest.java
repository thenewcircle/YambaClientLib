package com.thenewcircle.yamba.client;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.MediumTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assert_;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class OfflineClientTest {

    private static final int COUNT = 1;

    YambaClientInterface mClient;

    @Before
    public void initTargetContext() {
        YambaClient.setClientInstance(OfflineYambaClient.newClient());
        mClient = YambaClient.getClient(null, null);
    }

    @Test
    public void clientIsOfflineInstance() {
        assertThat(YambaClient.getClient(null, null))
                .isInstanceOf(OfflineYambaClient.class);
    }

    @Test
    public void postIsAppendedToEnd() {
        try {
            String statusMessage = "Yamba Automated Test";
            mClient.postStatus(statusMessage);

            List<YambaStatus> list = mClient.getTimeline(1);
            YambaStatus status = list.get(0);

            assertThat(status.getMessage())
                    .named("status message")
                    .isEqualTo(statusMessage);

        } catch (YambaClientException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void getTimelineCount() {
        try {
            List<YambaStatus> list = mClient.getTimeline(COUNT);
            assert_().withFailureMessage("List size should be < " + COUNT)
                    .that(list.size())
                    .isAtMost(COUNT);

        } catch (YambaClientException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
