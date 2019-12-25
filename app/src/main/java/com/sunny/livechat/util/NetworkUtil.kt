package com.sunny.livechat.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import com.orhanobut.logger.Logger
import com.sunny.livechat.MyApplication

/**
 * Desc 网络请求判断
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/10/22 17:38
 */
object NetworkUtil {
    var isAvailable = false
    private val connectivityManager: ConnectivityManager = MyApplication.getInstance()
        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager


    fun init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(object :
                ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    isAvailable = true


                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    isAvailable = false
                    Logger.i("网络状态不可用")
                }
            })
        }
    }


    fun isNetworkAvailable(networkCallback: NetworkCallback) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            val net = connectivityManager.activeNetworkInfo
            isAvailable = net != null && net.isAvailable && net.isConnected
        }
        Logger.i("网络状态是否可用:$isAvailable")
        networkCallback.isNetworkAvailable(isAvailable)
    }


    interface NetworkCallback {
        fun isNetworkAvailable(isAvailable: Boolean)
    }
}



