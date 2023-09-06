package com.zegocloud.uikit.prebuilt.livestreaming.internal.components;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.uikit.components.common.ZEGOImageButton;
import com.zegocloud.uikit.prebuilt.livestreaming.R;

public class BeautyButton extends ZEGOImageButton {

    public BeautyButton(@NonNull Context context) {
        super(context);
    }

    public BeautyButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BeautyButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initView() {
        super.initView();
        setImageResource(R.drawable.livestreaming_beauty_effect,R.drawable.livestreaming_beauty_effect);
    }
}
