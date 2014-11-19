package com.borqs.se.home3d;

import android.content.Context;

import com.borqs.se.engine.SECameraData;
import com.borqs.se.engine.SEVector.SEVector3f;

public class HomeSceneInfo {
    public String mSceneName;
    public int mWallIndex = 0;
    public SECameraData mSECameraData;
    private int mSceneWidth = 0;
    private int mSceneHeight = 0;
    private ThemeInfo mThemeInfo;
    private Context mContext;
    /** 下面四个值代表的意思是相机距离房间中心点的最小/大值以及相机在最小位置的张角和最大位置的张角 **/
    private float mMinCameraRadius;
    private float mMaxCameraRadius;
    private float mBestCameraRadius;

    private float mMinCameraFov;
    private float mMaxCameraFov;
    private float mBestCameraFov;

    private float mMinCameraDownUp;
    private float mMaxCameraDownUp;

    public HomeSceneInfo() {
        mContext = HomeManager.getInstance().getContext();
    }

    public void setThemeInfo(ThemeInfo themeInfo) {
        mSceneName = themeInfo.mSceneName;
        mWallIndex = themeInfo.mCurrentWallIndex;
        mSECameraData = themeInfo.mSECameraData;
        mMinCameraRadius = Math.abs(themeInfo.mNearestCameraLocation.getY());
        mMaxCameraRadius = Math.abs(themeInfo.mFarthestCameraLocation.getY());
        mBestCameraRadius = Math.abs(themeInfo.mBestCameraLocation.getY());
        mMinCameraFov = themeInfo.mNearestCameraFov;
        mMaxCameraFov = themeInfo.mFarthestCameraFov;
        mBestCameraFov = themeInfo.mBestCameraFov;
        mMinCameraDownUp = themeInfo.mBestCameraLocation.getZ() - 80;
        mMaxCameraDownUp = themeInfo.mBestCameraLocation.getZ() + 80;
        mThemeInfo = themeInfo;
    }

    /**
     * 计算出当前相机位置张角应该为多少合适,即相机位置变化时调整张角。
     */
    public float getCameraFovByRadius(float radius) {
        float curFov;
        if (radius < mBestCameraRadius) {
            curFov = (mBestCameraRadius - radius) * (mMinCameraFov - mBestCameraFov)
                    / (mBestCameraRadius - mMinCameraRadius) + mBestCameraFov;
        } else {
            curFov = (radius - mBestCameraRadius) * (mMaxCameraFov - mBestCameraFov)
                    / (mMaxCameraRadius - mBestCameraRadius) + mBestCameraFov;
        }
        return curFov;
    }

    public float getCameraMinRadius() {
        return mMinCameraRadius;
    }

    public float getCameraMaxRadius() {
        return mMaxCameraRadius;
    }

    public float getCameraMinDownUp() {
        return mMinCameraDownUp;
    }

    public float getCameraMaxDownUp() {
        return mMaxCameraDownUp;
    }

    public ThemeInfo getThemeInfo() {
        return mThemeInfo;
    }

    public void updateWallIndex(int index) {
        mThemeInfo.mCurrentWallIndex = index;
        mThemeInfo.updateWallIndex(HomeManager.getInstance().getContext());
    }

    public void updateCameraDataToDB(SEVector3f location, float fov) {
        mThemeInfo.updateCameraDataToDB(mContext, location, fov);
    }

    public void notifySurfaceChanged(int width, int height) {
        mSceneWidth = width;
        mSceneHeight = height;
    }

    // moved from SESceneManager
    public int getSceneWidth() {
        return mSceneWidth;
    }

    public int getSceneHeight() {
        return mSceneHeight;
    }

}
