package com.ql.recovery.yay.ui.mine

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.ql.recovery.bean.UserInfo
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.databinding.ActivityEmailBinding
import com.ql.recovery.yay.ui.base.BaseActivity
import com.ql.recovery.yay.ui.dialog.WaitingDialog
import com.ql.recovery.yay.util.ToastUtil

class EmailActivity : BaseActivity() {
    private lateinit var binding: ActivityEmailBinding
    private var email: String? = null
    private var lastSendTime = 0L
    private var waitingDialog: WaitingDialog? = null

    override fun getViewBinding(baseBinding: ActivityBaseBinding) {
        binding = ActivityEmailBinding.inflate(layoutInflater, baseBinding.flBase, true)
    }

    override fun initView() {
        setStatusBarLight()
        waitingDialog = WaitingDialog(this)

        binding.includeTitle.ivBack.setOnClickListener { finish() }
        binding.tvCommit.setOnClickListener { commit() }
        binding.tvResend.setOnClickListener { checkSend() }

        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            @SuppressLint("SetTextI18n")
            override fun afterTextChanged(s: Editable?) {
                val length = s?.length
                if (length != null) {
                    binding.tvCommit.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_yellow_10, null)
                } else {
                    binding.tvCommit.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_light_yellow, null)
                }
            }
        })
    }

    override fun initData() {
        binding.includeTitle.tvName.text = getString(R.string.email_title)

        val userInfo = getLocalStorage().decodeParcelable("user_info", UserInfo::class.java)
        if (userInfo != null) {
            if (!userInfo.email.isNullOrBlank()) {
                binding.etEmail.visibility = View.GONE
                binding.tvResend.visibility = View.VISIBLE
                binding.tvCommit.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_green, null)
                binding.tvCommit.setText(R.string.email_btn_validate)
                binding.tvHint.text = String.format(getString(R.string.email_tip_3), userInfo.email)
                binding.tvResend.text = getString(R.string.email_change)
            }
        }
    }

    private fun checkSend() {
        if (email != null) {
            resendCode()
        } else {
            updateEmail()
        }
    }

    private fun resendCode() {
        if (email == null) return

        if (System.currentTimeMillis() - lastSendTime < 60 * 1000L) {
            ToastUtil.showShort(this, getString(R.string.email_wait))
            return
        }

        DataManager.sendEmailCode(email!!) {
            if (it) {
                lastSendTime = System.currentTimeMillis()
                binding.etEmail.setHint(R.string.email_edit_code_hint)
                binding.tvHint.text = String.format(getString(R.string.email_tip_2), email)
            }
        }
    }

    private fun updateEmail() {
        binding.etEmail.visibility = View.VISIBLE
        binding.etEmail.setHint(R.string.email_edit_hint)
        binding.tvHint.text = getString(R.string.email_tip_1)
        binding.tvCommit.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_yellow_10, null)
        binding.tvCommit.setText(R.string.email_btn_validate)
        binding.tvCommit.text = getString(R.string.email_validate_send)
        binding.tvResend.visibility = View.INVISIBLE
        email = null
    }

    private fun commit() {
        val content = binding.etEmail.editableText.toString()
        if (content.isNotBlank()) {
            if (email == null) {
                if (!content.contains("@")) {
                    return
                }

                waitingDialog?.show()
                DataManager.sendEmailCode(content) {
                    waitingDialog?.cancel()
                    if (it) {
                        lastSendTime = System.currentTimeMillis()
                        email = content
                        binding.etEmail.setText("")
                        binding.etEmail.setHint(R.string.email_edit_code_hint)
                        binding.tvCommit.text = getString(R.string.email_validate_finish)
                        binding.tvHint.text = String.format(getString(R.string.email_tip_2), content)
                        binding.tvResend.visibility = View.VISIBLE
                        binding.tvResend.text = getString(R.string.email_resend)
                    }
                }
            } else {
                DataManager.bindEmail(content, email!!) {
                    if (it) {
                        binding.tvCommit.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_green, null)
                        binding.tvCommit.setText(R.string.email_btn_validate)
                        binding.tvResend.text = getString(R.string.email_change)
                        binding.tvHint.text = String.format(getString(R.string.email_tip_3), email)
                        binding.etEmail.visibility = View.GONE
                        email = null

                        //更新用户信息
                        DataManager.getUserInfo { }
                    }
                }
            }

        }
    }

}