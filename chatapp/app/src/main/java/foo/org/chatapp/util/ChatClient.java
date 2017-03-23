package foo.org.chatapp.util;

import android.content.Context;

import com.loopj.android.http.*;

import java.security.MessageDigest;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

/**
 * Created by enoks on 22.1.2017.
 */

public class ChatClient {

    public interface ChatRestClientHandler
    {
        void success(String data);
        void fail(Throwable error, String frienlyErrorMsg);
    }

    private static final String SERVER_URL = "http://10.0.2.2:3000";
    private static final String BASE_URL = SERVER_URL + "/chatserver/";
    private static final String IMAGE_URL = SERVER_URL + "/images/";
    private AsyncHttpClient client = new AsyncHttpClient();
    private static ChatClient instance;
    private String username = "", password = "";

    private ChatClient()
    {}

    public static ChatClient getInstance() {
        synchronized (ChatClient.class){
            if (instance == null) {
                instance = new ChatClient();
            }
        }
        return instance;
    }

    private void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    private void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private void postEntity(Context context, String url, String json, AsyncHttpResponseHandler responseHandler) throws Exception {
        client.post(context,  getAbsoluteUrl(url), new StringEntity(json), "application/json", responseHandler);
    }


    private String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

    public void setCredentials(String username, String password)
    {
        this.username = username;
        this.password = password;
    }

    public String getImageUrl() {
        return IMAGE_URL;
    }

    public void login(final ChatRestClientHandler handler) {

        client.setBasicAuth(username, md5(password));

        get("login", null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                handler.success(new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                handler.fail(error, "error");
            }
        });
    }

    public void getChannels(final ChatRestClientHandler handler) {

        client.setBasicAuth(username, md5(password));

        get("channels", null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                handler.success(new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                handler.fail(error, "error");
            }
        });
    }

    public void getChannelMessages(String channelGUID, final ChatRestClientHandler handler) {

        client.setBasicAuth(username, md5(password));

        get("messages/" + channelGUID, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                handler.success(new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                handler.fail(error, "error");
            }
        });
    }

    public void postMessage(Context context, String channelGUID, String json, final ChatRestClientHandler handler) {
        client.setBasicAuth(username, md5(password));

        try {
            postEntity(context, "publish/" + channelGUID, json, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    handler.success(new String(responseBody));
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    handler.fail(error, "error");
                }
            });
        } catch (Exception e) {
            handler.fail(e, e.toString());
        }
    }

    private String md5(final String toHash) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("md5");
            digest.update(toHash.getBytes());
            final byte[] bytes = digest.digest();
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(String.format("%02X", bytes[i]));
            }
            return sb.toString().toLowerCase();
        } catch (Exception exc) {
            return ""; // Impossibru!
        }
    }
}
