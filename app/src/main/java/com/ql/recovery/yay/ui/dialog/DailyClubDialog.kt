package com.ql.recovery.yay.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig
import com.netease.yunxin.kit.adapters.DataAdapter
import com.netease.yunxin.kit.chatkit.repo.ChatRepo
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback
import com.ql.recovery.bean.Anchor
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.DialogDailyClubBinding
import com.ql.recovery.yay.databinding.ItemDailyClubBinding
import com.ql.recovery.yay.util.AppUtil
import com.ql.recovery.yay.util.JLog
import com.tencent.mmkv.MMKV
import java.util.Random

class DailyClubDialog(
    private val activity: Activity
) : Dialog(activity, R.style.app_dialog2) {
    private lateinit var binding: DialogDailyClubBinding
    private lateinit var mAdapter: DataAdapter<Anchor>
    private var mList = mutableListOf<Anchor>()
    private var checkList = mutableListOf<Anchor>()
    private var mk = MMKV.defaultMMKV()

    init {
        initVew()
    }

    private fun initVew() {
        binding = DialogDailyClubBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(false)

        binding.tvClub.setOnClickListener {
            cancel()
            doTask()
        }

        binding.ivClose.setOnClickListener { cancel() }
        binding.tvMsgMember.setOnClickListener {
            PrimeDialog(activity, false) {}
        }

        initTags()
        checkDailyMessage()
    }


    private fun initTags() {
        val w = AppUtil.getScreenWidth(activity)
        mAdapter = DataAdapter.Builder<Anchor>()
            .setData(mList)
            .setLayoutId(R.layout.item_daily_club)
            .addBindView { itemView, itemData ->
                val itemBinding = ItemDailyClubBinding.bind(itemView)
                Glide.with(activity).load(itemData.avatar).into(itemBinding.ivAvatar)

                val lp = itemView.layoutParams
                lp.width = w / 4
                lp.height = w / 4
                itemView.layoutParams = lp

                itemView.setOnClickListener {
                    if (itemBinding.checkBox.isChecked) {
                        checkList.add(itemData)
                    } else {
                        checkList.remove(itemData)
                    }
                }
            }
            .create()

        val lp = binding.rcList.layoutParams
        lp.height = w / 3
        binding.rcList.layoutParams = lp

        binding.rcList.adapter = mAdapter
        binding.rcList.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)

        DataManager.getAnchorList("all", 0, 5) { list ->
            mList.clear()
            mList.addAll(list)
            checkList.addAll(list)
            mAdapter.notifyItemRangeChanged(0, mList.size)
        }
    }

    private fun checkDailyMessage() {
        //检查今日免费次数
        val today = AppUtil.getTodayDay()
        val date = mk.decodeString("daily_msg_date")
        if (date == null || date != today) {
            mk.encode("daily_msg_date", today)
            mk.encode("daily_msg_time", 0)
            binding.tvMsgTip.text = String.format(activity.getString(R.string.recommend_tip_1), 10)
        } else {
            val times = MMKV.defaultMMKV().decodeInt("daily_msg_time", 0)
            binding.tvMsgTip.text = String.format(activity.getString(R.string.recommend_tip_1), 10 - times)
        }
    }

    private fun doTask() {
        if (checkList.isNotEmpty()) {
            for (child in checkList) {
                DataManager.getGreetingTemplates("normal", "woman") { list ->
                    //随机选一条内容
                    val random = Random().nextInt(list.size)
                    //自动发送打招呼内容
                    sendMessage(child.uid, list[random].content)
                }
            }
        }
    }

    private fun sendMessage(uid: Int, content: String) {
        val sessionType = SessionTypeEnum.P2P
        val textMessage = MessageBuilder.createTextMessage(uid.toString(), sessionType, content)
        val config = CustomMessageConfig()
        config.enableHistory = true
        config.enableRoaming = true
        config.enableSelfSync = true
        textMessage.config = config

        ChatRepo.sendMessage(textMessage, false, object : FetchCallback<Void> {
            override fun onException(exception: Throwable?) {
                JLog.i("exception = ${exception?.message}")
            }

            override fun onFailed(code: Int) {
                JLog.i("onFailed, code = $code")
            }

            override fun onSuccess(param: Void?) {
                JLog.i("onSuccess")
                val times = mk.decodeInt("daily_msg_time", 0)
                mk.encode("daily_msg_time", times + 1)
            }
        })
    }

    override fun show() {
        val w = AppUtil.getScreenWidth(activity)
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