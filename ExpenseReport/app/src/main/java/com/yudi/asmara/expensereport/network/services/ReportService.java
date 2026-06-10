package com.yudi.asmara.expensereport.network.services;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.yudi.asmara.expensereport.network.Endpoint;
import com.yudi.asmara.expensereport.network.NetworkResult;
import com.yudi.asmara.expensereport.network.VolleyHelper;
import com.yudi.asmara.expensereport.sessions.AppSession;

public class ReportService {

    private RequestQueue queue;
    private AppSession session;

    public ReportService(Context context) {
        queue = Volley.newRequestQueue(context.getApplicationContext());
        session = new AppSession(context);
    }

    public void getDaily(String date, final NetworkResult callback) {
        String url = Endpoint.getBaseUrl() + Endpoint.REPORT_DAILY + "?date=" + date;
        VolleyHelper.stringRequest(queue, Request.Method.GET, url, session.getToken(), callback);
    }

    public void getMonthly(String month, String year, final NetworkResult callback) {
        String url = Endpoint.getBaseUrl() + Endpoint.REPORT_MONTHLY + "?month=" + month + "&year=" + year;
        VolleyHelper.stringRequest(queue, Request.Method.GET, url, session.getToken(), callback);
    }

    public void getStats(String startDate, String endDate, final NetworkResult callback) {
        String url = Endpoint.getBaseUrl() + Endpoint.REPORT_STATS + "?start_date=" + startDate + "&end_date=" + endDate;
        VolleyHelper.stringRequest(queue, Request.Method.GET, url, session.getToken(), callback);
    }
}
