package com.borqs.se.home3d;

import java.util.ArrayList;

import com.borqs.se.R;
import com.borqs.se.engine.SESceneManager;
import com.borqs.se.home3d.HomeUtils.CropImageInfo;
import com.borqs.se.widget3d.House;

import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class LongPressWallDialog extends Dialog {

    public LongPressWallDialog(Context context) {
        super(context, R.style.HomeDialogStyle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addobject_dialog_main_layout);

        String[] list = getContext().getResources().getStringArray(R.array.long_click_wall);
        findViewById(R.id.settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                HomeUtils.gotoSettingsActivity();
            }
        });
        ((TextView) (findViewById(R.id.settingsv))).setText(list[0]);

        findViewById(R.id.add3dwidget).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                SESceneManager.getInstance().handleMessage(HomeScene.MSG_TYPE_SHOW_OBJECT_VIEW, null);
            }
        });
        ((TextView) (findViewById(R.id.add3dwidgetv))).setText(list[1]);

        findViewById(R.id.addapp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                SESceneManager.getInstance().handleMessage(HomeScene.MSG_TYPE_SHOW_ADDAPP_DIALOG,
                        ApplicationMenu.TYPE_SHOW_APP);
            }
        });
        ((TextView) (findViewById(R.id.addappv))).setText(list[2]);

        findViewById(R.id.addfolder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                SESceneManager.getInstance().handleMessage(HomeScene.MSG_TYPE_SHOW_CREATEFOLDER_DIALOG, null);
            }
        });
        ((TextView)(findViewById(R.id.addfolderv))).setText(list[3]);

        findViewById(R.id.addshortcut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                pickShortcut(HomeScene.REQUEST_CODE_BIND_SHORTCUT, R.string.title_select_shortcut);
            }
        });
        ((TextView) (findViewById(R.id.addshortcutv))).setText(list[4]);

        findViewById(R.id.addwidget).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                int widgetId = HomeManager.getInstance().getAppWidgetHost().allocateAppWidgetId();
                Intent selectIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
                selectIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
                ArrayList<AppWidgetProviderInfo> customInfo = new ArrayList<AppWidgetProviderInfo>();
                selectIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, customInfo);
                ArrayList<Bundle> customExtras = new ArrayList<Bundle>();
                selectIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, customExtras);
                HomeManager.getInstance().startActivityForResult(selectIntent, HomeScene.REQUEST_CODE_BIND_WIDGET);
            }
        });
        ((TextView) (findViewById(R.id.addwidgetv))).setText(list[5]);

        findViewById(R.id.addwallpaper).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                SESceneManager.getInstance().handleMessage(HomeScene.MSG_TYPE_SHOW_SELECT_WALLPAPER_DIALOG, null);
            }
        });
        ((TextView) (findViewById(R.id.addwallpaperv))).setText(list[6]);

        Window window = getWindow();
        window.setWindowAnimations(R.style.dialog_win_anim_style);

        // View antView = findViewById(R.id.addwidget);
        // antView.setRotationX(30);
        // antView.setTranslationX(150);
        // antView.setTranslationY(-150);
    }

    private void pickShortcut(int requestCode, int title) {
        Bundle bundle = new Bundle();
        Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
        pickIntent.putExtra(Intent.EXTRA_INTENT, new Intent(Intent.ACTION_CREATE_SHORTCUT));
        pickIntent.putExtra(Intent.EXTRA_TITLE, getContext().getResources().getString(title));
        pickIntent.putExtras(bundle);
        HomeManager.getInstance().startActivityForResult(pickIntent, requestCode);
    }

}
