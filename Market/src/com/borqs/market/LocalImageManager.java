package com.borqs.market;

import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.borqs.market.utils.MarketUtils;
import com.borqs.market.utils.QiupuHelper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;

public class LocalImageManager {
    public interface ImageUpdateListener {
        public String getImageName();

        public void onImageUpdated(Bitmap image);
    }

    private List<HandlerThread> sWorkerThreads;
    private List<Handler> sWorkers;

    private Handler mUIMSGHandler;
    private List<ImageItem> ImageItems = new ArrayList<ImageItem>();
    private boolean mCanWork;
    private static int mThreadCount = 0;
    private int mLimitSize;
    private int mSizeOfThread;

    public LocalImageManager(Handler handler) {
        mUIMSGHandler = handler;
        mSizeOfThread = 1;
        mLimitSize = 30;
    }

    public void setThreadNumber(int threadNum) {
        mSizeOfThread = threadNum;
    }

    public void onResume() {
        if (!mCanWork) {
            sWorkerThreads = new ArrayList<HandlerThread>();
            sWorkers = new ArrayList<Handler>();
            for (int i = 0; i < mSizeOfThread; i++) {
                HandlerThread sWorkerThread = new HandlerThread("local-image-loader-" + mThreadCount);
                sWorkerThreads.add(sWorkerThread);
                sWorkerThread.start();
                mThreadCount++;
                Handler sWorker = new Handler(sWorkerThread.getLooper());
                sWorkers.add(sWorker);
            }
            mCanWork = true;
        }
    }

    public void ondestory() {
        if (mCanWork) {
            mCanWork = false;
            for (HandlerThread sWorkerThread : sWorkerThreads) {
                sWorkerThread.quit();
            }
            sWorkerThreads.clear();
            sWorkers.clear();
            for (ImageItem item : ImageItems) {
                item.destory();
            }
            ImageItems.clear();

        }
    }

    public void removeImageUpdateListener(ImageUpdateListener l) {
        if (l == null) {
            return;
        }
        for (ImageItem item : ImageItems) {
            if (item.mImagePath.equals(l.getImageName())) {
                item.removeImageUpdateListener(l);
            }
        }
    }

    public void addImageUpdateListener(int w, ImageUpdateListener l) {
        getBitmap(w, 0, l, true);
    }

    public void addImageUpdateListener(int w, int h, ImageUpdateListener l) {
        getBitmap(w, h, l, false);
    }

    private void getBitmap(int w, int h, ImageUpdateListener l, boolean limitWidth) {
        if (!mCanWork) {
            return;
        }

        ImageItem imageItem = null;
        for (ImageItem item : ImageItems) {
            if (item.mImagePath.equals(l.getImageName())) {
                // 比较活跃的图片放在队列最后面
                imageItem = item;
                ImageItems.remove(imageItem);
                ImageItems.add(imageItem);
                item.addImageUpdateListener(l);
                if (item.mBitmap == null) {
                    imageItem.loadImage(limitWidth);
                }
                break;
            }
        }

        if (imageItem == null) {
            imageItem = new ImageItem(l.getImageName(), w, h);
            imageItem.addImageUpdateListener(l);
            imageItem.loadImage(limitWidth);
            ImageItems.add(imageItem);
        }
        if (ImageItems.size() > mLimitSize) {
            // 删除队列中不活跃的图片
            for (ImageItem item : ImageItems) {
                if (item.canReleased()) {
                    item.cancelLoad();
                    item.destory();
                    ImageItems.remove(item);
                    break;
                }
            }
        }
    }

    private int mCurrentWorksIndex = 0;

    private class ImageItem {
        public String mImagePath;
        public Bitmap mBitmap;
        public int mImageW;
        public int mImageH;
        public List<ImageUpdateListener> mImageUpdateListener = new ArrayList<ImageUpdateListener>();
        private Runnable mTask;
        private Handler sWorker;
        private boolean mWrongURL = false;

        public ImageItem(String imagePath, int w, int h) {
            mImageW = w;
            mImageH = h;
            mImagePath = imagePath;
        }

        public void addImageUpdateListener(ImageUpdateListener l) {
            if (l != null) {
                l.onImageUpdated(mBitmap);
            }
            if (!mImageUpdateListener.contains(l)) {
                mImageUpdateListener.add(l);
            }
        }

        public void removeImageUpdateListener(ImageUpdateListener l) {
            if (mImageUpdateListener.contains(l)) {
                mImageUpdateListener.remove(l);
            }
        }

        public void destory() {
            if (mBitmap != null) {
                mBitmap.recycle();
                mBitmap = null;
            }
        }

        public boolean canReleased() {
            return mImageUpdateListener.size() == 0;
        }

        public void cancelLoad() {
            if (mTask != null && sWorker != null) {
                sWorker.removeCallbacks(mTask);
                sWorker = null;
            }
        }

        public void loadImage(final boolean limitWidth) {
            cancelLoad();
            if (!mCanWork || mWrongURL) {
                return;
            }
            mTask = new Runnable() {
                public void run() {
                    if (!mCanWork || sWorker == null) {
                        return;
                    }
                    String localPath = mImagePath;

                    Uri uri = Uri.parse(localPath);
                    String scheme = uri.getScheme();
                    final Bitmap res;
                    if (!TextUtils.isEmpty(scheme) && (scheme.equalsIgnoreCase("file"))) {
                        localPath = uri.getPath();
                        res = loadImageLocal(limitWidth, localPath);
                    } else {
                        res = loadImageOnLine(limitWidth, mImagePath);
                    }

                    mUIMSGHandler.post(new Runnable() {
                        public void run() {
                            mBitmap = res;
                            if (mCanWork && sWorker != null) {
                                for (ImageUpdateListener l : mImageUpdateListener) {
                                    l.onImageUpdated(mBitmap);
                                }
                            }
                        }
                    });

                }
            };
            mCurrentWorksIndex = mCurrentWorksIndex % mSizeOfThread;
            if (sWorkers != null && sWorkers.size() > mCurrentWorksIndex) {
                sWorker = sWorkers.get(mCurrentWorksIndex);
                sWorker.postAtFrontOfQueue(mTask);
                mCurrentWorksIndex++;
            }
        }

        private Bitmap loadImageLocal(boolean limitWidth, String localPath) {
            Bitmap res = null;
            File file = new File(localPath);
            String parentPath = file.getParent();
            File newFile = new File(parentPath + File.separator + mImageW + "x" + mImageH + "_cache_" + file.getName());
            try {
                if (newFile.exists()) {
                    FileInputStream fis = new FileInputStream(newFile);
                    res = BitmapFactory.decodeStream(fis);
                    fis.close();
                }
            } catch (Exception e) {

            }

            if (res == null && file.exists()) {
                if (limitWidth) {
                    Bitmap bitmap = MarketUtils.decodeSampledBitmapFromResource(localPath, mImageW, Integer.MAX_VALUE);
                    if (bitmap != null) {
                        if (bitmap.getWidth() > mImageW) {
                            int newHeight = mImageW * bitmap.getHeight() / bitmap.getWidth();
                            res = Bitmap.createScaledBitmap(bitmap, mImageW, newHeight, true);
                        } else {
                            res = bitmap;
                        }
                        if (file.getName().endsWith(".png")) {
                            MarketUtils.saveBitmap(res, newFile.getPath(), Bitmap.CompressFormat.PNG);
                        } else {
                            MarketUtils.saveBitmap(res, newFile.getPath(), Bitmap.CompressFormat.JPEG);
                        }
                    }

                } else {
                    Bitmap bitmap = MarketUtils.decodeSampledBitmapFromResource(localPath, mImageW, mImageH);
                    if (bitmap != null) {
                        if (bitmap.getWidth() != mImageW || bitmap.getHeight() != mImageH) {
                            res = Bitmap.createScaledBitmap(bitmap, mImageW, mImageH, true);
                        } else {
                            res = bitmap;
                        }
                        if (file.getName().endsWith(".png")) {
                            MarketUtils.saveBitmap(res, newFile.getPath(), Bitmap.CompressFormat.PNG);
                        } else {
                            MarketUtils.saveBitmap(res, newFile.getPath(), Bitmap.CompressFormat.JPEG);
                        }
                    }

                }
            }
            return res;
        }

        private Bitmap loadImageOnLine(boolean limitWidth, String url) {
            Bitmap res = null;
            URL imageUrl = null;
            try {
                imageUrl = new URL(url);
            } catch (MalformedURLException e1) {
                mWrongURL = true;
                e1.printStackTrace();
            }
            if (imageUrl == null) {
                return res;
            }
            String imageSavePath = MarketUtils.getImageFilePath(imageUrl, true);
            File file = new File(imageSavePath);
            try {
                if (file.exists()) {
                    FileInputStream fis = new FileInputStream(imageSavePath);
                    res = BitmapFactory.decodeStream(fis);
                    fis.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (res == null) {
                boolean downloadSuc = QiupuHelper.downloadImageFromInternet(imageUrl, file);
                if (downloadSuc) {
                    if (limitWidth) {
                        Bitmap bitmap = MarketUtils.decodeSampledBitmapFromResource(imageSavePath, mImageW,
                                Integer.MAX_VALUE);
                        if (bitmap != null) {
                            if (bitmap.getWidth() > mImageW) {
                                int newHeight = mImageW * bitmap.getHeight() / bitmap.getWidth();
                                res = Bitmap.createScaledBitmap(bitmap, mImageW, newHeight, true);
                            } else {
                                res = bitmap;
                            }
                            if (file.getName().endsWith(".png")) {
                                MarketUtils.saveBitmap(res, imageSavePath, Bitmap.CompressFormat.PNG);
                            } else {
                                MarketUtils.saveBitmap(res, imageSavePath, Bitmap.CompressFormat.JPEG);
                            }
                        }
                    } else {
                        Bitmap bitmap = MarketUtils.decodeSampledBitmapFromResource(imageSavePath, mImageW, mImageH);
                        if (bitmap != null) {
                            if (bitmap.getWidth() != mImageW || bitmap.getHeight() != mImageH) {
                                res = Bitmap.createScaledBitmap(bitmap, mImageW, mImageH, true);

                            } else {
                                res = bitmap;
                            }
                            if (file.getName().endsWith(".png")) {
                                MarketUtils.saveBitmap(res, imageSavePath, Bitmap.CompressFormat.PNG);
                            } else {
                                MarketUtils.saveBitmap(res, imageSavePath, Bitmap.CompressFormat.JPEG);
                            }
                        }
                    }
                }
            }
            return res;
        }

        @Override
        public boolean equals(Object o) {
            if (o != null && (o instanceof ImageItem)) {
                ImageItem item = (ImageItem) o;
                if (item.mImagePath.equals(mImagePath)) {
                    return true;
                }
            }
            return false;
        }

    }

}
