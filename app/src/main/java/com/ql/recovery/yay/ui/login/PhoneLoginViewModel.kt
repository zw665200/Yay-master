package com.ql.recovery.yay.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ql.recovery.config.Config
import com.ql.recovery.manager.DataManager
import com.tencent.mmkv.MMKV

class PhoneLoginViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is notifications Fragment"
    }

    val text: LiveData<String> = _text

    fun signUpWithPhone(phoneCode: String, phone: String, code: String, password: String, deviceId: String, isSuccess: (Boolean) -> Unit) {
        DataManager.getAuthFromPhoneRegister(phoneCode, phone, code, password, deviceId) {
            val accessToken = it.type + " " + it.access_token
            Config.CLIENT_TOKEN = accessToken
            MMKV.defaultMMKV()?.encode("access_token", accessToken)
            MMKV.defaultMMKV()?.encode("token", it.access_token)
            isSuccess(true)
        }
    }

    fun loginWithPhone(password: String, phone: String, phoneCode: String, isSuccess: (Boolean) -> Unit) {
        DataManager.getAuthFromPhone(password, phone, phoneCode) {
            val accessToken = it.type + " " + it.access_token
            Config.CLIENT_TOKEN = accessToken
            MMKV.defaultMMKV()?.encode("access_token", accessToken)
            MMKV.defaultMMKV()?.encode("token", it.access_token)
            isSuccess(true)
        }
    }

    fun resetPassword(phoneCode: String, phone: String, code: String, password: String, isSuccess: (Boolean) -> Unit) {
        DataManager.resetPassword(phoneCode, phone, code, password) {
            isSuccess(it)
        }
    }

    fun sendSMS(phoneCode: String, phone: String, isSuccess: (Boolean) -> Unit) {
        DataManager.sendSMS(phoneCode, phone) {
            isSuccess(it)
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}