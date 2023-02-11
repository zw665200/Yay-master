package com.ql.recovery.yay.ui.mine

import android.annotation.SuppressLint
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.ql.recovery.bean.Resource
import com.ql.recovery.manager.DataManager
import com.ql.recovery.yay.R
import com.ql.recovery.yay.adapter.MutableDataAdapter
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.databinding.ActivityLevelBinding
import com.ql.recovery.yay.databinding.ItemExpStyleOneBinding
import com.ql.recovery.yay.databinding.ItemExpStyleTwoBinding
import com.ql.recovery.yay.ui.base.BaseActivity
import com.ql.recovery.yay.util.AppUtil

class LevelActivity : BaseActivity() {
    private lateinit var binding: ActivityLevelBinding
    private lateinit var adapter: MutableDataAdapter<Resource>
    private var mList = arrayListOf<Resource>()
    private var gameList = arrayListOf<String>()
    private var giftList = arrayListOf<String>()

    override fun getViewBinding(baseBinding: ActivityBaseBinding) {
        binding = ActivityLevelBinding.inflate(layoutInflater, baseBinding.flBase, true)
    }

    override fun initView() {
        binding.ivBack.setOnClickListener { finish() }
    }

    override fun initData() {
        initExpView()
        getGradeDetail()
    }

    private fun initExpView() {
        val width = AppUtil.getScreenWidth(this)
        adapter = MutableDataAdapter.Builder<Resource>()
            .setData(mList)
            .addBindType("exp", "gift", "game")
            .setViewType { position -> mList[position].type }
            .setLayoutId(R.layout.item_exp_style_one, R.layout.item_exp_style_two, R.layout.item_exp_style_two)
            .addBindView { itemView, itemData ->
                when (itemData.type) {
                    "exp" -> {
                        val itemBinding = ItemExpStyleOneBinding.bind(itemView)
                        itemBinding.tvName.text = itemData.name
                        itemBinding.tvScore.text = itemData.icon.toString()
                        itemBinding.tvDes.text = String.format(getString(R.string.level_item_content), itemData.name)
                    }

                    "gift" -> {
                        val itemBinding = ItemExpStyleTwoBinding.bind(itemView)
                        itemBinding.tvName.text = getString(R.string.level_unlock)
                        for (child in giftList) {
                            val view = ImageView(this)
                            val lp = LinearLayout.LayoutParams(width / 5, LinearLayout.LayoutParams.WRAP_CONTENT)
                            lp.setMargins(5, 0, 5, 0)
                            view.layoutParams = lp
                            Glide.with(this).load(child).into(view)
                            itemBinding.llGift.addView(view)
                        }
                    }

                    "game" -> {
                        val itemBinding = ItemExpStyleTwoBinding.bind(itemView)
                        itemBinding.tvName.text = getString(R.string.level_unlock)
                        for (child in gameList) {
                            val view = ImageView(this)
                            val lp = LinearLayout.LayoutParams(width / 5, LinearLayout.LayoutParams.WRAP_CONTENT)
                            lp.setMargins(5, 0, 5, 0)
                            view.layoutParams = lp
                            Glide.with(this).load(child).into(view)
                            itemBinding.llGift.addView(view)
                        }
                    }
                }
            }
            .create()

        binding.rcExpList.adapter = adapter
        binding.rcExpList.layoutManager = LinearLayoutManager(this)
    }

    private fun getGradeDetail() {
        DataManager.getGrade { grade ->
            binding.tvLevel.text = String.format(getString(R.string.club_level), grade.grade)
            binding.tvNextExp.text = String.format(getString(R.string.level_next_hint), (grade.end_exp - grade.current_exp))
            binding.tvExpStart.text = grade.start_exp.toString()
            binding.tvExpEnd.text = grade.end_exp.toString()

            //设置进度条
            binding.progress.max = grade.end_exp
            binding.progress.progress = grade.current_exp

            mList.clear()

            for (child in grade.source) {
                mList.add(Resource("exp", child.exp, child.type))
            }

            val list1 = grade.unlock_games.map { it.icon }
            val list2 = grade.unlock_gifts.map { it.icon }
            gameList.addAll(list1)
            giftList.addAll(list2)

            if (grade.unlock_gifts.isNotEmpty()) {
                mList.add(Resource("gift", 0, ""))
            }

            if (grade.unlock_games.isNotEmpty()) {
                mList.add(Resource("game", 0, ""))
            }

            adapter.notifyItemRangeChanged(0, mList.size)
        }
    }
}