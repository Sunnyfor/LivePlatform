package com.sunny.livechat.live

import android.content.Intent
import android.view.View
import com.google.gson.Gson
import com.orhanobut.logger.Logger
import com.starrtc.starrtcsdk.api.XHClient
import com.starrtc.starrtcsdk.api.XHConstants
import com.starrtc.starrtcsdk.api.XHLiveItem
import com.starrtc.starrtcsdk.api.XHSDKHelper
import com.starrtc.starrtcsdk.apiInterface.IXHResultCallback
import com.sunny.livechat.R
import com.sunny.livechat.base.BaseActivity
import com.sunny.livechat.base.BaseModel
import com.sunny.livechat.chat.MLOC
import com.sunny.livechat.constant.RefreshLiveListEvent
import com.sunny.livechat.constant.UrlConstant
import com.sunny.livechat.http.ApiManager
import com.sunny.livechat.live.bean.LiveListBean
import com.sunny.livechat.util.ToastUtil
import com.sunny.livechat.util.intent.IntentKey
import kotlinx.android.synthetic.main.activity_live_start.*
import org.greenrobot.eventbus.EventBus

/**
 * Desc 开始直播
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/12/24 14:52
 */
class StartLiveActivity : BaseActivity() {

    private var xhSdkHelper: XHSDKHelper? = null
    private var liveInfoBean = LiveListBean.LiveInfoBean()

    override fun setLayout(): Int = R.layout.activity_live_start

    override fun initTitle(): View? = titleManager.defaultTitle("设备检查")

    override fun initView() {

        xhSdkHelper = XHSDKHelper()
        xhSdkHelper?.setDefaultCameraId(1)
        xhSdkHelper?.startPerview(this, starPlayer)

        et_live_name.setText(intent.getStringExtra(IntentKey.liveName))
        et_live_name.isEnabled = false

        btn_create.setOnClickListener(this)
        iv_switch_camera.setOnClickListener(this)
    }

    override fun onClickEvent(v: View) {
        when (v.id) {
            R.id.btn_create -> {
                xhSdkHelper?.stopPerview()
                xhSdkHelper = null
                startActivity(Intent(this@StartLiveActivity, LiveVideoActivity::class.java))
                finish()
            }
            R.id.iv_switch_camera -> xhSdkHelper?.switchCamera()
        }
    }

    override fun loadData() {}

    override fun close() {}

    override fun onPause() {
        super.onPause()
        xhSdkHelper?.stopPerview()
        xhSdkHelper = null
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

                val json = Gson().toJson(liveInfoBean)

                doCreateLiveApi(json)

            }

            override fun failed(errMsg: String) {
                ToastUtil.show(errMsg)
            }
        })
    }

}