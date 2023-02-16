package com.ql.recovery.yay.ui.mine

import android.annotation.SuppressLint
import android.content.Intent
import android.view.Gravity
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.ql.recovery.bean.Income
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.adapter.MutableDataAdapter
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.databinding.ActivityEarnBinding
import com.ql.recovery.yay.databinding.ItemEarnGiftBinding
import com.ql.recovery.yay.databinding.ItemEarnGiftPersonBinding
import com.ql.recovery.yay.ui.base.BaseActivity
import com.ql.recovery.yay.ui.dialog.RechargeDialog
import com.ql.recovery.yay.ui.pop.QuestionPop
import com.ql.recovery.yay.util.AppUtil

class IncomeActivity : BaseActivity() {
    private lateinit var binding: ActivityEarnBinding
    private lateinit var adapter: MutableDataAdapter<Income>
    private var mList = arrayListOf<Income>()
    private var giftList = arrayListOf<Income>()
    private var videoList = arrayListOf<Income>()
    private var gameList = arrayListOf<Income>()
    private var mRole: String? = null
    private var mType: String? = null

    override fun getViewBinding(baseBinding: ActivityBaseBinding) {
        binding = ActivityEarnBinding.inflate(layoutInflater, baseBinding.flBase, true)
    }

    override fun initView() {
        setStatusBarLight()
        binding.ivBack.setOnClickListener { finish() }
        binding.tvHistory.setOnClickListener { toHistoryPage() }
        binding.tvGift.setOnClickListener { getEarnDetail("gift", false) }
        binding.tvVideo.setOnClickListener { getEarnDetail("video", false) }
        binding.tvGame.setOnClickListener { getEarnDetail("game", false) }
        binding.tvRechage.setOnClickListener { recharge() }
        binding.ivQ.setOnClickListener { showQuestionPop(binding.ivQ) }
    }

    override fun initData() {
        checkUserInfo()
    }

    private fun checkUserInfo() {
        DataManager.getUserInfo {
            mRole = it.role
            mType = "gift"

            initAdapter()

            binding.tvCoin.text = it.coin_income.toString()

            if (mRole == "anchor") {
                binding.tvName.text = getString(R.string.earn_title)
                binding.tvVideo.visibility = View.VISIBLE
            } else {
                binding.tvName.text = getString(R.string.earn_title_2)
                binding.tvVideo.visibility = View.GONE
                binding.llGift.visibility = View.GONE
                binding.llVideo.visibility = View.GONE
                binding.llGame.visibility = View.GONE
            }

            getData(mRole!!)
        }
    }

    private fun initAdapter() {
        val width = AppUtil.getScreenWidth(this)
        adapter = MutableDataAdapter.Builder<Income>()
            .setData(mList)
            .setLayoutId(R.layout.item_earn_gift, R.layout.item_earn_gift_person)
            .addBindType("anchor", "normal")
            .setViewType { mRole!! }
            .addBindView { itemView, itemData ->
                when (mRole) {
                    "anchor" -> {
                        val itemBinding = ItemEarnGiftBinding.bind(itemView)

                        when (mType) {
                            "gift" -> {
                                val l = itemBinding.ivIcon.layoutParams
                                l.width = width / 7
                                itemBinding.ivIcon.layoutParams = l

                                itemBinding.ivAvatar.visibility = View.VISIBLE
                                itemBinding.tvName.visibility = View.VISIBLE
                                itemBinding.viewMargin.visibility = View.VISIBLE

                                Glide.with(this).load(itemData.icon).into(itemBinding.ivIcon)
                                itemBinding.tvName.text = String.format(getString(R.string.earn_count), itemData.number)
                            }

                            "video" -> {
                                val l = itemBinding.ivIcon.layoutParams
                                l.width = width / 7
                                itemBinding.ivIcon.layoutParams = l

                                itemBinding.ivAvatar.visibility = View.VISIBLE
                                itemBinding.tvName.visibility = View.VISIBLE
                                itemBinding.viewMargin.visibility = View.VISIBLE

                                itemBinding.ivIcon.setImageResource(R.drawable.pp_union)
                                itemBinding.tvName.text = String.format(getString(R.string.earn_duration), itemData.duration)
                            }

                            "game" -> {
                                val l = itemBinding.ivIcon.layoutParams
                                l.width = width / 5
                                itemBinding.ivIcon.layoutParams = l

                                itemBinding.ivAvatar.visibility = View.GONE
                                itemBinding.tvName.visibility = View.GONE
                                itemBinding.viewMargin.visibility = View.GONE

                                Glide.with(this).load(itemData.icon).into(itemBinding.ivIcon)
                            }
                        }

                        itemBinding.tvOrigin.text = itemData.total_cost.toString()
                        itemBinding.tvReal.text = itemData.total_income.toString()
                        Glide.with(this).load(itemData.avatar).apply(RequestOptions.bitmapTransform(CircleCrop())).into(itemBinding.ivAvatar)
                    }

                    "normal" -> {
                        val itemBinding = ItemEarnGiftPersonBinding.bind(itemView)
                        when (mType) {
                            "gift" -> {
                                val lp = itemBinding.ivIcon.layoutParams
                                lp.width = width / 5
                                itemBinding.ivIcon.layoutParams = lp
                            }

                            "game" -> {
                                itemBinding.tvName.visibility = View.GONE

                                val lp = itemBinding.ivIcon.layoutParams
                                lp.width = width / 2 - AppUtil.dp2px(this, 35f)
                                itemBinding.ivIcon.layoutParams = lp
                            }
                        }

                        Glide.with(this).load(itemData.icon).into(itemBinding.ivIcon)
                        itemBinding.tvName.text = String.format(getString(R.string.earn_count), itemData.number)
                        itemBinding.tvOrigin.text = String.format(getString(R.string.earn_cost), itemData.total_cost.toString())
                        itemBinding.tvReal.text = itemData.total_income.toString()
                    }
                }
            }
            .create()

        when (mRole) {
            "anchor" -> {
                binding.rcData.adapter = adapter
                binding.rcData.layoutManager = LinearLayoutManager(this)
            }

            "normal" -> {
                binding.rcData.adapter = adapter
                binding.rcData.layoutManager = GridLayoutManager(this, 2)
            }
        }
    }

    private fun getData(type: String) {
        if (type == "anchor") {
            DataManager.getAnchorIncome("gift") {
                binding.tvGift.text = String.format(getString(R.string.earn_gift_count), it.size)
                mList.clear()
                mList.addAll(it)
                adapter.notifyDataSetChanged()

                getEarnDetail("gift", true)
            }

            DataManager.getAnchorIncome("video") {
                binding.tvVideo.text = String.format(getString(R.string.earn_video_count), it.size)
                videoList.clear()
                videoList.addAll(it)
            }

            DataManager.getAnchorIncome("game") {
                binding.tvGame.text = String.format(getString(R.string.earn_game_count), it.size)
                gameList.clear()
                gameList.addAll(it)
            }
        } else {
            DataManager.getIncome("gift") {
                binding.tvGift.text = String.format(getString(R.string.earn_gift_count), it.size)
                mList.clear()
                mList.addAll(it)
                adapter.notifyDataSetChanged()

                getEarnDetail("gift", true)
            }

            DataManager.getIncome("video") {
                binding.tvVideo.text = String.format(getString(R.string.earn_video_count), it.size)
                videoList.clear()
                videoList.addAll(it)
            }


            DataManager.getIncome("game") {
                binding.tvGame.text = String.format(getString(R.string.earn_game_count), it.size)
                gameList.clear()
                gameList.addAll(it)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getEarnDetail(type: String, firstLoad: Boolean) {
        mType = type
        if (mRole == "anchor") {
            when (type) {
                "gift" -> {
                    binding.tvGift.textSize = 18f
                    binding.tvVideo.textSize = 14f
                    binding.tvGame.textSize = 14f
                    binding.ivGiftLine.visibility = View.VISIBLE
                    binding.ivVideoLine.visibility = View.GONE
                    binding.ivGameLine.visibility = View.GONE
                    binding.llGift.visibility = View.VISIBLE
                    binding.llVideo.visibility = View.GONE
                    binding.llGame.visibility = View.GONE
                }

                "video" -> {
                    binding.tvGift.textSize = 14f
                    binding.tvVideo.textSize = 18f
                    binding.tvGame.textSize = 14f
                    binding.ivGiftLine.visibility = View.GONE
                    binding.ivVideoLine.visibility = View.VISIBLE
                    binding.ivGameLine.visibility = View.GONE
                    binding.llGift.visibility = View.GONE
                    binding.llVideo.visibility = View.VISIBLE
                    binding.llGame.visibility = View.GONE
                }

                "game" -> {
                    binding.tvGift.textSize = 14f
                    binding.tvVideo.textSize = 14f
                    binding.tvGame.textSize = 18f
                    binding.ivGiftLine.visibility = View.GONE
                    binding.ivVideoLine.visibility = View.GONE
                    binding.ivGameLine.visibility = View.VISIBLE
                    binding.llGift.visibility = View.GONE
                    binding.llVideo.visibility = View.GONE
                    binding.llGame.visibility = View.VISIBLE
                }
            }

            if (firstLoad) return
            DataManager.getAnchorIncome(type) {
                if (it.isEmpty()) {
                    binding.llNoData.visibility = View.VISIBLE
                } else {
                    binding.llNoData.visibility = View.GONE
                }

                when (type) {
                    "gift" -> binding.tvGift.text = String.format(getString(R.string.earn_gift_count), it.size)
                    "video" -> binding.tvVideo.text = String.format(getString(R.string.earn_video_count), it.size)
                    "game" -> binding.tvGame.text = String.format(getString(R.string.earn_game_count), it.size)
                }

                mList.clear()
                mList.addAll(it)
                adapter.notifyDataSetChanged()
            }

        } else {
            when (type) {
                "gift" -> {
                    binding.tvGift.textSize = 18f
                    binding.tvVideo.textSize = 14f
                    binding.tvGame.textSize = 14f
                    binding.ivGiftLine.visibility = View.VISIBLE
                    binding.ivVideoLine.visibility = View.GONE
                    binding.ivGameLine.visibility = View.GONE
                }

                "game" -> {
                    binding.tvGift.textSize = 14f
                    binding.tvVideo.textSize = 14f
                    binding.tvGame.textSize = 18f
                    binding.ivGiftLine.visibility = View.GONE
                    binding.ivVideoLine.visibility = View.GONE
                    binding.ivGameLine.visibility = View.VISIBLE
                }
            }

            if (firstLoad) return
            DataManager.getIncome(type) {
                if (it.isEmpty()) {
                    binding.llNoData.visibility = View.VISIBLE
                } else {
                    binding.llNoData.visibility = View.GONE
                }

                when (type) {
                    "gift" -> binding.tvVideo.text = String.format(getString(R.string.earn_gift_count), it.size)
                    "video" -> binding.tvVideo.text = String.format(getString(R.string.earn_video_count), it.size)
                    "game" -> binding.tvVideo.text = String.format(getString(R.string.earn_game_count), it.size)
                }

                mList.clear()
                mList.addAll(it)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun toHistoryPage() {
        startActivity(Intent(this, HistoryActivity::class.java))
    }

    private fun recharge() {
        getUserInfo { userInfo ->
            RechargeDialog(this, userInfo.coin_income) {
                checkUserInfo()
            }
        }
    }

    private fun showQuestionPop(view: View) {
        getBasePrice { basePrice ->
            if (mRole == "anchor") {
                val anchorPrice = basePrice.service_charge.anchor
                val content = String.format(
                    getString(R.string.earn_sub_des),
                    (anchorPrice.video * 100).toInt().toString() + "%",
                    (anchorPrice.gift * 100).toInt().toString() + "%",
                    (anchorPrice.game * 100).toInt().toString() + "%"
                )
                QuestionPop(this, content).showAsDropDown(view, -20, 0)
            } else {
                val normalPrice = basePrice.service_charge.normal
                val content = String.format(
                    getString(R.string.earn_sub_des_2),
                    (normalPrice.gift * 100).toInt().toString() + "%",
                    (normalPrice.game * 100).toInt().toString() + "%"
                )
                QuestionPop(this, content).showAsDropDown(view, -20, 0)
            }
        }
    }

}