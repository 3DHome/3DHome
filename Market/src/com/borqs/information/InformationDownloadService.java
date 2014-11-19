package com.borqs.information;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.borqs.information.util.InformationConstant;
import com.borqs.market.utils.BLog;

public class InformationDownloadService extends IntentService {  
	private static final String TAG = InformationDownloadService.class.getSimpleName();
	private Context mContext;
	private IabManager iabManager = null;
    public InformationDownloadService() {  
        super(TAG);
    }  
    
  	@Override
	public void onCreate() {
		super.onCreate();
		BLog.v(TAG, "----------onCreate()-----------");
        mContext = this.getApplicationContext();
        iabManager = new IabManager(mContext,this);
	}
  	
  	@Override
  	public void onStart(Intent intent, int startId) {
  	  BLog.v(TAG, "----------onStart()-----------");
  	    super.onStart(intent, startId);
  	}
  	
	@Override
	protected void onHandleIntent(Intent intent) {
        int syncType = intent.getIntExtra(InformationConstant.NOTIFICATION_INTENT_SYNC_TYPE, -1);
        BLog.v(TAG, "----------onHandleIntent()-----------syncType= " + syncType);
		performSyncTask(intent, syncType);
	}

	private boolean performSyncTask(Intent intent, int syncType) {
		try {

            if (syncType == InformationConstant.NOTIFICATION_INTENT_PARAM_SYNC_TYPE_PURCHASE) {
                iabManager.queryPurchases();
            } else if (syncType == InformationConstant.NOTIFICATION_INTENT_PARAM_SYNC_TYPE_UPLOAD) {
                iabManager.batchOrder();
            } else if (syncType == InformationConstant.NOTIFICATION_INTENT_PARAM_SYNC_TYPE_CONSUME) {
                iabManager.consumeAsync();
            } else {
                Log.w(TAG, "onSyncTaskFinish, do nothing with unknown type = " + syncType);
                return true;
            }

		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Information download failed, logic exception.");
		} finally {
			Log.d(TAG, "Download information finished, type = " + syncType);
		}
		return false;
	}
	
	
}  