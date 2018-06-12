package com.borqs.market.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

public class DialogUtils {
    
    @SuppressWarnings("unused")
    private static final String TAG = DialogUtils.class.getSimpleName();


    public static AlertDialog createDialog(final Context context, int resTitle, int resMsg,
                                           DialogInterface.OnClickListener listener) {
        AlertDialog.Builder replaceBuilder = new AlertDialog.Builder(context);
        replaceBuilder.setTitle(resTitle)
                .setMessage(resMsg)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, listener);
        AlertDialog dialog = replaceBuilder.create();
        return dialog;
    }
    
    public static Dialog showProgressDialog(Context context, int titleRes, int msgRes, boolean cancleable) {
        ProgressDialog dialog = new ProgressDialog(context);
        if (titleRes > 0) {
            dialog.setTitle(titleRes);
        }
        dialog.setMessage(context.getString(msgRes));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setIndeterminate(true);
        dialog.setCancelable(cancleable);
        dialog.show();
        return dialog;
    }
}