package com.yudi.asmara.expensereport.network.services;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.yudi.asmara.expensereport.network.Endpoint;
import com.yudi.asmara.expensereport.network.NetworkResult;
import com.yudi.asmara.expensereport.network.VolleyHelper;
import com.yudi.asmara.expensereport.sessions.AppSession;

public class DashboardService {

    private RequestQueue queue;
    private AppSession session;

    public DashboardService(Context context) {
        queue = Volley.newRequestQueue(context.getApplicationContext());
        session = new AppSession(context);
    }

    public void getDashboard(final NetworkResult callback) {
        String url = Endpoint.getBaseUrl() + Endpoint.DASHBOARD;
        VolleyHelper.stringRequest(queue, Request.Method.GET, url, session.getToken(), callback);
    }
}
