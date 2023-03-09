package com.ql.recovery.yay

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.appsflyer.AppsFlyerLib
import com.blongho.country_data.World
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.gson.reflect.TypeToken
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.SDKOptions
import com.netease.nimlib.sdk.auth.AuthService
import com.netease.nimlib.sdk.auth.AuthServiceObserver
import com.netease.nimlib.sdk.auth.LoginInfo
import com.netease.nimlib.sdk.event.EventSubscribeServiceObserver
import com.netease.nimlib.sdk.mixpush.MixPushConfig
import com.netease.nimlib.sdk.mixpush.MixPushServiceObserve
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.uinfo.UserService
import com.netease.nimlib.sdk.uinfo.constant.UserInfoFieldEnum
import com.netease.yunxin.kit.chatkit.ui.ChatKitClient
import com.netease.yunxin.kit.corekit.im.IMKitClient
import com.netease.yunxin.kit.corekit.im.login.LoginCallback
import com.netease.yunxin.kit.corekit.im.utils.IMKitUtils
import com.ql.recovery.bean.*
import com.ql.recovery.config.Config
import com.ql.recovery.manager.DataManager
import com.ql.recovery.manager.RetrofitServiceManager
import com.ql.recovery.yay.config.MatchStatus
import com.ql.recovery.yay.manager.DBManager
import com.ql.recovery.yay.manager.ReportManager
import com.ql.recovery.yay.ui.dialog.MatchVideoDialog
import com.ql.recovery.yay.ui.dialog.NoticeDialog
import com.ql.recovery.yay.ui.dialog.PrimeDialog
import com.ql.recovery.yay.ui.login.LoginActivity
import com.ql.recovery.yay.ui.match.VideoActivity
import com.ql.recovery.yay.util.*
import com.tencent.mmkv.MMKV
import com.yanzhenjie.album.Album
import com.yanzhenjie.album.AlbumConfig
import io.branch.referral.Branch
import java.lang.ref.WeakReference


/**
 * @author Herr_Z
 * @description:
 * @date : 2022/11/8 17:46
 */
class BaseApp : Application() {
    private var currentActivity: WeakReference<Activity>? = null
    private var matchVideoDialog: MatchVideoDialog? = null
    private var handler = Handler(Looper.getMainLooper())
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var mMatcher: User? = null
    private var activityCount = 0
    private var isForeground = false
    private var isScreenOff = false
    private var startAt = 0L
    private var leaveAt = 0L

    override fun onCreate() {
        super.onCreate()

        initNIM()

        if (IMKitUtils.isMainProcess(this)) {
            initData()
            initHandler()
            initHttpRequest()
            initMMKV()
            initCountryLib()
            initAppsFlyer()

            //注册多端登录监听
            registerOtherClientStatus()

            //注册在线状态监听
            registerOnlineClientStatus()

            initUserInfo()
            initLifecycle()

            // Branch logging for debugging
//            Branch.enableTestMode()
            // Branch object initialization
            Branch.getAutoInstance(this)

            Album.initialize(
                AlbumConfig.newBuilder(this)
                    .setAlbumLoader(MediaLoader())
                    .build()
            )
        }
    }

    private fun initData() {
        if (AppUtil.isDebugger(this)) {
            Config.isDebug = true
        }
    }

    private fun initHandler() {
        Config.mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    0x999 -> {
                        val bundle = msg.data
                        if (bundle != null) {
                            val code = bundle.getInt("code")
                            val message = bundle.getString("message")
                            if (code == 20100 || code == 20101 || code == 20102) {
                                val mk = MMKV.defaultMMKV()
                                if (mk != null) {
                                    mk.remove("user_info")
                                    mk.remove("access_token")
                                    mk.remove("login_time")
                                    Config.CLIENT_TOKEN = ""
                                    Config.USER_ID = 0
                                    Config.USER_NAME = ""
                                }

                                val activity = currentActivity?.get()
                                if (activity != null && activity::class.java.simpleName != "LoginActivity") {
                                    val intent = Intent(activity, LoginActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    startActivity(intent)
                                }
                            }

                            ToastUtil.showShort(applicationContext, message)
                        }
                    }

                    0x10000 -> {
                        val bundle = msg.data
                        if (bundle != null) {
                            val userInfo = bundle.getParcelable<UserInfo>("user_info")
                            if (userInfo != null) {
                                val activity = currentActivity?.get()
                                if (activity != null) {
                                    PrimeDialog(activity, userInfo.is_vip) {}
                                }
                            }
                        }
                    }

                    0x10001 -> {
                        val bundle = msg.data
                        if (bundle != null) {
                            val user = bundle.getParcelable<User>("user")
                            if (user != null) {
                                showVideoChatFromMyself(user)
                            }
                        }
                    }

                    0x10002 -> {
                        val bundle = msg.data
                        if (bundle != null) {
                            val json = bundle.getString("json")
                            if (json != null) {
                                handleCustomMessage(json)
                            }
                        }
                    }

                    0x10003 -> {
                        val mk = MMKV.defaultMMKV()
                        if (mk != null) {
                            mk.remove("user_info")
                            mk.remove("access_token")
                            mk.remove("login_time")
                            Config.CLIENT_TOKEN = ""
                            Config.USER_ID = 0
                            Config.USER_NAME = ""
                            val activity = currentActivity?.get()
                            if (activity != null) {
                                val intent = Intent(activity, LoginActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                            }
                        }
                    }

                    0x10004 -> initChat()

                    0x10005 -> {
                        val bundle = msg.data
                        if (bundle != null) {
                            val uid = bundle.getInt("uid", 0)
                            if (uid != 0) {
                                val activity = currentActivity?.get()
                                if (activity != null) {
                                    requestVideoChat(activity, uid)
                                }
                            }
                        }
                    }

                    0x10006 -> {
                        val activity = currentActivity?.get()
                        if (activity != null) {
                            addUsageToDatabase(activity)
                        }
                    }

                }
            }
        }

        Config.messageHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    0x1 -> {
                        val data = msg.obj as String
                        JLog.i("data = $data")
                        val message = GsonUtils.fromJson(data, MsgInfo::class.java) ?: return

                        when (message.type) {
                            "match_invite" -> {
                                val typeToken = object : TypeToken<MessageInfo<Invite>>() {}
                                val info = GsonUtils.fromJson<MessageInfo<Invite>>(data, typeToken.type) ?: return

                                val activity = currentActivity?.get()
                                if (activity != null) {
                                    mMatcher = info.content.user
                                    matchVideoDialog?.setUser(info.content.user)
                                    if (!activity.isFinishing && !activity.isDestroyed) {
                                        matchVideoDialog?.setMatchType(info.content.room_type, info.content.transaction_type)
                                        matchVideoDialog?.startConnect()
                                        matchVideoDialog?.show()

                                        //免提模式
                                        val config = getMatchConfig()
                                        if (config.hand_free) {
                                            matchVideoDialog?.waitConnect()
                                            acceptInvite()
                                        }
                                    }
                                }
                            }

                            "match_accept_invite" -> {

                            }

                            "match_reject_invite" -> {
                                matchVideoDialog?.handOff()

                                //5秒后进入匹配池
                                rematch(5000L)

                                handler.postDelayed({
                                    matchVideoDialog?.cancel()
                                }, 1500L)
                            }

                            "match_start_play" -> {
                                val typeToken = object : TypeToken<MessageInfo<Room>>() {}
                                val info = GsonUtils.fromJson<MessageInfo<Room>>(data, typeToken.type) ?: return

                                MMKV.defaultMMKV().encode("recent_record", "match")

                                matchVideoDialog?.cancel()

                                val activity = currentActivity?.get()
                                if (activity != null) {
                                    startVideoPage(activity, info.content)
                                }
                            }

                            "match_invite_timeout" -> {
                                matchVideoDialog?.connectingTimeout()
                                matchVideoDialog?.cancel()
                                rematch(1000L)

                                val activity = currentActivity?.get()
                                if (activity != null) {
                                    ReportManager.firebaseCustomLog(firebaseAnalytics, "video_match_timeout", "timeout")
                                    ReportManager.appsFlyerCustomLog(activity, "video_match_timeout", "timeout")
                                }
                            }

                            "match_peer_disconnect" -> {
                                matchVideoDialog?.handOff()
                                matchVideoDialog?.cancel()
                                rematch(1500L)
                            }

                            "match_countdown" -> {
                                val typeToken = object : TypeToken<MessageInfo<Int>>() {}
                                val info = GsonUtils.fromJson<MessageInfo<Int>>(data, typeToken.type) ?: return
                                matchVideoDialog?.setTime(info.content)
                            }

                            "system" -> {
                                val typeToken = object : TypeToken<MessageInfo<String>>() {}
                                val info = GsonUtils.fromJson<MessageInfo<String>>(data, typeToken.type) ?: return
                                val activity = currentActivity?.get()
                                if (activity != null) {
                                    ToastUtil.showShort(activity, info.content)
                                }
                            }
                        }
                    }

                    0x2 -> {
                        val activity = currentActivity?.get()
                        if (activity != null) {
                            ToastUtil.showShort(activity, msg.obj as String)
                        }
                    }
                }
            }
        }
    }

    private fun initHttpRequest() {
        RetrofitServiceManager.get().initRetrofitService(this)
    }

    private fun initMMKV() {
        MMKV.initialize(this)
    }

    private fun initCountryLib() {
        World.init(this)
    }

    private fun initAppsFlyer() {
        AppsFlyerLib.getInstance().init(Config.APPS_FLYER_KEY, null, this)
    }

    private fun initNIM() {
        //配置PUSH
        val config = MixPushConfig()
        config.fcmCertificateName = Config.NIM_CERTIFICATE_NAME

        val options = SDKOptions()
        options.appKey = Config.NIM_APP_KEY
        options.mixPushConfig = config

        IMKitClient.init(this, null, options)
    }

    private fun initUserInfo() {
        val mk = MMKV.defaultMMKV()
        val token = mk.decodeString("access_token")
        if (token != null) {
            Config.CLIENT_TOKEN = token

            JLog.i("token = $token")

            initChat()
        }
    }

    private fun initChat() {
        val mk = MMKV.defaultMMKV()
        DataManager.getIMToken {
            mk.encode("im_token", it)
            initIM(it)
        }
    }

    private fun initIM(imToken: IMToken) {
        if (!IMKitUtils.isMainProcess(this.applicationContext)) return
        ChatKitClient.init(this.applicationContext)

        val loginInfo = LoginInfo(imToken.accid, imToken.token)
        IMKitClient.loginIM(loginInfo, object : LoginCallback<LoginInfo> {
            override fun onError(errorCode: Int, errorMsg: String) {
                JLog.i("IM login onError: errorCode = $errorCode; errorMsg = $errorMsg")
            }

            override fun onSuccess(data: LoginInfo?) {
                JLog.i("IM login onSuccess")

//                NIMClient.getService(MsgService::class.java).clearMsgDatabase(true)
//                NIMClient.getService(MsgService::class.java).clearChattingHistory(userInfo.uid.toString(), SessionTypeEnum.P2P)
//                NIMClient.getService(MsgService::class.java).clearServerHistory(userInfo.uid.toString(), SessionTypeEnum.P2P, true, "")

                //开启通知
                NIMClient.toggleNotification(true)

                //注册Push监听
                registerPushListener()

                //注册消息监听
                registerMsgStatusListener()

                //注册订阅状态
                registerUserOnlineStatus()

                //更新用户资料
                val userInfo = MMKV.defaultMMKV().decodeParcelable("user_info", UserInfo::class.java)
                if (userInfo != null) {
                    val fields = HashMap<UserInfoFieldEnum, Any>()
                    fields[UserInfoFieldEnum.Name] = userInfo.nickname
                    NIMClient.getService(UserService::class.java).updateUserInfo(fields)
                }

                //更新未读消息
                if (Config.mainHandler != null) {
                    Config.mainHandler!!.sendEmptyMessage(0x10001)
                }
            }
        })
    }

    private fun registerMsgStatusListener() {
        val incomingMessageObserver = object : Observer<List<IMMessage>> {

            override fun onEvent(t: List<IMMessage>?) {
                if (t.isNullOrEmpty()) return
                // 处理新收到的消息
                for (message in t) {
                    if (message.msgType == MsgTypeEnum.custom) {
                        JLog.i("receive a new custom message")

                        val content = message.attachStr

                        JLog.i("json = $content")

                        //从数据库删除这条消息
                        NIMClient.getService(MsgService::class.java).deleteChattingHistory(message, false)

                        //通知自定义消息逻辑处理
                        val bundle = Bundle()
                        bundle.putString("json", content)
                        val msg = Message()
                        msg.data = bundle
                        msg.what = 0x10002
                        Config.mHandler?.sendMessage(msg)
                    }
                }

                //更新未读消息
                Config.mainHandler?.sendEmptyMessage(0x10001)
            }
        }

        NIMClient.getService(MsgServiceObserve::class.java).observeReceiveMessage(incomingMessageObserver, true)
    }

    private fun registerPushListener() {
        NIMClient.getService(MixPushServiceObserve::class.java)
            .observeMixPushToken({ mixPushToken ->
                JLog.i("pushType = ${mixPushToken.pushType}")

            }, true)
    }

    private fun unregisterPushListener() {
        NIMClient.getService(MixPushServiceObserve::class.java).observeMixPushToken({}, false)
    }

    private fun registerOtherClientStatus() {
        NIMClient.getService(AuthServiceObserver::class.java)
            .observeOtherClients({ onlineClients ->
                if (onlineClients.isNullOrEmpty()) return@observeOtherClients

                for (client in onlineClients) {
                    //踢出其他登录端
                    NIMClient.getService(AuthService::class.java).kickOtherClient(client)
                }

            }, true)
    }

    private fun unregisterOtherClientStatus() {
        NIMClient.getService(AuthServiceObserver::class.java).observeOtherClients({}, false)
    }

    private fun registerOnlineClientStatus() {
        NIMClient.getService(AuthServiceObserver::class.java).observeOnlineStatus(
            { statusCode ->
//                JLog.i("statusCode = $statusCode")
                if (statusCode.wontAutoLogin()) {
                    //被踢出、账号被禁用、密码错误等情况，自动登录失败，需要返回到登录界面进行重新登录操作
                    Config.mHandler?.sendEmptyMessage(0x10003)
                }
            }, true
        )
    }

    private fun unregisterOnlineClientStatus() {
        NIMClient.getService(AuthServiceObserver::class.java).observeOnlineStatus({}, false)
    }

    private fun registerUserOnlineStatus() {
        NIMClient.getService(EventSubscribeServiceObserver::class.java).observeEventChanged({
            for (event in it) {
                val uid = event.publisherAccount
                val online = event.eventValue == 1

                JLog.i("uid = $uid, online = $online")

                //save temp Subscriber
                val subscriber = Subscriber(uid, online)

                //save to db
                DBManager.update(this, subscriber)

                val bundle = Bundle()
                bundle.putParcelable("subscriber", subscriber)
                val msg = Message()
                msg.what = 0x10000
                msg.data = bundle
                Config.subscriberHandler?.sendMessage(msg)
            }

        }, true)
    }

    private fun unRegisterUserOnlineStatus() {
        NIMClient.getService(EventSubscribeServiceObserver::class.java).observeEventChanged({}, false)
    }

    private fun initLifecycle() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                currentActivity = if (activity.parent != null) {
                    WeakReference<Activity>(activity.parent)
                } else {
                    WeakReference<Activity>(activity)
                }
            }

            override fun onActivityStarted(activity: Activity) {}

            override fun onActivityResumed(activity: Activity) {
                currentActivity = if (activity.parent != null) {
                    WeakReference<Activity>(activity.parent)
                } else {
                    WeakReference<Activity>(activity)
                }

                firebaseAnalytics = Firebase.analytics

                val userInfo = MMKV.defaultMMKV().decodeParcelable("user_info", UserInfo::class.java)
                if (userInfo != null) {
                    //保证对话框能在所有页面弹出
                    initAnchorVideoDialog(activity)

                    if (userInfo.role == "anchor") {
                        //检测主播在前台时间
                        activityCount++
                        startAt = System.currentTimeMillis() / 1000L

                        isForeground = true
                        isScreenOff = false
                    }
                }
            }

            override fun onActivityPaused(activity: Activity) {
                addUsageToDatabase(activity)
            }

            override fun onActivityStopped(activity: Activity) {
                activityCount--

                //退到后台立即上报
                if (activityCount == 0) {
                    isForeground = false
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    private fun initAnchorVideoDialog(activity: Activity) {
        matchVideoDialog = MatchVideoDialog(activity) { status, type ->
            val user = matchVideoDialog?.getUser()
            if (user != null) {
                if (type == "private") {
                    when (status) {
                        MatchStatus.Accept -> {
                            matchVideoDialog?.waitConnectForPersonal()
                            DataManager.handlerVideoInvite(user.uid, true, "default") { room ->
                                if (room != null) {
                                    matchVideoDialog?.cancel()
                                    startVideoPay(activity, room, user, "receiver")
                                }
                            }
                        }

                        MatchStatus.Reject -> {
                            DataManager.handlerVideoInvite(user.uid, false, "default") {}
                        }

                        MatchStatus.Cancel -> {}
                    }
                } else {
                    when (status) {
                        MatchStatus.Accept -> {
                            acceptInvite()
                            ReportManager.firebaseCustomLog(firebaseAnalytics, "accept_video_match_click", "accept")
                            ReportManager.appsFlyerCustomLog(this, "accept_video_match_click", "accept")
                        }

                        MatchStatus.Reject -> {
                            rejectInvite()
                            rematch(1500L)
                            ReportManager.firebaseCustomLog(firebaseAnalytics, "reject_video_match_click", "reject")
                            ReportManager.appsFlyerCustomLog(this, "reject_video_match_click", "reject")
                        }

                        MatchStatus.Cancel -> {}
                    }
                }
            }
        }
    }

    /**
     * 私聊同意开始视频聊天
     */
    private fun startVideoPay(activity: Activity, room: Room, user: User, from: String) {
        val intent = Intent(activity, VideoActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("room", room)
        intent.putExtra("user", user)
        intent.putExtra("from", from)
        intent.putExtra("type", "private")
        startActivity(intent)
    }

    /**
     * 弹出视频聊天对话框（接收方）
     */
    private fun showVideoChat(user: User) {
        if (matchVideoDialog != null && matchVideoDialog?.isShowing == false) {
            matchVideoDialog?.setUser(user)
            matchVideoDialog?.setMatchType("private", null)
            matchVideoDialog?.startConnectForPersonal()
            matchVideoDialog?.show()
        }
    }

    /**
     * 弹出视频聊天对话框（发起方）
     */
    private fun showVideoChatFromMyself(user: User) {
        if (matchVideoDialog != null && matchVideoDialog?.isShowing == false) {
            matchVideoDialog?.setUser(user)
            matchVideoDialog?.setMatchType("private", null)
            matchVideoDialog?.waitConnectForPersonal()
            matchVideoDialog?.show()
        }
    }

    /**
     * 发起视频聊天
     * @param activity
     */
    private fun requestVideoChat(activity: Activity, uid: Int) {
        val mk = MMKV.defaultMMKV()
        val userInfo = mk.decodeParcelable("user_info", UserInfo::class.java)
        val basePrice = mk.decodeParcelable("base_price", BasePrice::class.java)
        if (userInfo == null || basePrice == null) return

        NoticeDialog(activity, userInfo.coin, basePrice.common.private_video) {
            if (!DoubleUtils.isFastDoubleClick()) {
                //发起视频聊天
                DataManager.inviteVideoChat(uid.toString()) { user ->
                    val bundle = Bundle()
                    bundle.putParcelable("user", user)
                    val message = Message()
                    message.data = bundle
                    message.what = 0x10001
                    Config.mHandler?.sendMessage(message)
                }
            }
        }
    }

    fun getMatchConfig(): MatchConfig {
        val mk = MMKV.defaultMMKV()
        val config = mk.decodeParcelable("match_config", MatchConfig::class.java)
        if (config == null) {
            val defaultConfig = MatchConfig(0, "", "", false)
            mk.encode("match_config", defaultConfig)
            return defaultConfig
        } else {
            return config
        }
    }

    /**
     * 匹配结束开始视频聊天
     * @param activity
     */
    private fun startVideoPage(activity: Activity, room: Room) {
        val intent = Intent(activity, VideoActivity::class.java)
        intent.putExtra("room", room)
        intent.putExtra("user", mMatcher)
        intent.putExtra("type", "match")
        startActivity(intent)
    }

    private fun acceptInvite() {
        matchVideoDialog?.waitConnect()
        Config.mainHandler?.sendEmptyMessage(0x10007)
    }

    private fun rejectInvite() {
        Config.mainHandler?.sendEmptyMessage(0x10008)
    }

    private fun rematch(time: Long) {
        handler.postDelayed({ Config.mainHandler?.sendEmptyMessage(0x10009) }, time)
    }

    private fun handleCustomMessage(json: String) {
        try {
            val message = GsonUtils.fromJson(json, MsgInfo::class.java)
            if (message != null) {
                when (message.type) {
                    "room_extra_time_success" -> {
                        val typeToken = object : TypeToken<MessageInfo<Room>>() {}
                        val info = GsonUtils.fromJson<MessageInfo<Room>>(json, typeToken.type)
                        if (info != null) {
                            if (Config.roomHandler != null) {
                                val bundle = Bundle()
                                bundle.putParcelable("room_extra_time_success", info.content)

                                val msg = Message()
                                msg.what = 0x10000
                                msg.data = bundle
                                Config.roomHandler!!.sendMessage(msg)
                            }
                        }
                    }

                    "room_extra_time_failed" -> {
//                        if (Config.roomHandler != null) {
//                            Config.roomHandler!!.sendEmptyMessage(0x10004)
//                        }
                    }

                    "invite_video" -> {
                        //对方发送视频邀请
                        val typeToken = object : TypeToken<MessageInfo<User>>() {}
                        val info = GsonUtils.fromJson<MessageInfo<User>>(json, typeToken.type)
                        if (info != null) {
                            val activity = currentActivity?.get()
                            if (activity != null) {
                                val name = activity.localClassName
                                if (!name.contains("VideoActivity")
                                    && !name.contains("AudioActivity")
                                    && !name.contains("GameActivity")
                                ) {
                                    matchVideoDialog?.setMatchType("private", null)
                                    showVideoChat(info.content)
                                } else {
                                    //自动拒绝对方的邀请
                                    DataManager.handlerVideoInvite(info.content.uid, false, "busy") {}
                                }
                            }
                        }
                    }

                    "invite_success" -> {
                        //对方同意视频邀请
                        val typeToken = object : TypeToken<MessageInfo<Room>>() {}
                        val info = GsonUtils.fromJson<MessageInfo<Room>>(json, typeToken.type)
                        if (info != null) {
                            if (matchVideoDialog != null) {
                                val user = matchVideoDialog!!.getUser()
                                if (user != null) {
                                    if (matchVideoDialog?.isShowing == true) {
                                        matchVideoDialog?.cancel()
                                        val activity = currentActivity?.get()
                                        if (activity != null) {
                                            startVideoPay(activity, info.content, user, "sender")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "invite_reject" -> {
                        //拒绝私聊邀请
                        val typeToken = object : TypeToken<MessageInfo<Reason>>() {}
                        val info = GsonUtils.fromJson<MessageInfo<Reason>>(json, typeToken.type)
                        if (info != null) {
                            if (matchVideoDialog != null) {
                                matchVideoDialog?.rejectConnectForPersonal(info.content)

                                //2秒后关闭对话框
                                handler.postDelayed({ matchVideoDialog?.cancel() }, 2000L)

                                //如果进入房间则通知房间取消视频
                                Config.roomHandler?.sendEmptyMessage(0x10005)
                            }
                        }
                    }

                    "room_get_lottery_ticket" -> {
                        //对方发送抽奖券
                        val typeToken = object : TypeToken<MessageInfo<LotteryTicket>>() {}
                        val info = GsonUtils.fromJson<MessageInfo<LotteryTicket>>(json, typeToken.type)
                        if (info != null) {
                            if (Config.roomHandler != null) {
                                val bundle = Bundle()
                                bundle.putParcelable("lottery_ticket", info.content)

                                val msg = Message()
                                msg.what = 0x10002
                                msg.data = bundle
                                Config.roomHandler!!.sendMessage(msg)
                            }
                        }
                    }

                    "room_lottery_result" -> {
                        //对方抽奖结果
                        val typeToken = object : TypeToken<MessageInfo<List<LotteryRecord>>>() {}
                        val info = GsonUtils.fromJson<MessageInfo<List<LotteryRecord>>>(json, typeToken.type)
                        if (info != null) {
                            if (Config.roomHandler != null) {
                                val bundle = Bundle()
                                val arrayList = arrayListOf<LotteryRecord>()
                                arrayList.addAll(info.content)
                                bundle.putParcelableArrayList("lottery_result", arrayList)

                                val msg = Message()
                                msg.what = 0x10003
                                msg.data = bundle
                                Config.roomHandler!!.sendMessage(msg)
                            }
                        }
                    }

                    "gifted" -> {
                        //对方赠送礼物
                        val typeToken = object : TypeToken<MessageInfo<Gift>>() {}
                        val info = GsonUtils.fromJson<MessageInfo<Gift>>(json, typeToken.type)
                        if (info != null) {
                            if (Config.roomHandler != null) {
                                val bundle = Bundle()
                                bundle.putParcelable("gift", info.content)
                                val msg = Message()
                                msg.what = 0x10001
                                msg.data = bundle
                                Config.roomHandler!!.sendMessage(msg)
                            }
                        }
                    }

                    else -> {
                        val info = GsonUtils.fromJson(json, PushMessageInfo::class.java)
                        if (info != null) {
                            if (info.content == null) {
                                AppUtil.sendNotification(this, info.type, "you have a new message")
                            } else {
                                AppUtil.sendNotification(this, info.type, info.content as String)
                            }
                        }
                    }
                }
            }

        } catch (ex: Exception) {

        }
    }

    private fun addUsageToDatabase(activity: Activity) {
        val userInfo = MMKV.defaultMMKV().decodeParcelable("user_info", UserInfo::class.java)
        if (userInfo != null && userInfo.role == "anchor" && startAt != 0L) {
            leaveAt = System.currentTimeMillis() / 1000L
            val activityName = activity::class.java.simpleName
            val usageStatus = UsageStatus(activityName, startAt, leaveAt, userInfo.uid)

            //记录到数据库
            DBManager.insert(activity, usageStatus) {
                //上报时长
                addAnchorOnlineTime(activity)

                //重置记录开始的时间
                startAt = System.currentTimeMillis() / 1000L
            }
        }
    }

    private fun addAnchorOnlineTime(activity: Activity) {
        val userInfo = MMKV.defaultMMKV().decodeParcelable("user_info", UserInfo::class.java)
        if (userInfo != null && userInfo.role == "anchor") {
            DBManager.findAllUsageStatus(activity) { usageList ->
                if (!usageList.isNullOrEmpty()) {
                    for (usage in usageList) {
                        JLog.i("usage = $usage")
                        if (usage.startAt != 0L && usage.leaveAt != 0L && usage.uid == userInfo.uid) {
                            DataManager.addAnchorOnlineTime(usage.startAt, usage.leaveAt) {
                                if (it) {
                                    Config.subscriberHandler?.sendEmptyMessage(0x10002)

                                    //上报成功则删除这条数据
                                    DBManager.delete(activity, usage)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        //主播异常退出时长上报
        val activity = currentActivity?.get()
        if (activity != null) {
            addUsageToDatabase(activity)
        }

        unregisterPushListener()
        unregisterOtherClientStatus()
        unRegisterUserOnlineStatus()
        unregisterOnlineClientStatus()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (newConfig.fontScale != 1.0f) {
            resources
        }
        super.onConfigurationChanged(newConfig)
    }

    override fun getResources(): Resources {
        val res = super.getResources()
        val config = Configuration()
        config.setToDefaults()
        res.updateConfiguration(config, res.displayMetrics)
        return res
    }

}