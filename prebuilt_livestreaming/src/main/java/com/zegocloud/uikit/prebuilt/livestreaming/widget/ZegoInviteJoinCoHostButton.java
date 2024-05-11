package com.zegocloud.uikit.prebuilt.livestreaming.widget;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.Gravity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.uikit.components.common.ZTextButton;
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.prebuilt.livestreaming.R;
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoLiveStreamingManager;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoTranslationText;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.components.LiveInvitationType;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ZegoInviteJoinCoHostButton extends ZTextButton {

    private PluginCallbackListener callbackListener;
    private ZegoUIKitUser invitee;

    public ZegoInviteJoinCoHostButton(@NonNull Context context) {
        super(context);
    }

    public ZegoInviteJoinCoHostButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void initView() {
        super.initView();
        setTextColor(Color.WHITE);
        setTextSize(14);
        setSingleLine();
        setEllipsize(TruncateAt.END);
        setGravity(Gravity.CENTER);
    }

    public void setInvitee(ZegoUIKitUser invitee) {
        this.invitee = invitee;
        ZegoTranslationText translationText = ZegoLiveStreamingManager.getInstance().getTranslationText();
        if (translationText != null) {
            setText(String.format(translationText.inviteCoHostButton,invitee.userName));
        }
    }

    @Override
    protected boolean beforeClick() {
        boolean isPKStarted = ZegoLiveStreamingManager.getInstance().getPKInfo() != null;
        if (isPKStarted) {
            ZegoLiveStreamingManager.getInstance().showTopTips("cannot invite coHost because PK", true);
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void afterClick() {
        if (invitee != null) {
            if (ZegoLiveStreamingManager.getInstance().hasInviteUserCoHost(invitee.userID)) {
                Map<String, Object> map = new HashMap<>();
                map.put("code", -1);
                ZegoTranslationText translationText = ZegoLiveStreamingManager.getInstance().getTranslationText();
                if (translationText != null && translationText.repeatInviteCoHostFailedToast != null) {
                    map.put("message", translationText.repeatInviteCoHostFailedToast);
                }
                if (callbackListener != null) {
                    callbackListener.callback(map);
                }
                ZegoLiveStreamingManager.getInstance().showTopTips((String) map.get("message"), false);
                return;
            }
        }

        ZegoLiveStreamingManager.getInstance().sendCoHostRequest(Collections.singletonList(invitee.userID), 60,
            LiveInvitationType.INVITE_TO_COHOST.getValue(), "", new PluginCallbackListener() {
                @Override
                public void callback(Map<String, Object> result) {
                    int code = (int) result.get("code");
                    if (code != 0) {
                        ZegoTranslationText translationText = ZegoLiveStreamingManager.getInstance().getTranslationText();
                        if (translationText != null && translationText.inviteCoHostFailedToast != null) {
                            result.put("message", translationText.inviteCoHostFailedToast);
                        }
                        ZegoLiveStreamingManager.getInstance().showTopTips((String) result.get("message"), code == 0);
                    } else {
                        if (invitee != null) {
                            ZegoLiveStreamingManager.getInstance()
                                .setUserStatus(invitee.userID, ZegoLiveStreamingManager.INVITE_JOIN_COHOST);
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
