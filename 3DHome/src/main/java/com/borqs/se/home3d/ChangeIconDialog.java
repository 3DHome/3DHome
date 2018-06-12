package com.borqs.se.home3d;

import java.io.File;

import com.borqs.se.R;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ChangeIconDialog extends Dialog implements android.view.View.OnClickListener {
    LayoutInflater inflater;
    Context mContext;
    private ImageView mIcon;
    private TextView mLabel;
    private Button mBtnSelectImg;
    private Button mBtnReset;

    private static final int MESSAGE_NOTIFY_REFRESH_ICON = 1;
    private static final int MESSAGE_NOTIFY_REFRESH_LABEL = 2;
    public static final int OPTION_TYPE_CHANGEIMAGE = 1;
    public static final int OPTION_TYPE_RESETICON = 2;

    private Bitmap mBitmapIcon;
    private Drawable mDrawbleIcon;
    private String mDisplayName;
    private boolean mIconHasChanged = false;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case MESSAGE_NOTIFY_REFRESH_ICON:
                if (mDrawbleIcon == null) {
                    mIcon.setImageDrawable(new BitmapDrawable(mBitmapIcon));
                } else {
                    mIcon.setImageDrawable(mDrawbleIcon);
                }
                break;

            case MESSAGE_NOTIFY_REFRESH_LABEL:
                mLabel.setText(mDisplayName + "");

                break;

            default:
                break;
            }
            
        }
    };

    protected ChangeIconDialog(Context context) {
        super(context);
        mContext = context;
        init(mContext);
    }

    public ChangeIconDialog(Context context, int theme) {
        super(context, theme);
        mContext = context;
        init(mContext);
    }

    private void init(Context context) {
        inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.dialog_change_icon, null);
        mIcon = (ImageView) v.findViewById(R.id.change_icon_dialog_appicon);
        mLabel = (TextView) v.findViewById(R.id.change_icon_dialog_appname);
        mBtnSelectImg = (Button)v.findViewById(R.id.btn_select_img);
        mBtnReset = (Button)v.findViewById(R.id.btn_reset_icon);
        mBtnSelectImg.setOnClickListener(this);
        mBtnReset.setOnClickListener(this);
        setContentView(v);
        getWindow().setTitle(mContext.getResources().getString(R.string.change_icon_dialog_title));
    }

    public void setAppIcon(Bitmap bmap) {
        mBitmapIcon = bmap;
        mDrawbleIcon = null;
        handler.sendEmptyMessage(MESSAGE_NOTIFY_REFRESH_ICON);

    }

    public void changeAppIcon(Bitmap bmap) {
        mIconHasChanged = true;
        mBitmapIcon = bmap;
        handler.sendEmptyMessage(MESSAGE_NOTIFY_REFRESH_ICON);

    }

    public void setAppIcon(Drawable bmap) {
        mDrawbleIcon = bmap;
        mBitmapIcon = null;
        handler.sendEmptyMessage(MESSAGE_NOTIFY_REFRESH_ICON);

    }

    public void setCustomName(String name) {
        mDisplayName = name;
        handler.sendEmptyMessage(MESSAGE_NOTIFY_REFRESH_LABEL);
    }

    public OnDialogFinished mDialogFinished;
    public interface OnDialogFinished {
        void onFinish(Bitmap icon, boolean changed);
    }

    public void setOnDialogFinished(OnDialogFinished mOnDialogFinished) {
        mDialogFinished = mOnDialogFinished;
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mBtnSelectImg)) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", 256);
            intent.putExtra("outputY", 256);
            intent.putExtra("output", Uri.fromFile(new File(HomeUtils.getTmpImageFilePath())));
            intent.putExtra("outputFormat", "PNG");
            intent.putExtra("return-data", false);
            HomeManager.getInstance().startActivityForResult(intent, HomeScene.REQUESTCODE_APPOPTION_GETIMAGE2);

        }
        if (v.equals(mBtnReset)) {
            mIconHasChanged = false;
            dismiss();
            if (mDialogFinished != null) {
                mDialogFinished.onFinish(mBitmapIcon, false);
            }
        }

    }

    @Override
    public void dismiss() {
        // TODO Auto-generated method stub
        super.dismiss();
        if (mDialogFinished != null && mIconHasChanged) {
            mDialogFinished.onFinish(mBitmapIcon, true);
        }
    }
}
