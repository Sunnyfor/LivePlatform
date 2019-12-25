package com.sunny.livechat.http

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Desc 头信息
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/10/22 0022 12:02
 */
class HeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val authorised = originalRequest.newBuilder()
                .header("authorization", "")
                .build()
        return chain.proceed(authorised)
    }

}