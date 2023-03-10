// Generated by view binder compiler. Do not edit!
package com.netease.yunxin.kit.chatkit.ui.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.netease.yunxin.kit.chatkit.ui.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityWatchImageVideoBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final ImageView mediaClose;

  @NonNull
  public final FrameLayout mediaContainer;

  @NonNull
  public final ImageView mediaDownload;

  @NonNull
  public final ImageView mediaMore;

  private ActivityWatchImageVideoBinding(@NonNull ConstraintLayout rootView,
      @NonNull ImageView mediaClose, @NonNull FrameLayout mediaContainer,
      @NonNull ImageView mediaDownload, @NonNull ImageView mediaMore) {
    this.rootView = rootView;
    this.mediaClose = mediaClose;
    this.mediaContainer = mediaContainer;
    this.mediaDownload = mediaDownload;
    this.mediaMore = mediaMore;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityWatchImageVideoBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityWatchImageVideoBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_watch_image_video, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityWatchImageVideoBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.media_close;
      ImageView mediaClose = ViewBindings.findChildViewById(rootView, id);
      if (mediaClose == null) {
        break missingId;
      }

      id = R.id.media_container;
      FrameLayout mediaContainer = ViewBindings.findChildViewById(rootView, id);
      if (mediaContainer == null) {
        break missingId;
      }

      id = R.id.media_download;
      ImageView mediaDownload = ViewBindings.findChildViewById(rootView, id);
      if (mediaDownload == null) {
        break missingId;
      }

      id = R.id.media_more;
      ImageView mediaMore = ViewBindings.findChildViewById(rootView, id);
      if (mediaMore == null) {
        break missingId;
      }

      return new ActivityWatchImageVideoBinding((ConstraintLayout) rootView, mediaClose,
          mediaContainer, mediaDownload, mediaMore);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
