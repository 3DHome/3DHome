package com.borqs.se.widget3d;

import java.io.File;

import com.borqs.se.engine.SEXMLAnimation;
import com.borqs.se.engine.SECamera.CameraChangedListener;
import com.borqs.se.home3d.HomeScene;
import com.borqs.se.home3d.HomeUtils;

public class EmailBox extends NormalObject implements CameraChangedListener {
    private SEXMLAnimation mOpenEmailBox;

    public EmailBox(HomeScene scene, String name, int index) {
        super(scene, name, index);
    }

    @Override
    public void initStatus() {
        super.initStatus();
        if (getObjectInfo().mModelInfo.mAssetsPath.startsWith("assets/")) {
            if (HomeUtils.isAssetFileExist(getContext(), getObjectInfo().mModelInfo.mAssetsPath.substring(7)
                    + File.separator + "animation.xml")) {
                mOpenEmailBox = new SEXMLAnimation(getScene(), getObjectInfo().mModelInfo.mAssetsPath + File.separator
                        + "animation.xml", mIndex);
            }
        } else {
            File file = new File(getObjectInfo().mModelInfo.mAssetsPath + File.separator + "animation.xml");
            if (file.exists()) {
                mOpenEmailBox = new SEXMLAnimation(getScene(), file.getPath(), mIndex);
            }
        }

        if (mOpenEmailBox != null) {
            getCamera().addCameraChangedListener(this);
        }
    }

    public void onCameraChanged() {
        if (getHomeScene().getSightValue() == -1) {
            if (mOpenEmailBox != null) {
                mOpenEmailBox.setIsReversed(false);
                mOpenEmailBox.execute();
            }
        } else if (getHomeScene().getSightValue() == 0) {
            if (mOpenEmailBox != null) {
                mOpenEmailBox.setIsReversed(true);
                mOpenEmailBox.execute();
            }
        }
    }

    @Override
    public void onRelease() {
        super.onRelease();
        if (mOpenEmailBox != null) {
            mOpenEmailBox.pause();
            mOpenEmailBox = null;
            getCamera().removeCameraChangedListener(this);
        }
    }

}
