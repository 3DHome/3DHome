package com.borqs.se.home3d;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Xml;

import com.borqs.market.json.Product;
import com.borqs.market.utils.MarketUtils;
import com.borqs.market.utils.ThemeXmlParser;
import com.borqs.se.engine.SEComponentAttribute;
import com.borqs.se.engine.SEUtils;
import com.borqs.se.engine.SEVector.SEVector4f;
import com.borqs.se.home3d.ProviderUtils.AppsDrawerColumns;
import com.borqs.se.home3d.ProviderUtils.ModelColumns;
import com.borqs.se.home3d.ProviderUtils.ObjectInfoColumns;
import com.borqs.se.home3d.ProviderUtils.Tables;
import com.borqs.se.home3d.ProviderUtils.ThemeColumns;
import com.borqs.se.home3d.ProviderUtils.VesselColumns;

public class HomeDataBaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "SEHomeDataBaseHelper";
    private String mCurrentTheme;
    private String mDefaultTheme;
    private String mDefaultThemeID;
    private Context mContext;

    private static final String EVENTS_CLEANUP_TRIGGER_SQL_OBJECTS = "DELETE FROM " + Tables.VESSEL
            + " WHERE objectID=old._id;";

    private static final String EVENTS_CLEANUP_TRIGGER_SQL_MODELS_IMAGE = "DELETE FROM " + Tables.IMAGE_INFO
            + " WHERE _id=old._id;";
    private static final String EVENTS_CLEANUP_TRIGGER_SQL_MODELS_COMPONENT = "DELETE FROM " + Tables.COMPONENT_INFO
            + " WHERE _id=old._id;";

    private static final String EVENTS_CLEANUP_TRIGGER_SQL_THEME__BOUNDING_MODEL = "DELETE FROM " + Tables.MODEL_INFO
            + " WHERE boundingThemeName= old.name;";

    static HashMap<String, String> MODEL_XMLS = new HashMap<String, String>();
    static {
        MODEL_XMLS.put("group_airplane", "base/airplane");
        MODEL_XMLS.put("group_camera", "base/camera");
        MODEL_XMLS.put("group_clock", "base/clock");
        MODEL_XMLS.put("group_contact", "base/contact");
        MODEL_XMLS.put("group_email", "base/email");
        MODEL_XMLS.put("group_feiting", "base/feiting");
        MODEL_XMLS.put("group_globe", "base/globe");
        MODEL_XMLS.put("group_gplay", "base/gplay");
        MODEL_XMLS.put("group_iconbox", "base/iconbox");
        MODEL_XMLS.put("group_pc", "base/pc");
        MODEL_XMLS.put("group_ipad", "base/ipad");
        MODEL_XMLS.put("group_largewallpicframe", "base/picframe/horizontal_large");
        MODEL_XMLS.put("group_hengwallpicframe", "base/picframe/horizontal_small");
        MODEL_XMLS.put("group_s3", "base/s3");
        MODEL_XMLS.put("group_sky", "base/sky");
        MODEL_XMLS.put("group_sms", "base/sms");
        MODEL_XMLS.put("group_tv", "base/tv");
        MODEL_XMLS.put("group_appwall", "base/appwall");
        MODEL_XMLS.put("woodfolderopen", "base/woodfolderopen");
        MODEL_XMLS.put("recycle", "base/recycle");
        MODEL_XMLS.put("fangdajing", "base/fangdajing");
        MODEL_XMLS.put("desk_fang", "base/desk_fang");
        MODEL_XMLS.put("group_desk_2", "base/desk_2");
        MODEL_XMLS.put("desk_fang_land", "base/desk_fang_land");
        MODEL_XMLS.put("shelf", "base/shelf");
    }

    static HashMap<String, String> THEME_XMLS = new HashMap<String, String>();
    static {
        THEME_XMLS.put("lighten", "base/lightenTexture");
        THEME_XMLS.put("wood", "base/woodTexture");
        THEME_XMLS.put("landscape", "base/landscapeTexture");
    }

    private HomeDataBaseHelper(Context context) {
        super(context, ProviderUtils.DATABASE_NAME, null, ProviderUtils.DATABASE_VERSION);
        mContext = context;
        int screenSize = mContext.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        boolean isScreenLarge = screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE
                || screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE;
        if (isScreenLarge) {
            mDefaultTheme = "landscape";
        } else {
            mDefaultTheme = "lighten";
        }
        mDefaultThemeID = HomeUtils.getDefaultThemeId(mDefaultTheme);
    }

    private static HomeDataBaseHelper mSEHomeDataBaseHelper;

    public static HomeDataBaseHelper getInstance(Context context) {
        if (mSEHomeDataBaseHelper == null) {
            mSEHomeDataBaseHelper = new HomeDataBaseHelper(context);
        }
        return mSEHomeDataBaseHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {   

        // ////////////////////////////////////////////////////////////////////////////////////////begin
        db.execSQL("CREATE TABLE " + ProviderUtils.Tables.OBJECTS_INFO + " (" 
                + ObjectInfoColumns.OBJECT_ID + " INTEGER PRIMARY KEY,"
                + ObjectInfoColumns.OBJECT_NAME + " TEXT NOT NULL,"
                + ObjectInfoColumns.OBJECT_TYPE + " TEXT,"
                + ObjectInfoColumns.SHADER_TYPE + " INTEGER NOT NULL DEFAULT 0,"
                + ObjectInfoColumns.SCENE_NAME + " TEXT,"
                + ObjectInfoColumns.COMPONENT_NAME + " TEXT,"
                + ObjectInfoColumns.SLOT_TYPE + " INTEGER NOT NULL DEFAULT 0,"
                + ObjectInfoColumns.CLASS_NAME + " TEXT,"
                + ObjectInfoColumns.SHORTCUT_ICON + " BLOB,"
                + ObjectInfoColumns.SHORTCUT_URL + " TEXT,"
                + ObjectInfoColumns.IS_NATIVE_OBJ + " INTEGER NOT NULL DEFAULT 0,"
                + ObjectInfoColumns.WIDGET_ID + " INTEGER NOT NULL DEFAULT -1,"
                + ObjectInfoColumns.OBJECT_INDEX + " INTEGER NOT NULL DEFAULT 0,"
                + ObjectInfoColumns.DISPLAY_NAME + " TEXT,"
                + ObjectInfoColumns.FACE_COLOR + " INTEGER NOT NULL DEFAULT -1,"
                + ObjectInfoColumns.FACE_IMAGE_PATH + " TEXT" + ");");

        db.execSQL("CREATE TABLE " + ProviderUtils.Tables.VESSEL + " (" + VesselColumns.VESSEL_ID + " INTEGER,"
                + VesselColumns.OBJECT_ID + " INTEGER," + VesselColumns.SLOT_INDEX + " INTEGER NOT NULL DEFAULT 0,"
                + VesselColumns.SLOT_StartX + " INTEGER NOT NULL DEFAULT 0," + VesselColumns.SLOT_StartY
                + " INTEGER NOT NULL DEFAULT 0," + VesselColumns.SLOT_SpanX + " INTEGER NOT NULL DEFAULT 0,"
                + VesselColumns.SLOT_SpanY + " INTEGER NOT NULL DEFAULT 0" + ");");
        
        // Trigger to remove data tied to an event when we delete that event.
        db.execSQL("CREATE TRIGGER objects_cleanup_delete DELETE ON " + ProviderUtils.Tables.OBJECTS_INFO + " "
                + "BEGIN " + EVENTS_CLEANUP_TRIGGER_SQL_OBJECTS + "END");
        
        // ////////////////////////////////////////////////////////////////////////////////////////////////////
        db.execSQL("CREATE TABLE " + ProviderUtils.Tables.MODEL_INFO + " ("
                + ModelColumns._ID + " INTEGER PRIMARY KEY," 
                + ModelColumns.OBJECT_NAME + " TEXT NOT NULL," 
                + ModelColumns.BOUNDING_THEME_NAME + " TEXT,"
                + ModelColumns.ACTION_STATUS + " INTEGER NOT NULL DEFAULT 4," 
                + ModelColumns.COMPONENT_NAME + " TEXT,"
                + ModelColumns.TYPE + " TEXT NOT NULL,"
                + ModelColumns.SCENE_FILE + " TEXT,"
                + ModelColumns.BASEDATA_FILE + " TEXT," 
                + ModelColumns.ASSETS_PATH + " TEXT,"
                + ModelColumns.LOCAL_TRANS + " TEXT,"
                + ModelColumns.KEY_WORDS + " TEXT,"
                + ModelColumns.SLOT_TYPE + " INTEGER NOT NULL DEFAULT 0,"
                + ModelColumns.SLOT_SPANX + " INTEGER NOT NULL DEFAULT 0,"
                + ModelColumns.SLOT_SPANY + " INTEGER NOT NULL DEFAULT 0,"
                + ModelColumns.MIN_POINT + " TEXT,"
                + ModelColumns.MAX_POINT + " TEXT,"
                + ModelColumns.PRODUCT_ID + " TEXT," 
                + ModelColumns.IS_DOWNLOADED + " INTEGER NOT NULL DEFAULT 0"+ ");");

        db.execSQL("CREATE TABLE " + ProviderUtils.Tables.IMAGE_INFO + " (" + "image_id INTEGER PRIMARY KEY,"
                + ModelColumns._ID + " INTEGER," + ModelColumns.IMAGE_NAME + " TEXT," + ModelColumns.IMAGE_PATH
                + " TEXT," + ModelColumns.IMAGE_NEW_PATH + " TEXT" + ");");

        db.execSQL("CREATE TABLE " + ProviderUtils.Tables.COMPONENT_INFO + " (" + "component_id INTEGER PRIMARY KEY,"
                + ModelColumns._ID + " INTEGER," + ModelColumns.COMPONENT_OBJECT + " TEXT,"
                + ModelColumns.COMPONENT_REGULAR_NAME + " TEXT," + ModelColumns.LIGHT_NAMES + " TEXT,"
                + ModelColumns.STATUS_VALUE + " INTEGER NOT NULL DEFAULT 0," + ModelColumns.SPATIAL_DATA_VALUE
                + " INTEGER NOT NULL DEFAULT 15," + ModelColumns.ALPHA_VALUE + " FLOAT NOT NULL DEFAULT 1,"
                + ModelColumns.EFFECT_DATA + " TEXT" + ");");

        db.execSQL("CREATE TRIGGER models_cleanup_delete DELETE ON " + ProviderUtils.Tables.MODEL_INFO + " " + "BEGIN "
                + EVENTS_CLEANUP_TRIGGER_SQL_MODELS_IMAGE + "END");
        db.execSQL("CREATE TRIGGER models_cleanup_delete_component DELETE ON " + ProviderUtils.Tables.MODEL_INFO + " " + "BEGIN "
                + EVENTS_CLEANUP_TRIGGER_SQL_MODELS_COMPONENT + "END");
        
        // ////////////////////////////////////////////////////////////////////////////////////////////////////
        db.execSQL("CREATE TABLE " + ProviderUtils.Tables.THEME + " (" + ThemeColumns._ID + " INTEGER PRIMARY KEY,"
                + ThemeColumns.NAME + " TEXT NOT NULL,"
                + ThemeColumns.SCENE_NAME + " TEXT NOT NULL DEFAULT home8,"
                + ThemeColumns.FILE_PATH + " TEXT,"
                + ThemeColumns.IS_DOWNLOADED + " INTEGER DEFAULT 0," 
                + ThemeColumns.IS_APPLY + " INTEGER DEFAULT 0,"
                + ThemeColumns.CONFIG + " TEXT,"
                + ThemeColumns.PRODUCT_ID + " TEXT,"
                + ThemeColumns.HOUSE_NAME + " TEXT,"
                + ThemeColumns.CAMERA_LOCATION + " TEXT,"
                + ThemeColumns.CAMERA_FOV + " FLOAT  NOT NULL DEFAULT 30,"
                + ThemeColumns.WALL_INDEX + " INTEGER NOT NULL DEFAULT 0,"
                + " UNIQUE (" + ThemeColumns.NAME + ")" + ");");
        db.execSQL("CREATE TRIGGER theme_cleanup_bounding_model_info DELETE ON " + ProviderUtils.Tables.THEME + " " + "BEGIN "
                + EVENTS_CLEANUP_TRIGGER_SQL_THEME__BOUNDING_MODEL + "END"); 
        // ////////////////////////////////////////////////////////////////////////////////////////////////////
        createAppsDrawer(db);
        mCurrentTheme = mDefaultTheme;
        loadDefaultData(db);
        moveAssetToExternal();
    }

    public String getDefaultThemeID() {
        return mDefaultThemeID;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 70) {
            onDowngrade(db, oldVersion, newVersion);
            return;
        }
        switch (oldVersion) {
        case 70:
            upgradeDatabaseToVersion71(db);
        case 71:
            if (!upgradeDatabaseToVersion72(db)) {
                onDowngrade(db, oldVersion, newVersion);
                return;
            }
        case 72:
            upgradeDatabaseToVersion73(db);
        case 73:
            upgradeDatabaseToVersion74(db);
        case 74:
            upgradeDatabaseToVersion75(db);
        case 75:
            upgradeDatabaseToVersion76(db);
        case 76:
            upgradeDatabaseToVersion77(db);
        case 77:
            upgradeDatabaseToVersion78(db);
        case 78:
            upgradeDatabaseToVersion79(db);
        case 79:
            upgradeDatabaseToVersion80(db);
        case 81:
            upgradeDatabaseToVersion82(db);
        case 82:
            upgradeDatabaseToVersion83(db);
        case 83:
            upgradeDatabaseToVersion84(db);
        case 84:
            upgradeDatabaseToVersion85(db);
        case 85:
            upgradeDatabaseToVersion86(db);
        case 86:
            upgradeDatabaseToVersion87(db);
        case 87:
            upgradeDatabaseToVersion88(db);
        case 88:
            upgradeDatabaseToVersion89(db);
        case 89:
            upgradeDatabaseToVersion90(db);
        case 90:
            upgradeDatabaseToVersion91(db);
        case 91:
            upgradeDatabaseToVersion92(db);
        case 92:
            upgradeDatabaseToVersion93(db);
        case 93:
            upgradeDatabaseToVersion94(db);
        case 94:
            upgradeDatabaseToVersion95(db);
        case 95:
            upgradeDatabaseToVersion96(db);
        case 96:
            if (!upgradeDatabaseToVersion97(db)) {
                onDowngrade(db, oldVersion, newVersion);
                return;
            }
        default:
            break;
        }
        loadDefaultDataAfterUpgraded(db);
        moveAssetToExternal();
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Scene_Config");
        db.execSQL("DROP TABLE IF EXISTS Camrea_data");
        db.execSQL("DROP TABLE IF EXISTS Objects_Config");
        db.execSQL("DROP TABLE IF EXISTS Desk_Slot");
        db.execSQL("DROP TABLE IF EXISTS Wall_Slot");
        db.execSQL("DROP TABLE IF EXISTS Wall_Gap_Slot");
        db.execSQL("DROP TABLE IF EXISTS model_info");
        db.execSQL("DROP TABLE IF EXISTS image_info");
        db.execSQL("DROP TABLE IF EXISTS file_url_info");
        db.execSQL("DROP TABLE IF EXISTS theme");
        db.execSQL("DROP TABLE IF EXISTS Vessel");
        db.execSQL("DROP TABLE IF EXISTS appdrawer");
        db.execSQL("DROP TABLE IF EXISTS component_info");
        db.execSQL("DROP TABLE IF EXISTS Camrea_data");
        db.execSQL("DROP TABLE IF EXISTS camrea_info");
        db.execSQL("DROP TABLE IF EXISTS house_info");
        new Thread() {
            public void run() {
                HomeUtils.deleteFile(mContext.getFilesDir().getAbsolutePath());
                HomeUtils.deleteFile(HomeUtils.get3DHomePath());
            }
        }.start();
        onCreate(db);
    }

       
    private void upgradeDatabaseToVersion71(SQLiteDatabase db) {
        try {
            db.execSQL("DROP TABLE IF EXISTS Scene_Config"); 
            String where = "_id>0";
            db.delete("Camrea_data", where, null);
        } catch (Throwable ex) {
            Log.e(TAG, ex.getMessage(), ex);
        } finally {
        }
    }
    
    private boolean upgradeDatabaseToVersion72(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE " + Tables.COMPONENT_INFO + " ADD " + ModelColumns.SPATIAL_DATA_VALUE
                    + " INTEGER NOT NULL DEFAULT 15;");
            /**
             * 升到该版本做了如下工作： 第一：桌子以前不允许克隆，在房间中的桌子objectIndex字段为0.
             * 升级后桌子假如有实例保留一份objectIndex==0的实例到ModelObjectManager,对应房间为该实例克隆出来的
             * objectIndex==1 objectIndex==0的实例 可以放到ObjectsMenu中显示
             * 
             * 第二：为了和新的桌子slot对应起来，调整了下桌子上物体的slotIndex值：
             * 如在圆桌的最右端物体在桌子换成方桌后仍然摆在方桌的最右端
             */
            String where = ObjectInfoColumns.OBJECT_NAME + "='group_desk'";
            ContentValues values = new ContentValues();
            values.put(ObjectInfoColumns.OBJECT_INDEX, 1);
            db.update(Tables.OBJECTS_INFO, values, where, null);

            Cursor cursor = db.query(Tables.OBJECTS_INFO, new String[] { ObjectInfoColumns.OBJECT_ID }, where, null,
                    null, null, null);
            int groupDeskID = -1;
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    groupDeskID = cursor.getInt(0);
                }
                cursor.close();
            }
            if (groupDeskID != -1) {
                where = VesselColumns.VESSEL_ID + "=" + groupDeskID;
                cursor = db.query(Tables.VESSEL, new String[] { VesselColumns.OBJECT_ID, VesselColumns.SLOT_INDEX },
                        where, null, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        int objectID = cursor.getInt(0);
                        int slotIndex = cursor.getInt(1);
                        where = VesselColumns.OBJECT_ID + "=" + objectID;
                        values = new ContentValues();
                        int newSlotIndex = 0;
                        if (slotIndex == -1) {
                            newSlotIndex = 0;
                        } else if (slotIndex == 0) {
                            newSlotIndex = 5;
                        } else if (slotIndex == 1) {
                            newSlotIndex = 4;
                        } else if (slotIndex == 2) {
                            newSlotIndex = 2;
                        } else if (slotIndex == 3) {
                            newSlotIndex = 1;
                        } else if (slotIndex == 4) {
                            newSlotIndex = 3;
                        } else if (slotIndex == 5) {
                            newSlotIndex = 6;
                        }
                        values.put(VesselColumns.SLOT_INDEX, newSlotIndex);
                        db.update(Tables.VESSEL, values, where, null);
                    }
                    cursor.close();
                }
            }
        } catch (Throwable ex) {
            Log.e(TAG, ex.getMessage(), ex);
            return false;
        } finally {

        }
        return true;
    }

    private void upgradeDatabaseToVersion73(SQLiteDatabase db) {
        try {
            String where = "name='woodfolderopen'";
            db.delete(Tables.MODEL_INFO, where, null);
        } catch (Throwable ex) {
            Log.e(TAG, ex.getMessage(), ex);
        } finally {
        }
    }

    /**
     * 由于childNames字段去除了，需要更新使用了这个字段的模型的配置：
     * 特殊物体如时钟的时针等，分针，秒针等更新后通过ComponentAttribute的regularName字段查找
     * 
     * 需要更新的模型有书架，时钟，笔记本，相框，日历，电视， 飞艇，飞机等
     */
    private void upgradeDatabaseToVersion74(SQLiteDatabase db) {
        try {
            // slotType=AppWall 去掉 修改成 slotType = wall
            String where = ModelColumns.SLOT_TYPE + "=4";
            ContentValues values = new ContentValues();
            values.put(ModelColumns.SLOT_TYPE, 1);
            db.update(Tables.MODEL_INFO, values, where, null);
            
            values = new ContentValues();
            where = ObjectInfoColumns.SLOT_TYPE + "=4";
            values.put(ObjectInfoColumns.SLOT_TYPE, 1);
            db.update(Tables.OBJECTS_INFO, values, where, null);
            
            where = ModelColumns.TYPE + "='app_wall'";
            values = new ContentValues();
            values.put(ModelColumns.TYPE, "AppWall");
            db.update(Tables.MODEL_INFO, values, where, null);
            
            where = ObjectInfoColumns.OBJECT_NAME + "='group_ipad' or " + ObjectInfoColumns.OBJECT_NAME + "='group_pc'";
            values = new ContentValues();
            values.put(ObjectInfoColumns.OBJECT_INDEX, 1);
            db.update(Tables.OBJECTS_INFO, values, where, null);
            
            SEComponentAttribute componentAttribute = new SEComponentAttribute();
            componentAttribute.mAlphaValue = 1;
            componentAttribute.mEffectData = new SEVector4f();
            componentAttribute.mSpatialDataValue = 0;
            componentAttribute.mStatusValue = 0;
            where = ModelColumns.TYPE + "='Bookshelf'";
            Cursor cursor = db.query(Tables.MODEL_INFO, new String[] { ModelColumns._ID, "childNames" }, where, null,
                    null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(0);
                    String[] childNames = cursor.getString(1).split(ProviderUtils.SPLIT_SYMBOL);
                    for (String book : childNames) {
                        componentAttribute.mRegularName = "book";
                        componentAttribute.mComponentName = book;
                        savecomponentAttributeToDB73(db, id, componentAttribute);
                    }
                }
                cursor.close();
            }

            where = ModelColumns.TYPE + "='Clock'";
            cursor = db.query(Tables.MODEL_INFO, new String[] { ModelColumns._ID, "childNames" }, where, null, null,
                    null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(0);
                    String[] childNames = cursor.getString(1).split(ProviderUtils.SPLIT_SYMBOL);

                    componentAttribute.mRegularName = "hour_hand";
                    componentAttribute.mComponentName = childNames[0];
                    savecomponentAttributeToDB73(db, id, componentAttribute);

                    componentAttribute.mRegularName = "min_hand";
                    componentAttribute.mComponentName = childNames[1];
                    savecomponentAttributeToDB73(db, id, componentAttribute);

                    componentAttribute.mRegularName = "sec_hand";
                    componentAttribute.mComponentName = childNames[2];
                    savecomponentAttributeToDB73(db, id, componentAttribute);

                    componentAttribute.mRegularName = "dial_pad";
                    componentAttribute.mComponentName = childNames[3];
                    savecomponentAttributeToDB73(db, id, componentAttribute);
                }
                cursor.close();
            }

            where = ModelColumns.TYPE + "='Calendary'";
            cursor = db.query(Tables.MODEL_INFO, new String[] { ModelColumns._ID, "childNames" }, where, null, null,
                    null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(0);
                    String[] childNames = cursor.getString(1).split(ProviderUtils.SPLIT_SYMBOL);
                    componentAttribute.mRegularName = "image_face";
                    componentAttribute.mComponentName = childNames[0];
                    savecomponentAttributeToDB73(db, id, componentAttribute);
                }
                cursor.close();
            }
            where = ModelColumns.TYPE + "='TV'";
            db.delete(Tables.MODEL_INFO, where, null);
            where = ModelColumns.TYPE + "='Airship'";
            db.delete(Tables.MODEL_INFO, where, null);
            where = ModelColumns.TYPE + "='Laptop'";
            db.delete(Tables.MODEL_INFO, where, null);
            where = ModelColumns.TYPE + "='VerticalWallFrame'";
            db.delete(Tables.MODEL_INFO, where, null);
            where = ModelColumns.TYPE + "='HorizontalWallFrame'";
            db.delete(Tables.MODEL_INFO, where, null);
            where = ModelColumns.TYPE + "='TableFrame'";
            db.delete(Tables.MODEL_INFO, where, null);
            where = ModelColumns.TYPE + "='Folder'";
            db.delete(Tables.MODEL_INFO, where, null);
        } catch (Throwable ex) {
            Log.e(TAG, ex.getMessage(), ex);
        } finally {

        }
    }

    private void upgradeDatabaseToVersion75(SQLiteDatabase db) {
        try {
            String where = ObjectInfoColumns.OBJECT_NAME + "='group_hengwallpicframe' OR "
                    + ObjectInfoColumns.OBJECT_NAME + "='group_largewallpicframe'";
            ContentValues values = new ContentValues();
            values.put(ObjectInfoColumns.OBJECT_TYPE, "PictureFrame");
            values.putNull(ObjectInfoColumns.CLASS_NAME);
            db.update(Tables.OBJECTS_INFO, values, where, null);
            db.delete(Tables.MODEL_INFO, ModelColumns.TYPE + "='Airship'", null);
        } catch (Throwable ex) {
            Log.e(TAG, ex.getMessage(), ex);
        } finally {

        }
    }

    private static final String EVENTS_CLEANUP_TRIGGER_SQL_THEME_CAMERA = "DELETE FROM " + Tables.CAMERA_INFO
            + " WHERE _id= old._id;";
    private static final String EVENTS_CLEANUP_TRIGGER_SQL_THEME_HOUSE = "DELETE FROM " + Tables.HOUSE_INFO
            + " WHERE _id= old._id;";
    private static final String EVENTS_CLEANUP_TRIGGER_SQL_HOUSE_MODEL = "DELETE FROM " + Tables.MODEL_INFO
            + " WHERE name= old.house_name;";

    private void upgradeDatabaseToVersion76(SQLiteDatabase db) {
        try {
            // 在上个版本中把PC修改成可以克隆且可以移动的物体时，应该修改之前版本的PC物体的Index：
            // 应该把Index为0的实例保留在ModelObjectManager中，供克隆使用
            String where = "(" + ObjectInfoColumns.OBJECT_NAME + "='group_ipad' OR " + ObjectInfoColumns.OBJECT_NAME
                    + "='group_pc')" + " AND " + ObjectInfoColumns.OBJECT_INDEX + "=0";
            ContentValues values = new ContentValues();
            values.put(ObjectInfoColumns.OBJECT_INDEX, 10000);
            db.update(Tables.OBJECTS_INFO, values, where, null);
            float oldCameraFov = 34;
            String oldCameraLoc = "0,-853,460";
            Cursor cursor = db.query("Camrea_data", new String[] { "fov", "location" }, null, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    oldCameraFov =  cursor.getFloat(0);
                    oldCameraLoc = cursor.getString(1);
                }
                cursor.close();
            }
            db.execSQL("DROP TABLE IF EXISTS Scene_Config");
            db.execSQL("DROP TABLE IF EXISTS Camrea_data");
            db.execSQL("DROP TABLE IF EXISTS camrea_info");
            db.execSQL("DROP TABLE IF EXISTS house_info");
            
            db.execSQL("CREATE TABLE " + ProviderUtils.Tables.HOUSE_INFO + " (" +"house_id INTEGER PRIMARY KEY,"
                    + ThemeColumns._ID+ " INTEGER," 
                    + ThemeColumns.HOUSE_NAME + " TEXT NOT NULL,"
                    + ThemeColumns.SKY_RADIUS + " FLOAT NOT NULL DEFAULT 750," 
                    + ThemeColumns.WALL_INDEX + " INTEGER NOT NULL DEFAULT 0,"
                    + ThemeColumns.WALL_PADDINGTOP + " FLOAT NOT NULL DEFAULT 0,"
                    + ThemeColumns.WALL_PADDINGBOTTOM + " FLOAT NOT NULL DEFAULT 45,"
                    + ThemeColumns.WALL_PADDINGLEFT + " FLOAT NOT NULL DEFAULT 0,"
                    + ThemeColumns.WALL_PADDINGRIGHT + " FLOAT NOT NULL DEFAULT 0,"
                    + ThemeColumns.WALL_NUM + " INTEGER NOT NULL DEFAULT 8,"
                    + ThemeColumns.WALL_RADIUS + " FLOAT NOT NULL DEFAULT 1000,"
                    + ThemeColumns.WALL_SPANX + " INTEGER NOT NULL DEFAULT 4,"
                    + ThemeColumns.WALL_SPANY + " INTEGER NOT NULL DEFAULT 4,"
                    + ThemeColumns.CELL_WIDTH + " FLOAT NOT NULL DEFAULT 189," 
                    + ThemeColumns.CELL_HEIGHT + " FLOAT NOT NULL DEFAULT 231," 
                    + ThemeColumns.CELL_GAP_WIDTH + " FLOAT NOT NULL DEFAULT 4,"
                    + ThemeColumns.CELL_GAP_HEIGHT + " FLOAT NOT NULL DEFAULT 15" + ");");
            
            db.execSQL("CREATE TABLE " + ProviderUtils.Tables.CAMERA_INFO + " (" + "camera_id INTEGER PRIMARY KEY,"
                    + ThemeColumns._ID + " INTEGER," 
                    + ThemeColumns.FOV + " FLOAT NOT NULL DEFAULT 60," 
                    + "old_fov" + " FLOAT NOT NULL DEFAULT 60," 
                    + ThemeColumns.NEAR + " FLOAT NOT NULL DEFAULT 1,"
                    + ThemeColumns.FAR + " FLOAT NOT NULL DEFAULT 2000,"
                    + ThemeColumns.LOCATION + " TEXT NOT NULL,"
                    + "old_location" + " TEXT NOT NULL,"
                    + ThemeColumns.ZAXIS + " TEXT NOT NULL,"
                    + ThemeColumns.UP  + " TEXT NOT NULL" + ");");
            
            db.execSQL("ALTER TABLE theme RENAME TO __temp__theme;");
            db.execSQL("CREATE TABLE " + ProviderUtils.Tables.THEME + " (" + ThemeColumns._ID + " INTEGER PRIMARY KEY,"
                    + ThemeColumns.NAME + " TEXT NOT NULL,"
                    + ThemeColumns.SCENE_NAME + " TEXT NOT NULL DEFAULT home8,"
                    + ThemeColumns.FILE_PATH + " TEXT,"
                    + ThemeColumns.IS_DOWNLOADED + " INTEGER DEFAULT 0," 
                    + ThemeColumns.IS_APPLY + " INTEGER DEFAULT 0,"
                    + ThemeColumns.CONFIG + " TEXT,"
                    + ThemeColumns.PRODUCT_ID + " TEXT," + " UNIQUE (" + ThemeColumns.NAME
                    + ")" + ");");
            db.execSQL("INSERT INTO theme(name, file_path, is_downloaded, is_apply, config, product_id)"
                    +" SELECT name, file_path, is_downloaded, is_apply, config, product_id FROM __temp__theme;");
            db.execSQL("DROP TABLE __temp__theme;"); 
            
            db.execSQL("CREATE TRIGGER theme_cleanup_camera DELETE ON " + ProviderUtils.Tables.THEME + " " + "BEGIN "
                    + EVENTS_CLEANUP_TRIGGER_SQL_THEME_CAMERA + "END");
            db.execSQL("CREATE TRIGGER theme_cleanup_house_info DELETE ON " + ProviderUtils.Tables.THEME + " " + "BEGIN "
                    + EVENTS_CLEANUP_TRIGGER_SQL_THEME_HOUSE + "END");        
            db.execSQL("CREATE TRIGGER theme_cleanup_house_model_info DELETE ON " + ProviderUtils.Tables.HOUSE_INFO + " " + "BEGIN "
                    + EVENTS_CLEANUP_TRIGGER_SQL_HOUSE_MODEL + "END");

            cursor = db
                    .query(Tables.THEME, new String[] { ThemeColumns._ID,  ThemeColumns.NAME}, null, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(0);
                    String themeName = cursor.getString(1);
                    values = new ContentValues();
                    values.put(ThemeColumns._ID, id);
                    values.put(ThemeColumns.HOUSE_NAME, "house_" + themeName + "_theme");
                    values.put(ThemeColumns.SKY_RADIUS, 488);
                    values.put(ThemeColumns.WALL_PADDINGTOP, 0);
                    values.put(ThemeColumns.WALL_PADDINGBOTTOM, 45);
                    values.put(ThemeColumns.WALL_PADDINGLEFT, 0);
                    values.put(ThemeColumns.WALL_PADDINGRIGHT, 0);
                    values.put(ThemeColumns.WALL_NUM, 8);
                    values.put(ThemeColumns.WALL_RADIUS, 1000);
                    values.put(ThemeColumns.WALL_SPANX, 4);
                    values.put(ThemeColumns.WALL_SPANY, 4);
                    values.put(ThemeColumns.CELL_WIDTH, 191);
                    values.put(ThemeColumns.CELL_HEIGHT, 231);
                    values.put(ThemeColumns.CELL_GAP_WIDTH, 6);
                    values.put(ThemeColumns.CELL_GAP_HEIGHT, 15);
                    values.put(ThemeColumns.WALL_INDEX, 0);
                    db.insert(Tables.HOUSE_INFO, null, values);

                    values = new ContentValues();
                    values.put(ThemeColumns._ID, id);
                    values.put(ThemeColumns.FOV, oldCameraFov);
                    values.put("old_fov", 34);
                    values.put(ThemeColumns.NEAR, 10);
                    values.put(ThemeColumns.FAR, 5000);
                    values.put(ThemeColumns.LOCATION, oldCameraLoc);
                    values.put("old_location", "0,-853,460");
                    values.put(ThemeColumns.ZAXIS, "0,-1,0");
                    values.put(ThemeColumns.UP, "0, 0, 1");
                    db.insert(Tables.CAMERA_INFO, null, values);
                }
                cursor.close();
            }

        } catch (Throwable ex) {
            Log.e(TAG, ex.getMessage(), ex);
        } finally {

        }
    }       

    private void upgradeDatabaseToVersion77(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE " + Tables.MODEL_INFO + " ADD " + ModelColumns.MIN_POINT + " TEXT;");
            db.execSQL("ALTER TABLE " + Tables.MODEL_INFO + " ADD " + ModelColumns.MAX_POINT + " TEXT;");
            String where = ModelColumns.OBJECT_NAME + "='group_desk'";
            ContentValues values = new ContentValues();
            values.put(ModelColumns.MIN_POINT, "-240, -240, 0");
            values.put(ModelColumns.MAX_POINT, "240, 240, 131");
            db.update(Tables.MODEL_INFO, values, where, null);

            where = ModelColumns.OBJECT_NAME + "='desk_fang'";
            values = new ContentValues();
            values.put(ModelColumns.MIN_POINT, "-250, -175, 0");
            values.put(ModelColumns.MAX_POINT, "250, 175, 103");
            db.update(Tables.MODEL_INFO, values, where, null);
            
            //解决在老的主题中灯光在ApplicationMenu之上显示的问题
            where = ModelColumns.COMPONENT_OBJECT + "='deng@group_house8'";
            values = new ContentValues();
            values.put(ModelColumns.STATUS_VALUE, 1064965);
            db.update(Tables.COMPONENT_INFO, values, where, null);
        } catch (Throwable ex) {
            Log.e(TAG, ex.getMessage(), ex);
        } finally {

        }
    }

    private void upgradeDatabaseToVersion78(SQLiteDatabase db) {
        try {
            String where = "name='group_tv'";
            db.delete(Tables.MODEL_INFO, where, null);
            where = "name='desk_fang'";
            db.delete(Tables.MODEL_INFO, where, null);
        } catch (Throwable ex) {
            Log.e(TAG, ex.getMessage(), ex);
        } finally {

        }
    }

    private void upgradeDatabaseToVersion79(SQLiteDatabase db) {
        try {
            String where = ThemeColumns.IS_APPLY + "=1";
            Cursor cursor = db.query(Tables.THEME, new String[] { ThemeColumns.NAME }, where, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    mCurrentTheme = cursor.getString(0);
                }
                cursor.close();
            }
            where = ThemeColumns.NAME + "='lighten'";
            db.delete(Tables.THEME, where, null);
            // 以下值和模型的配置文件assets/base/desk/models_config.xml相匹配
            where = ModelColumns.OBJECT_NAME + "='group_desk'";
            ContentValues values = new ContentValues();
            values.put(ModelColumns.MAX_POINT, "220, 220, 118");
            values.put(ModelColumns.MIN_POINT, "-220, -220, 0");
            values.put(ModelColumns.LOCAL_TRANS, "0,0,0,0,0,0,1,0.9,0.9,0.9");
            db.update(Tables.MODEL_INFO, values, where, null);
            // 以下值和模型的配置文件assets/base/desk_fang/models_config.xml相匹配
            where = ModelColumns.OBJECT_NAME + "='desk_fang'";
            values = new ContentValues();
            values.put(ModelColumns.MAX_POINT, "242.27,112.164,116");
            values.put(ModelColumns.LOCAL_TRANS, "0,0,0,0,0,0,1,0.85,0.85,0.9");
            db.update(Tables.MODEL_INFO, values, where, null);
            // 以下值和模型的配置文件assets/base/home8/xxx/theme_config.xml相匹配
            values = new ContentValues();
            values.put(ThemeColumns.FOV, 29);
            values.put(ThemeColumns.LOCATION, "0,-803,430");
            values.put("old_fov", 29);
            values.put("old_location", "0,-803,430");
            db.update(Tables.CAMERA_INFO, values, null, null);
        } catch (Throwable ex) {
            Log.e(TAG, ex.getMessage(), ex);
        } finally {

        }
    }

    private void upgradeDatabaseToVersion80(SQLiteDatabase db) {
        try {
            String where = ThemeColumns.IS_DOWNLOADED + "=0";
            Cursor cursor = db.query(Tables.THEME, new String[] { ThemeColumns._ID, ThemeColumns.NAME }, where, null,
                    null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int themeID = cursor.getInt(0);
                    where = ThemeColumns._ID + "=" + themeID;
                    ContentValues values = new ContentValues();
                    values = new ContentValues();
                    values.put(ThemeColumns.FOV, 29);
                    values.put(ThemeColumns.LOCATION, "0,-803,430");
                    values.put("old_fov", 29);
                    values.put("old_location", "0,-803,430");
                    db.update(Tables.CAMERA_INFO, values, where, null);
                    String themeName = cursor.getString(1);
                    if ("lighten".equals(themeName)) {
                        values = new ContentValues();
                        values.put(ThemeColumns.CELL_GAP_HEIGHT, 46);
                        values.put(ThemeColumns.WALL_PADDINGBOTTOM, 55);
                        values.put(ThemeColumns.WALL_PADDINGTOP, 50);
                        db.update(Tables.HOUSE_INFO, values, where, null);
                    } else {
                        values = new ContentValues();
                        values.put(ThemeColumns.CELL_GAP_HEIGHT, 46);
                        values.put(ThemeColumns.WALL_PADDINGBOTTOM, 40);
                        values.put(ThemeColumns.WALL_PADDINGTOP, 65);
                        db.update(Tables.HOUSE_INFO, values, where, null);
                    }
                }
                cursor.close();
            }
            // 更新lighten主题下房间的地板属性
            where = ModelColumns.COMPONENT_OBJECT + "='Object01@3dhme'";
            ContentValues values = new ContentValues();
            values.put(ModelColumns.STATUS_VALUE, 35668224);
            db.update(Tables.COMPONENT_INFO, values, where, null);
            
            where = ModelColumns.OBJECT_NAME + "='fangdajing'";
            db.delete(Tables.MODEL_INFO, where, null);
        } catch (Throwable ex) {
            Log.e(TAG, ex.getMessage(), ex);
        } finally {

        }
    }

    private void upgradeDatabaseToVersion82(SQLiteDatabase db) {
        try {
            String where = ModelColumns.OBJECT_NAME + "='group_camera'";
            ContentValues values = new ContentValues();
            values.put(ModelColumns.LOCAL_TRANS, "0,0,0,0,0,0,1,1,1,1");
            db.update(Tables.MODEL_INFO, values, where, null);
            
            where = ModelColumns.OBJECT_NAME + "='group_contact'";
            values = new ContentValues();
            values.put(ModelColumns.LOCAL_TRANS, "0,0,0,0,0,0,1,0.9,0.9,0.9");
            db.update(Tables.MODEL_INFO, values, where, null);
            
            where = ModelColumns.OBJECT_NAME + "='group_globe'";
            values = new ContentValues();
            values.put(ModelColumns.LOCAL_TRANS, "0,0,0,0,0,0,1,1,1,1");
            db.update(Tables.MODEL_INFO, values, where, null);
            
            where = ModelColumns.OBJECT_NAME + "='group_ipad'";
            values = new ContentValues();
            values.put(ModelColumns.LOCAL_TRANS, "0,0,0,0,0,0,1,1.3,1.3,1.3");
            db.update(Tables.MODEL_INFO, values, where, null);
            
            where = ModelColumns.OBJECT_NAME + "='group_s3'";
            values = new ContentValues();
            values.put(ModelColumns.LOCAL_TRANS, "0,0,0,0,0,0,1,1.1,1.1,1.1");
            db.update(Tables.MODEL_INFO, values, where, null);
            
            where = ModelColumns.OBJECT_NAME + "='group_sms'";
            values = new ContentValues();
            values.put(ModelColumns.LOCAL_TRANS, "0,0,0,0,0,0,1,1.5,1.5,1.5");
            db.update(Tables.MODEL_INFO, values, where, null);

        } catch (Throwable ex) {
            Log.e(TAG, ex.getMessage(), ex);
        } finally {

        }
    }

    private void upgradeDatabaseToVersion83(SQLiteDatabase db) {
        try {
            String where = ModelColumns.OBJECT_NAME + "='group_iconbox'";
            db.delete(Tables.MODEL_INFO, where, null);
            where = ModelColumns.OBJECT_NAME + "='group_s3'";
            ContentValues values = new ContentValues();
            values.put(ModelColumns.LOCAL_TRANS, "0,0,0,0,0,0,1,1.05,1.05,1.05");
            db.update(Tables.MODEL_INFO, values, where, null);
        } catch (Throwable ex) {
            Log.e(TAG, ex.getMessage(), ex);
        } finally {

        }
    }

    private void upgradeDatabaseToVersion84(SQLiteDatabase db) {
        try {
            String where = ModelColumns.COMPONENT_OBJECT + "='fangdajing@fangdajing'";
            ContentValues values = new ContentValues();
            values.put(ModelColumns.STATUS_VALUE, 2113568);
            db.update(Tables.COMPONENT_INFO, values, where, null);

            where = "name = 'woodfolderclose'";
            db.delete(Tables.MODEL_INFO, where, null);

            values = new ContentValues();
            values.put(ObjectInfoColumns.IS_NATIVE_OBJ, 0);
            db.update(Tables.OBJECTS_INFO, values, where, null);

        } catch (Throwable ex) {
            Log.e(TAG, ex.getMessage(), ex);
        } finally {

        }
    }

    private void upgradeDatabaseToVersion85(SQLiteDatabase db) {
        // changed
        try {
            String where = "name='woodfolderopen'";
            db.delete(Tables.MODEL_INFO, where, null);
        } catch (Throwable ex) {
            Log.e(TAG, ex.getMessage(), ex);
        } finally {

        }
    }

    private void upgradeDatabaseToVersion86(SQLiteDatabase db) {
        // changed
        try {
            String where = "name='woodfolderopen'";
            db.delete(Tables.MODEL_INFO, where, null);
        } catch (Throwable ex) {
            Log.e(TAG, ex.getMessage(), ex);
        } finally {

        }
    }

    private void upgradeDatabaseToVersion87(SQLiteDatabase db) {
        try {
            String where = ThemeColumns.IS_APPLY + "=1";
            Cursor cursor = db.query(Tables.THEME, new String[] { ThemeColumns.NAME }, where, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    mCurrentTheme = cursor.getString(0);
                }
                cursor.close();
            }
            where = ThemeColumns.NAME + "='dark' OR " + ThemeColumns.NAME + "='wood' OR " + ThemeColumns.NAME
                    + "='lighten'";
            db.delete(Tables.THEME, where, null);

            db.execSQL("ALTER TABLE " + Tables.MODEL_INFO + " ADD " + "supportScreen"
                    + " INTEGER NOT NULL DEFAULT -1;");
            where = ModelColumns.TYPE + "='Desk'";
            ContentValues values = new ContentValues();
            values.put("supportScreen", ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            db.update(Tables.MODEL_INFO, values, where, null);

            where = ModelColumns.OBJECT_NAME + "='fangdajing'";
            values = new ContentValues();
            values.putNull(ModelColumns.MAX_POINT);
            values.putNull(ModelColumns.MIN_POINT);
            db.update(Tables.MODEL_INFO, values, where, null);

            where = ModelColumns.OBJECT_NAME + "='group_largewallpicframe'";
            values = new ContentValues();
            values.put(ModelColumns.MAX_POINT, "155,0,238.5");
            values.put(ModelColumns.MIN_POINT, "-155,0,-238.5");
            db.update(Tables.MODEL_INFO, values, where, null);

            where = ModelColumns.OBJECT_NAME + "='group_hengwallpicframe'";
            values = new ContentValues();
            values.put(ModelColumns.MAX_POINT, "191,0,165");
            values.put(ModelColumns.MIN_POINT, "-191,0,-165");
            db.update(Tables.MODEL_INFO, values, where, null);

            where = ModelColumns.OBJECT_NAME + "='group_speaker'";
            values = new ContentValues();
            values.put("supportScreen", ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            db.update(Tables.MODEL_INFO, values, where, null);

            where = ModelColumns.OBJECT_NAME + "='group_tv'";
            values = new ContentValues();
            values.put(ModelColumns.MAX_POINT, "325,0,200");
            values.put(ModelColumns.MIN_POINT, "-325,-20,-200");
            db.update(Tables.MODEL_INFO, values, where, null);

        } catch (Throwable ex) {
            Log.e(TAG, ex.getMessage(), ex);
        } finally {

        }
    }

    private void upgradeDatabaseToVersion88(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE " + Tables.MODEL_INFO + " ADD " + ModelColumns.ACTION_STATUS
                    + " INTEGER NOT NULL DEFAULT 4;");
            String where = ModelColumns.OBJECT_NAME + "='fangdajing'";
            ContentValues values = new ContentValues();
            values.put(ModelColumns.ACTION_STATUS, 0);
            values.put(ModelColumns.COMPONENT_NAME, HomeUtils.encodeSearchComponentName());
            db.update(Tables.MODEL_INFO, values, where, null);

            where = ModelColumns.OBJECT_NAME + "='IconBackground'";
            db.delete(Tables.MODEL_INFO, where, null);
            
            where = ModelColumns.OBJECT_NAME + "='shop'";
            db.delete(Tables.MODEL_INFO, where, null);
            
            where = "name='woodfolderopen'";
            db.delete(Tables.MODEL_INFO, where, null);
            
            db.execSQL("ALTER TABLE camrea_info RENAME TO __temp__camrea_info;");
            db.execSQL("CREATE TABLE " + ProviderUtils.Tables.CAMERA_INFO + " (" + "camera_id INTEGER PRIMARY KEY,"
                    + ThemeColumns._ID + " INTEGER," 
                    + ThemeColumns.FOV + " FLOAT NOT NULL DEFAULT 60," 
                    + ThemeColumns.BEST_FOV + " FLOAT NOT NULL DEFAULT 29," 
                    + ThemeColumns.NEAREST_FOV + " FLOAT NOT NULL DEFAULT 26.274373," 
                    + ThemeColumns.FARTHEST_FOV + " FLOAT NOT NULL DEFAULT 39.73937," 
                    + ThemeColumns.NEAR + " FLOAT NOT NULL DEFAULT 1,"
                    + ThemeColumns.FAR + " FLOAT NOT NULL DEFAULT 2000,"
                    + ThemeColumns.LOCATION + " TEXT NOT NULL,"
                    + ThemeColumns.BEST_LOCATION + " TEXT NOT NULL DEFAULT '0,-803,430',"
                    + ThemeColumns.NEAREST_LOCATION + " TEXT NOT NULL DEFAULT '0,-753,430',"
                    + ThemeColumns.FARTHEST_LOCATION + " TEXT NOT NULL DEFAULT '0,-1000,430',"
                    + ThemeColumns.ZAXIS + " TEXT NOT NULL,"
                    + ThemeColumns.UP  + " TEXT NOT NULL" + ");");
            db.execSQL("INSERT INTO camrea_info(_id, fov, best_fov, near, far, location, best_location, zaxis, up)"
                    +" SELECT _id, fov, old_fov, near, far, location, old_location, zaxis, up FROM __temp__camrea_info;");
            db.execSQL("DROP TABLE __temp__camrea_info;"); 

        } catch (Throwable ex) {
            Log.e(TAG, ex.getMessage(), ex);
        } finally {

        }
    }

    private void upgradeDatabaseToVersion89(SQLiteDatabase db) {
        // changed (line issue)
        try {
            String where = ModelColumns.OBJECT_NAME + "='xiangkuangxin'";
            db.delete(Tables.MODEL_INFO, where, null);

            where = ObjectInfoColumns.OBJECT_NAME + "='xiangkuangxin'";
            db.delete(Tables.OBJECTS_INFO, where, null);
        } catch (Throwable ex) {
            Log.e(TAG, ex.getMessage(), ex);
        } finally {

        }
    }

    private void upgradeDatabaseToVersion90(SQLiteDatabase db) {
        try {
            String where = ModelColumns.OBJECT_NAME + "='group_contact'";
            db.delete(Tables.MODEL_INFO, where, null);
            
            where = ModelColumns.OBJECT_NAME + "='fangdajing'";
            db.delete(Tables.MODEL_INFO, where, null);
            
            where = ModelColumns.OBJECT_NAME + "='group_sms'";
            db.delete(Tables.MODEL_INFO, where, null);
        } catch (Throwable ex) {
            Log.e(TAG, ex.getMessage(), ex);
        } finally {

        }

    }
    
    private void upgradeDatabaseToVersion91(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE " + Tables.OBJECTS_INFO + " ADD " + ObjectInfoColumns.FACE_COLOR + " INTEGER NOT NULL DEFAULT -1;");
            db.execSQL("ALTER TABLE " + Tables.OBJECTS_INFO + " ADD " + ObjectInfoColumns.FACE_IMAGE_PATH + " TEXT;");
            
            String where = ModelColumns.OBJECT_NAME + "='airplane'";
            db.delete(Tables.MODEL_INFO, where, null);

            where = ModelColumns.OBJECT_NAME + "='group_appwall'";
            db.delete(Tables.MODEL_INFO, where, null);

            where = ModelColumns.OBJECT_NAME + "='group_appwallheng'";
            db.delete(Tables.MODEL_INFO, where, null);

            where = ModelColumns.OBJECT_NAME + "='group_camera'";
            db.delete(Tables.MODEL_INFO, where, null);

            where = ModelColumns.OBJECT_NAME + "='group_clock'";
            db.delete(Tables.MODEL_INFO, where, null);

            where = ModelColumns.OBJECT_NAME + "='group_desk'";
            db.delete(Tables.MODEL_INFO, where, null);
            where = ModelColumns.OBJECT_NAME + "='group_desk_2'";
            db.delete(Tables.MODEL_INFO, where, null);
            where = ModelColumns.OBJECT_NAME + "='desk_fang'";
            db.delete(Tables.MODEL_INFO, where, null);
            where = ModelColumns.OBJECT_NAME + "='desk_fang_land'";
            db.delete(Tables.MODEL_INFO, where, null);

            where = ModelColumns.OBJECT_NAME + "='group_email'";
            db.delete(Tables.MODEL_INFO, where, null);

            where = ModelColumns.OBJECT_NAME + "='group_feiting'";
            db.delete(Tables.MODEL_INFO, where, null);

            where = ModelColumns.OBJECT_NAME + "='group_globe'";
            db.delete(Tables.MODEL_INFO, where, null);

            where = ModelColumns.OBJECT_NAME + "='group_gplay'";
            db.delete(Tables.MODEL_INFO, where, null);

            where = ModelColumns.OBJECT_NAME + "='group_ipad'";
            db.delete(Tables.MODEL_INFO, where, null);

            where = ModelColumns.OBJECT_NAME + "='group_pc'";
            db.delete(Tables.MODEL_INFO, where, null);

            where = ModelColumns.OBJECT_NAME + "='group_largewallpicframe'";
            db.delete(Tables.MODEL_INFO, where, null);

            where = ModelColumns.OBJECT_NAME + "='group_hengwallpicframe'";
            db.delete(Tables.MODEL_INFO, where, null);

            where = ModelColumns.OBJECT_NAME + "='group_tablepicframe'";
            db.delete(Tables.MODEL_INFO, where, null);

            where = ObjectInfoColumns.OBJECT_NAME + "='group_tablepicframe'";
            db.delete(Tables.OBJECTS_INFO, where, null);

            where = ModelColumns.OBJECT_NAME + "='group_s3'";
            db.delete(Tables.MODEL_INFO, where, null);

            where = ModelColumns.OBJECT_NAME + "='group_pc'";
            db.delete(Tables.MODEL_INFO, where, null);

            where = ModelColumns.OBJECT_NAME + "='group_largewallpicframe'";
            db.delete(Tables.MODEL_INFO, where, null);
            
            where = ObjectInfoColumns.OBJECT_NAME + "='group_desk'";
            ContentValues values = new ContentValues();
            values.put(ObjectInfoColumns.OBJECT_NAME, "group_desk_2");
            db.update(Tables.OBJECTS_INFO, values, where, null);
        } catch (Throwable ex) {
            Log.e(TAG, ex.getMessage(), ex);
        } finally {

        }

    }

    private void upgradeDatabaseToVersion92(SQLiteDatabase db) {
        try {
            String where = ModelColumns.OBJECT_NAME + "='group_largewallpicframe'";
            db.delete(Tables.MODEL_INFO, where, null);

            where = ModelColumns.OBJECT_NAME + "='group_hengwallpicframe'";
            db.delete(Tables.MODEL_INFO, where, null);

            where = ModelColumns.OBJECT_NAME + "='group_appwall'";
            db.delete(Tables.MODEL_INFO, where, null);

            where = ModelColumns.OBJECT_NAME + "='group_appwallheng'";
            db.delete(Tables.MODEL_INFO, where, null);

            db.execSQL("UPDATE Objects_Config SET objectIndex=objectIndex+1000,name='group_appwall' WHERE name='group_appwallheng'");
        } catch (Throwable ex) {
            Log.e(TAG, ex.getMessage(), ex);
        } finally {

        }
    }
    

    private void upgradeDatabaseToVersion93(SQLiteDatabase db) {
        try {
            String where = ModelColumns.OBJECT_NAME + "='group_speaker'";
            db.delete(Tables.MODEL_INFO, where, null);
            
            where = ThemeColumns.IS_APPLY + "=1";
            Cursor cursor = db.query(Tables.THEME, new String[] { ThemeColumns.NAME }, where, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    mCurrentTheme = cursor.getString(0);
                }
                cursor.close();
            }
            where = ThemeColumns.NAME + "='dark'";
            db.delete(Tables.THEME, where, null);
        } catch (Throwable ex) {
            Log.e(TAG, ex.getMessage(), ex);
        } finally {

        }
    }

    private void upgradeDatabaseToVersion94(SQLiteDatabase db) {
        try {
            String where = ModelColumns.OBJECT_NAME + "='christmasTree'";
            db.delete(Tables.MODEL_INFO, where, null);
        } catch (Throwable ex) {
            Log.e(TAG, ex.getMessage(), ex);
        } finally {

        }
    }
    

    private void upgradeDatabaseToVersion95(SQLiteDatabase db) {
        try {
            String where = ObjectInfoColumns.OBJECT_NAME + "='group_iconbox' AND " + ObjectInfoColumns.COMPONENT_NAME
                    + " IS NULL";
            db.delete(Tables.OBJECTS_INFO, where, null);
            db.execSQL("UPDATE Objects_Config SET name=name||objectIndex,isNativeObj=0,objectIndex=0,type='App' WHERE type='IconBox'");
        } catch (Throwable ex) {
            Log.e(TAG, ex.getMessage(), ex);
        } finally {

        }
    }


    private void upgradeDatabaseToVersion96(SQLiteDatabase db) {
        try {
            String where = ThemeColumns.IS_APPLY + "=1";
            Cursor cursor = db.query(Tables.THEME, new String[] { ThemeColumns.NAME }, where, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    mCurrentTheme = cursor.getString(0);
                }
                cursor.close();
            }
            where = ThemeColumns.NAME + "='landscape'";
            db.delete(Tables.THEME, where, null);
            // remove wallpaper
            String wallpaperPath = mContext.getFilesDir() + File.separator + "landscape";
            HomeUtils.deleteFile(wallpaperPath);
        } catch (Throwable ex) {
            Log.e(TAG, ex.getMessage(), ex);
        } finally {

        }
    }

    private boolean upgradeDatabaseToVersion97(SQLiteDatabase db) {
        try {
            File file = new File(HomeUtils.getDownloadedFilePath(mContext, null));
            if (file.exists() && file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (File resouce : files) {
                        File configFile = new File(resouce.getPath() + File.separator + "theme_config.xml");
                        if (!configFile.exists()) {
                            continue;
                        }
                        InputStream is = null;
                        is = mContext.getResources().getAssets().open("base/lightenTexture/camera_config.xml");
                        File destFile = new File(resouce.getPath() + File.separator + "camera_config.xml");
                        destFile.createNewFile();
                        SEUtils.copyToFile(is, destFile);
                        is.close();

                        is = mContext.getResources().getAssets().open("base/lightenTexture/house/special_config.xml");
                        destFile = new File(resouce.getPath() + File.separator + "special_config.xml");
                        destFile.createNewFile();
                        SEUtils.copyToFile(is, destFile);
                        is.close();
                    }
                }
            }
            String where = ModelColumns.OBJECT_NAME + "='group_desk_2'";
            ContentValues values = new ContentValues();
            values.put(ModelColumns.TYPE, "DeskPort");
            db.update(Tables.MODEL_INFO, values, where, null);

            where = ModelColumns.OBJECT_NAME + "='desk_fang'";
            values = new ContentValues();
            values.put(ModelColumns.TYPE, "DeskPort");
            db.update(Tables.MODEL_INFO, values, where, null);

            where = ModelColumns.OBJECT_NAME + "='desk_fang_land'";
            values = new ContentValues();
            values.put(ModelColumns.TYPE, "DeskLand");
            db.update(Tables.MODEL_INFO, values, where, null);

            where = ModelColumns.OBJECT_NAME + "='jiazi'";
            db.delete(Tables.MODEL_INFO, where, null);
            where = ModelColumns.OBJECT_NAME + "='shop'";
            db.delete(Tables.MODEL_INFO, where, null);
            where = ModelColumns.OBJECT_NAME + "='group_showbox'";
            db.delete(Tables.MODEL_INFO, where, null);
            
            where = ObjectInfoColumns.OBJECT_NAME + "='group_desk_2'";
            values = new ContentValues();
            values.put(ObjectInfoColumns.OBJECT_TYPE, "DeskPort");
            db.update(Tables.OBJECTS_INFO, values, where, null);

            where = ObjectInfoColumns.OBJECT_NAME + "='desk_fang'";
            values = new ContentValues();
            values.put(ObjectInfoColumns.OBJECT_TYPE, "DeskPort");
            db.update(Tables.OBJECTS_INFO, values, where, null);

            where = ObjectInfoColumns.OBJECT_NAME + "='desk_fang_land'";
            values = new ContentValues();
            values.put(ObjectInfoColumns.OBJECT_TYPE, "DeskLand");
            db.update(Tables.OBJECTS_INFO, values, where, null);

            where = ObjectInfoColumns.OBJECT_NAME + "='jiazi'";
            db.delete(Tables.OBJECTS_INFO, where, null);
            String localFilePath = HomeUtils.getLocalFilePath(mContext, "jiazi");
            HomeUtils.deleteFile(localFilePath);
            
            where = ThemeColumns.IS_APPLY + "=1";
            Cursor cursor = db.query(Tables.THEME, new String[] { ThemeColumns.NAME }, where, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    mCurrentTheme = cursor.getString(0);
                }
                cursor.close();
            }
            
            db.execSQL("ALTER TABLE " + Tables.MODEL_INFO + " ADD " + ModelColumns.BOUNDING_THEME_NAME + " TEXT;");
            where = ThemeColumns.NAME + "='landscape'";
            db.delete(Tables.THEME, where, null);
            where = ThemeColumns.NAME + "='wood'";
            db.delete(Tables.THEME, where, null);
            where = ThemeColumns.NAME + "='lighten'";
            db.delete(Tables.THEME, where, null);
            
            db.execSQL("DROP TABLE IF EXISTS CAMERA_INFO");
            db.execSQL("DROP TABLE IF EXISTS HOUSE_INFO");
            db.execSQL("DROP TRIGGER IF EXISTS theme_cleanup_camera");
            db.execSQL("DROP TRIGGER IF EXISTS theme_cleanup_house_info");
            db.execSQL("DROP TRIGGER IF EXISTS theme_cleanup_house_model_info");
            db.execSQL("ALTER TABLE " + Tables.THEME + " ADD " + ThemeColumns.HOUSE_NAME + " TEXT;");
            db.execSQL("ALTER TABLE " + Tables.THEME + " ADD " + ThemeColumns.CAMERA_LOCATION + " TEXT;");
            db.execSQL("ALTER TABLE " + Tables.THEME + " ADD " + ThemeColumns.CAMERA_FOV + " FLOAT  NOT NULL DEFAULT 30;");
            db.execSQL("ALTER TABLE " + Tables.THEME + " ADD " + ThemeColumns.WALL_INDEX + " INTEGER NOT NULL DEFAULT 0;");
            db.execSQL("CREATE TRIGGER theme_cleanup_bounding_model_info DELETE ON " + ProviderUtils.Tables.THEME + " "
                    + "BEGIN " + EVENTS_CLEANUP_TRIGGER_SQL_THEME__BOUNDING_MODEL + "END");
            db.execSQL("UPDATE theme SET house_name='house_'||name||'_theme'");
            return true;
        } catch (Throwable ex) {
            Log.e(TAG, ex.getMessage(), ex);
            return false;
        } finally {

        }

    }
    private void savecomponentAttributeToDB73(SQLiteDatabase db, int modelId, SEComponentAttribute componentAttribute) {

        ContentValues values = new ContentValues();
        values.put(ModelColumns._ID, modelId);
        values.put(ModelColumns.COMPONENT_OBJECT, componentAttribute.mComponentName);
        values.put(ModelColumns.LIGHT_NAMES, componentAttribute.mLightNames);
        values.put(ModelColumns.COMPONENT_REGULAR_NAME, componentAttribute.mRegularName);
        values.put(ModelColumns.STATUS_VALUE, componentAttribute.mStatusValue);
        values.put(ModelColumns.SPATIAL_DATA_VALUE, componentAttribute.mSpatialDataValue);
        values.put(ModelColumns.ALPHA_VALUE, componentAttribute.mAlphaValue);
        values.put(ModelColumns.EFFECT_DATA, ProviderUtils.floatArrayToString(componentAttribute.mEffectData.mD));
        db.insert(Tables.COMPONENT_INFO, null, values);
    }
    
    public void createAppsDrawer(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE " + ProviderUtils.Tables.APPS_DRAWER + " (" + AppsDrawerColumns._ID
                    + " INTEGER PRIMARY KEY," + AppsDrawerColumns.COMPONENTNAME + " TEXT,"
                    + AppsDrawerColumns.LAUNCHERCOUNT + " INTEGER DEFAULT 0," + " UNIQUE ("
                    + AppsDrawerColumns.COMPONENTNAME + ")" + ");");
        } catch (Throwable ex) {
            Log.e(TAG, ex.getMessage(), ex);
        } finally {
        }
    }

    public void loadDefaultData(SQLiteDatabase db) {
        loadThemeInfo(db, false);
        loadModelInfo(db, false);
    }

    public void loadDefaultDataAfterUpgraded(SQLiteDatabase db) {
        loadModelInfo(db, true);
        loadThemeInfo(db, true);
        loadModelAndThemeInfoFromExternalPath(db);
    }

    private void loadThemeInfo(SQLiteDatabase db, boolean check) {
        Iterator<Entry<String, String>> iter = THEME_XMLS.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, String> entry = iter.next();
            String name = entry.getKey();
            String path = entry.getValue();
            if (check) {
                String where = ThemeColumns.NAME + "='" + name + "'";
                String[] columns = { ThemeColumns._ID };
                Cursor cursor = db.query(Tables.THEME, columns, where, null, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        cursor.close();
                        continue;
                    }
                    cursor.close();
                }
            }
            try {
                InputStream is = mContext.getAssets().open(path + File.separator + "theme_config.xml");
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(is, "utf-8");
                XmlUtils.beginDocument(parser, "config");
                String id = HomeUtils.getDefaultThemeId(name);
                ThemeInfo config = ThemeInfo.CreateFromXml(mContext, parser, id, "assets" + File.separator + path);
                if (config.mThemeName.equals(mCurrentTheme)) {
                    config.mIsApplyed = true;
                }
                config.saveToDB(mContext, db);
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean loadModelInfo(SQLiteDatabase db, String name, String path, boolean check) {
        if (check) {
            String where = ModelColumns.OBJECT_NAME + "='" + name + "'";
            String[] columns = { ModelColumns._ID };
            Cursor cursor = db.query(Tables.MODEL_INFO, columns, where, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    cursor.close();
                    return false;
                }
                cursor.close();
            }
        }

        try {
            InputStream is = mContext.getAssets().open(path + File.separator + "models_config.xml");
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(is, "utf-8");
            XmlUtils.beginDocument(parser, "config");
            String id = HomeUtils.getDefaultObjectId(name);
            ModelInfo config = ModelInfo.CreateFromXml(parser, id, "assets" + File.separator + path);
            config.saveToDB(db);
            is.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void loadModelInfo(SQLiteDatabase db, boolean check) {
        Iterator<Entry<String, String>> iter = MODEL_XMLS.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, String> entry = iter.next();
            String name = entry.getKey();
            String path = entry.getValue();
            loadModelInfo(db, name, path, check);
        }
    }

    /**
     * 数据库升级完成后
     * 从data/data/com.borqs.se/files/local_resource/路径下获取在Asset路径下以及数据库中没有的物体
     */
    private void loadModelAndThemeInfoFromExternalPath(SQLiteDatabase db) {
        File file = new File(HomeUtils.getLocalFilePath(mContext, null));
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File resouce : files) {
                    if (MODEL_XMLS.containsKey(resouce) || THEME_XMLS.containsKey(resouce)) {
                        continue;
                    } else {
                        File configFile = new File(resouce.getPath() + File.separator
                                + MarketUtils.MARKET_CONFIG_FILE_NAME);
                        if (!configFile.exists()) {
                            continue;
                        }
                        Product resourceXML = ThemeXmlParser.parser(resouce);
                        if (resourceXML!= null) {
                            String type = resourceXML.type;
                            String id = resourceXML.product_id;
                            if ("theme".equals(type)) {
                                String where = ThemeColumns.PRODUCT_ID + "='" + id + "'";
                                String[] columns = { ThemeColumns._ID };
                                Cursor cursor = db.query(Tables.THEME, columns, where, null, null, null, null);
                                if (cursor != null) {
                                    if (cursor.moveToFirst()) {
                                        cursor.close();
                                        continue;
                                    }
                                    cursor.close();
                                }
                                try {
                                    FileInputStream fis = new FileInputStream(resouce.getPath() + File.separator
                                            + "theme_config.xml");
                                    XmlPullParser parser = Xml.newPullParser();
                                    parser.setInput(fis, "utf-8");
                                    XmlUtils.beginDocument(parser, "config");
                                    ThemeInfo config = ThemeInfo.CreateFromXml(mContext, parser, id, resouce.getPath());
                                    if (config.mThemeName.equals(mCurrentTheme)) {
                                        config.mIsApplyed = true;
                                    }
                                    config.saveToDB(mContext, db);
                                    fis.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else if ("object".equals(type)) {
                                String where = ModelColumns.PRODUCT_ID + "='" + id + "'";
                                String[] columns = { ModelColumns._ID };
                                Cursor cursor = db.query(Tables.MODEL_INFO, columns, where, null, null, null, null);
                                if (cursor != null) {
                                    if (cursor.moveToFirst()) {
                                        cursor.close();
                                        continue;
                                    }
                                    cursor.close();
                                }
                                try {
                                    FileInputStream fis = new FileInputStream(resouce.getPath() + File.separator
                                            + "models_config.xml");
                                    XmlPullParser parser = Xml.newPullParser();
                                    parser.setInput(fis, "utf-8");
                                    XmlUtils.beginDocument(parser, "config");
                                    ModelInfo config = ModelInfo.CreateFromXml(parser, id, resouce.getPath());
                                    config.saveToDB(db);
                                    fis.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

	private void moveAssetToExternal() {
		SettingsActivity.setUpdateLocalResFinished(mContext, false);
		HomeUtils.moveAssetToExternal(mContext);
	}
}


