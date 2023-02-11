package com.ql.recovery.yay.ui.mine

import android.annotation.SuppressLint
import android.widget.FrameLayout
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.ql.recovery.yay.R
import com.ql.recovery.yay.adapter.DataAdapter
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.databinding.ActivityImageBinding
import com.ql.recovery.yay.databinding.ItemBannerVideoBinding
import com.ql.recovery.yay.ui.base.BaseActivity
import com.ql.recovery.yay.ui.self.ScaleInTransformer
import com.ql.recovery.yay.util.AppUtil
import com.ql.recovery.yay.util.JLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class VideoPreviewActivity : BaseActivity(), CoroutineScope by MainScope() {
    private lateinit var binding: ActivityImageBinding
    private lateinit var mAdapter: DataAdapter<String>
    private var exoPlayer: ExoPlayer? = null
    private var currentPos = 0

    override fun getViewBinding(baseBinding: ActivityBaseBinding) {
        binding = ActivityImageBinding.inflate(layoutInflater, baseBinding.flBase, true)
    }

    override fun initView() {
    }

    override fun initData() {
        val list = intent.getStringArrayListExtra("video_list")
        currentPos = intent.getIntExtra("position", 0)
        if (list != null) {
            exoPlayer = ExoPlayer.Builder(this).build()
            initBannerViewPager(list)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initBannerViewPager(list: List<String>) {
        val width = AppUtil.getScreenWidth(this)
        mAdapter = DataAdapter.Builder<String>()
            .setData(list)
            .setLayoutId(R.layout.item_banner_video)
            .addBindView { itemView, itemData, position ->
                val itemBinding = ItemBannerVideoBinding.bind(itemView)

                if (position == currentPos) {
                    val playerView = PlayerView(this)
                    playerView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
                    playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH

                    itemBinding.root.removeAllViews()
                    itemBinding.root.addView(playerView)

                    playerView.player = exoPlayer

                    launch {
                        if (exoPlayer?.isPlaying == true) {
                            exoPlayer?.stop()
                        }

                        val mediaItem = MediaItem.fromUri(itemData)
                        exoPlayer?.setMediaItem(mediaItem)
                        exoPlayer?.prepare()
                    }
                }

            }
            .create()

//        binding.vpBanner.offscreenPageLimit = list.size

        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(ScaleInTransformer())
//        binding.vpBanner.setPageTransformer(compositePageTransformer)
        binding.vpBanner.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        binding.vpBanner.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.vpBanner.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPos = position
                mAdapter.notifyDataSetChanged()

                JLog.i("position = $position")
            }
        })

        binding.vpBanner.setCurrentItem(currentPos, false)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
        exoPlayer?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
    }

}