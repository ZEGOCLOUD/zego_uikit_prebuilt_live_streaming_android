package com.zegocloud.uikit.prebuilt.livestreaming.internal.components;

public enum LiveInvitationType {
    REQUEST_COHOST(2), INVITE_TO_COHOST(3), REMOVE_COHOST(4), PK(5);

    private int value;

    LiveInvitationType(int var3) {
        this.value = var3;
    }

    public int getValue() {
        return this.value;
    }

}
