package com.ql.recovery.bean

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * @author Herr_Z
 * @description:
 * @date : 2022/12/2 16:14
 */
@Entity(tableName = "subscriber")
@Parcelize
data class Subscriber(
    @PrimaryKey var uid: String,
    var online: Boolean
) : Parcelable
