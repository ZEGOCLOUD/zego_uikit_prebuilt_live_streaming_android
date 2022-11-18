package com.zegocloud.uikit.prebuilt.livestreaming.internal;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.components.audiovideo.ZegoCameraStateView;
import com.zegocloud.uikit.components.audiovideo.ZegoMicrophoneStateView;
import com.zegocloud.uikit.prebuilt.livestreaming.R;
import com.zegocloud.uikit.service.defines.ZegoMicrophoneStateChangeListener;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.utils.Utils;
import java.util.Objects;

public class ZegoVideoForegroundView extends FrameLayout {

    private TextView textView;
    private ZegoMicrophoneStateView micStatusView;
    private ZegoUIKitUser userInfo;
    private ZegoCameraStateView cameraStatusView;
    private ZegoMicrophoneStateChangeListener microphoneStateChangeListener;

    public ZegoVideoForegroundView(@NonNull Context context, ZegoUIKitUser userInfo) {
        super(context);
        this.userInfo = userInfo;
        initView();
    }

    public ZegoVideoForegroundView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ZegoVideoForegroundView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(getContext())
            .inflate(R.layout.layout_video_foreground, null, false);
        textView = viewGroup.findViewById(R.id.foreground_textview);
        micStatusView = viewGroup.findViewById(R.id.foreground_mic);
        cameraStatusView = viewGroup.findViewById(R.id.foreground_camera);
        setUserInfo(userInfo);

        LayoutParams layoutParams = new LayoutParams(-1,
            Utils.dp2px(34, getContext().getResources().getDisplayMetrics()));
        layoutParams.gravity = Gravity.BOTTOM;
        addView(viewGroup, layoutParams);

        microphoneStateChangeListener = new ZegoMicrophoneStateChangeListener() {
            @Override
            public void onMicrophoneOn(ZegoUIKitUser uiKitUser, boolean isOn) {
                if (Objects.equals(uiKitUser, userInfo)) {
                    if (isOn) {
                        showMicrophone(false);
                    } else {
                        showMicrophone(true);
                    }
                }
            }
        };
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ZegoUIKit.addMicrophoneStateListener(microphoneStateChangeListener);
        if (userInfo != null && !ZegoUIKit.isMicrophoneOn(userInfo.userID)) {
            showMicrophone(true);
        } else {
            showMicrophone(false);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ZegoUIKit.removeMicrophoneStateListener(microphoneStateChangeListener);
    }

    public void setUserInfo(ZegoUIKitUser userInfo) {
        this.userInfo = userInfo;
        if (textView != null) {
            if (userInfo != null) {
                textView.setText(userInfo.userName);
            }
        }
        if (micStatusView != null) {
            if (userInfo != null) {
                micStatusView.setUserID(userInfo.userID);
            }
        }
        if (cameraStatusView != null) {
            if (userInfo != null) {
                cameraStatusView.setUserID(userInfo.userID);
            }
        }
    }

    public void showCamera(boolean showMicStatusOnView) {
        cameraStatusView.setVisibility(showMicStatusOnView ? View.VISIBLE : View.GONE);
    }

    public void showMicrophone(boolean showMicStatusOnView) {
        micStatusView.setVisibility(showMicStatusOnView ? View.VISIBLE : View.GONE);
    }

    public void showUserName(boolean showUserName) {
        textView.setVisibility(showUserName ? View.VISIBLE : View.GONE);
    }

    public ZegoMicrophoneStateView getMicStatusView() {
        return micStatusView;
    }

    public ZegoCameraStateView getCameraStatusView() {
        return cameraStatusView;
    }
}
