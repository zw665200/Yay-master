package com.ql.recovery.yay.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.PorterDuff
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.widget.ContentLoadingProgressBar
import com.ql.recovery.yay.R
import com.ql.recovery.yay.util.AppUtil


class WaitingDialog(
    private val activity: Activity
) : Dialog(activity, R.style.app_dialog) {

    private lateinit var progress: ContentLoadingProgressBar

    init {
        initVew()
    }

    private fun initVew() {
        val dialogContent = LayoutInflater.from(activity).inflate(R.layout.dialog_waiting, null)
        setContentView(dialogContent)
        setCancelable(true)

        progress = dialogContent.findViewById(R.id.progress)
        progress.indeterminateDrawable.setColorFilter(ContextCompat.getColor(context, R.color.color_blue), PorterDuff.Mode.MULTIPLY)
    }


    override fun show() {
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.CENTER
            width = AppUtil.getScreenWidth(context) - 50
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        super.show()
    }

}