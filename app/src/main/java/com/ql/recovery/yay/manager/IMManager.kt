package com.ql.recovery.yay.manager

import android.content.Context
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallbackWrapper
import com.netease.nimlib.sdk.ResponseCode
import com.netease.nimlib.sdk.event.EventSubscribeService
import com.netease.nimlib.sdk.event.model.EventSubscribeRequest
import com.ql.recovery.bean.Subscriber
import com.ql.recovery.yay.util.JLog

/**
 * @author Herr_Z
 * @description:
 * @date : 2022/12/1 17:24
 */
object IMManager {

    fun subscribe(context: Context, uidList: List<String>) {
        for (uid in uidList) {
            val subscriber = Subscriber(uid, false)
            DBManager.insert(context, subscriber)
        }

        val eventSubscribeRequest = EventSubscribeRequest()
        eventSubscribeRequest.eventType = 1
        eventSubscribeRequest.expiry = 30 * 24 * 3600L
        eventSubscribeRequest.publishers = uidList
        eventSubscribeRequest.isSyncCurrentValue = true
        NIMClient.getService(EventSubscribeService::class.java).subscribeEvent(eventSubscribeRequest)
            .setCallback(object : RequestCallbackWrapper<List<String>>() {
                override fun onResult(code: Int, result: List<String>?, exception: Throwable?) {
                    if (code == ResponseCode.RES_SUCCESS.toInt()) {

                    }
                }
            })
    }

    fun subscribe(context: Context, uid: String) {
        val subscriber = Subscriber(uid, false)
        DBManager.insert(context, subscriber)

        val list = arrayListOf(uid)
        val eventSubscribeRequest = EventSubscribeRequest()
        eventSubscribeRequest.eventType = 1
        eventSubscribeRequest.expiry = 30 * 24 * 3600L
        eventSubscribeRequest.publishers = list
        NIMClient.getService(EventSubscribeService::class.java).subscribeEvent(eventSubscribeRequest)
            .setCallback(object : RequestCallbackWrapper<List<String>>() {
                override fun onResult(code: Int, result: List<String>?, exception: Throwable?) {
                    if (code == ResponseCode.RES_SUCCESS.toInt()) {

                    }
                }
            })
    }

    fun unsubscribe(cidList: List<String>) {
        val eventSubscribeRequest = EventSubscribeRequest()
        eventSubscribeRequest.eventType = 1
        eventSubscribeRequest.publishers = cidList
        NIMClient.getService(EventSubscribeService::class.java).unSubscribeEvent(eventSubscribeRequest)
    }

    fun querysubscribe(cidList: List<String>) {
        val eventSubscribeRequest = EventSubscribeRequest()
        eventSubscribeRequest.eventType = 1
        eventSubscribeRequest.publishers = cidList
        val eventSubscribeResult = NIMClient.getService(EventSubscribeService::class.java).querySubscribeEvent(eventSubscribeRequest)
    }

    fun checkOnlineStatus(context: Context, uidList: List<String>, func: (List<Subscriber>) -> Unit) {
        DBManager.find(context, uidList) { subList ->
            val list = arrayListOf<Subscriber>()
            for (uid in uidList) {
                for (subscriber in subList) {
                    if (uid == subscriber.uid) {
                        list.add(subscriber)
                    }
                }
            }
            func(list)
        }
    }

    fun checkOnlineStatus(context: Context, uid: String, func: (Subscriber) -> Unit) {
        DBManager.find(context, uid) { subscriber ->
            if (subscriber != null) {
                func(subscriber)
            }
        }
    }
}