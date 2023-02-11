//package com.netease.yunxin.kit.chatkit.ui.dialog
//
//import android.app.Activity
//import android.app.Dialog
//import android.content.Context
//import android.graphics.Typeface
//import android.os.Handler
//import android.os.Looper
//import android.view.Gravity
//import android.view.View
//import android.view.WindowManager
//import android.widget.LinearLayout
//import androidx.core.content.res.ResourcesCompat
//import androidx.viewpager2.widget.CompositePageTransformer
//import androidx.viewpager2.widget.ViewPager2
//import com.android.billingclient.api.*
//import com.google.common.collect.ImmutableList
//import com.netease.yunxin.kit.adapters.DataAdapter
//import com.netease.yunxin.kit.common.utils.ThreadUtils.runOnUiThread
//import com.ql.recovery.bean.Prime
//import com.ql.recovery.bean.Server
//import com.ql.recovery.manager.DataManager
//import com.ql.recovery.yay.R
//import com.ql.recovery.yay.callback.PayCallback
//import com.ql.recovery.yay.databinding.DialogPrimeBinding
//import com.ql.recovery.yay.databinding.ItemPrimeBinding
//import com.ql.recovery.yay.manager.PayManager
//import com.ql.recovery.yay.util.AppUtil
//import com.ql.recovery.yay.util.JLog
//import com.ql.recovery.yay.util.ToastUtil
//
//class PrimeDialog(
//    private val activity: Activity,
//    private val isPrime: Boolean,
//    private val func: () -> Unit
//) : Dialog(activity, R.style.app_dialog2) {
//    private lateinit var binding: DialogPrimeBinding
//    private lateinit var pagerAdapter: DataAdapter<Prime>
//    private lateinit var billingClient: BillingClient
//    private var handler = Handler(Looper.getMainLooper())
//    private var runnable: Runnable? = null
//    private var currentPosition = 0
//    private var mList = arrayListOf<Prime>()
//    private var lastClickTime = 0L
//    private var currentServer: Server? = null
//    private var mType = Type.Pay
//
//    init {
//        initVew()
//    }
//
//    private fun initVew() {
//        binding = DialogPrimeBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//        setCancelable(true)
//
//        binding.tvCommit.setOnClickListener { checkPay() }
//
//        getSubProductList()
//        initViewPager()
//        getRightList()
//
//        if (isPrime) {
//            initPrimeStatus()
//        } else {
//            binding.llPrice.visibility = View.VISIBLE
//            binding.flDemon.visibility = View.GONE
//
//            mType = Type.Pay
//        }
//
//        show()
//    }
//
//    private fun initPrimeStatus() {
//        binding.llPrice.visibility = View.GONE
//        binding.flDemon.visibility = View.VISIBLE
//
//        mType = Type.Reward
//        checkVIPStatus()
//
//        //更新用户资料
//        DataManager.getUserInfo { }
//    }
//
//    private fun initViewPager() {
//        if (runnable != null) {
//            return
//        }
//
//        runnable = kotlinx.coroutines.Runnable {
//            if (currentPosition == mList.size - 1) {
//                currentPosition = 0
//                binding.vpRights.setCurrentItem(0, true)
//            } else {
//                currentPosition += 1
//                binding.vpRights.setCurrentItem(currentPosition, true)
//            }
//        }
//
//        binding.vpRights.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
//            override fun onPageScrollStateChanged(state: Int) {
//                if (state == ViewPager2.SCROLL_STATE_IDLE) {
//                    binding.vpRights.setCurrentItem(currentPosition, true)
//                    return
//                }
//
//                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
//                    handler.removeCallbacks(runnable!!)
//                }
//            }
//
//            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
//
//            }
//
//            override fun onPageSelected(position: Int) {
//                currentPosition = position
//                handler.postDelayed(runnable!!, 1500)
//
//                for (pos in 0 until binding.llIndicator.childCount) {
//                    val view = binding.llIndicator.getChildAt(pos)
//                    if (pos == position) {
//                        view.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.shape_corner_white, null)
//                    } else {
//                        view.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.shape_corner_grey, null)
//                    }
//                }
//            }
//
//        })
//
//        pagerAdapter = DataAdapter.Builder<Prime>()
//            .setData(mList)
//            .setLayoutId(R.layout.item_prime)
//            .addBindView { itemView, itemData ->
//                val itemBinding = ItemPrimeBinding.bind(itemView)
//                itemBinding.ivPic.setImageResource(itemData.icon)
//                itemBinding.tvName.text = itemData.res
//            }
//            .create()
//
//        val compositePageTransformer = CompositePageTransformer()
//
//        binding.vpRights.apply {
//            adapter = pagerAdapter
//            setPageTransformer(compositePageTransformer)
//            orientation = ViewPager2.ORIENTATION_HORIZONTAL
//        }
//    }
//
//    private fun getRightList() {
//        mList.clear()
//        mList.add(Prime(R.drawable.prime_01, activity.getString(R.string.prime_rights_01)))
//        mList.add(Prime(R.drawable.prime_02, activity.getString(R.string.prime_rights_02)))
//        mList.add(Prime(R.drawable.prime_03, activity.getString(R.string.prime_rights_03)))
//        mList.add(Prime(R.drawable.prime_04, activity.getString(R.string.prime_rights_04)))
//        mList.add(Prime(R.drawable.prime_05, activity.getString(R.string.prime_rights_05)))
//        mList.add(Prime(R.drawable.prime_06, activity.getString(R.string.prime_rights_06)))
//        mList.add(Prime(R.drawable.prime_07, activity.getString(R.string.prime_rights_07)))
//        mList.add(Prime(R.drawable.prime_08, activity.getString(R.string.prime_rights_08)))
//
//        pagerAdapter.notifyItemRangeChanged(0, mList.size)
//
//        for (item in 1..mList.size) {
//            val view = View(activity)
//            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
//            lp.setMargins(AppUtil.dp2px(activity, 5f), 0, 0, 0)
//            lp.width = AppUtil.dp2px(activity, 5f)
//            lp.height = AppUtil.dp2px(activity, 5f)
//            view.layoutParams = lp
//            view.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.shape_corner_grey, null)
//            binding.llIndicator.addView(view)
//        }
//    }
//
//    private fun getSubProductList() {
//        val listener = PurchasesUpdatedListener { billingResult, purchases -> }
//
//        billingClient = BillingClient.newBuilder(activity)
//            .setListener(listener)
//            .enablePendingPurchases()
//            .build()
//
//        DataManager.getProductList("sub") {
//            if (it.isNotEmpty()) {
//                currentServer = it[0]
//                binding.tvPrice.typeface = Typeface.createFromAsset(activity.assets, "fonts/DINPro-Bold.otf")
//
//                billingClient.startConnection(object : BillingClientStateListener {
//                    override fun onBillingServiceDisconnected() {
//                        // Try to restart the connection on the next request to
//                        // Google Play by calling the startConnection() method.
//                        JLog.i("onBillingServiceDisconnected")
//                    }
//
//                    override fun onBillingSetupFinished(billingResult: BillingResult) {
//                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
//                            // The BillingClient is ready. You can query purchases here.
//                            JLog.i("onBillingSetupFinished")
//
//                            val queryProductDetailsParams =
//                                QueryProductDetailsParams.newBuilder()
//                                    .setProductList(
//                                        ImmutableList.of(
//                                            QueryProductDetailsParams.Product.newBuilder()
//                                                .setProductId(it[0].code)
//                                                .setProductType(BillingClient.ProductType.SUBS)
//                                                .build()
//                                        )
//                                    )
//                                    .build()
//
//                            billingClient.queryProductDetailsAsync(queryProductDetailsParams) { result, productDetailsList ->
//                                // check billingResult
//                                // process returned productDetailsList
//                                if (productDetailsList.isEmpty()) {
//                                    JLog.i("no goods found")
//                                    return@queryProductDetailsAsync
//                                }
//
//                                for (productDetails in productDetailsList) {
//                                    JLog.i("product = $productDetails")
//                                    if (!productDetails.subscriptionOfferDetails.isNullOrEmpty()) {
//                                        val pricingPhases = productDetails.subscriptionOfferDetails!![0].pricingPhases
//                                        for (item in pricingPhases.pricingPhaseList) {
//                                            JLog.i("price = ${item.formattedPrice}")
//                                            JLog.i("code = ${item.priceCurrencyCode}")
//                                            runOnUiThread {
//                                                binding.tvPrice.text = item.formattedPrice
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//
//                        } else {
//                            JLog.i("error code = ${billingResult.responseCode},${billingResult.debugMessage}")
//                            when (billingResult.responseCode) {
//                            }
//                        }
//                    }
//                })
//            }
//        }
//    }
//
//    private fun checkVIPStatus() {
//        DataManager.getVipStatus { vip ->
//            //设置会员截止时间
//            binding.tvEndTime.text = String.format(activity.getString(R.string.prime_end_time), AppUtil.timeStamp2Date(vip.expires_date, ""))
//
//            if (vip.received) {
//                binding.ivCheck.visibility = View.VISIBLE
//                binding.tvCount.text = vip.number_of_coins.toString()
//                binding.tvCommit.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.shape_corner_light_yellow, null)
//                binding.tvCommit.text = activity.getString(R.string.prime_get)
//            } else {
//                binding.ivCheck.visibility = View.GONE
//                binding.tvCount.text = vip.number_of_coins.toString()
//                binding.tvCommit.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.shape_corner_yellow_10, null)
//                binding.tvCommit.text = activity.getString(R.string.prime_get_free)
//            }
//        }
//    }
//
//    private fun checkPay() {
//        when (mType) {
//            Type.Pay -> doPay()
//            Type.Reward -> getReward()
//        }
//    }
//
//    private fun getReward() {
//        DataManager.getVipReward {
//            if (it) {
//                binding.ivCheck.visibility = View.VISIBLE
//                binding.tvCommit.background = ResourcesCompat.getDrawable(activity.resources, R.drawable.shape_corner_light_yellow, null)
//                binding.tvCommit.text = activity.getString(R.string.prime_get)
//            }
//        }
//    }
//
//    private fun doPay() {
//        if (currentServer == null) return
//
//        if (lastClickTime == 0L) {
//            lastClickTime = System.currentTimeMillis()
//        } else if (System.currentTimeMillis() - lastClickTime < 2 * 1000) {
//            return
//        }
//
//        lastClickTime = System.currentTimeMillis()
//
//        DataManager.createOrder(currentServer!!.id) {
//            if (it.is_paid) {
//                cancel()
//                func()
//                return@createOrder
//            }
//
////            PayManager.get().doGoogleFastPay(
////                activity,
////                currentServer!!.code,
////                "acknowledge",
////                it.order_id,
////                object : PayCallback {
////                    override fun success(token: String) {
////                        runOnUiThread {
////                            initPrimeStatus()
////                            func()
////                        }
////                    }
////
////                    override fun failed(msg: String) {
////                        runOnUiThread {
////                            ToastUtil.showShort(activity, msg)
////                        }
////                    }
////                })
//        }
//    }
//
//    override fun show() {
//        window!!.decorView.setPadding(0, 0, 0, 0)
//        window!!.attributes = window!!.attributes.apply {
//            gravity = Gravity.BOTTOM
//            width = AppUtil.getScreenWidth(context)
//            height = WindowManager.LayoutParams.WRAP_CONTENT
//        }
//        super.show()
//    }
//
//    enum class Type { Pay, Reward }
//
//}