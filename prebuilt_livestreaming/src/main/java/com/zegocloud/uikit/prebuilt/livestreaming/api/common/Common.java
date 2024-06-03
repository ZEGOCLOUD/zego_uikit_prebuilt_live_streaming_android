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

    /**
     *  message show on top UI
     * @param message  message content
     * @param green  message color ,green or red
     */
    public void showTopTips(String message, boolean green) {
        ZegoLiveStreamingManager.getInstance().showTopTips(message, green);
    }

    /**
     *
     * @param message  in room message content
     * @param listener  send message result
     */
    public void sendInRoomMessage(String message, ZegoInRoomMessageSendStateListener listener) {
        ZegoUIKit.sendInRoomMessage(message, listener);
    }
}
