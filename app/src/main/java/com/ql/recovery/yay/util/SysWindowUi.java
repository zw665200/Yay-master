package com.ql.recovery.yay.util;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

/**
 * @author Herr_Z
 * @description:
 * @date : 2023/1/12 11:10
 */
public class SysWindowUi {

    /**
     * noStateBarAndNavigationBar 为true时，隐藏系统状态栏和导航栏用于加载页，
     * noStateBarAndNavigationBar 为false时，透明沉浸式系统状态栏和导航栏用于其他页面
     *
     * @param activity
     * @param noStateBarAndNavigationBar
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP) // 跳过低版本没有此api的编译报错
    public static void hideStatusNavigationBar(Activity activity, Boolean noStateBarAndNavigationBar) {

        if (Build.VERSION.SDK_INT < 16) {
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {

            if (Build.VERSION.SDK_INT >= 28) {
                // 设置窗口占用刘海区
                WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
                lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                activity.getWindow().setAttributes(lp);
            }

            int uiFlags =
                    // 稳定布局(当StatusBar和NavigationBar动态显示和隐藏时，系统为fitSystemWindow=true的view设置的padding大小都不会变化，所以view的内容的位置也不会发生移动。)
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            // 主体内容占用系统导航栏的空间
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            // 沉浸式(避免某些用户交互造成系统自动清除全屏状态。)
                            | View.SYSTEM_UI_FLAG_IMMERSIVE;


            if (!noStateBarAndNavigationBar) {
                uiFlags = uiFlags
                        // 在不隐藏StatusBar状态栏的情况下，将view所在window的显示范围扩展到StatusBar下面
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // 状态栏字体颜色设置为黑色这个是Android 6.0才出现的属性   默认是白色
                        | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;

            } else {
                // 隐藏导航栏
                uiFlags |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

                if (Build.VERSION.SDK_INT >= 28) {
                    // 隐藏状态栏
                    uiFlags |= View.SYSTEM_UI_FLAG_FULLSCREEN;
                } else {
                    // 版本小于28不能设置占用刘海区，加载页布局使用白色背景，达到同样的效果
                    uiFlags = uiFlags
                            // 状态栏显示处于低能显示状态(low profile模式)，状态栏上一些图标显示会被隐藏
                            | View.SYSTEM_UI_FLAG_LOW_PROFILE
                            // 在不隐藏StatusBar状态栏的情况下，将view所在window的显示范围扩展到StatusBar下面
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                }

            }

            activity.getWindow().getDecorView().setSystemUiVisibility(uiFlags);

            //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            //将导航栏设置成透明色
            activity.getWindow().setNavigationBarColor(Color.TRANSPARENT);

            //将状态栏设置成透明色
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);

        }

    }
}
