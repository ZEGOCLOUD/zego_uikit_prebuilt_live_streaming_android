package com.zegocloud.uikit.prebuilt.livestreaming.widget;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoLiveStreamingManager;
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoLiveStreamingManager.ZegoLiveStreamingListener;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoDialogInfo;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoLiveStreamingRole;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoTranslationText;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.components.ConfirmDialog.Builder;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.core.PKService.PKInfo;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import java.util.Collections;
import java.util.Map;
import timber.log.Timber;

public class ZegoCoHostControlButton extends FrameLayout {

    private ZegoRequestCoHostButton requestCoHostButton;
    private ZegoCancelRequestCoHostButton cancelRequestCoHostButton;
    private ZegoEndCoHostButton endCoHostButton;

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
                String title = "";
                String message = "";
                String cancelButtonName = "";
                String confirmButtonName = "";
                ZegoTranslationText translationText = ZegoLiveStreamingManager.getInstance().getTranslationText();
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
                new Builder(getContext()).setTitle(title).setMessage(message)
                    .setPositiveButton(confirmButtonName, (dialog, which) -> {
                        ZegoLiveStreamingManager.getInstance().endCoHost();
                        showRequestCoHostButton();
                        dialog.dismiss();
                    }).setNegativeButton(cancelButtonName, (dialog, which) -> {
                        dialog.dismiss();
                    }).build().show();
            }
        });
        addView(endCoHostButton);
        ZegoTranslationText translationText = ZegoLiveStreamingManager.getInstance().getTranslationText();
        if (translationText != null && translationText.requestCoHostButton != null) {
            requestCoHostButton.setText(translationText.requestCoHostButton);
        }
        if (translationText != null && translationText.cancelRequestCoHostButton != null) {
            cancelRequestCoHostButton.setText(translationText.cancelRequestCoHostButton);
        }
        if (translationText != null && translationText.endCoHostButton != null) {
            endCoHostButton.setText(translationText.endCoHostButton);
        }
        showRequestCoHostButton();

        ZegoLiveStreamingManager.getInstance().addLiveStreamingListener(new ZegoLiveStreamingListener() {

            @Override
            public void onPKStarted() {
                for (int i = 0; i < getChildCount(); i++) {
                    getChildAt(i).setVisibility(GONE);
                }
            }

            @Override
            public void onPKEnded() {
                ZegoLiveStreamingRole userRole = ZegoLiveStreamingManager.getInstance().getCurrentUserRole();
                if (userRole == ZegoLiveStreamingRole.AUDIENCE) {
                    showRequestCoHostButton();
                }
            }
        });
    }

    public void showRequestCoHostButton() {
        PKInfo pkInfo = ZegoLiveStreamingManager.getInstance().getPKInfo();
        if (pkInfo != null) {
            return;
        }
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).setVisibility(GONE);
        }
        requestCoHostButton.setVisibility(VISIBLE);
    }

    public void showCancelRequestCoHostButton() {
        PKInfo pkInfo = ZegoLiveStreamingManager.getInstance().getPKInfo();
        if (pkInfo != null) {
            return;
        }
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).setVisibility(GONE);
        }
        cancelRequestCoHostButton.setVisibility(VISIBLE);
    }

    public void showEndCoHostButton() {
        PKInfo pkInfo = ZegoLiveStreamingManager.getInstance().getPKInfo();
        if (pkInfo != null) {
            return;
        }
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).setVisibility(GONE);
        }
        endCoHostButton.setVisibility(VISIBLE);
    }

    public void onLiveEnd() {
        if (cancelRequestCoHostButton.getVisibility() == VISIBLE) {
            String hostUserID = ZegoLiveStreamingManager.getInstance().getHostID();
            ZegoUIKitUser uiKitUser = ZegoUIKit.getUser(hostUserID);
            if (uiKitUser == null) {
                return;
            }
            ZegoUIKit.getSignalingPlugin().cancelInvitation(Collections.singletonList(hostUserID), "", null);
        }
    }
}
