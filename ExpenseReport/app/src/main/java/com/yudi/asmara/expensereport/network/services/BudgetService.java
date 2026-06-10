package com.yudi.asmara.expensereport.network.services;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.yudi.asmara.expensereport.network.Endpoint;
import com.yudi.asmara.expensereport.network.NetworkResult;
import com.yudi.asmara.expensereport.network.VolleyHelper;
import com.yudi.asmara.expensereport.sessions.AppSession;

public class BudgetService {

    private RequestQueue queue;
    private AppSession session;

    public BudgetService(Context context) {
        queue = Volley.newRequestQueue(context.getApplicationContext());
        session = new AppSession(context);
    }

    public void getAll(final NetworkResult callback) {
        String url = Endpoint.getBaseUrl() + Endpoint.BUDGETS;
        VolleyHelper.stringRequest(queue, Request.Method.GET, url, session.getToken(), callback);
    }

    public void save(String body, final NetworkResult callback) {
        String url = Endpoint.getBaseUrl() + Endpoint.BUDGET;
        VolleyHelper.jsonRequest(queue, Request.Method.POST, url, session.getToken(), body, callback);
    }
}
