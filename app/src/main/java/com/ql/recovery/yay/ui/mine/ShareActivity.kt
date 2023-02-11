package com.ql.recovery.yay.ui.mine

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.share.Sharer
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.widget.MessageDialog
import com.facebook.share.widget.ShareDialog
import com.ql.recovery.bean.Inviter
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.adapter.DataAdapter
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.databinding.ActivityShareBinding
import com.ql.recovery.yay.databinding.ItemShareBinding
import com.ql.recovery.yay.ui.base.BaseActivity
import com.ql.recovery.yay.util.AppUtil
import com.ql.recovery.yay.util.JLog
import com.ql.recovery.yay.util.ToastUtil

class ShareActivity : BaseActivity() {
    private lateinit var binding: ActivityShareBinding
    private lateinit var adapter: DataAdapter<Inviter>
    private var mList = arrayListOf<Inviter>()

    override fun getViewBinding(baseBinding: ActivityBaseBinding) {
        binding = ActivityShareBinding.inflate(layoutInflater, baseBinding.flBase, true)
    }

    override fun initView() {
        binding.includeTitle.ivBack.setOnClickListener { finish() }
        binding.llShortMessage.setOnClickListener { shareShortMessage() }
        binding.llShareOther.setOnClickListener { shareOther() }
        binding.llShareMessenger.setOnClickListener { shareMessenger() }
        binding.llShareWhatsapp.setOnClickListener { shareWhatsApp() }
        binding.llShareCopyLink.setOnClickListener { copyLink() }
        binding.llShareFacebook.setOnClickListener { shareFaceBook() }
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        binding.includeTitle.tvName.text = getString(R.string.share_title)

        initInviterList()
        getInviterList()
    }

    private fun initInviterList() {
        adapter = DataAdapter.Builder<Inviter>()
            .setData(mList)
            .setLayoutId(R.layout.item_share)
            .addBindView { itemView, itemData ->
                val itemBinding = ItemShareBinding.bind(itemView)
                Glide.with(this).load(itemData.avatar).apply(RequestOptions.bitmapTransform(CircleCrop())).into(itemBinding.ivAvatar)
                itemBinding.tvName.text = itemData.nickname
                itemBinding.tvCoin.text = itemData.coin.toString()
            }
            .create()

        binding.rcInviter.adapter = adapter
        binding.rcInviter.layoutManager = LinearLayoutManager(this)
    }

    private fun getInviterList() {
        DataManager.getInviter {
            mList.clear()
            mList.addAll(it)
            adapter.notifyItemRangeChanged(0, mList.size)
        }
    }

    private fun shareShortMessage() {
        DataManager.postInviter {
            val smsUri = Uri.parse("smsto:")
            val intent = Intent(Intent.ACTION_SENDTO, smsUri)
            intent.putExtra("sms_body", it)
            startActivity(intent)
        }
    }

    private fun shareMessenger() {
        DataManager.postInviter {
            val content = ShareLinkContent.Builder()
                .setContentUrl(Uri.parse(it))
                .build()

            //创建分享容器
            val messageDialog = MessageDialog(this)

            //注册分享监听
            messageDialog.registerCallback(CallbackManager.Factory.create(), object : FacebookCallback<Sharer.Result> {
                override fun onSuccess(result: Sharer.Result?) {
                    JLog.i("share success")
                }

                override fun onCancel() {
                    JLog.i("share cancel")
                }

                override fun onError(error: FacebookException?) {
                    JLog.i("share error")
                }
            })

            //调用分享
//            if (MessageDialog.canShow(ShareOpenGraphContent::class.java)) {
            JLog.i("can share")
            messageDialog.show(content)
//            }
        }
    }

    private fun shareFaceBook() {
        DataManager.postInviter {
            val content = ShareLinkContent.Builder()
                .setContentUrl(Uri.parse(it))
                .build()

            //创建分享容器
            val shareDialog = ShareDialog(this)

            //注册分享监听
            shareDialog.registerCallback(CallbackManager.Factory.create(), object : FacebookCallback<Sharer.Result> {
                override fun onSuccess(result: Sharer.Result?) {
                    JLog.i("share success")
                }

                override fun onCancel() {
                    JLog.i("share cancel")
                }

                override fun onError(error: FacebookException?) {
                    JLog.i("share error")
                }
            })

            //调用分享
            shareDialog.show(content)
        }
    }

    private fun shareWhatsApp() {
        DataManager.postInviter {
            val intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(Intent.EXTRA_TEXT, it)
            intent.setType("text/plain")
            intent.setPackage("com.whatsapp")
            startActivity(intent)
        }
    }

    private fun shareOther() {
        val title = "Share your APP"
        DataManager.postInviter {
            AppUtil.shareText(this, it, title)
        }
    }

    private fun copyLink() {
        DataManager.postInviter {
            AppUtil.copyToClipboard(this, it)
            ToastUtil.showShort(this, getString(R.string.share_copy_success))
        }
    }

}