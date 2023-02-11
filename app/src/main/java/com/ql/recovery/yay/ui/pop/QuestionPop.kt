package com.ql.recovery.yay.ui.pop

import android.app.Activity
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.core.content.res.ResourcesCompat
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.PopQuestionBinding
import com.ql.recovery.yay.util.AppUtil

/**
 * @author Herr_Z
 * @description:
 * @date : 2022/1/25 10:37
 */
class QuestionPop constructor(
    private val activity: Activity,
    private val content: String
) : PopupWindow(activity) {
    private lateinit var binding: PopQuestionBinding

    init {
        initView()
    }

    fun initView() {
        binding = PopQuestionBinding.inflate(activity.layoutInflater)
        contentView = binding.root

        binding.tvContent.text = content

        val width = AppUtil.getScreenWidth(activity)
        val lp = binding.flContent.layoutParams
        lp.width = width * 4 / 5
        binding.flContent.layoutParams = lp

        setBackgroundDrawable(ResourcesCompat.getDrawable(activity.resources, R.color.transparent, null))

        isFocusable = true
        isOutsideTouchable = true
    }

    override fun showAsDropDown(anchor: View?) {
        super.showAsDropDown(anchor)
    }

    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int) {
        super.showAsDropDown(anchor, xoff, yoff)
    }

    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int, gravity: Int) {
        super.showAsDropDown(anchor, xoff, yoff, gravity)
    }

    override fun showAtLocation(parent: View?, gravity: Int, x: Int, y: Int) {
        super.showAtLocation(parent, gravity, x, y)
//        setWindowAlpha(0.2f)
    }

    /**
     * 消失PopupWindow
     */
    override fun dismiss() {
        super.dismiss()
//        setWindowAlpha(1.0f)
    }

    /**
     * 动态设置Activity背景透明度
     *
     * @param bgAlpha
     */
    fun setWindowAlpha(bgAlpha: Float) {
        val window = activity.window
        val lp = window.attributes
        lp.alpha = bgAlpha
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.attributes = lp
    }

}