package com.ql.recovery.yay.ui.self;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.ql.recovery.yay.callback.IKeyEventDispatcher;

/**
 * @author Herr_Z
 * @description:
 * @date : 2023/3/10 9:52
 */
public class KeyEventFrameLayout extends FrameLayout implements IKeyEventDispatcher {

    KeyEventListener listener;
    int x;
    int y;

    public KeyEventFrameLayout(Context context) {
        super(context);
    }

    public KeyEventFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KeyEventFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setKeyEventListener(KeyEventListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (listener == null)
            return super.dispatchKeyEvent(event);
        return listener.dispatchKeyEvent(event);
    }
}
