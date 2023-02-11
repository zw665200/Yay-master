package com.netease.yunxin.kit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class MutableDataAdapter<T> private constructor() : RecyclerView.Adapter<MutableDataAdapter<T>.MyViewHolder>() {

    //数据
    private var mDataList: MutableList<T>? = null

    //布局id
    private var mLayoutId: IntArray? = null

    //布局类型
    private var mType: Array<out Any>? = null
    private var mCurrentType: Any? = null

    //绑定事件的lambda放发
    private var addBindView: ((itemView: View, itemData: T) -> Unit)? = null
    private var addBindView2: ((itemView: View, itemData: T, position: Int) -> Unit)? = null

    private var setViewType: ((position: Int) -> Any)? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, type: Int): MyViewHolder {
        var view = LayoutInflater.from(viewGroup.context).inflate(mLayoutId!![0], viewGroup, false)
        if (type != -1) {
            view = LayoutInflater.from(viewGroup.context).inflate(mLayoutId!![mType!!.indexOf(mCurrentType!!)], viewGroup, false)
        }
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataList?.size ?: -1
    }

    override fun getItemViewType(position: Int): Int {
        mCurrentType = setViewType?.invoke(position)!!
        return mType!!.indexOf(mCurrentType!!)
    }

    override fun onBindViewHolder(p0: MyViewHolder, p1: Int) {
        addBindView?.invoke(p0.itemView, mDataList?.get(p1)!!)
        addBindView2?.invoke(p0.itemView, mDataList?.get(p1)!!, p1)
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    /**
     * 建造者，用来完成adapter的数据组合
     */
    class Builder<B> {

        private var adapter: MutableDataAdapter<B> = MutableDataAdapter()

        /**
         * 设置数据
         */
        fun setData(lists: MutableList<B>): Builder<B> {
            adapter.mDataList = lists
            return this
        }

        /**
         * 设置布局id
         */
        fun setLayoutId(vararg layoutId: Int): Builder<B> {
            adapter.mLayoutId = layoutId
            return this
        }

        /**
         * 获取多布局类型
         */
        fun setViewType(type: (position: Int) -> Any): Builder<B> {
            adapter.setViewType = type
            return this
        }

        /**
         * 设置多布局类型
         */
        fun addBindType(vararg type: Any): Builder<B> {
            adapter.mType = type
            return this
        }

        /**
         * 绑定View和数据
         */
        fun addBindView(itemBind: ((itemView: View, itemData: B) -> Unit)): Builder<B> {
            adapter.addBindView = itemBind
            return this
        }

        /**
         * 绑定View和数据(带position)
         */
        fun addBindView(itemBind: ((itemView: View, itemData: B, position: Int) -> Unit)): Builder<B> {
            adapter.addBindView2 = itemBind
            return this
        }


        fun create(): MutableDataAdapter<B> {
            return adapter
        }
    }
}