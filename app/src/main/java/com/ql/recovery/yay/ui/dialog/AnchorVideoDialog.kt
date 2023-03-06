package com.ql.recovery.yay.ui.dialog

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.Dialog
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import com.bumptech.glide.Glide
import com.netease.yunxin.kit.adapters.DataAdapter
import com.ql.recovery.bean.Reason
import com.ql.recovery.bean.Tag
import com.ql.recovery.bean.User
import com.ql.recovery.yay.R
import com.ql.recovery.yay.config.MatchStatus
import com.ql.recovery.yay.databinding.DialogAnchorVideoBinding
import com.ql.recovery.yay.manager.ImageManager
import com.ql.recovery.yay.ui.self.BreatheInterpolator
import com.ql.recovery.yay.util.AppUtil


class AnchorVideoDialog(
    private val activity: Activity,
    private val status: (status: MatchStatus) -> Unit
) : Dialog(activity, R.style.app_dialog2) {
    private lateinit var binding: DialogAnchorVideoBinding
    private lateinit var mAdapter: DataAdapter<Tag>
    private var timer: CountDownTimer? = null
    private var mList = mutableListOf<Tag>()
    private var set: AnimatorSet? = null
    private var mUser: User? = null
    private var from = From.Other

    init {
        initVew()
    }

    private fun initVew() {
        binding = DialogAnchorVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(true)

        binding.ivConnect.setOnClickListener {
            status(MatchStatus.Accept)
        }

        binding.ivHandOff.setOnClickListener {
            cancel()
            status(MatchStatus.Reject)
        }

        initTimer()

        show()
    }

    private fun initTimer() {
        timer = object : CountDownTimer(45000L, 1000L) {
            override fun onFinish() {
                //45秒之后关闭对话框挂掉视频
                this@AnchorVideoDialog.cancel()
                status(MatchStatus.Reject)
            }

            override fun onTick(millisUntilFinished: Long) {
                binding.progressView.progress = (millisUntilFinished * 100 / 45000L).toInt()
                binding.progressView.bottomText = AppUtil.timeStamp2Date(millisUntilFinished, "ss")
                if (millisUntilFinished in 14000L..15000L) {
                    //对方30秒不接听，发起者显示超时
                    if (from == From.MySelf) {
                        binding.tvNotice.text = activity.getString(R.string.match_wait_long_time)
                    }
                }
            }
        }
    }

    fun setUser(user: User?) {
        if (user == null) return
        mUser = user

        Glide.with(activity).load(user.cover_url).into(binding.ivAvatar)
        Glide.with(activity).load(user.cover_url).into(binding.ivUserBg)
        binding.tvNickname.text = user.nickname
        binding.tvAge.text = user.age.toString()

        //设置性别
        when (user.sex) {
            1 -> binding.ivGender.setImageResource(R.drawable.pp_xbnn)
            2 -> binding.ivGender.setImageResource(R.drawable.pp_xbn)
        }

        //设置国家
        if (user.country.isNotBlank()) {
            val res = "file:///android_asset/images/${user.country}.png"
            ImageManager.getBitmap(activity, res) { bitmap ->
                binding.ivNation.setImageBitmap(bitmap)
            }
        }

        val tags = user.tags
        if (!tags.isNullOrEmpty()) {
            mList.clear()
            if (tags.size > 8) {
                mList.addAll(tags.subList(0, 8))
            } else {
                mList.addAll(tags)
            }

            mAdapter.notifyItemRangeChanged(0, mList.size)
        }
    }

    fun getUser(): User? {
        return mUser
    }

    fun getActivity(): Activity {
        return activity
    }

    fun setTime(time: Long) {
        binding.tvTimer.text = time.toString()
    }

    fun startConnect() {
        binding.tvNotice.text = activity.getString(R.string.match_video_apply)
        binding.tvTimer.visibility = View.VISIBLE
        binding.ivHandOff.visibility = View.GONE
        binding.ivConnect.visibility = View.VISIBLE
        binding.viewMargin.visibility = View.GONE
    }

    fun waitConnect() {
        binding.tvNotice.text = activity.getString(R.string.match_waiting_to_connect)
        binding.tvTimer.visibility = View.INVISIBLE
        binding.ivHandOff.visibility = View.GONE
        binding.ivConnect.visibility = View.GONE
        binding.viewMargin.visibility = View.GONE
    }

    /**
     * 接收到私人视频邀请
     */
    fun startConnectForPersonal() {
        from = From.Other
        binding.tvNotice.text = activity.getString(R.string.match_video_apply)
        binding.tvTimer.visibility = View.GONE
        binding.ivConnect.visibility = View.VISIBLE
        binding.ivHandOff.visibility = View.VISIBLE
        binding.viewMargin.visibility = View.VISIBLE
    }

    /**
     * 被拒绝私人视频邀请
     */
    fun rejectConnectForPersonal(reason: Reason) {
        binding.tvTimer.visibility = View.GONE
        binding.ivConnect.visibility = View.GONE
        binding.ivHandOff.visibility = View.VISIBLE
        binding.viewMargin.visibility = View.GONE

        if (from == From.MySelf) {
            when (reason.reason) {
                "default" -> {
                    binding.tvNotice.text = activity.getString(R.string.match_video_reject)
                }

                "busy" -> {
                    binding.tvNotice.text = activity.getString(R.string.match_video_reject_busy)
                }

                "offline" -> {
                    binding.tvNotice.text = activity.getString(R.string.match_video_reject_offline)
                }
            }
        } else {
            //如果是对方挂断取消对话框
            cancel()
        }
    }

    /**
     * 发起私人视频邀请
     */
    fun waitConnectForPersonal() {
        from = From.MySelf
        binding.tvNotice.text = activity.getString(R.string.match_waiting_to_connect_personal)
        binding.tvTimer.visibility = View.GONE
        binding.ivConnect.visibility = View.GONE
        binding.ivHandOff.visibility = View.VISIBLE
        binding.viewMargin.visibility = View.GONE
    }

    /**
     * 挂断视频邀请
     */
    fun handOff() {
        binding.tvNotice.text = activity.getString(R.string.match_disconnect)
        binding.tvTimer.visibility = View.INVISIBLE
        binding.ivConnect.visibility = View.GONE
    }

    /**
     * 连接超时
     */
    fun connectingTimeout() {
        binding.tvNotice.text = activity.getString(R.string.match_connect_timeout)
        binding.tvTimer.visibility = View.INVISIBLE
        binding.ivConnect.visibility = View.GONE
    }

    private fun beginAnimation(view: View) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.9f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.9f)
        scaleX.repeatCount = Animation.INFINITE
        scaleY.repeatCount = Animation.INFINITE
        set = AnimatorSet()
        set!!.playTogether(scaleX, scaleY)
        set!!.duration = 1500
        set!!.interpolator = BreatheInterpolator()
        set!!.start()
    }

    override fun cancel() {
        super.cancel()
        timer?.cancel()
        set?.cancel()

        if (!activity.isFinishing && !activity.isDestroyed) {
            status(MatchStatus.Cancel)
        }
    }

    override fun show() {
        timer?.start()

        val w = AppUtil.getScreenWidth(activity)
        val h = AppUtil.getScreenHeight(activity)
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.BOTTOM
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
            dimAmount = 0.5f
        }

        super.show()
    }

    enum class From { MySelf, Other }

}