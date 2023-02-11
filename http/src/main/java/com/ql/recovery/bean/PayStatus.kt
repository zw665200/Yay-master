package com.ql.recovery.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PayStatus(
    var pay_way: String,
    var period_type: String,
    var price: String,
    var time: String
) : Parcelable
