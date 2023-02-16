package com.ql.recovery.yay.manager

import android.content.Context
import android.os.Bundle
import com.facebook.appevents.AppEventsConstants
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.analytics.FirebaseAnalytics
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

    fun branchItemLog(context: Context, itemId: String, itemName: String, itemType: String, type: String) {
        BranchEvent(BRANCH_STANDARD_EVENT.VIEW_ITEM)
            .setCustomerEventAlias(type)
            .setDescription(itemId + itemName)
            .addCustomDataProperty("item_type", itemType)
            .addCustomDataProperty("type", type)
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
}