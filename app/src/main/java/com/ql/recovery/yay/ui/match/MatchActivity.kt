package com.ql.recovery.yay.ui.match

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.*
import android.view.View
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.AssetDataSource
import com.google.android.exoplayer2.upstream.DataSource
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
import com.ql.recovery.yay.util.*
import java.util.*

class MatchActivity : BaseActivity() {
    private lateinit var binding: ActivityMatchBinding
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var timer: CountDownTimer? = null
    private var mWebSocketService: SosWebSocketClientService? = null
    private var bindIntent: Intent? = null
    private var matchDialog: MatchDialog? = null
    private var matchFailedDialog: MatchFailedDialog? = null
    private var matcher: User? = null
    private var handler = Handler(Looper.getMainLooper())
    private var exoPlayer: ExoPlayer? = null
    private var waitingDialog: WaitingDialog? = null
    private var rejectCallTimes = 0

    private var videoUri = "file:///android_asset/videos/match_video.mp4"
    private var audioUri = "file:///android_asset/videos/match_audio.mp4"

    override fun getViewBinding(baseBinding: ActivityBaseBinding) {
        binding = ActivityMatchBinding.inflate(layoutInflater, baseBinding.flBase, true)
    }

    override fun initView() {
        binding.ivClose.setOnClickListener { finish() }
        binding.llChooseGender.setOnClickListener { showFilterDialog(ChooseType.Gender) }
        binding.llChooseRegion.setOnClickListener { showFilterDialog(ChooseType.Region) }
        binding.ivGame.setOnClickListener { showGameDialog() }
        binding.flGameStart.setOnClickListener { startGamePage() }
        binding.llGameChooseGender.setOnClickListener { showFilterDialog(ChooseType.Gender) }
    }

    override fun initData() {
        exoPlayer = ExoPlayer.Builder(this).build()
        firebaseAnalytics = Firebase.analytics
        waitingDialog = WaitingDialog(this)

        checkMatchTimes()
        flushConfig()
        loadOnlineCount()
        initMatch()
    }

    /**
     * 每日重置匹配规则
     */
    private fun checkMatchTimes() {
        val today = AppUtil.getTodayDate()
        val date = getLocalStorage().decodeString("match_date", AppUtil.timeStamp2Date(System.currentTimeMillis(), "yyyy-MM-dd"))
        if (today != date) {
            getLocalStorage().encode("match_times", 1)
        }
    }

    /**
     * 初始化匹配
     */
    private fun initMatch() {
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
                    //只有普通用户在匹配页开启匹配，主播进app就开始视频匹配
                    if (userInfo.role == "normal") {
                        initTimer(type, 30000L)
                        initHandler(type)
                        initMatchDialog(type)
                        startMatch()
                    }

                    //主播语音匹配是进入才开启
                    if (type == "voice" && userInfo.role == "anchor") {
                        //关闭全局匹配池连接
                        Config.mainHandler?.sendEmptyMessage(0x10010)
                        initHandler(type)
                        initMatchDialog(type)
                        startMatch()
                    }
                }
            }
        }
    }

    private fun initPlayer(type: String) {
        binding.playerView.setShutterBackgroundColor(Color.TRANSPARENT)
        binding.playerView.player = exoPlayer
        exoPlayer?.repeatMode = Player.REPEAT_MODE_ALL
        exoPlayer?.playWhenReady = true

        val dataSourceFactory = DataSource.Factory { AssetDataSource(this@MatchActivity) }
        when (type) {
            "video" -> {
                val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(videoUri))
                exoPlayer?.setMediaSource(mediaSource)
                exoPlayer?.prepare()
            }

            "voice" -> {
                val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(audioUri))
                exoPlayer?.setMediaSource(mediaSource)
                exoPlayer?.prepare()
            }
        }
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

    /**
     * 在线人数
     */
    private fun loadOnlineCount() {
        DataManager.getOnlineCount { count ->
            binding.tvOnlineCount.text = count.toString()
        }
    }

    /**
     * 开始匹配
     */
    private fun startMatch() {
        val type = intent.getStringExtra("type")
        if (type != null && type != "game") {
            getUserInfo { userInfo ->
                val times = getLocalStorage().decodeInt("match_times", 0)
                if (times == 4) {
                    DataManager.getProductList("lemon") { list ->
                        for (child in list) {
                            if (child.code == "coin_level_one") {
                                MatchGuideDialog(this, child) {
                                    //一秒后开启匹配
                                    waiting(1000L) {
                                        startWebSocketService(userInfo.uid, type, getMatchConfig())
                                    }
                                }
                                break
                            }
                        }
                    }
                    return@getUserInfo
                }

                //一秒后开启匹配
                waiting(1000L) {
                    startWebSocketService(userInfo.uid, type, getMatchConfig())
                }
            }
        }
    }

    private fun initMatchDialog(type: String) {
        matchDialog = MatchDialog(this) { status, _ ->
            when (status) {
                MatchStatus.Accept -> {
                    acceptInvite()
                    ReportManager.firebaseCustomLog(firebaseAnalytics, "accept_video_match_click", "accept")
                    ReportManager.appsFlyerCustomLog(this, "accept_video_match_click", "accept")
                }

                MatchStatus.Reject -> {
                    rejectInvite()

                    //当普通用户在匹配池配到第一个付费主播后，如果TA有没有接听，则回到匹配池后就检测宝石余额是否充足（大于等于120），
                    // 若不足则就弹窗引导购买4.99套餐包（购买过的则随机弹其他套餐包：1.99、6.99、9.99、10.99），如果充足则规则往后走
                    val times = getLocalStorage().decodeInt("match_times", 0)
                    if (times == 4) {
                        timer?.cancel()
                        getUserInfo { userInfo ->
                            if (userInfo.coin < 120) {
                                DataManager.getProductList("lemon") { list ->
                                    var from: Server? = null
                                    for (child in list) {
                                        if (child.code == "coin_level_two") {
                                            from = child
                                        }
                                    }

                                    if (from != null) {
                                        MatchGuideDialog(this, from) {
                                            initTimer(type, 30000L)
                                            timer?.start()
                                            rematch(1500L)
                                        }
                                    } else {
                                        val randomIndex = Random().nextInt(list.size)
                                        MatchGuideDialog(this, list[randomIndex]) {
                                            timer?.start()
                                            rematch(1500L)
                                        }
                                    }
                                }
                            } else {
                                rematch(1500L)
                            }
                        }
                    }

                    //记录连续拒接的次数
                    if (times == 5) {
                        rejectCallTimes++
                    }

                    ReportManager.firebaseCustomLog(firebaseAnalytics, "reject_video_match_click", "reject")
                    ReportManager.appsFlyerCustomLog(this, "reject_video_match_click", "reject")
                }

                MatchStatus.Cancel -> {}
            }
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
                                val typeToken = object : TypeToken<MessageInfo<Invite>>() {}
                                val info = GsonUtils.fromJson<MessageInfo<Invite>>(data, typeToken.type) ?: return

                                //取消所有对话框和计时器
                                timer?.cancel()
                                matchFailedDialog?.cancel()

                                if (!isFinishing && !isDestroyed) {
                                    matcher = info.content.user
                                    matchDialog?.setUser(info.content.user)
                                    matchDialog?.setMatchType(info.content.room_type, info.content.transaction_type)
                                    matchDialog?.startConnect(type)
                                    matchDialog?.show()

                                    //全局免提
                                    val autoAccept = getLocalStorage().decodeBool("auto_accept", false)
                                    if (autoAccept) {
                                        matchDialog?.waitConnect()
                                        acceptInvite()
                                    }

                                    //局部免提
                                    val config = getMatchConfig()
                                    if (config.hand_free) {
                                        matchDialog?.waitConnect()
                                        acceptInvite()
                                    }
                                }
                            }

                            "match_accept_invite" -> {}

                            "match_reject_invite" -> {
                                //重新计算匹配中的时间
                                timer?.cancel()
                                initTimer(type, 30000L)
                                timer?.start()

                                matchDialog?.handOff()
                                waiting(1500L) {
                                    matchDialog?.cancel()
                                }

                                //5秒后进入匹配池
                                rematch(5000L)
                            }

                            "match_start_play" -> {
                                val typeToken = object : TypeToken<MessageInfo<Room>>() {}
                                val info = GsonUtils.fromJson<MessageInfo<Room>>(data, typeToken.type) ?: return

                                getLocalStorage().encode("recent_record", "match")

                                //充值未接次数
                                rejectCallTimes = 0

                                //第一次直接进入聊天
                                val firstMatch = getLocalStorage().decodeBool("first_match", false)
                                if (!firstMatch) {
                                    getLocalStorage().encode("first_match", true)
                                }

                                //记录每天进入聊天的次数
                                val times = getLocalStorage().decodeInt("match_times", 0)
                                getLocalStorage().encode("match_times", times + 1)

                                //关闭对话框，进入到聊天
                                matchDialog?.cancel()
                                val roomType = matchDialog?.getType()
                                if (roomType != null) {
                                    when (type) {
                                        "video" -> startVideoPage(info.content, roomType)
                                        "voice" -> startVoicePage(info.content, roomType)
                                    }
                                }
                            }

                            "match_invite_timeout" -> {
                                //重新计算匹配中的时间
                                timer?.cancel()
                                initTimer(type, 30000L)
                                timer?.start()

                                //关闭对话框
                                matchDialog?.connectingTimeout()
                                matchDialog?.cancel()

                                //记录连续拒接的次数
                                val times = getLocalStorage().decodeInt("match_times", 0)
                                if (times == 5) {
                                    rejectCallTimes++
                                }

                                when (type) {
                                    "video" -> {
                                        ReportManager.firebaseCustomLog(firebaseAnalytics, "video_match_timeout", "timeout")
                                        ReportManager.appsFlyerCustomLog(this@MatchActivity, "video_match_timeout", "timeout")
                                    }

                                    "voice" -> {
                                        ReportManager.firebaseCustomLog(firebaseAnalytics, "voice_match_timeout", "timeout")
                                        ReportManager.appsFlyerCustomLog(this@MatchActivity, "voice_match_timeout", "timeout")
                                    }
                                }

                                rematch(1000L)
                            }

                            "match_peer_disconnect" -> {
                                timer?.cancel()
                                matchDialog?.handOff()
                                matchDialog?.cancel()
                                rematch(1500L)
                            }

                            "match_countdown" -> {
                                val typeToken = object : TypeToken<MessageInfo<Int>>() {}
                                val info = GsonUtils.fromJson<MessageInfo<Int>>(data, typeToken.type) ?: return
                                matchDialog?.setTime(info.content)
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

    private fun initTimer(type: String, time: Long) {
        timer = object : CountDownTimer(time, 1000L) {
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
                if (millisUntilFinished < 15000L) {
                    timer?.cancel()
                    getUserInfo { userInfo ->
                        if (!userInfo.is_vip) {
                            DataManager.getProductList("sub") { list ->
                                if (list.isEmpty()) return@getProductList
                                MatchGuideDialog(this@MatchActivity, list[0]) {
                                    //取消对话框后继续计时
                                    initTimer(type, 15000L)
                                    timer?.start()
                                }
                            }
                        }
                    }
                }
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
            }

            "voice" -> {
                val list = arrayListOf<String>()
                list.add(getString(R.string.match_wait_tip_1))
                list.add(getString(R.string.match_wait_tip_2))
                list.add(getString(R.string.match_wait_tip_3))
                list.add(getString(R.string.match_wait_tip_5))
                list.add(getString(R.string.match_wait_tip_6))
                list.add(getString(R.string.match_wait_tip_7))
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
            ReportManager.firebaseCustomLog(firebaseAnalytics, "match", userInfo.nickname)
            ReportManager.facebookCustomLog(this, "match", userInfo.nickname)
            ReportManager.branchCustomLog(this, "match", null)
            ReportManager.appsFlyerCustomLog(this, "match", userInfo.nickname)
        }
    }

    private fun acceptInvite() {
        val msg = MsgInfo("match_accept_invite")
        val json = GsonUtils.toJson(msg)
        mWebSocketService?.sendMsg(json)
        matchDialog?.waitConnect()
    }

    private fun rejectInvite() {
        if (!isFinishing && !isDestroyed) {
            val msg = MsgInfo("match_reject_invite")
            val json = GsonUtils.toJson(msg)
            mWebSocketService?.sendMsg(json)
        }
    }

    private fun waiting(time: Long, func: () -> Unit) {
        if (!isFinishing && !isDestroyed) {
            handler.postDelayed({
                func()
            }, time)
        }
    }

    private fun rematch(time: Long) {
        if (!isFinishing && !isDestroyed) {
            getUserInfo { userInfo ->
                if (!userInfo.is_vip && rejectCallTimes == 5) {
                    timer?.cancel()
                    DataManager.getProductList("sub") { list ->
                        if (list.isEmpty()) return@getProductList
                        MatchGuideDialog(this, list[0]) {
                            rejectCallTimes = 0
                            timer?.start()
                            //重新匹配
                            handler.postDelayed({
                                val msg = MessageInfo("rematch", 0, 0, "")
                                val json = GsonUtils.toJson(msg)
                                mWebSocketService?.sendMsg(json)
                            }, time)
                        }
                    }
                } else {
                    handler.postDelayed({
                        val msg = MessageInfo("rematch", 0, 0, "")
                        val json = GsonUtils.toJson(msg)
                        mWebSocketService?.sendMsg(json)
                    }, time)
                }
            }
        }
    }

    private fun startVideoPage(room: Room, type: String) {
        val intent = Intent(this, VideoActivity::class.java)
        intent.putExtra("room", room)
        intent.putExtra("user", matcher)
        intent.putExtra("type", type)
        startActivity(intent)
        finish()
    }

    private fun startVoicePage(room: Room, type: String) {
        val intent = Intent(this, AudioActivity::class.java)
        intent.putExtra("room", room)
        intent.putExtra("user", matcher)
        intent.putExtra("type", type)
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
                            ReportManager.firebaseCustomLog(firebaseAnalytics, "gender_filter", "gender filter")
                        }

                        ChooseType.Region -> FilterDialog(this, userInfo, it, getMatchConfig(), ChooseType.Region) {
                            reloadMatch(userInfo)
                            ReportManager.firebaseCustomLog(firebaseAnalytics, "region_filter", "region filter")
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

        val type = intent.getStringExtra("type")
        if (type != null) {
            closeService()
            startWebSocketService(userInfo.uid, type, getMatchConfig())
        }

        if (Config.mainHandler != null) {
            Config.mainHandler!!.sendEmptyMessage(0x10006)
        }
    }

    private fun showGameDialog() {
        waitingDialog?.show()
        if (!DoubleUtils.isFastDoubleClick()) {
            getUserInfo { userInfo ->
                DataManager.getLotteryCoin { list ->
                    waitingDialog?.cancel()
                    GameDialog(this, userInfo, null, userInfo.avatar, true).apply {
                        setLotteryGiftList(list, "coin")
                        show()
                    }
                }
            }
        }
    }


    override fun onStop() {
        super.onStop()

        val type = intent.getStringExtra("type")
        if (type != null && type != "game") {
            //刷新金币数量
            Config.mainHandler?.sendEmptyMessage(0x10006)
            Config.subscriberHandler?.sendEmptyMessage(0x10001)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        exoPlayer?.stop()
        exoPlayer?.release()

        val type = intent.getStringExtra("type")
        if (type == "video" || type == "voice") {
            timer?.cancel()
            closeService()
        }
    }

    private fun closeService() {
        try {
            if (bindIntent != null) {
                mWebSocketService?.closeConnect()
                unbindService(serviceConnection)
                stopService(bindIntent)
                mWebSocketService = null
            }
        } catch (ex: Exception) {

        }
    }

    override fun onBackPressed() {
        matchDialog?.cancel()
        finish()
    }
}