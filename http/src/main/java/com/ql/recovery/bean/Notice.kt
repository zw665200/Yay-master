package com.ql.recovery.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Notice(
    var id: String,
    var title: String,
    var content: String,
    var created_at: String,
    var is_read: Boolean
) : Parcelable