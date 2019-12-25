package com.sunny.livechat.chat;

public interface IChatListener {
    void dispatchEvent(String aEventID, boolean success, Object eventObj);
}