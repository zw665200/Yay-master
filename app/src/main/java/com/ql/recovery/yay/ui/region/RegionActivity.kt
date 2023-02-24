package com.ql.recovery.yay.ui.region

import android.annotation.SuppressLint
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.ql.recovery.yay.R
import com.netease.yunxin.kit.adapters.DataAdapter
import com.netease.yunxin.kit.adapters.MutableDataAdapter
import com.ql.recovery.bean.Region
import com.ql.recovery.bean.Regions
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.databinding.ActivityRegionBinding
import com.ql.recovery.yay.databinding.ItemRegionContentBinding
import com.ql.recovery.yay.databinding.ItemRegionIndexBinding
import com.ql.recovery.yay.databinding.ItemRegionTitleBinding
import com.ql.recovery.yay.manager.CManager
import com.ql.recovery.yay.ui.base.BaseActivity
import com.ql.recovery.yay.ui.dialog.WaitingDialog
import com.ql.recovery.yay.util.JLog

class RegionActivity : BaseActivity() {
    private lateinit var binding: ActivityRegionBinding
    private lateinit var adapter: MutableDataAdapter<Region>
    private var regionList = arrayListOf<Region>()
    private var searchList = arrayListOf<Region>()
    private var indexList = arrayListOf<String>()
    private var waitingDialog: WaitingDialog? = null

    override fun getViewBinding(baseBinding: ActivityBaseBinding) {
        binding = ActivityRegionBinding.inflate(layoutInflater, baseBinding.flBase, true)
    }

    override fun initView() {
        binding.includeHead.tvName.text = getString(R.string.region_title)
        binding.includeHead.ivBack.setOnClickListener { finish() }
    }

    override fun initData() {
        waitingDialog = WaitingDialog(this)

        initIndexList()
        initRegionList()
        initSearch()
    }

    private fun initIndexList() {
        val adapter = DataAdapter.Builder<String>()
            .setLayoutId(R.layout.item_region_index)
            .setData(indexList)
            .addBindView { itemView, itemData ->
                val title = itemView.findViewById<TextView>(R.id.tv_index_name)
                title.text = itemData

                itemView.setOnClickListener {
                    val region = regionList.find { it.name == itemData }
                    if (region != null) {
                        val index = regionList.indexOf(region)
                        if (index != -1) {
                            (binding.rcContent.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(index, 0)
                        }
                    }
                }
            }
            .create()

        indexList.addAll(CManager.getRegionIndexList())
        binding.rcIndex.adapter = adapter
        binding.rcIndex.layoutManager = LinearLayoutManager(this)
        adapter.notifyItemRangeChanged(0, indexList.size)
    }

    private fun initRegionList() {
        adapter = MutableDataAdapter.Builder<Region>()
            .setLayoutId(R.layout.item_region_title, R.layout.item_region_content)
            .setViewType { position -> regionList[position].type }
            .addBindType("title", "content")
            .setData(regionList)
            .addBindView { itemView, itemData ->
                when (itemData.type) {
                    "title" -> {
                        val itemBinding = ItemRegionTitleBinding.bind(itemView)
                        itemBinding.tvIndexName.text = itemData.name
                    }

                    "content" -> {
                        val itemBinding = ItemRegionContentBinding.bind(itemView)
                        itemBinding.tvRegionName.text = String.format(getString(R.string.region_name), itemData.name, itemData.phone_code)
                    }
                }

                itemView.setOnClickListener {
                    val intent = Intent()
                    intent.putExtra("phone_code", itemData.phone_code)
                    intent.putExtra("phone_iso", itemData.iso)
                    setResult(0x1, intent)
                    finish()
                }
            }
            .create()

        binding.rcContent.adapter = adapter
        binding.rcContent.layoutManager = LinearLayoutManager(this)

        waitingDialog?.show()
        DataManager.getRegion { regions ->
            waitingDialog?.cancel()

             regions.forEach { it.type = "content" }
            regions.sortedBy { it.name }

            for (index in indexList) {
                val region = Region(0, index, "", "", "title")
                regionList.add(region)
                regionList.addAll(regions.filter { it.name.startsWith(index) })
            }

            searchList.addAll(regionList)
            adapter.notifyItemRangeChanged(0, regionList.size)
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
                    var list = searchList.filter { it.name.contains(key) }
                    if (list.isEmpty()) {
                        list = searchList.filter { it.phone_code.contains(key) }
                    }
                    regionList.clear()
                    regionList.addAll(list)
                    adapter.notifyDataSetChanged()
                }
            }
        })
    }

}