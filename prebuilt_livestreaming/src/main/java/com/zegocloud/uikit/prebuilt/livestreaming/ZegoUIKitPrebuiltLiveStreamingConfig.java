package com.zegocloud.uikit.prebuilt.livestreaming;

import androidx.annotation.IntDef;
import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ZegoUIKitPrebuiltLiveStreamingConfig implements Serializable {

    public boolean showSoundWaveOnAudioView = true; // 语音模式下，是否显示头像周围的声浪
    public boolean turnOnCameraWhenJoining = true; // 是否默认开启摄像头，如果摄像头和麦克风都关闭，则停止推流。
    public boolean turnOnMicrophoneWhenJoining = true; // 是否默认开启麦克风，如果摄像头和麦克风都关闭，则停止推流。
    public boolean useSpeakerWhenJoining = true; // 是否默认使用扬声器，默认为false。如果否，使用系统默认设备。
    public boolean showInRoomMessageButton = true; // 是否显示发送消息的按钮
    public List<ZegoMenuBarButtonName> menuBarButtons = Arrays.asList(
        ZegoMenuBarButtonName.TOGGLE_CAMERA_BUTTON,
        ZegoMenuBarButtonName.TOGGLE_MICROPHONE_BUTTON,
        ZegoMenuBarButtonName.SWITCH_CAMERA_FACING_BUTTON);
    public int menuBarButtonsMaxCount = 5;

    public ZegoConfirmDialogInfo confirmDialogInfo;

    public static final int ROLE_NONE = 0;
    public static final int ROLE_HOST = 1;
    public static final int ROLE_AUDIENCE = 2;

    @IntDef({ROLE_NONE, ROLE_HOST, ROLE_AUDIENCE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ROLE {

    }

    public ZegoUIKitPrebuiltLiveStreamingConfig() {
        this(ROLE_NONE);
    }

    public ZegoUIKitPrebuiltLiveStreamingConfig(@ROLE int role) {
        if (role == ROLE_HOST) {
            showSoundWaveOnAudioView = true;
            turnOnCameraWhenJoining = true;
            turnOnMicrophoneWhenJoining = true;
            useSpeakerWhenJoining = true;
            showInRoomMessageButton = true;
            menuBarButtons = Arrays.asList(
                ZegoMenuBarButtonName.TOGGLE_CAMERA_BUTTON,
                ZegoMenuBarButtonName.TOGGLE_MICROPHONE_BUTTON,
                ZegoMenuBarButtonName.SWITCH_CAMERA_FACING_BUTTON);
            menuBarButtonsMaxCount = 5;
        } else if (role == ROLE_AUDIENCE) {
            showSoundWaveOnAudioView = true;
            turnOnCameraWhenJoining = false;
            turnOnMicrophoneWhenJoining = false;
            useSpeakerWhenJoining = true;
            showInRoomMessageButton = true;
            menuBarButtons = new ArrayList<>();
            menuBarButtonsMaxCount = 5;
        } else {
            showSoundWaveOnAudioView = true;
            turnOnCameraWhenJoining = false;
            turnOnMicrophoneWhenJoining = false;
            useSpeakerWhenJoining = true;
            showInRoomMessageButton = true;
            menuBarButtons = new ArrayList<>();
            menuBarButtonsMaxCount = 5;
        }
    }

}
