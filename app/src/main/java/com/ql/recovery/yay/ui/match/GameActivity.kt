package com.ql.recovery.yay.ui.match

import android.graphics.drawable.BitmapDrawable
import android.view.View
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.gson.reflect.TypeToken
import com.ql.recovery.bean.Story
import com.ql.recovery.config.Config
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.databinding.ActivityGameBinding
import com.ql.recovery.yay.manager.ImageManager
import com.ql.recovery.yay.ui.base.BaseActivity
import com.ql.recovery.yay.ui.dialog.QuitDialog
import com.ql.recovery.yay.util.AppUtil
import com.ql.recovery.yay.util.GsonUtils

class GameActivity : BaseActivity() {
    private lateinit var binding: ActivityGameBinding
    private var storyList = arrayListOf<Story>()
    private var dialogIdList = arrayListOf<Int>()
    private var currentStory: Story? = null
    private var currentSelect = -1
    private lateinit var exoPlayer: ExoPlayer

    override fun getViewBinding(baseBinding: ActivityBaseBinding) {
        binding = ActivityGameBinding.inflate(layoutInflater, baseBinding.flBase, true)
    }

    override fun initView() {
        binding.includeTitle.ivBack.setOnClickListener { onBackPressed() }
        binding.flNo.setOnClickListener { checkStep(Type.No) }
        binding.flYes.setOnClickListener { checkStep(Type.Yes) }
        binding.flNext.setOnClickListener { checkStep(Type.Continue) }
        binding.flFindMatcher.setOnClickListener { gameOver(true) }
        binding.flGameOver.setOnClickListener { gameOver(false) }
        binding.ivImageFirst.setOnClickListener { chooseFirst() }
        binding.ivImageSecond.setOnClickListener { chooseSecond() }
    }

    override fun initData() {
        exoPlayer = ExoPlayer.Builder(this).build()

        val id = intent.getIntExtra("id", -1)
        if (id != -1) {
            getGameDetail(id)
        }

        val config = getMatchConfig()
        when (config.target_sex) {
            1 -> binding.includeTitle.ivOption.setImageResource(R.drawable.game_male)
            2 -> binding.includeTitle.ivOption.setImageResource(R.drawable.game_female)
        }
    }

    private fun getGameDetail(mapId: Int) {
        val config = getMatchConfig()
        DataManager.getMapDetail(mapId, config.target_sex) { mapInfo ->
            val data = mapInfo.data
            val typeToken = object : TypeToken<List<Story>>() {}
            val info = GsonUtils.fromJson<List<Story>>(data, typeToken.type) ?: return@getMapDetail
            storyList.addAll(info)

            if (storyList.isNotEmpty()) {
                currentStory = storyList[0]
                loadStory(storyList[0])
            }
        }
    }

    private fun loadStory(story: Story) {
        //??????id
        dialogIdList.add(story.id)

        //????????????
        if (story.sound.isNotBlank()) {
            val mediaItem = MediaItem.fromUri(story.sound)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }

        //??????????????????
        ImageManager.getBitmap(this, story.background) {
            binding.root.background = BitmapDrawable(resources, it)
        }

        //????????????
        if (!story.picture.isNullOrBlank()) {
            Glide.with(this).load(story.picture).into(binding.ivIcon)
        }

        //????????????
        if (AppUtil.isChinese(this)) {
            binding.tvContent.text = story.dialog.zh
        } else {
            binding.tvContent.text = story.dialog.en
        }

        if (story.options.isNotEmpty()) {
            if (story.options[0].picture != null) {
                binding.llChoose.visibility = View.VISIBLE
                binding.ivIcon.visibility = View.GONE

                binding.ivChooseFirst.setImageResource(R.drawable.game_unselect)
                binding.ivChooseSecond.setImageResource(R.drawable.game_unselect)

                //??????????????????
                if (story.options.size == 2) {
                    binding.ivImageFirst.visibility = View.VISIBLE
                    binding.ivImageSecond.visibility = View.VISIBLE
                    Glide.with(this).load(story.options[0].picture).into(binding.ivImageFirst)
                    Glide.with(this).load(story.options[1].picture).into(binding.ivImageSecond)
                }

                //???????????????
                binding.flNext.visibility = View.VISIBLE
                binding.flDoubleNext.visibility = View.GONE

            } else {
                binding.llChoose.visibility = View.GONE
                binding.ivIcon.visibility = View.VISIBLE

                //???????????????
                if (story.options.size == 2) {
                    binding.flNext.visibility = View.GONE
                    binding.flDoubleNext.visibility = View.VISIBLE
                    if (AppUtil.isChinese(this)) {
                        binding.tvYes.text = story.options[0].title.zh
                        binding.tvNo.text = story.options[1].title.zh
                    } else {
                        binding.tvYes.text = story.options[0].title.en
                        binding.tvNo.text = story.options[1].title.en
                    }
                }
            }
        } else {
            //????????????
            binding.ivImageFirst.visibility = View.GONE
            binding.ivImageSecond.visibility = View.GONE
            binding.ivChooseFirst.visibility = View.GONE
            binding.ivChooseSecond.visibility = View.GONE

            //??????????????????
            binding.flNext.visibility = View.GONE
            binding.flDoubleNext.visibility = View.GONE
            binding.flFinish.visibility = View.VISIBLE
            currentStory = null

            if (!story.picture.isNullOrBlank()) {
                binding.ivIcon.visibility = View.VISIBLE
            }
        }
    }

    private fun checkStep(yesOrNo: Type) {
        when (yesOrNo) {
            Type.Yes -> {
                if (currentStory!!.options.size == 2) {
                    val id = currentStory!!.options[0].id
                    for (child in storyList) {
                        if (child.id == id) {
                            currentStory = child
                            loadStory(child)
                        }
                    }
                }
            }

            Type.No -> {
                if (currentStory!!.options.size == 2) {
                    val id = currentStory!!.options[1].id
                    for (child in storyList) {
                        if (child.id == id) {
                            currentStory = child
                            loadStory(child)
                        }
                    }
                }
            }

            Type.Continue -> {

                //????????????
                if (currentStory == null) {
                    gameOver(false)
                    finish()
                    return
                }

                //?????????????????????????????????
                for (child in storyList) {
                    if (child.id == currentSelect) {
                        currentStory = child
                        loadStory(child)
                        currentSelect = -1
                    }
                }
            }
        }
    }

    private fun chooseFirst() {
        binding.ivChooseFirst.setImageResource(R.drawable.game_select)
        binding.ivChooseSecond.setImageResource(R.drawable.game_unselect)
        currentSelect = currentStory!!.options[0].id
    }

    private fun chooseSecond() {
        binding.ivChooseSecond.setImageResource(R.drawable.game_select)
        binding.ivChooseFirst.setImageResource(R.drawable.game_unselect)
        currentSelect = currentStory!!.options[1].id
    }

    private fun gameOver(toMatch: Boolean) {
        val id = intent.getIntExtra("id", -1)
        if (id != -1) {
            getLocalStorage().encode("recent_record", "game")
            val config = getMatchConfig()
            DataManager.gameOver(id, config.target_sex, dialogIdList) {
                finish()

                if (toMatch) {
                    Config.mainHandler?.sendEmptyMessage(0x10003)
                    finish()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        exoPlayer.stop()
        exoPlayer.release()
    }

    override fun onBackPressed() {
        QuitDialog(this, "game") {
            finish()
        }
    }

    enum class Type { Yes, No, Continue }

}