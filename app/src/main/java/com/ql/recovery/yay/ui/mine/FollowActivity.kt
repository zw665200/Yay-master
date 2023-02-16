package com.ql.recovery.yay.ui.mine

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.netease.yunxin.kit.adapters.DataAdapter
import com.ql.recovery.bean.UserInfo
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.config.Follow
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.databinding.ActivityFollowBinding
import com.ql.recovery.yay.databinding.ItemFollowBinding
import com.ql.recovery.yay.manager.IMManager
import com.ql.recovery.yay.ui.base.BaseActivity
import com.ql.recovery.yay.ui.dialog.ProfileDialog
import com.ql.recovery.yay.util.AppUtil
import com.ql.recovery.yay.util.DoubleUtils
import com.ql.recovery.yay.util.JLog

class FollowActivity : BaseActivity() {
    private lateinit var binding: ActivityFollowBinding
    private lateinit var mAdapter: DataAdapter<UserInfo>
    private var mList = arrayListOf<UserInfo>()
    private var followingList = arrayListOf<String>()
    private var mType = Follow.Following
    private var page = 0
    private var size = 500

    override fun getViewBinding(baseBinding: ActivityBaseBinding) {
        binding = ActivityFollowBinding.inflate(layoutInflater, baseBinding.flBase, true)
    }

    override fun initView() {
        setStatusBarLight()
        binding.includeTitle.ivBack.setOnClickListener { finish() }
        binding.tvFollowing.setOnClickListener { checkFollowing() }
        binding.tvFollower.setOnClickListener { checkFollowed() }
    }

    override fun initData() {
        binding.includeTitle.tvName.text = getString(R.string.follow_title)

        initFollowList()
        initCount()
        checkFollowing()
    }

    private fun initFollowList() {
        mAdapter = DataAdapter.Builder<UserInfo>()
            .setData(mList)
            .setLayoutId(R.layout.item_follow)
            .addBindView { itemView, itemData, position, payloads ->
                val itemBinding = ItemFollowBinding.bind(itemView)

                if (payloads.isNotEmpty()) {
                    if (itemData.online) {
                        itemBinding.onlineStatus.setImageResource(R.drawable.pp_zx)
                    } else {
                        itemBinding.onlineStatus.setImageResource(R.drawable.pp_bzx)
                    }
                    return@addBindView
                }

                Glide.with(this).load(itemData.avatar).apply(RequestOptions.bitmapTransform(CircleCrop())).into(itemBinding.ivAvatar)
                itemBinding.tvName.text = itemData.nickname
                itemBinding.tvAge.text = itemData.age.toString()

                if (itemData.online) {
                    itemBinding.onlineStatus.setImageResource(R.drawable.pp_zx)
                } else {
                    itemBinding.onlineStatus.setImageResource(R.drawable.pp_bzx)
                }

                JLog.i("follow = ${itemData.follow_status}")

                //设置关注状态
                when (itemData.follow_status) {
                    1, 3 -> {
                        itemBinding.tvFollow.text = getString(R.string.mine_unfollow)
                        itemBinding.tvFollow.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_rectangle_black_8, null)
                    }
                    else -> {
                        itemBinding.tvFollow.text = getString(R.string.mine_follow)
                        itemBinding.tvFollow.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_yellow_8, null)
                    }
                }

                //设置性别
                when (itemData.sex) {
                    1 -> itemBinding.ivGender.setImageResource(R.drawable.pp_xbnn)
                    2 -> itemBinding.ivGender.setImageResource(R.drawable.pp_xbn)
                }

                if (itemData.date != null) {
                    val year = AppUtil.timeStamp2Date(itemData.date, "yyyy-MM-dd")
                    val today = AppUtil.getTodayDate()
                    if (year == today) {
                        itemBinding.tvDate.text = AppUtil.timeStamp2Date(itemData.date, "HH:mm")
                    } else {
                        itemBinding.tvDate.text = AppUtil.timeStamp2Date(itemData.date, "MM-dd HH:mm")
                    }
                }

                itemBinding.tvFollow.setOnClickListener {
                    checkFollow(itemData.follow_status, itemData.uid) { status ->
                        when (mType) {
                            Follow.Following -> getFollowingList()
                            Follow.Follower -> {
                                DataManager.getUserInfo { initCount() }
                                itemData.follow_status = status
                                mAdapter.notifyItemChanged(position)
                            }
                        }
                    }
                }

                itemView.setOnClickListener { showMatcherDetail(itemData.uid, itemData.nickname, itemData.avatar, itemData.online) }
            }
            .create()

        binding.rcFollowList.adapter = mAdapter
        binding.rcFollowList.layoutManager = LinearLayoutManager(this)
    }

    private fun initCount() {
        val userInfo = getLocalStorage().decodeParcelable("user_info", UserInfo::class.java)
        if (userInfo != null) {
            binding.tvFollowing.text = String.format(getString(R.string.follow_following), userInfo.following)
            binding.tvFollower.text = String.format(getString(R.string.follow_followed), userInfo.followers)
        }
    }

    private fun checkFollowing() {
        mType = Follow.Following
        binding.tvFollowing.setTextColor(Color.BLACK)
        binding.tvFollower.setTextColor(Color.GRAY)
        binding.tvFollowing.setTypeface(null, Typeface.BOLD)
        binding.tvFollower.setTypeface(null, Typeface.NORMAL)
        binding.ivFollowingLine.visibility = View.VISIBLE
        binding.ivFollowerLine.visibility = View.INVISIBLE
        getFollowingList()
    }

    private fun checkFollowed() {
        mType = Follow.Follower
        binding.tvFollowing.setTextColor(Color.GRAY)
        binding.tvFollower.setTextColor(Color.BLACK)
        binding.tvFollowing.setTypeface(null, Typeface.NORMAL)
        binding.tvFollower.setTypeface(null, Typeface.BOLD)
        binding.ivFollowingLine.visibility = View.INVISIBLE
        binding.ivFollowerLine.visibility = View.VISIBLE
        getFollowerList()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getFollowingList() {
        DataManager.getFollowingList(page, size) { userList ->
            binding.tvFollowing.text = String.format(getString(R.string.follow_following), userList.size)

            if (userList.isEmpty()) {
                binding.includeNoData.root.visibility = View.VISIBLE
                binding.includeNoData.tvContent.text = getString(R.string.record_no_following)
                binding.includeNoData.tvToMatch.text = getString(R.string.record_make_friends)
            }

//            userList.forEach { it.follow_status = 1 }

            mList.clear()
            mList.addAll(userList)
            mAdapter.notifyDataSetChanged()

            //subscribe
            val uidList = userList.map { it.uid.toString() }
//            followingList.clear()
//            followingList.addAll(uidList)
            IMManager.subscribe(this, uidList)

            //check online status
            IMManager.checkOnlineStatus(this, uidList) { subscriberList ->
                runOnUiThread {
                    for (subscriber in subscriberList) {
                        for ((position, item) in uidList.withIndex()) {
                            if (item == subscriber.uid) {
                                mList[position].online = subscriber.online
                                mAdapter.notifyItemChanged(position, "online")
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getFollowerList() {
        DataManager.getFollowedList(page, size) { userList ->
            binding.tvFollower.text = String.format(getString(R.string.follow_followed), userList.size)

            if (userList.isEmpty()) {
                binding.includeNoData.root.visibility = View.VISIBLE
                binding.includeNoData.tvContent.text = getString(R.string.record_no_followed)
                binding.includeNoData.tvToMatch.text = getString(R.string.record_make_friends)
                mList.clear()
                mList.addAll(userList)
                mAdapter.notifyDataSetChanged()
                return@getFollowedList
            }

            binding.includeNoData.root.visibility = View.GONE

//            userList.forEach { it.follow_status = 2 }

            mList.clear()
            mList.addAll(userList)
            mAdapter.notifyDataSetChanged()

            //subscribe
            val uidList = userList.map { it.uid.toString() }
//            for (item in userList) {
//                if (followingList.contains(item.uid.toString())) {
//                    item.follow_status = 3
//                }
//            }
//            mList.addAll(userList)


            IMManager.subscribe(this, uidList)

            //check online status
            IMManager.checkOnlineStatus(this, uidList) { subscriberList ->
                runOnUiThread {
                    for (subscriber in subscriberList) {
                        for ((position, item) in uidList.withIndex()) {
                            if (item == subscriber.uid) {
                                mList[position].online = subscriber.online
                                mAdapter.notifyItemChanged(position, "online")
                            }
                        }
                    }
                }
            }
        }
    }


    private fun checkFollow(status: Int, uid: Int, func: (Int) -> Unit) {
        if (!DoubleUtils.isFastDoubleClick()) {
            when (status) {
                1, 3 -> {
                    DataManager.unfollow(uid) {
                        if (it) {
                            if (status == 1) {
                                func(0)
                            } else {
                                func(2)
                            }
                        }
                    }
                }

                else -> {
                    DataManager.follow(uid) {
                        if (it) {
                            when (status) {
                                0 -> func(1)
                                2 -> func(3)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showMatcherDetail(uid: Int, nickname: String, avatar: String, online: Boolean) {
        DataManager.getUserInfoById(uid) { userInfo ->
            ProfileDialog(this, userInfo, online, true) {
                when (it) {
                    "video" -> requestVideoChat(uid, online)
                    "im" -> requestChat(uid, nickname, avatar)
                    "follow" -> {
                        when (mType) {
                            Follow.Following -> getFollowingList()
                            Follow.Follower -> getFollowerList()
                        }
                    }
                }
            }
        }
    }

}