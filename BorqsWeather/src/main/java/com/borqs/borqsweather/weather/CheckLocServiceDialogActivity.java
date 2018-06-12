package com.borqs.borqsweather.weather;

import com.borqs.borqsweather.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Window;
import android.widget.Toast;

public class CheckLocServiceDialogActivity extends Activity {
    Context mContext;
    CheckLocServiceDialogActivity mDialogActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mContext = this;
        mDialogActivity = this;
        new AlertDialog.Builder(this,
                AlertDialog.THEME_DEVICE_DEFAULT_DARK).setMessage(R.string.dialog_gls_msg)
                .setPositiveButton(R.string.enable, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            mContext.startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(mContext, R.string.activity_not_found,
                                    Toast.LENGTH_SHORT).show();
                        }
                        mDialogActivity.finish();
                    }
                }).setNegativeButton(R.string.disable, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mDialogActivity.finish();
                    }
                }).setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mDialogActivity.finish();
                    }
                }).show();
    }

}
