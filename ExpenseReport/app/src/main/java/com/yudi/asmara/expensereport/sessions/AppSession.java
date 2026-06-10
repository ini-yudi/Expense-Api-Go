package com.yudi.asmara.expensereport.sessions;

import android.content.Context;
import android.content.SharedPreferences;

import com.yudi.asmara.expensereport.utils.AppConfig;
import com.yudi.asmara.expensereport.utils.CryptoUtil;

/**
 * AppSession:
 * Mengelola penyimpanan session secara aman menggunakan SharedPreferences.
 * Semua data sensitif (String) otomatis dienkripsi memakai CryptoUtil.
 */
public class AppSession {
    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    public AppSession(Context context) {
        pref = context.getSharedPreferences(AppConfig.SECURE_PREF_FILENAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    // ============================================================
    // ENUM: Semua key disimpan di sini (lebih konsisten & aman)
    // ============================================================
    private enum Key {
        USERNAME, PASSWORD, NIP, COURIER_CODE, COURIER_NAME, WHATSAPP,
        BRANCH_ID, BRANCH_NAME, BRANCH_CODE,
        PHOTO_URL, DEVICE_INFO, VERSION_APP,
        FREQ_DELIVERY, FUEL_QUOTA,
        IS_DELIVERY, IS_PICKUP,
        IS_LOGIN, REMEMBER_ME, REMEMBER_USERNAME, REMEMBER_PASSWORD,
        TIME_LOGIN, TOKEN
    }

    // ============================================================
    // SECURE STRING (Auto Encrypt / Decrypt)
    // ============================================================
    private void setString(Key key, String value) {
        try {
            String encrypted = CryptoUtil.encrypt(value);
            editor.putString(key.name(), encrypted).apply();
        } catch (Exception e) {
            e.printStackTrace(); // jika encrypt gagal
        }
    }

    private String getString(Key key) {
        try {
            String encrypted = pref.getString(key.name(), null);
            return encrypted != null ? CryptoUtil.decrypt(encrypted) : "";
        } catch (Exception e) {
            e.printStackTrace(); // jika decrypt gagal
            return "";
        }
    }

    // ============================================================
    // NON-SECURE PRIMITIVES (Integer, Boolean, Long)
    // ============================================================
    private void setInt(Key key, int value) {
        editor.putInt(key.name(), value).apply();
    }

    private int getInt(Key key) {
        return pref.getInt(key.name(), 0);
    }

    private void setBool(Key key, boolean value) {
        editor.putBoolean(key.name(), value).apply();
    }

    private boolean getBool(Key key, boolean defaultValue) {
        return pref.getBoolean(key.name(), defaultValue);
    }

    private void setLong(Key key, long value) {
        editor.putLong(key.name(), value).apply();
    }

    private long getLong(Key key) {
        return pref.getLong(key.name(), 0);
    }

    // ============================================================
    // CLEAR SESSION (hapus semua data)
    // ============================================================
    public void clearSession() {
        editor.clear().apply();
    }

    // ============================================================
    // TOKEN
    // ============================================================
    public void setToken(String token) {
        setString(Key.TOKEN, token);
    }

    public String getToken() {
        return getString(Key.TOKEN);
    }

    // ============================================================
    // LOGIN STATUS
    // ============================================================
    public boolean isLoggedIn() {
        return getBool(Key.IS_LOGIN, false);
    }

    public void setLoggedIn(boolean loggedIn) {
        setBool(Key.IS_LOGIN, loggedIn);
    }

    // ============================================================
// REMEMBER ME FEATURE
// ============================================================

    // Simpan status remember me
    public void setRememberMe(boolean value) {
        setBool(Key.REMEMBER_ME, value);
    }

    public boolean isRememberMe() {
        return getBool(Key.REMEMBER_ME, false);
    }

    // Simpan username & password terenkripsi
    public void saveRememberCredentials(String username, String password) {
        setString(Key.REMEMBER_USERNAME, username);
        setString(Key.REMEMBER_PASSWORD, password);
    }

    // Ambil username/password tersimpan
    public String getRememberUsername() {
        return getString(Key.REMEMBER_USERNAME);
    }

    public String getRememberPassword() {
        return getString(Key.REMEMBER_PASSWORD);
    }

    // Hapus credential
    public void clearRememberCredentials() {
        setString(Key.REMEMBER_USERNAME, "");
        setString(Key.REMEMBER_PASSWORD, "");
    }


    // ============================================================
    // GROUP: USER INFO (data user)
    // ============================================================
    public class UserInfo {
        // String (encrypted)
        public void setUsername(String value) {
            setString(Key.USERNAME, value);
        }

        public String getUsername() {
            return getString(Key.USERNAME);
        }

        public void setPassword(String value) {
            setString(Key.PASSWORD, value);
        }

        public String getPassword() {
            return getString(Key.PASSWORD);
        }

        public void setCourierCode(String value) {
            setString(Key.COURIER_CODE, value);
        }

        public String getCourierCode() {
            return getString(Key.COURIER_CODE);
        }

        public void setWhatsapp(String value) {
            setString(Key.WHATSAPP, value);
        }

        public String getWhatsapp() {
            return getString(Key.WHATSAPP);
        }

        public void setPhotoUrl(String value) {
            setString(Key.PHOTO_URL, value);
        }

        public String getPhotoUrl() {
            return getString(Key.PHOTO_URL);
        }

        public void setDeviceInfo(String value) {
            setString(Key.DEVICE_INFO, value);
        }

        public String getDeviceInfo() {
            return getString(Key.DEVICE_INFO);
        }

        public void setVersionApp(String value) {
            setString(Key.VERSION_APP, value);
        }

        public String getVersionApp() {
            return getString(Key.VERSION_APP);
        }

        public void setNip(String value) {
            setString(Key.NIP, value);
        }

        public String getNip() {
            return getString(Key.NIP);
        }

        public void setCourierName(String value) {
            setString(Key.COURIER_NAME, value);
        }

        public String getCourierName() {
            return getString(Key.COURIER_NAME);
        }

        public void setBranchName(String value) {
            setString(Key.BRANCH_NAME, value);
        }

        public String getBranchName() {
            return getString(Key.BRANCH_NAME);
        }

        public void setBranchCode(String value) {
            setString(Key.BRANCH_CODE, value);
        }

        public String getBranchCode() {
            return getString(Key.BRANCH_CODE);
        }

        public void setTimeLogin(String value) {
            setString(Key.TIME_LOGIN, value);
        }

        public String getTimeLogin() {
            return getString(Key.TIME_LOGIN);
        }


        // int (non-encrypted)
        public void setFreqDelivery(int value) {
            setInt(Key.FREQ_DELIVERY, value);
        }

        public int getFreqDelivery() {
            return getInt(Key.FREQ_DELIVERY);
        }

        public void setFuelQuota(int value) {
            setInt(Key.FUEL_QUOTA, value);
        }

        public int getFuelQuota() {
            return getInt(Key.FUEL_QUOTA);
        }

        public void setBranchId(int value) {
            setInt(Key.BRANCH_ID, value);
        }

        public int getBranchId() {
            return getInt(Key.BRANCH_ID);
        }


        // boolean (non-encrypted)
        public void setIsDelivery(boolean value) {
            setBool(Key.IS_DELIVERY, value);
        }

        public boolean getIsDelivery() {
            return getBool(Key.IS_DELIVERY, false);
        }

        public void setIsPickup(boolean value) {
            setBool(Key.IS_PICKUP, value);
        }

        public boolean getIsPickup() {
            return getBool(Key.IS_PICKUP, false);
        }

    }

    public UserInfo user() {
        return new UserInfo();
    }
}
