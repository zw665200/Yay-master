package com.ql.recovery.yay.ui.notifications

import android.annotation.SuppressLint
import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import com.ql.recovery.bean.Notification
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.adapter.DataAdapter
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.databinding.ActivityNotificationBinding
import com.ql.recovery.yay.databinding.ItemNotificationsBinding
import com.ql.recovery.yay.ui.base.BaseActivity
import com.ql.recovery.yay.ui.mine.FollowActivity
import com.ql.recovery.yay.ui.mine.ShareActivity
import com.ql.recovery.yay.util.AppUtil

class NotificationActivity : BaseActivity() {
    private lateinit var binding: ActivityNotificationBinding
    private lateinit var adapter: DataAdapter<Notification>
    private var mList = arrayListOf<Notification>()
    private var page = 0
    private var size = 20
    private var isToLast = false

    override fun getViewBinding(baseBinding: ActivityBaseBinding) {
        binding = ActivityNotificationBinding.inflate(layoutInflater, baseBinding.flBase, true)
    }

    override fun initView() {
        setStatusBarLight()
        binding.ivBack.setOnClickListener { finish() }
    }

    override fun initData() {
        initNotification()
        getNotificationList()

        binding.refreshLayout.setOnRefreshListener {
            page = 0
            getNotificationList()
        }

        binding.refreshLayout.setOnLoadMoreListener {
            page++
            getNotificationList()
        }
    }

    private fun initNotification() {
        adapter = DataAdapter.Builder<Notification>()
            .setData(mList)
            .setLayoutId(R.layout.item_notifications)
            .addBindView { itemView, itemData ->
                val itemBinding = ItemNotificationsBinding.bind(itemView)
                itemBinding.tvContent.text = itemData.content
                itemBinding.tvTime.text = AppUtil.timeStamp2Date(itemData.created_at, "yyyy/MM/dd HH:mm:ss")

                itemBinding.tvTask.setOnClickListener {
                    if (itemData.event.startsWith("yay://share")) {
                        toSharePage()
                    }

                    if (itemData.event.startsWith("yay://follow")) {
                        toFollowPage()
                    }
                }
            }
            .create()

        binding.rcNotification.adapter = adapter
        binding.rcNotification.layoutManager = LinearLayoutManager(this)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getNotificationList() {
        DataManager.getMessages(page, size) {
            binding.refreshLayout.finishRefresh()
            binding.refreshLayout.finishLoadMore()

            if (page == 0) {
                mList.clear()
            }

            if (it.isEmpty()) {
                page--
                if (page < 0) {
                    page = 0
                }
            }

            mList.addAll(it)
            adapter.notifyDataSetChanged()
        }
    }

    private fun toFollowPage() {
        startActivity(Intent(this, FollowActivity::class.java))
    }

    private fun toSharePage() {
        startActivity(Intent(this, ShareActivity::class.java))
    }

}