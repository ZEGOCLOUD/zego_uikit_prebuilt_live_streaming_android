package com.zegocloud.uikit.prebuilt.livestreaming.internal;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.prebuilt.livestreaming.R;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoTranslationText;
import com.zegocloud.uikit.prebuilt.livestreaming.widget.ZegoRemoveCoHostButton;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.utils.Utils;
import java.util.Collections;
import java.util.Map;

public class RemoveCoHostDialog extends Dialog {

    private LinearLayout childParent;
    private int cellHeight;
    private ZegoUIKitUser userInfo;

    public RemoveCoHostDialog(@NonNull Context context, ZegoUIKitUser uiKitUser) {
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
        childParent.setBackgroundResource(R.drawable.bg_bottom_menu_dialog);
        rootView.addView(childParent);

        ZegoRemoveCoHostButton button = new ZegoRemoveCoHostButton(getContext());
        button.setInvitee(userInfo);
        childParent.addView(button, new LinearLayout.LayoutParams(-1, cellHeight));
        button.setRequestCallbackListener(new PluginCallbackListener() {
            @Override
            public void callback(Map<String, Object> result) {
                int code = (int) result.get("code");
                if (code == 0) {
                    dismiss();
                }
            }
        });
        ZegoTranslationText translationText = LiveStreamingManager.getInstance().getTranslationText();
        if (translationText != null && translationText.removeCoHostButton != null) {
            button.setText(translationText.removeCoHostButton);
        }

        View view = new View(getContext());
        view.setBackgroundColor(Color.parseColor("#1affffff"));
        childParent.addView(view,
            new LinearLayout.LayoutParams(-1, Utils.dp2px(1, getContext().getResources().getDisplayMetrics())));

        TextView cancelButton = new TextView(getContext());
        cancelButton.setText(R.string.cancel);
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
