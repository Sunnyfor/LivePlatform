package com.sunny.livechat.login.bean

import com.sunny.livechat.base.BaseModel

/**
 * Desc 用户信息实体类
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/12/25 16:01
 */
class UserBean {

    var userId: Int? = null
    var username: String? = null
    var roleIdList: ArrayList<Int>? = null
    var createTime: Long? = null
    var deptId: Int? = null
    var deptName: String? = null
    var levelCode: String? = null
}