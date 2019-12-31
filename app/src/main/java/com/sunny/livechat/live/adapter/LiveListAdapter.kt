package com.sunny.livechat.live.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sunny.livechat.R
import com.sunny.livechat.base.BaseRecycleAdapter
import com.sunny.livechat.base.BaseRecycleViewHolder
import com.sunny.livechat.chat.MLOC
import com.sunny.livechat.live.bean.LiveListBean
import kotlinx.android.synthetic.main.item_live_room.view.*

/**
 * Desc 直播室列表适配器
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/12/23 19:31
 */
class LiveListAdapter(list: ArrayList<LiveListBean.LiveInfoBean>) : BaseRecycleAdapter<LiveListBean.LiveInfoBean>(list) {

    override fun setLayout(parent: ViewGroup, viewType: Int): View {
        return LayoutInflater.from(context).inflate(R.layout.item_live_room, parent, false)
    }

    override fun onBindViewHolder(holder: BaseRecycleViewHolder, position: Int) {
        holder.itemView.tv_live_room_name.text = getData(position).liveName
        holder.itemView.tv_live_creator.text = getData(position).creatName
        holder.itemView.tv_create_time.text = getData(position).creatTime

        holder.itemView.tv_label_mine.visibility = if (MLOC.userId == getData(position).creator) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
}