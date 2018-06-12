package com.borqs.information.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;

public class NotificationProvider extends ContentProvider {
	
	public final static String DB_NAME = "notification.db";
	public final static int DB_VERSION = 4;
	public final static String INFORMATION_TABLE = "notification";
	
	private static SQLiteOpenHelper mOpenHelper;
	
	private static UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
	private final static int NOTIFICATIONS = 1;
	private final static int NOTIFICATION_ID = 2;
	private final static int NOTIFICATIONS_WIHOUT_NOTIFY = 3;
	private final static int NOTIFICATION_ID_WIHOUT_NOTIFY = 4;
	
	static {
		matcher.addURI(Notification.AUTHORITY, "notification", NOTIFICATIONS);
		matcher.addURI(Notification.AUTHORITY, "notification/#", NOTIFICATION_ID);
		matcher.addURI(Notification.AUTHORITY, "notification_without_notify", NOTIFICATIONS_WIHOUT_NOTIFY);
		matcher.addURI(Notification.AUTHORITY, "notification_without_notify/#", NOTIFICATION_ID_WIHOUT_NOTIFY);
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new InformationDBHelper(getContext());
		return true;
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		boolean needNotify = true;
		switch(matcher.match(uri)) {
		case NOTIFICATION_ID:
		case NOTIFICATION_ID_WIHOUT_NOTIFY:
			throw new IllegalArgumentException("Improper Uri: " + uri);
		case NOTIFICATIONS:
			break;
		case NOTIFICATIONS_WIHOUT_NOTIFY:
			needNotify = false;
			break;
		default:
			throw new IllegalArgumentException("Unkown Uri: " + uri);
		}
		long rowId = db.insert(INFORMATION_TABLE, Notification.NotificationColumns._ID, values);
		if(rowId > 0) {
			Uri inforUri = ContentUris.withAppendedId(uri, rowId);
			if(needNotify) {
				getContext().getContentResolver().notifyChange(inforUri, null);
			}
			return inforUri;
		}
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int num = 0;
		boolean needNotify = true;
		switch(matcher.match(uri)) {
		case NOTIFICATIONS:
			num = db.delete(INFORMATION_TABLE, selection, selectionArgs);
			break;
		case NOTIFICATION_ID:
			num = db.delete(INFORMATION_TABLE, appendIDWithSelection(uri, selection), selectionArgs);
			break;
		case NOTIFICATIONS_WIHOUT_NOTIFY:
			num = db.delete(INFORMATION_TABLE, selection, selectionArgs);
			needNotify = false;
			break;
		case NOTIFICATION_ID_WIHOUT_NOTIFY:
			num = db.delete(INFORMATION_TABLE, appendIDWithSelection(uri, selection), selectionArgs);
			needNotify = false;
			break;
		default:
			throw new IllegalArgumentException("Unkown Uri: " + uri);
		}
		if(needNotify) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return num;
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int num = 0;
		boolean needNotify = true;
		switch(matcher.match(uri)) {
		case NOTIFICATIONS:
			num = db.update(INFORMATION_TABLE, values, selection, selectionArgs);
			break;
		case NOTIFICATION_ID:
			num = db.update(INFORMATION_TABLE, values, appendIDWithSelection(uri, selection), selectionArgs);
			break;
		case NOTIFICATIONS_WIHOUT_NOTIFY:
			num = db.update(INFORMATION_TABLE, values, selection, selectionArgs);
			needNotify = false;
			break;
		case NOTIFICATION_ID_WIHOUT_NOTIFY:
			num = db.update(INFORMATION_TABLE, values, appendIDWithSelection(uri, selection), selectionArgs);
			needNotify = false;
			break;
		default:
			throw new IllegalArgumentException("Unkown Uri: " + uri);
		}
		if(needNotify) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return num;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		switch(matcher.match(uri)) {
		case NOTIFICATIONS:
		case NOTIFICATIONS_WIHOUT_NOTIFY:
			return db.query(INFORMATION_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
		case NOTIFICATION_ID:
		case NOTIFICATION_ID_WIHOUT_NOTIFY:
			return db.query(INFORMATION_TABLE, projection, appendIDWithSelection(uri, selection), selectionArgs, null, null, sortOrder);
		default:
			throw new IllegalArgumentException("Unkown Uri: " + uri);
		}
	}
	
	private String appendIDWithSelection(Uri uri, String selection) {
		long id = ContentUris.parseId(uri);
		String where = Notification.NotificationColumns.M_ID + "=" + id;
		if(!TextUtils.isEmpty(selection)) {
			where = where + " and " + selection;
		}
		return where;
	}
	
	@Override
	public String getType(Uri uri) {
		switch(matcher.match(uri)) {
		case NOTIFICATIONS:
			return "NOTIFICATIONS";
		case NOTIFICATION_ID:
			return "NOTIFICATION_ID";
		case NOTIFICATIONS_WIHOUT_NOTIFY:
			return "NOTIFICATIONS_WIHOUT_NOTIFY";
		case NOTIFICATION_ID_WIHOUT_NOTIFY:
			return "NOTIFICATION_ID_WIHOUT_NOTIFY";
		default:
			throw new IllegalArgumentException("Unkown Uri: " + uri);
		}
	}
	
	static class InformationDBHelper extends SQLiteOpenHelper {

		private final static String CREATE_TABLE_SQL = "create table "
				+ INFORMATION_TABLE + "(" 
				+ Notification.NotificationColumns._ID         + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ Notification.NotificationColumns.M_ID        + " LONG, "
				+ Notification.NotificationColumns.APP_ID      + " TEXT, "
				+ Notification.NotificationColumns.TYPE        + " TEXT, "
				+ Notification.NotificationColumns.IMAGE_URL   + " TEXT, "
				+ Notification.NotificationColumns.RECEIVER_ID + " TEXT, "
				+ Notification.NotificationColumns.SENDER_ID   + " TEXT, "
				+ Notification.NotificationColumns.DATE        + " LONG, "
				+ Notification.NotificationColumns.TITLE       + " TEXT, "
				+ Notification.NotificationColumns.URI         + " TEXT, "
				+ Notification.NotificationColumns.LAST_MODIFY + " LONG, "
				+ Notification.NotificationColumns.LONGPRESSURI        + " TEXT, "
				+ Notification.NotificationColumns.DATA        + " TEXT, "
				+ Notification.NotificationColumns.IS_READ     + " SHORT, "
				+ Notification.NotificationColumns.PROCESSED   + " SHORT DEFAULT 0, "
				+ Notification.NotificationColumns.BODY        + " TEXT, "
				+ Notification.NotificationColumns.BODY_HTML   + " TEXT, "
				+ Notification.NotificationColumns.TITLE_HTML  + " TEXT, "
				+ Notification.NotificationColumns.U_ID        + " LONG)";

		public InformationDBHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_TABLE_SQL);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
            if (oldVersion < 4) {
                dropTables(db);
                onCreate(db);
                return;
            }
            } catch (Exception e) {
                e.printStackTrace();
                dropTables(db);
                onCreate(db);
            }
		}

        private void dropTables(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + INFORMATION_TABLE);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                super.onDowngrade(db, oldVersion, newVersion);
            } catch (Exception e) {
                e.printStackTrace();
                dropTables(db);
                onCreate(db);
            }

            /**
             * When users install qiupu from high version to low version, we need to support.
             *  comments:
             *    1.drop all tables and re-create database.
             *    2.whether saving high version database's data or not.
             *    3.when do so, it will use the installing application's version to 
             *      decide how to create database and tables.
             */
        }

	}

}