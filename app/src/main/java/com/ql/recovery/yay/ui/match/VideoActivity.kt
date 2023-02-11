package com.ql.recovery.yay.ui.match

import android.content.Intent
import android.graphics.Color
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.blongho.country_data.World
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
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
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.databinding.ActivityVideoBinding
import com.ql.recovery.yay.databinding.ItemChatBinding
import com.ql.recovery.yay.ui.base.BaseActivity
import com.ql.recovery.yay.ui.dialog.*
import com.ql.recovery.yay.ui.self.BlurTransformation
import com.ql.recovery.yay.util.*
import io.agora.rtc2.*
import io.agora.rtc2.video.BeautyOptions
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoCanvas.RENDER_MODE_HIDDEN

class VideoActivity : BaseActivity() {
    private lateinit var binding: ActivityVideoBinding
    private lateinit var mAdapter: DataAdapter<Chat>
    private var chatList = arrayListOf<Chat>()
    private var handler = Handler(Looper.getMainLooper())

    private var mUser: User? = null
    private var timer: CountDownTimer? = null
    private var mRoom: Room? = null
    private var mType: String? = null
    private var mRtcEngine: RtcEngine? = null
    private var beautyOptions: BeautyOptions? = null
    private var observerReceive: Observer<List<IMMessage>>? = null
    private var gameDialog: GameDialog? = null
    private var timerDialog: TimerDialog? = null
    private var requestPayCode = 0x1
    private var isEnough = true
    private var isFinish = false
    private var isPaying = false
    private var faceCheck = false

    private var mRtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            super.onUserJoined(uid, elapsed)
            handler.post {
                setupRemoteVideo(uid)
                timer?.start()
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            handler.post {
                mRtcEngine?.setupRemoteVideo(VideoCanvas(null, RENDER_MODE_HIDDEN, uid))
                binding.flRemote.removeAllViews()
                toFinishPage()
            }
        }

        override fun onLeaveChannel(stats: RtcStats?) {
            handler.post {
                toFinishPage()
            }
        }

        override fun onConnectionLost() {
            //丢失连接10秒
        }

        override fun onError(err: Int) {
            super.onError(err)
            if (err == 110 || err == 109) {
                mRtcEngine?.leaveChannel()
                mRtcEngine?.stopPreview()
            }
        }


        override fun onFacePositionChanged(imageWidth: Int, imageHeight: Int, faceRectArr: Array<out AgoraFacePositionInfo>?) {
//            JLog.i("onFacePositionChanged")
            //人脸检测
//            if (faceRectArr.isNullOrEmpty()) {
//                if (faceCheck) return
//                runOnUiThread {
//                    JLog.i("1111")
//                    faceCheck = true
//                    ToastUtil.showShort(this@VideoActivity, getString(R.string.match_face_not_found))
//                    mRtcEngine?.takeSnapshot(mUser!!.uid, CManager.getCachePath(this@VideoActivity) + System.currentTimeMillis())
//                }
//            }
        }

        override fun onSnapshotTaken(uid: Int, filePath: String?, width: Int, height: Int, errCode: Int) {
            //截图
//            if (errCode == 0 && filePath != null) {
//                runOnUiThread {
//                    JLog.i("2222")
//                    binding.flBlur.visibility = View.VISIBLE
//                    高斯模糊
//                    Glide.with(this@VideoActivity).load(filePath)
//                        .apply(RequestOptions.bitmapTransform(BlurTransformation(this@VideoActivity, 25, 8)))
//                        .into(binding.ivBlur)
//                }
//            }
        }
    }

    override fun getViewBinding(baseBinding: ActivityBaseBinding) {
        binding = ActivityVideoBinding.inflate(layoutInflater, baseBinding.flBase, true)
    }

    override fun initView() {
        setStatusBarLight()
        binding.includeTitle.ivBack.setOnClickListener { onBackPressed() }
        binding.includeTitle.ivAvatar.setOnClickListener { showProfileDialog() }
        binding.includeTitle.tvFollow.setOnClickListener { checkFollow() }
        binding.includeTitle.ivSwitchCamera.setOnClickListener { switchCamera() }
        binding.includeBottom.tvSend.setOnClickListener { sendMessage() }
        binding.flSurface.setOnClickListener { binding.includeBottom.etMessage.clearFocus() }
        binding.includeTimer.tvAdd.setOnClickListener { getAdditionTime() }
        binding.ivBeauty.setOnClickListener { showOrHideBeauty() }
        binding.includeTitle.ivReport.setOnClickListener { showReportDialog() }
        binding.ivGift.setOnClickListener { showGiftDialog() }
        binding.ivGame.setOnClickListener { showGameDialog(null) }

        //设置editText的属性
        binding.includeBottom.etMessage.imeOptions = EditorInfo.IME_ACTION_SEND
        binding.includeBottom.etMessage.setOnFocusChangeListener { view, b ->
            if (!b) {
                AppUtil.hideSoftKeyboard(this, view)
            } else {
//                AppUtil.showSoftKeyboard(this)
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
        mType = intent.getStringExtra("type")
        if (mRoom != null && mUser != null && mType != null) {

            beautyOptions = BeautyOptions()

            initUserInfo(mUser!!)
            initHandler()
            initGameDialog()
            initTimerDialog()
            initTimer(mRoom!!.duration * 1000L)

            getUserInfo {
                initializeAndJoinChannel(mRoom!!, it)
                initChat()
            }
        }
    }

    private fun initUserInfo(user: User) {
        //设置头像
        Glide.with(this).load(user.avatar).apply(RequestOptions.bitmapTransform(CircleCrop())).into(binding.includeTitle.ivAvatar)

        //设置蒙版
        Glide.with(this).load(user.cover_url)
            .apply(RequestOptions.bitmapTransform(BlurTransformation(this, 15, 8)))
            .into(binding.ivBlur)

        //设置名字
        binding.includeTitle.tvName.text = user.nickname

        //设置国家
        if (user.country.isNotBlank()) {
            val flag = World.getFlagOf(user.country)
            binding.includeTitle.ivNation.setImageResource(flag)
        }

        //设置关注状态
        when (user.follow_status) {
            1, 3 -> {
                binding.includeTitle.tvFollow.text = getString(R.string.match_followed)
                binding.includeTitle.tvFollow.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_grey_30, null)
                binding.includeTitle.tvFollow.setTextColor(Color.WHITE)
            }

            else -> {
                binding.includeTitle.tvFollow.text = getString(R.string.match_follow)
                binding.includeTitle.tvFollow.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_yellow, null)
                binding.includeTitle.tvFollow.setTextColor(Color.BLACK)
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
                            }
                        }
                    }

                    0x10001 -> {
                        val bundle = msg.data
                        if (bundle != null) {
                            val gift = bundle.getParcelable<Gift>("gift")
                            if (gift != null) {
                                runOnUiThread {
                                    GiftPlayDialog(this@VideoActivity, gift)
                                }
                            }
                        }
                    }

                    0x10002 -> {
                        val bundle = msg.data
                        if (bundle != null) {
                            val lotteryTicket = bundle.getParcelable<LotteryTicket>("lottery_ticket")
                            if (lotteryTicket != null) {
                                ToastUtil.showLong(this@VideoActivity, getString(R.string.match_ticket_receive))
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

    private fun initTimer(time: Long) {
        JLog.i("total time = $time")
        timer = object : CountDownTimer(time, 1000L) {
            override fun onFinish() {
                //倒计时结束，离开频道
                mRtcEngine?.leaveChannel()
                mRtcEngine?.stopPreview()
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

                    "private" -> {
                        if (millisUntilFinished > 30000L) {
                            isPaying = false
                            timerDialog?.cancel()
                            binding.includeTimer.root.visibility = View.INVISIBLE
                        } else {
                            //只扣除发起方的金币
                            val from = intent.getStringExtra("from")
                            if (from != null && from == "sender") {

                                //金币不足
                                if (!isEnough) {
                                    binding.includeTimer.root.visibility = View.VISIBLE

                                    //只有玩游戏的时候才弹出倒计时对话框
                                    if (gameDialog?.isShowing == true) {
                                        timerDialog?.setTimer(millisUntilFinished)
                                        timerDialog?.show()
                                    }
                                    return
                                }

                                //距结束还有5秒的时候扣下一分钟
                                if (millisUntilFinished < 5000L) {
                                    if (!isPaying) {
                                        isPaying = true
                                        getAdditionTimeByPersonal()
                                    }
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

                //重置未读数
                NIMClient.getService(MsgService::class.java).setChattingAccount(message.fromAccount, message.sessionType)

                //从数据库删除消息
                NIMClient.getService(MsgService::class.java).deleteChattingHistory(message, false)

                if (message.msgType == MsgTypeEnum.text) {
                    val sessionId = message.sessionId

                    //从服务端删除消息
                    NIMClient.getService(MsgService::class.java).clearServerHistory(sessionId, true)

                    //筛选给我发的消息
                    val nickname = message.fromNick
                    val content = message.content
                    val time = message.time

                    val chat = Chat(sessionId, nickname, content, time, true)
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

        NIMClient.getService(MsgServiceObserve::class.java).observeReceiveMessage(observerReceive, true)
    }

    private fun getAdditionTime() {
        DataManager.getUserInfo { userInfo ->
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

                            isEnough = true

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

                            if (mType == "private") {
                                //如果金币不支持扣下一分钟，则发起方显示倒计时
                                DataManager.getUserInfo { userInfo ->
                                    isEnough = userInfo.coin >= 60
                                }
                            }
                        } else {
                            isEnough = false
                        }
                    }
                }
            }
        }
    }

    private fun getAdditionTimeByPersonal() {
        DataManager.getAdditionPriceList("private") { list ->
            if (list.size == 1) {
                //请求增加1分钟
                DataManager.additionTime(list[0].id, mRoom!!.room_id) { room ->
                    if (room != null) {

                        isEnough = true

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

                        if (mType == "private") {
                            //如果金币不支持扣下一分钟，则发起方显示倒计时
                            DataManager.getUserInfo { userInfo ->
                                isEnough = userInfo.coin >= 60
                            }
                        }
                    } else {
                        isEnough = false
                    }
                }
            }
        }
    }

    private fun initializeAndJoinChannel(room: Room, userInfo: UserInfo) {
        try {
            val config = RtcEngineConfig()
            config.mContext = applicationContext
            config.mAppId = Config.AGORA_APP_ID
            config.mEventHandler = mRtcEventHandler
            config.mAudioScenario = Constants.AudioScenario.getValue(Constants.AudioScenario.DEFAULT)
            mRtcEngine = RtcEngine.create(config)

        } catch (ex: Exception) {
            ToastUtil.showShort(this, ex.message)
        }

        //开启美颜
        mRtcEngine?.setBeautyEffectOptions(true, beautyOptions)

        //开启人脸检测
//        mRtcEngine?.enableFaceDetection(true)

        //将SurfaceView对象传入Agora，以渲染本地视频
        mRtcEngine?.setupLocalVideo(VideoCanvas(binding.surfaceLocal, RENDER_MODE_HIDDEN, userInfo.uid))

        mRtcEngine?.setEnableSpeakerphone(true)

        mRtcEngine?.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)

        //视频默认禁用，你需要调用enableVideo启用视频流
        mRtcEngine?.enableVideo()

        val options = ChannelMediaOptions()
        //视频通话场景下，设置频道场景为 BROADCASTING
        options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
        //将用户角色设置为 BROADCASTER
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
        options.autoSubscribeAudio = true
        options.autoSubscribeVideo = true
        options.publishMicrophoneTrack = true
        options.publishCameraTrack = true

        //开启本地视频预览
        mRtcEngine?.startPreview()

        //加入频道
        val res = mRtcEngine?.joinChannel(room.token, room.room_id, userInfo.uid, options)
        if (res != 0) {
            ToastUtil.showShort(this, "join channel failed")
        }
    }

    private fun setupRemoteVideo(uid: Int) {
        handler.post {
            val surfaceView = SurfaceView(this)
            binding.flRemote.addView(surfaceView, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            mRtcEngine?.setupRemoteVideo(VideoCanvas(surfaceView, RENDER_MODE_HIDDEN, 1, uid))
        }
    }

    private fun showOrHideBeauty() {
        if (!DoubleUtils.isFastDoubleClick()) {
            BeautyDialog(this, mRtcEngine, beautyOptions!!) {}
        }
    }

    private fun checkFollow() {
        if (mUser == null) return
        if (!DoubleUtils.isFastDoubleClick()) {
            DataManager.getUserInfoById(mUser!!.uid) { userInfo ->
                when (userInfo.follow_status) {
                    1, 3 -> {
                        DataManager.unfollow(mUser!!.uid) {
                            if (it) {
                                mUser!!.follow_status = 1
                                binding.includeTitle.tvFollow.text = getString(R.string.match_follow)
                                binding.includeTitle.tvFollow.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_yellow, null)
                                binding.includeTitle.tvFollow.setTextColor(Color.BLACK)
                            }
                        }
                    }

                    else -> {
                        DataManager.follow(mUser!!.uid) {
                            if (it) {
                                mUser!!.follow_status = 0
                                binding.includeTitle.tvFollow.text = getString(R.string.match_followed)
                                binding.includeTitle.tvFollow.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_grey_30, null)
                                binding.includeTitle.tvFollow.setTextColor(Color.WHITE)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun switchCamera() {
        if (!DoubleUtils.isFastDoubleClick()) {
            getUserInfo { userInfo ->
                if (userInfo.is_vip) {
                    mRtcEngine?.switchCamera()
                } else {
                    PrimeDialog(this, false) {}
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

    private fun showProfileDialog() {
        if (!DoubleUtils.isFastDoubleClick()) {
            DataManager.getUserInfoById(mUser!!.uid) { userInfo ->
                ProfileDialog(this, userInfo, true, showChat = false) {}
            }
        }
    }

    private fun showReportDialog() {
        if (!DoubleUtils.isFastDoubleClick()) {
            DataManager.getTemplateList {
                ReportDialog(this, mRoom!!.room_id, mUser!!.uid, it)
            }
        }
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
                    JLog.i("onFailed, code = $code")
                }

                override fun onSuccess(param: Void?) {
                    JLog.i("onSuccess")

                    //从本地数据库删除这条消息
                    NIMClient.getService(MsgService::class.java).deleteChattingHistory(textMessage, false)
                    //从远程服务器删除这条消息
//                    NIMClient.getService(MsgService::class.java).deleteMsgSelf(textMessage)

                    //隐藏软键盘
                    AppUtil.hideKeyboard(this@VideoActivity, binding.includeBottom.etMessage)

                    getUserInfo {
                        val chat = Chat(sessionId, getString(R.string.match_me), message, System.currentTimeMillis(), true)
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
        mRtcEngine?.stopPreview()
        handler.post(RtcEngine::destroy)

        if (observerReceive != null) {
            NIMClient.getService(MsgServiceObserve::class.java).observeReceiveMessage(observerReceive, false)
        }
    }

    override fun onBackPressed() {
        QuitDialog(this, "video") {
            mRtcEngine?.leaveChannel()
            mRtcEngine?.stopPreview()
            toFinishPage()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestPayCode) {

        }
    }
}