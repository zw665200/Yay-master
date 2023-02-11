package com.ql.recovery.yay.ui.mine

import android.annotation.SuppressLint
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.ql.recovery.yay.R
import com.ql.recovery.yay.adapter.DataAdapter
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.databinding.ActivityImageBinding
import com.ql.recovery.yay.databinding.ItemBannerPicBinding
import com.ql.recovery.yay.ui.base.BaseActivity
import com.ql.recovery.yay.ui.self.ScaleInTransformer

class ImagePreviewActivity : BaseActivity() {
    private lateinit var binding: ActivityImageBinding
    private lateinit var mAdapter: DataAdapter<String>

    override fun getViewBinding(baseBinding: ActivityBaseBinding) {
        binding = ActivityImageBinding.inflate(layoutInflater, baseBinding.flBase, true)
    }

    override fun initView() {
    }

    override fun initData() {
        val list = intent.getStringArrayListExtra("pic_list")
        if (list != null) {
            initBannerViewPager(list)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initBannerViewPager(mainPics: List<String>) {
        binding.vpBanner.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        mAdapter = DataAdapter.Builder<String>()
            .setData(mainPics)
            .setLayoutId(R.layout.item_banner_pic)
            .addBindView { itemView, itemData ->
                val itemBinding = ItemBannerPicBinding.bind(itemView)
                Glide.with(this).load(itemData).into(itemBinding.ivBanner)
            }
            .create()

        binding.vpBanner.apply {
            offscreenPageLimit = 2
//            (getChildAt(0) as RecyclerView).apply {
//                val padding = resources.getDimensionPixelOffset(R.dimen.dp_15)
//                // setting padding on inner RecyclerView puts overscroll effect in the right place
//                setPadding(padding, 0, padding, 0)
//                clipToPadding = false
//            }
        }

        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(ScaleInTransformer())
        compositePageTransformer.addTransformer(MarginPageTransformer(resources.getDimension(R.dimen.dp_5).toInt()))
        binding.vpBanner.setPageTransformer(compositePageTransformer)
        binding.vpBanner.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        val position = intent.getIntExtra("position", 0)
        binding.vpBanner.setCurrentItem(position, false)

    }

}