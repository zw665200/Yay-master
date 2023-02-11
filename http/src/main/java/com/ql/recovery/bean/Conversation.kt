package com.ql.recovery.bean

/**
 * @author Herr_Z
 * @description:
 * @date : 2023/2/1 12:08
 */
data class Conversation(
    var sessionId: String,
    var nickName: String,
    var avatar: String,
    var date: Long,
    var msg: String,
    var unread_count: Int,
    var online: Boolean = false
)
