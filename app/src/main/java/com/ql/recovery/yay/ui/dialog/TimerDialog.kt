package com.ql.recovery.yay.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.CountDownTimer
import android.view.Gravity
import android.view.WindowManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.DialogTimerBinding
import com.ql.recovery.yay.ui.store.StoreActivity
import com.ql.recovery.yay.util.AppUtil


class TimerDialog(
    private val activity: Activity,
    private val func: () -> Unit
) : Dialog(activity, R.style.app_dialog2) {
    private lateinit var binding: DialogTimerBinding
    private var timer: CountDownTimer? = null

    init {
        initVew()
    }

    private fun initVew() {
        binding = DialogTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(false)

        binding.tvTimer.text = timer.toString()

        binding.tvAdd.setOnClickListener { func() }
    }

    fun setTimer(time: Long) {
        binding.tvTimer.text = AppUtil.timeStamp2Date(time, "ss")
    }

    private fun toStorePage() {
        cancel()
        activity.startActivity(Intent(activity, StoreActivity::class.java))
    }

    override fun show() {
        if (isShowing) return

        val w = AppUtil.getScreenWidth(activity)
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.CENTER_HORIZONTAL
            width = w * 5 / 12
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }

        super.show()
    }

}