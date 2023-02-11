package com.ql.recovery.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserInfo(
    var id: Int,
    var uid: Int,
    var email: String?,
    var avatar: String,
    var nickname: String,
    var sex: Int,
    var age: Int,
    var country: String,
    var follow_status: Int,
    var coin: Int,
    var coin_income: Int,
    var role: String,
    var followers: Int,
    var following: Int,
    var photos: List<String>,
    var videos: List<String>,
    var grade: Int,
    var tags: List<Tag>,
    var grace_score: Float,
    var is_vip: Boolean,
    var online: Boolean = false,
    var is_unlock: Boolean = false,
    var date: Long?
) : Parcelable
