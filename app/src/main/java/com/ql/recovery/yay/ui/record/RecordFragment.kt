package com.ql.recovery.yay.ui.record

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blongho.country_data.World
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.netease.yunxin.kit.adapters.DataAdapter
import com.ql.recovery.bean.UserInfo
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.FragmentDashboardBinding
import com.ql.recovery.yay.databinding.ItemMatchRecordListBinding
import com.ql.recovery.yay.databinding.ItemMatchVisitListBinding
import com.ql.recovery.yay.manager.IMManager
import com.ql.recovery.yay.ui.MainActivity
import com.ql.recovery.yay.ui.base.BaseFragment
import com.ql.recovery.yay.ui.dialog.PrimeDialog
import com.ql.recovery.yay.ui.dialog.UnlockDialog
import com.ql.recovery.yay.ui.self.BlurTransformation
import com.ql.recovery.yay.ui.store.StoreActivity
import com.ql.recovery.yay.util.AppUtil
import com.ql.recovery.yay.util.DoubleUtils
import com.ql.recovery.yay.util.JLog

class RecordFragment : BaseFragment() {
    private lateinit var mAdapter: DataAdapter<UserInfo>
    private lateinit var mVisitorAdapter: DataAdapter<UserInfo>
    private var binding: FragmentDashboardBinding? = null
    private var mList = arrayListOf<UserInfo>()
    private var mVisitorList = arrayListOf<UserInfo>()
    private var mPage = 0
    private var mSize = 20
    private var isToLast = false
    private var mType = TagType.Visitor


    override fun initView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val dashboardViewModel = ViewModelProvider(this)[RecordViewModel::class.java]
        binding = FragmentDashboardBinding.inflate(inflater, container, false)

        initVisitorList()
        initRecordList()

        binding?.ivVip?.setOnClickListener { showPrimeDialog() }
        binding?.llCoin?.setOnClickListener { toStorePage() }
        binding!!.llTitleTop.setOnClickListener { changeTag() }
        binding!!.llCoin.setOnClickListener { toStorePage() }
        binding!!.includeNoData.tvToMatch.setOnClickListener { (requireActivity() as MainActivity).changeFragment(0) }

        return binding!!.root
    }

    override fun initData() {
        mPage = 0
        getUserInfo()
        getMatchInfoList(mPage, mSize)

        val record = getLocalStorage().decodeString("recent_record")
        if (record != null) {
            when (record) {
                "game" -> {
                    mType = TagType.Gamer
                    binding?.tvTitleTop?.text = getString(R.string.record_game_matcher)
                }

                "match" -> {
                    mType = TagType.Visitor
                    binding?.tvTitleTop?.text = getString(R.string.record_visitor)
                }
            }
        }

        when (mType) {
            TagType.Visitor -> {
                getVisitorInfoList()
            }

            TagType.Gamer -> {
                getGamerInfoList()
            }
        }

        binding!!.refreshLayout.setOnRefreshListener { refreshLayout ->
            mPage = 0
            refreshLayout.finishRefresh()
            initData()
        }

        binding!!.refreshLayout.setOnLoadMoreListener {
            mPage++
            getMatchInfoList(mPage, mSize)
        }
    }

    override fun setOnlineStatus(uid: String, online: Boolean) {
        setVisitorOnline(uid, online)
        setMatcherOnline(uid, online)
    }

    private fun initVisitorList() {
        val width = AppUtil.getScreenWidth(requireContext())
        mVisitorAdapter = DataAdapter.Builder<UserInfo>()
            .setData(mVisitorList)
            .setLayoutId(R.layout.item_match_visit_list)
            .addBindView { itemView, itemData, position, payloads ->
                val itemBinding = ItemMatchVisitListBinding.bind(itemView)

                if (payloads.isNotEmpty()) {
                    if (itemData.online) {
                        itemBinding.onlineStatus.setImageResource(R.drawable.pp_zx)
                    } else {
                        itemBinding.onlineStatus.setImageResource(R.drawable.pp_bzx)
                    }
                    return@addBindView
                }

                //设置view的宽度
                val layout = itemView.layoutParams
                layout.width = width * 2 / 5
                itemView.layoutParams = layout

                //设置高斯模糊的宽度
                val ly = itemBinding.flBlur.layoutParams
                ly.width = width * 2 / 5
                itemBinding.flBlur.layoutParams = ly

                //填充头像名字和年龄
                Glide.with(requireActivity()).load(itemData.avatar).apply(RequestOptions.bitmapTransform(CircleCrop())).into(itemBinding.ivAvatar)
                itemBinding.tvName.text = itemData.nickname
                itemBinding.tvAge.text = itemData.age.toString()

                //设置国家
                if (itemData.country.isNotBlank()) {
                    val flag = World.getFlagOf(itemData.country)
                    itemBinding.ivNation.setImageResource(flag)
                }

                //设置性别
                when (itemData.sex) {
                    1 -> itemBinding.ivSex.setImageResource(R.drawable.pp_xbnn)
                    2 -> itemBinding.ivSex.setImageResource(R.drawable.pp_xbn)
                }

                //填充在线状态
                if (itemData.online) {
                    itemBinding.onlineStatus.setImageResource(R.drawable.pp_zx)
                } else {
                    itemBinding.onlineStatus.setImageResource(R.drawable.pp_bzx)
                }

                if (mType == TagType.Gamer) {
                    //检查是否是解锁用户
                    if (itemData.is_unlock) {
                        itemBinding.flBlur.visibility = View.GONE
                    } else {
                        itemBinding.flBlur.visibility = View.VISIBLE
                        Glide.with(requireActivity()).load(itemData.avatar)
                            .apply(RequestOptions.bitmapTransform(BlurTransformation(requireContext(), 25, 8)))
                            .into(itemBinding.ivImageBlur)
                    }
                } else {
                    //检查是否是会员状态
                    getUserInfo { userInfo ->
                        if (userInfo.is_vip) {
                            itemBinding.flBlur.visibility = View.GONE
                        } else {
                            itemBinding.flBlur.visibility = View.VISIBLE
                            Glide.with(requireActivity()).load(itemData.avatar)
                                .apply(RequestOptions.bitmapTransform(BlurTransformation(requireContext(), 25, 8)))
                                .into(itemBinding.ivImageBlur)
                        }
                    }
                }

                itemBinding.ivVideo.setOnClickListener { requestVideoChat(itemData.uid, itemData.online) }
                itemBinding.flBlur.setOnClickListener { checkLockStatus(itemData) }
                itemView.setOnClickListener { showUserDetail(itemData.uid, itemData.online, true) }
            }
            .create()

        binding?.rcVisitList?.adapter = mVisitorAdapter
        binding?.rcVisitList?.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun initRecordList() {
        mAdapter = DataAdapter.Builder<UserInfo>()
            .setData(mList)
            .setLayoutId(R.layout.item_match_record_list)
            .addBindView { itemView, itemData, position, payloads ->
                val itemBinding = ItemMatchRecordListBinding.bind(itemView)
                if (payloads.isNotEmpty()) {
                    if (itemData.online) {
                        itemBinding.onlineStatus.setImageResource(R.drawable.pp_zx)
                    } else {
                        itemBinding.onlineStatus.setImageResource(R.drawable.pp_bzx)
                    }
                    return@addBindView
                }

                itemBinding.tvName.text = itemData.nickname
                itemBinding.tvAge.text = itemData.age.toString()

                //设置头像
                if (itemData.avatar.isNotBlank()) {
                    Glide.with(requireActivity()).load(itemData.avatar).apply(RequestOptions.bitmapTransform(CircleCrop())).into(itemBinding.ivAvatar)
                } else {
                    when (itemData.sex) {
                        1 -> itemBinding.ivAvatar.setImageResource(R.drawable.male_find)
                        2 -> itemBinding.ivAvatar.setImageResource(R.drawable.female_find)
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

                if (itemData.online) {
                    itemBinding.onlineStatus.setImageResource(R.drawable.pp_zx)
                } else {
                    itemBinding.onlineStatus.setImageResource(R.drawable.pp_bzx)
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

                itemBinding.ivIm.setOnClickListener {
                    if (!DoubleUtils.isFastDoubleClick()) {
                        requestChat(itemData.uid, itemData.nickname, itemData.avatar)
                    }
                }
            }
            .create()

        binding?.rcRecordList?.adapter = mAdapter
        binding?.rcRecordList?.layoutManager = LinearLayoutManager(requireContext())
        binding?.rcRecordList?.isLayoutFrozen = true

        binding?.rcRecordList?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val manager = recyclerView.layoutManager as LinearLayoutManager
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //获取最后一个完全显示的ItemPosition
                    val lastVisibleItem = manager.findLastCompletelyVisibleItemPosition()//从0开始
                    val totalItemCount = manager.itemCount
                    // 判断是否滚动到底部，并且是向下滚动
                    if (lastVisibleItem == (totalItemCount - 1) && isToLast) {
                        //加载更多功能的代码
                        getMatchInfoList(mPage++, mSize)
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                isToLast = dy > 0
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun changeTag() {
        when (mType) {
            TagType.Visitor -> {
                //切换成游戏访客模式
                mType = TagType.Gamer
                binding?.tvTitleTop?.text = getString(R.string.record_game_matcher)
                getGamerInfoList()
            }

            TagType.Gamer -> {
                //切换成资料访客模式
                mType = TagType.Visitor
                binding?.tvTitleTop?.text = getString(R.string.record_visitor)
                getVisitorInfoList()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getMatchInfoList(page: Int, size: Int) {
        DataManager.getMatchList(page, size) { userList ->
            binding!!.refreshLayout.finishRefresh()
            binding!!.refreshLayout.finishLoadMore()

            if (page == 0 && userList.isEmpty()) {
                binding?.tvTitleAll?.visibility = View.GONE
                binding?.includeNoData?.root?.visibility = View.VISIBLE
            }

            if (page == 0) {
                mList.clear()
            }

            if (userList.isEmpty()) {
                if (mPage > 0) {
                    mPage--
                }
            } else {
                binding?.tvTitleAll?.visibility = View.VISIBLE
            }

            mList.addAll(userList)
            mAdapter.notifyDataSetChanged()

            //subscribe
            val uidList = userList.map { it.uid.toString() }
            IMManager.subscribe(requireContext(), uidList)

            //check online status
            IMManager.checkOnlineStatus(requireContext(), uidList) { subscriberList ->
                requireActivity().runOnUiThread {
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
    private fun getVisitorInfoList() {
        //获取访客记录
        DataManager.getVisitorList { list ->
            //获取游戏匹配记录
            if (list.isEmpty()) {
                getGamerInfoList()
                return@getVisitorList
            } else {

                mVisitorList.clear()
                mVisitorList.addAll(list)
                mVisitorAdapter.notifyDataSetChanged()
            }

            //订阅用户
            val uidList = list.map { it.uid.toString() }
            IMManager.subscribe(requireContext(), uidList)

            //检查在线状态
            IMManager.checkOnlineStatus(requireContext(), uidList) { subscriberList ->
                requireActivity().runOnUiThread {
                    val newList = mVisitorList.map { it.uid }
                    for (subscriber in subscriberList) {
                        for ((position, uid) in newList.withIndex()) {
                            if (uid.toString() == subscriber.uid) {
                                mVisitorList[position].online = subscriber.online
                                mVisitorAdapter.notifyItemChanged(position, "online")
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getGamerInfoList() {
        //检查游戏匹配记录
        DataManager.getGameList { list ->
            if (list.isEmpty()) {
                mVisitorList.clear()
                mVisitorAdapter.notifyDataSetChanged()
                return@getGameList
            }

            //有记录就显示标题
            binding?.llTitleTop?.visibility = View.VISIBLE

            //刷新列表
            mVisitorList.clear()
            mVisitorList.addAll(list)
            mVisitorAdapter.notifyDataSetChanged()

            //订阅用户
            val uidList = list.map { it.uid.toString() }
            IMManager.subscribe(requireContext(), uidList)

            //检查在线状态
            IMManager.checkOnlineStatus(requireContext(), uidList) { subscriberList ->
                requireActivity().runOnUiThread {
                    val newList = mVisitorList.map { it.uid }
                    for (subscriber in subscriberList) {
                        for ((position, uid) in newList.withIndex()) {
                            if (uid.toString() == subscriber.uid) {
                                mVisitorList[position].online = subscriber.online
                                mVisitorAdapter.notifyItemChanged(position, "online")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setVisitorOnline(uid: String, online: Boolean) {
        for ((position, child) in mVisitorList.withIndex()) {
            if (child.uid.toString() == uid) {
                mVisitorList[position].online = online
                mVisitorAdapter.notifyItemChanged(position, "online")
            }
        }
    }

    private fun setMatcherOnline(uid: String, online: Boolean) {
        JLog.i("uid = $uid , online = $online")
        for ((position, child) in mList.withIndex()) {
            if (child.uid.toString() == uid) {
                mList[position].online = online
                mAdapter.notifyItemChanged(position, "online")
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

    @SuppressLint("NotifyDataSetChanged")
    private fun checkLockStatus(user: UserInfo) {
        when (mType) {
            TagType.Gamer -> {
                getUserInfo { userInfo ->
                    DataManager.getBasePrice { basePrice ->
                        UnlockDialog(requireActivity(), userInfo.coin, basePrice.common.game_unlock) {
                            DataManager.unlockGamer(user.id) { lock ->
                                if (lock) {
                                    user.is_unlock = true
                                    mVisitorAdapter.notifyDataSetChanged()
                                }
                            }
                        }
                    }
                }
            }

            TagType.Visitor -> {
                showPrimeDialog()
            }
        }
    }

    override fun flushUserInfo() {
        getUserInfo()
    }

    private fun toStorePage() {
        if (!DoubleUtils.isFastDoubleClick()) {
            startActivity(Intent(requireActivity(), StoreActivity::class.java))
        }
    }

    private fun showPrimeDialog() {
        if (!DoubleUtils.isFastDoubleClick()) {
            val userInfo = getLocalStorage().decodeParcelable("user_info", UserInfo::class.java) ?: return
            PrimeDialog(requireActivity(), userInfo.is_vip) {
                getUserInfo()
                getVisitorInfoList()
            }
        }
    }

    override fun click(v: View) {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    enum class TagType { Visitor, Gamer }
}