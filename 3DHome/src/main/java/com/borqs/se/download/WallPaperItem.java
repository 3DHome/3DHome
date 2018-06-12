package com.borqs.se.download;

import android.graphics.Bitmap;

import com.borqs.se.home3d.HomeManager;
import com.borqs.se.home3d.HomeScene;
import com.borqs.se.home3d.HomeUtils;

public class WallPaperItem {
    public enum WallPaperType {
        WALLPAPER, GROUNDPAPER
    }

    /**
     * 图片覆盖几面墙
     */
    public int mPaperCoverWallNum = 1;

    /**
     * 图片裁剪位置：假如一张图片贴三面墙，那么靠左边的墙为０，中间１，右边２
     */
    public int mCropImagePosition = 0;

    /**
     * 图片类型：是墙面的还是地面的
     */
    public WallPaperType mType;

    /**
     * 原始图片路径
     */
    public String mImagePath;
    public String mWallPaperImagePath;
    /**
     * 图片序号，决定图片贴在哪一面墙
     */
    public int mIndex = 0;

    public void processWallPaper(int wallPaperCropSizeX, int wallPaperCropSizeY, int wallPaperSizeX, int wallPaperSizeY) {
        if (mType == WallPaperType.GROUNDPAPER) {
            if (!processWallPaperOfGround(1024, 1024)) {
                processWallPaperOfGround(512, 512);
            }

        } else if (mType == WallPaperType.WALLPAPER) {
            processWallPaperOfWall(wallPaperCropSizeX, wallPaperCropSizeY, wallPaperSizeX, wallPaperSizeY);
        }
    }

    /**
     * 地板默认大小为1024*1024
     */
    private boolean processWallPaperOfGround(int w, int h) {
        Bitmap src = null;
        Bitmap tmp = null;
        Bitmap des = null;
        try {
            src = HomeUtils.decodeSampledBitmapFromResource(mImagePath, w, h);
            int cropW = src.getWidth();
            int cropH = src.getHeight();
            int startX = 0;
            int startY = 0;
            if (cropW > cropH) {
                cropW = cropH;
                startX = (src.getWidth() - cropW) / 2;
                startY = 0;
            } else {
                cropH = cropW;
                startX = 0;
                startY = (src.getHeight() - cropH) / 2;
            }
            tmp = Bitmap.createBitmap(src, startX, startY, cropW, cropH);
            if (src != tmp) {
                src.recycle();
            }
            des = HomeUtils.resizeBitmap(tmp, w, h);
            if (tmp != des) {
                tmp.recycle();
            }
            HomeUtils.saveBitmap(des, mWallPaperImagePath, Bitmap.CompressFormat.JPEG);
        } catch (Exception e) {
            return false;
        } finally {
            if (src != null && !src.isRecycled()) {
                src.recycle();
            }
            if (tmp != null && !tmp.isRecycled()) {
                tmp.recycle();
            }
            if (des != null && !des.isRecycled()) {
                des.recycle();
            }
        }
        return true;
    }

    private void processWallPaperOfWall(int wallPaperCropSizeX, int wallPaperCropSizeY, int wallPaperSizeX,
            int wallPaperSizeY) {
        HomeScene homeScene = HomeManager.getInstance().getHomeScene();
        if (homeScene == null) {
            return;
        }
        Bitmap bm = HomeUtils.decodeSampledBitmapFromResource(mImagePath, wallPaperCropSizeX * mPaperCoverWallNum,
                wallPaperCropSizeY);
        int cropW = bm.getWidth() / mPaperCoverWallNum;
        int cropH = bm.getHeight();
        int startX = 0;
        int startY = 0;
        if (cropW * wallPaperCropSizeY > wallPaperCropSizeX * cropH) {
            cropW = cropH * wallPaperCropSizeX / wallPaperCropSizeY;
            startX = cropW * mCropImagePosition + (bm.getWidth() - cropW * mPaperCoverWallNum) / 2;
            startY = 0;
        } else {
            cropH = cropW * wallPaperCropSizeY / wallPaperCropSizeX;
            startX = cropW * mCropImagePosition;
            startY = (bm.getHeight() - cropH) / 2;
        }
        bm = Bitmap.createBitmap(bm, startX, startY, cropW, cropH);
        Bitmap des = HomeUtils.resizeBitmap(bm, wallPaperSizeX, wallPaperSizeY);
        HomeUtils.saveBitmap(des, mWallPaperImagePath, Bitmap.CompressFormat.JPEG);
        des.recycle();
    }

}
