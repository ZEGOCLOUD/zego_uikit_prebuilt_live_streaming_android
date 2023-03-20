package com.zegocloud.uikit.prebuilt.livestreaming.core;

import java.util.ArrayList;
import java.util.List;

public class ZegoBottomMenuBarConfig {

    public List<ZegoMenuBarButtonName> hostButtons = new ArrayList<>();
    public List<ZegoMenuBarButtonName> coHostButtons = new ArrayList<>();
    public List<ZegoMenuBarButtonName> audienceButtons = new ArrayList<>();
    public int menuBarButtonsMaxCount = 4;
    public boolean showInRoomMessageButton = true;

    public ZegoBottomMenuBarConfig() {

    }

    public ZegoBottomMenuBarConfig(
        List<ZegoMenuBarButtonName> hostButtons,
        List<ZegoMenuBarButtonName> coHostButtons,
        List<ZegoMenuBarButtonName> audienceButtons) {
        this.hostButtons = hostButtons;
        this.coHostButtons = coHostButtons;
        this.audienceButtons = audienceButtons;
    }
}
