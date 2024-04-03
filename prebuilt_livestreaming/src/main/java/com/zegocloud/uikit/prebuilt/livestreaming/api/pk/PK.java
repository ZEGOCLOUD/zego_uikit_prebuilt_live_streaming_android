package com.zegocloud.uikit.prebuilt.livestreaming.api.pk;

import com.zegocloud.uikit.prebuilt.livestreaming.ZegoLiveStreamingManager;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.core.PKService.PKInfo;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.core.PKService.PKRequest;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.core.UserRequestCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

public class PK {

    public Events events = new Events();

    public void acceptIncomingPKBattleRequest(String requestID, String anotherHostLiveID,
        ZegoUIKitUser anotherHostUser) {
        ZegoLiveStreamingManager.getInstance()
            .acceptIncomingPKBattleRequest(requestID, anotherHostLiveID, anotherHostUser, "");
    }

    public void acceptIncomingPKBattleRequest(String requestID, String anotherHostLiveID, ZegoUIKitUser anotherHostUser,
        String customData) {
        ZegoLiveStreamingManager.getInstance()
            .acceptIncomingPKBattleRequest(requestID, anotherHostLiveID, anotherHostUser, customData);
    }

    public void rejectPKBattleStartRequest(String requestID) {
        ZegoLiveStreamingManager.getInstance().rejectPKBattleStartRequest(requestID);
    }

    public boolean isAnotherHostMuted() {
        return ZegoLiveStreamingManager.getInstance().isAnotherHostMuted();
    }

    public void muteAnotherHostAudio(boolean mute, ZegoUIKitCallback callback) {
        ZegoLiveStreamingManager.getInstance().muteAnotherHostAudio(mute, callback);
    }

    public void startPKBattleWith(String anotherHostLiveID, String anotherHostUserID, String anotherHostName) {
        ZegoLiveStreamingManager.getInstance().startPKBattleWith(anotherHostLiveID, anotherHostUserID, anotherHostName);
    }

    public PKRequest getSendPKStartRequest() {
        return ZegoLiveStreamingManager.getInstance().getSendPKStartRequest();
    }

    public void sendPKBattleRequest(String anotherHostUserID, int timeout, String customData,
        UserRequestCallback callback) {
        ZegoLiveStreamingManager.getInstance().sendPKBattleRequest(anotherHostUserID, timeout, customData, callback);
    }

    public void sendPKBattleRequest(String anotherHostUserID, UserRequestCallback callback) {
        ZegoLiveStreamingManager.getInstance().sendPKBattleRequest(anotherHostUserID, 60, "", callback);
    }

    public void cancelPKBattleRequest(UserRequestCallback callback) {
        ZegoLiveStreamingManager.getInstance().cancelPKBattleRequest("", callback);
    }

    public void cancelPKBattleRequest(String customData, UserRequestCallback callback) {
        ZegoLiveStreamingManager.getInstance().cancelPKBattleRequest(customData, callback);
    }

    public void stopPKBattle() {
        ZegoLiveStreamingManager.getInstance().stopPKBattle();
    }

    public PKInfo getPKInfo() {
        return ZegoLiveStreamingManager.getInstance().getPKInfo();
    }

}
