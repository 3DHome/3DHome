package com.borqs.se.widget3d;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.text.TextUtils;
import android.view.MotionEvent;

import com.borqs.se.engine.SEScene;
import com.borqs.se.engine.SEAnimFinishListener;
import com.borqs.se.engine.SECommand;
import com.borqs.se.engine.SELoadResThread;
import com.borqs.se.engine.SEObject;
import com.borqs.se.engine.SETransParas;
import com.borqs.se.engine.SEAnimation.CountAnimation;
import com.borqs.se.engine.SEUtils;
import com.borqs.se.engine.SEVector.SERotate;
import com.borqs.se.engine.SEVector.SEVector3f;
import com.borqs.se.home3d.HomeScene;
import com.borqs.se.home3d.HomeUtils;
import com.borqs.se.home3d.HomeManager;
import com.borqs.se.widget3d.ShakeDetector.OnShakeListener;

public class WallFrame extends WallObject {
    private final static int Max_IMAGE_SIZE = 512;
    protected SwingAnimation mSwingAnimation;
    protected SEObject mPicture;
    protected SEObject mFrame;
    protected SEObject mShadow;
    public SEObject mRope;
    public SEObject mHook;

    protected int mRequestCode;
    private int mImageWidth;
    private int mImageHeight;
    private int mFrameWidth;
    private int mFrameHeight;
    private int mResizeWidth;
    private int mResizeHeight;
    private String mImageName;
    protected String mSaveImagePath;

    private ShakeDetector mShakeDetector;
    private OnShakeListener mListener;
    private House mHouse;
    
    public WallFrame(HomeScene scene, String name, int index) {
        super(scene, name, index);
        mImageWidth = 0;
        mImageHeight = 0;
        mFrameWidth = 0;
        mFrameHeight = 0;
        mListener = new OnShakeListener() {

            @Override
            public void onShake(float shakeLevel) {
                if (mSwingAnimation != null && !mSwingAnimation.isFinish() && mSwingAnimation.mSwingLevel > shakeLevel) {
                    return;
                }

                if (mSwingAnimation != null) {
                    mSwingAnimation.stop();
                }
                mSwingAnimation = new SwingAnimation(getScene(), shakeLevel);
                mSwingAnimation.execute();
            }
        };
        mShakeDetector = new ShakeDetector(getContext(), mListener);
    }
    
    

    @Override
    public void setOnMove(boolean onFinger) {
        super.setOnMove(onFinger);
            if (mHook != null) {
                mHook.setVisible(!onFinger);
            }
            if (mRope != null) {
                mRope.setVisible(!onFinger);
            }
    }

    @Override
    public void initStatus() {
        super.initStatus();
        setCanChangeBind(false);
        setCanBeResized(true);
        mRequestCode = HomeManager.getInstance().getRequestCode();
        String oldFileName = getContext().getFilesDir() + File.separator + getScene().getSceneName() + mName + mIndex
                + ".png";
        File oldFile = new File(oldFileName);
        mSaveImagePath = getContext().getFilesDir() + File.separator + getScene().getSceneName() + mName + mIndex;
        if (oldFile.exists()) {
            oldFile.renameTo(new File(mSaveImagePath));
        }
        mPicture = findComponenetObjectByRegularName("picture");
        mFrame =findComponenetObjectByRegularName("frame");
        mShadow = findComponenetObjectByRegularName("shadow");
        mRope =  findComponenetObjectByRegularName("rope");
        mHook =  findComponenetObjectByRegularName("hook");
        String imageSizeX = getObjectInfo().mModelInfo.getSpecialAttribute(getContext(), "imageSizeX");
        if (!TextUtils.isEmpty(imageSizeX)) {
            mImageWidth = Integer.parseInt(imageSizeX);
        }
        String imageSizeY = getObjectInfo().mModelInfo.getSpecialAttribute(getContext(), "imageSizeY");
        if (!TextUtils.isEmpty(imageSizeY)) {
            mImageHeight = Integer.parseInt(imageSizeY);
        }
        mFrameWidth = (int) (2 * Math.max(getObjectInfo().mModelInfo.mMaxPoint.getX(),
                -getObjectInfo().mModelInfo.mMinPoint.getX()));
        mFrameHeight = (int) (2 * Math.max(getObjectInfo().mModelInfo.mMaxPoint.getZ(),
                -getObjectInfo().mModelInfo.mMinPoint.getZ()));
        mHook.setVisible(true);
        mRope.setVisible(true);
        mImageName = mPicture.getImageName();
        if (getParent().getParent() instanceof House) {
            mHouse = (House) getParent().getParent();
        } else {
            mHouse = getHomeScene().getHouse();
        }
        resizeFrame();
        initImage();
    }

    private void initImage() {
        File f = new File(mSaveImagePath);
        if (f.exists()) {
            SEObject.applyImage_JNI(mImageName, mSaveImagePath);
            SELoadResThread.getInstance().process(new Runnable() {
                public void run() {
                    int imageSize = mResizeWidth > mResizeHeight ? SEUtils.higherPower2(mResizeWidth) : SEUtils
                            .higherPower2(mResizeHeight);
                    if (imageSize > Max_IMAGE_SIZE) {
                        imageSize = Max_IMAGE_SIZE;
                    }
                    Bitmap bm = HomeUtils.decodeSampledBitmapFromResource(mSaveImagePath, imageSize, imageSize);
                    if (bm == null) {
                        return;
                    }
                    final int imageData = SEObject.loadImageData_JNI(bm);
                    bm.recycle();
                    if (imageData != 0)
                        new SECommand(getHomeScene()) {
                            public void run() {
                                SEObject.addImageData_JNI(mSaveImagePath, imageData);
                            }
                        }.execute();
                }
            });
        }
    }

    private void resizeImage() {
        initImage();
    }

    @Override
    public void handOnClick() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", mResizeWidth);
        intent.putExtra("aspectY", mResizeHeight);
        intent.putExtra("output", Uri.fromFile(new File(HomeUtils.getTmpImageFilePath())));
        intent.putExtra("outputFormat", "JPEG");
        HomeManager.getInstance().startActivityForResult(intent, mRequestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == mRequestCode) {
            SELoadResThread.getInstance().process(new Runnable() {
                public void run() {
                    Uri url = null;
                    if (data != null) {
                        url = data.getData();
                    }
                    final Uri imageFileUri = url;
                    Bitmap bm = null;
                    if (imageFileUri != null) {
                        bm = HomeUtils.decodeSampledBitmapFromResource(getContext(), imageFileUri, Max_IMAGE_SIZE, Max_IMAGE_SIZE);
                    } else {
                        bm = HomeUtils.decodeSampledBitmapFromResource(HomeUtils.getTmpImageFilePath(),
                            Max_IMAGE_SIZE, Max_IMAGE_SIZE);
                    }
                    if (bm == null) {
                        return;
                    }
                    Bitmap des = HomeUtils.resizeBitmap(bm, Max_IMAGE_SIZE, Max_IMAGE_SIZE);
                    HomeUtils.saveBitmap(des, mSaveImagePath, Bitmap.CompressFormat.JPEG);
                    des.recycle();
                    resizeImage();
                }
            });

        }
    }

    public void onWallRadiusChanged(int faceIndex) {
        if (isPressed()) {
            return;
        }
        if (faceIndex == getObjectInfo().getSlotIndex()) {
            if (mSwingAnimation != null) {
                mSwingAnimation.stop();
            }
            mSwingAnimation = new SwingAnimation(getScene(), 1);
            mSwingAnimation.execute();

            if (mShakeDetector != null) {
                mShakeDetector.stop();
                mShakeDetector.start();
            }
        } else {
            if (mShakeDetector != null) {
                mShakeDetector.stop();
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mSwingAnimation != null) {
                mSwingAnimation.stop();
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void onSizeAndPositionChanged(Rect sizeRect) {
        updateObjectSlotRect(sizeRect);
        resizeFrame();
        resizeImage();
        if (getParent() != null && (getParent() instanceof VesselObject)) {
            VesselObject vessel = (VesselObject) getParent();
            SETransParas transParas = vessel.getTransParasInVessel(this, getObjectSlot());
            if (transParas != null) {
                getUserTransParas().set(transParas);
                setUserTransParas();
            }
        }
    }

    private void resizeFrame() {
        float scale = getScale(getObjectSlot().mSpanX, getObjectSlot().mSpanY);
        SEObject model = getModelComponenet();
        model.setUserScale(new SEVector3f(scale, 1, scale));
        mResizeWidth = (int) (mImageWidth * scale);
        mResizeHeight = (int) (mImageHeight * scale);
    }

    public float getScale(int spanX, int spanY) {
        float newW = getWidthOfWidget(spanX);
        float newH = getHeightOfWidget(spanY);
        float scale = Math.min(newW / mFrameWidth, newH / mFrameHeight);
        return scale;
    }

    @Override
    public boolean handleBackKey(SEAnimFinishListener l) {
        if (getHomeScene().getDragLayer().isOnResize()) {
            getHomeScene().getDragLayer().finishResize();
            return true;
        }
        return false;
    }

    private class SwingAnimation extends CountAnimation {
        private boolean mDirection_Center;
        private float mSpan_Center;
        public float mSwingLevel;

        public SwingAnimation(SEScene scene, float swingLevel) {
            super(scene);
            mSwingLevel = swingLevel;
        }

        @Override
        public void runPatch(int count) {
            float angle = (float) ((mSpan_Center - count * mSpan_Center / 200) * Math.sin(Math.PI * count / 50));
            angle *= mSwingLevel;
            if (mDirection_Center) {
                angle = -angle;
            }
            mFrame.setUserRotate(new SERotate(angle, 0, 1, 0));
            mPicture.setUserRotate(new SERotate(angle, 0, 1, 0));
            mShadow.setUserRotate(new SERotate(angle, 0, 1, 0));
            mRope.setUserRotate(new SERotate(angle, 0, 1, 0));
        }

        @Override
        public void onFirstly(int count) {
            mDirection_Center = Math.random() > 0.5;
            mSpan_Center = (float) (Math.random() * 15 + 1);
        }

        @Override
        public void onFinish() {
            mFrame.setUserRotate(new SERotate(0, 0, 1, 0));
            mPicture.setUserRotate(new SERotate(0, 0, 1, 0));
            mShadow.setUserRotate(new SERotate(0, 0, 1, 0));
            mRope.setUserRotate(new SERotate(0, 0, 1, 0));
        }

        @Override
        public int getAnimationCount() {
            return 200;
        }
    }

    @Override
    public void onRelease() {
        super.onRelease();

        File f = new File(mSaveImagePath);
        if (f.exists()) {
            f.delete();
        }
    }

    private float getWidthOfWidget(float spanX) {
        return mHouse.getWallCellWidth(spanX);
    }

    private float getHeightOfWidget(float spanY) {
        return mHouse.getWallCellHeight(spanY);
    }

}
