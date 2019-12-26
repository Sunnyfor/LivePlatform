package com.sunny.livechat.base

import com.sunny.livechat.util.ToastUtil

/**
 * 公共实体类
 * Created by 张野 on 2017/9/14.
 */

class BaseModel<T> {
    var msg = ""
    var code = 0
    var data: T? = null
    var page: T? = null
    var type = javaClass

    /**
     * 请求结果
     */
    fun requestResult(onSuccess: () -> Unit, onFailed: () -> Unit) {
        if (code == 0 && msg == "success") {
            onSuccess()
        } else {
            ToastUtil.show(msg)
            onFailed()
        }
    }

    override fun toString(): String {
        return "BaseModel(msg='$msg', code='$code', data=$data, page=$page, type=$type)"
    }

}