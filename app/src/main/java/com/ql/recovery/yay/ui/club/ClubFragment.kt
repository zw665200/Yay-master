package com.ql.recovery.yay.ui.club

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
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
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.netease.yunxin.kit.adapters.DataAdapter
import com.ql.recovery.bean.Anchor
import com.ql.recovery.bean.Cate
import com.ql.recovery.bean.UserInfo
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.BaseApp
import com.ql.recovery.yay.R
import com.ql.recovery.yay.callback.FileCallback
import com.ql.recovery.yay.databinding.FragmentClubBinding
import com.ql.recovery.yay.databinding.ItemFunAnchorBinding
import com.ql.recovery.yay.manager.IMManager
import com.ql.recovery.yay.manager.ImageManager
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
import java.io.File


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
    private var isToFirst = false
    private var isToLast = false
    private var isFirstLoad = true
    private var firstVisibleViewPosition = 0
    private var currentFirstVisibleViewPosition = 0
    private var lastVisibleViewPosition = 5
    private var currentLastVisibleViewPosition = 5

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
            .setBufferDurationsMs(3000, 4000, 250, 500)
            .build()

//        val mediaSourceFactory = DefaultMediaSourceFactory(requireContext())
//            .setDataSourceFactory(cacheDataSourceFactory)
//            .setLocalAdInsertionComponents(adsLoaderProvider, playerView)

        val exoPlayer = ExoPlayer.Builder(requireContext())
            .setRenderersFactory(DefaultRenderersFactory(requireContext()).setEnableDecoderFallback(true))
            .setMediaSourceFactory(DefaultMediaSourceFactory(requireContext()))
            .setLoadControl(loadControl)
            .build()

        exoPlayer.volume = 0f
        exoPlayer.repeatMode = Player.REPEAT_MODE_ALL

        return exoPlayer
    }

    private fun initPicsShow() {
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

                        "refresh_video_add" -> {
                            if (itemData.cover_url!!.contains(".mp4") || itemData.cover_url!!.contains("type=video")) {
//                                JLog.i("local_video = " + itemData.cover_url + " : $position")
                                itemBinding.ivPhoto.visibility = View.VISIBLE

                                launch {
                                    if (exoPlayerList[position] == null) {
                                        exoPlayerList[position] = getPlayer()
                                    }

//                                    val uri = BaseApp.getProxy(requireContext())?.getProxyUrl(itemData.cover_url)

                                    val uri = Uri.parse(itemData.cover_url)
                                    val mediaItem = MediaItem.Builder()
                                        .setUri(uri)
                                        .build()

                                    val mediaSource = ProgressiveMediaSource
                                        .Factory(DefaultDataSourceFactory(requireContext(), "exoplayer"))
                                        .createMediaSource(mediaItem)

                                    if (exoPlayerList[position] != null) {
                                        exoPlayerList[position]!!.setMediaSource(mediaSource)
                                        exoPlayerList[position]!!.prepare()

                                        loadVideo(exoPlayerList[position]!!, itemBinding, position)
                                    }
                                }
                            }
                        }
                    }

                    return@addBindView
                }

                itemBinding.tvName.text = itemData.nickname
                itemBinding.tvAge.text = itemData.age.toString()

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

                if (!itemData.cover_url.isNullOrBlank()) {
                    itemBinding.ivPhoto.visibility = View.VISIBLE
                    Glide.with(requireActivity()).load(itemData.cover_url).placeholder(R.drawable.placeholder).into(itemBinding.ivPhoto)

                    if (itemData.cover_url!!.contains(".mp4") || itemData.cover_url!!.contains("type=video")) {
                        itemBinding.ivPhoto.visibility = View.INVISIBLE
                        itemBinding.playerView.visibility = View.VISIBLE

                        launch {
//                            val uri = BaseApp.getProxy(requireContext())?.getProxyUrl(itemData.cover_url)

                            val uri = Uri.parse(itemData.cover_url)

                            val mediaItem = MediaItem.Builder()
                                .setUri(uri)
                                .build()

                            val mediaSource = ProgressiveMediaSource
                                .Factory(DefaultDataSourceFactory(requireContext(), "exoplayer"))
                                .createMediaSource(mediaItem)

                            itemBinding.playerView.setMediaSource(mediaSource)
                        }

                    }
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
            .onViewAttachedToWindow { itemView ->
                JLog.i("add view")
                val itemBinding = ItemFunAnchorBinding.bind(itemView)
                itemBinding.playerView.initPlayer()
            }
            .onViewDetachToWindow { itemView ->
                JLog.i("remove view")
                val itemBinding = ItemFunAnchorBinding.bind(itemView)
                itemBinding.playerView.releasePlayer()
            }

            .create()


        binding!!.rcAnchorList.itemAnimator?.changeDuration = 0
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

                    currentFirstVisibleViewPosition = firstVisiblePosition
                    currentLastVisibleViewPosition = lastVisiblePosition

//                    for (index in 0 until firstVisiblePosition) {
//                        launch {
//                            if (exoPlayerList[index] != null) {
//                                exoPlayerList[index]?.release()
//                                exoPlayerList[index] = null
//                            }
//
//                            mList[index].isPlaying = false
//                            mAdapter.notifyItemChanged(index)
//                        }
//                    }
//
//                    for (index in lastVisiblePosition until count) {
//                        launch {
//                            if (exoPlayerList[index] != null) {
//                                exoPlayerList[index]?.release()
//                                exoPlayerList[index] = null
//                            }
//
//                            mList[index].isPlaying = false
//                            mAdapter.notifyItemChanged(index)
//                        }
//                    }


//                    if (firstVisibleViewPosition != firstVisiblePosition) {
//                        val start = firstVisibleViewPosition
//                        if (isToLast) {
//                            val end = start + firstVisiblePosition - firstVisibleViewPosition
//                            for (index in 0 until end) {
//                                binding?.rcAnchorList?.post {
//                                    JLog.i("下拉 remove $index")
//
//                                    launch {
//                                        if (exoPlayerList[index] != null) {
//                                            exoPlayerList[index]?.release()
//                                            exoPlayerList[index] = null
//                                        }
//
//                                        mList[index].isPlaying = false
//                                        mAdapter.notifyItemChanged(index)
//                                    }
//                                }
//                            }
//                        }
//
//                        if (isToFirst) {
//                            val end = start + firstVisiblePosition - firstVisibleViewPosition
//                            for (index in end until start) {
//                                binding?.rcAnchorList?.post {
//                                    JLog.i("上拉 add $index")
//                                    mList[index].isPlaying = true
//                                    mAdapter.notifyItemChanged(index)
////                                    mAdapter.notifyItemChanged(index, "refresh_video_add")
//                                }
//                            }
//                        }
//                    }
//
//                    if (lastVisibleViewPosition != lastVisiblePosition) {
//                        if (lastVisibleViewPosition < count) {
//                            val start = lastVisibleViewPosition + 1
//                            if (isToLast) {
//                                val end = start + lastVisiblePosition - lastVisibleViewPosition
//                                for (index in start until end) {
//                                    JLog.i("下拉 add $index")
//                                    mList[index].isPlaying = true
//                                    mAdapter.notifyItemChanged(index)
////                                    mAdapter.notifyItemChanged(index, "refresh_video_add")
//                                }
//                            }
//
//                            if (isToFirst) {
//                                val end = start + lastVisibleViewPosition - lastVisiblePosition
//                                for (index in end until start) {
//                                    JLog.i("上拉 remove $index")
//
//                                    launch {
//                                        if (exoPlayerList[index] != null) {
//                                            exoPlayerList[index]?.release()
//                                            exoPlayerList[index] = null
//                                        }
//                                    }
//
//                                    mList[index].isPlaying = false
//                                    mAdapter.notifyItemChanged(index)
//                                }
//
//                            }
//                        }
//                    }

                    firstVisibleViewPosition = firstVisiblePosition
                    lastVisibleViewPosition = lastVisiblePosition

                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                isToFirst = dy < 0
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

    /**
     * 下载视频
     */
    private fun downloadVideo(itemData: Anchor, position: Int) {
        launch(Dispatchers.IO) {
            FileUtil.downloadPartOfVideo(requireContext(), itemData.cover_url!!, object : FileCallback {
                override fun onSuccess(filePath: String) {
                    //裁剪视频
//                    RtcManager.cutVideo(requireActivity(), filePath) { localPath ->
                    binding?.rcAnchorList?.post {
                        itemData.local_video_url = filePath
//                        mAdapter.notifyItemChanged(position, "refresh_video")
                    }
//                    }
                }

                override fun onFailed(message: String) {
                }
            })
        }
    }

    /**
     * 下载图片
     */
    private fun downloadPic(itemBinding: ItemFunAnchorBinding, itemData: Anchor, position: Int) {
        val file = requireContext().getExternalFilesDir("video")
        if (file != null) {
            val target = file.absolutePath + File.separator + AppUtil.MD5Encode(itemData.cover_url!!) + ".jpg"
            if (File(target).exists()) {
                binding?.rcAnchorList?.post {
                    Glide.with(requireActivity()).load(itemData.cover_url).into(itemBinding.ivPhoto)
                }
                return
            }
        }

        ImageManager.getBitmap(requireContext(), itemData.cover_url!!) {
            FileUtil.saveImageToCache(requireContext(), it, itemData.cover_url!!, object : FileCallback {
                override fun onSuccess(filePath: String) {
                    JLog.i("filePath = $filePath")
                    binding?.rcAnchorList?.post {
                        itemData.local_pic_url = filePath
                        mAdapter.notifyItemChanged(position, "refresh_pic")
                    }
                }

                override fun onFailed(message: String) {
                }
            })
        }
    }

    private fun loadVideo(exoPlayer: ExoPlayer, itemBinding: ItemFunAnchorBinding, position: Int) {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                when (playbackState) {
                    Player.STATE_READY -> {
                        while (exoPlayer.currentPosition < 3000) {
                            try {
                                Thread.sleep(100L)
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }
                        }

//                        val endPos = Math.min(exoPlayer.currentPosition, 3000)
//                        val cache = SimpleCache(File(requireContext().externalCacheDir, "video-cache"), NoOpCacheEvictor())
//                        val cacheDataSourceFactory = CacheDataSourceFactory(cache, dataSourceFactory)

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
                JLog.i("onPlayerError : $position")
                itemBinding.ivPhoto.visibility = View.VISIBLE
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun changeTag(type: String) {
        if (!DoubleUtils.isFastDoubleClick()) {

            //清屏
            isFirstLoad = true
            page = 0
            mList.clear()
            mAdapter.notifyDataSetChanged()

            firstVisibleViewPosition = 0
            currentFirstVisibleViewPosition = 0
            lastVisibleViewPosition = 5
            currentLastVisibleViewPosition = 5

            launch {
                for (exoPlayer in exoPlayerList) {
                    exoPlayer?.release()
                }
                exoPlayerList.clear()
            }

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

            exoPlayerList.clear()
            for (index in 0 until mList.size) {
                exoPlayerList.add(null)
            }

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