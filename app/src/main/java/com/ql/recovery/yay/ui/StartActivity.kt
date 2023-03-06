package com.ql.recovery.yay.ui

import android.content.Intent
import android.os.CountDownTimer
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.ql.recovery.bean.Countries
import com.ql.recovery.bean.UserInfo
import com.ql.recovery.config.Config
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.databinding.ActivityStartBinding
import com.ql.recovery.yay.ui.base.BaseActivity
import com.ql.recovery.yay.ui.guide.GuideActivity
import com.ql.recovery.yay.ui.login.LoginActivity
import com.ql.recovery.yay.util.GsonUtils
import com.ql.recovery.yay.util.JLog
import com.ql.recovery.yay.util.ToastUtil
import com.tencent.mmkv.MMKV
import io.branch.referral.Branch
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
        if (intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT != 0) {
            finish()
            return
        }
    }

    override fun onStart() {
        super.onStart()
        initBranch()
    }

    override fun initData() {
        initTimer()
        getCountries()
        getBasePrice()
        initAppsFlyer()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Branch.sessionBuilder(this).withCallback { referringParams, error ->
            if (error != null) {
                JLog.e("BranchSDK_Tester", error.message)
            } else if (referringParams != null) {
                JLog.e("BranchSDK_Tester", referringParams.toString())
            }
        }.reInit()
    }

    private fun initTimer() {
        timer = object : CountDownTimer(2000L, 1000L) {
            override fun onFinish() {
                val userInfo = getLocalStorage().decodeParcelable("user_info", UserInfo::class.java)
                if (userInfo == null) {
                    openLoginPage()
                } else {
                    if (userInfo.sex == 0 || userInfo.age == 0 || userInfo.country.isBlank()) {
                        openGuidePage()
                    } else {
                        openMainPage()
                    }
                }
            }

            override fun onTick(millisUntilFinished: Long) {
            }
        }

        timer.start()
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

    private fun initAppsFlyer() {
        AppsFlyerLib.getInstance().start(this.applicationContext)
        AppsFlyerLib.getInstance().setDebugLog(false)
    }

    private fun initBranch() {
        Branch.sessionBuilder(this).withCallback { branchUniversalObject, linkProperties, error ->
            if (error != null) {
                JLog.e("BranchSDK_Tester", "branch init failed. Caused by -" + error.message)
            } else {
                JLog.e("BranchSDK_Tester", "branch init complete!")
                if (branchUniversalObject != null) {
                    JLog.e("BranchSDK_Tester", "title " + branchUniversalObject.title)
                    JLog.e("BranchSDK_Tester", "CanonicalIdentifier " + branchUniversalObject.canonicalIdentifier)
                    JLog.e("BranchSDK_Tester", "metadata " + branchUniversalObject.contentMetadata.convertToJson())
                }
                if (linkProperties != null) {
                    JLog.e("BranchSDK_Tester", "Channel " + linkProperties.channel)
                    JLog.e("BranchSDK_Tester", "control params " + linkProperties.controlParams)
                }
            }
        }.withData(this.intent.data).init()
    }

    private fun getBasePrice() {
        getUserInfo {
            DataManager.getBasePrice { basePrice ->
                getLocalStorage().encode("base_price", basePrice)
            }
        }
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

    private fun openMainPage() {
        val intent = Intent()
        intent.setClass(this, MainActivity::class.java)
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