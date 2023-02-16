package com.ql.recovery.yay.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.WindowManager
import android.widget.SeekBar
import androidx.recyclerview.widget.GridLayoutManager
import com.netease.yunxin.kit.adapters.DataAdapter
import com.ql.recovery.bean.BeautyParam
import com.ql.recovery.bean.BeautyResource
import com.ql.recovery.bean.UserInfo
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.DialogBeautyBinding
import com.ql.recovery.yay.databinding.ItemBeautyBinding
import com.ql.recovery.yay.util.AppUtil
import com.tencent.mmkv.MMKV
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.BeautyOptions
import io.agora.rtc2.video.VideoCanvas


class BeautyDialog(
    private val activity: Activity,
    private val mRtcEngine: RtcEngine?,
    private val userInfo: UserInfo,
    private val func: () -> Unit
) : Dialog(activity, R.style.app_dialog) {
    private lateinit var binding: DialogBeautyBinding
    private lateinit var adapter: DataAdapter<BeautyResource>
    private var currentType = "lightening"
    private var mk = MMKV.defaultMMKV()

    init {
        initVew()
    }

    private fun initVew() {
        binding = DialogBeautyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(true)

//        window?.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND or WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        //将SurfaceView对象传入Agora，以渲染本地视频
        mRtcEngine?.setupLocalVideo(VideoCanvas(binding.surfaceLocal, VideoCanvas.RENDER_MODE_HIDDEN, userInfo.uid))

        val options = BeautyOptions()
        var beautyParam = mk.decodeParcelable("beauty_param", BeautyParam::class.java)
        if (beautyParam == null) {
            beautyParam = BeautyParam(options.lighteningLevel, options.smoothnessLevel, options.rednessLevel, options.sharpnessLevel)
            mk.encode("beauty_param", beautyParam)
        } else {
            //历史设置填充
            options.lighteningLevel = beautyParam.lighteningLevel
            options.smoothnessLevel = beautyParam.smoothnessLevel
            options.rednessLevel = beautyParam.rednessLevel
            options.sharpnessLevel = beautyParam.sharpnessLevel
        }

        //开启美颜
        mRtcEngine?.setBeautyEffectOptions(true, options)

        //开启本地视频预览
        mRtcEngine?.startPreview()

        binding.ivCancel.setOnClickListener { cancel() }

        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress.div(100f)
                when (currentType) {
                    "lightening" -> {
                        options.lighteningLevel = value
                        binding.tvSeekbarValue.text = progress.toString()
                    }
                    "smoothness" -> {
                        options.smoothnessLevel = value
                        binding.tvSeekbarValue.text = progress.toString()
                    }
                    "redness" -> {
                        options.rednessLevel = value
                        binding.tvSeekbarValue.text = progress.toString()
                    }
                    "sharpness" -> {
                        options.sharpnessLevel = value
                        binding.tvSeekbarValue.text = progress.toString()
                    }
                }
                mRtcEngine?.setBeautyEffectOptions(true, options)
                mk.encode("beauty_param", BeautyParam(options.lighteningLevel, options.smoothnessLevel, options.rednessLevel, options.sharpnessLevel))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        initRegionList(options)
        show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initRegionList(options: BeautyOptions) {
        val list = mutableListOf<BeautyResource>()
        list.add(BeautyResource("lightening", R.drawable.lj_mb_c, R.drawable.lj_mb_n, activity.getString(R.string.beauty_lightening)))
        list.add(BeautyResource("smoothness", R.drawable.lj_mp_c, R.drawable.lj_mp_n, activity.getString(R.string.beauty_smoothness)))
        list.add(BeautyResource("redness", R.drawable.lj_hr_c, R.drawable.lj_hr_n, activity.getString(R.string.beauty_redness)))
        list.add(BeautyResource("sharpness", R.drawable.lj_rh_c, R.drawable.lj_rh_n, activity.getString(R.string.beauty_sharpness)))

        adapter = DataAdapter.Builder<BeautyResource>()
            .setData(list)
            .setLayoutId(R.layout.item_beauty)
            .addBindView { itemView, itemData ->
                val itemBinding = ItemBeautyBinding.bind(itemView)
                itemBinding.title.text = itemData.name

                if (currentType == itemData.type) {
                    itemBinding.src.setImageResource(itemData.after)
                } else {
                    itemBinding.src.setImageResource(itemData.before)
                }

                when (currentType) {
                    "lightening" -> {
                        val value = (options.lighteningLevel * 100).toInt()
                        binding.seekbar.progress = value
                        binding.tvSeekbarValue.text = value.toString()
                    }
                    "smoothness" -> {
                        val value = (options.smoothnessLevel * 100).toInt()
                        binding.seekbar.progress = value
                        binding.tvSeekbarValue.text = value.toString()
                    }
                    "redness" -> {
                        val value = (options.rednessLevel * 100).toInt()
                        binding.seekbar.progress = (options.rednessLevel * 100).toInt()
                        binding.tvSeekbarValue.text = value.toString()
                    }
                    "sharpness" -> {
                        val value = (options.sharpnessLevel * 100).toInt()
                        binding.seekbar.progress = (options.sharpnessLevel * 100).toInt()
                        binding.tvSeekbarValue.text = value.toString()
                    }
                }

                itemView.setOnClickListener {
                    currentType = itemData.type
                    adapter.notifyDataSetChanged()
                }
            }
            .create()

        binding.rcBeauty.adapter = adapter
        binding.rcBeauty.layoutManager = GridLayoutManager(activity, 4)
        adapter.notifyDataSetChanged()
    }

    override fun show() {
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.CENTER
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
        }

        super.show()
    }

    override fun cancel() {
        super.cancel()
        func()
    }

}