package com.ql.recovery.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BeautyParam(
    var lighteningLevel: Float,
    var smoothnessLevel: Float,
    var rednessLevel: Float,
    var sharpnessLevel: Float
) : Parcelable
