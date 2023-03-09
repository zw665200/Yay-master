package com.ql.recovery.yay.ui.store

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.android.billingclient.api.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.netease.yunxin.kit.adapters.DataAdapter
import com.ql.recovery.bean.Server
import com.ql.recovery.bean.UserInfo
import com.ql.recovery.config.Config
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.callback.PayCallback
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.databinding.ActivityStoreBinding
import com.ql.recovery.yay.databinding.ItemStoreBinding
import com.ql.recovery.yay.manager.ImageManager
import com.ql.recovery.yay.manager.PayManager
import com.ql.recovery.yay.manager.ReportManager
import com.ql.recovery.yay.ui.base.BaseActivity
import com.ql.recovery.yay.ui.login.LoginActivity
import com.ql.recovery.yay.ui.mine.FeedbackActivity
import com.ql.recovery.yay.ui.mine.ShareActivity
import com.ql.recovery.yay.util.ToastUtil

class StoreActivity : BaseActivity() {
    private lateinit var binding: ActivityStoreBinding
    private lateinit var mAdapter: DataAdapter<Server>
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var billingClient: BillingClient

    private var mList = arrayListOf<Server>()
    private var currentServer: Server? = null
    private var lastClickTime: Long = 0L

    override fun getViewBinding(baseBinding: ActivityBaseBinding) {
        binding = ActivityStoreBinding.inflate(layoutInflater, baseBinding.flBase, true)
    }

    override fun initView() {
        binding.ivBack.setOnClickListener {
            setResult(0x1)
            finish()
        }
        setStatusBarLight()
        initServerList()

        binding.ivShare.setOnClickListener { toSharePage() }
        binding.llFeedback.setOnClickListener { toFeedbackPage() }
    }

    override fun initData() {
        initGoogleClient()
        loadServerList()
        getUserInfo()
        firebaseAnalytics = Firebase.analytics
    }

    private fun initGoogleClient() {
        val listener = PurchasesUpdatedListener { _, _ -> }

        billingClient = BillingClient.newBuilder(this)
            .setListener(listener)
            .enablePendingPurchases()
            .build()
    }

    private fun initServerList() {
        mAdapter = DataAdapter.Builder<Server>()
            .setData(mList)
            .setLayoutId(R.layout.item_store)
            .addBindView { itemView, itemData ->
                val itemBinding = ItemStoreBinding.bind(itemView)
                itemBinding.tvCount.text = itemData.count.toString()
                itemBinding.tvMoney.typeface = Typeface.createFromAsset(assets, "fonts/din_b.otf")

                if (itemData.price.contains("$")) {
                    itemBinding.tvMoney.text = itemData.price
                } else {
                    itemBinding.tvMoney.text = String.format(getString(R.string.store_price), itemData.price)
                }

                if (!itemData.change_Price.isNullOrEmpty()) {
                    if (itemData.change_Price!!.contains("$")) {
                        itemBinding.tvMoney.text = itemData.change_Price
                    }
                } else {
                    itemBinding.tvMoney.text = String.format(getString(R.string.store_price), itemData.price)
                }

                if (itemData.price == "0.00") {
                    itemBinding.tvMoney.text = getString(R.string.store_pay_free)
                }

                when (itemData.code) {
                    "coin_level_one" -> {
                        itemBinding.tvCount.visibility = View.VISIBLE
                        itemBinding.ivTag.setImageResource(R.drawable.hybg_standard)
                        itemBinding.tvMoney.background = ResourcesCompat.getDrawable(resources, R.drawable.hybg_m, null)

                        ImageManager.getBitmap(this, itemData.icon) {
                            itemBinding.ivIcon.setImageBitmap(it)
                        }
                    }

                    "coin_level_two", "" -> {
                        itemBinding.tvCount.visibility = View.VISIBLE
                        itemBinding.ivTag.setImageResource(R.drawable.hybg_free)
                        itemBinding.tvMoney.background = ResourcesCompat.getDrawable(resources, R.drawable.hybg_b, null)
                        itemBinding.root.background = ResourcesCompat.getDrawable(resources, R.drawable.hybg_c, null)
                        itemBinding.tvCount.setTextColor(Color.WHITE)

                        ImageManager.getBitmap(this, itemData.icon) {
                            itemBinding.ivIcon.setImageBitmap(it)
                        }
                    }

                    "coin_level_three" -> {
                        itemBinding.tvCount.visibility = View.VISIBLE
                        itemBinding.ivTag.setImageResource(R.drawable.hybg_save_17)
                        itemBinding.tvMoney.background = ResourcesCompat.getDrawable(resources, R.drawable.hybg_m, null)

                        ImageManager.getBitmap(this, itemData.icon) {
                            itemBinding.ivIcon.setImageBitmap(it)
                        }
                    }

                    "member_subscribe_monthly" -> {
                        itemBinding.ivTag.setImageResource(R.drawable.hybg_save_33)
                        itemBinding.tvMoney.background = ResourcesCompat.getDrawable(resources, R.drawable.hybg_b, null)
                        itemBinding.root.background = ResourcesCompat.getDrawable(resources, R.drawable.hybg_a, null)
                        itemBinding.ivIcon.setImageResource(R.drawable.hybg_vip)
                        itemBinding.tvCount.visibility = View.GONE
                    }

                    "coin_level_four_in_app" -> {
                        itemBinding.tvCount.visibility = View.VISIBLE
                        itemBinding.ivTag.setImageResource(R.drawable.hybg_save_23)
                        itemBinding.tvMoney.background = ResourcesCompat.getDrawable(resources, R.drawable.hybg_m, null)

                        ImageManager.getBitmap(this, itemData.icon) {
                            itemBinding.ivIcon.setImageBitmap(it)
                        }
                    }

                    "coin_level_five" -> {
                        itemBinding.tvCount.visibility = View.VISIBLE
                        itemBinding.ivTag.setImageResource(R.drawable.hybg_save_27)
                        itemBinding.tvMoney.background = ResourcesCompat.getDrawable(resources, R.drawable.hybg_b, null)
                        itemBinding.root.background = ResourcesCompat.getDrawable(resources, R.drawable.hybg_a, null)

                        ImageManager.getBitmap(this, itemData.icon) {
                            itemBinding.ivIcon.setImageBitmap(it)
                        }
                    }

                    "coin_level_six" -> {
                        itemBinding.tvCount.visibility = View.VISIBLE
                        itemBinding.ivTag.setImageResource(R.drawable.hybg_save_29)
                        itemBinding.tvMoney.background = ResourcesCompat.getDrawable(resources, R.drawable.hybg_m, null)

                        ImageManager.getBitmap(this, itemData.icon) {
                            itemBinding.ivIcon.setImageBitmap(it)
                        }
                    }

                    "coin_level_seven" -> {
                        itemBinding.tvCount.visibility = View.VISIBLE
                        itemBinding.ivTag.setImageResource(R.drawable.hybg_save_32)
                        itemBinding.tvMoney.background = ResourcesCompat.getDrawable(resources, R.drawable.hybg_b, null)
                        itemBinding.root.background = ResourcesCompat.getDrawable(resources, R.drawable.hybg_a, null)

                        ImageManager.getBitmap(this, itemData.icon) {
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
        binding.rvProduct.layoutManager = GridLayoutManager(this, 2)
        binding.rvProduct.adapter = mAdapter
    }

    override fun onResume() {
        super.onResume()
        if (currentServer != null) {
            PayManager.get().checkConsumePurchased(billingClient, currentServer!!.id)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadServerList() {
        DataManager.getProductList("lemon") { list ->
            mList.clear()
            mList.addAll(list)
            mAdapter.notifyDataSetChanged()

            PayManager.get().getSubProductList(this, billingClient, list) { position ->
                mAdapter.notifyItemChanged(position)
            }
        }
    }

    private fun getUserInfo() {
        DataManager.getUserInfo {
            binding.tvCoin.text = it.coin.toString()
        }
    }

    private fun checkPay() {
        val userInfo = getLocalStorage().decodeParcelable("user_info", UserInfo::class.java)
        if (userInfo == null) {
            startActivity(Intent(this, LoginActivity::class.java))
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
            ReportManager.appsFlyerCustomLog(this, "purchase_begin_click", "free")
        } else {
            ReportManager.firebaseCustomLog(firebaseAnalytics, "purchase_begin_click", currentServer!!.code)
            ReportManager.appsFlyerCustomLog(this, "purchase_begin_click", currentServer!!.code)
        }

        when (currentServer!!.type) {
            "free" -> doPay(this, userInfo, 0)
            "sub" -> doPay(this, userInfo, 1)
            "lemon" -> doPay(this, userInfo, 2)
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
                                runOnUiThread {
                                    paySuccess(false)
                                }
                            }

                            override fun failed(msg: String) {
                                runOnUiThread {
                                    ToastUtil.showShort(c, msg)
                                    ReportManager.firebaseCustomLog(firebaseAnalytics, "purchase_cancel", "purchase cancel")
                                    ReportManager.appsFlyerCustomLog(this@StoreActivity, "purchase_cancel", "purchase cancel")
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
                                runOnUiThread {
                                    paySuccess(false)
                                }
                            }

                            override fun failed(msg: String) {
                                runOnUiThread {
                                    ToastUtil.showShort(c, msg)
                                    ReportManager.firebaseCustomLog(firebaseAnalytics, "purchase_cancel", "purchase cancel")
                                    ReportManager.appsFlyerCustomLog(this@StoreActivity, "purchase_cancel", "purchase cancel")
                                }
                            }
                        })
                }
            }
        }
    }

    private fun paySuccess(isFree: Boolean) {
        //pay success
        ToastUtil.showShort(this, getString(R.string.store_pay_success))

        //get coin
        getUserInfo()

        //load server
        loadServerList()

        if (!isFree) {
            //上报支付日志
            if (currentServer!!.currency != null) {
                ReportManager.firebasePurchaseLog(firebaseAnalytics, currentServer!!.currency!!, currentServer!!.price)
                ReportManager.facebookPurchaseLog(this, currentServer!!.currency!!, currentServer!!.price)
                ReportManager.branchPurchaseLog(this, currentServer!!.name, currentServer!!.currency!!, currentServer!!.price)
                ReportManager.appsFlyerPurchaseLog(this, currentServer!!.code, currentServer!!.price)
            } else {
                ReportManager.firebasePurchaseLog(firebaseAnalytics, "USD", currentServer!!.price)
                ReportManager.facebookPurchaseLog(this, "USD", currentServer!!.price)
                ReportManager.branchPurchaseLog(this, currentServer!!.name, "USD", currentServer!!.price)
                ReportManager.appsFlyerPurchaseLog(this, currentServer!!.code, currentServer!!.price)
            }
        }

        if (Config.mainHandler != null) {
            Config.mainHandler!!.sendEmptyMessage(0x10006)
        }
    }

    private fun toSharePage() {
        startActivity(Intent(this, ShareActivity::class.java))
    }

    private fun toFeedbackPage() {
        startActivity(Intent(this, FeedbackActivity::class.java))
    }
}