
package com.thenewcircle.yamba.client;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import org.apache.http.conn.ConnectTimeoutException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

/**
 * YambaClient
 */
public final class YambaClient implements YambaClientInterface {
    /**
     * The default Yamba service
     */
    public static final String DEFAULT_API_ROOT = "http://yamba.newcircle.com/api";

    /**
     * Created at format
     */
    public static final String DATE_FORMAT_PATTERN = "EEE MMM dd HH:mm:ss Z yyyy";

    private static final String TAG = "YambaClient";
    private static int DEFAULT_TIMEOUT = 60000;
    private static final String DEFAULT_USER_AGENT = "YambaClient/2.0";

    /**
     * TimelineProcessor
     */
    public static interface TimelineProcessor {
        /**
         * @return true if the processor can accept more data
         */
        public boolean isRunnable();

        /**
         * Called before the first entry in the timeline
         */
        public void onStartProcessingTimeline();

        /**
         * Called after the last entry in the timeline
         */
        public void onEndProcessingTimeline();

        /**
         * @param id        the unique id for the status message
         * @param createdAt creation time for the status message
         * @param user      user posting the status message
         * @param msg       the text of the status message
         */
        public void onTimelineStatus(long id, Date createdAt, String user, String msg);
    }

    private final String username;
    private final String password;
    private final String defaultCharSet;
    private final String apiRoot;

    /* Singleton injected instance */
    private static YambaClientInterface sClientInstance;

    /**
     * Return a new client for the default endpoint
     * @param username Account username
     * @param password Account password
     */
    public static synchronized YambaClientInterface getClient(String username,
                                                              String password) {
        return getClient(username, password, null);
    }

    /**
     * Return a new client for the default endpoint
     * @param username Account username
     * @param password Account password
     * @param apiRoot Custom API endpoint URL
     */
    public static synchronized YambaClientInterface getClient(String username,
                                                              String password,
                                                              String apiRoot) {
        if (sClientInstance == null) {
            return new YambaClient(username, password, apiRoot);
        }

        return sClientInstance;
    }

    /**
     * Inject a client instance. For use in automated test cases.
     */
    public static synchronized void setClientInstance(YambaClientInterface client) {
        sClientInstance = client;
    }

    private YambaClient(String username, String password, String apiRoot) {
        if (TextUtils.isEmpty(username)) {
            throw new IllegalArgumentException("Username must not be blank");
        }
        this.username = username;

        if (TextUtils.isEmpty(password)) {
            throw new IllegalArgumentException("Password must not be blank");
        }
        this.password = password;

        if (TextUtils.isEmpty(apiRoot)) {
            apiRoot = DEFAULT_API_ROOT;
        }
        try {
            URL url = new URL(apiRoot);
            this.apiRoot = apiRoot;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid API Root: " + apiRoot);
        }

        this.defaultCharSet = Charset.defaultCharset().displayName();
    }

    @Override
    public void postStatus(String status) throws YambaClientException {
        postStatus(status, Double.NaN, Double.NaN);
    }

    @Override
    public void postStatus(String status, double latitude, double longitude)
            throws YambaClientException {
        try {
            URL endpoint = this.getUri("/statuses/update.xml");
            HashMap<String, String> postParams = new HashMap<>(3);
            postParams.put("status", status);
            if (-90.00 <= latitude && latitude <= 90.00
                    && -180.00 <= longitude && longitude <= 180.00) {
                postParams.put("lat", String.valueOf(latitude));
                postParams.put("long", String.valueOf(longitude));
            }

            HttpURLConnection connection = getConnection(endpoint);
            String postBody = getFormBody(postParams);
            try {
                Log.d(TAG, "Submitting " + postParams + " to " + endpoint);
                this.attachBasicAuthentication(connection, this.username, this.password);
                connection.setDoOutput(true);
                connection.connect();

                //Write the form data
                OutputStream output = connection.getOutputStream();
                try {
                    output.write(postBody.getBytes(this.defaultCharSet));
                    output.flush();
                } finally {
                    if (output != null) {
                        output.close();
                    }
                }

                //Verify response
                this.checkResponse(connection);
            } finally {
                connection.disconnect();
            }
        } catch (Exception e) {
            throw translateException(e);
        }
    }

    @Override
    public List<YambaStatus> getTimeline(final int maxPosts) throws YambaClientException {
        final List<YambaStatus> statuses = new ArrayList<YambaStatus>();

        fetchFriendsTimeline(
                new TimelineProcessor() {
                    @Override
                    public boolean isRunnable() {
                        return statuses.size() < maxPosts;
                    }

                    @Override
                    public void onStartProcessingTimeline() {
                    }

                    @Override
                    public void onEndProcessingTimeline() {
                    }

                    @Override
                    public void onTimelineStatus(long id, Date createdAt, String user, String msg) {
                        statuses.add(new YambaStatus(id, createdAt, user, msg));
                    }
                });

        return statuses;
    }

    /**
     * Fetch the friends timeline.
     *
     * @param hdlr callback handler for each status
     * @throws YambaClientException
     */
    private void fetchFriendsTimeline(TimelineProcessor hdlr)
            throws YambaClientException {
        long t = System.currentTimeMillis();
        try {
            URL endpoint = this.getUri("/statuses/friends_timeline.xml");
            HttpURLConnection connection = this.getConnection(endpoint);
            try {
                Log.d(TAG, "Getting " + endpoint);
                this.attachBasicAuthentication(connection, this.username, this.password);
                connection.setDoInput(true);
                connection.connect();
                //Verify response
                this.checkResponse(connection);

                //Pull and parse the timeline
                XmlPullParser xpp = this.getXmlPullParser();
                InputStream in = connection.getInputStream();
                try {
                    parseStatus(xpp, in, hdlr);
                } finally {
                    in.close();
                }
            } finally {
                connection.disconnect();
            }
        } catch (Exception e) {
            throw translateException(e);
        }
        t = System.currentTimeMillis() - t;
        Log.d(TAG, "Fetched timeline in " + t + " ms");
    }

    private void checkResponse(HttpURLConnection connection)
            throws YambaClientException, IOException {
        int responseCode = connection.getResponseCode();
        String reason = connection.getResponseMessage();
        switch (responseCode) {
            case 200:
                return;
            case 401:
                throw new YambaClientUnauthorizedException(reason);
            default:
                throw new YambaClientException("Unexpected response ["
                        + responseCode + "] while posting update: " + reason);
        }
    }

    private boolean endsWithTags(Stack<String> stack, String tag1, String tag2) {
        int s = stack.size();
        return s >= 2 && tag1.equals(stack.get(s - 2))
                && tag2.equals(stack.get(s - 1));
    }

    private HttpURLConnection getConnection(URL endpoint) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) endpoint.openConnection();
        connection.setReadTimeout(DEFAULT_TIMEOUT);
        connection.setConnectTimeout(DEFAULT_TIMEOUT);
        connection.setRequestProperty("User-Agent", DEFAULT_USER_AGENT);

        return connection;
    }

    private void attachBasicAuthentication(URLConnection connection,
            String username, String password) {
        //Add Basic Authentication Headers
        String userpassword = username + ":" + password;
        String encodedAuthorization = Base64.encodeToString(
                userpassword.getBytes(), Base64.NO_WRAP);
        connection.setRequestProperty("Authorization", "Basic "
                + encodedAuthorization);
    }

    private String getFormBody(HashMap<String, String> formData) throws UnsupportedEncodingException {
        if (formData == null) {
            return null;
        }
        List<String> params = new ArrayList<>();
        for (String key : formData.keySet()) {
            String value = formData.get(key);
            params.add(URLEncoder.encode(key, this.defaultCharSet)
                    + "="
                    + URLEncoder.encode(value, this.defaultCharSet));
        }
        return TextUtils.join("&", params);
    }

    private URL getUri(String relativePath) throws MalformedURLException {
        return new URL(apiRoot + relativePath);
    }

    private XmlPullParser getXmlPullParser() throws YambaClientException {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(false);
            return factory.newPullParser();
        } catch (Exception e) {
            throw new YambaClientException("Failed to create parser", e);
        }
    }

    private void parseStatus(XmlPullParser xpp, InputStream in, TimelineProcessor hdlr)
            throws XmlPullParserException, IOException, ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN);

        long id = -1;
        Date createdAt = null;
        String user = null;
        String message = null;

        xpp.setInput(in, "UTF-8");
        Stack<String> stack = new Stack<String>();
        Log.d(TAG, "Parsing timeline");
        for (int eventType = xpp.getEventType();
             eventType != XmlPullParser.END_DOCUMENT && hdlr.isRunnable();
             eventType = xpp.next()) {
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    hdlr.onStartProcessingTimeline();
                    break;
                case XmlPullParser.START_TAG:
                    stack.push(xpp.getName());
                    break;
                case XmlPullParser.END_TAG:
                    if ("status".equals(stack.pop())) {
                        hdlr.onTimelineStatus(id, createdAt, user, message);
                        id = -1;
                        createdAt = null;
                        user = null;
                        message = null;
                    }
                    break;
                case XmlPullParser.TEXT:
                    String text = xpp.getText();
                    if (endsWithTags(stack, "status", "id")) {
                        id = Long.parseLong(text);
                    } else if (endsWithTags(stack, "status", "created_at")) {
                        createdAt = dateFormat.parse(text);
                    } else if (endsWithTags(stack, "status", "text")) {
                        message = text;
                    } else if (endsWithTags(stack, "user", "name")) {
                        user = text;
                    }
                    break;
            } // switch
        } // for
        hdlr.onEndProcessingTimeline();
        Log.d(TAG, "Finished parsing timeline");
    }

    private YambaClientException translateException(Exception e) {
        if (e instanceof YambaClientException) {
            return (YambaClientException) e;
        } else if (e instanceof ConnectTimeoutException) {
            return new YambaClientTimeoutException(
                    "Timeout while communicating to" + this.apiRoot, e);
        } else if (e instanceof IOException) {
            return new YambaClientIOException(
                    "I/O error while communicating to" + this.apiRoot, e);
        } else {
            return new YambaClientException(
                    "Unexpected error while communicating to" + this.apiRoot, e);
        }
    }
}
