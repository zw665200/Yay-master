package com.ql.recovery.util;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.ql.recovery.bean.BirthDay;
import com.ql.recovery.core.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.security.MessageDigest;
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

    public static String MD5Encode(String origin) {
        String resultString = null;
        try {
            resultString = new String(origin);
            MessageDigest md = MessageDigest.getInstance("MD5");
            resultString = byteArrayToHexString(md.digest(resultString.getBytes()));
        } catch (Exception ex) {

        }
        return resultString;
    }

    public static String MD5Encode(byte[] origin) {
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
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.CHINA);
        return sdf.format(new Date(timeStamp));
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
            Toast.makeText(activity, "?????????????????????QQ", Toast.LENGTH_SHORT).show();
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

        if (notification != null) {
            notifyMgr.notify(1, notification);
        }
    }

    public static Notification getNotification(Context context, String title, String content) {
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

}
