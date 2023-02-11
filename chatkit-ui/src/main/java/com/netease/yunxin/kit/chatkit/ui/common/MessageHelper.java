// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.common;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.model.IMMessageRecord;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.ui.ChatCustom;
import com.netease.yunxin.kit.chatkit.ui.ChatKitClient;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.model.FriendInfo;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import java.util.ArrayList;
import java.util.List;

public class MessageHelper {

  private static ChatCustom chatCustom = new ChatCustom();
  /**
   * get nickName display
   *
   * @param tid team id
   * @param account user accId
   */
  public static String getTeamMemberDisplayNameYou(String tid, String account) {
    if (account.equals(IMKitClient.account())) {
      return IMKitClient.getApplicationContext().getString(R.string.chat_you);
    }

    FriendInfo friend = ChatRepo.getFriendInfo(account);
    if (friend != null && !TextUtils.isEmpty(friend.getAlias())) {
      return friend.getAlias();
    }

    String memberNick = getTeamNick(tid, account);
    if (!TextUtils.isEmpty(memberNick)) {
      return memberNick;
    }

    UserInfo user = ChatRepo.getUserInfo(account);
    return user == null ? account : (TextUtils.isEmpty(user.getName()) ? account : user.getName());
  }

  public static void getChatDisplayNameYou(
      String tid, String account, FetchCallback<String> callback) {
    String nick = null;
    if (account.equals(IMKitClient.account())) {
      nick = IMKitClient.getApplicationContext().getString(R.string.chat_you);
      callback.onSuccess(nick);
      return;
    }

    FriendInfo friend = ChatRepo.getFriendInfo(account);
    if (friend != null && !TextUtils.isEmpty(friend.getAlias())) {
      nick = friend.getAlias();
      callback.onSuccess(nick);
      return;
    }

    if (!TextUtils.isEmpty(tid)) {
      nick = getTeamNick(tid, account);
      if (!TextUtils.isEmpty(nick)) {
        callback.onSuccess(nick);
        return;
      }
    }

    if (nick == null) {
      ChatRepo.fetchUserInfo(
          account,
          new FetchCallback<UserInfo>() {
            @Override
            public void onSuccess(@Nullable UserInfo user) {
              String nick =
                  user == null
                      ? null
                      : (TextUtils.isEmpty(user.getName()) ? account : user.getName());
              callback.onSuccess(nick);
            }

            @Override
            public void onFailed(int code) {
              callback.onSuccess(account);
            }

            @Override
            public void onException(@Nullable Throwable exception) {
              callback.onSuccess(account);
            }
          });
    } else {
      callback.onSuccess(account);
    }
  }

  public static String getTeamNick(String tid, String account) {
    Team team = ChatRepo.getTeamInfo(tid);
    if (team != null && team.getType() == TeamTypeEnum.Advanced) {
      TeamMember member = ChatRepo.getTeamMember(tid, account);
      if (member != null && !TextUtils.isEmpty(member.getTeamNick())) {
        return member.getTeamNick();
      }
    }
    return null;
  }

  /**
   * get nickName display
   *
   * @param tid team id
   * @param user UserInfo
   */
  public static String getTeamMemberDisplayName(String tid, UserInfo user) {
    if (TextUtils.equals(IMKitClient.account(), user.getAccount())) {
      return IMKitClient.getApplicationContext().getString(R.string.chat_you);
    }
    FriendInfo friend = ChatRepo.getFriendInfo(user.getAccount());
    if (friend != null && !TextUtils.isEmpty(friend.getAlias())) {
      return friend.getAlias();
    }

    String memberNick = getTeamNick(tid, user.getAccount());
    if (!TextUtils.isEmpty(memberNick)) {
      return memberNick;
    }

    return (TextUtils.isEmpty(user.getName()) ? user.getAccount() : user.getName());
  }

  public static String getTeamAitName(String tid, UserInfo user) {
    if (TextUtils.equals(IMKitClient.account(), user.getAccount())) {
      return "";
    }

    String memberNick = getTeamNick(tid, user.getAccount());
    if (!TextUtils.isEmpty(memberNick)) {
      return memberNick;
    }

    return (TextUtils.isEmpty(user.getName()) ? user.getAccount() : user.getName());
  }

  public static String getUserNickByAccId(String accId, UserInfo userInfo, boolean withYou) {
    if (withYou && accId.equals(IMKitClient.account())) {
      return IMKitClient.getApplicationContext().getString(R.string.chat_you);
    }
    FriendInfo friend = ChatRepo.getFriendInfo(accId);
    if (friend != null && !TextUtils.isEmpty(friend.getAlias())) {
      return friend.getAlias();
    }
    if (userInfo == null) {
      userInfo = ChatRepo.getUserInfo(accId);
    }
    return userInfo == null
        ? null
        : (TextUtils.isEmpty(userInfo.getName()) ? accId : userInfo.getName());
  }

  public static String getChatMessageUserName(IMMessageInfo message) {
    String name = null;
    //first is friend alias
    FriendInfo friendInfo = ChatRepo.getFriendInfo(message.getMessage().getFromAccount());
    if (friendInfo != null && !TextUtils.isEmpty(friendInfo.getAlias())) {
      name = friendInfo.getAlias();
    }
    if (!TextUtils.isEmpty(name)) {
      return name;
    }
    //second is team alias
    if (message.getMessage().getSessionType() == SessionTypeEnum.Team) {
      name =
          getTeamNick(message.getMessage().getSessionId(), message.getMessage().getFromAccount());
      if (!TextUtils.isEmpty(name)) {
        return name;
      }
    }
    //third is user nick
    UserInfo user = message.getFromUser();
    if (user != null) {
      name = user.getName();
    }
    if (TextUtils.isEmpty(name)) {
      //last is accId
      name = message.getMessage().getFromAccount();
    }
    return name;
  }

  public static String getChatSearchMessageUserName(IMMessageRecord message) {
    String name = null;
    //first is friend alias
    FriendInfo friendInfo =
        ChatRepo.getFriendInfo(message.getIndexRecord().getMessage().getFromAccount());
    if (friendInfo != null && !TextUtils.isEmpty(friendInfo.getAlias())) {
      name = friendInfo.getAlias();
    }
    if (!TextUtils.isEmpty(name)) {
      return name;
    }
    //second is team alias
    if (message.getIndexRecord().getSessionType() == SessionTypeEnum.Team) {
      name =
          getTeamNick(
              message.getIndexRecord().getSessionId(),
              message.getIndexRecord().getMessage().getFromAccount());
      if (!TextUtils.isEmpty(name)) {
        return name;
      }
    }
    //third is user nick
    UserInfo user = message.getFromUser();
    if (user != null) {
      name = user.getName();
    }
    if (TextUtils.isEmpty(name)) {
      //last is accId
      name = message.getIndexRecord().getMessage().getFromAccount();
    }
    return name;
  }

  public static String getReplyMessageTips(IMMessageInfo messageInfo) {
    if (messageInfo == null) {
      return "...";
    }
    String nickName = getChatMessageUserName(messageInfo);
    String content = getReplyMsgBrief(messageInfo);
    return nickName + ": " + content;
  }

  public static void getReplyMessageInfo(String uuid, FetchCallback<String> callback) {
    if (TextUtils.isEmpty(uuid)) {
      callback.onSuccess("...");
    }
    List<String> uuidList = new ArrayList<>(1);
    uuidList.add(uuid);
    ChatRepo.queryMessageListByUuid(
        uuidList,
        new FetchCallback<List<IMMessageInfo>>() {
          @Override
          public void onSuccess(@Nullable List<IMMessageInfo> msgList) {
            String result = "";
            if (msgList == null || msgList.isEmpty()) {
              result = "...";
            } else {
              IMMessageInfo msg = msgList.get(0);
              String nickName = getChatMessageUserName(msg);
              String content = getReplyMsgBrief(msg);
              result = nickName + ": " + content;
            }
            callback.onSuccess(result);
          }

          @Override
          public void onFailed(int code) {
            callback.onSuccess("...");
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            callback.onSuccess("...");
          }
        });
  }

  public static String getReplyMsgBrief(IMMessageInfo messageInfo) {
    if (ChatKitClient.getChatUIConfig() != null
        && ChatKitClient.getChatUIConfig().chatCustom != null) {
      return ChatKitClient.getChatUIConfig().chatCustom.getReplyMsgBrief(messageInfo);
    }
    return chatCustom.getReplyMsgBrief(messageInfo);
  }
}
