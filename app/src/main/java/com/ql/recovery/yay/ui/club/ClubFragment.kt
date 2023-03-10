package com.ql.recovery.yay.ui.club

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.*
import com.netease.yunxin.kit.adapters.DataAdapter
import com.ql.recovery.bean.*
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.callback.FileCallback
import com.ql.recovery.yay.databinding.FragmentClubBinding
import com.ql.recovery.yay.databinding.ItemFunAnchorBinding
import com.ql.recovery.yay.manager.IMManager
import com.ql.recovery.yay.manager.RtcManager
import com.ql.recovery.yay.ui.base.BaseFragment
import com.ql.recovery.yay.ui.dialog.PrimeDialog
import com.ql.recovery.yay.ui.store.StoreActivity
import com.ql.recovery.yay.util.AppUtil
import com.ql.recovery.yay.util.DoubleUtils
import com.ql.recovery.yay.util.FileUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class ClubFragment : BaseFragment(), CoroutineScope by MainScope() {
    private var binding: FragmentClubBinding? = null
    private lateinit var mAdapter: DataAdapter<Anchor>
    private var mainList = mutableListOf<Cate>()
    private var mList = mutableListOf<Anchor>()
    private var handler = Handler(Looper.getMainLooper())

    private var currentPosition = 0
    private var page = 0
    private var pageCount = 10

    override fun initView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentClubBinding.inflate(inflater, container, false)

        binding?.llCoin?.setOnClickListener { toStorePage() }
        binding?.ivVip?.setOnClickListener { showPrimeDialog() }
        binding!!.llTitle1.setOnClickListener { changeTag("all") }
        binding!!.llTitle2.setOnClickListener { changeTag("star") }
        binding!!.llTitle3.setOnClickListener { changeTag("hot") }
        binding!!.llTitle4.setOnClickListener { changeTag("new") }

        initPicsShow()
        getTitleList()
        getLotteryBroadcastList()
        changeTag("all")

        return binding!!.root
    }

    override fun initData() {
        getUserInfo()
    }

    override fun setOnlineStatus(uid: String, online: Boolean) {
        for ((position, child) in mList.withIndex()) {
            if (child.uid.toString() == uid) {
                mList[position].online = online
                mAdapter.notifyItemChanged(position, "online")
            }
        }
    }

    private fun initPicsShow() {
        val height = AppUtil.getScreenHeight(requireActivity())
        mAdapter = DataAdapter.Builder<Anchor>()
            .setData(mList)
            .setLayoutId(R.layout.item_fun_anchor)
            .addBindView { itemView, itemData, position, payloads ->
                val itemBinding = ItemFunAnchorBinding.bind(itemView)
                val lp = itemBinding.ivPhoto.layoutParams
                lp.height = height / 4
                itemBinding.ivPhoto.layoutParams = lp

                val l = itemBinding.playerView.layoutParams
                l.height = height / 4
                itemBinding.playerView.layoutParams = l

                if (payloads.isNotEmpty()) {
                    when (payloads[0]) {
                        "online" -> {
                            if (itemData.online) {
                                itemBinding.onlineStatus.setImageResource(R.drawable.shape_round_green)
                                itemBinding.onlineDes.text = getString(R.string.club_available)
                            } else {
                                itemBinding.onlineStatus.setImageResource(R.drawable.shape_round_red)
                                itemBinding.onlineDes.text = getString(R.string.club_unavailable)
                            }
                        }
                    }

                    return@addBindView
                }

                itemBinding.tvName.text = itemData.nickname + "," + itemData.age.toString()

//                when (itemData.sex) {
//                    1 -> itemBinding.ivGender.setImageResource(R.drawable.pp_xbnn)
//                    2 -> itemBinding.ivGender.setImageResource(R.drawable.pp_xbn)
//                }

                if (itemData.online) {
                    itemBinding.onlineStatus.setImageResource(R.drawable.shape_round_green)
                    itemBinding.onlineDes.text = getString(R.string.club_available)
                } else {
                    itemBinding.onlineStatus.setImageResource(R.drawable.shape_round_red)
                    itemBinding.onlineDes.text = getString(R.string.club_unavailable)
                }

                itemView.setOnClickListener {
                    if (!DoubleUtils.isFastDoubleClick()) {
                        showUserDetail(itemData.uid, itemData.online, true)
                    }
                }

                itemBinding.ivVideo.setOnClickListener {
                    if (!DoubleUtils.isFastDoubleClick()) {
                        requestVideoChat(itemData.uid, itemData.online)
                    }
                }

            }
            .onViewAttachedToWindow { itemView, position ->
                val itemData = mList[position]
                val itemBinding = ItemFunAnchorBinding.bind(itemView)

                //???????????????????????????????????????
                if (itemData.cover_url.isNullOrBlank()) {
                    itemBinding.ivPhoto.visibility = View.VISIBLE
                    Glide.with(requireActivity()).load(itemData.avatar).placeholder(R.drawable.placeholder).into(itemBinding.ivPhoto)
                    return@onViewAttachedToWindow
                }

                //???????????????
                itemBinding.ivPhoto.visibility = View.VISIBLE
                Glide.with(requireActivity()).load(itemData.cover_url).placeholder(R.drawable.placeholder).into(itemBinding.ivPhoto)

                //?????????????????????????????????????????????????????????
                if (!itemData.local_video_url.isNullOrBlank()) {
                    launch {
                        itemBinding.playerView.initPlayer(itemData.cover_url!!)
                        itemBinding.playerView.setMediaSource(itemData.local_video_url, itemBinding)
                    }
                    return@onViewAttachedToWindow
                }

                //????????????
                if (itemData.cover_url!!.contains(".mp4") || itemData.cover_url!!.contains("type=video")) {
                    launch {
                        itemBinding.playerView.initPlayer(itemData.cover_url)
//                        itemBinding.playerView.setMediaSource(itemData.cover_url, itemBinding)
                        downloadVideo(itemBinding, itemData, position)
                    }
                } else {
                    itemBinding.ivPhoto.visibility = View.VISIBLE
                    itemBinding.playerView.visibility = View.GONE
                    Glide.with(requireActivity()).load(itemData.cover_url).placeholder(R.drawable.placeholder).into(itemBinding.ivPhoto)
                }

            }
            .onViewDetachToWindow { itemView, position ->
                val itemBinding = ItemFunAnchorBinding.bind(itemView)
                itemBinding.playerView.releasePlayer()
            }.create()


        binding!!.rcAnchorList.itemAnimator?.changeDuration = 0
        binding!!.rcAnchorList.adapter = mAdapter
        binding!!.rcAnchorList.layoutManager = GridLayoutManager(requireContext(), 2)

        binding!!.refreshLayout.setOnRefreshListener {
            page = 0
            getAnchorData()
        }

        binding!!.refreshLayout.setOnLoadMoreListener {
            page++
            getAnchorData()
        }
    }

    /**
     * ????????????
     */
    private fun downloadVideo(itemBinding: ItemFunAnchorBinding, itemData: Anchor, position: Int) {
        launch(Dispatchers.IO) {
            FileUtil.downloadPartOfVideo(requireContext(), itemData.cover_url!!, object : FileCallback {
                override fun onSuccess(filePath: String) {

                    context?.let { ctx ->
                        //????????????
                        RtcManager.cutVideo(ctx, filePath) { localPath ->
                            binding?.rcAnchorList?.post {
                                itemData.local_video_url = localPath
                                itemBinding.playerView.setMediaSource(localPath, itemBinding)
                            }
                        }
                    }

                }

                override fun onFailed(message: String) {
                }
            })
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun changeTag(type: String) {
        if (!DoubleUtils.isFastDoubleClick()) {

            //??????
            page = 0
            mList.clear()
            mAdapter.notifyDataSetChanged()

            when (type) {
                "all" -> {
                    binding!!.tvName1.setTextColor(ResourcesCompat.getColor(resources, R.color.color_black, null))
                    binding!!.tvName1.textSize = 20f
                    binding!!.ivDes1.visibility = View.VISIBLE
                    binding!!.tvName1.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                    binding!!.tvName2.setTextColor(ResourcesCompat.getColor(resources, R.color.color_content, null))
                    binding!!.tvName3.setTextColor(ResourcesCompat.getColor(resources, R.color.color_content, null))
                    binding!!.tvName4.setTextColor(ResourcesCompat.getColor(resources, R.color.color_content, null))
                    binding!!.tvName2.textSize = 15f
                    binding!!.tvName3.textSize = 15f
                    binding!!.tvName4.textSize = 15f
                    binding!!.ivDes2.visibility = View.INVISIBLE
                    binding!!.ivDes3.visibility = View.INVISIBLE
                    binding!!.ivDes4.visibility = View.INVISIBLE
                    binding!!.tvName2.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                    binding!!.tvName3.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                    binding!!.tvName4.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)

                    currentPosition = 0
                    getAnchorData()
                }

                "star" -> {
                    binding!!.tvName2.setTextColor(ResourcesCompat.getColor(resources, R.color.color_black, null))
                    binding!!.tvName2.textSize = 20f
                    binding!!.ivDes2.visibility = View.VISIBLE
                    binding!!.tvName2.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                    binding!!.tvName1.setTextColor(ResourcesCompat.getColor(resources, R.color.color_content, null))
                    binding!!.tvName3.setTextColor(ResourcesCompat.getColor(resources, R.color.color_content, null))
                    binding!!.tvName4.setTextColor(ResourcesCompat.getColor(resources, R.color.color_content, null))
                    binding!!.tvName1.textSize = 15f
                    binding!!.tvName3.textSize = 15f
                    binding!!.tvName4.textSize = 15f
                    binding!!.ivDes1.visibility = View.INVISIBLE
                    binding!!.ivDes3.visibility = View.INVISIBLE
                    binding!!.ivDes4.visibility = View.INVISIBLE
                    binding!!.tvName1.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                    binding!!.tvName3.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                    binding!!.tvName4.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)

                    currentPosition = 1
                    getAnchorData()
                }

                "hot" -> {
                    binding!!.tvName3.setTextColor(ResourcesCompat.getColor(resources, R.color.color_black, null))
                    binding!!.tvName3.textSize = 20f
                    binding!!.ivDes3.visibility = View.VISIBLE
                    binding!!.tvName3.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                    binding!!.tvName1.setTextColor(ResourcesCompat.getColor(resources, R.color.color_content, null))
                    binding!!.tvName2.setTextColor(ResourcesCompat.getColor(resources, R.color.color_content, null))
                    binding!!.tvName4.setTextColor(ResourcesCompat.getColor(resources, R.color.color_content, null))
                    binding!!.tvName1.textSize = 15f
                    binding!!.tvName2.textSize = 15f
                    binding!!.tvName4.textSize = 15f
                    binding!!.ivDes1.visibility = View.INVISIBLE
                    binding!!.ivDes2.visibility = View.INVISIBLE
                    binding!!.ivDes4.visibility = View.INVISIBLE
                    binding!!.tvName1.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                    binding!!.tvName2.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                    binding!!.tvName4.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)

                    currentPosition = 2
                    binding!!.refreshLayout.showContextMenu()
                    getAnchorData()
                }

                "new" -> {
                    binding!!.tvName4.setTextColor(ResourcesCompat.getColor(resources, R.color.color_black, null))
                    binding!!.tvName4.textSize = 20f
                    binding!!.ivDes4.visibility = View.VISIBLE
                    binding!!.tvName4.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                    binding!!.tvName1.setTextColor(ResourcesCompat.getColor(resources, R.color.color_content, null))
                    binding!!.tvName2.setTextColor(ResourcesCompat.getColor(resources, R.color.color_content, null))
                    binding!!.tvName3.setTextColor(ResourcesCompat.getColor(resources, R.color.color_content, null))
                    binding!!.tvName1.textSize = 15f
                    binding!!.tvName2.textSize = 15f
                    binding!!.tvName3.textSize = 15f
                    binding!!.ivDes1.visibility = View.INVISIBLE
                    binding!!.ivDes2.visibility = View.INVISIBLE
                    binding!!.ivDes3.visibility = View.INVISIBLE
                    binding!!.tvName1.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                    binding!!.tvName2.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                    binding!!.tvName3.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)

                    currentPosition = 3
                    getAnchorData()
                }
            }
        }
    }

    private fun getTitleList() {
        mainList.clear()
        mainList.add(Cate("all", getString(R.string.club_subtitle_01)))
        mainList.add(Cate("star", getString(R.string.club_subtitle_02)))
        mainList.add(Cate("hot", getString(R.string.club_subtitle_03)))
        mainList.add(Cate("new", getString(R.string.club_subtitle_04)))
        currentPosition = 0
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getAnchorData() {
        DataManager.getAnchorList(mainList[currentPosition].id, page, pageCount) { anchorList ->
            if (page == 0) {
                mList.clear()
                anchorList.forEach { it.isPlaying = false }
            }

            if (anchorList.isEmpty()) {
                page--
                if (page < 0) {
                    page = 0
                }
            }

            mList.addAll(anchorList)

            if (page == 0) {
                mAdapter.notifyItemRangeChanged(0, anchorList.size)
            } else {
                mAdapter.notifyItemRangeChanged(mList.size - anchorList.size, anchorList.size)
            }

            binding!!.refreshLayout.finishRefresh()
            binding!!.refreshLayout.finishLoadMore()

            val uidList = mList.map { it.uid.toString() }
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

    private fun getUserInfo() {
        DataManager.getUserInfo {
            binding?.tvCoin?.typeface = Typeface.createFromAsset(requireActivity().assets, "fonts/din_b.otf")
            binding?.tvCoin?.text = it.coin.toString()

            if (it.is_vip) {
                binding?.ivVip?.setImageResource(R.drawable.in_vip)
            } else {
                binding?.ivVip?.setImageResource(R.drawable.vip_c)
            }
        }
    }

    override fun refreshUserInfo() {
        getUserInfo()
    }

    override fun refreshOnlineTime() {}

    private fun getLotteryBroadcastList() {
        DataManager.getAnchorMessageList { list ->
            if (list.isNotEmpty()) {
                val clubFollow = list[0]
                binding?.tvBroadcast?.setText(String.format(getString(R.string.club_top_notice), clubFollow.nickname, clubFollow.target_nickname))
                broadcastLoop(list, 1)
            }
        }
    }

    private fun broadcastLoop(list: List<ClubFollow>, position: Int) {
        handler.postDelayed({
            val clubFollow = list[position]
            binding?.tvBroadcast?.setText(String.format(getString(R.string.club_top_notice), clubFollow.nickname, clubFollow.target_nickname))
            if (position == list.size - 1) {
                broadcastLoop(list, 0)
            } else {
                broadcastLoop(list, position + 1)
            }

        }, 2000L)
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
            }
        }
    }

    override fun click(v: View) {
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}