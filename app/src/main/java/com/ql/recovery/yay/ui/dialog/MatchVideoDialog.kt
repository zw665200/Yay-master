package com.ql.recovery.yay.ui.dialog

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.ql.recovery.bean.Reason
import com.ql.recovery.bean.User
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.config.MatchStatus
import com.ql.recovery.yay.databinding.DialogMatchVideoBinding
import com.ql.recovery.yay.manager.ImageManager
import com.ql.recovery.yay.ui.base.BaseDialog
import com.ql.recovery.yay.ui.self.BreatheInterpolator
import com.ql.recovery.yay.util.AppUtil
import com.tencent.mmkv.MMKV


class MatchVideoDialog(
    private val activity: Activity,
    private val status: (status: MatchStatus, type: String?) -> Unit
) : BaseDialog(activity) {
    private lateinit var binding: DialogMatchVideoBinding
    private var timer: CountDownTimer? = null
    private var set: AnimatorSet? = null
    private var mUser: User? = null
    private var mType: String? = null
    private var max = 0
    private var from = From.Other
    private var mk = MMKV.defaultMMKV()

    init {
        initVew()
    }

    private fun initVew() {
        binding = DialogMatchVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(false)

        getUserInfo { userInfo ->
            if (userInfo.role == "anchor") {
                binding.flAutoAccept.visibility = View.VISIBLE
                binding.tvWaitBackground.visibility = View.VISIBLE
            } else {
                binding.flAutoAccept.visibility = View.GONE
                binding.tvWaitBackground.visibility = View.INVISIBLE
            }
        }

        binding.ivConnect.setOnClickListener {
            status(MatchStatus.Accept, mType)
        }

        binding.ivHandOff.setOnClickListener {
            cancel()
            status(MatchStatus.Reject, mType)
        }

        binding.tvSkip.setOnClickListener {
            cancel()
            status(MatchStatus.Reject, mType)
        }

        initTimer()
    }


    private fun initTimer() {
        timer = object : CountDownTimer(45000L, 1000L) {
            override fun onFinish() {
                //45秒之后关闭对话框挂掉视频
                this@MatchVideoDialog.cancel()
                status(MatchStatus.Reject, mType)
            }

            override fun onTick(millisUntilFinished: Long) {
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

        Glide.with(activity).load(user.avatar).apply(RequestOptions.bitmapTransform(CircleCrop())).into(binding.ivAvatar)
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
    }

    fun getUser(): User? {
        return mUser
    }

    fun setTime(time: Int) {
        if (max == 0) {
            max = time
            binding.progressView.max = max
        }

        binding.progressView.progress = time
    }

    fun setMatchType(matchType: String?, transactionType: String?) {
        //检查匹配模式
        when (matchType) {
            "match" -> {
                binding.tvCallDes.visibility = View.GONE
            }

            "private" -> {
                getBasePrice { basePrice ->
                    binding.tvCallDes.visibility = View.VISIBLE
                    getUserInfo { userInfo ->
                        if (userInfo.role == "anchor") {
                            binding.tvCallDes.text = String.format(activity.getString(R.string.match_call_income), basePrice.common.private_video)
                        } else {
                            binding.tvCallDes.text = String.format(activity.getString(R.string.match_call_cost), basePrice.common.private_video)
                        }
                    }
                }
            }

            "pri_match" -> {
                DataManager.getAdditionPriceList(matchType) { list ->
                    if (list.isNotEmpty()) {
                        binding.tvCallDes.visibility = View.VISIBLE
                        if (transactionType == "income") {
                            binding.tvCallDes.text = String.format(activity.getString(R.string.match_call_income), list[0].cost)
                        } else {
                            binding.tvCallDes.text = String.format(activity.getString(R.string.match_call_cost), list[0].cost)
                        }
                    }
                }
            }
        }
    }

    fun startConnect() {
        binding.tvNotice.text = activity.getString(R.string.match_video_apply)
        binding.progressView.visibility = View.VISIBLE
        binding.ivHandOff.visibility = View.GONE
        binding.ivConnect.visibility = View.VISIBLE
        binding.viewMargin.visibility = View.GONE
    }

    fun waitConnect() {
        binding.tvNotice.text = activity.getString(R.string.match_waiting_to_connect)
        binding.progressView.visibility = View.INVISIBLE
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
        binding.progressView.visibility = View.GONE
        binding.ivConnect.visibility = View.VISIBLE
        binding.ivHandOff.visibility = View.VISIBLE
        binding.viewMargin.visibility = View.VISIBLE
        binding.tvSkip.visibility = View.GONE
    }

    /**
     * 被拒绝私人视频邀请
     */
    fun rejectConnectForPersonal(reason: Reason) {
        binding.progressView.visibility = View.GONE
        binding.ivConnect.visibility = View.GONE
        binding.ivHandOff.visibility = View.VISIBLE
        binding.viewMargin.visibility = View.GONE
        binding.tvSkip.visibility = View.GONE

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
        binding.progressView.visibility = View.GONE
        binding.ivConnect.visibility = View.GONE
        binding.ivHandOff.visibility = View.VISIBLE
        binding.viewMargin.visibility = View.GONE
        binding.tvSkip.visibility = View.GONE
    }

    /**
     * 挂断视频邀请
     */
    fun handOff() {
        binding.tvNotice.text = activity.getString(R.string.match_disconnect)
        binding.progressView.visibility = View.INVISIBLE
        binding.ivConnect.visibility = View.GONE
    }

    /**
     * 连接超时
     */
    fun connectingTimeout() {
        binding.tvNotice.text = activity.getString(R.string.match_connect_timeout)
        binding.progressView.visibility = View.INVISIBLE
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
            status(MatchStatus.Cancel, mType)
        }
    }

    override fun show() {
//        beginAnimation(binding.llProfile)
        timer?.start()

        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.CENTER
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
        }

        if (!activity.isFinishing && !activity.isDestroyed) {
            super.show()
        }
    }

    enum class From { MySelf, Other }

}