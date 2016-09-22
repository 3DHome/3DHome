package com.borqs.se.home3d;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.borqs.se.R;

public class ChangeLabelDialog extends Dialog implements android.view.View.OnClickListener {
    private Context mContext;
//    private LayoutInflater inflater;
    private EditText mEdtLabel;
    private Button mBtnOK;
    private Button mBtnCancel;
    private static final int MESSAGE_NOTIFY_REFRESH_LABEL = 2;
    private String mDisplayName;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case MESSAGE_NOTIFY_REFRESH_LABEL:
                mEdtLabel.setText(mDisplayName + "");
                mEdtLabel.setSelection(mEdtLabel.getText().length());
                break;

            default:
                break;
            }

        }
    };

    public ChangeLabelDialog(Context context) {
        super(context);
        mContext = context;
        init(mContext);
    }
    public ChangeLabelDialog(Context context, int theme) {
        super(context, theme);
        mContext = context;
        init(mContext);
    }

    private void init(Context context) {
        getWindow().setTitle(mContext.getResources().getString(R.string.change_label_dialog_title));
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.dialog_change_label, null);
        mBtnOK = (Button) v.findViewById(R.id.change_label_dialog_btnOK);
        mBtnCancel = (Button) v.findViewById(R.id.change_label_dialog_btnCancel);
        mBtnOK.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);
        mEdtLabel = (EditText) v.findViewById(R.id.change_label_dialog_edtLabel);
        mEdtLabel.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String strInput = s.toString();
                strInput = strInput.trim();
                if (TextUtils.isEmpty(strInput)) {
                    mBtnOK.setEnabled(false);
                } else {
                    mDisplayName = s.toString();
                    mBtnOK.setEnabled(true);
                }
            }
        });
        setContentView(v);
    }

    public OnDialogFinished mDialogFinished;
    public interface OnDialogFinished {
        void onFinish(String displayName, boolean changed);
    }

    public void setOnDialogFinished(OnDialogFinished mOnDialogFinished) {
        mDialogFinished = mOnDialogFinished;
    }

    public void setCustomName(String name) {
        mDisplayName = name;
        handler.sendEmptyMessage(MESSAGE_NOTIFY_REFRESH_LABEL);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mBtnOK)) {
            dismiss();
            if(mDialogFinished != null) {
                mDialogFinished.onFinish(mDisplayName, true);
            }
        }
        if (v.equals(mBtnCancel)) {
            dismiss();
            if(mDialogFinished != null) {
                mDialogFinished.onFinish(mDisplayName, false);
            }
        }
        
    }

}
