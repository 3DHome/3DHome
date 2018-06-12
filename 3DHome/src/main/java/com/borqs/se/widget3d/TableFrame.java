package com.borqs.se.widget3d;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import com.borqs.se.engine.SECommand;
import com.borqs.se.engine.SELoadResThread;
import com.borqs.se.engine.SEObject;
import com.borqs.se.home3d.HomeScene;
import com.borqs.se.home3d.HomeUtils;
import com.borqs.se.home3d.HomeManager;

public class TableFrame extends NormalObject {
    private SEObject mPhotoObject;
    private int mRequestCode;

    private String mImageName;
    protected String mSaveImagePath;
    protected int mWidth;
    protected int mHeight;

    protected int mImageSize;

    public TableFrame(HomeScene scene, String name, int index) {
        super(scene, name, index);
        mWidth = 43;
        mHeight = 64;
        mImageSize = 128;
    }

    @Override
    public void initStatus() {
        super.initStatus();
        setCanChangeBind(false);
        mRequestCode = HomeManager.getInstance().getRequestCode();
        String oldFileName = getContext().getFilesDir() + File.separator + getScene().getSceneName() + mName + mIndex
                + ".png";
        File oldFile = new File(oldFileName);
        mSaveImagePath = getContext().getFilesDir() + File.separator + getScene().getSceneName() + mName + mIndex;
        if (oldFile.exists()) {
            oldFile.renameTo(new File(mSaveImagePath));
        }
        mPhotoObject = findComponenetObjectByRegularName("picture");
        mImageName = mPhotoObject.getImageName();
        initImage();
    }

    private void initImage() {
        File f = new File(mSaveImagePath);
        if (f.exists()) {
            SEObject.applyImage_JNI(mImageName, mSaveImagePath);
            SELoadResThread.getInstance().process(new Runnable() {
                public void run() {
                    final int imageData = SEObject.loadImageData_JNI(mSaveImagePath);
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

    @Override
    public void onRelease() {
        super.onRelease();
        File f = new File(mSaveImagePath);
        if (f.exists()) {
            f.delete();
        }
    }

    @Override
    public void handOnClick() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", mWidth);
        intent.putExtra("aspectY", mHeight);
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
                        bm = HomeUtils.decodeSampledBitmapFromResource(getContext(), imageFileUri, mImageSize,
                                mImageSize);
                    } else {
                        bm = HomeUtils.decodeSampledBitmapFromResource(HomeUtils.getTmpImageFilePath(), mImageSize,
                                mImageSize);
                    }
                    if (bm == null) {
                        return;
                    }
                    Bitmap des = HomeUtils.resizeBitmap(bm, mImageSize, mImageSize);
                    HomeUtils.saveBitmap(des, mSaveImagePath, Bitmap.CompressFormat.JPEG);
                    des.recycle();
                    final int imageData = SEObject.loadImageData_JNI(mSaveImagePath);
                    new SECommand(getHomeScene()) {
                        public void run() {
                            SEObject.applyImage_JNI(mImageName, mSaveImagePath);
                            SEObject.addImageData_JNI(mSaveImagePath, imageData);
                        }
                    }.execute();
                }
            });

        }
    }

}
