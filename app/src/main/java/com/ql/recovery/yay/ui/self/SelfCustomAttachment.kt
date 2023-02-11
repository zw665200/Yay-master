package com.ql.recovery.yay.ui.self

import com.netease.nimlib.sdk.msg.attachment.MsgAttachment
import com.netease.yunxin.kit.corekit.im.IMKitClient.getApplicationContext
import com.netease.yunxin.kit.corekit.im.custom.CustomAttachment
import com.ql.recovery.yay.R
import org.json.JSONObject


/**
 * @author Herr_Z
 * @description:
 * @date : 2022/12/2 18:26
 */
abstract class SelfCustomAttachment constructor(private val type: Int) : MsgAttachment {

    fun fromJson(data: JSONObject?) {
        if (data != null) {
            parseData(data)
        }
    }

    override fun toJson(send: Boolean): String? {
        return SelfCustomAttachParser.packData(type, packData())
    }

    fun getType(): Int {
        return type
    }

    fun getContent(): String {
        return getApplicationContext().getString(R.string.chat_reply_message_brief_custom)
    }

    protected abstract fun parseData(data: JSONObject?)

    protected abstract fun packData(): JSONObject?
}