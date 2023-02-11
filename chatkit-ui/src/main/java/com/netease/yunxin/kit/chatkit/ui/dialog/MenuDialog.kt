package com.netease.yunxin.kit.chatkit.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import com.netease.yunxin.kit.chatkit.ui.R
import com.netease.yunxin.kit.chatkit.ui.databinding.DialogMenuBinding
import com.netease.yunxin.kit.chatkit.ui.util.AppUtil
import com.ql.recovery.manager.DataManager


class MenuDialog(
    private val activity: Activity,
    private val targetUid: Int,
    private val isBlock: Boolean,
    private val func: (Boolean) -> Unit
) : Dialog(activity, R.style.app_dialog_1) {
    private lateinit var binding: DialogMenuBinding

    init {
        initVew()
    }

    private fun initVew() {
        binding = DialogMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(true)

        if (isBlock) {
            binding.tvBlock.text = activity.getString(R.string.report_remove_from_blacklist)
        } else {
            binding.tvBlock.text = activity.getString(R.string.report_add_to_blacklist)
        }

        binding.tvReport.setOnClickListener { showReportDialog() }
        binding.tvBlock.setOnClickListener {
            cancel()
            func(isBlock)
        }

        show()
    }

    private fun showReportDialog() {
        cancel()
        DataManager.getTemplateList {
            ReportDialog(activity, targetUid, it).show()
        }
    }

    override fun show() {
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.BOTTOM
            width = AppUtil.getScreenWidth(context)
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }

        super.show()
    }

    /**
     * 动态设置Activity背景透明度
     *
     * @param bgAlpha
     */
    fun setWindowAlpha(bgAlpha: Float) {
        val window: Window = activity.window
        val lp = window.attributes
        lp.alpha = bgAlpha
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.attributes = lp
    }

}