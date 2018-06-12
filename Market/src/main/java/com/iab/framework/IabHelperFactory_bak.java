package com.iab.framework;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.text.format.DateUtils;
import android.util.Log;
import com.iab.engine.google.util.IabHelper;
import com.iab.engine.google.util.IabResult;
import com.iab.engine.google.util.Purchase;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: b608
 * Date: 13-5-21
 * Time: 下午4:42
 * To change this template use File | Settings | File Templates.
 */
public class IabHelperFactory_bak {
    private static final String TAG = "IabHelperFactory";
    public static final boolean FORCE_IAB_DEBUG = true;

    public static final int RC_REQUEST = 10001;

    public static final int GOOGLE = 1;

    private static boolean mEngineReady = false;
    public static boolean ismEngineReady() {
        return mEngineReady;
    }

    private IabHelperFactory_bak() {
        // no instance
    }

    // The helper object
    private static IabHelper mHelper;

    public static void createIabHelper(final Activity activity, int type, String base64EncodedPublicKey,
                                       final IabHelper.QueryInventoryFinishedListener listener) {
        Log.d(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(activity, base64EncodedPublicKey);

        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(true);

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");
                mEngineReady = result.isSuccess();
                if (!mEngineReady) {
                    // Oh noes, there was a problem.
                    complain(activity, "Problem setting up in-app billing: " + result);
                    return;
                }
                
                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;
                
                // Hooray, IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d(TAG, "Setup successful. Querying inventory.");
                try {
                    mHelper.queryInventoryAsync(listener);
                } catch (Exception e) {
                    Log.e(TAG, "queryInventoryAsync, failed after setup succeed? should not be here!");
                    e.printStackTrace();
                }
            }
        });
    }

    public static void consumeAsync(Purchase purchase, IabHelper.OnConsumeFinishedListener listener) {
        mHelper.consumeAsync(purchase, listener);
    }
    
    public static void consumeAsync(List<Purchase> purchases, IabHelper.OnConsumeMultiFinishedListener listener) {
        if (mEngineReady) {
            mHelper.consumeAsync(purchases, listener);
        }
    }

    public static void launchPurchaseFlow(Activity act, String sku,
                IabHelper.OnIabPurchaseFinishedListener listener, String payload) {
        if (!mEngineReady) {
            complain(act, "Problem setting up in-app billing: Bill Unavailable for " + payload);
        } else {
            mHelper.launchPurchaseFlow(act, sku, RC_REQUEST,
                    listener, payload);
        }
    }

    public static void complain(Context context, String message) {
        Log.e(TAG, "**** TrivialDrive Error: " + message);
        alert(context, "Error: " + message);
    }

    public static void alert(Context context, String message) {
//        AlertDialog.Builder bld = new AlertDialog.Builder(context);
//        bld.setMessage(message);
//        bld.setNeutralButton("OK", null);
        Log.d(TAG, "Showing alert dialog: " + message);
//        bld.create().show();
    }

    public static void despose() {
        if (mEngineReady && mHelper != null) mHelper.dispose();
        mEngineReady = false;
        mHelper = null;
    }

    public static boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RC_REQUEST == requestCode) {
            // TODO: handle the result.
            // Pass on the activity result to the helper for handling
            if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
                // not handled, so handle it ourselves (here's where you'd
                // perform any handling of activity results not related to
                // in-app
                // billing...
                return false;
            } else {
                Log.d(TAG, "onActivityResult handled by IABUtil.");
            }
            return true;
        }
        return false;
    }

    /** Verifies the developer payload of a purchase. */
    public static boolean verifyDeveloperPayload(Context context, Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */
        if (FORCE_IAB_DEBUG) {
            //            new File(Environment.getExternalStorageDirectory().getPath() + "/borqs/apps/home3dhd/.purchasehistory");

            String sdStatus = Environment.getExternalStorageState();
            if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
                complain(context, "SD card is not avaiable/writeable right now.");
                return true;
            }
            try {
                String pathName = Environment.getExternalStorageDirectory().getPath() + "/borqs/apps/home3dhd/";
                String fileName = ".purchase_history";
                File path = new File(pathName);
                File file = new File(pathName + fileName);
                if (!path.exists()) {
                    Log.d("verifyDeveloperPayload", "Create the path:" + pathName);
                    path.mkdir();
                }
                if (!file.exists()) {
                    Log.d("verifyDeveloperPayload", "Create the file:" + fileName);
                    file.createNewFile();
                }
                FileOutputStream stream = new FileOutputStream(file);

                stream.write(payload.getBytes());
                stream.write(p.toString().getBytes());
                String s = DateUtils.formatDateTime(context, System.currentTimeMillis(),
                        DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME) + "\n";
                byte[] buf = s.getBytes();
                stream.write(buf);
                stream.close();

            } catch (Exception e) {
                Log.e("TestFile", "Error on writeFilToSD.");
                e.printStackTrace();
            }
        }
        return true;
    }
}
