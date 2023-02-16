package com.ql.recovery.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * @author Herr_Z
 * @description:
 * @date : 2022/11/16 10:02
 */
data class MessageInfo<T>(
    val type: String,
    val sender: Int?,
    val recipient: Int?,
    val content: T
)

data class MsgInfo(
    val type: String
)

@Parcelize
data class User(
    val uid: Int,
    val avatar: String,
    val nickname: String,
    val sex: Int,
    val age: Int,
    val tags: List<Tag>?,
    val date: Long,
    val country: String,
    var follow_status: Int,
    var cover_url: String
) : Parcelable

@Parcelize
data class Room(
    val room_id: String,
    val token: String,
    val duration: Long,
    val start_at: Long,
    var `package`: Addition?
) : Parcelable

@Parcelize
data class Tag(
    val id: Int,
    val name: String
) : Parcelable

@Parcelize
data class Reason(
    val reason: String
) : Parcelable

data class Attachment(
    val type: String,
    val content: Any
) : java.io.Serializable
