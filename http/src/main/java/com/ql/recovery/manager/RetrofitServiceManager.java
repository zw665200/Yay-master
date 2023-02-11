package com.ql.recovery.manager;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.ql.recovery.config.Config;
import com.ql.recovery.http.ApiConfig;
import com.ql.recovery.http.schedulers.HttpCommonInterceptor;
import com.ql.recovery.util.AppUtil;
import com.ql.recovery.util.HarmonyUtils;
import com.ql.recovery.http.request.BaseService;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitServiceManager {

    private static final int DEFAULT_TIME_OUT = 10;
    private static final int DEFAULT_READ_TIME_OUT = 10;
    private Retrofit mRetrofit;
    private static RetrofitServiceManager mInstance;
    private static volatile BaseService baseService = null;

    public static RetrofitServiceManager get() {
        if (mInstance == null) {
            synchronized (RetrofitServiceManager.class) {
                if (mInstance == null) {
                    mInstance = new RetrofitServiceManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化retrofit
     */
    public void initRetrofitService(Context context) {
        // 创建 OKHttpClient
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS);//连接超时时间
        builder.writeTimeout(DEFAULT_READ_TIME_OUT, TimeUnit.SECONDS);//写操作 超时时间
        builder.readTimeout(DEFAULT_READ_TIME_OUT, TimeUnit.SECONDS);//读操作超时时间
//        builder.addInterceptor(new BaseUrlInterceptor());

        //打印网络请求日志
        if (Config.INSTANCE.isDebug()) {
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(httpLoggingInterceptor);
        }

        // 添加公共参数拦截器
        if (HarmonyUtils.isHarmonyOs()) {
            HttpCommonInterceptor commonInterceptor = new HttpCommonInterceptor.Builder()
                    .addHeaderParams("User-Agent", "Yay/" + AppUtil.getPackageVersionName(context, context.getPackageName())
                            + "(build:" + AppUtil.getPackageVersionCode(context, context.getPackageName()) + "; Android: " + Build.VERSION.RELEASE
                            + "; " + Build.BRAND + ":" + Build.MODEL + "; HarmonyOS: " + HarmonyUtils.getHarmonyVersion() +
                            ")")
                    .build();
            builder.addInterceptor(commonInterceptor);
        } else {
            HttpCommonInterceptor commonInterceptor = new HttpCommonInterceptor.Builder()
                    .addHeaderParams("User-Agent", "Yay/" + AppUtil.getPackageVersionName(context, context.getPackageName())
                            + "(build:" + AppUtil.getPackageVersionCode(context, context.getPackageName()) + "; Android: " + Build.VERSION.RELEASE
                            + "; " + Build.BRAND + ":" + Build.MODEL + ")")
                    .build();
            builder.addInterceptor(commonInterceptor);
        }

        setRxJavaErrorHandler();


        // 创建Retrofit
        mRetrofit = new Retrofit.Builder()
                .client(builder.build())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(ApiConfig.BASE_URL)
                .build();
    }

    public BaseService getBaseService() {
        if (baseService == null) {
            synchronized (BaseService.class) {
                baseService = mRetrofit.create(BaseService.class);
            }
        }
        return baseService;
    }

    private void setRxJavaErrorHandler() {
        if (RxJavaPlugins.getErrorHandler() != null || RxJavaPlugins.isLockdown()) {
            Log.d("App", "setRxJavaErrorHandler getErrorHandler()!=null||isLockdown()");
            return;
        }
        RxJavaPlugins.setErrorHandler(e -> {
            if (e instanceof UndeliverableException) {
                e = e.getCause();
                Log.d("App", "setRxJavaErrorHandler UndeliverableException=" + e);
                return;
            } else if ((e instanceof IOException)) {
                // fine, irrelevant network problem or API that throws on cancellation
                return;
            } else if (e instanceof InterruptedException) {
                // fine, some blocking code was interrupted by a dispose call
                return;
            } else if ((e instanceof NullPointerException) || (e instanceof IllegalArgumentException)) {
                // that's likely a bug in the application
                Thread.UncaughtExceptionHandler uncaughtExceptionHandler =
                        Thread.currentThread().getUncaughtExceptionHandler();
                if (uncaughtExceptionHandler != null) {
                    uncaughtExceptionHandler.uncaughtException(Thread.currentThread(), e);
                }
                return;
            } else if (e instanceof IllegalStateException) {
                // that's a bug in RxJava or in a custom operator
                Thread.UncaughtExceptionHandler uncaughtExceptionHandler =
                        Thread.currentThread().getUncaughtExceptionHandler();
                if (uncaughtExceptionHandler != null) {
                    uncaughtExceptionHandler.uncaughtException(Thread.currentThread(), e);
                }
                return;
            }
            Log.d("App", "setRxJavaErrorHandler unknown exception=" + e);
        });
    }


    /**
     * 获取对应的Service
     *
     * @param service Service 的 class
     * @param <T>
     * @return
     */
    public <T> T create(Class<T> service) {
        return mRetrofit.create(service);
    }

}
