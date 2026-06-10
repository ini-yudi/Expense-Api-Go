package com.yudi.asmara.expensereport.network;

public class Endpoint {
    // Emulator: 10.0.2.2 -> localhost host machine
    // Device fisik: pakai IP komputer (contoh: 192.168.1.10)
    private static final String BASE_URL = "http://192.168.123.4:8080/api";

    public static String getBaseUrl() {
        return BASE_URL;
    }

    public static final String LOGIN = "/auth/login";
    public static final String REGISTER = "/auth/register";
    public static final String DASHBOARD = "/dashboard";
    public static final String CATEGORIES = "/categories";
    public static final String TRANSACTIONS = "/transactions";
    public static final String REPORT_DAILY = "/report/daily";
    public static final String REPORT_MONTHLY = "/report/monthly";
    public static final String REPORT_STATS = "/report/stats";
    public static final String CHART = "/chart";
    public static final String EXPORT = "/export";
    public static final String BUDGETS = "/budgets";
    public static final String BUDGET = "/budget";
}
