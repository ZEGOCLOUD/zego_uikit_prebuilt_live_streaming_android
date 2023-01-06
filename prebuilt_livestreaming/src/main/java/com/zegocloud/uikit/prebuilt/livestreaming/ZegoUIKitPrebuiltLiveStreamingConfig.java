package com.zegocloud.uikit.prebuilt.livestreaming;

import android.view.View;
import androidx.annotation.Nullable;
import com.zegocloud.uikit.plugin.common.IZegoUIKitPlugin;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoLiveStreamingEndListener;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoBottomMenuBarConfig;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoDialogInfo;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoLiveStreamingRole;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoMemberListConfig;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoMenuBarButtonName;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoPrebuiltAudioVideoViewConfig;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoTranslationText;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.ZegoLeaveLiveStreamingListener;
import com.zegocloud.uikit.prebuilt.livestreaming.widget.ZegoStartLiveButton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ZegoUIKitPrebuiltLiveStreamingConfig {

    public ZegoLiveStreamingRole role = ZegoLiveStreamingRole.AUDIENCE;
    public boolean turnOnCameraWhenJoining = false;
    public boolean turnOnMicrophoneWhenJoining = false;
    public boolean useSpeakerWhenJoining = true;
    public ZegoPrebuiltAudioVideoViewConfig audioVideoViewConfig;
    public List<IZegoUIKitPlugin> plugins = new ArrayList<>();
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
    public boolean markAsLargeRoom = false;
    public boolean needConfirmWhenOthersTurnOnYourCamera = false;
    public boolean needConfirmWhenOthersTurnOnYourMicrophone = false;
    public ZegoDialogInfo othersTurnOnYourCameraConfirmDialogInfo;
    public ZegoDialogInfo othersTurnOnYourMicrophoneConfirmDialogInfo;
    public transient ZegoStartLiveButton startLiveButton;
    public transient View.OnClickListener onStartLiveButtonPressed;


    public static ZegoUIKitPrebuiltLiveStreamingConfig host() {
        return host(new ArrayList<>());
    }

    public static ZegoUIKitPrebuiltLiveStreamingConfig host(@Nullable List<IZegoUIKitPlugin> plugins) {
        ZegoUIKitPrebuiltLiveStreamingConfig config = new ZegoUIKitPrebuiltLiveStreamingConfig();
        config.role = ZegoLiveStreamingRole.HOST;
        config.turnOnCameraWhenJoining = true;
        config.turnOnMicrophoneWhenJoining = true;
        config.plugins = plugins;
        config.confirmDialogInfo = new ZegoDialogInfo("Stop the live", "Are you sure to stop the live?", "Cancel",
            "Stop it");
        if (plugins != null && !plugins.isEmpty()) {
            config.bottomMenuBarConfig.audienceButtons = Collections.singletonList(
                ZegoMenuBarButtonName.COHOST_CONTROL_BUTTON);
        } else {
            config.bottomMenuBarConfig.audienceButtons = new ArrayList<>();
        }

        return config;
    }

    public static ZegoUIKitPrebuiltLiveStreamingConfig audience() {
        return audience(new ArrayList<>());
    }

    public static ZegoUIKitPrebuiltLiveStreamingConfig audience(List<IZegoUIKitPlugin> plugins) {
        ZegoUIKitPrebuiltLiveStreamingConfig config = new ZegoUIKitPrebuiltLiveStreamingConfig();
        config.role = ZegoLiveStreamingRole.AUDIENCE;
        config.plugins = plugins;
        if (plugins != null && !plugins.isEmpty()) {
            config.bottomMenuBarConfig.audienceButtons = Collections.singletonList(
                ZegoMenuBarButtonName.COHOST_CONTROL_BUTTON);
        } else {
            config.bottomMenuBarConfig.audienceButtons = new ArrayList<>();
        }
        return config;
    }
}
