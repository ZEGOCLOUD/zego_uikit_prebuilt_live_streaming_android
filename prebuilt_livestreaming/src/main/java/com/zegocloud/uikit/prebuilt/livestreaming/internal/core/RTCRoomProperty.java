package com.zegocloud.uikit.prebuilt.livestreaming.internal.core;

import com.zegocloud.uikit.ZegoUIKit;
import java.util.Map;

public class RTCRoomProperty {

    public static final String LIVE_STATUS = "live_status";
    public static final String LIVE_STATUS_START = "1";
    public static final String LIVE_STATUS_STOP = "0";
    public static final String ENABLE_CHAT = "enableChat";
    public static final String ENABLE_CHAT_ENABLE = "1";
    public static final String ENABLE_CHAT_DISABLE = "0";
    public static final String HOST = "host";
    public static final String HOST_REMOVE = "";

    public void updateRoomProperties(Map<String, String> newProperties) {
        ZegoUIKit.updateRoomProperties(newProperties);
    }

    public Map<String, String> getRoomProperties() {
        return ZegoUIKit.getRoomProperties();
    }

    public boolean isLiveStarted() {
        return LIVE_STATUS_START.equals(getRoomProperties().get(LIVE_STATUS));
    }

    public String getHostID() {
        return getRoomProperties().get(HOST);
    }
}
