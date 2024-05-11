package com.zegocloud.uikit.prebuilt.livestreaming.core;

import com.zegocloud.uikit.internal.ZegoUIKitLanguage;

public class ZegoTranslationText {

    public String startLiveStreamingButton;
    public String endCoHostButton;
    public String requestCoHostButton;
    public String cancelRequestCoHostButton;
    public String removeCoHostButton;
    public String inviteCoHostButton;
    public String removeUserMenuDialogButton;
    public String cancelMenuDialogButton;

    public String noHostOnline;
    public String memberListTitle;

    public String sendRequestCoHostToast;
    public String hostRejectCoHostRequestToast;
    public String inviteCoHostFailedToast;
    public String repeatInviteCoHostFailedToast;
    public String audienceRejectInvitationToast;
    public String requestCoHostFailed;

    public String coHostEndBecausePK;

    public String permissionExplainCamera;
    public String permissionExplainMic;
    public String permissionExplainMicAndCamera;
    public String settingCamera;
    public String settingMic;
    public String settingMicAndCamera;
    public String ok;
    public String cancel;
    public String settings;

    public String you;
    public String cohost;
    public String host;

    public String agree;
    public String disagree;

    public String hostReconnecting;

    public ZegoDialogInfo receivedCoHostRequestDialogInfo;
    public ZegoDialogInfo receivedCoHostInvitationDialogInfo;
    public ZegoDialogInfo endConnectionDialogInfo;
    public ZegoDialogInfo stopLiveDialogInfo;


    private LanguageText languageText = new LanguageTextEnglish();

    public LanguageText getLanguageText() {
        return languageText;
    }

    public ZegoTranslationText() {
        languageText = new LanguageTextEnglish();
    }

    public ZegoTranslationText(ZegoUIKitLanguage language) {
        if (language == ZegoUIKitLanguage.CHS) {
            languageText = new LanguageTextCHS();
        }
        startLiveStreamingButton = languageText.startLiveStreamingButton;
        endCoHostButton = languageText.endCoHostButton;
        requestCoHostButton = languageText.requestCoHostButton;
        cancelRequestCoHostButton = languageText.cancelRequestCoHostButton;
        removeCoHostButton = languageText.removeCoHostButton;
        inviteCoHostButton = languageText.inviteCoHostButton;
        removeUserMenuDialogButton = languageText.removeUserMenuDialogButton;
        cancelMenuDialogButton = languageText.cancelMenuDialogButton;

        noHostOnline = languageText.noHostOnline;
        memberListTitle = languageText.memberListTitle;

        sendRequestCoHostToast = languageText.sendRequestCoHostToast;
        hostRejectCoHostRequestToast = languageText.hostRejectCoHostRequestToast;
        inviteCoHostFailedToast = languageText.inviteCoHostFailedToast;
        repeatInviteCoHostFailedToast = languageText.repeatInviteCoHostFailedToast;
        audienceRejectInvitationToast = languageText.audienceRejectInvitationToast;
        requestCoHostFailed = languageText.requestCoHostFailed;

        permissionExplainCamera = languageText.permissionExplainCamera;
        permissionExplainMic = languageText.permissionExplainMic;
        permissionExplainMicAndCamera = languageText.permissionExplainMicAndCamera;
        settingCamera = languageText.settingCamera;
        settingMic = languageText.settingMic;
        settingMicAndCamera = languageText.settingMicAndCamera;
        ok = languageText.ok;
        cancel = languageText.cancel;
        settings = languageText.settings;
        you = languageText.you;
        cohost = languageText.cohost;
        host = languageText.host;

        agree = languageText.agree;
        disagree = languageText.disagree;
        hostReconnecting = languageText.hostReconnecting;

        receivedCoHostRequestDialogInfo = languageText.receivedCoHostRequestDialogInfo;
        receivedCoHostInvitationDialogInfo = languageText.receivedCoHostInvitationDialogInfo;
        endConnectionDialogInfo = languageText.endConnectionDialogInfo;
        stopLiveDialogInfo = languageText.stopLiveDialogInfo;
    }

    public ZegoUIKitLanguage getLanguage() {
        if (languageText instanceof LanguageTextCHS) {
            return ZegoUIKitLanguage.CHS;
        }
        return ZegoUIKitLanguage.ENGLISH;
    }
}
