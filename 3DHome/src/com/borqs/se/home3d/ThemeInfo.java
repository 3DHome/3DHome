package com.borqs.se.home3d;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.borqs.se.engine.SECameraData;
import com.borqs.se.engine.SEUtils;
import com.borqs.se.engine.SEVector.SEVector3f;
import com.borqs.se.home3d.ProviderUtils.ObjectInfoColumns;
import com.borqs.se.home3d.ProviderUtils.Tables;
import com.borqs.se.home3d.ProviderUtils.ThemeColumns;
import com.borqs.se.widget3d.ObjectInfo;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Xml;

public class ThemeInfo {
    public static final String DEFAULT_SCENE_NAME = "home8";
    public boolean mIsApplyed = false;
    public int mID;
    public String mThemeName;
    public String mSceneName = DEFAULT_SCENE_NAME;
    public String mFilePath;
    public boolean mIsDownloaded = false;
    public String mProductID;
    public SEVector3f mCurrentCameraLoction;
    public float mCurrentCameraFov;
    public int mCurrentWallIndex;
    
    private DefaultObjectsInfo mDefaultObjectsInfo;
    public String mHouseName;
    private CameraInfo mCameraInfo;
    private List<ModelInfo> mBoundingModel;
    public SECameraData mSECameraData;
    public float mBestCameraFov;
    public SEVector3f mBestCameraLocation;
    public float mNearestCameraFov = 26.274373f;
    public SEVector3f mNearestCameraLocation = new SEVector3f(0, -753, 430);
    public float mFarthestCameraFov = 39.73937f;
    public SEVector3f mFarthestCameraLocation = new SEVector3f(0, -1000, 430);
    private boolean mHadBeenInitFromXML = false;
    private static class DefaultObjectsInfo {
        public String mSceneName;
        public ObjectInfo mHouse;
        public ObjectInfo mDesk;
        public ObjectInfo mSky;
        public List<ObjectInfo> mObjectsOnGround;
        public List<ObjectInfo> mObjectsOnDesk;
        public List<ObjectInfo> mObjectsOnWall;
    }

    public void updateWallIndex(final Context context) {
        UpdateDBThread.getInstance().process(new Runnable() {
            public void run() {
                String where = ThemeColumns._ID + "=" + mID;
                ContentValues values = new ContentValues();
                values.put(ThemeColumns.WALL_INDEX, mCurrentWallIndex);
                context.getContentResolver().update(ThemeColumns.CONTENT_URI, values, where, null);
            }
        });
    }

    public void updateCameraDataToDB(final Context context, SEVector3f location, float fov) {
        mCurrentCameraLoction = location.clone();
        mCurrentCameraFov = fov;
        UpdateDBThread.getInstance().process(new Runnable() {
            public void run() {
                String where = ThemeColumns._ID + "=" + mID;
                ContentValues values = new ContentValues();
                values.put(ThemeColumns.CAMERA_FOV, mCurrentCameraFov);
                values.put(ThemeColumns.CAMERA_LOCATION, mCurrentCameraLoction.toString());
                context.getContentResolver().update(ThemeColumns.CONTENT_URI, values, where, null);
            }
        });
    }

    public static ThemeInfo CreateFromXml(Context context, XmlPullParser parser, String id, String filePath)
            throws XmlPullParserException, IOException {
        ThemeInfo info = new ThemeInfo();
        info.mProductID = id;
        info.mThemeName = parser.getAttributeValue(null, "themeName");
        info.mHouseName = parser.getAttributeValue(null, "houseName");
        if (info.mHouseName == null) {
            info.mHouseName = "house_" + info.mThemeName + "_theme";
        }
        if (filePath == null) {
            info.mFilePath = parser.getAttributeValue(null, "filePath");
        } else {
            info.mFilePath = filePath;
        }
        String isApplyed = parser.getAttributeValue(null, "isApplyed");
        if (!TextUtils.isEmpty(isApplyed)) {
            info.mIsApplyed = Integer.parseInt(isApplyed) == 1 ? true : false;
        }
        boolean isAsset = filePath.startsWith("assets") ? true : false;
        // 开始解析房间中摆放了什么物体，假如default_objects_in_scene.xml文件没有那么使用默认配置，有的话切换场景时物体的摆放会更改成自己的配置
        if (!isAsset) {
            try {
                FileInputStream fis = new FileInputStream(info.mFilePath + "/default_objects_in_scene.xml");
                parser = Xml.newPullParser();
                parser.setInput(fis, "utf-8");
                XmlUtils.beginDocument(parser, "sceneConfig");
                info.mDefaultObjectsInfo = loadObjectsInScene(info, parser);
                fis.close();
            } catch (Exception e) {

            }
        } else {
            try {
                InputStream is = context.getAssets()
                        .open(info.mFilePath.substring(7) + "/default_objects_in_scene.xml");
                parser = Xml.newPullParser();
                parser.setInput(is, "utf-8");
                XmlUtils.beginDocument(parser, "sceneConfig");
                info.mDefaultObjectsInfo = loadObjectsInScene(info, parser);
                is.close();
            } catch (Exception e) {

            }
        }
        if (info.mDefaultObjectsInfo != null) {
            if (TextUtils.isEmpty(info.mDefaultObjectsInfo.mSceneName)) {
                info.mSceneName = "scene_of_" + info.mThemeName;
                info.mDefaultObjectsInfo.mSceneName = info.mSceneName;
            } else {
                info.mSceneName = info.mDefaultObjectsInfo.mSceneName;
            }
        } else {
            try {
                InputStream is = context.getAssets().open("base/default_objects_in_scene.xml");
                parser = Xml.newPullParser();
                parser.setInput(is, "utf-8");
                XmlUtils.beginDocument(parser, "sceneConfig");
                info.mDefaultObjectsInfo = loadObjectsInScene(info, parser);
                is.close();
            } catch (Exception e) {
            }
        }

        info.mBoundingModel = loadAllBoundingModelInfo(context, isAsset, filePath);

        return info;
    }

    public void initFromXML(Context context) {
        if (mHadBeenInitFromXML) {
            return;
        }
        mHadBeenInitFromXML = true;
        fixUpgrade(context, mFilePath);
        InputStream is = null;
        try {
            boolean isAsset = mFilePath.startsWith("assets") ? true : false;
            if (!isAsset) {
                is = new FileInputStream(mFilePath + File.separator + "camera_config.xml");
            } else {
                is = context.getAssets().open(mFilePath.substring(7) + File.separator + "camera_config.xml");
            }
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(is, "utf-8");
            XmlUtils.beginDocument(parser, "config");
            mCameraInfo = CameraInfo.CreateFromXml(context, parser);

        } catch (Exception e) {

        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        mSECameraData = mCameraInfo.mSECameraData;
        if (mCurrentCameraLoction != null) {
            mSECameraData.mLocation = mCurrentCameraLoction.clone();
            mSECameraData.mFov = mCurrentCameraFov;
        } else {
            mCurrentCameraLoction = mCameraInfo.mSECameraData.mLocation.clone();
            mCurrentCameraFov = mCameraInfo.mSECameraData.mFov;
        }
        mBestCameraFov = mCameraInfo.mBestCameraFov;
        mBestCameraLocation = mCameraInfo.mBestCameraLocation;
        mNearestCameraFov = mCameraInfo.mNearestCameraFov;
        mNearestCameraLocation = mCameraInfo.mNearestCameraLocation;
        mFarthestCameraFov = mCameraInfo.mFarthestCameraFov;
        mFarthestCameraLocation = mCameraInfo.mFarthestCameraLocation;
    }

    /**
     * 主题在新版本中可能文件缺少或者格式不对
     */
    private void fixUpgrade(Context context, String themePath) {
        boolean isAsset = mFilePath.startsWith("assets") ? true : false;
        if (!isAsset) {
            InputStream is = null;
            try {
                File destFile = new File(themePath + File.separator + "camera_config.xml");
                if (!destFile.exists()) {
                    is = context.getResources().getAssets().open("base/lightenTexture/camera_config.xml");
                    destFile.createNewFile();
                    SEUtils.copyToFile(is, destFile);
                    is.close();
                    
                    is = context.getResources().getAssets().open("base/lightenTexture/house/special_config.xml");
                    destFile = new File(themePath + File.separator + "special_config.xml");
                    destFile.createNewFile();
                    SEUtils.copyToFile(is, destFile);
                    is.close();
                }

            } catch (Exception e) {
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception e1) {

                    }
                }
            }
        }

    }
    
    private static DefaultObjectsInfo loadObjectsInScene(ThemeInfo themeInfo, XmlPullParser parser) throws IOException,
            XmlPullParserException {
        DefaultObjectsInfo defaultObjectInfo = new DefaultObjectsInfo();
        defaultObjectInfo.mSceneName = parser.getAttributeValue(null, "sceneName");
        XmlUtils.nextElement(parser);
        while (true) {
            if ("skyInScene".equals(parser.getName())) {
                XmlUtils.nextElement(parser);
                while (true) {
                    if ("Object".equals(parser.getName())) {
                        defaultObjectInfo.mSky = ObjectInfo.CreateFromXml(parser);
                        XmlUtils.nextElement(parser);
                    } else {
                        break;
                    }
                }
            } else if ("houseInScene".equals(parser.getName())) {
                XmlUtils.nextElement(parser);
                while (true) {
                    if ("Object".equals(parser.getName())) {
                        defaultObjectInfo.mHouse = ObjectInfo.CreateFromXml(parser);
                        XmlUtils.nextElement(parser);
                    } else {
                        break;
                    }
                }
            } else if ("deskInScene".equals(parser.getName())) {
                XmlUtils.nextElement(parser);
                while (true) {
                    if ("Object".equals(parser.getName())) {
                        defaultObjectInfo.mDesk = ObjectInfo.CreateFromXml(parser);
                        XmlUtils.nextElement(parser);
                    } else {
                        break;
                    }
                }
            } else if ("objectsOnwall".equals(parser.getName())) {
                XmlUtils.nextElement(parser);
                while (true) {
                    if ("Object".equals(parser.getName())) {
                        if (defaultObjectInfo.mObjectsOnWall == null) {
                            defaultObjectInfo.mObjectsOnWall = new ArrayList<ObjectInfo>();
                        }
                        defaultObjectInfo.mObjectsOnWall.add(ObjectInfo.CreateFromXml(parser));
                        XmlUtils.nextElement(parser);
                    } else {
                        break;
                    }
                }
            } else if ("objectsOnGround".equals(parser.getName())) {
                XmlUtils.nextElement(parser);
                while (true) {
                    if ("Object".equals(parser.getName())) {
                        if (defaultObjectInfo.mObjectsOnGround == null) {
                            defaultObjectInfo.mObjectsOnGround = new ArrayList<ObjectInfo>();
                        }
                        defaultObjectInfo.mObjectsOnGround.add(ObjectInfo.CreateFromXml(parser));
                        XmlUtils.nextElement(parser);
                    } else {
                        break;
                    }
                }
            } else if ("objectsOnDesktop".equals(parser.getName())) {
                XmlUtils.nextElement(parser);
                while (true) {
                    if ("Object".equals(parser.getName())) {
                        if (defaultObjectInfo.mObjectsOnDesk == null) {
                            defaultObjectInfo.mObjectsOnDesk = new ArrayList<ObjectInfo>();
                        }
                        defaultObjectInfo.mObjectsOnDesk.add(ObjectInfo.CreateFromXml(parser));
                        XmlUtils.nextElement(parser);
                    } else {
                        break;
                    }
                }
            } else {
                break;
            }
        }
        return defaultObjectInfo;
    }

    public static ThemeInfo CreateFromDB(Cursor cursor) {
        ThemeInfo info = new ThemeInfo();
        info.mID = cursor.getInt(cursor.getColumnIndexOrThrow(ThemeColumns._ID));
        info.mThemeName = cursor.getString(cursor.getColumnIndexOrThrow(ThemeColumns.NAME));
        info.mFilePath = cursor.getString(cursor.getColumnIndexOrThrow(ThemeColumns.FILE_PATH));
        info.mIsApplyed = cursor.getInt(cursor.getColumnIndexOrThrow(ThemeColumns.IS_APPLY)) == 1 ? true : false;
        info.mProductID = cursor.getString(cursor.getColumnIndexOrThrow(ThemeColumns.PRODUCT_ID));
        info.mSceneName = cursor.getString(cursor.getColumnIndexOrThrow(ThemeColumns.SCENE_NAME));
        info.mIsDownloaded = cursor.getInt(cursor.getColumnIndexOrThrow(ThemeColumns.IS_DOWNLOADED)) == 1 ? true
                : false;
        info.mHouseName = cursor.getString(cursor.getColumnIndexOrThrow(ThemeColumns.HOUSE_NAME));

        String cameraLoc = cursor.getString(cursor.getColumnIndexOrThrow(ThemeColumns.CAMERA_LOCATION));
        if (cameraLoc != null) {
            info.mCurrentCameraLoction = new SEVector3f(ProviderUtils.getFloatArray(cameraLoc, 3));
            info.mCurrentCameraFov = cursor.getFloat(cursor.getColumnIndexOrThrow(ThemeColumns.CAMERA_FOV));
        }
        info.mCurrentWallIndex = cursor.getInt(cursor.getColumnIndexOrThrow(ThemeColumns.WALL_INDEX));
        return info;
    }

    public void saveToDB(Context context) {
        ContentValues values = new ContentValues();
        values.put(ThemeColumns.NAME, mThemeName);
        values.put(ThemeColumns.FILE_PATH, mFilePath);
        values.put(ThemeColumns.IS_DOWNLOADED, mIsDownloaded);
        values.put(ThemeColumns.IS_APPLY, mIsApplyed);
        values.put(ThemeColumns.PRODUCT_ID, mProductID);
        values.put(ThemeColumns.SCENE_NAME, mSceneName);
        values.put(ThemeColumns.HOUSE_NAME, mHouseName);
        Uri insertUri = context.getContentResolver().insert(ThemeColumns.CONTENT_URI, values);
        mID = (int) ContentUris.parseId(insertUri);
        for (ModelInfo modelInfo : mBoundingModel) {
            modelInfo.mBoundingThemeName = mThemeName;
            modelInfo.saveToDB();
            if (HomeManager.getInstance().getModelManager() != null)
                HomeManager.getInstance().getModelManager().mModels.put(modelInfo.mName, modelInfo);
            modelInfo.loadMountPointData();
        }
        if (mDefaultObjectsInfo != null) {
            String where = ObjectInfoColumns.SCENE_NAME + "='" + mSceneName + "'";
            String[] columns = { ObjectInfoColumns.OBJECT_ID };
            Cursor cursor = context.getContentResolver().query(ObjectInfoColumns.CONTENT_URI, columns, where, null,
                    null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.close();
                    return;
                }
                cursor.close();
            }
            saveObjectsOfSceneToDB(mDefaultObjectsInfo, 8, 4);
        }

    }

    public void saveToDB(Context context, SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(ThemeColumns.NAME, mThemeName);
        values.put(ThemeColumns.FILE_PATH, mFilePath);
        values.put(ThemeColumns.IS_DOWNLOADED, mIsDownloaded);
        values.put(ThemeColumns.IS_APPLY, mIsApplyed);
        values.put(ThemeColumns.PRODUCT_ID, mProductID);
        values.put(ThemeColumns.SCENE_NAME, mSceneName);
        values.put(ThemeColumns.HOUSE_NAME, mHouseName);
        mID = (int) db.insert(Tables.THEME, null, values);
        for (ModelInfo modelInfo : mBoundingModel) {
            modelInfo.mBoundingThemeName = mThemeName;
            modelInfo.saveToDB(db);
        }
        if (mDefaultObjectsInfo != null) {
            String where = ObjectInfoColumns.SCENE_NAME + "='" + mSceneName + "'";
            String[] columns = { ObjectInfoColumns.OBJECT_ID };
            Cursor cursor = db.query(Tables.OBJECTS_INFO, columns, where, null, null, null, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.close();
                    return;
                }
                cursor.close();
            }
            saveObjectsOfSceneToDB(db, mDefaultObjectsInfo, 8, 4);
        }

    }

    public static void saveObjectsOfSceneToDB(DefaultObjectsInfo defaultObjectInfo, int wallNumPort, int wallNumLand) {
        saveObjectsOfSceneToDB(null, defaultObjectInfo, wallNumPort, wallNumLand);
    }

    public static void saveObjectsOfSceneToDB(SQLiteDatabase db, DefaultObjectsInfo defaultObjectInfo, int wallNumPort,
            int wallNumLand) {
        ObjectInfo root = new ObjectInfo();
        root.mName = "home_root";
        root.mType = "RootNode";
        root.mSceneName = defaultObjectInfo.mSceneName;
        if (db != null) {
            root.saveToDB(db);
        } else {
            root.saveToDB();
        }

        defaultObjectInfo.mDesk.mVesselIndex = root.mIndex;
        defaultObjectInfo.mDesk.mVesselName = root.mName;
        defaultObjectInfo.mDesk.mSceneName = defaultObjectInfo.mSceneName;
        defaultObjectInfo.mDesk.mIndex = 1;
        if (db != null) {
            defaultObjectInfo.mDesk.saveToDB(db);
        } else {
            defaultObjectInfo.mDesk.saveToDB();
        }
        if (defaultObjectInfo.mObjectsOnDesk != null) {
            for (ObjectInfo objectOnDesk : defaultObjectInfo.mObjectsOnDesk) {
                objectOnDesk.mVesselIndex = defaultObjectInfo.mDesk.mIndex;
                objectOnDesk.mVesselName = defaultObjectInfo.mDesk.mName;
                objectOnDesk.mSceneName = defaultObjectInfo.mSceneName;
                if (objectOnDesk.mIndex == 0) {
                    objectOnDesk.mIndex = 1;
                }
                if (db != null) {
                    objectOnDesk.saveToDB(db);
                } else {
                    objectOnDesk.saveToDB();
                }
            }
        }

        defaultObjectInfo.mHouse.mVesselIndex = root.mIndex;
        defaultObjectInfo.mHouse.mVesselName = root.mName;
        defaultObjectInfo.mHouse.mSceneName = defaultObjectInfo.mSceneName;
        if (db != null) {
            defaultObjectInfo.mHouse.saveToDB(db);
        } else {
            defaultObjectInfo.mHouse.saveToDB();
        }

        for (int index = 0; index < wallNumPort; index++) {
            ObjectInfo wall = new ObjectInfo();
            wall.mIndex = index;
            wall.mName = "wall_port";
            wall.mObjectSlot.mSlotIndex = index;
            wall.mSceneName = defaultObjectInfo.mSceneName;
            wall.mType = "Wall";
            wall.mVesselIndex = defaultObjectInfo.mHouse.mIndex;
            wall.mVesselName = defaultObjectInfo.mHouse.mName;
            if (db != null) {
                wall.saveToDB(db);
            } else {
                wall.saveToDB();
            }
            if (defaultObjectInfo.mObjectsOnWall != null) {
                for (ObjectInfo objectOnWall : defaultObjectInfo.mObjectsOnWall) {
                    if (objectOnWall.getSlotIndex() == index) {
                        objectOnWall.mVesselIndex = wall.mIndex;
                        objectOnWall.mVesselName = wall.mName;
                        objectOnWall.mSceneName = defaultObjectInfo.mSceneName;
                        if (objectOnWall.mIndex == 0) {
                            objectOnWall.mIndex = 1;
                        }
                        if (db != null) {
                            objectOnWall.saveToDB(db);
                        } else {
                            objectOnWall.saveToDB();
                        }
                    }
                }
            }

        }

        ObjectInfo ground = new ObjectInfo();
        ground.mName = "ground_port";
        ground.mType = "Ground";
        ground.mObjectSlot.mSlotIndex = -1;
        ground.mSceneName = defaultObjectInfo.mSceneName;
        ground.mVesselIndex = defaultObjectInfo.mHouse.mIndex;
        ground.mVesselName = defaultObjectInfo.mHouse.mName;
        if (db != null) {
            ground.saveToDB(db);
        } else {
            ground.saveToDB();
        }
        if (defaultObjectInfo.mObjectsOnGround != null) {
            for (ObjectInfo objectOnGround : defaultObjectInfo.mObjectsOnGround) {
                objectOnGround.mVesselIndex = ground.mIndex;
                objectOnGround.mVesselName = ground.mName;
                objectOnGround.mSceneName = defaultObjectInfo.mSceneName;
                if (objectOnGround.mIndex == 0) {
                    objectOnGround.mIndex = 1;
                }
                if (db != null) {
                    objectOnGround.saveToDB(db);
                } else {
                    objectOnGround.saveToDB();
                }
            }
        }
        if (defaultObjectInfo.mSky != null) {
            defaultObjectInfo.mSky.mVesselIndex = root.mIndex;
            defaultObjectInfo.mSky.mVesselName = root.mName;
            defaultObjectInfo.mSky.mSceneName = defaultObjectInfo.mSceneName;
            if (db != null) {
                defaultObjectInfo.mSky.saveToDB(db);
            } else {
                defaultObjectInfo.mSky.saveToDB();
            }
        }
    }

    private static List<ModelInfo> loadAllBoundingModelInfo(Context context, boolean isAsset, String path) {
        // 加载主题包下的房间模型
        List<ModelInfo> allBoundingModelInfo = new ArrayList<ModelInfo>();
        if (!isAsset) {
            File file = new File(path);
            if (file.exists()) {
                try {
                    InputStream is = new FileInputStream(file.getPath() + "/models_config.xml");
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(is, "utf-8");
                    XmlUtils.beginDocument(parser, "config");
                    ModelInfo config = ModelInfo.CreateFromXml(parser, null, file.getPath());
                    config.mIsDownloaded = false;
                    is.close();
                    allBoundingModelInfo.add(config);
                } catch (Exception e) {
                }
                File[] files = file.listFiles();
                for (File item : files) {
                    if (item.isDirectory()) {
                        try {
                            InputStream is = new FileInputStream(item.getPath() + "/models_config.xml");
                            XmlPullParser parser = Xml.newPullParser();
                            parser.setInput(is, "utf-8");
                            XmlUtils.beginDocument(parser, "config");
                            ModelInfo config = ModelInfo.CreateFromXml(parser, null, item.getPath());
                            config.mIsDownloaded = false;
                            is.close();
                            allBoundingModelInfo.add(config);
                        } catch (Exception e) {
                        }
                    }
                }
            }
        } else {
            String[] filePathes = null;
            try {
                filePathes = context.getAssets().list(path.substring(7));
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            if (filePathes != null) {
                for (String filePath : filePathes) {
                    if (filePath.contains(".")) {
                        continue;
                    }
                    try {
                        InputStream is = context.getAssets().open(
                                path.substring(7) + "/" + filePath + "/models_config.xml");
                        XmlPullParser parser = Xml.newPullParser();
                        parser.setInput(is, "utf-8");
                        XmlUtils.beginDocument(parser, "config");
                        ModelInfo config = ModelInfo.CreateFromXml(parser, null, path + "/" + filePath);
                        config.mIsDownloaded = false;
                        is.close();
                        allBoundingModelInfo.add(config);
                    } catch (Exception e) {

                    }
                }
            }
        }

        return allBoundingModelInfo;
    }

    public static class CameraInfo {
        public SECameraData mSECameraData;
        public float mBestCameraFov = 29;
        public SEVector3f mBestCameraLocation = new SEVector3f(0, -803, 430);
        public float mNearestCameraFov = 26.274373f;
        public SEVector3f mNearestCameraLocation = new SEVector3f(0, -753, 430);
        public float mFarthestCameraFov = 39.73937f;
        public SEVector3f mFarthestCameraLocation = new SEVector3f(0, -1000, 430);

        // 绑定在该主题下的模型，默认绑定了一个房间

        public static CameraInfo CreateFromXml(Context context, XmlPullParser parser) throws XmlPullParserException,
                IOException {
            CameraInfo info = new CameraInfo();
            XmlUtils.nextElement(parser);
            while (true) {
                if ("bestCamera".equals(parser.getName())) {
                    info.mSECameraData = HomeUtils.loadCameraDataFromXml(parser);
                    info.mBestCameraFov = info.mSECameraData.mFov;
                    info.mBestCameraLocation = info.mSECameraData.mLocation;
                } else if ("nearestCamera".equals(parser.getName())) {
                    SECameraData nearestCamera = HomeUtils.loadCameraDataFromXml(parser);
                    info.mNearestCameraFov = nearestCamera.mFov;
                    info.mNearestCameraLocation = nearestCamera.mLocation;
                } else if ("farthestCamera".equals(parser.getName())) {
                    SECameraData farthestCamera = HomeUtils.loadCameraDataFromXml(parser);
                    info.mFarthestCameraFov = farthestCamera.mFov;
                    info.mFarthestCameraLocation = farthestCamera.mLocation;
                } else {
                    break;
                }
            }
            if (info.mSECameraData == null) {
                info.mSECameraData = new SECameraData();
                info.mSECameraData.mAxisZ = new SEVector3f(0, -1, 0);
                info.mSECameraData.mLocation = new SEVector3f(0, -803, 460);
                info.mSECameraData.mUp = new SEVector3f(0, 0, 1);
                info.mSECameraData.mFov = 29;
                info.mSECameraData.mNear = 10;
                info.mSECameraData.mFar = 5000;
            }
            return info;
        }

    }
}
