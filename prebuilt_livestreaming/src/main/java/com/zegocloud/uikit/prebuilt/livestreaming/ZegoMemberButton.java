package com.zegocloud.uikit.prebuilt.livestreaming;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.Nullable;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.service.defines.RoomUserUpdateListener;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.service.internal.UIKitCore;
import java.util.List;

public class ZegoMemberButton extends androidx.appcompat.widget.AppCompatTextView {

    private RoomUserUpdateListener roomUserUpdateListener;

    public ZegoMemberButton(Context context) {
        super(context);
        initView();
    }

    public ZegoMemberButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ZegoMemberButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        setText(String.valueOf(ZegoUIKit.getAllUsers().size()));
        roomUserUpdateListener = new RoomUserUpdateListener() {
            @Override
            public void onUserJoined(List<ZegoUIKitUser> userInfoList) {
                int size = ZegoUIKit.getAllUsers().size();
                setText(String.valueOf(size));
            }

            @Override
            public void onUserJoinLeft(List<ZegoUIKitUser> userInfoList) {
                int size = ZegoUIKit.getAllUsers().size();
                setText(String.valueOf(size));
            }
        };
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        UIKitCore.getInstance().addUserUpdateListener(roomUserUpdateListener, true);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        UIKitCore.getInstance().removeUserUpdateListener(roomUserUpdateListener, true);
    }
}
