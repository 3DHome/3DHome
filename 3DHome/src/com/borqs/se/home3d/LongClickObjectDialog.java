package com.borqs.se.home3d;

import com.borqs.se.R;
import com.borqs.se.engine.SECommand;
import com.borqs.se.engine.SESceneManager;
import com.borqs.se.widget3d.NormalObject;
import com.borqs.se.widget3d.ObjectInfo;
import com.borqs.se.widget3d.VesselObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LongClickObjectDialog extends Dialog {
    private HomeScene mHomeScene;

    public LongClickObjectDialog(Context context) {
        super(context, R.style.HomeDialogDimUnableStyle);
        mHomeScene = HomeManager.getInstance().getHomeScene();
    }

    private void setButtonProperties(TextView view, Drawable icon, int textId) {
        view.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
        view.setText(textId);
        view.setWidth(view.getResources().getDimensionPixelSize(R.dimen.object_menu_item_width));
        view.setHeight(view.getResources().getDimensionPixelSize(R.dimen.object_menu_item_height));
        view.setPadding(0, getContext().getResources().getDimensionPixelSize(R.dimen.object_menu_item_padding_top), 0,
                14);
    }

    private void setButtonBackground(TextView view) {
        view.setBackgroundResource(R.drawable.long_click_object_bg_down);
        view.setPadding(0, getContext().getResources().getDimensionPixelSize(R.dimen.object_menu_item_padding_top), 0,
                14);
    }

    private void playLightAnimation(View v, MotionEvent ev) {
        if (ev.getAction() != MotionEvent.ACTION_DOWN)
            return;

        FrameLayout contentView = (FrameLayout) v.getParent().getParent();
        Animation a1 = AnimationUtils.loadAnimation(SESceneManager.getInstance().getGLActivity(), R.anim.lighteffect);
        Animation a2 = AnimationUtils.loadAnimation(SESceneManager.getInstance().getGLActivity(), R.anim.lighteffect);
        contentView.getChildAt(0).startAnimation(a1);
        contentView.getChildAt(1).startAnimation(a2);
    }

    public void prepareContent(final NormalObject selectedObject, Bundle bundle) {
        Activity context = SESceneManager.getInstance().getGLActivity();
        boolean reversal = bundle.getBoolean("reversal");
        int x = bundle.getInt("dlgx");
        int y = bundle.getInt("dlgy");
        LinearLayout layout = (LinearLayout) context.getLayoutInflater().inflate(R.layout.object_menu_pop, null);
        if (!"DeskLand".equals(selectedObject.getObjectInfo().mType)
                && !"DeskPort".equals(selectedObject.getObjectInfo().mType)) {
            TextView deleteView = (TextView) context.getLayoutInflater().inflate(R.layout.object_menu_item, null);
            Drawable iconDelete = context.getResources().getDrawable(R.drawable.long_click_object_delete);
            setButtonProperties(deleteView, iconDelete, R.string.long_click_object_menu_delete);
            deleteView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dismiss();
                    new SECommand(mHomeScene) {
                        @Override
                        public void run() {
                            VesselObject parent = selectedObject.getVesselParent();
                            if (null != parent) {
                                parent.removeChild(selectedObject, true);
                                SESceneManager.getInstance().removeMessage(HomeScene.MSG_TYPE_UPDATE_SHELF);
                                SESceneManager.getInstance().handleMessage(HomeScene.MSG_TYPE_UPDATE_SHELF, null);
                            }
                        }
                    }.execute();
                }
            });
            deleteView.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    playLightAnimation(v, event);
                    return false;
                }

            });
            layout.addView(deleteView);
        }
        if (selectedObject.canChangeBind()) {
            TextView changeBindView = (TextView) context.getLayoutInflater().inflate(R.layout.object_menu_item, null);
            Drawable iconBind = context.getResources().getDrawable(R.drawable.long_click_object_changebind);
            setButtonProperties(changeBindView, iconBind, R.string.long_click_object_menu_change_bind);
            changeBindView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dismiss();
                    SESceneManager.getInstance().removeMessage(HomeScene.MSG_TYPE_SHOW_BIND_APP_DIALOG);
                    SESceneManager.getInstance().handleMessage(HomeScene.MSG_TYPE_SHOW_BIND_APP_DIALOG, selectedObject);
                }
            });

            changeBindView.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    playLightAnimation(v, event);
                    return false;
                }

            });
            layout.addView(changeBindView);

        }

        if (selectedObject.canChangeIcon()) {
            TextView changeIconView = (TextView) context.getLayoutInflater().inflate(R.layout.object_menu_item, null);
            Drawable iconChangeIcon = context.getResources().getDrawable(R.drawable.long_click_object_changeicon);
            setButtonProperties(changeIconView, iconChangeIcon, R.string.long_click_object_menu_change_icon);
            changeIconView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    mHomeScene.showDialog(HomeScene.DIALOG_OBJECT_CHANGE_ICON);
                }
            });
            changeIconView.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    playLightAnimation(v, event);
                    return false;
                }

            });
            layout.addView(changeIconView);
        }
        if (selectedObject.canChangeLabel()) {
            TextView changeLabelView = (TextView) context.getLayoutInflater().inflate(R.layout.object_menu_item, null);
            Drawable iconBind = context.getResources().getDrawable(R.drawable.long_click_object_changelabel);
            setButtonProperties(changeLabelView, iconBind, R.string.long_click_object_menu_change_label);
            changeLabelView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    mHomeScene.showDialog(HomeScene.DIALOG_OBJECT_CHANGE_LABLE);
                }
            });
            changeLabelView.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    playLightAnimation(v, event);
                    return false;
                }

            });
            layout.addView(changeLabelView);
        }

        if (selectedObject.canUninstall()) {
            TextView uninstallView = (TextView) context.getLayoutInflater().inflate(R.layout.object_menu_item, null);
            Drawable iconUninstall = context.getResources().getDrawable(R.drawable.long_click_object_uninstall);
            setButtonProperties(uninstallView, iconUninstall, R.string.long_click_object_menu_uninstall);
            uninstallView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dismiss();
                    if (selectedObject.getObjectInfo().mModelInfo != null
                            && selectedObject.getObjectInfo().mModelInfo.mIsDownloaded) {
                        SESceneManager.getInstance().removeMessage(HomeScene.MSG_TYPE_SHOW_DELETE_OBJECTS);
                        SESceneManager.getInstance().handleMessage(HomeScene.MSG_TYPE_SHOW_DELETE_OBJECTS, selectedObject.mName);
                    } else {
                        ComponentName componentName = selectedObject.getObjectInfo().mComponentName;
                        if (componentName == null) {
                            return;
                        }
                        String packageName = componentName.getPackageName();
                        String className = componentName.getClassName();
                        Intent intent = new Intent(Intent.ACTION_DELETE, Uri.fromParts("package", packageName,
                                className));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        HomeManager.getInstance().startActivity(intent);
                    }
                }
            });
            uninstallView.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    playLightAnimation(v, event);
                    return false;
                }

            });
            layout.addView(uninstallView);

        }
        if (selectedObject.canBeResized()) {
            TextView resizeView = (TextView) context.getLayoutInflater().inflate(R.layout.object_menu_item, null);
            Drawable iconResize = context.getResources().getDrawable(R.drawable.long_click_object_resize);
            setButtonProperties(resizeView, iconResize, R.string.long_click_object_menu_resize);
            resizeView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dismiss();
                    new SECommand(mHomeScene) {
                        @Override
                        public void run() {
                            selectedObject.startResize();
                        }
                    }.execute();
                }
            });
            resizeView.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    playLightAnimation(v, event);
                    return false;
                }

            });
            layout.addView(resizeView);
        }

        if (selectedObject.canChangeSetting()) {
            TextView settingView = (TextView) context.getLayoutInflater().inflate(R.layout.object_menu_item, null);
            Drawable iconSetting = context.getResources().getDrawable(R.drawable.long_click_object_setting);
            setButtonProperties(settingView, iconSetting, R.string.long_click_object_menu_setting);
            settingView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dismiss();
                    selectedObject.onSettingClick();
                }
            });

            settingView.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    playLightAnimation(v, event);
                    return false;
                }

            });
            layout.addView(settingView);
        }

        if (selectedObject.canChangeColor()) {
            TextView settingView = (TextView) context.getLayoutInflater().inflate(R.layout.object_menu_item, null);
            Drawable iconSetting = context.getResources().getDrawable(R.drawable.long_click_object_changelabel);
            setButtonProperties(settingView, iconSetting, R.string.long_click_object_menu_change_color);
            settingView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    SESceneManager.getInstance().handleMessage(HomeScene.MSG_TYPE_SHOW_CHANGE_COLOR_DIALOG, null);
                    dismiss();

                }
            });

            settingView.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    playLightAnimation(v, event);
                    return false;
                }

            });
            layout.addView(settingView);
        }
        layout.measure(0, 0);

        // FrameLayout, v1, v2 are designed for the light effect.
        int layoutWidth = layout.getMeasuredWidth();
        int layoutHeight = layout.getMeasuredHeight();
        FrameLayout contentView = (FrameLayout) context.getLayoutInflater().inflate(R.layout.object_menu_effect, null);
        final View v1 = new View(context);
        v1.setBackgroundResource(R.drawable.long_click_object_light);
        v1.setTranslationX(-layoutWidth / 2);
        contentView.addView(v1, new FrameLayout.LayoutParams(layoutWidth, layoutHeight));

        final View v2 = new View(context);
        v2.setBackgroundResource(R.drawable.long_click_object_light);
        v2.setTranslationX(layoutWidth);
        contentView.addView(v2, new FrameLayout.LayoutParams(layoutWidth, layoutHeight));

        contentView.addView(layout, new FrameLayout.LayoutParams(layoutWidth, layoutHeight));

        setContentView(contentView);
        Window window = getWindow();
        window.setGravity(Gravity.LEFT | Gravity.TOP);
        LayoutParams params = window.getAttributes();

        int startX = selectedObject.getObjectSlot().mStartX;
        int btnWidth = layout.getChildAt(0).getMeasuredWidth();
        int childCount = layout.getChildCount();
        int cellCountX = mHomeScene.getHouse().mCellCountX;
        if (selectedObject.getObjectInfo().mSlotType == ObjectInfo.SLOT_TYPE_WALL
                || selectedObject.getObjectInfo().mSlotType == ObjectInfo.SLOT_TYPE_APP_WALL
                || selectedObject.getObjectInfo().mSlotType == ObjectInfo.SLOT_TYPE_BOOKSHELF) {
            if (startX == 0) {
                x -= btnWidth * 0.5;
                setButtonBackground((TextView) layout.getChildAt(0));
            } else if (startX < cellCountX / 2) {
                if (childCount == 1) {
                    x -= btnWidth * 0.5;
                    setButtonBackground((TextView) layout.getChildAt(0));
                } else {
                    x -= btnWidth * 1.5;
                    setButtonBackground((TextView) layout.getChildAt(1));
                }
            } else if (startX >= cellCountX / 2 && startX != (cellCountX - 1)) {
                if (childCount == 1) {
                    x -= btnWidth * 0.5;
                    setButtonBackground((TextView) layout.getChildAt(0));
                } else {
                    x -= (childCount - 1.5) * btnWidth;
                    setButtonBackground((TextView) layout.getChildAt(childCount - 2));
                }
            } else if (startX == cellCountX - 1) {
                x -= (childCount - 0.5) * btnWidth;
                setButtonBackground((TextView) layout.getChildAt(childCount - 1));
            }
        } else if (selectedObject.getObjectInfo().mSlotType == ObjectInfo.SLOT_TYPE_DESKTOP) {
            if (x > mHomeScene.getHomeSceneInfo().getSceneWidth() / 2 + 30) {
                x -= (childCount - 0.5) * btnWidth;
                setButtonBackground((TextView) layout.getChildAt(childCount - 1));
            } else if ((x > mHomeScene.getHomeSceneInfo().getSceneWidth() / 2 - 30)
                    && (x < mHomeScene.getHomeSceneInfo().getSceneWidth() / 2 + 30)) {
                // Icon's location is almost center.
                if (childCount == 1) {
                    x -= btnWidth * 0.5;
                    setButtonBackground((TextView) layout.getChildAt(0));
                } else {
                    x -= btnWidth * 1.5;
                    setButtonBackground((TextView) layout.getChildAt(1));
                }
            } else {
                x -= btnWidth * 0.5;
                setButtonBackground((TextView) layout.getChildAt(0));
            }
        } else if (selectedObject.getObjectInfo().mSlotType == ObjectInfo.SLOT_TYPE_GROUND) {
            if (reversal) {
                x -= (childCount - 0.5) * btnWidth;
                setButtonBackground((TextView) layout.getChildAt(childCount - 1));
            } else {
                x -= btnWidth * 0.5;
                setButtonBackground((TextView) layout.getChildAt(0));
            }
        } else {
            setButtonBackground((TextView) layout.getChildAt(0));
        }
        params.x = x;
        params.y = y;
        window.setAttributes(params);
    }
}
