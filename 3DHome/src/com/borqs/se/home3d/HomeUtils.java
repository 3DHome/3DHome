package com.borqs.se.home3d;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Intent;
import android.os.StrictMode;

import com.borqs.market.db.DownloadInfo;
import com.borqs.market.json.Product;
import com.borqs.market.wallpaper.WallpaperUtils;
import com.borqs.se.download.Utils;
import com.support.StaticUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.borqs.market.utils.IntentUtil;
import com.borqs.market.utils.MarketUtils;
import com.borqs.market.utils.ThemeXmlParser;
import com.borqs.se.engine.SECameraData;
import com.borqs.se.engine.SEScene;
import com.borqs.se.engine.SESceneManager;
import com.borqs.se.engine.SEUtils;
import com.borqs.se.engine.SEVector.SEVector3f;
import com.borqs.se.home3d.ProviderUtils.ModelColumns;
import com.borqs.se.home3d.ProviderUtils.ObjectInfoColumns;
import com.borqs.se.home3d.ProviderUtils.ThemeColumns;
import com.borqs.se.shortcut.AppItemInfo;
import com.borqs.se.shortcut.LauncherModel;
import com.borqs.se.widget3d.House;
import com.borqs.se.R;

import android.app.ActivityManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.Layout.Alignment;
import android.util.Log;
import android.util.Xml;
import android.view.MotionEvent;

public class HomeUtils {
    public enum SceneOrientation {
        AUTO_LAND, AUTO_PORT, ONLY_PORT, ONLY_LAND
    }

    public static boolean DEBUG = false;
    public static final String TAG = "3DHome";

    private static final int UNCONSTRAINED = -1;

    public static boolean hasSDcard() {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File file = new File("sdcard-ext");
            if (file.exists() && file.canWrite()) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    public static String getSdcardPath() {
        String sdcardPath = Environment.getExternalStorageDirectory().getPath();
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File file = new File("sdcard-ext");
            if (file.exists() && file.canWrite()) {
                sdcardPath = file.getPath();
            }
        }
        return sdcardPath;
    }
    public static String get3DHomePath() {
        String str = getSdcardPath() + File.separator + "3DHome";
        File file = new File(str);
        if (!file.exists()) {
            file.mkdir();
        }
        return str;
    }
    public static String getThumbPath() {
        String str = get3DHomePath() + File.separator + ".thumb";
        File file = new File(str);
        if (!file.exists()) {
            file.mkdir();
        }
        return str;
    }

    public static String getTmpImageFilePath() {
        String str = get3DHomePath() + File.separator + ".tmp_icon";
        return str;
    }

   

    public static String getLocalFilePath(Context context, String name) {
        if (TextUtils.isEmpty(name)) {
            return context.getFilesDir() + File.separator + "local_resource";
        } else {
            return context.getFilesDir() + File.separator + "local_resource" + File.separator + name;
        }
    }

    public static String getDownloadedFilePath(Context context, DownloadInfo info) {
        String id = null == info ? null : info.product_id;
        if (TextUtils.isEmpty(id)) {
            return context.getFilesDir() + File.separator + "downloaded_resource";
        } else {
            return context.getFilesDir() + File.separator + "downloaded_resource" + File.separator + id;
        }
    }

    public static boolean isAssetFileExist(Context context, String filePath) {
        InputStream is = null;
        try {
            is = context.getResources().getAssets().open(filePath);
            if (is != null) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

    }

    public static void moveAssetFilesToExternal(Context context, String src, String des) {
        String localFilePath = getLocalFilePath(context, des);
        String[] filePaths = null;
        try {
            filePaths = context.getResources().getAssets().list(src);
        } catch (Exception e) {
        }
        if (filePaths == null) {
            return;
        }
        for (String file : filePaths) {
            InputStream is = null;
            try {
                is = context.getResources().getAssets().open(src + File.separator + file);
                File parentFile = new File(localFilePath);
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                File destFile = new File(localFilePath + File.separator + file);
                destFile.createNewFile();
                SEUtils.copyToFile(is, destFile);
                is.close();
            } catch (Exception e) {
                String newSrc = src + File.separator + file;
                String newDes = des + File.separator + file;
                moveAssetFilesToExternal(context, newSrc, newDes);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception e) {

                    }
                }
            }
        }
    }

    public static void loadDebugThemeAndModelFromSDCard(Context context) {
        // 假如为调试状态可从sdcard/3DHome目录下加载模型包以及主题包， 3DHome每次启动重新加载
        if (HomeUtils.DEBUG) {
            boolean hasThemeOrModel = false;
            File file = new File(get3DHomePath());
            if (file.exists()) {
                File[] allZipFile = file.listFiles();
                for (File zipFile : allZipFile) {
                    if (zipFile.isFile() && (zipFile.getName().endsWith(".zip") || zipFile.getName().endsWith(".apk"))) {
                        try {
                            String unzipFile = HomeUtils.getLocalFilePath(context,
                                    zipFile.getName().substring(0, (int) (zipFile.getName().length() - 4)));
                            HomeUtils.deleteFile(unzipFile);
                            Utils.unzipDataPartition(zipFile.getAbsolutePath(), unzipFile);
                            File configFile = new File(unzipFile + File.separator + MarketUtils.MARKET_CONFIG_FILE_NAME);
                            if (!configFile.exists()) {
                                continue;
                            }
                            Product resourceXML = ThemeXmlParser.parser(new File(unzipFile));
                            String type = resourceXML.type;
                            String id = resourceXML.product_id;

                            if ("theme".equals(type)) {
                                // load debug theme begin
                                
                                String where = ModelColumns.ASSETS_PATH + "='" + unzipFile + "'";
                                context.getContentResolver().delete(ModelColumns.CONTENT_URI, where, null);
                                
                                FileInputStream fis = new FileInputStream(unzipFile + "/theme_config.xml");
                                XmlPullParser parser = Xml.newPullParser();
                                parser.setInput(fis, "utf-8");
                                XmlUtils.beginDocument(parser, "config");
                                ThemeInfo config = ThemeInfo.CreateFromXml(context, parser, id, unzipFile);
                                fis.close();
                                config.saveToDB(context);
                                hasThemeOrModel = true;
                                // load debug theme end
                            } else {
                                
                                String where = ThemeColumns.FILE_PATH + "='" + unzipFile + "'";
                                context.getContentResolver().delete(ThemeColumns.CONTENT_URI, where, null);

                                // load debug model begin
                                FileInputStream fis = new FileInputStream(unzipFile + "/models_config.xml");
                                XmlPullParser parser = Xml.newPullParser();
                                parser.setInput(fis, "utf-8");
                                XmlUtils.beginDocument(parser, "config");
                                ModelInfo config = ModelInfo.CreateFromXml(parser, id, unzipFile);
                                fis.close();
                                config.saveToDB();
                                hasThemeOrModel = true;
                                // load debug model end
                            }
                        } catch (Exception e) {

                        }
                    }
                }
            }
            if (hasThemeOrModel) {
                Intent intent = new Intent("com.borqs.market.intent.action.Init");
                context.sendBroadcast(intent);
            }
        }

    }

    public static Object invokeMethod(Object object, Method method, Object[] paras) {
        try {
            return method.invoke(object, paras);
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static Method getMethod(Class clazz, String name, Class[] paratype) {
        Method method = null;
        try {
            method = clazz.getMethod(name, paratype);
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return method;
    }

    @SuppressWarnings("unchecked")
    public static Method getDeclaredMethod(Class clazz, String name, Class[] paratype) {
        Method method = null;
        try {
            method = clazz.getDeclaredMethod(name, paratype);
            method.setAccessible(true);
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return method;
    }

    public static int getStatusbarHeight(Context context) {
        int height = 0;
        try {
            Class c = Class.forName("com.android.internal.R$dimen");
            Field field = c.getField("status_bar_height");
            field.setAccessible(true);
            int id = field.getInt(c);
            height = context.getResources().getDimensionPixelSize(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return height;
    }


    public static void saveBitmap(Bitmap bitmap, String path, Bitmap.CompressFormat format) {
        try {
            File f = new File(path);
            f.createNewFile();
            FileOutputStream fOut = new FileOutputStream(f);
            bitmap.compress(format, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Bitmap resizeBitmap(Bitmap bm, int maxW, int maxH) {
        int imageSizeX = SEUtils.higherPower2(bm.getWidth());
        int imageSizeY = SEUtils.higherPower2(bm.getHeight());
        if (imageSizeX >= maxW || imageSizeY >= maxH) {
            imageSizeX = maxW;
            imageSizeY = maxH;
        } else {
            if ((float) imageSizeX / (float) imageSizeY > (float) maxW / (float) maxH) {
                imageSizeY = maxH * imageSizeX / maxW;
            } else {
                imageSizeX = maxW * imageSizeY / maxH;
            }
        }
        float scaleX = (float) imageSizeX / bm.getWidth();
        float scaleY = (float) imageSizeY / bm.getHeight();
        float scale = Math.min(scaleX, scaleY);
        Bitmap src = Bitmap.createScaledBitmap(bm, (int) (bm.getWidth() * scale), (int) (bm.getHeight() * scale), true);
        if (bm != src) {
            bm.recycle();
        }
        Bitmap des = Bitmap.createBitmap(imageSizeX, imageSizeY, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(des);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawBitmap(src, (imageSizeX - src.getWidth()) / 2, (imageSizeY - src.getHeight()) / 2, paint);
        src.recycle();
        return des;
    }

    public static void writeBitmap(ContentValues values, Bitmap bitmap) {
        if (bitmap != null) {
            byte[] data = flattenBitmap(bitmap);
            values.put(ObjectInfoColumns.SHORTCUT_ICON, data);
        }
    }

    private static byte[] flattenBitmap(Bitmap bitmap) {
        // Try go guesstimate how much space the icon will take when serialized
        // to avoid unnecessary allocations/copies during the write.
        int size = bitmap.getWidth() * bitmap.getHeight() * 4;
        ByteArrayOutputStream out = new ByteArrayOutputStream(size);
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
            if (DEBUG)
                Log.d("Favorite", "Could not write icon");
            return null;
        }
    }

    public static ComponentName getCategoryComponentName(Context context, String[] keywords) {
        ComponentName componentName = null;
        if (keywords != null) {
            List<AppItemInfo> apps = LauncherModel.getInstance().getApps();
            for (String keyword : keywords) {
                for (AppItemInfo info : apps) {
                    ResolveInfo ri = info.getResolveInfo();
                    if (ri.activityInfo.name.toLowerCase().contains(keyword)) {
                        componentName = new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name);
                        break;
                    }
                    if (componentName != null) {
                        break;
                    }
                }
            }
        }

        return componentName;
    }

    public static String getAction(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            return "ACTION_DOWN";
        } else if (ev.getAction() == MotionEvent.ACTION_UP) {
            return "ACTION_UP";
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            return "ACTION_MOVE";
        } else {
            return "ACTION_CANCEL";
        }
    }

    public static Bitmap createEmptyBitmap(int w, int h) {
        int newW = SEUtils.higherPower2(w);
        int newH = SEUtils.higherPower2(h);
        Bitmap des = Bitmap.createBitmap(newW, newH, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(des);
        canvas.drawColor(Color.TRANSPARENT);
        return des;
    }

    public static int[] getSpanForWidget(Context context, AppWidgetProviderInfo info) {
        return getSpanForWidget(context, info.provider, info.minWidth, info.minHeight);
    }

    private static int[] getSpanForWidget(Context context, ComponentName component, int minWidth, int minHeight) {
        Resources resources = context.getResources();
        Rect padding = getDefaultPaddingForWidget(context, component, null);
        int requiredWidth = minWidth + padding.left + padding.right;
        int requiredHeight = minHeight + padding.top + padding.bottom;
        return rectToCell(resources, requiredWidth, requiredHeight);
    }

    public static Rect getDefaultPaddingForWidget(Context context, ComponentName component, Rect padding) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo appInfo;

        if (padding == null) {
            padding = new Rect(0, 0, 0, 0);
        } else {
            padding.set(0, 0, 0, 0);
        }

        try {
            appInfo = packageManager.getApplicationInfo(component.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            // if we can't find the package, return 0 padding
            return padding;
        }

        if (appInfo.targetSdkVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            Resources r = context.getResources();
            padding.left = r.getDimensionPixelSize(R.dimen.default_app_widget_padding_left);
            padding.right = r.getDimensionPixelSize(R.dimen.default_app_widget_padding_right);
            padding.top = r.getDimensionPixelSize(R.dimen.default_app_widget_padding_top);
            padding.bottom = r.getDimensionPixelSize(R.dimen.default_app_widget_padding_bottom);
        }
        return padding;
    }

    private static int[] rectToCell(Resources resources, int width, int height) {
        int actualWidth = resources.getDimensionPixelSize(R.dimen.workspace_cell_width);
        int actualHeight = resources.getDimensionPixelSize(R.dimen.workspace_cell_height);
        int smallerSize = Math.min(actualWidth, actualHeight);
        int spanX = (int) Math.ceil(width / (float) smallerSize);
        int spanY = (int) Math.ceil(height / (float) smallerSize);
        if (spanX < 1) {
            spanX = 1;
        } else if (spanX > 4) {
            spanX = 4;
        }
        if (spanY < 1) {
            spanY = 1;
        } else if (spanY > 4) {
            spanY = 4;
        }
        return new int[] { spanX, spanY };
    }

    @SuppressWarnings("unchecked")
    public static SEScene getSceneByClassName(String className, String sceneName) {
        try {
            Class<SEScene> clazz = (Class<SEScene>) Class.forName(className);
            Class[] paratype = { String.class };
            Constructor constructor = clazz.getConstructor(paratype);
            Object[] para = { sceneName };
            SEScene object = (SEScene) constructor.newInstance(para);
            return object;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getRomeNumber(int n) {
        String[] a = new String[] { "", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "", "X", "XX", "XXX",
                "XL", "L", "LX", "LXX", "LXXX", "XCC", "", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM" };
        int t, i, m;
        String result = "";
        for (m = 0, i = 1000; m < 3; m++, i /= 10) {
            t = (n % i) / (i / 10);
            result = result + a[(2 - m) * 10 + t];
        }
        return result;
    }

    public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == UNCONSTRAINED) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == UNCONSTRAINED) ? 128 : (int) Math.min(Math.floor(w / minSideLength),
                Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == UNCONSTRAINED) && (minSideLength == UNCONSTRAINED)) {
            return 1;
        } else if (minSideLength == UNCONSTRAINED) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    public static boolean isPad(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static String getAppLabel(Context context, ResolveInfo resolveInfo) {
        if (resolveInfo == null) {
            return null;
        }
        PackageManager pm = context.getPackageManager();
        String label = resolveInfo.loadLabel(pm).toString();
        if (label == null) {
            label = resolveInfo.activityInfo.packageName;
        }
        return label;
    }

    private static Drawable getFullResIcon(Context context, ResolveInfo info) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        int iconDpi = activityManager.getLauncherLargeIconDensity();

        PackageManager pm = context.getPackageManager();
        Resources resources;
        try {
            resources = pm.getResourcesForApplication(info.activityInfo.applicationInfo);
        } catch (Exception e) {
            resources = null;
        }
        if (resources != null) {
            int iconId = info.activityInfo.getIconResource();
            if (iconId != 0) {
                return getFullResIcon(resources, iconId, iconDpi);
            }
        }
        return null;
    }

    private static Drawable getFullResIcon(Resources resources, int iconId, int iconDpi) {
        Drawable d;
        try {
            Method method = HomeUtils.getMethod(Resources.class, "getDrawableForDensity", new Class[] { int.class,
                    int.class });
            // d = resources.getDrawableForDensity(iconId, mIconDpi);
            method.setAccessible(true);
            d = (Drawable) method.invoke(resources, new Object[] { iconId, iconDpi });
        } catch (Exception e) {
            Log.d("AppItemInfo", "error : " + e.getMessage());
            d = null;
        }

        return d;
    }

    public static Drawable getAppIcon(Context context, ResolveInfo resolveInfo) {
        Resources resources = context.getResources();
        if (resolveInfo == null) {
            return resources.getDrawable(R.drawable.ic_launcher_application);
        }
        Drawable icon = getFullResIcon(context, resolveInfo);
        if (icon == null) {
            PackageManager pm = context.getPackageManager();
            try {
                icon = resolveInfo.loadIcon(pm);
            } catch (Exception e) {

            }
        }
        if (icon == null) {
            icon = resources.getDrawable(R.drawable.ic_launcher_application);
        }
        return icon;
    }

    public static float getFontSize(Context context) {
        return context.getResources().getConfiguration().fontScale;
    }

    /**
     * Get sorted apps by name
     */
    public static ArrayList<AppItemInfo> getSortedAppsByName() {
        ArrayList<AppItemInfo> apps = LauncherModel.getInstance().getApps();
        try {
            Collections.sort(apps, new AppComparator());
        } catch (Exception e) {
        }
        return apps;
    }

    static class AppComparator implements Comparator<AppItemInfo> {
        public int compare(AppItemInfo o1, AppItemInfo o2) {
            return Collator.getInstance().compare(o1.getLabel(), o2.getLabel());
        }
    }

    private static int generateInSampleSize(BitmapFactory.Options options, int outW, int outH) {
        int height = options.outHeight;
        int width = options.outWidth;
        int size = 1;
        if (height > outH || width > outW) {
            if (width > height) {
                size = Math.round((float) height / (float) outH);
            } else {
                size = Math.round((float) width / (float) outW);
            }
        }
        return size;
    }

    public static Bitmap decodeSampledBitmapFromResource(String bf, int outW, int outH) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(bf, options);
        options.inSampleSize = generateInSampleSize(options, outW, outH);
        options.inJustDecodeBounds = false;
        options.inPreferQualityOverSpeed = true;
        return BitmapFactory.decodeFile(bf, options);
    }

    public static Bitmap decodeSampledBitmapFromResource(Context context, Uri uri, int outW, int outH) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);
            options.inSampleSize = generateInSampleSize(options, outW, outH);
            options.inJustDecodeBounds = false;
            options.inPreferQualityOverSpeed = true;
            return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);
        } catch (Exception e) {
        }
        return null;
    }

    public static void deleteFile(String path) {
        File f = new File(path);
        if (!f.exists())
            return;
        if (!(f.delete())) {
            File subs[] = f.listFiles();
            for (int i = 0; i <= subs.length - 1; i++) {
                if (subs[i].isDirectory())
                    deleteFile(subs[i].getAbsolutePath());
                subs[i].delete();
            }
            f.delete();
        }
    }

    public static ModelInfo getModelInfoFromDB(Context context, String name) {
        String where = ModelColumns.OBJECT_NAME + "='" + name + "'";
        Cursor cursor = context.getContentResolver().query(ModelColumns.CONTENT_URI, null, where, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                ModelInfo modelInfo = ModelInfo.CreateFromDB(cursor);
                cursor.close();
                return modelInfo;
            }
            cursor.close();
        }
        return null;
    }

    // / 1. clear current possible applied theme id
    // / 2. set new applying theme id
    // / 3. set last used paper id for new theme
    public static void markThemeAsApply(Context context, String productId) {
        String where = ThemeColumns.IS_APPLY + "=" + 1;
        ContentValues values = new ContentValues();
        values.put(ThemeColumns.IS_APPLY, 0);
        context.getContentResolver().update(ThemeColumns.CONTENT_URI, values, where, null);

        values = new ContentValues();
        where = ThemeColumns.PRODUCT_ID + "='" + productId + "'";
        int status = 1;
        values.put(ThemeColumns.IS_APPLY, status);
        context.getContentResolver().update(ThemeColumns.CONTENT_URI, values, where, null);
        if (productId != null) {
            MarketUtils.updatePlugIn(context, productId, true, MarketUtils.CATEGORY_THEME);
            WallpaperUtils.clearAppliedFlag(context);
            String themePaperId = SettingsActivity.getWallpaperId(context, productId);
            if (!TextUtils.isEmpty(themePaperId)) {
                MarketUtils.updatePlugIn(context, themePaperId, true, MarketUtils.CATEGORY_WALLPAPER);
            }
        }
    }

    // / 1. set applying paper id
    // / 2. save using paper id for current theme, which restore while
    // / come back to the theme later, see to: markThemeAsApply()
    public static void markPaperAsApply(Context context, String productId) {
        MarketUtils.updatePlugIn(context, productId, true, MarketUtils.CATEGORY_WALLPAPER);
        ThemeInfo themeInfo = HomeManager.getInstance().getCurrentThemeInfo();
        if (null != themeInfo) {
            SettingsActivity.setWallpaperId(context, themeInfo.mProductID, productId);
        }
    }

    public static String getHouseOfTheme(Context context, String theme) {
        String where = ThemeColumns.NAME + "='" + theme + "'";
        String[] selection = { ThemeColumns.HOUSE_NAME };
        Cursor cursor = context.getContentResolver().query(ThemeColumns.CONTENT_URI, selection, where, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            return cursor.getString(0);
        }
        return null;
    }

    public static String getThemeNameByProductID(Context context, String productID) {
        String where = ThemeColumns.PRODUCT_ID + "='" + productID + "'";
        String[] selection = { ThemeColumns.NAME };
        Cursor cursor = context.getContentResolver().query(ThemeColumns.CONTENT_URI, selection, where, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            return cursor.getString(0);
        }
        return null;
    }

    public static void updateHouseName(Context context, ThemeInfo themeInfo) {
        String where = ObjectInfoColumns.SCENE_NAME + "='" + themeInfo.mSceneName + "' AND "
                + ObjectInfoColumns.OBJECT_TYPE + "='House'";
        ContentValues values = new ContentValues();
        values.put(ObjectInfoColumns.OBJECT_NAME, themeInfo.mHouseName);
        context.getContentResolver().update(ObjectInfoColumns.CONTENT_URI, values, where, null);
    }

    public static String deleteThemeDBByProductID(Context context, String productID) {
        String where = ThemeColumns.PRODUCT_ID + " = '" + productID + "'";
        String assetPath;
        String name;
        String sceneName;
        Cursor cursor = context.getContentResolver().query(ThemeColumns.CONTENT_URI, null, where, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            assetPath = cursor.getString(cursor.getColumnIndexOrThrow(ThemeColumns.FILE_PATH));
            name = cursor.getString(cursor.getColumnIndexOrThrow(ThemeColumns.NAME));
            sceneName = cursor.getString(cursor.getColumnIndexOrThrow(ThemeColumns.SCENE_NAME));
        } else {
            if (cursor != null) {
                cursor.close();
            }
            return null;
        }
        if (cursor != null) {
            cursor.close();
        }
        context.getContentResolver().delete(ThemeColumns.CONTENT_URI, where, null);
        if (!"home8".equals(sceneName)) {
            where = ObjectInfoColumns.SCENE_NAME + " = '" + sceneName + "'";
            context.getContentResolver().delete(ObjectInfoColumns.CONTENT_URI, where, null);
        }
        // 在老版本中资源包和wallpaper存在一个路径，假如是老版本上来的不删除这个文件夹
        if (!name.equals(new File(assetPath).getName()))
            deleteFile(assetPath);
        return name;
    }

    public static String deleteModelDBByProductID(Context context, String productID) {
        ContentResolver resolver = context.getContentResolver();
        String where = ModelColumns.PRODUCT_ID + " = '" + productID + "'";
        String[] selection = { ModelColumns.OBJECT_NAME, ModelColumns.ASSETS_PATH };
        Cursor cursor = resolver.query(ModelColumns.CONTENT_URI, selection, where, null, null);
        String name = null;
        String localPath = null;
        if (cursor != null && cursor.moveToFirst()) {
            name = cursor.getString(0);
            localPath = cursor.getString(1);
        } else {
            if (cursor != null) {
                cursor.close();
            }
            return null;
        }
        if (cursor != null) {
            cursor.close();
        }
        where = ModelColumns.OBJECT_NAME + "='" + name + "'";
        resolver.delete(ModelColumns.CONTENT_URI, where, null);
        deleteFile(localPath);
        return name;
    }

    public static String deleteModelAndObjectDBByProductID(Context context, String productID) {
        ContentResolver resolver = context.getContentResolver();
        String where = ModelColumns.PRODUCT_ID + " = '" + productID + "'";
        String[] selection = { ModelColumns.OBJECT_NAME, ModelColumns.ASSETS_PATH };
        Cursor cursor = resolver.query(ModelColumns.CONTENT_URI, selection, where, null, null);
        String name = null;
        String localPath = null;
        if (cursor != null && cursor.moveToFirst()) {
            name = cursor.getString(0);
            localPath = cursor.getString(1);
        } else {
            if (cursor != null) {
                cursor.close();
            }
            return null;
        }
        if (cursor != null) {
            cursor.close();
        }
        where = ModelColumns.OBJECT_NAME + "='" + name + "'";
        resolver.delete(ModelColumns.CONTENT_URI, where, null);
        where = ObjectInfoColumns.OBJECT_NAME + "='" + name + "'";
        resolver.delete(ObjectInfoColumns.CONTENT_URI, where, null);
        deleteFile(localPath);
        return name;
    }

    public static String deleteModelAndObjectDBByName(Context context, String name) {
        ContentResolver resolver = context.getContentResolver();
        String where = ModelColumns.OBJECT_NAME + " = '" + name + "'";
        String[] selection = { ModelColumns.PRODUCT_ID, ModelColumns.ASSETS_PATH };
        Cursor cursor = resolver.query(ModelColumns.CONTENT_URI, selection, where, null, null);
        String localPath = null;
        String productId = null;
        if (cursor != null && cursor.moveToNext()) {
            productId = cursor.getString(0);
            localPath = cursor.getString(1);
        } else {
            if (cursor != null) {
                cursor.close();
            }
            return null;
        }
        if (cursor != null) {
            cursor.close();
        }
        where = ModelColumns.OBJECT_NAME + "='" + name + "'";
        resolver.delete(ModelColumns.CONTENT_URI, where, null);
        where = ObjectInfoColumns.OBJECT_NAME + "='" + name + "'";
        resolver.delete(ObjectInfoColumns.CONTENT_URI, where, null);
        deleteFile(localPath);
        return productId;
    }

    public static long mPhoneMemory = 0;

    /**
     * @return total memory of phone (M)
     */
    public static long getPhoneMemory() {
        if (mPhoneMemory > 0) {
            return mPhoneMemory;
        }
        String path = "/proc/meminfo";
        String content = null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path), 8);
            String line;
            if ((line = br.readLine()) != null) {
                content = line;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        int begin = content.indexOf(':');
        int end = content.indexOf('k');
        content = content.substring(begin + 1, end).trim();
        mPhoneMemory = Integer.parseInt(content) / 1024;
        return mPhoneMemory;
    }

    public static HashMap<String, String> loadSpecialAttributeForModel(Context context, String assetPath) {
        InputStream is = null;
        try {
            HashMap<String, String> values = new HashMap<String, String>();
            if (assetPath.contains("assets")) {
                // length of "assets/" is 7
                String filePath = assetPath.substring(7) + "/special_config.xml";
                is = context.getAssets().open(filePath);
            } else {
                is = new FileInputStream(assetPath + "/special_config.xml");
            }
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(is, "utf-8");
            XmlUtils.beginDocument(parser, "config");
            XmlUtils.nextElement(parser);
            while (true) {
                String name = parser.getName();
                if (TextUtils.isEmpty(name)) {
                    break;
                }
                values.put(name, parser.getAttributeValue(null, "value"));
                XmlUtils.nextElement(parser);
            }
            return values;
        } catch (XmlPullParserException e) {
        } catch (IOException e) {
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;
    }

    public static SECameraData loadCameraDataFromXml(XmlPullParser parser) {
        SECameraData cameraData = new SECameraData();
        try {
            cameraData.mFov = Float.valueOf(parser.getAttributeValue(null, ThemeColumns.FOV));
            cameraData.mNear = Float.valueOf(parser.getAttributeValue(null, ThemeColumns.NEAR));
            cameraData.mFar = Float.valueOf(parser.getAttributeValue(null, ThemeColumns.FAR));
            XmlUtils.nextElement(parser);
            while (true) {
                String item = parser.getName();
                if (ThemeColumns.LOCATION.equals(item)) {
                    cameraData.mLocation = new SEVector3f();
                    cameraData.mLocation.initFromXml(parser);
                    XmlUtils.nextElement(parser);
                } else if (ThemeColumns.ZAXIS.equals(item)) {
                    cameraData.mAxisZ = new SEVector3f();
                    cameraData.mAxisZ.initFromXml(parser);
                    XmlUtils.nextElement(parser);
                } else if (ThemeColumns.UP.equals(item)) {
                    cameraData.mUp = new SEVector3f();
                    cameraData.mUp.initFromXml(parser);
                    XmlUtils.nextElement(parser);
                } else {
                    break;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cameraData;
    }

    public static void gotoAdRemovalActivity() {
        gotoProductDetailActivity(PID_AD_FIGHTER);
    }

    private static void gotoProductDetailActivity(String productId) {
        IntentUtil.startProductDetailActivity(SESceneManager.getInstance().getGLActivity(), productId, 0, null,
                Utils.isScreenOrientationPortrait());
    }

    public static String PKG_CURRENT_NAME = "com.borqs.se";

    public static void attachApplication(Context context) {
        PKG_CURRENT_NAME = context.getPackageName();
        testDebugFlags();
        testStrictMode();
    }

    private static boolean DEBUG_STRICT_MODE = false;
    private static boolean PROFILE_STARTUP = false;

    private static void testDebugFlags() {
        DEBUG = checkDebugFlag(".debug");
        DEBUG_STRICT_MODE = checkDebugFlag(".strict_mode");
        PROFILE_STARTUP = checkDebugFlag(".startup");
    }

    private static boolean checkDebugFlag(String flag) {
        File file = new File(get3DHomePath() + File.separator + flag);
        return file.exists();
    }

    // / reference count that use for stack calls pairs of start/stop tracing,
    // which
    // / ensure only one call to Debug.startMethodTracing, and then call
    // Debug.stopMethodTracing if
    // / and only if the count decrease to 0.
    private static int tracingReference = 0;

    public static void startTracing() {
        startTracing(null);
    }

    public static void startTracing(String tag) {
        if (PROFILE_STARTUP) {
            if (tracingReference <= 0) {
                if (TextUtils.isEmpty(tag)) {
                    tag = "launcher";
                }
                final String traceDir = get3DHomePath() + File.separator + "trace";
                final File dir = new File(traceDir);
                if (!dir.exists()) {
                    dir.mkdir();
                }
                android.os.Debug.startMethodTracing(traceDir + File.separator + tag + "-" + System.currentTimeMillis());
                tracingReference = 1;
            } else {
                tracingReference++;
            }
        }
    }

    public static void stopTracing() {
        if (PROFILE_STARTUP && tracingReference > 0) {
            tracingReference--;
            if (tracingReference == 0) {
                android.os.Debug.stopMethodTracing();
            }
        }
    }

    private static void testStrictMode() {
        if (DEBUG_STRICT_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites()
                    .detectNetwork() // or .detectAll() for all detectable
                                     // problems
                    .penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
        }
    }

    public static void staticUsingDesk(Context context, String objName) {
        StaticUtil.staticUsingDesk(context, objName);
    }

    public static void staticUsingTheme(Context context, String themeName) {
        StaticUtil.staticUsingTheme(context, themeName);
    }

    public static void showFeedbackActivity(Context context) {
        StaticUtil.showFeedbackActivity(context);
    }

    public static void staticPurchaseEvent(Context context, String resourceID) {
        StaticUtil.staticPurchaseEvent(context, resourceID);
    }

    // / static stub end
    private static final String PID_AD_FIGHTER = "com.borqs.se.object.fighter";

    public static boolean isExistingProductId(String productId) {
        boolean existing = false;
        final String whereProductId = ModelColumns.PRODUCT_ID + "=?";
        Cursor cursor = SESceneManager
                .getInstance()
                .getGLActivity()
                .getContentResolver()
                .query(ModelColumns.CONTENT_URI, new String[] { ModelColumns.OBJECT_NAME }, whereProductId,
                        new String[] { productId }, null);
        if (cursor != null && cursor.getCount() > 0) {
            existing = true;
        }
        if (cursor != null) {
            cursor.close();
        }
        return existing;
    }

    public static String getDefaultThemeId(String themeName) {
        return HomeUtils.PKG_CURRENT_NAME + ".theme." + themeName;
    }

    public static String getDefaultObjectId(String name) {
        return HomeUtils.PKG_CURRENT_NAME + ".object." + name;
    }

    // / hard code component name, identical to that in AndroidManifest
    public static String encodeSearchComponentName() {
        return HomeUtils.PKG_CURRENT_NAME + "/.home3d.SearchActivity";
    }

    public static String encodeProviderAuthority() {
        return HomeUtils.PKG_CURRENT_NAME + ".settings";
    }

    public static void gotoSettingsActivity() {
        Intent intentHome = new Intent();
        intentHome.setAction(HomeUtils.PKG_CURRENT_NAME + ".settings");
        intentHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        HomeManager.getInstance().startActivity(intentHome);
    }

    // / hard code component name end.

    public static void deleteWallpaperByLocalPath(Context context, String localPath) {
        deleteFile(localPath);
    }

    private static boolean mHasBeenExecuted = false;

    public static void moveAssetToExternal(Context context) {
        if (!mHasBeenExecuted && !SettingsActivity.hasUpdatedLocalRes(context)) {
            mHasBeenExecuted = true;
            Thread thread = new MoveAssetThread(context);
            thread.start();
        }
    }

    private static class MoveAssetThread extends Thread {
        private Context mContext;

        public MoveAssetThread(Context context) {
            super("MoveAssetThread");
            mContext = context;
            setPriority(MIN_PRIORITY);
        }

        @Override
        public void run() {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            Iterator<Entry<String, String>> iter = HomeDataBaseHelper.MODEL_XMLS.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, String> entry = iter.next();
                String name = entry.getKey();
                String path = entry.getValue();
                if (isAssetFileExist(mContext, path + File.separator + MarketUtils.MARKET_CONFIG_FILE_NAME)) {
                    String localFilePath = HomeUtils.getLocalFilePath(mContext, name);
                    deleteFile(localFilePath);
                    moveAssetFilesToExternal(mContext, path, name);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

            iter = HomeDataBaseHelper.THEME_XMLS.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, String> entry = iter.next();
                String name = entry.getKey();
                String path = entry.getValue();
                if (isAssetFileExist(mContext, path + File.separator + MarketUtils.MARKET_CONFIG_FILE_NAME)) {
                    String localFilePath = HomeUtils.getLocalFilePath(mContext, name);
                    deleteFile(localFilePath);
                    moveAssetFilesToExternal(mContext, path, name);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            Intent intent = new Intent("com.borqs.market.intent.action.Init");
            mContext.sendBroadcast(intent);
        }

        private boolean isAssetFileExist(Context context, String fileName) {
            InputStream is = null;
            try {
                is = context.getAssets().open(fileName);
            } catch (IOException e1) {
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
                return true;
            }
            return false;
        }
    }

    /***************************** App icon background relates begin *******************/
    public static Bitmap getAppIconBitmap(House house, Bitmap bitmapIcon, Drawable icon, String title,
            boolean needBackground) {
        int bitmapW = (int) house.mCellWidth;
        int bitmapH = (int) house.mCellHeight;
        float screenDensity = house.getScene().getScreenDensity();
        Bitmap res = Bitmap.createBitmap(bitmapW, bitmapH, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(res);

        if (icon != null && (icon instanceof BitmapDrawable)) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
            bitmapIcon = bitmapDrawable.getBitmap();
        }

        int iconLeft = house.mAppIconPaddingLeft;
        int iconRight = house.mAppIconPaddingRight;
        int iconTop = house.mAppIconPaddingTop;
        int iconSize = bitmapW - iconRight - iconLeft;
        float lableTop = iconSize + iconTop + house.mAppIconPaddingBottom;
        if (needBackground) {
            Drawable iconBackground = house
                    .getContext()
                    .getResources()
                    .getDrawable(
                            HomeUtils.getBackgroudRes(bitmapIcon, HomeManager.getInstance().getAppIconBackgroundType()));
            Rect rect = new Rect(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize);
            iconBackground.setBounds(rect);
            iconBackground.draw(canvas);
            iconTop = (int) (iconTop + 0.106f * iconSize);
            iconSize = (int) (0.726666667f * iconSize);
            iconLeft = (bitmapW - iconSize) / 2;
        } else {
            iconSize = (int) (0.95f * iconSize);
            iconLeft = (bitmapW - iconSize) / 2;
            iconTop = iconTop - 2;
        }
        if (bitmapIcon != null) {
            Bitmap scaleIcon = Bitmap.createScaledBitmap(bitmapIcon, iconSize, iconSize, true);
            canvas.drawBitmap(scaleIcon, iconLeft, iconTop, null);
        } else if (icon != null) {
            Rect oldBounds = icon.copyBounds();
            icon.setBounds(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize);
            icon.draw(canvas);
            icon.setBounds(oldBounds);
        }
        String label = title;
        if (label != null) {
            int labelW = house.getContext().getResources().getDimensionPixelSize(R.dimen.apps_customize_cell_width);
            float scale = (float) bitmapW / (float) labelW;
            labelW = bitmapW;
            TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            textPaint.density = screenDensity;
            float fontScale = HomeUtils.getFontSize(house.getContext());
            int textSize = house.getContext().getResources()
                    .getDimensionPixelSize(R.dimen.app_customize_icon_text_size);
            textPaint.setTextSize(textSize * fontScale * scale);
            textPaint.setColor(Color.WHITE);
            textPaint.setFakeBoldText(true);
            textPaint.setShadowLayer(screenDensity * 2 * scale, screenDensity * scale, screenDensity * scale,
                    Color.BLACK);
            StaticLayout titleLayout = new StaticLayout(label, textPaint, labelW, Alignment.ALIGN_CENTER, 1f, 0.0F,
                    false);
            int lineCount = titleLayout.getLineCount();
            if (lineCount > 1) {
                float allowW = labelW;
                int index = 0;
                String newlabel = null;
                float newLableW;
                while (true) {
                    index++;
                    if (index > label.length()) {
                        break;
                    }
                    newlabel = label.substring(0, index) + "...";
                    newLableW = StaticLayout.getDesiredWidth(newlabel, textPaint);
                    if (newLableW > allowW) {
                        newlabel = label.substring(0, --index) + "...";
                        break;
                    }
                }
                titleLayout = new StaticLayout(newlabel, textPaint, labelW, Alignment.ALIGN_CENTER, 1f, 0.0F, false);
            }
            float left = 0;
            canvas.save();
            canvas.translate(left, lableTop);
            canvas.scale((float) bitmapW / labelW, (float) bitmapW / labelW);
            titleLayout.draw(canvas);
            canvas.restore();
        }
        return res;
    }

    public static HSV Rgb2Hsv(int color) {

        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        int Max = Math.max(Math.max(g, b), r);
        int Min = Math.min(Math.min(b, g), r);

        int delta = Max - Min;
        float h = 0;
        float s;
        float v = (float) Max / 255;

        if (Max != 0.0) {
            s = (float) delta / (float) Max;
        } else {
            s = 0;
        }

        if (s == 0) {
            h = 0;
        } else {
            if (r == Max)
                h = (float) (g - b) / (float) delta;
            else if (g == Max) {
                h = 2.0f + (float) (b - r) / (float) delta;
            } else if (b == Max) {
                h = 4.0f + ((float) r - g) / (float) delta;
            }
        }
        h = h * 60.0f;
        if (h < 0) {
            h = h + 360.0f;
        }
        h = h / 360.0f;
        return new HSV(h, s, v);

    }

    public static int Hsv2Rgb(HSV hsv) {
        float H = hsv.mH;
        float S = hsv.mS;
        float V = hsv.mV;
        int i;
        int R, G, B;
        float f, p, q, t;

        if (S == 0) {
            // achromatic (grey)
            R = G = B = Math.round(V * 255);
            return Color.argb(255, R, G, B);
        }

        H = H * 6; // sector 0 to 5
        i = (int) H;
        f = H - i; // factorial part of h
        p = V * (1 - S);
        q = V * (1 - S * f);
        t = V * (1 - S * (1 - f));

        switch (i) {
        case 0:
            R = Math.round(V * 255);
            G = Math.round(t * 255);
            B = Math.round(p * 255);
            break;
        case 1:
            R = Math.round(q * 255);
            G = Math.round(V * 255);
            B = Math.round(p * 255);
            break;
        case 2:
            R = Math.round(p * 255);
            G = Math.round(V * 255);
            B = Math.round(t * 255);
            break;
        case 3:
            R = Math.round(p * 255);
            G = Math.round(q * 255);
            B = Math.round(V * 255);
            break;
        case 4:
            R = Math.round(t * 255);
            G = Math.round(p * 255);
            B = Math.round(V * 255);
            break;
        default: // case 5:
            R = Math.round(V * 255);
            G = Math.round(p * 255);
            B = Math.round(q * 255);
            break;
        }
        return Color.argb(255, R, G, B);
    }

    public static List<AppIconBackground> mAppIconBackgrounds;
    static {
        mAppIconBackgrounds = new ArrayList<AppIconBackground>();
        for (int i = 0; i <= 12; i++) {
            AppIconBackground appIconBackground = new AppIconBackground(i);
            mAppIconBackgrounds.add(appIconBackground);
        }
    }

    public static int getBackgroudRes(Bitmap bitmap, int appIconBackgroundType) {
        if (bitmap == null) {
            return mAppIconBackgrounds.get(0).getHighSaturationRes();
        }
        for (AppIconBackground appIconBackground : mAppIconBackgrounds) {
            appIconBackground.clearSimilarColors();
        }
        int sampleX = 12;
        int sampleY = 12;
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        // 抽样图片的12*12个颜色值
        for (int x = 0; x < sampleX; x++) {
            for (int y = 0; y < sampleY; y++) {
                int newX = w * x / sampleX;
                int newY = h * y / sampleY;
                int color = bitmap.getPixel(newX, newY);
                float alpha = Color.alpha(color) / 255f;
                if (alpha > 0.5f) {
                    // 判断图片采样点颜色值和哪个背景图片颜色值接近
                    int r = Math.round(Color.red(color) * alpha);
                    int g = Math.round(Color.green(color) * alpha);
                    int b = Math.round(Color.blue(color) * alpha);
                    int newColor = Color.argb(255, r, g, b);
                    HSV hsv = Rgb2Hsv(newColor);
                    if (appIconBackgroundType == 1) {
                        hsv.mH = hsv.mH + 0.5f;
                        if (hsv.mH >= 1) {
                            hsv.mH = hsv.mH - 1;
                        }
                    }
                    for (AppIconBackground appIconBackground : mAppIconBackgrounds) {
                        if (appIconBackground.isSimilar(hsv)) {
                            appIconBackground.addSimilarColor(hsv);
                            break;
                        }
                    }
                }
            }
        }
        // 除去黑白背景， 判断哪个背景图片包含的采样点最多
        int maxCount = 0;
        AppIconBackground iconBackground = null;
        for (AppIconBackground appIconBackground : mAppIconBackgrounds) {
            if (appIconBackground.getSimilarColors().size() > maxCount) {
                maxCount = appIconBackground.getSimilarColors().size();
                iconBackground = appIconBackground;
            }
        }
        // 在彩色采样点的个数小于5的情况下，亮度大于0.5的使用灰色背景，亮度小于0.5的使用白色背景
        if (iconBackground == null) {
            HSV avgHSV = mAppIconBackgrounds.get(0).getSimilarColorsAvgHSV();
            if (avgHSV.mV > 0.5f) {
                return mAppIconBackgrounds.get(0).getLowSaturationRes();
            } else {
                return mAppIconBackgrounds.get(0).getHighSaturationRes();
            }
        } else {
            HSV avgHSV = iconBackground.getSimilarColorsAvgHSV();
            if (avgHSV.mV > 0.5f) {
                return iconBackground.getLowSaturationRes();
            } else {
                return iconBackground.getHighSaturationRes();
            }
        }
    }

    public static int getAverageColorOfBitmap(Bitmap bitmap, int sampleX, int sampleY) {
        int averageColor;
        if (bitmap == null) {
            averageColor = 0xFFFFFFFF;
            return averageColor;
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int avgr = 0;
        int avgg = 0;
        int avgb = 0;
        int count = 0;
        for (int x = 0; x < sampleX; x++) {
            for (int y = 0; y < sampleY; y++) {
                int newX = w * x / sampleX;
                int newY = h * y / sampleY;
                int color = bitmap.getPixel(newX, newY);
                float alpha = Color.alpha(color) / 255f;
                if (alpha > 0.5f) {
                    int r = Math.round(Color.red(color) * alpha);
                    int g = Math.round(Color.green(color) * alpha);
                    int b = Math.round(Color.blue(color) * alpha);
                    avgr = avgr + r;
                    avgg = avgg + g;
                    avgb = avgb + b;
                    count++;
                }
            }
        }
        if (count == 0) {
            averageColor = 0xFFFFFFFF;
        } else {
            avgr = avgr / count;
            avgg = avgg / count;
            avgb = avgb / count;
            averageColor = Color.argb(0xff, avgr, avgg, avgb);
        }
        return averageColor;
    }

    public static class AppIconInfo {
        public int mAverageColor;
        public int mRevertColor;
        public float mAverageSaturation;

        public AppIconInfo(Bitmap bitmap, int sampleX, int sampleY) {
            mAverageColor = getAverageColorOfBitmap(bitmap, sampleX, sampleY);
            mRevertColor = getColorRevert(mAverageColor);
            mAverageSaturation = getColorSaturation(mAverageColor);
        }

        private float getColorSaturation(int color) {
            int r = Color.red(color);
            int g = Color.green(color);
            int b = Color.blue(color);
            int maxV = Math.max(Math.max(r, g), b);
            int minV = Math.min(Math.min(r, g), b);
            float result = (float) (maxV - minV) / (float) maxV;
            return result;
        }

        private int getColorRevert(int color) {
            return Color.argb(Color.alpha(color), 255 - Color.red(color), 255 - Color.green(color),
                    255 - Color.blue(color));
        }
    }

    public static class AppIconBackground {
        private int mIndex;
        /**
         * HSV颜色分量值
         */
        private float mH;
        /**
         * 根据最大和最小的颜色值来判断颜色是否相似
         */
        private float mMinH, mMaxH;
        private int mHighSaturationRes;
        private int mLowSaturationRes;

        private List<HSV> mSimilarColor = new ArrayList<HSV>();

        public AppIconBackground(int index) {
            mIndex = index;
            switch (mIndex) {
            case 0:
                // 黑白色
                mH = 0.0f;
                mMinH = 0;
                mMaxH = 1;
                mHighSaturationRes = R.drawable.icon_background_0_a;
                mLowSaturationRes = R.drawable.icon_background_0_b;
                break;
            case 1:
                mH = 0.882413f;
                mMinH = 0.8292662f;
                mMaxH = 0.9412065f;
                mHighSaturationRes = R.drawable.icon_background_1_a;
                mLowSaturationRes = R.drawable.icon_background_1_b;
                break;
            case 2:
                mH = 0.7761194f;
                mMinH = 0.729589725f;
                mMaxH = 0.8292662f;
                mHighSaturationRes = R.drawable.icon_background_2_a;
                mLowSaturationRes = R.drawable.icon_background_2_b;
                break;
            case 3:
                mH = 0.68306005f;
                mMinH = 0.618069325f;
                mMaxH = 0.729589725f;
                mHighSaturationRes = R.drawable.icon_background_3_a;
                mLowSaturationRes = R.drawable.icon_background_3_b;
                break;
            case 4:
                mH = 0.5530786f;
                mMinH = 0.5216373f;
                mMaxH = 0.618069325f;
                mHighSaturationRes = R.drawable.icon_background_4_a;
                mLowSaturationRes = R.drawable.icon_background_4_b;
                break;
            case 5:
                mH = 0.4901960f;
                mMinH = 0.4514233f;
                mMaxH = 0.5216373f;
                mHighSaturationRes = R.drawable.icon_background_5_a;
                mLowSaturationRes = R.drawable.icon_background_5_b;
                break;
            case 6:
                mH = 0.4126506f;
                mMinH = 0.32529082f;
                mMaxH = 0.4514233f;
                mHighSaturationRes = R.drawable.icon_background_6_a;
                mLowSaturationRes = R.drawable.icon_background_6_b;
                break;
            case 7:
                mH = 0.23793104f;
                mMinH = 0.20074606f;
                mMaxH = 0.32529082f;
                mHighSaturationRes = R.drawable.icon_background_7_a;
                mLowSaturationRes = R.drawable.icon_background_7_b;
                break;
            case 8:
                mH = 0.16356108f;
                mMinH = 0.145322205f;
                mMaxH = 0.20074606f;
                mHighSaturationRes = R.drawable.icon_background_8_a;
                mLowSaturationRes = R.drawable.icon_background_8_b;
                break;
            case 9:
                mH = 0.12708333f;
                mMinH = 0.109672613f;
                mMaxH = 0.145322205f;
                mHighSaturationRes = R.drawable.icon_background_9_a;
                mLowSaturationRes = R.drawable.icon_background_9_b;
                break;
            case 10:
                mH = 0.092261896f;
                mMinH = 0.070721113f;
                mMaxH = 0.109672613f;
                mHighSaturationRes = R.drawable.icon_background_10_a;
                mLowSaturationRes = R.drawable.icon_background_10_b;
                break;
            case 11:
                mH = 0.04918033f;
                mMinH = 0.024590165f;
                mMaxH = 0.070721113f;
                mHighSaturationRes = R.drawable.icon_background_11_a;
                mLowSaturationRes = R.drawable.icon_background_11_b;
                break;
            case 12:
                // 纯红色
                mH = 0.0f;
                mMinH = 0;
                mMaxH = 1;
                mHighSaturationRes = R.drawable.icon_background_12_a;
                mLowSaturationRes = R.drawable.icon_background_12_b;
                break;
            }
        }

        public boolean isSimilar(HSV hsv) {
            // 假如颜色的明暗度和饱和度都比较小，那么判断该颜色为黑白图
            if (mIndex == 0) {
                if (hsv.mS < 0.1f || hsv.mV < 0.1) {
                    return true;
                } else {
                    return false;
                }
                // 假如颜色分量靠近0，那么为纯红色
            } else if (mIndex == 12) {
                if (hsv.mH < 0.024590165f || hsv.mH > 0.9412065f) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return (hsv.mH >= mMinH && hsv.mH <= mMaxH);
            }

        }

        public void addSimilarColor(HSV hsv) {
            mSimilarColor.add(hsv);
        }

        public List<HSV> getSimilarColors() {
            return mSimilarColor;
        }

        public HSV getSimilarColorsAvgHSV() {
            float h = 0;
            float s = 0;
            float v = 0;
            if (mSimilarColor.size() > 0) {
                for (HSV hsv : mSimilarColor) {
                    h = h + hsv.mH;
                    s = s + hsv.mS;
                    v = v + hsv.mV;
                }
                h = h / mSimilarColor.size();
                s = s / mSimilarColor.size();
                v = v / mSimilarColor.size();
            }
            return new HSV(h, s, v);
        }

        public void clearSimilarColors() {
            mSimilarColor.clear();
        }

        public int getHighSaturationRes() {
            return mHighSaturationRes;
        }

        public int getLowSaturationRes() {
            return mLowSaturationRes;
        }

    }

    public static class HSV {
        public float mH;
        public float mS;
        public float mV;

        public HSV(float h, float s, float v) {
            mH = h;
            mS = s;
            mV = v;

        }

    }

    /***************************** App icon background relates end *******************/

    public static class CropImageInfo {
        public String mImageName;
        public int mAspectX;
        public int mAspectY;
        public File mTempCropImageFile;

        public int mSaveImageSizeX;
        public int mSaveImageSizeY;
        public String mSaveImagePath;
        public OnCropImageFinishedListener mOnCropImageFinishedListener;
    }

    public static interface OnCropImageFinishedListener {
        public void onResetImage(String imageName);

        public void onChangeImage(String imageName, String imageNewPath);
    }

    public static boolean getDeskObjectShelfShownPreference(Context context) {
        return SettingsActivity.getShowDeskObjectShelfSetting(context);
    }

    public static List<boolean[][]> getShelfSlotFromFile(Context context, String sceneName, int sizeX, int sizeY) {
        String fileName = context.getFilesDir() + File.separator + sceneName + "_shelf_slot";
        File saveFile = new File(fileName);
        if (!saveFile.exists()) {
            return null;
        }
        List<boolean[][]> shelfSlots = null;
        try {
            shelfSlots = new ArrayList<boolean[][]>();
            FileReader reader = new FileReader(saveFile);
            BufferedReader bRreader = new BufferedReader(reader);
            String line = null;
            while ((line = bRreader.readLine()) != null) {
                boolean[][] shelfSlot = new boolean[sizeY][sizeX];
                shelfSlots.add(shelfSlot);
                for (int y = 0; y < sizeY; y++) {
                    for (int x = 0; x < sizeX; x++) {
                        char c = line.charAt(y * sizeX + x);
                        shelfSlot[y][x] = c == '0' ? false : true;
                    }
                }
            }
            bRreader.close();

        } catch (Exception e) {
            return null;
        }
        return shelfSlots;

    }

    public static void saveShelfSlotToFile(Context context, String sceneName, List<boolean[][]> shelfSlots, int sizeX,
            int sizeY) {
        try {
            String fileName = context.getFilesDir() + File.separator + sceneName + "_shelf_slot";
            File saveFile = new File(fileName);
            if (saveFile.exists()) {
                saveFile.delete();
            }
            saveFile.createNewFile();
            FileOutputStream writer = new FileOutputStream(saveFile, true);

            for (boolean[][] shelfSlot : shelfSlots) {
                StringBuffer stringBuffer = new StringBuffer();
                for (int y = 0; y < sizeY; y++) {
                    for (int x = 0; x < sizeX; x++) {
                        String str = shelfSlot[y][x] ? "1" : "0";
                        stringBuffer.append(str);
                    }
                }
                stringBuffer.append("\n");
                writer.write(stringBuffer.toString().getBytes());
                writer.flush();
            }
            writer.close();

        } catch (Exception e) {

        }

    }
    
    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }

    public static ModelInfo getModelInfo(String resourcePath) {
        try {
            FileInputStream fis = new FileInputStream(resourcePath + File.separator + "models_config.xml");
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(fis, "utf-8");
            XmlUtils.beginDocument(parser, "config");
            ModelInfo config = ModelInfo.CreateFromXml(parser, null, resourcePath);
            fis.close();
            return config;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

}
