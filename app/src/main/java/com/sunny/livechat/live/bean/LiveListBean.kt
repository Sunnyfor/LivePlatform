package com.sunny.livechat.live.bean

import java.io.Serializable

/**
 * Desc 直播列表实体类
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/12/23 19:26
 */
class LiveListBean {
    var totalCount = 0
    var pageSize = 0
    var totalPage = 0
    var currPage = 0
    var list: ArrayList<LiveInfoBean>? = null

    class LiveInfoBean : Serializable {
        var liveId: String? = null//直播间id，后端自动生成
        var liveName: String? = null
        var liveCode: String? = null//直播码，StarRtc服务端返回
        var creator: String? = null //创建者id
        var creatName: String? = null
        var creatTime: String? = null
        var liveState: String? = null
        var isMsg: String? = null
        var liveSrc: String? = null
        var liveClassId: Int? = null//直播类型
        var liveNotice: String? = null
        var liveCover: String? = null

        override fun toString(): String {
            return "LiveInfoBean(liveId=$liveId, liveName=$liveName, liveCode=$liveCode, creator=$creator, creatName=$creatName, creatTime=$creatTime, liveState=$liveState, isMsg=$isMsg, liveSrc=$liveSrc, liveClassId=$liveClassId, liveNotice=$liveNotice, liveCover=$liveCover)"
        }
    }

    override fun toString(): String {
        return "LiveListBean(totalCount=$totalCount, pageSize=$pageSize, totalPage=$totalPage, currPage=$currPage, list=$list)"
    }


}