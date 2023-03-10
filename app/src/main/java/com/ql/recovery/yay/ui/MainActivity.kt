package com.ql.recovery.yay.ui

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.netease.yunxin.kit.conversationkit.repo.ConversationRepo
import com.ql.recovery.bean.*
import com.ql.recovery.config.Config
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.databinding.ActivityMainBinding
import com.ql.recovery.yay.databinding.LayoutHomeBottomBinding
import com.ql.recovery.yay.service.SosWebSocketClientService
import com.ql.recovery.yay.ui.base.BaseFragment
import com.ql.recovery.yay.ui.base.BaseFragmentActivity
import com.ql.recovery.yay.ui.club.ClubFragment
import com.ql.recovery.yay.ui.dialog.DailyClubDialog
import com.ql.recovery.yay.ui.dialog.DailyCoinDialog
import com.ql.recovery.yay.ui.dialog.PrimeDialog
import com.ql.recovery.yay.ui.home.HomeFragment
import com.ql.recovery.yay.ui.mine.MineFragment
import com.ql.recovery.yay.ui.notifications.NotificationsFragment
import com.ql.recovery.yay.ui.record.RecordFragment
import com.ql.recovery.yay.util.AppUtil
import com.ql.recovery.yay.util.GsonUtils
import com.ql.recovery.yay.util.JLog

class MainActivity : BaseFragmentActivity() {
    companion object {
        private const val FRAGMENT_HOME = 0
        private const val DEFAULT_INDEX = FRAGMENT_HOME

        val BOTTOM_ICON_CHECKED = arrayOf(
            R.drawable.in_sy,
            R.drawable.in_ppjl,
            R.drawable.in_club,
            R.drawable.in_notification,
            R.drawable.in_my
        )

        val BOTTOM_ICON_UNCHECKED = arrayOf(
            R.drawable.out_sy,
            R.drawable.out_ppjl,
            R.drawable.out_club,
            R.drawable.out_ltxx,
            R.drawable.out_my
        )

        var BOTTOM_TEXT_ARRAY = arrayOf("", "", "", "", "")

        val FRAGMENT_CLASS_ARRAY: Array<Class<out BaseFragment>> = arrayOf(
            HomeFragment::class.java,
            RecordFragment::class.java,
            ClubFragment::class.java,
            NotificationsFragment::class.java,
            MineFragment::class.java
        )
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomBinding: LayoutHomeBottomBinding
    private var mCheckedFragmentID: Int = DEFAULT_INDEX

    private var mWebSocketService: SosWebSocketClientService? = null
    private var bindIntent: Intent? = null
    private var handler = Handler(Looper.getMainLooper())

    override fun getViewBinding(baseBinding: ActivityBaseBinding) {
        binding = ActivityMainBinding.inflate(layoutInflater, baseBinding.flBase, true)
    }

    override fun initView() {
        checkDailyRecommend()
        checkMemberDailyReward()

        getUserInfo { userInfo ->
            //??????????????????????????????????????????
            if (userInfo.role == "anchor") {
                if (mWebSocketService == null) {
                    //??????????????????
                    startWebSocketService(userInfo.uid, "video", getMatchConfig())
                }
            }
        }
    }

    /**
     * ??????????????????
     */
    private fun checkDailyRecommend() {
        getUserInfo { userInfo ->
            if (userInfo.role == "normal") {
                val today = AppUtil.getTodayDay()
                val date = getLocalStorage().decodeString("daily_recommend_date", "")
                if (today != date) {
                    handler.postDelayed({
                        getLocalStorage().encode("daily_recommend_date", today)
                        DailyClubDialog(this).show()
                    }, 1000L)
                }
            }
        }
    }

    /**
     * ????????????????????????
     */
    private fun checkMemberDailyReward() {
        getUserInfo { userInfo ->
            if (userInfo.is_vip) {
                DataManager.checkDailyReward {
                    if (!it) {
                        getBasePrice { basePrice ->
                            DailyCoinDialog(this, basePrice.app.membership_daily_reward).show()
                        }
                    }
                }
            }
        }
    }

    override fun putFragments(): Array<Class<out BaseFragment>> {
        return FRAGMENT_CLASS_ARRAY
    }

    override fun getBottomItemView(index: Int): View {
        BOTTOM_TEXT_ARRAY[0] = getString(R.string.title_home)
        BOTTOM_TEXT_ARRAY[1] = getString(R.string.title_dashboard)
        BOTTOM_TEXT_ARRAY[2] = getString(R.string.title_club)
        BOTTOM_TEXT_ARRAY[3] = getString(R.string.title_notifications)
        BOTTOM_TEXT_ARRAY[4] = getString(R.string.title_mine)

        bottomBinding = LayoutHomeBottomBinding.inflate(layoutInflater)
        bottomBinding.homePageBottomLayout.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f)
        bottomBinding.homePageBottomImage.setImageResource(BOTTOM_ICON_UNCHECKED[index])
        return bottomBinding.root
    }

    override fun getFLid(): Int {
        return binding.flHomeBody.id
    }

    override fun getBottomLayout(): LinearLayout {
        return binding.llHomeBottom
    }

    override fun checkAllBottomItem(item: View?, position: Int, isChecked: Boolean) {
        if (item == null) return
        val bindingView = LayoutHomeBottomBinding.bind(item)
        bindingView.homePageBottomImage.setImageResource(if (isChecked) BOTTOM_ICON_CHECKED[position] else BOTTOM_ICON_UNCHECKED[position])
    }

    override fun initData() {
        initHandler()
    }

    override fun onItemClick(item: View?, index: Int) {
        mCheckedFragmentID = index
    }

    fun changeFragment(index: Int) {
        setTabSel(bottomBinding.root.getChildAt(index), index)
    }

    @SuppressLint("SetTextI18n")
    fun changeCount(count: Int) {
        val c = bottomLayout.childCount
        if (c < 5) return
        val bottomView = bottomLayout.getChildAt(3).findViewById<ImageView>(R.id.home_count)
        if (count == 0) {
            bottomView.visibility = View.GONE
        } else {
            bottomView.visibility = View.VISIBLE
        }

        flushUserInfo()
    }

    private fun initHandler() {
        Config.mainHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    0x10000 -> {
                        changeFragment(2)
                    }

                    0x10001 -> {
                        //????????????????????????
                        changeCount(ConversationRepo.getMsgUnreadCount())
                    }

                    0x10002 -> {
                        getUserInfo { userInfo ->
                            PrimeDialog(this@MainActivity, userInfo.is_vip) {}
                        }
                    }

                    0x10003 -> {
                        changeFragment(1)
                    }

                    0x10006 -> {
                        //??????????????????
                        flushUserInfo()
                    }

                    0x10007 -> {
                        val message = MsgInfo("match_accept_invite")
                        val json = GsonUtils.toJson(message)
                        mWebSocketService?.sendMsg(json)
                    }

                    0x10008 -> {
                        val message = MsgInfo("match_reject_invite")
                        val json = GsonUtils.toJson(message)
                        mWebSocketService?.sendMsg(json)
                    }

                    0x10009 -> {
                        val message = MessageInfo("rematch", 0, 0, "")
                        val json = GsonUtils.toJson(message)
                        mWebSocketService?.sendMsg(json)
                    }

                    0x10010 -> {
                        //???????????????
                        getUserInfo { userInfo ->
                            //??????????????????????????????????????????
                            if (userInfo.role == "anchor") {
                                if (mWebSocketService == null) {
                                    //??????????????????
                                    startWebSocketService(userInfo.uid, "video", getMatchConfig())
                                }
                            }
                        }
                    }

                    0x10011 -> {
                        getUserInfo { userInfo ->
                            if (userInfo.role == "anchor") {
                                if (mWebSocketService != null) {
                                    //???????????????
                                    closeService()
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()

        //??????????????????
        Config.mainHandler?.sendEmptyMessage(0x10006)
        Config.subscriberHandler?.sendEmptyMessage(0x10001)
    }

    override fun onDestroy() {
        super.onDestroy()
        closeService()
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
            JLog.i("webSocket????????????")
        }

        override fun onServiceDisconnected(componentName: ComponentName?) {
            JLog.i("webSocket????????????")
        }

        override fun onBindingDied(name: ComponentName?) {
            JLog.i("webSocket????????????")
            super.onBindingDied(name)
        }

        override fun onNullBinding(name: ComponentName?) {
            JLog.i("webSocket????????????")
            super.onNullBinding(name)
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
}