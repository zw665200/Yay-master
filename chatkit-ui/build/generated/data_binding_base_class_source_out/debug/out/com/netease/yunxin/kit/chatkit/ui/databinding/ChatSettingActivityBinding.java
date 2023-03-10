// Generated by view binder compiler. Do not edit!
package com.netease.yunxin.kit.chatkit.ui.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;
import com.netease.yunxin.kit.common.ui.widgets.ContactAvatarView;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ChatSettingActivityBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final ContactAvatarView avatar;

  @NonNull
  public final ImageView ivAdd;

  @NonNull
  public final RelativeLayout rlyMessageNotice;

  @NonNull
  public final RelativeLayout rlySessionTop;

  @NonNull
  public final RelativeLayout rlySignal;

  @NonNull
  public final SwitchCompat scMessageNotice;

  @NonNull
  public final SwitchCompat scSessionTop;

  @NonNull
  public final BackTitleBar title;

  @NonNull
  public final TextView tvName;

  private ChatSettingActivityBinding(@NonNull LinearLayout rootView,
      @NonNull ContactAvatarView avatar, @NonNull ImageView ivAdd,
      @NonNull RelativeLayout rlyMessageNotice, @NonNull RelativeLayout rlySessionTop,
      @NonNull RelativeLayout rlySignal, @NonNull SwitchCompat scMessageNotice,
      @NonNull SwitchCompat scSessionTop, @NonNull BackTitleBar title, @NonNull TextView tvName) {
    this.rootView = rootView;
    this.avatar = avatar;
    this.ivAdd = ivAdd;
    this.rlyMessageNotice = rlyMessageNotice;
    this.rlySessionTop = rlySessionTop;
    this.rlySignal = rlySignal;
    this.scMessageNotice = scMessageNotice;
    this.scSessionTop = scSessionTop;
    this.title = title;
    this.tvName = tvName;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ChatSettingActivityBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ChatSettingActivityBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.chat_setting_activity, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ChatSettingActivityBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.avatar;
      ContactAvatarView avatar = ViewBindings.findChildViewById(rootView, id);
      if (avatar == null) {
        break missingId;
      }

      id = R.id.ivAdd;
      ImageView ivAdd = ViewBindings.findChildViewById(rootView, id);
      if (ivAdd == null) {
        break missingId;
      }

      id = R.id.rly_message_notice;
      RelativeLayout rlyMessageNotice = ViewBindings.findChildViewById(rootView, id);
      if (rlyMessageNotice == null) {
        break missingId;
      }

      id = R.id.rly_session_top;
      RelativeLayout rlySessionTop = ViewBindings.findChildViewById(rootView, id);
      if (rlySessionTop == null) {
        break missingId;
      }

      id = R.id.rly_signal;
      RelativeLayout rlySignal = ViewBindings.findChildViewById(rootView, id);
      if (rlySignal == null) {
        break missingId;
      }

      id = R.id.sc_message_notice;
      SwitchCompat scMessageNotice = ViewBindings.findChildViewById(rootView, id);
      if (scMessageNotice == null) {
        break missingId;
      }

      id = R.id.sc_session_top;
      SwitchCompat scSessionTop = ViewBindings.findChildViewById(rootView, id);
      if (scSessionTop == null) {
        break missingId;
      }

      id = R.id.title;
      BackTitleBar title = ViewBindings.findChildViewById(rootView, id);
      if (title == null) {
        break missingId;
      }

      id = R.id.tvName;
      TextView tvName = ViewBindings.findChildViewById(rootView, id);
      if (tvName == null) {
        break missingId;
      }

      return new ChatSettingActivityBinding((LinearLayout) rootView, avatar, ivAdd,
          rlyMessageNotice, rlySessionTop, rlySignal, scMessageNotice, scSessionTop, title, tvName);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
