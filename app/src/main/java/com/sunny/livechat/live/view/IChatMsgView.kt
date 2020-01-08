package com.sunny.livechat.live.view

import com.sunny.livechat.base.IBaseView
import com.sunny.livechat.live.bean.GetMsgBean
import com.sunny.livechat.live.bean.SendMsgBean

/**
 * Desc 聊天消息上传和获取
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2020/1/3 09:44
 */
interface IChatMsgView : IBaseView {

    fun sendChatMsgList()

    fun getChatMsgList(list: ArrayList<GetMsgBean>)

    fun getUserNickname(list: ArrayList<SendMsgBean>)

}