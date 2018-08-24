package com.oorja.credence;

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

public class NewsServerClient {

    private static final String TAG = "NewsServerClient";

    public static boolean subscribe(final Context context, final String category, final String customerId) {
        try {
            if (category == null) {
                Log.e(TAG, "category null, not sending to server");
                return false;
            }
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("category", category);
            jsonBody.put("customer_id", customerId);
            jsonBody.put("appId", context.getPackageName());
            jsonBody.put("platform", "android");
            final String mRequestBody = jsonBody.toString();
            final String url = context.getString(R.string.url_newscategory_subscribe);
            Log.d(TAG, "url: " + url + ", request body: " + mRequestBody);

            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // Display the first 500 characters of the response string.
                            Log.d(TAG, "Response: " + response);
                            CacheUtils.updateSubscriptionStatus(context, category, true);
                            Toast.makeText(context, "Subscribed to category: " + category, Toast.LENGTH_SHORT).show();
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
                    headers.put("Authorization", "Basic bWF4YnVwYTp3a3IyNTdsdXd5cWVwZG5w");
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
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean unsubscribe(final Context context, final String category, final String customerId) {
        try {
            if (category == null) {
                Log.e(TAG, "category null, not sending to server");
                return false;
            }
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("category", category);
            jsonBody.put("customer_id", customerId);
            jsonBody.put("appId", context.getPackageName());
            jsonBody.put("platform", "android");
            final String mRequestBody = jsonBody.toString();
            final String url = context.getString(R.string.url_newscategory_unsubscribe);
            Log.d(TAG, "url: " + url + ", request body: " + mRequestBody);

            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // Display the first 500 characters of the response string.
                            Log.d(TAG, "Response: " + response);
                            CacheUtils.updateSubscriptionStatus(context, category, false);
                            Toast.makeText(context, "Unsubscribed from category: " + category, Toast.LENGTH_SHORT).show();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "That didn't work!");
                    Toast.makeText(context, "Error: " + error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map map = super.getHeaders();
                    Map headers = new HashMap(map);
                    headers.put("Authorization", "Basic bWF4YnVwYTp3a3IyNTdsdXd5cWVwZG5w");
                    return headers;
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
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
