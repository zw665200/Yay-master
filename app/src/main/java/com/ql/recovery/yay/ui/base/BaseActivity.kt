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
import com.ql.recovery.yay.ui.dialog.MatchDialog
import com.ql.recovery.yay.ui.dialog.NoticeDialog
import com.ql.recovery.yay.ui.guide.GuideActivity
import com.ql.recovery.yay.ui.match.VideoActivity
import com.ql.recovery.yay.util.*
import com.tencent.mmkv.MMKV

abstract class BaseActivity : FragmentActivity() {
    private lateinit var baseBinding: ActivityBaseBinding
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var mk = MMKV.defaultMMKV()
    private var dialog: MatchDialog? = null
    private var startAt = 0L
    private var leaveAt = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        baseBinding = ActivityBaseBinding.inflate(layoutInflater)

        getViewBinding(baseBinding)
        setContentView(baseBinding.root)

        firebaseAnalytics = Firebase.analytics
        setStatusBarLight()
        initView()
        initData()
        checkFitsSystemWindow()
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

    private fun checkFitsSystemWindow() {
        val different = AppUtil.getNavigationBarHeightIfRoom(this)
        if (different != 0) {
            baseBinding.divider.visibility = View.VISIBLE
            baseBinding.divider.height = different
        } else {
            baseBinding.divider.visibility = View.GONE
            baseBinding.divider.height = 0
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

    protected fun checkLocationPermissions(success: (String) -> Unit) {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        requestPermission(permissions) { success(it) }
    }

    protected fun checkVideoPermissions(success: () -> Unit) {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        requestPermission(permissions) {
            when (it) {
                "grant" -> {
                    success()

                    val permission = getLocalStorage().decodeBool("home_video_permission", false)
                    if (!permission) {
                        ReportManager.firebaseCustomLog(firebaseAnalytics, "home_video_permission_success", "get permission success")
                        ReportManager.appsFlyerCustomLog(this, "home_video_permission_success", "get permission success")
                    }
                }

                "deny" -> {
                    success()
                    val permission = getLocalStorage().decodeBool("home_video_permission", false)
                    if (!permission) {
                        ReportManager.firebaseCustomLog(firebaseAnalytics, "home_video_permission_failed", "get permission failed")
                        ReportManager.appsFlyerCustomLog(this, "home_video_permission_failed", "get permission failed")
                    }
                }

                else -> {
                    success()
                    val permission = getLocalStorage().decodeBool("home_video_permission", false)
                    if (!permission) {
                        ReportManager.firebaseCustomLog(firebaseAnalytics, "home_video_permission_failed", "get permission failed")
                        ReportManager.appsFlyerCustomLog(this, "home_video_permission_failed", "get permission failed")
                    }
                }
            }
        }
    }

    private fun requestPermission(permissions: Array<out String>, method: (String) -> Unit) {
        LivePermissions(this)
            .request(permissions)
            .observe(this) {
                when (it) {
                    is PermissionResult.Grant -> {
                        method("grant")
                    }

                    is PermissionResult.Rationale -> {
                        //????????????
                        ToastUtil.showShort(this, "please open the related permissions")
                        method("rationale")
                    }

                    is PermissionResult.Deny -> {
                        ToastUtil.showShort(this, "please open the related permissions")
                        //???????????????????????????????????????
                        method("deny")
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
     * ?????????????????????????????????????????????
     * @param online ????????????
     */
    protected fun requestVideoChat(uid: Int, online: Boolean) {
        getUserInfo { userInfo ->
            if (userInfo.uid == uid) return@getUserInfo

            ReportManager.firebaseCustomLog(firebaseAnalytics, "private_video_click", "private video click")
            ReportManager.appsFlyerCustomLog(this, "private_video_click", "private video click")

            if (userInfo.sex == 0 || userInfo.age == 0 || userInfo.avatar.isBlank() || userInfo.nickname.isBlank()) {
                //?????????????????????????????????????????????????????????
                ToastUtil.showLong(this, getString(R.string.notice_incomplete_profile))
                startActivity(Intent(this, GuideActivity::class.java))
                return@getUserInfo
            }

            DataManager.getBasePrice { basePrice ->
                NoticeDialog(this, userInfo.coin, basePrice.common.private_video) {

                    if (!DoubleUtils.isFastDoubleClick()) {
                        //??????????????????
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
     * ??????????????????
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