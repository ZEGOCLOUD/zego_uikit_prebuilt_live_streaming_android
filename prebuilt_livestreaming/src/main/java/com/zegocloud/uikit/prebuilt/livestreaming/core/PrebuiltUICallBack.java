package com.zegocloud.uikit.prebuilt.livestreaming.core;

import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

public interface PrebuiltUICallBack {

    void showReceiveCoHostRequestDialog(ZegoUIKitUser inviter, int type, String data);

    void dismissReceiveCoHostRequestDialog();

    void showReceiveCoHostInviteDialog(ZegoUIKitUser inviter, int type, String data);

    void dismissReceiveCoHostInviteDialog();

    void removeCoHost(ZegoUIKitUser inviter, int type, String data);

    void showRequestCoHostButton();

    void showCoHostButtons();

    void showRedPoint();

    void hideRedPoint();

    void showTopTips(String tips, boolean green);
}
