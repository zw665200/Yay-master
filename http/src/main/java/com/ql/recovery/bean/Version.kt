package com.ql.recovery.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Version(
    val desc: String,
    val download_url: String,
    val is_force: Boolean,
    val version: String
) : Parcelable