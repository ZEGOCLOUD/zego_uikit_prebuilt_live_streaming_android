package com.zegocloud.uikit.prebuilt.livestreaming.core;

import java.io.Serializable;

public class ZegoDialogInfo implements Serializable {

    public String title;
    public String message;
    public String cancelButtonName;
    public String confirmButtonName;

    public ZegoDialogInfo() {
        this.title = "";
        this.message = "";
        this.cancelButtonName = "";
        this.confirmButtonName = "";
    }

    public ZegoDialogInfo(String title, String message, String cancelButtonName, String confirmButtonName) {
        this.title = title;
        this.message = message;
        this.cancelButtonName = cancelButtonName;
        this.confirmButtonName = confirmButtonName;
    }
}
