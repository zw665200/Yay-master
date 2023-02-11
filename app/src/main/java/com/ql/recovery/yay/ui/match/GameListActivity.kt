package com.ql.recovery.yay.ui.match

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.ql.recovery.bean.GameCategory
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.adapter.DataAdapter
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.databinding.ActivityGameListBinding
import com.ql.recovery.yay.databinding.ItemGameBinding
import com.ql.recovery.yay.ui.base.BaseActivity
import com.ql.recovery.yay.util.AppUtil

class GameListActivity : BaseActivity() {
    private lateinit var binding: ActivityGameListBinding
    private lateinit var adapter: DataAdapter<GameCategory>
    private var mList = arrayListOf<GameCategory>()
    private var mCurrentGame: GameCategory? = null
    private var mCurrentPos = -1

    override fun getViewBinding(baseBinding: ActivityBaseBinding) {
        binding = ActivityGameListBinding.inflate(layoutInflater, baseBinding.flBase, true)
    }

    override fun initView() {
        binding.tvContinue.typeface = Typeface.createFromAsset(assets, "fonts/abc.ttf")
        binding.includeTitle.ivBack.setOnClickListener { finish() }
        binding.tvContinue.setOnClickListener { gameBegin() }
    }

    override fun initData() {
        val config = getMatchConfig()
        when (config.target_sex) {
            1 -> binding.includeTitle.ivOption.setImageResource(R.drawable.game_male)
            2 -> binding.includeTitle.ivOption.setImageResource(R.drawable.game_female)
        }

        initGameList()
        getGameList()
    }

    private fun initGameList() {
        val width = AppUtil.getScreenWidth(this)
        val height = AppUtil.getScreenHeight(this)
        adapter = DataAdapter.Builder<GameCategory>()
            .setData(mList)
            .setLayoutId(R.layout.item_game)
            .addBindView { itemView, itemData, position ->
                val itemBinding = ItemGameBinding.bind(itemView)
                val w = width / 3
                val lp = itemView.layoutParams
//                lp.width = w
                lp.height = height * 15 / 48
                itemView.layoutParams = lp

                itemBinding.tvTitle.text = itemData.name
                itemBinding.tvContent.text = itemData.description
                itemBinding.tvTitle.typeface = Typeface.createFromAsset(assets, "fonts/abc.ttf")

                if (mCurrentPos == position) {
                    itemView.background = ResourcesCompat.getDrawable(resources, R.drawable.game_list_select, null)
                    itemBinding.tvTitle.setTextColor(Color.BLACK)
                    itemBinding.tvContent.setTextColor(Color.BLACK)
                } else {
                    itemView.background = ResourcesCompat.getDrawable(resources, R.drawable.game_list_unselect, null)
                    itemBinding.tvTitle.setTextColor(Color.WHITE)
                    itemBinding.tvContent.setTextColor(Color.WHITE)
                }

                itemView.setOnClickListener {
                    mCurrentPos = position
                    mCurrentGame = itemData
                    adapter.notifyDataSetChanged()
                }
            }
            .create()

        binding.rcGame.adapter = adapter
        binding.rcGame.layoutManager = GridLayoutManager(this, 2)
    }

    private fun getGameList() {
        DataManager.getGameCategoryList {
            mList.clear()
            mList.addAll(it)
            adapter.notifyItemRangeChanged(0, mList.size)
        }
    }

    private fun gameBegin() {
        if (mCurrentGame != null) {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("id", mCurrentGame!!.id)
            startActivity(intent)
            finish()
        }
    }

}