package com.ql.recovery.yay.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.netease.yunxin.kit.adapters.DataAdapter
import com.ql.recovery.bean.*
import com.ql.recovery.config.Config
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.callback.PayCallback
import com.ql.recovery.yay.databinding.*
import com.ql.recovery.yay.manager.ImageManager
import com.ql.recovery.yay.manager.PayManager
import com.ql.recovery.yay.manager.ReportManager
import com.ql.recovery.yay.ui.login.LoginActivity
import com.ql.recovery.yay.util.AppUtil
import com.ql.recovery.yay.util.ToastUtil
import com.tencent.mmkv.MMKV

class StoreDialog(
    private val activity: Activity,
    private val userInfo: UserInfo
) : Dialog(activity, R.style.app_dialog) {
    private lateinit var binding: DialogStoreBinding
    private lateinit var mAdapter: DataAdapter<Server>
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var waitingDialog: WaitingDialog? = null
    private var mList = arrayListOf<Server>()
    private var currentServer: Server? = null
    private var lastClickTime: Long = 0L

    init {
        initVew()
    }

    private fun initVew() {
        binding = DialogStoreBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(true)

        waitingDialog = WaitingDialog(activity)
        firebaseAnalytics = Firebase.analytics

        binding.tvCoin.text = userInfo.coin.toString()

        binding.ivClose.setOnClickListener { cancel() }

        initServerList()
        loadServerList()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initServerList() {
        mAdapter = DataAdapter.Builder<Server>()
            .setData(mList)
            .setLayoutId(R.layout.item_store)
            .addBindView { itemView, itemData ->
                val itemBinding = ItemStoreBinding.bind(itemView)
                itemBinding.tvCount.text = itemData.count.toString()

                if (itemData.price.contains("$")) {
                    itemBinding.tvMoney.text = itemData.price
                } else {
                    itemBinding.tvMoney.text = String.format(activity.getString(R.string.store_price), itemData.price)
                }

                if (!itemData.change_Price.isNullOrEmpty()) {
                    if (itemData.change_Price!!.contains("$")) {
                        itemBinding.tvMoney.text = itemData.change_Price
                    }
                } else {
                    itemBinding.tvMoney.text = String.format(activity.getString(R.string.store_price), itemData.price)
                }

                if (itemData.price == "0.00") {
                    itemBinding.tvMoney.text = activity.getString(R.string.store_pay_free)
                }

                when (itemData.code) {
                    "coin_level_one", "" -> {
                        itemBinding.tvCount.visibility = View.VISIBLE
                        itemBinding.ivTag.setImageResource(R.drawable.hybg_standard)
                        itemBinding.tvMoney.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.hybg_m, null)

                        ImageManager.getBitmap(activity, itemData.icon) {
                            itemBinding.ivIcon.setImageBitmap(it)
                        }
                    }

                    "coin_level_two" -> {
                        itemBinding.tvCount.visibility = View.VISIBLE
                        itemBinding.ivTag.setImageResource(R.drawable.hybg_free)
                        itemBinding.tvMoney.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.hybg_b, null)
                        itemBinding.root.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.hybg_c, null)
                        itemBinding.tvCount.setTextColor(Color.WHITE)

                        ImageManager.getBitmap(activity, itemData.icon) {
                            itemBinding.ivIcon.setImageBitmap(it)
                        }
                    }

                    "coin_level_three" -> {
                        itemBinding.tvCount.visibility = View.VISIBLE
                        itemBinding.ivTag.setImageResource(R.drawable.hybg_save_17)
                        itemBinding.tvMoney.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.hybg_m, null)

                        ImageManager.getBitmap(activity, itemData.icon) {
                            itemBinding.ivIcon.setImageBitmap(it)
                        }
                    }

                    "member_subscribe_monthly" -> {
                        itemBinding.ivTag.setImageResource(R.drawable.hybg_save_33)
                        itemBinding.tvMoney.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.hybg_b, null)
                        itemBinding.root.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.hybg_a, null)
                        itemBinding.ivIcon.setImageResource(R.drawable.hybg_vip)
                        itemBinding.tvCount.visibility = View.GONE
                    }

                    "coin_level_four_in_app" -> {
                        itemBinding.tvCount.visibility = View.VISIBLE
                        itemBinding.ivTag.setImageResource(R.drawable.hybg_save_23)
                        itemBinding.tvMoney.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.hybg_m, null)

                        ImageManager.getBitmap(activity, itemData.icon) {
                            itemBinding.ivIcon.setImageBitmap(it)
                        }
                    }

                    "coin_level_five" -> {
                        itemBinding.tvCount.visibility = View.VISIBLE
                        itemBinding.ivTag.setImageResource(R.drawable.hybg_save_27)
                        itemBinding.tvMoney.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.hybg_b, null)
                        itemBinding.root.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.hybg_a, null)

                        ImageManager.getBitmap(activity, itemData.icon) {
                            itemBinding.ivIcon.setImageBitmap(it)
                        }
                    }

                    "coin_level_six" -> {
                        itemBinding.tvCount.visibility = View.VISIBLE
                        itemBinding.ivTag.setImageResource(R.drawable.hybg_save_29)
                        itemBinding.tvMoney.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.hybg_m, null)

                        ImageManager.getBitmap(activity, itemData.icon) {
                            itemBinding.ivIcon.setImageBitmap(it)
                        }
                    }

                    "coin_level_seven" -> {
                        itemBinding.tvCount.visibility = View.VISIBLE
                        itemBinding.ivTag.setImageResource(R.drawable.hybg_save_32)
                        itemBinding.tvMoney.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.hybg_b, null)
                        itemBinding.root.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.hybg_a, null)

                        ImageManager.getBitmap(activity, itemData.icon) {
                            itemBinding.ivIcon.setImageBitmap(it)
                        }
                    }
                }

                itemView.setOnClickListener {
                    currentServer = itemData
                    checkPay()
                }
            }
            .create()

        binding.rvProduct.itemAnimator?.changeDuration = 0L
        binding.rvProduct.layoutManager = GridLayoutManager(activity, 2)
        binding.rvProduct.adapter = mAdapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadServerList() {
        DataManager.getProductList("lemon") { list ->
            mList.clear()
            mList.addAll(list)
            mAdapter.notifyDataSetChanged()
        }
    }

    fun setTime(time: Int) {
        binding.progressView.progress = time
        if (time > 25000L) {
            binding.progressView.visibility = View.GONE
        } else {
            binding.progressView.visibility = View.VISIBLE
        }
    }

    private fun checkPay() {
        val userInfo = MMKV.defaultMMKV().decodeParcelable("user_info", UserInfo::class.java)
        if (userInfo == null) {
            activity.startActivity(Intent(activity, LoginActivity::class.java))
            return
        }

        if (lastClickTime == 0L) {
            lastClickTime = System.currentTimeMillis()
        } else if (System.currentTimeMillis() - lastClickTime < 2 * 1000) {
            return
        }

        lastClickTime = System.currentTimeMillis()

        if (currentServer == null) {
            return
        }

        if (currentServer!!.code.isBlank()) {
            ReportManager.firebaseCustomLog(firebaseAnalytics, "purchase_begin_click", "free")
            ReportManager.appsFlyerCustomLog(activity, "purchase_begin_click", "free")
        } else {
            ReportManager.firebaseCustomLog(firebaseAnalytics, "purchase_begin_click", currentServer!!.code)
            ReportManager.appsFlyerCustomLog(activity, "purchase_begin_click", currentServer!!.code)
        }

        when (currentServer!!.type) {
            "free" -> doPay(activity, userInfo, 0)
            "sub" -> doPay(activity, userInfo, 1)
            "lemon" -> doPay(activity, userInfo, 2)
        }
    }

    /**
     *  发起支付
     *  @param index 1：google订阅 2：google支付
     */
    private fun doPay(c: Activity, userInfo: UserInfo, index: Int) {
        when (index) {
            0 -> {
                DataManager.createOrder(currentServer!!.id) {
                    if (it.is_paid) {
                        paySuccess(true)
                        return@createOrder
                    }
                }
            }

            1 -> {
                DataManager.createOrder(currentServer!!.id) { orderParam ->
                    if (orderParam.is_paid) {
                        paySuccess(true)
                        return@createOrder
                    }

                    PayManager.get().doGoogleFastPay(
                        c,
                        currentServer!!.code,
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
                DataManager.createOrder(currentServer!!.id) { orderParam ->
                    if (orderParam.is_paid) {
                        paySuccess(true)
                        return@createOrder
                    }

                    PayManager.get().doGoogleFastPay(
                        c,
                        currentServer!!.code,
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
        getUserInfo()

        //load server
        loadServerList()

        if (!isFree) {
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

        if (Config.mainHandler != null) {
            Config.mainHandler!!.sendEmptyMessage(0x10006)
        }
    }

    private fun getUserInfo() {
        DataManager.getUserInfo {
            binding.tvCoin.text = it.coin.toString()
        }
    }

    override fun show() {
        if (isShowing) return

        val h = AppUtil.getScreenHeight(activity)
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.BOTTOM
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = h * 3 / 4
            flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
            dimAmount = 0.5f
        }
        super.show()
    }

    enum class Type { Game, Record }

}