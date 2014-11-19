package com.borqs.se.thumbnails;

import java.io.File;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;

import com.borqs.se.R;
import com.borqs.se.home3d.HomeUtils;

public class VideoThumbnailsService {

    public static int CREATE_THUMBNAILS = 0;
    private ContentResolver mResolver;
    private MediaDBObserver mObserver;
    private Task mTask;
    private Drawable mPlayBitmap;
    private Context mContext;

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == CREATE_THUMBNAILS) {
                if (mTask != null) {
                    mTask.mStop = true;
                }
                mTask = new Task();
                mTask.start();
            }
        }
    };

    public void start(Context context) {
        mContext = context;
        HomeUtils.deleteFile(HomeUtils.getThumbPath());
        mHandler.sendEmptyMessageDelayed(CREATE_THUMBNAILS, 10000);
        mResolver = mContext.getContentResolver();
        if (mObserver == null) {
            HandlerThread mediaDBObserverThread = new HandlerThread("MediaDBObserverThread");
            mediaDBObserverThread.start();
            Handler handler = new Handler(mediaDBObserverThread.getLooper());
            mObserver = new MediaDBObserver(handler, mContext);
        }
        mObserver.startObserving();
    }

    private class Task extends Thread {
        public boolean mStop = false;
        private String[] mPreListFiles;

        @Override
        public void run() {
            if (mStop) {
                return;
            }
            mPreListFiles = getThumbNameList();
            try {
                getAndSaveVideoThumbnail(MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            } catch (Exception e) {
                Log.e(HomeUtils.TAG, "create video thumb failed : " + e.getMessage());
            }
        }

        public void getAndSaveVideoThumbnail(Uri uri) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inDither = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Cursor cursor = mResolver.query(uri,
                    new String[] { MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA }, null, null, null);
            if (cursor == null || cursor.getCount() == 0) {
                return;
            }
            if (mPlayBitmap == null) {
                mPlayBitmap = getPlayBitmap();
            }
            int index = 0;
            while (cursor.moveToNext() && !mStop) {
                boolean shoudBreak = false;
                String pathA;
                String pathB;
                long idB;
                pathA = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                if (cursor.moveToNext()) {
                    idB = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                    pathB = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                    cursor.moveToPrevious();
                } else {
                    cursor.moveToFirst();
                    idB = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                    pathB = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                    shoudBreak = true;
                }
                String imgName = String.valueOf(idB);
                if (mPreListFiles != null && mPreListFiles.length > index) {
                    if (mPreListFiles[index].equals(imgName)) {
                        index++;
                        if (shoudBreak) {
                            break;
                        } else {
                            continue;
                        }
                    } else {
                        for (int i = index; i < mPreListFiles.length; i++) {
                            File file = new File(HomeUtils.getThumbPath() + File.separator + mPreListFiles[i]);
                            file.delete();
                        }
                        mPreListFiles = null;
                    }
                }
                Bitmap bitmapA = ThumbnailUtils.createVideoThumbnail(pathA, Images.Thumbnails.MINI_KIND);
                Bitmap bitmapB = ThumbnailUtils.createVideoThumbnail(pathB, Images.Thumbnails.MINI_KIND);
                createBitmap(bitmapA, bitmapB, imgName);
                index++;
                if (shoudBreak) {
                    break;
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
        }

        private void createBitmap(Bitmap preBitmap, Bitmap bitmap, String name) {
            Bitmap des = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(des);
            int w;
            int h;
            int newW;
            int newH;
            int left;
            int top;
            Bitmap tmp;
            if (preBitmap != null) {
                w = preBitmap.getWidth();
                h = preBitmap.getHeight();
                if (w > 2 * h) {
                    newW = 512;
                    newH = h * 512 / w;
                } else {
                    newH = 256;
                    newW = w * 256 / h;
                }
                left = (512 - newW) / 2;
                top = (256 - newH) / 2;
                tmp = Bitmap.createScaledBitmap(preBitmap, newW, newH, true);
                canvas.drawBitmap(tmp, left, top, null);
                tmp.recycle();
            }
            if (bitmap != null) {
                w = bitmap.getWidth();
                h = bitmap.getHeight();
                if (w > 2 * h) {
                    newW = 512;
                    newH = h * 512 / w;
                } else {
                    newH = 256;
                    newW = w * 256 / h;
                }
                left = (512 - newW) / 2;
                top = 256 + (256 - newH) / 2;
                tmp = Bitmap.createScaledBitmap(bitmap, newW, newH, true);
                canvas.drawBitmap(tmp, left, top, null);
                tmp.recycle();
            }
            if (mPlayBitmap != null) {
                canvas.save();
                canvas.translate(208, 80);
                mPlayBitmap.setBounds(0, 0, 96, 96);
                mPlayBitmap.draw(canvas);
                canvas.restore();
                canvas.save();
                canvas.translate(208, 336);
                mPlayBitmap.setBounds(0, 0, 96, 96);
                mPlayBitmap.draw(canvas);
                canvas.restore();
            }
            HomeUtils.saveBitmap(des, HomeUtils.getThumbPath() + File.separator + name, Bitmap.CompressFormat.PNG);
        }

    }

    private String[] getThumbNameList() {
        File dir = new File(HomeUtils.getThumbPath());
        if (dir.exists() && dir.isDirectory()) {
            return dir.list();
        }
        return null;
    }

    public void stopObserving() {
        Log.i("3DHome", "Thumb service has been destroyed.");
        if (mObserver != null) {
            mObserver.stopObserving();
        }
    }

    class MediaDBObserver extends ContentObserver {

        private Context mContext;

        MediaDBObserver(Handler handler, Context context) {
            super(handler);
            mContext = context;
        }

        void startObserving() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, false, this);
            resolver.registerContentObserver(MediaStore.Video.Media.INTERNAL_CONTENT_URI, false, this);
        }

        void stopObserving() {
            mContext.getContentResolver().unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange) {
            mHandler.removeMessages(CREATE_THUMBNAILS);
            mHandler.sendEmptyMessageDelayed(CREATE_THUMBNAILS, 10000);
        }
    }

    private Drawable getPlayBitmap() {
        return mContext.getResources().getDrawable(R.drawable.tvplay);
    }

}
