package com.borqs.borqsweather.weather.yahoo;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import android.util.Log;

public class HttpConnectHelper {
    /** TAG for debugging */
    private static final boolean DEBUG = false;
    private static final String TAG = "Weather_HttpConnectHelper";

    /** HTTP Connection */
    private HttpURLConnection httpConnection;

    private void requestConnectServer(String strURL) throws Exception {
        if (DEBUG) Log.i(TAG, "requestConnectServer: " + strURL);
        httpConnection = (HttpURLConnection) new URL(strURL).openConnection();
        httpConnection.setConnectTimeout(15 * 1000);
        httpConnection.setReadTimeout(15 * 1000);
        httpConnection.connect();

        if (httpConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            Log.e(TAG, "Something wrong with connection");
            httpConnection.disconnect();
            throw new IOException("Error in connection: " + httpConnection.getResponseCode());
        }
    }

    private void requestDisconnect() {
        if (httpConnection != null) {
            httpConnection.disconnect();
        }
    }

    public Document getDocumentFromURL(String strURL) throws Exception {
        /* Verify URL */
        if (strURL == null) {
            Log.e(TAG, "Invalid input URL");
            return null;
        }

        /* Connect to server */
        requestConnectServer(strURL);

        /* Get data from server */
        String strDocContent = getDataFromConnection();

        /* Close connection */
        requestDisconnect();

        if (strDocContent == null) {
            Log.e(TAG, "Can not get xml content");
            return null;
        }

        int strContentSize = strDocContent.length();
        StringBuffer strBuff = new StringBuffer();
        strBuff.setLength(strContentSize + 1);
        strBuff.append(strDocContent);
        ByteArrayInputStream is = new ByteArrayInputStream(strDocContent.getBytes());

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        Document docData = null;

        try {
            db = dbf.newDocumentBuilder();
            docData = db.parse(is);
        } catch (Exception e) {
            Log.e(TAG, "Parser data error");
            return null;
        }

        return docData;
    }

    private String getDataFromConnection() throws IOException {
        if (httpConnection == null) {
            Log.e(TAG, "connection is null");
            return null;
        }

        String strValue = "";
        InputStream inputStream = httpConnection.getInputStream();
        if (inputStream == null) {
            Log.e(TAG, "Get input tream error");
            return null;
        }

        StringBuffer strBuf = new StringBuffer();
        BufferedReader buffReader = new BufferedReader(new InputStreamReader(inputStream));
        String strLine = "";

        while ((strLine = buffReader.readLine()) != null) {
            strBuf.append(strLine + "\n");
            strValue += strLine + "\n";
        }

        /* Release resource to system */
        buffReader.close();
        inputStream.close();
        if (DEBUG)
            Log.d(TAG, "get data from connection : " + strValue);

        return strBuf.toString();
    }
}
