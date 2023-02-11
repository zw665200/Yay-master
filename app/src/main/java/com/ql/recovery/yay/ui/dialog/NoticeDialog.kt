package com.ql.recovery.yay.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.DialogNoticeBinding
import com.ql.recovery.yay.ui.store.StoreActivity
import com.ql.recovery.yay.util.AppUtil


class NoticeDialog(
    private val activity: Activity,
    private val coin: Int,
    private val cost: Int,
    private val func: () -> Unit
) : Dialog(activity, R.style.app_dialog2) {
    private lateinit var binding: DialogNoticeBinding

    init {
        initVew()
    }

    private fun initVew() {
        binding = DialogNoticeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(true)

        if (coin < cost) {
            binding.btnCancel.visibility = View.GONE
            binding.btnTask.text = activity.getString(R.string.club_notice_top_up)
            binding.tvContent.text = String.format(activity.getString(R.string.club_notice_coin_deficient), cost)
        } else {
            binding.tvContent.text = String.format(activity.getString(R.string.club_notice_coin_enough), cost)
        }

        binding.btnCancel.setOnClickListener { cancel() }

        binding.btnTask.setOnClickListener {
            if (coin < cost) {
                toPayPage()
                cancel()
            } else {
                cancel()
                func()
            }
        }

        show()
    }

    private fun toPayPage() {
        activity.startActivity(Intent(activity, StoreActivity::class.java))
    }

    override fun show() {
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.CENTER
            width = AppUtil.getScreenWidth(context) * 9 / 10
            height = WindowManager.LayoutParams.WRAP_CONTENT
            flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
            dimAmount = 0.5f
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