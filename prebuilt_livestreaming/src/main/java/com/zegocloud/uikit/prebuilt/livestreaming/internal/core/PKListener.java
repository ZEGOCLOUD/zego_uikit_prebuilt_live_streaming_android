package com.zegocloud.uikit.prebuilt.livestreaming.internal.core;

import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

public interface PKListener {

    default void onPKStarted() {
    }

    default void onPKEnded() {
    }

    default void onIncomingPKBattleRequestReceived(String requestID, ZegoUIKitUser anotherHostUser,
        String anotherHostLiveID, String customData) {
    }

    default void onIncomingPKBattleRequestTimeout(String requestID, ZegoUIKitUser anotherHostUser) {
    }

    default void onOutgoingPKBattleRequestTimeout(String requestID, ZegoUIKitUser anotherHost) {
    }

    default void onIncomingPKBattleRequestCanceled(String requestID, ZegoUIKitUser anotherHostUser, String customData) {
    }

    default void onOutgoingPKBattleRequestAccepted(String anotherHostLiveID, ZegoUIKitUser anotherHostUser) {

    }

    default void onOutgoingPKBattleRequestRejected(int reason, ZegoUIKitUser anotherHostUser) {
    }

    default void onOtherHostCameraOpen(String userID, boolean open) {
    }

    default void onOtherHostMicrophoneOpen(String userID, boolean open) {

    }

    default void onOtherHostMuted(String userID, boolean muted) {

    }

    default void onPKUserDisConnected(String userID, boolean disconnected) {
    }
}
