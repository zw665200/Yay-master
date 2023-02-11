package com.ql.recovery.util;

import android.util.Log;

import com.ql.recovery.config.Config;

public class JLog {

    /**
     * 调试开关
     */

    private static final String TAG = "JXWL";

    /**
     * 错误级别日志
     */
    public static void e(String tag, String log) {
        if (Config.INSTANCE.isDebug()) {
            Log.e(tag, log);
        }
    }

    /**
     * 警告级别日志
     */
    public static void w(String tag, String log) {
        if (Config.INSTANCE.isDebug()) {
            Log.w(tag, log);
        }
    }

    /**
     * 信息级别日志
     */
    public static void i(String tag, String log) {
        if (Config.INSTANCE.isDebug()) {
            Log.i(tag, log);
        }
    }

    /**
     * 调试级别日志
     */
    public static void d(String tag, String log) {
        if (Config.INSTANCE.isDebug()) {
            Log.d(tag, log);
        }
    }

    /**  */
    /**
     * 错误级别日志
     */
    public static void e(String log) {
        if (Config.INSTANCE.isDebug()) {
            Log.e(TAG, log);
        }
    }

    /**
     * 警告级别日志
     */
    public static void w(String log) {
        if (Config.INSTANCE.isDebug()) {
            Log.w(TAG, log);
        }
    }

    /**
     * 信息级别日志
     */
    public static void i(String log) {
        if (Config.INSTANCE.isDebug()) {
            Log.i(TAG, log);
        }
    }

    /**
     * 调试级别日志
     */
    public static void d(String log) {
        if (Config.INSTANCE.isDebug()) {
            Log.d(TAG, log);
        }
    }
}
