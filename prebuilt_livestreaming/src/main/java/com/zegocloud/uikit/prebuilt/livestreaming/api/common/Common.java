package com.zegocloud.uikit.prebuilt.livestreaming.api.common;

import com.zegocloud.uikit.prebuilt.livestreaming.ZegoLiveStreamingManager;

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
}
