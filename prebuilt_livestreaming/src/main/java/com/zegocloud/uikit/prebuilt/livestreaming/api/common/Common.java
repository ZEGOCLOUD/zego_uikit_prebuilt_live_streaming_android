package com.zegocloud.uikit.prebuilt.livestreaming.api.common;

import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoLiveStreamingManager;
import com.zegocloud.uikit.service.defines.ZegoInRoomMessageSendStateListener;

public class Common {

    public Events events = new Events();

    public void unMuteAllAudioVideo() {
        ZegoLiveStreamingManager.getInstance().unMuteAllAudioVideo();
    }

    public void muteAllAudioVideo() {
        ZegoLiveStreamingManager.getInstance().muteAllAudioVideo();
    }

    public void showTopTips(String message, boolean green) {
        ZegoLiveStreamingManager.getInstance().showTopTips(message, green);
    }

    public void sendInRoomMessage(String message, ZegoInRoomMessageSendStateListener listener) {
        ZegoUIKit.sendInRoomMessage(message, listener);
    }
}
