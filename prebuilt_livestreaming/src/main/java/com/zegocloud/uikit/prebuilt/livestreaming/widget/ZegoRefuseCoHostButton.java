package com.zegocloud.uikit.prebuilt.livestreaming.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.plugin.invitation.components.ZegoRefuseInvitationButton;
import com.zegocloud.uikit.prebuilt.livestreaming.R;
import java.util.Map;

public class ZegoRefuseCoHostButton extends ZegoRefuseInvitationButton {

    private PluginCallbackListener callbackListener;

    public ZegoRefuseCoHostButton(@NonNull Context context) {
        super(context);
    }

    public ZegoRefuseCoHostButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void initView() {
        setText(R.string.disagree);
        setTextColor(Color.WHITE);
        setTextSize(16);
        setGravity(Gravity.CENTER);
        setOnClickListener(null);
    }

    @Override
    protected void invokedWhenClick() {
        ZegoUIKit.refuseInvitation(inviterID, "", new PluginCallbackListener() {
            @Override
            public void callback(Map<String, Object> result) {
                if (callbackListener != null) {
                    callbackListener.callback(result);
                }
            }
        });
    }

    public void setRequestCallbackListener(PluginCallbackListener callbackListener) {
        this.callbackListener = callbackListener;
    }
}
