package com.ql.recovery.yay.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.WindowManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.DialogDailyCoinBinding
import com.ql.recovery.yay.util.AppUtil


class DailyCoinDialog(
    private val activity: Activity,
    private val coin: Int,
    private val func: () -> Unit
) : Dialog(activity, R.style.app_dialog2) {
    private lateinit var binding: DialogDailyCoinBinding

    init {
        initVew()
    }

    private fun initVew() {
        binding = DialogDailyCoinBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(false)

        binding.tvCoin.text = coin.toString()

        binding.ivCancel.setOnClickListener { cancel() }

        show()
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