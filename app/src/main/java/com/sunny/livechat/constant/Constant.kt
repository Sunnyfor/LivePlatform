package com.sunny.livechat.constant

import com.sunny.livechat.util.sp.SpKey
import com.sunny.livechat.util.sp.SpUtil

/**
 * Desc 常量配置清单
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/10/30 0030 18:13
 */
object Constant {

    /**
     * 是否打印LOG
     */
    var isDebug = true


    /**
     * 是否自动登录
     */
    fun setAutoLogin(isAutoLogin: Boolean) {
        SpUtil.setBoolean(SpKey.isAutoLogin, isAutoLogin)
    }

    fun isAutoLogin(): Boolean {
        return SpUtil.getBoolean(SpKey.isAutoLogin, true)
    }

}