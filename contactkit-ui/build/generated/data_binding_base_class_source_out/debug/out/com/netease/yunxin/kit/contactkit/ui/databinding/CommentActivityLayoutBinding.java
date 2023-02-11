// Generated by view binder compiler. Do not edit!
package com.netease.yunxin.kit.contactkit.ui.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;
import com.netease.yunxin.kit.common.ui.widgets.CleanableEditText;
import com.netease.yunxin.kit.contactkit.ui.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class CommentActivityLayoutBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final CleanableEditText edtComment;

  @NonNull
  public final BackTitleBar title;

  private CommentActivityLayoutBinding(@NonNull LinearLayout rootView,
      @NonNull CleanableEditText edtComment, @NonNull BackTitleBar title) {
    this.rootView = rootView;
    this.edtComment = edtComment;
    this.title = title;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static CommentActivityLayoutBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static CommentActivityLayoutBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.comment_activity_layout, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static CommentActivityLayoutBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.edt_comment;
      CleanableEditText edtComment = ViewBindings.findChildViewById(rootView, id);
      if (edtComment == null) {
        break missingId;
      }

      id = R.id.title;
      BackTitleBar title = ViewBindings.findChildViewById(rootView, id);
      if (title == null) {
        break missingId;
      }

      return new CommentActivityLayoutBinding((LinearLayout) rootView, edtComment, title);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
