package com.sunny.livechat.util.sp

/**
 * Desc SharedPreferences配置清单
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/10/31 0031 10:57
 */
object SpKey {

    /**
     * 是否进行版本更新，版本更新时间
     */
    const val updateTime = "updateTime"
    const val isUpdateVersion = "isUpdateVersion"

    /**
     * 直播信息实体类
     */
    const val liveInfoBean = "liveInfoBean"

    /**
     * 手动修改hostURl
     */
    const val hostUrl: String = "hostUrl"

    /**
     * 保存hostList
     */
    const val hostList: String = "hostList"

    /**
     * 是否自动登录
     */
    const val isAutoLogin = "isAutoLogin"

    /**
     * 是否记住密码
     */
    const val isRememberPassword: String = "isRememberPassword"

    /**
     * 帐号密码：自动登录
     */
    const val username: String = "username"
    const val password: String = "password"
}