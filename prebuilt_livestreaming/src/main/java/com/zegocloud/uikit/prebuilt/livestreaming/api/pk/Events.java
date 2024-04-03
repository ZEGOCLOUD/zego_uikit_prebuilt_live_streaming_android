package com.zegocloud.uikit.prebuilt.livestreaming.api.pk;

import com.zegocloud.uikit.prebuilt.livestreaming.ZegoLiveStreamingManager;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.core.PKListener;

public class Events {

    private PKListener listener;

    public void setPKListener(PKListener listener) {
        if (this.listener != null) {
            ZegoLiveStreamingManager.getInstance().removePKListener(this.listener);
        }
        this.listener = listener;
        if (listener != null) {
            ZegoLiveStreamingManager.getInstance().addPKListener(listener);
        }
    }
}
