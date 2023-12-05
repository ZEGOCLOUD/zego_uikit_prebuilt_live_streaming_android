package com.zegocloud.uikit.prebuilt.livestreaming.internal.core;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.components.audiovideo.ZegoAvatarViewProvider;
import com.zegocloud.uikit.components.internal.RippleIconView;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.service.express.IExpressEngineEventHandler;
import com.zegocloud.uikit.service.internal.UIKitCore;
import com.zegocloud.uikit.service.internal.UIKitCoreUser;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Objects;

public class ZegoPKAvatarView extends FrameLayout {


    private static final int MIN_SOUND = 5;
    protected String mUserID;
    private boolean showSoundWave;
    private RippleIconView rippleIconView;
    private FrameLayout customView;
    private ZegoAvatarViewProvider avatarViewProvider;
    private String streamID;

    public ZegoPKAvatarView(Context context) {
        super(context);
        initView();
    }

    public ZegoPKAvatarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        rippleIconView = new RippleIconView(getContext());
        addView(rippleIconView);
        rippleIconView.setText("", false);
        customView = new FrameLayout(getContext());
        addView(customView);

        ZegoUIKit.addEventHandler(new IExpressEngineEventHandler() {
            @Override
            public void onCapturedSoundLevelUpdate(float soundLevel) {
                super.onCapturedSoundLevelUpdate(soundLevel);
                if (ZegoUIKit.getLocalUser().userID.equals(mUserID)) {
                    if (soundLevel > MIN_SOUND) {
                        if (showSoundWave) {
                            rippleIconView.startAnimation();
                        }
                    } else {
                        rippleIconView.stopAnimation();
                    }
                }
            }

            @Override
            public void onRemoteSoundLevelUpdate(HashMap<String, Float> soundLevels) {
                super.onRemoteSoundLevelUpdate(soundLevels);
                for (Entry<String, Float> entry : soundLevels.entrySet()) {
                    if (entry.getKey().equals(streamID)) {
                        if (entry.getValue() > MIN_SOUND) {
                            if (showSoundWave) {
                                rippleIconView.startAnimation();
                            }
                        } else {
                            rippleIconView.stopAnimation();
                        }
                        break;
                    }
                }
            }
        });
    }

    public void setShowSoundWave(boolean showSoundWave) {
        this.showSoundWave = showSoundWave;
    }

    public void setRadius(int radius) {
        rippleIconView.setRadius(radius);
    }

    public void setTextSize(int textSize) {
        rippleIconView.setTextSize(textSize);
    }

    public void setRippleWidth(int rippleWidth) {
        rippleIconView.setRippleWidth(rippleWidth);
    }

    public void setRippleColor(int color) {
        rippleIconView.setRippleColor(color);
    }

    public void updateUser(String userID, String userName) {
        boolean userIDChanged = Objects.equals(mUserID, userID);
        this.mUserID = userID;
        if (userName != null) {
            rippleIconView.setText(userName, userIDChanged);
        } else {
            UIKitCoreUser coreUser = UIKitCore.getInstance().getUserbyUserID(mUserID);
            if (coreUser != null) {
                rippleIconView.setText(coreUser.userName, userIDChanged);
            } else {
                rippleIconView.setText("", userIDChanged);
            }
        }

        int size = rippleIconView.getRadius() * 2;
        LayoutParams params = new LayoutParams(size, size);
        params.gravity = Gravity.CENTER;
        customView.setLayoutParams(params);

        if (avatarViewProvider != null) {
            UIKitCoreUser coreUser = UIKitCore.getInstance().getUserbyUserID(mUserID);
            View providerView;
            if (coreUser != null) {
                providerView = avatarViewProvider.onUserIDUpdated(this, coreUser.getUIKitUser());
            } else {
                providerView = avatarViewProvider.onUserIDUpdated(this, new ZegoUIKitUser(userID, userName));
            }
            if (providerView != null) {
                customView.removeAllViews();
                customView.addView(providerView);
            }
        }
    }

    public void setAvatarViewProvider(ZegoAvatarViewProvider avatarViewProvider) {
        this.avatarViewProvider = avatarViewProvider;
    }

    public void onSizeChanged(int radius) {
        LayoutParams params = new LayoutParams(radius * 2, radius * 2);
        params.gravity = Gravity.CENTER;
        customView.setLayoutParams(params);
    }

    public void setStreamID(String streamID) {
        this.streamID = streamID;
    }
}
