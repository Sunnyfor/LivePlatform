package com.sunny.livechat

import android.app.Application
import com.orhanobut.logger.*
import com.sunny.livechat.chat.MLOC
import com.sunny.livechat.util.NetworkUtil


/**
 * Desc
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/10/22 13:28
 */
class MyApplication : Application() {

    /**
     * 单例
     */
    companion object {
        private lateinit var instance: MyApplication
        fun getInstance(): MyApplication =
            instance
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        NetworkUtil.init()

        MLOC.init(this)

        Logger.addLogAdapter(AndroidLogAdapter())

    }
}