package com.yudi.asmara.expensereport.sessions;

import android.content.Context;
import android.content.SharedPreferences;

import com.yudi.asmara.expensereport.utils.AppConfig;
import com.yudi.asmara.expensereport.utils.CryptoUtil;


public class SessionManager {
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_DEVICE_INFO = "deviceInfo";
    private static final String KEY_VERSION_APP = "versionApp";
    private static final String KEY_LOGGED_IN = "loggedIn";
    private static final String KEY_SESSION_DATE = "sessionDate";

    private final Context mContext;
    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private Boolean isLoggedIn;
    private String username, password, sessionDate, courierCode, whatsapp, photoUrl, deviceInfo, versionApp, nip, courierName, freqDelivery, fuelQuota, isDelivery, isPickup, branchId, branchName, branchCode;

    public SessionManager(Context context) {
        mContext = context;
        pref = context.getSharedPreferences(AppConfig.SECURE_PREF_FILENAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void clearSession() {
        pref.edit().clear().apply();
    }

    public String getUsername() {
        username = pref.getString(KEY_USERNAME, "");
        return CryptoUtil.decrypt(username);
    }

    public void setUsername(String username) {
        pref.edit().putString(KEY_USERNAME, CryptoUtil.encrypt(username)).apply();
    }

    public String getPassword() {
        password = pref.getString(KEY_PASSWORD, "");
        return CryptoUtil.decrypt(password);
    }

    public void setPassword(String password) {
        pref.edit().putString(KEY_PASSWORD, CryptoUtil.encrypt(password)).apply();
    }

    public Boolean getLoggedIn() {
        isLoggedIn = pref.getBoolean(KEY_LOGGED_IN, false);
        return isLoggedIn;
    }

    public void setLoggedIn(Boolean loggedIn) {
        pref.edit().putBoolean(KEY_LOGGED_IN, loggedIn).apply();
    }

    public String getSessionDate() {
        sessionDate = pref.getString(KEY_SESSION_DATE, "");
        return CryptoUtil.decrypt(sessionDate);

    }

    public void setSessionDate(String sessionDate) {
        pref.edit().putString(KEY_SESSION_DATE, CryptoUtil.encrypt(sessionDate)).apply();
    }

    public String getDeviceInfo() {
        deviceInfo = pref.getString(KEY_DEVICE_INFO, "");
        return CryptoUtil.decrypt(deviceInfo);
    }

    public void setDeviceInfo(String deviceInfo) {
        pref.edit().putString(KEY_DEVICE_INFO, CryptoUtil.encrypt(deviceInfo)).apply();
    }

    public String getVersionApp() {
        versionApp = pref.getString(KEY_VERSION_APP, "");
        return CryptoUtil.decrypt(versionApp);
    }

    public void setVersionApp(String versionApp) {
        pref.edit().putString(KEY_VERSION_APP, CryptoUtil.encrypt(versionApp)).apply();
    }

    public void setLoginTime(long time) {
        editor.putLong("loginTime", time);
        editor.apply();
    }

    public long getLoginTime() {
        return pref.getLong("loginTime", 0);
    }
}
