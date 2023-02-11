package com.ql.recovery.bean

/**
 * @author Herr_Z
 * @description:
 * @date : 2021/3/26 15:49
 */
data class OssParam(
    var endpoint: String,
    var bucket_name: String,
    var access_key_id: String,
    var scheme: String,
    var access_key_secret: String,
    var security_token: String
)