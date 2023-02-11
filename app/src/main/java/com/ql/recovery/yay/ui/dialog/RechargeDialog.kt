package com.ql.recovery.yay.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.DialogRechargeBinding
import com.ql.recovery.yay.util.AppUtil

class RechargeDialog(
    private val activity: Activity,
    private val totalAmount: Int,
    private val func: () -> Unit
) : Dialog(activity, R.style.app_dialog) {
    private lateinit var binding: DialogRechargeBinding

    init {
        initVew()
    }

    private fun initVew() {
        binding = DialogRechargeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(true)

        binding.tvAmountAll.setOnClickListener {
            binding.etAmount.setText(totalAmount.toString())
        }

        binding.flRecharge.setOnClickListener {
            val amount = binding.etAmount.editableText.toString()
            if (amount.isNotBlank()) {
                DataManager.recharge(amount.toInt()) {
                    if (it) {
                        cancel()
                        func()
                    }
                }
            }
        }

        show()
    }


    override fun show() {
        val w = AppUtil.getScreenWidth(activity)
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.CENTER
            width = w * 9 / 10
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