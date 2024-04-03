package com.zegocloud.uikit.prebuilt.livestreaming.api.common;

import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.service.express.IExpressEngineEventHandler;

public class Events {

    private IExpressEngineEventHandler expressEngineEventHandler;

    public void setExpressEngineEventHandler(IExpressEngineEventHandler eventHandler) {
        if (this.expressEngineEventHandler != null) {
            ZegoUIKit.removeEventHandler(this.expressEngineEventHandler);
        }
        this.expressEngineEventHandler = eventHandler;
        if (eventHandler != null) {
            ZegoUIKit.addEventHandler(eventHandler);
        }
    }
}
