package com.ql.recovery.yay.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.netease.yunxin.kit.adapters.DataAdapter
import com.ql.recovery.bean.Addition
import com.ql.recovery.bean.Room
import com.ql.recovery.bean.UserInfo
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.DialogAdditionBinding
import com.ql.recovery.yay.databinding.ItemAdditionBinding
import com.ql.recovery.yay.ui.store.StoreActivity
import com.ql.recovery.yay.util.AppUtil
import com.tencent.mmkv.MMKV


class AdditionDialog(
    private val activity: Activity,
    private val userInfo: UserInfo,
    private val roomId: String,
    private val func: (Room, Addition) -> Unit
) : Dialog(activity, R.style.app_dialog2) {
    private lateinit var binding: DialogAdditionBinding
    private lateinit var adapter: DataAdapter<Addition>
    private val list = mutableListOf<Addition>()
    private val mk = MMKV.defaultMMKV()
    private var currentAddition: Addition? = null
    private var currentPos = -1

    init {
        initVew()
    }

    private fun initVew() {
        binding = DialogAdditionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(true)

        binding.tvCoin.text = userInfo.coin.toString()

        binding.tvPurchase.setOnClickListener { toStorePage() }
        binding.tvMember.setOnClickListener { openPrimeDialog() }
        binding.tvCommit.setOnClickListener { commit() }

        initRegionList()
        show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initRegionList() {
        adapter = DataAdapter.Builder<Addition>()
            .setData(list)
            .setLayoutId(R.layout.item_addition)
            .addBindView { itemView, itemData, position ->
                val itemBinding = ItemAdditionBinding.bind(itemView)
                itemBinding.tvCoin.text = itemData.cost.toString()

                val time = itemData.duration / 60
                if (time == 1) {
                    itemBinding.tvName.text = activity.getString(R.string.addition_item_every_minute)
                } else {
                    itemBinding.tvName.text = String.format(activity.getString(R.string.addition_item_title), time)
                }

                if (position == currentPos) {
                    itemView.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.shape_corner_yellow_10, null)
                } else {
                    itemView.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.shape_corner_white, null)
                }

                itemView.setOnClickListener {
                    currentPos = position
                    currentAddition = itemData
                    adapter.notifyDataSetChanged()
                }
            }
            .create()

        binding.rcAddition.adapter = adapter
        binding.rcAddition.layoutManager = GridLayoutManager(activity, 2)

        DataManager.getAdditionPriceList("match") {
            list.clear()
            list.addAll(it)
            adapter.notifyItemRangeInserted(0, list.size)
        }
    }

    private fun commit() {
        if (currentAddition == null) return
        //金币不足跳到商店
        if (currentAddition!!.cost > userInfo.coin) {
            toStorePage()
            return
        }

        DataManager.additionTime(currentAddition!!.id, roomId) { room ->
            cancel()
            if (room != null) {
                func(room, currentAddition!!)
            }
        }
    }

    private fun toStorePage() {
        cancel()
        activity.startActivity(Intent(activity, StoreActivity::class.java))
    }

    private fun openPrimeDialog() {
        cancel()
        val userInfo = mk.decodeParcelable("user_info", UserInfo::class.java) ?: return
        PrimeDialog(activity, userInfo.is_vip) {}
    }

    override fun show() {
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.BOTTOM
            width = AppUtil.getScreenWidth(context)
            height = WindowManager.LayoutParams.WRAP_CONTENT
            flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
            dimAmount = 0.5f
        }

        super.show()
    }

    /**
     * 显示PopupWindow
     */
//    private fun show(v: View) {
//        if (mPopupWindow != null && !mPopupWindow.isShowing()) {
//            mPopupWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0)
//        }
//        setWindowAlpa(0.5f)
//    }


    /**
     * 消失PopupWindow
     */
//    override fun dismiss() {
//        if (mPopupWindow != null && mPopupWindow.isShowing()) {
//            mPopupWindow.dismiss()
//        }
//        setWindowAlpa(1.0f)
//    }

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