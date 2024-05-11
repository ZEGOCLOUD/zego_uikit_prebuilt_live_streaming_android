package com.zegocloud.uikit.prebuilt.livestreaming.internal.core;

import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoLiveStreamingManager;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.components.LiveInvitationType;
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.service.defines.ZegoRoomPropertyUpdateListener;
import com.zegocloud.uikit.service.defines.ZegoUIKitSignalingPluginRoomAttributesOperatedCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitSignalingPluginRoomPropertyUpdateListener;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.service.express.IExpressEngineEventHandler;
import im.zego.zegoexpress.callback.IZegoMixerStartCallback;
import im.zego.zegoexpress.callback.IZegoMixerStopCallback;
import im.zego.zegoexpress.constants.ZegoMixRenderMode;
import im.zego.zegoexpress.constants.ZegoMixerInputContentType;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.entity.ZegoMixerInput;
import im.zego.zegoexpress.entity.ZegoMixerOutput;
import im.zego.zegoexpress.entity.ZegoMixerTask;
import im.zego.zegoexpress.entity.ZegoMixerVideoConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import org.json.JSONException;
import org.json.JSONObject;
import timber.log.Timber;

public class PKService {

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable syncSEIRunnable;
    private Runnable checkSEIRunnable;
    private PKInfo currentPKInfo;
    private Map<String, Long> seiTimeMap = new HashMap<>();
    private Map<String, Boolean> seiStateMap = new HashMap<>();
    private Map<String, String> signalRoomProperties = new HashMap<>();
    private PKRequest sendPKStartRequest;
    private PKRequest recvPKStartRequest;
    private boolean hasNotified = false;
    private boolean mutePKUser = false;
    private ZegoMixerTask task;
    private CopyOnWriteArrayList<PKListener> listenerList = new CopyOnWriteArrayList<>();
    private ZegoUIKitSignalingPluginRoomPropertyUpdateListener pluginRoomPropertyUpdateListener;
    private ZegoRoomPropertyUpdateListener expressRoomPropertyUpdateListener;
    private List<String> pkInvitations = new ArrayList<>();

    public PKService() {
        syncSEIRunnable = new Runnable() {
            @Override
            public void run() {
                JSONObject jsonObject = new JSONObject();
                ZegoUIKitUser localUser = ZegoUIKit.getLocalUser();
                boolean cameraEnabled = ZegoUIKit.isCameraOn(localUser.userID);
                boolean microphoneOpen = ZegoUIKit.isMicrophoneOn(localUser.userID);
                try {
                    jsonObject.put("type", 0);
                    jsonObject.put("sender_id", localUser.userID);
                    jsonObject.put("cam", cameraEnabled);
                    jsonObject.put("mic", microphoneOpen);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                ZegoUIKit.sendSEI(jsonObject.toString());

                handler.postDelayed(syncSEIRunnable, 500);
            }
        };

        checkSEIRunnable = new Runnable() {
            @Override
            public void run() {
                for (Entry<String, Long> entry : seiTimeMap.entrySet()) {
                    String userID = entry.getKey();
                    Long timeStamp = entry.getValue();
                    boolean isTimeOut = System.currentTimeMillis() - timeStamp > 5000;
                    if (seiStateMap.containsKey(userID)) {
                        boolean lastTimeOutState = seiStateMap.get(userID);
                        seiStateMap.put(userID, isTimeOut);
                        if (isTimeOut != lastTimeOutState) {
                            for (PKListener listener : listenerList) {
                                listener.onPKUserDisConnected(userID, isTimeOut);
                            }
                        }
                    } else {
                        seiStateMap.put(userID, isTimeOut);
                        for (PKListener listener : listenerList) {
                            listener.onPKUserDisConnected(userID, isTimeOut);
                        }
                    }
                }
                handler.postDelayed(checkSEIRunnable, 1000);
            }
        };
    }

    public void onInvitationCanceled(ZegoUIKitUser inviter, String data) {
        String invitationID = getStringFromJson(getJsonObjectFromString(data), "invitationID");
        PKExtendedData pkExtendedData = PKExtendedData.parse(data);
        if (invitationID != null) {
            if (recvPKStartRequest != null && invitationID.equals(recvPKStartRequest.requestID)) {
                inviter.userID = recvPKStartRequest.anotherUserID;
                inviter.userName = recvPKStartRequest.anotherUserName;
                recvPKStartRequest = null;
                if (pkExtendedData != null && pkExtendedData.type == PKExtendedData.START_PK) {
                    String customData = "";
                    try {
                        JSONObject jsonObject = new JSONObject(data);
                        customData = jsonObject.getString("custom_data");
                    } catch (JSONException e) {
                    }
                    for (PKListener listener : listenerList) {
                        listener.onIncomingPKBattleRequestCanceled(invitationID, inviter, customData);
                    }
                }
            }
            pkInvitations.remove(invitationID);
        }
    }

    public void onInvitationRefused(ZegoUIKitUser invitee, String data) {
        JSONObject jsonObject = getJsonObjectFromString(data);
        String invitationID = getStringFromJson(jsonObject, "invitationID");
        PKExtendedData pkExtendedData = PKExtendedData.parse(data);
        if (sendPKStartRequest != null && Objects.equals(invitationID, sendPKStartRequest.requestID)) {
            invitee.userName = pkExtendedData.userName;
            sendPKStartRequest = null;
            int reason = 0;
            if (jsonObject.has("reason")) {
                try {
                    reason = jsonObject.getInt("reason");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
            for (PKListener listener : listenerList) {
                listener.onOutgoingPKBattleRequestRejected(reason, invitee);
            }
            pkInvitations.remove(invitationID);
        } else {
            // if is resume be rejected,need to delete room attributes
            if (pkExtendedData != null && pkExtendedData.type == PKExtendedData.RESUME_PK) {
                deletePKRoomAttributes();
                pkInvitations.remove(invitationID);
            }
        }
    }

    public void onInvitationAccepted(ZegoUIKitUser invitee, String data) {
        Timber.d("onInvitationAccepted() called with: invitee = [" + invitee + "], data = [" + data + "]");
        String invitationID = getStringFromJson(getJsonObjectFromString(data), "invitationID");
        PKExtendedData pkExtendedData = PKExtendedData.parse(data);
        if (pkExtendedData != null && invitationID != null) {
            if (pkExtendedData.type == PKExtendedData.START_PK) {
                invitee.userName = pkExtendedData.userName;
                sendPKStartRequest = null;
                for (PKListener listener : listenerList) {
                    listener.onOutgoingPKBattleRequestAccepted(pkExtendedData.roomID, invitee);
                }
            } else if (pkExtendedData.type == PKExtendedData.END_PK) {
                stopPKBattle();
            } else if (pkExtendedData.type == PKExtendedData.RESUME_PK) {
                // re enter room
                String pk_room = signalRoomProperties.get("pk_room");
                String pk_user_id = signalRoomProperties.get("pk_user_id");
                String pk_user_name = signalRoomProperties.get("pk_user_name");
                String pk_seq = signalRoomProperties.get("pk_seq");
                String host = signalRoomProperties.get("host");
                PKInfo pkInfo = new PKInfo(new ZegoUIKitUser(pk_user_id, pk_user_name), pk_room);
                pkInfo.hostUserID = host;
                pkInfo.seq = Long.parseLong(pk_seq);
                setCurrentPKInfo(pkInfo);
                startPKBattle();
            }
            pkInvitations.remove(invitationID);
        }
    }

    public void onInvitationResponseTimeout(List<ZegoUIKitUser> invitees, String data) {
        String invitationID = getStringFromJson(getJsonObjectFromString(data), "invitationID");
        if (sendPKStartRequest != null && Objects.equals(invitationID, sendPKStartRequest.requestID)) {
            ZegoUIKitUser anotherHost = new ZegoUIKitUser(sendPKStartRequest.anotherUserID,
                sendPKStartRequest.anotherUserName);
            sendPKStartRequest = null;
            for (PKListener listener : listenerList) {
                listener.onOutgoingPKBattleRequestTimeout(invitationID, anotherHost);
            }
            pkInvitations.remove(invitationID);
        }
    }

    public void onInvitationTimeout(ZegoUIKitUser inviter, String data) {
        String invitationID = getStringFromJson(getJsonObjectFromString(data), "invitationID");
        if (recvPKStartRequest != null && Objects.equals(invitationID, recvPKStartRequest.requestID)) {
            inviter.userID = recvPKStartRequest.anotherUserID;
            inviter.userName = recvPKStartRequest.anotherUserName;
            recvPKStartRequest = null;
            for (PKListener listener : listenerList) {
                listener.onIncomingPKBattleRequestTimeout(invitationID, inviter);
            }
            pkInvitations.remove(invitationID);
        }
    }

    public void onInvitationReceived(ZegoUIKitUser inviter, int type, String data) {
        String invitationID = getStringFromJson(getJsonObjectFromString(data), "invitationID");
        pkInvitations.add(invitationID);
        PKExtendedData pkExtendedData = PKExtendedData.parse(data);
        if (pkExtendedData != null && invitationID != null) {
            if (pkExtendedData.type == PKExtendedData.START_PK) {
                String currentRoomID = ZegoUIKit.getRoom().roomID;
                boolean userNotHost = TextUtils.isEmpty(currentRoomID) || (!ZegoLiveStreamingManager.getInstance().isCurrentUserHost());
                boolean alreadySend = sendPKStartRequest != null;
                boolean alreadyReceived = recvPKStartRequest != null;
                boolean liveNotStarted = !ZegoLiveStreamingManager.getInstance().isLiveStarted();
                boolean isInAPK = currentPKInfo != null;
                if (userNotHost || isInAPK || alreadySend || alreadyReceived || liveNotStarted) {
                    ZegoLiveStreamingPKBattleRejectCode rejectCode;
                    if (userNotHost) {
                        rejectCode = ZegoLiveStreamingPKBattleRejectCode.USER_NOT_HOST;
                    } else if (isInAPK) {
                        rejectCode = ZegoLiveStreamingPKBattleRejectCode.IN_PK;
                    } else if (alreadySend) {
                        rejectCode = ZegoLiveStreamingPKBattleRejectCode.ALREADY_SEND;
                    } else if (alreadyReceived) {
                        rejectCode = ZegoLiveStreamingPKBattleRejectCode.ALREADY_RECEIVED;
                    } else {
                        rejectCode = ZegoLiveStreamingPKBattleRejectCode.LIVE_NOT_STARTED;
                    }
                    rejectPKBattleStartRequest(invitationID, rejectCode.ordinal());
                    return;
                }
                recvPKStartRequest = new PKRequest();
                recvPKStartRequest.requestID = invitationID;
                recvPKStartRequest.anotherUserID = inviter.userID;
                recvPKStartRequest.anotherUserName = pkExtendedData.userName;
                String customData = "";
                try {
                    JSONObject jsonObject = new JSONObject(data);
                    customData = jsonObject.getString("custom_data");
                } catch (JSONException e) {
                }

                inviter.userName = pkExtendedData.userName;
                for (PKListener listener : listenerList) {
                    listener.onIncomingPKBattleRequestReceived(invitationID, inviter, pkExtendedData.roomID,
                        customData);
                }
            } else if (pkExtendedData.type == PKExtendedData.END_PK) {
                acceptPKBattleStopRequest(invitationID);
                if (currentPKInfo == null) {
                    return;
                }
                stopPKBattle();
            }
            if (pkExtendedData.type == PKExtendedData.RESUME_PK) {
                if (currentPKInfo == null) {
                    rejectPKBattleResumeRequest(invitationID);
                } else {
                    acceptPKBattleResumeRequest(invitationID);
                }
            }
        }
    }

    public void addRoomListeners() {
        ZegoUIKit.addEventHandler(new IExpressEngineEventHandler() {

            @Override
            public void onPlayerRecvVideoFirstFrame(String streamID) {
                PKService.this.onPlayerRecvVideoFirstFrame(streamID);
            }

            @Override
            public void onPlayerSyncRecvSEI(String streamID, byte[] data) {
                PKService.this.onPlayerSyncRecvSEI(streamID, data);
            }

            @Override
            public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode,
                JSONObject extendedData) {
            }

            @Override
            public void onPlayerStateUpdate(String streamID, ZegoPlayerState state, int errorCode,
                JSONObject extendedData) {
                super.onPlayerStateUpdate(streamID, state, errorCode, extendedData);
            }
        });
        pluginRoomPropertyUpdateListener = new ZegoUIKitSignalingPluginRoomPropertyUpdateListener() {
            @Override
            public void onRoomPropertyUpdated(String key, String oldValue, String newValue) {
            }

            @Override
            public void onRoomPropertiesFullUpdated(List<String> updateKeys, HashMap<String, String> oldProperties,
                HashMap<String, String> properties) {
                Map<String, String> setProperties = new HashMap<>();
                Map<String, String> deleteProperties = new HashMap<>();
                for (String updateKey : updateKeys) {
                    if (!TextUtils.isEmpty(properties.get(updateKey))) {
                        setProperties.put(updateKey, properties.get(updateKey));
                    } else {
                        deleteProperties.put(updateKey, "");
                    }
                }
                onRoomAttributesUpdated(Collections.singletonList(setProperties),
                    Collections.singletonList(deleteProperties));
            }
        };
        ZegoUIKit.getSignalingPlugin().addRoomPropertyUpdateListener(pluginRoomPropertyUpdateListener);

        expressRoomPropertyUpdateListener = new ZegoRoomPropertyUpdateListener() {
            @Override
            public void onRoomPropertyUpdated(String key, String oldValue, String newValue) {
                if (Objects.equals("host", key)) {
                    String hostID = ZegoLiveStreamingManager.getInstance().getHostID();
                    if (!TextUtils.isEmpty(hostID)) {
                        if (signalRoomProperties != null && !signalRoomProperties.isEmpty()) {
                            String pk_user_id = signalRoomProperties.get("pk_user_id");
                            if (!TextUtils.isEmpty(pk_user_id)) {
                                onReceivePKRoomAttribute(signalRoomProperties);
                            }
                        }
                    }
                }
            }

            @Override
            public void onRoomPropertiesFullUpdated(List<String> updateKeys, Map<String, String> oldProperties,
                Map<String, String> properties) {

            }
        };
        ZegoUIKit.addRoomPropertyUpdateListener(expressRoomPropertyUpdateListener);
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

    private void onReceivePKRoomAttribute(Map<String, String> roomProperties) {
        String pk_user_id = roomProperties.get("pk_user_id");
        String pk_user_name = roomProperties.get("pk_user_name");
        String pk_room = roomProperties.get("pk_room");
        String pk_seq = roomProperties.get("pk_seq");
        String host = roomProperties.get("host");

        PKInfo pkInfo = new PKInfo(new ZegoUIKitUser(pk_user_id, pk_user_name), pk_room);
        pkInfo.hostUserID = host;
        pkInfo.seq = Long.parseLong(pk_seq);

        if (ZegoLiveStreamingManager.getInstance().isCurrentUserHost()) {
            // receive attribute but no pkInfo, resume PK
            if (currentPKInfo == null) {
                sendPKBattleResumeRequest(pk_user_id);
            }
            seiTimeMap.put(pk_user_id, System.currentTimeMillis());
        } else {
            seiTimeMap.put(host, System.currentTimeMillis());
            seiTimeMap.put(pk_user_id, System.currentTimeMillis());
            if (currentPKInfo == null) {
                // normal，audience receive Host PK action
                if (ZegoLiveStreamingManager.getInstance().getHostID() != null) {
                    setCurrentPKInfo(pkInfo);
                    startPKBattle();
                }
            }
        }
    }

    public boolean isPKUser(String userID) {
        if (currentPKInfo == null) {
            return false;
        } else {
            return currentPKInfo.pkUser.userID.equals(userID);
        }
    }

    @NonNull
    private String getPKExtendedData(int type) {
        ZegoUIKitUser localUser = ZegoUIKit.getLocalUser();
        String currentRoomID = ZegoUIKit.getRoom().roomID;
        PKExtendedData data = new PKExtendedData();
        data.roomID = currentRoomID;
        data.userName = localUser.userName;
        data.type = type;
        return data.toString();
    }

    public void sendPKBattlesStartRequest(String targetUserID, int timeout, String customData,
        UserRequestCallback callback) {
        sendPKStartRequest = new PKRequest();
        String pkExtendedData = getPKExtendedData(PKExtendedData.START_PK);
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(pkExtendedData);
            jsonObject.put("custom_data", customData);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        sendUserRequest(targetUserID, timeout, jsonObject.toString(), new PluginCallbackListener() {
            @Override
            public void callback(Map<String, Object> result) {
                int code = (int) result.get("code");
                String invitationID = (String) result.get("invitationID");
                if (code == 0) {
                    sendPKStartRequest.requestID = invitationID;
                    sendPKStartRequest.anotherUserID = targetUserID;
                } else {
                    sendPKStartRequest = null;
                }
                if (callback != null) {
                    callback.onUserRequestSend(code, invitationID);
                }
            }
        });
    }

    public void acceptPKBattleStartRequest(String requestID, String anotherHostLiveID, ZegoUIKitUser anotherHostUser,
        String customData) {
        if (recvPKStartRequest != null && requestID.equals(recvPKStartRequest.requestID)) {
            recvPKStartRequest = null;
        }
        String pkExtendedData = getPKExtendedData(PKExtendedData.START_PK);
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(pkExtendedData);
            jsonObject.put("custom_data", customData);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        acceptUserRequest(requestID, jsonObject.toString(), new PluginCallbackListener() {
            @Override
            public void callback(Map<String, Object> result) {
                int code = (int) result.get("code");
                if (code == 0) {
                    startPKBattleWith(anotherHostLiveID, anotherHostUser.userID, anotherHostUser.userName);
                } else {
                    sendPKBattlesStopRequestInner();
                }
            }
        });
    }

    public void rejectPKBattleStartRequest(String requestID, int rejectCode) {
        if (recvPKStartRequest != null && requestID.equals(recvPKStartRequest.requestID)) {
            recvPKStartRequest = null;
        }
        String pkExtendedData = getPKExtendedData(PKExtendedData.START_PK);
        JSONObject jsonObject = getJsonObjectFromString(pkExtendedData);
        try {
            jsonObject.put("reason", rejectCode);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        rejectUserRequest(requestID, jsonObject.toString(), null);
    }

    public void cancelPKBattleStartRequest(String customData, UserRequestCallback callback) {
        if (sendPKStartRequest != null) {
            String requestID = sendPKStartRequest.requestID;
            String pkExtendedData = getPKExtendedData(PKExtendedData.START_PK);
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(pkExtendedData);
                jsonObject.put("custom_data", customData);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            cancelUserRequest(requestID, jsonObject.toString(), new PluginCallbackListener() {
                @Override
                public void callback(Map<String, Object> result) {
                    int code = (int) result.get("code");
                    String invitationID = (String) result.get("invitationID");
                    if (code == 0) {
                        sendPKStartRequest = null;
                    }
                    if (callback != null) {
                        callback.onUserRequestSend(code, invitationID);
                    }
                }
            });
        }
    }

    public void sendPKBattlesStopRequest() {
        if (currentPKInfo != null) {
            sendPKBattlesStopRequestInner();
            stopPKBattle(); // end right now,no need to wait for answer because there
        }
    }

    private void sendPKBattlesStopRequestInner() {
        if (currentPKInfo != null) {
            String pkExtendedData = getPKExtendedData(PKExtendedData.END_PK);
            sendUserRequest(currentPKInfo.pkUser.userID, 60, pkExtendedData, null);
            setCurrentPKInfo(null);
        }
    }

    public void sendPKBattleResumeRequest(String pk_user_id) {
        String pkExtendedData = getPKExtendedData(PKExtendedData.RESUME_PK);
        sendUserRequest(pk_user_id, 60, pkExtendedData, null);
    }

    public void rejectPKBattleResumeRequest(String requestID) {
        String pkExtendedData = getPKExtendedData(PKExtendedData.RESUME_PK);
        rejectUserRequest(requestID, pkExtendedData, null);
    }

    public void acceptPKBattleResumeRequest(String requestID) {
        String pkExtendedData = getPKExtendedData(PKExtendedData.RESUME_PK);
        acceptUserRequest(requestID, pkExtendedData, null);
        startMixStreamTask(false, new IZegoMixerStartCallback() {
            @Override
            public void onMixerStartResult(int errorCode, JSONObject extendedData) {
                if (errorCode == 0 && getPKInfo() != null) {
                    setPKRoomAttributes();
                } else {
                    sendPKBattlesStopRequestInner();
                }
            }
        });
    }

    public void acceptPKBattleStopRequest(String requestID) {
        String pkExtendedData = getPKExtendedData(PKExtendedData.END_PK);
        acceptUserRequest(requestID, pkExtendedData, null);
    }

    public void setCurrentPKInfo(PKInfo currentPKInfo) {
        this.currentPKInfo = currentPKInfo;
    }

    public PKInfo getPKInfo() {
        return currentPKInfo;
    }

    public PKRequest getSendPKStartRequest() {
        return sendPKStartRequest;
    }

    public void startPKBattleWith(String anotherHostLiveID, String anotherHostUserID, String anotherHostName) {
        PKInfo pkInfo = new PKInfo(new ZegoUIKitUser(anotherHostUserID, anotherHostName), anotherHostLiveID);
        setCurrentPKInfo(pkInfo);
        startPKBattle();
    }

    private void startPKBattle() {
        if (ZegoLiveStreamingManager.getInstance().isCurrentUserHost()) {
            ZegoLiveStreamingManager.getInstance().startPublishingStream();
            startMixStreamTask(false, new IZegoMixerStartCallback() {
                @Override
                public void onMixerStartResult(int errorCode, JSONObject extendedData) {
                    if (errorCode == 0 && getPKInfo() != null) {
                        syncDeviceStatus();
                        setPKRoomAttributes();
                        hasNotified = false;
                        for (PKListener listener : listenerList) {
                            listener.onPKStarted();
                        }
                        checkPKUserSEI();
                    } else {
                        sendPKBattlesStopRequestInner();
                    }
                }
            });
        } else {
            hasNotified = false;

            for (PKListener listener : listenerList) {
                listener.onPKStarted();
            }
            checkPKUserSEI();
        }
    }


    private void setPKRoomAttributes() {
        ZegoUIKitUser localUser = ZegoUIKit.getLocalUser();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("host", localUser.userID);
        hashMap.put("pk_room", currentPKInfo.pkRoom);
        hashMap.put("pk_user_id", currentPKInfo.pkUser.userID);
        hashMap.put("pk_user_name", currentPKInfo.pkUser.userName);
        hashMap.put("pk_seq", String.valueOf(currentPKInfo.seq + 1));

        ZegoUIKit.getSignalingPlugin().updateRoomProperty(hashMap, false, false, false,
            new ZegoUIKitSignalingPluginRoomAttributesOperatedCallback() {
                @Override
                public void onSignalingPluginRoomAttributesOperated(int errorCode, String errorMessage,
                    List<String> errorKeys) {

                }
            });
    }

    public void stopPKBattle() {
        if (ZegoLiveStreamingManager.getInstance().isCurrentUserHost()) {
            ZegoUIKitUser localUser = ZegoUIKit.getLocalUser();
            if (!ZegoUIKit.isCameraOn(localUser.userID) && !ZegoUIKit.isMicrophoneOn(localUser.userID)) {
                ZegoLiveStreamingManager.getInstance().stopPublishStream();
            }
            deletePKRoomAttributes();
            stopMixTask();
            stopSyncDeviceStatus();
        } else {
            muteHostAudioVideo(false);
        }

        setCurrentPKInfo(null);

        stopCheckPKUserSEI();

        hasNotified = false;
        mutePKUser = false;
        seiTimeMap.clear();
        seiStateMap.clear();

        for (PKListener listener : listenerList) {
            listener.onPKEnded();
        }
    }

    public void muteHostAudioVideo(boolean mute) {
        String hostID = ZegoLiveStreamingManager.getInstance().getHostID();
        ZegoUIKitUser hostUser = ZegoUIKit.getUser(hostID);
        if (hostUser != null) {
            String currentRoomID = ZegoUIKit.getRoom().roomID;
            String streamID = ZegoLiveStreamingManager.generateCameraStreamID(currentRoomID, hostID);
            ZegoUIKit.mutePlayStreamAudio(streamID, mute);
            ZegoUIKit.mutePlayStreamVideo(streamID, mute);
        }
    }

    private void deletePKRoomAttributes() {
        if (signalRoomProperties != null && !signalRoomProperties.isEmpty()) {
            String pk_user_id = signalRoomProperties.get("pk_user_id");
            if (!TextUtils.isEmpty(pk_user_id)) {
                List<String> keys = new ArrayList<>();
                keys.add("host");
                keys.add("pk_room");
                keys.add("pk_user_id");
                keys.add("pk_user_name");
                keys.add("pk_seq");
                ZegoUIKit.getSignalingPlugin().deleteRoomProperties(keys, true, null);
            }
        }
    }

    private void startMixStreamTask(boolean muteAudio, IZegoMixerStartCallback callback) {
        String currentRoomID = ZegoUIKit.getRoom().roomID;
        String mixStreamID = currentRoomID + "_mix";
        if (task == null) {
            task = new ZegoMixerTask(mixStreamID);
            task.enableSoundLevel(true);
        }

        ZegoMixerVideoConfig videoConfig = new ZegoMixerVideoConfig();
        videoConfig.width = 1080;
        videoConfig.height = 960;
        task.videoConfig = videoConfig;

        ArrayList<ZegoMixerInput> inputList = new ArrayList<>();
        ZegoUIKitUser localUser = ZegoUIKit.getLocalUser();

        String streamID = ZegoLiveStreamingManager.generateCameraStreamID(currentRoomID, localUser.userID);
        ZegoMixerInput input_1 = new ZegoMixerInput(streamID, ZegoMixerInputContentType.VIDEO,
            new Rect(0, 0, 540, 960));
        input_1.renderMode = ZegoMixRenderMode.FILL;
        inputList.add(input_1);
        ZegoMixerInput input_2 = new ZegoMixerInput(currentPKInfo.getPKStream(), ZegoMixerInputContentType.VIDEO,
            new Rect(540, 0, 1080, 960));
        input_2.renderMode = ZegoMixRenderMode.FILL;
        if (muteAudio) {
            input_2.contentType = ZegoMixerInputContentType.VIDEO_ONLY;
        } else {
            input_2.contentType = ZegoMixerInputContentType.VIDEO;
        }
        inputList.add(input_2);
        task.setInputList(inputList);

        ZegoMixerOutput mixerOutput = new ZegoMixerOutput(mixStreamID);
        ArrayList<ZegoMixerOutput> mixerOutputList = new ArrayList<>();
        mixerOutputList.add(mixerOutput);
        task.setOutputList(mixerOutputList);

        ZegoUIKit.startMixerTask(task, new IZegoMixerStartCallback() {
            @Override
            public void onMixerStartResult(int errorCode, JSONObject extendedData) {
                // 1005026 non_exists_stream_list
                if (callback != null) {
                    callback.onMixerStartResult(errorCode, extendedData);
                }
            }
        });
    }

    public void stopMixTask() {
        if (task != null) {
            ZegoUIKit.stopMixerTask(task, new IZegoMixerStopCallback() {
                @Override
                public void onMixerStopResult(int errorCode) {

                }
            });
            task = null;
        }
    }

    public boolean isPKUserMuted() {
        return mutePKUser;
    }

    public void mutePKUser(boolean mute, IZegoMixerStartCallback callback) {
        startMixStreamTask(mute, new IZegoMixerStartCallback() {
            @Override
            public void onMixerStartResult(int errorCode, JSONObject extendedData) {
                if (errorCode == 0) {
                    ZegoUIKit.mutePlayStreamAudio(currentPKInfo.getPKStream(), mute);
                    mutePKUser = mute;
                    if (callback != null) {
                        callback.onMixerStartResult(errorCode, extendedData);
                    }
                    for (PKListener listener : listenerList) {
                        listener.onOtherHostMuted(currentPKInfo.pkUser.userID, mutePKUser);
                    }
                }
            }
        });
    }

    public void syncDeviceStatus() {
        handler.removeCallbacks(syncSEIRunnable);
        handler.post(syncSEIRunnable);
    }

    public void stopSyncDeviceStatus() {
        handler.removeCallbacks(syncSEIRunnable);
    }

    public void checkPKUserSEI() {
        handler.removeCallbacks(checkSEIRunnable);
        handler.post(checkSEIRunnable);
    }

    public void stopCheckPKUserSEI() {
        handler.removeCallbacks(checkSEIRunnable);
    }

    public boolean isPKInvitation(String invitationID) {
        boolean contains = pkInvitations.contains(invitationID);
        return contains;
    }

    public void addListener(PKListener listener) {
        listenerList.add(listener);
    }

    public void removeListener(PKListener listener) {
        listenerList.remove(listener);
    }

    public void removeRoomListeners() {
        task = null;
        listenerList.clear();
        ZegoUIKit.removeRoomPropertyUpdateListener(expressRoomPropertyUpdateListener);
        ZegoUIKit.getSignalingPlugin().removeRoomPropertyUpdateListener(pluginRoomPropertyUpdateListener);
    }

    public void removeRoomData() {
        setCurrentPKInfo(null);
        seiTimeMap.clear();
        seiStateMap.clear();
        signalRoomProperties.clear();
        hasNotified = false;
        mutePKUser = false;
        handler.removeCallbacksAndMessages(null);
    }

    public void removeUserListeners() {
    }

    public void removeUserData() {
        pkInvitations.clear();
        removeRoomData();
        removeRoomListeners();
    }

    private void onPlayerRecvVideoFirstFrame(String streamID) {
        if (streamID.endsWith("_mix")) {
            muteHostAudioVideo(true);
        }
    }

    public void onPlayerSyncRecvSEI(String streamID, byte[] data) {
        try {
            JSONObject jsonObject = new JSONObject(new String(data));
            int type = jsonObject.getInt("type");
            String senderID = jsonObject.getString("sender_id");
            seiTimeMap.put(senderID, System.currentTimeMillis());

            boolean isMicOpen = jsonObject.getBoolean("mic");
            boolean isCameraOpen = jsonObject.getBoolean("cam");

            boolean isPKUser =
                currentPKInfo != null && currentPKInfo.pkUser != null && Objects.equals(currentPKInfo.pkUser.userID,
                    senderID);

            String hostID = ZegoLiveStreamingManager.getInstance().getHostID();
            ZegoUIKitUser hostUser = ZegoUIKit.getUser(hostID);
            boolean isHostUser = hostUser != null && Objects.equals(hostUser.userID, senderID);

            if (isPKUser) {
                boolean micChanged = currentPKInfo.pkUser.isMicrophoneOn != isMicOpen;
                if (micChanged || !hasNotified) {
                    currentPKInfo.pkUser.isMicrophoneOn = (isMicOpen);
                    handler.post(() -> {
                        for (PKListener listener : listenerList) {
                            listener.onOtherHostMicrophoneOpen(currentPKInfo.pkUser.userID, isMicOpen);
                        }
                    });
                }
                boolean camChanged = currentPKInfo.pkUser.isCameraOn != isCameraOpen;
                if (camChanged || !hasNotified) {
                    currentPKInfo.pkUser.isCameraOn = (isCameraOpen);
                    handler.post(() -> {
                        for (PKListener listener : listenerList) {
                            listener.onOtherHostCameraOpen(currentPKInfo.pkUser.userID, isCameraOpen);
                        }
                    });
                }
                hasNotified = true;
            } else if (isHostUser) {
                //                if (hostUser.isCameraOn != isCameraOpen) {
                //                    handler.post(() -> {
                //                        ZegoUIKit.turnCameraOn(hostUser.userID, isCameraOpen);
                //                    });
                //                }
                //                if (hostUser.isMicrophoneOn != isMicOpen) {
                //                    handler.post(() -> {
                //                        ZegoUIKit.turnMicrophoneOn(hostUser.userID, isMicOpen);
                //                    });
                //                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void onRoomAttributesUpdated(List<Map<String, String>> setProperties,
        List<Map<String, String>> deleteProperties) {
        for (Map<String, String> deleteProperty : deleteProperties) {
            for (Entry<String, String> entry : deleteProperty.entrySet()) {
                signalRoomProperties.remove(entry.getKey());
            }
        }
        for (Map<String, String> setPropertie : setProperties) {
            signalRoomProperties.putAll(setPropertie);
        }

        for (Map<String, String> setPropertyMap : setProperties) {
            if (setPropertyMap.containsKey("pk_user_id")) {
                onReceivePKRoomAttribute(setPropertyMap);
            }
        }

        for (Map<String, String> deleteProperty : deleteProperties) {
            if (deleteProperty.containsKey("pk_user_id")) {
                if (currentPKInfo == null) {
                    return;
                } else {
                    stopPKBattle();
                }
            }
        }
    }

    private void sendUserRequest(String userID, int timeout, String extendedData,
        PluginCallbackListener callbackListener) {
        ZegoUIKit.getSignalingPlugin()
            .sendInvitation(Collections.singletonList(userID), timeout, LiveInvitationType.PK.getValue(), extendedData,
                new PluginCallbackListener() {
                    @Override
                    public void callback(Map<String, Object> result) {
                        String invitationID = (String) result.get("invitationID");
                        pkInvitations.add(invitationID);
                        if (callbackListener != null) {
                            callbackListener.callback(result);
                        }
                    }
                });
    }

    private void putStringToJson(String key, String value, JSONObject jsonObject) {
        try {
            jsonObject.put(key, value);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void acceptUserRequest(String requestID, String extendedData, PluginCallbackListener callbackListener) {
        JSONObject jsonObject = getJsonObjectFromString(extendedData);
        putStringToJson("invitationID", requestID, jsonObject);
        ZegoUIKit.getSignalingPlugin().acceptInvitation("", jsonObject.toString(), callbackListener);
    }

    private void rejectUserRequest(String requestID, String extendedData, PluginCallbackListener callbackListener) {
        JSONObject jsonObject = getJsonObjectFromString(extendedData);
        putStringToJson("invitationID", requestID, jsonObject);
        ZegoUIKit.getSignalingPlugin().refuseInvitation("", jsonObject.toString(), callbackListener);
    }

    private void cancelUserRequest(String requestID, String extendedData, PluginCallbackListener callbackListener) {
        JSONObject jsonObject = getJsonObjectFromString(extendedData);
        putStringToJson("invitationID", requestID, jsonObject);
        ZegoUIKit.getSignalingPlugin().cancelInvitation(new ArrayList<>(), jsonObject.toString(), callbackListener);
    }

    public static class PKRequest {

        public String requestID;
        public String anotherUserID;
        // only receive pkrequest have value
        public String anotherUserName;
    }

    public static class PKInfo {

        public ZegoUIKitUser pkUser;
        public String pkRoom;
        public long seq;
        public String hostUserID;

        public PKInfo(ZegoUIKitUser pkUser, String pkRoom) {
            this.pkUser = pkUser;
            this.pkRoom = pkRoom;
        }

        public PKInfo(String targetUserID) {
            this.pkUser = new ZegoUIKitUser(targetUserID, "");
        }

        public String getPKStream() {
            return ZegoLiveStreamingManager.generateCameraStreamID(pkRoom, pkUser.userID);
        }

        @Override
        public String toString() {
            return "PKInfo{" + "pkUser=" + pkUser + ", pkRoom='" + pkRoom + '\'' + '}';
        }
    }
}
