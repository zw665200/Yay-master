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
import com.blongho.country_data.World
import com.google.gson.reflect.TypeToken
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.SDKOptions
import com.netease.nimlib.sdk.auth.AuthService
import com.netease.nimlib.sdk.auth.AuthServiceObserver
import com.netease.nimlib.sdk.auth.LoginInfo
import com.netease.nimlib.sdk.event.EventSubscribeServiceObserver
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
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
import com.ql.recovery.yay.ui.dialog.MatchVideoDialog
import com.ql.recovery.yay.ui.dialog.NoticeDialog
import com.ql.recovery.yay.ui.dialog.PrimeDialog
import com.ql.recovery.yay.ui.login.LoginActivity
import com.ql.recovery.yay.ui.match.VideoActivity
import com.ql.recovery.yay.util.*
import com.tencent.mmkv.MMKV
import java.lang.ref.WeakReference


/**
 * @author Herr_Z
 * @description:
 * @date : 2022/11/8 17:46
 */
class BaseApp : Application() {
    private var currentActivity: WeakReference<Activity>? = null
    private var dialog: MatchVideoDialog? = null
    private var handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()

        initNIM()

        if (IMKitUtils.isMainProcess(this)) {
            initData()
            initHandler()
            initHttpRequest()
            initMMKV()
            initCountryLib()

            //注册多端登录监听
            registerOtherClientStatus()

            //注册在线状态监听
            registerOnlineClientStatus()

            initUserInfo()
            initLifecycle()
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

                                if (code == 20100 || code == 20102) {
                                    val activity = currentActivity?.get()
                                    if (activity != null) {
                                        startActivity(Intent(activity, LoginActivity::class.java))
                                    }
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

    private fun initNIM() {
        val options = SDKOptions()
        options.appKey = Config.NIM_APP_KEY
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

                //注册消息监听
                registerMsgStatusListener()

                //注册订阅状态
                registerUserOnlineStatus()

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
                if (Config.mainHandler != null) {
                    Config.mainHandler!!.sendEmptyMessage(0x10001)
                }
            }
        }

        NIMClient.getService(MsgServiceObserve::class.java).observeReceiveMessage(incomingMessageObserver, true)
    }

    private fun registerOtherClientStatus() {
        NIMClient.getService(AuthServiceObserver::class.java).observeOtherClients({ onlineClients ->
            JLog.i("client online = $onlineClients")
            if (onlineClients.isNullOrEmpty()) return@observeOtherClients

            for (client in onlineClients) {
                //踢出其他登录端
                NIMClient.getService(AuthService::class.java).kickOtherClient(client)
            }

        }, true)
    }

    private fun registerOnlineClientStatus() {
        NIMClient.getService(AuthServiceObserver::class.java).observeOnlineStatus(
            { statusCode ->
                JLog.i("statusCode = $statusCode")
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

                //save temp Subscriber
                val subscriber = Subscriber(uid, online)

                //save to db
                DBManager.update(this, subscriber)

                if (Config.subscriberHandler != null) {
                    val bundle = Bundle()
                    bundle.putParcelable("subscriber", subscriber)
                    val msg = Message()
                    msg.what = 0x10000
                    msg.data = bundle
                    Config.subscriberHandler!!.sendMessage(msg)
                }
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
                    //如果这个视图是嵌入的子视图
                    WeakReference<Activity>(activity.parent)
                } else {
                    WeakReference<Activity>(activity)
                }
            }

            override fun onActivityStarted(activity: Activity) {
            }

            override fun onActivityResumed(activity: Activity) {
                currentActivity = if (activity.parent != null) {
                    //如果这个视图是嵌入的子视图
                    WeakReference<Activity>(activity.parent)
                } else {
                    WeakReference<Activity>(activity)
                }

                //保证对话框能在所有页面弹出
                initVideoDialog(activity)
            }

            override fun onActivityPaused(activity: Activity) {
            }

            override fun onActivityStopped(activity: Activity) {
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityDestroyed(activity: Activity) {
            }
        })
    }

    private fun initVideoDialog(activity: Activity) {
        dialog = MatchVideoDialog(activity) { status ->
            val user = dialog?.getUser()
            if (user != null) {
                when (status) {
                    MatchStatus.Accept -> {
                        dialog?.waitConnectForPersonal()
                        DataManager.handlerVideoInvite(user.uid, true, "default") { room ->
                            dialog?.cancel()

                            if (room != null) {
                                startVideoPay(activity, room, user, "receiver")
                            }
                        }
                    }

                    MatchStatus.Reject -> {
                        DataManager.handlerVideoInvite(user.uid, false, "default") {}
                    }

                    MatchStatus.Cancel -> {

                    }
                }
            }
        }
    }

    private fun startVideoPay(activity: Activity, room: Room, user: User, from: String) {
        val intent = Intent(activity, VideoActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("room", room)
        intent.putExtra("user", user)
        intent.putExtra("from", from)
        intent.putExtra("type", "private")
        startActivity(intent)
    }

    private fun showVideoChat(user: User) {
        if (dialog != null && dialog?.isShowing == false) {
            dialog?.setUser(user)
            dialog?.startConnectForPersonal()
            dialog?.show()
        }
    }

    fun showVideoChatFromMyself(user: User) {
        if (dialog != null && dialog?.isShowing == false) {
            dialog?.setUser(user)
            dialog?.waitConnectForPersonal()
            dialog?.show()
        }
    }

    /**
     * 发起视频聊天，对方在线才能聊天
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
                            val activity = currentActivity?.get()!!.localClassName
                            if (!activity.contains("VideoActivity")
                                && !activity.contains("AudioActivity")
                                && !activity.contains("GameActivity")
                            ) {
                                showVideoChat(info.content)
                            } else {
                                //自动拒绝对方的邀请
                                DataManager.handlerVideoInvite(info.content.uid, false, "busy") {}
                            }
                        }
                    }

                    "invite_success" -> {
                        //对方同意视频邀请
                        val typeToken = object : TypeToken<MessageInfo<Room>>() {}
                        val info = GsonUtils.fromJson<MessageInfo<Room>>(json, typeToken.type)
                        if (info != null) {
                            if (dialog != null) {
                                val user = dialog!!.getUser()
                                if (user != null) {
                                    if (dialog?.isShowing == true) {
                                        dialog?.cancel()
                                        startVideoPay(dialog!!.getActivity(), info.content, user, "sender")
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
                            if (dialog != null) {
                                dialog?.rejectConnectForPersonal(info.content)

                                //2秒后关闭对话框
                                handler.postDelayed({ dialog?.cancel() }, 2000L)

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
                }
            }

        } catch (ex: Exception) {

        }
    }

    override fun onTerminate() {
        JLog.i("onTerminate")
        super.onTerminate()
        unRegisterUserOnlineStatus()
        unregisterOnlineClientStatus()
        unRegisterUserOnlineStatus()
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