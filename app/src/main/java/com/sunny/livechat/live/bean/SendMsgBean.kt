package com.sunny.livechat.live.bean

/**
 * Desc 上传聊天消息实体类
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2020/1/3 10:34
 */
class SendMsgBean {
    var content: String? = null//内容
    var userId: String? = null//发送人
    var sendTime: Long? = null//时间戳
    var chatRoomId: String? = null//所属群组
}