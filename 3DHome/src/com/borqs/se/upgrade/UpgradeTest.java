package com.borqs.se.upgrade;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.borqs.se.home3d.HomeActivity;
import com.borqs.se.home3d.HomeUtils;
import com.borqs.se.home3d.SettingsActivity;

public class UpgradeTest extends Thread {
    private Context mContext;
    private static final String URL_RELEASE = "http://release.borqs.com/apk/find.php";

    private Handler mHandler;
    private boolean mStop;
    public static int TEST_TYPE_SETTINGS = 0;
    public static int TEST_TYEP_ACTIVITY = 1;
    private int testType = TEST_TYPE_SETTINGS;//0 mean check from settings, 1 mean check from SEHomeActivity.

    public UpgradeTest(Context context, Handler handler, int type) {
        mContext = context;
        mHandler = handler;
        mStop = false;
        testType = type;
    }

    @Override
    public void run() {
        checkAppVersion(mContext);
    }
    
    public void stopUpgrade() {
        mStop = true;
    }

    private String getAppInfo(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = telephonyManager.getDeviceId();
        String imsi = telephonyManager.getSubscriberId();
        String packageName = context.getPackageName();
        int version = 0;
        try {
            version = context.getPackageManager().getPackageInfo(packageName, 0).versionCode;
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String appInfo = "<update>\r\n" + "<manufacture>" + android.os.Build.MANUFACTURER + "</manufacture>\r\n"
                + "<model>" + android.os.Build.MODEL + "</model>\r\n" + "<imei>" + imei + "</imei>\r\n" + "<imsi>"
                + imsi + "</imsi>\r\n" + "<sdk>" + android.os.Build.VERSION.SDK_INT + "</sdk>\r\n" + "<android>"
                + android.os.Build.VERSION.RELEASE + "</android>\r\n" + "<env>" + getEnv(context) + "</env>\r\n"
                + "<item>\r\n" + "<package>" + context.getPackageName() + "</package>\r\n" + "<version>" + version
                + "</version>\r\n" + "</item>\r\n" + "</update>";
        return appInfo;
    }

    private String getEnv(Context context) {
        boolean isTest = SettingsActivity.getIsTestOrRelease(context);
        String env = isTest ? "test" : "release";
        return env;
    }

    private void checkAppVersion(Context context) {
        try {
            URL netUrl = new URL(URL_RELEASE);
            if (HomeUtils.DEBUG)
                Log.d("UpgradeTest ##############", "upgrade netUrl = " + netUrl);
            HttpURLConnection con = (HttpURLConnection) netUrl.openConnection();
            con.setRequestMethod("POST");
            con.setConnectTimeout(30000);
            con.setReadTimeout(30000);
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.connect();
            if (mStop) {
                return;
            }
            String appsInfo = getAppInfo(context);
            if (HomeUtils.DEBUG)
                Log.d("UpgradeTest ##############", "upgrade current appsInfo = " + appsInfo);
            OutputStream out = con.getOutputStream();
            out.write(appsInfo.getBytes());
            out.flush();
            out.close();
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                if (mStop) {
                    return;
                }
                if (HomeUtils.DEBUG)
                    Log.d("UpgradeTest ##############", "upgrade response success responseCode = " + responseCode);
                InputStream in = con.getInputStream();
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = dbf.newDocumentBuilder();
                Document doc = docBuilder.parse(in);
                Element root = doc.getDocumentElement();
                NodeList nodes = root.getElementsByTagName("item");
                int len = nodes.getLength();
                if (HomeUtils.DEBUG)
                    Log.d("UpgradeTest ##############", "upgrade response get len = " + len);
                for (int i = 0; i < len; i++) {
                    if (mStop) {
                        return;
                    }
                    Element node = (Element) nodes.item(i);
                    String packageName = getNodeValue(node, "package");
                    long size = Long.parseLong(getNodeValue(node, "size"));
                    String md5 = getNodeValue(node, "md5");
                    String url = getNodeValue(node, "url");
                    int version = Integer.parseInt(getNodeValue(node, "version"));
                    String able = context.getResources().getConfiguration().locale.getCountry();
                    String releasenotes;
                    if (able.equals("CN")) {
                        releasenotes = getNodeValue(node, "releasenotes_cn");
                    } else {
                        releasenotes = getNodeValue(node, "releasenotes_en");
                    }

                    String curPackageName = context.getPackageName();
                    int curVersion = 0;
                    try {
                        curVersion = context.getPackageManager().getPackageInfo(packageName, 0).versionCode;
                    } catch (NameNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    if (curPackageName.equals(packageName) && version > curVersion) {
                        Message msg = null;
                        if (testType == TEST_TYPE_SETTINGS) {
                            mHandler.removeMessages(SettingsActivity.MSG_SHOW_DIALOG_UPGRADE);
                            msg = mHandler.obtainMessage(SettingsActivity.MSG_SHOW_DIALOG_UPGRADE);
                        } else if (testType == TEST_TYEP_ACTIVITY) {
                            mHandler.removeMessages(HomeActivity.MSG_GET_UPGRADE_INFO);
                            msg = mHandler.obtainMessage(HomeActivity.MSG_GET_UPGRADE_INFO);
                        }
                        if (msg != null) {
                            Bundle data = new Bundle();
                            data.putInt("cur_version", curVersion);
                            data.putInt("latest_version", version);
                            data.putLong("size", size);
                            data.putString("release_note", releasenotes);
                            data.putString("url", url);
                            msg.setData(data);
                            mHandler.sendMessage(msg);
                        }
                        return;
                    }
                    if ((i == len - 1) && curVersion > 0) {
                        if (testType == TEST_TYPE_SETTINGS) {
                            mHandler.sendEmptyMessage(SettingsActivity.MSG_LATEST_REMOVE_UPGRADE_PD);
                        }
                        return;
                    }
                    if (HomeUtils.DEBUG)
                        Log.d("UpgradeTest ##############", "upgrade response success packageName = " + packageName
                                + "; size = " + size + "; md5 = " + md5 + "; url = " + url + "; version = " + version);
                    // download the app by the absolute url and install it
                }
                if (len == 0) {
                    if (testType == TEST_TYPE_SETTINGS) {
                        mHandler.sendEmptyMessage(SettingsActivity.MSG_LATEST_REMOVE_UPGRADE_PD);
                    }
                    return;
                }
            } else {
                if (HomeUtils.DEBUG)
                    Log.d("UpgradeTest ##############", "upgrade response failed responseCode = " + responseCode);
            }
        } catch (Exception e) {
            if (HomeUtils.DEBUG)
                Log.d("UpgradeTest ##############", "upgrade exception = " + e.toString());
        }
        if (testType == TEST_TYPE_SETTINGS) {
            mHandler.sendEmptyMessage(SettingsActivity.MSG_ERROR_REMOVE_UPGRADE_PD);
        }
    }

    private String getNodeValue(Element parent, String nodeName) {
        String value = "";
        if (parent != null) {
            NodeList nodes = parent.getElementsByTagName(nodeName);
            if (nodes.getLength() > 0) {
                value = nodes.item(0).getTextContent();
            }
        }
        return value;
    }

}
