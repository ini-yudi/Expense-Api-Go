package com.yudi.asmara.expensereport.network.services;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.yudi.asmara.expensereport.network.Endpoint;
import com.yudi.asmara.expensereport.network.NetworkResult;
import com.yudi.asmara.expensereport.network.VolleyHelper;
import com.yudi.asmara.expensereport.sessions.AppSession;

public class ChartService {

    private RequestQueue queue;
    private AppSession session;

    public ChartService(Context context) {
        queue = Volley.newRequestQueue(context.getApplicationContext());
        session = new AppSession(context);
    }

    public void getChartData(String tipe, int month, int year, final NetworkResult callback) {
        String url = Endpoint.getBaseUrl() + Endpoint.CHART
                + "?tipe=" + tipe
                + "&bulan=" + month
                + "&tahun=" + year;
        VolleyHelper.stringRequest(queue, Request.Method.GET, url, session.getToken(), callback);
    }
}
