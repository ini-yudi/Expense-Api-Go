package com.yudi.asmara.expensereport.network;

import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class VolleyHelper {

    private static final String TAG = "VolleyHelper";
    private static final int TIMEOUT = 15000;

    public static void stringRequest(RequestQueue queue, int method, String url,
                                     final NetworkResult callback) {
        stringRequest(queue, method, url, null, callback);
    }

    public static void stringRequest(RequestQueue queue, int method, String url,
                                     final String token, final NetworkResult callback) {
        Log.d(TAG, "Request: " + method + " " + url);
        StringRequest request = new StringRequest(method, url,
                response -> {
                    Log.d(TAG, "Response: " + response);
                    callback.onSuccess(response);
                },
                error -> callback.onError(getErrorMessage(error, url))
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return getAuthHeaders(token);
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(TIMEOUT, 1, 1f));
        queue.add(request);
    }

    public static void jsonRequest(RequestQueue queue, int method, String url,
                                   final String jsonBody, final NetworkResult callback) {
        jsonRequest(queue, method, url, null, jsonBody, callback);
    }

    public static void jsonRequest(RequestQueue queue, int method, String url,
                                   final String token, final String jsonBody,
                                   final NetworkResult callback) {
        Log.d(TAG, "Request: " + method + " " + url + " body=" + jsonBody);
        StringRequest request = new StringRequest(method, url,
                response -> {
                    Log.d(TAG, "Response: " + response);
                    callback.onSuccess(response);
                },
                error -> callback.onError(getErrorMessage(error, url))
        ) {
            @Override
            public byte[] getBody() {
                return jsonBody != null ? jsonBody.getBytes() : null;
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public Map<String, String> getHeaders() {
                return getAuthHeaders(token);
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(TIMEOUT, 1, 1f));
        queue.add(request);
    }

    private static String getErrorMessage(VolleyError error, String url) {
        String msg;

        if (error.networkResponse != null) {
            NetworkResponse resp = error.networkResponse;
            String body = new String(resp.data);
            msg = "HTTP " + resp.statusCode + " (" + url + "): " + body;
            Log.e(TAG, msg);
        } else if (error instanceof TimeoutError) {
            msg = "Timeout: Server tidak merespon (" + url + ")";
            Log.e(TAG, msg);
        } else if (error.getCause() != null) {
            msg = error.getCause().getClass().getSimpleName() + ": " + error.getCause().getMessage() + " (" + url + ")";
            Log.e(TAG, msg, error);
        } else {
            msg = "Kesalahan jaringan: " + error.getClass().getSimpleName() + " (" + url + ")";
            Log.e(TAG, msg, error);
        }

        return msg;
    }

    private static Map<String, String> getAuthHeaders(String token) {
        Map<String, String> headers = new HashMap<>();
        if (token != null && !token.isEmpty()) {
            headers.put("Authorization", "Bearer " + token);
        }
        return headers;
    }
}
