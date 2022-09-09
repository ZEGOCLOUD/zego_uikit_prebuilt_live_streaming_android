package com.zegocloud.uikit.prebuilt.livestreaming;

import android.Manifest;
import android.Manifest.permission;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.permissionx.guolindev.PermissionX;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.components.audiovideo.ZegoViewProvider;
import com.zegocloud.uikit.components.audiovideocontainer.ZegoAudioVideoViewConfig;
import com.zegocloud.uikit.prebuilt.livestreaming.databinding.FragmentZegouikitPrebuiltLivestreamingBinding;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.LeaveLiveStreamingListener;
import com.zegocloud.uikit.service.defines.AudioVideoUpdateListener;
import com.zegocloud.uikit.service.defines.RoomUserUpdateListener;
import com.zegocloud.uikit.service.defines.ZegoScenario;
import com.zegocloud.uikit.service.defines.ZegoUIKitCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ZegoUIKitPrebuiltLiveStreamingFragment extends Fragment {

    private OnBackPressedCallback onBackPressedCallback;
    private FragmentZegouikitPrebuiltLivestreamingBinding binding;
    private List<View> menuBarExtendedButtons = new ArrayList<>();
    private ZegoViewProvider provider;
    private LeaveLiveStreamingListener leaveListener;

    public ZegoUIKitPrebuiltLiveStreamingFragment() {
        // Required empty public constructor
    }

    public static ZegoUIKitPrebuiltLiveStreamingFragment newInstance(long appID, String appSign,
        String userID, String userName, String liveID, ZegoUIKitPrebuiltLiveStreamingConfig config) {

        ZegoUIKitPrebuiltLiveStreamingFragment fragment = new ZegoUIKitPrebuiltLiveStreamingFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("appID", appID);
        bundle.putString("appSign", appSign);
        bundle.putString("liveID", liveID);
        bundle.putString("userID", userID);
        bundle.putString("userName", userName);
        bundle.putSerializable("config", config);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        long appID = arguments.getLong("appID");
        String appSign = arguments.getString("appSign");
        String userID = arguments.getString("userID");
        String userName = arguments.getString("userName");
        if (appID != 0) {
            ZegoUIKit.init(requireActivity().getApplication(), appID, appSign, ZegoScenario.LIVE);
            ZegoUIKit.login(userID, userName);
        }
        ZegoUIKitPrebuiltLiveStreamingConfig config = (ZegoUIKitPrebuiltLiveStreamingConfig) arguments.getSerializable(
            "config");
        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (config.confirmDialogInfo != null) {
                    showQuitDialog(config.confirmDialogInfo);
                } else {
                    ZegoUIKit.leaveRoom();
                    setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        binding = FragmentZegouikitPrebuiltLivestreamingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String liveID = getArguments().getString("liveID");
        if (!TextUtils.isEmpty(liveID)) {
            ZegoUIKit.joinRoom(liveID, new ZegoUIKitCallback() {
                @Override
                public void onResult(int errorCode) {
                    if (errorCode == 0) {
                        onRoomJoinSucceed();
                    } else {
                        onRoomJoinFailed();
                    }
                }
            });
        }
    }

    private void onRoomJoinFailed() {

    }

    private void onRoomJoinSucceed() {
        Bundle arguments = getArguments();
        ZegoUIKitPrebuiltLiveStreamingConfig config = (ZegoUIKitPrebuiltLiveStreamingConfig) arguments.getSerializable(
            "config");
        String userID = arguments.getString("userID");

        ZegoUIKit.addAudioVideoUpdateListener(new AudioVideoUpdateListener() {
            @Override
            public void onAudioVideoAvailable(List<ZegoUIKitUser> userList) {
                if (userList.size() > 0) {
                    ZegoUIKitUser uiKitUser = userList.get(0);
                    binding.hostVideoView.setUserID(uiKitUser.userID);
                    binding.hostVideoView.setAudioViewBackgroundColor(Color.parseColor("#4A4B4D"));
                    binding.noHostHint.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAudioVideoUnAvailable(List<ZegoUIKitUser> userList) {
            }
        });

        if (provider != null) {
            binding.hostVideoView.setForegroundViewProvider(provider);
        }
        ZegoUIKit.addUserUpdateListener(new RoomUserUpdateListener() {
            @Override
            public void onUserJoined(List<ZegoUIKitUser> userInfoList) {

            }

            @Override
            public void onUserJoinLeft(List<ZegoUIKitUser> userInfoList) {
                for (ZegoUIKitUser zegoUIKitUser : userInfoList) {
                    if (Objects.equals(zegoUIKitUser.userID, binding.hostVideoView.getUserID())) {
                        binding.hostVideoView.setUserID("");
                        binding.hostVideoView.setAudioViewBackgroundColor(Color.TRANSPARENT);
                        binding.noHostHint.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        if (config.confirmDialogInfo != null) {
            binding.exitLive.setConfirmDialogInfo(config.confirmDialogInfo);
        }
        binding.exitLive.setLeaveLiveListener(() -> {
            if (leaveListener != null) {
                leaveListener.onLeaveLiveStreaming();
            } else {
                ZegoUIKit.leaveRoom();
                requireActivity().finish();
            }
        });

        ZegoAudioVideoViewConfig audioVideoViewConfig = new ZegoAudioVideoViewConfig();
        audioVideoViewConfig.showSoundWavesInAudioMode = config.showSoundWaveOnAudioView;
        binding.hostVideoView.setAudioVideoConfig(audioVideoViewConfig);

        if (config.turnOnCameraWhenJoining) {
            ZegoUIKit.turnCameraOn(userID, true);
        }
        if (config.turnOnMicrophoneWhenJoining) {
            ZegoUIKit.turnMicrophoneOn(userID, true);
        }
        ZegoUIKit.setAudioOutputToSpeaker(config.useSpeakerWhenJoining);

        boolean permissionGranted =
            PermissionX.isGranted(getContext(), permission.CAMERA)
                && PermissionX.isGranted(getContext(), permission.RECORD_AUDIO);
        if (!permissionGranted) {
            PermissionX.init(requireActivity())
                .permissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                .onExplainRequestReason((scope, deniedList) -> {
                    scope.showRequestReasonDialog(deniedList,
                        "We require camera&microphone access to connect a living",
                        "OK", "Cancel");
                })
                .request((allGranted, grantedList, deniedList) -> {
                    if (config.turnOnCameraWhenJoining) {
                        ZegoUIKit.turnCameraOn(userID, false);
                        ZegoUIKit.turnCameraOn(userID, true);
                    }
                    if (config.turnOnMicrophoneWhenJoining) {
                        ZegoUIKit.turnMicrophoneOn(userID, false);
                        ZegoUIKit.turnMicrophoneOn(userID, true);
                    }
                });
        }

        binding.bottomMenuBar.setButtons(config.menuBarButtons);
        binding.bottomMenuBar.showInRoomMessageButton(config.showInRoomMessageButton);
        binding.bottomMenuBar.setLimitedCount(config.menuBarButtonsMaxCount);
        if (menuBarExtendedButtons.size() > 0) {
            binding.bottomMenuBar.addButtons(menuBarExtendedButtons);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ZegoUIKit.leaveRoom();
    }

    private void showQuitDialog(ZegoConfirmDialogInfo confirmDialogInfo) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setTitle(confirmDialogInfo.title);
        builder.setMessage(confirmDialogInfo.message);
        builder.setPositiveButton(confirmDialogInfo.confirmButtonName, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (onBackPressedCallback != null) {
                    onBackPressedCallback.setEnabled(false);
                }
                ZegoUIKit.leaveRoom();
                requireActivity().onBackPressed();
            }
        });
        builder.setNegativeButton(confirmDialogInfo.cancelButtonName, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    public void addButtonToBottomMenuBar(List<View> viewList) {
        menuBarExtendedButtons.addAll(viewList);
        if (binding != null) {
            binding.bottomMenuBar.addButtons(viewList);
        }
    }

    public void setForegroundViewProvider(ZegoViewProvider provider) {
        this.provider = provider;
        if (binding != null) {
            binding.hostVideoView.setForegroundViewProvider(provider);
        }
    }

    public void setLeaveLiveStreamingListener(LeaveLiveStreamingListener listener) {
        leaveListener = listener;
    }

}