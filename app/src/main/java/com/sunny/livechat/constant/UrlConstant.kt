package com.sunny.livechat.constant

/**
 * Desc 接口配置清单
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/10/30 0030 18:13
 */
object UrlConstant {

    const val host = "http://10.0.0.158:1081" //杜康直播

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
     * 【直播】获取直播平台的用户信息
     */
    const val GET_LIVE_USER_INFO_URL = "app/sys/user/getUserIdAndNameToList"

}