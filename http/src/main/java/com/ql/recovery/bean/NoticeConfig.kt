package com.ql.recovery.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class NoticeConfig(
    var openVideo: Boolean = true,
    var openMessage: Boolean = true,
    var openAddition: Boolean = true,
    var openFollow: Boolean = true
) : Parcelable