package com.ql.recovery.yay.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.netease.yunxin.kit.adapters.DataAdapter
import com.ql.recovery.bean.LotteryRecord
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.DialogAwardBinding
import com.ql.recovery.yay.databinding.ItemRewardBinding
import com.ql.recovery.yay.util.AppUtil

class RewardDialog(
    private val activity: Activity,
    private val type: String,
    private val resultList: List<LotteryRecord>,
    private val isInitiator: Boolean
) : Dialog(activity, R.style.app_dialog) {
    private lateinit var binding: DialogAwardBinding
    private lateinit var adapter: DataAdapter<LotteryRecord>

    init {
        initVew()
    }

    private fun initVew() {
        binding = DialogAwardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(true)

        when (type) {
            "coin" -> {
                var count = 0
                for (gift in resultList) {
                    count += gift.coin
                }

                binding.tvCoin.text = count.toString()
            }

            "gift" -> {
                binding.llCoin.visibility = View.GONE
                initRegionList()
            }
        }

        if (isInitiator) {
            binding.tvGet.text = activity.getString(R.string.match_game_reward_get)
        } else {
            binding.tvGet.text = activity.getString(R.string.match_game_reward_title)
        }

        binding.tvGet.setOnClickListener { cancel() }

        show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initRegionList() {
        val width = AppUtil.getScreenWidth(activity)
        adapter = DataAdapter.Builder<LotteryRecord>()
            .setData(resultList)
            .setLayoutId(R.layout.item_reward)
            .addBindView { itemView, itemData ->
                val itemBinding = ItemRewardBinding.bind(itemView)

                if (resultList.size == 1) {
                    val lp = itemBinding.ivImg.layoutParams
                    lp.width = width / 3
                    lp.height = width / 3
                    itemBinding.ivImg.layoutParams = lp
                } else {
                    val lp = itemBinding.ivImg.layoutParams
                    lp.width = width / 6
                    lp.height = width / 6
                    itemBinding.ivImg.layoutParams = lp
                }

                itemBinding.tvCoin.text = itemData.coin.toString()
                Glide.with(activity).load(itemData.icon).into(itemBinding.ivImg)
            }
            .create()

        binding.rcGift.adapter = adapter
        if (resultList.size < 5) {
            binding.rcGift.layoutManager = GridLayoutManager(activity, resultList.size)
        } else {
            binding.rcGift.layoutManager = GridLayoutManager(activity, 4)
        }
        adapter.notifyDataSetChanged()
    }

    override fun show() {
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.CENTER
            width = AppUtil.getScreenWidth(context) * 9 / 10
            height = WindowManager.LayoutParams.WRAP_CONTENT
            flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
            dimAmount = 0.5f
        }
        super.show()
    }

    /**
     * 动态设置Activity背景透明度
     *
     * @param bgAlpha
     */
    fun setWindowAlpha(bgAlpha: Float) {
        val window: Window = activity.window
        val lp = window.attributes
        lp.alpha = bgAlpha
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.attributes = lp
    }

}