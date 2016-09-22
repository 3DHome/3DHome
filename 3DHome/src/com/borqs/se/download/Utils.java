package com.borqs.se.download;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.borqs.market.db.DownloadInfo;
import com.borqs.market.db.PlugInColumns;
import com.borqs.market.json.Product;
import com.borqs.market.utils.MarketUtils;
import com.borqs.market.wallpaper.RawPaperItem;
import com.borqs.market.wallpaper.WallpaperUtils;
import com.borqs.se.home3d.*;

public class Utils {

    private static final String TAG = HomeUtils.TAG;

    public static Bitmap getLocalDrawable(String imagePath) {
        FileInputStream fis =  null;
        try {
            fis = new FileInputStream(imagePath);
            return BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            return null;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    public static Bitmap getAssetDrawable(Context context, String imagePath) {
        InputStream inputStream = null;
        try {
            inputStream = context.getAssets().open(imagePath);
            return BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            return null;
        } finally {
            if (inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }


    public static boolean hasNetWork(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isAvailable()) == true;
    }
    
    public static void saveToPlugTable(Context context, DownloadInfo info, String filePath) {
        Cursor cursor = MarketUtils.queryPlugIn(context, info.product_id);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                int version = cursor.getInt(cursor.getColumnIndexOrThrow(PlugInColumns.VERSION_CODE));
                if (version < info.version_code) {
                    MarketUtils.updatePluginWithDownloadInfo(context, info, new File(filePath));
                }
            } else {
                MarketUtils.insertPlugIn(context, info, false, new File(filePath));
            }
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
    }


    public static void reportMonitoredApps(final Context context) {
        if (!hasNetWork(context)) {
            if (HomeUtils.DEBUG) {
                Log.d("SETTINGS_THREAD", "reportMonitoredApps, skip while set already.");
            }
            return;
        }

        final String preferenceName = SettingsActivity.PREFS_SETTING_NAME;
        SharedPreferences settings = context.getSharedPreferences(preferenceName, 0);
        final String pkgName = context.getPackageName();
        if (settings.getBoolean(pkgName, false)) {
            if (HomeUtils.DEBUG) {
                Log.d("SETTINGS_THREAD", "reportMonitoredApps, skip while set already.");
            }
            return;
        }

        final String REPORT_URL = "http://api.borqs.com/internal/keyvalue?device=%1$s&key=%2$s&value=%3$s";
        final String[] REPORT_LIST = {"com.facebook.katana", "com.google.android.apps.plus", "com.android.vending"};
        new Thread(new Runnable() {
            public void run() {
                try {
                    final PackageManager packageManager = context.getPackageManager();
                    ArrayList<String> reportList = new ArrayList<String>(Arrays.asList(REPORT_LIST));
                    for (String testPkgName : REPORT_LIST) {
                        try {
                            packageManager.getPackageInfo(testPkgName, 0);
                        } catch (PackageManager.NameNotFoundException ne) {
                            reportList.remove(testPkgName);
                        }
                    }

                    if (reportList.isEmpty()) {
                        if (HomeUtils.DEBUG) {
                            Log.d("SETTINGS_THREAD", "skip without target apps");
                        }
                        return;
                    }

                    final String reportValues = TextUtils.join(" ", reportList);

                    String packageName = context.getPackageName();
                    int version = 0;
                    try {
                        version = context.getPackageManager().getPackageInfo(packageName, 0).versionCode;
                    } catch (PackageManager.NameNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    String macId = "";
                    WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    try {
                        WifiInfo info = wm.getConnectionInfo();
                        macId = info.getMacAddress().replace(":", "");
                        if (HomeUtils.DEBUG) {
                            Log.d("SETTINGS_THREAD", "macId=" + macId);
                        }
                    } catch (Exception ne) {
                        ne.printStackTrace();
                        Log.d("SETTINGS_THREAD", "macId exception=" + ne.getMessage());
                    }

                    String device = "mac-" + macId
                            + "-manufacture-" + Build.MANUFACTURER
                            + "-locale-" + Locale.getDefault().toString()
                            + "-model-" + Build.MODEL
                            + "-dev-" + Build.DEVICE
                            + "-sdk-" + Build.VERSION.SDK_INT
                            + "-android-" + Build.VERSION.RELEASE
                            + "-package-" + context.getPackageName()
                            + "-version-" + version;

                    String url = String.format(REPORT_URL,
                            URLEncoder.encode(device, "UTF-8"),
                            URLEncoder.encode(pkgName, "UTF-8"),
                            URLEncoder.encode(reportValues, "UTF-8"));
                    if (HomeUtils.DEBUG) {
                        Log.d("SETTINGS_THREAD", "statics device = " + device + ", url = " + url);
                    }
                    HttpURLConnection con = (HttpURLConnection) (new URL(url).openConnection());
                    int code = con.getResponseCode();
                    if (HttpURLConnection.HTTP_OK == code) {
                        SharedPreferences settings = context.getSharedPreferences(preferenceName, 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putBoolean(pkgName, true);
                        editor.commit();
                    } else {
                        if (HomeUtils.DEBUG) {
                            Log.d("SETTINGS_THREAD", "http code = " + code);
                        }
                    }
                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "SETTINGS_THREAD").start();
    }

    public static boolean unzipDataPartition(String zipFilePath, String unzipFilePath) throws IOException {
        OutputStream os = null;
        InputStream is = null;
        ZipFile zipFile = null;
        if (zipFilePath.startsWith("file:///")) {
            zipFilePath = zipFilePath.replace("file:///", "/");
        }
        try {
            zipFile = new ZipFile(zipFilePath);
            Enumeration<? extends ZipEntry> entryEnum = zipFile.entries();
            if (null != entryEnum) {
                ZipEntry zipEntry = null;
                while (entryEnum.hasMoreElements()) {
                    zipEntry = entryEnum.nextElement();

                    if (zipEntry.isDirectory()) {
                        File dir = new File(unzipFilePath + "/" + zipEntry.getName());
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        continue;
                    }
                    File file = new File(unzipFilePath + "/" + zipEntry.getName());
                    File parentFile = file.getParentFile();
                    if (!parentFile.exists()) {
                        parentFile.mkdirs();
                    }
                    if (zipEntry.getSize() > 0) {
                        os = new BufferedOutputStream(new FileOutputStream(file));
                        is = zipFile.getInputStream(zipEntry);
                        byte[] buffer = new byte[4096];
                        int readLen = 0;
                        while ((readLen = is.read(buffer, 0, 4096)) >= 0) {
                            os.write(buffer, 0, readLen);
                        }
                        os.flush();
                    } else {
                        File dir = new File(unzipFilePath + "/" + zipEntry.getName());
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                    }
                }
                return true;
            }
        } catch (IOException ex) {
            Log.e(TAG, "unzip the downloaded files error."+zipFilePath, ex);
            return false;
        } finally {
            if (null != zipFile) {
                zipFile = null;
            }
            if (null != is) {
                is.close();
            }
            if (null != os) {
                os.close();
            }
        }
        return false;
    }

    public static void showWallpapers(Context context) {
        WallpaperUtils.ENABLE_BACKDOOR = HomeUtils.DEBUG;
        String supported_mod = isScreenOrientationPortrait() ? Product.SupportedMod.PORTRAIT
                : Product.SupportedMod.LANDSCAPE;
        MarketUtils.startLocalProductListIntent(context, context.getPackageName(), MarketUtils.CATEGORY_WALLPAPER,
                false, supported_mod);
    }

    public static void exportOrImportWallpaper(Activity context) {
        WallpaperUtils.startExportOrImportIntent(context, isScreenOrientationPortrait(),
                HomeManager.getWallpaperMaxSize(context), HomeManager.exportWallPaperToMarket());
    }

    public static boolean isScreenOrientationPortrait() {
        return !HomeManager.getInstance().isLandscapeOrientation();
    }
    /// wallpaper and user share code end

    public static void applyWallpaper(Context context, DownloadInfo info) {
        final String parentPath = info.local_path;
        if (TextUtils.isEmpty(parentPath)) {
            return;
        }

        File root = new File(parentPath);
        if (!root.exists()) {
            return;
        }

        ArrayList<String> srcWallPapers = new ArrayList<String>();
        ArrayList<String> srcGroundPapers = new ArrayList<String>();
        if (!decodePaperFromRawItem(parentPath, srcWallPapers, srcGroundPapers)) {
            decodePaperFromFile(root, srcWallPapers, srcGroundPapers);
        }

        if (srcWallPapers.isEmpty() && srcGroundPapers.isEmpty()) {
            Log.e(TAG, "applyWallpaper, no valid paper found, skip.");
            return;
        }
        HomeScene homeScene = HomeManager.getInstance().getHomeScene();
        if (homeScene != null && homeScene.getHouse() != null) {
            int wallNum = homeScene.getHouse().mWallNum;
            ArrayList<WallPaperItem> wallPapers = new ArrayList<WallPaperItem>();
            if (srcWallPapers.size() >= wallNum) {
                // 一张图片贴一面墙
                for (int index = 0; index < wallNum; index++) {
                    WallPaperItem item = new WallPaperItem();
                    item.mType = WallPaperItem.WallPaperType.WALLPAPER;
                    item.mImagePath = srcWallPapers.get(index);
                    item.mIndex = index;
                    wallPapers.add(item);
                }

            } else if (srcWallPapers.size() >= (float) wallNum / 2) {
                // 一张图片贴两面墙
                for (int index = 0; index < wallNum; index++) {
                    int imageIndex = index / 2;
                    WallPaperItem item = new WallPaperItem();
                    item.mType = WallPaperItem.WallPaperType.WALLPAPER;
                    item.mImagePath = srcWallPapers.get(imageIndex);
                    item.mPaperCoverWallNum = 2;
                    item.mIndex = index;
                    item.mCropImagePosition = 2 - index % 2 - 1;
                    wallPapers.add(item);
                }

            } else  {
                // 一张图片最多贴三面墙
                int wallPaperSize = srcWallPapers.size();
                for (int index = 0; index < wallNum; index++) {
                    int imageIndex = index / 3;
                    WallPaperItem item = new WallPaperItem();
                    item.mType = WallPaperItem.WallPaperType.WALLPAPER;
                    item.mImagePath = srcWallPapers.get(imageIndex % wallPaperSize);
                    item.mPaperCoverWallNum = 3;
                    item.mIndex = index;
                    item.mCropImagePosition = 3 - index % 3 - 1;
                    wallPapers.add(item);
                }
            }
            int index = 0;
            for (String str : srcGroundPapers) {
                WallPaperItem item = new WallPaperItem();
                item.mType = WallPaperItem.WallPaperType.GROUNDPAPER;
                item.mImagePath = str;
                item.mIndex = index;
                wallPapers.add(item);
                index++;
            }
            HomeManager.applyWallpaperFromMarket(wallPapers, info.product_id);
        }
    }

    private static boolean decodePaperFromRawItem(String parentPath, ArrayList<String> wallList,
                                                  ArrayList<String> groundList) {
        ArrayList<RawPaperItem> rawPaperItems = WallpaperUtils.parseRawPaperItemList(parentPath);

        if (null == rawPaperItems || rawPaperItems.isEmpty()) {
            return false;
        }

        String filePath;
        for (RawPaperItem item : rawPaperItems) {
            filePath = item.mImagePath;
            if (TextUtils.isEmpty(filePath)) continue;
            filePath = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
            filePath = parentPath + File.separator + filePath;
            File file = new File(filePath);
            if (!file.exists()) {
                Log.e(TAG, "applyWallpaper, skip not existing file " + filePath);
                continue;
            }
            if (RawPaperItem.TYPE_WALL.equalsIgnoreCase(item.mType)) {
                wallList.add(filePath);
            } else if (RawPaperItem.TYPE_GROUND.equalsIgnoreCase(item.mType)) {
                groundList.add(filePath);
            } else {
                Log.w(TAG, "applyWallpaper, unknown item type " + item.mType);
            }
        }
        return true;
    }

    /// decode papers for walls and ground if and only if both list are empty.
    /// 1. try the rootFile folder for possible valid paper item
    /// 2. try those valid sub folder file if no valid paper item found in rootFile
    /// 3. skip those sub folder that automatically created by machines that name start (or/and end)
    /// with underscore sign '_'
    /// 4. sort the paper by its name,
    private static void decodePaperFromFile(File rootFile, ArrayList<String> wallList,
                                            ArrayList<String> groundList) {
        if (wallList.isEmpty() && groundList.isEmpty()) {
            if (null == rootFile) return;
            if (!rootFile.exists() || !rootFile.isDirectory()) return;
            String fileName = rootFile.getName();
            if (fileName.startsWith("_") || fileName.endsWith("_")) {
                // skip those folder with the name _xxxx_, that automatically add by machine
                return;
            }

            ArrayList<File> subFolder = new ArrayList<File>();
            File[] files = rootFile.listFiles();
            for (int i = 0; i < files.length; i++)
            {
                File f = files[i];
                if (f.isFile())
                {
                    if (isWallpaperFile(f)) {
                        wallList.add(f.getPath());
                    } else if (isGroundPaperFile(f)) {
                        groundList.add(f.getPath());
                    }
                } else if (f.isDirectory()) {
                    subFolder.add(f);
                }
            }

            if (wallList.isEmpty() && groundList.isEmpty()) {
                for (File sub : subFolder) {
                    decodePaperFromFile(sub, wallList, groundList);
                }
            } else {
                Collections.sort(wallList);
                Collections.sort(groundList);
            }
        }
    }

    private static boolean isGroundPaperFile(File file) {
        final String name = file.getName().toLowerCase();
        return name.startsWith("background");
    }

    private static boolean isWallpaperFile(File file) {
        final String name = file.getName().toLowerCase();
        return name.startsWith("wallpaper");
    }

    public static void showOnlineObjects(Context context) {
        MarketUtils.startProductListIntent(context, MarketUtils.CATEGORY_OBJECT, false, isScreenOrientationPortrait());
    }

    public static void showObjects(Context context) {
        MarketUtils.startLocalProductListIntent(context, context.getPackageName(), MarketUtils.CATEGORY_OBJECT, false,
                "");
    }

    public static void showThemes(Context context) {
        MarketUtils.startLocalProductListIntent(context, context.getPackageName(), MarketUtils.CATEGORY_THEME, false,
                "");
    }
}
