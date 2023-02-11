package com.ql.recovery.yay.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.WindowManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.DialogQuitBinding
import com.ql.recovery.yay.util.AppUtil

class QuitDialog(
    private val activity: Activity,
    private val type: String,
    private val success: () -> Unit
) : Dialog(activity, R.style.app_dialog2) {
    private lateinit var binding: DialogQuitBinding

    init {
        initVew()
    }

    private fun initVew() {
        binding = DialogQuitBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(true)

        when (type) {
            "video" -> {
                binding.tvContent.text = activity.getString(R.string.quit_match_video)
            }

            "voice" -> {
                binding.tvContent.text = activity.getString(R.string.quit_match_audio)
            }

            "game" -> {
                binding.tvContent.text = activity.getString(R.string.quit_match_game)
            }
        }

        binding.btnCancel.setOnClickListener { cancel() }

        binding.btnTask.setOnClickListener {
            cancel()
            success()
        }

        show()
    }


    override fun show() {
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.CENTER
            width = AppUtil.getScreenWidth(context) * 9 / 10
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        super.show()
    }

}