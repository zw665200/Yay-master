// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatActivityBinding;
import com.netease.yunxin.kit.chatkit.ui.view.message.audio.ChatMessageAudioControl;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;

/**
 * BaseActivity for Chat include P2P chat page and Team chat page
 */
public abstract class ChatBaseActivity extends BaseActivity {

    ChatActivityBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ChatActivityBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        setStatusBarLight();
        initChat();
    }

    protected void setStatusBarLight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(binding.getRoot());
            if (controller != null) {
                controller.show(WindowInsetsCompat.Type.statusBars());
                controller.setAppearanceLightStatusBars(true);
            }
        }
    }

    protected abstract void initChat();

    @Override
    protected void onStop() {
        super.onStop();
        //stop message audio
        ChatMessageAudioControl.getInstance().stopAudio();
    }
}
