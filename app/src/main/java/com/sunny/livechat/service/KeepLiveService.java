package com.sunny.livechat.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;
import com.starrtc.starrtcsdk.api.XHClient;
import com.starrtc.starrtcsdk.api.XHCustomConfig;
import com.starrtc.starrtcsdk.apiInterface.IXHErrorCallback;
import com.starrtc.starrtcsdk.apiInterface.IXHResultCallback;
import com.starrtc.starrtcsdk.core.videosrc.XHVideoSourceManager;
import com.sunny.livechat.chat.AEvent;
import com.sunny.livechat.chat.IChatListener;
import com.sunny.livechat.chat.MLOC;
import com.sunny.livechat.chat.beauty.DemoVideoSourceCallback;
import com.sunny.livechat.chat.listener.XHChatManagerListener;
import com.sunny.livechat.chat.listener.XHGroupManagerListener;
import com.sunny.livechat.chat.listener.XHLoginManagerListener;
import com.sunny.livechat.chat.listener.XHVoipManagerListener;
import com.sunny.livechat.chat.listener.XHVoipP2PManagerListener;

/**
 * Desc 直播、聊天、消息收发服务
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date
 */
public class KeepLiveService extends Service implements IChatListener {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.i("IM服务销毁");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initFree();
        return super.onStartCommand(intent, flags, startId);
    }


    //开放版SDK初始化
    private void initFree() {
        Logger.i("IM初始化,UserID:" + MLOC.userId);
        XHCustomConfig customConfig = XHCustomConfig.getInstance(this);
        customConfig.setChatroomServerUrl(MLOC.CHATROOM_SERVER_URL);
        customConfig.setLiveSrcServerUrl(MLOC.LIVE_SRC_SERVER_URL);
        customConfig.setLiveVdnServerUrl(MLOC.LIVE_VDN_SERVER_URL);
        customConfig.setImServerUrl(MLOC.IM_SERVER_URL);
        customConfig.setVoipServerUrl(MLOC.VOIP_SERVER_URL);

        customConfig.initSDKForFreeWithoutAudioCheck(MLOC.userId, new IXHErrorCallback() {
            @Override
            public void error(final String errMsg, Object data) {
                Logger.i("IM错误消息：" + errMsg);
            }
        }, new Handler());
        customConfig.setDefConfigOpenGLESEnable(false);

        XHClient.getInstance().getChatManager().addListener(new XHChatManagerListener());
        XHClient.getInstance().getGroupManager().addListener(new XHGroupManagerListener());
        XHClient.getInstance().getVoipManager().addListener(new XHVoipManagerListener());
        XHClient.getInstance().getVoipP2PManager().addListener(new XHVoipP2PManagerListener());
        XHClient.getInstance().getLoginManager().addListener(new XHLoginManagerListener());
        XHVideoSourceManager.getInstance().setVideoSourceCallback(new DemoVideoSourceCallback());

        XHClient.getInstance().getLoginManager().loginFree(new IXHResultCallback() {
            @Override
            public void success(Object data) {
                addListener();
                Logger.i("IM登录成功");
            }

            @Override
            public void failed(final String errMsg) {
                Logger.i("IM登录失败");
                MLOC.showMsg(KeepLiveService.this, errMsg);
            }
        });

    }


    @Override
    public void dispatchEvent(String aEventID, boolean success, Object eventObj) {
        switch (aEventID) {
            case AEvent.AEVENT_C2C_REV_MSG:
                MLOC.hasNewC2CMsg = true;
                break;
            case AEvent.AEVENT_GROUP_REV_MSG:
                MLOC.hasNewGroupMsg = true;
                break;
            case AEvent.AEVENT_LOGOUT:
                removeListener();
                this.stopSelf();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                        startActivity(LaunchIntent);
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                },100);

                break;
        }
    }


    private void addListener() {
        AEvent.addListener(AEvent.AEVENT_LOGOUT, this);
        AEvent.addListener(AEvent.AEVENT_VOIP_REV_CALLING, this);
        AEvent.addListener(AEvent.AEVENT_VOIP_REV_CALLING_AUDIO, this);
        AEvent.addListener(AEvent.AEVENT_VOIP_P2P_REV_CALLING, this);
        AEvent.addListener(AEvent.AEVENT_C2C_REV_MSG, this);
        AEvent.addListener(AEvent.AEVENT_GROUP_REV_MSG, this);
    }

    private void removeListener() {
        AEvent.removeListener(AEvent.AEVENT_LOGOUT, this);
        AEvent.removeListener(AEvent.AEVENT_VOIP_REV_CALLING, this);
        AEvent.removeListener(AEvent.AEVENT_VOIP_REV_CALLING_AUDIO, this);
        AEvent.removeListener(AEvent.AEVENT_VOIP_P2P_REV_CALLING, this);
        AEvent.removeListener(AEvent.AEVENT_C2C_REV_MSG, this);
        AEvent.removeListener(AEvent.AEVENT_GROUP_REV_MSG, this);
    }

}