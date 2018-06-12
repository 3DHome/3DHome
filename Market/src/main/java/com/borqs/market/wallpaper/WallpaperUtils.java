package com.borqs.market.wallpaper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.borqs.market.api.ApiUtil;
import com.borqs.market.json.Product;
import com.borqs.market.net.RequestListener;
import com.borqs.market.net.WutongException;
import com.borqs.market.utils.BLog;
import com.borqs.market.utils.MarketConfiguration;
import com.borqs.market.utils.MarketUtils;

/**
 * Created by b608 on 13-8-1.
 */
public class WallpaperUtils {
    private static final String TAG = "WallpaperUtils";

    public static final String EXTRA_RAW_WALL_PAPERS = "EXTRA_RAW_WALL_PAPERS";
    public static final String EXTRA_RAW_WALL_COUNT = "EXTRA_RAW_WALL_COUNT";

    private WallpaperUtils() {

    }

    public static boolean ENABLE_BACKDOOR = false;

    /**
     * 
     * @param context
     */
    public static void startExportOrImportIntent(Context context, boolean isPort, int maxPaperSize,
            ArrayList<RawPaperItem> wallPaperItems) {
        final String package_name = MarketConfiguration.PACKAGE_NAME;
        if (TextUtils.isEmpty(package_name)) {
            throw new IllegalArgumentException("package name is null");
        }
        try {
            int version_code = context.getPackageManager().getPackageInfo(package_name,
                    PackageManager.GET_UNINSTALLED_PACKAGES).versionCode;

            Intent intent = new Intent(context, WallpaperExportActivity.class);
            intent.putExtra(MarketUtils.EXTRA_APP_VERSION, version_code);
            intent.putExtra(MarketUtils.EXTRA_PACKAGE_NAME, package_name);
            intent.putExtra(MarketUtils.EXTRA_CATEGORY, MarketUtils.CATEGORY_WALLPAPER);
            intent.putExtra(MarketUtils.EXTRA_MOD, isPort ? Product.SupportedMod.PORTRAIT
                    : Product.SupportedMod.LANDSCAPE);
            intent.putParcelableArrayListExtra(WallpaperUtils.EXTRA_RAW_WALL_PAPERS, wallPaperItems);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intent);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final String MANIFEST_FILE_NAME = "ResourceManifest.xml";

    private static void runFolderRemove(String path) {
        try {
            Runtime.getRuntime().exec("rm -r " + path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int parseOrientationMode(String supported_mod) {
        if (Product.SupportedMod.LANDSCAPE.equalsIgnoreCase(supported_mod)) {
            return Wallpaper.PaperOrientation.LAND;
        } else if (Product.SupportedMod.PORTRAIT.equalsIgnoreCase(supported_mod)) {
            return Wallpaper.PaperOrientation.PORT;
        } else {
            return Wallpaper.PaperOrientation.NONE;
        }
    }

    public static void exportWallpaperSuite(Wallpaper.Builder builder, String supported_mod,
            final RequestListener listener) {

        Wallpaper.PaperOrientation orientation = new Wallpaper.PaperOrientation();
        orientation.orientation = parseOrientationMode(supported_mod);
        final String exportRoot = MarketUtils.getExportWallpaperCatchPath();
        final File manifestFile = new File(exportRoot + File.separator + MANIFEST_FILE_NAME);
        try {
            if (!manifestFile.exists()) {
                manifestFile.createNewFile();
            }

            builder.setOrientation(orientation);
            builder.buildManifest(manifestFile);
            final String zipFileName = exportRoot + File.separator + "wallpaper.zip";
            ZipFolder(exportRoot, zipFileName);
            ApiUtil.getInstance().shareUpload(builder.hostPackageName, builder.appVersion,
                    Product.ProductType.WALL_PAPER, supported_mod, new File(zipFileName), new RequestListener() {
                        @Override
                        public void onComplete(String response) {
                            listener.onComplete(response);
                            runFolderRemove(exportRoot);
                        }

                        @Override
                        public void onIOException(IOException e) {
                            listener.onIOException(e);
                        }

                        @Override
                        public void onError(WutongException e) {
                            listener.onError(e);
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clearAppliedFlag(Context context) {
        MarketUtils.updatePlugIn(context, "dumb_id", false, MarketUtils.CATEGORY_WALLPAPER);
    }

    private static boolean isFileExisting(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        File testFile = new File(path);
        return testFile.exists();
    }

    public static ArrayList<RawPaperItem> decodePaper(Context context, ArrayList<RawPaperItem> srcList) {
        String folderName = MarketUtils.getExportWallpaperCatchPath();
        File folder = new File(folderName);
        if (folder.exists()) {
            MarketUtils.deleteFile(folderName);
        }

        ArrayList<RawPaperItem> decodedList = new ArrayList<RawPaperItem>();
        RawPaperItem decodeItem;
        int groundPaperIndex = 0;
        int wallPaperIndex = 0;
        for (RawPaperItem item : srcList) {
            File file = new File(item.mImagePath);
            String cacheImagePath;
            if (item.mType.equals(RawPaperItem.TYPE_GROUND)) {
                cacheImagePath = MarketUtils.getExportWallpaperCatchPath() + File.separator + "ground_paper_"
                        + groundPaperIndex + ".jpg";
                groundPaperIndex++;
            } else {
                cacheImagePath = MarketUtils.getExportWallpaperCatchPath() + File.separator + "wall_paper_"
                        + wallPaperIndex + ".jpg";
                wallPaperIndex++;
            }
            Bitmap bitmap = null;
            try {
                InputStream fis = null;
                if (file.exists()) {
                    fis = new FileInputStream(file);
                } else if (item.mImagePath.startsWith("assets")) {
                    fis = context.getAssets().open(item.mImagePath.substring(7));
                }
                Bitmap tmp = BitmapFactory.decodeStream(fis);
                if (item.mType.equals(RawPaperItem.TYPE_GROUND)) {
                    bitmap = tmp;
                } else {
                    int w = tmp.getWidth();
                    int h = tmp.getHeight();
                    int newW = item.mWallPaperCropSizeX;
                    int newH = item.mWallPaperCropSizeY;
                    if (h * newW < w * newH) {
                        newW = (h * newW) / newH;
                        newH = h;
                    } else {
                        newW = w;
                        newH = (w * newH) / newW;
                    }
                    int x = (w - newW) / 2;
                    int y = (h - newH) / 2;
                    bitmap = Bitmap.createBitmap(tmp, x, y, newW, newH);
                }
            } catch (Exception e) {
            }
            if (bitmap != null) {
                MarketUtils.saveBitmap(bitmap, cacheImagePath, Bitmap.CompressFormat.JPEG);
            }
            decodeItem = new RawPaperItem(item);
            decodeItem.mImagePath = cacheImagePath;
            decodedList.add(decodeItem);
        }

        return decodedList;
    }

    /**
     * 压缩文件,文件夹
     * 
     * @param srcFileString
     *            要压缩的文件/文件夹名字
     * @param zipFileString
     *            指定压缩的目的和名字
     * @throws Exception
     */
    public static void ZipFolder(String srcFileString, String zipFileString) throws Exception {
        android.util.Log.v("XZip", "ZipFolder(String, String)");
        File zipFile = new File(zipFileString);
        if (zipFile.exists()) {
            zipFile.delete();
        }
        List<String> allFileInFolder = new ArrayList<String>();
        java.io.File file = new java.io.File(srcFileString);
        if (file.isDirectory()) {
            String fileList[] = file.list();
            for (int i = 0; i < fileList.length; i++) {
                if (!fileList[i].contains("_cache_")) {
                    allFileInFolder.add(fileList[i]);
                }
            }
        }
        java.util.zip.ZipOutputStream outZip = new java.util.zip.ZipOutputStream(new java.io.FileOutputStream(zipFile));
        for (String fileInFolder : allFileInFolder) {
            ZipFiles(srcFileString, fileInFolder, outZip);
        }

        outZip.finish();
        outZip.close();

    }// end of func

    /**
     * 压缩文件
     * 
     * @param folderString
     * @param fileString
     * @param zipOutputSteam
     * @throws Exception
     */
    private static void ZipFiles(String folderString, String fileString, java.util.zip.ZipOutputStream zipOutputSteam)
            throws Exception {
        android.util.Log.v("XZip", "ZipFiles(String, String, ZipOutputStream)");

        if (zipOutputSteam == null)
            return;

        java.io.File file = new java.io.File(folderString + java.io.File.separator + fileString);

        // 判断是不是文件
        if (file.isFile()) {

            java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry(fileString);
            java.io.FileInputStream inputStream = new java.io.FileInputStream(file);
            zipOutputSteam.putNextEntry(zipEntry);

            int len;
            byte[] buffer = new byte[4096];

            while ((len = inputStream.read(buffer)) != -1) {
                zipOutputSteam.write(buffer, 0, len);
            }

            zipOutputSteam.closeEntry();
        } else {

            // 文件夹的方式,获取文件夹下的子文件
            String fileList[] = file.list();

            // 如果没有子文件, 则添加进去即可
            if (fileList.length <= 0) {
                java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry(fileString + java.io.File.separator);
                zipOutputSteam.putNextEntry(zipEntry);
                zipOutputSteam.closeEntry();
            }

            // 如果有子文件, 遍历子文件
            for (int i = 0; i < fileList.length; i++) {
                ZipFiles(folderString, fileString + java.io.File.separator + fileList[i], zipOutputSteam);
            }// end of for

        }// end of if

    }// end of func

    public static ArrayList<RawPaperItem> parseRawPaperItemList(String parentPath) {
        if (isFileExisting(parentPath)) {
            final String manifestPath = parentPath + File.separator + MANIFEST_FILE_NAME;
            if (isFileExisting(manifestPath)) {
                File file = new File(manifestPath);
                BLog.d(TAG, WallpaperXmlParser.test(file));
                Wallpaper.Builder builder = Wallpaper.Builder.createPaperUnitFromFile(file);
                if (null != builder) {
                    return builder.paperUnits;
                }
            }
        }
        return null;
    }

}
