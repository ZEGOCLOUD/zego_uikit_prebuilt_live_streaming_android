package com.zegocloud.uikit.prebuilt.livestreaming;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import com.zegocloud.uikit.components.live.ZegoInRoomMessageInput;
import com.zegocloud.uikit.utils.KeyboardUtils;

public class ZegoInRoomMessageInputBoard extends Dialog {

    private ZegoInRoomMessageInput contentView;

    public ZegoInRoomMessageInputBoard(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        contentView = new ZegoInRoomMessageInput(getContext());
        FrameLayout frameLayout = new FrameLayout(getContext());
        frameLayout.addView(contentView);
        setContentView(frameLayout);

        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = LayoutParams.MATCH_PARENT;
        lp.height = LayoutParams.WRAP_CONTENT;
        lp.dimAmount = 0.1f;
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);
        window.setBackgroundDrawable(new ColorDrawable());

        setCanceledOnTouchOutside(false);

        int mode = WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE |
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;
        window.setSoftInputMode(mode);

        setOnDismissListener(dialog -> {
            KeyboardUtils.hideInputWindow(contentView);
        });
        contentView.setListener(message -> {
            dismiss();
        });
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (event.getX() < 0 || event.getY() < 0) {
            dismiss();
        }
        if (event.getX() > contentView.getWidth() || event.getY() > contentView.getHeight()) {
            dismiss();
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        dismiss();
    }
}
