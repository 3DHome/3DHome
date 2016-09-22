//package com.borqs.information.db;
//
//import android.content.ContentProviderOperation;
//import android.content.ContentResolver;
//import android.content.ContentUris;
//import android.content.ContentValues;
//import android.content.Context;
//import android.database.Cursor;
//import android.text.TextUtils;
//
//import com.borqs.information.InformationBase;
//import com.borqs.information.db.Notification.NotificationColumns;
//
//import java.util.HashSet;
//import java.util.List;
//
//public class NotificationOperator implements IOperator {
//
//    private ContentResolver mResolver;
//    private BatchOperation batchOperation;
//    private Context mContext;
//    private static final String TAG = "NotificationOperator";
//
//    public NotificationOperator(Context context) {
//        mContext = context;
//        mResolver = context.getContentResolver();
//        batchOperation = new BatchOperation(context);
//    }
//
//    @Override
//    public void add(InformationBase infor) {
//        ContentValues values = buildContentValues(infor);
//        mResolver.insert(NotificationColumns.CONTENT_URI, values);
//    }
//
//    @Override
//    public void delete(String where, String selectionArgs[]) {
//        mResolver.delete(NotificationColumns.CONTENT_URI, where, selectionArgs);
//    }
//
//    @Override
//    public boolean update(long id, ContentValues values) {
//        return mResolver.update(ContentUris.withAppendedId(NotificationColumns.CONTENT_URI, id), values, null, null) > 0;
//    }
//
//    @Override
//    public boolean update(long id, InformationBase infor) {
//        ContentValues values = buildContentValues(infor);
//        return mResolver.update(ContentUris.withAppendedId(NotificationColumns.CONTENT_URI, id), values, null, null) > 0;
//    }
//
//    @Override
//    public long getLastModifyDate() {
//        Cursor cursor = mResolver.query(NotificationColumns.CONTENT_URI, new String[]{NotificationColumns.LAST_MODIFY}, null, null, NotificationColumns.LAST_MODIFY + " desc");
//        long lastModifyDate = 0;
//        if (cursor.moveToNext()) {
//            lastModifyDate = cursor.getLong(0);
//        }
//        cursor.close();
//        return lastModifyDate;
//    }
//
//    @Override
//    public int getUnReadCount() {
//        Cursor cursor = mResolver.query(NotificationColumns.CONTENT_URI, null, NotificationColumns.IS_READ + "=? and " + NotificationColumns.PROCESSED + "=?", new String[]{"0", "0"}, null);
//        int count = 0;
//        if (cursor.moveToNext()) {
//            count = cursor.getCount();
//        }
//        cursor.close();
//        return count;
//    }
//
//    @Override
//    public int getThisWeekUnReadCount() {
//        long time = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L;
//        Cursor cursor = mResolver.query(NotificationColumns.CONTENT_URI, null, NotificationColumns.IS_READ + "=? and " + NotificationColumns.LAST_MODIFY + ">? and " + NotificationColumns.PROCESSED + "=?", new String[]{"0", Long.toString(time), "0"}, null);
//        int count = 0;
//        if (cursor.moveToNext()) {
//            count = cursor.getCount();
//        }
//        cursor.close();
//        return count;
//    }
//
//    public String getThisWeekUnReadInforIds() {
//        long time = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L;
//        String[] projectString = new String[]{NotificationColumns.M_ID};
//        Cursor cursor = mResolver.query(NotificationColumns.CONTENT_URI, projectString, NotificationColumns.IS_READ + "=? and " + NotificationColumns.LAST_MODIFY + ">? and " + NotificationColumns.PROCESSED + "=?", new String[]{"0", Long.toString(time), "0"}, null);
//        StringBuilder ids = new StringBuilder();
//        if (cursor != null) {
//            if (cursor.moveToFirst()) {
//                do {
//                    if (ids.length() > 0) {
//                        ids.append(",");
//                    }
//                    ids.append(cursor.getString(0));
//                } while (cursor.moveToNext());
//
//            }
//            cursor.close();
//            cursor = null;
//        }
//        return ids.toString();
//    }
//
//    private ContentValues buildContentValues(InformationBase infor) {
//        ContentValues values = new ContentValues();
//        values.put(NotificationColumns.M_ID, infor.id);
//        values.put(NotificationColumns.TYPE, infor.type);
//        values.put(NotificationColumns.APP_ID, infor.appId);
//        values.put(NotificationColumns.DATA, infor.data);
//        values.put(NotificationColumns.LONGPRESSURI, infor.apppickurl == null ? "" : infor.apppickurl.toString());
//        values.put(NotificationColumns.DATE, infor.date);
//        values.put(NotificationColumns.IMAGE_URL, infor.image_url);
//        values.put(NotificationColumns.IS_READ, infor.read);
//        values.put(NotificationColumns.PROCESSED, false);
//        values.put(NotificationColumns.LAST_MODIFY, infor.lastModified);
//        values.put(NotificationColumns.RECEIVER_ID, infor.receiverId);
//        values.put(NotificationColumns.SENDER_ID, infor.senderId);
//        values.put(NotificationColumns.TITLE, infor.title);
//        values.put(NotificationColumns.URI, infor.uri);
//        values.put(NotificationColumns.BODY, infor.body);
//        values.put(NotificationColumns.BODY_HTML, infor.body_html);
//        values.put(NotificationColumns.TITLE_HTML, infor.title_html);
//        return values;
//    }
//
//    @Override
//    public void add(List<InformationBase> items) {
//        HashSet<Long> mIdSet = getExistedItems();
//        for (InformationBase item : items) {
//            if (mIdSet.contains(item.id)) {
//                batchOperation.add(ContentProviderOperation
//                        .newUpdate(ContentUris.withAppendedId(NotificationColumns.CONTENT_URI_WITHOUT_NOTIFY, item.id))
//                        .withValues(buildContentValues(item)).build());
//            } else {
//                batchOperation.add(ContentProviderOperation
//                        .newInsert(NotificationColumns.CONTENT_URI_WITHOUT_NOTIFY)
//                        .withValues(buildContentValues(item)).build());
//            }
//            if (batchOperation.size() >= 100) {
//                batchOperation.execute(true);
//            }
//        }
//
//        batchOperation.execute(true);
//    }
//
//    @Override
//    public boolean updateReadStatus(long id, boolean status) {
//        ContentValues values = new ContentValues();
//        values.put(NotificationColumns.IS_READ, status);
//        return mResolver.update(ContentUris.withAppendedId(NotificationColumns.CONTENT_URI_WITHOUT_NOTIFY, id), values, null, null) > 0;
//    }
//
//    @Override
//    public boolean updateAllReadStatus(boolean status) {
//        ContentValues values = new ContentValues();
//        values.put(NotificationColumns.IS_READ, status);
//        return mResolver.update(NotificationColumns.CONTENT_URI_WITHOUT_NOTIFY, values, null, null) > 0;
//    }
//
//    @Override
//    public Cursor loadAll(String queryType) {
//        final String actualType = TextUtils.isEmpty(queryType) ? "" : " and " + queryType;
//        return mResolver.query(NotificationColumns.CONTENT_URI, null,
//                NotificationColumns.PROCESSED + "=?" + actualType,
//                new String[]{"0"},
//                NotificationColumns.LAST_MODIFY + " desc");
//    }
//
//    @Override
//    public Cursor loadThisWeek(String queryType) {
//        long time = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L;
//        final String actualType = TextUtils.isEmpty(queryType) ? "" : " and " + queryType;
//        return mResolver.query(NotificationColumns.CONTENT_URI, null,
//                NotificationColumns.LAST_MODIFY + ">? and " +
//                        NotificationColumns.PROCESSED + "=?" +
//                        actualType,
//                new String[]{
//                        Long.toString(time), "0"},
//                NotificationColumns.LAST_MODIFY + " desc");
//    }
//
//    private HashSet<Long> getExistedItems() {
//        Cursor cursor = mResolver.query(NotificationColumns.CONTENT_URI,
//                new String[]{NotificationColumns.M_ID}, null, null, null);
//        HashSet<Long> result = new HashSet<Long>();
//        if (cursor != null) {
//            try {
//                while (cursor.moveToNext()) {
//                    long m_id = cursor.getLong(0);
//                    result.add(m_id);
//                }
//            } finally {
//                cursor.close();
//            }
//        }
//        return result;
//    }
//
//    public HashSet<Long> getProcessedItems() {
//        Cursor cursor = mResolver.query(NotificationColumns.CONTENT_URI,
//                new String[]{NotificationColumns.M_ID}, NotificationColumns.PROCESSED + "=?", new String[]{"1"}, null);
//        HashSet<Long> result = new HashSet<Long>();
//        if (cursor != null) {
//            try {
//                while (cursor.moveToNext()) {
//                    long m_id = cursor.getLong(0);
//                    result.add(m_id);
//                }
//            } finally {
//                cursor.close();
//            }
//        }
//        return result;
//    }
//
//    public boolean updateReadStatusByUrl(String url, boolean status) {
//        synchronized (this) {
//            ContentValues values = new ContentValues();
//            values.put(NotificationColumns.IS_READ, status);
//            return mResolver.update(NotificationColumns.CONTENT_URI, values, NotificationColumns.URI + " like '" + url + "'", null) > 0;
//        }
//    }
//
//    public String getUnReadInformationWithUrl(String url) {
//        String where = NotificationColumns.URI + " like '" + url + "' and " + NotificationColumns.IS_READ + "=" + 0;
//        Cursor cursor = mResolver.query(NotificationColumns.CONTENT_URI,
//                new String[]{NotificationColumns.M_ID}, where, null, null);
//
//        StringBuilder inforIds = new StringBuilder();
//        if (cursor != null) {
//            if (cursor.moveToFirst()) {
//                do {
//                    if (inforIds.length() > 0) {
//                        inforIds.append(",");
//                    }
//                    inforIds.append(cursor.getString(0));
//
//                } while (cursor.moveToNext());
//            }
//        }
//        return inforIds.toString();
//    }
//}
