package com.yudi.asmara.expensereport.network.services;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.yudi.asmara.expensereport.network.Endpoint;
import com.yudi.asmara.expensereport.network.NetworkResult;
import com.yudi.asmara.expensereport.network.VolleyHelper;
import com.yudi.asmara.expensereport.sessions.AppSession;

public class CategoryService {

    private RequestQueue queue;
    private AppSession session;

    public CategoryService(Context context) {
        queue = Volley.newRequestQueue(context.getApplicationContext());
        session = new AppSession(context);
    }

    public void getAll(final NetworkResult callback) {
        String url = Endpoint.getBaseUrl() + Endpoint.CATEGORIES;
        VolleyHelper.stringRequest(queue, Request.Method.GET, url, session.getToken(), callback);
    }

    public void getById(int id, final NetworkResult callback) {
        String url = Endpoint.getBaseUrl() + Endpoint.CATEGORIES + "/" + id;
        VolleyHelper.stringRequest(queue, Request.Method.GET, url, session.getToken(), callback);
    }

    public void create(String jsonBody, final NetworkResult callback) {
        String url = Endpoint.getBaseUrl() + Endpoint.CATEGORIES;
        VolleyHelper.jsonRequest(queue, Request.Method.POST, url, session.getToken(), jsonBody, callback);
    }

    public void update(int id, String jsonBody, final NetworkResult callback) {
        String url = Endpoint.getBaseUrl() + Endpoint.CATEGORIES + "/" + id;
        VolleyHelper.jsonRequest(queue, Request.Method.PUT, url, session.getToken(), jsonBody, callback);
    }

    public void delete(int id, final NetworkResult callback) {
        String url = Endpoint.getBaseUrl() + Endpoint.CATEGORIES + "/" + id;
        VolleyHelper.stringRequest(queue, Request.Method.DELETE, url, session.getToken(), callback);
    }
}
