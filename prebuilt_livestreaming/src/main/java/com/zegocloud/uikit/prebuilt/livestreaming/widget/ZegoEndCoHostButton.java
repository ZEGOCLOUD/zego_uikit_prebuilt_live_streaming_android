package com.zegocloud.uikit.prebuilt.livestreaming.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import androidx.annotation.Nullable;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.prebuilt.livestreaming.R;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.utils.Utils;

public class ZegoEndCoHostButton extends androidx.appcompat.widget.AppCompatButton {

    public ZegoEndCoHostButton(Context context) {
        super(context);
        initView();
    }

    public ZegoEndCoHostButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ZegoEndCoHostButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    protected void initView() {
        setBackgroundResource(R.drawable.bg_end_cohost_btn);
        setText(R.string.end);
        setTextColor(Color.WHITE);
        setTextSize(13);
        setGravity(Gravity.CENTER);
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        setPadding(Utils.dp2px(14, displayMetrics), 0, Utils.dp2px(16, displayMetrics), 0);
        setCompoundDrawablePadding(Utils.dp2px(6, displayMetrics));
        setCompoundDrawablesWithIntrinsicBounds(R.drawable.bottombar_cohost, 0, 0, 0);
    }
}
