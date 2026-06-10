package com.yudi.asmara.expensereport.helpers;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;

import com.yudi.asmara.expensereport.utils.AppConfig;
import com.yudi.asmara.expensereport.utils.CryptoUtil;

public class PermissionHelper {
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    public static final int MEDIA_PERMISSION_REQUEST_CODE = 101;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 102;
    public static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 103;
    public static final int CALL_PHONE_PERMISSION_REQUEST_CODE = 104;
    public static final int MULTIPLE_PERMISSION_REQUEST_CODE = 105;

    public static boolean hasCallPhonePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public static boolean hasNotificationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public static boolean hasCameraPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public class SessionManager {
        private static final String KEY_USERNAME = "username";
        private static final String KEY_PASSWORD = "password";
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

    public static boolean hasLocationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public static boolean hasMediaPermission(Activity activity) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_GRANTED;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    public static void requestMultiplePermission(Activity activity, String[] permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(activity, permission, MULTIPLE_PERMISSION_REQUEST_CODE);
        }
    }

    public static void requestNotificationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
        }
    }

    public static void requestCallPhonePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CALL_PHONE}, CALL_PHONE_PERMISSION_REQUEST_CODE);
        }
    }

    public static void requestLocationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    public static void requestCameraPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    public static void requestMediaPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED}, MEDIA_PERMISSION_REQUEST_CODE);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, MEDIA_PERMISSION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, MEDIA_PERMISSION_REQUEST_CODE);
        }
    }

    public static void showRationaleDialog(Activity activity, int type) {
        String message = "";
        if (type == LOCATION_PERMISSION_REQUEST_CODE) {
            message = "Aplikasi ini membutuhkan akses lokasi Anda untuk menampilkan maps. Izinkan akses untuk melanjutkan.";
        } else if (type == CAMERA_PERMISSION_REQUEST_CODE) {
            message = "Aplikasi ini membutuhkan akses kamera Anda untuk dapat melakukan foto. Izinkan akses untuk melanjutkan.";
        } else if (type == MEDIA_PERMISSION_REQUEST_CODE) {
            message = "Aplikasi ini membutuhkan akses media Anda untuk dapat menampilkan foto. Izinkan akses untuk melanjutkan.";
        } else {
            message = "asd";
        }

        new AlertDialog.Builder(activity)
                .setTitle("Akses Diperlukan")
                .setMessage(message)
                .setPositiveButton("Izinkan", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (type == LOCATION_PERMISSION_REQUEST_CODE) {
                            requestLocationPermission(activity);
                        } else if (type == CAMERA_PERMISSION_REQUEST_CODE) {
                            requestCameraPermission(activity);
                        }
                    }
                })
                .setNegativeButton("Tolak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Pengguna memilih untuk tidak memberikan izin
                    }
                })
                .show();
    }

    public static void showPermissionDeniedDialog(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Izin Diperlukan");
        builder.setMessage("Aplikasi memerlukan izin untuk melanjutkan. Buka Pengaturan untuk memberikan izin?");
        builder.setPositiveButton("Buka Pengaturan", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + activity.getPackageName()));
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
            }
        });
        builder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Tampilkan pesan bahwa izin tidak diberikan
            }
        });
        builder.show();
    }
}
