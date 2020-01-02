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
import com.sunny.livechat.MyApplication
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
import com.sunny.livechat.util.intent.IntentKey
import com.sunny.livechat.util.intent.IntentValue
import com.sunny.livechat.util.sp.SpKey
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

    override fun initTitle(): View? = titleManager.defaultTitle("摄像直播")

    override fun initView() {
        iv_live_cover.setOnClickListener(this)
        btn_create.setOnClickListener(this)
    }

    override fun onClickEvent(v: View) {
        when (v.id) {
            R.id.btn_create -> modifyLiveRoomInfo()
            R.id.iv_live_cover -> {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(intent, IntentValue.requestCode)
            }
        }
    }

    override fun loadData() {
        getLiveRoomInfo()
    }

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

    /**
     * 获取直播间信息
     */
    private fun getLiveRoomInfo() {

        ApiManager.post(null, hashMapOf(), UrlConstant.GET_LIVE_ROOM_INFO_URL,
            object : ApiManager.OnResult<BaseModel<LiveListBean.LiveInfoBean>>() {
                override fun onSuccess(model: BaseModel<LiveListBean.LiveInfoBean>) {
                    model.requestResult({
                        liveInfoBean = model.data ?: return@requestResult
                        if (MLOC.userId == model.data?.creator) {
                            btn_create.text = "开始直播"
                            et_live_name.setText(model.data?.liveName)
                            et_live_notice.setText(model.data?.liveNotice)
                            et_live_name.setSelection(et_live_name.text.toString().length)
                        } else {
                            btn_create.text = "创建直播"
                        }
                    }, {})
                }

                override fun onFailed(code: String, msg: String) {
                    ToastUtil.showInterfaceError(code, msg)
                }
            })
    }

    /**
     * 修改直播间信息
     */
    private fun modifyLiveRoomInfo() {

        liveInfoBean.liveName = et_live_name.text.toString()
        liveInfoBean.liveNotice = et_live_notice.text.toString()

        val json = Gson().toJson(liveInfoBean)

        ApiManager.postJson(null, json.toString(), UrlConstant.SET_LIVE_ROOM_INFO_URL,
            object : ApiManager.OnResult<BaseModel<LiveListBean.LiveInfoBean>>() {
                override fun onSuccess(model: BaseModel<LiveListBean.LiveInfoBean>) {
                    model.requestResult({
                        if (MLOC.userId == liveInfoBean.creator) {
                            comeToLivePage() //开始直播
                        } else {
                            doCreateLiveSDK() //创建直播
                        }
                    }, {})
                }

                override fun onFailed(code: String, msg: String) {
                    ToastUtil.showInterfaceError(code, msg)
                }
            })
    }

    /**
     * 进入直播页
     */
    private fun comeToLivePage() {
        MyApplication.getInstance().putData(SpKey.liveInfoBean, liveInfoBean)
        val intent = Intent(this@CreateLiveActivity, StartLiveActivity::class.java)
        intent.putExtra(IntentKey.liveName, et_live_name.text.toString())
        startActivity(intent)
        finish()
    }


    /**
     * 创建直播间，走SDK
     */
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

    /**
     * 创建直播间，走API接口
     */
    private fun doCreateLiveApi(json: String) {

        ApiManager.postJson(null, json, UrlConstant.CREATE_LIVE_ROOM_URL,
            object : ApiManager.OnResult<BaseModel<LiveListBean>>() {
                override fun onSuccess(model: BaseModel<LiveListBean>) {
                    model.requestResult({
                        EventBus.getDefault().post(RefreshLiveListEvent())
                        ToastUtil.show("创建成功")
                        comeToLivePage()
                    }, {})
                }

                override fun onFailed(code: String, msg: String) {
                    ToastUtil.showInterfaceError(code, msg)
                }
            })
    }

}