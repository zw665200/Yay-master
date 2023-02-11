// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui;

import android.content.Context;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.contactkit.ContactService;
import com.netease.yunxin.kit.contactkit.ui.addfriend.AddFriendActivity;
import com.netease.yunxin.kit.contactkit.ui.blacklist.BlackListActivity;
import com.netease.yunxin.kit.contactkit.ui.contact.ContactActivity;
import com.netease.yunxin.kit.contactkit.ui.selector.ContactSelectorActivity;
import com.netease.yunxin.kit.contactkit.ui.team.TeamListActivity;
import com.netease.yunxin.kit.contactkit.ui.userinfo.UserInfoActivity;
import com.netease.yunxin.kit.contactkit.ui.verify.VerifyListActivity;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

@Keep
public class ContactUIService extends ContactService {

  @NonNull
  @Override
  public String getServiceName() {
    return "ContactUIKit";
  }

  @NonNull
  @Override
  public String getVersionName() {
    return BuildConfig.versionName;
  }

  @NonNull
  @Override
  public ContactService create(@NonNull Context context) {
    XKitRouter.registerRouter(
        RouterConstant.PATH_CONTACT_SELECTOR_PAGE, ContactSelectorActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_ADD_FRIEND_PAGE, AddFriendActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_USER_INFO_PAGE, UserInfoActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_MY_TEAM_PAGE, TeamListActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_MY_BLACK_PAGE, BlackListActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_MY_NOTIFICATION_PAGE, VerifyListActivity.class);
    XKitRouter.registerRouter(RouterConstant.PATH_CONTACT_PAGE, ContactActivity.class);
    return this;
  }
}
