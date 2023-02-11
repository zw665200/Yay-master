package com.ql.recovery.yay.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.DialogImageBinding
import com.ql.recovery.yay.util.AppUtil


class ImageDialog(
    private val activity: Activity,
    private val url: String
) : Dialog(activity, R.style.app_dialog2) {
    private lateinit var binding: DialogImageBinding

    init {
        initVew()
    }

    private fun initVew() {
        binding = DialogImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(true)

        Glide.with(activity).load(url).into(binding.img)

        binding.img.enable()


//        binding.photoview.setPhotoUri(Uri.parse(url))

//        (binding.ivIcon as DragPhotoView).setOnExitListener { p0, p1, p2, p3, p4 -> }

//        PhotoViewer.setClickSingleImg(url, binding.ivImage)   //因为本框架不参与加载图片，所以还是要写回调方法
//            .setShowImageViewInterface(object : PhotoViewer.ShowImageViewInterface {
//                override fun show(iv: ImageView, url: String) {
//                    Glide.with(activity).load(url).into(iv)
//                }
//            })
//            .start(activity)

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