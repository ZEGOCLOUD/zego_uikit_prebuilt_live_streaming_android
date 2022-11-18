package com.zegocloud.uikit.prebuilt.livestreaming.internal;


import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.components.common.ZegoMemberListComparator;
import com.zegocloud.uikit.components.common.ZegoMemberListItemViewProvider;
import com.zegocloud.uikit.components.internal.RippleIconView;
import com.zegocloud.uikit.plugin.common.ZegoUIKitPluginType;
import com.zegocloud.uikit.prebuilt.livestreaming.R;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoTranslationText;
import com.zegocloud.uikit.prebuilt.livestreaming.databinding.LayoutMemberlistBinding;
import com.zegocloud.uikit.prebuilt.livestreaming.widget.ZegoAcceptCoHostButton;
import com.zegocloud.uikit.prebuilt.livestreaming.widget.ZegoRefuseCoHostButton;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.service.defines.ZegoUserUpdateListener;
import com.zegocloud.uikit.utils.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LiveMemberList extends BottomSheetDialog {

    private LayoutMemberlistBinding binding;
    private ZegoMemberListItemViewProvider memberListItemProvider;
    private ZegoUserUpdateListener userUpdateListener;

    public LiveMemberList(@NonNull Context context) {
        super(context, R.style.TransparentDialog);
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
    }

    public LiveMemberList(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = LayoutMemberlistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Window window = getWindow();
        LayoutParams lp = window.getAttributes();
        lp.width = LayoutParams.MATCH_PARENT;
        lp.height = LayoutParams.WRAP_CONTENT;
        lp.dimAmount = 0.1f;
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);
        setCanceledOnTouchOutside(true);
        window.setBackgroundDrawable(new ColorDrawable());

        ZegoTranslationText translationText = LiveStreamingManager.getInstance().getTranslationText();
        if (translationText != null && translationText.memberListTitle != null) {
            binding.liveMemberListTitle.setText(translationText.memberListTitle);
        }

        binding.liveMemberList.setMemberListComparator(new ZegoMemberListComparator() {
            @Override
            public List<ZegoUIKitUser> sortUserList(List<ZegoUIKitUser> userList) {
                List<ZegoUIKitUser> result = new ArrayList<>();
                List<ZegoUIKitUser> coHost = new ArrayList<>();
                List<ZegoUIKitUser> requested = new ArrayList<>();
                List<ZegoUIKitUser> audience = new ArrayList<>();
                ZegoUIKitUser host = null;
                ZegoUIKitUser you = null;

                String hostUserID = ZegoUIKit.getRoomProperties().get("host");
                for (ZegoUIKitUser uiKitUser : userList) {
                    boolean isHost = Objects.equals(uiKitUser.userID, hostUserID);
                    boolean isYou = Objects.equals(uiKitUser, ZegoUIKit.getLocalUser());
                    if (isHost) {
                        host = uiKitUser;
                    } else {
                        if (isYou) {
                            you = uiKitUser;
                        } else {
                            boolean isCoHost = uiKitUser.isCameraOpen || uiKitUser.isMicOpen;
                            if (isCoHost) {
                                coHost.add(uiKitUser);
                            } else {
                                boolean isRequested = LiveStreamingManager.getInstance()
                                    .isUserCoHostRequestExisted(uiKitUser.userID);
                                if (isRequested) {
                                    requested.add(uiKitUser);
                                } else {
                                    audience.add(uiKitUser);
                                }
                            }
                        }
                    }
                }
                if (host != null) {
                    result.add(host);
                }
                if (you != null && you != host) {
                    result.add(you);
                }
                result.addAll(coHost);
                result.addAll(requested);
                result.addAll(audience);
                return result;
            }
        });

        if (memberListItemProvider != null) {
            binding.liveMemberList.setItemViewProvider(memberListItemProvider);
        } else {
            binding.liveMemberList.setItemViewProvider(new ZegoMemberListItemViewProvider() {
                @Override
                public View onCreateView(ViewGroup parent) {
                    View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_liveroom_member, parent, false);
                    int height = Utils.dp2px(70, parent.getContext().getResources().getDisplayMetrics());
                    view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
                    return view;
                }

                @Override
                public void onBindView(View view, ZegoUIKitUser uiKitUser, int position) {
                    RippleIconView rippleIconView = view.findViewById(R.id.live_member_item_icon);
                    TextView memberName = view.findViewById(R.id.live_member_item_name);
                    TextView more = view.findViewById(R.id.live_member_item_more);
                    TextView tag = view.findViewById(R.id.live_member_item_tag);
                    rippleIconView.setText(uiKitUser.userName, false);
                    memberName.setText(uiKitUser.userName);
                    ZegoUIKitUser localUser = ZegoUIKit.getLocalUser();
                    String hostUserID = ZegoUIKit.getRoomProperties().get("host");
                    boolean isYou = Objects.equals(uiKitUser, localUser);
                    boolean isHost = Objects.equals(uiKitUser.userID, hostUserID);
                    boolean isCoHost = uiKitUser.isCameraOpen || uiKitUser.isMicOpen;
                    StringBuilder builder = new StringBuilder();
                    if (isYou || isHost || isCoHost) {
                        builder.append("(");
                    }
                    if (isYou) {
                        builder.append(getContext().getString(R.string.you));
                    }
                    if (isHost) {
                        if (isYou) {
                            builder.append(",");
                        }
                        builder.append(getContext().getString(R.string.host));
                    } else {
                        if (isCoHost) {
                            if (isYou) {
                                builder.append(",");
                            }
                            builder.append(getContext().getString(R.string.co_host));
                        }
                    }

                    if (isYou || isHost || isCoHost) {
                        builder.append(")");
                    }
                    tag.setText(builder.toString());

                    more.setOnClickListener(v -> {
                        dismiss();
                        if (isCoHost) {
                            showRemoveCoHostDialog(uiKitUser);
                        } else {
                            showInviteCoHostDialog(uiKitUser);
                        }
                    });
                    ZegoAcceptCoHostButton agree = view.findViewById(R.id.live_member_item_agree);
                    agree.setInviterID(uiKitUser.userID);
                    agree.setTextSize(14);
                    ZegoRefuseCoHostButton disagree = view.findViewById(R.id.live_member_item_disagree);
                    disagree.setInviterID(uiKitUser.userID);
                    agree.setTextSize(14);
                    agree.setRequestCallbackListener(v -> {
                        dismiss();
                        LiveStreamingManager.getInstance().removeReceiveCoHostRequestUser(uiKitUser.userID);
                    });
                    disagree.setRequestCallbackListener(v -> {
                        dismiss();
                        LiveStreamingManager.getInstance().removeReceiveCoHostRequestUser(uiKitUser.userID);
                    });
                    if (LiveStreamingManager.getInstance().isUserCoHostRequestExisted(uiKitUser.userID)) {
                        agree.setVisibility(View.VISIBLE);
                        disagree.setVisibility(View.VISIBLE);
                        more.setVisibility(View.GONE);
                    } else {
                        agree.setVisibility(View.GONE);
                        disagree.setVisibility(View.GONE);
                        boolean hasSignalPlugIn = ZegoUIKit.getPlugin(ZegoUIKitPluginType.SIGNALING) != null;
                        boolean isSelfHost = Objects.equals(localUser.userID, hostUserID);
                        if (isSelfHost && hasSignalPlugIn && !isYou) {
                            more.setVisibility(View.VISIBLE);
                        } else {
                            more.setVisibility(View.GONE);
                        }
                    }
                }
            });
        }

        binding.liveMemberListListCount.setText(String.valueOf(ZegoUIKit.getAllUsers().size()));
        userUpdateListener = new ZegoUserUpdateListener() {
            @Override
            public void onUserJoined(List<ZegoUIKitUser> userInfoList) {
                binding.liveMemberListListCount.setText(String.valueOf(ZegoUIKit.getAllUsers().size()));
            }

            @Override
            public void onUserLeft(List<ZegoUIKitUser> userInfoList) {
                binding.liveMemberListListCount.setText(String.valueOf(ZegoUIKit.getAllUsers().size()));
            }
        };
        ZegoUIKit.addUserUpdateListener(userUpdateListener);

        setOnDismissListener(dialog -> {
            ZegoUIKit.removeUserUpdateListener(userUpdateListener);
        });

        // both need setPeekHeight & setLayoutParams
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int height = (int) (displayMetrics.heightPixels * 0.6f);
        getBehavior().setPeekHeight(height);
        CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(-1, height);
        binding.liveMemberListLayout.setLayoutParams(params);
    }

    private void showRemoveCoHostDialog(ZegoUIKitUser uiKitUser) {
        RemoveCoHostDialog removeCoHostDialog = new RemoveCoHostDialog(getContext(), uiKitUser);
        removeCoHostDialog.show();
    }

    private void showInviteCoHostDialog(ZegoUIKitUser uiKitUser) {
        InviteCoHostDialog dialog = new InviteCoHostDialog(getContext(), uiKitUser);
        dialog.show();
    }

    public void updateList() {
        if (binding != null) {
            binding.liveMemberList.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
            dismiss();
            return true;
        }
        return false;
    }

    public void setMemberListItemViewProvider(ZegoMemberListItemViewProvider memberListItemProvider) {
        this.memberListItemProvider = memberListItemProvider;
    }
}
