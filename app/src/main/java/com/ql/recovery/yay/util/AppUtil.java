package com.ql.recovery.yay.util;

import static android.app.Notification.EXTRA_CHANNEL_ID;
import static android.content.ContentValues.TAG;
import static android.content.Context.ACTIVITY_SERVICE;
import static android.provider.Settings.EXTRA_APP_PACKAGE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;

import com.ql.recovery.bean.BirthDay;
import com.ql.recovery.yay.BuildConfig;
import com.ql.recovery.yay.R;
import com.ql.recovery.yay.callback.Callback;
import com.ql.recovery.yay.callback.LocationCallback;
import com.tencent.mmkv.MMKV;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class AppUtil {

    private final static String[] hexDigits = {
            "0", "1", "2", "3", "4", "5", "6", "7",
            "8", "9", "a", "b", "c", "d", "e", "f"};


    public static boolean isDebugger(Context context) {
        boolean debuggable = false;
        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo appinfo = pm.getApplicationInfo(context.getPackageName(), 0);
            debuggable = (0 != (appinfo.flags & ApplicationInfo.FLAG_DEBUGGABLE));
        } catch (PackageManager.NameNotFoundException e) {
            /*debuggable variable will remain false*/
        }
        return debuggable;
    }

    public static String md5Encode(String origin) {
        String resultString = null;
        try {
            resultString = new String(origin);
            MessageDigest md = MessageDigest.getInstance("MD5");
            resultString = byteArrayToHexString(md.digest(resultString.getBytes()));
        } catch (Exception ex) {

        }
        return resultString;
    }

    public static String md5Encode(byte[] origin) {
        String resultString = null;
        try {
            resultString = new String(origin);
            MessageDigest md = MessageDigest.getInstance("MD5");
            resultString = byteArrayToHexString(md.digest(resultString.getBytes()));
        } catch (Exception ex) {

        }
        return resultString;
    }

    public static String byteArrayToHexString(byte[] b) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            resultSb.append(byteToHexString(b[i]));
        }
        return resultSb.toString();
    }


    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n = 256 + n;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    /**
     * ??????????????????
     *
     * @param timeStamp
     * @return
     */
    public static String timeStamp2Date(Long timeStamp, String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            pattern = "yyyy-MM-dd HH:mm:ss";
        }

        //??????????????????????????????????????????
        if (timeStamp.toString().length() == 10) {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.CHINA);
            return sdf.format(new Date(timeStamp * 1000));
        }

        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.CHINA);
        return sdf.format(new Date(timeStamp));
    }

    public static String second2Hour(long seconds) {
        long hour = 0;
        long minutes = 0;
        long sencond = 0;
        long temp = seconds % 3600;
        if (seconds > 3600) {
            hour = seconds / 3600;
            if (temp != 0) {
                if (temp > 60) {
                    minutes = temp / 60;
                    if (temp % 60 != 0) {
                        sencond = temp % 60;
                    }
                } else {
                    sencond = temp;
                }
            }
        } else {
            minutes = seconds / 60;
            if (seconds % 60 != 0) {
                sencond = seconds % 60;
            }
        }

        return (hour < 10 ? ("0" + hour) : hour) + ":" + (minutes < 10 ? ("0" + minutes) : minutes) + ":" + (sencond < 10 ? ("0" + sencond) : sencond);
    }

    /**
     * ??????????????????
     *
     * @param date
     * @return
     */
    public static long date2TimeStamp(String date) {
        Date d;
        long timeStamp = 0;
        try {
            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            d = sf.parse(date);// ????????????????????????
            timeStamp = d.getTime();
        } catch (ParseException e) {
            return 0;
        }

        return timeStamp;
    }

//    public static String timestamp2Time(long time) {
//        if (time < 60) {
//            return time + "s";
//        }
//
//        if (time < 3600) {
//            long a = time / 60;
//            long b = time % 60;
//
//            return a + ":" + b;
//        }
//    }

    public static String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(System.currentTimeMillis());
    }

    public static String getTodayYear() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
        return sdf.format(System.currentTimeMillis());
    }

    public static String getTodayMonth() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM", Locale.getDefault());
        return sdf.format(System.currentTimeMillis());
    }

    public static String getTodayDay() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd", Locale.getDefault());
        return sdf.format(System.currentTimeMillis());
    }

    public static List<String> getYearList() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR) + 1;
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            year--;
            list.add(year + "");
        }

        return list;
    }

    public static List<String> getMonthList() {
        List<String> list = new ArrayList<>();
        for (int i = 12; i > 0; i--) {
            list.add(i + "");
        }

        return list;
    }

    public static List<BirthDay> getBirthDayList() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        List<BirthDay> list = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            int newYear = year - i;
            if (month == 1) {
                month = 12;
            }

            month--;
            int newDay = getMonthOfDay(newYear, month);

            BirthDay birthDay = new BirthDay(newYear, month, newDay);
            list.add(birthDay);
        }

        return list;
    }

    public static int getMonthOfDay(int year, int month) {
        int day = 0;
        if (year % 4 == 0 && year % 100 != 0 || year % 400 == 0) {
            day = 29;
        } else {
            day = 28;
        }
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                return 31;
            case 4:
            case 6:
            case 9:
            case 11:
                return 30;
            case 2:
                return day;

        }

        return 0;
    }

    /**
     * ???????????????
     *
     * @param ctx
     * @param key
     * @return
     */
    public static String getAppMetaData(Context ctx, String key) {
        if (ctx == null || TextUtils.isEmpty(key)) {
            return null;
        }

        String resultData = null;
        try {
            PackageManager packageManager = ctx.getPackageManager();
            if (packageManager != null) {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
                if (applicationInfo != null) {
                    if (applicationInfo.metaData != null) {
                        resultData = applicationInfo.metaData.getString(key);
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {

        }
        return resultData;
    }

    public static int getScreenWidth(Context context) {
        DisplayMetrics outMetrics = context.getResources().getDisplayMetrics();
        return outMetrics.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics outMetrics = context.getResources().getDisplayMetrics();
        return outMetrics.heightPixels;
    }

    /**
     * ??????QQ????????????
     */
    public static void joinQQ(Activity activity, String qqNum) {
        try {
            //??????????????????????????????????????????????????????qq??????????????????????????????
            String url = "mqqwpa://im/chat?chat_type=wpa&uin=" + qqNum;//uin??????????????????qq??????
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.showShort(activity, "?????????????????????QQ");
        }
    }

    /**
     * ?????????????????????
     *
     * @param packname
     * @return
     */
    public static boolean checkPackageInfo(Context context, String packname) {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packname, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            JLog.i("pack null");
            return false;
        }

        return packageInfo != null;
    }


    public static void getAllInstalledAppPakageName(Context context) {
        //????????????pid
        final PackageManager packageManager = context.getPackageManager();
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        // get all apps
        final List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
        for (int i = 0; i < apps.size(); i++) {
            String name = apps.get(i).activityInfo.packageName;
            int version = getPackageVersionCode(context, name);
            JLog.i("getAppProcessName: " + name);
            JLog.i("getAppProcessVersion: " + version);
        }
    }


    /**
     * ?????????????????????
     *
     * @return ????????????????????????
     */
    public static String getPackageVersionName(Context context, String packageName) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(packageName, 0);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * ?????????????????????
     *
     * @return ????????????????????????
     */
    public static int getPackageVersionCode(Context context, String packageName) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(packageName, 0);
            return info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * ???????????????
     *
     * @param context
     * @param fileName assets???????????????
     * @param path     ????????????
     */
    public static void copyApkFromAssets(Context context, String fileName, String path) {
        try {
            InputStream is = context.getAssets().open(fileName);
            File file = new File(path + fileName);
            JLog.i(path + fileName);
            if (!file.exists()) {
                boolean create = file.createNewFile();
                if (!create) return;
            }

            JLog.i("filename = " + file.getPath());
            FileOutputStream fos = new FileOutputStream(file);
            byte[] temp = new byte[1024];
            int i = 0;
            while ((i = is.read(temp)) > 0) {
                fos.write(temp, 0, i);
            }
            fos.close();
            is.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ??????apk ??????????????????apk??????Android7.0??????path????????????
     *
     * @param file
     * @param c
     */
    public static void installApk(File file, Activity c) {
        if (!file.exists()) return;

        c.runOnUiThread(() -> {
            if (Build.VERSION.SDK_INT >= 26) {
                if (!c.getPackageManager().canRequestPackageInstalls()) {
                    //???????????????8.0???API
                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                    c.startActivityForResult(intent, 0x10086);
                    return;
                }
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            //???????????????AndroidN?????????????????????
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = FileProvider.getUriForFile(c, BuildConfig.APPLICATION_ID + ".fileprovider", file);
                intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
            } else {
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            c.startActivityForResult(intent, 0x88);
        });
    }

    public static void sendNotification(Context context, String title, String content) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setClass(context, context.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent pi = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationManager notifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification;

        //??????Android8.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String id = "my_channel_01";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            CharSequence name = "notice";
            NotificationChannel mChannel = new NotificationChannel(id, name, importance);
            mChannel.enableLights(true);
            mChannel.setDescription("just show notice");
            mChannel.setLightColor(Color.GREEN);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notifyMgr.createNotificationChannel(mChannel);

            Notification.Builder builder = new Notification.Builder(context, id);
            builder.setAutoCancel(true)
                    .setContentIntent(pi)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setOngoing(false)
                    .setAutoCancel(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setWhen(System.currentTimeMillis());
            notification = builder.build();
        } else if (Build.VERSION.SDK_INT >= 23) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setContentTitle(title)
                    .setContentText(content)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .setOngoing(false)
                    .setWhen(System.currentTimeMillis());
            notification = builder.build();
        } else {

            Notification.Builder builder = new Notification.Builder(context);
            builder.setAutoCancel(true)
                    .setContentIntent(pi)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setAutoCancel(true)
                    .setOngoing(false)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setWhen(System.currentTimeMillis());
            notification = builder.build();
        }

        if (notification != null) {
            notifyMgr.notify(1, notification);
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    public static Notification getNotification(Context context, String title, String content) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setClass(context, context.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        PendingIntent pi;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pi = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            pi = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        final NotificationManager notifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification;

        //??????Android8.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String id = "my_channel_01";
            int importance = NotificationManager.IMPORTANCE_LOW;
            CharSequence name = "notice";
            NotificationChannel mChannel = new NotificationChannel(id, name, importance);
            mChannel.enableLights(true);
            mChannel.setDescription("just show notice");
            mChannel.setLightColor(Color.GREEN);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notifyMgr.createNotificationChannel(mChannel);

            Notification.Builder builder = new Notification.Builder(context, id);
            builder.setAutoCancel(true)
                    .setContentIntent(pi)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setOngoing(false)
                    .setAutoCancel(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setWhen(System.currentTimeMillis());
            notification = builder.build();
        } else if (Build.VERSION.SDK_INT >= 23) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setContentTitle(title)
                    .setContentText(content)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .setOngoing(false)
                    .setWhen(System.currentTimeMillis());
            notification = builder.build();
        } else {

            Notification.Builder builder = new Notification.Builder(context);
            builder.setAutoCancel(true)
                    .setContentIntent(pi)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setAutoCancel(true)
                    .setOngoing(false)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setWhen(System.currentTimeMillis());
            notification = builder.build();
        }

        return notification;
    }

    public static String getRandomStringByLength(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    public static String getPathFromUri(final Context context, final Uri uri) {
        if (!("content").equalsIgnoreCase(uri.getScheme())) {
            return "";
        }

        //??????????????????????????????????????????????????????????????????????????????????????????sdk>19???????????????
        Cursor cursor = null;
        String path;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            //?????????android????????????????????????????????????????????????Android??????
            cursor = context.getContentResolver().query(uri, proj, null, null, null);
            //???????????????????????????????????????
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            //????????????????????? ???????????????????????????????????????????????????
            cursor.moveToFirst();
            //???????????????????????????????????????   ???????????????/mnt/sdcard/DCIM/Camera/IMG_20151124_013332.jpg
            path = cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return path;

    }

    public static void checkNotifySetting(Context context, Callback callback) {
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        // areNotificationsEnabled??????????????????????????????????????????API 19?????????19??????????????????????????????????????????true?????????????????????????????????????????????
        boolean isOpened = manager.areNotificationsEnabled();

        if (isOpened) {
            callback.onSuccess();
        } else {
            callback.onFailed("closed");
        }
    }

    public static void openNotifySettings(Context context) {
        try {
            // ??????isOpened?????????????????????????????????????????????AppInfo??????????????????App????????????
            Intent intent = new Intent();
            //????????????????????? API 26, ???8.0(???8.0)???????????????
            if (Build.VERSION.SDK_INT >= 26) {
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(EXTRA_APP_PACKAGE, context.getPackageName());
                intent.putExtra(EXTRA_CHANNEL_ID, context.getApplicationInfo().uid);
            } else {
                //????????????????????? API21??????25?????? 5.0??????7.1 ???????????????????????????
                intent.putExtra("app_package", context.getPackageName());
                intent.putExtra("app_uid", context.getApplicationInfo().uid);
            }

            // ??????6 -MIUI9.6-8.0.0??????????????????????????????????????????????????????"????????????????????????"??????????????????????????????????????????????????????????????????I'm not ok!!!
            if ("MI 6".equals(Build.MODEL)) {
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                intent.setData(uri);
                // intent.setAction("com.android.settings/.SubSettings");
            }

            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            // ?????????????????????????????????????????????????????????3??????OC105 API25
            Intent intent = new Intent();

            //??????????????????????????????????????????????????????????????????
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
            intent.setData(uri);
            context.startActivity(intent);
        }
    }


    private static final String HARMONY_OS = "harmony";

    /**
     * check the system is harmony os
     *
     * @return true if it is harmony os
     */
    public static boolean isHarmonyOS() {
        try {
            Class<?> clz = Class.forName("com.huawei.system.BuildEx");
            Method method = clz.getMethod("getOsBrand");
            return HARMONY_OS.equals(method.invoke(clz));
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "occured ClassNotFoundException");
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "occured NoSuchMethodException");
        } catch (Exception e) {
            Log.e(TAG, "occur other problem");
        }
        return false;
    }

    /**
     * ???????????????????????????
     * 1. ????????????
     * 1.1 ?????????????????????-??????0
     * 1.2 ?????????????????????-?????????????????????????????????
     * 2. ???????????????
     * 2.1 ???????????????-??????0
     * 2.1 ???????????????-??????0
     * 2.2 ???????????????????????????-???????????????????????????
     */
    public static int getNavigationBarHeightIfRoom(Activity context) {
        if (navigationGestureEnabled(context)) {
            return 0;
        }

        return getNavigationBarHeight(context);
    }

    /**
     * ??????????????????????????????????????? 0 ??????  1 ?????????
     *
     * @param context
     * @return
     */
    public static boolean navigationGestureEnabled(Context context) {
        int val = Settings.Global.getInt(context.getContentResolver(), getDeviceInfo(), 0);
        return val != 0;
    }

    /**
     * ??????????????????????????????????????????????????????????????????????????????????????????oppo????????????vivo????????????
     *
     * @return
     */
    public static String getDeviceInfo() {
        String brand = Build.BRAND;
        if (TextUtils.isEmpty(brand)) return "navigationbar_is_min";

        if (brand.equalsIgnoreCase("HUAWEI")) {
            return "navigationbar_is_min";
        } else if (brand.equalsIgnoreCase("XIAOMI")) {
            return "force_fsg_nav_bar";
        } else if (brand.equalsIgnoreCase("VIVO")) {
            return "navigation_gesture_on";
        } else if (brand.equalsIgnoreCase("OPPO")) {
            return "navigation_gesture_on";
        } else {
            return "navigationbar_is_min";
        }
    }

    /**
     * ??????????????? ?????????????????????(??????????????????0)
     *
     * @param activity
     * @return
     */
    public static int getCurrentNavigationBarHeight(Activity activity) {
        if (isNavigationBarShown(activity)) {
            return getNavigationBarHeight(activity);
        } else {
            JLog.i("NavigationBar is gone");
            return 0;
        }
    }

    /**
     * ??????????????? ????????????????????????
     *
     * @param activity
     * @return
     */
    public static boolean isNavigationBarShown(Activity activity) {
        //????????????view,???????????????????????????????????????
        View view = activity.findViewById(android.R.id.navigationBarBackground);
        if (view == null) {
            return false;
        }
        int visible = view.getVisibility();
        if (visible == View.GONE || visible == View.INVISIBLE) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * ??????????????? ???????????????(??????????????????)
     *
     * @param context
     * @return
     */
    public static int getNavigationBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }

    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int sp2px(Context context, int spValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, context.getResources().getDisplayMetrics());
    }

    public static String getDeviceId(Context context) {
        String deviceId;

        MMKV mmkv = MMKV.defaultMMKV();
        var uuid = mmkv.decodeString("uuid");
        if (uuid == null) {
            uuid = DeviceUtil.getUUID(context);
            mmkv.encode("uuid", uuid);
            deviceId = uuid;
        } else {
            deviceId = uuid;
        }

        return deviceId;
    }

    /**
     * ???????????????????????????????????????
     *
     * @param rootView ????????????view
     * @return
     */
    public static boolean isKeyboardShown(View rootView) {
        Rect rect = new Rect();
        rootView.getWindowVisibleDisplayFrame(rect);
        DisplayMetrics dm = rootView.getResources().getDisplayMetrics();
        int heightDiff = rootView.getBottom() - rect.bottom;
        return heightDiff > 100 * dm.density;
    }

    /**
     * ???????????????????????????????????????
     *
     * @param context ?????????
     * @return
     */
    public static boolean isKeyboardShown(Context context) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        return inputMethodManager.isActive();
    }

    /**
     * ???????????????
     *
     * @param context ?????????
     * @param view    view
     */
    public static void hideKeyboard(Context context, View view) {
//        if (isKeyboardShown(view.getRootView())) {
        if (isKeyboardShown(context)) {
            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * ???????????????
     *
     * @param view
     */
    public static void hideSoftKeyboard(Context context, View view) {
        if (view == null)
            return;

        ((InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                view.getWindowToken(), 0);
    }

    /**
     * ???????????????
     */
    public static void showSoftKeyboard(Context context) {
        ((InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE)).toggleSoftInput(0, InputMethodManager.RESULT_SHOWN);

    }

    public static LocationListener locationListener = null;

    public static void getLocation(Context context, LocationCallback locationCallback) {
        LocationManager locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

//        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
//        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
//                    JLog.i("location = " + location);
//                    if (location != null) {
//                        getAddress(context, location.getLatitude(), location.getLongitude(), locationCallback, locManager);
//                        return;
//                    }
//
//                    LocationRequest request = LocationRequest.create();
//                    request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

//                    fusedLocationProviderClient.getLocationAvailability().addOnCompleteListener(availabilityTask -> {
//                        if (availabilityTask.getResult() != null) {
//                            JLog.i("is location available " + availabilityTask.getResult().isLocationAvailable());
//
//                        }
//                    });


//                    fusedLocationProviderClient.requestLocationUpdates(request, new com.google.android.gms.location.LocationCallback() {
//                        @Override
//                        public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
//                            super.onLocationAvailability(locationAvailability);
//                            JLog.i("locationAvailability = " + locationAvailability);
//                        }
//
//                        @Override
//                        public void onLocationResult(@NonNull LocationResult locationResult) {
//                            super.onLocationResult(locationResult);
//                            JLog.i("LocationResult = " + locationResult);
//                            List<Location> list = locationResult.getLocations();
//                            if (!list.isEmpty()) {
//                                getAddress(context, list.get(0).getLatitude(), list.get(0).getLongitude(), locationCallback, locManager);
//                            }
//                        }
//                    }, Looper.getMainLooper());
//                }
//        );


        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                getAddress(context, location.getLatitude(), location.getLongitude(), locationCallback, locManager);
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                locationCallback.onFailed();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                JLog.i("provider status = " + status);
            }
        }

        ;

        List<String> list = locManager.getAllProviders();
        if (list.isEmpty()) {
            locationCallback.onFailed();
            return;
        }

        for (String c : list) {
            if (c.contains("network")) {
                //??????????????????,??????????????????????????????????????????????????????
                LocationProvider netProvider = locManager.getProvider(LocationManager.NETWORK_PROVIDER);
                if (netProvider != null) {
                    locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 10, locationListener);
                } else {
                    locationCallback.onFailed();
                }
            }
        }

    }


    /*
     * ?????????????????? ?????????????????????
     * */
    private static void getAddress(Context context, double latitude, double longitude, LocationCallback locationCallback, LocationManager locManager) {
        List<Address> addList = null;
        Geocoder ge = new Geocoder(context);
        try {
            addList = ge.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
            locationCallback.onFailed();
            locManager.removeUpdates(locationListener);
        }

        if (addList != null && addList.size() > 0) {
            Address ad = addList.get(0);
            JLog.i("GPS ??????: " + ad.getCountryName());
            JLog.i("GPS ????????????: " + ad.getCountryCode());
            JLog.i("GPS: ??????" + ad.getAdminArea());
            JLog.i("GPS: ????????????" + ad.getFeatureName());
            locationCallback.onSuccess(ad);

            locManager.removeUpdates(locationListener);
        } else {
            locationCallback.onFailed();
            locManager.removeUpdates(locationListener);
        }
    }

    public static Bitmap getFirstFrameFromVideo(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);
        return retriever.getFrameAtTime(0);
    }

    public static Bitmap getVideoFrame(Context context, Uri uri, long time) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, uri);
            bitmap = retriever.getFrameAtTime(time);

        } catch (RuntimeException ex) {
            ex.printStackTrace();

        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }
        }

        return bitmap;
    }

    //???????????????
    public static void shareText(Context context, String text, String title) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        context.startActivity(Intent.createChooser(intent, title));
    }

    //???????????????
    public static void shareImage(Context context, Uri uri, String title) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        context.startActivity(
                Intent.createChooser(intent, TextUtils.isEmpty(title) ? "????????????" : title));
    }

    //???????????????
    public static void shareMultipleImage(Context context, ArrayList<Uri> imageUris, String title) {
        Intent mulIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        mulIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
        mulIntent.setType("image/*");
        context.startActivity(
                Intent.createChooser(mulIntent, TextUtils.isEmpty(title) ? "??????????????????" : title));
    }

    //????????????
    public static void sendEmail(Context context, String title, String email) {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + email));
        context.startActivity(Intent.createChooser(intent, title));
    }

    /**
     * ???????????????????????????
     *
     * @param context
     * @return
     */
    public static boolean isChinese(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh"))
            return true;
        else
            return false;
    }

    /**
     * ??????????????????
     *
     * @param context
     * @param content
     */
    public static void copyToClipboard(Context context, String content) {
        //????????????????????????
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        //????????????????????????
        cm.setPrimaryClip(ClipData.newPlainText(null, content));
    }

    public static void facebookHashKey(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                JLog.i("KeyHash:" + Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
        } catch (NoSuchAlgorithmException e) {
        }
    }

    /**
     * ????????? getRunningAppProcesses()?????????5.0????????????????????????????????????????????????????????????????????????????????????
     * ?????????400????????????null,????????????????????????Activity?????????
     *
     * @param context
     * @return
     */
    @SuppressLint("DiscouragedPrivateApi")
    public static String getCurrentPkgName(Context context) {
        // 5x??????????????????????????????????????????activity?????????.
        ActivityManager.RunningAppProcessInfo currentInfo = null;
        Field field = null;
        int startTaskToFront = 2;
        String pkgName = null;
        try {
            // ????????????????????????????????????.
            field = ActivityManager.RunningAppProcessInfo.class.getDeclaredField("processState");
        } catch (Exception e) {
            e.printStackTrace();
        }
        ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        List appList = am.getRunningAppProcesses();
        ActivityManager.RunningAppProcessInfo app;
        for (int i = 0; i < appList.size(); i++) {
            //ActivityManager.RunningAppProcessInfo app : appList
            app = (ActivityManager.RunningAppProcessInfo) appList.get(i);
            //????????????????????????.
            if (app.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                Integer state = null;
                try {
                    // ??????????????????????????????,????????????????????????.
                    state = field.getInt(app);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // ?????????????????????????????????????????????????????????????????????
                if (state != null && state == startTaskToFront) {
                    currentInfo = app;
                    break;
                }
            }
        }
        if (currentInfo != null) {
            pkgName = currentInfo.processName;
        }
        return pkgName;
    }

    public static String getTopActivity(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
        if (runningTaskInfo != null) {
            //?????????????????????
            return runningTaskInfo.get(0).topActivity.getPackageName();
        } else {
            return null;
        }
    }
}
