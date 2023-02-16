package com.ql.recovery.yay.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.DialogModifyBirthdayBinding
import com.ql.recovery.yay.util.AppUtil
import com.ql.recovery.yay.util.ToastUtil
import com.weigan.loopview.LoopView
import com.weigan.loopview.OnItemScrollListener


class ModifyBirthdayDialog(
    private val activity: Activity,
    private val func: () -> Unit
) : Dialog(activity, R.style.app_dialog2) {
    private lateinit var binding: DialogModifyBirthdayBinding
    private var currentYear: String? = null
    private var currentMonth: String? = null
    private var currentDay: String? = null

    init {
        initVew()
    }

    private fun initVew() {
        binding = DialogModifyBirthdayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(true)

        binding.tvSave.setOnClickListener { commit() }

        chooseBirthDay()

        show()
    }

    private fun chooseBirthDay() {
        val yearList = AppUtil.getYearList()
        val monthList = AppUtil.getMonthList()
        val todayOfMonth = AppUtil.getTodayMonth()
        val dayList = arrayListOf<String>()

        binding.loopYear.setItems(yearList)
        binding.loopMonth.setItems(monthList)
        currentYear = yearList[18]
        currentMonth = todayOfMonth
        binding.loopYear.setCurrentPosition(18)
        binding.loopMonth.setCurrentPosition(12 - todayOfMonth.toInt())

        binding.loopYear.setOnItemScrollListener(object : OnItemScrollListener {
            override fun onItemScrollStateChanged(
                loopView: LoopView?,
                currentPassItem: Int,
                oldScrollState: Int,
                scrollState: Int,
                totalScrollY: Int
            ) {
                currentYear = yearList[currentPassItem]
                val d = AppUtil.getMonthOfDay(currentYear!!.toInt(), currentMonth!!.toInt())
                dayList.clear()
                for (index in 0 until d) {
                    dayList.add((index + 1).toString())
                }
                binding.loopDay.setItems(dayList)
            }

            override fun onItemScrolling(loopView: LoopView?, currentPassItem: Int, scrollState: Int, totalScrollY: Int) {
            }
        })

        binding.loopMonth.setOnItemScrollListener(object : OnItemScrollListener {
            override fun onItemScrollStateChanged(
                loopView: LoopView?,
                currentPassItem: Int,
                oldScrollState: Int,
                scrollState: Int,
                totalScrollY: Int
            ) {
                currentMonth = monthList[currentPassItem]
                val d = AppUtil.getMonthOfDay(currentYear!!.toInt(), currentMonth!!.toInt())
                dayList.clear()
                for (index in 0 until d) {
                    dayList.add((index + 1).toString())
                }
                binding.loopDay.setItems(dayList)
            }

            override fun onItemScrolling(loopView: LoopView?, currentPassItem: Int, scrollState: Int, totalScrollY: Int) {
            }
        })

        binding.loopDay.setOnItemScrollListener(object : OnItemScrollListener {
            override fun onItemScrollStateChanged(
                loopView: LoopView?,
                currentPassItem: Int,
                oldScrollState: Int,
                scrollState: Int,
                totalScrollY: Int
            ) {
                currentDay = dayList[currentPassItem]
            }

            override fun onItemScrolling(loopView: LoopView?, currentPassItem: Int, scrollState: Int, totalScrollY: Int) {
            }
        })
    }

    private fun commit() {
        if (currentYear == null || currentMonth == null || currentDay == null) {
            return
        }

        if (currentMonth!!.length == 1) {
            currentMonth = "0$currentMonth"
        }
        if (currentDay!!.length == 1) {
            currentDay = "0$currentDay"
        }

        val birthday = "$currentYear-$currentMonth-$currentDay 00:00:00"
        val birthdayTimestamp = AppUtil.date2TimeStamp(birthday)

        if (System.currentTimeMillis() - birthdayTimestamp < 18 * 365 * 24 * 3600 * 1000L) {
            ToastUtil.showShort(activity, activity.getString(R.string.guide_notice_limit_age))
            return
        }

        val nil = null
        DataManager.updateUserInfo(nil, birthday, nil, nil, nil, nil, nil) {
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