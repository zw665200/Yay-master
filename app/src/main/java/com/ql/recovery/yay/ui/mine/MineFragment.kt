package com.ql.recovery.yay.ui.mine

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.netease.yunxin.kit.adapters.DataAdapter
import com.ql.recovery.bean.Resource
import com.ql.recovery.bean.UserInfo
import com.ql.recovery.config.Config
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.callback.FileCallback
import com.ql.recovery.yay.databinding.FragmentMineBinding
import com.ql.recovery.yay.databinding.ItemFunctionBinding
import com.ql.recovery.yay.databinding.ItemVideoBinding
import com.ql.recovery.yay.databinding.ItemWallpaperBinding
import com.ql.recovery.yay.manager.CManager
import com.ql.recovery.yay.manager.ImageManager
import com.ql.recovery.yay.manager.RtcManager
import com.ql.recovery.yay.ui.base.BaseFragment
import com.ql.recovery.yay.ui.dialog.PrimeDialog
import com.ql.recovery.yay.ui.dialog.WaitingDialog
import com.ql.recovery.yay.ui.notifications.ClubViewModel
import com.ql.recovery.yay.ui.store.StoreActivity
import com.ql.recovery.yay.util.AppUtil
import com.ql.recovery.yay.util.DoubleUtils
import com.ql.recovery.yay.util.FileUtil
import com.ql.recovery.yay.util.JLog
import com.yanzhenjie.album.Album

class MineFragment : BaseFragment() {
    private var binding: FragmentMineBinding? = null
    private lateinit var mAdapter: DataAdapter<Resource>
    private lateinit var mPicAdapter: DataAdapter<String>
    private lateinit var mVideoAdapter: DataAdapter<String>
    private lateinit var waitingDialog: WaitingDialog
    private var mPicList = arrayListOf<String>()
    private var mVideoList = arrayListOf<String>()

    override fun initView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMineBinding.inflate(inflater, container, false)
        val notificationsViewModel = ViewModelProvider(this)[ClubViewModel::class.java]

        binding!!.ivSetting.setOnClickListener { toSettingPage() }
        binding!!.ivAvatar.setOnClickListener { showUserDetail() }
        binding!!.includeUser.tvNickname.setOnClickListener { toProfilePage() }
        binding!!.includeUser.flMoreProfile.setOnClickListener { toProfilePage() }
        binding!!.llFollowed.setOnClickListener { toFollowPage() }
        binding!!.llFollowing.setOnClickListener { toFollowPage() }
        binding!!.includeUser.tvLevel.setOnClickListener { toLevelPage() }
        binding!!.includeUser.tvScore.setOnClickListener { toScorePage() }
        binding!!.ivUnfold.setOnClickListener { unFold() }
        binding!!.ivFold.setOnClickListener { fold() }

        waitingDialog = WaitingDialog(requireActivity())
        getUserInfo()
        loadFunction()

        binding!!.refreshLayout.setOnRefreshListener { refreshLayout ->
            refreshLayout.finishRefresh()
            getUserInfo()
            requestToUpdateOnlineTime()
            loadFunction()
        }

        return binding!!.root
    }

    override fun initData() {
        requestToUpdateOnlineTime()
    }

    override fun setOnlineStatus(uid: String, online: Boolean) {
    }

    override fun refreshOnlineTime() {
        getAnchorOnline()
    }

    private fun loadFunction() {
        val list = arrayListOf<Resource>()
        list.add(Resource("member", R.drawable.mine_member, getString(R.string.mine_member)))
        list.add(Resource("store", R.drawable.mine_store, getString(R.string.mine_store)))
        list.add(Resource("income", R.drawable.mine_income, getString(R.string.mine_income_host)))

        mAdapter = DataAdapter.Builder<Resource>()
            .setData(list)
            .setLayoutId(R.layout.item_function)
            .addBindView { itemView, itemData ->
                val itemBinding = ItemFunctionBinding.bind(itemView)
                itemBinding.ivIcon.setImageResource(itemData.icon)
                itemBinding.tvName.text = itemData.name

                when (itemData.type) {
                    "store" -> {
                        itemBinding.tvCoin.visibility = View.VISIBLE

                        getUserInfo { userInfo ->
                            itemBinding.tvCoin.text = userInfo.coin.toString()
                        }
                    }

                    "income" -> {
                        itemBinding.tvCoin.visibility = View.VISIBLE

                        getUserInfo { userInfo ->
                            itemBinding.tvCoin.text = userInfo.coin_income.toString()
                        }

                        getUserInfo { userInfo ->
                            if (userInfo.role == "anchor") {
                                itemBinding.tvName.text = getString(R.string.mine_income_host)
                            } else {
                                itemBinding.tvName.text = getString(R.string.mine_income)
                            }
                        }
                    }
                }

                itemView.setOnClickListener {
                    when (itemData.type) {
                        "store" -> toStorePage()
                        "income" -> toIncomePage()
                        "member" -> showPrimeDialog()
                    }
                }
            }
            .create()

        binding?.rcMenuList?.layoutManager = LinearLayoutManager(requireActivity())
        binding?.rcMenuList?.adapter = mAdapter
        mAdapter.notifyItemRangeChanged(0, list.size)
    }

    private fun getUserInfo() {
        DataManager.getUserInfo { userInfo ->
            //??????VIP????????????
            if (userInfo.is_vip) {
                binding?.ivBackground?.visibility = View.VISIBLE
                binding?.includeUser?.ivVip?.visibility = View.VISIBLE
            } else {
                binding?.ivBackground?.visibility = View.GONE
                binding?.includeUser?.ivVip?.visibility = View.GONE
            }

            //????????????
            Glide.with(this).load(userInfo.avatar).apply(RequestOptions.bitmapTransform(CircleCrop())).into(binding!!.ivAvatar)

            //?????????????????????
            binding?.includeUser?.tvNickname?.text = userInfo.nickname
            binding?.includeUser?.tvAge?.text = userInfo.age.toString()

            //????????????
            when (userInfo.sex) {
                1 -> binding?.includeUser?.ivGender?.setImageResource(R.drawable.home_male)
                2 -> binding?.includeUser?.ivGender?.setImageResource(R.drawable.home_female)
            }

            //????????????
            if (userInfo.country.isNotBlank()) {
                val res = "file:///android_asset/images/${userInfo.country}.png"
                ImageManager.getBitmap(requireContext(), res) { bitmap ->
                    binding!!.includeUser.ivNation.setImageBitmap(bitmap)
                }
            }

            //??????????????????
            if (userInfo.role == "anchor") {
                binding?.ivHost?.visibility = View.VISIBLE
                binding?.flOnlineTime?.visibility = View.VISIBLE
            } else {
                binding?.ivHost?.visibility = View.GONE
                binding?.flOnlineTime?.visibility = View.GONE
            }

            //??????????????????
            val score = String.format("%.1f", userInfo.grace_score)
            binding?.includeUser?.tvScore?.text = String.format(requireActivity().getString(R.string.club_score), score)

            //??????????????????
            binding?.includeUser?.tvLevel?.text = String.format(requireActivity().getString(R.string.club_level), userInfo.grade)

            //??????UID
            binding?.includeUser?.tvUid?.text = String.format(getString(R.string.club_uid), userInfo.uid)

            //????????????
            binding?.tvFollower?.text = userInfo.followers.toString()
            binding?.tvFollowing?.text = userInfo.following.toString()

            //????????????
            if (userInfo.tags.isNotEmpty()) {
                binding?.llTags?.removeAllViews()
                for (item in userInfo.tags) {
                    val p5 = AppUtil.dp2px(requireContext(), 5f)
                    val p10 = AppUtil.dp2px(requireContext(), 10f)
                    val view = TextView(requireContext())
                    view.setPadding(p10, p5, p10, p5)
                    val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    lp.setMargins(0, 0, p10, 0)
                    view.layoutParams = lp
                    view.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_white_30_alpha_50, null)
                    view.text = item.name
                    view.textSize = 12f
                    view.setTextColor(ResourcesCompat.getColor(resources, R.color.color_black, null))
                    binding!!.llTags.addView(view)
                }
            }

            //???????????????????????????
            mAdapter.notifyItemChanged(1)
            mAdapter.notifyItemChanged(2)

            loadPicList(userInfo)
            loadVideoList(userInfo)
        }
    }

    private fun requestToUpdateOnlineTime() {
        getUserInfo { userInfo ->
            if (userInfo.role == "anchor") {
                Config.mHandler?.sendEmptyMessage(0x10006)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getAnchorOnline() {
        getUserInfo { userInfo ->
            if (userInfo.role == "anchor") {
                DataManager.getAnchorOnlineTime { onlineTime ->
                    val allTime = AppUtil.second2Hour(onlineTime.duration)
                    JLog.i("allTime = $allTime")
                    binding?.tvOnlineTime?.text = allTime.toString()
                    binding?.tvCallTimes?.text = onlineTime.answer_count.toString()
                    binding?.tvCallTime?.text = AppUtil.second2Hour(onlineTime.call_duration)
                    binding?.tvCallRejectRate?.text = "${onlineTime.reject_rate}%"
                    binding?.tvCallBenefit?.text = "${onlineTime.call_income} gems"
                    binding?.tvGiftBenefit?.text = "${onlineTime.gift_income} gems"
                }
            }
        }
    }

    private fun unFold() {
        binding?.ivUnfold?.visibility = View.GONE
        binding?.llSecondData?.visibility = View.VISIBLE
        binding?.llThirdData?.visibility = View.VISIBLE
    }

    private fun fold() {
        binding?.ivUnfold?.visibility = View.VISIBLE
        binding?.llSecondData?.visibility = View.GONE
        binding?.llThirdData?.visibility = View.GONE
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadPicList(userInfo: UserInfo) {
        val width = AppUtil.getScreenWidth(requireContext()) / 3
        mPicAdapter = DataAdapter.Builder<String>()
            .setData(mPicList)
            .setLayoutId(R.layout.item_wallpaper)
            .addBindView { itemView, itemData, position ->
                //??????item?????????
                val lp = itemView.layoutParams
                lp.width = width
                lp.height = width * 152 / 123
                itemView.layoutParams = lp

                val itemBinding = ItemWallpaperBinding.bind(itemView)
                if (itemData.isBlank()) {
                    itemBinding.ivUpload.visibility = View.VISIBLE
                } else {
                    itemBinding.ivUpload.visibility = View.GONE
                    Glide.with(requireActivity()).load(itemData).placeholder(R.drawable.placeholder).into(itemBinding.ivPic)
                }

                itemView.setOnClickListener {
                    if (itemData.isBlank()) {
                        getImageFromAlbum()
                    } else {

                        Album.gallery(requireContext())
                            .checkedList(mPicList.filter { it != "" } as java.util.ArrayList<String>?)
                            .checkable(false)
                            .currentPosition(position)
                            .onResult { }.start()
                    }
                }
            }
            .create()

        binding?.rcPhotoWallList?.itemAnimator?.changeDuration = 0L
        binding?.rcPhotoWallList?.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        binding?.rcPhotoWallList?.adapter = mPicAdapter

        mPicList.clear()
        mPicList.addAll(userInfo.photos)
        mPicList.add("")
        mPicAdapter.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadVideoList(userInfo: UserInfo) {
        val width = (AppUtil.getScreenWidth(requireContext()) / 3.5).toInt()
        mVideoAdapter = DataAdapter.Builder<String>()
            .setData(mVideoList)
            .setLayoutId(R.layout.item_video)
            .addBindView { itemView, itemData, position ->
                //??????item?????????
                val lp = itemView.layoutParams
                lp.width = width
                lp.height = width * 118 / 105
                itemView.layoutParams = lp

                val itemBinding = ItemVideoBinding.bind(itemView)
                if (itemData.isBlank()) {
                    itemBinding.ivPlay.setImageResource(R.drawable.grzx_sc)
                    itemBinding.ivPic.visibility = View.GONE
                    itemBinding.ivMask.visibility = View.GONE
                } else {
                    itemBinding.ivPlay.setImageResource(R.drawable.gezx_bf)
                    itemBinding.ivPic.visibility = View.VISIBLE
                    itemBinding.ivMask.visibility = View.VISIBLE

                    Glide.with(requireActivity())
                        .setDefaultRequestOptions(RequestOptions().frame(0).centerCrop())
                        .load(itemData)
                        .placeholder(R.drawable.placeholder)
                        .into(itemBinding.ivPic)
                }

                itemView.setOnClickListener {
                    if (itemData.isBlank()) {
                        getVideoFromAlbum()
                    } else {
                        val intent = Intent(requireActivity(), VideoPreviewActivity::class.java)
                        intent.putExtra("video_list", mVideoList.filter { it != "" } as java.util.ArrayList<String>?)
                        intent.putExtra("position", position)
                        startActivity(intent)
                    }
                }
            }
            .create()

        binding?.rcShortVideoList?.itemAnimator?.changeDuration = 0L
        binding?.rcShortVideoList?.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        binding?.rcShortVideoList?.adapter = mVideoAdapter

        mVideoList.clear()
        mVideoList.addAll(userInfo.videos)
        mVideoList.add("")
        mAdapter.notifyDataSetChanged()
    }

    private fun showUserDetail() {
        if (!DoubleUtils.isFastDoubleClick()) {
            getUserInfo { userInfo ->
                showUserDetail(userInfo.uid, true, showChat = false)
            }
        }
    }

    private fun toStorePage() {
        startActivity(Intent(requireActivity(), StoreActivity::class.java))
    }

    private fun toFollowPage() {
        val intent = Intent(requireActivity(), FollowActivity::class.java)
        startActivity(intent)
    }

    private fun toProfilePage() {
        checkReadAndWritePermissions {
            startActivity(Intent(requireActivity(), ProfileActivity::class.java))
        }
    }

    private fun toSettingPage() {
        checkReadAndWritePermissions {
            startActivity(Intent(requireActivity(), SettingActivity::class.java))
        }
    }

    private fun toLevelPage() {
        startActivity(Intent(requireActivity(), LevelActivity::class.java))
    }

    private fun toScorePage() {
        startActivity(Intent(requireActivity(), ScoreActivity::class.java))
    }

    private fun toIncomePage() {
        startActivity(Intent(requireActivity(), IncomeActivity::class.java))
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getVideoFromAlbum() {
//        val intent = Intent()
//        intent.action = Intent.ACTION_PICK
//        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "video/*")
//        startActivityForResult(intent, 0x1001)

        Album.video(this)
            .multipleChoice()
            .camera(false)
            .columnCount(2)
            .selectCount(6)
            .filterSize(null)
            .filterMimeType(null)
            .afterFilterVisibility(true)
            .onResult { albumList ->
                for (item in albumList) {
                    waitingDialog.show()
                    //????????????
                    RtcManager.compressVideo(requireActivity(), item.path) { compressPath ->
                        //????????????
                        DataManager.uploadFileToOss(requireContext(), compressPath) { ossPath ->
                            if (ossPath.isNotBlank()) {
                                val l = arrayListOf<String>()
                                l.addAll(mVideoList.filter { it != "" })
                                l.add(ossPath)
                                DataManager.updateVideo(l) {
                                    waitingDialog.cancel()
                                    if (it) {
                                        mVideoList.clear()
                                        mVideoList.addAll(l)
                                        mVideoList.add("")
                                        mVideoAdapter.notifyDataSetChanged()
                                        binding?.rcShortVideoList?.scrollToPosition(mVideoList.size - 1)
                                    }
                                }
                            }
                        }
                    }
                }

            }.start()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getImageFromAlbum() {
        Album.image(this)
            .multipleChoice()
            .camera(false)
            .columnCount(2)
            .selectCount(6)
            .filterSize(null)
            .filterMimeType(null)
            .afterFilterVisibility(true)
            .onResult { albumList ->
                waitingDialog.show()

                val list = arrayListOf<String>()
                for (item in albumList) {
                    //????????????
                    CManager.compress(requireActivity(), item.path, object : FileCallback {
                        override fun onSuccess(filePath: String) {
                            list.add(filePath)
                        }

                        override fun onFailed(message: String) {
                        }
                    })
                }

                //????????????
                DataManager.uploadFileListToOss(requireContext(), list) { ossPathList ->
                    if (ossPathList.isNotEmpty()) {
                        val l = arrayListOf<String>()
                        l.addAll(mPicList.filter { it != "" })
                        l.addAll(ossPathList)
                        DataManager.updateImage(l) {
                            waitingDialog.cancel()
                            if (it) {
                                mPicList.clear()
                                mPicList.addAll(l)
                                mPicList.add("")
                                mPicAdapter.notifyDataSetChanged()
                                binding?.rcPhotoWallList?.scrollToPosition(mVideoList.size - 1)
                            }
                        }
                    }
                }
            }.start()
    }

    override fun refreshUserInfo() {
        getUserInfo()
    }

    private fun showPrimeDialog() {
        val userInfo = getLocalStorage().decodeParcelable("user_info", UserInfo::class.java) ?: return
        PrimeDialog(requireActivity(), userInfo.is_vip) {}
    }

    override fun click(v: View) {
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    @Deprecated("")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //????????????
        if (requestCode == 0x1001) {
            if (data != null) {
                val uri = data.data
                if (uri != null) {
                    val realPath = FileUtil.getRealPathFromUri(requireContext(), uri)
                    if (realPath != null) {
                        waitingDialog.show()

                        //????????????
                        RtcManager.compressVideo(requireActivity(), realPath) { compressPath ->
                            DataManager.uploadFileToOss(requireContext(), compressPath) { ossPath ->
                                waitingDialog.cancel()
                                if (ossPath.isNotBlank()) {
                                    val list = arrayListOf<String>()
                                    list.add(ossPath)

                                    getUserInfo { userInfo ->
                                        list.addAll(userInfo.videos)
                                    }

                                    DataManager.updateVideo(list) {
                                        if (it) {
                                            getUserInfo()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        //????????????
        if (requestCode == 0x1002) {
            if (data != null) {
                val uri = data.data
                if (uri != null) {
                    val realPath = FileUtil.getRealPathFromUri(requireContext(), uri)
                    if (realPath != null) {

                        //????????????
                        CManager.compress(requireActivity(), realPath, object : FileCallback {
                            override fun onSuccess(filePath: String) {
                                waitingDialog.show()

                                DataManager.uploadFileToOss(requireContext(), filePath) { ossPath ->
                                    waitingDialog.cancel()
                                    if (ossPath.isNotBlank()) {
                                        val list = arrayListOf<String>()
                                        list.add(ossPath)

                                        getUserInfo { userInfo ->
                                            list.addAll(userInfo.photos)
                                        }

                                        DataManager.updateImage(list) {
                                            if (it) {
                                                getUserInfo()
                                            }
                                        }
                                    }
                                }
                            }

                            override fun onFailed(message: String) {
                            }
                        })
                    }
                }
            }
        }
    }
}