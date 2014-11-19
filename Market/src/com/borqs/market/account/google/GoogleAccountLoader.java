package com.borqs.market.account.google;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.borqs.market.R;
import com.borqs.market.account.AccountLoader;
import com.borqs.market.account.AccountObserver;
import com.borqs.market.net.WutongParameters;
import com.borqs.market.utils.BLog;
import com.borqs.market.utils.IntentUtil;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class GoogleAccountLoader extends AccountLoader{
    private final String TAG = GoogleAccountLoader.class.getSimpleName();

    public String mEmail = null;
    private Handler mHandler;

    public GoogleAccountLoader(Activity activity) {
        super(activity);
        mHandler = new GoogleAuthHandler();
    }

    String[] accounts;
    
    private String[] getAccountNames() {
        if (isFinishing()) return null;
        Account[] accounts = AccountManager.get(mActivity).getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        int accountLength = accounts.length;
        String[] names = new String[accountLength + 1];
        for (int i = 0; i < accountLength; i++) {
            names[i] = accounts[i].name;
        }
        names[accountLength] = mActivity.getString(R.string.add_a_google_account);
        return names;
    }

    private void showSingleChoiceDialog(final Context context,int resTitle, final String[] items, final int checkItem,
            DialogInterface.OnClickListener itemClickListener,
            DialogInterface.OnClickListener positiveListener,
            DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder replaceBuilder = new AlertDialog.Builder(context);
        replaceBuilder.setTitle(resTitle).setSingleChoiceItems(items,
                checkItem, itemClickListener);
        if (positiveListener != null) {
            replaceBuilder.setPositiveButton(android.R.string.ok,
                    positiveListener);
        }
        if (negativeListener != null) {
            replaceBuilder.setNegativeButton(android.R.string.cancel,
                    negativeListener);
        }
        AlertDialog dialog = replaceBuilder.create();

        dialog.show();
    }
    
    private DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
        
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if(accounts != null) {
                if(default_index == (accounts.length-1)) {
                    //添加帐户
                    if (isFinishing()) return;
                    IntentUtil.startAddAccountSetting(mActivity);
                }else {
                    mEmail = accounts[default_index];
                    fetchUserInfoFromServer();
                }
            }
            
        }
    };
    
    int default_index = 0;
    private DialogInterface.OnClickListener itemListener = new DialogInterface.OnClickListener() {
        
        @Override
        public void onClick(DialogInterface dialog, int which) {
            default_index = which;
            
        }
    };
    
    @Override
    public void login() {
        mEmail = null;
        authenticate();
    };
    
    @Override
    public void authenticate() {
        if (!isFinishing()) {
            if(GooglePlayServicesUtil.isGooglePlayServicesAvailable(mActivity) != ConnectionResult.SUCCESS) {
                Toast.makeText(mActivity, "not support google play service", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if(TextUtils.isEmpty(mEmail) && !isRetry) {
            if (isFinishing()) return;
            accounts =  getAccountNames();
            showSingleChoiceDialog(mActivity, R.string.choose_account_title, accounts, default_index, itemListener, positiveListener, null);
        }else {
            fetchUserInfoFromServer();
        }
        isRetry = false;
    }

    boolean isRetry = false;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && requestCode == GoogleConstants.REQUEST_CODE_RECOVER_FROM_AUTH_ERROR) {
            if (resultCode == Activity.RESULT_OK) {
                BLog.v(TAG, "Retrying");
                isRetry = true;
                authenticate();
                return;
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                onAuthReject();
                return;
            }
        }
    }
    
    private String fetchToken() {
        BLog.v(TAG, "fetchToken() begin");
        if (isFinishing()) return null;
        try {
           return  GoogleAuthUtil.getToken(mActivity, mEmail, GoogleConstants.SCOPE);
        } catch (GooglePlayServicesAvailabilityException playEx) {
            // GooglePlayServices.apk is either old, disabled, or not present.
//            mActivity.showErrorDialog(playEx.getConnectionStatusCode());
            BLog.e(TAG,playEx.getMessage());
            mHandler.sendEmptyMessage(AUTH_FAILED);
        } catch (UserRecoverableAuthException userRecoverableException) {
            // Unable to authenticate, but the user can fix this.
            // Forward the user to the appropriate activity.
            mActivity.startActivityForResult(userRecoverableException.getIntent(), GoogleConstants.REQUEST_CODE_RECOVER_FROM_AUTH_ERROR);
            BLog.d(TAG, userRecoverableException.toString());
        } catch (GoogleAuthException fatalException) {
            BLog.e(TAG,fatalException.getMessage());
            mHandler.sendEmptyMessage(AUTH_FAILED);
        } catch (Exception e) {
            Log.e(TAG,e.getMessage());
            mHandler.sendEmptyMessage(AUTH_FAILED);
        }
        BLog.v(TAG, "fetchToken() end");
        return null;
    }

    /**
     * Contacts the user info server to get the profile of the user and extracts the first name
     * of the user from the profile. In order to authenticate with the user info server the method
     * first fetches an access token from Google Play services.
     * @throws IOException if communication with user info server failed.
     * @throws JSONException if the response from the server could not be parsed.
     */
    private void fetchUserInfoFromServer() {
        BLog.e(TAG,"fetchUserInfoFromServer() bein");
        AccountObserver.loging();
        new Thread() {
            @Override
            public void run() {
                String token = fetchToken();
                if (token == null) {
                    // error has already been handled in fetchToken()
                    return;
                }
                URL url;
                try {
                    url = new URL(GoogleConstants.APIS_GOOGLE_USER_INFO + token);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    int sc = con.getResponseCode();
                    BLog.e(TAG,"fetchUserInfoFromServer() response code = " + sc);
                    if (sc == 200) {
                        InputStream is = con.getInputStream();
                        String reponse = readResponse(is);
                        Message msg = mHandler.obtainMessage(AUTH_SUCCESS);
                        msg.getData().putString(RESULT, reponse);
                        msg.sendToTarget();
                        is.close();
                        return;
                    } else if (sc == 401) {
                        if (!isFinishing()) {
                            GoogleAuthUtil.invalidateToken(mActivity, token);
                        }
                        // "Server auth error, please try again."
                        BLog.d(TAG, "Server auth error: " + readResponse(con.getErrorStream()));
                        mHandler.sendEmptyMessage(TOKEN_INVALIDATED);
                        return;
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mHandler.sendEmptyMessage(AUTH_FAILED);
            }
        }.start();
        BLog.e(TAG,"fetchUserInfoFromServer() end");
    }
    
    public static final int AUTH_FAILED = 0x001;
    public static final int AUTH_REJECT = 0x002;
    public static final int AUTH_SUCCESS = 0x003;
    public static final int TOKEN_INVALIDATED = 0x004;
    
    public static final String RESULT = "RESULT";
    class GoogleAuthHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            if(what == AUTH_FAILED) {
                onAuthFailed();
            }else if(what == AUTH_REJECT) {
                onAuthReject();
            }else if(what == AUTH_SUCCESS) {
                String reponse = msg.getData().getString(RESULT);
//                Toast.makeText(mActivity, reponse, Toast.LENGTH_SHORT).show();
                GoogleUser user = new GoogleUser(reponse);
                WutongParameters params = new WutongParameters();
                params.add("name", user.name);
                params.add("email", mEmail);
                params.add("google_id",user.id);
                activeAccount(params, mEmail, user.name);
            }else if(what == TOKEN_INVALIDATED) {
                end();
                if (!isFinishing()) {
                    Toast.makeText(mActivity, "token invalidated,please try agin! ", Toast.LENGTH_SHORT).show();
                }
            }
        }
        
    }

    /**
     * Reads the response from the input stream and returns it as a string.
     */
    private static String readResponse(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] data = new byte[2048];
        int len = 0;
        while ((len = is.read(data, 0, data.length)) >= 0) {
            bos.write(data, 0, len);
        }
        return new String(bos.toByteArray(), "UTF-8");
    }
}
