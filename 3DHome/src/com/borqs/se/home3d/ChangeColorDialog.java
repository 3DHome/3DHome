package com.borqs.se.home3d;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.borqs.se.R;
import com.borqs.se.widget3d.NormalObject;

public class ChangeColorDialog extends Dialog implements OnSeekBarChangeListener {
    private SeekBar mRedBar;
    private SeekBar mGreenBar;
    private SeekBar mBlueBar;
//    private Button mButton;
    private NormalObject mNormalObject;

    public ChangeColorDialog(Context context) {
        super(context, R.style.ChangeColorDialog);
        setContentView(R.layout.dialog_change_color);
       
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        init();
    }

    public void setOperateObject(NormalObject obj) {
        mNormalObject = obj;
    }

    private void init() {
        int color = 0xffffff;
        if (mNormalObject != null && mNormalObject.getObjectInfo().mFaceColor != -1) {
            color = mNormalObject.getObjectInfo().mFaceColor;
        }
        mRedBar = (SeekBar) findViewById(R.id.red_bar);
        mRedBar.setMax(255);
        mRedBar.setProgress(Color.red(color));
        mRedBar.setOnSeekBarChangeListener(this);
        mGreenBar = (SeekBar) findViewById(R.id.green_bar);
        mGreenBar.setMax(255);
        mGreenBar.setProgress(Color.green(color));
        mGreenBar.setOnSeekBarChangeListener(this);
        mBlueBar = (SeekBar) findViewById(R.id.blue_bar);
        mBlueBar.setMax(255);
        mBlueBar.setProgress(Color.blue(color));
        mBlueBar.setOnSeekBarChangeListener(this);
        View button = findViewById(R.id.btn_reset_color);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mRedBar.setProgress(255);
                mGreenBar.setProgress(255);
                mBlueBar.setProgress(255);

            }
        });

        setOnDismissListener(new OnDismissListener() {

            public void onDismiss(DialogInterface dialog) {
                if (mNormalObject != null) {
                    int valueRed = mRedBar.getProgress();
                    int valueGreen = mGreenBar.getProgress();
                    int valueBlue = mBlueBar.getProgress();
                    int color = (valueRed << 16) + (valueGreen << 8) + valueBlue;
                    if (mNormalObject != null) {
                        mNormalObject.changeColor(color, true, true);
                    }
                }

            }
        });
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int valueRed = mRedBar.getProgress();
        int valueGreen = mGreenBar.getProgress();
        int valueBlue = mBlueBar.getProgress();
        int color = (valueRed << 16) + (valueGreen << 8) + valueBlue;
        if (mNormalObject != null) {
            mNormalObject.changeColor(color, true, false);
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int valueRed = mRedBar.getProgress();
        int valueGreen = mGreenBar.getProgress();
        int valueBlue = mBlueBar.getProgress();
        int color = (valueRed << 16) + (valueGreen << 8) + valueBlue;
        if (mNormalObject != null) {
            mNormalObject.changeColor(color, true, true);
        }

    }

}
