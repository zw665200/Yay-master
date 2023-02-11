package com.ql.recovery.yay.ui.self

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.ql.recovery.yay.util.JLog
import java.util.*
import kotlin.concurrent.thread

/**
 * @author Herr_Z
 * @description:
 * @date : 2023/1/11 10:43
 */
class ItemTouchHelperCallback<T>(private val adapter: com.netease.yunxin.kit.adapters.DataAdapter<String>, private var list: List<T>, private val func: () -> Unit) : ItemTouchHelper.Callback() {

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        if (recyclerView.layoutManager is GridLayoutManager) {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            val swipeFlags = 0
            return makeMovementFlags(dragFlags, swipeFlags)
        } else {
            val dragFlags = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            val swipeFlags = 0
            return makeMovementFlags(dragFlags, swipeFlags)
        }
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        val fromPosition = viewHolder.bindingAdapterPosition
        val toPosition = target.bindingAdapterPosition

        JLog.i("from = $fromPosition")
        JLog.i("to = $toPosition")
        JLog.i("list size = ${list.size}")

        //最后的位置不让插入
        if (toPosition == list.size - 1) return false

        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                JLog.i("i = $i")
                Collections.swap(list, i, i + 1)
            }

        } else {
            for (i in fromPosition downTo toPosition + 1) {
                JLog.i("i = $i")
                Collections.swap(list, i, i - 1)
            }
        }

        adapter.notifyItemMoved(fromPosition, toPosition)

        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

    }

    //当长按选中item的时候（拖拽开始的时候）调用
    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
        }
        super.onSelectedChanged(viewHolder, actionState)
    }

    //当手指松开的时候（拖拽完成的时候）调用
    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        JLog.i("clearview")
        thread {
            func()
        }
    }

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }
}