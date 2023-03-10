// Generated by view binder compiler. Do not edit!
package com.netease.yunxin.kit.chatkit.ui.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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

public final class ChatUserListLayoutBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final LinearLayout llyEmpty;

  @NonNull
  public final RecyclerView recyclerView;

  @NonNull
  public final TextView tvAllState;

  private ChatUserListLayoutBinding(@NonNull ConstraintLayout rootView,
      @NonNull LinearLayout llyEmpty, @NonNull RecyclerView recyclerView,
      @NonNull TextView tvAllState) {
    this.rootView = rootView;
    this.llyEmpty = llyEmpty;
    this.recyclerView = recyclerView;
    this.tvAllState = tvAllState;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ChatUserListLayoutBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ChatUserListLayoutBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.chat_user_list_layout, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ChatUserListLayoutBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.llyEmpty;
      LinearLayout llyEmpty = ViewBindings.findChildViewById(rootView, id);
      if (llyEmpty == null) {
        break missingId;
      }

      id = R.id.recyclerView;
      RecyclerView recyclerView = ViewBindings.findChildViewById(rootView, id);
      if (recyclerView == null) {
        break missingId;
      }

      id = R.id.tvAllState;
      TextView tvAllState = ViewBindings.findChildViewById(rootView, id);
      if (tvAllState == null) {
        break missingId;
      }

      return new ChatUserListLayoutBinding((ConstraintLayout) rootView, llyEmpty, recyclerView,
          tvAllState);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
