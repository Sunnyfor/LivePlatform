package com.sunny.livechat.live

import android.app.Activity
import android.content.Intent
import android.view.View
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.gson.Gson
import com.orhanobut.logger.Logger
import com.starrtc.starrtcsdk.api.XHClient
import com.starrtc.starrtcsdk.api.XHConstants
import com.starrtc.starrtcsdk.api.XHLiveItem
import com.starrtc.starrtcsdk.apiInterface.IXHResultCallback
import com.sunny.livechat.R
import com.sunny.livechat.base.BaseActivity
import com.sunny.livechat.base.BaseModel
import com.sunny.livechat.chat.MLOC
import com.sunny.livechat.constant.RefreshLiveListEvent
import com.sunny.livechat.constant.UrlConstant
import com.sunny.livechat.http.ApiManager
import com.sunny.livechat.live.bean.LiveListBean
import com.sunny.livechat.util.GlideApp
import com.sunny.livechat.util.ToastUtil
import com.sunny.livechat.util.URIUtil
import com.sunny.livechat.util.intent.IntentValue
import kotlinx.android.synthetic.main.activity_live_create.*
import org.greenrobot.eventbus.EventBus

/**
 * Desc 创建直播间
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/12/24 14:52
 */
class CreateLiveActivity : BaseActivity() {

    private var liveInfoBean = LiveListBean.LiveInfoBean()

    override fun setLayout(): Int = R.layout.activity_live_create

    override fun initTitle(): View? = titleManager.defaultTitle("创建直播间")

    override fun initView() {
        iv_live_cover.setOnClickListener(this)
        btn_create.setOnClickListener(this)
    }

    override fun onClickEvent(v: View) {
        when (v.id) {
            R.id.btn_create -> doCreateLiveSDK()
            R.id.iv_live_cover -> {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(intent, IntentValue.requestCode)
            }
        }
    }

    override fun loadData() {}

    override fun close() {}

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IntentValue.requestCode && resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data
            val path = URIUtil.getRealPathFromUri(this, uri)
            liveInfoBean.liveCover = path
            GlideApp.with(this)
                .load(path)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .centerCrop()
                .into(iv_live_cover)

            Logger.i("图片的绝对地址:$path")
        }
    }

    private fun doCreateLiveApi(json: String) {

        ApiManager.postJson(null, json, UrlConstant.CREATE_LIVE_ROOM_URL,
            object : ApiManager.OnResult<BaseModel<LiveListBean>>() {
                override fun onSuccess(model: BaseModel<LiveListBean>) {
                    model.requestResult({
                        ToastUtil.show("创建成功")
                        finish()
                        EventBus.getDefault().post(RefreshLiveListEvent())
                    }, {})
                }

                override fun onFailed(code: String, msg: String) {
                    ToastUtil.showInterfaceError(code, msg)
                }
            })
    }

    private fun doCreateLiveSDK() {

        val liveName = et_live_name.text.toString()

        if (liveName.isEmpty()) {
            ToastUtil.show("请输入直播间名称！")
            return
        }

        val liveItem = XHLiveItem()
        liveItem.liveName = liveName
        liveItem.liveType = XHConstants.XHLiveType.XHLiveTypeGlobalPublic

        val liveManager = XHClient.getInstance().getLiveManager(this)
        liveManager?.createLive(liveItem, object : IXHResultCallback {
            override fun success(data: Any) {

                Logger.i("创建直播房间成功：SDK")

                val liveCode = data as String

                //上报到直播列表

                liveInfoBean.liveCode = liveCode
                liveInfoBean.liveName = liveName
                liveInfoBean.creator = MLOC.userId
                liveInfoBean.liveNotice = et_live_notice.text.toString()

                val json = Gson().toJson(liveInfoBean)

                doCreateLiveApi(json)

            }

            override fun failed(errMsg: String) {
                ToastUtil.show(errMsg)
            }
        })
    }

}