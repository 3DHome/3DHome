package com.borqs.market.utils;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import com.borqs.market.api.ApiUtil;
import com.borqs.market.net.RequestListener;
import com.borqs.market.net.WutongException;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

public class FileUploadTask extends AsyncTask<File, Void, String> {
//    public static String API_SHARE_UPDATE = "http://api.borqs.com:7890/api/v2/purchase/shares/update";
    private static final String SHARE_UPDATE="api/v2/purchase/shares/update";

    private static final int SET_CONNECTION_TIMEOUT = 20 * 1000;
    private static final int SET_SOCKET_TIMEOUT = 120 * 1000;

    private static final String BOUNDARY = "------boundary1";

    private static final String NEWLINE = "\r\n";

    private static final String START_BOUNDARY = "--" + BOUNDARY;

    private static final String END_BOUNDARY = "--" + BOUNDARY + "--" + NEWLINE;

    private static final String TAG = FileUploadTask.class.getSimpleName();

    private String app_id;
    private int app_version;
    private String category_id;
    private String supported_mod;
    private String ticket;
    private RequestListener listener;

    private Exception mException;

    public FileUploadTask(String app_id, int app_version, String category_id, String ticket, String supported_mod, final RequestListener listener) {
        this.app_id = app_id;
        this.app_version = app_version;
        this.category_id = category_id;
        this.ticket = ticket;
        this.supported_mod = supported_mod;
        this.listener = listener;
    }

    @Override
    protected String doInBackground(File... params) {
        String result = null;
        try {
            result = sendData(params[0], supported_mod);
        } catch (Exception e) {
            if (!isCancelled()) {
                mException = e;
            }
        }

        return result;
    }

    private static void encodeParameter(DataOutputStream outStream, String key, String value) throws IOException {
        if (null != outStream) {
            outStream.writeBytes(START_BOUNDARY + NEWLINE);
            final String keyLine = "Content-Disposition: form-data; name=\"" + key + "\"";
            outStream.writeBytes(keyLine + NEWLINE);
            outStream.writeBytes(NEWLINE + value + NEWLINE);
        }
    }
    protected String sendData(File file, String supported_mod) {
        String result = null;
        String responseBody = null;

        HttpURLConnection conn = null;
        DataOutputStream outStream = null;

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            final StringBuffer url = new StringBuffer(ApiUtil.getInstance().getApiUrl(SHARE_UPDATE)).append("?").append("ticket").append("=").append(ticket);
            conn = (HttpURLConnection) new URL(url.toString()).openConnection();

            conn.setConnectTimeout(SET_CONNECTION_TIMEOUT);
            conn.setReadTimeout(SET_SOCKET_TIMEOUT);

            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setChunkedStreamingMode(1024);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("User-Agent", QiupuHelper.user_agent);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

            outStream = new DataOutputStream(conn.getOutputStream());
//            outStream.writeBytes(START_BOUNDARY + NEWLINE);
//            outStream.writeBytes("Content-Disposition: form-data; name=\"app_id\"" + NEWLINE);
//            outStream.writeBytes(NEWLINE + app_id + NEWLINE);
            encodeParameter(outStream, "app_id", app_id);
            
//            outStream.writeBytes(START_BOUNDARY + NEWLINE);
//            outStream.writeBytes("Content-Disposition: form-data; name=\"app_version\"" + NEWLINE);
//            outStream.writeBytes(NEWLINE + app_version + NEWLINE);
            encodeParameter(outStream, "app_version", String.valueOf(app_version));
            
//            outStream.writeBytes(START_BOUNDARY + NEWLINE);
//            outStream.writeBytes("Content-Disposition: form-data; name=\"category_id\"" + NEWLINE);
//            outStream.writeBytes(NEWLINE + category_id + NEWLINE);
            encodeParameter(outStream, "category_id", category_id);

//            outStream.writeBytes(START_BOUNDARY + NEWLINE);
//            outStream.writeBytes("Content-Disposition: form-data; name=\"ticket\"" + NEWLINE);
//            outStream.writeBytes(NEWLINE + ticket + NEWLINE);
            encodeParameter(outStream, "ticket", ticket);

            if(!TextUtils.isEmpty(supported_mod)) {
                encodeParameter(outStream, ApiUtil.SUPPORTED_MODE, supported_mod);
            }

            outStream.writeBytes(START_BOUNDARY + NEWLINE);
            outStream.writeBytes("Content-Disposition: form-data; "
                    + "name=\"file\"; filename=\"" + file + "\"" + NEWLINE);
            outStream.writeBytes("Content-Type: application/octet-stream" + NEWLINE);

            outStream.writeBytes(NEWLINE);
            
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];
            String response = "error";
            // Read file
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            Log.e("Image length", bytesAvailable + "");
            try {
                while (bytesRead > 0) {
//                    Log.e("bytesRead", bytesRead + "");
                    try {
                        outStream.write(buffer, 0, bufferSize);
                    } catch (OutOfMemoryError e) {
                        e.printStackTrace();
                        response = "outofmemoryerror";
                        return response;
                    }
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }
            } catch (Exception e) {
                e.printStackTrace();
                response = "error";
                return response;
            }
//            outStream.writeBytes(lineEnd);
//            outStream.writeBytes(twoHyphens + boundary + twoHyphens
//                    + lineEnd);
//            
            
            
            outStream.writeBytes(NEWLINE);
            outStream.writeBytes(END_BOUNDARY);

            int responseCode = conn.getResponseCode();
            
            if (responseCode == 200) {
                responseBody = HttpUtils.getContent(conn.getInputStream()).toString("UTF-8");
                result = responseBody.toString();
                
            } else {
                Log.w(TAG, "Error response: " + HttpUtils.getErrorResponse(conn));
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            if(fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outStream != null) {
                try {
                    outStream.flush();
                    outStream.close();
                    outStream = null;
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }

        
        return result;
    }

    protected void onPostExecute(String result) {
        if (result == null || mException != null) {
            listener.onError(new WutongException(mException));
            onError();
        } else {
            JSONObject obj;
            try {
                obj = new JSONObject(result);
                int code = obj.optInt("code", -1);
                if (code != 0) {
                    String error_message = obj.optString("error_message");
                    Log.e(BLog.TAG, error_message);
                    listener.onError(new WutongException(error_message));
                } else {
                    listener.onComplete(result);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            onSuccess(result);
        }
    }

    protected void onError() {
        Log.w(TAG, "Error sending data.", mException);
    }

//    protected abstract void onSuccess(String result);
    protected void onSuccess(String result) {
        Log.v(TAG, result);
    }

    protected Exception getException() {
        return mException;
    }
}