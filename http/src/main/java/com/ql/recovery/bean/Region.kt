package com.ql.recovery.bean

/**
 * @author Herr_Z
 * @description:
 * @date : 2022/11/10 10:02
 */
data class Region(
    val id: Int,
    val name: String,
    val phone_code: String,
    val iso: String,
    var type: String
)
