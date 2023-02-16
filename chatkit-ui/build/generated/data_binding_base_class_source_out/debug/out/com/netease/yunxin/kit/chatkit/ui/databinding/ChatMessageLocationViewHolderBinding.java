// Generated by view binder compiler. Do not edit!
package com.netease.yunxin.kit.chatkit.ui.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.common.ui.widgets.RoundFrameLayout;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ChatMessageLocationViewHolderBinding implements ViewBinding {
  @NonNull
  private final RoundFrameLayout rootView;

  @NonNull
  public final View locationClick;

  @NonNull
  public final TextView locationItemAddress;

  @NonNull
  public final FrameLayout locationItemMapView;

  @NonNull
  public final TextView locationItemTitle;

  private ChatMessageLocationViewHolderBinding(@NonNull RoundFrameLayout rootView,
      @NonNull View locationClick, @NonNull TextView locationItemAddress,
      @NonNull FrameLayout locationItemMapView, @NonNull TextView locationItemTitle) {
    this.rootView = rootView;
    this.locationClick = locationClick;
    this.locationItemAddress = locationItemAddress;
    this.locationItemMapView = locationItemMapView;
    this.locationItemTitle = locationItemTitle;
  }

  @Override
  @NonNull
  public RoundFrameLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ChatMessageLocationViewHolderBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ChatMessageLocationViewHolderBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.chat_message_location_view_holder, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ChatMessageLocationViewHolderBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.location_click;
      View locationClick = ViewBindings.findChildViewById(rootView, id);
      if (locationClick == null) {
        break missingId;
      }

      id = R.id.location_item_address;
      TextView locationItemAddress = ViewBindings.findChildViewById(rootView, id);
      if (locationItemAddress == null) {
        break missingId;
      }

      id = R.id.location_item_map_view;
      FrameLayout locationItemMapView = ViewBindings.findChildViewById(rootView, id);
      if (locationItemMapView == null) {
        break missingId;
      }

      id = R.id.location_item_title;
      TextView locationItemTitle = ViewBindings.findChildViewById(rootView, id);
      if (locationItemTitle == null) {
        break missingId;
      }

      return new ChatMessageLocationViewHolderBinding((RoundFrameLayout) rootView, locationClick,
          locationItemAddress, locationItemMapView, locationItemTitle);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}