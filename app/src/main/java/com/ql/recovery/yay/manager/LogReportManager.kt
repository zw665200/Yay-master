package com.ql.recovery.yay.manager

import android.os.Build
import com.ql.recovery.bean.VLog
import com.ql.recovery.bean.Value
import com.ql.recovery.config.Config
import com.ql.recovery.manager.DataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

/**
 * @author Herr_Z
 * @description:
 * @date : 2021/8/18 10:35
 */
object LogReportManager : CoroutineScope by MainScope() {

    /**
     * 用户行为类型 1:点击了功能 2:点击某功能的开始处理, 3:点击了广告， 4： 点击了加入会员，
     * 5：点击立即购买 6：已支付 7： 服务评价 8：质量检测 9：点击了人工服务 10：点击分享"`
     */
    fun logReport(action: String, type: LogType) {
        if (Config.CLIENT_TOKEN == "") return

        val t = when (type) {
            LogType.CLICK -> 1
            LogType.TASK -> 2
            LogType.AD -> 3
            LogType.MEMBER -> 4
            LogType.PURCHASE -> 5
            LogType.PURCHASED -> 6
            LogType.SERVICE_COMMEND -> 7
            LogType.QUALITY -> 8
            LogType.CUSTOMER_SERVICE -> 9
            LogType.SHARE -> 10
        }

        if (type == LogType.CLICK || type == LogType.TASK) {
            val log = VLog(t, "Android ${Build.VERSION.RELEASE}", Value(action))
            DataManager.addLog(log) {}
        } else {
            val log = VLog(t, "Android ${Build.VERSION.RELEASE}", Value(null, action))
            DataManager.addLog(log) {}
        }

    }

    /**
     * 用户行为类型 1:点击了功能 2:点击某功能的开始处理, 3:点击了广告， 4： 点击了加入会员，
     * 5：点击立即购买 6：已支付 7： 服务评价 8：质量检测 9：点击了人工服务 10：点击分享"`
     */
    fun logReport(action: String, value: String, type: LogType) {
        if (Config.CLIENT_TOKEN == "") return

        val t = when (type) {
            LogType.CLICK -> 1
            LogType.TASK -> 2
            LogType.AD -> 3
            LogType.MEMBER -> 4
            LogType.PURCHASE -> 5
            LogType.PURCHASED -> 6
            LogType.SERVICE_COMMEND -> 7
            LogType.QUALITY -> 8
            LogType.CUSTOMER_SERVICE -> 9
            LogType.SHARE -> 10
        }

        val log = VLog(t, "Android ${Build.VERSION.RELEASE}", Value(action, value))

        DataManager.addLog(log) {}
    }

    enum class LogType {
        CLICK, TASK, AD, MEMBER, PURCHASE, PURCHASED, SERVICE_COMMEND, QUALITY, CUSTOMER_SERVICE, SHARE
    }
}