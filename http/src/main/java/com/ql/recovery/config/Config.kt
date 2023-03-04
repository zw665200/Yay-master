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
    var NIM_CERTIFICATE_NAME = "yay push"

    //umeng key
    var umengKey = "63f86e9bd64e68613937cfd8"

    //appsflyer
    var APPS_FLYER_KEY = "DPLpZK4xzXEXfi9gcgGgpm"
}