package com.zegocloud.uikit.prebuilt.livestreaming.core;

public class LanguageTextCHS extends LanguageText {

    public LanguageTextCHS() {
        startLiveStreamingButton = "开始直播";
        endCoHostButton = "结束";
        requestCoHostButton = "申请连麦";
        cancelRequestCoHostButton = "取消申请";
        removeCoHostButton = "取消连麦";
        inviteCoHostButton = "邀请 %s 连麦";
        removeUserMenuDialogButton = "将 %s 踢出房间";
        cancelMenuDialogButton = "取消";

        noHostOnline = "主播不在线";
        memberListTitle = "观众";

        sendRequestCoHostToast = "您正在申请连麦，请等待确认。";
        hostRejectCoHostRequestToast = "您的连麦申请已被拒绝。";
        inviteCoHostFailedToast = "连麦失败，请重试。";
        repeatInviteCoHostFailedToast = "您已发送连麦邀请，请等待确认。";
        audienceRejectInvitationToast = "%s 拒绝连麦。";
        requestCoHostFailed = "申请连麦失败。";

        coHostEndBecausePK = "主播已发起PK，连麦已结束";

        permissionExplainCamera = "需要摄像头访问权限才能开始直播";
        permissionExplainMic = "需要麦克风访问权限才能开始直播";
        permissionExplainMicAndCamera = "需要摄像头和麦克风访问权限才能开始直播";
        settingCamera = "请前往系统设置允许访问摄像头";
        settingMic = "请前往系统设置允许访问麦克风";
        settingMicAndCamera = "请前往系统设置允许访问摄像头和麦克风";
        ok = "确定";
        cancel = "取消";
        settings = "设置";

        you = "我";
        cohost = "连麦用户";
        host = "主播";

        agree = "同意";
        disagree = "不同意";

        hostReconnecting = "主播重新连接中";

        receivedCoHostRequestDialogInfo = new ZegoDialogInfo("请求连麦", "%s 想与您连麦",
            "不同意", "同意");
        receivedCoHostInvitationDialogInfo = new ZegoDialogInfo("邀请", "主播邀请您连麦",
            "不同意", "同意");
        endConnectionDialogInfo = new ZegoDialogInfo("结束连麦", "您确定要结束连麦吗？","取消","确定");
        stopLiveDialogInfo = new ZegoDialogInfo("停止直播",
            "您确定要停止直播吗？", "取消", "停止直播");
    }
}
