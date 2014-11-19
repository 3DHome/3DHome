package com.borqs.market.account.phone;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;

import com.borqs.market.R;
import com.borqs.market.account.AccountLoader;
import com.borqs.market.net.WutongParameters;
import com.borqs.market.utils.DialogUtils;

public class PhoneAccountLoader extends AccountLoader {
    
    public PhoneAccountLoader(Activity activity) {
        super(activity);
    }

    @Override
    public void authenticate() {
        begin();
        PhoneLogin login = new PhoneLogin(mActivity.getBaseContext(),new PhoneLogin.GetPhoneListener() {
            
            @Override
            public void getPhoneNumberSuccess(String phoneNumber) {
//                Toast.makeText(mActivity, "phoneNumber="+phoneNumber, Toast.LENGTH_SHORT).show();
                WutongParameters params = new WutongParameters();
                params.add("phone", phoneNumber);
                activeAccount(params, null, null);
            }
            
            @Override
            public void getPhoneNumberFailed() {
                end();
                Toast.makeText(mActivity, R.string.login_failed, Toast.LENGTH_SHORT).show();
            }
        });
        login.getMmsGateWay();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void login() {
        DialogUtils.createDialog(mActivity,R.string.onekey_login, R.string.onekey_login_message,
                new  DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                authenticate();
            }
        }).show();
        
    }

}
