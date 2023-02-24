package com.ql.recovery.config

import android.os.Handler

object Config {
    var isDebug = true

    //MatchActivity
    var messageHandler: Handler? = null

    //home页刷新数据
    var mainHandler: Handler? = null

    //订阅
    var subscriberHandler: Handler? = null

    //刷新房间
    var roomHandler: Handler? = null

    //全局
    var mHandler: Handler? = null

    var baseHandler: Handler? = null

    var startAt = 0L
    var leaveAt = 0L

    var ROM = ""
    const val ROM_MIUI = "MIUI"
    const val ROM_EMUI = "EMUI"
    var CLIENT_TOKEN = ""
    var USER_NAME: String? = null
    var USER_ID = 0

    var CHANNEL_ID = 10
    var PICTURE_PATH = "/Pictures/"
    var PERMISSION_CAMERA_REQUEST_CODE = 0x00000011
    var CAMERA_REQUEST_CODE = 0x00000012

    //Realm
    var ROOM_DB_NAME = "common"

    //Bugly
    var BUGLY_APPID = "0dc1cc3833"

    //Agora
    var AGORA_APP_ID = "575a6f4277884019957df850eae7a610"

    //NetEasy IM
    var NIM_APP_KEY = "db7ef9b2ee547446080d64c2dd65f387"
    var NIM_APP_SECRET = "9ac8ed8c99a5"

    //umeng key
    var umengKey = "63f86e9bd64e68613937cfd8"

    //appsflyer
    var APPS_FLYER_KEY = "DPLpZK4xzXEXfi9gcgGgpm"

    //百度01 3
    //头条 4
    //UC 5
    //搜狗 6
    //HUAWEI 7
    //VIVO 8
    //应用宝 9
    //OPPO 10
    //小米 11
    //百度02 12
    //百度03 13
    fun getChannelName(channelId: Int): String {
        var channelName = "unknown"
        when (channelId) {
            3 -> channelName = "百度01"
            7 -> channelName = "华为"
            8 -> channelName = "VIVO"
            10 -> channelName = "OPPO"
            12 -> channelName = "百度02"
            13 -> channelName = "百度03"
        }
        return channelName
    }
}