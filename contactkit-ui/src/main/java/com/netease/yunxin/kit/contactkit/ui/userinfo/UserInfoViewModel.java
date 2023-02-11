// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.userinfo;

import static com.netease.yunxin.kit.contactkit.ui.ContactConstant.LIB_TAG;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.repo.ContactRepo;
import com.netease.yunxin.kit.contactkit.ui.model.ContactUserInfoBean;
import com.netease.yunxin.kit.corekit.im.model.FriendInfo;
import com.netease.yunxin.kit.corekit.im.model.FriendVerifyType;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.provider.UserInfoObserver;
import java.util.ArrayList;
import java.util.List;

public class UserInfoViewModel extends BaseViewModel {
  private final String TAG = "UserInfoViewModel";

  private final MutableLiveData<FetchResult<ContactUserInfoBean>> friendLiveData =
      new MutableLiveData<>();
  private final FetchResult<ContactUserInfoBean> fetchResult = new FetchResult<>(LoadStatus.Finish);
  private final MutableLiveData<FetchResult<List<UserInfo>>> userInfoLiveData =
      new MutableLiveData<>();
  private final FetchResult<List<UserInfo>> userInfoFetchResult =
      new FetchResult<>(LoadStatus.Finish);

  public UserInfoViewModel() {
    registerObserver();
  }

  private final UserInfoObserver userInfoObserver =
      userList -> {
        userInfoFetchResult.setLoadStatus(LoadStatus.Finish);
        userInfoFetchResult.setData(userList);
        userInfoFetchResult.setType(FetchResult.FetchType.Update);
        userInfoFetchResult.setTypeIndex(-1);
        userInfoLiveData.setValue(userInfoFetchResult);
      };

  public void registerObserver() {
    ContactRepo.registerUserInfoObserver(userInfoObserver);
  }

  public MutableLiveData<FetchResult<ContactUserInfoBean>> getFetchResult() {
    return friendLiveData;
  }

  public MutableLiveData<FetchResult<List<UserInfo>>> getUserInfoLiveData() {
    return userInfoLiveData;
  }

  public void fetchData(String account) {
    ALog.d(LIB_TAG, TAG, "fetchData:" + account);
    if (TextUtils.isEmpty(account)) {
      return;
    }
    List<String> accountList = new ArrayList<>();
    accountList.add(account);
    FriendInfo friendInfo = ContactRepo.getFriend(account);
    ContactRepo.getUserInfo(
        accountList,
        new FetchCallback<List<UserInfo>>() {
          @Override
          public void onSuccess(@Nullable List<UserInfo> param) {
            ALog.d(LIB_TAG, TAG, "fetchData,onSuccess:" + (param == null ? "null" : param.size()));
            if (param != null && param.size() > 0) {
              ContactUserInfoBean userInfo = new ContactUserInfoBean(param.get(0));
              userInfo.friendInfo = friendInfo;
              userInfo.isBlack = isBlack(account);
              userInfo.isFriend = isFriend(account);
              fetchResult.setData(userInfo);
              fetchResult.setStatus(LoadStatus.Success);
            } else {
              fetchResult.setError(-1, "");
            }
            friendLiveData.postValue(fetchResult);
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "fetchData,onFailed:" + code);
            fetchResult.setError(code, "");
            friendLiveData.postValue(fetchResult);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "fetchData,onException");
            fetchResult.setError(-1, "");
            friendLiveData.postValue(fetchResult);
          }
        });
  }

  public boolean isBlack(String account) {
    ALog.d(LIB_TAG, TAG, "isBlack:" + account);
    return ContactRepo.isBlackList(account);
  }

  public boolean isFriend(String account) {
    ALog.d(LIB_TAG, TAG, "isFriend:" + account);
    return ContactRepo.isFriend(account);
  }

  public void addBlack(String account) {
    ALog.d(LIB_TAG, TAG, "addBlack:" + account);
    ContactRepo.addBlackList(
        account,
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            ALog.d(LIB_TAG, TAG, "addBlack,onSuccess");
            fetchData(account);
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "addBlack,onFailed:" + code);
            fetchResult.setError(code, "");
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "addBlack,onException");
            fetchResult.setError(-1, "");
          }
        });
  }

  public void removeBlack(String account) {
    ALog.d(LIB_TAG, TAG, "removeBlack:" + account);
    ContactRepo.removeBlackList(
        account,
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            ALog.d(LIB_TAG, TAG, "removeBlack,onSuccess");
            fetchData(account);
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "removeBlack,onFailed:" + code);
            fetchResult.setError(code, "");
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "removeBlack,onException");
            fetchResult.setError(-1, "");
          }
        });
  }

  public void deleteFriend(String account) {
    ALog.d(LIB_TAG, TAG, "deleteFriend:" + account);
    ContactRepo.deleteFriend(
        account,
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            ALog.d(LIB_TAG, TAG, "deleteFriend,onSuccess");
            fetchData(account);
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "deleteFriend,onFailed:" + code);
            fetchResult.setError(code, "");
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "deleteFriend,onException");
            fetchResult.setError(-1, "");
          }
        });
  }

  public void addFriend(String account, FriendVerifyType type, FetchCallback<Void> callback) {
    ALog.d(LIB_TAG, TAG, "addFriend:" + account);
    ContactRepo.addFriend(account, type, callback);
  }

  public void updateAlias(String account, String alias) {
    ALog.d(LIB_TAG, TAG, "updateAlias:" + account);
    ContactRepo.updateAlias(account, alias);
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    ContactRepo.unregisterUserInfoObserver(userInfoObserver);
  }
}
