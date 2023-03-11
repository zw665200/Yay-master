package com.ql.recovery.yay.callback;

import android.view.KeyEvent;

/**
 * @author Herr_Z
 * @description:
 * @date : 2023/3/10 9:58
 */
public interface IKeyEventDispatcher {

    boolean dispatchKeyEvent(KeyEvent event);

    void setKeyEventListener(KeyEventListener listener);

    interface KeyEventListener {
        boolean dispatchKeyEvent(KeyEvent event);
    }
}