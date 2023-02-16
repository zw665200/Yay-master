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
        binding.tvNext.setOnClickListener { checkStep(Type.Continue) }
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
        //记录id
        dialogIdList.add(story.id)

        //播放声音
        if (story.sound.isNotBlank()) {
            val mediaItem = MediaItem.fromUri(story.sound)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }

        //加载背景图片
        ImageManager.getBitmap(this, story.background) {
            binding.root.background = BitmapDrawable(resources, it)
        }

        //加载小图
        if (!story.picture.isNullOrBlank()) {
            Glide.with(this).load(story.picture).into(binding.ivIcon)
        }

        //加载内容
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

                //加载两个选项
                if (story.options.size == 2) {
                    binding.ivImageFirst.visibility = View.VISIBLE
                    binding.ivImageSecond.visibility = View.VISIBLE
                    Glide.with(this).load(story.options[0].picture).into(binding.ivImageFirst)
                    Glide.with(this).load(story.options[1].picture).into(binding.ivImageSecond)
                }

                //加载单按钮
                binding.flNext.visibility = View.VISIBLE
                binding.flDoubleNext.visibility = View.GONE

            } else {
                binding.llChoose.visibility = View.GONE
                binding.ivIcon.visibility = View.VISIBLE

                //加载双按钮
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
            //游戏结束
            binding.ivImageFirst.visibility = View.GONE
            binding.ivImageSecond.visibility = View.GONE
            binding.ivChooseFirst.visibility = View.GONE
            binding.ivChooseSecond.visibility = View.GONE

            //加载结束按钮
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

                //游戏结束
                if (currentStory == null) {
                    gameOver(false)
                    finish()
                    return
                }

                //根据选项加载下一个章节
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