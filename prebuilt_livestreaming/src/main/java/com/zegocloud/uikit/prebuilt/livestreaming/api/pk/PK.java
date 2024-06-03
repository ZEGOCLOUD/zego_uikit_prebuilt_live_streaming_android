package com.zegocloud.uikit.prebuilt.livestreaming.api.pk;

import com.zegocloud.uikit.prebuilt.livestreaming.ZegoLiveStreamingManager;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.core.PKService.PKInfo;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.core.PKService.PKRequest;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.core.UserRequestCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

public class PK {

    public Events events = new Events();

    /**
     * Accept an incoming PK battle request.
     *
     * @param requestID         The ID of the PK invitation, indicating agreement to which PK invitation.
     * @param anotherHostLiveID The Live ID of the other host participating in the PK.
     * @param anotherHostUser   Information of the other host participating in the PK.
     */
    public void acceptIncomingPKBattleRequest(String requestID, String anotherHostLiveID,
        ZegoUIKitUser anotherHostUser) {
        ZegoLiveStreamingManager.getInstance()
            .acceptIncomingPKBattleRequest(requestID, anotherHostLiveID, anotherHostUser);
    }

    /**
     * Reject a PK battle start request.
     *
     * @param requestID The ID of the PK invitation, indicating rejection of which PK invitation.
     */
    public void rejectPKBattleStartRequest(String requestID) {
        ZegoLiveStreamingManager.getInstance().rejectPKBattleStartRequest(requestID);
    }

    public boolean isAnotherHostMuted() {
        return ZegoLiveStreamingManager.getInstance().isAnotherHostMuted();
    }

    /**
     * Mute the audio of the other host in the mixed stream.
     *
     * @param mute     Whether to mute the audio of the other host in the mixed stream.
     * @param callback The callback for the result of the operation.
     */
    public void muteAnotherHostAudio(boolean mute, ZegoUIKitCallback callback) {
        ZegoLiveStreamingManager.getInstance().muteAnotherHostAudio(mute, callback);
    }

    /**
     * Start a PK battle with another host.
     *
     * @param anotherHostLiveID The Live ID of the other host participating in the PK.
     * @param anotherHostUserID The User ID of the other host participating in the PK.
     * @param anotherHostName   The name of the other host participating in the PK.
     */
    public void startPKBattleWith(String anotherHostLiveID, String anotherHostUserID, String anotherHostName) {
        ZegoLiveStreamingManager.getInstance().startPKBattleWith(anotherHostLiveID, anotherHostUserID, anotherHostName);
    }

    public PKRequest getSendPKStartRequest() {
        return ZegoLiveStreamingManager.getInstance().getSendPKStartRequest();
    }

    /**
     * Send a PK battle request.
     *
     * @param anotherHostUserID The User ID of the other host participating in the PK.
     * @param timeout           The timeout period.
     * @param customData        Custom data for transmission.
     * @param callback          The listener for the result of sending the invitation.
     */
    public void sendPKBattleRequest(String anotherHostUserID, int timeout, String customData,
        UserRequestCallback callback) {
        ZegoLiveStreamingManager.getInstance().sendPKBattleRequest(anotherHostUserID, timeout, customData, callback);
    }

    /**
     * Send a PK battle request with default parameters.
     *
     * @param anotherHostUserID The User ID of the other host participating in the PK.
     * @param callback          The listener for the result of sending the invitation.
     */
    public void sendPKBattleRequest(String anotherHostUserID, UserRequestCallback callback) {
        ZegoLiveStreamingManager.getInstance().sendPKBattleRequest(anotherHostUserID, 60, "", callback);
    }

    public void cancelPKBattleRequest(UserRequestCallback callback) {
        ZegoLiveStreamingManager.getInstance().cancelPKBattleRequest("", callback);
    }

    /**
     * Cancel a PK battle request.
     *
     * @param customData Custom data for transmission.
     * @param callback   The listener for the result of canceling the invitation.
     */
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