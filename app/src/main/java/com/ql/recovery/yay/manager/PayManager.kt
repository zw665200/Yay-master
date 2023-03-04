package com.ql.recovery.yay.manager

import android.app.Activity
import com.android.billingclient.api.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.common.collect.ImmutableList
import com.ql.recovery.bean.Server
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.callback.PayCallback
import com.ql.recovery.yay.config.WalletConstant.LOAD_PAYMENT_DATA_REQUEST_CODE
import com.ql.recovery.yay.config.WalletConstant.SHIPPING_COST_CENTS
import com.ql.recovery.yay.util.DoubleUtils
import com.ql.recovery.yay.util.JLog
import com.ql.recovery.yay.util.Json
import com.ql.recovery.yay.util.PaymentsUtil
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

class PayManager private constructor() {
    private lateinit var garmentList: JSONArray
    private lateinit var selectedGarment: JSONObject
    private lateinit var billingClient: BillingClient

    companion object {

        @Volatile
        private var instance: PayManager? = null

        fun get(): PayManager {
            if (instance == null) {
                synchronized(PayManager::class) {
                    if (instance == null) {
                        instance = PayManager()
                    }
                }
            }

            return instance!!
        }
    }

    /**
     * 查询谷歌商品信息
     */
    fun getSubProductList(activity: Activity, billingClient: BillingClient, serverList: List<Server>, func: (position: Int) -> Unit) {
        if (serverList.isNotEmpty()) {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingServiceDisconnected() {
                    // Try to restart the connection on the next request to
                    // Google Play by calling the startConnection() method.
//                    JLog.i("onBillingServiceDisconnected")
                }

                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        // The BillingClient is ready. You can query purchases here.
//                        JLog.i("onBillingSetupFinished")
//                        JLog.i("serverList = $serverList")
//                        JLog.i("serverList = ${serverList.size}")

                        val subList = serverList.filter { it.type == "sub" }
                        val consumeList = serverList.filter { it.type == "lemon" }

                        val subListProductList = arrayListOf<QueryProductDetailsParams.Product>()
                        for (server in subList) {
//                            JLog.i("server type = ${server.code}")
                            subListProductList.add(
                                QueryProductDetailsParams.Product.newBuilder()
                                    .setProductId(server.code)
                                    .setProductType(BillingClient.ProductType.SUBS)
                                    .build()
                            )
                        }

                        val querySubProductDetailsParams =
                            QueryProductDetailsParams.newBuilder()
                                .setProductList(subListProductList)
                                .build()

                        billingClient.queryProductDetailsAsync(querySubProductDetailsParams) { result, productDetailsList ->
                            // check billingResult, process returned productDetailsList
                            if (productDetailsList.isEmpty()) {
                                JLog.i("no goods found")
                                return@queryProductDetailsAsync
                            }

                            for (productDetails in productDetailsList) {
                                if (!productDetails.subscriptionOfferDetails.isNullOrEmpty()) {
                                    for (productDetail in productDetails.subscriptionOfferDetails!!) {
                                        val pricingPhases = productDetails.subscriptionOfferDetails!![0].pricingPhases
                                        for (item in pricingPhases.pricingPhaseList) {
//                                            JLog.i("price = ${item.formattedPrice}")
//                                            JLog.i("code = ${item.priceCurrencyCode}")
                                            activity.runOnUiThread {
                                                val server = serverList.find { it.code == productDetails.productId }
                                                if (server != null) {
                                                    val position = serverList.indexOf(server)
                                                    serverList[position].change_Price = item.formattedPrice
                                                    serverList[position].currency = item.priceCurrencyCode
                                                    func(position)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        val consumeProductList = arrayListOf<QueryProductDetailsParams.Product>()
                        for (server in consumeList) {
//                            JLog.i("server type = ${server.type}")
                            consumeProductList.add(
                                QueryProductDetailsParams.Product.newBuilder()
                                    .setProductId(server.code)
                                    .setProductType(BillingClient.ProductType.INAPP)
                                    .build()
                            )
                        }

                        val queryConsumeProductDetailsParams =
                            QueryProductDetailsParams.newBuilder()
                                .setProductList(consumeProductList)
                                .build()


                        billingClient.queryProductDetailsAsync(queryConsumeProductDetailsParams) { result, productDetailsList ->
                            // check billingResult
                            // process returned productDetailsList
                            if (productDetailsList.isEmpty()) {
                                JLog.i("no goods found")
                                return@queryProductDetailsAsync
                            }

//                            JLog.i("result = $result")

                            for (productDetails in productDetailsList) {
//                                JLog.i("productDetails = $productDetails")
                                val details = productDetails.oneTimePurchaseOfferDetails
                                if (details != null) {
//                                    JLog.i("price = ${details.formattedPrice}")
//                                    JLog.i("code = ${details.priceCurrencyCode}")
                                    activity.runOnUiThread {
                                        val server = serverList.find { it.code == productDetails.productId }
                                        if (server != null) {
                                            val position = serverList.indexOf(server)
                                            serverList[position].change_Price = details.formattedPrice
                                            serverList[position].currency = details.priceCurrencyCode
                                            func(position)
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

    fun doGooglePay(activity: Activity, callback: PayCallback) {
        selectedGarment = fetchRandomGarment(activity)

        val paymentsClient = PaymentsUtil.createPaymentsClient(activity)
        val isReadyToPayJson = PaymentsUtil.isReadyToPayRequest() ?: return
        val req = IsReadyToPayRequest.fromJson(isReadyToPayJson.toString())

        // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
        // OnCompleteListener to be triggered when the result of the call is known.
        val task = paymentsClient.isReadyToPay(req)
        task.addOnCompleteListener { completedTask ->
            try {
                completedTask.getResult(ApiException::class.java)
            } catch (exception: ApiException) {
                // Process error
                JLog.i(exception.toString())
                callback.failed(exception.toString())
            }
        }

        // The price provided to the API should include taxes and shipping.
        // This price is not displayed to the user.
        val garmentPrice = selectedGarment.getDouble("price")
        val priceCents = (garmentPrice * PaymentsUtil.CENTS.toLong()).roundToInt() + SHIPPING_COST_CENTS

        val paymentDataRequestJson = PaymentsUtil.getPaymentDataRequest(priceCents)
        if (paymentDataRequestJson == null) {
            JLog.i("Can't fetch payment data request")
            callback.failed("Can't fetch payment data request")
            return
        }

        val request = PaymentDataRequest.fromJson(paymentDataRequestJson.toString())

        // Since loadPaymentData may show the UI asking the user to select a payment method, we use
        // AutoResolveHelper to wait for the user interacting with it. Once completed,
        // onActivityResult will be called with the result.
        AutoResolveHelper.resolveTask(
            paymentsClient.loadPaymentData(request), activity, LOAD_PAYMENT_DATA_REQUEST_CODE
        )
    }

    private fun fetchRandomGarment(activity: Activity): JSONObject {
        if (!::garmentList.isInitialized) {
            garmentList = Json.readFromResources(activity, R.raw.tshirts)
        }

        val randomIndex: Int = (Math.random() * (garmentList.length() - 1)).roundToInt()
        return garmentList.getJSONObject(randomIndex)
    }

    /**
     * 检查消耗型订单掉单的情况
     */
    fun checkConsumePurchased(client: BillingClient, produceId: Int) {
        val queryParam = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP).build()

        client.queryPurchasesAsync(queryParam) { _, purchaseList ->
            for (purchase in purchaseList) {
                JLog.i("GoogleOrderId = ${purchase.orderId}")
                JLog.i("GooglePurchaseState = ${purchase.purchaseState}")

                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    //在自己服务器创建订单
                    DataManager.createOrder(produceId) { orderParam ->
                        //发起验证
                        DataManager.googleValidate(orderParam.order_id, purchase.purchaseToken) {
                            if (it) {
                                //发放成功则消耗
                                consumePurchase(purchase)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 检查订阅订单掉单的情况
     */
    fun checkSubPurchased(client: BillingClient, produceId: Int) {
        val queryParam = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS).build()

        client.queryPurchasesAsync(queryParam) { _, purchaseList ->
            for (purchase in purchaseList) {
                JLog.i("GoogleOrderId = ${purchase.orderId}")
                JLog.i("GooglePurchaseState = ${purchase.purchaseState}")

                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                    //在自己服务器创建订单
                    DataManager.createOrder(produceId) { orderParam ->
                        //发起验证
                        DataManager.googleValidate(orderParam.order_id, purchase.purchaseToken) {
                            if (it) {
                                //确认订阅
                                acknowledgePurchase(purchase)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Google Pay
     * @param productId productId of Google Play
     */
    fun doGoogleFastPay(
        activity: Activity,
        productId: String,
        productType: String,
        orderId: Int,
        orderNo: String,
        uid: Int,
        callback: PayCallback
    ) {
        val listener = PurchasesUpdatedListener { billingResult, purchases ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    if (purchases != null) {
                        activity.runOnUiThread {
                            for (purchase in purchases) {
                                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                                    JLog.i("pay success")
                                    validatePay(orderId, purchase, productType, callback)
                                }
                            }
                        }
                    }
                }

                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    JLog.i("user cancel")
                }

                else -> {
                    JLog.i("unknown message")
                    val queryParam = QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.INAPP).build()

                    billingClient.queryPurchasesAsync(queryParam) { _, purchaseList ->
                        for (purchase in purchaseList) {
                            JLog.i("orderId = ${purchase.orderId}")
                            JLog.i("orderId = ${purchase.purchaseState}")
                            consumePurchase(purchase)
                        }
                    }
                }
            }
        }

        billingClient = BillingClient.newBuilder(activity)
            .setListener(listener)
            .enablePendingPurchases()
            .build()

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

                    val params = if (productType == "consume") {
                        BillingClient.ProductType.INAPP
                    } else {
                        BillingClient.ProductType.SUBS
                    }

                    val queryProductDetailsParams =
                        QueryProductDetailsParams.newBuilder()
                            .setProductList(
                                ImmutableList.of(
                                    QueryProductDetailsParams.Product.newBuilder()
                                        .setProductId(productId)
                                        .setProductType(params)
                                        .build()
                                )
                            )
                            .build()

                    billingClient.queryProductDetailsAsync(queryProductDetailsParams) { result, productDetailsList ->
                        // check billingResult, process returned productDetailsList
                        if (productDetailsList.isEmpty()) {
                            callback.failed("no goods found")
                            return@queryProductDetailsAsync
                        }

                        for ((index, productDetails) in productDetailsList.withIndex()) {
                            //purchase

                            activity.runOnUiThread {
                                if (productType == "acknowledge") {
                                    val offerDetails = productDetails.subscriptionOfferDetails
                                    if (offerDetails.isNullOrEmpty()) {
                                        callback.failed("purchase error")
                                        return@runOnUiThread
                                    }

                                    val offerToken = productDetails.subscriptionOfferDetails?.get(index)?.offerToken ?: ""
                                    val productDetailsParamsList = listOf(
                                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                            .setProductDetails(productDetails)
                                            .setOfferToken(offerToken)
                                            .build()
                                    )

                                    val flowParams = BillingFlowParams.newBuilder()
                                        .setProductDetailsParamsList(productDetailsParamsList)
                                        .setObfuscatedAccountId(uid.toString())
                                        .setObfuscatedProfileId(orderNo)
                                        .build()

                                    val responseCode = billingClient.launchBillingFlow(activity, flowParams).responseCode
                                    if (responseCode == BillingClient.BillingResponseCode.OK) {
                                        JLog.i("sku is normal")
                                    }
                                }
                            }

                            if (productType == "consume") {
                                val productDetailsParamsList = listOf(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                        .setProductDetails(productDetails)
                                        .setOfferToken("")
                                        .build()
                                )

                                val flowParams = BillingFlowParams.newBuilder()
                                    .setProductDetailsParamsList(productDetailsParamsList)
                                    .setObfuscatedAccountId(uid.toString())
                                    .setObfuscatedProfileId(orderNo)
                                    .build()

                                val responseCode = billingClient.launchBillingFlow(activity, flowParams).responseCode
                                if (responseCode == BillingClient.BillingResponseCode.OK) {
                                    JLog.i("sku is normal")
                                }
                            }
                        }
                    }

                } else {
                    JLog.i("error code = ${billingResult.responseCode},${billingResult.debugMessage}")
                    when (billingResult.responseCode) {
                        3 -> callback.failed("Unsupported country or region")
                    }
                }
            }
        })
    }

    @Synchronized
    private fun validatePay(
        orderId: Int,
        purchase: Purchase,
        productType: String,
        callback: PayCallback
    ) {
        //消耗商品
//        when (productType) {
//            "acknowledge" -> {
//                if (!purchase.isAcknowledged) {
//                    acknowledgePurchase(purchase, callback)
//                }
//            }
//
//            "consume" -> consumePurchase(purchase, callback)
//        }

        if (DoubleUtils.isFastDoubleClick()) return

        JLog.i("orderId = ${purchase.orderId}")

        DataManager.googleValidate(orderId, purchase.purchaseToken) {
            if (it) {
                callback.success(purchase.purchaseToken)

                when (productType) {
                    "acknowledge" -> {
                        if (!purchase.isAcknowledged) {
                            acknowledgePurchase(purchase)
                        } else {
                            callback.success(purchase.purchaseToken)
                        }
                    }

                    "consume" -> consumePurchase(purchase)
                }
            }
        }
    }

    private fun consumePurchase(purchase: Purchase) {
        //consume purchase
        val consumeParams =
            ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

        billingClient.consumeAsync(
            consumeParams
        ) { BillingResult, _ ->
            when (BillingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    val packName = purchase.packageName
                    val purchaseToken = purchase.purchaseToken
                    JLog.i("packName = $packName")
                    JLog.i("purchaseToken = $purchaseToken")
                    JLog.i("consume success")
                }
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        //subscription purchase
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)

        billingClient.acknowledgePurchase(acknowledgePurchaseParams.build()) {
            when (it.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    val packName = purchase.packageName
                    val purchaseToken = purchase.purchaseToken
                    JLog.i("packName = $packName")
                    JLog.i("purchaseToken = $purchaseToken")
                    JLog.i("acknowledge success")
                }
            }
        }
    }

}