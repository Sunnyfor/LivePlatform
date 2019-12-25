package com.sunny.livechat.base

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * Desc BaseAdapter 父类
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/11/5 13:55
 */
abstract class BaseRecycleAdapter<T>(val list: ArrayList<T>) : RecyclerView.Adapter<BaseRecycleViewHolder>() {

    /**
     * 分页页数
     */
    private var page = 1

    private var isDouble = false
    private var onItemClickListener: ((View, Int) -> Unit)? = null
    private var onLongItemClickListener: ((View, Int) -> Unit)? = null
    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseRecycleViewHolder {
        context = parent.context
        return BaseRecycleViewHolder(setLayout(parent, viewType), onItemClickListener,onLongItemClickListener)
    }

    override fun getItemCount(): Int = list.size

    abstract fun setLayout(parent: ViewGroup, viewType: Int): View

    /**
     * 绑定数据
     */
    abstract override fun onBindViewHolder(holder: BaseRecycleViewHolder, position: Int)

    fun getData(position: Int): T = list[position]

    fun deleteData(position: Int) {
        list.removeAt(position)
    }

    /**
     * 子条目点击事件
     */
    fun setOnItemClickListener(onItemClickListener: ((View, Int) -> Unit)?) {
        this.onItemClickListener = onItemClickListener
    }

    /**
     * 子条目长按事件
     */
    fun setOnLangClickListener(onLongItemClickListener: ((View, Int) -> Unit)?){
        this.onLongItemClickListener = onLongItemClickListener
    }


    fun refreshPage(): Int {
        page = 1
        return page
    }


    fun loadMorePage(hasData: Boolean): Int {
        if (hasData)
            page++
        else
            page--

        return page
    }

}