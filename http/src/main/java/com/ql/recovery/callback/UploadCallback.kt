package com.ql.recovery.callback

interface UploadCallback {
    fun onSuccess(path: String)
    fun onFailed(msg: String)
}