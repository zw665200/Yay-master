package com.ql.recovery.yay.ui.notifications

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.blongho.country_data.World
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallbackWrapper
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.netease.nimlib.sdk.uinfo.UserService
import com.netease.yunxin.kit.adapters.DataAdapter
import com.ql.recovery.bean.Conversation
import com.ql.recovery.bean.UserInfo
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.FragmentNotificationsBinding
import com.ql.recovery.yay.databinding.ItemConversationBinding
import com.ql.recovery.yay.databinding.ItemMatchVisitListBinding
import com.ql.recovery.yay.manager.IMManager
import com.ql.recovery.yay.ui.MainActivity
import com.ql.recovery.yay.ui.base.BaseFragment
import com.ql.recovery.yay.ui.dialog.PrimeDialog
import com.ql.recovery.yay.ui.dialog.WaitingDialog
import com.ql.recovery.yay.ui.mine.ShareActivity
import com.ql.recovery.yay.ui.store.StoreActivity
import com.ql.recovery.yay.util.AppUtil
import com.ql.recovery.yay.util.DoubleUtils
import com.ql.recovery.yay.util.JLog

class NotificationsFragment : BaseFragment() {
    private var binding: FragmentNotificationsBinding? = null
    private lateinit var mAdapter: DataAdapter<UserInfo>
    private lateinit var conversationAdapter: DataAdapter<Conversation>
    private var mList = arrayListOf<UserInfo>()
    private var conversationList = arrayListOf<Conversation>()
    private var waitingDialog: WaitingDialog? = null
    private var firstLoad = true
    private var mPage = 0
    private var mSize = 20

    override fun initView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        binding!!.includeNoData.tvToMatch.setOnClickListener { (requireActivity() as MainActivity).changeFragment(0) }
        binding!!.ivVip.setOnClickListener { showPrimeDialog() }
        binding!!.includeSystemNotification.root.setOnClickListener { toNotificationPage() }
        binding!!.ivShare.setOnClickListener { toSharePage() }
        binding!!.llCoin.setOnClickListener { toStorePage() }

        waitingDialog = WaitingDialog(requireActivity())

        return binding!!.root
    }

    override fun initData() {
        if (firstLoad) {
            firstLoad = false
            initConversationList()
            initFollowList()

            binding!!.refreshLayout.setOnRefreshListener { refreshLayout ->
                refreshLayout.finishRefresh()
                initData()
            }

            binding!!.refreshLayout.setOnLoadMoreListener {
                mPage++
                getConversationList()
            }
        }

        mPage = 0

        getUserInfo()
        getFollowingInfoList()
        getNotificationList()
        getConversationList()
    }

    private fun getConversationList() {
        NIMClient.getService(MsgService::class.java).queryRecentContacts()
            .setCallback(object : RequestCallbackWrapper<List<RecentContact>>() {

                @SuppressLint("NotifyDataSetChanged")
                override fun onResult(code: Int, result: List<RecentContact>?, exception: Throwable?) {
                    binding!!.refreshLayout.finishRefresh()
                    binding!!.refreshLayout.finishLoadMore()

                    if (result != null) {
                        for (recent in result) {
                            //超管用户消息屏蔽
                            if (recent.contactId == "1") return

                            //subscribe
                            IMManager.subscribe(requireContext(), recent.contactId)

                            val userInfo = NIMClient.getService(UserService::class.java).getUserInfo(recent.contactId)
                            if (userInfo != null) {
                                JLog.i("unread count = ${recent.unreadCount}")

                                val conversation = Conversation(
                                    recent.contactId, userInfo.name, userInfo.avatar,
                                    recent.time, recent.content, recent.unreadCount, false
                                )

                                if (conversationList.isEmpty()) {
                                    conversationList.add(conversation)
                                    conversationAdapter.notifyDataSetChanged()

                                    //check online status
                                    checkConversationOnlineStatus(recent.contactId, conversationList.size - 1)
                                    continue
                                }

                                val list = arrayListOf<Conversation>()
                                list.addAll(conversationList)

                                val con = conversationList.find { it.sessionId == recent.contactId }
                                if (con != null) {
                                    val position = conversationList.indexOf(con)
                                    conversationList[position] = conversation
                                    conversationAdapter.notifyDataSetChanged()

                                    //check online status
                                    checkConversationOnlineStatus(recent.contactId, position)
                                } else {
                                    conversationList.add(conversation)
                                    conversationAdapter.notifyDataSetChanged()

                                    //check online status
                                    checkConversationOnlineStatus(recent.contactId, conversationList.size - 1)
                                }
                            }
                        }
                    }
                }
            })
    }

    override fun setOnlineStatus(uid: String, online: Boolean) {
        setVisitorOnline(uid, online)
    }

    private fun initConversationList() {
        conversationAdapter = DataAdapter.Builder<Conversation>()
            .setData(conversationList)
            .setLayoutId(R.layout.item_conversation)
            .addBindView { itemView, itemData ->
                val itemBinding = ItemConversationBinding.bind(itemView)

                //填充头像名字和年龄
                Glide.with(requireActivity())
                    .load(itemData.avatar)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .into(itemBinding.ivAvatar)

                itemBinding.tvName.text = itemData.nickName
                itemBinding.tvMsg.text = itemData.msg

                val today = AppUtil.getTodayDate()
                val day = AppUtil.timeStamp2Date(itemData.date, "yyyy-MM-dd")
                if (today == day) {
                    itemBinding.tvDate.text = AppUtil.timeStamp2Date(itemData.date, "HH:mm")
                } else {
                    itemBinding.tvDate.text = AppUtil.timeStamp2Date(itemData.date, "MM-dd HH:mm")
                }

                if (itemData.unread_count == 0) {
                    itemBinding.flUnreadCount.visibility = View.INVISIBLE
                } else {
                    itemBinding.flUnreadCount.visibility = View.VISIBLE
                    if (itemData.unread_count > 99) {
                        itemBinding.tvUnreadCount.text = "99+"
                    } else {
                        itemBinding.tvUnreadCount.text = itemData.unread_count.toString()
                    }
                }

                //填充在线状态
                if (itemData.online) {
                    itemBinding.onlineStatus.setImageResource(R.drawable.pp_zx)
                } else {
                    itemBinding.onlineStatus.setImageResource(R.drawable.pp_bzx)
                }


                itemView.setOnClickListener { requestChat(itemData.sessionId.toInt(), itemData.nickName, itemData.avatar) }
            }
            .create()

        binding?.rcConversation?.adapter = conversationAdapter
        binding?.rcConversation?.layoutManager = LinearLayoutManager(requireContext())
        binding?.rcConversation?.isLayoutFrozen = true
    }

    private fun initFollowList() {
        val width = AppUtil.getScreenWidth(requireContext())
        mAdapter = DataAdapter.Builder<UserInfo>()
            .setData(mList)
            .setLayoutId(R.layout.item_match_visit_list)
            .addBindView { itemView, itemData, position, payloads ->
                val itemBinding = ItemMatchVisitListBinding.bind(itemView)

                if (payloads.isNotEmpty()) {
                    //填充在线状态
                    if (itemData.online) {
                        itemBinding.onlineStatus.setImageResource(R.drawable.pp_zx)
                    } else {
                        itemBinding.onlineStatus.setImageResource(R.drawable.pp_bzx)
                    }
                    return@addBindView
                }

                itemBinding.flBlur.visibility = View.GONE

                //设置view的宽度
                val layout = itemView.layoutParams
                layout.width = width * 2 / 5
                itemView.layoutParams = layout

                //设置头像
                if (itemData.avatar.isNotBlank()) {
                    Glide.with(requireActivity())
                        .load(itemData.avatar)
                        .apply(RequestOptions.bitmapTransform(CircleCrop()))
                        .into(itemBinding.ivAvatar)
                } else {
                    when (itemData.sex) {
                        1 -> itemBinding.ivAvatar.setImageResource(R.drawable.male_find)
                        2 -> itemBinding.ivAvatar.setImageResource(R.drawable.female_find)
                    }
                }

                //设置国家
                if (itemData.country.isNotBlank()) {
                    val flag = World.getFlagOf(itemData.country)
                    itemBinding.ivNation.setImageResource(flag)
                } else {
                    itemBinding.ivNation.visibility = View.GONE
                }

                //设置性别
                when (itemData.sex) {
                    1 -> itemBinding.ivSex.setImageResource(R.drawable.pp_xbnn)
                    2 -> itemBinding.ivSex.setImageResource(R.drawable.pp_xbn)
                }

                itemBinding.tvName.text = itemData.nickname
                itemBinding.tvAge.text = itemData.age.toString()

                //填充在线状态
                if (itemData.online) {
                    itemBinding.onlineStatus.setImageResource(R.drawable.pp_zx)
                } else {
                    itemBinding.onlineStatus.setImageResource(R.drawable.pp_bzx)
                }

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

                itemBinding.tvFollow.setOnClickListener {
                    checkFollow(itemData.follow_status, itemData.uid) {
                        getFollowingInfoList()
                    }
                }

                itemBinding.ivVideo.setOnClickListener {
                    if (!DoubleUtils.isFastDoubleClick()) {
                        requestVideoChat(itemData.uid, itemData.online)
                    }
                }

                itemView.setOnClickListener {
                    if (!DoubleUtils.isFastDoubleClick()) {
                        showUserDetail(itemData.uid, itemData.online, true)
                    }
                }
            }
            .create()

        binding?.rcVisitList?.adapter = mAdapter
        binding?.rcVisitList?.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }


    /**
     * 检查关注我的用户记录
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun getFollowedInfoList() {
        DataManager.getFollowedList(mPage, mSize) { list ->
            if (list.isEmpty()) {
                binding?.includeNoData?.root?.visibility = View.VISIBLE
                binding?.includeNoData?.tvContent?.text = getString(R.string.record_no_followed)
                binding?.includeNoData?.tvToMatch?.text = getString(R.string.record_make_friends)
            } else {
                binding?.includeNoData?.root?.visibility = View.GONE
            }

            list.forEach { it.follow_status = 2 }

//            mList.clear()
//            mList.addAll(list)
//            mAdapter.notifyDataSetChanged()


            val uidList = list.map { it.uid.toString() }
            val cList = mList.map { it.uid.toString() }
            for (item in list) {
                if (!cList.contains(item.uid.toString())) {
                    mList.add(item)
                }

                mAdapter.notifyDataSetChanged()
            }

            //subscribe
            IMManager.subscribe(requireContext(), uidList)

            //check online status
            checkFollowOnlineStatus(uidList)

//            getFollowingInfoList()
        }
    }

    /**
     * 检查我关注的用户记录
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun getFollowingInfoList() {
//        waitingDialog?.show()
        DataManager.getFollowingList(mPage, mSize) { list ->
//            waitingDialog?.cancel()

            if (list.isEmpty()) {
                binding?.includeNoData?.root?.visibility = View.VISIBLE
                binding?.includeNoData?.tvContent?.text = getString(R.string.record_no_following)
                binding?.includeNoData?.tvToMatch?.text = getString(R.string.record_make_friends)
            } else {
                binding?.includeNoData?.root?.visibility = View.GONE
            }

            list.forEach { it.follow_status = 1 }

            mList.clear()
            mList.addAll(list)
            mAdapter.notifyDataSetChanged()

            //subscribe
            val uidList = list.map { it.uid.toString() }
            IMManager.subscribe(requireContext(), uidList)

            //check online status
            checkFollowOnlineStatus(uidList)

            getFollowedInfoList()
        }
    }

    /**
     * 检查相互关注的用户记录
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun getFriendsInfoList() {
        DataManager.getFriendList(mPage, mSize) { list ->
            if (list.isEmpty()) {
                binding?.includeNoData?.root?.visibility = View.VISIBLE
                binding?.includeNoData?.tvContent?.text = getString(R.string.record_no_friends)
                binding?.includeNoData?.tvToMatch?.text = getString(R.string.record_make_friends)
            } else {
                binding?.includeNoData?.root?.visibility = View.GONE
            }

            list.forEach { it.follow_status = 3 }

//            mList.clear()
            mList.addAll(list)
            mAdapter.notifyDataSetChanged()

            //subscribe
            val uidList = list.map { it.uid.toString() }
            IMManager.subscribe(requireContext(), uidList)

            //check online status
            checkFollowOnlineStatus(uidList)
        }
    }

    private fun getNotificationList() {
        DataManager.getMessages(0, 1) {
            if (it.isNotEmpty()) {
                binding?.includeSystemNotification?.tvDes?.text = it[0].content
                binding?.includeSystemNotification?.tvDate?.text = AppUtil.timeStamp2Date(it[0].created_at, "HH:mm")
            }
        }
    }

    private fun checkFollowOnlineStatus(uidList: List<String>) {
        //check online status
        IMManager.checkOnlineStatus(requireContext(), uidList) { subscriberList ->
            requireActivity().runOnUiThread {
                val newList = mList.map { it.uid }
                for (subscriber in subscriberList) {
                    for ((position, uid) in newList.withIndex()) {
                        if (uid.toString() == subscriber.uid) {
                            mList[position].online = subscriber.online
                            mAdapter.notifyItemChanged(position, "online")
                        }
                    }
                }
            }
        }
    }

    private fun checkConversationOnlineStatus(uid: String, position: Int) {
        //check online status
        IMManager.checkOnlineStatus(requireContext(), uid) { subscriber ->
            requireActivity().runOnUiThread {
                conversationList[position].online = subscriber.online
                conversationAdapter.notifyItemChanged(position)
            }
        }
    }

    private fun getUserInfo() {
        DataManager.getUserInfo {
            binding?.tvCoin?.typeface = Typeface.createFromAsset(requireActivity().assets, "fonts/DINPro-Bold.otf")
            binding?.tvCoin?.text = it.coin.toString()

            if (it.is_vip) {
                binding?.ivVip?.setImageResource(R.drawable.in_vip)
            } else {
                binding?.ivVip?.setImageResource(R.drawable.vip_c)
            }
        }
    }

    private fun toNotificationPage() {
        if (!DoubleUtils.isFastDoubleClick()) {
            startActivity(Intent(requireActivity(), NotificationActivity::class.java))
        }
    }

    private fun toSharePage() {
        if (!DoubleUtils.isFastDoubleClick()) {
            startActivity(Intent(requireActivity(), ShareActivity::class.java))
        }
    }

    private fun toStorePage() {
        if (!DoubleUtils.isFastDoubleClick()) {
            startActivity(Intent(requireActivity(), StoreActivity::class.java))
        }
    }

    private fun setVisitorOnline(uid: String, online: Boolean) {
        for ((position, child) in mList.withIndex()) {
            if (child.uid.toString() == uid) {
                mList[position].online = online
                mAdapter.notifyItemChanged(position, "online")
            }
        }

        for ((position, child) in conversationList.withIndex()) {
            if (child.sessionId == uid) {
                conversationList[position].online = online
                conversationAdapter.notifyItemChanged(position, "online")
            }
        }
    }

    override fun flushUserInfo() {
        getUserInfo()
        getConversationList()
    }

    private fun showPrimeDialog() {
        if (!DoubleUtils.isFastDoubleClick()) {
            val userInfo = getLocalStorage().decodeParcelable("user_info", UserInfo::class.java) ?: return
            PrimeDialog(requireActivity(), userInfo.is_vip) {}
        }
    }

    private fun checkFollow(status: Int, uid: Int, func: () -> Unit) {
        if (!DoubleUtils.isFastDoubleClick()) {
            when (status) {
                1, 3 -> {
                    DataManager.unfollow(uid) {
                        if (it) {
                            func()
                        }
                    }
                }

                else -> {
                    DataManager.follow(uid) {
                        if (it) {
                            func()
                        }
                    }
                }
            }
        }
    }

    override fun click(v: View) {
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    enum class TagType { Followed, Following, Friend }
}