package com.sunny.livechat.util

import com.sunny.livechat.MyApplication

/**
 * Desc 系统级工具类
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2020/1/10 18:00
 */
object ToolUtil {

    /**
     * 获取app版本号
     */
    fun getVersionName(): String {
        val packInfo = MyApplication.getInstance().packageManager.getPackageInfo(MyApplication.getInstance().packageName, 0)
        return packInfo.versionName
    }

}