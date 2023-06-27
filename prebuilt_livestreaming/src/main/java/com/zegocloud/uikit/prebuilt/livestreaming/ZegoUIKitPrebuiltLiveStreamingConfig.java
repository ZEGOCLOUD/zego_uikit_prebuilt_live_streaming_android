package com.zegocloud.uikit.prebuilt.livestreaming;

import android.view.View;
import com.zegocloud.uikit.components.audiovideo.ZegoAvatarViewProvider;
import com.zegocloud.uikit.components.audiovideocontainer.ZegoLayout;
import com.zegocloud.uikit.components.common.ZegoPresetResolution;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoBottomMenuBarConfig;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoDialogInfo;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoLiveStreamingEndListener;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoLiveStreamingRole;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoMemberListConfig;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoMenuBarButtonName;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoPrebuiltAudioVideoViewConfig;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoPrebuiltVideoConfig;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoTranslationText;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.ZegoLeaveLiveStreamingListener;
import com.zegocloud.uikit.prebuilt.livestreaming.widget.ZegoStartLiveButton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class ZegoUIKitPrebuiltLiveStreamingConfig {

    public ZegoLiveStreamingRole role = ZegoLiveStreamingRole.AUDIENCE;
    public boolean turnOnCameraWhenJoining = false;
    public boolean turnOnMicrophoneWhenJoining = false;
    public boolean useSpeakerWhenJoining = true;
    public ZegoPrebuiltAudioVideoViewConfig audioVideoViewConfig = new ZegoPrebuiltAudioVideoViewConfig();
    public ZegoBottomMenuBarConfig bottomMenuBarConfig = new ZegoBottomMenuBarConfig(
        Arrays.asList(ZegoMenuBarButtonName.TOGGLE_CAMERA_BUTTON, ZegoMenuBarButtonName.TOGGLE_MICROPHONE_BUTTON,
            ZegoMenuBarButtonName.SWITCH_CAMERA_FACING_BUTTON),
        Arrays.asList(ZegoMenuBarButtonName.TOGGLE_CAMERA_BUTTON, ZegoMenuBarButtonName.TOGGLE_MICROPHONE_BUTTON,
            ZegoMenuBarButtonName.SWITCH_CAMERA_FACING_BUTTON, ZegoMenuBarButtonName.COHOST_CONTROL_BUTTON),
        Collections.singletonList(ZegoMenuBarButtonName.COHOST_CONTROL_BUTTON));
    public ZegoMemberListConfig memberListConfig;
    public ZegoDialogInfo confirmDialogInfo;
    public transient ZegoLiveStreamingEndListener zegoLiveStreamingEndListener;
    public transient ZegoLeaveLiveStreamingListener leaveLiveStreamingListener;
    public ZegoTranslationText translationText = new ZegoTranslationText();
    private boolean enableCoHosting;
    public boolean markAsLargeRoom = false;
    public boolean needConfirmWhenOthersTurnOnYourCamera = false;
    public boolean needConfirmWhenOthersTurnOnYourMicrophone = false;
    public ZegoDialogInfo othersTurnOnYourCameraConfirmDialogInfo;
    public ZegoDialogInfo othersTurnOnYourMicrophoneConfirmDialogInfo;
    public transient ZegoStartLiveButton startLiveButton;
    public transient View.OnClickListener onStartLiveButtonPressed;
    public ZegoLayout zegoLayout;
    public ZegoPrebuiltVideoConfig screenSharingVideoConfig = new ZegoPrebuiltVideoConfig(
        ZegoPresetResolution.PRESET_540P);
    public ZegoPrebuiltVideoConfig videoConfig = new ZegoPrebuiltVideoConfig(ZegoPresetResolution.PRESET_360P);
    public transient ZegoAvatarViewProvider avatarViewProvider;



    public static ZegoUIKitPrebuiltLiveStreamingConfig host() {
        return host(false);
    }

    public static ZegoUIKitPrebuiltLiveStreamingConfig host(boolean enableCoHosting) {
        ZegoUIKitPrebuiltLiveStreamingConfig config = new ZegoUIKitPrebuiltLiveStreamingConfig();
        config.role = ZegoLiveStreamingRole.HOST;
        config.turnOnCameraWhenJoining = true;
        config.turnOnMicrophoneWhenJoining = true;
        config.confirmDialogInfo = new ZegoDialogInfo("Stop the live", "Are you sure to stop the live?", "Cancel",
            "Stop it");
        if (enableCoHosting) {
            config.bottomMenuBarConfig.audienceButtons = Collections.singletonList(
                ZegoMenuBarButtonName.COHOST_CONTROL_BUTTON);
        } else {
            config.bottomMenuBarConfig.audienceButtons = new ArrayList<>();
        }
        config.enableCoHosting = enableCoHosting;
        return config;
    }

    public static ZegoUIKitPrebuiltLiveStreamingConfig audience() {
        return audience(false);
    }

    public static ZegoUIKitPrebuiltLiveStreamingConfig audience(boolean enableCoHosting) {
        ZegoUIKitPrebuiltLiveStreamingConfig config = new ZegoUIKitPrebuiltLiveStreamingConfig();
        config.role = ZegoLiveStreamingRole.AUDIENCE;
        if (enableCoHosting) {
            config.bottomMenuBarConfig.audienceButtons = Collections.singletonList(
                ZegoMenuBarButtonName.COHOST_CONTROL_BUTTON);
        } else {
            config.bottomMenuBarConfig.audienceButtons = new ArrayList<>();
        }
        config.enableCoHosting = enableCoHosting;
        return config;
    }

    public boolean isEnableCoHosting() {
        return enableCoHosting;
    }
}
