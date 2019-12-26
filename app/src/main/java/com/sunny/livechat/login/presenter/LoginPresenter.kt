package com.sunny.livechat.login.presenter

import com.sunny.livechat.base.BaseModel
import com.sunny.livechat.base.BasePresenter
import com.sunny.livechat.constant.UrlConstant
import com.sunny.livechat.http.ApiManager
import com.sunny.livechat.login.bean.UserBean
import com.sunny.livechat.login.view.ILoginView
import org.json.JSONObject

/**
 * Desc
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/10/22 18:20
 */
class LoginPresenter(view: ILoginView) : BasePresenter<ILoginView>(view) {

    override fun onCreate() {
        //查询操作
    }

    override fun onClose() {

    }


    fun login(username: String, password: String) {

        val params = JSONObject()
        params.put("username", username)
        params.put("password", password)

        ApiManager.postJson(composites, params.toString(), UrlConstant.LOGIN_URL,
            object : ApiManager.OnResult<BaseModel<UserBean>>() {

                override fun onSuccess(model: BaseModel<UserBean>) {
                    model.requestResult({ view?.loginResult(model.data) }, {})
                }

                override fun onFailed(code: String, msg: String) {

                }

            })
    }
}