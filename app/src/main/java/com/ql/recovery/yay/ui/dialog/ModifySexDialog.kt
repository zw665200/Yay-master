package com.ql.recovery.yay.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import com.ql.recovery.bean.UserInfo
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.DialogModifyBinding
import com.ql.recovery.yay.databinding.DialogModifySexBinding
import com.ql.recovery.yay.util.AppUtil


class ModifySexDialog(
    private val activity: Activity,
    private val func: (Int) -> Unit
) : Dialog(activity, R.style.app_dialog2) {
    private lateinit var binding: DialogModifySexBinding

    private enum class Gender { Male, Female }

    init {
        initVew()
    }

    private fun initVew() {
        binding = DialogModifySexBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(true)

        binding.tvMale.setOnClickListener { commit(Gender.Male) }
        binding.tvFemale.setOnClickListener { commit(Gender.Female) }

        show()
    }

    private fun commit(gender: Gender) {
        var isMale = 0
        isMale = when (gender) {
            Gender.Male -> 1
            Gender.Female -> 2
        }

        val nil = null
        DataManager.updateUserInfo(nil, nil, nil, isMale, nil, nil, nil) {
            if (it) {
                cancel()
                func(isMale)
            }
        }
    }

    override fun show() {
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.BOTTOM
            width = AppUtil.getScreenWidth(context)
            height = WindowManager.LayoutParams.WRAP_CONTENT
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