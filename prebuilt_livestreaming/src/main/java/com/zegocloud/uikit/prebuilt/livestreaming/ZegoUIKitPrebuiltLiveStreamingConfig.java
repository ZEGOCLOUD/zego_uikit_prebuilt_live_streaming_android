package com.zegocloud.uikit.prebuilt.livestreaming;

import android.view.View;
import com.zegocloud.uikit.components.audiovideo.ZegoAvatarViewProvider;
import com.zegocloud.uikit.components.audiovideocontainer.ZegoLayout;
import com.zegocloud.uikit.components.common.ZegoPresetResolution;
import com.zegocloud.uikit.plugin.adapter.plugins.beauty.ZegoBeautyPluginConfig;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoBottomMenuBarConfig;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoDialogInfo;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoLiveStreamingEndListener;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoLiveStreamingPKBattleConfig;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoLiveStreamingRole;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoMemberListConfig;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoMenuBarButtonName;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoPrebuiltAudioVideoViewConfig;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoPrebuiltVideoConfig;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoTranslationText;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.components.ZegoLeaveLiveStreamingListener;
import com.zegocloud.uikit.prebuilt.livestreaming.widget.ZegoStartLiveButton;
import com.zegocloud.uikit.service.defines.ZegoMeRemovedFromRoomListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class ZegoUIKitPrebuiltLiveStreamingConfig {

    public ZegoLiveStreamingRole role = ZegoLiveStreamingRole.AUDIENCE;
    public boolean turnOnCameraWhenJoining = false;
    public boolean turnOnMicrophoneWhenJoining = false;
    public boolean turnOnCameraWhenCohosted = true;
    public boolean useSpeakerWhenJoining = true;
    public ZegoPrebuiltAudioVideoViewConfig audioVideoViewConfig = new ZegoPrebuiltAudioVideoViewConfig();
    public ZegoBottomMenuBarConfig bottomMenuBarConfig = new ZegoBottomMenuBarConfig(new ArrayList<>(
        Arrays.asList(ZegoMenuBarButtonName.TOGGLE_CAMERA_BUTTON, ZegoMenuBarButtonName.TOGGLE_MICROPHONE_BUTTON,
            ZegoMenuBarButtonName.SWITCH_CAMERA_FACING_BUTTON)), new ArrayList<>(
        Arrays.asList(ZegoMenuBarButtonName.TOGGLE_CAMERA_BUTTON, ZegoMenuBarButtonName.TOGGLE_MICROPHONE_BUTTON,
            ZegoMenuBarButtonName.SWITCH_CAMERA_FACING_BUTTON, ZegoMenuBarButtonName.COHOST_CONTROL_BUTTON)),
        new ArrayList<>(Collections.singletonList(ZegoMenuBarButtonName.COHOST_CONTROL_BUTTON)));
    public ZegoMemberListConfig memberListConfig;

    /**
     * if confirmDialogInfo is not null,a confirm dialog will show when host stop live or exit button is clicked or
     * back button is pressed. Please use {@link ZegoTranslationText#stopLiveDialogInfo }  to custom stopLiveDialog
     * dialog texts.
     */
    @Deprecated
    public ZegoDialogInfo confirmDialogInfo;
    public transient ZegoLiveStreamingEndListener zegoLiveStreamingEndListener;
    public transient ZegoLeaveLiveStreamingListener leaveLiveStreamingListener;
    public transient ZegoMeRemovedFromRoomListener removedFromRoomListener;
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
    public ZegoLiveStreamingPKBattleConfig pkBattleConfig = new ZegoLiveStreamingPKBattleConfig();
    public ZegoBeautyPluginConfig beautyConfig = new ZegoBeautyPluginConfig();
    public boolean showMemberButton = true;


    public static ZegoUIKitPrebuiltLiveStreamingConfig host() {
        return host(false);
    }

    public static ZegoUIKitPrebuiltLiveStreamingConfig host(boolean enableCoHosting) {
        ZegoUIKitPrebuiltLiveStreamingConfig config = new ZegoUIKitPrebuiltLiveStreamingConfig();
        config.role = ZegoLiveStreamingRole.HOST;
        config.turnOnCameraWhenJoining = true;
        config.turnOnMicrophoneWhenJoining = true;
        config.confirmDialogInfo = new ZegoDialogInfo();
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
