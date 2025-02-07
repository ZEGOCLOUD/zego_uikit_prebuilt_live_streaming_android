package com.zegocloud.uikit.prebuilt.livestreaming.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoLiveStreamingManager;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoTranslationText;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.components.LiveInvitationType;
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.plugin.invitation.components.ZegoStartInvitationButton;
import com.zegocloud.uikit.plugin.adapter.utils.GenericUtils;
import com.zegocloud.uikit.prebuilt.livestreaming.R;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import im.zego.uikit.libuikitreport.ReportUtil;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZegoRemoveCoHostButton extends ZegoStartInvitationButton {

    private PluginCallbackListener callbackListener;

    public ZegoRemoveCoHostButton(@NonNull Context context) {
        super(context);
    }

    public ZegoRemoveCoHostButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void initView() {
        super.initView();
        type = LiveInvitationType.REMOVE_COHOST.getValue();
        ZegoTranslationText translationText = ZegoLiveStreamingManager.getInstance().getTranslationText();
        if (translationText != null) {
            setText(translationText.removeCoHostButton);
        }
        setBackground(null);
        setTextColor(Color.WHITE);
        setTextSize(14);
        setGravity(Gravity.CENTER);
    }

    @Override
    protected void invokedWhenClick() {
        List<String> idList = GenericUtils.map(invitees, zegoUIKitUser -> zegoUIKitUser.userID);
        ZegoLiveStreamingManager.getInstance()
            .sendCoHostRequest(idList, timeout, type, data, new PluginCallbackListener() {
                @Override
                public void callback(Map<String, Object> result) {
                    int code = (int) result.get("code");
                    String invitationID = (String) result.get("invitationID");
                    if (code == 0) {
                        String currentRoomID = ZegoUIKit.getRoom().roomID;
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("cohost_id", idList.get(0));
                        hashMap.put("room_id", currentRoomID);
                        hashMap.put("call_id", invitationID);
                        ReportUtil.reportEvent("livestreaming/cohost/host/stop", hashMap);
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

    public void setInvitee(ZegoUIKitUser userInfo) {
        setInvitees(Collections.singletonList(userInfo));
    }
}
