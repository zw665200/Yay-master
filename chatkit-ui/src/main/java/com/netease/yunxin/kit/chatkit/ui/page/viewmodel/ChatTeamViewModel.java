// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMTeamMessageReceiptInfo;
import com.netease.yunxin.kit.chatkit.model.UserInfoWithTeam;
import com.netease.yunxin.kit.chatkit.repo.ChatObserverRepo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im.model.EventObserver;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.model.ErrorMsg;
import com.netease.yunxin.kit.corekit.model.ResultInfo;
import java.util.ArrayList;
import java.util.List;

/** Team chat info view model team message receipt, team member info for Team chat page */
public class ChatTeamViewModel extends ChatBaseViewModel {

  private static final String TAG = "ChatTeamViewModel";

  private final MutableLiveData<FetchResult<List<IMTeamMessageReceiptInfo>>>
      teamMessageReceiptLiveData = new MutableLiveData<>();

  private final MutableLiveData<Team> teamLiveData = new MutableLiveData<>();
  private final MutableLiveData<ResultInfo<List<UserInfoWithTeam>>> userInfoData =
      new MutableLiveData<>();

  private final EventObserver<List<IMTeamMessageReceiptInfo>> teamMessageReceiptObserver =
      new EventObserver<List<IMTeamMessageReceiptInfo>>() {
        @Override
        public void onEvent(@Nullable List<IMTeamMessageReceiptInfo> event) {
          ALog.d(LIB_TAG, TAG, "messageReceipt:" + (event == null ? "null" : event.size()));
          FetchResult<List<IMTeamMessageReceiptInfo>> receiptResult =
              new FetchResult<>(LoadStatus.Finish);
          receiptResult.setData(event);
          receiptResult.setType(FetchResult.FetchType.Update);
          receiptResult.setTypeIndex(-1);
          teamMessageReceiptLiveData.setValue(receiptResult);
        }
      };

  private final Observer<List<Team>> teamObserver =
      event -> {
        ALog.d(LIB_TAG, TAG, "teamObserver:" + (event == null ? "null" : event.size()));
        if (event == null) return;
        for (Team team : event) {
          if (TextUtils.equals(team.getId(), mSessionId)) {
            teamLiveData.setValue(team);
          }
        }
      };

  public MutableLiveData<FetchResult<List<IMTeamMessageReceiptInfo>>>
      getTeamMessageReceiptLiveData() {
    return teamMessageReceiptLiveData;
  }

  public void refreshTeamMessageReceipt(List<ChatMessageBean> messageBeans) {
    ALog.d(
        LIB_TAG,
        TAG,
        "refreshTeamMessageReceipt:" + (messageBeans == null ? "null" : messageBeans.size()));
    List<IMMessage> messages = new ArrayList<>();
    for (ChatMessageBean messageBean : messageBeans) {
      messages.add(messageBean.getMessageData().getMessage());
    }
    ChatRepo.refreshTeamMessageReceipt(messages);
  }

  /** team info change live data */
  public MutableLiveData<Team> getTeamLiveData() {
    return teamLiveData;
  }

  /** team member info live data */
  public MutableLiveData<ResultInfo<List<UserInfoWithTeam>>> getUserInfoData() {
    return userInfoData;
  }

  @Override
  public void registerObservers() {
    super.registerObservers();
    ChatObserverRepo.registerTeamMessageReceiptObserve(teamMessageReceiptObserver);
    ChatObserverRepo.registerTeamUpdateObserver(teamObserver);
  }

  @Override
  public void unregisterObservers() {
    super.unregisterObservers();
    ChatObserverRepo.unregisterTeamMessageReceiptObserve(teamMessageReceiptObserver);
    ChatObserverRepo.unregisterTeamUpdateObserver(teamObserver);
  }

  @Override
  public void sendReceipt(IMMessage message) {
    ALog.d(LIB_TAG, TAG, "sendReceipt:" + (message == null ? "null" : message.getUuid()));
    ChatRepo.markTeamMessageRead(message);
  }

  public void requestTeamInfo(String teamId) {
    ALog.d(LIB_TAG, TAG, "requestTeamInfo:" + teamId);
    ChatRepo.fetchTeamInfo(
        teamId,
        new FetchCallback<Team>() {
          @Override
          public void onSuccess(@Nullable Team param) {
            ALog.d(LIB_TAG, TAG, "sendReceipt,onSuccess:" + (param == null));
            teamLiveData.setValue(param);
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "sendReceipt,onFailed:" + code);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "sendReceipt,onException");
          }
        });
  }

  public void requestTeamMembers(String teamId) {
    ALog.d(LIB_TAG, TAG, "requestTeamMembers:" + teamId);
    ChatRepo.queryTeamMemberList(
        teamId,
        new FetchCallback<List<UserInfoWithTeam>>() {
          @Override
          public void onSuccess(@Nullable List<UserInfoWithTeam> param) {
            ALog.d(LIB_TAG, TAG, "requestTeamMembers,onSuccess:" + (param == null));
            userInfoData.postValue(new ResultInfo<>(param));
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "requestTeamMembers,onFailed:" + code);
            userInfoData.postValue(new ResultInfo<>(null, false, new ErrorMsg(code)));
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "requestTeamMembers,onException");
            userInfoData.postValue(new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
          }
        });
  }
}
