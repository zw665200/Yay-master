package com.ql.recovery.yay.ui.base

import android.app.Activity
import android.app.Dialog
import com.ql.recovery.bean.Addition
import com.ql.recovery.bean.BasePrice
import com.ql.recovery.bean.UserInfo
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.tencent.mmkv.MMKV

abstract class BaseDialog(
    private val activity: Activity,
) : Dialog(activity, R.style.app_dialog2) {
    private val mk = MMKV.defaultMMKV()

    protected fun getUserInfo(res: (UserInfo) -> Unit) {
        val userInfo = mk.decodeParcelable("user_info", UserInfo::class.java)
        if (userInfo == null) {
            DataManager.getUserInfo {
                res(it)
            }
        } else {
            res(userInfo)
        }
    }

    protected fun getBasePrice(res: (BasePrice) -> Unit) {
        val basePrice = mk.decodeParcelable("base_price", BasePrice::class.java)
        if (basePrice != null) {
            res(basePrice)
            return
        }

        DataManager.getBasePrice {
            mk.encode("base_price", it)
            res(it)
        }
    }

    protected fun getLocalStorage(): MMKV {
        return mk
    }
}