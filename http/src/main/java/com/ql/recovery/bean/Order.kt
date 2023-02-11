package com.ql.recovery.bean

data class Order(
    var money: String,
    var name: String,
    var period_type: Int,
    var time: String
)

data class OrderParam(
    var order_id: Int,
    var order_no: String,
    var is_paid: Boolean
)
