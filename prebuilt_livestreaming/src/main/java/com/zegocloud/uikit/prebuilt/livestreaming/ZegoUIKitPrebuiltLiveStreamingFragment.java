package com.zegocloud.uikit.prebuilt.livestreaming;

import android.Manifest.permission;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.components.audiovideocontainer.ZegoAudioVideoComparator;
import com.zegocloud.uikit.components.audiovideocontainer.ZegoAudioVideoViewConfig;
import com.zegocloud.uikit.components.audiovideocontainer.ZegoLayout;
import com.zegocloud.uikit.components.audiovideocontainer.ZegoLayoutGalleryConfig;
import com.zegocloud.uikit.components.audiovideocontainer.ZegoLayoutMode;
import com.zegocloud.uikit.components.audiovideocontainer.ZegoLayoutPictureInPictureConfig;
import com.zegocloud.uikit.components.audiovideocontainer.ZegoViewPosition;
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoLiveStreamingManager.ZegoLiveStreamingListener;
import com.zegocloud.uikit.prebuilt.livestreaming.core.PrebuiltUICallBack;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoDialogInfo;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoLiveStreamingRole;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoTranslationText;
import com.zegocloud.uikit.prebuilt.livestreaming.databinding.LivestreamingFragmentLivestreamingBinding;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.components.ConfirmDialog;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.components.LiveMemberList;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.components.ReceiveCoHostRequestDialog;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.components.ZegoAudioVideoForegroundView;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.components.ZegoScreenShareForegroundView;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.core.PKService.PKInfo;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.core.RTCRoomProperty;
import com.zegocloud.uikit.prebuilt.livestreaming.widget.ZegoAcceptCoHostButton;
import com.zegocloud.uikit.prebuilt.livestreaming.widget.ZegoRefuseCoHostButton;
import com.zegocloud.uikit.service.defines.ZegoAudioVideoUpdateListener;
import com.zegocloud.uikit.service.defines.ZegoMeRemovedFromRoomListener;
import com.zegocloud.uikit.service.defines.ZegoRoomPropertyUpdateListener;
import com.zegocloud.uikit.service.defines.ZegoTurnOnYourCameraRequestListener;
import com.zegocloud.uikit.service.defines.ZegoUIKitCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.service.defines.ZegoUserUpdateListener;
import im.zego.zegoexpress.constants.ZegoOrientation;
import im.zego.zegoexpress.constants.ZegoVideoConfigPreset;
import im.zego.zegoexpress.entity.ZegoVideoConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

public class ZegoUIKitPrebuiltLiveStreamingFragment extends Fragment implements PrebuiltUICallBack {

    private OnBackPressedCallback onBackPressedCallback;
    private LivestreamingFragmentLivestreamingBinding binding;
    private Map<ZegoLiveStreamingRole, List<View>> bottomMenuBarExtendedButtons = new HashMap<>();
    private ConfirmDialog receiveCoHostInviteDialog;
    private ReceiveCoHostRequestDialog receiveCoHostRequestDialog;
    private LiveMemberList livememberList;
    private boolean isLocalUserHost = false;
    private boolean hostFirst = true;
    private Runnable hideTipsRunnable = new Runnable() {
        @Override
        public void run() {
            binding.liveToast.setVisibility(View.GONE);
        }
    };
    private View backgroundView;
    private IntentFilter configurationChangeFilter;
    private BroadcastReceiver configurationChangeReceiver;

    public ZegoUIKitPrebuiltLiveStreamingFragment() {
        // Required empty public constructor
    }

    /**
     * @param appID    You can create a project and obtain the appID from the [ZEGO Console]().
     * @param appSign  You can create a project and obtain the appSign from the [ZEGO Console]().
     * @param userID   The ID of the currently logged-in user. It can be any valid string, typically, you would use the ID from your own user system.
     * @param userName The name of the currently logged-in user. It can be any valid string, typically, you would use the name from your own user system.
     * @param liveID   The ID of the live broadcast. This ID is the unique identifier for the current live broadcast, so you need to ensure its uniqueness. It can be any valid string. Users providing the same liveID will log in to the same live broadcast room.
     * @param config   The configuration for initializing the live broadcast.
     * @return
     */
    public static ZegoUIKitPrebuiltLiveStreamingFragment newInstance(long appID, String appSign, String userID,
        String userName, String liveID, ZegoUIKitPrebuiltLiveStreamingConfig config) {

        ZegoUIKitPrebuiltLiveStreamingFragment fragment = new ZegoUIKitPrebuiltLiveStreamingFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("appID", appID);
        bundle.putString("appSign", appSign);
        bundle.putString("liveID", liveID);
        bundle.putString("userID", userID);
        bundle.putString("userName", userName);
        fragment.setArguments(bundle);

        ZegoLiveStreamingManager.getInstance().setPrebuiltConfig(config);
        return fragment;
    }

    /**
     * @param appID    You can create a project and obtain the appID from the [ZEGO Console]().
     * @param token  You can create a project and obtain the ServerSecret from the [ZEGO Console]() and then generate token by your server to avoid leaking your appSign.
     * @param userID   The ID of the currently logged-in user. It can be any valid string, typically, you would use the ID from your own user system.
     * @param userName The name of the currently logged-in user. It can be any valid string, typically, you would use the name from your own user system.
     * @param liveID   The ID of the live broadcast. This ID is the unique identifier for the current live broadcast, so you need to ensure its uniqueness. It can be any valid string. Users providing the same liveID will log in to the same live broadcast room.
     * @param config   The configuration for initializing the live broadcast.
     * @return
     */
    public static ZegoUIKitPrebuiltLiveStreamingFragment newInstanceWithToken(long appID, String token, String userID,
        String userName, String liveID, ZegoUIKitPrebuiltLiveStreamingConfig config) {
        ZegoUIKitPrebuiltLiveStreamingFragment fragment = new ZegoUIKitPrebuiltLiveStreamingFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("appID", appID);
        bundle.putString("appToken", token);
        bundle.putString("liveID", liveID);
        bundle.putString("userID", userID);
        bundle.putString("userName", userName);
        fragment.setArguments(bundle);

        ZegoLiveStreamingManager.getInstance().setPrebuiltConfig(config);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        long appID = arguments.getLong("appID");
        String appSign = arguments.getString("appSign");
        String userName = arguments.getString("userName");
        String userID = arguments.getString("userID");
        String token = arguments.getString("appToken");

        if (appID != 0) {
            ZegoLiveStreamingManager.getInstance().init(requireActivity().getApplication(), appID, appSign);
            if (!TextUtils.isEmpty(token)) {
                ZegoUIKit.renewToken(token);
            }
            ZegoLiveStreamingManager.getInstance().login(userID, userName, new ZegoUIKitCallback() {
                @Override
                public void onResult(int errorCode) {
                    if (errorCode == 0) {
                        String liveID = getArguments().getString("liveID");
                        if (!TextUtils.isEmpty(liveID)) {
                            ZegoLiveStreamingManager.getInstance().joinRoom(liveID, errorCode2 -> {
                                if (errorCode2 == 0) {
                                    onRoomJoinSucceed();
                                } else {
                                    onRoomJoinFailed();
                                }
                            });
                        }
                    } else {
                        requireActivity().finish();
                    }
                }
            });
        }

        configurationChangeFilter = new IntentFilter();
        configurationChangeFilter.addAction("android.intent.action.CONFIGURATION_CHANGED");

        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                boolean hostStarted = ZegoLiveStreamingManager.getInstance().isLiveStarted() && isLocalUserHost;
                boolean isNotHost = !isLocalUserHost;
                ZegoUIKitPrebuiltLiveStreamingConfig liveConfig = ZegoLiveStreamingManager.getInstance()
                    .getPrebuiltConfig();
                if (liveConfig.confirmDialogInfo != null && (hostStarted || isNotHost)) {
                    showQuitDialog(getDialogInfo());
                } else {
                    leaveRoom();
                    setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    private void leaveRoom() {
        if (configurationChangeReceiver != null) {
            requireActivity().unregisterReceiver(configurationChangeReceiver);
            configurationChangeReceiver = null;
        }
        if (livememberList != null) {
            livememberList.dismiss();
        }

        ZegoLiveStreamingManager.getInstance().leaveRoom();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = LivestreamingFragmentLivestreamingBinding.inflate(inflater, container, false);
        if (backgroundView != null) {
            binding.liveBackgroundViewParent.removeAllViews();
            binding.liveBackgroundViewParent.addView(backgroundView);
        }
        binding.liveGroup.setVisibility(View.GONE);
        binding.previewGroup.setVisibility(View.GONE);
        return binding.getRoot();
    }

    private void onRoomJoinFailed() {
        requireActivity().finish();
    }

    private void onRoomJoinSucceed() {
        configurationChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ZegoOrientation orientation = ZegoOrientation.ORIENTATION_0;

                if (Surface.ROTATION_0 == requireActivity().getWindowManager().getDefaultDisplay().getRotation()) {
                    orientation = ZegoOrientation.ORIENTATION_0;
                } else if (Surface.ROTATION_180 == requireActivity().getWindowManager().getDefaultDisplay()
                    .getRotation()) {
                    orientation = ZegoOrientation.ORIENTATION_180;
                } else if (Surface.ROTATION_270 == requireActivity().getWindowManager().getDefaultDisplay()
                    .getRotation()) {
                    orientation = ZegoOrientation.ORIENTATION_270;
                } else if (Surface.ROTATION_90 == requireActivity().getWindowManager().getDefaultDisplay()
                    .getRotation()) {
                    orientation = ZegoOrientation.ORIENTATION_90;
                }
                ZegoUIKit.setAppOrientation(orientation);
            }
        };

        requireActivity().registerReceiver(configurationChangeReceiver, configurationChangeFilter);

        ZegoUIKitPrebuiltLiveStreamingConfig config = ZegoLiveStreamingManager.getInstance().getPrebuiltConfig();
        String userID = ZegoUIKit.getLocalUser().userID;
        isLocalUserHost = config.role == ZegoLiveStreamingRole.HOST;

        if (config.turnOnCameraWhenJoining || config.turnOnMicrophoneWhenJoining) {
            requestPermissionIfNeeded((allGranted, grantedList, deniedList) -> {
                if (config.turnOnCameraWhenJoining) {
                    if (grantedList.contains(permission.CAMERA)) {
                        ZegoUIKit.turnCameraOn(userID, true);
                    }
                } else {
                    ZegoUIKit.turnCameraOn(userID, false);
                }
                if (config.turnOnMicrophoneWhenJoining) {
                    if (grantedList.contains(permission.RECORD_AUDIO)) {
                        ZegoUIKit.turnMicrophoneOn(userID, true);
                    }
                } else {
                    ZegoUIKit.turnMicrophoneOn(userID, false);
                }
            });
        }

        ZegoLiveStreamingManager.getInstance().setPrebuiltUiCallBack(this);

        initPreviewBtns();
        initLiveBtns();

        if (isLocalUserHost) {
            showPreview();
            Map<String, String> map = new HashMap<>();
            map.put(RTCRoomProperty.HOST, userID);
            map.put(RTCRoomProperty.LIVE_STATUS, RTCRoomProperty.LIVE_STATUS_STOP);
            ZegoLiveStreamingManager.getInstance().updateRoomProperties(map);
            ZegoLiveStreamingManager.getInstance().resumePlayingAllAudioVideo(false);
        } else {
            showLiveView();
            ZegoLiveStreamingManager.getInstance().pausePlayingAllAudioVideo(false);
        }

        ZegoUIKit.setAudioOutputToSpeaker(config.useSpeakerWhenJoining);

        addUIKitListeners();

        ZegoLiveStreamingManager.getInstance().addLiveStreamingListener(new ZegoLiveStreamingListener() {

            @Override
            public void onPKStarted() {

                binding.livePkLayout.setVisibility(View.VISIBLE);
                binding.liveVideoContainer.setVisibility(View.GONE);

                if (ZegoLiveStreamingManager.getInstance().isCurrentUserCoHost()) {
                    if (config.translationText != null && config.translationText.coHostEndBecausePK != null) {
                        showTopTips(config.translationText.coHostEndBecausePK, true);
                    }
                }

                if (binding.previewGroup.getVisibility() == View.VISIBLE) {
                    Map<String, String> map = new HashMap<>();
                    map.put(RTCRoomProperty.LIVE_STATUS, RTCRoomProperty.LIVE_STATUS_START);
                    ZegoLiveStreamingManager.getInstance().updateRoomProperties(map);
                }
            }

            @Override
            public void onPKEnded() {
                binding.livePkLayout.setVisibility(View.INVISIBLE);
                binding.liveVideoContainer.setVisibility(View.VISIBLE);

                String hostID = ZegoLiveStreamingManager.getInstance().getHostID();
                ZegoUIKitUser hostUser = ZegoUIKit.getUser(hostID);
                binding.liveVideoContainer.updateLayout();

            }
        });
        binding.livePkLayout.onJoinRoomSucceed();
        binding.livePkLayout.setPKBattleConfig(config.pkBattleConfig);
        binding.livePkLayout.setPrebuiltAudioVideoConfig(config.audioVideoViewConfig);
        binding.livePkLayout.setAvatarViewProvider(config.avatarViewProvider);
    }

    private void addUIKitListeners() {
        ZegoUIKitPrebuiltLiveStreamingConfig config = ZegoLiveStreamingManager.getInstance().getPrebuiltConfig();
        ZegoUIKit.addAudioVideoUpdateListener(new ZegoAudioVideoUpdateListener() {
            @Override
            public void onAudioVideoAvailable(List<ZegoUIKitUser> userList) {
            }

            @Override
            public void onAudioVideoUnAvailable(List<ZegoUIKitUser> userList) {
                for (ZegoUIKitUser uiKitUser : userList) {
                    if (Objects.equals(uiKitUser.userID, ZegoLiveStreamingManager.getInstance().getHostID())) {
                        hostFirst = true;
                    }
                }
            }
        });

        ZegoUIKit.addRoomPropertyUpdateListener(new ZegoRoomPropertyUpdateListener() {
            @Override
            public void onRoomPropertyUpdated(String key, String oldValue, String newValue) {
                if (ZegoUIKit.getLocalUser() == null) {
                    return;
                }

                String userID = ZegoUIKit.getLocalUser().userID;
                isLocalUserHost = Objects.equals(ZegoLiveStreamingManager.getInstance().getHostID(), userID);
                if (Objects.equals(RTCRoomProperty.HOST, key)) {
                    if (newValue != null) {
                        ZegoUIKitUser uiKitUser = ZegoUIKit.getUser(ZegoLiveStreamingManager.getInstance().getHostID());
                        if (uiKitUser != null) {
                            binding.liveTopHostIcon.setTextOnly(uiKitUser.userName);
                            binding.liveTopHostName.setText(uiKitUser.userName);
                        } else {
                            binding.liveTopHostIcon.setTextOnly("");
                            binding.liveTopHostName.setText("");
                        }
                        // second host in ,pre become audience
                        if (oldValue != null && oldValue.equals(userID) && !isLocalUserHost) {
                            ZegoUIKit.turnCameraOn(userID, false);
                            ZegoUIKit.turnMicrophoneOn(userID, false);
                            ZegoLiveStreamingManager.getInstance().pausePlayingAllAudioVideo(false);
                            showLiveView();
                            ZegoLiveStreamingManager.getInstance().setCurrentRole(ZegoLiveStreamingRole.AUDIENCE);
                            binding.liveBackgroundViewParent.setVisibility(View.VISIBLE);
                            binding.liveVideoContainer.setVisibility(View.GONE);
                        }
                    }
                    binding.liveVideoContainer.updateLayout();
                }
                if (Objects.equals(RTCRoomProperty.LIVE_STATUS, key)) {
                    if (RTCRoomProperty.LIVE_STATUS_START.equals(newValue)) {
                        ZegoLiveStreamingManager.getInstance().resumePlayingAllAudioVideo(false);
                        binding.liveBackgroundViewParent.setVisibility(View.GONE);
                        if (ZegoLiveStreamingManager.getInstance().getPKInfo() == null) {
                            binding.liveVideoContainer.setVisibility(View.VISIBLE);
                        }
                        if (oldValue != null) {
                            showLiveView();
                        }
                    } else if (RTCRoomProperty.LIVE_STATUS_STOP.equals(newValue)) {
                        ZegoLiveStreamingManager.getInstance().pausePlayingAllAudioVideo(false);
                        hostFirst = true;
                        if (RTCRoomProperty.LIVE_STATUS_START.equals(oldValue)) {
                            if (!isLocalUserHost) {
                                ZegoUIKit.turnCameraOn(userID, false);
                                ZegoUIKit.turnMicrophoneOn(userID, false);
                                if (config.zegoLiveStreamingEndListener != null) {
                                    config.zegoLiveStreamingEndListener.onLiveStreamingEnded();
                                }
                            }

                        }
                        if (isLocalUserHost) {
                            binding.liveBackgroundViewParent.setVisibility(View.GONE);
                            if (ZegoLiveStreamingManager.getInstance().getPKInfo() == null) {
                                binding.liveVideoContainer.setVisibility(View.VISIBLE);
                            }
                            ZegoLiveStreamingManager.getInstance().setCurrentRole(ZegoLiveStreamingRole.HOST);
                        } else {
                            binding.liveBackgroundViewParent.setVisibility(View.VISIBLE);
                            binding.liveVideoContainer.setVisibility(View.GONE);
                            ZegoLiveStreamingManager.getInstance().setCurrentRole(ZegoLiveStreamingRole.AUDIENCE);
                            dismissReceiveCoHostInviteDialog();
                            binding.liveBottomMenuBar.onLiveEnd();
                        }
                    }
                }
                if (Objects.equals(RTCRoomProperty.ENABLE_CHAT, key)) {
                    if (!isLocalUserHost) {
                        if (RTCRoomProperty.ENABLE_CHAT_DISABLE.equals(newValue)) {
                            binding.liveBottomMenuBar.enableChat(false);
                        } else if (RTCRoomProperty.ENABLE_CHAT_ENABLE.equals(newValue)) {
                            binding.liveBottomMenuBar.enableChat(true);
                        }
                    }
                }
            }

            @Override
            public void onRoomPropertiesFullUpdated(List<String> updateKeys, Map<String, String> oldProperties,
                Map<String, String> properties) {
                ZegoUIKitPrebuiltLiveStreamingConfig config = ZegoLiveStreamingManager.getInstance()
                    .getPrebuiltConfig();
                if (config.role == ZegoLiveStreamingRole.HOST) {
                    String currentUserID = ZegoUIKit.getLocalUser().userID;
                    boolean thereIsHostInRoom = false;
                    for (int i = 0; i < updateKeys.size(); ++i) {
                        String key = updateKeys.get(i);
                        if (RTCRoomProperty.HOST.equals(key) && (Objects.equals(properties.get(key), "")
                            || Objects.equals(properties.get(key), currentUserID))) {
                            thereIsHostInRoom = true;
                            break;
                        }
                    }
                    if (thereIsHostInRoom) {
                        config.role = ZegoLiveStreamingRole.AUDIENCE;
                    }
                }
            }
        });
        ZegoUIKit.addUserUpdateListener(new ZegoUserUpdateListener() {
            @Override
            public void onUserJoined(List<ZegoUIKitUser> userInfoList) {
                for (ZegoUIKitUser uiKitUser : userInfoList) {
                    if (Objects.equals(uiKitUser.userID, ZegoLiveStreamingManager.getInstance().getHostID())) {
                        binding.liveTopHostIcon.setText(uiKitUser.userName, false);
                        binding.liveTopHostName.setText(uiKitUser.userName);
                        break;
                    }
                    ZegoLiveStreamingManager.getInstance().removeUserStatus(uiKitUser.userID);
                }
            }

            @Override
            public void onUserLeft(List<ZegoUIKitUser> userInfoList) {
                for (ZegoUIKitUser uiKitUser : userInfoList) {
                    ZegoLiveStreamingManager.getInstance().removeUserStatus(uiKitUser.userID);
                }
                if (!ZegoLiveStreamingManager.getInstance().isCurrentUserHost()) {
                    PKInfo pkInfo = ZegoLiveStreamingManager.getInstance().getPKInfo();
                    if (pkInfo != null) {
                        for (ZegoUIKitUser zegosdkUser : userInfoList) {
                            if (zegosdkUser.userID.equals(pkInfo.hostUserID)) {
                                ZegoLiveStreamingManager.getInstance().stopPKBattleInner();
                            }
                        }
                    }
                }
            }
        });

        ZegoUIKit.addTurnOnYourMicrophoneRequestListener(fromUser -> {
            if (config.needConfirmWhenOthersTurnOnYourCamera) {
                ZegoDialogInfo dialogInfo = config.othersTurnOnYourMicrophoneConfirmDialogInfo;
                if (dialogInfo != null) {
                    String message = dialogInfo.message;
                    if (message.contains("%s")) {
                        message = String.format(message, fromUser.userName);
                    }
                    new ConfirmDialog.Builder(getContext()).setTitle(dialogInfo.title).setMessage(message)
                        .setPositiveButton(dialogInfo.confirmButtonName, (dialog, which) -> {
                            dialog.dismiss();
                            requestPermissionIfNeeded((allGranted, grantedList, deniedList) -> {
                                String userID = ZegoUIKit.getLocalUser().userID;
                                if (grantedList.contains(permission.RECORD_AUDIO)) {
                                    ZegoUIKit.turnMicrophoneOn(userID, true);
                                }
                            });
                        }).setNegativeButton(dialogInfo.cancelButtonName, (dialog, which) -> {
                            dialog.dismiss();
                        }).build().show();
                }
            } else {
                requestPermissionIfNeeded((allGranted, grantedList, deniedList) -> {
                    String userID = ZegoUIKit.getLocalUser().userID;
                    if (grantedList.contains(permission.RECORD_AUDIO)) {
                        ZegoUIKit.turnMicrophoneOn(userID, true);
                    }
                });
            }
        });

        ZegoUIKit.addTurnOnYourCameraRequestListener(new ZegoTurnOnYourCameraRequestListener() {
            @Override
            public void onTurnOnYourCameraRequest(ZegoUIKitUser fromUser) {
                if (config.needConfirmWhenOthersTurnOnYourMicrophone) {
                    ZegoDialogInfo dialogInfo = config.othersTurnOnYourCameraConfirmDialogInfo;
                    if (dialogInfo != null) {
                        String message = dialogInfo.message;
                        if (message.contains("%s")) {
                            message = String.format(message, fromUser.userName);
                        }
                        new ConfirmDialog.Builder(getContext()).setTitle(dialogInfo.title).setMessage(message)
                            .setPositiveButton(dialogInfo.confirmButtonName, (dialog, which) -> {
                                dialog.dismiss();
                                requestPermissionIfNeeded((allGranted, grantedList, deniedList) -> {
                                    String userID = ZegoUIKit.getLocalUser().userID;
                                    if (grantedList.contains(permission.CAMERA)) {
                                        ZegoUIKit.turnCameraOn(userID, true);
                                    }
                                });
                            }).setNegativeButton(dialogInfo.cancelButtonName, (dialog, which) -> {
                                dialog.dismiss();
                            }).build().show();
                    }
                } else {
                    requestPermissionIfNeeded((allGranted, grantedList, deniedList) -> {
                        String userID = ZegoUIKit.getLocalUser().userID;
                        if (grantedList.contains(permission.CAMERA)) {
                            ZegoUIKit.turnCameraOn(userID, true);
                        }
                    });
                }
            }
        });
        ZegoUIKit.addOnMeRemovedFromRoomListener(new ZegoMeRemovedFromRoomListener() {
            @Override
            public void onMeRemovedFromRoom() {
                ZegoUIKitPrebuiltLiveStreamingConfig config = ZegoLiveStreamingManager.getInstance()
                    .getPrebuiltConfig();
                if (config.removedFromRoomListener == null) {
                    leaveRoom();
                    requireActivity().finish();
                } else {
                    config.removedFromRoomListener.onMeRemovedFromRoom();
                }
            }
        });
    }

    public void setBackgroundView(View view) {
        this.backgroundView = view;
    }

    private void initLiveBtns() {
        ZegoUIKitPrebuiltLiveStreamingConfig config = ZegoLiveStreamingManager.getInstance().getPrebuiltConfig();
        if (config.role != ZegoLiveStreamingRole.HOST) {
            binding.liveBackgroundViewParent.setVisibility(View.VISIBLE);
            binding.liveVideoContainer.setVisibility(View.GONE);
        }
        ZegoTranslationText translationText = ZegoLiveStreamingManager.getInstance().getTranslationText();
        if (translationText != null && translationText.noHostOnline != null) {
            binding.liveNoHostHint.setText(translationText.noHostOnline);
        }
        initVideoContainer();

        if (config.confirmDialogInfo != null) {
            binding.liveTopExit.setConfirmDialogInfo(getDialogInfo());
        }
        binding.liveTopExit.setLeaveLiveListener(() -> {
            if (config.leaveLiveStreamingListener != null) {
                config.leaveLiveStreamingListener.onLeaveLiveStreaming();
            } else {
                leaveRoom();
                requireActivity().finish();
            }
        });

        binding.liveTopMemberCount.setOnClickListener(v -> {
            livememberList = new LiveMemberList(getContext());
            if (config.memberListConfig != null) {
                livememberList.setMemberListItemViewProvider(config.memberListConfig.memberListItemViewProvider);
            }
            if (config.avatarViewProvider != null) {
                livememberList.setAvatarViewProvider(config.avatarViewProvider);
            }
            livememberList.setEnableCoHosting(config.isEnableCoHosting());
            livememberList.show();
        });

        binding.liveBottomMenuBar.setConfig(config.bottomMenuBarConfig);
        for (Entry<ZegoLiveStreamingRole, List<View>> entry : bottomMenuBarExtendedButtons.entrySet()) {
            binding.liveBottomMenuBar.addExtendedButtons(entry.getValue(), entry.getKey());
        }
        if (config.role == ZegoLiveStreamingRole.HOST) {
            ZegoLiveStreamingManager.getInstance().setCurrentRole(ZegoLiveStreamingRole.HOST);
        } else if (config.role == ZegoLiveStreamingRole.COHOST) {
            ZegoLiveStreamingManager.getInstance().setCurrentRole(ZegoLiveStreamingRole.COHOST);
        } else {
            ZegoLiveStreamingManager.getInstance().setCurrentRole(ZegoLiveStreamingRole.AUDIENCE);
        }

        binding.liveBottomMenuBar.setScreenShareVideoConfig(config.screenSharingVideoConfig);

    }

    private ZegoDialogInfo getDialogInfo() {
        ZegoUIKitPrebuiltLiveStreamingConfig config = ZegoLiveStreamingManager.getInstance().getPrebuiltConfig();
        if (TextUtils.isEmpty(config.confirmDialogInfo.title)) {
            ZegoTranslationText translationText = ZegoLiveStreamingManager.getInstance().getTranslationText();
            if (translationText != null && translationText.stopLiveDialogInfo != null) {
                config.confirmDialogInfo.title = translationText.stopLiveDialogInfo.title;
            }
        }
        if (TextUtils.isEmpty(config.confirmDialogInfo.message)) {
            ZegoTranslationText translationText = ZegoLiveStreamingManager.getInstance().getTranslationText();
            if (translationText != null && translationText.stopLiveDialogInfo != null) {
                config.confirmDialogInfo.message = translationText.stopLiveDialogInfo.message;
            }
        }
        if (TextUtils.isEmpty(config.confirmDialogInfo.confirmButtonName)) {
            ZegoTranslationText translationText = ZegoLiveStreamingManager.getInstance().getTranslationText();
            if (translationText != null && translationText.stopLiveDialogInfo != null) {
                config.confirmDialogInfo.confirmButtonName = translationText.stopLiveDialogInfo.confirmButtonName;
            }
        }
        if (TextUtils.isEmpty(config.confirmDialogInfo.cancelButtonName)) {
            ZegoTranslationText translationText = ZegoLiveStreamingManager.getInstance().getTranslationText();
            if (translationText != null && translationText.stopLiveDialogInfo != null) {
                config.confirmDialogInfo.cancelButtonName = translationText.stopLiveDialogInfo.cancelButtonName;
            }
        }

        return config.confirmDialogInfo;
    }

    private void initPreviewBtns() {
        ZegoUIKitPrebuiltLiveStreamingConfig config = ZegoLiveStreamingManager.getInstance().getPrebuiltConfig();
        binding.previewBack.setOnClickListener(v -> {
            if (config.leaveLiveStreamingListener != null) {
                config.leaveLiveStreamingListener.onLeaveLiveStreaming();
            } else {
                leaveRoom();
                requireActivity().finish();
            }
        });
        binding.previewSwitch.setVisibility(config.turnOnCameraWhenJoining ? View.VISIBLE : View.GONE);

        if (config.translationText != null && config.translationText.startLiveStreamingButton != null) {
            binding.previewStart.setText(config.translationText.startLiveStreamingButton);
        }
        binding.previewStart.setOnClickListener(v -> {
            if (config.onStartLiveButtonPressed != null) {
                config.onStartLiveButtonPressed.onClick(v);
            }
        });
        if (config.startLiveButton != null) {
            binding.previewStartParent.removeView(binding.previewStart);
            binding.previewStartParent.addView(config.startLiveButton);
        }
    }

    private void showPreview() {
        binding.liveGroup.setVisibility(View.GONE);
        binding.previewGroup.setVisibility(View.VISIBLE);
    }

    public void showLiveView() {
        ZegoUIKitPrebuiltLiveStreamingConfig config = ZegoLiveStreamingManager.getInstance().getPrebuiltConfig();
        binding.liveGroup.setVisibility(View.VISIBLE);
        binding.liveTopMemberCount.setVisibility(config.showMemberButton ? View.VISIBLE : View.GONE);
        binding.previewGroup.setVisibility(View.GONE);
    }

    private void initVideoContainer() {
        ZegoUIKitPrebuiltLiveStreamingConfig config = ZegoLiveStreamingManager.getInstance().getPrebuiltConfig();
        if (config.zegoLayout == null) {
            ZegoLayout layout = new ZegoLayout();
            layout.mode = ZegoLayoutMode.PICTURE_IN_PICTURE;
            ZegoLayoutPictureInPictureConfig pipConfig = new ZegoLayoutPictureInPictureConfig();
            pipConfig.smallViewDefaultPosition = ZegoViewPosition.BOTTOM_RIGHT;
            pipConfig.removeViewWhenAudioVideoUnavailable = true;
            layout.config = pipConfig;
            binding.liveVideoContainer.setLayout(layout);
        } else {
            binding.liveVideoContainer.setLayout(config.zegoLayout);
        }
        if (config.audioVideoViewConfig != null) {
            ZegoAudioVideoViewConfig audioVideoViewConfig = new ZegoAudioVideoViewConfig();
            audioVideoViewConfig.showSoundWavesInAudioMode = config.audioVideoViewConfig.showSoundWaveOnAudioView;
            audioVideoViewConfig.useVideoViewAspectFill = config.audioVideoViewConfig.useVideoViewAspectFill;
            binding.liveVideoContainer.setAudioVideoConfig(audioVideoViewConfig);

            if (config.videoConfig != null) {
                ZegoVideoConfigPreset zegoVideoConfigPreset = ZegoVideoConfigPreset.getZegoVideoConfigPreset(
                    config.videoConfig.resolution.value());
                ZegoUIKit.setVideoConfig(new ZegoVideoConfig(zegoVideoConfigPreset));
            }
        }
        binding.liveVideoContainer.setAudioVideoComparator(new ZegoAudioVideoComparator() {
            @Override
            public List<ZegoUIKitUser> sortAudioVideo(List<ZegoUIKitUser> userList) {
                ZegoUIKitUser host = ZegoUIKit.getUser(ZegoLiveStreamingManager.getInstance().getHostID());
                if (userList.contains(host)) {
                    if (hostFirst) {
                        userList.remove(host);
                        userList.add(0, host);
                        hostFirst = false;
                    }
                }
                return userList;
            }
        });

        if (config.audioVideoViewConfig != null) {
            binding.liveVideoContainer.setAudioVideoForegroundViewProvider(config.audioVideoViewConfig.provider);
        } else {
            binding.liveVideoContainer.setAudioVideoForegroundViewProvider((parent, uiKitUser) -> {
                ZegoAudioVideoForegroundView foregroundView = new ZegoAudioVideoForegroundView(parent.getContext(),
                    uiKitUser.userID);
                return foregroundView;
            });
        }

        if (config.avatarViewProvider != null) {
            binding.liveVideoContainer.setAvatarViewProvider(config.avatarViewProvider);
        }

        binding.liveVideoContainer.setScreenShareForegroundViewProvider((parent, uiKitUser) -> {
            ZegoScreenShareForegroundView foregroundView = new ZegoScreenShareForegroundView(parent, uiKitUser.userID);
            foregroundView.setParentContainer(binding.liveVideoContainer);

            if (config.zegoLayout.config instanceof ZegoLayoutGalleryConfig) {
                ZegoLayoutGalleryConfig galleryConfig = (ZegoLayoutGalleryConfig) config.zegoLayout.config;
                foregroundView.setShowFullscreenModeToggleButtonRules(
                    galleryConfig.showScreenSharingFullscreenModeToggleButtonRules);
            }

            return foregroundView;
        });
    }

    private void requestPermissionIfNeeded(RequestCallback requestCallback) {
        List<String> permissions = Arrays.asList(permission.CAMERA, permission.RECORD_AUDIO);

        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
            }
        }
        if (allGranted) {
            requestCallback.onResult(true, permissions, new ArrayList<>());
            return;
        }

        PermissionX.init(requireActivity()).permissions(permissions).onExplainRequestReason((scope, deniedList) -> {
            String message = "";
            String camera = "";
            String mic = "";
            String ok = "";
            String micAndCamera = "";
            ZegoTranslationText translationText = ZegoLiveStreamingManager.getInstance().getTranslationText();
            if (translationText != null) {
                camera = translationText.permissionExplainCamera;
                mic = translationText.permissionExplainMic;
                micAndCamera = translationText.permissionExplainMicAndCamera;
                ok = translationText.ok;
            }
            if (deniedList.size() == 1) {
                if (deniedList.contains(permission.CAMERA)) {
                    message = camera;
                } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                    message = mic;
                }
            } else {
                message = micAndCamera;
            }
            scope.showRequestReasonDialog(deniedList, message, ok);
        }).onForwardToSettings((scope, deniedList) -> {
            String message = "";
            String settings = "";
            String cancel = "";
            String settingsCamera = "";
            String settingsMic = "";
            String settingsMicAndCamera = "";
            ZegoTranslationText translationText = ZegoLiveStreamingManager.getInstance().getTranslationText();
            if (translationText != null) {
                settings = translationText.settings;
                cancel = translationText.cancel;
                settingsCamera = translationText.settingCamera;
                settingsMic = translationText.settingMic;
                settingsMicAndCamera = translationText.settingMicAndCamera;
            }
            if (deniedList.size() == 1) {
                if (deniedList.contains(permission.CAMERA)) {
                    message = settingsCamera;
                } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                    message = settingsMic;
                }
            } else {
                message = settingsMicAndCamera;
            }
            scope.showForwardToSettingsDialog(deniedList, message, settings, cancel);
        }).request(new RequestCallback() {
            @Override
            public void onResult(boolean allGranted, @NonNull List<String> grantedList,
                @NonNull List<String> deniedList) {
                if (requestCallback != null) {
                    requestCallback.onResult(allGranted, grantedList, deniedList);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        leaveRoom();
    }

    private void showQuitDialog(ZegoDialogInfo dialogInfo) {
        new ConfirmDialog.Builder(getContext()).setTitle(dialogInfo.title).setMessage(dialogInfo.message)
            .setPositiveButton(dialogInfo.confirmButtonName, (dialog, which) -> {
                if (onBackPressedCallback != null) {
                    onBackPressedCallback.setEnabled(false);
                }
                dialog.dismiss();
                leaveRoom();
                requireActivity().onBackPressed();
            }).setNegativeButton(dialogInfo.cancelButtonName, (dialog, which) -> {
                dialog.dismiss();
            }).build().show();
    }

    /**
     *
     * @param viewList The list of custom buttons to be added.
     * @param role The role to which these buttons will be added for display.
     */
    public void addButtonToBottomMenuBar(List<View> viewList, ZegoLiveStreamingRole role) {
        bottomMenuBarExtendedButtons.put(role, viewList);
        if (binding != null) {
            binding.liveBottomMenuBar.addExtendedButtons(viewList, role);
        }
    }

    public void clearBottomMenuBarExtendButtons(ZegoLiveStreamingRole role) {
        bottomMenuBarExtendedButtons.remove(role);
        if (binding != null) {
            binding.liveBottomMenuBar.clearExtendedButtons(role);
        }
    }

    @Override
    public void showReceiveCoHostRequestDialog(ZegoUIKitUser inviter, int type, String data) {
        receiveCoHostRequestDialog = new ReceiveCoHostRequestDialog(getContext(), inviter, type, data);
        receiveCoHostRequestDialog.show();
    }

    @Override
    public void dismissReceiveCoHostRequestDialog() {
        if (receiveCoHostRequestDialog != null) {
            receiveCoHostRequestDialog.dismiss();
        }
    }

    @Override
    public void showReceiveCoHostInviteDialog(ZegoUIKitUser inviter, int type, String data) {
        Context context = getContext();
        ZegoAcceptCoHostButton acceptButton = new ZegoAcceptCoHostButton(context);
        acceptButton.setInviterID(inviter.userID);
        ZegoRefuseCoHostButton refuseButton = new ZegoRefuseCoHostButton(context);
        refuseButton.setInviterID(inviter.userID);
        refuseButton.setRequestCallbackListener(v -> {
            if (receiveCoHostInviteDialog != null) {
                receiveCoHostInviteDialog.dismiss();
            }
            ZegoLiveStreamingManager.getInstance().removeUserStatus(inviter.userID);
        });
        acceptButton.setRequestCallbackListener(v -> {
            if (receiveCoHostInviteDialog != null) {
                receiveCoHostInviteDialog.dismiss();
            }
            ZegoLiveStreamingManager.getInstance().removeUserStatus(inviter.userID);
            showCoHostButtons();
        });

        String title = "";
        String message = "";
        String cancelButtonName = "";
        String confirmButtonName = "";
        ZegoTranslationText translationText = ZegoLiveStreamingManager.getInstance().getTranslationText();
        if (translationText != null) {
            ZegoDialogInfo dialogInfo = translationText.receivedCoHostInvitationDialogInfo;
            if (dialogInfo != null && dialogInfo.title != null) {
                title = dialogInfo.title;
            }
            if (dialogInfo != null && dialogInfo.message != null) {
                message = dialogInfo.message;
            }
            if (dialogInfo != null && dialogInfo.cancelButtonName != null) {
                cancelButtonName = dialogInfo.cancelButtonName;
            }
            if (dialogInfo != null && dialogInfo.confirmButtonName != null) {
                confirmButtonName = dialogInfo.confirmButtonName;
            }
        }
        acceptButton.setText(confirmButtonName);
        refuseButton.setText(cancelButtonName);
        receiveCoHostInviteDialog = new ConfirmDialog.Builder(context).setTitle(title).setMessage(message)
            .setCustomPositiveButton(acceptButton).setCustomNegativeButton(refuseButton).build();
        receiveCoHostInviteDialog.show();
    }

    @Override
    public void dismissReceiveCoHostInviteDialog() {
        if (receiveCoHostInviteDialog != null) {
            receiveCoHostInviteDialog.dismiss();
        }
    }

    @Override
    public void removeCoHost(ZegoUIKitUser inviter, int type, String data) {
        ZegoUIKitUser localUser = ZegoUIKit.getLocalUser();
        if (localUser == null) {
            return;
        }
        ZegoUIKit.turnCameraOn(localUser.userID, false);
        ZegoUIKit.turnMicrophoneOn(localUser.userID, false);
        ZegoLiveStreamingManager.getInstance().setCurrentRole(ZegoLiveStreamingRole.AUDIENCE);
    }

    @Override
    public void showRequestCoHostButton() {
        binding.liveBottomMenuBar.showRequestCoHostButton();
    }

    @Override
    public void showCoHostButtons() {
        requestPermissionIfNeeded(new RequestCallback() {
            @Override
            public void onResult(boolean allGranted, @NonNull List<String> grantedList,
                @NonNull List<String> deniedList) {
                String localUserID = ZegoUIKit.getLocalUser().userID;
                ZegoUIKitPrebuiltLiveStreamingConfig config = ZegoLiveStreamingManager.getInstance()
                    .getPrebuiltConfig();
                ZegoUIKit.turnCameraOn(localUserID, config.turnOnCameraWhenCohosted);
                ZegoUIKit.turnMicrophoneOn(localUserID, true);
                ZegoLiveStreamingManager.getInstance().setCurrentRole(ZegoLiveStreamingRole.COHOST);
            }
        });
    }

    @Override
    public void showRedPoint() {
        binding.liveTopMemberCount.showRedPoint();
        if (livememberList != null) {
            livememberList.updateList();
        }
    }

    @Override
    public void hideRedPoint() {
        binding.liveTopMemberCount.hideRedPoint();
        if (livememberList != null) {
            livememberList.updateList();
        }
    }

    @Override
    public void showTopTips(String tips, boolean green) {
        binding.liveToast.setText(tips);
        binding.liveToast.setVisibility(View.VISIBLE);
        if (green) {
            binding.liveToast.setBackgroundColor(Color.parseColor("#55BC9E"));
        } else {
            binding.liveToast.setBackgroundColor(Color.parseColor("#BD5454"));
        }
        Handler handler = binding.getRoot().getHandler();
        if (handler != null) {
            handler.removeCallbacks(hideTipsRunnable);
            handler.postDelayed(hideTipsRunnable, 2000);
        }
    }
}