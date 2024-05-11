package com.zegocloud.uikit.prebuilt.livestreaming.widget;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.components.common.ZTextButton;
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.prebuilt.livestreaming.R;
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoLiveStreamingManager;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoTranslationText;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.components.LiveInvitationType;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.utils.Utils;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ZegoRequestCoHostButton extends ZTextButton {

    public ZegoRequestCoHostButton(@NonNull Context context) {
        super(context);
        initView();
    }

    public ZegoRequestCoHostButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private PluginCallbackListener callbackListener;

    @Override
    protected void initView() {
        super.initView();
        setBackgroundResource(R.drawable.livestreaming_bg_cohost_btn);
        ZegoTranslationText translationText = ZegoLiveStreamingManager.getInstance().getTranslationText();
        if (translationText != null) {
            setText(translationText.requestCoHostButton);
        }
        setTextColor(Color.WHITE);
        setTextSize(13);
        setGravity(Gravity.CENTER);
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        setPadding(Utils.dp2px(14, displayMetrics), 0, Utils.dp2px(16, displayMetrics), 0);
        setCompoundDrawablePadding(Utils.dp2px(6, displayMetrics));
        setCompoundDrawablesWithIntrinsicBounds(R.drawable.livestreaming_bottombar_cohost, 0, 0, 0);
    }

    @Override
    protected boolean beforeClick() {
        boolean isPKStarted = ZegoLiveStreamingManager.getInstance().getPKInfo() != null;
        if (isPKStarted) {
            ZegoTranslationText translationText = ZegoLiveStreamingManager.getInstance().getTranslationText();
            if (translationText != null && translationText.coHostEndBecausePK != null) {
                ZegoLiveStreamingManager.getInstance().showTopTips(translationText.coHostEndBecausePK, true);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void afterClick() {
        super.afterClick();
        if (!ZegoLiveStreamingManager.getInstance().isLiveStarted()) {
            Map<String, Object> map = new HashMap<>();
            map.put("code", -1);
            ZegoTranslationText translationText = ZegoLiveStreamingManager.getInstance().getTranslationText();
            if (translationText != null && translationText.requestCoHostFailed != null) {
                map.put("message", translationText.requestCoHostFailed);
            }
            if (callbackListener != null) {
                callbackListener.callback(map);
            }
            ZegoLiveStreamingManager.getInstance().showTopTips((String) map.get("message"), false);
            return;
        }

        String hostUserID = ZegoLiveStreamingManager.getInstance().getHostID();
        ZegoUIKitUser hostUser = ZegoUIKit.getUser(hostUserID);
        if (TextUtils.isEmpty(hostUserID) || hostUser == null) {
            Map<String, Object> map = new HashMap<>();
            map.put("code", -1);
            ZegoTranslationText translationText = ZegoLiveStreamingManager.getInstance().getTranslationText();
            if (translationText != null && translationText.requestCoHostFailed != null) {
                map.put("message", translationText.requestCoHostFailed);
            }
            if (callbackListener != null) {
                callbackListener.callback(map);
            }
            ZegoLiveStreamingManager.getInstance().showTopTips((String) map.get("message"), false);
            return;
        }

        ZegoLiveStreamingManager.getInstance()
            .sendCoHostRequest(Collections.singletonList(hostUserID), 60, LiveInvitationType.REQUEST_COHOST.getValue(),
                "",
                new PluginCallbackListener() {
                    @Override
                    public void callback(Map<String, Object> result) {
                        int code = (int) result.get("code");
                        if (code != 0) {
                            ZegoTranslationText translationText = ZegoLiveStreamingManager.getInstance()
                                .getTranslationText();
                            if (translationText != null && translationText.requestCoHostFailed != null) {
                                result.put("message", translationText.requestCoHostFailed);
                            }
                        } else {
                            ZegoTranslationText translationText = ZegoLiveStreamingManager.getInstance()
                                .getTranslationText();
                            if (translationText != null && translationText.sendRequestCoHostToast != null) {
                                result.put("message", translationText.sendRequestCoHostToast);
                            }
                            ZegoLiveStreamingManager.getInstance().setUserStatus(hostUserID,
                                ZegoLiveStreamingManager.SEND_COHOST_REQUEST);
                        }
                        ZegoLiveStreamingManager.getInstance().showTopTips((String) result.get("message"), code == 0);
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
