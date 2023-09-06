package com.zegocloud.uikit.prebuilt.livestreaming.internal.components;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.TextureView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.uikit.ZegoUIKit;
import im.zego.zegoexpress.constants.ZegoViewMode;
import im.zego.zegoexpress.entity.ZegoCanvas;

public class ZEGOVideoView extends TextureView {

    private String userID;
    private String streamID;

    public ZEGOVideoView(@NonNull Context context) {
        super(context);
    }

    public ZEGOVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ZEGOVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ZEGOVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getStreamID() {
        return streamID;
    }

    public void setStreamID(String streamID) {
        this.streamID = streamID;
    }

    public void startPlayRemoteAudioVideo() {
        startPlayRemoteAudioVideo(ZegoViewMode.ASPECT_FILL);
    }

    public void startPlayRemoteAudioVideo(ZegoViewMode viewMode) {
        if (TextUtils.isEmpty(streamID)) {
            return;
        }
        ZegoCanvas canvas = new ZegoCanvas(this);
        canvas.viewMode = viewMode;
        ZegoUIKit.startPlayingStream(streamID, canvas);
    }


    public void stopPlayRemoteAudioVideo() {
        if (TextUtils.isEmpty(streamID)) {
            return;
        }
        ZegoUIKit.stopPlayingStream(streamID);
    }

    public void mutePlayAudio(boolean mute) {
        if (TextUtils.isEmpty(streamID)) {
            return;
        }
        ZegoUIKit.mutePlayStreamAudio(streamID, mute);
    }

    public void startPublishAudioVideo() {
        ZegoUIKit.startPublishingStream(streamID);
    }

    public void stopPublishAudioVideo() {
        ZegoUIKit.stopPublishingStream();
    }

    public void startPreviewOnly() {
        ZegoCanvas canvas = new ZegoCanvas(this);
        canvas.viewMode = ZegoViewMode.ASPECT_FILL;
        ZegoUIKit.startPreview(canvas);
    }
}
