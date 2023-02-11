package com.ql.recovery.http.response

data class Response<T>(
    var code: Int = 0,
    var message: String? = null,
    var data: T? = null
)