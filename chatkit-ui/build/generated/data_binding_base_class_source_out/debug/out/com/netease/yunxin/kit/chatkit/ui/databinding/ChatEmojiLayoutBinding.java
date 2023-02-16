// Generated by view binder compiler. Do not edit!
package com.netease.yunxin.kit.chatkit.ui.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import androidx.viewpager.widget.ViewPager;
import com.netease.yunxin.kit.chatkit.ui.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ChatEmojiLayoutBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final HorizontalScrollView emojTabViewContainer;

  @NonNull
  public final LinearLayout emojiLayout;

  @NonNull
  public final TextView emojiSendTv;

  @NonNull
  public final LinearLayout emojiTabView;

  @NonNull
  public final LinearLayout layoutScrBottom;

  @NonNull
  public final ViewPager scrPlugin;

  @NonNull
  public final View topDividerLine;

  private ChatEmojiLayoutBinding(@NonNull LinearLayout rootView,
      @NonNull HorizontalScrollView emojTabViewContainer, @NonNull LinearLayout emojiLayout,
      @NonNull TextView emojiSendTv, @NonNull LinearLayout emojiTabView,
      @NonNull LinearLayout layoutScrBottom, @NonNull ViewPager scrPlugin,
      @NonNull View topDividerLine) {
    this.rootView = rootView;
    this.emojTabViewContainer = emojTabViewContainer;
    this.emojiLayout = emojiLayout;
    this.emojiSendTv = emojiSendTv;
    this.emojiTabView = emojiTabView;
    this.layoutScrBottom = layoutScrBottom;
    this.scrPlugin = scrPlugin;
    this.topDividerLine = topDividerLine;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ChatEmojiLayoutBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ChatEmojiLayoutBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.chat_emoji_layout, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ChatEmojiLayoutBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.emoj_tab_view_container;
      HorizontalScrollView emojTabViewContainer = ViewBindings.findChildViewById(rootView, id);
      if (emojTabViewContainer == null) {
        break missingId;
      }

      LinearLayout emojiLayout = (LinearLayout) rootView;

      id = R.id.emoji_send_tv;
      TextView emojiSendTv = ViewBindings.findChildViewById(rootView, id);
      if (emojiSendTv == null) {
        break missingId;
      }

      id = R.id.emoji_tab_view;
      LinearLayout emojiTabView = ViewBindings.findChildViewById(rootView, id);
      if (emojiTabView == null) {
        break missingId;
      }

      id = R.id.layout_scr_bottom;
      LinearLayout layoutScrBottom = ViewBindings.findChildViewById(rootView, id);
      if (layoutScrBottom == null) {
        break missingId;
      }

      id = R.id.scrPlugin;
      ViewPager scrPlugin = ViewBindings.findChildViewById(rootView, id);
      if (scrPlugin == null) {
        break missingId;
      }

      id = R.id.top_divider_line;
      View topDividerLine = ViewBindings.findChildViewById(rootView, id);
      if (topDividerLine == null) {
        break missingId;
      }

      return new ChatEmojiLayoutBinding((LinearLayout) rootView, emojTabViewContainer, emojiLayout,
          emojiSendTv, emojiTabView, layoutScrBottom, scrPlugin, topDividerLine);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}