package com.ql.recovery.yay.ui.self;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.LinearLayoutManager;

/**
 * @author Herr_Z
 * @description:
 * @date : 2023/2/16 16:01
 */
public class SelfLinearLayoutManager extends LinearLayoutManager {

    public SelfLinearLayoutManager(Context context) {
        super(context);
    }

    public SelfLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public SelfLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean canScrollVertically() {
        return false;
    }
}
