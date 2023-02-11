package com.ql.recovery.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * @author Herr_Z
 * @description:
 * @date : 2022/11/16 10:02
 */
@Parcelize
data class MatchConfig(
    var target_sex: Int,
    var country_name: String,
    var country_locale: String,
    var hand_free: Boolean
) : Parcelable
