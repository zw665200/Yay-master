package com.ql.recovery.yay.ui.self

import com.netease.nimlib.sdk.msg.attachment.MsgAttachment
import com.netease.nimlib.sdk.msg.attachment.MsgAttachmentParser
import com.netease.yunxin.kit.corekit.im.custom.CustomAttachParser
import com.ql.recovery.bean.MsgInfo
import com.ql.recovery.config.Config
import com.ql.recovery.yay.util.GsonUtils
import com.ql.recovery.yay.util.JLog
import com.tencent.mmkv.MMKV
import org.json.JSONObject


/**
 * @author Herr_Z
 * @description:
 * @date : 2022/12/2 18:32
 */
class SelfCustomAttachParser private constructor() : MsgAttachmentParser {
    private val mk = MMKV.defaultMMKV()

    companion object {
        private const val KEY_TYPE = "type"
        private const val KEY_DATA = "content"

        fun packData(type: Int, data: JSONObject?): String {
            val obj = JSONObject()
            try {
                obj.put(KEY_TYPE, type)
                if (data != null) {
                    obj.put(KEY_DATA, data)
                }
            } catch (exception: java.lang.Exception) {
            }
            return obj.toString()
        }


        @Volatile
        private var ins: SelfCustomAttachParser? = null

        fun get(): SelfCustomAttachParser {
            if (ins == null) {
                synchronized(SelfCustomAttachParser::class) {
                    ins = SelfCustomAttachParser()
                }
            }

            return ins!!
        }

    }

    private val attachmentMap: MutableMap<Int, Class<out SelfCustomAttachment>> = HashMap()

    override fun parse(json: String?): MsgAttachment? {
        var attachment: SelfCustomAttachment? = null
        try {
            val obj = JSONObject(json)
            val type: Int = obj.getInt(KEY_TYPE)
            val data: JSONObject = obj.getJSONObject(KEY_DATA)
            if (attachmentMap.containsKey(type)) {
                attachment = attachmentMap[type]!!.newInstance()
                attachment?.fromJson(data)
            } else {
                JLog.i("attachment json = $json")
                val message = GsonUtils.fromJson(json, MsgInfo::class.java)
                if (message != null) {
                    mk.encode("custom_json", json)
                }
            }

        } catch (e: java.lang.Exception) {
        }
        return attachment
    }

    fun <T : SelfCustomAttachment?> addCustomAttach(type: Int, attachment: Class<out SelfCustomAttachment>) {
        attachmentMap[type] = attachment
    }

    fun removeCustomAttach(type: Int) {
        attachmentMap.remove(type)
    }

    private object CustomAttachParserHolder {
        val instance = CustomAttachParser()
    }

}