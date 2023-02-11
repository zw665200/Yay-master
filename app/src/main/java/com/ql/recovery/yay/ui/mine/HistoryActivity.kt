package com.ql.recovery.yay.ui.mine

import android.annotation.SuppressLint
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.ql.recovery.bean.ReChargeRecord
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.adapter.DataAdapter
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.databinding.ActivityHistoryBinding
import com.ql.recovery.yay.databinding.ItemHistoryBinding
import com.ql.recovery.yay.ui.base.BaseActivity
import com.ql.recovery.yay.util.AppUtil

class HistoryActivity : BaseActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private lateinit var adapter: DataAdapter<ReChargeRecord>
    private var mList = arrayListOf<ReChargeRecord>()
    private var page = 0
    private val size = 100

    override fun getViewBinding(baseBinding: ActivityBaseBinding) {
        binding = ActivityHistoryBinding.inflate(layoutInflater, baseBinding.flBase, true)
    }

    override fun initView() {
        setStatusBarLight()
        binding.ivBack.setOnClickListener { finish() }
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        checkUserInfo()
        initAdapter()
        getTopUpRecords()
    }

    private fun checkUserInfo() {
        DataManager.getTopUpCount { coin ->
            binding.tvCoin.text = coin.toString()
        }
    }

    private fun initAdapter() {
        adapter = DataAdapter.Builder<ReChargeRecord>()
            .setData(mList)
            .setLayoutId(R.layout.item_history)
            .addBindView { itemView, itemData ->
                val itemBinding = ItemHistoryBinding.bind(itemView)
                itemBinding.tvTime.text = AppUtil.timeStamp2Date(itemData.date, "")
                itemBinding.tvCoin.text = itemData.amount.toString()
            }
            .create()

        binding.rcData.adapter = adapter
        binding.rcData.layoutManager = LinearLayoutManager(this)
    }

    private fun getTopUpRecords() {
        DataManager.getTopUpRecords(page, size) {
            if (it.isEmpty()) {
                binding.llNoData.visibility = View.VISIBLE
            } else {
                binding.llNoData.visibility = View.GONE
            }

            mList.clear()
            mList.addAll(it)
            adapter.notifyItemRangeChanged(0, mList.size)
        }
    }

}