package com.ql.recovery.bean

/**
 * @author Herr_Z
 * @description:
 * @date : 2022/11/16 10:10
 */
data class Template(
    val id: Int,
    val subtitle: String,
    val title: String,
    var check: Boolean = false
)
