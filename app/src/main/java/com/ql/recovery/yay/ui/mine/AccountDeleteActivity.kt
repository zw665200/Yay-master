package com.ql.recovery.yay.ui.mine

import android.annotation.SuppressLint
import android.content.Intent
import com.ql.recovery.bean.UserInfo
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.ActivityAccountDeleteBinding
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.ui.base.BaseActivity
import com.ql.recovery.yay.ui.dialog.AccountDeleteDialog
import com.ql.recovery.yay.ui.login.LoginActivity
import com.ql.recovery.yay.util.ToastUtil

class AccountDeleteActivity : BaseActivity() {
    private lateinit var binding: ActivityAccountDeleteBinding

    override fun getViewBinding(baseBinding: ActivityBaseBinding) {
        binding = ActivityAccountDeleteBinding.inflate(layoutInflater, baseBinding.flBase, true)
    }

    override fun initView() {
        binding.includeTitle.ivBack.setOnClickListener { finish() }
        binding.refuse.setOnClickListener { finish() }
        binding.submit.setOnClickListener { accountDelete() }
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        binding.includeTitle.tvName.text = getString(R.string.account_delete_title)
    }

    private fun accountDelete() {
        val userInfo = getLocalStorage().decodeParcelable("user_info", UserInfo::class.java)
        if (userInfo == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            return
        }

        AccountDeleteDialog(this) {
            ToastUtil.showShort(this@AccountDeleteActivity, getString(R.string.not_find_commit))
            finish()
        }
    }
}