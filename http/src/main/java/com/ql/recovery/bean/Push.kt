package com.ql.recovery.bean

data class Push(
    val call_push: Boolean,
    val chat_push: Boolean,
    val new_fans_push: Boolean,
    val online_push: Boolean
)
