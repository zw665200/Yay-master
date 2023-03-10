package com.ql.recovery.yay.ui.base

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.Address
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant
import com.netease.yunxin.kit.corekit.route.XKitRouter
import com.ql.recovery.bean.BasePrice
import com.ql.recovery.bean.MatchConfig
import com.ql.recovery.bean.Subscriber
import com.ql.recovery.bean.UserInfo
import com.ql.recovery.config.Config
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.callback.LocationCallback
import com.ql.recovery.yay.manager.ReportManager
import com.ql.recovery.yay.ui.dialog.NoticeDialog
import com.ql.recovery.yay.ui.dialog.ProfileDialog
import com.ql.recovery.yay.ui.dialog.WaitingDialog
import com.ql.recovery.yay.ui.guide.GuideActivity
import com.ql.recovery.yay.ui.mine.CountryActivity
import com.ql.recovery.yay.util.*
import com.tencent.mmkv.MMKV

abstract class BaseFragment : Fragment(), View.OnClickListener {
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var mContext: Context? = null
    private var waitingDialog: WaitingDialog? = null
    private val mk = MMKV.defaultMMKV()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = initView(inflater, container, savedInstanceState)
        initData()
        initHandler()
        firebaseAnalytics = Firebase.analytics

        waitingDialog = WaitingDialog(requireActivity())

        return v
    }

    override fun onClick(v: View) {
        click(v)
    }

    fun onActivityResume() {}
    protected abstract fun initView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    protected abstract fun initData()
    protected abstract fun setOnlineStatus(uid: String, online: Boolean)
    protected abstract fun click(v: View)
    protected abstract fun refreshUserInfo()
    protected abstract fun refreshOnlineTime()

    protected fun getLocalStorage(): MMKV {
        return mk
    }

    protected fun initHandler() {
        Config.subscriberHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    0x10000 -> {
                        val bundle = msg.data
                        if (bundle != null) {
                            val subscriber = bundle.getParcelable<Subscriber>("subscriber")
                            if (subscriber != null) {
                                activity?.let {
                                    //???????????????????????????
                                    setOnlineStatus(subscriber.uid, subscriber.online)
                                }
                            }
                        }
                    }

                    0x10001 -> {
                        activity?.let {
                            refreshUserInfo()
                        }
                    }

                    0x10002 -> {
                        activity?.let {
                            refreshOnlineTime()
                        }
                    }
                }
            }
        }
    }

    protected fun getUserInfo(res: (UserInfo) -> Unit) {
        val userInfo = getLocalStorage().decodeParcelable("user_info", UserInfo::class.java)
        if (userInfo == null) {
            DataManager.getUserInfo {
                res(it)
            }
        } else {
            res(userInfo)
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

    /**
     * ?????????????????????????????????
     */
    protected fun getLocation(success: () -> Unit) {
        waitingDialog?.show()

        AppUtil.getLocation(requireContext(), object : LocationCallback {
            override fun onSuccess(address: Address) {
                waitingDialog?.cancel()
                success()
                DataManager.updateCountry(address.countryCode) {}

                context?.let { ctx ->
                    val permission = getLocalStorage().decodeBool("home_get_location_success", false)
                    if (!permission) {
                        ReportManager.firebaseCustomLog(firebaseAnalytics, "home_get_location_success", "get location success")
                        ReportManager.appsFlyerCustomLog(ctx, "home_get_location_success", "get location success")
                    }
                }

            }

            override fun onFailed() {
                requireActivity().runOnUiThread {
                    waitingDialog?.cancel()

                    activity?.let { ctx ->
                        ToastUtil.showShort(ctx, "get location failed, please check your country")
                        startActivity(Intent(ctx, CountryActivity::class.java))

                        val permission = getLocalStorage().decodeBool("home_get_location_failed", false)
                        if (!permission) {
                            ReportManager.firebaseCustomLog(firebaseAnalytics, "home_get_location_failed", "get location failed")
                            ReportManager.appsFlyerCustomLog(ctx, "home_get_location_failed", "get location failed")
                        }
                    }

                }
            }
        })
    }

    protected fun getMatchConfig(): MatchConfig {
        var matchConfig = mk.decodeParcelable("match_config", MatchConfig::class.java)
        if (matchConfig == null) {
            matchConfig = MatchConfig(0, "", "", false)
            mk.encode("match_config", matchConfig)
        }
        return matchConfig
    }

    protected fun checkLocationPermissions(success: () -> Unit) {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        requestPermission(permissions) {
            when (it) {
                "grant" -> success()
                "rationale", "deny" -> {
                    PermissionPageUtils(requireContext()).jumpPermissionPage()
                }
            }
        }
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
                        ReportManager.appsFlyerCustomLog(requireContext(), "home_video_permission_success", "get permission success")
                    }
                }

                "deny" -> {
                    PermissionPageUtils(requireContext()).jumpPermissionPage()
                    val permission = getLocalStorage().decodeBool("home_video_permission", false)
                    if (!permission) {
                        ReportManager.firebaseCustomLog(firebaseAnalytics, "home_video_permission_failed", "get permission failed")
                        ReportManager.appsFlyerCustomLog(requireContext(), "home_video_permission_failed", "get permission failed")
                    }
                }

                else -> {
                    val permission = getLocalStorage().decodeBool("home_video_permission", false)
                    if (!permission) {
                        ReportManager.firebaseCustomLog(firebaseAnalytics, "home_video_permission_failed", "get permission failed")
                        ReportManager.appsFlyerCustomLog(requireContext(), "home_video_permission_failed", "get permission failed")
                    }
                }
            }
        }
    }

    protected fun checkPermissions(success: () -> Unit) {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )

        requestPermission(permissions) {
            when (it) {
                "grant" -> success()
                "rationale", "deny" -> {
                    PermissionPageUtils(requireContext()).jumpPermissionPage()
                }
            }
        }
    }

    protected fun checkReadAndWritePermissions(success: () -> Unit) {
        val permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
        )

        requestPermission(permissions) {
            when (it) {
                "grant" -> success()
                "deny" -> {
                    PermissionPageUtils(requireContext()).jumpPermissionPage()
                }
            }
        }
    }

    private fun requestPermission(permissions: Array<out String>, method: (String) -> Unit) {
        LivePermissions(this@BaseFragment)
            .request(permissions)
            .observe(this@BaseFragment) {
                when (it) {
                    is PermissionResult.Grant -> {
                        method("grant")
                    }

                    is PermissionResult.Rationale -> {
                        //????????????
                        it.permissions.forEach { s ->
                            JLog.i("Rationale:${s}")
                            method("rationale")
                        }
                    }

                    is PermissionResult.Deny -> {
                        ToastUtil.showShort(context, "please open the related permissions")
                        //???????????????????????????????????????
                        it.permissions.forEach { s ->
                            JLog.i("deny:${s}")
                            method("deny")
                        }
                    }
                }
            }
    }

    /**
     * ??????????????????
     * @param uid ??????id
     * * @param online ????????????
     */
    protected fun requestVideoChat(uid: Int, online: Boolean) {
        getUserInfo { userInfo ->
            if (userInfo.uid == uid) return@getUserInfo

            ReportManager.firebaseCustomLog(firebaseAnalytics, "private_video_click", "private video click")
            ReportManager.appsFlyerCustomLog(requireContext(), "private_video_click", "private video click")

            if (userInfo.sex == 0 || userInfo.age == 0 || userInfo.avatar.isBlank() || userInfo.nickname.isBlank()) {
                //?????????????????????????????????????????????????????????
                ToastUtil.showLong(requireContext(), getString(R.string.notice_incomplete_profile))
                startActivity(Intent(requireActivity(), GuideActivity::class.java))
                return@getUserInfo
            }

            DataManager.getBasePrice { basePrice ->
                NoticeDialog(requireActivity(), userInfo.coin, basePrice.common.private_video) {

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
            XKitRouter.withKey(RouterConstant.PATH_CHAT_P2P_PAGE).withParam(RouterConstant.CHAT_KRY, user).withContext(requireContext()).navigate()

            ReportManager.firebaseCustomLog(firebaseAnalytics, "private_msg_click", "private msg click")
            ReportManager.appsFlyerCustomLog(requireContext(), "private_msg_click", "private msg click")
        }
    }

    /**
     * ??????????????????
     * @param uid ??????id
     * @param online ????????????
     */
    protected fun showUserDetail(uid: Int, online: Boolean, showChat: Boolean) {
        DataManager.getUserInfoById(uid) { userInfo ->
            ProfileDialog(requireActivity(), userInfo, online, showChat = showChat) {
                when (it) {
                    "video" -> requestVideoChat(uid, online)
                    "im" -> requestChat(userInfo.uid, userInfo.nickname, userInfo.avatar)
                }
            }

            ReportManager.firebaseCustomLog(firebaseAnalytics, "look_user_info", "look user info")
            ReportManager.appsFlyerCustomLog(requireContext(), "look_user_info", "look user info")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Config.subscriberHandler = null
    }

}