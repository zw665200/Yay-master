package com.ql.recovery.yay.ui.base

import android.Manifest
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.view.View
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant
import com.netease.yunxin.kit.corekit.route.XKitRouter
import com.ql.recovery.bean.BasePrice
import com.ql.recovery.bean.MatchConfig
import com.ql.recovery.bean.Room
import com.ql.recovery.bean.UserInfo
import com.ql.recovery.config.Config
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.manager.ReportManager
import com.ql.recovery.yay.ui.dialog.MatchVideoDialog
import com.ql.recovery.yay.ui.dialog.NoticeDialog
import com.ql.recovery.yay.ui.guide.GuideActivity
import com.ql.recovery.yay.ui.match.VideoActivity
import com.ql.recovery.yay.util.*
import com.tencent.mmkv.MMKV

abstract class BaseActivity : FragmentActivity() {
    private lateinit var baseBinding: ActivityBaseBinding
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var mk = MMKV.defaultMMKV()
    private var dialog: MatchVideoDialog? = null
    private var startAt = 0L
    private var leaveAt = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        baseBinding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(baseBinding.root)
        getViewBinding(baseBinding)

        setStatusBarLight()
        initView()
        initData()

        firebaseAnalytics = Firebase.analytics

        val different = AppUtil.getNavigationBarHeightIfRoom(this)
        if (different != 0) {
            baseBinding.divider.visibility = View.VISIBLE
            baseBinding.divider.height = different
        } else {
            baseBinding.divider.visibility = View.GONE
            baseBinding.divider.height = 0
        }
    }

    protected fun setStatusBarLight() {
        SysWindowUi.hideStatusNavigationBar(this, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resources.getColor(R.color.transparent, null)
            val controller = ViewCompat.getWindowInsetsController(baseBinding.root)
            controller?.show(WindowInsetsCompat.Type.statusBars())
            controller?.isAppearanceLightStatusBars = true
        }
    }

    protected fun setStatusBarDark() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resources.getColor(R.color.transparent, null)
            val controller = ViewCompat.getWindowInsetsController(baseBinding.root)
            controller?.show(WindowInsetsCompat.Type.statusBars())
            controller?.isAppearanceLightStatusBars = false
        }
    }

    protected abstract fun getViewBinding(baseBinding: ActivityBaseBinding)
    protected abstract fun initView()
    protected abstract fun initData()

    protected fun getLocalStorage(): MMKV {
        return mk
    }

    fun getMatchConfig(): MatchConfig {
        val config = mk.decodeParcelable("match_config", MatchConfig::class.java)
        if (config == null) {
            val defaultConfig = MatchConfig(0, "", "", false)
            mk.encode("match_config", defaultConfig)
            return defaultConfig
        } else {
            return config
        }
    }

    protected fun getBasePrice(res: (BasePrice) -> Unit) {
        val basePrice = getLocalStorage().decodeParcelable("base_price", BasePrice::class.java)
        if (basePrice != null) {
            res(basePrice)
            return
        }

        DataManager.getBasePrice {
            getLocalStorage().encode("base_price", it)
            res(it)
        }
    }

    protected fun checkReadAndWritePermissions(isPermitted: () -> Unit) {
        val permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        requestPermission(permissions) { isPermitted() }
    }

    protected fun requestPermission(permissions: Array<out String>, method: () -> Unit) {
        LivePermissions(this)
            .request(permissions)
            .observe(this) {
                when (it) {
                    is PermissionResult.Grant -> {
                        method()
                    }

                    is PermissionResult.Rationale -> {
                        //权限拒绝
                        ToastUtil.showShort(this, "please open the related permissions")
                        method()
                    }

                    is PermissionResult.Deny -> {
                        ToastUtil.showShort(this, "please open the related permissions")
                        //权限拒绝，且勾选了不再询问
                        method()
                    }
                }
            }
    }


    private fun startVideoPay(room: Room) {
        val intent = Intent(this, VideoActivity::class.java)
        intent.putExtra("room", room)
        startActivity(intent)
    }

    protected fun getUserInfo(res: (UserInfo) -> Unit) {
        val userInfo = getLocalStorage().decodeParcelable("user_info", UserInfo::class.java)
        if (userInfo == null) {
            val token = getLocalStorage().decodeString("access_token")
            if (token != null) {
                DataManager.getUserInfo {
                    res(it)
                }
            }
        } else {
            res(userInfo)
        }
    }

    /**
     * 发起视频聊天，对方在线才能聊天
     * @param online 在线状态
     */
    protected fun requestVideoChat(uid: Int, online: Boolean) {
        getUserInfo { userInfo ->
            if (userInfo.uid == uid) return@getUserInfo

            ReportManager.firebaseCustomLog(firebaseAnalytics, "private_video_click", "private video click")
            ReportManager.appsFlyerCustomLog(this, "private_video_click", "private video click")

            if (userInfo.sex == 0 || userInfo.age == 0 || userInfo.avatar.isBlank() ||
                userInfo.nickname.isBlank() || userInfo.photos.isEmpty() || userInfo.tags.isEmpty()
            ) {
                //如果用户资料不完整，要填写完整才能匹配
                ToastUtil.showLong(this, getString(R.string.notice_incomplete_profile))
                startActivity(Intent(this, GuideActivity::class.java))
                return@getUserInfo
            }

            DataManager.getBasePrice { basePrice ->
                NoticeDialog(this, userInfo.coin, basePrice.common.private_video) {

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
        }
    }

    /**
     * 发起私信聊天
     */
    protected fun requestChat(uid: Int, nickname: String, avatar: String) {
        getUserInfo { userInfo ->
            val user = com.netease.yunxin.kit.corekit.im.model.UserInfo(uid.toString(), nickname, avatar)
            val map = HashMap<String, Any>()
            map["online"] = userInfo.online
            XKitRouter.withKey(RouterConstant.PATH_CHAT_P2P_PAGE).withParam(RouterConstant.CHAT_KRY, user).withContext(this).navigate()

            ReportManager.firebaseCustomLog(firebaseAnalytics, "private_msg_click", "private msg click")
            ReportManager.appsFlyerCustomLog(this, "private_msg_click", "private msg click")
        }
    }

    override fun getResources(): Resources {
        val res = super.getResources()
        val config = Configuration()
        config.setToDefaults()
        res.updateConfiguration(config, res.displayMetrics)
        return res
    }

}