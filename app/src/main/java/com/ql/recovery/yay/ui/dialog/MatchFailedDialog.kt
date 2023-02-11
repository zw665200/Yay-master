package com.ql.recovery.yay.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.ql.recovery.yay.R
import com.netease.yunxin.kit.adapters.DataAdapter
import com.ql.recovery.bean.Anchor
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.databinding.DialogMatchFailedBinding
import com.ql.recovery.yay.databinding.ItemMatchRecommandUserBinding
import com.ql.recovery.yay.util.AppUtil


class MatchFailedDialog(
    private val mContext: Activity,
    private val func: () -> Unit
) : Dialog(mContext, R.style.app_dialog2) {
    private lateinit var binding: DialogMatchFailedBinding
    private lateinit var mAdapter: DataAdapter<Anchor>
    private var mList = mutableListOf<Anchor>()

    init {
        initVew()
    }

    private fun initVew() {
        binding = DialogMatchFailedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(false)

        binding.tvClub.setOnClickListener {
            cancel()
            func()
        }

        binding.ivClose.setOnClickListener { cancel() }

        initTags()
    }


    private fun initTags() {
        val w = AppUtil.getScreenWidth(mContext)
        mAdapter = DataAdapter.Builder<Anchor>()
            .setData(mList)
            .setLayoutId(R.layout.item_match_recommand_user)
            .addBindView { itemView, itemData ->
                val itemBinding = ItemMatchRecommandUserBinding.bind(itemView)
                Glide.with(mContext).load(itemData.avatar).into(itemBinding.ivAvatar)

                val lp = itemView.layoutParams
                lp.width = w / 4
                lp.height = w / 4
                itemView.layoutParams = lp
            }
            .create()

        binding.rcList.adapter = mAdapter
        binding.rcList.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)

        DataManager.getAnchorList("all", 0, 20) {
            mList.clear()
            mList.addAll(it)
            mAdapter.notifyItemRangeChanged(0, mList.size)
        }
    }


    override fun show() {
        val w = AppUtil.getScreenWidth(mContext)
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.CENTER
            width = w * 9 / 10
            height = WindowManager.LayoutParams.WRAP_CONTENT
            flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
            dimAmount = 0.5f
        }
        super.show()
    }

}