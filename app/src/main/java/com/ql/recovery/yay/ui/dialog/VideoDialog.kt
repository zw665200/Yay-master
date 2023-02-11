package com.ql.recovery.yay.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.DialogVideoBinding
import com.ql.recovery.yay.util.AppUtil


class VideoDialog(
    private val activity: Activity,
    private val url: String
) : Dialog(activity, R.style.app_dialog2) {
    private lateinit var binding: DialogVideoBinding
    private lateinit var exoPlayer: ExoPlayer

    init {
        initVew()
    }

    private fun initVew() {
        binding = DialogVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(true)

        exoPlayer = ExoPlayer.Builder(activity).build()
        val mediaItem = MediaItem.fromUri(url)
        binding.playerView.player = exoPlayer
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true

        show()
    }

    override fun show() {
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.CENTER
            width = AppUtil.getScreenWidth(context)
            height = AppUtil.getScreenHeight(context)
        }

        super.show()
    }

    override fun cancel() {
        super.cancel()
        exoPlayer.stop()
        exoPlayer.release()
    }

    /**
     * 显示PopupWindow
     */
//    private fun show(v: View) {
//        if (mPopupWindow != null && !mPopupWindow.isShowing()) {
//            mPopupWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0)
//        }
//        setWindowAlpa(0.5f)
//    }


    /**
     * 消失PopupWindow
     */
//    override fun dismiss() {
//        if (mPopupWindow != null && mPopupWindow.isShowing()) {
//            mPopupWindow.dismiss()
//        }
//        setWindowAlpa(1.0f)
//    }

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