package com.yudi.asmara.expensereport.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.ParseException;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by FTRZFZI with love on 1/25/2021.
 */

public class AppUtils {

    private static final String TAG = AppUtils.class.getSimpleName();

    private static ProgressDialog mProgressDialog;
    private static AlertDialog mAlertDialog;

    // Close Soft Keyboard
    public static void closeSoftKeyboard(@NotNull Context context) {
        InputMethodManager input = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        View view = ((Activity) context).getCurrentFocus();
        if (view != null) {
            input.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // Open Soft Keyboard
    public static void openSoftKeyboard(@NotNull Context context) {
        InputMethodManager input = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        View view = ((Activity) context).getCurrentFocus();
        if (view != null) {
            input.toggleSoftInputFromWindow(view.getWindowToken(), InputMethodManager.SHOW_IMPLICIT, 0);
        }
    }

    // Show Toast
    public static void showToast(Context context, CharSequence message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        toast.show();
    }

    // Show Toast with Duration
    public static void showToast(Context context, CharSequence message, int duration) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setDuration(duration);
        toast.show();
    }

    // Show Custom Snackbar
    public static void showSnackbar(Context context, View view, CharSequence message, int color) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);

        if (color != 0) {
            snackbar.setBackgroundTint(ContextCompat.getColor(context, color));
        }

        snackbar.show();
    }

    // Show Custom Snackbar with Duration
    public static void showSnackbar(Context context, View view, CharSequence message, int color, int duration) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);

        if (color != 0) {
            snackbar.setBackgroundTint(ContextCompat.getColor(context, color));
        }

        snackbar.setDuration(duration);
        snackbar.show();
    }

    // Show Progress Dialog
    public static void showProgressDialog(Context context, String title, String body, boolean isCancellable) {
        showProgressDialog(context, title, body, null, isCancellable);
    }

    public static void showProgressDialog(Context context, String title, String body, Drawable icon, boolean isCancellable) {

        if (context instanceof Activity) {
            if (!((Activity) context).isFinishing()) {
                mProgressDialog = ProgressDialog.show(context, title, body, true);
                mProgressDialog.setIcon(icon);
                mProgressDialog.setCancelable(isCancellable);
            }
        }
    }

    public static boolean isProgressDialogVisible() {
        return (mProgressDialog != null);
    }

    public static void dismissProgressDialog() {

        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        mProgressDialog = null;
    }

    // Show Alert Dialog
    public static void showAlertDialog(Context context, String title, String body) {
        showAlertDialog(context, title, body, null);
    }

    // Show Alert Dialog with OK button listener
    public static void showAlertDialog(Context context, String title, String body, DialogInterface.OnClickListener okListener) {

        if (okListener == null) {
            okListener = new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            };
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context).setMessage(body).setPositiveButton("OK", okListener);

        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }

        builder.show();
    }

    // Show Alert Dialog with Options
    public static void showAlertDialogOptions(Context context, String title, String[] options, DialogInterface.OnClickListener okListener) {

        if (okListener == null) {
            okListener = new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            };
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context).setItems(options, okListener);

        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }

        builder.show();
    }

    // Creates a confirmation dialog with Yes-No Button. By default the buttons just dismiss the dialog
    public static void showConfirmDialog(Context ctx, String title, String message, DialogInterface.OnClickListener yesListener, DialogInterface.OnClickListener noListener) {
        showConfirmDialog(ctx, title, message, yesListener, noListener, "Yes", "No");
    }

    // Creates a confirmation dialog with Yes-No Button. By default the buttons just dismiss the dialog
    public static void showConfirmDialog(Context ctx, String title, String message, DialogInterface.OnClickListener yesListener, DialogInterface.OnClickListener noListener, String yesLabel, String noLabel) {

        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);

        if (yesListener == null) {
            yesListener = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            };
        }

        if (noListener == null) {
            noListener = new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            };
        }

        builder.setMessage(message).setPositiveButton(yesLabel, yesListener).setNegativeButton(noLabel, noListener);

        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }

        builder.show();
    }

    // Creates a confirmation dialog with Yes-No Button. By default the buttons just dismiss the dialog
    public static void showConfirmDialogMaterial(Context ctx, String title, String message, DialogInterface.OnClickListener yesListener, DialogInterface.OnClickListener noListener, String yesLabel, String noLabel) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(ctx);

        if (yesListener == null) {
            yesListener = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            };
        }

        if (noListener == null) {
            noListener = new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            };
        }

        builder.setMessage(message).setPositiveButton(yesLabel, yesListener).setNegativeButton(noLabel, noListener);

        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }

        builder.show();
    }

    public static void showOkDialogMaterial(Context context, String title, String body) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);

        if (title != null && !title.isEmpty()) {
            builder.setTitle(title);
        }

        builder.setMessage(body)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // Gets the Version Name of the application. For e.g. 1.9.3
    public static String getApplicationVersionNumber() {
        String versionName = null;
//        versionName = BuildConfig.VERSION_NAME;

        return versionName;
    }

    // Gets the Version Code of the application. For e.g. Maverick Meerkat or 2013050301
    public static int getApplicationVersionCode() {

        int versionCode = 0;
//        versionCode = BuildConfig.VERSION_CODE;
        return versionCode;
    }

    // Gets the Application ID of the application. For e.g. com.example.myappkonkud
    public static String getApplicationId() {
        String applicationId = null;
//        applicationId = BuildConfig.APPLICATION_ID;

        return applicationId;
    }

    // Get OS Version
    public static String getOSVersion() {
        return Build.VERSION.RELEASE;
    }

    // Gets DeviceID (Android ID or IMEI)
//    public static String getDeviceId(Context context) {
//
//        String deviceId;
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//
//            deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
//
//        } else {
//
//            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//
//            if (tm.getDeviceId() != null) {
//                deviceId = tm.getDeviceId();
//            } else {
//                deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
//            }
//        }
//
//        return deviceId;
//
//    }

    // Fixing Rotation Image or Photo
    public static Bitmap getRotateImage(InputStream inputStream, Bitmap bitmap) throws IOException {
        ExifInterface ei = new ExifInterface(inputStream);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap rotatedBitmap = null;
        switch (orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }

        return rotatedBitmap;

    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    // Scale Bitmap
    public Bitmap scaleDownBitmap(int maxSize, @NotNull Bitmap sourceImage) {
        int widthOri = 0; // original width
        int heightOri = 0; // original height
        int widthNew = 0;
        int heightNew = 0;

        widthOri = sourceImage.getWidth();
        heightOri = sourceImage.getHeight();

        if (widthOri > heightOri) {
            widthNew = maxSize;
            heightNew = (heightOri * maxSize) / widthOri;
        } else {
            heightNew = maxSize;
            widthNew = (widthOri * maxSize) / heightOri;
        }

        return Bitmap.createScaledBitmap(sourceImage, widthNew, heightNew, true);
    }

    public static InputStream bitmapToInputStream(@NotNull Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream);
        byte[] bitmapData = byteArrayOutputStream.toByteArray();
        return new ByteArrayInputStream(bitmapData);
    }

    // Convert Bitmap to String Base64
    public static String bitmapToStringBase64(@NotNull Bitmap bitmap, int quality) {
        String base64Bitmap = null;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        byte[] imageBytes = stream.toByteArray();
        base64Bitmap = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        return base64Bitmap;
    }

    // Convert Bitmap to String Base64 with custom format
    public static String bitmapToStringBase64(@NotNull Bitmap bitmap, Bitmap.CompressFormat compressFormat, int quality) {
        String base64Bitmap = null;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(compressFormat, quality, stream);
        byte[] imageBytes = stream.toByteArray();
        base64Bitmap = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        return base64Bitmap;
    }

    // Gets random color integer
    public static int getRandomColor() {
        Random random = new Random();
        int red = random.nextInt(255);
        int green = random.nextInt(255);
        int blue = random.nextInt(255);

        return Color.argb(255, red, green, blue);
    }

    // Checks if the input parameter is a valid email
    public static boolean isValidEmail(String email) {
        final String emailPattern = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Matcher matcher;
        Pattern pattern = Pattern.compile(emailPattern);

        matcher = pattern.matcher(email);

        if (matcher != null)
            return matcher.matches();
        else
            return false;
    }

    // Checks if the input parameter is a valid phone number
    public static boolean isValidPhoneNumber(String phone) {
        final String phonePattern = "^[0-9]{9,13}$";
        Matcher matcher;
        Pattern pattern = Pattern.compile(phonePattern);

        matcher = pattern.matcher(phone);

        if (matcher != null)
            return matcher.matches();
        else
            return false;
    }

    // Parses date string and return a Date object
    public static Date parseDate(String date) {

        if (date == null) {
            return null;
        }

        StringBuffer sbDate = new StringBuffer();
        sbDate.append(date);
        String newDate = null;
        Date dateDT = null;

        try {
            newDate = sbDate.substring(0, 19);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String rDate = newDate.replace("T", " ");
        String nDate = rDate.replaceAll("-", "/");

        try {
            dateDT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).parse(nDate);
            // Log.v( TAG, "#parseDate dateDT: " + dateDT );
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dateDT;
    }

    // Calculates the elapsed time after the given parameter date.
    public static String getElapsedTime(String time) {
        TimeZone defaultTimeZone = TimeZone.getDefault();

        Date eventTime = parseDate(time);

        Date currentDate = new Date();

        long diffInSeconds = (currentDate.getTime() - eventTime.getTime()) / 1000;
        String elapsed = "";
        long seconds = diffInSeconds;
        long mins = diffInSeconds / 60;
        long hours = diffInSeconds / (60 * 60);
        long days = diffInSeconds / 86400;
        long weeks = diffInSeconds / 604800;
        long months = diffInSeconds / 2592000;

        // Log.v( TAG, "#getElapsedTime seconds: " + seconds + " mins: " + mins
        // + " hours: " + hours + " days: " + days );

        if (seconds < 120) {
            elapsed = "a min ago";
        } else if (mins < 60) {
            elapsed = mins + " mins ago";
        } else if (hours < 24) {
            elapsed = hours + " " + (hours > 1 ? "hrs" : "hr") + " ago";
        } else if (hours < 48) {
            elapsed = "a day ago";
        } else if (days < 7) {
            elapsed = days + " days ago";
        } else if (weeks < 5) {
            elapsed = weeks + " " + (weeks > 1 ? "weeks" : "week") + " ago";
        } else if (months < 12) {
            elapsed = months + " " + (months > 1 ? "months" : "months") + " ago";
        } else {
            elapsed = "more than a year ago";
        }

        TimeZone.setDefault(defaultTimeZone);

        return elapsed;
    }

    public static String formatToRupiah(String amount) {
        String result;
        if (!amount.isEmpty()) {
            Double dNominal = Double.valueOf(amount);
            result = "Rp " + String.format(new Locale("in", "ID"),
                    "%,.0f", dNominal).replaceAll(",", ".");
        } else {
            result = "Rp 0";
        }
        return result;
    }

    public static void showDatePicker(Context context, final EditText editText, final String dateFormat) {
        final Calendar myCalendar = Calendar.getInstance();

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.US);

                editText.setText(sdf.format(myCalendar.getTime()));
            }
        };

        new DatePickerDialog(context, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    public static void showDatePicker(Context context, final EditText editText, final String dateFormat, int minDate) {
        final Calendar myCalendar = Calendar.getInstance();
        final Calendar minDateCalendar = Calendar.getInstance();
        minDateCalendar.add(Calendar.MONTH, -minDate); // Menetapkan tanggal minimal 1 bulan ke belakang

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.US);

                editText.setText(sdf.format(myCalendar.getTime()));
            }
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMinDate(minDateCalendar.getTimeInMillis()); // Menetapkan tanggal minimal

        datePickerDialog.show();
    }

    public static void showTimePicker(Context context, final EditText editText, final String timeFormat) {
        final Calendar myCalendar = Calendar.getInstance();

        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                myCalendar.set(Calendar.MINUTE, minute);

                SimpleDateFormat sdf = new SimpleDateFormat(timeFormat, Locale.US);
                editText.setText(sdf.format(myCalendar.getTime()));
            }
        };

        new TimePickerDialog(
                context,
                timeSetListener,
                myCalendar.get(Calendar.HOUR_OF_DAY),
                myCalendar.get(Calendar.MINUTE),
                true // true = 24 hour format, false = 12 hour
        ).show();
    }

    /**
     * **Format Tanggal dan Waktu Lengkap:**
     * - Dengan detik: yyyy-MM-dd HH:mm:ss (Contoh: 2023-11-24 13:30:59)
     * - Dengan zona waktu: EEEE, dd MMM yyyy HH:mm:ss Z (Contoh: Friday, 24 Nov 2023 13:30:59 +0700)
     * - Dengan AM/PM: EEEE, dd MMMM yyyy hh:mm a (Contoh: Friday, 24 November 2023 01:30 PM)
     * **Format Tanggal Singkat:**
     * - Hanya tanggal: yyyy-MM-dd (Contoh: 2023-11-24)
     * - Tanggal dan bulan: dd MMMM (Contoh: 24 November)
     * - Tanggal singkat: dd/MM/yyyy (Contoh: 24/11/2023)
     * **Format Waktu:**
     * - Waktu 24 jam: HH:mm:ss (Contoh: 13:30:59)
     * - Waktu 12 jam: hh:mm a (Contoh: 01:30 PM)
     * **Keterangan:**
     * - yyyy: Tahun dengan 4 digit
     * - MM: Bulan dalam bentuk angka (01-12)
     * - dd: Hari dalam bulan (01-31)
     * - MMM: Nama bulan singkat (Jan, Feb, ...)
     * - MMMM: Nama bulan lengkap (January, February, ...)
     * - EEEE: Nama hari dalam minggu lengkap (Sunday, Monday, ...)
     * - EEE: Nama hari dalam minggu singkat (Sun, Mon, ...)
     * - HH: Jam dalam format 24 jam (00-23)
     * - hh: Jam dalam format 12 jam (01-12)
     * - mm: Menit (00-59)
     * - ss: Detik (00-59)
     * - a: AM/PM penanda
     * - Z: Zona waktu
     */
    public static String formatDateTo(String inputDate, String formatInput, String formatOutput) {
        try {
            // Asumsi format input YYYY-MM-DD
            SimpleDateFormat inputFormat = new SimpleDateFormat(formatInput, Locale.getDefault());
            Date myDate = inputFormat.parse(inputDate);

            SimpleDateFormat outputFormat = new SimpleDateFormat(formatOutput, Locale.getDefault());
            return outputFormat.format(myDate);
        } catch (Exception e) {
            e.printStackTrace();
            return "Format tanggal tidak valid";
        }
    }
}
