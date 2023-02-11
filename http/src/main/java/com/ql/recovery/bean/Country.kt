package com.ql.recovery.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * @author Herr_Z
 * @description:
 * @date : 2022/11/10 10:02
 */
@Parcelize
data class Countries(
    val country: List<Country>
) : Parcelable

@Parcelize
data class Country(
    val code: Int,
    val tw: String,
    val en: String,
    val locale: String,
    val zh: String,
    val lat: Float,
    val lng: Float,
    val emoji: String,
    var check: Boolean = false
) : Parcelable
