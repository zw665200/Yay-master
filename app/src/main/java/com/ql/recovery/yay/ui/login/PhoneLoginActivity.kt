package com.ql.recovery.yay.ui.login

import android.content.Intent
import android.graphics.Typeface
import android.os.CountDownTimer
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.ql.recovery.config.Config
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.databinding.ActivityPhoneLoginBinding
import com.ql.recovery.yay.manager.ReportManager
import com.ql.recovery.yay.ui.MainActivity
import com.ql.recovery.yay.ui.base.BaseActivity
import com.ql.recovery.yay.ui.guide.GuideActivity
import com.ql.recovery.yay.ui.mine.AgreementActivity
import com.ql.recovery.yay.ui.region.RegionActivity
import com.ql.recovery.yay.util.AppUtil
import com.ql.recovery.yay.util.ToastUtil
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import java.util.*
import kotlin.concurrent.thread

class PhoneLoginActivity : BaseActivity() {
    private lateinit var binding: ActivityPhoneLoginBinding
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var timer: CountDownTimer
    private var viewModel: PhoneLoginViewModel? = null
    private var phoneUtil: PhoneNumberUtil? = null
    private var type = Type.Login
    private var pwdVisibility = PwdVisibility.Invisible
    private var isSent = false
    private var countryCode: String? = null

    override fun getViewBinding(baseBinding: ActivityBaseBinding) {
        binding = ActivityPhoneLoginBinding.inflate(layoutInflater, baseBinding.flBase, true)
    }

    override fun initView() {
        viewModel = ViewModelProvider(this)[PhoneLoginViewModel::class.java]

        binding.tvRegister.setOnClickListener {
            type = Type.SignUp
            binding.llPhone.visibility = View.VISIBLE
            binding.flCode.visibility = View.VISIBLE
            binding.flPwd.visibility = View.VISIBLE
            binding.flPwdAgain.visibility = View.GONE
            binding.tvPwdForget.visibility = View.GONE
            binding.tvRegister.visibility = View.GONE
            binding.llAgreement.visibility = View.VISIBLE
            binding.tvLoginOrRegisterTitle.text = getString(R.string.login_phone_register)
            binding.tvLoginOrRegister.text = getString(R.string.login_sign_up)
        }

        binding.tvPwdForget.setOnClickListener {
            type = Type.GetPassword
            binding.llPhone.visibility = View.VISIBLE
            binding.flCode.visibility = View.VISIBLE
            binding.flPwd.visibility = View.GONE
            binding.flPwdAgain.visibility = View.GONE
            binding.tvPwdForget.visibility = View.GONE
            binding.tvRegister.visibility = View.GONE
            binding.llAgreement.visibility = View.INVISIBLE
            binding.tvLoginOrRegisterTitle.text = getString(R.string.login_pwd_get)
            binding.tvLoginOrRegister.text = getString(R.string.login_next_step)
        }

        binding.tvPhoneCode.setOnClickListener { toRegionPage() }
        binding.ivPhoneCode.setOnClickListener { toRegionPage() }
        binding.ivBack.setOnClickListener { resetStatus() }
        binding.tvCodeCounter.setOnClickListener { getSMS() }
        binding.tvLoginOrRegister.setOnClickListener { doTask() }
        binding.ivPwdVisibility.setOnClickListener { changedPwdVisibility() }
        binding.userAgreement.setOnClickListener { toAgreementPage() }
        binding.privacyAgreement.setOnClickListener { toAgreementPage() }
    }

    override fun initData() {
        initEdit()
        initTimer()
        firebaseAnalytics = Firebase.analytics
    }

    private fun initTimer() {
        var totalTime = 59000L
        val lastTime = getLocalStorage().decodeLong("last_time", 0)
        if (lastTime >= 1000L) {
            totalTime = lastTime
        }

        timer = object : CountDownTimer(totalTime, 1000L) {
            override fun onFinish() {
                binding.tvCodeCounter.text = getString(R.string.login_get_message_code)
                getLocalStorage().encode("last_time", 0)
            }

            override fun onTick(millisUntilFinished: Long) {
                val text = AppUtil.timeStamp2Date(millisUntilFinished, "ss")
                binding.tvCodeCounter.text = text
                getLocalStorage().encode("last_time", text.toLong())
            }
        }
    }

    private fun initEdit() {
        phoneUtil = PhoneNumberUtil.createInstance(this)
        binding.etPhoneInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                binding.tvPhoneError.visibility = View.GONE
            }
        })

        binding.etPwdInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                binding.tvPwdError.visibility = View.GONE
            }
        })

        binding.etCodeInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                binding.tvCodeError.visibility = View.GONE
            }
        })
    }

    private fun doTask() {
        if ((type == Type.Login || type == Type.SignUp) && !binding.agreementCheck.isChecked) {
            ToastUtil.showShort(this, getString(R.string.login_agreement))
            return
        }

        if (type != Type.GetPassword) {
            val password = binding.etPwdInput.editableText.toString()
            if (password.length < 4) {
                binding.tvPwdError.visibility = View.VISIBLE
                return
            }
        }

        when (type) {
            Type.Login -> {
                val phoneCode = binding.tvPhoneCode.text.toString()
                val phoneNumber = binding.etPhoneInput.editableText.toString()
                val password = binding.etPwdInput.editableText.toString()
                if (phoneCode.isNotBlank() && phoneNumber.isNotBlank() && password.isNotBlank()) {

                    ReportManager.firebaseCustomLog(firebaseAnalytics, "phone_login_confirm_click", "before login")
                    ReportManager.appsFlyerCustomLog(this, "phone_login_confirm_click", "before login")

                    viewModel?.loginWithPhone(password, phoneNumber, phoneCode) {
                        if (it) {
                            loadUserInfo()

                            ReportManager.firebaseCustomLog(firebaseAnalytics, "phone_login_success", "after login")
                            ReportManager.appsFlyerCustomLog(this, "phone_login_success", "after login")
                        }
                    }
                }
            }

            Type.SignUp -> {
                val phoneCode = binding.tvPhoneCode.text.toString()
                val phone = binding.etPhoneInput.editableText.toString()
                val code = binding.etCodeInput.editableText.toString()
                val password = binding.etPwdInput.editableText.toString()
                val deviceId = AppUtil.getDeviceId(this)
                if (phoneCode.isNotBlank() && phone.isNotBlank() && code.isNotBlank()
                    && password.isNotBlank() && !deviceId.isNullOrBlank()
                ) {
                    ReportManager.firebaseCustomLog(firebaseAnalytics, "phone_register_click", "before login")
                    ReportManager.appsFlyerCustomLog(this, "phone_register_click", "before login")

                    viewModel?.signUpWithPhone(phoneCode, phone, code, password, deviceId) {
                        if (it) {
                            loadUserInfo()

                            ReportManager.firebaseCustomLog(firebaseAnalytics, "phone_register_success", "after login")
                            ReportManager.appsFlyerCustomLog(this, "phone_register_success", "after login")
                        }
                    }
                }
            }

            Type.GetPassword -> {
                if (!isSent) {
                    binding.tvCodeError.visibility = View.VISIBLE
                    return
                }

                binding.llPhone.visibility = View.GONE
                binding.flCode.visibility = View.GONE
                binding.flPwd.visibility = View.VISIBLE
                binding.flPwdAgain.visibility = View.VISIBLE
                binding.tvRegister.visibility = View.GONE
                binding.llAgreement.visibility = View.GONE
                binding.tvPwdTip.visibility = View.VISIBLE
                binding.tvLoginOrRegisterTitle.text = getString(R.string.login_pwd_reset)
                binding.tvLoginOrRegister.text = getString(R.string.login_next_submit)
                type = Type.ResetPassword
            }

            Type.ResetPassword -> {
                val phoneCode = binding.tvPhoneCode.text.toString()
                val phone = binding.etPhoneInput.editableText.toString()
                val code = binding.etCodeInput.editableText.toString()
                val password = binding.etPwdInput.editableText.toString()
                if (phoneCode.isNotBlank() && phone.isNotBlank() && code.isNotBlank() && password.isNotBlank()) {

                    ReportManager.firebaseCustomLog(firebaseAnalytics, "phone_reset_pwd_click", "reset pwd")
                    ReportManager.appsFlyerCustomLog(this, "phone_reset_pwd_click", "reset pwd")

                    viewModel?.resetPassword(phoneCode, phone, code, password) {
                        if (it) {
                            ToastUtil.showShort(this, getString(R.string.login_success))
                            resetStatus()

                            ReportManager.firebaseCustomLog(firebaseAnalytics, "phone_reset_pwd_success", "reset pwd")
                            ReportManager.appsFlyerCustomLog(this, "phone_reset_pwd_success", "reset pwd")
                        }
                    }
                }
            }
        }
    }

    private fun getSMS() {
        val counter = binding.tvCodeCounter.text.toString()
        val phoneCode = binding.tvPhoneCode.text.toString()
        val phone = binding.etPhoneInput.editableText.toString()

        if (phone.isBlank()) {
            binding.tvPhoneError.visibility = View.VISIBLE
            return
        }

        if (phoneUtil == null) return

        if (counter == getString(R.string.login_get_message_code) && phoneCode.isNotBlank() && phone.isNotBlank()) {
            try {
                val number = phoneUtil!!.parse(phoneCode + phone, "CH")
                val isValid = phoneUtil!!.isValidNumber(number)
                if (isValid) {
                    isSent = false

                    ReportManager.firebaseCustomLog(firebaseAnalytics, "phone_send_code_click", "send code")
                    ReportManager.appsFlyerCustomLog(this, "phone_send_code_click", "send code")

                    viewModel?.sendSMS(phoneCode, phone) {
                        if (it) {
                            isSent = true
                            timer.start()

                            ReportManager.firebaseCustomLog(firebaseAnalytics, "phone_send_code_success", "send code success")
                            ReportManager.appsFlyerCustomLog(this, "phone_send_code_success", "send code success")
                        }
                    }
                } else {
                    binding.tvPhoneError.visibility = View.VISIBLE
                }

            } catch (ex: Exception) {
                binding.tvPhoneError.visibility = View.VISIBLE
            }
        }
    }

    private fun changedPwdVisibility() {
        when (pwdVisibility) {
            PwdVisibility.Visible -> {
                pwdVisibility = PwdVisibility.Invisible
                binding.ivPwdVisibility.setImageResource(R.drawable.pw_hide)
                binding.etPwdInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.etPwdInput.setSelection(binding.etPwdInput.editableText.length)
                binding.etPwdInput.typeface = Typeface.createFromAsset(assets, "fonts/din_m.otf")
            }

            PwdVisibility.Invisible -> {
                pwdVisibility = PwdVisibility.Visible
                binding.ivPwdVisibility.setImageResource(R.drawable.yj_dl)
                binding.etPwdInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.etPwdInput.setSelection(binding.etPwdInput.editableText.length)
                binding.etPwdInput.typeface = Typeface.createFromAsset(assets, "fonts/din_m.otf")
            }
        }
    }

    private fun loadUserInfo() {
        DataManager.getUserInfo { userInfo ->
            ToastUtil.showShort(this, getString(R.string.login_success))

            if (userInfo.country.isBlank()) {
                val country = Locale.getDefault().country
                DataManager.updateCountry(country) {}
            }

            thread {
                //??????????????????
                Config.mainHandler?.sendEmptyMessage(0x10006)

                //??????IM
                Config.mHandler?.sendEmptyMessage(0x10004)

                //????????????
                ReportManager.firebaseLoginLog(firebaseAnalytics, userInfo.uid, userInfo.nickname)
                ReportManager.facebookLoginLog(this, userInfo.uid, userInfo.nickname)
                ReportManager.branchLoginLog(this, userInfo.uid, userInfo.nickname)
                ReportManager.appsFlyerLoginLog(this, userInfo.uid)
            }

            val guide = getLocalStorage().decodeBool("guide_finish", false)
            if (!guide) {
                if (userInfo.sex == 0 || userInfo.age == 0 || userInfo.country.isBlank()) {
                    startActivity(Intent(this, GuideActivity::class.java))
                    finish()
                    return@getUserInfo
                }
            }

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun resetStatus() {
        if (type == Type.Login) {
            finish()
            return
        }

        type = Type.Login
        binding.llPhone.visibility = View.VISIBLE
        binding.flCode.visibility = View.GONE
        binding.flPwd.visibility = View.VISIBLE
        binding.flPwdAgain.visibility = View.GONE
        binding.tvPwdForget.visibility = View.VISIBLE
        binding.tvRegister.visibility = View.VISIBLE
        binding.llAgreement.visibility = View.VISIBLE
        binding.tvPhoneError.visibility = View.GONE
        binding.tvCodeError.visibility = View.GONE
        binding.tvPwdError.visibility = View.GONE
        binding.tvPwdTip.visibility = View.GONE
        binding.tvLoginOrRegisterTitle.text = getString(R.string.login_phone_login)
        binding.tvLoginOrRegister.text = getString(R.string.login_login)
    }

    private fun toRegionPage() {
        startActivityForResult(Intent(this, RegionActivity::class.java), 0x1)

        ReportManager.firebaseCustomLog(firebaseAnalytics, "phone_choose_region_click", "click region")
        ReportManager.appsFlyerCustomLog(this, "phone_choose_region_click", "click region")
    }

    private fun toAgreementPage() {
        val intent = Intent(this, AgreementActivity::class.java)
        startActivity(intent)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x1 && resultCode == 0x1) {
            if (data != null) {
                val phoneCode = data.getStringExtra("phone_code")
                val phoneISO = data.getStringExtra("phone_iso")
                if (phoneCode != null && phoneISO != null) {
                    binding.tvPhoneCode.text = phoneCode
                    countryCode = phoneISO
                }
            }
        }
    }

    enum class Type { Login, SignUp, GetPassword, ResetPassword }
    enum class PwdVisibility { Visible, Invisible }

}