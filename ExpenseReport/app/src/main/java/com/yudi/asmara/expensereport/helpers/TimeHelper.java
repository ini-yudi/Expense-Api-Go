package com.yudi.asmara.expensereport.helpers;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by FTRZFZI with love on 12/2/2020.
 */

public class TimeHelper {

    public static final String FORMAT_MINUTE_SECOND = "%02d:%02d";

    public static int minuteToMilli(int minute) {
        return minute * 60 * 1000;
    }

    public static int milliToMinute(int milli) {
        return milli / 60 / 1000;
    }

    public static String getShortDateTime() {
        SimpleDateFormat dateFormat = getDefaultShortDateTimeFormat();
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String getLongDateTime() {
        SimpleDateFormat dateFormat = getDefaultLongDateTimeFormat();
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String getShortDate() {
        SimpleDateFormat dateFormat = getDefaultShortDateFormat();
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String getShortTime() {
        SimpleDateFormat dateFormat = getDefaultShortTimeFormat();
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String getDateTime(@NonNull SimpleDateFormat simpleDateFormat) {
        Date date = new Date();
        return simpleDateFormat.format(date);
    }

    public static String getDisplayDateTime(String datetime, @NonNull SimpleDateFormat simpleDateFormat) {
        return getDisplayDate(datetime, simpleDateFormat) + ", " + getDisplayTime(datetime, simpleDateFormat);
    }

    public static String getDisplayDate(String datetime, @NonNull SimpleDateFormat simpleDateFormat) {
        Date date = parse(simpleDateFormat, datetime);
        SimpleDateFormat formatter = new SimpleDateFormat(
                getDefaultDisplayDateFormat(), Locale.getDefault());
        return formatter.format(date);
    }

    public static String getDisplayTime(String datetime, @NonNull SimpleDateFormat simpleDateFormat) {
        Date date = parse(simpleDateFormat, datetime);
        SimpleDateFormat formatter = new SimpleDateFormat(
                getDefaultDisplayTimeFormat(), Locale.getDefault());
        return formatter.format(date).replace("am", "AM").replace("pm", "PM");
    }

    @Nullable
    public static Date parse(SimpleDateFormat simpleDateFormat, String datetime) {
        try {
            return simpleDateFormat.parse(datetime);
        } catch (Exception e) {
            Log.e("TimeHelper", Log.getStackTraceString(e));
            return null;
        }
    }

    private static String getDefaultDisplayDateFormat() {
        return "dd MMM yyyy";
    }

    private static String getDefaultDisplayTimeFormat() {
        return "hh:mm a";
    }

    @NonNull
    public static SimpleDateFormat getDefaultLongDateTimeFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.getDefault());
    }

    @NonNull
    public static SimpleDateFormat getDefaultShortDateTimeFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    }

    @NonNull
    public static SimpleDateFormat getDefaultShortDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }

    @NonNull
    public static SimpleDateFormat getDefaultShortTimeFormat() {
        return new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    }

    public static String getCurrentDate() {
        return getShortDate();
    }

    public static String getCurrentMonth() {
        return new SimpleDateFormat("MM", Locale.getDefault()).format(new Date());
    }

    public static String getCurrentYear() {
        return new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date());
    }

    public static String getFormattedTime(String format, int millis) {
        return String.format(Locale.getDefault(), format,
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

}
