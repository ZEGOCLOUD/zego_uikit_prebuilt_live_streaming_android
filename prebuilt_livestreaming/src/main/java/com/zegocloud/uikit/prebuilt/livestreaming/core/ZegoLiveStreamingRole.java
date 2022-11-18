package com.zegocloud.uikit.prebuilt.livestreaming.core;

public enum ZegoLiveStreamingRole {
    HOST(0), COHOST(1), AUDIENCE(2);

    private int value;

    ZegoLiveStreamingRole(int var3) {
        this.value = var3;
    }

    public int getValue() {
        return this.value;
    }

}
