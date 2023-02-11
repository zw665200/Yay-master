package com.ql.recovery.yay.callback

interface PayCallback {
    fun success(token: String)
    fun failed(msg: String)
}