package com.ql.recovery.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * @author Herr_Z
 * @description:
 * @date : 2022/12/6 14:06
 */
@Parcelize
data class Gift(
    val coin: Int,
    val grade: Int,
    val icon: String,
    val id: Int,
    val name: String,
    val animation_url: String
) : Parcelable
