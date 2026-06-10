package com.yudi.asmara.expensereport.network.services;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.yudi.asmara.expensereport.network.Endpoint;
import com.yudi.asmara.expensereport.network.NetworkResult;
import com.yudi.asmara.expensereport.network.VolleyHelper;

import org.json.JSONException;
import org.json.JSONObject;

public class AuthService {

    private RequestQueue queue;

    public AuthService(Context context) {
        queue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public void login(String username, String password, final NetworkResult callback) {
        String url = Endpoint.getBaseUrl() + Endpoint.LOGIN;
        try {
            JSONObject body = new JSONObject();
            body.put("username", username);
            body.put("password", password);
            VolleyHelper.jsonRequest(queue, Request.Method.POST, url, null, body.toString(), callback);
        } catch (JSONException e) {
            callback.onError("Gagal memproses data");
        }
    }

    public void register(String username, String password, final NetworkResult callback) {
        String url = Endpoint.getBaseUrl() + Endpoint.REGISTER;
        try {
            JSONObject body = new JSONObject();
            body.put("username", username);
            body.put("password", password);
            VolleyHelper.jsonRequest(queue, Request.Method.POST, url, null, body.toString(), callback);
        } catch (JSONException e) {
            callback.onError("Gagal memproses data");
        }
    }
}
