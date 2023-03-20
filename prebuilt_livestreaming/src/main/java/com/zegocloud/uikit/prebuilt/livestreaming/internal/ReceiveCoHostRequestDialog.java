package com.zegocloud.uikit.prebuilt.livestreaming.internal;

import android.content.Context;
import com.zegocloud.uikit.prebuilt.livestreaming.R;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoDialogInfo;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoTranslationText;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.ConfirmDialog.Builder;
import com.zegocloud.uikit.prebuilt.livestreaming.widget.ZegoAcceptCoHostButton;
import com.zegocloud.uikit.prebuilt.livestreaming.widget.ZegoRefuseCoHostButton;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

public class ReceiveCoHostRequestDialog {

    private final ConfirmDialog dialog;

    public ReceiveCoHostRequestDialog(Context context, ZegoUIKitUser inviter, int type, String data) {
        ZegoAcceptCoHostButton acceptButton = new ZegoAcceptCoHostButton(context);
        acceptButton.setInviterID(inviter.userID);
        ZegoRefuseCoHostButton refuseButton = new ZegoRefuseCoHostButton(context);
        refuseButton.setInviterID(inviter.userID);
        refuseButton.setRequestCallbackListener(result -> {
            int code = (int) result.get("code");
            if (code == 0) {
                dismiss();
                LiveStreamingManager.getInstance().removeUserStatusAndCheck(inviter.userID);
            }
        });
        acceptButton.setRequestCallbackListener(result -> {
            int code = (int) result.get("code");
            if (code == 0) {
                dismiss();
                LiveStreamingManager.getInstance().removeUserStatusAndCheck(inviter.userID);
            }
        });

        String title = context.getString(R.string.livestreaming_receive_co_host_request_title);
        String message = context.getString(R.string.livestreaming_receive_co_host_request_message, inviter.userName);
        String cancelButtonName = context.getString(R.string.livestreaming_receive_co_host_request_cancel);
        String confirmButtonName = context.getString(R.string.livestreaming_receive_co_host_request_ok);
        ZegoTranslationText translationText = LiveStreamingManager.getInstance().getTranslationText();
        if (translationText != null) {
            ZegoDialogInfo dialogInfo = translationText.receivedCoHostRequestDialogInfo;
            if (dialogInfo != null && dialogInfo.title != null) {
                title = dialogInfo.title;
            }
            if (dialogInfo != null && dialogInfo.message != null) {
                message = String.format(dialogInfo.message,inviter.userName);
            }
            if (dialogInfo != null && dialogInfo.cancelButtonName != null) {
                cancelButtonName = dialogInfo.cancelButtonName;
            }
            if (dialogInfo != null && dialogInfo.confirmButtonName != null) {
                confirmButtonName = dialogInfo.confirmButtonName;
            }
        }
        acceptButton.setText(cancelButtonName);
        refuseButton.setText(confirmButtonName);
        dialog = new Builder(context).setTitle(title).setMessage(message).setCustomPositiveButton(acceptButton)
            .setCustomNegativeButton(refuseButton).build();
        dialog.getWindow().setDimAmount(0.1f);
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
