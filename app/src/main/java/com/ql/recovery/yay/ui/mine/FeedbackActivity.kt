package com.ql.recovery.yay.ui.mine

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.ql.recovery.bean.Feedback
import com.ql.recovery.callback.Upload2Callback
import com.ql.recovery.manager.DataManager
import com.ql.recovery.manager.OSSManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.adapter.DataAdapter
import com.ql.recovery.yay.callback.FileCallback
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.databinding.ActivityFeedbackBinding
import com.ql.recovery.yay.databinding.ItemPicFeedbackBinding
import com.ql.recovery.yay.manager.CManager
import com.ql.recovery.yay.ui.base.BaseActivity
import com.ql.recovery.yay.ui.dialog.WaitingDialog
import com.ql.recovery.yay.util.AppUtil
import com.ql.recovery.yay.util.FileUtil
import com.ql.recovery.yay.util.JLog
import com.ql.recovery.yay.util.ToastUtil
import com.tencent.mmkv.MMKV

class FeedbackActivity : BaseActivity() {
    private lateinit var binding: ActivityFeedbackBinding
    private lateinit var mAdapter: DataAdapter<Bitmap>
    private var mList = arrayListOf<Bitmap>()
    private var uploadList = arrayListOf<String>()
    private var lastClickTime: Long = 0L
    private var waitingDialog: WaitingDialog? = null

    override fun getViewBinding(baseBinding: ActivityBaseBinding) {
        binding = ActivityFeedbackBinding.inflate(layoutInflater, baseBinding.flBase, true)
    }

    override fun initView() {
        binding.includeTitle.ivBack.setOnClickListener { finish() }
        binding.submit.setOnClickListener { submit() }

        initContent()
        initRecyclerView()
    }

    override fun initData() {
        waitingDialog = WaitingDialog(this)
        binding.includeTitle.tvName.text = getString(R.string.feedback)
    }

    private fun initContent() {
        binding.content.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            @SuppressLint("SetTextI18n")
            override fun afterTextChanged(s: Editable?) {
                val length = s?.length
                if (length != null) {
                    binding.count.text = "$length/500"

                    if (length == 0) {
                        binding.submit.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_grey, null)
                    } else {
                        binding.submit.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_yellow_10, null)
                    }

                } else {
                    binding.count.text = "0/500"
                }
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initRecyclerView() {
        mList.clear()
        uploadList.clear()
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.my_tj)
        mList.add(bitmap)
        uploadList.add("")

        val width = AppUtil.getScreenWidth(this)
        mAdapter = DataAdapter.Builder<Bitmap>()
            .setData(mList)
            .setLayoutId(R.layout.item_pic_feedback)
            .addBindView { itemView, itemData, position ->
                val itemBinding = ItemPicFeedbackBinding.bind(itemView)
                val layout = itemView.layoutParams
                layout.width = width / 5
                layout.height = width / 5
                itemView.layoutParams = layout

                if (position == mList.size - 1) {
                    itemBinding.ivDelete.visibility = View.GONE
                } else {
                    itemBinding.ivDelete.visibility = View.VISIBLE
                }

                if (position == 6) {
                    itemView.visibility = View.GONE
                } else {
                    itemView.visibility = View.VISIBLE
                }

                Glide.with(this).load(itemData).into(itemBinding.rvPic)

                itemView.setOnClickListener {
                    if (position == mList.size - 1) {
                        chooseAlbum()
                    }
                }

                itemBinding.ivDelete.setOnClickListener {
                    val list = arrayListOf<Bitmap>()
                    list.addAll(mList)
                    list.remove(itemData)
                    mList.clear()
                    mList.addAll(list)
                    mAdapter.notifyDataSetChanged()

                    uploadList.remove(uploadList[position])
                }
            }
            .create()

        binding.picsRecyclerview.layoutManager = GridLayoutManager(this, 4)
        binding.picsRecyclerview.adapter = mAdapter
        mAdapter.notifyDataSetChanged()
    }

    private fun submit() {
        val text = binding.content.text.trim()
        if (text.isBlank() || text.length > 500) {
            ToastUtil.show(this, getString(R.string.feedback_check))
            return
        }

        if (lastClickTime == 0L) {
            lastClickTime = System.currentTimeMillis()
        } else if (System.currentTimeMillis() - lastClickTime < 2 * 1000) {
            return
        }

        lastClickTime = System.currentTimeMillis()

        binding.submit.isEnabled = false
        binding.submit.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_grey, null)

        //图片不为空，先上传图片
        if (uploadList.isNotEmpty()) {
            uploadPics(text.toString())
        } else {
            commit(text.toString(), null)
            return
        }
    }

    private fun chooseAlbum() {
        val intent = Intent()
        intent.action = Intent.ACTION_PICK
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        startActivityForResult(intent, 0x1001)
    }

    @SuppressLint("CheckResult")
    private fun uploadPics(description: String) {
        uploadList.removeAt(0)

        JLog.i("uploadList.size = ${uploadList.size}")

        waitingDialog?.show()

        DataManager.getOSSToken { ossParam ->
            OSSManager.get().uploadFileToFeedback(this@FeedbackActivity, ossParam, uploadList, object : Upload2Callback {

                override fun onSuccess(pathList: List<String>) {
                    runOnUiThread {
                        waitingDialog?.cancel()
                        commit(description, pathList)
                    }
                }

                override fun onFailed(msg: String) {
                    runOnUiThread {
                        waitingDialog?.cancel()
                        resetStatus()
                        ToastUtil.showShort(this@FeedbackActivity, getString(R.string.feedback_commit_failed))
                    }
                }
            })
        }
    }

    private fun commit(description: String, pics: List<String>?) {
        val feedback = Feedback(description, pics, "Android ${Build.VERSION.RELEASE}")
        DataManager.feedback(feedback) {
            if (it) {
                resetStatus()
                ToastUtil.showLong(this@FeedbackActivity, getString(R.string.feedback_commit_success))

                MMKV.defaultMMKV()?.encode("feedback", AppUtil.getTodayDate())
                finish()
            } else {
                resetStatus()
            }
        }
    }

    private fun resetStatus() {
        binding.submit.isEnabled = true
        binding.submit.text = getString(R.string.feedback_submit)
        binding.submit.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_yellow_10, null)
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("NotifyDataSetChanged")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x1001) {
            if (data != null) {
                val uri = data.data
                if (uri != null) {
                    val realPath = FileUtil.getRealPathFromUri(this, uri)
                    if (realPath != null) {
                        CManager.compress(this, realPath, object : FileCallback {
                            override fun onSuccess(filePath: String) {
                                val bitmap = BitmapFactory.decodeFile(filePath)
                                mList.add(mList.size - 1, bitmap)
                                uploadList.add(uploadList.size, filePath)
                                mAdapter.notifyDataSetChanged()
                            }

                            override fun onFailed(message: String) {
                            }
                        })
                    }
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()

        mList.clear()
        uploadList.clear()

    }

}