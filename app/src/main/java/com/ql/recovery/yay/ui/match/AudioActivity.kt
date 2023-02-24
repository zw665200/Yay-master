package com.ql.recovery.yay.ui.match

import android.content.Intent
import android.graphics.Color
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.yunxin.kit.adapters.DataAdapter
import com.netease.yunxin.kit.chatkit.repo.ChatRepo
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback
import com.ql.recovery.bean.*
import com.ql.recovery.bean.UserInfo
import com.ql.recovery.config.Config
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.ActivityAudioBinding
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.databinding.ItemChatBinding
import com.ql.recovery.yay.manager.ImageManager
import com.ql.recovery.yay.manager.ReportManager
import com.ql.recovery.yay.ui.base.BaseActivity
import com.ql.recovery.yay.ui.dialog.*
import com.ql.recovery.yay.util.*
import io.agora.rtc2.*


class AudioActivity : BaseActivity() {
    private lateinit var binding: ActivityAudioBinding
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var mAdapter: DataAdapter<Chat>
    private var chatList = arrayListOf<Chat>()
    private var handler = Handler(Looper.getMainLooper())

    private var timer: CountDownTimer? = null
    private var observerReceive: Observer<List<IMMessage>>? = null
    private var mUser: User? = null
    private var mRoom: Room? = null
    private var mType: String = "match"
    private var mRtcEngine: RtcEngine? = null
    private var mVolume = 100
    private var speakerOpen = false
    private var isFinish = false
    private var isPaying = false
    private var gameDialog: GameDialog? = null
    private var timerDialog: TimerDialog? = null
    private var waitingDialog: WaitingDialog? = null

    private var mRtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread {
                JLog.i("onUserJoined")
                timer?.start()

                if (mRtcEngine?.isSpeakerphoneEnabled == true) {
                    binding.includeBottomAudio.ivSpeaker.setImageResource(R.drawable.speaker_active)
                } else {
                    binding.includeBottomAudio.ivSpeaker.setImageResource(R.drawable.speaker_close)
                }
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread {
                JLog.i("onUserOffline")
                toFinishPage()
            }
        }

        override fun onLeaveChannel(stats: RtcStats?) {
            runOnUiThread {
                JLog.i("onLeaveChannel")
                toFinishPage()
            }
        }

        override fun onConnectionLost() {
            JLog.i("onConnectionLost")
            //丢失连接10秒
        }

        override fun onError(err: Int) {
            super.onError(err)
            if (err == 110 || err == 109) {
                mRtcEngine?.leaveChannel()
            }
        }
    }

    override fun getViewBinding(baseBinding: ActivityBaseBinding) {
        binding = ActivityAudioBinding.inflate(layoutInflater, baseBinding.flBase, true)
    }

    override fun initView() {
        setStatusBarLight()
        binding.includeTimer.tvTimer.setTextColor(ResourcesCompat.getColor(resources, R.color.color_yellow, null))
        binding.ivBack.setOnClickListener { onBackPressed() }
        binding.ivYourAvatar.setOnClickListener { showProfileDialog(false) }
        binding.ivMyAvatar.setOnClickListener { showProfileDialog(true) }
        binding.includeBottom.tvSend.setOnClickListener { sendMessage() }
        binding.includeBottomAudio.ivHandOff.setOnClickListener { handOff() }
        binding.includeBottomAudio.ivSilent.setOnClickListener { setVolume() }
        binding.includeBottomAudio.ivSpeaker.setOnClickListener { setSpeaker() }
        binding.includeTimer.tvAdd.setOnClickListener { getAdditionTime() }
        binding.tvFollow.setOnClickListener { checkFollow() }
        binding.ivReport.setOnClickListener { showReportDialog() }
        binding.ivGift.setOnClickListener { showGiftDialog() }
        binding.ivGame.setOnClickListener { showGameDialog(null) }

        //设置editText的属性
        binding.includeBottom.etMessage.imeOptions = EditorInfo.IME_ACTION_SEND
        binding.includeBottom.etMessage.setOnFocusChangeListener { view, b ->
            if (!b) {
                AppUtil.hideKeyboard(this, view)
            }
        }

        binding.includeBottom.etMessage.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT
                || actionId == EditorInfo.IME_ACTION_DONE
                || actionId == EditorInfo.IME_ACTION_GO
                || actionId == EditorInfo.IME_ACTION_SEND
            ) {
                sendMessage()
            }
            false
        }
    }


    override fun initData() {
        mRoom = intent.getParcelableExtra("room")
        mUser = intent.getParcelableExtra("user")
        if (mRoom != null && mUser != null) {
            waitingDialog = WaitingDialog(this)
            firebaseAnalytics = Firebase.analytics

            initHandler()
            initUserInfo(mUser!!)
            initGameDialog()
            initTimerDialog()
            initTimer(mRoom!!.duration * 1000L)

            getUserInfo {
                initializeAndJoinChannel(mRoom!!, it)
                initChat()
            }
        }
    }

    private fun initHandler() {
        Config.roomHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    0x10000 -> {
                        val bundle = msg.data
                        if (bundle != null) {
                            val room = bundle.getParcelable<Room>("room_extra_time_success")
                            if (room != null) {
                                JLog.i("receive room = $room")
                                mRoom = room

                                //刷新房间Token
                                mRtcEngine?.renewToken(room.token)

                                //重置计时器
                                timer?.cancel()
                                initTimer(room.duration * 1000L)
                                timer?.start()

                                ReportManager.firebaseCustomLog(firebaseAnalytics, "voice_chat_timeout", "timeout")
                                ReportManager.appsFlyerCustomLog(this@AudioActivity, "voice_chat_timeout", "timeout")
                            }
                        }
                    }

                    0x10001 -> {
                        val bundle = msg.data
                        if (bundle != null) {
                            val gift = bundle.getParcelable<Gift>("gift")
                            if (gift != null) {
                                runOnUiThread {
                                    GiftPlayDialog(this@AudioActivity, gift)
                                }
                            }
                        }
                    }

                    0x10002 -> {
                        val bundle = msg.data
                        if (bundle != null) {
                            val lotteryTicket = bundle.getParcelable<LotteryTicket>("lottery_ticket")
                            if (lotteryTicket != null) {
                                ToastUtil.showShort(this@AudioActivity, getString(R.string.match_ticket_receive))
                                showGameDialog(lotteryTicket)
                            }
                        }
                    }

                    0x10003 -> {
                        val bundle = msg.data
                        if (bundle != null) {
                            val lotteryRecord = bundle.getParcelableArrayList<LotteryRecord>("lottery_result")
                            if (lotteryRecord != null) {
                                gameDialog?.show()
                                gameDialog?.setLotteryRecord(lotteryRecord)
                            }
                        }
                    }

                    0x10004 -> {
                        mRoom?.`package` = null
                    }
                }
            }
        }
    }

    private fun initUserInfo(user: User) {
        //设置对方头像
        Glide.with(this).load(user.avatar).apply(RequestOptions.bitmapTransform(CircleCrop())).into(binding.ivYourAvatar)

        //设置对方昵称
        binding.tvYourNickname.text = user.nickname

        //设置国家
        if (user.country.isNotBlank()) {
            val res = "file:///android_asset/images/${user.country}.png"
            ImageManager.getBitmap(this, res) { bitmap ->
                binding.ivNation.setImageBitmap(bitmap)
            }
        }

        //设置关注状态
        when (mUser!!.follow_status) {
            1, 3 -> {
                binding.tvFollow.text = getString(R.string.match_followed)
                binding.tvFollow.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_grey_30, null)
                binding.tvFollow.setTextColor(Color.WHITE)
            }

            else -> {
                binding.tvFollow.text = getString(R.string.match_follow)
                binding.tvFollow.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_yellow, null)
                binding.tvFollow.setTextColor(Color.BLACK)
            }
        }

        //设置我的头像
        val userInfo = getLocalStorage().decodeParcelable("user_info", UserInfo::class.java)
        if (userInfo != null) {
            Glide.with(this).load(userInfo.avatar).apply(RequestOptions.bitmapTransform(CircleCrop())).into(binding.ivMyAvatar)
        }
    }

    private fun initGameDialog() {
        getUserInfo { userInfo ->
            gameDialog = GameDialog(this, userInfo, mRoom!!.room_id, mUser!!.avatar, false)
        }
    }

    private fun initTimerDialog() {
        timerDialog = TimerDialog(this) {
            getAdditionTime()
        }
    }

    private fun initializeAndJoinChannel(room: Room, userInfo: UserInfo) {
        try {
            val config = RtcEngineConfig()
            config.mContext = this
            config.mAppId = Config.AGORA_APP_ID
            config.mEventHandler = mRtcEventHandler
            mRtcEngine = RtcEngine.create(config)
        } catch (ex: Exception) {
            ToastUtil.showShort(this, ex.message)
            JLog.i(ex.message)
        }

        mRtcEngine?.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)

        val options = ChannelMediaOptions()
        //视频通话场景下，设置频道场景为 BROADCASTING
        options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
        //将用户角色设置为 BROADCASTER
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER

        options.autoSubscribeAudio = true

        //加入频道
        mRtcEngine?.joinChannel(room.token, room.room_id, userInfo.uid, options)
    }

    private fun initTimer(time: Long) {
        JLog.i("remaining time = $time")
        timer = object : CountDownTimer(time, 1000L) {
            override fun onFinish() {
                mRtcEngine?.leaveChannel()
                timerDialog?.cancel()
            }

            override fun onTick(millisUntilFinished: Long) {
                binding.includeTimer.tvTimer.text = AppUtil.timeStamp2Date(millisUntilFinished, "ss")
                when (mType) {
                    "match" -> {
                        if (millisUntilFinished > 30000L) {
                            timerDialog?.cancel()
                            binding.includeTimer.root.visibility = View.INVISIBLE
                        } else {
                            val pkg = mRoom?.`package`
                            if (pkg != null && pkg.type == "keep") {
                                binding.includeTimer.root.visibility = View.INVISIBLE
                            } else {
                                binding.includeTimer.root.visibility = View.VISIBLE

                                //只有玩游戏的时候才弹出倒计时对话框
                                if (gameDialog?.isShowing == true) {
                                    timerDialog?.setTimer(millisUntilFinished)
                                    timerDialog?.show()
                                }
                            }
                        }
                    }

                    "auto" -> {
                        if (millisUntilFinished > 30000L) {
                            isPaying = false
                            timerDialog?.cancel()
                            binding.includeTimer.root.visibility = View.INVISIBLE
                        } else {

                            //距结束还有5秒的时候扣下一分钟
                            if (millisUntilFinished < 5000L) {
                                if (!isPaying) {
                                    isPaying = true
                                    getAdditionTimeByAuto()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initChat() {
        mAdapter = DataAdapter.Builder<Chat>()
            .setData(chatList)
            .setLayoutId(R.layout.item_chat)
            .addBindView { itemView, itemData ->
                val itemChatBinding = ItemChatBinding.bind(itemView)
                itemChatBinding.tvUser.text = itemData.name
                itemChatBinding.tvContent.text = itemData.content
            }
            .create()

        binding.rcChatList.adapter = mAdapter
        binding.rcChatList.layoutManager = LinearLayoutManager(this)

        observerReceive = Observer<List<IMMessage>> {
            for (message in it) {
                //从数据库删除这条消息
                NIMClient.getService(MsgService::class.java).deleteChattingHistory(message, false)

                if (message.sessionType == SessionTypeEnum.P2P) {
                    if (message.msgType == MsgTypeEnum.text) {
                        val sessionId = message.sessionId
                        val nick = message.fromNick
                        val content = message.content
                        val account = message.fromAccount
                        val time = message.time
                        JLog.i("nick = $nick")
                        JLog.i("content = $content")
                        JLog.i("account = $account")

                        val chat = Chat(sessionId, nick, content, time, true)
                        chatList.add(chat)
                        mAdapter.notifyItemInserted(chatList.size)
                        binding.rcChatList.smoothScrollToPosition(chatList.size)
                        binding.includeBottom.etMessage.setText("")
                    }

                    if (message.msgType == MsgTypeEnum.custom) {
                        val content = message.attachStr
                        JLog.i("content = $content")
                        val msg = GsonUtils.fromJson(content, MsgInfo::class.java)
                        if (msg != null) {
                            when (msg.type) {
                                "room_extra_time_failed" -> mRoom?.`package` = null
                            }
                        }
                    }
                }
            }
        }

        NIMClient.getService(MsgServiceObserve::class.java).observeReceiveMessage(observerReceive, true)
    }

    private fun sendMessage() {
        if (mUser == null) return
        val message = binding.includeBottom.etMessage.editableText.toString().trim()
        if (message.isNotBlank()) {
            val sessionId = mUser!!.uid.toString()
            val sessionType = SessionTypeEnum.P2P
            val textMessage = MessageBuilder.createTextMessage(sessionId, sessionType, message)
            val config = CustomMessageConfig()
            config.enableHistory = false
            config.enableRoaming = false
            config.enableSelfSync = false
            textMessage.config = config

            ChatRepo.sendMessage(textMessage, false, object : FetchCallback<Void> {
                override fun onException(exception: Throwable?) {
                    JLog.i("exception = ${exception?.message}")
                }

                override fun onFailed(code: Int) {
                    JLog.i("code = $code")
                }

                override fun onSuccess(param: Void?) {
                    JLog.i("onSuccess")

                    // 从数据库删除这条消息
                    NIMClient.getService(MsgService::class.java).deleteChattingHistory(textMessage, false)

                    AppUtil.hideKeyboard(this@AudioActivity, binding.includeBottom.etMessage)
                    val userInfo = getLocalStorage().decodeParcelable("user_info", UserInfo::class.java)
                    if (userInfo != null) {
                        val chat = Chat(sessionId, userInfo.nickname, message, System.currentTimeMillis(), true)
                        chatList.add(chat)
                        mAdapter.notifyItemInserted(chatList.size)
                        binding.rcChatList.smoothScrollToPosition(chatList.size)
                        binding.includeBottom.etMessage.setText("")
                    }
                }
            })
        }
    }

    private fun sendSelfMessage(json: String) {
        val sessionId = mUser!!.uid.toString()
        val sessionType = SessionTypeEnum.P2P
        val attachment = MsgAttachment { json }
        val textMessage = MessageBuilder.createCustomMessage(sessionId, sessionType, attachment)
        val config = CustomMessageConfig()
        config.enableHistory = false
        config.enableRoaming = false
        config.enableSelfSync = false
        textMessage.config = config

        ChatRepo.sendMessage(textMessage, false, object : FetchCallback<Void> {
            override fun onException(exception: Throwable?) {
                JLog.i("send exception = ${exception?.message}")
            }

            override fun onFailed(code: Int) {
                JLog.i("send onFailed, code = $code")
            }

            override fun onSuccess(param: Void?) {
                JLog.i("send onSuccess")

                //从本地数据库删除这条消息
                NIMClient.getService(MsgService::class.java).deleteChattingHistory(textMessage, false)
                //从远程服务器删除这条消息
                //NIMClient.getService(MsgService::class.java).deleteMsgSelf(textMessage)
            }
        })
    }

    private fun getAdditionTime() {
        getUserInfo { userInfo ->
            AdditionDialog(this, userInfo, mRoom!!.room_id) { room, addition ->
                mRoom = room

                //关闭倒计时对话框
                timerDialog?.cancel()

                //刷新房间的token
                mRtcEngine?.renewToken(room.token)

                //如果选择按分钟计时，付费模式变成自动扣除
                if (addition.type == "keep") {
                    mType = "auto"

                    //如果金币不支持扣下一分钟，则双方显示倒计时，切换成普通扣费模式
                    DataManager.getUserInfo { userInfo ->
                        if (userInfo.coin < 60) {
                            mType = "match"
                            val msgInfo = MsgInfo("room_extra_time_failed")
                            sendSelfMessage(GsonUtils.toJson(msgInfo))
                        }
                    }
                }

                //重置计时器
                timer?.cancel()
                initTimer(room.duration * 1000L)
                timer?.start()

                ReportManager.firebaseCustomLog(firebaseAnalytics, "add_time_success", addition.type)
                ReportManager.appsFlyerCustomLog(this, "add_time_success", addition.type)
            }
        }
    }

    private fun getAdditionTimeByAuto() {
        DataManager.getAdditionPriceList("match") { list ->
            for (addition in list) {
                if (addition.type == "keep") {
                    //请求增加1分钟
                    DataManager.additionTime(addition.id, mRoom!!.room_id) { room ->
                        if (room != null) {
                            mRoom = room

                            //关闭倒计时对话框
                            timerDialog?.cancel()

                            //刷新房间的token
                            mRtcEngine?.renewToken(room.token)

                            //重置计时器
                            timer?.cancel()
                            initTimer(room.duration * 1000L)
                            timer?.start()

                            //支付完成
                            isPaying = false

                            if (mType == "auto") {
                                //如果金币不支持扣下一分钟，则双方显示倒计时，切换成普通扣费模式
                                DataManager.getUserInfo { userInfo ->
                                    if (userInfo.coin < 60) {
                                        mType = "match"
                                        val msgInfo = MsgInfo("room_extra_time_failed")
                                        sendSelfMessage(GsonUtils.toJson(msgInfo))
                                    }
                                }
                            }
                        }
                    }

                    ReportManager.firebaseCustomLog(firebaseAnalytics, "add_time_success", addition.type)
                    ReportManager.appsFlyerCustomLog(this, "add_time_success", addition.type)
                }
            }
        }
    }

    private fun setSpeaker() {
        if (!DoubleUtils.isFastDoubleClick()) {
            if (mRtcEngine?.isSpeakerphoneEnabled == true) {
                binding.includeBottomAudio.ivSpeaker.setImageResource(R.drawable.speaker_close)
                mRtcEngine?.setEnableSpeakerphone(false)
            } else {
                binding.includeBottomAudio.ivSpeaker.setImageResource(R.drawable.speaker_active)
                mRtcEngine?.setEnableSpeakerphone(true)
            }
        }
    }

    private fun setVolume() {
        if (!DoubleUtils.isFastDoubleClick()) {
            if (speakerOpen) {
                binding.includeBottomAudio.ivSilent.setImageResource(R.drawable.mute_close)
                mRtcEngine?.muteLocalAudioStream(false)
//            mRtcEngine?.adjustPlaybackSignalVolume(100)
                mVolume = 100
                speakerOpen = false
            } else {
                binding.includeBottomAudio.ivSilent.setImageResource(R.drawable.mute_active)
                mRtcEngine?.muteLocalAudioStream(true)
//            mRtcEngine?.adjustPlaybackSignalVolume(0)

                mVolume = 0
                speakerOpen = true
            }
        }
    }

    /**
     * 更改关注状态
     */
    private fun checkFollow() {
        if (mUser == null) return

        waitingDialog?.show()
        DataManager.getUserInfoById(mUser!!.uid) { userInfo ->
            waitingDialog?.cancel()
            when (userInfo.follow_status) {
                1, 3 -> {
                    DataManager.unfollow(mUser!!.uid) {
                        if (it) {
                            mUser!!.follow_status = 1
                            binding.tvFollow.text = getString(R.string.match_follow)
                            binding.tvFollow.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_yellow, null)
                            binding.tvFollow.setTextColor(Color.BLACK)
                        }
                    }
                }

                else -> {
                    DataManager.follow(mUser!!.uid) {
                        if (it) {
                            mUser!!.follow_status = 0
                            binding.tvFollow.text = getString(R.string.match_followed)
                            binding.tvFollow.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_grey_30, null)
                            binding.tvFollow.setTextColor(Color.WHITE)
                        }
                    }
                }
            }
        }

    }

    private fun handOff() {
        if (!DoubleUtils.isFastDoubleClick()) {
            mRtcEngine?.leaveChannel()
            toFinishPage()
        }
    }

    private fun showReportDialog() {
        if (!DoubleUtils.isFastDoubleClick()) {
            DataManager.getTemplateList {
                ReportDialog(this, mRoom!!.room_id, mUser!!.uid, it)
            }
        }
    }

    private fun showProfileDialog(isMyself: Boolean) {
        if (isMyself) {
            getUserInfo {
                DataManager.getUserInfoById(it.uid) { userInfo ->
                    ProfileDialog(this, userInfo, true, showChat = false) {}
                }
            }
        } else {
            DataManager.getUserInfoById(mUser!!.uid) { userInfo ->
                ProfileDialog(this, userInfo, true, showChat = false) {
                    DataManager.getUserInfoById(mUser!!.uid) { user ->
                        //设置关注状态
                        when (user.follow_status) {
                            1, 3 -> {
                                binding.tvFollow.text = getString(R.string.match_followed)
                                binding.tvFollow.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_grey_30, null)
                                binding.tvFollow.setTextColor(Color.WHITE)
                            }

                            else -> {
                                binding.tvFollow.text = getString(R.string.match_follow)
                                binding.tvFollow.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_yellow, null)
                                binding.tvFollow.setTextColor(Color.BLACK)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showGiftDialog() {
        if (!DoubleUtils.isFastDoubleClick()) {
            DataManager.getUserInfo { userInfo ->
                DataManager.getGiftList {
                    GiftDialog(this, it, mUser!!.uid, userInfo.coin) { gift ->
                        GiftPlayDialog(this, gift)
                    }
                }
            }
        }
    }

    private fun showGameDialog(ticket: LotteryTicket?) {
        if (!DoubleUtils.isFastDoubleClick()) {
            //如果是金币抽奖就加载金币
            if (ticket == null || ticket.type == "coin") {
                DataManager.getLotteryCoin { list ->
                    gameDialog?.setLotteryGiftList(list, "coin")
                    gameDialog?.setLotteryTicket(ticket)
                    gameDialog?.show()
                }
                return
            }

            //如果是礼物抽奖则加载礼物
            if (ticket.type == "gift") {
                DataManager.getLotteryGift { list ->
                    gameDialog?.setLotteryGiftList(list, "gift")
                    gameDialog?.setLotteryTicket(ticket)
                    gameDialog?.show()
                }
            }
        }
    }

    override fun onBackPressed() {
        if (!DoubleUtils.isFastDoubleClick()) {
            QuitDialog(this, "voice") {
                mRtcEngine?.leaveChannel()
                toFinishPage()
            }
        }
    }

    private fun toFinishPage() {
        if (!isFinish) {
            isFinish = true
            val intent = Intent(this, ChatFinishActivity::class.java)
            intent.putExtra("user", mUser)
            intent.putExtra("room", mRoom)
            startActivity(intent)
            finish()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Config.mainHandler != null) {
            Config.mainHandler!!.sendEmptyMessage(0x10006)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Config.roomHandler = null
        timer?.cancel()
        mRtcEngine?.leaveChannel()
        handler.post(RtcEngine::destroy)

        if (observerReceive != null) {
            NIMClient.getService(MsgServiceObserve::class.java).observeReceiveMessage(observerReceive, false)
        }
    }
}