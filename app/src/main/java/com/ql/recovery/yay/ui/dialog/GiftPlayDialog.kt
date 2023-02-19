package com.ql.recovery.yay.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.opensource.svgaplayer.*
import com.opensource.svgaplayer.utils.log.SVGALogger
import com.ql.recovery.bean.Gift
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.DialogGiftPlayBinding
import com.ql.recovery.yay.util.AppUtil
import com.ql.recovery.yay.util.JLog
import java.io.File
import java.net.URL

class GiftPlayDialog(
    private val activity: Activity,
    private val gift: Gift
) : Dialog(activity, R.style.app_dialog2) {
    private lateinit var binding: DialogGiftPlayBinding

    init {
        initVew()
    }

    private fun initVew() {
        binding = DialogGiftPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(true)

        val width = AppUtil.getScreenWidth(activity)
        val lp = binding.ivAnimation.layoutParams
        lp.width = width
        lp.height = width
        binding.ivAnimation.layoutParams = lp

        val lp2 = binding.ivSvgaAnimation.layoutParams
        lp2.width = width
        lp2.height = width
        binding.ivSvgaAnimation.layoutParams = lp2

        if (gift.animation_url.contains(".gif")) {
            Glide.with(activity).asGif()
                .load(gift.animation_url)
                .addListener(object : RequestListener<GifDrawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?, target: Target<GifDrawable>?, isFirstResource: Boolean
                    ): Boolean {
                        e?.printStackTrace()
                        return true
                    }

                    override fun onResourceReady(
                        resource: GifDrawable,
                        model: Any?,
                        target: Target<GifDrawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        JLog.i("onResourceReady")
                        startGoldGif(resource)
                        return false
                    }
                })
                .into(binding.ivAnimation)
        } else {
            binding.ivSvgaAnimation.loops = 1
            binding.ivSvgaAnimation.callback = object : SVGACallback {
                override fun onFinished() {
//                    JLog.i("onFinished")
                    cancel()
                }

                override fun onPause() {
//                    JLog.i("onPause")
                }

                override fun onRepeat() {
//                    JLog.i("onRepeat")
                }

                override fun onStep(frame: Int, percentage: Double) {
//                    JLog.i("onStep")
                }
            }

            SVGAParser.shareParser().init(activity)
            val parser = SVGAParser(activity)
            parser.decodeFromURL(URL(gift.animation_url),
                object : SVGAParser.ParseCompletion {
                    override fun onComplete(videoItem: SVGAVideoEntity) {
                        val dynamicEntity = SVGADynamicEntity()
                        val drawable = SVGADrawable(videoItem, dynamicEntity)
                        binding.ivSvgaAnimation.setImageDrawable(drawable)
                        binding.ivSvgaAnimation.stepToFrame(0, true)
                    }

                    override fun onError() {
                    }
                }, object : SVGAParser.PlayCallback {
                    override fun onPlay(file: List<File>) {
                    }
                })

            SVGALogger.setLogEnabled(false)
        }

        show()
    }

    private fun startGoldGif(gifDrawable: GifDrawable?) {
        gifDrawable?.setLoopCount(1)
        gifDrawable?.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
            override fun onAnimationStart(drawable: Drawable?) {
                JLog.i("onAnimationStart")
                super.onAnimationStart(drawable)
            }

            override fun onAnimationEnd(drawable: Drawable?) {
                JLog.i("onAnimationEnd")
                super.onAnimationEnd(drawable)
                gifDrawable.unregisterAnimationCallback(this)
                cancel()
            }
        })

        gifDrawable?.start()
    }

    override fun show() {
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.CENTER
            width = AppUtil.getScreenWidth(context)
            height = WindowManager.LayoutParams.WRAP_CONTENT
            flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
            dimAmount = 0.1f
        }
        super.show()
    }

    override fun cancel() {
        if (activity.isFinishing || activity.isDestroyed) return
        super.cancel()
    }

}