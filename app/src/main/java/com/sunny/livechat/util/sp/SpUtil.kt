package com.sunny.livechat.util.sp

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import com.google.gson.Gson
import com.sunny.livechat.MyApplication

/**
 * Desc SharedPreferences保存数据工具类
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/10/22 17:38
 */
object SpUtil {

    /**
     * 文件名
     */
    private const val FILE_NAME = "sharedPreferences_info"

    /**
     * 获取SharedPreferences对象
     */
    private val sharedPreferences: SharedPreferences
        get() = MyApplication.getInstance().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)


    /**
     * 保存String信息
     */
    fun setString(key: String, content: String) =
        sharedPreferences.edit().putString(key, content).apply()

    fun getString(key: String, content: String): String =
        sharedPreferences.getString(key, null) ?: content

    fun getString(key: String): String = getString(key, "")


    /**
     * 保存Boolean类型的信息
     */
    fun setBoolean(key: String, flag: Boolean) =
        sharedPreferences.edit().putBoolean(key, flag).apply()

    fun getBoolean(key: String, flag: Boolean): Boolean = sharedPreferences.getBoolean(key, flag)

    fun getBoolean(key: String): Boolean = getBoolean(key, false)


    /**
     * 保存Integer信息
     */
    fun setInteger(key: String, content: Int = 0) =
        sharedPreferences.edit().putInt(key, content).apply()

    fun getInteger(key: String): Int = sharedPreferences.getInt(key, 0)

    /**
     * 保存Object信息
     */
    fun setObject(key: String, obj: Any) {
        val gson = Gson()
        val json = gson.toJson(obj)
        setString(key, json)
    }

    fun <T> getObject(key: String, clazz: Class<T>): T? {
        val json = getString(key)
        if (TextUtils.isEmpty(json)) {
            return null
        }
        return try {
            val gson = Gson()
            gson.fromJson<T>(json, clazz)
        } catch (e: Exception) {
            null
        }

    }


    /**
     * 删除元素
     */
    fun remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    /**
     * 退出登录后，清空数据
     */
    fun logout() {
        sharedPreferences.edit().clear().apply()
    }

}