package com.ql.recovery.yay.ui.mine

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.blongho.country_data.World
import com.bumptech.glide.Glide
import com.netease.yunxin.kit.adapters.DataAdapter
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant
import com.netease.yunxin.kit.corekit.route.XKitRouter
import com.ql.recovery.bean.Region
import com.ql.recovery.bean.UserInfo
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.databinding.ActivityCountryBinding
import com.ql.recovery.yay.databinding.ItemCountryUpdateBinding
import com.ql.recovery.yay.manager.ImageManager
import com.ql.recovery.yay.ui.base.BaseActivity
import com.ql.recovery.yay.ui.dialog.ProfileDialog
import com.ql.recovery.yay.util.AppUtil

class CountryActivity : BaseActivity() {
    private lateinit var binding: ActivityCountryBinding
    private lateinit var mAdapter: DataAdapter<Region>
    private var mList = arrayListOf<Region>()
    private var searchList = arrayListOf<Region>()
    private var currentCountry: String? = null

    override fun getViewBinding(baseBinding: ActivityBaseBinding) {
        binding = ActivityCountryBinding.inflate(layoutInflater, baseBinding.flBase, true)
    }

    override fun initView() {
        setStatusBarLight()
        binding.includeTitle.ivBack.setOnClickListener { finish() }
        binding.includeTitle.tvOption.setOnClickListener { updateCountry() }
    }

    override fun initData() {
        binding.includeTitle.tvName.text = getString(R.string.profile_region)
        binding.includeTitle.tvOption.text = getString(R.string.profile_save)

        initFollowList()
        initSearch()
        getData()
    }

    private fun initFollowList() {
        val width = AppUtil.getScreenWidth(this)
        mAdapter = DataAdapter.Builder<Region>()
            .setData(mList)
            .setLayoutId(R.layout.item_country_update)
            .addBindView { itemView, itemData ->
                val itemBinding = ItemCountryUpdateBinding.bind(itemView)

                //设置item的宽高
                val lp = itemView.layoutParams
                lp.height = width / 4
                itemView.layoutParams = lp

                //设置icon的宽高
                val iconLp = itemBinding.ivCountryIcon.layoutParams
                iconLp.width = width / 11
                itemBinding.ivCountryIcon.layoutParams = iconLp

                val flag = World.getFlagOf(itemData.iso)
                itemBinding.ivCountryIcon.setImageResource(flag)
                itemBinding.tvCountryName.text = itemData.name

                if (itemData.iso == currentCountry) {
                    itemBinding.ivCheck.visibility = View.VISIBLE
                } else {
                    itemBinding.ivCheck.visibility = View.GONE
                }

                itemView.setOnClickListener {
                    currentCountry = itemData.iso
                    mAdapter.notifyItemRangeChanged(0, mList.size)
                }
            }
            .create()

        binding.rcCountry.adapter = mAdapter
        binding.rcCountry.layoutManager = GridLayoutManager(this, 4)
    }

    private fun getData() {
        DataManager.getRegion { regions ->
            regions.sortedBy { it.name }
            mList.clear()
            searchList.clear()
            mList.addAll(regions)
            searchList.addAll(regions)
            mAdapter.notifyItemRangeChanged(0, mList.size)

            val userInfo = getLocalStorage().decodeParcelable("user_info", UserInfo::class.java)
            if (userInfo != null) {
                currentCountry = userInfo.country
                mAdapter.notifyItemRangeChanged(0, mList.size)
            }
        }
    }

    private fun updateCountry() {
        if (currentCountry == null) return
        DataManager.updateCountry(currentCountry!!) {
            if (it) {
                setResult(0x1003)
                finish()
            }
        }
    }

    private fun initSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    val key = s.toString()
                    val list = searchList.filter { it.name.lowercase().contains(key.lowercase()) }
                    mList.clear()
                    mList.addAll(list)
                    mAdapter.notifyDataSetChanged()
                }
            }
        })
    }

}