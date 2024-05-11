package com.zegocloud.uikit.prebuilt.livestreaming.internal.components;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.StringRes;
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoLiveStreamingManager;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoTranslationText;
import com.zegocloud.uikit.prebuilt.livestreaming.databinding.LivestreamingDialogConfirmBinding;

public class ConfirmDialog extends Dialog {

    private String titleText;
    private String contentText;
    private String okText;
    private String cancelText;
    private DialogInterface.OnClickListener positiveListener;
    private DialogInterface.OnClickListener negativeListener;
    private LivestreamingDialogConfirmBinding binding;
    private View negativeView;
    private View positiveView;

    public ConfirmDialog(ConfirmDialog.Builder builder) {
        super(builder.context);
        this.titleText = builder.title;
        this.contentText = builder.content;
        this.okText = builder.okText;
        this.cancelText = builder.cancelText;
        this.positiveListener = builder.positiveListener;
        this.negativeListener = builder.negativeListener;
        this.positiveView = builder.positiveView;
        this.negativeView = builder.negativeView;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = LivestreamingDialogConfirmBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Window window = getWindow();
        window.setGravity(Gravity.CENTER);
        window.setDimAmount(0.2f);
        window.setBackgroundDrawable(new ColorDrawable());

        setCanceledOnTouchOutside(false);

        if (!TextUtils.isEmpty(titleText)) {
            binding.confirmTitle.setText(titleText);
        }
        if (!TextUtils.isEmpty(contentText)) {
            binding.confirmContent.setText(contentText);
        }

        ZegoTranslationText translationText = ZegoLiveStreamingManager.getInstance().getTranslationText();
        if (!TextUtils.isEmpty(okText)) {
            binding.confirmOk.setText(okText);
        } else {
            if (translationText != null) {
                binding.confirmOk.setText(translationText.ok);
            }
        }
        if (!TextUtils.isEmpty(cancelText)) {
            binding.confirmCancel.setText(cancelText);
        } else {
            if (translationText != null) {
                binding.confirmOk.setText(translationText.cancel);
            }
        }
        if (negativeView == null) {
            binding.confirmCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (negativeListener != null) {
                        negativeListener.onClick(ConfirmDialog.this, DialogInterface.BUTTON_NEGATIVE);
                    }
                }
            });
        } else {
            binding.confirmCustomCancel.addView(negativeView);
            binding.confirmCancel.setVisibility(View.GONE);
        }

        if (positiveView == null) {
            binding.confirmOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (positiveListener != null) {
                        positiveListener.onClick(ConfirmDialog.this, DialogInterface.BUTTON_POSITIVE);
                    }
                }
            });
        } else {
            binding.confirmCustomOk.addView(positiveView);
            binding.confirmOk.setVisibility(View.GONE);
        }
    }

    public static class Builder {

        private String title;
        private String content;
        private String okText;
        private String cancelText;
        private Context context;
        private DialogInterface.OnClickListener positiveListener;
        private DialogInterface.OnClickListener negativeListener;
        private View positiveView;
        private View negativeView;
        private float dimAmount = -1;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setTitle(String titleText) {
            this.title = titleText;
            return this;
        }

        public Builder setTitle(@StringRes int title) {
            this.title = context.getString(title);
            return this;
        }

        public Builder setMessage(String content) {
            this.content = content;
            return this;
        }

        public Builder setMessage(@StringRes int content) {
            this.content = context.getString(content);
            return this;
        }

        public Builder setMessage(@StringRes int content, Object... formatArgs) {
            this.content = context.getString(content, formatArgs);
            return this;
        }

        public Builder setPositiveButton(String okText, DialogInterface.OnClickListener listener) {
            this.okText = okText;
            this.positiveListener = listener;
            return this;
        }

        public Builder setPositiveButton(@StringRes int okText, DialogInterface.OnClickListener listener) {
            this.okText = context.getString(okText);
            this.positiveListener = listener;
            return this;
        }

        public Builder setNegativeButton(String cancelText, DialogInterface.OnClickListener listener) {
            this.cancelText = cancelText;
            this.negativeListener = listener;
            return this;
        }

        public Builder setNegativeButton(@StringRes int cancelText, DialogInterface.OnClickListener listener) {
            this.cancelText = context.getString(cancelText);
            this.negativeListener = listener;
            return this;
        }

        public Builder setCustomPositiveButton(View view) {
            this.positiveView = view;
            return this;
        }

        public Builder setCustomNegativeButton(View view) {
            this.negativeView = view;
            return this;
        }

        public ConfirmDialog build() {
            ConfirmDialog dialog = new ConfirmDialog(this);
            return dialog;
        }
    }
}