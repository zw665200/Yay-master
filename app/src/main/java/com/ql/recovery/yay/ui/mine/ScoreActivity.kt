package com.ql.recovery.yay.ui.mine

import android.annotation.SuppressLint
import android.widget.FrameLayout
import androidx.core.view.setMargins
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.databinding.ActivityScoreBinding
import com.ql.recovery.yay.ui.base.BaseActivity
import com.ql.recovery.yay.util.AppUtil
import com.ql.recovery.yay.util.JLog

class ScoreActivity : BaseActivity() {
    private lateinit var binding: ActivityScoreBinding

    override fun getViewBinding(baseBinding: ActivityBaseBinding) {
        binding = ActivityScoreBinding.inflate(layoutInflater, baseBinding.flBase, true)
    }

    override fun initView() {
        setStatusBarLight()
        binding.ivBack.setOnClickListener { finish() }
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        getGradeDetail()
    }

    private fun getGradeDetail() {
        getUserInfo { userInfo ->
            binding.tvScore.text = String.format(getString(R.string.club_score), userInfo.grace_score)
            binding.tvCurrentScore.text = String.format("%.1f", userInfo.grace_score)
            binding.progress.progress = (userInfo.grace_score * 100).toInt()

            val width = AppUtil.getScreenWidth(this)
            val left = width * (userInfo.grace_score / 5)

            JLog.i("width = $width")
            JLog.i("left = $left")

            val lp = FrameLayout.LayoutParams(binding.tvCurrentScore.layoutParams)
            lp.setMargins(left.toInt() - AppUtil.dp2px(this, 30f), 0, 0, 0)
            binding.tvCurrentScore.layoutParams = lp
        }
    }
}