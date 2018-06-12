package com.borqs.se.widget3d;

import java.io.File;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.borqs.se.engine.SEAnimFinishListener;
import com.borqs.se.engine.SEEmptyAnimation;
import com.borqs.se.engine.SEScene;
import com.borqs.se.engine.SELoadResThread;
import com.borqs.se.engine.SEObject;
import com.borqs.se.engine.SEAnimation.CountAnimation;
import com.borqs.se.engine.SEVector.AXIS;
import com.borqs.se.engine.SEVector.SEVector2f;
import com.borqs.se.engine.SEVector.SEVector3f;
import com.borqs.se.home3d.HomeScene;
import com.borqs.se.home3d.HomeUtils;
import com.borqs.se.shortcut.LauncherModel;

public class Television extends WallObject {
    private SEObject mGUANG;
    private UpdateAnimation mUpdateAnimation;
    private PlayVideoThumbnails mPlayAnimation;
    private String mImageName;
    private int mFaceIndex = -1;
    private String mCurrentImageName;
    private int mCurrentImage;
    private boolean mHasScreen = false;

    public Television(HomeScene scene, String name, int index) {
        super(scene, name, index);
    }

    @Override
    public void initStatus() {
        super.initStatus();
        setCanChangeBind(false);
        mGUANG = findComponenetObjectByRegularName("screen");
        if (mGUANG != null) {
            mHasScreen = true;
            mGUANG.setUserScale(new SEVector3f(0, 0, 0));
            mGUANG.setVisible(true);
            mImageName = mGUANG.getImageName();
        }
        mCurrentImage = -1;
    }

    @Override
    public void handOnClick() {
        if (mCurrentImageName == null || !startToPlayVideo() || !mHasScreen) {
            super.handOnClick();
        }
    }

    public void onWallRadiusChanged(int faceIndex) {
        mFaceIndex = faceIndex;
        if (isPressed() || !mHasScreen) {
            return;
        }
        if (mFaceIndex == getObjectInfo().getSlotIndex()) {
            if (mCurrentImageName == null) {
                String[] thumbList = getThumbNameList();
                if (thumbList != null && thumbList.length > 0) {
                    mCurrentImage = 0;
                    mCurrentImageName = thumbList[mCurrentImage];
                    LauncherModel.getInstance().removeTask(mPlayCycle);
                    mPlayAnimation = new PlayVideoThumbnails(getScene(), 0, 1, 60);
                    mPlayAnimation.execute();
                    return;
                }
            }
            if (mCurrentImageName == null) {
                if (mUpdateAnimation == null || mUpdateAnimation.isFinish()) {
                    mUpdateAnimation = new UpdateAnimation(getScene());
                    mUpdateAnimation.execute();
                }
            } else {
                LauncherModel.getInstance().removeTask(mPlayCycle);
                LauncherModel.getInstance().process(mPlayCycle, 2500);
            }
        }
    }

    private void playUVAnimation() {
        if (mPlayAnimation == null || mPlayAnimation.isFinish()) {
            String[] thumbList = getThumbNameList();
            if (thumbList != null && thumbList.length > 0) {
                mGUANG.setBlendingable(false);
                mGUANG.setNeedForeverBlend(false);
                mCurrentImage++;
                if (mCurrentImage >= thumbList.length) {
                    mCurrentImage = 0;
                }
                mCurrentImageName = thumbList[mCurrentImage];
                LauncherModel.getInstance().removeTask(mPlayCycle);
                mPlayAnimation = new PlayVideoThumbnails(getScene(), 0, 1, 60);
                mPlayAnimation.execute();
            }

        }
    }

    private class UpdateAnimation extends CountAnimation {
        private float mStep;

        public UpdateAnimation(SEScene scene) {
            super(scene);
        }

        public void runPatch(int count) {
            if (count <= 15) {
                mStep = mStep + 0.05f;
                SEVector3f scale = new SEVector3f(1, 1, mStep);
                mGUANG.setUserScale(scale);
            } else if (count <= 25) {
                mStep = mStep - 0.05f;
                SEVector3f scale = new SEVector3f(1, 1, mStep);
                mGUANG.setUserScale(scale);
            } else if (count <= 37) {
                mStep = mStep + 0.075f;
                SEVector3f scale = new SEVector3f(1, 1, mStep);
                mGUANG.setUserScale(scale);
            } else if (count <= 50) {
                mStep = mStep - 0.1f;
                SEVector3f scale = new SEVector3f(1, 1, mStep);
                mGUANG.setUserScale(scale);
            } else if (count <= 100) {
                if (count == 51) {
                    mStep = -0.5f;
                    mGUANG.setUserScale(new SEVector3f(0, 0, 0));
                }
                mStep = mStep + 0.01f;
                if (count == 100) {
                    mStep = 1;
                }
            } else {
                mStep = mStep - 0.1f;
            }
        }

        @Override
        public void onFinish() {
            super.onFinish();
            mGUANG.setUserScale(new SEVector3f(0, 0, 0));
            mGUANG.setBlendingable(false);
            mGUANG.setNeedForeverBlend(false);
        }

        @Override
        public void onFirstly(int count) {
            super.onFirstly(count);
            mStep = 0;
            mGUANG.playUVAnimation(new SEVector2f(0, 0));
            mGUANG.setBlendingable(true);
            mGUANG.setNeedForeverBlend(true);
            mGUANG.setBlendSortAxis(AXIS.Y);
        }

        @Override
        public int getAnimationCount() {
            return 50;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////////
    private class PlayVideoThumbnails extends SEEmptyAnimation {
        private int mImageData = 0;
        private boolean mHasLoadImage = false;
        private boolean mCanUpdate = false;

        public PlayVideoThumbnails(SEScene scene, float from, float to, int times) {
            super(scene, from, to, times);
            setAnimFinishListener(new SEAnimFinishListener() {

                @Override
                public void onAnimationfinish() {
                    LauncherModel.getInstance().removeTask(mPlayCycle);
                    LauncherModel.getInstance().process(mPlayCycle, 8000);
                }

            });
        }

        @Override
        public void onBegin() {
            super.onBegin();
            changeImage(mCurrentImageName);
            mGUANG.setUserScale(new SEVector3f(1, 1, 1));
        }

        @Override
        public void onAnimationRun(float value) {
            super.onAnimationRun(value);
            if (mHasLoadImage && mImageData == 0) {
                stop();
            } else if (mHasLoadImage && !mCanUpdate) {
                SEObject.applyImage_JNI(mImageName, mCurrentImageName);
                SEObject.addImageData_JNI(mCurrentImageName, mImageData);
                mCanUpdate = true;
            }

            if (mCanUpdate) {
                SEVector2f dirCloud = new SEVector2f(0, -value / 2);
                mGUANG.playUVAnimation(dirCloud);
            }
        }

        private void changeImage(final String name) {
            SELoadResThread.getInstance().process(new Runnable() {
                public void run() {
                    mImageData = SEObject.loadImageData_JNI(HomeUtils.getThumbPath() + File.separator + name);
                    mHasLoadImage = true;
                }
            });
        }

    }

    Runnable mPlayCycle = new Runnable() {
        public void run() {
            if (mFaceIndex == getObjectInfo().getSlotIndex()) {
                playUVAnimation();
            }
        }

    };

    private String[] getThumbNameList() {
        File dir = new File(HomeUtils.getThumbPath());
        if (dir.exists() && dir.isDirectory()) {
            return dir.list();
        }
        return null;
    }

    private boolean startToPlayVideo() {
        if (!TextUtils.isEmpty(mCurrentImageName)) {
            String path = getPath(mCurrentImageName);
            if (!TextUtils.isEmpty(path)) {
                Uri fileUri = Uri.fromFile(new File(path));
                final Intent fileIntent = new Intent();
                fileIntent.setAction(Intent.ACTION_VIEW);
                fileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                fileIntent.setDataAndType(fileUri, "video/*");
                try {
                    getContext().startActivity(fileIntent);
                    return true;
                } catch (ActivityNotFoundException e) {
                    Log.e("3DHome", "Thumbnails can not find the activity to play video.");
                }
            }
        }
        return false;
    }

    private String getPath(String videoId) {
        String path = null;
        if (TextUtils.isEmpty(videoId)) {
            return path;
        }
        try {
            String[] projection = new String[] { MediaStore.Video.Media.DATA };
            String where = MediaStore.Video.Media._ID + "=" + videoId;
            Cursor cursor = getContext().getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    projection, where, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                }
                cursor.close();
            }
        } catch (Exception e) {
            return null;
        }
        return path;
    }

    @Override
    public void onRelease() {
        super.onRelease();
        LauncherModel.getInstance().removeTask(mPlayCycle);
        if (mUpdateAnimation != null) {
            mUpdateAnimation.stop();
        }
        if (mPlayAnimation != null) {
            mPlayAnimation.stop();
        }
    }
}
