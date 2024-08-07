package com.zegocloud.uikit.prebuilt.livestreaming.api.common;

import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoLiveStreamingManager;
import com.zegocloud.uikit.service.defines.ZegoBarrageMessageListener;
import com.zegocloud.uikit.service.defines.ZegoInRoomMessageListener;
import com.zegocloud.uikit.service.express.IExpressEngineEventHandler;
import im.zego.zegoexpress.callback.IZegoIMSendBarrageMessageCallback;

public class Events {

    public void addExpressEngineEventHandler(IExpressEngineEventHandler eventHandler) {
        ZegoUIKit.addEventHandler(eventHandler);
    }

    public void removeExpressEngineEventHandler(IExpressEngineEventHandler eventHandler) {
        ZegoUIKit.removeEventHandler(eventHandler);
    }

    public void addRoleChangedListener(RoleChangedListener listener) {
        ZegoLiveStreamingManager.getInstance().addRoleListener(listener);
    }

    public void removeRoleChangedListener(RoleChangedListener listener) {
        ZegoLiveStreamingManager.getInstance().removeRoleListener();
    }

    public void addInRoomMessageListener(ZegoInRoomMessageListener inRoomMessageListener) {
        ZegoUIKit.addInRoomMessageReceivedListener(inRoomMessageListener);
    }

    public void removeInRoomMessageListener(ZegoInRoomMessageListener inRoomMessageListener) {
        ZegoUIKit.removeInRoomMessageReceivedListener(inRoomMessageListener);
    }

    public void addBarrageMessageListener(ZegoBarrageMessageListener listener) {
        ZegoUIKit.addBarrageMessageListener(listener);
    }

    public void removeBarrageMessageListener(ZegoBarrageMessageListener listener) {
        ZegoUIKit.removeBarrageMessageListener(listener);
    }
}
