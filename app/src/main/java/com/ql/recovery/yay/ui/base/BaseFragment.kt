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
import com.ql.recovery.yay.ui.dialog.NoticeDialog
import com.ql.recovery.yay.ui.dialog.ProfileDialog
import com.ql.recovery.yay.ui.dialog.WaitingDialog
import com.ql.recovery.yay.ui.guide.GuideActivity
import com.ql.recovery.yay.ui.login.LoginActivity
import com.ql.recovery.yay.ui.mine.CountryActivity
import com.ql.recovery.yay.util.*
import com.tencent.mmkv.MMKV

abstract class BaseFragment : Fragment(), View.OnClickListener {
    private var mContext: Context? = null
    private var waitingDialog: WaitingDialog? = null
    private val mk = MMKV.defaultMMKV()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = initView(inflater, container, savedInstanceState)
        initHandler()
        initData()

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
    protected abstract fun flushUserInfo()

    protected fun getLocalStorage(): MMKV {
        return mk
    }

    private fun initHandler() {
        Config.subscriberHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    0x10000 -> {
                        val bundle = msg.data
                        if (bundle != null) {
                            val subscriber = bundle.getParcelable<Subscriber>("subscriber")
                            if (subscriber != null) {
                                if (!requireActivity().isFinishing && !requireActivity().isDestroyed) {
                                    //下发并刷新在线状态
                                    setOnlineStatus(subscriber.uid, subscriber.online)
                                }
                            }
                        }
                    }

                    0x10001 -> {
                        flushUserInfo()
                    }
                }
            }
        }
    }

    protected fun checkLogin(func: (UserInfo) -> Unit) {
        val userInfo = mk.decodeParcelable("user_info", UserInfo::class.java)
        if (userInfo != null) {
            func(userInfo)
            return
        }

        startActivity(Intent(requireActivity(), LoginActivity::class.java))
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
     * 定位拿到当前的国家编码
     */
    protected fun getLocation(success: () -> Unit) {
        waitingDialog?.show()
        AppUtil.getLocation(requireContext(), object : LocationCallback {
            override fun onSuccess(address: Address) {
                waitingDialog?.cancel()
                success()
                DataManager.updateCountry(address.countryCode) {}
            }

            override fun onFailed() {
                requireActivity().runOnUiThread {
                    waitingDialog?.cancel()
                    ToastUtil.showShort(requireContext(), "get location failed, please check your country")
                    startActivity(Intent(requireActivity(), CountryActivity::class.java))
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
                "deny" -> {
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
                "grant" -> success()
                "deny" -> {
                    PermissionPageUtils(requireContext()).jumpPermissionPage()
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
                "deny" -> {
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
                        //权限拒绝
                        it.permissions.forEach { s ->
                            JLog.i("Rationale:${s}")
                            method("rationale")
                        }
                    }

                    is PermissionResult.Deny -> {
                        ToastUtil.showShort(context, "please open the related permissions")
                        //权限拒绝，且勾选了不再询问
                        it.permissions.forEach { s ->
                            JLog.i("deny:${s}")
                            method("deny")
                        }
                    }
                }
            }
    }

    /**
     * 发起视频聊天
     * @param uid 用户id
     * * @param online 在线状态
     */
    protected fun requestVideoChat(uid: Int, online: Boolean) {
        getUserInfo { userInfo ->
            if (userInfo.uid == uid) return@getUserInfo

            if (userInfo.sex == 0 || userInfo.age == 0 || userInfo.avatar.isBlank() ||
                userInfo.nickname.isBlank() || userInfo.photos.isEmpty() || userInfo.tags.isEmpty()
            ) {
                //如果用户资料不完整，要填写完整才能匹配
                ToastUtil.showLong(requireContext(), getString(R.string.notice_incomplete_profile))
                startActivity(Intent(requireActivity(), GuideActivity::class.java))
                return@getUserInfo
            }

            DataManager.getBasePrice { basePrice ->
                NoticeDialog(requireActivity(), userInfo.coin, basePrice.common.private_video) {

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
            XKitRouter.withKey(RouterConstant.PATH_CHAT_P2P_PAGE).withParam(RouterConstant.CHAT_KRY, user).withContext(requireContext()).navigate()
        }
    }

    /**
     * 显示用户资料
     * @param uid 用户id
     * @param online 在线状态
     */
    protected fun showUserDetail(uid: Int, online: Boolean, showChat: Boolean) {
        DataManager.getUserInfoById(uid) { userInfo ->
            ProfileDialog(requireActivity(), userInfo, online, showChat = showChat) {
                when (it) {
                    "video" -> requestVideoChat(uid, online)
                    "im" -> requestChat(userInfo.uid, userInfo.nickname, userInfo.avatar)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Config.subscriberHandler = null
    }

}