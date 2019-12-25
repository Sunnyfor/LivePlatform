package com.sunny.livechat.live.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.starrtc.starrtcsdk.core.im.message.XHIMMessage
import com.sunny.livechat.R

/**
 * Desc 直播聊天列表实体
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/12/23 20:02
 */
class LiveMsgListAdapter(context: Context, var list: ArrayList<XHIMMessage>?) : BaseAdapter() {

    private val mInflater: LayoutInflater by lazy {
        LayoutInflater.from(context)
    }

    override fun getCount(): Int {
        return if (list == null) 0 else list?.size ?: 0
    }

    override fun getItem(position: Int): Any? {
        return if (list == null) null else list?.get(position)
    }

    override fun getItemId(position: Int): Long {
        return if (list == null) 0 else position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var mConvertView = convertView
        val holder: ViewHolder
        if (mConvertView == null) {
            holder = ViewHolder()
            mConvertView = mInflater.inflate(R.layout.item_live_msg_list, null)
            holder.vUserId = mConvertView?.findViewById(R.id.item_user_id) as TextView
            holder.vMsg = mConvertView.findViewById(R.id.item_msg) as TextView
            mConvertView.tag = holder
        } else {
            holder = mConvertView.tag as ViewHolder
        }
        val msgText = list?.get(position)?.contentData
        holder.vMsg?.text = msgText
        holder.vUserId?.text = list?.get(position)?.fromId
        return mConvertView
    }


    class ViewHolder {
        var vUserId: TextView? = null
        var vMsg: TextView? = null
    }
}