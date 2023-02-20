package com.ql.recovery.yay.ui.mine

import android.content.Intent
import android.net.Uri
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.databinding.ActivitySettingBinding
import com.ql.recovery.yay.ui.base.BaseActivity
import com.ql.recovery.yay.util.JLog

class SettingActivity : BaseActivity() {
    private lateinit var binding: ActivitySettingBinding
    private val anchorUrl = "https://yay.social/anchor"

    override fun getViewBinding(baseBinding: ActivityBaseBinding) {
        binding = ActivitySettingBinding.inflate(layoutInflater, baseBinding.flBase, true)
    }

    override fun initView() {
        setStatusBarLight()
        binding.includeTitle.ivBack.setOnClickListener { finish() }
        binding.flNotice.setOnClickListener { toNoticeSettingPage() }
        binding.flFeedback.setOnClickListener { toFeedbackSettingPage() }
        binding.ivBlurChoose.setOnClickListener { changeBlur() }
        binding.flEmail.setOnClickListener { toEmailPage() }
        binding.flShare.setOnClickListener { toSharePage() }
        binding.flAnchor.setOnClickListener { toAnchorPage() }
    }

    override fun initData() {
        binding.includeTitle.tvName.text = getString(R.string.setting_title)

        val blurChoose = getLocalStorage().decodeBool("open_blur", false)
        if (blurChoose) {
            binding.ivBlurChoose.setImageResource(R.drawable.filter_open)
        } else {
            binding.ivBlurChoose.setImageResource(R.drawable.filter_close)
        }
    }

    private fun changeBlur() {
        val blurChoose = getLocalStorage().decodeBool("open_blur", false)
        if (blurChoose) {
            getLocalStorage().encode("open_blur", false)
            binding.ivBlurChoose.setImageResource(R.drawable.filter_close)
        } else {
            getLocalStorage().encode("open_blur", true)
            binding.ivBlurChoose.setImageResource(R.drawable.filter_open)
        }
    }

    private fun toNoticeSettingPage() {
        startActivity(Intent(this, NoticeSettingActivity::class.java))
    }

    private fun toFeedbackSettingPage() {
        startActivity(Intent(this, FeedbackSettingActivity::class.java))
    }

    private fun toSharePage() {
        startActivity(Intent(this, ShareActivity::class.java))
    }

    private fun toEmailPage() {
        val intent = Intent(this, EmailActivity::class.java)
        startActivity(intent)
    }

    private fun toAnchorPage() {
//        val intent = Intent(this, AnchorWebActivity::class.java)
//        startActivity(intent)

        val token = getLocalStorage().decodeString("access_token") ?: return
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.data = Uri.parse("$anchorUrl?token=$token")

        JLog.i("$anchorUrl?token=$token")
        startActivity(intent)
    }

}