package com.netease.yunxin.kit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class DataAdapter<T> private constructor() : RecyclerView.Adapter<DataAdapter<T>.MyViewHolder>() {

    //数据
    private var mDataList: List<T>? = null

    //布局id
    private var mLayoutId: Int? = null

    //绑定事件的lambda放发
    private var addBindView: ((itemView: View, itemData: T) -> Unit)? = null
    private var addBindView2: ((itemView: View, itemData: T, position: Int) -> Unit)? = null
    private var addBindView3: ((itemView: View, itemData: T, position: Int, payloads: MutableList<Any>) -> Unit)? = null

    private var attachView: ((itemView: View) -> Unit)? = null
    private var detachView: ((itemView: View) -> Unit)? = null


    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MyViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(mLayoutId!!, p0, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        //左侧为null时返回-1
        return mDataList?.size ?: -1
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        addBindView?.invoke(holder.itemView, mDataList?.get(position)!!)
        addBindView2?.invoke(holder.itemView, mDataList?.get(position)!!, position)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int, payloads: MutableList<Any>) {
        addBindView?.invoke(holder.itemView, mDataList?.get(position)!!)
        addBindView2?.invoke(holder.itemView, mDataList?.get(position)!!, position)
        addBindView3?.invoke(holder.itemView, mDataList?.get(position)!!, position, payloads)
    }


    override fun onViewAttachedToWindow(holder: MyViewHolder) {
        super.onViewAttachedToWindow(holder)
        attachView?.invoke(holder.itemView)
    }

    override fun onViewDetachedFromWindow(holder: MyViewHolder) {
        super.onViewDetachedFromWindow(holder)
        detachView?.invoke(holder.itemView)
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    /**
     * 建造者，用来完成adapter的数据组合
     */
    class Builder<B> {

        private var adapter: DataAdapter<B> = DataAdapter()

//        /**
//         * 设置数据
//         */
//        fun setData(lists: MutableList<B>): Builder<B> {
//            adapter.mDataList = lists
//            return this
//        }

        /**
         * 设置数据
         */
        fun setData(lists: List<B>): Builder<B> {
            adapter.mDataList = lists
            return this
        }

        /**
         * 设置布局id
         */
        fun setLayoutId(layoutId: Int): Builder<B> {
            adapter.mLayoutId = layoutId
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
         * 绑定View和数据
         */
        fun addBindView(itemBind: ((itemView: View, itemData: B, position: Int) -> Unit)): Builder<B> {
            adapter.addBindView2 = itemBind
            return this
        }

        /**
         * 绑定View和数据
         */
        fun addBindView(itemBind: ((itemView: View, itemData: B, position: Int, payloads: MutableList<Any>) -> Unit)): Builder<B> {
            adapter.addBindView3 = itemBind
            return this
        }

        fun onViewAttachedToWindow(method: (itemView: View) -> Unit): Builder<B> {
            adapter.attachView = method
            return this
        }

        fun onViewDetachToWindow(method: (itemView: View) -> Unit): Builder<B> {
            adapter.detachView = method
            return this
        }


        fun create(): DataAdapter<B> {
            return adapter
        }
    }
}