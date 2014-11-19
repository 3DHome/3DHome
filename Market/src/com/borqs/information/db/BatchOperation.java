package com.borqs.information.db;

import java.util.ArrayList;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.util.Log;

/**
 * This class handles execution of batch mOperations on Contacts provider.
 */
public class BatchOperation {
    private final String TAG = "BatchOperation";

    private final ContentResolver mResolver;
    ArrayList<ContentProviderOperation> mOperations;

    public BatchOperation(Context context) {
        mResolver = context.getContentResolver();
        mOperations = new ArrayList<ContentProviderOperation>();
    }

    public int size() {
        return mOperations.size();
    }

    public void add(ContentProviderOperation cpo) {
        mOperations.add(cpo);
    }

    public void execute(boolean needNotify) {
        if (mOperations.size() == 0) {
            return;
        }
        // Apply the mOperations to the content provider
        try {
            mResolver.applyBatch(Notification.AUTHORITY, mOperations);
            if(needNotify) {
            	mResolver.notifyChange(Notification.NotificationColumns.CONTENT_URI, null);
            }
        } catch (final OperationApplicationException e1) {
            Log.e(TAG, "storing contact data failed", e1);
        } catch (final RemoteException e2) {
            Log.e(TAG, "storing contact data failed", e2);
        }
        mOperations.clear();
    }

}
