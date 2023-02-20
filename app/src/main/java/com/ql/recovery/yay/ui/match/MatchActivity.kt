package com.ql.recovery.yay.ui.match

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.*
import android.view.View
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.gson.reflect.TypeToken
import com.ql.recovery.bean.*
import com.ql.recovery.config.Config
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.config.ChooseType
import com.ql.recovery.yay.config.MatchStatus
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.databinding.ActivityMatchBinding
import com.ql.recovery.yay.manager.ImageManager
import com.ql.recovery.yay.manager.ReportManager
import com.ql.recovery.yay.service.SosWebSocketClientService
import com.ql.recovery.yay.ui.base.BaseActivity
import com.ql.recovery.yay.ui.dialog.*
import com.ql.recovery.yay.util.DoubleUtils
import com.ql.recovery.yay.util.GsonUtils
import com.ql.recovery.yay.util.JLog
import com.ql.recovery.yay.util.ToastUtil
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig

class MatchActivity : BaseActivity() {
    private lateinit var binding: ActivityMatchBinding
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var timer: CountDownTimer? = null
    private var mWebSocketService: SosWebSocketClientService? = null
    private var bindIntent: Intent? = null
    private var matchVideoDialog: MatchVideoDialog? = null
    private var matchAudioDialog: MatchAudioDialog? = null
    private var matchFailedDialog: MatchFailedDialog? = null
    private var mRtcEngine: RtcEngine? = null
    private var mMatcher: User? = null
    private var mHandler = Handler(Looper.getMainLooper())
    private var exoPlayer: ExoPlayer? = null
    private var mWaitingDialog: WaitingDialog? = null
    private var mRtcEventHandler = object : IRtcEngineEventHandler() {}

    //    private var videoUri = "https://picpro-cn.oss-cn-shenzhen.aliyuncs.com/feedback/match_video.mp4"
//    private var audioUri = "https://picpro-cn.oss-cn-shenzhen.aliyuncs.com/feedback/match_audio.mp4"
//    private var gameUri = "https://picpro-cn.oss-cn-shenzhen.aliyuncs.com/feedback/match_game.mp4"
    private var videoUri = "file:///android_asset/videos/match_video.mp4"
    private var audioUri = "file:///android_asset/videos/match_audio.mp4"

    override fun getViewBinding(baseBinding: ActivityBaseBinding) {
        binding = ActivityMatchBinding.inflate(layoutInflater, baseBinding.flBase, true)
    }

    override fun initView() {
        binding.ivClose.setOnClickListener { finish() }
        binding.ivBeauty.setOnClickListener { checkPreview() }
        binding.llChooseGender.setOnClickListener { showFilterDialog(ChooseType.Gender) }
        binding.llChooseRegion.setOnClickListener { showFilterDialog(ChooseType.Region) }
        binding.ivGame.setOnClickListener { showGameDialog() }
        binding.flGameStart.setOnClickListener { startGamePage() }
        binding.llGameChooseGender.setOnClickListener { showFilterDialog(ChooseType.Gender) }
    }

    override fun initData() {
        exoPlayer = ExoPlayer.Builder(this).build()
        firebaseAnalytics = Firebase.analytics
        mWaitingDialog = WaitingDialog(this)
        flushConfig()
        initLocalSurface()

        val type = intent.getStringExtra("type")
        if (type != null) {
            when (type) {
                "video" -> {
                    binding.tvMatchTitle.text = getString(R.string.match_video)
                    binding.ivBeauty.visibility = View.VISIBLE
                    //初始化动画
                    initPlayer(type)
                }

                "voice" -> {
                    binding.tvMatchTitle.text = getString(R.string.match_audio)
                    binding.ivBeauty.visibility = View.GONE
                    //初始化动画
                    initPlayer(type)
                }

                "game" -> {
                    binding.tvMatchTitle.text = getString(R.string.match_game)
                    binding.ivBeauty.visibility = View.GONE
                    binding.flGame.visibility = View.VISIBLE
                    binding.playerView.visibility = View.GONE
                    binding.tvMatching.visibility = View.GONE
                    binding.llFilter.visibility = View.GONE
                }
            }

            if (type == "video" || type == "voice") {
                getUserInfo { userInfo ->
                    //只有普通用户在匹配页开启匹配，主播进app就开启匹配
//                    if (userInfo.role == "normal") {
                    initMatchServer(type)
                    initTimer(type)
                    initNotice(type)
                    initReport(userInfo)
//                    }
                }
            }
        }
    }

    private fun initPlayer(type: String) {
        when (type) {
            "video" -> {
                val mediaItem = MediaItem.fromUri(videoUri)
                exoPlayer?.setMediaItem(mediaItem)
            }
            "voice" -> {
                val mediaItem = MediaItem.fromUri(audioUri)
                exoPlayer?.setMediaItem(mediaItem)
            }
        }

        binding.playerView.setShutterBackgroundColor(Color.TRANSPARENT)
        binding.playerView.player = exoPlayer
        exoPlayer!!.repeatMode = Player.REPEAT_MODE_ALL
        exoPlayer!!.prepare()
        exoPlayer!!.playWhenReady = true
    }

    private fun flushConfig() {
        val config = getMatchConfig()
        when (config.target_sex) {
            0 -> {
                binding.tvSex.text = getString(R.string.home_all_gender)
                binding.tvGameSex.text = getString(R.string.home_all_gender)
                binding.ivSex.setImageResource(R.drawable.in_xb)
                binding.ivGameSex.setImageResource(R.drawable.in_xb)
            }

            1 -> {
                binding.tvSex.text = getString(R.string.home_male)
                binding.tvGameSex.text = getString(R.string.home_male)
                binding.ivSex.setImageResource(R.drawable.man)
                binding.ivGameSex.setImageResource(R.drawable.man)
            }

            2 -> {
                binding.tvSex.text = getString(R.string.home_female)
                binding.tvGameSex.text = getString(R.string.home_female)
                binding.ivSex.setImageResource(R.drawable.woman)
                binding.ivGameSex.setImageResource(R.drawable.woman)
            }
        }

        if (config.country_name.isBlank()) {
            binding.tvRegion.text = getString(R.string.home_match_global)
        } else {
            binding.tvRegion.text = config.country_name
        }

        if (config.country_locale.isNotBlank()) {
            val res = "file:///android_asset/images/${config.country_locale}.png"
            ImageManager.getBitmap(this, res) { bitmap ->
                binding.ivRegion.setImageBitmap(bitmap)
            }
        } else {
            val res = "file:///android_asset/images/GLOBAL.png"
            ImageManager.getBitmap(this, res) { bitmap ->
                binding.ivRegion.setImageBitmap(bitmap)
            }
        }
    }

    private fun initMatchServer(type: String) {
        val userInfo = getLocalStorage().decodeParcelable("user_info", UserInfo::class.java)
        if (userInfo != null) {
            when (type) {
                "video" -> {
                    matchVideoDialog = MatchVideoDialog(this) { status ->
                        when (status) {
                            MatchStatus.Accept -> {
                                acceptInvite(type)
                            }

                            MatchStatus.Reject -> {
                                rejectInvite()
                                rematch(1500L)
                            }

                            MatchStatus.Cancel -> {
                                binding.ivUserBg.visibility = View.GONE
                            }
                        }
                    }
                }

                "voice" -> {
                    matchAudioDialog = MatchAudioDialog(this) { status ->
                        when (status) {
                            MatchStatus.Accept -> {
                                acceptInvite(type)
                            }

                            MatchStatus.Reject -> {
                                rejectInvite()
                                rematch(1500L)
                            }

                            MatchStatus.Cancel -> {
                                binding.ivUserBg.visibility = View.GONE
                            }
                        }
                    }
                }
            }

            initHandler(type)

            startWebSocketService(userInfo.uid, type, getMatchConfig())
        }
    }

    private fun initHandler(type: String) {
        Config.messageHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    0x1 -> {
                        val data = msg.obj as String
                        JLog.i("data = $data")
                        val message = GsonUtils.fromJson(data, MsgInfo::class.java) ?: return

                        when (message.type) {
                            "match_invite" -> {
                                val typeToken = object : TypeToken<MessageInfo<User>>() {}
                                val info = GsonUtils.fromJson<MessageInfo<User>>(data, typeToken.type) ?: return

                                timer?.cancel()
                                matchFailedDialog?.cancel()

                                when (type) {
                                    "video" -> {
                                        mMatcher = info.content
                                        matchVideoDialog?.setUser(info.content)
                                        if (!isFinishing && !isDestroyed) {
                                            //设置背景图片
                                            binding.ivUserBg.visibility = View.VISIBLE
                                            Glide.with(this@MatchActivity).load(info.content.cover_url).into(binding.ivUserBg)

                                            matchVideoDialog?.startConnect()
                                            matchVideoDialog?.show()

                                            val config = getMatchConfig()
                                            if (config.hand_free) {
                                                matchVideoDialog?.waitConnect()
                                            }
                                        }
                                    }

                                    "voice" -> {
                                        mMatcher = info.content
                                        matchAudioDialog?.setUser(info.content)
                                        if (!isFinishing && !isDestroyed) {
                                            //设置背景图片
                                            binding.ivUserBg.visibility = View.VISIBLE
                                            Glide.with(this@MatchActivity).load(info.content.cover_url).into(binding.ivUserBg)

                                            matchAudioDialog?.startConnect()
                                            matchAudioDialog?.show()

                                            val config = getMatchConfig()
                                            if (config.hand_free) {
                                                matchAudioDialog?.waitConnect()
                                            }
                                        }
                                    }
                                }
                            }

                            "match_accept_invite" -> {

                            }

                            "match_reject_invite" -> {
                                //重新计算匹配中的时间
                                timer?.cancel()
                                initTimer(type)
                                timer?.start()

                                when (type) {
                                    "video" -> {
                                        matchVideoDialog?.handOff()
                                        waiting(1500L) {
                                            matchVideoDialog?.cancel()
                                        }
                                    }

                                    "voice" -> {
                                        matchAudioDialog?.handOff()
                                        waiting(1500L) {
                                            matchAudioDialog?.cancel()
                                        }
                                    }
                                }

                                //5秒后进入匹配池
                                rematch(5000L)
                            }

                            "match_start_play" -> {
                                val typeToken = object : TypeToken<MessageInfo<Room>>() {}
                                val info = GsonUtils.fromJson<MessageInfo<Room>>(data, typeToken.type) ?: return

                                getLocalStorage().encode("recent_record", "match")

                                when (type) {
                                    "video" -> {
                                        matchVideoDialog?.cancel()
                                        startVideoPage(info.content)
                                    }
                                    "voice" -> {
                                        matchAudioDialog?.cancel()
                                        startAudioPage(info.content)
                                    }
                                }
                            }

                            "match_invite_timeout" -> {
                                //重新计算匹配中的时间
                                timer?.cancel()
                                initTimer(type)
                                timer?.start()

                                when (type) {
                                    "video" -> {
                                        matchVideoDialog?.connectingTimeout()
                                        matchVideoDialog?.cancel()
                                        binding.ivUserBg.visibility = View.GONE
                                    }

                                    "voice" -> {
                                        matchAudioDialog?.connectingTimeout()
                                        matchAudioDialog?.cancel()
                                        binding.ivUserBg.visibility = View.GONE
                                    }
                                }

                                rematch(1000L)
                            }

                            "match_peer_disconnect" -> {
                                timer?.cancel()
                                when (type) {
                                    "video" -> {
                                        matchVideoDialog?.handOff()
                                        matchVideoDialog?.cancel()
                                        binding.ivUserBg.visibility = View.GONE
                                        rematch(1500L)
                                    }
                                    "voice" -> {
                                        matchAudioDialog?.handOff()
                                        matchAudioDialog?.cancel()
                                        binding.ivUserBg.visibility = View.GONE
                                        rematch(1500L)
                                    }
                                }
                            }

                            "match_countdown" -> {
                                val typeToken = object : TypeToken<MessageInfo<Long>>() {}
                                val info = GsonUtils.fromJson<MessageInfo<Long>>(data, typeToken.type) ?: return
                                when (type) {
                                    "video" -> {
                                        matchVideoDialog?.setTime(info.content)
                                    }

                                    "voice" -> {
                                        matchAudioDialog?.setTime(info.content)
                                    }
                                }
                            }

                            "system" -> {
                                val typeToken = object : TypeToken<MessageInfo<String>>() {}
                                val info = GsonUtils.fromJson<MessageInfo<String>>(data, typeToken.type) ?: return
                                ToastUtil.showShort(this@MatchActivity, info.content)
                            }
                        }
                    }

                    0x2 -> {
                        ToastUtil.showShort(this@MatchActivity, msg.obj as String)
                    }
                }
            }
        }
    }

    private fun initTimer(type: String) {
        timer = object : CountDownTimer(30000L, 1000L) {
            override fun onFinish() {
                if (!isFinishing && !isDestroyed) {
                    if (type == "video" || type == "voice") {
                        matchTimeout()
                    } else {
                        startGamePage()
                    }
                }
            }

            override fun onTick(millisUntilFinished: Long) {
            }
        }

        timer?.start()
    }

    private fun initNotice(type: String) {
        when (type) {
            "video" -> {
                val list = arrayListOf<String>()
                list.add(getString(R.string.match_wait_tip_1))
                list.add(getString(R.string.match_wait_tip_2))
                list.add(getString(R.string.match_wait_tip_3))
                list.add(getString(R.string.match_wait_tip_4))
                list.add(getString(R.string.match_wait_tip_5))
                list.add(getString(R.string.match_wait_tip_6))
                list.add(getString(R.string.match_wait_tip_7))
                noticeLoop(list, 0)
            }

            "voice" -> {
                val list = arrayListOf<String>()
                list.add(getString(R.string.match_wait_tip_1))
                list.add(getString(R.string.match_wait_tip_2))
                list.add(getString(R.string.match_wait_tip_3))
                list.add(getString(R.string.match_wait_tip_5))
                list.add(getString(R.string.match_wait_tip_6))
                list.add(getString(R.string.match_wait_tip_7))
                noticeLoop(list, 0)
            }
        }
    }

    /**
     * 匹配上报，一个用户上报一次
     */
    private fun initReport(userInfo: UserInfo) {
        val matchReport = getLocalStorage().decodeBool("match_report", false)
        if (!matchReport) {
            getLocalStorage().encode("match_report", true)
            ReportManager.firebaseItemLog(firebaseAnalytics, userInfo.uid.toString(), userInfo.nickname, "match")
            ReportManager.facebookItemLog(this, userInfo.uid.toString(), userInfo.nickname, "open", "match")
            ReportManager.branchCustomLog(this, "match", null)
        }
    }

    private fun acceptInvite(type: String) {
        val msg = MsgInfo("match_accept_invite")
        val json = GsonUtils.toJson(msg)
        mWebSocketService?.sendMsg(json)

        when (type) {
            "video" -> matchVideoDialog?.waitConnect()
            "voice" -> matchAudioDialog?.waitConnect()
        }
    }

    private fun rejectInvite() {
        if (!isFinishing && !isDestroyed) {
            val msg = MsgInfo("match_reject_invite")
            val json = GsonUtils.toJson(msg)
            mWebSocketService?.sendMsg(json)
        }
    }

    private fun noticeLoop(list: List<String>, position: Int) {
        mHandler.postDelayed({
            binding.tvBroadcast.setText(list[position])
            if (position == list.size - 1) {
                noticeLoop(list, 0)
            } else {
                noticeLoop(list, position + 1)
            }
        }, 2000L)
    }

    private fun waiting(time: Long, func: () -> Unit) {
        if (!isFinishing && !isDestroyed) {
            mHandler.postDelayed({
                func()
            }, time)
        }
    }

    private fun rematch(time: Long) {
        if (!isFinishing && !isDestroyed) {
            mHandler.postDelayed({
                val msg = MessageInfo("rematch", 0, 0, "")
                val json = GsonUtils.toJson(msg)
                mWebSocketService?.sendMsg(json)
            }, time)
        }
    }

    private fun startVideoPage(room: Room) {
        val intent = Intent(this, VideoActivity::class.java)
        intent.putExtra("room", room)
        intent.putExtra("user", mMatcher)
        intent.putExtra("type", "match")
        startActivity(intent)
        finish()
    }

    private fun startAudioPage(room: Room) {
        val intent = Intent(this, AudioActivity::class.java)
        intent.putExtra("room", room)
        intent.putExtra("user", mMatcher)
        startActivity(intent)
        finish()
    }

    private fun startGamePage() {
        if (!DoubleUtils.isFastDoubleClick()) {
            val intent = Intent(this, GameListActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun matchTimeout() {
        if (matchFailedDialog == null) {
            matchFailedDialog = MatchFailedDialog(this) {
                val msg = Message()
                msg.what = 0x10000
                Config.mainHandler?.sendMessage(msg)
                finish()
            }
        }

        if (matchFailedDialog?.isShowing == false) {
            matchFailedDialog?.show()
        }
    }

    private fun startWebSocketService(id: Int, type: String, config: MatchConfig) {
        bindIntent = Intent(this, SosWebSocketClientService::class.java)
        bindIntent!!.putExtra("id", id)
        bindIntent!!.putExtra("type", type)
        bindIntent!!.putExtra("match_config", config)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(bindIntent)
        } else {
            startService(bindIntent)
        }

        bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE)
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
            mWebSocketService = (binder as SosWebSocketClientService.JWebSocketClientBinder).getService()
            JLog.i("webSocket绑定成功")
        }

        override fun onServiceDisconnected(componentName: ComponentName?) {
            JLog.i("webSocket绑定断开")
        }

        override fun onBindingDied(name: ComponentName?) {
            JLog.i("webSocket绑定销毁")
            super.onBindingDied(name)
        }

        override fun onNullBinding(name: ComponentName?) {
            JLog.i("webSocket绑定为空")
            super.onNullBinding(name)
        }
    }

    private fun showFilterDialog(type: ChooseType) {
        if (!DoubleUtils.isFastDoubleClick()) {
            getUserInfo { userInfo ->
                getBasePrice {
                    when (type) {
                        ChooseType.Gender -> FilterDialog(this, userInfo, it, getMatchConfig(), ChooseType.Gender) {
                            reloadMatch(userInfo)
                        }

                        ChooseType.Region -> FilterDialog(this, userInfo, it, getMatchConfig(), ChooseType.Region) {
                            reloadMatch(userInfo)
                        }
                    }
                }
            }
        }
    }

    private fun reloadMatch(userInfo: UserInfo) {
        //重新计时
        timer?.start()

        flushConfig()
        closeService()

        val tp = intent.getStringExtra("type")
        if (tp != null) {
            startWebSocketService(userInfo.uid, tp, getMatchConfig())
        }

        if (Config.mainHandler != null) {
            Config.mainHandler!!.sendEmptyMessage(0x10006)
        }
    }

    private fun showGameDialog() {
        mWaitingDialog?.show()
        if (!DoubleUtils.isFastDoubleClick()) {
            getUserInfo { userInfo ->
                DataManager.getLotteryCoin { list ->
                    mWaitingDialog?.cancel()
                    GameDialog(this, userInfo, null, userInfo.avatar, true).apply {
                        setLotteryGiftList(list, "coin")
                        show()
                    }
                }
            }
        }
    }

    private fun checkPreview() {
        if (!DoubleUtils.isFastDoubleClick()) {
            getUserInfo { userInfo ->
                binding.surfaceLocal.visibility = View.VISIBLE
                BeautyDialog(this, mRtcEngine, userInfo) {
                    mRtcEngine?.stopPreview()
                }
            }
        }
    }

    private fun initLocalSurface() {
        try {
            val config = RtcEngineConfig()
            config.mContext = this
            config.mEventHandler = mRtcEventHandler
            config.mAppId = Config.AGORA_APP_ID
            mRtcEngine = RtcEngine.create(config)

            //启用视频流
            mRtcEngine?.enableVideo()

        } catch (ex: Exception) {
            ToastUtil.showShort(this, ex.message)
        }
    }

    override fun onStop() {
        super.onStop()
        val type = intent.getStringExtra("type")
        if (type == "video" || type == "voice") {
            timer?.cancel()
            closeService()
        }

        exoPlayer?.stop()
        exoPlayer?.release()

        Config.mainHandler?.sendEmptyMessage(0x10006)
        Config.subscriberHandler?.sendEmptyMessage(0x10001)
    }

    private fun closeService() {
        if (bindIntent != null) {
            mWebSocketService?.closeConnect()
            unbindService(serviceConnection)
            stopService(bindIntent)
        }
    }

    override fun onBackPressed() {
        matchVideoDialog?.cancel()
        matchAudioDialog?.cancel()
        finish()
    }
}