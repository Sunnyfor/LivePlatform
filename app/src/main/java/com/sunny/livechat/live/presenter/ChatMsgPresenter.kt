package com.sunny.livechat.live.presenter

import com.google.gson.Gson
import com.sunny.livechat.base.BaseModel
import com.sunny.livechat.base.BasePresenter
import com.sunny.livechat.constant.UrlConstant
import com.sunny.livechat.http.ApiManager
import com.sunny.livechat.live.bean.GetMsgBean
import com.sunny.livechat.live.bean.SendMsgBean
import com.sunny.livechat.live.view.IChatMsgView
import com.sunny.livechat.util.ToastUtil

/**
 * Desc 聊天消息上传和获取
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2020/1/3 09:43
 */
class ChatMsgPresenter(view: IChatMsgView) : BasePresenter<IChatMsgView>(view) {

    override fun onCreate() {}

    override fun onClose() {}


    /**
     * 上传聊天记录
     */
    fun sendChatMsgList(chatMsgBean: SendMsgBean) {

        val json = Gson().toJson(chatMsgBean)

        ApiManager.postJson(null, json.toString(), UrlConstant.SEND_CHAT_MSG_URL,
            object : ApiManager.OnResult<BaseModel<String>>() {
                override fun onSuccess(model: BaseModel<String>) {
                    model.requestResult({
                        view?.sendChatMsgList()
                    }, {})
                }

                override fun onFailed(code: String, msg: String) {
                    ToastUtil.showInterfaceError(code, msg)
                }
            })
    }

    /**
     * 获取聊天记录
     */
    fun getChatMsgList(liveRoomId: String) {

        ApiManager.post(null, hashMapOf("toLiveId" to liveRoomId), UrlConstant.GET_CHAT_MSG_URL,
            object : ApiManager.OnResult<BaseModel<ArrayList<GetMsgBean>>>() {
                override fun onSuccess(model: BaseModel<ArrayList<GetMsgBean>>) {
                    model.requestResult({
                        view?.getChatMsgList(model.data ?: return@requestResult)
                    }, {})
                }

                override fun onFailed(code: String, msg: String) {
                    ToastUtil.showInterfaceError(code, msg)
                }
            })
    }
}