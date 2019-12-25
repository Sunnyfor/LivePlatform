package com.sunny.livechat.base

import android.util.SparseArray
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Desc ViewHolder 父类
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/11/5 13:56
 */
@Suppress("UNCHECKED_CAST")
class BaseRecycleViewHolder(itemView: View, onItemClickListener: ((View, Int) -> Unit)?,onLongItemClickListener: ((View, Int) -> Unit)?) : RecyclerView.ViewHolder(itemView), View.OnClickListener,View.OnLongClickListener {

    private val viewMap = SparseArray<View>()
    private var onItemClickListener: ((View, Int) -> Unit)? = null
    private var onLongItemClickListener: ((View, Int) -> Unit)? = null

    init {
        if (onItemClickListener != null) {
            this.onItemClickListener = onItemClickListener
            itemView.setOnClickListener(this)
        }

        if (onLongItemClickListener != null) {
            this.onLongItemClickListener = onLongItemClickListener
            itemView.setOnLongClickListener(this)
        }


    }

    fun <T : View> getView(id: Int): T {
        if (viewMap.get(id) != null) {
            return viewMap.get(id) as T
        }
        val view = itemView.findViewById(id) as T
        viewMap.put(id, view)
        return view
    }

    override fun onClick(v: View) {
        onItemClickListener?.invoke(v, adapterPosition)
    }

    override fun onLongClick(v: View): Boolean {
        onLongItemClickListener?.invoke(v,adapterPosition)
        return false
    }
}