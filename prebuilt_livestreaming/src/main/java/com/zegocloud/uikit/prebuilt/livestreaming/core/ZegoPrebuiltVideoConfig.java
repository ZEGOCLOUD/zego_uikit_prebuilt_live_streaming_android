package com.zegocloud.uikit.prebuilt.livestreaming.core;

import com.zegocloud.uikit.components.common.ZegoPresetResolution;

public class ZegoPrebuiltVideoConfig {

    public ZegoPresetResolution resolution = ZegoPresetResolution.PRESET_360P;

    public ZegoPrebuiltVideoConfig(ZegoPresetResolution resolution) {
        this.resolution = resolution;
    }

    public ZegoPrebuiltVideoConfig() {
    }
}
