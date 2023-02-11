// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.popmenu;

import android.text.TextUtils;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ChatActionFactory {

  private static volatile ChatActionFactory instance;

  private WeakReference<IChatPopMenuClickListener> actionListener;

  private WeakReference<IChatPopMenu> customPopMenu;

  private ChatActionFactory() {}

  public static ChatActionFactory getInstance() {
    if (instance == null) {
      synchronized (ChatActionFactory.class) {
        if (instance == null) {
          instance = new ChatActionFactory();
        }
      }
    }
    return instance;
  }

  public void setActionListener(IChatPopMenuClickListener actionListener) {
    this.actionListener = new WeakReference<>(actionListener);
  }

  public void setChatPopMenu(IChatPopMenu popMenu) {
    this.customPopMenu = new WeakReference<>(popMenu);
  }

  public List<ChatPopMenuAction> getNormalActions(ChatMessageBean message) {
    List<ChatPopMenuAction> actions = new ArrayList<>();
    if (message.getMessageData() == null) {
      return actions;
    }
    if (customPopMenu == null
        || customPopMenu.get() == null
        || customPopMenu.get().showDefaultPopMenu()) {
      if (message.getMessageData().getMessage().getStatus() == MsgStatusEnum.fail
          || message.getMessageData().getMessage().getStatus() == MsgStatusEnum.sending) {
        if (message.getViewType() == MsgTypeEnum.text.getValue()) {
          actions.add(getCopyAction(message));
        }
        actions.add(getDeleteAction(message));
        return actions;
      }
      if (message.getViewType() == MsgTypeEnum.text.getValue()) {
        //text
        actions.add(getCopyAction(message));
      }
      actions.add(getReplyAction(message));
      if (message.getViewType() != MsgTypeEnum.audio.getValue()) {
        actions.add(getTransmitAction(message));
      }
      if (message.getViewType() != MsgTypeEnum.location.getValue()) {
        actions.add(getPinAction(message));
      }
      //    actions.add(getMultiSelectAction(message));
      //    actions.add(getCollectionAction(message));
      actions.add(getDeleteAction(message));
      if (message.getMessageData().getMessage().getDirect() == MsgDirectionEnum.Out) {
        actions.add(getRecallAction(message));
      }
    }
    if (customPopMenu != null && customPopMenu.get() != null) {
      return customPopMenu.get().customizePopMenu(actions, message);
    }
    return actions;
  }

  private ChatPopMenuAction getReplyAction(ChatMessageBean message) {
    return new ChatPopMenuAction(
        ActionConstants.POP_ACTION_REPLY,
        R.string.chat_message_action_reply,
        R.drawable.ic_message_reply,
        (view, messageInfo) -> {
          if (actionListener != null) {
            actionListener.get().onReply(messageInfo);
          }
        });
  }

  private ChatPopMenuAction getCopyAction(ChatMessageBean message) {
    return new ChatPopMenuAction(
        ActionConstants.POP_ACTION_COPY,
        R.string.chat_message_action_copy,
        R.drawable.ic_message_copy,
        (view, messageInfo) -> {
          if (actionListener != null) {
            actionListener.get().onCopy(messageInfo);
          }
        });
  }

  private ChatPopMenuAction getRecallAction(ChatMessageBean message) {
    return new ChatPopMenuAction(
        ActionConstants.POP_ACTION_RECALL,
        R.string.chat_message_action_recall,
        R.drawable.ic_message_recall,
        (view, messageInfo) -> {
          if (actionListener != null) {
            actionListener.get().onRecall(messageInfo);
          }
        });
  }

  private ChatPopMenuAction getPinAction(ChatMessageBean message) {
    return new ChatPopMenuAction(
        ActionConstants.POP_ACTION_PIN,
        !TextUtils.isEmpty(message.getPinAccid())
            ? R.string.chat_message_action_pin_cancel
            : R.string.chat_message_action_pin,
        R.drawable.ic_message_sign,
        (view, messageInfo) -> {
          if (actionListener != null) {
            actionListener
                .get()
                .onSignal(messageInfo, !TextUtils.isEmpty(messageInfo.getPinAccid()));
          }
        });
  }

  private ChatPopMenuAction getMultiSelectAction(ChatMessageBean message) {
    return new ChatPopMenuAction(
        ActionConstants.POP_ACTION_MULTI_SELECT,
        R.string.chat_message_action_multi_select,
        R.drawable.ic_message_multi_select,
        (view, messageInfo) -> {
          if (actionListener != null) {
            actionListener.get().onMultiSelected(messageInfo);
          }
        });
  }

  private ChatPopMenuAction getCollectionAction(ChatMessageBean message) {
    return new ChatPopMenuAction(
        ActionConstants.POP_ACTION_COLLECTION,
        R.string.chat_message_action_collection,
        R.drawable.ic_message_collection,
        (view, messageInfo) -> {
          if (actionListener != null) {
            actionListener.get().onCollection(messageInfo);
          }
        });
  }

  private ChatPopMenuAction getDeleteAction(ChatMessageBean message) {
    return new ChatPopMenuAction(
        ActionConstants.POP_ACTION_DELETE,
        R.string.chat_message_action_delete,
        R.drawable.ic_message_delete,
        (view, messageInfo) -> {
          if (actionListener != null) {
            actionListener.get().onDelete(message);
          }
        });
  }

  private ChatPopMenuAction getTransmitAction(ChatMessageBean message) {
    return new ChatPopMenuAction(
        ActionConstants.POP_ACTION_TRANSMIT,
        R.string.chat_message_action_transmit,
        R.drawable.ic_message_transmit,
        (view, messageInfo) -> {
          if (actionListener != null) {
            actionListener.get().onForward(messageInfo);
          }
        });
  }
}
