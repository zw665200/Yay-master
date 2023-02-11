package com.ql.recovery.yay.util

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import com.ql.recovery.yay.ui.LiveFragment

/**
@author ZW
@description:
@date : 2020/11/24 15:46
 */
class LivePermissions {

    companion object {
        const val TAG = "permissions"
    }

    constructor(activity: FragmentActivity) {
        fLive = getInstance(activity.supportFragmentManager)
    }

    constructor(fragment: Fragment) {
        fLive = getInstance(fragment.childFragmentManager)
    }

    @Volatile
    private var fLive: LiveFragment? = null

    private fun getInstance(fragmentManager: FragmentManager) =
        fLive ?: synchronized(this) {
            fLive ?: if (fragmentManager.findFragmentByTag(TAG) == null) LiveFragment()
                .run {
                    fragmentManager.beginTransaction().add(
                        this,
                        TAG
                    ).commitNow()
                    this
                } else fragmentManager.findFragmentByTag(TAG) as LiveFragment
        }

    fun request(permissions: Array<out String>): MutableLiveData<PermissionResult> {
        return this.requestArray(permissions)
    }

    private fun requestArray(permissions: Array<out String>): MutableLiveData<PermissionResult> {
        fLive!!.requestPermissions(permissions)
        return fLive!!.liveData
    }

}
