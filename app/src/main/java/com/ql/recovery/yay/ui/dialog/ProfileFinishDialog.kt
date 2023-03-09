package com.ql.recovery.yay.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.WindowManager
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.DialogAccountDeleteBinding
import com.ql.recovery.yay.databinding.DialogProfileFinishBinding
import com.ql.recovery.yay.util.AppUtil
import com.ql.recovery.yay.util.ToastUtil

class ProfileFinishDialog(
    private val activity: Activity
) : Dialog(activity, R.style.app_dialog) {
    private lateinit var binding: DialogProfileFinishBinding

    init {
        initVew()
    }

    private fun initVew() {
        binding = DialogProfileFinishBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(true)

        binding.tvComfirm.setOnClickListener {
            cancel()
        }
    }


    override fun show() {
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.CENTER
            width = AppUtil.getScreenWidth(context) * 7 / 8
            height = WindowManager.LayoutParams.WRAP_CONTENT
            flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
            dimAmount = 0.5f
        }
        super.show()
    }


}