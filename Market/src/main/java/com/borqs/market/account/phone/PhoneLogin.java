package com.borqs.market.account.phone;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;

import com.borqs.market.utils.BLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class PhoneLogin {
    public static final String SMS_SERVER_ERROR = "NotWork";
    private Handler mHandler;
    private Context mContext;
    private GetPhoneListener phoneListener;
    private final static int DEFAULT_RETRY_TIMES = 3;
    private int retrytimes = 0;
    
    public PhoneLogin(Context context,GetPhoneListener listener) {
        super();
        this.mContext = context;
        this.phoneListener = listener;
        retrytimes = 0;
        mHandler = new BaseHandler();
    }

    public void getMmsGateWay() {
        new Thread() {
            @Override
            public void run() {
                URL url;
                try {
                    url = new URL("http://api.borqs.com/sync/webagent/configuration/query?key=sms_service_number");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    int sc = con.getResponseCode();
                    BLog.d("getMmsGateWay() code == "+sc);
                    if (sc == 200) {
                        InputStream is = con.getInputStream();
                        String reponse = readResponse(is);
                        is.close();
                        if(!SMS_SERVER_ERROR.equalsIgnoreCase(reponse)) {
                            JSONObject obj = new JSONObject(reponse);
                            String msgTo = obj.optString("result");
                            Message msg = mHandler.obtainMessage(BaseHandler.QUERY_MSG_GATEWAY_SUCCESS);
                            msg.getData().putString(RESULT, msgTo);
                            msg.sendToTarget();
                            return;
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mHandler.sendEmptyMessage(BaseHandler.QUERY_MSG_GATEWAY_FAILED);
            }
        }.start();
    }
    public  void getPhoneNumber() {
        final String  devicesId = getImsi(mContext);
        new Thread() {
            @Override
            public void run() {
                retrytimes++;
                URL url;
                try {
                    url = new URL("http://api.borqs.com/sync/webagent/accountrequest/getguidbysim?data="+devicesId);
                    BLog.d("getPhoneNumber() url == "+url);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    int sc = con.getResponseCode();
                    if (sc == 200) {
                        InputStream is = con.getInputStream();
                        String reponse = readResponse(is);
                        JSONObject obj = new JSONObject(reponse);
                        if(obj.has("phone")) {
                            String phone = obj.optString("phone");
                            is.close();
                            BLog.d("photoNumber="+phone);
                            Message msg = mHandler.obtainMessage(BaseHandler.GET_PHONE_NUMBER_SUCCESS);
                            msg.getData().putString(RESULT, phone);
                            msg.sendToTarget();
                        }else {
                            mHandler.sendEmptyMessage(BaseHandler.QUERY_MSG_GATEWAY_FAILED);
                        }
                        return;
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mHandler.sendEmptyMessage(BaseHandler.QUERY_MSG_GATEWAY_FAILED);
            }
        }.start();
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
    
    
    public static final String RESULT = "RESULT";
    class BaseHandler extends Handler {
        public static final int QUERY_MSG_GATEWAY_SUCCESS = 0x001;
        public static final int QUERY_MSG_GATEWAY_FAILED = 0x002;
        public static final int SEND_MMS_SUCCESS = 0x003;
        public static final int GET_PHONE_NUMBER_SUCCESS = 0x004;
        public static final int SEND_MMS_FAILED = 0x005;
        public static final int GET_PHONE_NUMBER = 0x006;

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            if(what == QUERY_MSG_GATEWAY_SUCCESS) {
                String msg_gate_way = msg.getData().getString(RESULT);
                BLog.d("getMmsGateWay() msg_gate_way == "+msg_gate_way);
                
                SMSSender sender = new SMSSender(mContext,mHandler);
                sender.sendMessage(msg_gate_way, getImsi(mContext));
                
            }else if(what == QUERY_MSG_GATEWAY_FAILED) {
                if(retrytimes >= DEFAULT_RETRY_TIMES) {
                    BLog.d("get phone number is falied...");
                    phoneListener.getPhoneNumberFailed();
                }else {
                    BLog.d("retry get phone number and times = " + (retrytimes+1));
                    mHandler.sendEmptyMessageDelayed(BaseHandler.SEND_MMS_SUCCESS,2000);
                }
            }else if(what == SEND_MMS_SUCCESS || what == GET_PHONE_NUMBER) {
                getPhoneNumber();
            }else if(what == GET_PHONE_NUMBER_SUCCESS) {
                String photo_number = msg.getData().getString(RESULT);
                phoneListener.getPhoneNumberSuccess(photo_number);
            }
        }
        
    }
    
    public static String getImsi(Context context) {
        TelephonyManager telephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imsi = telephonyMgr.getSubscriberId();
        if(imsi == null) {
            return null;
        }else {
            JSONObject obj = new JSONObject();
            try {
                obj.put("device_id", imsi);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return obj.toString();
        }
    }
    
    interface GetPhoneListener {
        void getPhoneNumberSuccess(String phoneNumber);
        void getPhoneNumberFailed();
    }
}
