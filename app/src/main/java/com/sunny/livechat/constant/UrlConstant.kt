package com.sunny.livechat.constant

import com.sunny.livechat.util.sp.SpKey
import com.sunny.livechat.util.sp.SpUtil

/**
 * Desc 接口配置清单
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/10/30 0030 18:13
 */
object UrlConstant {

    var host = ""
        get() {
            return if (field.isNotEmpty()) {
                field
            } else {
                val value = SpUtil.getString(SpKey.hostUrl)
                field = if (value.isNotEmpty()) {
                    value
                } else {
                    "http://114.242.23.225:1081"
                }
                field
            }
        }
        set(value) {
            if (value.isNotEmpty()) {
                field = value
                SpUtil.setString(SpKey.hostUrl, value)
            }
        }

    /**
     * 登录
     */
    const val LOGIN_URL = "app/sys/login"

    /**
     * 【直播】创建直播间
     * {liveName: "JoannChen", liveCode: "", liveState: "", isMsg: "", liveSrc: "", liveClassId: "",…}
     */
    const val CREATE_LIVE_ROOM_URL = "app/live/liveinfo/save"

    /**
     * 【直播】直播间列表
     */
    const val GET_LIVE_ROOM_LIST_URL = "app/live/liveinfo/list"

    /**
     * 【直播】获取直播间信息
     */
    const val GET_LIVE_ROOM_INFO_URL = "app/live/liveinfo/getLiveInfoByCreatorId"

    /**
     * 【直播】修改直播间信息
     */
    const val SET_LIVE_ROOM_INFO_URL = "app/live/liveinfo/update"

    /**
     * 【直播】上传图片
     */
    const val UPLOAD_IMAGE_URL = "app//sys/sysfujian/upload"

    /**
     * 【直播】获取直播平台的用户信息
     */
    const val GET_LIVE_USER_INFO_URL = "app/sys/user/getUserIdAndNameToList"

    /**
     * 【直播】上传聊天记录：postJson
     * content    //内容
     * userId     //发送人
     * sendTime   //时间戳
     * chatRoomId //所属群组
     */
    const val SEND_CHAT_MSG_URL = "app/app/appgroupmessage/save"


    /**
     * 【直播】获取聊天记录
     * ?toLiveId=liveId
     */
    const val GET_CHAT_MSG_URL = "app/app/appgroupmessage/getGroupMsgList"


}