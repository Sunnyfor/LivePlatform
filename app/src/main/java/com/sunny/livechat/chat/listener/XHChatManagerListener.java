package com.sunny.livechat.chat.listener;

import com.orhanobut.logger.Logger;
import com.starrtc.starrtcsdk.apiInterface.IXHChatManagerListener;
import com.starrtc.starrtcsdk.core.im.message.XHIMMessage;
import com.sunny.livechat.chat.AEvent;
import com.sunny.livechat.chat.database.CoreDB;
import com.sunny.livechat.chat.database.HistoryBean;
import com.sunny.livechat.chat.database.MessageBean;
import com.sunny.livechat.chat.MLOC;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class XHChatManagerListener implements IXHChatManagerListener {
    @Override
    public void onReceivedMessage(XHIMMessage message) {

        HistoryBean historyBean = new HistoryBean();
        historyBean.setType(CoreDB.HISTORY_TYPE_C2C);
        historyBean.setLastTime(new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(new java.util.Date()));
        historyBean.setLastMsg(message.contentData);
        historyBean.setConversationId(message.fromId);
        historyBean.setNewMsgCount(1);
        MLOC.addHistory(historyBean, false);

        MessageBean messageBean = new MessageBean();
        messageBean.setConversationId(message.fromId);
        messageBean.setTime(new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(new java.util.Date()));
        messageBean.setMsg(message.contentData);
        messageBean.setFromId(message.fromId);
        MLOC.saveMessage(messageBean);

        AEvent.notifyListener(AEvent.AEVENT_C2C_REV_MSG, true, message);

    }

    @Override
    public void onReceivedSystemMessage(XHIMMessage xhimMessage) {
        Logger.i("IM系统信息:" + xhimMessage);
    }
}