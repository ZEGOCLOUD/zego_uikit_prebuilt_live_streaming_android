package com.zegocloud.uikit.prebuilt.livestreaming.widget;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.Gravity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.plugin.invitation.components.ZegoStartInvitationButton;
import com.zegocloud.uikit.prebuilt.livestreaming.R;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoTranslationText;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.LiveStreamingManager;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.LiveInvitationType;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.utils.GenericUtils;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZegoInviteJoinCoHostButton extends ZegoStartInvitationButton {

    private PluginCallbackListener callbackListener;

    public ZegoInviteJoinCoHostButton(@NonNull Context context) {
        super(context);
    }

    public ZegoInviteJoinCoHostButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void initView() {
        super.initView();
        type = LiveInvitationType.INVITE_TO_COHOST.getValue();
        setTextColor(Color.WHITE);
        setTextSize(14);
        setSingleLine();
        setEllipsize(TruncateAt.END);
        setGravity(Gravity.CENTER);
    }

    @Override
    public void setInvitees(List<ZegoUIKitUser> invitees) {
        super.setInvitees(invitees);
        if (!invitees.isEmpty()) {
            String text = getContext().getString(R.string.invite_co_host, invitees.get(0).userName);
            setText(text);
        }
    }

    public void setInvitee(ZegoUIKitUser invitee) {
        setInvitees(Collections.singletonList(invitee));
        String text = getContext().getString(R.string.invite_co_host, invitee.userName);
        setText(text);
    }

    @Override
    protected void invokedWhenClick() {
        List<String> idList = GenericUtils.map(invitees, zegoUIKitUser -> zegoUIKitUser.userID);
        if (!idList.isEmpty()) {
            if (LiveStreamingManager.getInstance().hasInviteUserCoHost(idList.get(0))) {
                Map<String, Object> map = new HashMap<>();
                map.put("code", -1);
                map.put("message", getContext().getString(R.string.invite_co_host_repeat));
                ZegoTranslationText translationText = LiveStreamingManager.getInstance().getTranslationText();
                if (translationText != null && translationText.repeatInviteCoHostFailedToast != null) {
                    map.put("message", translationText.repeatInviteCoHostFailedToast);
                }
                if (callbackListener != null) {
                    callbackListener.callback(map);
                }
                LiveStreamingManager.getInstance().showTopTips((String) map.get("message"), false);
                return;
            }
        }

        ZegoUIKit.getSignalingPlugin().sendInvitation(idList, timeout, type, data, new PluginCallbackListener() {
            @Override
            public void callback(Map<String, Object> result) {
                int code = (int) result.get("code");
                if (code != 0) {
                    result.put("message", getContext().getString(R.string.invite_co_host_tips));
                    ZegoTranslationText translationText = LiveStreamingManager.getInstance().getTranslationText();
                    if (translationText != null && translationText.inviteCoHostFailedToast != null) {
                        result.put("message", translationText.inviteCoHostFailedToast);
                    }
                    LiveStreamingManager.getInstance().showTopTips((String) result.get("message"), code == 0);
                } else {
                    if (!idList.isEmpty()) {
                        LiveStreamingManager.getInstance()
                            .setUserStatus(idList.get(0), LiveStreamingManager.INVITE_JOIN_COHOST);
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
