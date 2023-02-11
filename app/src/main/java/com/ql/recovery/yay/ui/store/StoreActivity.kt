package com.ql.recovery.yay.ui.store

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
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
import com.ql.recovery.yay.ui.base.BaseActivity
import com.ql.recovery.yay.ui.login.LoginActivity
import com.ql.recovery.yay.ui.mine.FeedbackActivity
import com.ql.recovery.yay.ui.mine.ShareActivity
import com.ql.recovery.yay.util.JLog
import com.ql.recovery.yay.util.ToastUtil

class StoreActivity : BaseActivity() {
    private lateinit var binding: ActivityStoreBinding
    private lateinit var mAdapter: DataAdapter<Server>
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private var mList = arrayListOf<Server>()
    private var currentServer: Server? = null
    private var lastClickTime: Long = 0L
    private var firstLoad = false

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
        loadServerList()
        getUserInfo()
        firebaseAnalytics = Firebase.analytics
    }

    private fun initServerList() {
        mAdapter = DataAdapter.Builder<Server>()
            .setData(mList)
            .setLayoutId(R.layout.item_store)
            .addBindView { itemView, itemData ->
                val itemBinding = ItemStoreBinding.bind(itemView)
                itemBinding.tvCount.text = itemData.count.toString()
                itemBinding.tvMoney.typeface = Typeface.createFromAsset(assets, "fonts/DINPro-Bold.otf")

                if (itemData.price.contains("$")) {
                    itemBinding.tvMoney.text = itemData.price
                } else {
                    itemBinding.tvMoney.text = String.format(getString(R.string.store_price), itemData.price)
                }

                if (itemData.price == "0.00") {
                    itemBinding.tvMoney.text = getString(R.string.store_pay_free)
                }

                ImageManager.getBitmap(this, itemData.icon) {
                    itemBinding.ivIcon.setImageBitmap(it)
                }

                itemBinding.tvMoney.setOnClickListener {
                    currentServer = itemData
                    checkPay()
                }
            }
            .create()

        binding.rvProduct.layoutManager = GridLayoutManager(this, 2)
        binding.rvProduct.adapter = mAdapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadServerList() {
        DataManager.getProductList("lemon") { list ->
            mList.clear()
            mList.addAll(list)
            mAdapter.notifyDataSetChanged()

            if (!firstLoad) {
                firstLoad = true
                getSubProductList(list)
            }
        }
    }

    private fun getSubProductList(serverList: List<Server>) {
        val listener = PurchasesUpdatedListener { billingResult, purchases -> }

        val billingClient = BillingClient.newBuilder(this)
            .setListener(listener)
            .enablePendingPurchases()
            .build()

        if (serverList.isNotEmpty()) {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingServiceDisconnected() {
                    // Try to restart the connection on the next request to
                    // Google Play by calling the startConnection() method.
                    JLog.i("onBillingServiceDisconnected")
                }

                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        // The BillingClient is ready. You can query purchases here.
                        JLog.i("onBillingSetupFinished")
                        JLog.i("serverList = $serverList")
                        JLog.i("serverList = ${serverList.size}")

                        val subList = serverList.filter { it.type == "sub" }
                        val consumeList = serverList.filter { it.type == "lemon" }

                        val subListProductList = arrayListOf<QueryProductDetailsParams.Product>()
                        for (server in subList) {
                            JLog.i("server type = ${server.type}")
                            subListProductList.add(
                                QueryProductDetailsParams.Product.newBuilder()
                                    .setProductId(server.code)
                                    .setProductType(BillingClient.ProductType.SUBS)
                                    .build()
                            )
                        }

                        val consumeProductList = arrayListOf<QueryProductDetailsParams.Product>()
                        for (server in consumeList) {
                            JLog.i("server type = ${server.type}")
                            consumeProductList.add(
                                QueryProductDetailsParams.Product.newBuilder()
                                    .setProductId(server.code)
                                    .setProductType(BillingClient.ProductType.INAPP)
                                    .build()
                            )
                        }

                        val querySubProductDetailsParams =
                            QueryProductDetailsParams.newBuilder()
                                .setProductList(subListProductList)
                                .build()

                        val queryConsumeProductDetailsParams =
                            QueryProductDetailsParams.newBuilder()
                                .setProductList(consumeProductList)
                                .build()

                        billingClient.queryProductDetailsAsync(querySubProductDetailsParams) { result, productDetailsList ->
                            // check billingResult
                            // process returned productDetailsList
                            if (productDetailsList.isEmpty()) {
                                JLog.i("no goods found")
                                return@queryProductDetailsAsync
                            }

                            JLog.i("result = $result")
                            JLog.i("productDetailsList = $productDetailsList")

                            for (productDetails in productDetailsList) {
                                JLog.i("productDetails = $productDetails")
                                if (!productDetails.subscriptionOfferDetails.isNullOrEmpty()) {
                                    for (productDetail in productDetails.subscriptionOfferDetails!!) {
                                        val pricingPhases = productDetails.subscriptionOfferDetails!![0].pricingPhases
                                        for (item in pricingPhases.pricingPhaseList) {
                                            JLog.i("price = ${item.formattedPrice}")
                                            JLog.i("code = ${item.priceCurrencyCode}")
                                            runOnUiThread {
                                                val server = mList.find { it.code == productDetails.productId }
                                                if (server != null) {
                                                    val position = mList.indexOf(server)
                                                    mList[position].price = item.formattedPrice
                                                    mAdapter.notifyItemChanged(position)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        billingClient.queryProductDetailsAsync(queryConsumeProductDetailsParams) { result, productDetailsList ->
                            // check billingResult
                            // process returned productDetailsList
                            if (productDetailsList.isEmpty()) {
                                JLog.i("no goods found")
                                return@queryProductDetailsAsync
                            }

                            JLog.i("result = $result")

                            for (productDetails in productDetailsList) {
                                JLog.i("productDetails = $productDetails")
                                val details = productDetails.oneTimePurchaseOfferDetails
                                if (details != null) {
                                    JLog.i("price = ${details.formattedPrice}")
                                    JLog.i("code = ${details.priceCurrencyCode}")
                                    runOnUiThread {
                                        val server = mList.find { it.code == productDetails.productId }
                                        if (server != null) {
                                            val position = mList.indexOf(server)
                                            mList[position].price = details.formattedPrice
                                            mAdapter.notifyItemChanged(position)
                                        }
                                    }
                                }
                            }
                        }

                    } else {
                        JLog.i("error code = ${billingResult.responseCode},${billingResult.debugMessage}")
                        when (billingResult.responseCode) {
                        }
                    }
                }
            })
        }
    }

    private fun getUserInfo() {
        DataManager.getUserInfo {
            binding.tvCoin.typeface = Typeface.createFromAsset(assets, "fonts/DINPro-Bold.otf")
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

//        LogReportManager.logReport("支付", LogReportManager.LogType.PURCHASE)

        when (currentServer!!.type) {
            "free" -> doPay(this, 0)
            "sub" -> doPay(this, 1)
            "lemon" -> doPay(this, 2)
        }
    }

    /**
     *  发起支付
     *  @param index 1：google订阅 2：google支付
     */
    private fun doPay(c: Activity, index: Int) {
        when (index) {
            0 -> {
                DataManager.createOrder(currentServer!!.id) {
                    if (it.is_paid) {
                        paySuccess()
                        return@createOrder
                    }
                }
            }

            1 -> {
                DataManager.createOrder(currentServer!!.id) {
                    if (it.is_paid) {
                        paySuccess()
                        return@createOrder
                    }

                    PayManager.get().doGoogleFastPay(
                        c,
                        currentServer!!.code,
                        "acknowledge",
                        it.order_id,
                        object : PayCallback {
                            override fun success(token: String) {
                                runOnUiThread {
                                    paySuccess()
                                }
                            }

                            override fun failed(msg: String) {
                                runOnUiThread {
                                    ToastUtil.showShort(c, msg)
                                }
                            }
                        })
                }
            }

            2 -> {
                DataManager.createOrder(currentServer!!.id) {
                    if (it.is_paid) {
                        paySuccess()
                        return@createOrder
                    }

                    PayManager.get().doGoogleFastPay(
                        c,
                        currentServer!!.code,
                        "consume",
                        it.order_id,
                        object : PayCallback {
                            override fun success(token: String) {
                                runOnUiThread {
                                    paySuccess()
                                }
                            }

                            override fun failed(msg: String) {
                                runOnUiThread {
                                    ToastUtil.showShort(c, msg)
                                }
                            }
                        })
                }
            }
        }
    }

    private fun paySuccess() {
        //pay success
        ToastUtil.showShort(this, getString(R.string.store_pay_success))

        //get coin
        getUserInfo()

        //load server
        loadServerList()

        //firebase pay import
//        val bundle = Bundle()
//        bundle.putString(FirebaseAnalytics.Param.CURRENCY, "USD")
//        bundle.putFloat(FirebaseAnalytics.Param.VALUE, currentServer!!.price.toFloat())
//        bundle.putString(FirebaseAnalytics.Param.AFFILIATION, "Google Play")
//        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.PURCHASE, bundle)

//        LogReportManager.logReport("支付", LogReportManager.LogType.PURCHASED)

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