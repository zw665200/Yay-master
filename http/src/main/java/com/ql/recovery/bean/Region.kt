package com.ql.recovery.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * @author Herr_Z
 * @description:
 * @date : 2022/11/10 10:02
 */
@Parcelize
data class Region(
    val id: Int,
    val name: String,
    val phone_code: String,
    val iso: String,
    var type: String
) : Parcelable

@Parcelize
data class Regions(
    val list: List<Region>
) : Parcelable
