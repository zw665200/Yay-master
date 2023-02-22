package com.ql.recovery.yay.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.view.Gravity
import android.view.WindowManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.DialogUnlockBinding
import com.ql.recovery.yay.ui.store.StoreActivity
import com.ql.recovery.yay.util.AppUtil


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

        binding.tvContent.text = String.format(activity.getString(R.string.club_unlock_content), cost)

        binding.btnCancel.setOnClickListener { cancel() }

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