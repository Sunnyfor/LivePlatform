package com.sunny.livechat.login.view

import com.sunny.livechat.base.IBaseView
import com.sunny.livechat.login.bean.UserBean

/**
 * Desc
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/10/22 18:14
 */
interface ILoginView : IBaseView {

    //登录结果回调
    fun loginResult(model: UserBean?)

}