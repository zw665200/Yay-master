package com.ql.recovery.yay.ui.notifications

import android.content.Intent
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

        binding.rcNotification.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val manager = recyclerView.layoutManager as LinearLayoutManager
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //获取最后一个完全显示的ItemPosition
                    val lastVisibleItem = manager.findLastCompletelyVisibleItemPosition()//从0开始
                    val totalItemCount = manager.itemCount
                    // 判断是否滚动到底部，并且是向下滚动
                    if (lastVisibleItem == (totalItemCount - 1) && isToLast) {
                        //加载更多功能的代码
                        page++

                        getNotificationList()
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                isToLast = dy > 0
            }
        })
    }

    private fun getNotificationList() {
        DataManager.getMessages(page, size) {
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
            adapter.notifyItemRangeChanged(0, mList.size)
        }
    }

    private fun toFollowPage() {
        startActivity(Intent(this, FollowActivity::class.java))
    }

    private fun toSharePage() {
        startActivity(Intent(this, ShareActivity::class.java))
    }

}