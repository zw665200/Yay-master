package com.ql.recovery.yay.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.android.billingclient.api.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.netease.yunxin.kit.adapters.DataAdapter
import com.netease.yunxin.kit.common.utils.ThreadUtils.runOnUiThread
import com.ql.recovery.bean.Server
import com.ql.recovery.bean.UserInfo
import com.ql.recovery.config.Config
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.callback.PayCallback
import com.ql.recovery.yay.databinding.DialogPrimeBinding
import com.ql.recovery.yay.databinding.ItemPrimeBinding
import com.ql.recovery.yay.manager.PayManager
import com.ql.recovery.yay.manager.ReportManager
import com.ql.recovery.yay.util.AppUtil
import com.ql.recovery.yay.util.ToastUtil
import com.tencent.mmkv.MMKV

class PrimeDialog(
    private val activity: Activity,
    private val isPrime: Boolean,
    private val func: () -> Unit
) : Dialog(activity, R.style.app_dialog2) {
    private lateinit var binding: DialogPrimeBinding
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var pagerAdapter: DataAdapter<Int>
    private var mList = arrayListOf<Int>()
    private var lastClickTime = 0L
    private var currentServer: Server? = null
    private var mType = Type.Pay

    init {
        initVew()
    }

    private fun initVew() {
        binding = DialogPrimeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(true)

        firebaseAnalytics = Firebase.analytics

        binding.ivClose.setOnClickListener { cancel() }
        binding.flMember.setOnClickListener { checkPay() }

        getSubProductList()
        initViewPager()
        getBenefitsList()

        if (isPrime) {
            initPrimeStatus()
        } else {
            binding.flMember.visibility = View.VISIBLE
            binding.flClaim.visibility = View.GONE
            binding.tvEndTime.visibility = View.GONE
            mType = Type.Pay
        }

        ReportManager.firebaseCustomLog(firebaseAnalytics, "prime_dialog_open", "dialog open")
        ReportManager.appsFlyerCustomLog(activity, "prime_dialog_open", "dialog open")

        show()
    }

    private fun initPrimeStatus() {
        binding.flMember.visibility = View.GONE
        binding.flClaim.visibility = View.VISIBLE

        mType = Type.Reward
        checkVIPStatus()

        //更新用户资料
        DataManager.getUserInfo { }
    }

    private fun initViewPager() {
        pagerAdapter = DataAdapter.Builder<Int>()
            .setData(mList)
            .setLayoutId(R.layout.item_prime)
            .addBindView { itemView, itemData ->
                val itemBinding = ItemPrimeBinding.bind(itemView)
                itemBinding.ivPic.setImageResource(itemData)

                itemView.post {
                    val width = itemView.width
                    val lp = itemView.layoutParams
                    lp.height = width * 253 / 483
                    itemView.layoutParams = lp
                }
            }
            .create()

        binding.rcBenefits.adapter = pagerAdapter
        binding.rcBenefits.layoutManager = GridLayoutManager(activity, 2)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getBenefitsList() {
        mList.clear()
        mList.add(R.drawable.benefits_1)
        mList.add(R.drawable.benefits_2)
        mList.add(R.drawable.benefits_3)
        mList.add(R.drawable.benefits_4)
        mList.add(R.drawable.benefits_5)
        mList.add(R.drawable.benefits_6)
        mList.add(R.drawable.benefits_7)
        mList.add(R.drawable.benefits_8)

        pagerAdapter.notifyDataSetChanged()
    }

    @SuppressLint("SetTextI18n")
    private fun getSubProductList() {
        DataManager.getProductList("sub") { serverList ->
            if (serverList.isNotEmpty()) {
                currentServer = serverList[0]
                binding.tvPrice.text = "$${currentServer!!.price}/Mon"

                if (!isPrime) {
                    val price = 5.99 - serverList[0].price.toFloat()
                    binding.tvReduce.text = String.format(activity.getString(R.string.prime_reduce), price)
                }
            }
        }
    }

    private fun checkVIPStatus() {
        DataManager.checkDailyReward { isClaimd ->
            if (isClaimd) {
                binding.tvCount.text = String.format(activity.getString(R.string.prime_vip_daily_benefits_day), 50)
                binding.tvClaim.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.shape_corner_light_yellow, null)
                binding.tvClaim.text = activity.getString(R.string.prime_get)
            } else {
                binding.tvCount.text = String.format(activity.getString(R.string.prime_vip_daily_benefits_day), 50)
                binding.tvClaim.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.shape_corner_yellow_8, null)
                binding.tvClaim.text = activity.getString(R.string.prime_get_free)
            }
        }

        DataManager.getVipStatus { vip ->
            //设置会员截止时间
            binding.tvEndTime.text = String.format(activity.getString(R.string.prime_end_time), AppUtil.timeStamp2Date(vip.expires_date, ""))

            if (vip.received) {
                binding.tvCount.text = String.format(activity.getString(R.string.prime_vip_daily_benefits_day), vip.number_of_coins)
                binding.tvClaim.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.shape_corner_light_yellow, null)
                binding.tvClaim.text = activity.getString(R.string.prime_get)
            } else {
                binding.tvCount.text = String.format(activity.getString(R.string.prime_vip_daily_benefits_day), vip.number_of_coins)
                binding.tvClaim.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.shape_corner_yellow_8, null)
                binding.tvClaim.text = activity.getString(R.string.prime_get_free)
            }
        }
    }

    private fun checkPay() {
        when (mType) {
            Type.Pay -> doPay()
            Type.Reward -> getReward()
        }
    }

    private fun getReward() {
        DataManager.getVipReward {
            if (it) {
                binding.tvClaim.isEnabled = false
                binding.tvClaim.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.shape_corner_light_yellow, null)
                binding.tvClaim.text = activity.getString(R.string.prime_get)

                //刷新用户信息
                Config.mainHandler?.sendEmptyMessage(0x10006)
                Config.subscriberHandler?.sendEmptyMessage(0x10001)
            }
        }
    }

    private fun doPay() {
        if (currentServer == null) return

        if (lastClickTime == 0L) {
            lastClickTime = System.currentTimeMillis()
        } else if (System.currentTimeMillis() - lastClickTime < 2 * 1000) {
            return
        }

        val userInfo = MMKV.defaultMMKV().decodeParcelable("user_info", UserInfo::class.java) ?: return

        lastClickTime = System.currentTimeMillis()

        if (currentServer!!.code.isBlank()) {
            ReportManager.firebaseCustomLog(firebaseAnalytics, "purchase_begin_click", "free")
            ReportManager.appsFlyerCustomLog(activity, "purchase_begin_click", "free")
        } else {
            ReportManager.firebaseCustomLog(firebaseAnalytics, "purchase_begin_click", currentServer!!.code)
            ReportManager.appsFlyerCustomLog(activity, "purchase_begin_click", currentServer!!.code)
        }

        DataManager.createOrder(currentServer!!.id) { orderParam ->
            if (orderParam.is_paid) {
                cancel()
                func()
                return@createOrder
            }

            PayManager.get().doGoogleFastPay(
                activity,
                currentServer!!.code,
                "acknowledge",
                orderParam.order_id,
                orderParam.order_no,
                userInfo.uid,
                object : PayCallback {
                    override fun success(token: String) {
                        runOnUiThread {
                            initPrimeStatus()
                            func()

                            //刷新用户信息
                            Config.mainHandler?.sendEmptyMessage(0x10006)
                            Config.subscriberHandler?.sendEmptyMessage(0x10001)

                            //上报支付日志
                            if (currentServer!!.currency != null) {
                                ReportManager.firebasePurchaseLog(firebaseAnalytics, currentServer!!.currency!!, currentServer!!.price)
                                ReportManager.facebookPurchaseLog(activity, currentServer!!.currency!!, currentServer!!.price)
                                ReportManager.branchPurchaseLog(activity, currentServer!!.name, currentServer!!.currency!!, currentServer!!.price)
                                ReportManager.appsFlyerPurchaseLog(activity, currentServer!!.code, currentServer!!.price)
                            } else {
                                ReportManager.firebasePurchaseLog(firebaseAnalytics, "USD", currentServer!!.price)
                                ReportManager.facebookPurchaseLog(activity, "USD", currentServer!!.price)
                                ReportManager.branchPurchaseLog(activity, currentServer!!.name, "USD", currentServer!!.price)
                                ReportManager.appsFlyerPurchaseLog(activity, currentServer!!.code, currentServer!!.price)
                            }
                        }
                    }

                    override fun failed(msg: String) {
                        runOnUiThread {
                            ToastUtil.showShort(activity, msg)
                            ReportManager.firebaseCustomLog(firebaseAnalytics, "purchase_cancel", "purchase cancel")
                            ReportManager.appsFlyerCustomLog(activity, "purchase_cancel", "purchase cancel")
                        }
                    }
                })
        }
    }

    override fun show() {
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.BOTTOM
            width = AppUtil.getScreenWidth(context)
            height = WindowManager.LayoutParams.WRAP_CONTENT
            flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
            dimAmount = 0.5f
        }
        super.show()
    }

    enum class Type { Pay, Reward }

}