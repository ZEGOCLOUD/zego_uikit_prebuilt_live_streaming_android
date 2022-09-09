package com.zegocloud.uikit.prebuilt.livestreaming.internal;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoConfirmDialogInfo;

public class ZegoExitLiveButton extends AppCompatImageView {

    private ZegoConfirmDialogInfo confirmDialogInfo;
    private LeaveLiveStreamingListener leaveLiveListener;

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
        setImageResource(com.zegocloud.uikit.R.drawable.icon_nav_close);
    }

    public void setConfirmDialogInfo(ZegoConfirmDialogInfo info) {
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

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                invokeWhenClick();
                if (l != null) {
                    l.onClick(ZegoExitLiveButton.this);
                }
            }
        });
    }

    public void setLeaveLiveListener(LeaveLiveStreamingListener listener) {
        this.leaveLiveListener = listener;
    }

    private void showQuitDialog(ZegoConfirmDialogInfo dialogInfo) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setTitle(dialogInfo.title);
        builder.setMessage(dialogInfo.message);
        builder.setPositiveButton(dialogInfo.confirmButtonName, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (leaveLiveListener != null) {
                    leaveLiveListener.onLeaveLiveStreaming();
                }
            }
        });
        builder.setNegativeButton(dialogInfo.cancelButtonName, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

}
