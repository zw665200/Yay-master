package com.ql.recovery.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Server(
    val id: Int,
    val code: String,
    val count: Int,
    val discount: Float,
    val is_hot: Boolean,
    val name: String,
    var price: String,
    val icon: String,
    val type: String
) : Parcelable
