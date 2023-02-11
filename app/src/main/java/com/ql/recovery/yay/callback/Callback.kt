package com.ql.recovery.yay.callback

interface Callback {
    fun onSuccess()
    fun onFailed(msg: String)
}