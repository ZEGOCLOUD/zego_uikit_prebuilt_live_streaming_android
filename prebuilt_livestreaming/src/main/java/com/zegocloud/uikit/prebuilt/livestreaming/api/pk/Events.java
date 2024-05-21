package com.zegocloud.uikit.prebuilt.livestreaming.api.pk;

import com.zegocloud.uikit.prebuilt.livestreaming.ZegoLiveStreamingManager;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.core.PKListener;

public class Events {

    public void addPKListener(PKListener listener) {
        ZegoLiveStreamingManager.getInstance().addPKListener(listener);
    }

    public void removePKListener(PKListener listener) {
        ZegoLiveStreamingManager.getInstance().removePKListener(listener);
    }
}
