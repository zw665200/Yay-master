package com.ql.recovery.yay.ui.base;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.event.EventSubscribeServiceObserver;
import com.netease.nimlib.sdk.event.model.Event;
import com.ql.recovery.bean.Subscriber;
import com.ql.recovery.config.Config;
import com.ql.recovery.yay.R;
import com.ql.recovery.yay.databinding.ActivityBaseBinding;
import com.ql.recovery.yay.manager.DBManager;
import com.ql.recovery.yay.util.JLog;
import com.tencent.mmkv.MMKV;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by fish on 16-4-25.
 */
abstract public class BaseFragmentActivity extends BaseActivity {
    private Class<? extends BaseFragment>[] fCls = null;

    //fragment repository res id
    private int flMainId;
    //bottom buttons
    private LinearLayout llBottom = null;

    public BaseFragment[] fragments = null;

    private int mDefaultPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityBaseBinding baseBinding = ActivityBaseBinding.inflate(getLayoutInflater());
        setContentView(baseBinding.getRoot());
        getViewBinding(baseBinding);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.transparent));
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(baseBinding.getRoot());
            if (controller != null) {
                controller.show(WindowInsetsCompat.Type.statusBars());
                controller.setAppearanceLightNavigationBars(false);
            }
        }

        fCls = putFragments();
        fragments = new BaseFragment[fCls.length];
        flMainId = getFLid();
        llBottom = getBottomLayout();
        initBaseView();
        initView();
        initData();
        setTabSel(llBottom.getChildAt(mDefaultPage), mDefaultPage);
    }

    private void initBaseView() {
        for (int i = 0; i < fCls.length; i++) {
            final int index = i;
            View v = getBottomItemView(index);
            v.setOnClickListener(v1 -> setTabSel(v1, index));
            llBottom.addView(v);
        }
    }


    protected void setTabSel(View item, int index) {
        onItemClick(item, index);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        for (int i = 0; i < fCls.length; i++) {
            checkAllBottomItem(llBottom.getChildAt(i), i, false);
            if (i == index) {
                checkAllBottomItem(llBottom.getChildAt(index), index, true);
                if (fragments[index] == null) {
                    try {
                        BaseFragment bf = fCls[index].newInstance();
                        fragments[index] = bf;
                        ft.add(flMainId, bf);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    ft.show(fragments[index]);
                    fragments[index].initData();
                    fragments[index].onResume();
                }
            } else if (fragments[i] != null) {
                ft.hide(fragments[i]);
            }
        }
        ft.commitAllowingStateLoss();
    }

    /**
     * On action button click callback
     *
     * @param item  The clicked item
     * @param index The position
     */
    protected abstract void onItemClick(View item, int index);

    protected LayoutInflater getBottomLayoutInflater() {
        return LayoutInflater.from(this);
    }

    /**
     * Do operations after abstract methods called.
     * U can do onCreate after abstract methods called.
     */
    protected abstract void initView();

    protected abstract void initData();

    protected abstract void getViewBinding(@NonNull ActivityBaseBinding baseBinding);

    /**
     * @return Array of Fragments'class
     */
    protected abstract Class<? extends BaseFragment>[] putFragments();

    /**
     * @param index item's position
     * @return //Return Action Click bar's item at index
     */
    protected abstract View getBottomItemView(int index);

    /**
     * @return The repository of Fragments --> Resource id
     */
    protected abstract int getFLid();

    /**
     * @return The repository of Action buttons at bottom normally.
     */
    protected abstract LinearLayout getBottomLayout();

    /**
     * The method is used for fresh ui state.
     * The method will be called on every item when checked the item.
     * Must Only do UI operation!
     *
     * @param item      The checked item
     * @param position  Item's position
     * @param isChecked Whether the item is checked
     */
    protected abstract void checkAllBottomItem(View item, int position, boolean isChecked);

    protected void flushUserInfo() {
        for (BaseFragment f : fragments) {
            try {
                f.flushUserInfo();
            } catch (Exception ex) {
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (BaseFragment f : fragments) {
            try {
                f.onActivityResume();
            } catch (Exception ex) {
            }
        }
    }

    @NonNull
    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        if (res.getConfiguration().fontScale != 1) {//非默认值
            Configuration newConfig = new Configuration();
            newConfig.setToDefaults();//设置默认
            res.updateConfiguration(newConfig, res.getDisplayMetrics());
        }
        return res;
    }
}
