package com.zegocloud.uikit.prebuilt.livestreaming.internal.components;

import android.content.Context;
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoLiveStreamingManager;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoTranslationText;
import com.zegocloud.uikit.prebuilt.livestreaming.widget.ZegoAcceptCoHostButton;
import com.zegocloud.uikit.prebuilt.livestreaming.widget.ZegoRefuseCoHostButton;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

public class ReceiveCoHostInviteDialog {

    private final ConfirmDialog dialog;

    public ReceiveCoHostInviteDialog(Context context, ZegoUIKitUser inviter, int type, String data) {
        ZegoAcceptCoHostButton acceptButton = new ZegoAcceptCoHostButton(context);
        acceptButton.setInviterID(inviter.userID);
        ZegoRefuseCoHostButton refuseButton = new ZegoRefuseCoHostButton(context);
        refuseButton.setInviterID(inviter.userID);
        refuseButton.setRequestCallbackListener(result -> {
            int code = (int) result.get("code");
            if (code == 0) {
                dismiss();
            }
        });
        acceptButton.setRequestCallbackListener(result -> {
            int code = (int) result.get("code");
            if (code == 0) {
                dismiss();
            }
        });
        String title = "";
        String message = "";
        ZegoTranslationText translationText = ZegoLiveStreamingManager.getInstance().getTranslationText();
        if (translationText != null && translationText.receivedCoHostRequestDialogInfo != null) {
            title = translationText.receivedCoHostRequestDialogInfo.title;
            message = translationText.receivedCoHostRequestDialogInfo.message;
        }
        dialog = new ConfirmDialog.Builder(context).setTitle(title)
            .setMessage(message)
            .setCustomPositiveButton(acceptButton)
            .setCustomNegativeButton(refuseButton).build();
    }

    public void show() {
        if (dialog != null) {
            dialog.show();
        }
    }

    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}
