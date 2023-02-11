package com.ql.recovery.yay.manager

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.common.collect.ImmutableList
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.callback.PayCallback
import com.ql.recovery.yay.config.WalletConstant.LOAD_PAYMENT_DATA_REQUEST_CODE
import com.ql.recovery.yay.config.WalletConstant.SHIPPING_COST_CENTS
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
     * 检查会员权限
     * @param pay
     */
    fun checkPay(context: Context, pay: (Boolean) -> Unit) {

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
     * Google Pay
     * @param productId productId of Google Play
     */
    fun doGoogleFastPay(
        activity: Activity,
        productId: String,
        productType: String,
        orderId: Int,
        callback: PayCallback
    ) {
        var currentOrderId: String? = null
        val listener = PurchasesUpdatedListener { billingResult, purchases ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    JLog.i("pay success")
                    if (purchases != null) {
                        activity.runOnUiThread {
                            for (purchase in purchases) {
                                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                                    //多次触发的应对策略
                                    if (currentOrderId == purchase.orderId) return@runOnUiThread

                                    JLog.i("orderId = ${purchase.orderId}")
                                    currentOrderId = purchase.orderId
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
                            consumePurchase(purchase, callback)
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
                        // check billingResult
                        // process returned productDetailsList
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

                                    val offerToken =
                                        productDetails.subscriptionOfferDetails?.get(index)?.offerToken
                                            ?: ""
                                    val productDetailsParamsList = listOf(
                                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                            // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                            .setProductDetails(productDetails)
                                            // to get an offer token, call ProductDetails.subscriptionOfferDetails()
                                            // for a list of offers that are available to the user
                                            .setOfferToken(offerToken)
                                            .build()
                                    )

                                    val flowParams = BillingFlowParams.newBuilder()
                                        .setProductDetailsParamsList(productDetailsParamsList)
                                        .build()

                                    val responseCode = billingClient.launchBillingFlow(
                                        activity,
                                        flowParams
                                    ).responseCode
                                    if (responseCode == BillingClient.BillingResponseCode.OK) {
                                        JLog.i("sku is normal")
                                    }
                                }
                            }

                            if (productType == "consume") {
                                val productDetailsParamsList = listOf(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                        // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                        .setProductDetails(productDetails)
                                        // to get an offer token, call ProductDetails.subscriptionOfferDetails()
                                        // for a list of offers that are available to the user
                                        .setOfferToken("")
                                        .build()
                                )

                                val flowParams = BillingFlowParams.newBuilder()
                                    .setProductDetailsParamsList(productDetailsParamsList)
                                    .build()

                                val responseCode = billingClient.launchBillingFlow(
                                    activity,
                                    flowParams
                                ).responseCode
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

    private fun validatePay(
        orderId: Int,
        purchase: Purchase,
        productType: String,
        callback: PayCallback
    ) {
        DataManager.googleValidate(orderId, purchase.purchaseToken) {
            if (it) {
                when (productType) {
                    "acknowledge" -> {
                        if (!purchase.isAcknowledged) {
                            acknowledgePurchase(purchase, callback)
                        } else {
                            callback.success(purchase.purchaseToken)
                        }
                    }

                    "consume" -> consumePurchase(purchase, callback)
                }
            }
        }
    }

    private fun consumePurchase(purchase: Purchase, callback: PayCallback) {
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
                    callback.success(purchaseToken)
                }
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase, callback: PayCallback) {
        //subscription purchase
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
        billingClient.acknowledgePurchase(
            acknowledgePurchaseParams.build()
        ) {
            when (it.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    val packName = purchase.packageName
                    val purchaseToken = purchase.purchaseToken
                    JLog.i("packName = $packName")
                    JLog.i("purchaseToken = $purchaseToken")
                    JLog.i("acknowledge success")
                    callback.success(purchaseToken)
                }
            }
        }
    }

}