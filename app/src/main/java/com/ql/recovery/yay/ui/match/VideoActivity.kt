package com.ql.recovery.yay.ui.match

import android.content.Intent
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
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
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.databinding.ActivityVideoBinding
import com.ql.recovery.yay.databinding.ItemChatBinding
import com.ql.recovery.yay.manager.ReportManager
import com.ql.recovery.yay.ui.base.BaseActivity
import com.ql.recovery.yay.ui.dialog.*
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
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private var mUser: User? = null
    private var timer: CountDownTimer? = null
    private var mRoom: Room? = null
    private var mType: String? = null
    private var mRtcEngine: RtcEngine? = null
    private var observerReceive: Observer<List<IMMessage>>? = null
    private var gameDialog: GameDialog? = null
    private var timerDialog: TimerDialog? = null
    private var waitingDialog: WaitingDialog? = null
    private var storeDialog: StoreDialog? = null
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
            //????????????10???
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
            //????????????
//            if (faceRectArr.isNullOrEmpty()) {
//                if (faceCheck) return
//                JLog.i("1111")
//                faceCheck = true
//                mRtcEngine?.takeSnapshot(mUser!!.uid, CManager.getCachePath(this@VideoActivity) + System.currentTimeMillis())
//            }
        }

        override fun onSnapshotTaken(uid: Int, filePath: String?, width: Int, height: Int, errCode: Int) {
            //??????
//            if (errCode == 0 && filePath != null) {
//                runOnUiThread {
//                    JLog.i("2222")
//                    binding.flBlur.visibility = View.VISIBLE
//                    //????????????
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
        //??????????????????
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        binding.includeTitle.ivBack.setOnClickListener { onBackPressed() }
        binding.ivAvatar.setOnClickListener { showProfileDialog() }
        binding.ivFollow.setOnClickListener { checkFollow() }
        binding.includeTitle.ivSwitchCamera.setOnClickListener { switchCamera() }
        binding.includeBottom.tvSend.setOnClickListener { sendMessage() }
        binding.flSurface.setOnClickListener { binding.includeBottom.etMessage.clearFocus() }
        binding.includeTitle.ivReport.setOnClickListener { showReportDialog() }
        binding.ivGift.setOnClickListener { showGiftDialog() }
        binding.ivGame.setOnClickListener { showGameDialog(null) }
        binding.includeBottom.ivAddCoin.setOnClickListener { showStoreDialog() }

        //??????editText?????????
        binding.includeBottom.etMessage.imeOptions = EditorInfo.IME_ACTION_SEND
        binding.includeBottom.etMessage.setOnFocusChangeListener { view, b ->
            if (!b) {
                AppUtil.hideSoftKeyboard(this, view)
            } else {
//                AppUtil.showSoftKeyboard(this)
            }
        }

        binding.includeBottom.etMessage.setOnEditorActionListener { _, actionId, _ ->
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
            waitingDialog = WaitingDialog(this)
            firebaseAnalytics = Firebase.analytics

            initUserInfo(mUser!!)
            initHandler()
            initCostNotice(mRoom!!, mType!!)
            initGameDialog()
            initTimerDialog()
            initTimer(mRoom!!.duration * 1000L)

            getUserInfo { userInfo ->
                storeDialog = StoreDialog(this, userInfo)
                initializeAndJoinChannel(mRoom!!, userInfo)
                initChat()
            }
        }
    }

    private fun initUserInfo(user: User) {
        //????????????
        Glide.with(this).load(user.avatar).apply(RequestOptions.bitmapTransform(CircleCrop())).into(binding.ivAvatar)

        //????????????
//        Glide.with(this).load(user.cover_url)
//            .apply(RequestOptions.bitmapTransform(BlurTransformation(this, 15, 8)))
//            .into(binding.ivBlur)

        //??????????????????
        when (user.follow_status) {
            1, 3 -> {
                binding.ivFollow.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_grey_30, null)
            }

            else -> {
                binding.ivFollow.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_yellow, null)
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

                                //????????????Token
                                mRtcEngine?.renewToken(room.token)

                                //???????????????
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

                    0x10005 -> {
                        mRtcEngine?.stopPreview()
                        mRtcEngine?.leaveChannel()
                    }
                }
            }
        }
    }

    private fun initCostNotice(room: Room, type: String) {
        getUserInfo { userInfo ->
            if (userInfo.role == "normal") {
                binding.includeBottom.tvCoin.text = userInfo.coin.toString()
                when (type) {
                    "match" -> {
                        binding.includeBottom.tvRoomCostNotice.text = String.format(getString(R.string.match_room_cost_minute_start), room.duration, 60)
                    }

                    "pri_match", "private" -> {
                        binding.includeBottom.tvRoomCostNotice.text = String.format(getString(R.string.match_call_cost), 120)
                    }
                }
            } else {
                binding.includeBottom.llCoin.visibility = View.GONE
                when (type) {
                    "match" -> {
                        binding.includeBottom.tvRoomCostNotice.text = String.format(getString(R.string.match_call_income), 60)
                    }

                    "pri_match", "private" -> {
                        binding.includeBottom.tvRoomCostNotice.text = String.format(getString(R.string.match_call_income), 120)
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
            getAdditionTimeByAuto()
        }
    }

    private fun initTimer(time: Long) {
        JLog.i("total time = $time")
        timer = object : CountDownTimer(time, 1000L) {
            override fun onFinish() {
                //??????????????????????????????
                mRtcEngine?.leaveChannel()
                mRtcEngine?.stopPreview()
                timerDialog?.cancel()

                ReportManager.firebaseCustomLog(firebaseAnalytics, "video_chat_timeout", "timeout")
                ReportManager.appsFlyerCustomLog(this@VideoActivity, "video_chat_timeout", "timeout")
            }

            override fun onTick(millisUntilFinished: Long) {
                val progress = AppUtil.timeStamp2Date(millisUntilFinished, "ss").toInt()
                binding.progressView.progress = progress
                when (mType) {
                    "match" -> {
                        if (millisUntilFinished > 25000L) {
                            binding.progressView.visibility = View.INVISIBLE
                            timerDialog?.cancel()
                        } else {
                            binding.progressView.visibility = View.VISIBLE
                            storeDialog?.setTime(progress)

                            //???????????????????????????????????????????????????????????????
                            getUserInfo { userInfo ->
                                if (userInfo.role == "normal") {
                                    if (userInfo.coin > 60) {
                                        mType = "auto"
                                    }

                                    //???????????????????????????????????????????????????
                                    if (gameDialog?.isShowing == true) {
                                        timerDialog?.setTimer(millisUntilFinished)
                                        timerDialog?.show()
                                    }
                                }
                            }
                        }
                    }

                    "pri_match" -> {
                        if (millisUntilFinished > 30000L) {
                            isPaying = false
                            timerDialog?.cancel()
                            binding.progressView.visibility = View.INVISIBLE
                        } else {
                            storeDialog?.setTime(progress)
                            //??????????????????????????????
                            getUserInfo { userInfo ->
                                if (userInfo.role == "normal") {
                                    if (userInfo.coin < 120) {
                                        binding.progressView.visibility = View.VISIBLE

                                        //???????????????????????????????????????????????????
                                        if (gameDialog?.isShowing == true) {
                                            timerDialog?.setTimer(millisUntilFinished)
                                            timerDialog?.show()
                                        }

                                    } else {
                                        //???????????????2???????????????????????????
                                        if (millisUntilFinished < 2000L) {
                                            if (!isPaying) {
                                                isPaying = true
                                                getAdditionTimeByPersonal()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "private" -> {
                        if (millisUntilFinished > 30000L) {
                            isPaying = false
                            timerDialog?.cancel()
                            binding.progressView.visibility = View.INVISIBLE
                        } else {
                            storeDialog?.setTime(progress)

                            //???????????????????????????
                            val from = intent.getStringExtra("from")
                            if (from != null && from == "sender") {

                                getUserInfo { userInfo ->
                                    if (userInfo.coin < 120) {
                                        binding.progressView.visibility = View.VISIBLE

                                        //???????????????????????????????????????????????????
                                        if (gameDialog?.isShowing == true) {
                                            timerDialog?.setTimer(millisUntilFinished)
                                            timerDialog?.show()
                                        }

                                    } else {
                                        //???????????????2???????????????????????????
                                        if (millisUntilFinished < 2000L) {
                                            if (!isPaying) {
                                                isPaying = true
                                                getAdditionTimeByPersonal()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "auto" -> {
                        if (millisUntilFinished > 25000L) {
                            isPaying = false
                            timerDialog?.cancel()
                            binding.progressView.visibility = View.INVISIBLE
                        } else {
                            storeDialog?.setTime(progress)
                            getUserInfo { userInfo ->
                                if (userInfo.role == "normal") {
                                    if (userInfo.coin < 60) {
                                        mType = "match"
                                    } else {
                                        //???????????????2???????????????????????????,?????????????????????????????????
                                        if (millisUntilFinished < 2000L) {
                                            if (!isPaying) {
                                                isPaying = true
                                                getAdditionTimeByAuto()
                                            }
                                        }

                                        //???????????????????????????????????????????????????
                                        if (gameDialog?.isShowing == true) {
                                            timerDialog?.setTimer(millisUntilFinished)
                                            timerDialog?.show()
                                        }
                                    }
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

                //???????????????
                NIMClient.getService(MsgService::class.java).setChattingAccount(message.fromAccount, message.sessionType)

                //????????????????????????
                NIMClient.getService(MsgService::class.java).deleteChattingHistory(message, false)

                if (message.msgType == MsgTypeEnum.text) {
                    val sessionId = message.sessionId

                    //????????????????????????
                    NIMClient.getService(MsgService::class.java).clearServerHistory(sessionId, true)

                    //????????????????????????
                    var nickname = message.fromNick
                    val content = message.content
                    val time = message.time

                    if (nickname.isBlank()) {
                        nickname = mUser!!.nickname
                    }

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

    private fun getAdditionTimeByAuto() {
        DataManager.getAdditionPriceList("match") { list ->
            for (addition in list) {
                if (addition.type == "keep") {
                    //????????????1??????
                    DataManager.additionTime(addition.id, mRoom!!.room_id) { room ->
                        if (room != null) {

                            isEnough = true

                            mRoom = room

                            //????????????????????????
                            timerDialog?.cancel()

                            //???????????????token
                            mRtcEngine?.renewToken(room.token)

                            //???????????????
                            timer?.cancel()
                            initTimer(room.duration * 1000L)
                            timer?.start()

                            //????????????
                            isPaying = false

                            //?????????????????????????????????????????????????????????
                            DataManager.getUserInfo {}

                        } else {
                            isEnough = false
                        }
                    }

                    ReportManager.firebaseCustomLog(firebaseAnalytics, "video_add_time_success", addition.type)
                    ReportManager.appsFlyerCustomLog(this@VideoActivity, "video_add_time_success", addition.type)
                }
            }
        }
    }

    private fun getAdditionTimeByPersonal() {
        DataManager.getAdditionPriceList("private") { list ->
            if (list.size == 1) {
                //????????????1??????
                DataManager.additionTime(list[0].id, mRoom!!.room_id) { room ->
                    if (room != null) {

                        isEnough = true

                        mRoom = room

                        //????????????????????????
                        timerDialog?.cancel()

                        //???????????????token
                        mRtcEngine?.renewToken(room.token)

                        //???????????????
                        timer?.cancel()
                        initTimer(room.duration * 1000L)
                        timer?.start()

                        //????????????
                        isPaying = false

                        if (mType == "auto") {
                            //?????????????????????????????????????????????????????????????????????????????????????????????
                            DataManager.getUserInfo { userInfo ->
                                if (userInfo.coin < 60) {
                                    mType = "match"
                                    val msgInfo = MsgInfo("room_extra_time_failed")
                                    sendSelfMessage(GsonUtils.toJson(msgInfo))
                                }
                            }
                        }

                        if (mType == "private") {
                            //??????????????????????????????????????????????????????????????????
                            DataManager.getUserInfo { userInfo ->
                                isEnough = userInfo.coin >= 60
                            }
                        }
                    } else {
                        isEnough = false
                    }

                    ReportManager.firebaseCustomLog(firebaseAnalytics, "video_add_time_success", list[0].type)
                    ReportManager.appsFlyerCustomLog(this@VideoActivity, "video_add_time_success", list[0].type)
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

        //????????????
        val beautyOptions = BeautyOptions()
        val beautyParam = getLocalStorage().decodeParcelable("beauty_param", BeautyParam::class.java)
        if (beautyParam != null) {
            //??????????????????
            beautyOptions.lighteningLevel = beautyParam.lighteningLevel
            beautyOptions.smoothnessLevel = beautyParam.smoothnessLevel
            beautyOptions.rednessLevel = beautyParam.rednessLevel
            beautyOptions.sharpnessLevel = beautyParam.sharpnessLevel
        }
        mRtcEngine?.setBeautyEffectOptions(true, beautyOptions)

        //??????????????????
//        mRtcEngine?.enableFaceDetection(true)

        //???SurfaceView????????????Agora????????????????????????
        mRtcEngine?.setupLocalVideo(VideoCanvas(binding.surfaceLocal, RENDER_MODE_HIDDEN, userInfo.uid))

        mRtcEngine?.setEnableSpeakerphone(true)

        mRtcEngine?.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)

        //????????????????????????????????????enableVideo???????????????
        mRtcEngine?.enableVideo()

        val options = ChannelMediaOptions()
        //????????????????????????????????????????????? BROADCASTING
        options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
        //???????????????????????? BROADCASTER
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
        options.autoSubscribeAudio = true
        options.autoSubscribeVideo = true
        options.publishMicrophoneTrack = true
        options.publishCameraTrack = true

        //????????????????????????
        mRtcEngine?.startPreview()

        //????????????
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
            getUserInfo { userInfo ->
                BeautyDialog(this, mRtcEngine, userInfo) {
                    mRtcEngine?.setupLocalVideo(VideoCanvas(binding.surfaceLocal, RENDER_MODE_HIDDEN, userInfo.uid))
                }
            }
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
//                                binding.includeTitle.tvFollow.text = getString(R.string.match_follow)
                                binding.ivFollow.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_yellow, null)
//                                binding.includeTitle.tvFollow.setTextColor(Color.BLACK)
                            }
                        }
                    }

                    else -> {
                        DataManager.follow(mUser!!.uid) {
                            if (it) {
                                mUser!!.follow_status = 0
//                                binding.includeTitle.tvFollow.text = getString(R.string.match_followed)
                                binding.ivFollow.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_grey_30, null)
//                                binding.includeTitle.tvFollow.setTextColor(Color.WHITE)
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
                if (userInfo.is_vip || userInfo.role == "anchor") {
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
                ProfileDialog(this, userInfo, true, showChat = false) {
                    DataManager.getUserInfoById(mUser!!.uid) { user ->
                        //??????????????????
                        when (user.follow_status) {
                            1, 3 -> {
//                                binding.includeTitle.tvFollow.text = getString(R.string.match_followed)
                                binding.ivFollow.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_grey_30, null)
//                                binding.includeTitle.tvFollow.setTextColor(Color.WHITE)
                            }

                            else -> {
//                                binding.includeTitle.tvFollow.text = getString(R.string.match_follow)
                                binding.ivFollow.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_yellow, null)
//                                binding.includeTitle.tvFollow.setTextColor(Color.BLACK)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showReportDialog() {
        if (!DoubleUtils.isFastDoubleClick()) {
            DataManager.getTemplateList {
                ReportDialog(this, mRoom!!.room_id, mUser!!.uid, it)

                ReportManager.firebaseCustomLog(firebaseAnalytics, "video_report_click", "report click")
                ReportManager.appsFlyerCustomLog(this@VideoActivity, "video_report_click", "report click")
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

                    //????????????????????????????????????
                    NIMClient.getService(MsgService::class.java).deleteChattingHistory(textMessage, false)
                    //????????????????????????????????????
//                    NIMClient.getService(MsgService::class.java).deleteMsgSelf(textMessage)

                    //???????????????
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

                //????????????????????????????????????
                NIMClient.getService(MsgService::class.java).deleteChattingHistory(textMessage, false)

                //????????????????????????????????????
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
            //????????????????????????????????????
            if (ticket == null || ticket.type == "coin") {
                waitingDialog?.show()
                DataManager.getLotteryCoin { list ->
                    waitingDialog?.cancel()
                    gameDialog?.setLotteryGiftList(list, "coin")
                    gameDialog?.setLotteryTicket(ticket)
                    gameDialog?.show()
                }
                return
            }

            //????????????????????????????????????
            if (ticket.type == "gift") {
                DataManager.getLotteryGift { list ->
                    waitingDialog?.cancel()
                    gameDialog?.setLotteryGiftList(list, "gift")
                    gameDialog?.setLotteryTicket(ticket)
                    gameDialog?.show()
                }
            }
        }
    }

    private fun showStoreDialog() {
        storeDialog?.show()
    }


    override fun onStop() {
        super.onStop()
        if (Config.mainHandler != null) {
            Config.mainHandler!!.sendEmptyMessage(0x10006)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //??????????????????
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

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
