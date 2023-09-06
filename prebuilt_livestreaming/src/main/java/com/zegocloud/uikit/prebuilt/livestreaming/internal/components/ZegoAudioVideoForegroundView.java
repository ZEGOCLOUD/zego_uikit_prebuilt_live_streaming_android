package com.zegocloud.uikit.prebuilt.livestreaming.internal.components;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.uikit.components.audiovideo.ZegoCameraStateView;
import com.zegocloud.uikit.components.audiovideo.ZegoMicrophoneStateView;
import com.zegocloud.uikit.prebuilt.livestreaming.R;
import com.zegocloud.uikit.components.audiovideo.ZegoBaseAudioVideoForegroundView;
import com.zegocloud.uikit.service.defines.ZegoMicrophoneStateChangeListener;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.utils.Utils;

public class ZegoAudioVideoForegroundView extends ZegoBaseAudioVideoForegroundView {

    private TextView nameTextView;
    private ZegoMicrophoneStateView micStatusView;
    private ZegoCameraStateView cameraStatusView;
    private ZegoMicrophoneStateChangeListener microphoneStateChangeListener;

    public ZegoAudioVideoForegroundView(@NonNull Context context, String userID) {
        super(context, userID);
    }

    public ZegoAudioVideoForegroundView(@NonNull Context context, @Nullable AttributeSet attrs, String userID) {
        super(context, attrs, userID);
    }


    @Override
    protected void onForegroundViewCreated(ZegoUIKitUser uiKitUser) {
        super.onForegroundViewCreated(uiKitUser);
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(getContext())
            .inflate(R.layout.livestreaming_layout_video_foreground, null, false);
        nameTextView = viewGroup.findViewById(R.id.foreground_textview);
        micStatusView = viewGroup.findViewById(R.id.foreground_mic);
        cameraStatusView = viewGroup.findViewById(R.id.foreground_camera);

        if (uiKitUser != null) {
            nameTextView.setText(uiKitUser.userName);
        }
        if (!TextUtils.isEmpty(userID)) {
            micStatusView.setUserID(userID);
            cameraStatusView.setUserID(userID);
        }

        LayoutParams layoutParams = new LayoutParams(-1,
            Utils.dp2px(34, getContext().getResources().getDisplayMetrics()));
        layoutParams.gravity = Gravity.BOTTOM;
        addView(viewGroup, layoutParams);

        showCameraView(false);
    }

    @Override
    protected void onMicrophoneStateChanged(boolean isMicrophoneOn) {
        super.onMicrophoneStateChanged(isMicrophoneOn);
        showMicrophoneView(!isMicrophoneOn);
    }

    @Override
    protected void onCameraStateChanged(boolean isCameraOn) {
        super.onCameraStateChanged(isCameraOn);
        showCameraView(isCameraOn);
    }

    public void showCameraView(boolean showCameraStatusOnView) {
        cameraStatusView.setVisibility(showCameraStatusOnView ? View.VISIBLE : View.GONE);
    }

    public void showMicrophoneView(boolean showMicStatusOnView) {
        micStatusView.setVisibility(showMicStatusOnView ? View.VISIBLE : View.GONE);
    }

    public void showUserNameView(boolean showUserName) {
        nameTextView.setVisibility(showUserName ? View.VISIBLE : View.GONE);
    }
}
