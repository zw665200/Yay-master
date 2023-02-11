// Generated by view binder compiler. Do not edit!
package com.netease.yunxin.kit.chatkit.ui.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.netease.yunxin.kit.chatkit.ui.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ChatMessageForwardConfirmLayoutBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final View baseline;

  @NonNull
  public final RecyclerView rvAvatar;

  @NonNull
  public final TextView tvCancel;

  @NonNull
  public final TextView tvMessage;

  @NonNull
  public final TextView tvNickname;

  @NonNull
  public final TextView tvSend;

  @NonNull
  public final TextView tvTitle;

  @NonNull
  public final View verticalLine;

  private ChatMessageForwardConfirmLayoutBinding(@NonNull ConstraintLayout rootView,
      @NonNull View baseline, @NonNull RecyclerView rvAvatar, @NonNull TextView tvCancel,
      @NonNull TextView tvMessage, @NonNull TextView tvNickname, @NonNull TextView tvSend,
      @NonNull TextView tvTitle, @NonNull View verticalLine) {
    this.rootView = rootView;
    this.baseline = baseline;
    this.rvAvatar = rvAvatar;
    this.tvCancel = tvCancel;
    this.tvMessage = tvMessage;
    this.tvNickname = tvNickname;
    this.tvSend = tvSend;
    this.tvTitle = tvTitle;
    this.verticalLine = verticalLine;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ChatMessageForwardConfirmLayoutBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ChatMessageForwardConfirmLayoutBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.chat_message_forward_confirm_layout, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ChatMessageForwardConfirmLayoutBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.baseline;
      View baseline = ViewBindings.findChildViewById(rootView, id);
      if (baseline == null) {
        break missingId;
      }

      id = R.id.rv_avatar;
      RecyclerView rvAvatar = ViewBindings.findChildViewById(rootView, id);
      if (rvAvatar == null) {
        break missingId;
      }

      id = R.id.tv_cancel;
      TextView tvCancel = ViewBindings.findChildViewById(rootView, id);
      if (tvCancel == null) {
        break missingId;
      }

      id = R.id.tv_message;
      TextView tvMessage = ViewBindings.findChildViewById(rootView, id);
      if (tvMessage == null) {
        break missingId;
      }

      id = R.id.tv_nickname;
      TextView tvNickname = ViewBindings.findChildViewById(rootView, id);
      if (tvNickname == null) {
        break missingId;
      }

      id = R.id.tv_send;
      TextView tvSend = ViewBindings.findChildViewById(rootView, id);
      if (tvSend == null) {
        break missingId;
      }

      id = R.id.tv_title;
      TextView tvTitle = ViewBindings.findChildViewById(rootView, id);
      if (tvTitle == null) {
        break missingId;
      }

      id = R.id.verticalLine;
      View verticalLine = ViewBindings.findChildViewById(rootView, id);
      if (verticalLine == null) {
        break missingId;
      }

      return new ChatMessageForwardConfirmLayoutBinding((ConstraintLayout) rootView, baseline,
          rvAvatar, tvCancel, tvMessage, tvNickname, tvSend, tvTitle, verticalLine);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
