package com.zegocloud.uikit.prebuilt.livestreaming.core;

public class LanguageText {

    public String startLiveStreamingButton = "Start";
    public String endCoHostButton = "End";
    public String requestCoHostButton = "Apply to co-host";
    public String cancelRequestCoHostButton = "Cancel the application";
    public String removeCoHostButton = "Remove the co-host";
    public String inviteCoHostButton = "Invite %s to co-host";
    public String removeUserMenuDialogButton = "remove %s from the room";
    public String cancelMenuDialogButton = "Cancel";

    public String noHostOnline = "No host is online.";
    public String memberListTitle = "Audience";

    public String sendRequestCoHostToast = "You are applying to be a co-host, please wait for confirmation.";
    public String hostRejectCoHostRequestToast = "Your request to co-host with the host has been refused.";
    public String inviteCoHostFailedToast = "Failed to connect with the co-host, please try again.";
    public String repeatInviteCoHostFailedToast = "You've sent the co-host invitation, please wait for confirmation.";
    public String audienceRejectInvitationToast = "%s refused to be a co-host.";
    public String requestCoHostFailed = "Failed to apply for connection.";


    public String coHostEndBecausePK = "Host has started pk,cohost is ended";

    public String permissionExplainCamera = "Camera access is required to start a live stream";
    public String permissionExplainMic = "Microphone access is required to start a live stream";
    public String permissionExplainMicAndCamera = "Camera and microphone access are required to start a live stream.";
    public String settingCamera = "Please go to system settings to allow camera access.";
    public String settingMic = "Please go to system settings to allow microphone access.";
    public String settingMicAndCamera = "Please go to system settings to allow camera and microphone access.";
    public String ok = "OK";
    public String cancel = "Cancel";
    public String settings = "Settings";

    public String you = "You";
    public String cohost = "Co-host";
    public String host = "Host";

    public String agree = "Agree";
    public String disagree = "Disagree";
    public String hostReconnecting = "Host reconnecting";


    public ZegoDialogInfo receivedCoHostRequestDialogInfo = new ZegoDialogInfo("Co-host request",
        "%s wants to co-host with you.", "Disagree", "Agree");
    public ZegoDialogInfo receivedCoHostInvitationDialogInfo = new ZegoDialogInfo("Invitation",
        "The host is inviting you to co-host.", "Disagree", "Agree");
    public ZegoDialogInfo endConnectionDialogInfo = new ZegoDialogInfo("End the connection",
        "Do you want to end the cohosting?", "Cancel", "OK");
    public ZegoDialogInfo stopLiveDialogInfo = new ZegoDialogInfo("Stop the live",
        "Are you sure to stop the live?", "Cancel", "Stop it");

}
