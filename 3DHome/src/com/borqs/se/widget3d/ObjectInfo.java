package com.borqs.se.widget3d;

import java.lang.reflect.Constructor;
import java.net.URISyntaxException;

import org.xmlpull.v1.XmlPullParser;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;

import com.borqs.se.engine.SESceneManager;
import com.borqs.se.engine.SEVector.SEVector3f;
import com.borqs.se.home3d.HomeScene;
import com.borqs.se.home3d.ModelInfo;
import com.borqs.se.home3d.HomeManager;
import com.borqs.se.home3d.SearchActivity;
import com.borqs.se.home3d.UpdateDBThread;
import com.borqs.se.home3d.ProviderUtils.ObjectInfoColumns;
import com.borqs.se.home3d.ProviderUtils.Tables;
import com.borqs.se.home3d.ProviderUtils.VesselColumns;
import com.borqs.se.home3d.HomeUtils;
import com.borqs.se.shortcut.ItemInfo;
import com.borqs.se.shortcut.LauncherModel;

public class ObjectInfo {
    public final static int ACTION_CAN_RESIZE = 0x1;
    public final static int ACTION_CAN_UNINSATLL = 0x1 << 1;
    public final static int ACTION_CAN_CHANGE_BIND = 0x1 << 2;
    public final static int ACTION_CAN_CHANGE_ICON = 0x1 << 3;
    public final static int ACTION_CAN_CHANGE_LABEL = 0x1 << 4;
    public final static int ACTION_SHOW_SETTING = 0x1 << 5;
    public final static int ACTION_CAN_CHANGE_COLOR = 0x1 << 6;
    public final static int ACTION_CAN_CHANGE_IMAGE = 0x1 << 7;
    public final static int ACTION_CAN_REPLACE = 0x1 << 8;

    public static final int SLOT_TYPE_UNKNOWN = -1;
    public static final int SLOT_TYPE_SKY = 0;
    public static final int SLOT_TYPE_WALL = 1;
    public static final int SLOT_TYPE_DESKTOP = 2;
    public static final int SLOT_TYPE_GROUND = 3;
    public static final int SLOT_TYPE_APP_WALL = 4;
    public static final int SLOT_TYPE_BOOKSHELF = 5;

    public static final String ROOT_NODE = "RootNode";
    public static final String HOUSE = "House";
    public static final String Desk = "Desk";
    public static final String WALL = "Wall";
    public static final String GROUND = "Ground";
    public static final String SKY = "Sky";
    public static final String AIRSHIP = "Airship";
    public static final String TV = "TV";
    public static final String PAD = "Laptop";

    public int mID;

    public String mName;

    public String mType;

    public int mIndex;

    public int mShaderType;

    public String mSceneName;

    public String mVesselName;

    public int mVesselIndex;

    /**
     * 物体绑定的应用名
     */
    public ComponentName mComponentName;

    private Intent mIntent;

    private Context mContext;

    public int mSlotType;

    public ObjectSlot mObjectSlot;

    public int mAppWidgetId = -1;

    public String mShortcutUrl;

    public Bitmap mShortcutIcon;

    public boolean mIsNativeObject;

    public String mClassName;

    public ModelInfo mModelInfo;

    public String mDisplayName;

    private ContentResolver mContentResolver;
    public int mFaceColor;
    public int mActionStatus;

    /**
     * 物体不做任何平移，直接加到场景后的最小点，以及最大点。 通过这两个点能够大概知道物体中心点位置以及物体的大小
     */
    public SEVector3f mMinPoint;
    public SEVector3f mMaxPoint;

    public ObjectInfo() {
        mIndex = 0;
        mSlotType = -1;
        mShaderType = 0;
        mObjectSlot = new ObjectSlot();
        mIsNativeObject = false;
        mVesselIndex = 0;
        mContext = SESceneManager.getInstance().getContext();
        mContentResolver = mContext.getContentResolver();
        mActionStatus = ACTION_CAN_CHANGE_BIND;
        mID = -1;
        mFaceColor = -1;
    }

    public NormalObject CreateNormalObject(HomeScene scene) {
        NormalObject obj = null;
        if (mClassName == null) {
            if ("RootNode".equals(mType)) {
                obj = new NodeRoot(scene, mName, mIndex);
            } else if ("HouseLand".equals(mType) || "HousePort".equals(mType) || "House".equals(mType)) {
                obj = new House(scene, mName, mIndex);
            } else if ("DeskLand".equals(mType) || "DeskPort".equals(mType) || "Desk".equals(mType)) {
                obj = new Desk(scene, mName, mIndex);
            } else if ("Wall".equals(mType)) {
                obj = new Wall(scene, mName, mIndex);
            } else if ("Ground".equals(mType)) {
                obj = new Ground(scene, mName, mIndex);
            } else if ("Sky".equals(mType)) {
                obj = new Cloud(scene, mName, mIndex);
            } else if ("Airship".equals(mType)) {
                obj = new Flyer(scene, mName, mIndex);
            } else if ("Bookshelf".equals(mType)) {
                obj = new Bookshelf(scene, mName, mIndex);
            } else if ("Calendary".equals(mType)) {
                obj = new Calendary(scene, mName, mIndex);
            } else if ("TableFrame".equals(mType)) {
                obj = new TableFrame(scene, mName, mIndex);
            } else if ("PictureFrame".equals(mType)) {
                obj = new WallFrame(scene, mName, mIndex);
            } else if ("TV".equals(mType)) {
                obj = new Television(scene, mName, mIndex);
            } else if ("Clock".equals(mType)) {
                obj = new Clock(scene, mName, mIndex);
            } else if ("Laptop".equals(mType)) {
                obj = new Laptop(scene, mName, mIndex);
            } else if ("App".equals(mType)) {
                obj = new AppObject(scene, mName, mIndex);
            } else if ("Shortcut".equals(mType)) {
                obj = new ShortcutObject(scene, mName, mIndex);
            } else if ("Widget".equals(mType)) {
                obj = new WidgetObject(scene, mName, mIndex);
            } else if ("Folder".equals(mType)) {
                obj = new Folder(scene, mName, mIndex);
            } else if ("Email".equals(mType)) {
                obj = new EmailBox(scene, mName, mIndex);
            } else if ("AppWall".equals(mType)) {
                obj = new AppWall(scene, mName, mIndex);
            } else if ("WallGlobe".equals(mType)) {
                obj = new Navigation(scene, mName, mIndex);
            } else if ("PictureFrame2".equals(mType)) {
                obj = new WallFrame2(scene, mName, mIndex);
            } else if ("ChristmasTree".equals(mType)) {
                obj = new ChristmasTree(scene, mName, mIndex);
            } else if ("Bookshelf_L".equals(mType)) {
                obj = new BookshelfL(scene, mName, mIndex);
            } else {
                obj = new NormalObject(scene, mName, mIndex);
            }
        } else {
            try {
                Class<NormalObject> clazz = (Class<NormalObject>) Class.forName(mClassName);
                Class[] paratype = { HomeScene.class, String.class, int.class };
                Constructor constructor = clazz.getConstructor(paratype);
                Object[] para = { scene, mName, mIndex };
                obj = (NormalObject) constructor.newInstance(para);
            } catch (Exception e) {
                obj = new NormalObject(scene, mName, mIndex);
            }
        }
        obj.setObjectInfo(this);
        return obj;
    }

    public boolean isShortcut() {
        if (mShortcutUrl != null && !TextUtils.isEmpty(mShortcutUrl)) {
            return true;
        }
        return false;
    }

    /**
     * User updates the app object by icon or name
     */
    public boolean isAppUserUpdate() {
        if (!TextUtils.isEmpty(mType)) {
            if (mType.equals("App")) {
                if (!TextUtils.isEmpty(mDisplayName)) {
                    return true;
                }
                if (mShortcutIcon != null) {
                    return true;
                } // TODO: recover?
            }
        }
        return false;
    }

    public void setModelInfo(ModelInfo modelInfo) {
        mModelInfo = modelInfo;
        if (mModelInfo != null) {
            mActionStatus = mModelInfo.mActionStatus;
            if (mSlotType == SLOT_TYPE_UNKNOWN) {
                mSlotType = mModelInfo.mSlotType;
            }
            mName = mModelInfo.mName;
            mType = mModelInfo.mType;
            if (mObjectSlot.mSpanX <= 0) {
                mObjectSlot.mSpanX = mModelInfo.mSpanX;
            }
            if (mObjectSlot.mSpanY <= 0) {
                mObjectSlot.mSpanY = mModelInfo.mSpanY;
            }
            mIsNativeObject = true;
            mMinPoint = mModelInfo.mMinPoint.clone();
            mMaxPoint = mModelInfo.mMaxPoint.clone();
            if (mModelInfo.mSlotType == ObjectInfo.SLOT_TYPE_DESKTOP) {
                mObjectSlot.mSpanX = 1;
                mObjectSlot.mSpanY = 1;
            }
        }
    }

    public ResolveInfo getResolveInfo() {
        if (mComponentName == null) {
            return null;
        }
        ItemInfo itemInfo;
        if ("Widget".equals(mType)) {
            itemInfo = LauncherModel.getInstance().findWidgetItem(mComponentName);
        } else {
            itemInfo = LauncherModel.getInstance().findAppItem(mComponentName);
        }
        if (itemInfo != null) {
            return itemInfo.getResolveInfo();
        }
        return null;
    }

    public Context getContext() {
        return mContext;
    }

    public int getSlotIndex() {
        return mObjectSlot.mSlotIndex;
    }

    public int getSpanX() {
        return mObjectSlot.mSpanX;
    }

    public void setSpanX(int spanX) {
        mObjectSlot.mSpanX = spanX;
    }

    public int getSpanY() {
        return mObjectSlot.mSpanY;
    }

    public void setSpanY(int spanY) {
        mObjectSlot.mSpanY = spanY;
    }

    public int getStartX() {
        return mObjectSlot.mStartX;
    }

    public int getStartY() {
        return mObjectSlot.mStartY;
    }

    public Intent getIntent() {

        if (mIntent != null) {
            return mIntent;
        }
        if (mShortcutUrl != null) {
            try {
                mIntent = Intent.parseUri(mShortcutUrl, 0);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        } else if (getCategoryComponentName() != null) {
            mIntent = new Intent(Intent.ACTION_MAIN);
            mIntent.setComponent(mComponentName);
            if (!SearchActivity.class.getName().equals(mComponentName.getClassName())) {
                mIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            } else {
                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }

        }
        return mIntent;
    }

    public ComponentName getCategoryComponentName() {
        if (mComponentName == null && mIsNativeObject) {
            if (mModelInfo.mComponentName != null) {
                mComponentName = mModelInfo.mComponentName;
            } else {
                mComponentName = HomeUtils.getCategoryComponentName(mContext, mModelInfo.mKeyWords);
            }
            if (mComponentName != null) {
                UpdateDBThread.getInstance().process(new Runnable() {
                    public void run() {
                        String where = ObjectInfoColumns._ID + "=" + mID;
                        ContentValues values = new ContentValues();
                        values.put(ObjectInfoColumns.COMPONENT_NAME, mComponentName.flattenToShortString());
                        mContentResolver.update(ObjectInfoColumns.CONTENT_URI, values, where, null);
                    }
                });
            }
        }
        return mComponentName;
    }

    public void updateComponentName(ComponentName name) {
        mComponentName = name;
        mShortcutUrl = null;
        mShortcutIcon = null;
        mDisplayName = null;
        UpdateDBThread.getInstance().process(new Runnable() {
            public void run() {
                String where = ObjectInfoColumns._ID + "=" + mID;
                ContentValues values = new ContentValues();
                values.put(ObjectInfoColumns.COMPONENT_NAME, mComponentName.flattenToShortString());
                values.putNull(ObjectInfoColumns.SHORTCUT_URL);
                values.putNull(ObjectInfoColumns.SHORTCUT_ICON);
                values.putNull(ObjectInfoColumns.DISPLAY_NAME);
                mContentResolver.update(ObjectInfoColumns.CONTENT_URI, values, where, null);
            }
        });
        mIntent = null;
    }

    public void updateFaceColor(int color) {
        mFaceColor = color;
        UpdateDBThread.getInstance().process(new Runnable() {
            public void run() {
                String where = ObjectInfoColumns._ID + "=" + mID;
                ContentValues values = new ContentValues();
                values.put(ObjectInfoColumns.FACE_COLOR, mFaceColor);
                mContentResolver.update(ObjectInfoColumns.CONTENT_URI, values, where, null);
            }
        });
    }

    public void saveToDB(SQLiteDatabase db) {
        String where = ObjectInfoColumns.OBJECT_NAME + "='" + mName + "' and " + ObjectInfoColumns.SCENE_NAME + "='"
                + mSceneName + "' and " + ObjectInfoColumns.OBJECT_INDEX + "=" + mIndex;
        Cursor cursor = null;
        try {
            cursor = db.query(Tables.OBJECTS_INFO, null, where, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    return;
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        ContentValues values = new ContentValues();
        saveToDB(values);
        mID = (int) db.insert(Tables.OBJECTS_INFO, null, values);
        mObjectSlot.mObjectID = mID;
        if (mVesselName != null) {
            where = ObjectInfoColumns.OBJECT_NAME + "='" + mVesselName + "' and " + ObjectInfoColumns.SCENE_NAME + "='"
                    + mSceneName + "' and " + ObjectInfoColumns.OBJECT_INDEX + "=" + mVesselIndex;
            String[] columns = { ObjectInfoColumns.OBJECT_ID };
            cursor = db.query(Tables.OBJECTS_INFO, columns, where, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    mObjectSlot.mVesselID = cursor.getInt(0);
                }
                cursor.close();
            }
        }
        saveSlotToDB(db);
    }

    private void saveSlotToDB(SQLiteDatabase db) {
        String table = Tables.VESSEL;
        final String updateTable = table;
        ContentValues values = new ContentValues();
        saveSlotToDB(values);
        db.insert(updateTable, null, values);
    }

    public void saveToDB() {
        Runnable finishListener = null;
        saveToDB(finishListener);
    }

    public void saveToDB(final Runnable finishListener) {
        UpdateDBThread.getInstance().process(new Runnable() {
            public void run() {
                if (mID >= 0) {
                    updateSlotDB();
                    if (finishListener != null) {
                        finishListener.run();
                    }
                    return;
                }
                ContentValues values = new ContentValues();
                saveToDB(values);
                Uri insertUri = mContentResolver.insert(ObjectInfoColumns.CONTENT_URI, values);
                if (insertUri != null) {
                    long rowId = ContentUris.parseId(insertUri);
                    mID = (int) rowId;
                    mObjectSlot.mObjectID = mID;
                    if (mVesselName != null) {
                        String where = ObjectInfoColumns.OBJECT_NAME + "='" + mVesselName + "' and "
                                + ObjectInfoColumns.SCENE_NAME + "='" + mSceneName + "' and "
                                + ObjectInfoColumns.OBJECT_INDEX + "=" + mVesselIndex;
                        String[] columns = { ObjectInfoColumns.OBJECT_ID };
                        Cursor cursor = mContentResolver.query(ObjectInfoColumns.CONTENT_URI, columns, where, null,
                                null);
                        if (cursor != null) {
                            if (cursor.moveToFirst()) {
                                mObjectSlot.mVesselID = cursor.getInt(0);
                            }
                            cursor.close();
                        }
                    }
                    saveSlotToDB();
                }
                if (finishListener != null) {
                    finishListener.run();
                }
            }
        });
    }

    public void updateToDB(final boolean icon, final boolean disPalyName, final boolean url) {
        UpdateDBThread.getInstance().process(new Runnable() {
            public void run() {
                String where = ObjectInfoColumns.OBJECT_ID + "=" + mID;
                ContentValues values = new ContentValues();
                if (disPalyName) {
                    values.put(ObjectInfoColumns.DISPLAY_NAME, mDisplayName);
                }
                if (icon) {
                    if (mShortcutIcon != null && !mShortcutIcon.isRecycled()) {
                        HomeUtils.writeBitmap(values, mShortcutIcon);
                    } else {
                        mShortcutIcon = null;
                        values.putNull(ObjectInfoColumns.SHORTCUT_URL);
                    }
                }
                if (url) {
                    values.put(ObjectInfoColumns.SHORTCUT_URL, mShortcutUrl);
                }
                mContentResolver.update(ObjectInfoColumns.CONTENT_URI, values, where, null);
            }
        });
    }

    private void saveToDB(ContentValues values) {
        values.put(ObjectInfoColumns.OBJECT_NAME, mName);
        values.put(ObjectInfoColumns.OBJECT_TYPE, mType);
        values.put(ObjectInfoColumns.SHADER_TYPE, mShaderType);
        values.put(ObjectInfoColumns.SCENE_NAME, mSceneName);
        if (mComponentName != null) {
            values.put(ObjectInfoColumns.COMPONENT_NAME, mComponentName.flattenToShortString());
        }
        values.put(ObjectInfoColumns.CLASS_NAME, mClassName);
        values.put(ObjectInfoColumns.SLOT_TYPE, mSlotType);
        values.put(ObjectInfoColumns.WIDGET_ID, mAppWidgetId);
        values.put(ObjectInfoColumns.SHORTCUT_URL, mShortcutUrl);
        if (mIsNativeObject) {
            values.put(ObjectInfoColumns.IS_NATIVE_OBJ, 1);
        } else {
            values.put(ObjectInfoColumns.IS_NATIVE_OBJ, 0);
        }
        if (mShortcutIcon != null && !mShortcutIcon.isRecycled()) {
            HomeUtils.writeBitmap(values, mShortcutIcon);
        }
        values.put(ObjectInfoColumns.DISPLAY_NAME, mDisplayName);
        values.put(ObjectInfoColumns.OBJECT_INDEX, mIndex);
    }

    public void releaseDB() {
        UpdateDBThread.getInstance().process(new Runnable() {
            public void run() {
                String where = ObjectInfoColumns._ID + "=" + mID;
                mContentResolver.delete(ObjectInfoColumns.CONTENT_URI, where, null);
            }
        });
    }

    public void updateSlotDB() {
        UpdateDBThread.getInstance().process(new Runnable() {
            public void run() {
                String where = VesselColumns.OBJECT_ID + "=" + mID;
                ContentValues values = new ContentValues();
                saveSlotToDB(values);
                mContentResolver.update(VesselColumns.CONTENT_URI, values, where, null);
            }
        });

    }

    public void updateSlotTypeDB() {
        UpdateDBThread.getInstance().process(new Runnable() {
            public void run() {
                String where = ObjectInfoColumns._ID + "=" + mID;
                ContentValues values = new ContentValues();
                values.put(ObjectInfoColumns.SLOT_TYPE, mSlotType);
                mContentResolver.update(ObjectInfoColumns.CONTENT_URI, values, where, null);
            }
        });

    }

    public void saveSlotToDB() {
        ContentValues values = new ContentValues();
        saveSlotToDB(values);
        mContentResolver.insert(VesselColumns.CONTENT_URI, values);
    }

    private void saveSlotToDB(ContentValues values) {
        values.put(VesselColumns.VESSEL_ID, mObjectSlot.mVesselID);
        values.put(VesselColumns.OBJECT_ID, mObjectSlot.mObjectID);
        values.put(VesselColumns.SLOT_INDEX, mObjectSlot.mSlotIndex);
        values.put(VesselColumns.SLOT_StartX, mObjectSlot.mStartX);
        values.put(VesselColumns.SLOT_StartY, mObjectSlot.mStartY);
        values.put(VesselColumns.SLOT_SpanX, mObjectSlot.mSpanX);
        values.put(VesselColumns.SLOT_SpanY, mObjectSlot.mSpanY);
    }

    public static ObjectInfo CreateFromXml(XmlPullParser parser) {
        ObjectInfo info = new ObjectInfo();
        info.mName = parser.getAttributeValue(null, ObjectInfoColumns.OBJECT_NAME);
        String shaderType = parser.getAttributeValue(null, ObjectInfoColumns.SHADER_TYPE);
        if (shaderType != null) {
            info.mShaderType = Integer.parseInt(shaderType);
        }
        info.mSceneName = parser.getAttributeValue(null, ObjectInfoColumns.SCENE_NAME);
        String componentName = parser.getAttributeValue(null, ObjectInfoColumns.COMPONENT_NAME);
        if (componentName != null) {
            info.mComponentName = ComponentName.unflattenFromString(componentName);
        }
        String slotType = parser.getAttributeValue(null, ObjectInfoColumns.SLOT_TYPE);
        if ("wall".equals(slotType)) {
            info.mSlotType = ObjectInfo.SLOT_TYPE_WALL;
        } else if ("desktop".equals(slotType)) {
            info.mSlotType = ObjectInfo.SLOT_TYPE_DESKTOP;
        } else if ("ground".equals(slotType)) {
            info.mSlotType = ObjectInfo.SLOT_TYPE_GROUND;
        } else if ("sky".equals(slotType)) {
            info.mSlotType = ObjectInfo.SLOT_TYPE_SKY;
        }

        info.mVesselName = parser.getAttributeValue(null, "vesselName");
        String vesselIndex = parser.getAttributeValue(null, "vesselIndex");
        if (!TextUtils.isEmpty(vesselIndex)) {
            info.mVesselIndex = Integer.parseInt(vesselIndex);
        }

        String slotIndex = parser.getAttributeValue(null, "slotIndex");
        if (!TextUtils.isEmpty(slotIndex)) {
            info.mObjectSlot.mSlotIndex = Integer.parseInt(slotIndex);
        }
        String slotStartX = parser.getAttributeValue(null, "slotStartX");
        if (!TextUtils.isEmpty(slotStartX)) {
            info.mObjectSlot.mStartX = Integer.parseInt(slotStartX);
        }
        String slotStartY = parser.getAttributeValue(null, "slotStartY");
        if (!TextUtils.isEmpty(slotStartY)) {
            info.mObjectSlot.mStartY = Integer.parseInt(slotStartY);
        }
        String slotSpanX = parser.getAttributeValue(null, "slotSpanX");
        if (!TextUtils.isEmpty(slotSpanX)) {
            info.mObjectSlot.mSpanX = Integer.parseInt(slotSpanX);
        }
        String slotSpanY = parser.getAttributeValue(null, "slotSpanY");
        if (!TextUtils.isEmpty(slotSpanY)) {
            info.mObjectSlot.mSpanY = Integer.parseInt(slotSpanY);
        }

        String className = parser.getAttributeValue(null, ObjectInfoColumns.CLASS_NAME);
        if (!TextUtils.isEmpty(className)) {
            info.mClassName = className;
        }

        String isnative = parser.getAttributeValue(null, ObjectInfoColumns.IS_NATIVE_OBJ);
        if (!TextUtils.isEmpty(isnative)) {
            if (Integer.parseInt(isnative) > 0) {
                info.mIsNativeObject = true;
            } else {
                info.mIsNativeObject = false;
            }
        }

        String index = parser.getAttributeValue(null, ObjectInfoColumns.OBJECT_INDEX);
        if (!TextUtils.isEmpty(index)) {
            info.mIndex = Integer.parseInt(index);
        }
        String type = parser.getAttributeValue(null, ObjectInfoColumns.OBJECT_TYPE);
        if (!TextUtils.isEmpty(type)) {
            info.mType = type;
        }

        return info;
    }

    public static ObjectInfo CreateFromDB(Cursor cursor) {
        ObjectInfo info = new ObjectInfo();
        info.mAppWidgetId = cursor.getInt(cursor.getColumnIndexOrThrow(ObjectInfoColumns.WIDGET_ID));
        info.mName = cursor.getString(cursor.getColumnIndexOrThrow(ObjectInfoColumns.OBJECT_NAME));
        info.mType = cursor.getString(cursor.getColumnIndexOrThrow(ObjectInfoColumns.OBJECT_TYPE));
        info.mID = cursor.getInt(cursor.getColumnIndexOrThrow(ObjectInfoColumns._ID));
        info.mShaderType = cursor.getInt(cursor.getColumnIndexOrThrow(ObjectInfoColumns.SHADER_TYPE));
        info.mSceneName = cursor.getString(cursor.getColumnIndexOrThrow(ObjectInfoColumns.SCENE_NAME));
        String componentName = cursor.getString(cursor.getColumnIndexOrThrow(ObjectInfoColumns.COMPONENT_NAME));
        if (componentName != null) {
            info.mComponentName = ComponentName.unflattenFromString(componentName);
        }
        info.mSlotType = cursor.getInt(cursor.getColumnIndexOrThrow(ObjectInfoColumns.SLOT_TYPE));
        info.mObjectSlot.mObjectID = cursor.getInt(cursor.getColumnIndexOrThrow(VesselColumns.OBJECT_ID));
        info.mObjectSlot.mVesselID = cursor.getInt(cursor.getColumnIndexOrThrow(VesselColumns.VESSEL_ID));
        info.mObjectSlot.mSlotIndex = cursor.getInt(cursor.getColumnIndexOrThrow(VesselColumns.SLOT_INDEX));
        info.mObjectSlot.mStartX = cursor.getInt(cursor.getColumnIndexOrThrow(VesselColumns.SLOT_StartX));
        info.mObjectSlot.mStartY = cursor.getInt(cursor.getColumnIndexOrThrow(VesselColumns.SLOT_StartY));
        info.mObjectSlot.mSpanX = cursor.getInt(cursor.getColumnIndexOrThrow(VesselColumns.SLOT_SpanX));
        info.mObjectSlot.mSpanY = cursor.getInt(cursor.getColumnIndexOrThrow(VesselColumns.SLOT_SpanY));

        info.mClassName = cursor.getString(cursor.getColumnIndexOrThrow(ObjectInfoColumns.CLASS_NAME));
        byte[] bytes = cursor.getBlob(cursor.getColumnIndexOrThrow(ObjectInfoColumns.SHORTCUT_ICON));
        if (bytes != null) {
            info.mShortcutIcon = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
        }

        int nativeObj = cursor.getInt(cursor.getColumnIndexOrThrow(ObjectInfoColumns.IS_NATIVE_OBJ));
        if (nativeObj > 0) {
            info.mIsNativeObject = true;
        } else {
            info.mIsNativeObject = false;
        }
        info.mShortcutUrl = cursor.getString(cursor.getColumnIndexOrThrow(ObjectInfoColumns.SHORTCUT_URL));
        info.mIndex = cursor.getInt(cursor.getColumnIndexOrThrow(ObjectInfoColumns.OBJECT_INDEX));
        info.mDisplayName = cursor.getString(cursor.getColumnIndexOrThrow(ObjectInfoColumns.DISPLAY_NAME));
        info.mFaceColor = cursor.getInt(cursor.getColumnIndexOrThrow(ObjectInfoColumns.FACE_COLOR));
        return info;
    }

    public static ObjectInfo getObjectInfoByObjectInfoName(Context context, String name) {
        if ((TextUtils.isEmpty(name)) || (context == null)) {
            return null;
        }
        String where = ObjectInfoColumns.OBJECT_NAME + "='" + name + "'";
        Cursor cursor = null;
        ContentResolver resolver = context.getContentResolver();
        try {
            cursor = resolver.query(ObjectInfoColumns.OBJECT_LEFT_JOIN_ALL, null, where, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    ObjectInfo objectInfo = ObjectInfo.CreateFromDB(cursor);
                    return objectInfo;
                }
            }
        } catch (Exception e) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public static int parseSlotType(String slotType) {
        int value = -1;
        if ("wall".equals(slotType)) {
            value = ObjectInfo.SLOT_TYPE_WALL;
        } else if ("desktop".equals(slotType)) {
            value = ObjectInfo.SLOT_TYPE_DESKTOP;
        } else if ("ground".equals(slotType)) {
            value = ObjectInfo.SLOT_TYPE_GROUND;
        } else if ("sky".equals(slotType)) {
            value = ObjectInfo.SLOT_TYPE_SKY;
        }
        return value;
    }

    public static class ObjectSlot {
        public int mVesselID = -1;
        public int mObjectID = -1;
        public int mSlotIndex = -1;
        public int mStartX = -1;
        public int mStartY = -1;
        public int mSpanX = -1;
        public int mSpanY = -1;

        public String toString() {
            return mSlotIndex + "," + mStartX + "," + mStartY + "," + mSpanX + "," + mSpanY;
        }

        public void set(ObjectSlot slot) {
            mSlotIndex = slot.mSlotIndex;
            mStartX = slot.mStartX;
            mStartY = slot.mStartY;
            mSpanX = slot.mSpanX;
            mSpanY = slot.mSpanY;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            ObjectSlot newOP = (ObjectSlot) o;
            if (newOP.mSlotIndex == mSlotIndex && newOP.mStartX == mStartX && newOP.mStartY == mStartY
                    && newOP.mSpanX == mSpanX && newOP.mSpanY == mSpanY) {
                return true;
            }
            return false;
        }

        @Override
        public ObjectSlot clone() {
            ObjectSlot newSlot = new ObjectSlot();
            newSlot.mSlotIndex = mSlotIndex;
            newSlot.mStartX = mStartX;
            newSlot.mStartY = mStartY;
            newSlot.mSpanX = mSpanX;
            newSlot.mSpanY = mSpanY;
            newSlot.mVesselID = mVesselID;
            return newSlot;
        }

        public int left() {
            return mStartX;
        }

        public int right() {
            return mStartX + mSpanX;
        }

        public int top() {
            return mStartY;
        }

        public int bottom() {
            return mStartY + mSpanY;
        }

        public boolean isAbove(ObjectSlot other) {
            return top() <= other.top() && bottom() <= other.top();
        }

        public boolean isBelow(ObjectSlot other) {
            return top() >= other.bottom() && bottom() >= other.bottom();
        }

        public boolean isLeft(ObjectSlot other) {
            return left() <= other.left() && right() <= other.left();
        }

        public boolean isRight(ObjectSlot other) {
            return left() >= other.right() && right() >= other.right();
        }

        public boolean contain(ObjectSlot slot) {
            if (left() <= slot.left() && right() >= slot.right() && top() <= slot.top() && bottom() >= slot.bottom()) {
                return true;
            }
            return false;
        }

        public boolean horizontalIntersect(ObjectSlot mySlot) {
            if (isLeft(mySlot) || isRight(mySlot)) {
                return false;
            } else {
                return true;
            }
        }

        public boolean verticalIntersect(ObjectSlot mySlot) {
            if (isAbove(mySlot) || isBelow(mySlot)) {
                return false;
            } else {
                return true;
            }
        }

        public boolean isConflict(ObjectSlot cmpSlot) {
            boolean xConflict = horizontalIntersect(cmpSlot);
            boolean YConflict = verticalIntersect(cmpSlot);
            if (xConflict && YConflict) {
                return true;
            }
            return false;
        }
    }

}
