package com.borqs.se.widget3d;

import com.borqs.se.engine.SECommand;
import com.borqs.se.engine.SELoadResThread;
import com.borqs.se.engine.SEParticleSystem;
import com.borqs.se.engine.SEScene.SCENE_CHANGED_TYPE;
import com.borqs.se.engine.SEVector.SEVector3f;
import com.borqs.se.home3d.HomeScene;

public class ChristmasTree extends NormalObject {
    private SEParticleSystem mFlashStars;

    public ChristmasTree(HomeScene homeScene, String name, int index) {
        super(homeScene, name, index);
    }

    @Override
    public void initStatus() {
        super.initStatus();

        SEVector3f boxSize = getObjectInfo().mMaxPoint.subtract(getObjectInfo().mMinPoint);
        mFlashStars = new SEParticleSystem(mName + "_ChristmasTree_star_" + mIndex);
        mFlashStars.setParticleSystemAttribute(new float[] { 25, 25 }, 30);
        mFlashStars.setBoxEmitterSize(boxSize.getX() * 0.75f, boxSize.getY() * 0.75f, boxSize.getZ() / 2);
        mFlashStars.setEmitterPosition(0, 0, boxSize.getZ() / 2);
        mFlashStars.setEmitterDirection(new SEVector3f(0, 0, 1));
        mFlashStars.setEmitterParticleVelocity(0, 0);
        mFlashStars.setEmitterTimeToLive(2, 5);
        mFlashStars.setEmitterAngle(5);
        mFlashStars.setEmitterEmissionRate(6);
        mFlashStars.setColourFaderAffectorEnable(true);
        mFlashStars.setColorFaderAdjust(-0.0f, -0.0f, -0.0f, -0.5f);
        mFlashStars.setNeedDepthTest(true);
        mFlashStars.setImagePath(mName + "_" + mIndex + "_star_image_name");
        mFlashStars.attachObject(this);
        mFlashStars.addParticle_JNI();
        SELoadResThread.getInstance().process(new Runnable() {
            @Override
            public void run() {
                int imageIndex = mIndex % 2;
                final String imagePath = getObjectInfo().mModelInfo.mAssetsPath + "/star_" + imageIndex + ".png";
                final int imageData = loadImageData_JNI(imagePath);
                new SECommand(getScene()) {
                    public void run() {
                        applyImage_JNI(mName + "_" + mIndex + "_star_image_name", imagePath);
                        addImageData_JNI(imagePath, imageData);
                    }
                }.execute();
            }

        });

    }

    @Override
    public void onSceneChanged(SCENE_CHANGED_TYPE changeType) {
        if (changeType == SCENE_CHANGED_TYPE.NEW_SCENE) {
            if (mFlashStars != null) {
                mFlashStars.deleteParticleObject_JNI();
                mFlashStars = null;
            }
        }
    }

    @Override
    public void onRelease() {
        super.onRelease();
        if (mFlashStars != null) {
            mFlashStars.deleteParticleObject_JNI();
            mFlashStars = null;
        }
    }

}
