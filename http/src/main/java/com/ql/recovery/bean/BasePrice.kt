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
    val match_filter: MatchFilter,
    val service_charge: ServiceCharge,
    val lottery: LotteryService
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
data class ServiceCharge(
    val normal: NormalService,
    val anchor: AnchorService,
) : Parcelable

@Parcelize
data class NormalService(
    val game: Float,
    val gift: Float,
    val video: Float
) : Parcelable

@Parcelize
data class AnchorService(
    val game: Float,
    val gift: Float,
    val video: Float
) : Parcelable

@Parcelize
data class LotteryService(
    val coin: TimeService,
    val gift: TimeService
) : Parcelable

@Parcelize
data class TimeService(
    val one_time: Int,
    val ten_times: Int
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
