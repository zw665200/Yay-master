package com.ql.recovery.bean

data class Resource(
    val type: String, val icon: Int, val name: String
)

data class ExpRes(
    val type: String, val count: List<Int>, val name: List<String>
)

data class BeautyResource(
    val type: String, val before: Int, val after: Int, val name: String
)