package com.ql.recovery.yay.callback

interface FileCallback {
    fun onSuccess(filePath: String)
    fun onFailed(message: String)
}