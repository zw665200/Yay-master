package com.ql.recovery.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VLog(
    var behavior_type: Int,
    var os: String,
    var value: Value
) : Parcelable

@Parcelize
data class Value(
    var action: String? = null,
    var value: String? = null
) : Parcelable