package com.ql.recovery.bean

/**
 * @author Herr_Z
 * @description:
 * @date : 2022/12/30 10:54
 */
data class Notification(
    val content: String,
    val created_at: Long,
    val event: String,
    val id: Int
)
