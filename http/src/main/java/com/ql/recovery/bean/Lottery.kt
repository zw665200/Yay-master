package com.ql.recovery.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * @author Herr_Z
 * @description:
 * @date : 2023/1/5 11:58
 */
data class Lottery(
    val coin: Int,
    val gift_name: String,
    val nickname: String,
    val type: String
)

data class LotteryGift(
    val id: Int,
    val coin: Int,
    val icon: String?,
    val name: String?,
    var type: String = "",
    var check: Boolean = false,
    var insert_or_draw: Boolean = true
)

@Parcelize
data class LotteryRecord(
    val id: Int,
    val coin: Int,
    val count: Int,
    val icon: String,
    var type: String,
    var avatar: String?
) : Parcelable

@Parcelize
data class LotteryRecords(
    val records: List<LotteryRecord>
) : Parcelable

@Parcelize
data class LotteryTicket(
    val count: Int,
    val room_id: String,
    val type: String,
    val target_uid: Int
) : Parcelable
