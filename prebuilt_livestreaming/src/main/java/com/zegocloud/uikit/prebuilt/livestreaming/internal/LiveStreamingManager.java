package com.zegocloud.uikit.prebuilt.livestreaming.internal;

import android.content.Context;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.prebuilt.livestreaming.R;
import com.zegocloud.uikit.prebuilt.livestreaming.core.PrebuiltUICallBack;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoTranslationText;
import com.zegocloud.uikit.service.defines.ZegoUIKitSignalingPluginInvitationListener;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.utils.GenericUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LiveStreamingManager {

    private LiveStreamingManager() {
    }

    private static final class Holder {

        private static final LiveStreamingManager INSTANCE = new LiveStreamingManager();
    }

    public static LiveStreamingManager getInstance() {
        return LiveStreamingManager.Holder.INSTANCE;
    }

    // me is host ,invite audience to join cohost.
    public static final int INVITE_JOIN_COHOST = 1;

    // me is audience ,request to join cohost.
    public static final int SEND_COHOST_REQUEST = 2;
    private static final int INVITE_TIMEOUT = -1;
    private static final int INVITE_CANCELED = -2;

    // me is host ,receive audience's cohost request.
    public static final int RECEIVE_COHOST_REQUEST = 4;

    private static final int REQUEST_TIMEOUT = -3;
    private static final int REQUEST_CANCELED = -4;

    private Context context;
    private PrebuiltUICallBack uiCallBack;
    private ZegoUIKitSignalingPluginInvitationListener invitationListener;
    private Map<String, Integer> userStatusMap = new HashMap<>();

    private ZegoTranslationText translationText;

    public void init(Context context) {
        this.context = context.getApplicationContext();
        invitationListener = new ZegoUIKitSignalingPluginInvitationListener() {
            @Override
            public void onInvitationReceived(ZegoUIKitUser inviter, int type, String data) {
                if (type == LiveInvitationType.REQUEST_COHOST.getValue()) {
                    if (Objects.equals(getHostID(), ZegoUIKit.getLocalUser().userID)) {
                        addReceiveCoHostRequestUser(inviter.userID);
                        //                        if (uiCallBack != null) {
                        //                            uiCallBack.showReceiveCoHostRequestDialog(inviter, type, data);
                        //                        }
                    }
                } else if (type == LiveInvitationType.INVITE_TO_COHOST.getValue()) {
                    if (Objects.equals(getHostID(), inviter.userID)) {
                        if (uiCallBack != null) {
                            uiCallBack.showReceiveCoHostInviteDialog(inviter, type, data);
                        }
                    }
                } else if (type == LiveInvitationType.REMOVE_COHOST.getValue()) {
                    if (uiCallBack != null) {
                        uiCallBack.removeCoHost(inviter, type, data);
                    }
                }
            }

            @Override
            public void onInvitationTimeout(ZegoUIKitUser inviter, String data) {
                if (Objects.equals(getHostID(), inviter.userID)) {
                    // if inviter is host,then me is audience,
                    // and no respond to host's cohost invite
                    removeUserStatus(inviter.userID);
                    if (uiCallBack != null) {
                        uiCallBack.dismissReceiveCoHostInviteDialog();
                    }
                } else {
                    // if inviter not host,then me is the host,
                    // and no respond to audience's cohost request
                    removeUserStatusAndCheck(inviter.userID);
                    //                    if (uiCallBack != null) {
                    //                        uiCallBack.dismissReceiveCoHostRequestDialog();
                    //                    }
                }
            }

            @Override
            public void onInvitationResponseTimeout(List<ZegoUIKitUser> invitees, String data) {
                for (ZegoUIKitUser invitee : invitees) {
                    removeUserStatus(invitee.userID);
                }
                List<String> inviteeIDList = GenericUtils.map(invitees, uiKitUser -> uiKitUser.userID);
                if (inviteeIDList.contains(getHostID())) {
                    // invitee is host.then me is audience,
                    // and the host no respond to my cohost request
                    if (uiCallBack != null) {
                        uiCallBack.showRequestCoHostButton();
                    }
                } else {
                    // invitee is not host,then me is host,
                    // and the audience no respond to my cohost invite
                }
            }

            @Override
            public void onInvitationAccepted(ZegoUIKitUser invitee, String data) {
                if (Objects.equals(getHostID(), invitee.userID)) {
                    // invitee is host.then me is audience,
                    // and the host accept my cohost request
                    removeUserStatusAndCheck(invitee.userID);
                    if (uiCallBack != null) {
                        uiCallBack.showCoHostButtons();
                    }
                } else {
                    // invitee is not host,then me is host,
                    // and the audience accept my cohost invite
                    removeUserStatus(invitee.userID);
                }

            }

            @Override
            public void onInvitationRefused(ZegoUIKitUser invitee, String data) {
                if (Objects.equals(getHostID(), invitee.userID)) {
                    // invitee is host.then me is audience,
                    // and the host refused my cohost request
                    removeUserStatusAndCheck(invitee.userID);
                    if (uiCallBack != null) {
                        String string = context.getString(R.string.host_reject_co_host_tips);
                        if (translationText != null && translationText.hostRejectCoHostRequestToast != null) {
                            string = translationText.hostRejectCoHostRequestToast;
                        }
                        uiCallBack.showTopTips(string, false);
                    }
                    if (uiCallBack != null) {
                        uiCallBack.showRequestCoHostButton();
                    }
                } else {
                    // invitee is not host,then me is host,
                    // and the audience refused my cohost invite
                    removeUserStatus(invitee.userID);
                    if (uiCallBack != null) {
                        String string = context.getString(R.string.refuse_co_host_tips, invitee.userName);
                        if (translationText != null && translationText.audienceRejectInvitationToast != null) {
                            string = String.format(translationText.audienceRejectInvitationToast, invitee.userName);
                        }
                        uiCallBack.showTopTips(string, false);
                    }
                }
            }

            @Override
            public void onInvitationCanceled(ZegoUIKitUser inviter, String data) {
                if (Objects.equals(getHostID(), inviter.userID)) {
                    // if inviter is host,then me is audience,
                    // and host canceled cohost invite to me
                    removeUserStatus(inviter.userID);
                } else {
                    // if inviter not host,then me is host,
                    // and the audience canceled cohost request to me
                    removeUserStatusAndCheck(inviter.userID);
                    if (uiCallBack != null) {
                        uiCallBack.dismissReceiveCoHostRequestDialog();
                    }
                }
            }
        };
        ZegoUIKit.getSignalingPlugin().addInvitationListener(invitationListener);
    }

    public void unInit() {
        ZegoUIKit.getSignalingPlugin().removeInvitationListener(invitationListener);
        setPrebuiltUiCallBack(null);
        userStatusMap.clear();
    }

    public void addReceiveCoHostRequestUser(String userID) {
        setUserStatus(userID, RECEIVE_COHOST_REQUEST);
        if (Objects.equals(ZegoUIKit.getLocalUser().userID, getHostID())) {
            if (uiCallBack != null) {
                uiCallBack.showRedPoint();
            }
        }
    }

    public void removeUserStatusAndCheck(String userID) {
        Integer remove = removeUserStatus(userID);
        if (remove != null) {
            if (Objects.equals(ZegoUIKit.getLocalUser().userID, getHostID())) {
                if (isAnyUserCoHostRequestExisted()) {
                    if (uiCallBack != null) {
                        uiCallBack.showRedPoint();
                    }
                } else {
                    if (uiCallBack != null) {
                        uiCallBack.hideRedPoint();
                    }
                }
            }
        }
    }

    public boolean isUserCoHostRequestExisted(String userID) {
        Integer integer = userStatusMap.get(userID);
        if (integer != null) {
            return integer == RECEIVE_COHOST_REQUEST;
        } else {
            return false;
        }
    }

    public boolean isAnyUserCoHostRequestExisted() {
        for (Integer value : userStatusMap.values()) {
            if (value == RECEIVE_COHOST_REQUEST) {
                return true;
            }
        }
        return false;
    }

    public void setUserStatus(String userID, int status) {
        userStatusMap.put(userID, status);
    }

    public Integer removeUserStatus(String userID) {
        return userStatusMap.remove(userID);
    }

    public void showTopTips(String message, boolean green) {
        if (uiCallBack != null) {
            uiCallBack.showTopTips(message, green);
        }
    }

    public boolean hasInviteUserCoHost(String userID) {
        Integer integer = userStatusMap.get(userID);
        if (integer != null) {
            return integer == INVITE_JOIN_COHOST;
        } else {
            return false;
        }
    }

    private String getHostID() {
        return ZegoUIKit.getRoomProperties().get("host");
    }

    public void setTranslationText(ZegoTranslationText translationText) {
        this.translationText = translationText;
    }

    public ZegoTranslationText getTranslationText() {
        return translationText;
    }

    public void setPrebuiltUiCallBack(PrebuiltUICallBack uiCallBack) {
        this.uiCallBack = uiCallBack;
    }
}
