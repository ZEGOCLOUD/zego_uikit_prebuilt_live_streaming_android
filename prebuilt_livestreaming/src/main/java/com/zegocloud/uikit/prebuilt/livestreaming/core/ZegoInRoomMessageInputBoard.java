package com.zegocloud.uikit.prebuilt.livestreaming.core;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import com.zegocloud.uikit.components.message.ZegoInRoomMessageInput;
import com.zegocloud.uikit.utils.KeyboardUtils;
import com.zegocloud.uikit.utils.Utils;

public class ZegoInRoomMessageInputBoard extends Dialog {

    private ZegoInRoomMessageInput contentView;
    private boolean enableChat = true;

    public ZegoInRoomMessageInputBoard(@NonNull Context context) {
        super(context);
    }

    public ZegoInRoomMessageInputBoard(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        contentView = new ZegoInRoomMessageInput(getContext(), true);
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

        int mode = LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
        window.setSoftInputMode(mode);

        setCanceledOnTouchOutside(true);

        setOnDismissListener(dialog -> {
            KeyboardUtils.hideInputWindow(contentView);
        });
        contentView.setSubmitListener(message -> {
            dismiss();
        });

        int corner = Utils.dp2px(8f, getContext().getResources().getDisplayMetrics());
        float[] outerR = new float[]{corner, corner, corner, corner, corner, corner, corner, corner};
        RoundRectShape roundRectShape = new RoundRectShape(outerR, null, null);
        ShapeDrawable shapeDrawable = new ShapeDrawable(roundRectShape);
        shapeDrawable.getPaint().setColor(Color.parseColor("#222222"));
        contentView.setBackground(shapeDrawable);
    }

    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
            dismiss();
            return true;
        }
        return false;
    }
}
