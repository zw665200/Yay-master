package com.ql.recovery.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * @author Herr_Z
 * @description:
 * @date : 2022/11/30 14:28
 */
@Parcelize
data class IMToken(
    val accid: String,
    val token: String
) : Parcelable
