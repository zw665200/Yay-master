// Generated by view binder compiler. Do not edit!
package com.netease.yunxin.kit.chatkit.ui.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.view.media.PhotoView;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class WatchImageViewHolderBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final CircularProgressIndicator watchImageLoading;

  @NonNull
  public final AppCompatImageView watchImageView;

  @NonNull
  public final PhotoView watchPhotoView;

  private WatchImageViewHolderBinding(@NonNull ConstraintLayout rootView,
      @NonNull CircularProgressIndicator watchImageLoading,
      @NonNull AppCompatImageView watchImageView, @NonNull PhotoView watchPhotoView) {
    this.rootView = rootView;
    this.watchImageLoading = watchImageLoading;
    this.watchImageView = watchImageView;
    this.watchPhotoView = watchPhotoView;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static WatchImageViewHolderBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static WatchImageViewHolderBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.watch_image_view_holder, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static WatchImageViewHolderBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.watch_image_loading;
      CircularProgressIndicator watchImageLoading = ViewBindings.findChildViewById(rootView, id);
      if (watchImageLoading == null) {
        break missingId;
      }

      id = R.id.watch_image_view;
      AppCompatImageView watchImageView = ViewBindings.findChildViewById(rootView, id);
      if (watchImageView == null) {
        break missingId;
      }

      id = R.id.watch_photo_view;
      PhotoView watchPhotoView = ViewBindings.findChildViewById(rootView, id);
      if (watchPhotoView == null) {
        break missingId;
      }

      return new WatchImageViewHolderBinding((ConstraintLayout) rootView, watchImageLoading,
          watchImageView, watchPhotoView);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
