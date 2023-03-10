package com.ql.recovery.yay.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class PlayerAdapter<T> private constructor() : RecyclerView.Adapter<PlayerAdapter<T>.MyViewHolder>() {

    //数据
    private var mDataList: List<T>? = null

    //布局id
    private var mLayoutId: Int? = null

    //绑定事件的lambda放发
    private var addBindView: ((itemView: View, itemData: T) -> Unit)? = null
    private var addBindView2: ((itemView: View, itemData: T, position: Int) -> Unit)? = null
    private var addBindView3: ((itemView: View, itemData: T, position: Int, payloads: MutableList<Any>) -> Unit)? = null

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MyViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(mLayoutId!!, p0, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        //左侧为null时返回-1
        return mDataList?.size ?: -1
    }


    override fun onBindViewHolder(p0: MyViewHolder, p1: Int) {
        addBindView?.invoke(p0.itemView, mDataList?.get(p1)!!)
        addBindView2?.invoke(p0.itemView, mDataList?.get(p1)!!, p1)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int, payloads: MutableList<Any>) {
        addBindView?.invoke(holder.itemView, mDataList?.get(position)!!)
        addBindView2?.invoke(holder.itemView, mDataList?.get(position)!!, position)
        addBindView3?.invoke(holder.itemView, mDataList?.get(position)!!, position, payloads)
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    /**
     * 建造者，用来完成adapter的数据组合
     */
    class Builder<B> {

        private var adapter: PlayerAdapter<B> = PlayerAdapter()

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

        fun create(): PlayerAdapter<B> {
            return adapter
        }
    }
}