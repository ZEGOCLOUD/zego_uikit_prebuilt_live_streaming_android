package com.zegocloud.uikit.prebuilt.livestreaming.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.plugin.invitation.components.ZegoCancelInvitationButton;
import com.zegocloud.uikit.prebuilt.livestreaming.R;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.service.internal.UIKitCore;
import com.zegocloud.uikit.utils.Utils;
import java.util.Map;

public class ZegoCancelRequestCoHostButton extends ZegoCancelInvitationButton {

    public ZegoCancelRequestCoHostButton(@NonNull Context context) {
        super(context);
    }

    public ZegoCancelRequestCoHostButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private PluginCallbackListener callbackListener;

    @Override
    protected void initView() {
        setBackgroundResource(R.drawable.bg_cohost_btn);
        setText(R.string.cancel_co_host);
        setTextColor(Color.WHITE);
        setTextSize(13);
        setGravity(Gravity.CENTER);
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        setPadding(Utils.dp2px(14, displayMetrics), 0, Utils.dp2px(16, displayMetrics), 0);
        setCompoundDrawablePadding(Utils.dp2px(6, displayMetrics));
        setCompoundDrawablesWithIntrinsicBounds(R.drawable.bottombar_cohost,0,0,0);
        setOnClickListener(null);
    }

    @Override
    protected void invokedWhenClick() {
        String hostUserID = ZegoUIKit.getRoomProperties().get("host");
        ZegoUIKitUser uiKitUser = ZegoUIKit.getUser(hostUserID);
        if (uiKitUser == null) {
            return;
        }
        invitees.add(hostUserID);
        UIKitCore.getInstance().cancelInvitation(invitees, "", new PluginCallbackListener() {
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
