package com.borqs.se.home3d;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

import com.borqs.se.engine.SEComponentAttribute;
import com.borqs.se.engine.SEScene;
import com.borqs.se.engine.SECommand;
import com.borqs.se.engine.SELoadResThread;
import com.borqs.se.engine.SEObject;
import com.borqs.se.engine.SESceneManager;
import com.borqs.se.engine.SETransParas;
import com.borqs.se.engine.SEVector.SEVector3f;
import com.borqs.se.engine.SEVector.SEVector4f;
import com.borqs.se.home3d.ProviderUtils.ModelColumns;
import com.borqs.se.home3d.ProviderUtils.Tables;
import com.borqs.se.widget3d.ObjectInfo;

public class ModelInfo {
    public int mID;
    public int mSlotType = -1;
    public String mName;
    public String mBoundingThemeName;
    public String mType;
    public String mSceneFile;
    public String mBasedataFile;
    public String mAssetsPath;

    public String[] mKeyWords;
    /**
     * 对物体的位置，缩放以及旋转角度做调整
     */
    public SETransParas mCompensationTrans;
    /**
     * 物体在墙面占的格子的大小， 如：1*1 到 4*4 之间
     */
    public int mSpanX;
    public int mSpanY;

    /**
     * 物体不做任何平移，直接加到场景后的最小点，以及最大点。通过这两个点能够大概知道物体中心点位置以及物体的大小
     */
    public SEVector3f mMinPoint;
    public SEVector3f mMaxPoint;

    public ComponentName mComponentName;
    private int mNativeResource;
    public ImageInfo mImageInfo;
    public List<SEComponentAttribute> mComponentAttributes;
    public MountPointData mMountPointData;
    private List<SEObject> mInstances;

    private ContentResolver mContentResolver;

    public String mProductId;
    public boolean mIsDownloaded;
    public int mActionStatus;
    public boolean mStopLoadingImage;

    public ModelInfo() {
        mNativeResource = 0;
        mSpanX = 0;
        mSpanY = 0;
        mInstances = new ArrayList<SEObject>();
        Context context = SESceneManager.getInstance().getContext();
        mContentResolver = context.getContentResolver();
        mActionStatus = ObjectInfo.ACTION_CAN_CHANGE_BIND;
        mStopLoadingImage = false;
    }

    public List<SEObject> getInstances() {
        return mInstances;
    }

    public void moveInstancesTo(ModelInfo modelInfo) {
        modelInfo.mInstances.addAll(mInstances);
        mInstances.clear();
    }

    public boolean hasInstance() {
        return mInstances.size() > 0;
    }

    public static ModelInfo CreateFromXml(XmlPullParser parser, String id, String localPath)
            throws XmlPullParserException, IOException {
        ModelInfo info = new ModelInfo();
        info.mName = parser.getAttributeValue(null, ModelColumns.OBJECT_NAME);
        info.mProductId = id == null ? HomeUtils.getDefaultObjectId(info.mName) : id;
        String actionStatus = parser.getAttributeValue(null, ModelColumns.ACTION_STATUS);
        if (!TextUtils.isEmpty(actionStatus)) {
            info.mActionStatus = Integer.parseInt(actionStatus);
        }
        info.mType = parser.getAttributeValue(null, ModelColumns.TYPE);
        info.mSceneFile = parser.getAttributeValue(null, ModelColumns.SCENE_FILE);
        info.mBasedataFile = parser.getAttributeValue(null, ModelColumns.BASEDATA_FILE);
        info.mAssetsPath = localPath != null ? localPath : parser.getAttributeValue(null, ModelColumns.ASSETS_PATH);
        String slotType = parser.getAttributeValue(null, ModelColumns.SLOT_TYPE);
        info.mSlotType = ObjectInfo.parseSlotType(slotType);
        String spanX = parser.getAttributeValue(null, ModelColumns.SLOT_SPANX);
        if (!TextUtils.isEmpty(spanX)) {
            info.mSpanX = Integer.parseInt(spanX);
        }

        String spanY = parser.getAttributeValue(null, ModelColumns.SLOT_SPANY);
        if (!TextUtils.isEmpty(spanY)) {
            info.mSpanY = Integer.parseInt(spanY);
        }

        String minPoint = parser.getAttributeValue(null, ModelColumns.MIN_POINT);
        if (!TextUtils.isEmpty(minPoint)) {
            info.mMinPoint = new SEVector3f(ProviderUtils.getFloatArray(minPoint, 3));
        }
        String maxPoint = parser.getAttributeValue(null, ModelColumns.MAX_POINT);
        if (!TextUtils.isEmpty(maxPoint)) {
            info.mMaxPoint = new SEVector3f(ProviderUtils.getFloatArray(maxPoint, 3));
        }
        // 假如关于物体的大小在配置文件中没有定义，我们自动填充 begin
        if (info.mSlotType == ObjectInfo.SLOT_TYPE_DESKTOP) {
            if (info.mMinPoint == null || info.mMaxPoint == null) {
                info.mMinPoint = new SEVector3f(-50, -50, 0);
                info.mMaxPoint = new SEVector3f(50, 50, 100);
            }
        } else if (info.mSlotType == ObjectInfo.SLOT_TYPE_WALL) {
            if (info.mMinPoint == null || info.mMaxPoint == null) {
                float sizeX = 190 * info.mSpanX;
                float sizeY = 50;
                float sizeZ = 260 * info.mSpanY;
                info.mMinPoint = new SEVector3f(-sizeX / 2, -sizeY, -sizeZ / 2);
                info.mMaxPoint = new SEVector3f(sizeX / 2, 0, sizeZ / 2);
            }
        } else if (info.mSlotType == ObjectInfo.SLOT_TYPE_GROUND) {
            if (info.mMinPoint == null || info.mMaxPoint == null) {
                info.mMinPoint = new SEVector3f(-50, -50, 0);
                info.mMaxPoint = new SEVector3f(50, 50, 420);
            }
        } else if (info.mSlotType == ObjectInfo.SLOT_TYPE_SKY) {
            if (info.mMinPoint == null || info.mMaxPoint == null) {
                info.mMinPoint = new SEVector3f(-430.9156f, -35.1319f, -53.0596f);
                info.mMaxPoint = new SEVector3f(439.2625f, 35.1319f, 52.3254f);
            }
        } else {
            if (info.mMinPoint == null || info.mMaxPoint == null) {
                info.mMinPoint = new SEVector3f(-50, -50, -50);
                info.mMaxPoint = new SEVector3f(50, 50, 50);
            }
        }
        // 假如关于物体的大小在配置文件中没有定义，我们自动填充 end
        String localTrans = parser.getAttributeValue(null, ModelColumns.LOCAL_TRANS);
        if (localTrans != null) {
            info.mCompensationTrans = new SETransParas();
            info.mCompensationTrans.init(ProviderUtils.getFloatArray(localTrans, 10));
        }
        String keyWords = parser.getAttributeValue(null, ModelColumns.KEY_WORDS);
        if (!TextUtils.isEmpty(keyWords)) {
            info.mKeyWords = ProviderUtils.getStringArray(keyWords);
        }
        String componentName = parser.getAttributeValue(null, ModelColumns.COMPONENT_NAME);
        if (componentName != null) {
            info.mComponentName = ComponentName.unflattenFromString(componentName);
        }
        XmlUtils.nextElement(parser);
        while (true) {
            if ("imageInfos".equals(parser.getName())) {
                info.mImageInfo = ImageInfo.createFromXml(parser, localPath);
            } else if ("childrenAttribute".equals(parser.getName())) {
                XmlUtils.nextElement(parser);
                while (true) {
                    if ("child".equals(parser.getName())) {
                        XmlUtils.nextElement(parser);
                        if (info.mComponentAttributes == null) {
                            info.mComponentAttributes = new ArrayList<SEComponentAttribute>();
                        }
                        SEComponentAttribute componentAttribute = new SEComponentAttribute();
                        info.mComponentAttributes.add(componentAttribute);
                        while (true) {
                            if ("geometry".equals(parser.getName())) {
                                componentAttribute.mComponentName = parser.getAttributeValue(null, "spatialName");
                                componentAttribute.mRegularName = parser.getAttributeValue(null, "regularName");

                                XmlUtils.nextElement(parser);
                            } else if ("lights".equals(parser.getName())) {
                                XmlUtils.nextElement(parser);
                                while (true) {
                                    if ("light".equals(parser.getName())) {
                                        if (componentAttribute.mLightNames == null) {
                                            componentAttribute.mLightNames = parser
                                                    .getAttributeValue(null, "lightName");
                                        } else {
                                            componentAttribute.mLightNames += ProviderUtils.SPLIT_SYMBOL
                                                    + parser.getAttributeValue(null, "lightName");
                                        }
                                        XmlUtils.nextElement(parser);
                                    } else {
                                        break;
                                    }
                                }
                            } else if ("geostatus".equals(parser.getName())) {
                                String statusValue = parser.getAttributeValue(null, "statusValue");
                                if (!TextUtils.isEmpty(statusValue)) {
                                    componentAttribute.mStatusValue = Integer.parseInt(statusValue);
                                }
                                String spatialDataValue = parser.getAttributeValue(null, "spatialDataValue");
                                if (!TextUtils.isEmpty(spatialDataValue)) {
                                    componentAttribute.mSpatialDataValue = Integer.parseInt(spatialDataValue);
                                }
                                XmlUtils.nextElement(parser);
                            } else if ("alpha".equals(parser.getName())) {
                                String alphaValue = parser.getAttributeValue(null, "alphaValue");
                                if (!TextUtils.isEmpty(alphaValue)) {
                                    componentAttribute.mAlphaValue = Float.parseFloat(alphaValue);
                                }
                                XmlUtils.nextElement(parser);
                            } else if ("effectData".equals(parser.getName())) {
                                String effectDataX = parser.getAttributeValue(null, "effectDataX");
                                String effectDataY = parser.getAttributeValue(null, "effectDataY");
                                String effectDataZ = parser.getAttributeValue(null, "effectDataZ");
                                String effectDataW = parser.getAttributeValue(null, "effectDataW");
                                componentAttribute.mEffectData = new SEVector4f(Float.parseFloat(effectDataX),
                                        Float.parseFloat(effectDataY), Float.parseFloat(effectDataZ),
                                        Float.parseFloat(effectDataW));
                                XmlUtils.nextElement(parser);
                            } else {
                                break;
                            }
                        }
                    } else {
                        break;
                    }
                }

            } else {
                break;
            }
        }
        return info;
    }

    public static ModelInfo CreateFromDB(Cursor cursor) {
        ModelInfo info = new ModelInfo();
        info.mID = cursor.getInt(cursor.getColumnIndexOrThrow(ModelColumns._ID));
        info.mProductId = cursor.getString(cursor.getColumnIndexOrThrow(ModelColumns.PRODUCT_ID));
        info.mName = cursor.getString(cursor.getColumnIndexOrThrow(ModelColumns.OBJECT_NAME));
        info.mBoundingThemeName = cursor.getString(cursor.getColumnIndexOrThrow(ModelColumns.BOUNDING_THEME_NAME));
        info.mActionStatus = cursor.getInt(cursor.getColumnIndexOrThrow(ModelColumns.ACTION_STATUS));
        info.mType = cursor.getString(cursor.getColumnIndexOrThrow(ModelColumns.TYPE));
        info.mSceneFile = cursor.getString(cursor.getColumnIndexOrThrow(ModelColumns.SCENE_FILE));
        info.mBasedataFile = cursor.getString(cursor.getColumnIndexOrThrow(ModelColumns.BASEDATA_FILE));
        info.mAssetsPath = cursor.getString(cursor.getColumnIndexOrThrow(ModelColumns.ASSETS_PATH));
        info.mSlotType = cursor.getInt(cursor.getColumnIndexOrThrow(ModelColumns.SLOT_TYPE));
        info.mIsDownloaded = cursor.getInt(cursor.getColumnIndexOrThrow(ModelColumns.IS_DOWNLOADED)) == 1;
        if (info.mIsDownloaded) {
            info.mActionStatus |= ObjectInfo.ACTION_CAN_UNINSATLL;
        }
        info.mSpanX = cursor.getInt(cursor.getColumnIndexOrThrow(ModelColumns.SLOT_SPANX));
        info.mSpanY = cursor.getInt(cursor.getColumnIndexOrThrow(ModelColumns.SLOT_SPANY));

        String minPoint = cursor.getString(cursor.getColumnIndexOrThrow(ModelColumns.MIN_POINT));
        if (!TextUtils.isEmpty(minPoint)) {
            info.mMinPoint = new SEVector3f(ProviderUtils.getFloatArray(minPoint, 3));
        }
        String maxPoint = cursor.getString(cursor.getColumnIndexOrThrow(ModelColumns.MAX_POINT));
        if (!TextUtils.isEmpty(maxPoint)) {
            info.mMaxPoint = new SEVector3f(ProviderUtils.getFloatArray(maxPoint, 3));
        }
        // 假如关于物体的大小在配置文件中没有定义，我们自动填充 begin
        if (info.mSlotType == ObjectInfo.SLOT_TYPE_DESKTOP) {
            if (info.mMinPoint == null || info.mMaxPoint == null) {
                info.mMinPoint = new SEVector3f(-50, -50, 0);
                info.mMaxPoint = new SEVector3f(50, 50, 100);
            }
        } else if (info.mSlotType == ObjectInfo.SLOT_TYPE_WALL) {
            if (info.mMinPoint == null || info.mMaxPoint == null) {
                float sizeX = 190 * info.mSpanX;
                float sizeY = 50;
                float sizeZ = 260 * info.mSpanY;
                info.mMinPoint = new SEVector3f(-sizeX / 2, -sizeY, -sizeZ / 2);
                info.mMaxPoint = new SEVector3f(sizeX / 2, 0, sizeZ / 2);
            }
        } else if (info.mSlotType == ObjectInfo.SLOT_TYPE_GROUND) {
            if (info.mMinPoint == null || info.mMaxPoint == null) {
                info.mMinPoint = new SEVector3f(-50, -50, 0);
                info.mMaxPoint = new SEVector3f(50, 50, 420);
            }
        } else if (info.mSlotType == ObjectInfo.SLOT_TYPE_SKY) {
            if (info.mMinPoint == null || info.mMaxPoint == null) {
                info.mMinPoint = new SEVector3f(-430.9156f, -35.1319f, -53.0596f);
                info.mMaxPoint = new SEVector3f(439.2625f, 35.1319f, 52.3254f);
            }
        } else {
            if (info.mMinPoint == null || info.mMaxPoint == null) {
                info.mMinPoint = new SEVector3f(-50, -50, -50);
                info.mMaxPoint = new SEVector3f(50, 50, 50);
            }
        }
        // 假如关于物体的大小在配置文件中没有定义，我们自动填充 end
        String localTrans = cursor.getString(cursor.getColumnIndexOrThrow(ModelColumns.LOCAL_TRANS));
        if (localTrans != null) {
            info.mCompensationTrans = new SETransParas();
            info.mCompensationTrans.init(ProviderUtils.getFloatArray(localTrans, 10));
        }

        String keyWords = cursor.getString(cursor.getColumnIndexOrThrow(ModelColumns.KEY_WORDS));
        if (!TextUtils.isEmpty(keyWords)) {
            info.mKeyWords = ProviderUtils.getStringArray(keyWords);
        }
        String componentName = cursor.getString(cursor.getColumnIndexOrThrow(ModelColumns.COMPONENT_NAME));
        if (componentName != null) {
            info.mComponentName = ComponentName.unflattenFromString(componentName);
        }
        String where = ModelColumns._ID + "=" + info.mID;
        Context context = SESceneManager.getInstance().getContext();
        ContentResolver resolver = context.getContentResolver();
        Cursor imageCursor = resolver.query(ModelColumns.IMAGE_INFO_URI, null, where, null, null);
        while (imageCursor.moveToNext()) {
            ImageItem imageItem = new ImageItem();
            imageItem.mImageName = imageCursor.getString(imageCursor.getColumnIndexOrThrow(ModelColumns.IMAGE_NAME));
            imageItem.mPath = imageCursor.getString(imageCursor.getColumnIndexOrThrow(ModelColumns.IMAGE_PATH));
            imageItem.mNewPath = imageCursor.getString(imageCursor.getColumnIndexOrThrow(ModelColumns.IMAGE_NEW_PATH));
            if (info.mImageInfo == null) {
                info.mImageInfo = new ImageInfo();
            }
            info.mImageInfo.mImageItems.add(imageItem);
        }
        imageCursor.close();

        Cursor componentCursor = resolver.query(ModelColumns.COMPONENT_INFO_URI, null, where, null, null);
        if (componentCursor != null) {
            while (componentCursor.moveToNext()) {
                SEComponentAttribute componentAttribute = new SEComponentAttribute();
                componentAttribute.mComponentName = componentCursor.getString(componentCursor
                        .getColumnIndexOrThrow(ModelColumns.COMPONENT_OBJECT));
                componentAttribute.mRegularName = componentCursor.getString(componentCursor
                        .getColumnIndexOrThrow(ModelColumns.COMPONENT_REGULAR_NAME));
                componentAttribute.mLightNames = componentCursor.getString(componentCursor
                        .getColumnIndexOrThrow(ModelColumns.LIGHT_NAMES));
                componentAttribute.mStatusValue = componentCursor.getInt(componentCursor
                        .getColumnIndexOrThrow(ModelColumns.STATUS_VALUE));
                componentAttribute.mSpatialDataValue = componentCursor.getInt(componentCursor
                        .getColumnIndexOrThrow(ModelColumns.SPATIAL_DATA_VALUE));
                componentAttribute.mAlphaValue = componentCursor.getFloat(componentCursor
                        .getColumnIndexOrThrow(ModelColumns.ALPHA_VALUE));
                String effectData = componentCursor.getString(componentCursor
                        .getColumnIndexOrThrow(ModelColumns.EFFECT_DATA));
                float[] effectDatas = ProviderUtils.getFloatArray(effectData, 4);
                componentAttribute.mEffectData = new SEVector4f(effectDatas);
                if (info.mComponentAttributes == null) {
                    info.mComponentAttributes = new ArrayList<SEComponentAttribute>();
                }
                info.mComponentAttributes.add(componentAttribute);

            }
            componentCursor.close();
        }
        info.loadMountPointData();
        return info;
    }

    public void saveToDB(final SQLiteDatabase db) {
        String where = ModelColumns.OBJECT_NAME + "='" + mName + "'";
        Cursor cursor = db.query(Tables.MODEL_INFO, null, where, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                mID = cursor.getInt(cursor.getColumnIndexOrThrow(ModelColumns._ID));
                ContentValues values = new ContentValues();
                updateDB(values);
                db.update(Tables.MODEL_INFO, values, where, null);
                cursor.close();
                if (mImageInfo != null) {
                    updateImageInfo(db);
                }
                return;
            }
            cursor.close();
        }
        ContentValues values = new ContentValues();
        saveToDB(values);
        mID = (int) db.insert(Tables.MODEL_INFO, null, values);
        if (mImageInfo != null) {
            saveImageInfo(db);
        }
        if (mComponentAttributes != null) {
            saveComponentInfo(db);
        }

    }

    private void updateImageInfo(SQLiteDatabase db) {
        String table = Tables.IMAGE_INFO;
        for (ImageItem imageItem : mImageInfo.mImageItems) {
            String where = getImageQueryByName(imageItem.mImageName);
            Cursor cursor = db.query(table, null, where, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    ContentValues values = new ContentValues();
                    values.put(ModelColumns.IMAGE_PATH, imageItem.mPath);
                    db.update(table, values, where, null);
                    cursor.close();
                    return;
                }
                cursor.close();
            }
            ContentValues values = new ContentValues();
            values.put(ModelColumns._ID, mID);
            values.put(ModelColumns.IMAGE_PATH, imageItem.mPath);
            values.put(ModelColumns.IMAGE_NEW_PATH, imageItem.mNewPath);
            values.put(ModelColumns.IMAGE_NAME, imageItem.mImageName);
            db.insert(table, null, values);

        }

    }

    private void saveImageInfo(SQLiteDatabase db) {
        String table = Tables.IMAGE_INFO;
        for (ImageItem imageItem : mImageInfo.mImageItems) {
            ContentValues values = new ContentValues();
            values.put(ModelColumns._ID, mID);
            values.put(ModelColumns.IMAGE_PATH, imageItem.mPath);
            values.put(ModelColumns.IMAGE_NEW_PATH, imageItem.mNewPath);
            values.put(ModelColumns.IMAGE_NAME, imageItem.mImageName);
            db.insert(table, null, values);
        }
    }

    private void saveComponentInfo(SQLiteDatabase db) {
        String table = Tables.COMPONENT_INFO;
        for (SEComponentAttribute componentAttribute : mComponentAttributes) {
            ContentValues values = new ContentValues();
            values.put(ModelColumns._ID, mID);
            values.put(ModelColumns.COMPONENT_OBJECT, componentAttribute.mComponentName);
            values.put(ModelColumns.LIGHT_NAMES, componentAttribute.mLightNames);
            values.put(ModelColumns.COMPONENT_REGULAR_NAME, componentAttribute.mRegularName);
            values.put(ModelColumns.STATUS_VALUE, componentAttribute.mStatusValue);
            values.put(ModelColumns.SPATIAL_DATA_VALUE, componentAttribute.mSpatialDataValue);
            values.put(ModelColumns.ALPHA_VALUE, componentAttribute.mAlphaValue);
            values.put(ModelColumns.EFFECT_DATA, ProviderUtils.floatArrayToString(componentAttribute.mEffectData.mD));
            db.insert(table, null, values);
        }

    }

    public void saveToDB() {
        String where = ModelColumns.OBJECT_NAME + "='" + mName + "'";
        Cursor cursor = mContentResolver.query(ModelColumns.CONTENT_URI, null, where, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                mID = cursor.getInt(cursor.getColumnIndexOrThrow(ModelColumns._ID));
                ContentValues values = new ContentValues();
                updateDB(values);
                mContentResolver.update(ModelColumns.CONTENT_URI, values, where, null);
                cursor.close();
                // Delete image info from the image_info table for avoid
                // inserting duplicate records
                where = ModelColumns._ID + " = " + mID;
                mContentResolver.delete(ModelColumns.IMAGE_INFO_URI, where, null);
                if (mImageInfo != null) {
                    updateImageInfo();
                }
                return;
            }
            cursor.close();
        }
        ContentValues values = new ContentValues();
        saveToDB(values);
        Uri insertUri = mContentResolver.insert(ModelColumns.CONTENT_URI, values);
        mID = (int) ContentUris.parseId(insertUri);
        if (mImageInfo != null) {
            saveImageInfo();
        }
        if (mComponentAttributes != null) {
            saveComponentInfo();
        }

    }

    public String getImageOldKey(String imageName) {
        ImageItem imageItem = mImageInfo.getImageItem(imageName);
        if (imageItem != null) {
            return imageItem.mPath;
        }
        return null;
    }

    public String getImageNewKey(String imageName) {
        ImageItem imageItem = mImageInfo.getImageItem(imageName);
        if (imageItem != null) {
            return imageItem.mNewPath;
        }
        return null;
    }

    public void updateImageKey(final String imageName, final String newKey) {
        UpdateDBThread.getInstance().process(new Runnable() {
            public void run() {
                ImageItem imageItem = mImageInfo.getImageItem(imageName);
                if (imageItem != null) {
                    imageItem.mNewPath = TextUtils.isEmpty(newKey) ? imageItem.mPath : newKey;
                    ContentValues values = new ContentValues();
                    values.put(ModelColumns.IMAGE_NEW_PATH, imageItem.mNewPath);
                    String where = getImageQueryByName(imageItem.mImageName);
                    mContentResolver.update(ModelColumns.IMAGE_INFO_URI, values, where, null);
                }
            }
        });
    }

    public void updateImageItem(final ImageItem imageItem) {
        UpdateDBThread.getInstance().process(new Runnable() {
            public void run() {
                if (imageItem != null) {
                    ContentValues values = new ContentValues();
                    values.put(ModelColumns.IMAGE_NEW_PATH, imageItem.mNewPath);
                    String where = getImageQueryByName(imageItem.mImageName);
                    mContentResolver.update(ModelColumns.IMAGE_INFO_URI, values, where, null);
                }
            }
        });
    }

    public String getImageQueryByName(String name) {
        return ModelColumns.IMAGE_NAME + "='" + name + "' AND " + ModelColumns._ID + "=" + mID;
    }

    public void updateImageInfo() {
        for (ImageItem imageItem : mImageInfo.mImageItems) {
            String where = getImageQueryByName(imageItem.mImageName);
            Cursor cursor = mContentResolver.query(ModelColumns.IMAGE_INFO_URI, null, where, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    ContentValues values = new ContentValues();
                    values.put(ModelColumns.IMAGE_PATH, imageItem.mPath);
                    mContentResolver.update(ModelColumns.IMAGE_INFO_URI, values, where, null);
                    cursor.close();
                    return;
                }
                cursor.close();
            }
            ContentValues values = new ContentValues();
            values.put(ModelColumns._ID, mID);
            values.put(ModelColumns.IMAGE_PATH, imageItem.mPath);
            values.put(ModelColumns.IMAGE_NEW_PATH, imageItem.mNewPath);
            values.put(ModelColumns.IMAGE_NAME, imageItem.mImageName);
            mContentResolver.insert(ModelColumns.IMAGE_INFO_URI, values);
        }
    }

    public void saveImageInfo() {
        for (ImageItem imageItem : mImageInfo.mImageItems) {
            ContentValues values = new ContentValues();
            values.put(ModelColumns._ID, mID);
            values.put(ModelColumns.IMAGE_PATH, imageItem.mPath);
            values.put(ModelColumns.IMAGE_NEW_PATH, imageItem.mNewPath);
            values.put(ModelColumns.IMAGE_NAME, imageItem.mImageName);
            mContentResolver.insert(ModelColumns.IMAGE_INFO_URI, values);
        }

    }

    private void saveComponentInfo() {
        for (SEComponentAttribute componentAttribute : mComponentAttributes) {
            ContentValues values = new ContentValues();
            values.put(ModelColumns._ID, mID);
            values.put(ModelColumns.COMPONENT_OBJECT, componentAttribute.mComponentName);
            values.put(ModelColumns.LIGHT_NAMES, componentAttribute.mLightNames);
            values.put(ModelColumns.COMPONENT_REGULAR_NAME, componentAttribute.mRegularName);
            values.put(ModelColumns.STATUS_VALUE, componentAttribute.mStatusValue);
            values.put(ModelColumns.SPATIAL_DATA_VALUE, componentAttribute.mSpatialDataValue);
            values.put(ModelColumns.ALPHA_VALUE, componentAttribute.mAlphaValue);
            values.put(ModelColumns.EFFECT_DATA, ProviderUtils.floatArrayToString(componentAttribute.mEffectData.mD));
            mContentResolver.insert(ModelColumns.COMPONENT_INFO_URI, values);

        }
    }

    private void updateDB(ContentValues values) {
        values.put(ModelColumns.SCENE_FILE, mSceneFile);
        values.put(ModelColumns.BASEDATA_FILE, mBasedataFile);
        values.put(ModelColumns.ASSETS_PATH, mAssetsPath);
        values.put(ModelColumns.SLOT_SPANX, mSpanX);
        values.put(ModelColumns.SLOT_SPANY, mSpanY);
        values.put(ModelColumns.PRODUCT_ID, mProductId);
        values.put(ModelColumns.IS_DOWNLOADED, mIsDownloaded);
        if (mCompensationTrans != null) {
            values.put(ModelColumns.LOCAL_TRANS, mCompensationTrans.toString());
        }
        if (mKeyWords != null) {
            values.put(ModelColumns.KEY_WORDS, ProviderUtils.stringArrayToString(mKeyWords));
        }
    }

    private void saveToDB(ContentValues values) {
        values.put(ModelColumns.OBJECT_NAME, mName);
        if (mBoundingThemeName != null) {
            values.put(ModelColumns.BOUNDING_THEME_NAME, mBoundingThemeName);
        }
        if (mIsDownloaded) {
            mActionStatus |= ObjectInfo.ACTION_CAN_UNINSATLL;
        }
        values.put(ModelColumns.ACTION_STATUS, mActionStatus);
        values.put(ModelColumns.TYPE, mType);
        values.put(ModelColumns.SCENE_FILE, mSceneFile);
        values.put(ModelColumns.BASEDATA_FILE, mBasedataFile);
        values.put(ModelColumns.ASSETS_PATH, mAssetsPath);
        values.put(ModelColumns.SLOT_TYPE, mSlotType);
        values.put(ModelColumns.SLOT_SPANX, mSpanX);
        values.put(ModelColumns.SLOT_SPANY, mSpanY);
        if (mMinPoint != null) {
            values.put(ModelColumns.MIN_POINT, mMinPoint.toString());
        }
        if (mMaxPoint != null) {
            values.put(ModelColumns.MAX_POINT, mMaxPoint.toString());
        }
        values.put(ModelColumns.PRODUCT_ID, mProductId);
        values.put(ModelColumns.IS_DOWNLOADED, mIsDownloaded);
        if (mCompensationTrans != null) {
            values.put(ModelColumns.LOCAL_TRANS, mCompensationTrans.toString());
        }

        if (mKeyWords != null) {
            values.put(ModelColumns.KEY_WORDS, ProviderUtils.stringArrayToString(mKeyWords));
        }
        if (mComponentName != null) {
            values.put(ModelColumns.COMPONENT_NAME, mComponentName.flattenToShortString());
        }
    }

    private LoadingModelTask mLoadingModelTask;

    public boolean isOnLoading() {
        return mLoadingModelTask != null && mLoadingModelTask.isOnLoading();
    }

    public void stopLoading() {
        if (mLoadingModelTask != null) {
            mLoadingModelTask.stop();
            SELoadResThread.getInstance().cancel(mLoadingModelTask);
        }
        mStopLoadingImage = true;
    }

    public void load3DMAXModel(SEScene scene, SEObject parent, SEObject model, boolean removeTopNode, Runnable finish) {
        stopLoading();
        mStopLoadingImage = false;
        mLoadingModelTask = new LoadingModelTask(scene, parent, model, removeTopNode, finish);
        SELoadResThread.getInstance().process(mLoadingModelTask);
    }

    private class LoadingModelTask implements Runnable {
        private SEScene mSEScene;
        private SEObject mParent;
        private SEObject mModel;
        private boolean mRemoveTopNode;
        private Runnable mFinish;
        private boolean mStopLoading;
        private int mCommandID;
        private boolean mIsOnLoading = false;

        public LoadingModelTask(SEScene scene, SEObject parent, SEObject model, boolean removeTopNode, Runnable finish) {
            mSEScene = scene;
            mParent = parent;
            mModel = model;
            mRemoveTopNode = removeTopNode;
            mFinish = finish;
            mStopLoading = false;
            mIsOnLoading = true;
            mCommandID = (int) System.currentTimeMillis();
        }

        public boolean isOnLoading() {
            return mIsOnLoading;
        }

        public void stop() {
            mStopLoading = true;
            mIsOnLoading = false;
            mSEScene.removeMessage(mCommandID);
        }

        public void run() {
            if (mStopLoading) {
                mStopLoading = false;
                return;
            }
            mNativeResource = SEScene.loadResource_JNI(mAssetsPath + "/" + mSceneFile, mAssetsPath + "/"
                    + mBasedataFile);
            if (mStopLoading) {
                mStopLoading = false;
                SEScene.deleteResource_JNI(mNativeResource);
                mNativeResource = 0;
                return;
            }
            new SECommand(mSEScene, mCommandID) {
                public void run() {
                    if (mStopLoading) {
                        mStopLoading = false;
                        SEScene.deleteResource_JNI(mNativeResource);
                        mNativeResource = 0;
                        return;
                    }
                    add3DMAXModel(mSEScene, mParent, mModel, mRemoveTopNode);
                    if (mFinish != null) {
                        mFinish.run();
                    }
                    mIsOnLoading = false;
                }
            }.execute();
        }
    }

    private void add3DMAXModel(SEScene scene, SEObject parent, SEObject model, boolean removeTopNode) {
        if (mNativeResource != 0) {
            for (final ImageItem imageItem : mImageInfo.mImageItems) {
                SEObject.applyImage_JNI(imageItem.mImageName, imageItem.mNewPath);
            }
            scene.inflateResource_JNI(mNativeResource, model.mName, model.mIndex, parent.mName, parent.mIndex,
                    removeTopNode);
            model.setHasBeenAddedToEngine(true);
            if (mComponentAttributes != null) {
                for (SEComponentAttribute componentAttribute : mComponentAttributes) {
                    if (componentAttribute.mStatusValue != 0 || componentAttribute.mSpatialDataValue != 0) {
                        SEObject componentObject = new SEObject(scene, componentAttribute.mComponentName);
                        componentObject.setComponentAttribute_JNI(componentAttribute);
                    }
                }
            }
            loadImage(scene, parent, model);
        }
    }

    private void loadImage(final SEScene scene, final SEObject parent, final SEObject model) {
        for (final ImageItem imageItem : mImageInfo.mImageItems) {
            loadImageItem(scene, parent, model, imageItem);
        }

    }

    private void loadImageItem(final SEScene scene, final SEObject parent, final SEObject model,
            final ImageItem imageItem) {
        boolean exist = SEObject.isImageExist_JNI(imageItem.mNewPath);
        if (!exist && !mStopLoadingImage) {
            SELoadResThread.getInstance().process(new Runnable() {
                public void run() {
                    final int imageData = SEObject.loadImageData_JNI(imageItem.mNewPath);
                    if (imageData != 0) {
                        new SECommand(scene) {
                            public void run() {
                                if (!mStopLoadingImage) {
                                    SEObject.addImageData_JNI(imageItem.mNewPath, imageData);
                                } else {
                                    SEObject.releaseImageData_JNI(imageData);
                                }
                            }
                        }.execute();
                    } else {
                        if (!imageItem.mNewPath.equals(imageItem.mPath)) {
                            imageItem.mNewPath = imageItem.mPath;
                            new SECommand(scene) {
                                public void run() {
                                    loadImageItem(scene, parent, model, imageItem);
                                }
                            }.execute();
                        }
                    }
                }
            });
        }
    }

    public SEComponentAttribute findComponentAttrByRegularName(String regularName) {
        if (mComponentAttributes != null) {
            for (SEComponentAttribute componentAttribute : mComponentAttributes) {
                if (regularName.equals(componentAttribute.mRegularName)) {
                    return componentAttribute;
                }
            }
        }
        return null;
    }

    public List<SEComponentAttribute> findComponentAttrsByRegularName(String regularName) {
        if (mComponentAttributes != null) {
            List<SEComponentAttribute> componentAttributes = null;
            for (SEComponentAttribute componentAttribute : mComponentAttributes) {
                if (regularName.equals(componentAttribute.mRegularName)) {
                    if (componentAttributes == null) {
                        componentAttributes = new ArrayList<SEComponentAttribute>();
                    }
                    componentAttributes.add(componentAttribute);
                }
            }
            return componentAttributes;
        }
        return null;
    }

    public static class ImageInfo {
        public List<ImageItem> mImageItems = new ArrayList<ImageItem>();

        public static ImageInfo createFromXml(XmlPullParser parser, String assetPath) throws XmlPullParserException,
                IOException {
            ImageInfo imageInfo = new ImageInfo();
            XmlUtils.nextElement(parser);
            while (true) {
                if ("imageInfo".equals(parser.getName())) {
                    ImageItem imageItem = new ImageItem();
                    imageItem.mImageName = parser.getAttributeValue(null, ModelColumns.IMAGE_NAME);
                    if (assetPath != null) {
                        imageItem.mPath = assetPath + "/" + parser.getAttributeValue(null, ModelColumns.IMAGE_PATH);
                    } else {
                        imageItem.mPath = parser.getAttributeValue(null, ModelColumns.IMAGE_PATH);
                    }
                    imageItem.mNewPath = parser.getAttributeValue(null, "imageNewPath");
                    if (TextUtils.isEmpty(imageItem.mNewPath)) {
                        imageItem.mNewPath = imageItem.mPath;
                    } else {
                        File file = new File(imageItem.mNewPath);
                        if (!file.exists()) {
                            imageItem.mNewPath = imageItem.mPath;
                        }
                    }
                    if (!imageInfo.mImageItems.contains(imageItem)) {
                        imageInfo.mImageItems.add(imageItem);
                    }
                    XmlUtils.nextElement(parser);
                } else {
                    break;
                }
            }
            return imageInfo;
        }

        public ImageItem getImageItem(String imageName) {
            for (ImageItem item : mImageItems) {
                if (item.mImageName.equals(imageName)) {
                    return item;
                }
            }
            return null;
        }
    }

    public static class ImageItem {

        @Override
        public boolean equals(Object o) {
            ImageItem newItem = (ImageItem) o;
            return mImageName.equals(newItem.mImageName);
        }

        public String mImageName;
        public String mPath;
        public String mNewPath;
    }

    private HashMap<String, String> spatialAttributes;

    /**
     * 模型的特殊配置都存放在special_config.xml文件中，如相框截多大的图
     */
    public String getSpecialAttribute(Context context, String name) {
        if (spatialAttributes == null) {
            spatialAttributes = HomeUtils.loadSpecialAttributeForModel(context, mAssetsPath);
        }
        if (spatialAttributes != null) {
            return spatialAttributes.get(name);
        }
        return null;
    }

    public void loadMountPointData() {
        Context context = SESceneManager.getInstance().getContext();
        try {
            if (mAssetsPath.contains("assets")) {
                // length of "assets/" is 7
                String filePath = mAssetsPath.substring(7) + "/mountpoint.xml";
                InputStream is = context.getAssets().open(filePath);
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(is, "utf-8");
                XmlUtils.beginDocument(parser, "mountPointsConfig");
                mMountPointData = MountPointData.createfromXML(parser);
                if (HomeUtils.DEBUG) {
                    Log.d(HomeUtils.TAG, "loadMountPointData of :" + mName);
                    Log.d(HomeUtils.TAG, "############" + mMountPointData);
                }
                is.close();
            } else {
                FileInputStream is = new FileInputStream(mAssetsPath + "/mountpoint.xml");
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(is, "utf-8");
                XmlUtils.beginDocument(parser, "mountPointsConfig");
                mMountPointData = MountPointData.createfromXML(parser);
                if (HomeUtils.DEBUG) {
                    Log.d(HomeUtils.TAG, "loadMountPointData of :" + mName);
                    Log.d(HomeUtils.TAG, "############" + mMountPointData);
                }
                is.close();
            }
        } catch (Exception e) {

        }
    }
}
