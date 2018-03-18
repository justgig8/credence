package credence.oorja.com.androidsdk;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gaurav on 10/11/17.
 */

public class PushSdkClient {

    private static final String TAG = "PushSdkClient";
    private static final String URL_METAINFO = "https://g.d2c.in/datamanager/api/config/get";

    private static final boolean isTest = true;

    private static boolean isInitialised;
    private static String apiKey;
    private static String baseUrl;

    public static synchronized void init(final Context context, final String apiKey){
        if(isInitialised) {
            Log.w(TAG, "already initialised, no need to call this method more than once");
            return;
        }
        if(apiKey==null || apiKey.isEmpty()){
            Log.w(TAG, "apiKey can not be null or blank");
            return;
        }
        PushSdkClient.apiKey = apiKey;

        if(isTest){
            baseUrl = "https://g.d2c.in/pushserver/api";
            isInitialised = true;
            return;
        }

        try {
            final String url = URL_METAINFO;
            Log.d(TAG, "url: " + url);

            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d(TAG, "Response: " + response);
                            try {
                                JSONObject jsonObj = new JSONObject(response);
                                baseUrl = jsonObj.getString("baseUrl");
                                isInitialised = true;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(TAG, "That didn't work!");
                            Toast.makeText(context, "Error: " + error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                     }) {};
            RequestQueue queue = Volley.newRequestQueue(context);
            queue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendRegistrationTokenToServer(final Context context, final String token) {
        sendRegistrationTokenToServer(context, token, null);
    }

    public static void sendRegistrationTokenToServer(final Context context, final String token, final String identifier) {
        if(!isInitialised) {
            Log.w(TAG, "SDK not initialised yet! You need to call init(Context context, String apiKey) method first");
            return;
        }
        try {
            if (token == null) {
                Log.w(TAG, "token null, not sending to server");
                return;
            }
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("token", token);
            if (identifier != null)
                jsonBody.put("identifier", identifier);
            jsonBody.put("appId", context.getPackageName());
            jsonBody.put("appVersionCode", context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode);
            jsonBody.put("appVersionName", context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName);
            jsonBody.put("platform", "android");
            final String mRequestBody = jsonBody.toString();
            final String url = getFullUrl(context.getString(R.string.url_register));
            Log.d(TAG, "url: " + url + ", request body: " + mRequestBody);

            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // Display the first 500 characters of the response string.
                            Log.d(TAG, "Response: " + response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "That didn't work!");
                    Toast.makeText(context, "Error: " + error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }) {

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map map = super.getHeaders();
                    Map headers = new HashMap(map);
                    headers.put("Authorization", "Basic " + apiKey);
                    return headers;
                }

                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                        return null;
                    }
                }
            };
            RequestQueue queue = Volley.newRequestQueue(context);
            queue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void publishEventToServer(final Context context, final String notificationId, final int eventId) {
        if(!isInitialised) {
            Log.w(TAG, "SDK not initialised yet! You need to call init(Context context, String apiKey) method first");
            return;
        }
        final String event = context.getString(eventId);
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("notificationId", notificationId);
            jsonBody.put("event", event);
            jsonBody.put("timestamp", System.currentTimeMillis());
            final String mRequestBody = jsonBody.toString();
            final String url = getFullUrl(context.getString(R.string.url_publish_event));
            Log.d(TAG, "url: " + url + ", request body: " + mRequestBody);

            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // Display the first 500 characters of the response string.
                            Log.d(TAG, "Response: " + response);
                            Toast.makeText(context, "event published: " + event, Toast.LENGTH_SHORT).show();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "That didn't work!");
                    Toast.makeText(context, "Error: " + error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }) {

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map map = super.getHeaders();
                    Map headers = new HashMap(map);
                    headers.put("Authorization", "Basic " + apiKey);
                    return headers;
                }

                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                        return null;
                    }
                }
            };
            RequestQueue queue = Volley.newRequestQueue(context);
            queue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void requestPushNotification(final Context context, String identifier, String appId, String appServerKey, String title, String body, String sound, String image) {
        if(!isInitialised) {
            Log.w(TAG, "SDK not initialised yet! You need to call init(Context context, String apiKey) method first");
            return;
        }
        try {
            Log.d(TAG, "requesting for push notification..");
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("appServerKey", appServerKey);
            jsonBody.put("title", title);
            jsonBody.put("identifier", identifier);
            jsonBody.put("appId", appId);
//            jsonBody.put("appVersionCode", getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
//            jsonBody.put("appVersionName", getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
            jsonBody.put("body", body);
            jsonBody.put("sound", sound);
            jsonBody.put("image", image);
            final String mRequestBody = jsonBody.toString();
            final String url = getFullUrl(context.getString(R.string.url_request_push));
            Log.d(TAG, "url: " + url + ", request body: " + mRequestBody);

            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // Display the first 500 characters of the response string.
                            Log.d(TAG, "Response: " + response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "That didn't work!" + error);
                    Toast.makeText(context, "Error: " + error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }) {

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map map = super.getHeaders();
                    Map headers = new HashMap(map);
                    headers.put("Authorization", "Basic " + apiKey);
                    return headers;
                }

                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                        return null;
                    }
                }
            };
            RequestQueue queue = Volley.newRequestQueue(context);
            queue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getFullUrl(String relativeUrl) throws Exception {
        if(baseUrl==null)
            throw new Exception("baseUrl not initialised");
        return baseUrl + relativeUrl;
    }
}
