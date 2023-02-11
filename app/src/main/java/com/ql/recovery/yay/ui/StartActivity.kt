package com.ql.recovery.yay.ui

import android.content.Intent
import android.os.CountDownTimer
import com.ql.recovery.bean.Countries
import com.ql.recovery.bean.UserInfo
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.databinding.ActivityStartBinding
import com.ql.recovery.yay.ui.base.BaseActivity
import com.ql.recovery.yay.ui.guide.GuideActivity
import com.ql.recovery.yay.ui.login.LoginActivity
import com.ql.recovery.yay.ui.mine.AgreementActivity
import com.ql.recovery.yay.util.GsonUtils
import com.tencent.mmkv.MMKV
import java.io.ByteArrayOutputStream
import java.io.InputStream

class StartActivity : BaseActivity() {
    private lateinit var binding: ActivityStartBinding
    private lateinit var timer: CountDownTimer
    private var mk = MMKV.defaultMMKV()

    override fun getViewBinding(baseBinding: ActivityBaseBinding) {
        binding = ActivityStartBinding.inflate(layoutInflater, baseBinding.flBase, true)
    }

    override fun initView() {
        initTimer()
        getCountries()
    }

    override fun initData() {
        val agree = mk.decodeBool("service_agree", false)
        if (agree) {
            val intent = Intent(this, AgreementActivity::class.java)
            intent.putExtra("index", 3)
            startActivityForResult(intent, 0x1)
        } else {
            timer.start()
        }
    }

    private fun initTimer() {
        timer = object : CountDownTimer(1500L, 1000) {
            override fun onFinish() {
                openMainPage()
//                initUserInfo()
            }

            override fun onTick(millisUntilFinished: Long) {
            }
        }
    }

    private fun getCountries() {
        val inputStream: InputStream
        val bos: ByteArrayOutputStream
        try {
            inputStream = assets.open("countryData.json")
            bos = ByteArrayOutputStream()
            val bytes = ByteArray(4 * 1024)
            var len = 0
            while (run {
                    len = inputStream.read(bytes)
                    len
                } > 0) {
                bos.write(bytes, 0, len)
            }

            val json = String(bos.toByteArray())
            val obj = GsonUtils.fromJson(json, Countries::class.java)
            mk.encode("country_list", obj)
        } catch (ex: Exception) {

        }
    }

    private fun initUserInfo() {
        val userInfo = mk.decodeParcelable("user_info", UserInfo::class.java)
        if (userInfo != null) {
            val guide = getLocalStorage().decodeBool("guide_finish", false)
            if (!guide) {
                openGuidePage()
            } else {
                openMainPage()
            }
        } else {
            openLoginPage()
        }
    }

    private fun openMainPage() {
        val intent = Intent()
        intent.setClass(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun openLoginPage() {
        val intent = Intent()
        intent.setClass(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun openGuidePage() {
        val intent = Intent()
        intent.setClass(this, GuideActivity::class.java)
        startActivity(intent)
        finish()
    }

    @Deprecated("")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == 0x1) {
            mk?.encode("service_agree", true)
            openMainPage()
//            initUserInfo()
        }

        if (resultCode == 0x2) {
            mk?.encode("service_agree", false)
            timer.start()
        }
    }
}