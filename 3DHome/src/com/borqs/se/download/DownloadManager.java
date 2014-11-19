package com.borqs.se.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.borqs.se.engine.SEUtils;
import com.borqs.se.home3d.*;

import org.xmlpull.v1.XmlPullParser;

import com.borqs.market.db.DownloadInfo;
import com.borqs.market.utils.MarketUtils;
import com.borqs.se.home3d.ProviderUtils.ThemeColumns;

import android.content.Context;
import android.database.Cursor;
import android.util.Xml;

public class DownloadManager {
    public static boolean DEBUG = HomeUtils.DEBUG;

    private static volatile DownloadManager mInstance;
    private Context mContext;

    private DownloadManager(Context context) {
        mContext = context;
    }

    public static DownloadManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DownloadManager(context);
        }
        return mInstance;
    }

    public synchronized void installDownload(String productId) {
        final DownloadInfo info = MarketUtils.getDownloadInfoByProductId(mContext, productId);
        if (info != null && info.local_path != null) {
            Transactor transactor = new Transactor();
            transactor.mPluginPackageInfo = info;
            transactor.mInstallPath = HomeUtils.getDownloadedFilePath(mContext, info);
            transactor.executeInstallPlugin();
        }
    }

    public synchronized void applyDownload(String productId) {
        final DownloadInfo info = MarketUtils.getDownloadInfoFromPlugIn(mContext, productId);
        if (info != null) {
            if (MarketUtils.CATEGORY_THEME.equals(info.type)) {
                String where = ThemeColumns.PRODUCT_ID + "='" + productId + "'";
                Cursor cursor = mContext.getContentResolver().query(ThemeColumns.CONTENT_URI, null, where,
                        null, null);
                if (cursor != null && cursor.moveToNext()) {
                    HomeUtils.markThemeAsApply(mContext, productId);
                    ThemeInfo themeInfo = ThemeInfo.CreateFromDB(cursor);
                    HomeManager.getInstance().changeTheme(themeInfo);
                }
                if (cursor != null) {
                    cursor.close();
                }
            } else if (MarketUtils.CATEGORY_WALLPAPER.equals(info.type)) {
                Utils.applyWallpaper(mContext, info);
            } else if (MarketUtils.CATEGORY_OBJECT.equals(info.type)) {
                // we dose not need install 3DObject
            }
        }
    }

    public synchronized void removeDownload(String productId) {
        final DownloadInfo info = MarketUtils.getDownloadInfoFromPlugIn(mContext, productId);
        if (info != null && info.local_path != null) {
            Transactor transactor = new Transactor();
            transactor.mPluginPackageInfo = info;
            transactor.mInstallPath = info.local_path;
            transactor.executeRemovePlugin();
        }
    }

    private class Transactor {
        protected DownloadInfo mPluginPackageInfo;
        protected String mInstallPath;

        private boolean unzipPackage() {
            try {
                return Utils.unzipDataPartition(mPluginPackageInfo.local_path, mInstallPath);
            } catch (IOException e) {
                return false;
            }
        }

        /*
         * 一：假如主题不存在保存主题以及主题下的房间，桌子等
         * 二：假如存在但是没有使用，删除老主题，保存新主题
         * 三：假如存在并且在使用中，删除老主题，保存新主题，更新新主题
         */
        private void saveThemeToDB() {
            String where = ThemeColumns.PRODUCT_ID + "='" + mPluginPackageInfo.product_id + "'";
            String[] selection = { ThemeColumns.IS_APPLY };
            Cursor cursor = mContext.getContentResolver().query(ThemeColumns.CONTENT_URI, selection, where, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    boolean isApplyed = cursor.getInt(0) == 1 ? true : false;
                    HomeUtils.deleteThemeDBByProductID(mContext, mPluginPackageInfo.product_id);
                    if (!isApplyed) {
                        if (unzipPackage()) {
                            ThemeInfo themeInfo = loadThemeInfo(false);
                            if (themeInfo != null) {
                                themeInfo.saveToDB(mContext);
                                Utils.saveToPlugTable(mContext, mPluginPackageInfo, mInstallPath);
                            }
                        }
                    } else {
                        if (unzipPackage()) {
                            ThemeInfo themeInfo = loadThemeInfo(true);
                            if (themeInfo != null) {
                                HomeUtils.markThemeAsApply(mContext, mPluginPackageInfo.product_id);
                                themeInfo.mIsApplyed = true;
                                themeInfo.saveToDB(mContext);
                                Utils.saveToPlugTable(mContext, mPluginPackageInfo, mInstallPath);
                                HomeManager.getInstance().changeTheme(themeInfo);
                            } else {
                                applyDefaultTheme();
                            }
                        } else {
                            applyDefaultTheme();
                        }
                    }
                    cursor.close();
                    return;
                }
                cursor.close();
            }
            if (unzipPackage()) {
                ThemeInfo themeInfo = loadThemeInfo(false);
                if (themeInfo != null) {
                    themeInfo.saveToDB(mContext);
                    Utils.saveToPlugTable(mContext, mPluginPackageInfo, mInstallPath);
                }
            }
        }
        
        private void saveObjectToDB() {
            String name = HomeUtils.deleteModelAndObjectDBByProductID(mContext, mPluginPackageInfo.product_id);
            if (name != null) {
                HomeManager.getInstance().removeModelFromScene(name);
            }
            if (unzipPackage()) {
                ModelInfo modelInfo = loadModelInfo();
                if (modelInfo != null) {
                    modelInfo.saveToDB();
                    Utils.saveToPlugTable(mContext, mPluginPackageInfo, mInstallPath);
                    HomeManager.getInstance().addModelToScene(modelInfo);
                }
            }
        }

        private void saveWallpaperToDB() {
            HomeUtils.deleteWallpaperByLocalPath(mContext, mPluginPackageInfo.product_id);
            unzipPackage();
            Utils.saveToPlugTable(mContext, mPluginPackageInfo, mInstallPath);
        }

        private ThemeInfo loadThemeInfo(boolean isApply) {
            try {
                FileInputStream fis = new FileInputStream(mInstallPath + "/theme_config.xml");
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(fis, "utf-8");
                XmlUtils.beginDocument(parser, "config");
                ThemeInfo config = ThemeInfo.CreateFromXml(mContext, parser, mPluginPackageInfo.product_id,
                        mInstallPath);
                config.mIsDownloaded = true;
                config.mIsApplyed = isApply;
                fis.close();
                return config;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        private ModelInfo loadModelInfo() {
            try {
                FileInputStream fis = new FileInputStream(mInstallPath + "/models_config.xml");
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(fis, "utf-8");
                XmlUtils.beginDocument(parser, "config");
                ModelInfo config = ModelInfo.CreateFromXml(parser, mPluginPackageInfo.product_id, mInstallPath);
                config.mIsDownloaded = true;
                fis.close();
                return config;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        public void executeInstallPlugin() {
            if (MarketUtils.CATEGORY_THEME.equals(mPluginPackageInfo.type)) {
                saveThemeToDB();
            } else if (MarketUtils.CATEGORY_WALLPAPER.equals(mPluginPackageInfo.type)) {
                saveWallpaperToDB();
            } else {
                saveObjectToDB();
            }

        }

        public void executeRemovePlugin() {
            String type = mPluginPackageInfo.type;
            if (MarketUtils.CATEGORY_THEME.equals(type)) {
                String where = ThemeColumns.PRODUCT_ID + "='" + mPluginPackageInfo.product_id + "'";
                boolean isApply = false;
                Cursor cursor = mContext.getContentResolver().query(ThemeColumns.CONTENT_URI,
                        new String[] { ThemeColumns.IS_APPLY }, where, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        isApply = cursor.getInt(0) == 1 ? true : false;
                    }
                    cursor.close();
                }
                HomeUtils.deleteThemeDBByProductID(mContext, mPluginPackageInfo.product_id);
                MarketUtils.deletePlugIn(mContext, mPluginPackageInfo.product_id);
                if (isApply) {
                    applyDefaultTheme();
                }
            } else if (MarketUtils.CATEGORY_WALLPAPER.equals(type)) {
                HomeUtils.deleteWallpaperByLocalPath(mContext, mPluginPackageInfo.product_id);
                MarketUtils.deletePlugIn(mContext, mPluginPackageInfo.product_id);
            } else {

                String modelName = HomeUtils.deleteModelAndObjectDBByProductID(mContext, mPluginPackageInfo.product_id);
                MarketUtils.deletePlugIn(mContext, mPluginPackageInfo.product_id);
                HomeManager.getInstance().removeModelFromScene(modelName);

            }
        }
    }

    private void applyDefaultTheme() {
        HomeUtils.markThemeAsApply(mContext, HomeDataBaseHelper.getInstance(mContext).getDefaultThemeID());
        String where = ThemeColumns.PRODUCT_ID + "='" + HomeDataBaseHelper.getInstance(mContext).getDefaultThemeID()
                + "'";
        Cursor cursor = mContext.getContentResolver().query(ThemeColumns.CONTENT_URI, null, where, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            ThemeInfo themeInfo = ThemeInfo.CreateFromDB(cursor);
            HomeManager.getInstance().changeTheme(themeInfo);
        }
        if (cursor != null) {
            cursor.close();
        }
    }
}
