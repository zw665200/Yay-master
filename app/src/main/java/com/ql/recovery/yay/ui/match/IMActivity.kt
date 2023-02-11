package com.ql.recovery.yay.ui.match

import com.netease.yunxin.kit.conversationkit.ui.page.ConversationFragment
import com.ql.recovery.bean.UserInfo
import com.ql.recovery.config.Config
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.databinding.ActivityImBinding
import com.ql.recovery.yay.ui.base.BaseActivity
import com.ql.recovery.yay.util.JLog
import com.tencent.mmkv.MMKV

class IMActivity : BaseActivity() {
    private lateinit var binding: ActivityImBinding
    private var mk = MMKV.defaultMMKV()

    override fun getViewBinding(baseBinding: ActivityBaseBinding) {
        binding = ActivityImBinding.inflate(layoutInflater, baseBinding.root, true)
    }

    override fun initView() {
        addConversation()
    }

    override fun initData() {
        initUserInfo()
    }

    private fun addConversation() {
        val conversationFragment = ConversationFragment()
        supportFragmentManager.beginTransaction().add(R.id.container, conversationFragment).commit()
    }

    private fun initUserInfo() {
        val userInfo = mk.decodeParcelable("user_info", UserInfo::class.java)
        val token = mk.decodeString("access_token")
        JLog.i("userinfo = $userInfo")
        JLog.i("token = $token")
        if (userInfo != null && token != null) {
            Config.CLIENT_TOKEN = token
            Config.USER_NAME = userInfo.nickname
            Config.USER_ID = userInfo.uid
        }
    }
}