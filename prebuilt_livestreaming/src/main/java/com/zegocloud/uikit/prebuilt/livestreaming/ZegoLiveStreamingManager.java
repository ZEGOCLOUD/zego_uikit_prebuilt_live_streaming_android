package com.zegocloud.uikit.prebuilt.livestreaming;

import android.app.Application;
import android.content.Context;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.plugin.adapter.utils.GenericUtils;
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.prebuilt.livestreaming.core.PrebuiltUICallBack;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoLiveStreamingRole;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoMenuBarButtonName;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoTranslationText;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.components.LiveInvitationType;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.core.PKListener;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.core.PKService;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.core.PKService.PKInfo;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.core.PKService.PKRequest;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.core.RTCRoomProperty;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.core.UserRequestCallback;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.core.ZegoLiveStreamingPKBattleRejectCode;
import com.zegocloud.uikit.service.defines.ZegoScenario;
import com.zegocloud.uikit.service.defines.ZegoUIKitCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitPluginCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitSignalingPluginInvitationListener;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import im.zego.zegoexpress.callback.IZegoMixerStartCallback;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import org.json.JSONException;
import org.json.JSONObject;
import timber.log.Timber;

public class ZegoLiveStreamingManager {

    private ZegoLiveStreamingManager() {
    }

    private static final class Holder {

        private static final ZegoLiveStreamingManager INSTANCE = new ZegoLiveStreamingManager();
    }

    public static ZegoLiveStreamingManager getInstance() {
        return ZegoLiveStreamingManager.Holder.INSTANCE;
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

    private PrebuiltUICallBack uiCallBack;
    private ZegoUIKitSignalingPluginInvitationListener invitationListener;
    private Map<String, Integer> userStatusMap = new HashMap<>();
    private Context context;
    private PKService pkService = new PKService();
    private CopyOnWriteArrayList<ZegoLiveStreamingListener> liveStreamingListenerList = new CopyOnWriteArrayList<>();
    private ZegoLiveStreamingRole currentRole;
    private ZegoTranslationText translationText;
    private RTCRoomProperty rtcRoomProperty = new RTCRoomProperty();
    private boolean muteByUser = false;
    private List<String> coHostInvitations = new ArrayList<>();
    private ZegoUIKitPrebuiltLiveStreamingConfig liveConfig;


    void init(Application application, Long appID, String appSign) {
        ZegoUIKit.init(application, appID, appSign, ZegoScenario.GENERAL);
        if (liveConfig.bottomMenuBarConfig.hostButtons.contains(ZegoMenuBarButtonName.BEAUTY_BUTTON)
            || liveConfig.bottomMenuBarConfig.coHostButtons.contains(ZegoMenuBarButtonName.BEAUTY_BUTTON)
            || liveConfig.bottomMenuBarConfig.audienceButtons.contains(ZegoMenuBarButtonName.BEAUTY_BUTTON)) {
            ZegoUIKit.getBeautyPlugin().setZegoBeautyPluginConfig(liveConfig.beautyConfig);
            ZegoUIKit.getBeautyPlugin().init(application, appID, appSign);
        }
        context = application.getApplicationContext();
    }

    void login(String userID, String userName, ZegoUIKitCallback callback) {
        ZegoUIKit.login(userID, userName);
        if (ZegoUIKit.getSignalingPlugin().isPluginExited()) {
            ZegoUIKit.getSignalingPlugin().login(userID, userName, new ZegoUIKitPluginCallback() {
                @Override
                public void onResult(int errorCode, String message) {
                    if (callback != null) {
                        callback.onResult(errorCode);
                    }
                }
            });
        } else {
            if (callback != null) {
                callback.onResult(0);
            }
        }
        invitationListener = new ZegoUIKitSignalingPluginInvitationListener() {
            @Override
            public void onInvitationReceived(ZegoUIKitUser inviter, int type, String data) {
                if (type == LiveInvitationType.REQUEST_COHOST.getValue()) {
                    String invitationID = getStringFromJson(getJsonObjectFromString(data), "invitationID");
                    coHostInvitations.add(invitationID);
                    if (Objects.equals(getHostID(), ZegoUIKit.getLocalUser().userID)) {
                        addReceiveCoHostRequestUser(inviter.userID);
                        //                        if (uiCallBack != null) {
                        //                            uiCallBack.showReceiveCoHostRequestDialog(inviter, type, data);
                        //                        }
                    }
                } else if (type == LiveInvitationType.INVITE_TO_COHOST.getValue()) {
                    String invitationID = getStringFromJson(getJsonObjectFromString(data), "invitationID");
                    coHostInvitations.add(invitationID);
                    if (Objects.equals(getHostID(), inviter.userID)) {
                        if (uiCallBack != null) {
                            uiCallBack.showReceiveCoHostInviteDialog(inviter, type, data);
                        }
                    }
                } else if (type == LiveInvitationType.REMOVE_COHOST.getValue()) {
                    String invitationID = getStringFromJson(getJsonObjectFromString(data), "invitationID");
                    coHostInvitations.add(invitationID);
                    if (uiCallBack != null) {
                        uiCallBack.removeCoHost(inviter, type, data);
                    }
                } else if (type == LiveInvitationType.PK.getValue()) {
                    pkService.onInvitationReceived(inviter, type, data);
                }

            }

            @Override
            public void onInvitationTimeout(ZegoUIKitUser inviter, String data) {
                String invitationID = getStringFromJson(getJsonObjectFromString(data), "invitationID");
                if (pkService.isPKInvitation(invitationID)) {
                    pkService.onInvitationTimeout(inviter, data);
                } else {
                    if (coHostInvitations.contains(invitationID)) {
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

                }
            }

            @Override
            public void onInvitationResponseTimeout(List<ZegoUIKitUser> invitees, String data) {
                String invitationID = getStringFromJson(getJsonObjectFromString(data), "invitationID");
                if (pkService.isPKInvitation(invitationID)) {
                    pkService.onInvitationResponseTimeout(invitees, data);
                } else {
                    if (coHostInvitations.contains(invitationID)) {
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
                }
            }

            @Override
            public void onInvitationAccepted(ZegoUIKitUser invitee, String data) {
                String invitationID = getStringFromJson(getJsonObjectFromString(data), "invitationID");
                if (pkService.isPKInvitation(invitationID)) {
                    pkService.onInvitationAccepted(invitee, data);
                } else {
                    if (coHostInvitations.contains(invitationID)) {
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
                }
            }

            @Override
            public void onInvitationRefused(ZegoUIKitUser invitee, String data) {
                String invitationID = getStringFromJson(getJsonObjectFromString(data), "invitationID");
                if (pkService.isPKInvitation(invitationID)) {
                    pkService.onInvitationRefused(invitee, data);
                } else {
                    if (coHostInvitations.contains(invitationID)) {
                        if (Objects.equals(getHostID(), invitee.userID)) {
                            // invitee is host.then me is audience,
                            // and the host refused my cohost request
                            removeUserStatusAndCheck(invitee.userID);
                            if (uiCallBack != null) {
                                String string = context.getString(R.string.livestreaming_host_reject_co_host_tips);
                                if (translationText != null && translationText.hostRejectCoHostRequestToast != null) {
                                    string = translationText.hostRejectCoHostRequestToast;
                                }
                                showTopTips(string, false);
                            }
                            if (uiCallBack != null) {
                                uiCallBack.showRequestCoHostButton();
                            }
                        } else {
                            // invitee is not host,then me is host,
                            // and the audience refused my cohost invite
                            removeUserStatus(invitee.userID);
                            if (uiCallBack != null) {
                                String string = context.getString(R.string.livestreaming_refuse_co_host_tips,
                                    invitee.userName);
                                if (translationText != null && translationText.audienceRejectInvitationToast != null) {
                                    string = String.format(translationText.audienceRejectInvitationToast,
                                        invitee.userName);
                                }
                                showTopTips(string, false);
                            }
                        }
                    }
                }
            }

            @Override
            public void onInvitationCanceled(ZegoUIKitUser inviter, String data) {
                String invitationID = getStringFromJson(getJsonObjectFromString(data), "invitationID");
                if (pkService.isPKInvitation(invitationID)) {
                    pkService.onInvitationCanceled(inviter, data);
                } else {
                    if (coHostInvitations.contains(invitationID)) {
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
                }
            }
        };
        ZegoUIKit.getSignalingPlugin().addInvitationListener(invitationListener);
    }

    void joinRoom(String roomID, ZegoUIKitCallback callback) {
        if (ZegoUIKit.getSignalingPlugin().isPluginExited()) {
            ZegoUIKit.getSignalingPlugin().joinRoom(roomID, new ZegoUIKitPluginCallback() {
                @Override
                public void onResult(int errorCode, String message) {
                    if (errorCode == 0) {
                        ZegoUIKit.joinRoom(roomID, liveConfig.markAsLargeRoom, callback);
                    } else {
                        if (callback != null) {
                            callback.onResult(errorCode);
                        }
                    }
                }
            });
        } else {
            ZegoUIKit.joinRoom(roomID, liveConfig.markAsLargeRoom, callback);
        }
        pkService.addRoomListeners();
        pkService.addListener(new PKListener() {
            @Override
            public void onPKStarted() {
                List<String> cancelList = new ArrayList<>();
                List<String> refuseList = new ArrayList<>();
                for (Entry<String, Integer> entry : userStatusMap.entrySet()) {
                    String userID = entry.getKey();
                    int status = entry.getValue();
                    if (status == INVITE_JOIN_COHOST || status == SEND_COHOST_REQUEST) {
                        cancelList.add(userID);
                    } else if (status == RECEIVE_COHOST_REQUEST) {
                        refuseList.add(userID);
                    }
                }
                if (!cancelList.isEmpty()) {
                    ZegoUIKit.getSignalingPlugin().cancelInvitation(cancelList, "", null);
                }
                for (String userID : refuseList) {
                    ZegoUIKit.getSignalingPlugin().refuseInvitation(userID, "", new PluginCallbackListener() {
                        @Override
                        public void callback(Map<String, Object> result) {

                        }
                    });
                }
                userStatusMap.clear();
                if (isCurrentUserCoHost()) {
                    endCoHost();
                }
                if (uiCallBack != null) {
                    uiCallBack.hideRedPoint();
                }
                if (uiCallBack != null) {
                    uiCallBack.dismissReceiveCoHostInviteDialog();
                }
            }
        });
    }

    private JSONObject getJsonObjectFromString(String s) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(s);
        } catch (JSONException e) {
            jsonObject = new JSONObject();
        }
        return jsonObject;
    }

    private String getStringFromJson(JSONObject jsonObject, String key) {
        try {
            if (jsonObject.has(key)) {
                return jsonObject.getString(key);
            } else {
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    void leaveRoom() {
        if (isCurrentUserHost()) {
            Map<String, String> map = new HashMap<>();
            map.put(RTCRoomProperty.HOST, RTCRoomProperty.HOST_REMOVE);
            map.put(RTCRoomProperty.LIVE_STATUS, RTCRoomProperty.LIVE_STATUS_STOP);
            ZegoLiveStreamingManager.getInstance().updateRoomProperties(map);

            stopPKBattle();
        }
        setCurrentRole(null);
        ZegoUIKit.leaveRoom();
        ZegoUIKit.getSignalingPlugin().leaveRoom(null);
        removeRoomData();
        removeRoomListeners();
        muteByUser = false;
        coHostInvitations.clear();

        removeUserData();
        removeUserListeners();
    }

    void logout() {
        ZegoUIKit.logout();
        ZegoUIKit.getSignalingPlugin().logout();
        removeUserData();
        removeUserListeners();
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

    private boolean isAnyUserCoHostRequestExisted() {
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

    public String getHostID() {
        return rtcRoomProperty.getHostID();
    }

    public boolean isLiveStarted() {
        return rtcRoomProperty.isLiveStarted();
    }

    public boolean isHost(String userID) {
        return Objects.equals(getHostID(), userID);
    }

    public boolean isCurrentUserHost() {
        ZegoUIKitUser localUser = ZegoUIKit.getLocalUser();
        return localUser != null && Objects.equals(localUser.userID, getHostID());
    }

    public boolean isCurrentUserCoHost() {
        ZegoUIKitUser localUser = ZegoUIKit.getLocalUser();
        return localUser != null && !isCurrentUserHost() && (localUser.isMicrophoneOn || localUser.isCameraOn);
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

    public void endCoHost() {
        ZegoUIKitUser localUser = ZegoUIKit.getLocalUser();
        if (localUser == null) {
            return;
        }
        ZegoUIKit.turnCameraOn(localUser.userID, false);
        ZegoUIKit.turnMicrophoneOn(localUser.userID, false);
        setCurrentRole(ZegoLiveStreamingRole.AUDIENCE);
    }

    public void setCurrentRole(ZegoLiveStreamingRole liveStreamingRole) {
        boolean notifyChange = currentRole != liveStreamingRole;
        currentRole = liveStreamingRole;
        if (notifyChange) {
            for (ZegoLiveStreamingListener listener : liveStreamingListenerList) {
                listener.onRoleChanged(currentRole);
            }
        }
    }

    public ZegoLiveStreamingRole getCurrentUserRole() {
        return currentRole;
    }

    public void stopPKBattle() {
        pkService.sendPKBattlesStopRequest();
    }

    public PKInfo getPKInfo() {
        return pkService.getPKInfo();
    }

    void stopPKBattleInner() {
        pkService.stopPKBattle();
    }

    public void acceptIncomingPKBattleRequest(String requestID, String anotherHostLiveID, ZegoUIKitUser anotherHostUser,
        String customData) {
        pkService.acceptPKBattleStartRequest(requestID, anotherHostLiveID, anotherHostUser, customData);
    }

    public void acceptIncomingPKBattleRequest(String requestID, String anotherHostLiveID,
        ZegoUIKitUser anotherHostUser) {
        pkService.acceptPKBattleStartRequest(requestID, anotherHostLiveID, anotherHostUser, "");
    }

    public void rejectPKBattleStartRequest(String requestID) {
        pkService.rejectPKBattleStartRequest(requestID, ZegoLiveStreamingPKBattleRejectCode.HOST_REJECT.ordinal());
    }

    public boolean isPKUser(String userID) {
        return pkService.isPKUser(userID);
    }

    public boolean isAnotherHostMuted() {
        return pkService.isPKUserMuted();
    }

    public void muteAnotherHostAudio(boolean mute, ZegoUIKitCallback callback) {
        pkService.mutePKUser(mute, new IZegoMixerStartCallback() {
            @Override
            public void onMixerStartResult(int errorCode, JSONObject extendedData) {
                if (callback != null) {
                    callback.onResult(errorCode);
                }
            }
        });
    }

    public void setPrebuiltConfig(ZegoUIKitPrebuiltLiveStreamingConfig config) {
        this.liveConfig = config;
    }

    public ZegoUIKitPrebuiltLiveStreamingConfig getPrebuiltConfig() {
        return liveConfig;
    }


    public PKRequest getSendPKStartRequest() {
        return pkService.getSendPKStartRequest();
    }

    public void sendPKBattleRequest(String anotherHostUserID, int timeout, String customData,
        UserRequestCallback callback) {
        pkService.sendPKBattlesStartRequest(anotherHostUserID, timeout, customData, callback);
    }

    public void sendPKBattleRequest(String anotherHostUserID, UserRequestCallback callback) {
        pkService.sendPKBattlesStartRequest(anotherHostUserID, 60, "", callback);
    }

    public void cancelPKBattleRequest(UserRequestCallback callback) {
        pkService.cancelPKBattleStartRequest("", callback);
    }

    public void cancelPKBattleRequest(String customData, UserRequestCallback callback) {
        pkService.cancelPKBattleStartRequest(customData, callback);
    }

    public void startPKBattleWith(String anotherHostLiveID, String anotherHostUserID, String anotherHostName) {
        pkService.startPKBattleWith(anotherHostLiveID, anotherHostUserID, anotherHostName);
    }

    public void addLiveStreamingListener(ZegoLiveStreamingListener listener) {
        pkService.addListener(listener);
        liveStreamingListenerList.add(listener);
    }

    public void removeLiveStreamingListener(ZegoLiveStreamingListener listener) {
        pkService.removeListener(listener);
        liveStreamingListenerList.remove(listener);
    }

    public void removeRoomListeners() {
        pkService.removeRoomListeners();
        liveStreamingListenerList.clear();
        setPrebuiltUiCallBack(null);
    }

    public void removeRoomData() {
        pkService.removeRoomData();
    }

    public void removeUserListeners() {
        pkService.removeUserListeners();
        ZegoUIKit.getSignalingPlugin().removeInvitationListener(invitationListener);
        removeRoomListeners();
    }

    public void removeUserData() {
        pkService.removeUserData();
        userStatusMap.clear();
    }

    public static String generateCameraStreamID(String roomID, String userID) {
        return roomID + "_" + userID + "_main";
    }

    public void startPublishingStream() {
        String currentRoomID = ZegoUIKit.getRoom().roomID;
        ZegoUIKitUser localUser = ZegoUIKit.getLocalUser();
        String streamID = generateCameraStreamID(currentRoomID, localUser.userID);
        ZegoUIKit.startPublishingStream(streamID);
    }

    public void stopPublishStream() {
        ZegoUIKit.stopPublishingStream();
    }

    public void unMuteAllAudioVideo() {
        resumePlayingAllAudioVideo(true);
    }

    void resumePlayingAllAudioVideo(boolean startByUser) {
        Timber.d("resumePlayingAllAudioVideo() called with: startByUser = [" + startByUser + "]");
        if (muteByUser) {
            if (startByUser) {
                muteByUser = false;
                if (rtcRoomProperty.isLiveStarted()) {
                    ZegoUIKit.startPlayingAllAudioVideo();
                }
            }
        } else {
            if (rtcRoomProperty.isLiveStarted()) {
                ZegoUIKit.startPlayingAllAudioVideo();
            }
        }
    }

    public void muteAllAudioVideo() {
        pausePlayingAllAudioVideo(true);
    }

    void pausePlayingAllAudioVideo(boolean muteByUser) {
        Timber.d("pausePlayingAllAudioVideo() called with: muteByUser = [" + muteByUser + "]");
        this.muteByUser = muteByUser;
        ZegoUIKit.stopPlayingAllAudioVideo();
    }

    public void updateRoomProperties(Map<String, String> newProperties) {
        rtcRoomProperty.updateRoomProperties(newProperties);
    }

    public void sendCoHostRequest(List<String> invitees, int timeout, int type, String data,
        PluginCallbackListener callbackListener) {
        ZegoUIKit.getSignalingPlugin().sendInvitation(invitees, timeout, type, data, new PluginCallbackListener() {
            @Override
            public void callback(Map<String, Object> result) {
                int code = (int) result.get("code");
                if (code == 0) {
                    String invitationID = (String) result.get("invitationID");
                    coHostInvitations.add(invitationID);
                }
                if (callbackListener != null) {
                    callbackListener.callback(result);
                }
            }
        });
    }

    public interface ZegoLiveStreamingListener extends PKListener {

        default void onRoleChanged(ZegoLiveStreamingRole liveStreamingRole) {
        }
    }
}
