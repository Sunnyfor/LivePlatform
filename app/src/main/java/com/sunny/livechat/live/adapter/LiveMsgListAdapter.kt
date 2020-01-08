package com.sunny.livechat.live.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.starrtc.starrtcsdk.core.im.message.XHIMMessage
import com.sunny.livechat.MyApplication
import com.sunny.livechat.R
import com.sunny.livechat.base.BaseRecycleAdapter
import com.sunny.livechat.base.BaseRecycleViewHolder
import com.sunny.livechat.util.ToastUtil
import com.sunny.livechat.util.intent.IntentKey
import kotlinx.android.synthetic.main.item_live_msg_list.view.*

/**
 * Desc 直播聊天列表实体
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/12/23 20:02
 */
class LiveMsgListAdapter(list: ArrayList<XHIMMessage>) : BaseRecycleAdapter<XHIMMessage>(list) {

    override fun setLayout(parent: ViewGroup, viewType: Int): View =
        LayoutInflater.from(context).inflate(R.layout.item_live_msg_list, parent, false)

    override fun onBindViewHolder(holder: BaseRecycleViewHolder, position: Int) {

        val msgMap = MyApplication.getInstance().getData<HashMap<String, String>>(IntentKey.dataMap) ?: hashMapOf()

        if (msgMap.containsKey(getData(position).fromId)) {
            holder.itemView.tv_user_id.text = ("${msgMap[getData(position).fromId]} : ")
        } else {
            ToastUtil.show("该帐号不存在")
        }
        holder.itemView.tv_msg.text = getData(position).contentData
    }
}