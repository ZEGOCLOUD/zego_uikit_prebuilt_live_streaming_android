package com.zegocloud.uikit.prebuilt.livestreaming.widget;

import android.Manifest.permission;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import com.permissionx.guolindev.PermissionX;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.prebuilt.livestreaming.R;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoDialogInfo;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoTranslationText;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.ConfirmDialog;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.LiveStreamingManager;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.service.internal.UIKitCore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ZegoCoHostControlButton extends FrameLayout {

    private ZegoRequestCoHostButton requestCoHostButton;
    private ZegoCancelRequestCoHostButton cancelRequestCoHostButton;
    private ZegoEndCoHostButton endCoHostButton;
    private OnClickListener endCoHostListener;

    public ZegoCoHostControlButton(@NonNull Context context) {
        super(context);
        initView();
    }

    public ZegoCoHostControlButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ZegoCoHostControlButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        requestCoHostButton = new ZegoRequestCoHostButton(getContext());
        requestCoHostButton.setRequestCallbackListener(new PluginCallbackListener() {
            @Override
            public void callback(Map<String, Object> result) {
                int code = (int) result.get("code");
                if (code == 0) {
                    showCancelRequestCoHostButton();
                }
            }
        });
        addView(requestCoHostButton);
        cancelRequestCoHostButton = new ZegoCancelRequestCoHostButton(getContext());
        cancelRequestCoHostButton.setRequestCallbackListener(new PluginCallbackListener() {
            @Override
            public void callback(Map<String, Object> result) {
                int code = (int) result.get("code");
                if (code == 0) {
                    showRequestCoHostButton();
                }
            }
        });
        addView(cancelRequestCoHostButton);
        endCoHostButton = new ZegoEndCoHostButton(getContext());
        endCoHostButton.setOnClickListener(v -> {
            if (getContext() instanceof Activity) {
                String title = getContext().getString(R.string.end_co_host_title);
                String message = getContext().getString(R.string.end_co_host_message);
                String cancelButtonName = getContext().getString(R.string.end_co_host_cancel);
                String confirmButtonName = getContext().getString(R.string.end_co_host_ok);
                ZegoTranslationText translationText = LiveStreamingManager.getInstance().getTranslationText();
                if (translationText != null) {
                    ZegoDialogInfo dialogInfo = translationText.endConnectionDialogInfo;
                    if (dialogInfo != null && dialogInfo.title != null) {
                        title = dialogInfo.title;
                    }
                    if (dialogInfo != null && dialogInfo.message != null) {
                        message = dialogInfo.message;
                    }
                    if (dialogInfo != null && dialogInfo.cancelButtonName != null) {
                        cancelButtonName = dialogInfo.cancelButtonName;
                    }
                    if (dialogInfo != null && dialogInfo.confirmButtonName != null) {
                        confirmButtonName = dialogInfo.confirmButtonName;
                    }
                }
                new ConfirmDialog.Builder(getContext()).setTitle(title).setMessage(message)
                    .setPositiveButton(confirmButtonName, (dialog, which) -> {
                        ZegoUIKitUser localUser = ZegoUIKit.getLocalUser();
                        if (localUser == null) {
                            return;
                        }
                        ZegoUIKit.turnCameraOn(localUser.userID, false);
                        ZegoUIKit.turnMicrophoneOn(localUser.userID, false);
                        showRequestCoHostButton();
                        if (endCoHostListener != null) {
                            endCoHostListener.onClick(v);
                        }
                        dialog.dismiss();
                    }).setNegativeButton(cancelButtonName, (dialog, which) -> {
                        dialog.dismiss();
                    }).build().show();
            }
        });
        addView(endCoHostButton);
        ZegoTranslationText translationText = LiveStreamingManager.getInstance().getTranslationText();
        if (translationText != null && translationText.requestCoHostButton != null) {
            requestCoHostButton.setText(translationText.requestCoHostButton);
        }
        if (translationText != null && translationText.cancelRequestCoHostButton != null) {
            cancelRequestCoHostButton.setText(translationText.cancelRequestCoHostButton);
        }
        if (translationText != null && translationText.endCoHostButton != null) {
            endCoHostButton.setText(translationText.endCoHostButton);
        }
    }

    public void showRequestCoHostButton() {
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).setVisibility(GONE);
        }
        requestCoHostButton.setVisibility(VISIBLE);

    }

    public void showCancelRequestCoHostButton() {
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).setVisibility(GONE);
        }
        cancelRequestCoHostButton.setVisibility(VISIBLE);
    }

    public void showEndCoHostButton() {
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).setVisibility(GONE);
        }
        endCoHostButton.setVisibility(VISIBLE);
    }

    public void onLiveEnd() {
        if (cancelRequestCoHostButton.getVisibility() == VISIBLE) {
            String hostUserID = ZegoUIKit.getRoomProperties().get("host");
            ZegoUIKitUser uiKitUser = ZegoUIKit.getUser(hostUserID);
            if (uiKitUser == null) {
                return;
            }
            UIKitCore.getInstance().cancelInvitation(Collections.singletonList(hostUserID), "", null);
        }
    }

    public void setEndCoHostListener(OnClickListener endCoHostListener) {
        this.endCoHostListener = endCoHostListener;
    }
}
