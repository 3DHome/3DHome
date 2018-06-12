/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.market.account.phone;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.telephony.SmsManager;
import android.text.TextUtils;

import com.borqs.market.account.phone.PhoneLogin.BaseHandler;
import com.borqs.market.utils.BLog;

public final class SMSSender {
    public static final String ACCOUNT_REQUEST_SMS_RESPONSE_ACTION = "android.com.borqs.sms.account_requst";
    public static final String ACCOUNT_REQUEST_MESSAGE = "accountrequest/verifybysim";
    public static final String ACCOUNT_REQUEST_SEPERATE = " ";

    private Context mConetxt;
    private BroadcastReceiver mReceiver;
    private Handler mHandler;

    public SMSSender(Context context,Handler handler) {
        mConetxt = context;
        mHandler = handler;
    }

    public void sendMessage(String sendto, String body) {
        String message = ACCOUNT_REQUEST_MESSAGE + ACCOUNT_REQUEST_SEPERATE
                + body;
        Intent i = new Intent(ACCOUNT_REQUEST_SMS_RESPONSE_ACTION);
        PendingIntent deliveryIntent = PendingIntent.getBroadcast(mConetxt, 0,
                i, 0);
        SmsManager smsMgr = SmsManager.getDefault();

        mReceiver = new SMSGatewayReceiver();
        mConetxt.registerReceiver(mReceiver, new IntentFilter(
                ACCOUNT_REQUEST_SMS_RESPONSE_ACTION));
        try {
            BLog.d("SMS message: " + message);
            String smsNumber = sendto;
            BLog.d("SMS number: " + smsNumber);
            if (TextUtils.isEmpty(smsNumber)) {
                throw new Exception("Unsupported service on this server!");
            }
            smsMgr.sendTextMessage(smsNumber, null, message, deliveryIntent,
                    null);
        } catch (Exception e) {
            BLog.d("sendBindPhoneNumberMessage Exception: " + e.toString());
            mHandler.sendEmptyMessage(BaseHandler.SEND_MMS_FAILED);
        }
    }


    class SMSGatewayReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACCOUNT_REQUEST_SMS_RESPONSE_ACTION)) {                
                mHandler.sendEmptyMessageDelayed(BaseHandler.SEND_MMS_SUCCESS,2000);
            }
            mConetxt.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }
}
