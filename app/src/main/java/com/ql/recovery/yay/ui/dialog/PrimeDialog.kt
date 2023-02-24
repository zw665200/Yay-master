package com.ql.recovery.yay.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.android.billingclient.api.*
import com.google.common.collect.ImmutableList
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.netease.yunxin.kit.adapters.DataAdapter
import com.netease.yunxin.kit.common.utils.ThreadUtils.runOnUiThread
import com.ql.recovery.bean.Prime
import com.ql.recovery.bean.Server
import com.ql.recovery.config.Config
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.callback.PayCallback
import com.ql.recovery.yay.databinding.DialogPrimeBinding
import com.ql.recovery.yay.databinding.ItemPrimeBinding
import com.ql.recovery.yay.manager.PayManager
import com.ql.recovery.yay.manager.ReportManager
import com.ql.recovery.yay.util.AppUtil
import com.ql.recovery.yay.util.JLog
import com.ql.recovery.yay.util.ToastUtil

class PrimeDialog(
    private val activity: Activity,
    private val isPrime: Boolean,
    private val func: () -> Unit
) : Dialog(activity, R.style.app_dialog2) {
    private lateinit var binding: DialogPrimeBinding
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var pagerAdapter: DataAdapter<Prime>
    private lateinit var billingClient: BillingClient
    private var handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null
    private var currentPosition = 0
    private var mList = arrayListOf<Prime>()
    private var lastClickTime = 0L
    private var currentServer: Server? = null
    private var mType = Type.Pay

    init {
        initVew()
    }

    private fun initVew() {
        binding = DialogPrimeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(true)

        firebaseAnalytics = Firebase.analytics

        binding.tvCommit.setOnClickListener { checkPay() }

        getSubProductList()
        initViewPager()
        getBenefitsList()

        if (isPrime) {
            initPrimeStatus()
        } else {
            binding.llPrice.visibility = View.VISIBLE
            binding.flDemon.visibility = View.GONE

            mType = Type.Pay
        }

        ReportManager.firebaseCustomLog(firebaseAnalytics, "prime_dialog_open", "dialog open")
        ReportManager.appsFlyerCustomLog(activity, "prime_dialog_open", "dialog open")

        show()
    }

    private fun initPrimeStatus() {
        binding.llPrice.visibility = View.GONE
        binding.flDemon.visibility = View.VISIBLE

        mType = Type.Reward
        checkVIPStatus()

        //更新用户资料
        DataManager.getUserInfo { }
    }

    private fun initViewPager() {
        if (runnable != null) {
            return
        }

        runnable = kotlinx.coroutines.Runnable {
            if (currentPosition == mList.size - 1) {
                currentPosition = 0
                binding.vpRights.setCurrentItem(0, false)
            } else {
                currentPosition += 1
                binding.vpRights.setCurrentItem(currentPosition, false)
            }
        }

        binding.vpRights.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    binding.vpRights.setCurrentItem(currentPosition, false)
                    return
                }

                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    handler.removeCallbacks(runnable!!)
                }
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                currentPosition = position
                handler.postDelayed(runnable!!, 3000)

                for (pos in 0 until binding.llIndicator.childCount) {
                    val view = binding.llIndicator.getChildAt(pos)
                    if (pos == position) {
                        view.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.shape_corner_white, null)
                    } else {
                        view.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.shape_corner_grey, null)
                    }
                }
            }

        })

        pagerAdapter = DataAdapter.Builder<Prime>()
            .setData(mList)
            .setLayoutId(R.layout.item_prime)
            .addBindView { itemView, itemData ->
                val itemBinding = ItemPrimeBinding.bind(itemView)
                itemBinding.ivPic.setImageResource(itemData.icon)
                itemBinding.tvName.text = itemData.res
            }
            .create()

        val compositePageTransformer = CompositePageTransformer()

        binding.vpRights.apply {
            adapter = pagerAdapter
            setPageTransformer(compositePageTransformer)
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
        }
    }

    private fun getBenefitsList() {
        DataManager.getBasePrice { basePrice ->
            mList.clear()
            mList.add(Prime(R.drawable.prime_01, String.format(activity.getString(R.string.prime_rights_01), (basePrice.match_filter.sex_cost * basePrice.match_filter.vip_discounts).toInt())))
            mList.add(Prime(R.drawable.prime_02, activity.getString(R.string.prime_rights_02)))
            mList.add(Prime(R.drawable.prime_03, activity.getString(R.string.prime_rights_03)))
            mList.add(Prime(R.drawable.prime_04, activity.getString(R.string.prime_rights_04)))
            mList.add(Prime(R.drawable.prime_05, activity.getString(R.string.prime_rights_05)))
            mList.add(Prime(R.drawable.prime_06, activity.getString(R.string.prime_rights_06)))
            mList.add(Prime(R.drawable.prime_07, activity.getString(R.string.prime_rights_07)))
            mList.add(Prime(R.drawable.prime_08, activity.getString(R.string.prime_rights_08)))

            pagerAdapter.notifyItemRangeChanged(0, mList.size)

            for (item in 1..mList.size) {
                val view = View(activity)
                val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                lp.setMargins(AppUtil.dp2px(activity, 5f), 0, 0, 0)
                lp.width = AppUtil.dp2px(activity, 5f)
                lp.height = AppUtil.dp2px(activity, 5f)
                view.layoutParams = lp
                view.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.shape_corner_grey, null)
                binding.llIndicator.addView(view)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getSubProductList() {
        val listener = PurchasesUpdatedListener { billingResult, purchases -> }

        billingClient = BillingClient.newBuilder(activity)
            .setListener(listener)
            .enablePendingPurchases()
            .build()

        DataManager.getProductList("sub") {
            if (it.isNotEmpty()) {
                currentServer = it[0]
                binding.tvPrice.text = "$${currentServer!!.price}"

                billingClient.startConnection(object : BillingClientStateListener {
                    override fun onBillingServiceDisconnected() {
                        // Try to restart the connection on the next request to
                        // Google Play by calling the startConnection() method.
                        JLog.i("onBillingServiceDisconnected")
                    }

                    override fun onBillingSetupFinished(billingResult: BillingResult) {
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            // The BillingClient is ready. You can query purchases here.
                            JLog.i("onBillingSetupFinished")

                            val queryProductDetailsParams =
                                QueryProductDetailsParams.newBuilder()
                                    .setProductList(
                                        ImmutableList.of(
                                            QueryProductDetailsParams.Product.newBuilder()
                                                .setProductId(it[0].code)
                                                .setProductType(BillingClient.ProductType.SUBS)
                                                .build()
                                        )
                                    )
                                    .build()

                            billingClient.queryProductDetailsAsync(queryProductDetailsParams) { _, productDetailsList ->
                                // check billingResult
                                // process returned productDetailsList
                                if (productDetailsList.isEmpty()) {
                                    JLog.i("no goods found")
                                    return@queryProductDetailsAsync
                                }

                                for (productDetails in productDetailsList) {
//                                    JLog.i("product = $productDetails")
                                    if (!productDetails.subscriptionOfferDetails.isNullOrEmpty()) {
                                        val pricingPhases = productDetails.subscriptionOfferDetails!![0].pricingPhases
                                        for (item in pricingPhases.pricingPhaseList) {
//                                            JLog.i("price = ${item.formattedPrice}")
//                                            JLog.i("code = ${item.priceCurrencyCode}")
                                            runOnUiThread {
                                                binding.tvPrice.text = item.formattedPrice
                                                currentServer!!.currency = item.priceCurrencyCode
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                })
            }
        }
    }

    private fun checkVIPStatus() {
        DataManager.getVipStatus { vip ->
            //设置会员截止时间
            binding.tvEndTime.text = String.format(activity.getString(R.string.prime_end_time), AppUtil.timeStamp2Date(vip.expires_date, ""))

            if (vip.received) {
                binding.ivCheck.visibility = View.VISIBLE
                binding.tvCount.text = vip.number_of_coins.toString()
                binding.tvCommit.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.shape_corner_light_yellow, null)
                binding.tvCommit.text = activity.getString(R.string.prime_get)
            } else {
                binding.ivCheck.visibility = View.GONE
                binding.tvCount.text = vip.number_of_coins.toString()
                binding.tvCommit.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.shape_corner_yellow_10, null)
                binding.tvCommit.text = activity.getString(R.string.prime_get_free)
            }
        }
    }

    private fun checkPay() {
        when (mType) {
            Type.Pay -> doPay()
            Type.Reward -> getReward()
        }
    }

    private fun getReward() {
        DataManager.getVipReward {
            if (it) {
                binding.tvCommit.isEnabled = false
                binding.ivCheck.visibility = View.VISIBLE
                binding.tvCommit.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.shape_corner_light_yellow, null)
                binding.tvCommit.text = activity.getString(R.string.prime_get)

                //刷新用户信息
                Config.mainHandler?.sendEmptyMessage(0x10006)
                Config.subscriberHandler?.sendEmptyMessage(0x10001)
            }
        }
    }

    private fun doPay() {
        if (currentServer == null) return

        if (lastClickTime == 0L) {
            lastClickTime = System.currentTimeMillis()
        } else if (System.currentTimeMillis() - lastClickTime < 2 * 1000) {
            return
        }

        lastClickTime = System.currentTimeMillis()

        if (currentServer!!.code.isBlank()) {
            ReportManager.firebaseCustomLog(firebaseAnalytics, "purchase_begin_click", "free")
            ReportManager.appsFlyerCustomLog(activity, "purchase_begin_click", "free")
        } else {
            ReportManager.firebaseCustomLog(firebaseAnalytics, "purchase_begin_click", currentServer!!.code)
            ReportManager.appsFlyerCustomLog(activity, "purchase_begin_click", currentServer!!.code)
        }

        DataManager.createOrder(currentServer!!.id) {
            if (it.is_paid) {
                cancel()
                func()
                return@createOrder
            }

            PayManager.get().doGoogleFastPay(
                activity,
                currentServer!!.code,
                "acknowledge",
                it.order_id,
                object : PayCallback {
                    override fun success(token: String) {
                        runOnUiThread {
                            initPrimeStatus()
                            func()

                            //刷新用户信息
                            Config.mainHandler?.sendEmptyMessage(0x10006)
                            Config.subscriberHandler?.sendEmptyMessage(0x10001)

                            //上报支付日志
                            if (currentServer!!.currency != null) {
                                ReportManager.firebasePurchaseLog(firebaseAnalytics, currentServer!!.currency!!, currentServer!!.price)
                                ReportManager.facebookPurchaseLog(activity, currentServer!!.currency!!, currentServer!!.price)
                                ReportManager.branchPurchaseLog(activity, currentServer!!.name, currentServer!!.currency!!, currentServer!!.price)
                                ReportManager.appsFlyerPurchaseLog(activity, currentServer!!.code, currentServer!!.price)

                                val map = HashMap<String, String>()
                                map["currency"] = currentServer!!.currency!!
                                map["price"] = currentServer!!.price

                                ReportManager.branchCustomLog(activity, "purchased", map)
                            } else {
                                ReportManager.firebasePurchaseLog(firebaseAnalytics, "USD", currentServer!!.price)
                                ReportManager.facebookPurchaseLog(activity, "USD", currentServer!!.price)
                                ReportManager.branchPurchaseLog(activity, currentServer!!.name, "USD", currentServer!!.price)
                                ReportManager.appsFlyerPurchaseLog(activity, currentServer!!.code, currentServer!!.price)

                                val map = HashMap<String, String>()
                                map["currency"] = "USD"
                                map["price"] = currentServer!!.price

                                ReportManager.branchCustomLog(activity, "purchased", map)
                            }
                        }
                    }

                    override fun failed(msg: String) {
                        runOnUiThread {
                            ToastUtil.showShort(activity, msg)
                            ReportManager.firebaseCustomLog(firebaseAnalytics, "purchase_cancel", "purchase cancel")
                            ReportManager.appsFlyerCustomLog(activity, "purchase_cancel", "purchase cancel")
                        }
                    }
                })
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

    enum class Type { Pay, Reward }

}