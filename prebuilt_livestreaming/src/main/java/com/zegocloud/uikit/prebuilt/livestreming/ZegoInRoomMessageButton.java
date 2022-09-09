package com.zegocloud.uikit.prebuilt.livestreming;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

public class ZegoInRoomMessageButton extends AppCompatImageView {

    public ZegoInRoomMessageButton(@NonNull Context context) {
        super(context);
        iniView();
    }

    public ZegoInRoomMessageButton(@NonNull Context context,
        @Nullable AttributeSet attrs) {
        super(context, attrs);
        iniView();
    }

    public ZegoInRoomMessageButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        iniView();
    }

    private void iniView() {
        if (getContext() instanceof Activity) {
            setOnClickListener(v -> {
                ZegoInRoomMessageInputBoard board = new ZegoInRoomMessageInputBoard(getContext());
                board.show();
            });
        }
    }
}
