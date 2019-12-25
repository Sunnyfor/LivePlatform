package com.sunny.livechat.util.factory

/**
 * Desc 异常页面显示处理
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/11/6 22:47
 */
object ViewTypeFactory {

    /**
     * 数据为空
     */
    fun empty(msg: String = "当前数据为空"): ViewType {
        return ViewType(0, msg, 0)
    }

    /**
     * 网络错误
     */
    fun netError(): ViewType {
        return ViewType(1, "当前没有网络！", 0)
    }

}