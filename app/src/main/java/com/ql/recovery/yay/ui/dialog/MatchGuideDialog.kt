package com.ql.recovery.yay.ui.dialog

import android.app.Activity
import android.content.Intent
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.ql.recovery.bean.Server
import com.ql.recovery.bean.UserInfo
import com.ql.recovery.config.Config
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.callback.PayCallback
import com.ql.recovery.yay.databinding.DialogMatchGuideBinding
import com.ql.recovery.yay.manager.PayManager
import com.ql.recovery.yay.manager.ReportManager
import com.ql.recovery.yay.ui.base.BaseDialog
import com.ql.recovery.yay.ui.login.LoginActivity
import com.ql.recovery.yay.util.ToastUtil


class MatchGuideDialog(
    private val activity: Activity,
    private val server: Server,
    private val res: () -> Unit
) : BaseDialog(activity) {
    private lateinit var binding: DialogMatchGuideBinding
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var lastClickTime: Long = 0L

    init {
        initVew()
    }

    private fun initVew() {
        binding = DialogMatchGuideBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(false)

        firebaseAnalytics = Firebase.analytics

        setMatchType()
        show()

        binding.ivPay.setOnClickListener {
            checkPay()
        }

        binding.tvSkip.setOnClickListener {
            cancel()
            res()
        }
    }


    private fun setMatchType() {
        //检查匹配模式
        when (server.code) {
            "member_subscribe_monthly" -> {
                binding.flSub.visibility = View.VISIBLE
                binding.ivPay.text = activity.getString(R.string.match_sub)
                binding.tvNotice.text = activity.getString(R.string.match_ad_content_4)
                binding.tvCallDes.text = activity.getString(R.string.match_ad_title_3)
            }

            "else" -> {
                binding.flSub.visibility = View.GONE
                binding.ivPay.text = String.format(activity.getString(R.string.match_price), server.price)
                binding.tvNotice.text = activity.getString(R.string.match_ad_content_3)
                getUserInfo { userInfo ->
                    if (userInfo.coin < server.price.toInt()) {
                        binding.tvCallDes.text = activity.getString(R.string.match_ad_title_2)
                    } else {
                        binding.tvCallDes.text = activity.getString(R.string.match_ad_title_1)
                    }
                }
            }
        }
    }

    private fun checkPay() {
        if (lastClickTime == 0L) {
            lastClickTime = System.currentTimeMillis()
        } else if (System.currentTimeMillis() - lastClickTime < 2 * 1000) {
            return
        }

        lastClickTime = System.currentTimeMillis()

        if (server.code.isBlank()) {
            ReportManager.firebaseCustomLog(firebaseAnalytics, "purchase_begin_click", "free")
            ReportManager.appsFlyerCustomLog(activity, "purchase_begin_click", "free")
        } else {
            ReportManager.firebaseCustomLog(firebaseAnalytics, "purchase_begin_click", server.code)
            ReportManager.appsFlyerCustomLog(activity, "purchase_begin_click", server.code)
        }

        getUserInfo { userInfo ->
            when (server.type) {
                "free" -> doPay(activity, userInfo, 0)
                "sub" -> doPay(activity, userInfo, 1)
                "lemon" -> doPay(activity, userInfo, 2)
            }
        }
    }

    /**
     *  发起支付
     *  @param index 1：google订阅 2：google支付
     */
    private fun doPay(c: Activity, userInfo: UserInfo, index: Int) {
        when (index) {
            0 -> {
                DataManager.createOrder(server.id) {
                    if (it.is_paid) {
                        paySuccess(true)
                        return@createOrder
                    }
                }
            }

            1 -> {
                DataManager.createOrder(server.id) { orderParam ->
                    if (orderParam.is_paid) {
                        paySuccess(true)
                        return@createOrder
                    }

                    PayManager.get().doGoogleFastPay(
                        c,
                        server.code,
                        "acknowledge",
                        orderParam.order_id,
                        orderParam.order_no,
                        userInfo.uid,
                        object : PayCallback {
                            override fun success(token: String) {
                                activity.runOnUiThread {
                                    paySuccess(false)
                                }
                            }

                            override fun failed(msg: String) {
                                activity.runOnUiThread {
                                    ToastUtil.showShort(c, msg)
                                    ReportManager.firebaseCustomLog(firebaseAnalytics, "purchase_cancel", "purchase cancel")
                                    ReportManager.appsFlyerCustomLog(activity, "purchase_cancel", "purchase cancel")
                                }
                            }
                        })
                }
            }

            2 -> {
                DataManager.createOrder(server.id) { orderParam ->
                    if (orderParam.is_paid) {
                        paySuccess(true)
                        return@createOrder
                    }

                    PayManager.get().doGoogleFastPay(
                        c,
                        server.code,
                        "consume",
                        orderParam.order_id,
                        orderParam.order_no,
                        userInfo.uid,
                        object : PayCallback {
                            override fun success(token: String) {
                                activity.runOnUiThread {
                                    paySuccess(false)
                                }
                            }

                            override fun failed(msg: String) {
                                activity.runOnUiThread {
                                    ToastUtil.showShort(c, msg)
                                    ReportManager.firebaseCustomLog(firebaseAnalytics, "purchase_cancel", "purchase cancel")
                                    ReportManager.appsFlyerCustomLog(activity, "purchase_cancel", "purchase cancel")
                                }
                            }
                        })
                }
            }
        }
    }

    private fun paySuccess(isFree: Boolean) {
        //pay success
        ToastUtil.showShort(activity, activity.getString(R.string.store_pay_success))

        //get coin
        DataManager.getUserInfo {}

        res()

        if (!isFree) {
            //上报支付日志
            if (server.currency != null) {
                ReportManager.firebasePurchaseLog(firebaseAnalytics, server.currency!!, server.price)
                ReportManager.facebookPurchaseLog(activity, server.currency!!, server.price)
                ReportManager.branchPurchaseLog(activity, server.name, server.currency!!, server.price)
                ReportManager.appsFlyerPurchaseLog(activity, server.code, server.price)
            } else {
                ReportManager.firebasePurchaseLog(firebaseAnalytics, "USD", server.price)
                ReportManager.facebookPurchaseLog(activity, "USD", server.price)
                ReportManager.branchPurchaseLog(activity, server.name, "USD", server.price)
                ReportManager.appsFlyerPurchaseLog(activity, server.code, server.price)
            }
        }

        if (Config.mainHandler != null) {
            Config.mainHandler!!.sendEmptyMessage(0x10006)
        }
    }


    override fun cancel() {
        super.cancel()
    }

    override fun show() {
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.CENTER
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
        }

        if (!activity.isFinishing && !activity.isDestroyed) {
            super.show()
        }
    }

    enum class From { MySelf, Other }

}