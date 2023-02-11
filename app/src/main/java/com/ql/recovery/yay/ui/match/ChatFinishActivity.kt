package com.ql.recovery.yay.ui.match

import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.ql.recovery.bean.Room
import com.ql.recovery.bean.User
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.databinding.ActivityChatFinishBinding
import com.ql.recovery.yay.ui.base.BaseActivity

class ChatFinishActivity : BaseActivity() {
    private lateinit var binding: ActivityChatFinishBinding

    override fun getViewBinding(baseBinding: ActivityBaseBinding) {
        binding = ActivityChatFinishBinding.inflate(layoutInflater, baseBinding.flBase, true)
    }

    override fun initView() {
        binding.includeTitle.ivBack.setOnClickListener { finish() }
        binding.ivDown.setOnClickListener { upOrDown("bad") }
        binding.ivUp.setOnClickListener { upOrDown("good") }
    }

    override fun initData() {
        val user = intent.getParcelableExtra<User>("user")
        if (user != null) {
            //设置头像
            Glide.with(this).load(user.avatar).apply(RequestOptions.bitmapTransform(CircleCrop())).into(binding.ivAvatar)
        }
    }

    private fun upOrDown(type: String) {
        val room = intent.getParcelableExtra<Room>("room")
        if (room != null) {
            DataManager.commitReview(room.room_id, type) {
                if (it) {
                    when (type) {
                        "good" -> binding.ivUp.setImageResource(R.drawable.dp_up_c)
                        "bad" -> binding.ivDown.setImageResource(R.drawable.dp_down_c)
                    }

                    finish()
                } else {
                    binding.ivUp.setImageResource(R.drawable.dp_up)
                    binding.ivDown.setImageResource(R.drawable.dp_down)
                }
            }
        }
    }
}