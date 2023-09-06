package com.zegocloud.uikit.prebuilt.livestreaming.internal.components;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoDialogInfo;

public class ZegoExitLiveButton extends AppCompatImageView {

    private ZegoDialogInfo confirmDialogInfo;
    private ZegoLeaveLiveStreamingListener leaveLiveListener;

    public ZegoExitLiveButton(@NonNull Context context) {
        super(context);
        initView();
    }

    public ZegoExitLiveButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public void initView() {
        setOnClickListener(null);
        setImageResource(com.zegocloud.uikit.R.drawable.zego_uikit_icon_nav_close);
        setOnClickListener(v -> {
            invokeWhenClick();
        });
    }

    public void setConfirmDialogInfo(ZegoDialogInfo info) {
        confirmDialogInfo = info;
    }

    private void invokeWhenClick() {
        boolean isActivity = getContext() instanceof Activity;
        if (isActivity && confirmDialogInfo != null) {
            showQuitDialog(confirmDialogInfo);
        } else {
            if (leaveLiveListener != null) {
                leaveLiveListener.onLeaveLiveStreaming();
            }
        }
    }

    public void setLeaveLiveListener(ZegoLeaveLiveStreamingListener listener) {
        this.leaveLiveListener = listener;
    }

    private void showQuitDialog(ZegoDialogInfo dialogInfo) {
        new ConfirmDialog.Builder(getContext()).setTitle(dialogInfo.title).setMessage(dialogInfo.message)
            .setPositiveButton(dialogInfo.confirmButtonName, (dialog, which) -> {
                if (leaveLiveListener != null) {
                    leaveLiveListener.onLeaveLiveStreaming();
                }
                dialog.dismiss();
            }).setNegativeButton(dialogInfo.cancelButtonName, (dialog, which) -> {
                dialog.dismiss();
            }).build().show();
    }

}
