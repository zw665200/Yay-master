package com.ql.recovery.yay.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.PorterDuff
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.widget.ContentLoadingProgressBar
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.DialogNoticeBinding
import com.ql.recovery.yay.databinding.DialogUnlockBinding
import com.ql.recovery.yay.ui.store.StoreActivity
import com.ql.recovery.yay.util.AppUtil
import kotlin.math.cos


class UnlockDialog(
    private val activity: Activity,
    private val coin: Int,
    private val cost: Int,
    private val func: () -> Unit
) : Dialog(activity, R.style.app_dialog2) {
    private lateinit var binding: DialogUnlockBinding

    init {
        initVew()
    }

    private fun initVew() {
        binding = DialogUnlockBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(true)

        binding.btnCancel.setOnClickListener { cancel() }

        if (coin < cost) {
            binding.btnCancel.visibility = View.GONE
            binding.btnTask.text = activity.getString(R.string.club_notice_top_up)
            binding.tvContent.text = String.format(activity.getString(R.string.club_unlock_sure), cost)
        } else {
            binding.tvContent.text = String.format(activity.getString(R.string.club_unlock_content), cost)
        }

        binding.btnTask.setOnClickListener {
            if (coin < cost) {
                toPayPage()
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

}