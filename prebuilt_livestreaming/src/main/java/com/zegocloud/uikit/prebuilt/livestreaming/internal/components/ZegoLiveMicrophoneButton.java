package com.zegocloud.uikit.prebuilt.livestreaming.internal.components;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.components.common.ZEGOImageButton;
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoLiveStreamingManager;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.service.express.IExpressEngineEventHandler;

/**
 * only for local user
 */
public class ZegoLiveMicrophoneButton extends ZEGOImageButton {

    public ZegoLiveMicrophoneButton(@NonNull Context context) {
        super(context);
    }

    public ZegoLiveMicrophoneButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ZegoLiveMicrophoneButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initView() {
        super.initView();
        setImageResource(com.zegocloud.uikit.R.drawable.zego_uikit_icon_mic_switch,
            com.zegocloud.uikit.R.drawable.zego_uikit_icon_mic_switch_off);
        ZegoUIKit.addEventHandler(new IExpressEngineEventHandler() {
            @Override
            public void onLocalMicrophoneStateUpdate(boolean open) {
                updateState(open);
            }
        });
        ZegoUIKitUser localUser = ZegoUIKit.getLocalUser();
        if (localUser != null) {
            updateState(ZegoUIKit.isMicrophoneOn(localUser.userID));
        }
    }

    @Override
    public void open() {
        super.open();
        if (ZegoLiveStreamingManager.getInstance().getPKInfo() == null) {
            ZegoUIKitUser localUser = ZegoUIKit.getLocalUser();
            if (localUser != null) {
                ZegoUIKit.turnMicrophoneOn(localUser.userID, true);
            }
        } else {
            ZegoUIKit.openMicrophone(true);
        }
    }

    @Override
    public void close() {
        super.close();
        if (ZegoLiveStreamingManager.getInstance().getPKInfo() == null) {
            ZegoUIKitUser localUser = ZegoUIKit.getLocalUser();
            if (localUser != null) {
                ZegoUIKit.turnMicrophoneOn(localUser.userID, false);
            }
        } else {
            ZegoUIKit.openMicrophone(false);
        }
    }
}
