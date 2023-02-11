package com.ql.recovery.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * @author Herr_Z
 * @description:
 * @date : 2022/12/7 18:40
 */
@Parcelize
data class BasePrice(
    val common: Common,
    val match_filter: MatchFilter
) : Parcelable

@Parcelize
data class Common(
    val game_unlock: Int,
    val private_video: Int
) : Parcelable

@Parcelize
data class MatchFilter(
    val country_cost: Int,
    val sex_cost: Int,
    val vip_discounts: Float
) : Parcelable

@Parcelize
data class Addition(
    val id: Int,
    val cost: Int,
    val duration: Int,
    val type: String,
    val room_type: String,
    val vip_discount: Float,
    var check: Boolean = false
) : Parcelable
