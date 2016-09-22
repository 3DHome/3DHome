package com.borqs.market.account;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.borqs.market.R;
import com.borqs.market.api.ApiUtil;
import com.borqs.market.net.RequestListener;
import com.borqs.market.net.WutongException;
import com.borqs.market.net.WutongParameters;
import com.borqs.market.utils.BLog;
import com.borqs.market.utils.DataConnectionUtils;
import com.borqs.market.utils.DialogUtils;

public abstract class AccountLoader {
    private static final String TAG = AccountLoader.class.getSimpleName();
    public Activity mActivity;
    protected Handler mHandler;
    protected Dialog loginProcessDialog;
    
    public AccountLoader(Activity mActivity) {
        super();
        this.mActivity = mActivity;
        mHandler = new BaseHandler();
    }

    public abstract void authenticate();
    
    public abstract void onActivityResult(int requestCode, int resultCode, Intent data);
    
    public abstract void login();
    
//    public void loadAccount() {
//        BLog.d("loadAccount() begin");
//        SharedPreferences sp = getAccountSharePreferences();
//        String token = sp.getString(SP_EXTRAS_TOKEN, null);
//        String userId = sp.getString(SP_EXTRAS_ID, null);
//        if(TextUtils.isEmpty(token) || TextUtils.isEmpty(userId)) {
//            AccountSession.setAccount_id(null);
//            AccountSession.setAccount_session(null);
//            AccountSession.setLogin(false);
//        }else {
//            AccountSession.setAccount_id(userId);
//            AccountSession.setAccount_session(token);
//            AccountSession.setLogin(true);
//        }
//    }
//    protected void login(String userId,String token) {
//        BLog.d("login userid="+userId);
//        BLog.d("login token="+token);
//        SharedPreferences sp = getAccountSharePreferences();
//        SharedPreferences.Editor Editor = sp.edit();
//        Editor.putString(SP_EXTRAS_ID, userId);
//        Editor.putString(SP_EXTRAS_TOKEN, token);
//        Editor.commit();
//        AccountSession.setAccount_id(userId);
//        AccountSession.setAccount_session(token);
//        AccountSession.setLogin(true);
//        AccountObserver.login();
//    }
    
    private boolean isLoading;
    private final Object mLocked = new Object();
    protected void activeAccount(WutongParameters params,final String email, final String name) {
        if (isLoading) {
            BLog.v(TAG, "is Loading ...");
            return;
        }

        if (isFinishing()) return;

        BLog.v(TAG, "begin activeAccount");
        if (!DataConnectionUtils.testValidConnection(mActivity)) {
            BLog.v(TAG, mActivity.getString(R.string.dlg_msg_no_active_connectivity));
            return;
        }
        synchronized (mLocked) {
            isLoading = true;
        }
        ApiUtil mApiUtil = ApiUtil.getInstance();
        mApiUtil.accountActiveByGoogle(params,
                new RequestListener() {
            
            @Override
            public void onIOException(IOException e) {
                BLog.v(TAG,
                        "activeAccount onIOException  "
                                + e.getMessage());
                synchronized (mLocked) {
                    isLoading = false;
                }
                
                mHandler.sendEmptyMessage(BaseHandler.ACTIVE_ACCOUNT_FAILED);
            }
            
            @Override
            public void onError(WutongException e) {
                BLog.d(TAG, "activeAccount onError  " + e.getMessage());
                synchronized (mLocked) {
                    isLoading = false;
                }
                mHandler.sendEmptyMessage(BaseHandler.ACTIVE_ACCOUNT_FAILED);
            }
            
            @Override
            public void onComplete(String response) {
                JSONObject obj;
                try {
                    obj = new JSONObject(response);
                    if (!obj.has("data"))
                        return;
                    if (isFinishing()) return;
                    JSONObject dataObj = obj.optJSONObject("data");
                    AccountSession.login(mActivity.getApplicationContext(),dataObj.optString("id"),dataObj.optString("ticket"),email,name);
                    mHandler.sendEmptyMessage(BaseHandler.ACTIVE_ACCOUNT_SUCCESS);
                } catch (JSONException e) {
                    e.printStackTrace();
                }finally {
                    synchronized (mLocked) {
                        isLoading = false;
                    }
                }
            }
        });
    }
    
    class BaseHandler extends Handler {
        public static final int ACTIVE_ACCOUNT_SUCCESS = 0x001;
        public static final int ACTIVE_ACCOUNT_FAILED = 0x002;

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            end();
            if (isFinishing()) return;
            if(what == ACTIVE_ACCOUNT_SUCCESS) {
                Toast.makeText(mActivity, R.string.login_success, Toast.LENGTH_SHORT).show();
                AccountObserver.login();
            }else if(what == ACTIVE_ACCOUNT_FAILED) {
                AccountObserver.cancleLogin();
                Toast.makeText(mActivity, R.string.login_failed, Toast.LENGTH_SHORT).show();
            }
        }
        
    }
    
//    private SharedPreferences getAccountSharePreferences() {
//        return mActivity.getSharedPreferences(SHARE_PREFERENCES_ACCOUNT_SETTING, Context.MODE_PRIVATE);
//    }
    
    protected void onAuthReject() {
        BLog.d("User rejected authorization.");
        end();
    }
    protected void onAuthFailed() {
        end();
    }
    
    protected void begin() {
        if (isFinishing()) return;
        loginProcessDialog = DialogUtils.showProgressDialog(mActivity, 0, R.string.lable_being_login, false);
        loginProcessDialog.show();
    }
    
    protected void end() {
        if(loginProcessDialog != null) {
            loginProcessDialog.dismiss();
        }
    }

    protected boolean isFinishing() {
        return null == mActivity || mActivity.isFinishing();
    }
}
