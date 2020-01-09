package com.sunny.livechat.live.bean

import com.starrtc.starrtcsdk.core.im.message.XHIMMessage

/**
 * Desc 获取聊天记录实体
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2020/1/3 15:20
 */
class GetMsgBean {
    var username: String? = null
    var uid: String? = null
    var time: String? = null
    var content: String? = null
    var groupId: String? = null
    var groupName: String? = null
    var chatRoomId: String? = null
    var toAvatar: String? = null
    var uavatar: String? = null

    constructor()

    constructor(XHIMMessage: XHIMMessage) {
        this.uid = XHIMMessage.fromId
        this.time = XHIMMessage.time.toString()
        this.content = XHIMMessage.contentData
    }
}