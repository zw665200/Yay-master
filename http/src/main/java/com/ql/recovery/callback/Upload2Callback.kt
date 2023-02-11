package com.ql.recovery.callback

interface Upload2Callback {
    fun onSuccess(pathList: List<String>)
    fun onFailed(msg: String)
}