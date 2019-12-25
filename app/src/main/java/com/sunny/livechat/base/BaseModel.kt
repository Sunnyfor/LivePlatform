package com.sunny.livechat.base

/**
 * 公共实体类
 * Created by 张野 on 2017/9/14.
 */

class BaseModel<T> {
    var msg = ""
    var code = "0"
    var data: T? = null
    var page: T? = null
    var type = javaClass

    override fun toString(): String {
        return "BaseModel(msg='$msg', code='$code', data=$data, page=$page, type=$type)"
    }

}