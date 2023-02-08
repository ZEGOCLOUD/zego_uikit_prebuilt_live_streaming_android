package com.zegocloud.uikit.prebuilt.livestreaming.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.prebuilt.livestreaming.R;
import com.zegocloud.uikit.service.defines.ZegoRoomPropertyUpdateListener;
import java.util.List;
import java.util.Map;

public class ZegoEnableChatButton extends androidx.appcompat.widget.AppCompatImageView {

    protected GestureDetectorCompat gestureDetectorCompat;
    protected boolean enableChat = true;
    private ZegoRoomPropertyUpdateListener roomPropertyUpdateListener;

    public ZegoEnableChatButton(@NonNull Context context) {
        super(context);
        initView();
    }

    public ZegoEnableChatButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ZegoEnableChatButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        gestureDetectorCompat = new GestureDetectorCompat(getContext(), new SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (enableChat) {
                    ZegoUIKit.setRoomProperty("enableChat", "0");
                } else {
                    ZegoUIKit.setRoomProperty("enableChat", "1");
                }
                return true;
            }
        });
        roomPropertyUpdateListener = new ZegoRoomPropertyUpdateListener() {
            @Override
            public void onRoomPropertyUpdated(String key, String oldValue, String newValue) {
                if ("enableChat".equals(key)) {
                    if (newValue.equals("0")) {
                        enableChat = false;
                    } else if (newValue.equals("1")) {
                        enableChat = true;
                    }
                }
                updateImage();
            }

            @Override
            public void onRoomPropertiesFullUpdated(List<String> updateKeys, Map<String, String> oldProperties,
                Map<String, String> properties) {

            }
        };
        updateImage();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ZegoUIKit.addRoomPropertyUpdateListener(roomPropertyUpdateListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ZegoUIKit.removeRoomPropertyUpdateListener(roomPropertyUpdateListener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetectorCompat.onTouchEvent(event);
    }

    private void updateImage() {
        if (enableChat) {
            setImageResource(R.drawable.livestreaming_icon_message_enable);
        } else {
            setImageResource(R.drawable.livestreaming_icon_message_disable);
        }
    }
}
