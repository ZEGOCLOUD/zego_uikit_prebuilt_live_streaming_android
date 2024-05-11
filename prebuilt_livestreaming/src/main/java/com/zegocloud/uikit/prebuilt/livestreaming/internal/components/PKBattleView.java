package com.zegocloud.uikit.prebuilt.livestreaming.internal.components;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.components.audiovideo.ZegoAvatarViewProvider;
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoLiveStreamingManager;
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoLiveStreamingManager.ZegoLiveStreamingListener;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoLiveStreamingPKBattleConfig;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoPrebuiltAudioVideoViewConfig;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoTranslationText;
import com.zegocloud.uikit.prebuilt.livestreaming.internal.core.PKService.PKInfo;
import com.zegocloud.uikit.prebuilt.livestreaming.R;
import com.zegocloud.uikit.prebuilt.livestreaming.databinding.LivestreamingLayoutPkBinding;
import com.zegocloud.uikit.service.defines.ZegoCameraStateChangeListener;
import com.zegocloud.uikit.service.defines.ZegoUIKitCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import im.zego.zegoexpress.constants.ZegoViewMode;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class PKBattleView extends FrameLayout {

    private LivestreamingLayoutPkBinding binding;
    private ZegoLiveStreamingPKBattleConfig pkBattleConfig;
    private ZegoPrebuiltAudioVideoViewConfig prebuiltAudioVideoViewConfig;
    private ZegoAvatarViewProvider avatarViewProvider;

    public PKBattleView(@NonNull Context context) {
        super(context);
        initView();
    }

    public PKBattleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PKBattleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public PKBattleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        binding = LivestreamingLayoutPkBinding.inflate(LayoutInflater.from(getContext()), this, true);

        ZegoLiveStreamingManager.getInstance().addLiveStreamingListener(new ZegoLiveStreamingListener() {

            @Override
            public void onPKStarted() {
                PKInfo pkInfo = ZegoLiveStreamingManager.getInstance().getPKInfo();
                onRoomPKStarted(pkInfo);
            }

            @Override
            public void onPKEnded() {
                onRoomPKEnded();
            }

            @Override
            public void onOtherHostCameraOpen(String userID, boolean open) {
                if (ZegoLiveStreamingManager.getInstance().isPKUser(userID)) {
                    onPKUserCameraUpdate(userID, open);
                } else if (ZegoLiveStreamingManager.getInstance().isHost(userID)) {
                    onHostCameraUpdate(open);
                }
            }

            @Override
            public void onPKUserDisConnected(String userID, boolean disconnected) {
                if (ZegoLiveStreamingManager.getInstance().isCurrentUserHost()) {
                    if (userID.equals(binding.pkOtherVideo.getUserID())) {
                        if (disconnected) {
                            binding.pkOtherVideoTips.setVisibility(View.VISIBLE);
                            binding.pkOtherVideo.mutePlayAudio(true);
                            if (!ZegoLiveStreamingManager.getInstance().isAnotherHostMuted()) {
                                ZegoLiveStreamingManager.getInstance()
                                    .muteAnotherHostAudio(true, new ZegoUIKitCallback() {
                                        @Override
                                        public void onResult(int errorCode) {
                                        }
                                    });
                            }
                        } else {
                            if (ZegoLiveStreamingManager.getInstance().isAnotherHostMuted()) {
                                ZegoLiveStreamingManager.getInstance()
                                    .muteAnotherHostAudio(false, new ZegoUIKitCallback() {
                                        @Override
                                        public void onResult(int errorCode) {
                                        }
                                    });
                            }
                            binding.pkOtherVideoTips.setVisibility(View.GONE);
                            binding.pkOtherVideo.mutePlayAudio(false);
                        }
                    }
                } else {
                    if (ZegoLiveStreamingManager.getInstance().isHost(userID)) {
                        if (disconnected) {
                            if (binding.audienceMixSelfTips.getVisibility() != View.VISIBLE) {
                                binding.audienceMixSelfTips.setVisibility(View.VISIBLE);
                            }
                        } else {
                            if (binding.audienceMixSelfTips.getVisibility() != View.GONE) {
                                binding.audienceMixSelfTips.setVisibility(View.GONE);
                            }
                        }
                    } else if (ZegoLiveStreamingManager.getInstance().isPKUser(userID)) {
                        if (disconnected) {
                            if (binding.audienceMixOtherTips.getVisibility() != View.VISIBLE) {
                                binding.audienceMixOtherTips.setVisibility(View.VISIBLE);
                            }
                        } else {
                            if (binding.audienceMixOtherTips.getVisibility() != View.GONE) {
                                binding.audienceMixOtherTips.setVisibility(View.GONE);
                            }
                        }
                    }
                }
            }

        });

        ZegoUIKit.addCameraStateListener(new ZegoCameraStateChangeListener() {
            @Override
            public void onCameraOn(ZegoUIKitUser uiKitUser, boolean isOn) {
                String hostID = ZegoLiveStreamingManager.getInstance().getHostID();
                ZegoUIKitUser hostUser = ZegoUIKit.getUser(hostID);
                boolean isHostUser = hostUser != null && Objects.equals(hostUser.userID, uiKitUser.userID);
                if (isHostUser) {
                    onHostCameraUpdate(isOn);
                }
            }
        });
    }

    private void onPKUserCameraUpdate(String userID, boolean open) {
        if (ZegoLiveStreamingManager.getInstance().isCurrentUserHost()) {
            if (open) {
                binding.pkOtherVideoIcon.setVisibility(View.INVISIBLE);
                binding.pkOtherVideo.setVisibility(View.VISIBLE);
            } else {
                binding.pkOtherVideoIcon.setVisibility(View.VISIBLE);
                binding.pkOtherVideo.setVisibility(View.INVISIBLE);
            }
        } else {
            if (open) {
                binding.audienceMixOtherIcon.setVisibility(View.INVISIBLE);
            } else {
                binding.audienceMixOtherIcon.setVisibility(View.VISIBLE);
            }
        }
    }

    private void onHostCameraUpdate(boolean open) {
        if (ZegoLiveStreamingManager.getInstance().isCurrentUserHost()) {
            if (open) {
                binding.pkSelfVideoIcon.setVisibility(View.INVISIBLE);
                binding.pkSelfVideo.setVisibility(View.VISIBLE);
            } else {
                binding.pkSelfVideoIcon.setVisibility(View.VISIBLE);
                binding.pkSelfVideo.setVisibility(View.INVISIBLE);
            }
        } else {
            if (open) {
                binding.audienceMixSelfIcon.setVisibility(View.INVISIBLE);
            } else {
                binding.audienceMixSelfIcon.setVisibility(View.VISIBLE);
            }
        }
    }

    private void onRoomPKStarted(PKInfo pkInfo) {
        ZegoUIKitUser currentUser = ZegoUIKit.getLocalUser();

        String hostID = ZegoLiveStreamingManager.getInstance().getHostID();
        ZegoUIKitUser hostUser = ZegoUIKit.getUser(hostID);

        if (ZegoLiveStreamingManager.getInstance().isCurrentUserHost()) {
            binding.pkOtherVideoLayout.setVisibility(View.VISIBLE);
            binding.pkSelfVideoLayout.setVisibility(View.VISIBLE);
            binding.pkOtherVideo.setUserID(pkInfo.pkUser.userID);
            binding.pkOtherVideo.setStreamID(pkInfo.getPKStream());
            if (avatarViewProvider != null) {
                binding.pkOtherVideoIcon.setAvatarViewProvider(avatarViewProvider);
            }
            binding.pkOtherVideoIcon.updateUser(pkInfo.pkUser.userID, pkInfo.pkUser.userName);
            binding.pkOtherVideoIcon.setStreamID(pkInfo.getPKStream());
            if (prebuiltAudioVideoViewConfig != null) {
                //                if (prebuiltAudioVideoViewConfig.provider != null) {
                //                    ZegoBaseAudioVideoForegroundView foregroundView = prebuiltAudioVideoViewConfig.provider.getForegroundView(
                //                        this, pkInfo.pkUser);
                //                    binding.pkOtherVideoForeground.removeAllViews();
                //                    binding.pkOtherVideoForeground.addView(foregroundView);
                //                }
                binding.pkOtherVideoIcon.setShowSoundWave(prebuiltAudioVideoViewConfig.showSoundWaveOnAudioView);
                if (prebuiltAudioVideoViewConfig.useVideoViewAspectFill) {
                    binding.pkOtherVideo.startPlayRemoteAudioVideo();
                } else {
                    binding.pkOtherVideo.startPlayRemoteAudioVideo(ZegoViewMode.ASPECT_FIT);
                }
            } else {
                binding.pkOtherVideo.startPlayRemoteAudioVideo();
            }

            binding.pkSelfVideo.setUserID(currentUser.userID);
            if (avatarViewProvider != null) {
                binding.pkSelfVideoIcon.setAvatarViewProvider(avatarViewProvider);
            }
            binding.pkSelfVideoIcon.updateUser(currentUser.userID, currentUser.userName);
            if (prebuiltAudioVideoViewConfig != null) {
                //                if (prebuiltAudioVideoViewConfig.provider != null) {
                //                    ZegoBaseAudioVideoForegroundView foregroundView = prebuiltAudioVideoViewConfig.provider.getForegroundView(
                //                        this, currentUser);
                //                    binding.pkSelfVideoForeground.removeAllViews();
                //                    binding.pkSelfVideoForeground.addView(foregroundView);
                //                }
                binding.pkSelfVideoIcon.setShowSoundWave(prebuiltAudioVideoViewConfig.showSoundWaveOnAudioView);
            }
            binding.pkSelfVideo.startPreviewOnly();
        } else {

            binding.pkOtherVideoLayout.setVisibility(View.INVISIBLE);
            binding.pkSelfVideoLayout.setVisibility(View.INVISIBLE);

            if (avatarViewProvider != null) {
                binding.audienceMixSelfIcon.setAvatarViewProvider(avatarViewProvider);
            }
            binding.audienceMixSelfIcon.updateUser(hostUser.userID, hostUser.userName);
            if (prebuiltAudioVideoViewConfig != null) {
                //                if (prebuiltAudioVideoViewConfig.provider != null) {
                //                    ZegoBaseAudioVideoForegroundView foregroundView = prebuiltAudioVideoViewConfig.provider.getForegroundView(
                //                        this, hostUser);
                //                    binding.audienceMixSelfForeground.removeAllViews();
                //                    binding.audienceMixSelfForeground.addView(foregroundView);
                //                }
                binding.audienceMixSelfIcon.setShowSoundWave(prebuiltAudioVideoViewConfig.showSoundWaveOnAudioView);
            }

            if (avatarViewProvider != null) {
                binding.audienceMixOtherIcon.setAvatarViewProvider(avatarViewProvider);
            }
            binding.audienceMixOtherIcon.updateUser(pkInfo.pkUser.userID, pkInfo.pkUser.userName);
            if (prebuiltAudioVideoViewConfig != null) {
                //                if (prebuiltAudioVideoViewConfig.provider != null) {
                //                    ZegoBaseAudioVideoForegroundView foregroundView = prebuiltAudioVideoViewConfig.provider.getForegroundView(
                //                        this, pkInfo.pkUser);
                //                    binding.audienceMixOtherForeground.removeAllViews();
                //                    binding.audienceMixOtherForeground.addView(foregroundView);
                //                }
                binding.audienceMixOtherIcon.setShowSoundWave(prebuiltAudioVideoViewConfig.showSoundWaveOnAudioView);
            }

            String streamID = ZegoUIKit.getRoom().roomID + "_mix";
            binding.audienceMixVideo.setStreamID(streamID);
            binding.audienceMixVideo.startPlayRemoteAudioVideo();
        }

        onHostCameraUpdate(hostUser.isCameraOn);

        List<ZegoUIKitUser> uiKitUsers = Arrays.asList(hostUser, pkInfo.pkUser);
        if (pkBattleConfig != null && pkBattleConfig.pkBattleViewTopProvider != null) {
            View providerView = pkBattleConfig.pkBattleViewTopProvider.getView(binding.pkLayoutTop, uiKitUsers);
            binding.pkLayoutTop.removeAllViews();
            if (providerView != null) {
                binding.pkLayoutTop.addView(providerView);
            }
        }
        if (pkBattleConfig != null && pkBattleConfig.pkBattleViewBottomProvider != null) {
            View providerView = pkBattleConfig.pkBattleViewBottomProvider.getView(binding.pkLayoutBottom, uiKitUsers);
            binding.pkLayoutBottom.removeAllViews();
            if (providerView != null) {
                binding.pkLayoutBottom.addView(providerView);
            }
        }
        if (pkBattleConfig != null && pkBattleConfig.pkBattleViewForegroundProvider != null) {
            View providerView = pkBattleConfig.pkBattleViewForegroundProvider.getView(binding.pkLayoutCenter,
                uiKitUsers);
            binding.pkLayoutCenter.removeAllViews();
            if (providerView != null) {
                binding.pkLayoutCenter.addView(providerView);
            }
        }

        binding.audienceMixOtherTips.addView(getDefaultReconnectingView());
        binding.audienceMixSelfTips.addView(getDefaultReconnectingView());
        binding.pkOtherVideoTips.addView(getDefaultReconnectingView());

        if (pkBattleConfig != null && pkBattleConfig.hostReconnectingProvider != null) {
            View audienceMixOtherTips = pkBattleConfig.hostReconnectingProvider.getView(binding.audienceMixOtherTips,
                pkInfo.pkUser);
            binding.audienceMixOtherTips.removeAllViews();
            if (audienceMixOtherTips != null) {
                binding.audienceMixOtherTips.addView(audienceMixOtherTips);
            }
            View audienceMixSelfTips = pkBattleConfig.hostReconnectingProvider.getView(binding.audienceMixSelfTips,
                hostUser);
            binding.audienceMixSelfTips.removeAllViews();
            if (audienceMixSelfTips != null) {
                binding.audienceMixSelfTips.addView(audienceMixSelfTips);
            }
            View pkOtherVideoTips = pkBattleConfig.hostReconnectingProvider.getView(binding.pkOtherVideoTips,
                pkInfo.pkUser);
            binding.pkOtherVideoTips.removeAllViews();
            if (pkOtherVideoTips != null) {
                binding.pkOtherVideoTips.addView(pkOtherVideoTips);
            }
        }
    }

    private View getDefaultReconnectingView() {
        TextView textView = new TextView(getContext());
        textView.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        textView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.gray_444));
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        ZegoTranslationText translationText = ZegoLiveStreamingManager.getInstance().getTranslationText();
        if (translationText != null) {
            textView.setText(translationText.hostReconnecting);
        }
        return textView;
    }


    private void onRoomPKEnded() {
        String hostID = ZegoLiveStreamingManager.getInstance().getHostID();
        ZegoUIKitUser hostUser = ZegoUIKit.getUser(hostID);

        if (ZegoLiveStreamingManager.getInstance().isCurrentUserHost()) {
            binding.pkOtherVideo.stopPlayRemoteAudioVideo();
            binding.pkOtherVideo.setUserID("");
            if (prebuiltAudioVideoViewConfig != null && prebuiltAudioVideoViewConfig.provider != null) {
                binding.pkOtherVideoForeground.removeAllViews();
            }
            binding.pkOtherVideoIcon.updateUser("", "");
            binding.pkOtherVideo.setStreamID("");
        } else {
            binding.audienceMixVideo.stopPlayRemoteAudioVideo();
            binding.audienceMixVideo.setStreamID("");
        }
    }

    public void onJoinRoomSucceed() {
        int width = binding.getRoot().getWidth() / 4;
        binding.pkOtherVideoIcon.setRadius(width / 2);
        binding.pkSelfVideoIcon.setRadius(width / 2);
        binding.audienceMixSelfIcon.setRadius(width / 2);
        binding.audienceMixOtherIcon.setRadius(width / 2);

        binding.pkOtherVideoIcon.onSizeChanged(width);
        binding.pkSelfVideoIcon.onSizeChanged(width);
        binding.audienceMixSelfIcon.onSizeChanged(width);
        binding.audienceMixOtherIcon.onSizeChanged(width);
    }

    public void setPKBattleConfig(ZegoLiveStreamingPKBattleConfig pkBattleConfig) {
        this.pkBattleConfig = pkBattleConfig;
    }

    public void setPrebuiltAudioVideoConfig(ZegoPrebuiltAudioVideoViewConfig prebuiltAudioVideoViewConfig) {
        this.prebuiltAudioVideoViewConfig = prebuiltAudioVideoViewConfig;
    }

    public void setAvatarViewProvider(ZegoAvatarViewProvider zegoAvatarViewProvider) {
        this.avatarViewProvider = zegoAvatarViewProvider;
    }
}
