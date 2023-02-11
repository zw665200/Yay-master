package com.ql.recovery.yay.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.netease.yunxin.kit.adapters.DataAdapter
import com.ql.recovery.bean.Gift
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.DialogGiftBinding
import com.ql.recovery.yay.databinding.ItemGiftBinding
import com.ql.recovery.yay.ui.store.StoreActivity
import com.ql.recovery.yay.util.AppUtil
import com.ql.recovery.yay.util.ToastUtil

class GiftDialog(
    private val activity: Activity,
    private val giftList: List<Gift>,
    private val uid: Int,
    private val coin: Int,
    private val func: (Gift) -> Unit
) : Dialog(activity, R.style.app_dialog2) {
    private lateinit var binding: DialogGiftBinding
    private lateinit var adapter: DataAdapter<Gift>
    private var currentPos = -1
    private var currentGift: Gift? = null

    init {
        initVew()
    }

    private fun initVew() {
        binding = DialogGiftBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(true)

        binding.tvCoin.text = coin.toString()
        binding.ivClose.setOnClickListener { cancel() }
        binding.ivAddCoin.setOnClickListener { toPayPage() }
        binding.tvGetMore.setOnClickListener { sendGift() }

        initGift()
        show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initGift() {
        val width = AppUtil.getScreenWidth(activity)
        adapter = DataAdapter.Builder<Gift>()
            .setData(giftList)
            .setLayoutId(R.layout.item_gift)
            .addBindView { itemView, itemData, position ->
                val itemBinding = ItemGiftBinding.bind(itemView)

                val l = itemView.layoutParams
                l.width = width / 4
                itemView.layoutParams = l

                val lp = itemBinding.ivGift.layoutParams
                lp.width = width / 5
                lp.height = width / 5
                itemBinding.ivGift.layoutParams = lp

                Glide.with(activity).load(itemData.icon).into(itemBinding.ivGift)
                if (itemData.coin == 0) {
                    itemBinding.tvCoin.text = activity.getString(R.string.match_free)
                    itemBinding.ivCoin.visibility = View.GONE
                } else {
                    itemBinding.tvCoin.text = itemData.coin.toString()
                    itemBinding.ivCoin.visibility = View.VISIBLE
                }

                if (currentPos == position) {
                    itemView.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.shape_rectangle_yellow_solid_fefcde, null)
                } else {
                    itemView.background = ResourcesCompat.getDrawable(activity.resources, R.color.color_white, null)
                }

                itemView.setOnClickListener {
                    currentPos = position
                    currentGift = itemData
                    adapter.notifyDataSetChanged()
                }
            }
            .create()

        binding.rcGift.adapter = adapter
        binding.rcGift.layoutManager = GridLayoutManager(activity, 4)
        adapter.notifyDataSetChanged()
    }

    private fun toPayPage() {
        cancel()
        activity.startActivity(Intent(activity, StoreActivity::class.java))
    }

    private fun sendGift() {
        if (currentGift == null) return

        if (currentGift!!.coin > coin) {
            ToastUtil.showShort(activity, "not enough coins")
            toPayPage()
            return
        }

        //赠送礼物
        DataManager.giveGift(currentGift!!.id, uid) {
            if (it) {
                //赠送成功
                ToastUtil.showShort(activity, "send successful")
                cancel()
                func(currentGift!!)
            }
        }
    }

    override fun show() {
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.BOTTOM
            width = AppUtil.getScreenWidth(context)
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        super.show()
    }


}