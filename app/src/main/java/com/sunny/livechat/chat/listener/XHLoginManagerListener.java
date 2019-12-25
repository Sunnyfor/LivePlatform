package com.sunny.livechat.chat.listener;

import com.orhanobut.logger.Logger;
import com.starrtc.starrtcsdk.api.XHConstants;
import com.starrtc.starrtcsdk.apiInterface.IXHLoginManagerListener;
import com.sunny.livechat.chat.AEvent;
import com.sunny.livechat.constant.Constant;
import com.sunny.livechat.util.sp.SpUtil;

public class XHLoginManagerListener implements IXHLoginManagerListener {

    @Override
    public void onConnectionStateChanged(XHConstants.XHSDKConnectionState state) {
        /**
         * 1.无网、网络信号差
         * 2.其他端挤掉线
         * 3.服务器异常
         */
        if (state == XHConstants.XHSDKConnectionState.SDKConnectionStateDisconnect) {
//            Logger.i("IM用户状态：离线");
            AEvent.notifyListener(AEvent.AEVENT_USER_OFFLINE, true, "");
            Constant.INSTANCE.setAutoLogin(false);
            SpUtil.INSTANCE.logout();
        } else if (state == XHConstants.XHSDKConnectionState.SDKConnectionStateReconnect) {

//            Logger.i("IM用户状态：在线");
            AEvent.notifyListener(AEvent.AEVENT_USER_ONLINE, true, "");
        }
    }

    @Override
    public void onKickedByOtherDeviceLogin() {
        AEvent.notifyListener(AEvent.AEVENT_USER_KICKED, true, "");
        Logger.i("IM用户状态：其他设备登录");
    }

    @Override
    public void onLogout() {
        Logger.i("IM用户状态：退出");
    }
}
