package com.zegocloud.uikit.prebuilt.livestreaming.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.plugin.invitation.components.ZegoAcceptInvitationButton;
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoLiveStreamingManager;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoTranslationText;
import im.zego.uikit.libuikitreport.ReportUtil;
import java.util.HashMap;
import java.util.Map;

public class ZegoAcceptCoHostButton extends ZegoAcceptInvitationButton {

    private PluginCallbackListener callbackListener;

    public ZegoAcceptCoHostButton(@NonNull Context context) {
        super(context);
    }

    public ZegoAcceptCoHostButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void initView() {
        super.initView();
        ZegoTranslationText translationText = ZegoLiveStreamingManager.getInstance().getTranslationText();
        if (translationText != null) {
            setText(translationText.agree);
        }
        setTextColor(Color.WHITE);
        setTextSize(16);
        setGravity(Gravity.CENTER);
    }

    @Override
    protected void invokedWhenClick() {
        ZegoUIKit.getSignalingPlugin().acceptInvitation(inviterID, "", new PluginCallbackListener() {
            @Override
            public void callback(Map<String, Object> result) {
                int code = (int) result.get("code");
                String invitationID = (String) result.get("invitationID");
                if (code == 0) {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("action", "accept");
                    hashMap.put("call_id", invitationID);
                    boolean currentUserHost = ZegoLiveStreamingManager.getInstance().isCurrentUserHost();
                    if (currentUserHost) {
                        ReportUtil.reportEvent("livestreaming/cohost/host/respond", hashMap);
                    } else {
                        ReportUtil.reportEvent("livestreaming/cohost/audience/respond", hashMap);
                    }
                }
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
