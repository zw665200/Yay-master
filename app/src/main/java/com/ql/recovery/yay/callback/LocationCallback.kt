package com.ql.recovery.yay.callback

import android.location.Address

interface LocationCallback {
    fun onSuccess(address: Address)
    fun onFailed()
}