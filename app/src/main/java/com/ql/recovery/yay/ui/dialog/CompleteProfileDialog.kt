package com.ql.recovery.yay.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.netease.yunxin.kit.adapters.DataAdapter
import com.ql.recovery.bean.UserInfo
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.callback.FileCallback
import com.ql.recovery.yay.databinding.DialogCompleteProfileBinding
import com.ql.recovery.yay.databinding.ItemPhotoBinding
import com.ql.recovery.yay.manager.CManager
import com.ql.recovery.yay.util.AppUtil
import com.yanzhenjie.album.Album


class CompleteProfileDialog(
    private val activity: Activity,
    private val userInfo: UserInfo,
    private val func: () -> Unit
) : Dialog(activity, R.style.app_dialog2) {
    private lateinit var binding: DialogCompleteProfileBinding
    private lateinit var mAdapter: DataAdapter<String>
    private lateinit var waitingDialog: WaitingDialog
    private var mList = arrayListOf<String>()
    private var step = Step.One
    private var currentAvatar: String? = null

    init {
        initVew()
    }

    private fun initVew() {
        binding = DialogCompleteProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(true)

        waitingDialog = WaitingDialog(activity)

        binding.flAvatar.setOnClickListener { getAvatarFromAlbum() }
        binding.tvCommit.setOnClickListener { commit() }
        binding.tvGiveUp.setOnClickListener { func() }

        checkUserInfo()

        show()
    }

    private fun checkUserInfo() {
        //检查第一步条件是否满足
        if (userInfo.avatar.isNotBlank() && userInfo.nickname.isNotBlank()) {
            toStepTwo()
        }
    }

    private fun toStepTwo() {
        step = Step.Two
        binding.llStepOne.visibility = View.GONE
        binding.llStepTwo.visibility = View.VISIBLE
        binding.tvCommit.text = activity.getString(R.string.complete_finish)
        binding.ivStepPoint.setImageResource(R.drawable.co_point_second)
        chooseWallpaper()
    }

    private fun chooseWallpaper() {
        val width = AppUtil.getScreenWidth(activity)
        mAdapter = DataAdapter.Builder<String>()
            .setData(mList)
            .setLayoutId(R.layout.item_photo)
            .addBindView { itemView, itemData, position ->
                val itemBinding = ItemPhotoBinding.bind(itemView)

                val layout = itemView.layoutParams
                layout.width = width / 4
                layout.height = width / 4
                itemView.layoutParams = layout

                if (itemData.isBlank()) {
                    itemBinding.ivPic.visibility = View.GONE
                    itemBinding.ivUpload.visibility = View.VISIBLE
                } else {
                    itemBinding.ivPic.visibility = View.VISIBLE
                    itemBinding.ivUpload.visibility = View.GONE
                    Glide.with(activity).load(itemData).into(itemBinding.ivPic)
                }

                itemBinding.ivUpload.setOnClickListener {
                    getImageFromAlbum()
                }
            }
            .create()

        binding.rcImage.layoutManager = GridLayoutManager(activity, 3)
        binding.rcImage.adapter = mAdapter

        mList.clear()
        mList.add("")
        mAdapter.notifyItemInserted(0)
    }

    private fun commit() {
        when (step) {
            Step.One -> {
                val nickname = binding.etNickname.editableText.toString()
                if (currentAvatar != null && nickname.isNotBlank()) {
                    updateAvatarAndNickname(currentAvatar!!, nickname)
                }
            }

            Step.Two -> {
                if (mList.isEmpty()) return
                updatePhotoWall()
            }
        }
    }

    private fun getAvatarFromAlbum() {
        Album.image(activity)
            .multipleChoice()
            .camera(false)
            .columnCount(4)
            .selectCount(1)
            .filterSize(null)
            .filterMimeType(null)
            .afterFilterVisibility(true)
            .onResult { albumList ->

                if (albumList.isNotEmpty()) {
                    currentAvatar = albumList[0].path
                    Glide.with(activity).load(albumList[0].path)
                        .apply(RequestOptions.bitmapTransform(CircleCrop()))
                        .into(binding.ivAvatar)
                    binding.ivMask.visibility = View.GONE
                    binding.llSettingAvatar.visibility = View.GONE
                }

            }.start()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getImageFromAlbum() {
        Album.image(activity)
            .multipleChoice()
            .camera(false)
            .columnCount(2)
            .selectCount(6)
            .filterSize(null)
            .filterMimeType(null)
            .afterFilterVisibility(true)
            .onResult { albumList ->
                val realList = mList.filter { it != "" }
                mList.clear()
                mList.addAll(realList)
                for (child in albumList) {
                    mList.add(child.path)
                }
                mList.add("")
                mAdapter.notifyDataSetChanged()

            }.start()
    }

    private fun updateAvatarAndNickname(avatar: String, nickname: String) {
        waitingDialog.show()
        //压缩图片
        CManager.compress(activity, avatar, object : FileCallback {
            override fun onSuccess(filePath: String) {
                //上传至OSS
                DataManager.uploadFileToOss(activity, filePath) { ossPath ->
                    //更新资料
                    DataManager.updateAvatarAndNickname(ossPath, nickname) { isSuccess ->
                        waitingDialog.cancel()
                        if (isSuccess) {
                            toStepTwo()
                            DataManager.getUserInfo { }
                        }
                    }
                }
            }

            override fun onFailed(message: String) {
                waitingDialog.cancel()
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updatePhotoWall() {
        waitingDialog.show()

        val realList = mList.filter { it != "" }
        val list = arrayListOf<String>()
        for (item in realList) {
            //压缩图片
            CManager.compress(activity, item, object : FileCallback {
                override fun onSuccess(filePath: String) {
                    list.add(filePath)
                }

                override fun onFailed(message: String) {
                    waitingDialog.cancel()
                }
            })
        }

        //上传图片
        DataManager.uploadFileListToOss(activity, list) { ossPathList ->
            if (ossPathList.isNotEmpty()) {
                val l = arrayListOf<String>()
                l.addAll(ossPathList)
                DataManager.updateImage(l) {
                    waitingDialog.cancel()
                    if (it) {
                        cancel()
                        DataManager.getUserInfo { }

                        DataManager.checkFirstCompletionReward { isReceived ->
                            if (!isReceived) {
                                //领取奖励
                                DataManager.receiveFirstCompletionReward { complete ->
                                    if (complete) {
                                        ProfileFinishDialog(activity).show()
                                    }
                                }
                            } else {
                                cancel()
                            }
                        }
                    }
                }
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

    enum class Step { One, Two }

}