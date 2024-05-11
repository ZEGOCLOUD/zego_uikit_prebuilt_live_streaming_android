package com.zegocloud.uikit.prebuilt.livestreaming.internal.components;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoLiveStreamingManager;
import com.zegocloud.uikit.prebuilt.livestreaming.R;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoTranslationText;
import com.zegocloud.uikit.prebuilt.livestreaming.widget.ZegoInviteJoinCoHostButton;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.utils.Utils;
import java.util.Collections;
import java.util.Map;

public class InviteCoHostDialog extends Dialog {

    private LinearLayout childParent;
    private int cellHeight;
    private ZegoUIKitUser userInfo;

    public InviteCoHostDialog(@NonNull Context context, ZegoUIKitUser uiKitUser) {
        super(context);
        this.userInfo = uiKitUser;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cellHeight = Utils.dp2px(50, getContext().getResources().getDisplayMetrics());

        FrameLayout rootView = new FrameLayout(getContext());
        childParent = new LinearLayout(getContext());
        childParent.setOrientation(LinearLayout.VERTICAL);
        childParent.setBackgroundResource(R.drawable.livestreaming_bg_bottom_menu_dialog);
        rootView.addView(childParent);

        TextView removeUserButton = new TextView(getContext());
        ZegoTranslationText translationText = ZegoLiveStreamingManager.getInstance().getTranslationText();
        if (translationText != null && translationText.removeUserMenuDialogButton != null) {
            removeUserButton.setText(String.format(translationText.removeUserMenuDialogButton, userInfo.userName));
        }
        removeUserButton.setTextColor(Color.WHITE);
        removeUserButton.setTextSize(14);
        removeUserButton.setSingleLine(true);
        removeUserButton.setEllipsize(TruncateAt.END);
        removeUserButton.setGravity(Gravity.CENTER);
        removeUserButton.setOnClickListener(v -> {
            ZegoUIKit.removeUserFromRoom(Collections.singletonList(userInfo.userID));
            dismiss();
        });
        childParent.addView(removeUserButton, new LinearLayout.LayoutParams(-1, cellHeight));

        View seperator = new View(getContext());
        seperator.setBackgroundColor(Color.parseColor("#1affffff"));
        childParent.addView(seperator,
            new LinearLayout.LayoutParams(-1, Utils.dp2px(1, getContext().getResources().getDisplayMetrics())));

        ZegoInviteJoinCoHostButton button = new ZegoInviteJoinCoHostButton(getContext());
        button.setInvitee(userInfo);
        button.setRequestCallbackListener(new PluginCallbackListener() {
            @Override
            public void callback(Map<String, Object> result) {
                dismiss();
            }
        });
        button.setOnClickListener(v -> {
            dismiss();
        });
        childParent.addView(button, new LinearLayout.LayoutParams(-1, cellHeight));
        if (translationText != null && translationText.inviteCoHostButton != null) {
            button.setText(String.format(translationText.inviteCoHostButton, userInfo.userName));
        }

        View seperator2 = new View(getContext());
        seperator2.setBackgroundColor(Color.parseColor("#1affffff"));
        childParent.addView(seperator2,
            new LinearLayout.LayoutParams(-1, Utils.dp2px(1, getContext().getResources().getDisplayMetrics())));

        TextView cancelButton = new TextView(getContext());
        if (translationText != null) {
            cancelButton.setText(translationText.cancel);
        }
        cancelButton.setTextColor(Color.WHITE);
        cancelButton.setTextSize(14);
        cancelButton.setGravity(Gravity.CENTER);
        cancelButton.setOnClickListener(v -> {
            dismiss();
        });
        if (translationText != null && translationText.cancelMenuDialogButton != null) {
            cancelButton.setText(translationText.cancelMenuDialogButton);
        }
        childParent.addView(cancelButton, new LinearLayout.LayoutParams(-1, cellHeight));
        setContentView(rootView);

        Window window = getWindow();
        LayoutParams attributes = window.getAttributes();
        attributes.width = LayoutParams.MATCH_PARENT;
        attributes.height = LayoutParams.WRAP_CONTENT;
        window.setAttributes(attributes);
        window.setGravity(Gravity.BOTTOM);
        //        window.setDimAmount(0.2f);
        window.setBackgroundDrawable(new ColorDrawable());

        setCanceledOnTouchOutside(false);
    }
}
