package credence.oorja.com.androidsdk;

import android.content.Context;
import android.provider.Settings;
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
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gaurav on 10/11/17.
 */

public class PushSdkClient {

    private static final String TAG = "PushSdkClient";

    private static final boolean isTest = true;
    private static final String testBaseUrl = "https://t.d2c.in/pushserver/api";

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
            baseUrl = testBaseUrl;
            isInitialised = true;
            return;
        }

        try {
            final String url = context.getString(R.string.url_config);
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

    public static void registerIdentifier(Context context, String identifier){
        Log.d(TAG, "registering identifier: " + identifier);
        Cache.save(context, "pushIdentifier", identifier);
    }

    public static void unregisterIdentifier(Context context){
        Log.d(TAG, "registering identifier");
        Cache.delete(context, "pushIdentifier");
    }

    public static void sendRegistrationTokenToServer(final Context context) {
        String identifier = Cache.get(context, "pushIdentifier");
        sendRegistrationTokenToServer(context, identifier);
    }

    public static void sendRegistrationTokenToServer(final Context context, String identifier) {
        if(!isInitialised) {
            Log.w(TAG, "SDK not initialised yet! You need to call init(Context context, String apiKey) method first");
            return;
        }
        if(identifier==null){
            String androidId = Settings.Secure.getString(context.getContentResolver(),Settings.Secure.ANDROID_ID);
            identifier = androidId;
        }
        registerIdentifier(context, identifier);
        String token = FirebaseInstanceId.getInstance().getToken();
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

    private static String getFullUrl(String relativeUrl) throws Exception {
        if(baseUrl==null)
            throw new Exception("baseUrl not initialised");
        return baseUrl + relativeUrl;
    }
}
