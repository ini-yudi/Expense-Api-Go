package com.yudi.asmara.expensereport.network.services;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.yudi.asmara.expensereport.network.Endpoint;
import com.yudi.asmara.expensereport.network.NetworkResult;
import com.yudi.asmara.expensereport.network.VolleyHelper;
import com.yudi.asmara.expensereport.sessions.AppSession;

public class ExportService {

    private RequestQueue queue;
    private AppSession session;

    public ExportService(Context context) {
        queue = Volley.newRequestQueue(context.getApplicationContext());
        session = new AppSession(context);
    }

    public void exportCSV(String tipe, String dateFrom, String dateTo, final NetworkResult callback) {
        String url = Endpoint.getBaseUrl() + Endpoint.EXPORT
                + "?format=csv"
                + "&tipe=" + tipe
                + "&date_from=" + dateFrom
                + "&date_to=" + dateTo;
        VolleyHelper.stringRequest(queue, Request.Method.GET, url, session.getToken(), callback);
    }

    public void exportCSV(String tipe, String search, final NetworkResult callback) {
        String url = Endpoint.getBaseUrl() + Endpoint.EXPORT
                + "?format=csv"
                + "&tipe=" + tipe
                + "&search=" + search;
        VolleyHelper.stringRequest(queue, Request.Method.GET, url, session.getToken(), callback);
    }
}
