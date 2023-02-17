package com.ql.recovery.yay.ui.mine

import android.annotation.SuppressLint
import android.webkit.*
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.ActivityAnchorWebBinding
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.ui.base.BaseActivity

class AnchorWebActivity : BaseActivity() {
    private lateinit var binding: ActivityAnchorWebBinding
    private val anchorUrl = "https://yay.social/anchor"
//    private val anchorUrl = "http://192.168.1.27:3000"

    override fun getViewBinding(baseBinding: ActivityBaseBinding) {
        binding = ActivityAnchorWebBinding.inflate(layoutInflater, baseBinding.flBase, true)
    }

    override fun initView() {
        binding.includeTitle.tvName.text = getString(R.string.setting_anchor)
        binding.includeTitle.ivBack.setOnClickListener { finish() }
    }

    override fun initData() {
        initWebView()
//        val token = getLocalStorage().decodeString("access_token") ?: return
//        binding.webview.loadUrl("$anchorUrl?token = $token")
        binding.webview.loadUrl(anchorUrl)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        binding.webview.settings.apply {
            cacheMode = WebSettings.LOAD_NO_CACHE
            domStorageEnabled = true
            javaScriptEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            domStorageEnabled = true
            blockNetworkImage = false
            javaScriptCanOpenWindowsAutomatically = true
            loadsImagesAutomatically = true
            pluginState = WebSettings.PluginState.ON
        }

        binding.webview.webChromeClient = WebChromeClient()

        binding.webview.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                view?.loadUrl(anchorUrl)
                return true
            }
        }

    }
}