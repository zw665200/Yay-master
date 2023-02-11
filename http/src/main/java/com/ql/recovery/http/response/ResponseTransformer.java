package com.ql.recovery.http.response;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.ql.recovery.config.Config;
import com.ql.recovery.http.exception.ApiException;
import com.ql.recovery.http.exception.CustomException;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;

public class ResponseTransformer {

    public static <T> ObservableTransformer<Response<T>, T> handleResult() {
        return upstream -> upstream
                .onErrorResumeNext(new ErrorResumeFunction<>())
                .flatMap(new ResponseFunction<>());
    }


    /**
     * 非服务器产生的异常，比如本地无无网络请求，Json数据解析错误等等。
     *
     * @param <T>
     */
    private static class ErrorResumeFunction<T> implements Function<Throwable, ObservableSource<? extends Response<T>>> {

        @Override
        public ObservableSource<? extends Response<T>> apply(@NotNull Throwable throwable) throws Exception {
            ApiException apiException = CustomException.handleException(throwable);
            Bundle bundle = new Bundle();
            bundle.putInt("code", apiException.getCode());
            bundle.putString("message", apiException.getDisplayMessage());
            Message msg = new Message();
            msg.setData(bundle);
            msg.what = 0x10004;
            Handler handler = Config.INSTANCE.getMainHandler();
            if (handler != null) {
                handler.sendMessage(msg);
            }
            return Observable.error(apiException);
        }
    }

    /**
     * 服务其返回的数据解析
     * 正常服务器返回数据和服务器可能返回的exception
     *
     * @param <T>
     */
    private static class ResponseFunction<T> implements Function<Response<T>, ObservableSource<T>> {

        @Override
        public ObservableSource<T> apply(Response<T> tResponse) throws Exception {
            int code = tResponse.getCode();
            String message = tResponse.getMessage();
            if (code == 10000) {
                T data = tResponse.getData();
                if (data != null) {
                    return Observable.just(Objects.requireNonNull(data));
                }
            } else {
                Bundle bundle = new Bundle();
                bundle.putInt("code", code);
                bundle.putString("message", message);
                Message msg = new Message();
                msg.setData(bundle);
                msg.what = 0x10004;
                Handler handler = Config.INSTANCE.getMainHandler();
                if (handler != null) {
                    handler.sendMessage(msg);
                }
            }

            return Observable.error(new ApiException(code, message));

        }
    }
}
