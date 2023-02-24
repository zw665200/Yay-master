package com.ql.recovery.yay.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.netease.yunxin.kit.adapters.MutableDataAdapter
import com.ql.recovery.bean.*
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.*
import com.ql.recovery.yay.ui.store.StoreActivity
import com.ql.recovery.yay.util.AppUtil
import com.ql.recovery.yay.util.DoubleUtils
import com.ql.recovery.yay.util.JLog
import com.ql.recovery.yay.util.ToastUtil
import com.tencent.mmkv.MMKV

class GameDialog(
    private val activity: Activity,
    private val userInfo: UserInfo,
    private val roomId: String?,
    private val avatar: String,
    private val isMatching: Boolean,
) : Dialog(activity, R.style.app_dialog) {
    private lateinit var binding: DialogGameBinding
    private var handler = android.os.Handler(Looper.getMainLooper())
    private lateinit var adapter: MutableDataAdapter<LotteryGift>
    private lateinit var recordAdapter: MutableDataAdapter<LotteryRecord>
    private var waitingDialog: WaitingDialog? = null
    private var giftList = arrayListOf<LotteryGift>()
    private var recordList = arrayListOf<LotteryRecord>()
    private var giftRecords = mutableSetOf<LotteryRecords>()
    private var coinRecords = mutableSetOf<LotteryRecords>()
    private var lotteryTicket: LotteryTicket? = null
    private var currentPos = -1
    private var circleTimes = 0
    private var currentCount = 1
    private var currentType = "coin"
    private var gameStart = false
    private var showType = Type.Game

    init {
        initVew()
    }

    private fun initVew() {
        binding = DialogGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(true)

        waitingDialog = WaitingDialog(activity)

        binding.ivClose.setOnClickListener { cancel() }
        binding.llDrawOnce.setOnClickListener { changeDrawType("once") }
        binding.llDrawTen.setOnClickListener { changeDrawType("ten") }
        binding.tvCoinParadise.setOnClickListener { changeParadise("coin") }
        binding.tvGiftParadise.setOnClickListener { changeParadise("gift") }
        binding.tvWinRecord.setOnClickListener { showRecord(currentType) }
        binding.tvBackGame.setOnClickListener { showGame() }

        if (isMatching) {
            binding.tvTitle.visibility = View.VISIBLE
            binding.tvGiftParadise.visibility = View.GONE
            binding.tvCoinParadise.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.shape_corner_yellow_20, null)
            binding.tvCoinParadise.isEnabled = false
        } else {
            binding.tvTitle.visibility = View.GONE
        }

        getLotteryBroadcastList()
        initGift()
        initLotteryRecord()

        changeParadise("coin")
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initGift() {
        val width = AppUtil.getScreenWidth(activity)
        adapter = MutableDataAdapter.Builder<LotteryGift>()
            .setData(giftList)
            .setLayoutId(R.layout.item_award_1, R.layout.item_award_2)
            .setViewType { position -> giftList[position].type }
            .addBindType("coin", "gift")
            .addBindView { itemView, itemData, position ->

                when (itemData.type) {
                    "coin" -> {
                        val itemBinding = ItemAward1Binding.bind(itemView)

                        val param = itemBinding.root.layoutParams
                        param.height = width / 4
                        itemBinding.root.layoutParams = param

                        itemBinding.tvCoin.text = itemData.coin.toString()

                        if (itemData.check) {
                            itemView.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.sd_bg_g, null)
                        } else {
                            itemView.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.sd_bg_w, null)
                        }

                        if (itemData.coin == 0) {
                            itemView.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.game_start, null)
                            itemBinding.tvCoin.textSize = 22f
                            itemBinding.tvCoin.text = activity.getString(R.string.match_game_start)
                            itemBinding.tvCoin.typeface = Typeface.createFromAsset(activity.assets, "fonts/abc.ttf")
                            itemBinding.tvCoin.gravity = Gravity.CENTER
                            itemBinding.tvCoin.setTextColor(Color.WHITE)
                            itemBinding.ivDemon.visibility = View.GONE

                            itemBinding.tvCoin.text = activity.getString(R.string.match_game_start)

//                            if (roomId == null) {
//                                itemBinding.tvCoin.text = activity.getString(R.string.match_game_start)
//                            } else {
//                                if (itemData.insert_or_draw) {
//                                    itemBinding.tvCoin.text = activity.getString(R.string.match_game_insert)
//                                } else {
//                                    itemBinding.tvCoin.text = activity.getString(R.string.match_game_start)
//                                }
//                            }

                        } else {
                            itemBinding.tvCoin.typeface = Typeface.createFromAsset(activity.assets, "fonts/din_b.otf")
                            itemBinding.tvCoin.setTextColor(Color.BLACK)
                            itemBinding.tvCoin.text = itemData.coin.toString()
                            itemBinding.ivDemon.visibility = View.VISIBLE
                        }
                    }

                    "gift" -> {
                        val itemBinding = ItemAward2Binding.bind(itemView)
                        itemBinding.tvCoin.text = itemData.coin.toString()

                        val param = itemBinding.root.layoutParams
                        param.height = width / 4
                        itemBinding.root.layoutParams = param

                        val lp = itemBinding.ivGift.layoutParams
                        lp.width = width / 7
                        lp.height = width / 7
                        itemBinding.ivGift.layoutParams = lp
                        Glide.with(activity).load(itemData.icon).into(itemBinding.ivGift)

                        if (itemData.check) {
                            itemView.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.sd_bg_g, null)
                        } else {
                            itemView.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.sd_bg_w, null)
                        }

                        if (position == 4) {
                            itemView.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.game_start, null)
                            itemBinding.ivGift.visibility = View.GONE
                            itemBinding.ivDemon.visibility = View.GONE
                            itemBinding.tvCoin.textSize = 22f
                            itemBinding.tvCoin.typeface = Typeface.createFromAsset(activity.assets, "fonts/abc.ttf")
                            itemBinding.tvCoin.gravity = Gravity.CENTER
                            itemBinding.tvCoin.setTextColor(Color.WHITE)
                            itemBinding.ivDemon.visibility = View.GONE
                            itemBinding.tvCoin.text = activity.getString(R.string.match_game_start)

//                            if (itemData.insert_or_draw) {
//                                itemBinding.tvCoin.text = activity.getString(R.string.match_game_insert)
//                            } else {
//                                itemBinding.tvCoin.text = activity.getString(R.string.match_game_start)
//                            }

                        } else {
                            itemBinding.tvCoin.typeface = Typeface.createFromAsset(activity.assets, "fonts/din_b.otf")
                            itemBinding.ivGift.visibility = View.VISIBLE
                            itemBinding.tvCoin.textSize = 13f
                            itemBinding.tvCoin.setTextColor(Color.BLACK)
                            itemBinding.ivDemon.visibility = View.VISIBLE
                        }
                    }
                }

                itemView.setOnClickListener {
                    if (gameStart) return@setOnClickListener

                    if (!DoubleUtils.isFastDoubleClick()) {
                        if (position == 4) {
                            if (isMatching) {
                                lotteryStart()
                            } else {
                                giveLotteryCoins()
                            }
                        }
                    }
                }
            }
            .create()

        binding.rcGift.adapter = adapter
        binding.rcGift.layoutManager = GridLayoutManager(activity, 3)
    }

    private fun initLotteryRecord() {
        val width = AppUtil.getScreenWidth(activity)
        val height = AppUtil.getScreenHeight(activity)
        recordAdapter = MutableDataAdapter.Builder<LotteryRecord>()
            .setData(recordList)
            .setLayoutId(R.layout.item_lottery_coin_record, R.layout.item_lottery_gift_record)
            .setViewType { position -> recordList[position].type }
            .addBindType("coin", "gift")
            .addBindView { itemView, itemData, position ->
                when (itemData.type) {
                    "coin" -> {
                        val itemBinding = ItemLotteryCoinRecordBinding.bind(itemView)
                        itemBinding.tvName.text = String.format(activity.getString(R.string.match_game_lottery_time), itemData.count)
                        itemBinding.tvReal.text = itemData.coin.toString()

                        Glide.with(activity).load(itemData.avatar).apply(RequestOptions.bitmapTransform(CircleCrop())).into(itemBinding.ivIcon)

                        if (roomId != null) {
                            if (coinRecords.size > position) {
                                var total = 0
                                val recordList = coinRecords.elementAt(position)
                                for (record in recordList.records) {
                                    total += record.coin
                                }

                                itemBinding.tvReal.text = total.toString()
                            }
                        }
                    }

                    "gift" -> {
                        val itemBinding = ItemLotteryGiftRecordBinding.bind(itemView)
                        itemBinding.tvName.text = String.format(activity.getString(R.string.match_game_lottery_time), itemData.count)
                        Glide.with(activity).load(itemData.avatar).apply(RequestOptions.bitmapTransform(CircleCrop())).into(itemBinding.ivIcon)

                        if (giftRecords.size > position) {
                            itemBinding.llGifts.removeAllViews()
                            for (child in giftRecords.elementAt(position).records) {
                                val imageView = ImageView(activity)
                                Glide.with(activity).load(child.icon).into(imageView)
                                itemBinding.llGifts.addView(imageView)

                                //设置礼物的尺寸
                                imageView.post {
                                    val lp = imageView.layoutParams
                                    lp.width = width / 10
                                    lp.height = width / 10
                                    imageView.layoutParams = lp
                                }
                            }
                        }
                    }
                }
            }
            .create()

        binding.rcLotteryRecord.adapter = recordAdapter
        binding.rcLotteryRecord.layoutManager = LinearLayoutManager(activity)

        val lp = binding.rcLotteryRecord.layoutParams
        lp.height = height / 3
        binding.rcLotteryRecord.layoutParams = lp
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setLotteryGiftList(list: List<LotteryGift>, type: String) {
        giftList.clear()
        if (list.size == 8) {
            giftList.add(list[0])
            giftList.add(list[1])
            giftList.add(list[2])
            giftList.add(list[7])
            giftList.add(LotteryGift(0, 0, "", "", ""))
            giftList.add(list[3])
            giftList.add(list[6])
            giftList.add(list[5])
            giftList.add(list[4])
        }

        currentType = type
        giftList.forEach { it.type = type }
        adapter.notifyDataSetChanged()
    }

    fun setLotteryTicket(ticket: LotteryTicket?) {
        lotteryTicket = ticket

        if (ticket != null) {
            if (ticket.count == 1) {
                changeDrawType("once")
            } else {
                changeDrawType("ten")
            }

            if (ticket.type == "coin") {
                changeParadise("coin")
            } else {
                changeParadise("gift")
            }
        }

        showGame()
    }

    fun setLotteryRecord(list: List<LotteryRecord>) {
        playGame { showLotteryRecord(list, false) }
    }

    private fun changeParadise(type: String) {
        val basePrice = MMKV.defaultMMKV().decodeParcelable("base_price", BasePrice::class.java) ?: return
        when (type) {
            "coin" -> {
                currentType = "coin"
                binding.tvDrawOnce.text = String.format(activity.getString(R.string.match_game_draw_5), basePrice.lottery.coin.one_time)
                binding.tvDrawTen.text = String.format(activity.getString(R.string.match_game_draw_30), basePrice.lottery.coin.ten_times)
                binding.tvCoinParadise.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.shape_corner_left_yellow, null)
                binding.tvGiftParadise.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.shape_corner_right_light_grey, null)

                when (showType) {
                    Type.Game -> getLotteryCoinList()
                    Type.Record -> getLotteryRecord(type)
                }
            }

            "gift" -> {
                currentType = "gift"
                binding.tvDrawOnce.text = String.format(activity.getString(R.string.match_game_draw_8), basePrice.lottery.gift.one_time)
                binding.tvDrawTen.text = String.format(activity.getString(R.string.match_game_draw_50), basePrice.lottery.gift.ten_times)
                binding.tvCoinParadise.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.shape_corner_left_light_grey, null)
                binding.tvGiftParadise.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.shape_corner_right_yellow, null)

                when (showType) {
                    Type.Game -> getLotteryGiftList()
                    Type.Record -> getLotteryRecord(type)
                }
            }
        }
    }

    private fun changeDrawType(type: String) {
        when (type) {
            "once" -> {
                currentCount = 1
                binding.llDrawOnce.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.lottery_check, null)
                binding.llDrawTen.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.lottery_uncheck, null)
            }
            "ten" -> {
                currentCount = 10
                binding.llDrawOnce.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.lottery_uncheck, null)
                binding.llDrawTen.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.lottery_check, null)
            }
        }
    }

    private fun checkTickets(lotteryTicket: (LotteryTicket?) -> Unit) {
        if (roomId == null) {
            lotteryTicket(null)
            return
        }

        //检查自己是否有抽奖券
        DataManager.checkLotteryTickets(roomId) { ticket ->
            if (ticket == null) {
                lotteryTicket(null)
                return@checkLotteryTickets
            }

            //检查是否是自己的票据
            if (ticket.target_uid == userInfo.uid) {
                ToastUtil.showLong(activity, activity.getString(R.string.match_ticket_unused))
                this.lotteryTicket = ticket
                if (ticket.count == 1) {
                    changeDrawType("once")
                } else {
                    changeDrawType("ten")
                }

                lotteryTicket(ticket)
            } else {
                lotteryTicket(null)
            }
        }
    }

    private fun getLotteryBroadcastList() {
        DataManager.getLotteryBroadcast { list ->
            broadcastLoop(list, 0)
        }
    }

    private fun broadcastLoop(list: List<Lottery>, position: Int) {
        handler.postDelayed({
            val lottery = list[position]
            when (lottery.type) {
                "coin" -> {
                    val content = String.format(activity.getString(R.string.match_broadcast_content), lottery.nickname, lottery.coin.toString(), "coins")
                    binding.tvBroadcast.setText(content)
                    if (position == list.size - 1) {
                        broadcastLoop(list, 0)
                    } else {
                        broadcastLoop(list, position + 1)
                    }
                }

                "gift" -> {
                    val content = String.format(activity.getString(R.string.match_broadcast_content), lottery.nickname, lottery.gift_name, "")
                    binding.tvBroadcast.setText(content)
                    if (position == list.size - 1) {
                        broadcastLoop(list, 0)
                    } else {
                        broadcastLoop(list, position + 1)
                    }
                }
            }
        }, 2000L)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getLotteryCoinList() {
        DataManager.getLotteryCoin { list ->
            giftList.clear()
            if (list.size == 8) {
                giftList.add(list[0])
                giftList.add(list[1])
                giftList.add(list[2])
                giftList.add(list[7])
                giftList.add(LotteryGift(0, 0, "", "", ""))
                giftList.add(list[3])
                giftList.add(list[6])
                giftList.add(list[5])
                giftList.add(list[4])
            }
            giftList.forEach { it.type = "coin" }
            adapter.notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getLotteryGiftList() {
        DataManager.getLotteryGift { list ->
            giftList.clear()
            if (list.size == 8) {
                giftList.add(list[0])
                giftList.add(list[1])
                giftList.add(list[2])
                giftList.add(list[7])
                giftList.add(LotteryGift(0, 0, "", "", ""))
                giftList.add(list[3])
                giftList.add(list[6])
                giftList.add(list[5])
                giftList.add(list[4])
            }

            giftList.forEach { it.type = "gift" }
            adapter.notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getLotteryRecord(type: String) {
        if (roomId == null) {
            //查询个人抽奖记录
            DataManager.getLotteryRecord { list ->
                recordList.clear()
                recordList.addAll(list)
                giftList.forEach { it.type = type }
                recordList.forEach { it.avatar = avatar }
                recordAdapter.notifyDataSetChanged()
            }
        } else {
            //查询房间抽奖记录
            recordList.clear()

            when (type) {
                "coin" -> {
                    for (child in coinRecords) {
                        val records = child.records
                        recordList.add(LotteryRecord(records[0].id, records[0].coin, records[0].count, records[0].icon, records[0].type, records[0].avatar))
                    }
                }

                "gift" -> {
                    for (child in giftRecords) {
                        val records = child.records
                        recordList.add(LotteryRecord(records[0].id, records[0].coin, records[0].count, records[0].icon, records[0].type, records[0].avatar))
                    }
                }
            }

            recordAdapter.notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun playGame(finish: () -> Unit) {
        handler.postDelayed({
            gameStart = true
            if (currentPos < giftList.size) {

                when (currentPos) {
                    -1 -> currentPos = 0
                    0 -> currentPos = 1
                    1 -> currentPos = 2
                    2 -> currentPos = 5
                    5 -> currentPos = 8
                    8 -> currentPos = 7
                    7 -> currentPos = 6
                    6 -> currentPos = 3
                    3 -> currentPos = -1
                }

                //回到起点代表走完一圈
                if (currentPos == -1) {
                    currentPos = 0
                    circleTimes++
                }

                giftList.forEach { it.check = false }
                giftList[currentPos].check = true
                adapter.notifyDataSetChanged()

                if (circleTimes == 3) {
                    JLog.i("game finish")
                    gameStart = false
                    finish()
                    return@postDelayed
                }

                playGame(finish)
            }

        }, 100L)
    }

    /**
     * 个人开始抽奖
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun lotteryStart() {
        DataManager.lotteryStart(currentCount) { list ->
            playGame { showLotteryRecord(list, true) }
        }
    }

    /**
     * 赠送给对方抽奖券
     */
    private fun giveLotteryCoins() {
        if (roomId == null) return

        //收到对方抽奖券，则直接抽奖
        if (lotteryTicket != null) {
            //当给出抽奖的类型与当前选中的抽奖类型匹配时才能抽奖
            if (lotteryTicket!!.type == currentType && lotteryTicket!!.count == currentCount) {
                roomLotteryStart()
            }
            return
        }

        waitingDialog?.show()
        //检查是否有抽奖券未用
        checkTickets { ticket ->
            if (ticket == null) {
                //给对方发起投币
                DataManager.giveLotteryTicket(currentCount, roomId, currentType) {
                    waitingDialog?.cancel()
                    if (it != null) {
                        ToastUtil.showShort(activity, activity.getString(R.string.match_game_wait_lottery))
                    }
                }
                return@checkTickets
            }

            //当给出抽奖的类型与当前选中的抽奖类型匹配时才能抽奖
            if (ticket.type == currentType && ticket.count == currentCount) {
                roomLotteryStart()
            }
        }
    }

    /**
     * 房间开始抽奖
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun roomLotteryStart() {
        if (roomId == null) return

        DataManager.roomLotteryStart(roomId) { list ->
            playGame {
                showLotteryRecord(list, true)
            }
        }
    }

    /**
     * 展示抽奖结果
     * @param list 抽奖结果
     * @param isInitiator 是否是抽奖人
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun showLotteryRecord(list: List<LotteryRecord>, isInitiator: Boolean) {
        //保存房间抽奖记录
        if (isInitiator) {
            list.forEach { it.avatar = userInfo.avatar }
        } else {
            list.forEach { it.avatar = avatar }
        }

        //分开添加本地历史记录
        if (list[0].type == "gift") {
            JLog.i("gift record add")
            giftRecords.add(LotteryRecords(list))
        } else {
            JLog.i("coin record add")
            coinRecords.add(LotteryRecords(list))
        }

        //全部按钮重置
        currentPos = -1
        circleTimes = 0

        if (currentCount == 1) {
            giftList.forEach { it.check = false }
            adapter.notifyDataSetChanged()
            for (item in list) {
                for (child in giftList) {
                    if (item.id == child.id) {
                        //选中中奖的那个
                        child.check = true
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }

        //弹出奖励
        RewardDialog(activity, list[0].type, list, isInitiator)
        lotteryTicket = null
    }

    private fun showGame() {
        showType = Type.Game
        binding.llGameArea.visibility = View.VISIBLE
        binding.llRecordArea.visibility = View.GONE
        binding.llLottery.visibility = View.VISIBLE
        binding.tvTips.text = activity.getString(R.string.match_game_tip_2)
    }

    private fun showRecord(type: String) {
        showType = Type.Record
        binding.llGameArea.visibility = View.GONE
        binding.llRecordArea.visibility = View.VISIBLE
        binding.llLottery.visibility = View.GONE
        binding.tvTips.text = activity.getString(R.string.match_game_wait_lottery_tip_2)
        getLotteryRecord(type)
    }

    private fun toPayPage() {
        cancel()
        activity.startActivity(Intent(activity, StoreActivity::class.java))
    }

    override fun show() {
        if (isShowing) return

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

    enum class Type { Game, Record }

}