package com.ql.recovery.yay.ui.mine

import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import com.netease.yunxin.kit.corekit.im.IMKitClient
import com.netease.yunxin.kit.corekit.im.login.LoginCallback
import com.ql.recovery.bean.Resource
import com.ql.recovery.bean.UserInfo
import com.ql.recovery.config.Config
import com.ql.recovery.yay.R
import com.ql.recovery.yay.adapter.DataAdapter
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.databinding.ActivityFeedbackSettingBinding
import com.ql.recovery.yay.databinding.ItemFunctionSettingBinding
import com.ql.recovery.yay.ui.base.BaseActivity
import com.ql.recovery.yay.ui.login.LoginActivity
import com.ql.recovery.yay.util.FileUtil
import com.ql.recovery.yay.util.ToastUtil
import com.tencent.mmkv.MMKV
import kotlin.concurrent.thread

class FeedbackSettingActivity : BaseActivity() {
    private lateinit var binding: ActivityFeedbackSettingBinding

    override fun getViewBinding(baseBinding: ActivityBaseBinding) {
        binding = ActivityFeedbackSettingBinding.inflate(layoutInflater, baseBinding.flBase, true)
    }


    override fun initView() {
        binding.includeTitle.ivBack.setOnClickListener { finish() }
    }

    override fun initData() {
        binding.includeTitle.tvName.text = getString(R.string.setting_feedback)
        loadFunction()
    }

    private fun loadFunction() {
        val list = arrayListOf<Resource>()
        list.add(Resource("feedback", 0, getString(R.string.setting_feedback_feedback)))
        list.add(Resource("eq", 0, getString(R.string.setting_feedback_eq)))
        list.add(Resource("privacy", 0, getString(R.string.setting_feedback_privacy)))
        list.add(Resource("service", 0, getString(R.string.setting_feedback_service)))
        list.add(Resource("delete", 0, getString(R.string.setting_feedback_delete)))

        val userInfo = getLocalStorage().decodeParcelable("user_info", UserInfo::class.java)
        if (userInfo != null) {
            list.add(Resource("logout", 0, getString(R.string.setting_feedback_logout)))
        }

        val mAdapter = DataAdapter.Builder<Resource>()
            .setData(list)
            .setLayoutId(R.layout.item_function_setting)
            .addBindView { itemView, itemData ->
                val itemBinding = ItemFunctionSettingBinding.bind(itemView)
                itemBinding.tvName.text = itemData.name

                itemView.setOnClickListener {
                    when (itemData.type) {
                        "feedback" -> openFeedback()
                        "service" -> openUserAgreement()
                        "privacy" -> openPrivacyAgreement()
                        "check_update" -> checkUpdate()
                        "clear_cache" -> clearCache()
                        "about_us" -> aboutUs()
                        "delete" -> accountDelete()
                        "subscription_agreement" -> openSubscriptionDialog()
                        "eq" -> toQuestionPage()
                        "logout" -> logOut()
                    }
                }
            }
            .create()

        binding.rcContent.layoutManager = LinearLayoutManager(this)
        binding.rcContent.adapter = mAdapter
        mAdapter.notifyItemRangeChanged(0, list.size)
    }

    private fun openFeedback() {
        val intent = Intent(this, FeedbackActivity::class.java)
        startActivity(intent)
    }

    private fun toQuestionPage() {
        val intent = Intent(this, QuestionActivity::class.java)
        startActivity(intent)
    }

    private fun openUserAgreement() {
        val intent = Intent(this, AgreementActivity::class.java)
        intent.putExtra("index", 0)
        startActivity(intent)
    }

    private fun openPrivacyAgreement() {
        val intent = Intent(this, AgreementActivity::class.java)
        intent.putExtra("index", 1)
        startActivity(intent)
    }

    private fun clearCache() {
        ToastUtil.showShort(this@FeedbackSettingActivity, "清除成功")
        thread {
            FileUtil.clearAllCache(this)
        }
    }

    private fun checkUpdate() {
        ToastUtil.showShort(this@FeedbackSettingActivity, "已经是最新版本")
    }

    private fun accountDelete() {
        val userInfo = MMKV.defaultMMKV()?.decodeParcelable("user_info", UserInfo::class.java)
        if (userInfo == null) {
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            startActivity(Intent(this, AccountDeleteActivity::class.java))
        }
    }

    private fun openSubscriptionDialog() {
//        TermsDialog(this, 1, object : Callback {
//            override fun onSuccess() {
//                startActivity(Intent(this@FeedbackSettingActivity, AgreementActivity::class.java))
//            }
//
//            override fun onCancel() {
//            }
//        }).show()
    }

    private fun aboutUs() {
    }

    private fun logOut() {
        Config.USER_ID = 0
        Config.USER_NAME = ""
        Config.CLIENT_TOKEN = ""

        getLocalStorage().remove("user_info")
        getLocalStorage().remove("access_token")
        getLocalStorage().remove("token")

        IMKitClient.logoutIM(object : LoginCallback<Void> {
            override fun onError(errorCode: Int, errorMsg: String) {
            }

            override fun onSuccess(data: Void?) {
            }
        })

        startActivity(Intent(this, LoginActivity::class.java))

        finish()
    }

}