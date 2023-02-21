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
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.*
import com.netease.yunxin.kit.adapters.DataAdapter
import com.ql.recovery.bean.Anchor
import com.ql.recovery.bean.Cate
import com.ql.recovery.bean.UserInfo
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
import com.ql.recovery.yay.util.JLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.math.abs

class ClubFragment : BaseFragment(), CoroutineScope by MainScope() {
    private var binding: FragmentClubBinding? = null
    private lateinit var mAdapter: DataAdapter<Anchor>
    private var mainList = mutableListOf<Cate>()
    private var mList = mutableListOf<Anchor>()
    private var exoPlayerList = arrayListOf<ExoPlayer?>()
    private var handler = Handler(Looper.getMainLooper())

    private var currentPosition = 0
    private var page = 0
    private var pageCount = 6
    private var isToLast = false
    private var isFirstLoad = true
    private var firstVisibleViewPosition = 0
    private var lastVisibleViewPosition = 5

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

    private fun getPlayer(): ExoPlayer {
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(1000, 3000, 250, 500)
            .build()

        val exoPlayer = ExoPlayer.Builder(requireContext())
            .setRenderersFactory(DefaultRenderersFactory(requireContext()).setEnableDecoderFallback(true))
            .setLoadControl(loadControl)
            .build()

        exoPlayer.volume = 0f
        exoPlayer.repeatMode = Player.REPEAT_MODE_ALL

        return exoPlayer
    }

    private fun initPicsShow() {
        val width = AppUtil.getScreenWidth(requireContext())
        val height = AppUtil.getScreenHeight(requireActivity())
        mAdapter = DataAdapter.Builder<Anchor>()
            .setData(mList)
            .setLayoutId(R.layout.item_fun_anchor)
            .addBindView { itemView, itemData, position, payloads ->
                val itemBinding = ItemFunAnchorBinding.bind(itemView)
                val lp = itemBinding.ivPhoto.layoutParams
                lp.height = height / 5
                itemBinding.ivPhoto.layoutParams = lp

                val l = itemBinding.playerView.layoutParams
                l.height = height / 5
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

                itemBinding.tvName.text = itemData.nickname
                itemBinding.tvAge.text = itemData.age.toString()

//                itemBinding.ivPhoto.setImageResource(R.color.white)
                Glide.with(requireActivity()).load(itemData.cover_url).into(itemBinding.ivPhoto)

                when (itemData.sex) {
                    1 -> itemBinding.ivGender.setImageResource(R.drawable.pp_xbnn)
                    2 -> itemBinding.ivGender.setImageResource(R.drawable.pp_xbn)
                }

                if (itemData.online) {
                    itemBinding.onlineStatus.setImageResource(R.drawable.shape_round_green)
                    itemBinding.onlineDes.text = getString(R.string.club_available)
                } else {
                    itemBinding.onlineStatus.setImageResource(R.drawable.shape_round_red)
                    itemBinding.onlineDes.text = getString(R.string.club_unavailable)
                }

                if (position < firstVisibleViewPosition || position > lastVisibleViewPosition) {
                    itemBinding.playerView.player = null
                    itemBinding.ivPhoto.visibility = View.VISIBLE

                    launch {
                        if (exoPlayerList.size > position && exoPlayerList[position] != null) {
                            exoPlayerList[position]?.stop()
                            exoPlayerList[position]?.release()
                            exoPlayerList[position] = null
                        }
                    }

                    return@addBindView
                }

                var exoPlayer: ExoPlayer? = null
                if (exoPlayerList.size > position) {
                    if (exoPlayerList[position] == null) {
                        exoPlayer = getPlayer()
                        exoPlayerList[position] = exoPlayer
                    } else {
                        launch {
                            itemBinding.playerView.player = null
                            itemBinding.ivPhoto.visibility = View.VISIBLE
                            exoPlayer = exoPlayerList[position]
                            exoPlayer?.stop()
                            exoPlayer?.release()
                            exoPlayer = getPlayer()
                            exoPlayerList[position] = exoPlayer
                        }
                    }
                } else {
                    exoPlayer = getPlayer()
                    exoPlayerList.add(exoPlayer)
                }

                if (itemData.cover_url != null && exoPlayer != null) {
                    loadVideo(exoPlayer!!, itemBinding, itemData, position)
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
            .create()

        binding!!.rcAnchorList.adapter = mAdapter
        binding!!.rcAnchorList.layoutManager = GridLayoutManager(requireContext(), 2)
        binding!!.rcAnchorList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val manager = recyclerView.layoutManager as GridLayoutManager
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                    //获取最后一个完全显示的ItemPosition
                    val firstVisiblePosition = manager.findFirstVisibleItemPosition()//从0开始
                    val lastVisiblePosition = manager.findLastVisibleItemPosition()//从0开始
                    val count = manager.itemCount

                    JLog.i("first = $firstVisiblePosition")
                    JLog.i("last = $lastVisiblePosition")

                    if (firstVisibleViewPosition != firstVisiblePosition) {
                        mAdapter.notifyItemRangeChanged(0, abs(firstVisibleViewPosition - firstVisiblePosition))
                    }

                    if (lastVisibleViewPosition != lastVisiblePosition) {
                        if (lastVisibleViewPosition < count) {
                            mAdapter.notifyItemRangeChanged(lastVisibleViewPosition + 1, abs(lastVisibleViewPosition - lastVisiblePosition))
                        }
                    }

                    firstVisibleViewPosition = firstVisiblePosition
                    lastVisibleViewPosition = lastVisiblePosition
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                isToLast = dy > 0
            }
        })

        binding!!.refreshLayout.setOnRefreshListener {
            isFirstLoad = true
            page = 0
            getAnchorData()
        }

        binding!!.refreshLayout.setOnLoadMoreListener {
            page++
            getAnchorData()
        }
    }

    private fun loadVideo(exoPlayer: ExoPlayer, itemBinding: ItemFunAnchorBinding, itemData: Anchor, position: Int) {
        if (itemData.cover_url!!.contains(".mp4") || itemData.cover_url!!.contains("type=video")) {

            exoPlayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    when (playbackState) {
                        Player.STATE_READY -> {
                            itemBinding.playerView.visibility = View.VISIBLE
                            itemBinding.playerView.player = exoPlayer
                            exoPlayer.play()
                        }
                    }
                }

                override fun onRenderedFirstFrame() {
                    super.onRenderedFirstFrame()

                    itemBinding.ivPhoto.visibility = View.INVISIBLE
                }

                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    JLog.i("onPlayerError")
                    itemBinding.ivPhoto.visibility = View.VISIBLE
                }
            })

            launch(Dispatchers.IO) {
                FileUtil.downloadVideo(requireContext(), itemData.cover_url!!, object : FileCallback {
                    override fun onSuccess(filePath: String) {
                        RtcManager.cutVideo(requireActivity(), filePath) {
                            handler.post {
                                val mediaItem = MediaItem.Builder()
                                    .setMediaId(itemData.cover_url!!)
                                    .setUri(it)
                                    .build()

                                exoPlayer.setMediaItem(mediaItem)
                                exoPlayer.prepare()
                            }

                        }

                    }

                    override fun onFailed(message: String) {
                    }
                })
            }

        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun changeTag(type: String) {
        if (!DoubleUtils.isFastDoubleClick()) {

            //清屏
            isFirstLoad = true
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
            binding!!.refreshLayout.finishRefresh()
            binding!!.refreshLayout.finishLoadMore()

            if (page == 0) {
                mList.clear()
                anchorList.forEach { it.isPlaying = false }
            }

            if (anchorList.isEmpty()) {
                page--
                if (page < 0) {
                    page = 0
                }
                return@getAnchorList
            }

            mList.addAll(anchorList)
            mAdapter.notifyDataSetChanged()

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
            binding?.tvCoin?.typeface = Typeface.createFromAsset(requireActivity().assets, "fonts/DINPro-Bold.otf")
            binding?.tvCoin?.text = it.coin.toString()

            if (it.is_vip) {
                binding?.ivVip?.setImageResource(R.drawable.in_vip)
            } else {
                binding?.ivVip?.setImageResource(R.drawable.vip_c)
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
            }
        }
    }

    override fun click(v: View) {
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null

        for (exoplayer in exoPlayerList) {
            exoplayer?.release()
        }

        exoPlayerList.clear()
    }
}