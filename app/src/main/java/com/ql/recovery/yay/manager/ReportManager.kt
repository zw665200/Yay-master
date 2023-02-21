package com.ql.recovery.yay.manager

import android.content.Context
import android.os.Bundle
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AFInAppEventType
import com.appsflyer.AppsFlyerLib
import com.facebook.appevents.AppEventsConstants
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.analytics.FirebaseAnalytics
import com.ql.recovery.config.Config
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.util.*
import java.math.BigDecimal
import java.util.*

/**
 * @author Herr_Z
 * @description:
 * @date : 2023/2/15 11:00
 */
object ReportManager {

    fun firebaseItemLog(firebaseAnalytics: FirebaseAnalytics, itemId: String, itemName: String, itemType: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemId)
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, itemName)
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, itemType)

        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, bundle)
    }

    fun firebaseLoginLog(firebaseAnalytics: FirebaseAnalytics, uid: Int, name: String) {
        val bundle = Bundle()
        bundle.putInt("uid", uid)
        bundle.putString("nickname", name)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)
    }

    fun firebasePurchaseLog(firebaseAnalytics: FirebaseAnalytics, currency: String, value: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.CURRENCY, currency)
        bundle.putString(FirebaseAnalytics.Param.VALUE, value)
        bundle.putString(FirebaseAnalytics.Param.AFFILIATION, "Google Play")
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.PURCHASE, bundle)
    }

    fun facebookItemLog(context: Context, itemId: String, itemName: String, itemType: String, type: String) {
        val logger = AppEventsLogger.newLogger(context)
        val bundle = Bundle()
        bundle.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, itemId)
        bundle.putString(AppEventsConstants.EVENT_PARAM_CONTENT, itemName)
        bundle.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, itemType)
        logger.logEvent(type, bundle)
    }


    fun facebookLoginLog(context: Context, uid: Int, name: String) {
        val logger = AppEventsLogger.newLogger(context)
        val bundle = Bundle()
        bundle.putInt("uid", uid)
        bundle.putString("nickname", name)
        logger.logEvent("login", bundle)
    }

    fun facebookPurchaseLog(context: Context, currencyCode: String, value: String) {
        val logger = AppEventsLogger.newLogger(context)
        val currency = Currency.getInstance(Locale.getDefault())
        val bundle = Bundle()
        bundle.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, currencyCode)
        bundle.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "Google Play")
        logger.logPurchase(BigDecimal(value), currency, bundle)
    }

    fun branchItemLog(context: Context, alias: String, itemId: String, itemName: String) {
        BranchEvent(BRANCH_STANDARD_EVENT.VIEW_ITEM)
            .setCustomerEventAlias(alias)
            .setDescription(itemId + itemName)
            .logEvent(context)
    }

    fun branchLoginLog(context: Context, uid: Int, name: String) {
        BranchEvent(BRANCH_STANDARD_EVENT.LOGIN)
            .setCustomerEventAlias(name)
            .setTransactionID(uid.toString())
            .logEvent(context)
    }

    fun branchPurchaseLog(context: Context, productId: String, currencyCode: String, value: String) {
        val buo = BranchUniversalObject()
            .setTitle("Google Play")
            .setContentMetadata(
                ContentMetadata()
                    .setPrice(value.toDouble(), CurrencyType.USD)
                    .setQuantity(1.0)
                    .setProductName(productId)
                    .setContentSchema(BranchContentSchema.COMMERCE_PRODUCT)
            )
            .addKeyWord(currencyCode)
            .addKeyWord(value)

        BranchEvent(BRANCH_STANDARD_EVENT.PURCHASE)
            .setCurrency(CurrencyType.USD)
            .addContentItems(buo)
            .logEvent(context)
    }

    fun branchCustomLog(
        context: Context,
        customName: String,
        customMap: HashMap<String, String>?
    ) {
        val event = BranchEvent(customName)
        if (customMap.isNullOrEmpty()) {
            event.logEvent(context)
            return
        }

        for (item in customMap.iterator()) {
            event.addCustomDataProperty(item.key, item.value)
        }

        event.logEvent(context)
    }

    fun appsFlyerCustomLog(context: Context, customName: String, uid: Int) {
        val eventValues = HashMap<String, Any>()
        eventValues[AFInAppEventParameterName.CUSTOMER_USER_ID] = uid
        AppsFlyerLib.getInstance().logEvent(context.applicationContext, customName, eventValues)
    }

    fun appsFlyerLoginLog(context: Context, uid: Int) {
        val eventValues = HashMap<String, Any>()
        eventValues[AFInAppEventParameterName.CUSTOMER_USER_ID] = uid
        AppsFlyerLib.getInstance().logEvent(context.applicationContext, AFInAppEventType.LOGIN, eventValues)
    }

    /**
     * appsFlyer pay
     */
    fun appsFlyerPurchaseLog(context: Context, contentType: String, currencyValue: String) {
        val eventValues = HashMap<String, Any>()
        eventValues[AFInAppEventParameterName.PURCHASE_CURRENCY] = "USD"
        eventValues[AFInAppEventParameterName.PRICE] = currencyValue
        eventValues[AFInAppEventParameterName.CUSTOMER_USER_ID] = Config.USER_ID
        eventValues[AFInAppEventParameterName.CONTENT_TYPE] = contentType
        AppsFlyerLib.getInstance().logEvent(context.applicationContext, AFInAppEventType.PURCHASE, eventValues)
    }


}