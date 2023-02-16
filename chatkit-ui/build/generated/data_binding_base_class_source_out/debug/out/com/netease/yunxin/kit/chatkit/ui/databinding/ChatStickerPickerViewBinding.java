// Generated by view binder compiler. Do not edit!
package com.netease.yunxin.kit.chatkit.ui.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.netease.yunxin.kit.chatkit.ui.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ChatStickerPickerViewBinding implements ViewBinding {
  @NonNull
  private final RelativeLayout rootView;

  @NonNull
  public final TextView stickerDescLabel;

  @NonNull
  public final ImageView stickerThumbImage;

  private ChatStickerPickerViewBinding(@NonNull RelativeLayout rootView,
      @NonNull TextView stickerDescLabel, @NonNull ImageView stickerThumbImage) {
    this.rootView = rootView;
    this.stickerDescLabel = stickerDescLabel;
    this.stickerThumbImage = stickerThumbImage;
  }

  @Override
  @NonNull
  public RelativeLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ChatStickerPickerViewBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ChatStickerPickerViewBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.chat_sticker_picker_view, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ChatStickerPickerViewBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.sticker_desc_label;
      TextView stickerDescLabel = ViewBindings.findChildViewById(rootView, id);
      if (stickerDescLabel == null) {
        break missingId;
      }

      id = R.id.sticker_thumb_image;
      ImageView stickerThumbImage = ViewBindings.findChildViewById(rootView, id);
      if (stickerThumbImage == null) {
        break missingId;
      }

      return new ChatStickerPickerViewBinding((RelativeLayout) rootView, stickerDescLabel,
          stickerThumbImage);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}