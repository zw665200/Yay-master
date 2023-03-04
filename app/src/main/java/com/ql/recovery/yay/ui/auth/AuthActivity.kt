package com.ql.recovery.yay.ui.auth

import android.Manifest
import android.content.Intent
import android.location.Address
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.callback.LocationCallback
import com.ql.recovery.yay.databinding.ActivityAuthBinding
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.manager.ReportManager
import com.ql.recovery.yay.ui.MainActivity
import com.ql.recovery.yay.ui.base.BaseActivity
import com.ql.recovery.yay.util.AppUtil
import com.ql.recovery.yay.util.ToastUtil

class AuthActivity : BaseActivity() {
    private lateinit var binding: ActivityAuthBinding
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun getViewBinding(baseBinding: ActivityBaseBinding) {
        binding = ActivityAuthBinding.inflate(layoutInflater, baseBinding.flBase, true)
    }

    override fun initView() {
        binding.includeTitle.tvName.text = getString(R.string.permission_title)
        binding.includeTitle.ivBack.setOnClickListener { finish() }
        binding.tvAuth.setOnClickListener { checkPermission() }
        binding.tvCancel.setOnClickListener { toMainPage() }
    }

    override fun initData() {
        firebaseAnalytics = Firebase.analytics
    }

    private fun checkPermission() {
        if (!binding.agreementCheck.isChecked) {
            ToastUtil.showShort(this, getString(R.string.permission_agree))
            return
        }

        val list = arrayListOf<String>()
        if (binding.checkboxCamera.isChecked) {
            list.add(Manifest.permission.CAMERA)
        }

        if (binding.checkboxMicro.isChecked) {
            list.add(Manifest.permission.RECORD_AUDIO)
        }

        if (binding.checkboxLocation.isChecked) {
            list.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            list.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (binding.checkboxNotice.isChecked) {

        }

        if (list.isEmpty()) {
            ToastUtil.showShort(this, getString(R.string.permission_choose))
        }

        getUserInfo { userInfo ->
            if (userInfo.country.isBlank()) {
                checkLocationPermissions {
                    when (it) {
                        "grant" -> {
                            getLocation()
                            checkVideoPermissions()
                        }

                        "rationale", "deny" -> {
                            checkVideoPermissions()
                        }
                    }
                }
            } else {
                checkVideoPermissions()
            }
        }
    }

    /**
     * 定位设置国家和地区
     */
    private fun getLocation() {
        AppUtil.getLocation(this, object : LocationCallback {
            override fun onSuccess(address: Address) {
                DataManager.updateCountry(address.countryCode) {}

                val permission = getLocalStorage().decodeBool("home_get_location_success", false)
                if (!permission) {
                    ReportManager.firebaseCustomLog(firebaseAnalytics, "home_get_location_success", "get location success")
                    ReportManager.appsFlyerCustomLog(this@AuthActivity, "home_get_location_success", "get location success")
                }
            }

            override fun onFailed() {
                runOnUiThread {
                    ToastUtil.showShort(this@AuthActivity, "get location failed, please check your network")

                    val permission = getLocalStorage().decodeBool("home_get_location_failed", false)
                    if (!permission) {
                        ReportManager.firebaseCustomLog(firebaseAnalytics, "home_get_location_failed", "get location failed")
                        ReportManager.appsFlyerCustomLog(this@AuthActivity, "home_get_location_failed", "get location failed")
                    }
                }
            }
        })
    }

    private fun checkVideoPermissions() {
        checkVideoPermissions {
            getLocalStorage().encode("show_permission", true)
            toMainPage()
        }
    }

    private fun toMainPage() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}