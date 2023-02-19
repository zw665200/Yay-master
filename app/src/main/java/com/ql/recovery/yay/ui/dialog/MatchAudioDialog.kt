package com.ql.recovery.yay.ui.dialog

import android.animation.AnimatorSet
import android.app.Activity
import android.app.Dialog
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.blongho.country_data.World
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.ql.recovery.bean.User
import com.ql.recovery.yay.R
import com.ql.recovery.yay.config.MatchStatus
import com.ql.recovery.yay.databinding.DialogMatchAudioBinding
import com.ql.recovery.yay.manager.ImageManager
import com.ql.recovery.yay.util.AppUtil


class MatchAudioDialog(
    private val activity: Activity,
    private val status: (MatchStatus) -> Unit
) : Dialog(activity, R.style.app_dialog2) {
    private lateinit var binding: DialogMatchAudioBinding
    private var mUser: User? = null
    private var set: AnimatorSet? = null

    init {
        initVew()
    }

    private fun initVew() {
        binding = DialogMatchAudioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(false)

        binding.ivConnect.setOnClickListener {
            status(MatchStatus.Accept)
        }

        binding.llRematch.setOnClickListener {
            cancel()
            status(MatchStatus.Reject)
        }
    }


    fun setUser(user: User) {
        mUser = user
        Glide.with(activity).load(user.avatar).apply(RequestOptions.bitmapTransform(CircleCrop())).into(binding.ivAvatar)
        binding.tvNickname.text = user.nickname
        binding.tvAge.text = user.age.toString()

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

    fun setTime(time: Long) {
        binding.tvTimer.text = time.toString()
    }

    fun startConnect() {
        binding.tvNotice.text = activity.getString(R.string.match_audio_apply)
        binding.tvTimer.visibility = View.VISIBLE
        binding.ivConnect.visibility = View.VISIBLE
    }

    fun waitConnect() {
        binding.tvNotice.text = activity.getString(R.string.match_waiting_to_connect)
        binding.tvTimer.visibility = View.INVISIBLE
        binding.ivConnect.visibility = View.INVISIBLE
    }

    fun handOff() {
        binding.tvNotice.text = activity.getString(R.string.match_disconnect)
        binding.tvTimer.visibility = View.INVISIBLE
        binding.ivConnect.visibility = View.INVISIBLE
    }

    fun connectingTimeout() {
        binding.tvNotice.text = activity.getString(R.string.match_disconnect)
        binding.tvTimer.visibility = View.INVISIBLE
        binding.ivConnect.visibility = View.INVISIBLE
    }

    override fun cancel() {
        super.cancel()
        if (!activity.isFinishing && !activity.isDestroyed) {
            status(MatchStatus.Cancel)
            if (set != null) {
                set!!.cancel()
            }
        }
    }

    override fun show() {
        val w = AppUtil.getScreenWidth(activity)
        val h = AppUtil.getScreenHeight(activity)
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.CENTER
            width = w * 4 / 5
            height = WindowManager.LayoutParams.MATCH_PARENT
        }

        if (!activity.isFinishing && !activity.isDestroyed) {
            super.show()
        }
    }

}