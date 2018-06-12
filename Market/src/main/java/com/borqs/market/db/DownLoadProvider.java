package com.borqs.market.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.borqs.market.utils.MarketConfiguration;

public class DownLoadProvider extends ContentProvider {
    public static SQLiteOpenHelper mOpenHelper;
    public static final String DATABASE_NAME = "download.db";
    public static final int DATABASE_VERSION = 7;

    public static final String TABLE_DOWNLOAD = "download";
    public static final String TABLE_PLUGIN = "plugin";
    public static final String TABLE_ORDER = "order_info";

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(args.table);

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor result = qb.query(db, projection, args.where, args.args,
                args.groupby, null, sortOrder);
        // result.setNotificationUri(getContext().getContentResolver(), uri);
        return result;
    }

    @Override
    public String getType(Uri uri) {
        SqlArguments args = new SqlArguments(uri, null, null);
        if (TextUtils.isEmpty(args.where)) {
            return "vnd.android.cursor.dir/" + args.table;
        } else {
            return "vnd.android.cursor.item/" + args.table;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SqlArguments args = new SqlArguments(uri);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final long rowId = db.insert(args.table, null, values);
        if (rowId <= 0)
            return null;
        else {
             getContext().getContentResolver().notifyChange(uri, null);
        }
        uri = ContentUris.withAppendedId(uri, rowId);
        return uri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.delete(args.table, args.where, args.args);
        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.update(args.table, values, args.where, args.args);
        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public DatabaseHelper(Context context, String name,
                CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            createTableDownload(db);
            createTablePlugIn(db);
            createTableOrder(db);
        }

        public void createTableDownload(SQLiteDatabase db) {

            db.execSQL("CREATE TABLE " + TABLE_DOWNLOAD + " ("
                    + DownloadInfoColumns._ID + " INTEGER PRIMARY KEY,"
                    + DownloadInfoColumns.NAME + " TEXT NOT NULL,"
                    + DownloadInfoColumns.PRODUCT_ID + " TEXT NOT NULL,"
                    + DownloadInfoColumns.DOWNLOAD_ID + " TEXT NOT NULL,"
                    + DownloadInfoColumns.LOCAL_PATH + " TEXT,"
                    + DownloadInfoColumns.TYPE + " TEXT,"
                    + DownloadInfoColumns.VERSION_NAME + " TEXT,"
                    + DownloadInfoColumns.VERSION_CODE + " INTEGER DEFAULT 0,"
                    + DownloadInfoColumns.MD5_STRING + " TEXT,"
                    + DownloadInfoColumns.JSON_STR + " TEXT,"
                    + DownloadInfoColumns.SUPPORTED_MOD + " TEXT,"
                    + DownloadInfoColumns.DOWNLOAD_STATUS
                    + " INTEGER DEFAULT 0," + DownloadInfoColumns.SIZE
                    + " LONG NOT NULL DEFAULT 0 );");
        }
        public void createTablePlugIn(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_PLUGIN + " (" + PlugInColumns._ID
                    + " INTEGER PRIMARY KEY,"
                    + PlugInColumns.PRODUCT_ID + " TEXT,"
                    + PlugInColumns.NAME + " TEXT,"
                    + PlugInColumns.VERSION_CODE + " INTEGER,"
                    + PlugInColumns.VERSION_NAME + " TEXT,"
                    + PlugInColumns.TYPE + " TEXT,"
                    + PlugInColumns.LOCAL_JSON_STR + " TEXT,"
                    + PlugInColumns.JSON_STR + " TEXT,"
                    + PlugInColumns.SUPPORTED_MOD + " TEXT,"
                    + PlugInColumns.FILE_PATH + " TEXT,"
                    + PlugInColumns.IS_APPLY + " INTEGER  DEFAULT 0,"
                    + PlugInColumns.BELONG_SYSTEM + " INTEGER  DEFAULT 0,"
                    + PlugInColumns.UPDATE_TIME + " INTEGER );");
        }
        
        private void createTableOrder(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_ORDER + " (" + OrderInfoColumns._ID
                    + " INTEGER PRIMARY KEY,"
                    + OrderInfoColumns.PRODUCT_ID + " TEXT,"
                    + OrderInfoColumns.USER_ID + " TEXT,"
                    + OrderInfoColumns.PAY_CODE + " TEXT,"
                    + OrderInfoColumns.PAY_TYPE + " TEXT,"
                    + OrderInfoColumns.ITEM_TYPE + " TEXT,"
                    + OrderInfoColumns.JSON_PURCHASE_INFO + " TEXT,"
                    + OrderInfoColumns.SIGNATURE + " TEXT,"
                    + OrderInfoColumns.VERSION_CODE + " INTEGER,"
                    + OrderInfoColumns.IAB_ORDER_ID + " TEXT,"
                    + OrderInfoColumns.HAS_ORDERED + " INTEGER DEFAULT 0,"
                    + OrderInfoColumns.HAS_CONSUMED + " INTEGER DEFAULT 0 );");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                switch(oldVersion) {
                    case 1:
                    upgradeToVersion2(db);
                    case 2:
                        upgradeToVersion3(db);
                    case 3:
                        upgradeToVersion4(db);
                    case 4:
                        upgradeToVersion5(db);
                    case 5:
                        upgradeToVersion6(db);
                    case 6:
                        upgradeToVersion7(db);
                    default:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                dropTables(db);
                onCreate(db);
            }
        }
        
        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion,
                int newVersion) {
            dropTables(db);
            onCreate(db);
        }

        private void upgradeToVersion2(SQLiteDatabase db) {
            createTablePlugIn(db);
        }
        
        private void upgradeToVersion3(SQLiteDatabase db) {
            db.execSQL("ALTER TABLE " + TABLE_PLUGIN + " ADD COLUMN " + PlugInColumns.JSON_STR + " TEXT ;");
            db.execSQL("ALTER TABLE " + TABLE_PLUGIN + " ADD COLUMN " + PlugInColumns.LOCAL_JSON_STR + " TEXT ;");
            db.execSQL("ALTER TABLE " + TABLE_PLUGIN + " ADD COLUMN " + PlugInColumns.SUPPORTED_MOD + " TEXT ;");
            db.execSQL("ALTER TABLE " + TABLE_DOWNLOAD + " ADD COLUMN " + DownloadInfoColumns.JSON_STR + " TEXT ;");
            db.execSQL("ALTER TABLE " + TABLE_DOWNLOAD + " ADD COLUMN " + DownloadInfoColumns.SUPPORTED_MOD + " TEXT ;");
        }
        private void upgradeToVersion4(SQLiteDatabase db) {
            createTableOrder(db);
        }
        
        private void upgradeToVersion5(SQLiteDatabase db) {
            db.execSQL("ALTER TABLE " + TABLE_ORDER + " ADD COLUMN " + OrderInfoColumns.USER_ID + " TEXT ;");
        }
        
        private void upgradeToVersion6(SQLiteDatabase db) {
            db.execSQL("ALTER TABLE " + TABLE_PLUGIN + " ADD COLUMN " + PlugInColumns.FILE_PATH + " TEXT ;");
        }
        
        private void upgradeToVersion7(SQLiteDatabase db) {
            db.execSQL("ALTER TABLE " + TABLE_PLUGIN + " ADD COLUMN " + PlugInColumns.BELONG_SYSTEM + "  INTEGER DEFAULT 0 ;");
            db.execSQL("ALTER TABLE " + TABLE_PLUGIN + " ADD COLUMN " + PlugInColumns.UPDATE_TIME + "  INTEGER ;");
        }

        private void dropTables(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOWNLOAD);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLUGIN);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER);
        }
    }

    static class SqlArguments {
        public final String table;
        public final String where;
        public final String[] args;
        public String groupby = null;

        SqlArguments(Uri url, String where, String[] args) {
            if (url.getPathSegments().size() == 1) {
                this.table = url.getPathSegments().get(0);
                this.where = where;
                this.args = args;
            } else if (url.getPathSegments().size() != 2) {
                throw new IllegalArgumentException("Invalid URI: " + url);
            } else if (!TextUtils.isEmpty(where)) {
                throw new UnsupportedOperationException(
                        "WHERE clause not supported: " + url);
            } else {
                if (url.getPathSegments().size() == 2
                        && url.getPathSegments().get(1).equals("apkgroup")) {
                    this.table = url.getPathSegments().get(0);
                    this.where = where;
                    this.args = args;
                    // this.groupby = QiupuORM.ApkinfoGroupColumns.APKSTATUS;
                    return;
                }
                this.table = url.getPathSegments().get(0);
                this.where = "_id=" + ContentUris.parseId(url);
                this.args = null;

            }
        }

        SqlArguments(Uri url) {
            if (url.getPathSegments().size() == 1) {
                table = url.getPathSegments().get(0);
                where = null;
                args = null;
            } else {
                throw new IllegalArgumentException("Invalid URI: " + url);
            }
        }
    }
    
    private static final String AUTHORITIES_SUFFIX = ".download";
    
    /**
     * 用于返回指定表的URI
     * Provider中android:authorities定义需遵循以下格式
     * 应用packagename + ".download";
     * 
     * <provider android:name="com.borqs.market.db.DownLoadProvider"
                  android:exported="false"
                  android:authorities="com.borqs.freehdhome.download"/>
     */
    public static Uri getContentURI(Context ctx, String tableName) {
        String pkgName = null;
        if(TextUtils.isEmpty(MarketConfiguration.PACKAGE_NAME)) {
            pkgName = ctx.getPackageName();
        }else {
            pkgName = MarketConfiguration.PACKAGE_NAME;
        }
        StringBuilder sb = new StringBuilder("content://");
        sb.append(pkgName).append(AUTHORITIES_SUFFIX).append("/").append(tableName);
        return Uri.parse(sb.toString());
    }
}
