package com.ql.recovery.yay.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.graphics.BitmapFactory
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.blongho.country_data.World
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.netease.yunxin.kit.adapters.DataAdapter
import com.ql.recovery.bean.UserInfo
import com.ql.recovery.config.Config
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.DialogProfileBinding
import com.ql.recovery.yay.databinding.ItemProfilePicBinding
import com.ql.recovery.yay.manager.ImageManager
import com.ql.recovery.yay.util.AppUtil
import com.ql.recovery.yay.util.JLog
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class ProfileDialog(
    private val activity: Activity,
    private val userInfo: UserInfo,
    private val online: Boolean,
    private val showChat: Boolean,
    private val func: (String) -> Unit
) : Dialog(activity, R.style.app_dialog2), CoroutineScope by MainScope() {
    private lateinit var binding: DialogProfileBinding
    private val height = AppUtil.getScreenHeight(activity)
    private var exoPlayer: ExoPlayer? = null
    private var waitingDialog = WaitingDialog(activity)
    private var currentPosition = 0
    private var followStatus = 0
    private var currentPos = 0

    init {
        initVew()
    }

    private fun initVew() {
        binding = DialogProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(true)

        binding.ivClose.setOnClickListener { cancel() }

        exoPlayer = ExoPlayer.Builder(activity).build()

        //设置资料高度
        val lp = binding.flPager.layoutParams
        lp.height = height * 5 / 12
        binding.flPager.layoutParams = lp

        //设置用户头像，昵称，年龄
//        Glide.with(activity).load(userInfo.avatar).apply(RequestOptions.bitmapTransform(CircleCrop())).into(binding.ivAvatar)
        binding.includeUser.tvNickname.text = userInfo.nickname
        binding.includeUser.tvAge.text = userInfo.age.toString()

        //设置国家
        if (userInfo.country.isNotBlank()) {
            val res = "file:///android_asset/images/${userInfo.country}.png"
            ImageManager.getBitmap(activity, res) { bitmap ->
                binding.includeUser.ivNation.setImageBitmap(bitmap)
            }
        }

        //设置性别
        when (userInfo.sex) {
            1 -> binding.includeUser.ivGender.setImageResource(R.drawable.pp_xbnn)
            2 -> binding.includeUser.ivGender.setImageResource(R.drawable.pp_xbn)
        }

        //设置在线状态
//        if (online) {
//            binding.onlineStatus.setImageResource(R.drawable.pp_zx)
//        } else {
//            binding.onlineStatus.setImageResource(R.drawable.pp_bzx)
//        }

        //设置关注状态
        when (userInfo.follow_status) {
            1, 3 -> binding.includeUser.tvFollow.text = activity.getString(R.string.match_followed)
            else -> binding.includeUser.tvFollow.text = activity.getString(R.string.match_follow)
        }

        //如果是本人的资料则不显示关注状态
        val user = MMKV.defaultMMKV().decodeParcelable("user_info", UserInfo::class.java)
        if (user != null) {
            if (user.uid == userInfo.uid) {
                binding.ivIm.visibility = View.GONE
                binding.ivVideo.visibility = View.GONE
                binding.includeUser.tvFollow.visibility = View.GONE
            }
        }

        //设置风度评分
        val score = String.format("%.1f", userInfo.grace_score)
        binding.includeUser.tvScore.text = String.format(activity.getString(R.string.club_score), score)
        //设置会员等级
        binding.includeUser.tvLevel.text = String.format(activity.getString(R.string.club_level), userInfo.grade)

        //设置背景墙
        val list = arrayListOf<String>()
//        list.addAll(userInfo.videos)
        list.addAll(userInfo.photos)
        initViewPager(list)

        //显示或者隐藏聊天和关注
        if (!showChat) {
            binding.ivIm.visibility = View.GONE
            binding.ivVideo.visibility = View.GONE
        }

        if (userInfo.uid == Config.USER_ID) {
            binding.includeUser.tvFollow.visibility = View.GONE
        }

        binding.ivIm.setOnClickListener {
            cancel()
            func("im")
        }

        binding.ivVideo.setOnClickListener {
            cancel()
            func("video")
        }

        binding.includeUser.tvFollow.setOnClickListener {
            checkFollow()
        }

        show()
    }

    private fun initViewPager(list: List<String>) {
        for (item in list.indices) {
            val view = View(activity)
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            lp.setMargins(0, AppUtil.dp2px(activity, 5f), 0, 0)
            lp.width = AppUtil.dp2px(activity, 5f)
            lp.height = AppUtil.dp2px(activity, 5f)
            view.layoutParams = lp
            view.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.shape_corner_grey, null)
            binding.llIndicator.addView(view)
        }

        val pagerAdapter = DataAdapter.Builder<String>()
            .setData(list)
            .setLayoutId(R.layout.item_profile_pic)
            .addBindView { itemView, itemData, position ->
                val itemBinding = ItemProfilePicBinding.bind(itemView)

                val lp = itemBinding.ivPic.layoutParams
                lp.height = height / 2
                itemBinding.ivPic.layoutParams = lp

                if (itemData.contains(".mp4")) {
                    itemBinding.ivPic.visibility = View.GONE
                    itemBinding.playerView.visibility = View.VISIBLE
                    if (position == currentPos) {
                        itemBinding.playerView.player = exoPlayer!!

                        launch {
                            val mediaItem = MediaItem.fromUri(itemData)
                            exoPlayer!!.setMediaItem(mediaItem)
                            exoPlayer!!.playWhenReady = true
                            exoPlayer!!.prepare()
                        }
                    }
                } else {
                    itemBinding.ivPic.visibility = View.VISIBLE
                    itemBinding.playerView.visibility = View.GONE
                    Glide.with(activity).load(itemData).into(itemBinding.ivPic)
                }
            }
            .create()

        val compositePageTransformer = CompositePageTransformer()

        binding.vpImage.apply {
            adapter = pagerAdapter
            setPageTransformer(compositePageTransformer)
            orientation = ViewPager2.ORIENTATION_VERTICAL

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrollStateChanged(state: Int) {
                    if (state == ViewPager2.SCROLL_STATE_IDLE) {
                        binding.vpImage.currentItem = currentPosition
                        return
                    }
                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

                }

                @SuppressLint("NotifyDataSetChanged")
                override fun onPageSelected(position: Int) {
                    binding.vpImage.post {
                        currentPosition = position
                        pagerAdapter.notifyDataSetChanged()
                    }

                    for (pos in 0 until binding.llIndicator.childCount) {
                        val view = binding.llIndicator.getChildAt(pos)
                        if (pos == position) {
                            view.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.shape_corner_white, null)
                        } else {
                            view.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.shape_corner_grey, null)
                        }
                    }
                }

            })
        }
    }

    private fun checkFollow() {
        waitingDialog.show()
        DataManager.getUserInfoById(userInfo.uid) { userInfo ->
            waitingDialog.cancel()
            when (userInfo.follow_status) {
                1, 3 -> {
                    DataManager.unfollow(userInfo.uid) {
                        if (it) {
                            followStatus = 1
                            binding.includeUser.tvFollow.text = activity.getString(R.string.match_follow)
                            func("follow")

                            //通知刷新数据
                            Config.subscriberHandler?.sendEmptyMessage(0x10001)
                        }
                    }
                }

                else -> {
                    DataManager.follow(userInfo.uid) {
                        if (it) {
                            followStatus = 0
                            binding.includeUser.tvFollow.text = activity.getString(R.string.match_followed)
                            func("follow")

                            //通知刷新数据
                            Config.subscriberHandler?.sendEmptyMessage(0x10001)
                        }
                    }
                }
            }
        }
    }

    override fun show() {
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.CENTER
            width = AppUtil.getScreenWidth(context) * 9 / 10
            height = WindowManager.LayoutParams.WRAP_CONTENT
            flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
            dimAmount = 0.5f
        }
        super.show()
    }

}