package com.ql.recovery.yay.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.blongho.country_data.World
import com.bumptech.glide.Glide
import com.netease.yunxin.kit.adapters.DataAdapter
import com.ql.recovery.bean.*
import com.ql.recovery.yay.R
import com.ql.recovery.yay.config.ChooseType
import com.ql.recovery.yay.config.GenderType
import com.ql.recovery.yay.databinding.DialogFilterBinding
import com.ql.recovery.yay.databinding.ItemCountryBinding
import com.ql.recovery.yay.ui.store.StoreActivity
import com.ql.recovery.yay.util.AppUtil
import com.ql.recovery.yay.util.ToastUtil
import com.tencent.mmkv.MMKV


class FilterDialog(
    private val activity: Activity,
    private val userInfo: UserInfo,
    private val basePrice: BasePrice,
    private var matchConfig: MatchConfig,
    private val type: ChooseType,
    private val func: () -> Unit
) : Dialog(activity, R.style.app_dialog2) {
    private lateinit var binding: DialogFilterBinding
    private lateinit var adapter: DataAdapter<Country>
    private var mk = MMKV.defaultMMKV()

    init {
        initVew()
    }

    @SuppressLint("SetTextI18n")
    private fun initVew() {
        binding = DialogFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(true)

        when (type) {
            ChooseType.Gender -> {
                binding.includeGender.root.visibility = View.VISIBLE
                binding.includeRegion.root.visibility = View.GONE
                binding.tvFilterTitle.text = activity.getString(R.string.home_sex_ref)
                binding.includeGender.tvAmountMale.typeface = Typeface.createFromAsset(activity.assets, "fonts/DINPro-Bold.otf")
                binding.includeGender.tvAmountFemale.typeface = Typeface.createFromAsset(activity.assets, "fonts/DINPro-Bold.otf")
                binding.includeGender.tvCoin.typeface = Typeface.createFromAsset(activity.assets, "fonts/DINPro-Bold.otf")
                binding.includeGender.tvCoin.text = userInfo.coin.toString()

                val count = basePrice.match_filter.sex_cost
                val countVip = (basePrice.match_filter.sex_cost * basePrice.match_filter.vip_discounts).toInt()
                if (userInfo.is_vip) {
                    binding.includeGender.tvAmountMale.text = countVip.toString()
                    binding.includeGender.tvAmountFemale.text = countVip.toString()
                } else {
                    binding.includeGender.tvAmountMale.text = count.toString()
                    binding.includeGender.tvAmountFemale.text = count.toString()
                }

                val discount = (basePrice.match_filter.vip_discounts * 100).toInt()
                val content = String.format(activity.getString(R.string.home_choose_tip_1), "${100 - discount}%")

                binding.includeGender.tvVipDiscount1.text = content

                when (matchConfig.target_sex) {
                    0 -> chooseGender(GenderType.All)
                    1 -> chooseGender(GenderType.Male)
                    2 -> chooseGender(GenderType.Female)
                }

                if (matchConfig.hand_free) {
                    binding.includeGender.ivHandFree.setImageResource(R.drawable.filter_open)
                } else {
                    binding.includeGender.ivHandFree.setImageResource(R.drawable.filter_close)
                }
            }

            ChooseType.Region -> {
                binding.includeGender.root.visibility = View.GONE
                binding.includeRegion.root.visibility = View.VISIBLE
                binding.tvFilterTitle.text = activity.getString(R.string.home_region_ref)

                val discount = (basePrice.match_filter.vip_discounts * 100).toInt()

                binding.includeRegion.tvMemberDiscount1.text = String.format(activity.getString(R.string.home_choose_tip_1), "${100 - discount}%")

                initRegionList()
            }
        }

        binding.includeGender.llMale.setOnClickListener { chooseGender(GenderType.Male) }
        binding.includeGender.llFemale.setOnClickListener { chooseGender(GenderType.Female) }
        binding.includeGender.llAllGender.setOnClickListener { chooseGender(GenderType.All) }
        binding.includeGender.tvMember.setOnClickListener { showPrimeDialog() }
        binding.includeGender.tvPurchase.setOnClickListener { toPayPage() }
        binding.includeGender.ivHandFree.setOnClickListener { changeHandFree() }

        show()
    }

    private fun chooseGender(type: GenderType) {
        when (type) {
            GenderType.All -> {
                binding.includeGender.llMale.background = ResourcesCompat.getDrawable(
                    activity.resources,
                    R.drawable.shape_rectangle_grey_solid_ffffff,
                    null
                )
                binding.includeGender.llFemale.background = ResourcesCompat.getDrawable(
                    activity.resources,
                    R.drawable.shape_rectangle_grey_solid_ffffff,
                    null
                )

                binding.includeGender.llAllGender.background = ResourcesCompat.getDrawable(
                    activity.resources,
                    R.drawable.shape_rectangle_yellow_solid_fefcde,
                    null
                )
                matchConfig.target_sex = 0
                mk.encode("match_config", matchConfig)
                cancel()
            }
            GenderType.Male -> {
                val userInfo = mk.decodeParcelable("user_info", UserInfo::class.java) ?: return
                if (userInfo.coin < basePrice.match_filter.sex_cost) {
                    toPayPage()
                    return
                }

                binding.includeGender.llMale.background = ResourcesCompat.getDrawable(
                    activity.resources,
                    R.drawable.shape_rectangle_yellow_solid_fefcde,
                    null
                )
                binding.includeGender.llFemale.background = ResourcesCompat.getDrawable(
                    activity.resources,
                    R.drawable.shape_rectangle_grey_solid_ffffff,
                    null
                )

                binding.includeGender.llAllGender.background = ResourcesCompat.getDrawable(
                    activity.resources,
                    R.drawable.shape_rectangle_grey_solid_ffffff,
                    null
                )

                matchConfig.target_sex = 1
                mk.encode("match_config", matchConfig)
                cancel()
            }
            GenderType.Female -> {
                val userInfo = mk.decodeParcelable("user_info", UserInfo::class.java) ?: return
                if (userInfo.coin < basePrice.match_filter.sex_cost) {
                    toPayPage()
                    return
                }

                binding.includeGender.llMale.background = ResourcesCompat.getDrawable(
                    activity.resources,
                    R.drawable.shape_rectangle_grey_solid_ffffff,
                    null
                )
                binding.includeGender.llFemale.background = ResourcesCompat.getDrawable(
                    activity.resources,
                    R.drawable.shape_rectangle_yellow_solid_fefcde,
                    null
                )

                binding.includeGender.llAllGender.background = ResourcesCompat.getDrawable(
                    activity.resources,
                    R.drawable.shape_rectangle_grey_solid_ffffff,
                    null
                )
                matchConfig.target_sex = 2
                mk.encode("match_config", matchConfig)
                cancel()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initRegionList() {
        val countryList = mk.decodeParcelable("country_list", Countries::class.java) ?: return
        val list = mutableListOf<Country>()
        list.addAll(countryList.country)

        adapter = DataAdapter.Builder<Country>()
            .setData(list)
            .setLayoutId(R.layout.item_country)
            .addBindView { itemView, itemData, position ->
                val itemBinding = ItemCountryBinding.bind(itemView)
                itemBinding.tvCoinConsume.typeface = Typeface.createFromAsset(activity.assets, "fonts/DINPro-Bold.otf")
                itemBinding.tvCountryName.text = itemData.en

                if (itemData.locale.isBlank()) {
                    Glide.with(activity).load("file:///android_asset/images/GLOBAL.png").into(itemBinding.ivCountryIcon)
                } else {
                    val flag = World.getFlagOf(itemData.locale)
                    itemBinding.ivCountryIcon.setImageResource(flag)
                }

                if (matchConfig.country_locale.isBlank()) {
                    if (itemData.code == 0) {
                        itemBinding.ivCheck.setImageResource(R.drawable.checked)
                    } else {
                        itemBinding.ivCheck.setImageResource(R.drawable.unchecked)
                    }
                } else {
                    if (itemData.locale == matchConfig.country_locale) {
                        itemBinding.ivCheck.setImageResource(R.drawable.checked)
                    } else {
                        itemBinding.ivCheck.setImageResource(R.drawable.unchecked)
                    }
                }

                if (itemData.code == 0) {
                    itemBinding.ivCoin.visibility = View.GONE
                    itemBinding.tvCoinConsume.text = activity.getString(R.string.home_free)
                } else {
                    itemBinding.ivCoin.visibility = View.VISIBLE
                    val count = basePrice.match_filter.sex_cost
                    val countVip = (basePrice.match_filter.sex_cost * basePrice.match_filter.vip_discounts).toInt()
                    if (userInfo.is_vip) {
                        itemBinding.tvCoinConsume.text = countVip.toString()
                    } else {
                        itemBinding.tvCoinConsume.text = count.toString()
                    }
                }

                itemBinding.ivCheck.setOnClickListener {
                    if (itemData.code != 0) {
                        val userInfo = mk.decodeParcelable("user_info", UserInfo::class.java) ?: return@setOnClickListener
                        if (userInfo.coin < basePrice.match_filter.country_cost) {
                            toPayPage()
                            return@setOnClickListener
                        }
                    }

                    matchConfig.country_locale = itemData.locale
                    matchConfig.country_name = itemData.en
                    mk.encode("match_config", matchConfig)
                    adapter.notifyDataSetChanged()
                    cancel()
                }

            }
            .create()

        binding.includeRegion.rvRegion.adapter = adapter
        binding.includeRegion.rvRegion.layoutManager = LinearLayoutManager(activity)
        adapter.notifyDataSetChanged()

        for ((position, item) in list.withIndex()) {
            if (item.locale == matchConfig.country_locale) {
                binding.includeRegion.rvRegion.scrollToPosition(position)
            }
        }
    }

    private fun changeHandFree() {
        if (matchConfig.hand_free) {
            binding.includeGender.ivHandFree.setImageResource(R.drawable.filter_close)
            matchConfig.hand_free = false
            mk.encode("match_config", matchConfig)
        } else {
            binding.includeGender.ivHandFree.setImageResource(R.drawable.filter_open)
            matchConfig.hand_free = true
            mk.encode("match_config", matchConfig)
            ToastUtil.showShort(activity, activity.getString(R.string.match_hand_off_tip))
        }
    }

    private fun toPayPage() {
        activity.startActivity(Intent(activity, StoreActivity::class.java))
    }

    private fun showPrimeDialog() {
        cancel()
        val userInfo = mk.decodeParcelable("user_info", UserInfo::class.java) ?: return
        PrimeDialog(activity, userInfo.is_vip) {}
    }

    override fun cancel() {
        super.cancel()
//        setWindowAlpha(1.0f)
        func()
    }

    override fun show() {
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.BOTTOM
            width = AppUtil.getScreenWidth(context)
            height = WindowManager.LayoutParams.WRAP_CONTENT
            flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
            windowAnimations = R.style.translate_up_down
            dimAmount = 0.5f
        }

        super.show()
    }

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