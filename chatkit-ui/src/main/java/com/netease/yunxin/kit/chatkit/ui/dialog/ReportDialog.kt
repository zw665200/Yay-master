package com.netease.yunxin.kit.chatkit.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.netease.yunxin.kit.adapters.DataAdapter
import com.netease.yunxin.kit.chatkit.ui.R
import com.netease.yunxin.kit.chatkit.ui.databinding.DialogReportBinding
import com.netease.yunxin.kit.chatkit.ui.databinding.ItemReportBinding
import com.netease.yunxin.kit.chatkit.ui.util.ToastUtil
import com.ql.recovery.bean.Template
import com.ql.recovery.manager.DataManager
import com.ql.recovery.util.AppUtil

class ReportDialog(
    private val activity: Activity,
    private val targetUid: Int,
    private val list:List<Template>
) : Dialog(activity, R.style.app_dialog_1) {
    private lateinit var binding: DialogReportBinding
    private lateinit var adapter: DataAdapter<Template>

    init {
        initVew()
    }

    private fun initVew() {
        binding = DialogReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(true)

        binding.tvCommit.setOnClickListener { commit() }

        initRegionList()
    }

    private fun initRegionList() {
        adapter = DataAdapter.Builder<Template>()
            .setData(list)
            .setLayoutId(R.layout.item_report)
            .addBindView { itemView, itemData, position ->
                val itemBinding = ItemReportBinding.bind(itemView)
                itemBinding.tvTitle.text = itemData.title
                itemBinding.tvDes.text = itemData.subtitle

                if (itemData.check) {
                    itemBinding.ivCheck.setImageResource(R.drawable.kit_checked)
                } else {
                    itemBinding.ivCheck.setImageResource(R.drawable.kit_unchecked)
                }

                itemBinding.ivCheck.setOnClickListener {
                    for ((pos, item) in list.withIndex()) {
                        if (pos != position) {
                            item.check = false
                        }
                    }

                    list[position].check = !itemData.check

                    adapter.notifyItemRangeChanged(0, list.size)
                }
            }
            .create()

        binding.rcReport.adapter = adapter
        binding.rcReport.layoutManager = LinearLayoutManager(activity)
        adapter.notifyItemRangeInserted(0, list.size)
    }

    private fun commit() {
        for (item in list) {
            if (item.check) {
                DataManager.report(null, targetUid, item.id) {
                    if (it) {
                        ToastUtil.showShort(activity, activity.getString(R.string.report_report_commit_success))
                        cancel()
                    }
                }
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