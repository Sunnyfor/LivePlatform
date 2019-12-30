package com.sunny.livechat.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import com.orhanobut.logger.Logger
import com.starrtc.starrtcsdk.api.XHClient
import com.starrtc.starrtcsdk.api.XHConstants
import com.starrtc.starrtcsdk.api.XHCustomConfig
import com.starrtc.starrtcsdk.apiInterface.IXHResultCallback
import com.starrtc.starrtcsdk.core.pusher.XHCameraRecorder
import com.starrtc.starrtcsdk.core.videosrc.XHVideoSourceManager
import com.sunny.livechat.chat.AEvent
import com.sunny.livechat.chat.IChatListener
import com.sunny.livechat.chat.MLOC
import com.sunny.livechat.chat.beauty.DemoVideoSourceCallback
import com.sunny.livechat.chat.listener.*

/**
 * Desc 直播、聊天、消息收发服务
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019年12月26日 15:20:08
 */
class KeepLiveService : Service(), IChatListener {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Logger.i("IM服务创建")
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.i("IM服务销毁")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        initFree()
        return super.onStartCommand(intent, flags, startId)
    }

    //开放版SDK初始化
    private fun initFree() {
        Logger.i("IM初始化,UserID:" + MLOC.userId)

        val liveManager = XHClient.getInstance().getLiveManager(this)
        liveManager?.setRtcMediaType(XHConstants.XHRtcMediaTypeEnum.STAR_RTC_MEDIA_TYPE_VIDEO_AND_AUDIO)
        liveManager?.setRecorder(XHCameraRecorder())
        liveManager?.addListener(XHLiveManagerListener())

        val customConfig = XHCustomConfig.getInstance(this)
        customConfig?.chatroomServerUrl = MLOC.CHATROOM_SERVER_URL
        customConfig?.liveSrcServerUrl = MLOC.LIVE_SRC_SERVER_URL
        customConfig?.liveVdnServerUrl = MLOC.LIVE_VDN_SERVER_URL
        customConfig?.setImServerUrl(MLOC.IM_SERVER_URL)
        customConfig?.voipServerUrl = MLOC.VOIP_SERVER_URL
        customConfig?.initSDKForFreeWithoutAudioCheck(MLOC.userId, { errMsg, _ -> Logger.i("IM错误消息：$errMsg") }, Handler())
        customConfig?.setDefConfigOpenGLESEnable(false)

        val xhClient = XHClient.getInstance()
        xhClient?.chatManager?.addListener(XHChatManagerListener())
        xhClient?.groupManager?.addListener(XHGroupManagerListener())
        xhClient?.voipManager?.addListener(XHVoipManagerListener())
        xhClient?.voipP2PManager?.addListener(XHVoipP2PManagerListener())
        xhClient?.loginManager?.addListener(XHLoginManagerListener())

        XHVideoSourceManager.getInstance().videoSourceCallback = DemoVideoSourceCallback()

        xhClient?.loginManager?.loginFree(object : IXHResultCallback {
            override fun success(data: Any?) {
                addListener()
                Logger.i("IM登录成功")
            }

            override fun failed(errMsg: String) {
                Logger.i("IM登录失败")
                Logger.e(errMsg)
            }
        })

    }


    override fun dispatchEvent(aEventID: String, success: Boolean, eventObj: Any) {
        when (aEventID) {
            AEvent.AEVENT_C2C_REV_MSG -> MLOC.hasNewC2CMsg = true
            AEvent.AEVENT_GROUP_REV_MSG -> MLOC.hasNewGroupMsg = true
            AEvent.AEVENT_LOGOUT -> {
                removeListener()
                this.stopSelf()
                Handler().postDelayed({
                    startActivity(packageManager.getLaunchIntentForPackage(packageName))
                    android.os.Process.killProcess(android.os.Process.myPid())
                }, 100)
            }
        }
    }

    private fun addListener() {
        AEvent.addListener(AEvent.AEVENT_LOGOUT, this)
        AEvent.addListener(AEvent.AEVENT_VOIP_REV_CALLING, this)
        AEvent.addListener(AEvent.AEVENT_VOIP_REV_CALLING_AUDIO, this)
        AEvent.addListener(AEvent.AEVENT_VOIP_P2P_REV_CALLING, this)
        AEvent.addListener(AEvent.AEVENT_C2C_REV_MSG, this)
        AEvent.addListener(AEvent.AEVENT_GROUP_REV_MSG, this)
    }

    private fun removeListener() {
        AEvent.removeListener(AEvent.AEVENT_LOGOUT, this)
        AEvent.removeListener(AEvent.AEVENT_VOIP_REV_CALLING, this)
        AEvent.removeListener(AEvent.AEVENT_VOIP_REV_CALLING_AUDIO, this)
        AEvent.removeListener(AEvent.AEVENT_VOIP_P2P_REV_CALLING, this)
        AEvent.removeListener(AEvent.AEVENT_C2C_REV_MSG, this)
        AEvent.removeListener(AEvent.AEVENT_GROUP_REV_MSG, this)
    }

}