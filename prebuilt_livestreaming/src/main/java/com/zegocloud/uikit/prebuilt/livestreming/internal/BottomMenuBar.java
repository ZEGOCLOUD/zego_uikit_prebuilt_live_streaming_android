package com.zegocloud.uikit.prebuilt.livestreming.internal;

import android.content.Context;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import com.zegocloud.uikit.components.audiovideo.ZegoLeaveButton;
import com.zegocloud.uikit.components.audiovideo.ZegoSwitchAudioOutputButton;
import com.zegocloud.uikit.components.audiovideo.ZegoSwitchCameraButton;
import com.zegocloud.uikit.components.audiovideo.ZegoToggleCameraButton;
import com.zegocloud.uikit.components.audiovideo.ZegoToggleMicrophoneButton;
import com.zegocloud.uikit.prebuilt.livestreming.R;
import com.zegocloud.uikit.prebuilt.livestreming.ZegoInRoomMessageButton;
import com.zegocloud.uikit.prebuilt.livestreming.ZegoMenuBarButtonName;
import com.zegocloud.uikit.utils.Utils;
import java.util.ArrayList;
import java.util.List;

public class BottomMenuBar extends LinearLayout {

    private float downY;
    private float currentY;
    private int limitedCount = 5;
    private List<ZegoMenuBarButtonName> zegoMenuBarButtons = new ArrayList<>();
    private List<View> showList = new ArrayList<>();
    private List<View> hideList = new ArrayList<>();
    private MoreDialog moreDialog;
    private LinearLayout childLinearLayout;
    private ZegoInRoomMessageButton messageButton;

    public BottomMenuBar(@NonNull Context context) {
        super(context);
        initView();
    }

    public BottomMenuBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public BottomMenuBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        setOrientation(LinearLayout.HORIZONTAL);
        setLayoutParams(new LayoutParams(-1, -2));
        setGravity(Gravity.CENTER_HORIZONTAL);

        messageButton = new ZegoInRoomMessageButton(getContext());
        messageButton.setImageResource(R.drawable.icon_im);
        messageButton.setScaleType(ScaleType.FIT_XY);
        LinearLayout.LayoutParams btnParam = new LayoutParams(-2, -2);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int marginStart = Utils.dp2px(16, displayMetrics);
        int marginTop = Utils.dp2px(10, displayMetrics);
        btnParam.setMargins(marginStart, marginTop, 0, marginStart);
        addView(messageButton, btnParam);

        childLinearLayout = new LinearLayout(getContext());
        childLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        childLinearLayout.setGravity(Gravity.END);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -2, 1);
        addView(childLinearLayout, params);
        int paddingEnd = Utils.dp2px(8, getResources().getDisplayMetrics());
        childLinearLayout.setPadding(0, 0, paddingEnd, 0);
    }

    public void setButtons(List<ZegoMenuBarButtonName> zegoMenuBarButtons) {
        this.zegoMenuBarButtons.clear();
        this.zegoMenuBarButtons.addAll(zegoMenuBarButtons);

        showList.clear();
        hideList.clear();
        List<View> menuBarViews = getMenuBarViews(zegoMenuBarButtons);
        if (zegoMenuBarButtons.size() <= limitedCount) {
            showList.addAll(menuBarViews);
        } else {
            int showChildCount = limitedCount - 1;
            if (showChildCount > 0) {
                showList.addAll(menuBarViews.subList(0, showChildCount));
                hideList = menuBarViews.subList(showChildCount, menuBarViews.size());
            }
            showList.add(new MoreButton(getContext()));
        }
        notifyListChanged();
    }

    private List<View> getMenuBarViews(List<ZegoMenuBarButtonName> list) {
        List<View> viewList = new ArrayList<>();
        for (ZegoMenuBarButtonName zegoMenuBarButton : list) {
            View viewFromType = getViewFromType(zegoMenuBarButton);
            viewList.add(viewFromType);
        }
        return viewList;
    }

    private void addChildView(View view, ViewGroup.LayoutParams params) {
        childLinearLayout.addView(view, params);
    }

    private void removeChildView() {
        childLinearLayout.removeAllViews();
    }

    private void resetVisibleChildren(List<View> viewList) {
        removeChildView();
        for (int i = 0; i < viewList.size(); i++) {
            LayoutParams params = generateChildLayoutParams();
            View view = viewList.get(i);
            addChildView(view, params);
        }
    }

    private LayoutParams generateChildLayoutParams() {
        int size = Utils.dp2px(36f, getResources().getDisplayMetrics());
        int marginTop = Utils.dp2px(10f, getResources().getDisplayMetrics());
        int marginBottom = Utils.dp2px(16f, getResources().getDisplayMetrics());
        int marginEnd = Utils.dp2px(8, getResources().getDisplayMetrics());
        LayoutParams layoutParams = new LayoutParams(size, size);
        layoutParams.topMargin = marginTop;
        layoutParams.bottomMargin = marginBottom;
        layoutParams.rightMargin = marginEnd;
        return layoutParams;
    }

    private View getViewFromType(ZegoMenuBarButtonName menuBar) {
        View view = null;
        switch (menuBar) {
            case TOGGLE_CAMERA_BUTTON:
                view = new ZegoToggleCameraButton(getContext());
                break;
            case TOGGLE_MICROPHONE_BUTTON:
                view = new ZegoToggleMicrophoneButton(getContext());
                break;
            case SWITCH_CAMERA_FACING_BUTTON:
                view = new ZegoSwitchCameraButton(getContext());
                break;
            case LEAVE_BUTTON:
                view = new ZegoLeaveButton(getContext());
                break;
            case SWITCH_AUDIO_OUTPUT_BUTTON:
                view = new ZegoSwitchAudioOutputButton(getContext());
                break;
        }
        if (view != null) {
            view.setTag(menuBar);
        }
        return view;
    }

    public void addButtons(List<View> viewList) {
        if (viewList.size() == 0) {
            return;
        }
        for (View view : viewList) {
            if (showList.size() < limitedCount) {
                showList.add(view);
            } else {
                View lastView = showList.get(showList.size() - 1);
                if (!(lastView instanceof MoreButton)) {
                    showList.remove(lastView);
                    showList.add(new MoreButton(getContext()));
                    hideList.add(lastView);
                }
                hideList.add(view);
            }
        }
        notifyListChanged();
    }

    private void showMoreDialog() {
        if (moreDialog == null) {
            moreDialog = new MoreDialog(getContext());
        }
        if (!moreDialog.isShowing()) {
            moreDialog.show();
        }
        moreDialog.setHideChildren(hideList);
    }

    private void notifyListChanged() {
        resetVisibleChildren(showList);
        if (moreDialog != null) {
            moreDialog.setHideChildren(hideList);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            currentY = event.getY();
            downY = currentY;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float currentY = event.getY();
            int scaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
            if (currentY - downY < -scaledTouchSlop) {

            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            currentY = 0;
            downY = 0;
        }
        return super.onTouchEvent(event);
    }

    public void setLimitedCount(int limitedCount) {
        this.limitedCount = limitedCount;
    }

    public void showInRoomMessageButton(boolean show) {
        messageButton.setVisibility(show ? VISIBLE : GONE);
    }

    public class MoreButton extends AppCompatImageView {

        public MoreButton(@NonNull Context context) {
            super(context);
            initView();
        }

        public MoreButton(@NonNull Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            initView();
        }

        private void initView() {
            StateListDrawable sld = new StateListDrawable();
            sld.addState(new int[]{android.R.attr.state_pressed},
                ContextCompat.getDrawable(getContext(), R.drawable.icon_tab_more));
            sld.addState(new int[]{}, ContextCompat.getDrawable(getContext(), R.drawable.icon_tab_more));
            setImageDrawable(sld);
            setOnClickListener(v -> showMoreDialog());
        }
    }
}
