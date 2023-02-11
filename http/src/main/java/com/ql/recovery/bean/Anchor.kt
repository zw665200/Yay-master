package com.ql.recovery.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Anchor(
    var age: Int,
    var avatar: String,
    var country: String,
    var cover_type: String,
    var cover_url: String?,
    var uid: Int,
    var nickname: String,
    var sex: Int,
    var online: Boolean = false,
    var isPlaying: Boolean = false
) : Parcelable

/**
 * avatar string 头像
cost integer 价值
duration integer 通话总时长
icon string 礼物图标
number integer 个数
total_cost integer 总价值
total_income integer 总收益
uid	integer 目标用户I
 */
data class Income(
    var avatar: String,
    var cost: Int,
    var duration: Long,
    var icon: String,
    var number: Int,
    var total_cost: Int,
    var total_income: Int,
    var uid: Int
)

data class ReChargeRecord(
    var amount: Int,
    var date: Long
)
