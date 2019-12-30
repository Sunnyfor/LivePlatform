package com.sunny.livechat

import android.app.Application
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.sunny.livechat.chat.MLOC
import com.sunny.livechat.util.NetworkUtil


/**
 * Desc 全局管理类
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/10/22 13:28
 */
class MyApplication : Application() {

    /**
     * 内存数据存储
     */
    private val saveDataMap = HashMap<String, Any>()


    /**
     * 单例
     */
    companion object {

        private lateinit var instance: MyApplication

        fun getInstance(): MyApplication = instance

    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        NetworkUtil.init()

        MLOC.init(this)

        Logger.addLogAdapter(AndroidLogAdapter())

    }

    /**
     * 取数据
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getData(key: String, isDelete: Boolean): T? {

        if (!saveDataMap.containsKey(key)) {
            return null
        }

        val result = saveDataMap[key]

        if (isDelete) {
            removeData(key)
        }
        return result as T
    }

    fun <T> getData(key: String): T? {
        return getData(key, false)
    }

    /**
     * 存储数据
     */
    fun putData(key: String, t: Any?) {
        if (t != null) {
            saveDataMap[key] = t
        }
    }

    /**
     * 删除数据
     */
    fun removeData(key: String) {
        saveDataMap.remove(key)
    }

}