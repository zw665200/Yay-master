package com.ql.recovery.yay.ui.mine

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.ql.recovery.bean.Resource
import com.ql.recovery.yay.R
import com.ql.recovery.yay.adapter.DataAdapter
import com.ql.recovery.yay.adapter.MutableDataAdapter
import com.ql.recovery.yay.databinding.ActivityBaseBinding
import com.ql.recovery.yay.databinding.ActivityQuestionBinding
import com.ql.recovery.yay.databinding.ItemQuestionContentBinding
import com.ql.recovery.yay.databinding.ItemQuestionTitleBinding
import com.ql.recovery.yay.ui.base.BaseActivity

class QuestionActivity : BaseActivity() {
    private lateinit var binding: ActivityQuestionBinding
    private val mainList = arrayListOf<Resource>()
    private lateinit var adapter: DataAdapter<Resource>

    override fun getViewBinding(baseBinding: ActivityBaseBinding) {
        binding = ActivityQuestionBinding.inflate(layoutInflater, baseBinding.flBase, true)
    }

    override fun initView() {
        setStatusBarLight()
        binding.includeTitle.ivBack.setOnClickListener { finish() }
    }

    override fun initData() {
        binding.includeTitle.tvName.text = getString(R.string.question_title)
        initRecyclerView()
        getData()
    }

    private fun initRecyclerView() {
        adapter = DataAdapter.Builder<Resource>()
            .setData(mainList)
            .setLayoutId(R.layout.item_question_content)
            .addBindView { itemView, itemData ->
                val itemBinding = ItemQuestionContentBinding.bind(itemView)
                itemBinding.questionContentTitle.text = itemData.type
                itemBinding.questionContent.text = itemData.name
                itemBinding.root.setOnClickListener {
                    when (itemBinding.questionContent.visibility) {
                        View.VISIBLE -> itemBinding.questionContent.visibility = View.GONE
                        View.GONE -> itemBinding.questionContent.visibility = View.VISIBLE
                    }
                }
            }.create()

        binding.questionList.adapter = adapter
        binding.questionList.layoutManager = LinearLayoutManager(this)
    }

    private fun getData() {
        mainList.add(Resource(getString(R.string.question_title_1), 0, getString(R.string.question_content_1)))
        mainList.add(Resource(getString(R.string.question_title_2), 0, getString(R.string.question_content_2)))
        mainList.add(Resource(getString(R.string.question_title_3), 0, getString(R.string.question_content_3)))
        mainList.add(Resource(getString(R.string.question_title_4), 0, getString(R.string.question_content_4)))
        mainList.add(Resource(getString(R.string.question_title_5), 0, getString(R.string.question_content_5)))
        mainList.add(Resource(getString(R.string.question_title_6), 0, getString(R.string.question_content_6)))
        mainList.add(Resource(getString(R.string.question_title_7), 0, getString(R.string.question_title_7)))
        adapter.notifyItemRangeChanged(0, mainList.size)
    }

}