// Generated by view binder compiler. Do not edit!
package com.netease.yunxin.kit.chatkit.ui.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.common.ui.widgets.ContactAvatarView;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ChatMessageAitContactViewHolderBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final ContactAvatarView contactHeader;

  @NonNull
  public final TextView contactName;

  private ChatMessageAitContactViewHolderBinding(@NonNull ConstraintLayout rootView,
      @NonNull ContactAvatarView contactHeader, @NonNull TextView contactName) {
    this.rootView = rootView;
    this.contactHeader = contactHeader;
    this.contactName = contactName;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ChatMessageAitContactViewHolderBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ChatMessageAitContactViewHolderBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.chat_message_ait_contact_view_holder, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ChatMessageAitContactViewHolderBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.contact_header;
      ContactAvatarView contactHeader = ViewBindings.findChildViewById(rootView, id);
      if (contactHeader == null) {
        break missingId;
      }

      id = R.id.contact_name;
      TextView contactName = ViewBindings.findChildViewById(rootView, id);
      if (contactName == null) {
        break missingId;
      }

      return new ChatMessageAitContactViewHolderBinding((ConstraintLayout) rootView, contactHeader,
          contactName);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}