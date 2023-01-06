package com.zegocloud.uikit.prebuilt.livestreaming;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.components.audiovideocontainer.ZegoAudioVideoComparator;
import com.zegocloud.uikit.components.audiovideocontainer.ZegoAudioVideoViewConfig;
import com.zegocloud.uikit.components.audiovideocontainer.ZegoLayout;
import com.zegocloud.uikit.components.audiovideocontainer.ZegoLayoutMode;
import com.zegocloud.uikit.components.audiovideocontainer.ZegoLayoutPictureInPictureConfig;
import com.zegocloud.uikit.components.audiovideocontainer.ZegoViewPosition;
import com.zegocloud.uikit.prebuilt.livestreaming.core.PrebuiltUICallBack;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoDialogInfo;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoLiveStreamingRole;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoTranslationText;
import com.zegocloud.uikit.prebuilt.livestreaming.databinding.FragmentZegouikitPrebuiltLivestreamingBinding;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.ConfirmDialog;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.LiveMemberList;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.LiveStreamingManager;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.ReceiveCoHostRequestDialog;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.ZegoVideoForegroundView;
import com.zegocloud.uikit.prebuilt.livestreaming.widget.ZegoAcceptCoHostButton;
import com.zegocloud.uikit.prebuilt.livestreaming.widget.ZegoRefuseCoHostButton;
import com.zegocloud.uikit.service.defines.ZegoAudioVideoUpdateListener;
import com.zegocloud.uikit.service.defines.ZegoMeRemovedFromRoomListener;
import com.zegocloud.uikit.service.defines.ZegoRoomPropertyUpdateListener;
import com.zegocloud.uikit.service.defines.ZegoScenario;
import com.zegocloud.uikit.service.defines.ZegoTurnOnYourCameraRequestListener;
import com.zegocloud.uikit.service.defines.ZegoTurnOnYourMicrophoneRequestListener;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.service.defines.ZegoUserUpdateListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

public class ZegoUIKitPrebuiltLiveStreamingFragment extends Fragment implements PrebuiltUICallBack {

    private OnBackPressedCallback onBackPressedCallback;
    private FragmentZegouikitPrebuiltLivestreamingBinding binding;
    private Map<ZegoLiveStreamingRole, List<View>> bottomMenuBarExtendedButtons = new HashMap<>();

    private boolean isLocalUserHost = false;
    private ZegoUIKitPrebuiltLiveStreamingConfig config;
    private ConfirmDialog receiveCoHostInviteDialog;
    private ReceiveCoHostRequestDialog receiveCoHostRequestDialog;
    private LiveMemberList livememberList;
    private boolean hostFirst = true;
    private Runnable hideTipsRunnable = new Runnable() {
        @Override
        public void run() {
            binding.liveToast.setVisibility(View.GONE);
        }
    };

    public ZegoUIKitPrebuiltLiveStreamingFragment() {
        // Required empty public constructor
    }

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
        fragment.setPrebuiltLiveStreamingConfig(config);
        return fragment;
    }

    public void setPrebuiltLiveStreamingConfig(ZegoUIKitPrebuiltLiveStreamingConfig prebuiltLiveStreamingConfig) {
        config = prebuiltLiveStreamingConfig;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        long appID = arguments.getLong("appID");
        String appSign = arguments.getString("appSign");
        String userName = arguments.getString("userName");
        String userID = arguments.getString("userID");
        if (appID != 0) {
            ZegoUIKit.installPlugins(config.plugins);
            ZegoUIKit.init(requireActivity().getApplication(), appID, appSign, ZegoScenario.LIVE);
            ZegoUIKit.login(userID, userName);
            ZegoUIKit.getSignalingPlugin().login(userID, userName, null);
        }
        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                boolean hostStarted = isLiveStarted() && isLocalUserHost;
                boolean isNotHost = !isLocalUserHost;
                if (config.confirmDialogInfo != null && (hostStarted || isNotHost)) {
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
        if (isLocalUserHost) {
            Map<String, String> map = new HashMap<>();
            map.put("host", "");
            map.put("live_status", "0");
            ZegoUIKit.updateRoomProperties(map);
        }
        LiveStreamingManager.getInstance().unInit();
        ZegoUIKit.leaveRoom();
        ZegoUIKit.logout();
        ZegoUIKit.getSignalingPlugin().logout();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentZegouikitPrebuiltLivestreamingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.liveGroup.setVisibility(View.GONE);
        binding.previewGroup.setVisibility(View.GONE);
        String liveID = getArguments().getString("liveID");
        if (!TextUtils.isEmpty(liveID)) {
            ZegoUIKit.joinRoom(liveID, config.markAsLargeRoom, errorCode -> {
                if (errorCode == 0) {
                    onRoomJoinSucceed();
                } else {
                    onRoomJoinFailed();
                }
            });
        }
    }

    private void onRoomJoinFailed() {

    }

    private void onRoomJoinSucceed() {
        String userID = ZegoUIKit.getLocalUser().userID;
        isLocalUserHost = config.role == ZegoLiveStreamingRole.HOST;

        if (config.turnOnCameraWhenJoining || config.turnOnMicrophoneWhenJoining) {
            requestPermissionIfNeeded((allGranted, grantedList, deniedList) -> {
                if (grantedList.contains(permission.CAMERA)) {
                    if (config.turnOnCameraWhenJoining) {
                        ZegoUIKit.turnCameraOn(userID, true);
                    }
                }
                if (grantedList.contains(permission.RECORD_AUDIO)) {
                    if (config.turnOnMicrophoneWhenJoining) {
                        ZegoUIKit.turnMicrophoneOn(userID, true);
                    }
                }
            });
        }

        LiveStreamingManager.getInstance().init(getContext());
        LiveStreamingManager.getInstance().setTranslationText(config.translationText);
        LiveStreamingManager.getInstance().setPrebuiltUiCallBack(this);

        initPreviewBtns();
        initLiveBtns();

        if (isLocalUserHost) {
            showPreview();
            Map<String, String> map = new HashMap<>();
            map.put("host", userID);
            map.put("live_status", "0");
            ZegoUIKit.updateRoomProperties(map);
            ZegoUIKit.startPlayingAllAudioVideo();
        } else {
            showLiveView();
            ZegoUIKit.stopPlayingAllAudioVideo();
        }

        ZegoUIKit.setAudioOutputToSpeaker(config.useSpeakerWhenJoining);

        addUIKitListeners();
    }

    private void addUIKitListeners() {
        ZegoUIKit.addAudioVideoUpdateListener(new ZegoAudioVideoUpdateListener() {
            @Override
            public void onAudioVideoAvailable(List<ZegoUIKitUser> userList) {

            }

            @Override
            public void onAudioVideoUnAvailable(List<ZegoUIKitUser> userList) {
                for (ZegoUIKitUser uiKitUser : userList) {
                    if (Objects.equals(uiKitUser.userID, getHostID())) {
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
                isLocalUserHost = Objects.equals(getHostID(), userID);
                if (Objects.equals("host", key)) {
                    if (newValue != null) {
                        ZegoUIKitUser uiKitUser = ZegoUIKit.getUser(getHostID());
                        if (uiKitUser != null) {
                            binding.liveHostIcon.setTextOnly(uiKitUser.userName);
                            binding.liveHostName.setText(uiKitUser.userName);
                        } else {
                            binding.liveHostIcon.setTextOnly("");
                            binding.liveHostName.setText("");
                        }
                        // second host in ,pre become audience
                        if (oldValue != null && oldValue.equals(userID) && !isLocalUserHost) {
                            ZegoUIKit.turnCameraOn(userID, false);
                            ZegoUIKit.turnMicrophoneOn(userID, false);
                            ZegoUIKit.stopPlayingAllAudioVideo();
                            showLiveView();
                            binding.liveBottomMenuBar.showAudienceButtons();
                            binding.liveNoHostHint.setVisibility(View.VISIBLE);
                            binding.liveVideoContainer.setVisibility(View.GONE);
                        }
                    }
                    binding.liveVideoContainer.updateLayout();
                }
                if (Objects.equals("live_status", key)) {
                    if ("1".equals(newValue)) {
                        ZegoUIKit.startPlayingAllAudioVideo();
                        binding.liveNoHostHint.setVisibility(View.GONE);
                        binding.liveVideoContainer.setVisibility(View.VISIBLE);
                        if (oldValue != null) {
                            showLiveView();
                        }
                    } else if ("0".equals(newValue)) {
                        ZegoUIKit.stopPlayingAllAudioVideo();
                        hostFirst = true;
                        if ("1".equals(oldValue)) {
                            if (!isLocalUserHost) {
                                ZegoUIKit.turnCameraOn(userID, false);
                                ZegoUIKit.turnMicrophoneOn(userID, false);
                                if (config.zegoLiveStreamingEndListener != null) {
                                    config.zegoLiveStreamingEndListener.onLiveStreamingEnded();
                                }
                            }

                        }
                        if (isLocalUserHost) {
                            binding.liveNoHostHint.setVisibility(View.GONE);
                            binding.liveVideoContainer.setVisibility(View.VISIBLE);
                            binding.liveBottomMenuBar.showHostButtons();
                        } else {
                            binding.liveNoHostHint.setVisibility(View.VISIBLE);
                            binding.liveVideoContainer.setVisibility(View.GONE);
                            binding.liveBottomMenuBar.showAudienceButtons();
                            dismissReceiveCoHostInviteDialog();
                            binding.liveBottomMenuBar.onLiveEnd();
                        }
                    }
                }
                if (Objects.equals("enableChat", key)) {
                    if (!isLocalUserHost) {
                        if (newValue.equals("0")) {
                            binding.liveBottomMenuBar.enableChat(false);
                        } else if (newValue.equals("1")) {
                            binding.liveBottomMenuBar.enableChat(true);
                        }
                    }
                }
            }

            @Override
            public void onRoomPropertiesFullUpdated(List<String> updateKeys, Map<String, String> oldProperties,
                Map<String, String> properties) {
                if (config.role == ZegoLiveStreamingRole.HOST) {
                    String currentUserID = ZegoUIKit.getLocalUser().userID;
                    boolean thereIsHostInRoom = false;
                    for (int i = 0; i < updateKeys.size(); ++i) {
                        String key = updateKeys.get(i);
                        if ("host".equals(key) && (Objects.equals(properties.get(key), "") || Objects.equals(
                            properties.get(key), currentUserID))) {
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
                    if (Objects.equals(uiKitUser.userID, getHostID())) {
                        binding.liveHostIcon.setText(uiKitUser.userName, false);
                        binding.liveHostName.setText(uiKitUser.userName);
                        break;
                    }
                    LiveStreamingManager.getInstance().removeUserStatus(uiKitUser.userID);
                }
            }

            @Override
            public void onUserLeft(List<ZegoUIKitUser> userInfoList) {
                for (ZegoUIKitUser uiKitUser : userInfoList) {
                    LiveStreamingManager.getInstance().removeUserStatus(uiKitUser.userID);
                }
            }
        });
        ZegoUIKit.addTurnOnYourMicrophoneRequestListener(new ZegoTurnOnYourMicrophoneRequestListener() {
            @Override
            public void onTurnOnYourMicrophoneRequest(ZegoUIKitUser fromUser) {
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
                requireActivity().onBackPressed();
            }
        });
    }

    private void initLiveBtns() {
        if (config.role != ZegoLiveStreamingRole.HOST) {
            binding.liveNoHostHint.setVisibility(View.VISIBLE);
            binding.liveVideoContainer.setVisibility(View.GONE);
        }
        ZegoTranslationText translationText = LiveStreamingManager.getInstance().getTranslationText();
        if (translationText != null && translationText.noHostOnline != null) {
            binding.liveNoHostHint.setText(translationText.noHostOnline);
        }
        initVideoContainer();

        if (config.confirmDialogInfo != null) {
            binding.liveExit.setConfirmDialogInfo(getDialogInfo());
        }
        binding.liveExit.setLeaveLiveListener(() -> {
            if (config.leaveLiveStreamingListener != null) {
                config.leaveLiveStreamingListener.onLeaveLiveStreaming();
            } else {
                leaveRoom();
                requireActivity().finish();
            }
        });

        binding.liveMemberCount.setOnClickListener(v -> {
            livememberList = new LiveMemberList(getContext());
            if (config.memberListConfig != null) {
                livememberList.setMemberListItemViewProvider(config.memberListConfig.memberListItemViewProvider);
            }
            livememberList.setEnableCoHosting(config.plugins != null && !config.plugins.isEmpty());
            livememberList.show();
        });
        initBottomMenuBar();
    }

    private void initBottomMenuBar() {
        binding.liveBottomMenuBar.setConfig(config.bottomMenuBarConfig);
        for (Entry<ZegoLiveStreamingRole, List<View>> entry : bottomMenuBarExtendedButtons.entrySet()) {
            binding.liveBottomMenuBar.addExtendedButtons(entry.getValue(), entry.getKey());
        }
        if (config.role == ZegoLiveStreamingRole.HOST) {
            binding.liveBottomMenuBar.showHostButtons();
        } else if (config.role == ZegoLiveStreamingRole.COHOST) {
            binding.liveBottomMenuBar.showCoHostButtons();
        } else {
            binding.liveBottomMenuBar.showAudienceButtons();
        }
    }

    private ZegoDialogInfo getDialogInfo() {
        ZegoDialogInfo dialogInfo = new ZegoDialogInfo();
        if (config.confirmDialogInfo.title == null) {
            dialogInfo.title = getString(R.string.stop_live_title);
        } else {
            dialogInfo.title = config.confirmDialogInfo.title;
        }
        if (config.confirmDialogInfo.message == null) {
            dialogInfo.message = getString(R.string.stop_live_message);
        } else {
            dialogInfo.message = config.confirmDialogInfo.message;
        }
        if (config.confirmDialogInfo.confirmButtonName == null) {
            dialogInfo.confirmButtonName = getString(R.string.stop_live_ok);
        } else {
            dialogInfo.confirmButtonName = config.confirmDialogInfo.confirmButtonName;
        }
        if (config.confirmDialogInfo.cancelButtonName == null) {
            dialogInfo.cancelButtonName = getString(R.string.stop_live_cancel);
        } else {
            dialogInfo.cancelButtonName = config.confirmDialogInfo.cancelButtonName;
        }
        return dialogInfo;
    }

    private void initPreviewBtns() {
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

    private boolean isLiveStarted() {
        return "1".equals(ZegoUIKit.getRoomProperties().get("live_status"));
    }

    private String getHostID() {
        return ZegoUIKit.getRoomProperties().get("host");
    }

    private void showPreview() {
        binding.liveGroup.setVisibility(View.GONE);
        binding.previewGroup.setVisibility(View.VISIBLE);
    }

    public void showLiveView() {
        binding.liveGroup.setVisibility(View.VISIBLE);
        binding.previewGroup.setVisibility(View.GONE);
    }

    private void initVideoContainer() {
        ZegoLayout layout = new ZegoLayout();
        layout.mode = ZegoLayoutMode.PICTURE_IN_PICTURE;
        ZegoLayoutPictureInPictureConfig pipConfig = new ZegoLayoutPictureInPictureConfig();
        pipConfig.smallViewDefaultPosition = ZegoViewPosition.BOTTOM_RIGHT;
        layout.config = pipConfig;
        binding.liveVideoContainer.setLayout(layout);
        if (config.audioVideoViewConfig != null) {
            ZegoAudioVideoViewConfig audioVideoViewConfig = new ZegoAudioVideoViewConfig();
            audioVideoViewConfig.showSoundWavesInAudioMode = config.audioVideoViewConfig.showSoundWaveOnAudioView;
            audioVideoViewConfig.useVideoViewAspectFill = config.audioVideoViewConfig.useVideoViewAspectFill;
            binding.liveVideoContainer.setAudioVideoConfig(audioVideoViewConfig);
        }
        binding.liveVideoContainer.setAudioVideoComparator(new ZegoAudioVideoComparator() {
            @Override
            public List<ZegoUIKitUser> sortAudioVideo(List<ZegoUIKitUser> userList) {
                ZegoUIKitUser host = ZegoUIKit.getUser(getHostID());
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
            binding.liveVideoContainer.setForegroundViewProvider(config.audioVideoViewConfig.provider);
        } else {
            binding.liveVideoContainer.setForegroundViewProvider((parent, uiKitUser) -> {
                ZegoVideoForegroundView foregroundView = new ZegoVideoForegroundView(parent.getContext(), uiKitUser);
                foregroundView.showCamera(false);
                return foregroundView;
            });
        }
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
            if (deniedList.size() == 1) {
                if (deniedList.contains(permission.CAMERA)) {
                    message = getContext().getString(R.string.permission_explain_camera);
                } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                    message = getContext().getString(R.string.permission_explain_mic);
                }
            } else {
                message = getContext().getString(R.string.permission_explain_camera_mic);
            }
            scope.showRequestReasonDialog(deniedList, message, getString(R.string.ok));
        }).onForwardToSettings((scope, deniedList) -> {
            String message = "";
            if (deniedList.size() == 1) {
                if (deniedList.contains(permission.CAMERA)) {
                    message = getContext().getString(R.string.settings_camera);
                } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                    message = getContext().getString(R.string.settings_mic);
                }
            } else {
                message = getContext().getString(R.string.settings_camera_mic);
            }
            scope.showForwardToSettingsDialog(deniedList, message, getString(R.string.settings),
                getString(R.string.cancel));
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
        if (livememberList != null) {
            livememberList.dismiss();
        }
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
            LiveStreamingManager.getInstance().removeUserStatus(inviter.userID);
        });
        acceptButton.setRequestCallbackListener(v -> {
            if (receiveCoHostInviteDialog != null) {
                receiveCoHostInviteDialog.dismiss();
            }
            LiveStreamingManager.getInstance().removeUserStatus(inviter.userID);
            showCoHostButtons();
        });

        String title = context.getString(R.string.receive_co_host_invite_title);
        String message = context.getString(R.string.receive_co_host_invite_message);
        String cancelButtonName = context.getString(R.string.receive_co_host_invite_cancel);
        String confirmButtonName = context.getString(R.string.receive_co_host_invite_ok);
        ZegoTranslationText translationText = LiveStreamingManager.getInstance().getTranslationText();
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
        binding.liveBottomMenuBar.showAudienceButtons();
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
                ZegoUIKit.turnCameraOn(localUserID, true);
                ZegoUIKit.turnMicrophoneOn(localUserID, true);
                binding.liveBottomMenuBar.showCoHostButtons();
            }
        });
    }

    @Override
    public void showRedPoint() {
        binding.liveMemberCountNotify.setVisibility(View.VISIBLE);
        if (livememberList != null) {
            livememberList.updateList();
        }
    }

    @Override
    public void hideRedPoint() {
        binding.liveMemberCountNotify.setVisibility(View.GONE);
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