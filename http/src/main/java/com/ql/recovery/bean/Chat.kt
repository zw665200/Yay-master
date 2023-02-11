package com.ql.recovery.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Chat(
    var id: String,
    var name: String,
    var content: String,
    var created_at: Long,
    var is_read: Boolean
) : Parcelable