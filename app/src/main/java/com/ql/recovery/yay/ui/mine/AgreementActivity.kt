package com.ql.recovery.yay.ui.mine

import android.os.Build
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.ql.recovery.http.ApiConfig
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.ActivityAgreementBinding
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.ui.base.BaseActivity

class AgreementActivity : BaseActivity() {
    private lateinit var binding: ActivityAgreementBinding
    private val customerUrl = ApiConfig.BASE_URL_WEBSITE + "termsOfUser"
    private val privacyUrl = ApiConfig.BASE_URL_WEBSITE + "privacyPolicy"


    override fun getViewBinding(baseBinding: ActivityBaseBinding) {
        binding = ActivityAgreementBinding.inflate(layoutInflater, baseBinding.flBase, true)
    }

    override fun initView() {
        binding.ivBack.setOnClickListener { finish() }
        binding.privacyAgreementTitle.setOnClickListener { loadPrivacyPage() }
        binding.userAgreementTitle.setOnClickListener { loadUserPage() }
        binding.refuse.setOnClickListener { refuse() }
        binding.agree.setOnClickListener { agree() }
    }

    override fun initData() {
        initWebView()
        binding.webview.loadUrl(privacyUrl)
        checkAgreementPermission()
    }

    private fun initWebView() {
        binding.webview.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
//                view?.loadUrl(url)
                return true
            }
        }
    }

    private fun checkAgreementPermission() {
        val value = getLocalStorage().decodeInt("index")
        if (value == 3) {
            binding.ivBack.visibility = View.GONE
            binding.bottomView.visibility = View.VISIBLE
        }
    }

    private fun loadPrivacyPage() {
        binding.webview.loadUrl(privacyUrl)
        binding.privacyAgreementTitle.setBackgroundResource(R.drawable.shape_left_corner_yellow)
        binding.userAgreementTitle.setBackgroundResource(R.drawable.shape_right_corner_white)
        if (Build.VERSION.SDK_INT < 23) {
            binding.privacyAgreementTitle.setTextColor(resources.getColor(R.color.color_white))
            binding.userAgreementTitle.setTextColor(resources.getColor(R.color.color_content))
        } else {
            binding.privacyAgreementTitle.setTextColor(resources.getColor(R.color.color_white, null))
            binding.userAgreementTitle.setTextColor(resources.getColor(R.color.color_content, null))
        }
    }

    private fun loadUserPage() {
        binding.webview.loadUrl(customerUrl)
        binding.privacyAgreementTitle.setBackgroundResource(R.drawable.shape_left_corner_white)
        binding.userAgreementTitle.setBackgroundResource(R.drawable.shape_right_corner_yellow)
        if (Build.VERSION.SDK_INT < 23) {
            binding.privacyAgreementTitle.setTextColor(resources.getColor(R.color.color_content))
            binding.userAgreementTitle.setTextColor(resources.getColor(R.color.color_white))
        } else {
            binding.privacyAgreementTitle.setTextColor(resources.getColor(R.color.color_content, null))
            binding.userAgreementTitle.setTextColor(resources.getColor(R.color.color_white, null))
        }
    }

    private fun refuse() {
        setResult(0x2)
        finish()
    }

    private fun agree() {
        setResult(0x1)
        finish()
    }
}