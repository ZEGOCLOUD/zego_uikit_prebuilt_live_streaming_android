package com.zegocloud.uikit.prebuilt.livestreaming.widget;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.AttributeSet;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.FragmentActivity;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.prebuilt.livestreaming.R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZegoStartLiveButton extends androidx.appcompat.widget.AppCompatButton {

    protected GestureDetectorCompat gestureDetectorCompat;

    public ZegoStartLiveButton(@NonNull Context context) {
        super(context);
        initView();
    }

    public ZegoStartLiveButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ZegoStartLiveButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
                requestPermissionIfNeeded((allGranted, grantedList, deniedList) -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("live_status", "1");
                    ZegoUIKit.updateRoomProperties(map);
                });
                performClick();
                return true;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetectorCompat.onTouchEvent(event);
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

        if (getContext() instanceof FragmentActivity) {
            PermissionX.init((FragmentActivity) getContext()).permissions(permissions).onExplainRequestReason((scope, deniedList) -> {
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
                scope.showRequestReasonDialog(deniedList, message, getContext().getString(R.string.ok));
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
                scope.showForwardToSettingsDialog(deniedList, message, getContext().getString(R.string.settings),
                    getContext().getString(R.string.cancel));
            }).request(new RequestCallback() {
                @Override
                public void onResult(boolean allGranted, @NonNull List<String> grantedList,
                    @NonNull List<String> deniedList) {
                    if (requestCallback != null) {
                        requestCallback.onResult(allGranted, grantedList, deniedList);
                    }
                }
            });
        } else {
            requestCallback.onResult(false, new ArrayList<>(), new ArrayList<>());
        }
    }
}