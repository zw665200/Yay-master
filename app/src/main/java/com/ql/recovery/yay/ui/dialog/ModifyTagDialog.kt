package com.ql.recovery.yay.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.netease.yunxin.kit.adapters.DataAdapter
import com.ql.recovery.bean.Tag
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.DialogModifyBirthdayBinding
import com.ql.recovery.yay.databinding.DialogModifyTagBinding
import com.ql.recovery.yay.databinding.ItemGuideTagBinding
import com.ql.recovery.yay.util.AppUtil
import com.ql.recovery.yay.util.ToastUtil


class ModifyTagDialog(
    private val activity: Activity,
    private val tagList: List<Tag>,
    private val func: () -> Unit
) : Dialog(activity, R.style.app_dialog2) {
    private lateinit var binding: DialogModifyTagBinding
    private lateinit var mTagAdapter: DataAdapter<Tag>
    private var mTagList = arrayListOf<Tag>()
    private var mTargetList = arrayListOf<Int>()

    init {
        initVew()
    }

    private fun initVew() {
        binding = DialogModifyTagBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(true)

        binding.tvSave.setOnClickListener { commit() }

        val list = tagList.map { it.id }
        mTargetList.addAll(list)

        chooseTags()

        show()
    }

    private fun chooseTags() {
        val width = AppUtil.getScreenWidth(activity)
        mTagAdapter = DataAdapter.Builder<Tag>()
            .setData(mTagList)
            .setLayoutId(R.layout.item_guide_tag)
            .addBindView { itemView, itemData, position ->
                val itemGuideTagBinding = ItemGuideTagBinding.bind(itemView)
//                val layout = itemView.layoutParams
//                layout.width = width / 4
//                layout.height = width / 4
//                itemView.layoutParams = layout

                itemGuideTagBinding.tvName.text = itemData.name
                if (mTargetList.contains(itemData.id)) {
                    itemGuideTagBinding.tvName.setTextColor(ResourcesCompat.getColor(activity.resources, R.color.color_yellow, null))
                    itemGuideTagBinding.ivChoose.setImageResource(R.drawable.xx_n_dl)
                } else {
                    itemGuideTagBinding.tvName.setTextColor(ResourcesCompat.getColor(activity.resources, R.color.color_black, null))
                    itemGuideTagBinding.ivChoose.setImageResource(R.drawable.xx_c_dl)
                }

                itemView.setOnClickListener {
                    if (mTargetList.contains(itemData.id)) {
                        mTargetList.remove(itemData.id)
                        mTagAdapter.notifyItemChanged(position)
                    } else {
                        mTargetList.add(itemData.id)
                        mTagAdapter.notifyItemChanged(position)
                    }
                }
            }
            .create()

        binding.rcTagList.layoutManager = GridLayoutManager(activity, 3)
        binding.rcTagList.adapter = mTagAdapter

        DataManager.getTags {
            mTagList.clear()
            mTagList.addAll(it)
            mTagAdapter.notifyItemRangeChanged(0, mTagList.size)
        }
    }

    private fun commit() {
        if (mTargetList.isEmpty()) return
        val nil = null
        DataManager.updateUserInfo(nil, nil, nil, nil, nil, nil, mTargetList) {
            if (it) {
                cancel()
                func()
            }
        }
    }

    override fun show() {
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.BOTTOM
            width = AppUtil.getScreenWidth(context)
            height = AppUtil.getScreenHeight(context) / 2
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