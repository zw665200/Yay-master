package com.ql.recovery.yay.ui.mine

import android.annotation.SuppressLint
import android.content.Intent
import android.provider.MediaStore
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blongho.country_data.World
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.uinfo.UserService
import com.netease.nimlib.sdk.uinfo.constant.UserInfoFieldEnum
import com.netease.yunxin.kit.adapters.DataAdapter
import com.ql.recovery.bean.UserInfo
import com.ql.recovery.config.Config
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.callback.FileCallback
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.databinding.ActivityProfileBinding
import com.ql.recovery.yay.databinding.ItemVideoBinding
import com.ql.recovery.yay.databinding.ItemWallpaperBinding
import com.ql.recovery.yay.manager.CManager
import com.ql.recovery.yay.manager.RtcManager
import com.ql.recovery.yay.ui.base.BaseActivity
import com.ql.recovery.yay.ui.dialog.*
import com.ql.recovery.yay.ui.self.ItemTouchHelperCallback
import com.ql.recovery.yay.ui.self.OnRecyclerItemClickListener
import com.ql.recovery.yay.util.AppUtil
import com.ql.recovery.yay.util.FileUtil
import com.ql.recovery.yay.util.ToastUtil
import java.util.*

class ProfileActivity : BaseActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var waitingDialog: WaitingDialog
    private lateinit var picAdapter: DataAdapter<String>
    private lateinit var videoAdapter: DataAdapter<String>
    private var mUserInfo: UserInfo? = null

    override fun getViewBinding(baseBinding: ActivityBaseBinding) {
        binding = ActivityProfileBinding.inflate(layoutInflater, baseBinding.flBase, true)
    }

    override fun initView() {
        binding.includeTitle.ivBack.setOnClickListener { finish() }
        binding.llAvatar.setOnClickListener { getImageFromAlbum(0x1004) }
        binding.llNickname.setOnClickListener { modifyNickname() }
        binding.llSex.setOnClickListener { modifySex() }
        binding.llBirthday.setOnClickListener { modifyBirthday() }
        binding.llCountry.setOnClickListener { modifyCountry() }
        binding.flTagsView.setOnClickListener { modifyTags() }
    }

    override fun initData() {
        binding.includeTitle.tvName.text = getString(R.string.profile_title)
        waitingDialog = WaitingDialog(this)
        getUserInfo()
    }

    private fun getUserInfo() {
        DataManager.getUserInfo { userInfo ->
            mUserInfo = userInfo
            Glide.with(this).load(userInfo.avatar).apply(RequestOptions.bitmapTransform(CircleCrop())).into(binding.ivAvatar)

            binding.tvNickname.text = userInfo.nickname
            binding.tvAge.text = userInfo.age.toString()

            when (userInfo.sex) {
                1 -> binding.tvSex.text = getString(R.string.home_male)
                2 -> binding.tvSex.text = getString(R.string.home_female)
            }

            if (userInfo.country.isNotBlank()) {
                val flag = World.getFlagOf(userInfo.country)
                binding.ivCountry.setImageResource(flag)
            }

            if (userInfo.tags.isNotEmpty()) {
                binding.llTags.removeAllViews()
                for (item in userInfo.tags) {
                    if (binding.llTags.childCount > 3) break
                    val p3 = AppUtil.dp2px(this, 3f)
                    val p5 = AppUtil.dp2px(this, 5f)
                    val p10 = AppUtil.dp2px(this, 10f)
                    val view = TextView(this)
                    view.setPadding(p10, p3, p10, p3)
                    val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    lp.setMargins(0, 0, p5, 0)
                    view.layoutParams = lp
                    view.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_yellow, null)
                    view.text = item.name
                    view.textSize = 12f
                    view.setTextColor(ResourcesCompat.getColor(resources, R.color.color_black, null))
                    binding.llTags.addView(view)
                }
            }

            loadPicList(userInfo)
            loadVideoList(userInfo)

            if (Config.mainHandler != null) {
                Config.mainHandler!!.sendEmptyMessage(0x10006)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadPicList(userInfo: UserInfo) {
        val list = arrayListOf<String>()
        list.addAll(userInfo.photos)
        list.add("")

        val width = AppUtil.getScreenWidth(this)
        picAdapter = DataAdapter.Builder<String>()
            .setData(list)
            .setLayoutId(R.layout.item_wallpaper)
            .addBindView { itemView, itemData, position ->
                //限制item的宽高
                val lp = itemView.layoutParams
                lp.width = width / 4
                lp.height = width / 4
                itemView.layoutParams = lp

                val itemBinding = ItemWallpaperBinding.bind(itemView)
                if (itemData.isBlank()) {
                    itemBinding.ivDelete.visibility = View.GONE
                    itemBinding.ivPic.visibility = View.GONE
                    itemBinding.ivUpload.visibility = View.VISIBLE
                    itemBinding.ivUpload.setImageResource(R.drawable.grzx_sc)
                } else {
                    itemBinding.ivPic.visibility = View.VISIBLE
                    itemBinding.ivDelete.visibility = View.VISIBLE
                    itemBinding.ivUpload.visibility = View.GONE
                    Glide.with(this).load(itemData).into(itemBinding.ivPic)
                }

                itemBinding.ivDelete.setOnClickListener {
                    val tempList = arrayListOf<String>()
                    tempList.addAll(list)
                    tempList.remove(itemData)
                    tempList.removeLast()

                    DataManager.updateImage(tempList) {
                        if (it) {
                            getUserInfo()
                        }
                    }
                }

                itemView.setOnClickListener {
                    if (itemData.isBlank()) {
                        getImageFromAlbum(0x1002)
                    } else {
                        val intent = Intent(this, ImagePreviewActivity::class.java)
                        intent.putExtra("pic_list", userInfo.photos as ArrayList<String>)
                        intent.putExtra("position", position)
                        startActivity(intent)
                    }
                }
            }
            .create()

        binding.rcPhotoWallList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcPhotoWallList.adapter = picAdapter
        picAdapter.notifyDataSetChanged()
        binding.rcPhotoWallList.scrollToPosition(list.size - 1)

        val itemTouchHelper = ItemTouchHelper(ItemTouchHelperCallback(picAdapter, list) {
            val tempList = arrayListOf<String>()
            tempList.addAll(list)
            tempList.removeLast()

            DataManager.updateImage(tempList) {
                if (it) {
                    if (Config.mainHandler != null) {
                        Config.mainHandler!!.sendEmptyMessage(0x10006)
                    }
                }
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.rcPhotoWallList)

        binding.rcPhotoWallList.addOnItemTouchListener(object : OnRecyclerItemClickListener(binding.rcPhotoWallList) {
            override fun onItemLongClick(vh: RecyclerView.ViewHolder) {
                //假设item不是最后一个，则执行拖拽
                if (vh.layoutPosition != list.size - 1) {
                    itemTouchHelper.startDrag(vh)
                }
            }

            override fun onItemClick(vh: RecyclerView.ViewHolder?) {
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadVideoList(userInfo: UserInfo) {
        val list = arrayListOf<String>()
        list.addAll(userInfo.videos)
        list.add("")

        val width = AppUtil.getScreenWidth(this)
        videoAdapter = DataAdapter.Builder<String>()
            .setData(list)
            .setLayoutId(R.layout.item_video)
            .addBindView { itemView, itemData, position ->
                //限制item的宽高
                val lp = itemView.layoutParams
                lp.width = width / 4
                lp.height = width / 4
                itemView.layoutParams = lp

                val itemBinding = ItemVideoBinding.bind(itemView)
                if (itemData.isBlank()) {
                    itemBinding.ivPlay.setImageResource(R.drawable.grzx_sc)
                    itemBinding.ivPic.visibility = View.GONE
                } else {
                    itemBinding.ivDelete.visibility = View.VISIBLE

                    Glide.with(this)
                        .setDefaultRequestOptions(RequestOptions().frame(0).centerCrop())
                        .load(itemData)
                        .into(itemBinding.ivPic)
                }

                if (itemData.isBlank()) {
                    itemBinding.ivDelete.visibility = View.GONE
                    itemBinding.ivPic.visibility = View.GONE
                    itemBinding.ivPlay.visibility = View.VISIBLE
                    itemBinding.ivPlay.setImageResource(R.drawable.grzx_sc)
                } else {
                    itemBinding.ivPic.visibility = View.VISIBLE
                    itemBinding.ivDelete.visibility = View.VISIBLE
                    itemBinding.ivPlay.visibility = View.GONE

                    Glide.with(this)
                        .setDefaultRequestOptions(RequestOptions().frame(0).centerCrop())
                        .load(itemData)
                        .into(itemBinding.ivPic)
                }

                itemBinding.ivDelete.setOnClickListener {
                    val tempList = arrayListOf<String>()
                    tempList.addAll(list)
                    tempList.remove(itemData)
                    tempList.removeLast()

                    DataManager.updateVideo(tempList) {
                        if (it) {
                            getUserInfo()
                        }
                    }
                }

                itemView.setOnClickListener {
                    if (itemData.isBlank()) {
                        getVideoFromAlbum()
                    } else {
                        val intent = Intent(this, VideoPreviewActivity::class.java)
                        intent.putExtra("video_list", userInfo.videos as ArrayList<String>)
                        intent.putExtra("position", position)
                        startActivity(intent)
                    }
                }
            }
            .create()

        binding.rcShortVideoList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcShortVideoList.adapter = videoAdapter
        videoAdapter.notifyDataSetChanged()
        binding.rcShortVideoList.scrollToPosition(list.size - 1)

        val itemTouchHelper = ItemTouchHelper(ItemTouchHelperCallback(videoAdapter, list) {
            val tempList = arrayListOf<String>()
            tempList.addAll(list)
            tempList.removeLast()

            DataManager.updateVideo(tempList) {
                if (it) {
                    if (Config.mainHandler != null) {
                        Config.mainHandler!!.sendEmptyMessage(0x10006)
                    }
                }
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.rcShortVideoList)

        binding.rcShortVideoList.addOnItemTouchListener(object : OnRecyclerItemClickListener(binding.rcShortVideoList) {
            override fun onItemLongClick(vh: RecyclerView.ViewHolder) {
                //item是最后一个拒绝拖拽
                if (vh.layoutPosition != list.size - 1) {
                    itemTouchHelper.startDrag(vh)
                }
            }

            override fun onItemClick(vh: RecyclerView.ViewHolder?) {
            }
        })
    }

    private fun getVideoFromAlbum() {
        val intent = Intent()
        intent.action = Intent.ACTION_PICK
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "video/*")
        startActivityForResult(intent, 0x1001)
    }

    private fun getImageFromAlbum(requestCode: Int) {
        val intent = Intent()
        intent.action = Intent.ACTION_PICK
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        startActivityForResult(intent, requestCode)
    }

    private fun modifyNickname() {
        val useInfo = getLocalStorage().decodeParcelable("user_info", UserInfo::class.java)
        if (useInfo != null) {
            ModifyDialog(this, getString(R.string.profile_nickname), useInfo) { nickname ->
                binding.tvNickname.text = nickname

                //更新用户资料
                val fields = HashMap<UserInfoFieldEnum, Any>()
                fields[UserInfoFieldEnum.Name] = nickname
                NIMClient.getService(UserService::class.java).updateUserInfo(fields)
            }
        }
    }

    private fun modifyBirthday() {
        ModifyBirthdayDialog(this) {
            getUserInfo()
        }
    }

    private fun modifySex() {
        getUserInfo { userInfo ->
            if (userInfo.is_vip) {
                ModifySexDialog(this) { sex ->
                    when (sex) {
                        1 -> binding.tvSex.text = getString(R.string.home_male)
                        2 -> binding.tvSex.text = getString(R.string.home_female)
                    }
                }
            } else {
                showPrimeDialog()
            }
        }
    }

    private fun modifyCountry() {
        getUserInfo { userInfo ->
            if (userInfo.is_vip) {
                startActivityForResult(Intent(this, CountryActivity::class.java), 0x1003)
            } else {
                showPrimeDialog()
            }
        }
    }

    private fun showPrimeDialog() {
        PrimeDialog(this, false) {}
    }

    private fun modifyTags() {
        getUserInfo { userInfo ->
            ModifyTagDialog(this, userInfo.tags) {
                getUserInfo()
            }
        }
    }

    @Deprecated("")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x1001) {
            if (data != null) {
                val uri = data.data
                if (uri != null) {
                    val realPath = FileUtil.getRealPathFromUri(this, uri)
                    if (realPath != null) {
                        waitingDialog.show()
                        //压缩视频
                        RtcManager.compressVideo(this, realPath) { compressPath ->
                            DataManager.uploadFileToOss(this, compressPath) { ossPath ->
                                waitingDialog.cancel()
                                if (ossPath.isNotBlank() && ossPath.startsWith("http")) {
                                    val list = arrayListOf<String>()
                                    list.addAll(mUserInfo!!.videos)
                                    list.add(ossPath)
                                    DataManager.updateVideo(list) {
                                        if (it) {
                                            getUserInfo()
                                        }
                                    }
                                } else {
                                    ToastUtil.showShort(this, ossPath)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (requestCode == 0x1002) {
            if (data != null) {
                val uri = data.data
                if (uri != null) {
                    val realPath = FileUtil.getRealPathFromUri(this, uri)
                    if (realPath != null) {
                        //压缩图片
                        CManager.compress(this, realPath, object : FileCallback {
                            override fun onSuccess(filePath: String) {
                                waitingDialog.show()
                                DataManager.uploadFileToOss(this@ProfileActivity, realPath) { ossPath ->
                                    waitingDialog.cancel()
                                    if (ossPath.isNotBlank() && ossPath.startsWith("http")) {
                                        val list = arrayListOf<String>()
                                        list.addAll(mUserInfo!!.photos)
                                        list.add(ossPath)
                                        DataManager.updateImage(list) {
                                            if (it) {
                                                getUserInfo()
                                            }
                                        }
                                    } else {
                                        ToastUtil.showShort(this@ProfileActivity, ossPath)
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

        if (requestCode == 0x1003 && resultCode == 0x1003) {
            getUserInfo()
        }

        if (requestCode == 0x1004) {
            if (data != null) {
                val uri = data.data
                if (uri != null) {
                    val realPath = FileUtil.getRealPathFromUri(this, uri)
                    if (realPath != null) {
                        //压缩图片
                        CManager.compress(this, realPath, object : FileCallback {
                            override fun onSuccess(filePath: String) {
                                waitingDialog.show()
                                DataManager.uploadFileToOss(this@ProfileActivity, realPath) { ossPath ->
                                    waitingDialog.cancel()
                                    if (ossPath.isNotBlank() && ossPath.startsWith("http")) {
                                        DataManager.updateUserInfo(ossPath, null, null, null, null, null, null) {
                                            if (it) {
                                                getUserInfo()
                                            }
                                        }
                                    } else {
                                        ToastUtil.showShort(this@ProfileActivity, ossPath)
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