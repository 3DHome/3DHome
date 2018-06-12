package com.borqs.se.ase;

import java.io.File;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.MotionEvent;

import com.borqs.se.engine.SECameraData;
import com.borqs.se.engine.SECommand;
import com.borqs.se.engine.SELoadResThread;
import com.borqs.se.engine.SEObject;
import com.borqs.se.engine.SERenderView;
import com.borqs.se.engine.SEScene;
import com.borqs.se.engine.SESceneManager;
import com.borqs.se.engine.SEVector.SEQuat;
import com.borqs.se.engine.SEVector.SERotate;
import com.borqs.se.engine.SEVector.SEVector3f;
import com.borqs.se.home3d.HomeUtils;
import com.borqs.se.home3d.ModelInfo;
import com.borqs.se.home3d.ProviderUtils.ModelColumns;

public class ASEScene extends SEScene {
    public static final int MSG_TYPE_SCALE = 1000;
    public static final int MSG_TYPE_MOVE_X = MSG_TYPE_SCALE + 1;
    public static final int MSG_TYPE_MOVE_Y = MSG_TYPE_MOVE_X + 1;
    public static final int MSG_TYPE_MOVE_Z = MSG_TYPE_MOVE_Y + 1;
    private SECameraData mSECameraData;
    private SEObject mModelObjectGroup;
    private float mPreTouchX;
    private float mPreTouchY;

    private SERotate mPreRotate;
    private SEQuat mPreQuat;
    private SEVector3f mPreTranslate;
    private SEVector3f mPreScale;
    private ModelInfo mModelInfo;

    public ASEScene(Context context, String sceneName) {
        super(context, sceneName);
        mSECameraData = new SECameraData();
        mSECameraData.mLocation = new SEVector3f(0, -850, 200);
        mSECameraData.mAxisZ = new SEVector3f(0, -1, 0);
        mSECameraData.mUp = new SEVector3f(0, 0, 1);
        mSECameraData.mFov = 32;
        mSECameraData.mNear = 1;
        mSECameraData.mFar = 3000;

    }

    @Override
    public void onSceneStart() {
        SEObject root = new SEObject(this, "Root");
        root.setIsGroup(true);
        setContentObject(root);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            mPreTouchX = touchX;
            mPreTouchY = touchY;
            break;
        case MotionEvent.ACTION_MOVE:
            float dX = touchX - mPreTouchX;
            float dZ = mPreTouchY - touchY;
            if (dZ == 0 && dX == 0) {
                break;
            }

            float rotateZ = dX * 180 / getSceneWidth();
            float rotateX = -dZ * 180 / getSceneWidth();

            if (mModelObjectGroup != null) {
                SEQuat quatA = new SERotate(rotateZ, 0, 0, 1).toQuat();
                SEQuat quatB = new SERotate(rotateX, 1, 0, 0).toQuat();
                if (mPreQuat == null) {
                    mPreQuat = quatA.mul(quatB);
                } else {
                    mPreQuat = quatA.mul(quatB).mul(mPreQuat);
                }
                mPreRotate = mPreQuat.toRotate();
                mModelObjectGroup.setLocalRotate(mPreRotate);
            }
            mPreTouchX = touchX;
            mPreTouchY = touchY;
            break;
        }

        return true;
    }

    @Override
    public void handleMessage(int type, Object message) {
        switch (type) {
        case MSG_TYPE_SCALE:
            float dS = (Float) message;
            mPreScale = mPreScale.mul(dS);
            mModelObjectGroup.setLocalScale(mPreScale);
            break;
        case MSG_TYPE_MOVE_X:
            float dX = (Float) message;
            mPreTranslate = mPreTranslate.add(new SEVector3f(dX, 0, 0));
            mModelObjectGroup.setLocalTranslate(mPreTranslate);
            break;
        case MSG_TYPE_MOVE_Y:
            float dY = (Float) message;
            mPreTranslate = mPreTranslate.add(new SEVector3f(0, dY, 0));
            mModelObjectGroup.setLocalTranslate(mPreTranslate);
            break;
        case MSG_TYPE_MOVE_Z:
            float dZ = (Float) message;
            mPreTranslate = mPreTranslate.add(new SEVector3f(0, 0, dZ));
            mModelObjectGroup.setLocalTranslate(mPreTranslate);
            break;
        }

    }

    public void updateXML(final Runnable finish) {
        SELoadResThread.getInstance().process(new Runnable() {
            public void run() {
                ASEUtils.changeModelInfoXML(mModelInfo.mAssetsPath + File.separator + "models_config.xml",
                        mPreTranslate, mPreRotate, mPreScale, true);
                ASEUtils.changeModelInfoXML(mModelInfo.mAssetsPath, mModelInfo.mAssetsPath + File.separator
                        + "ResourceManifest.xml");
                catchImage(finish);
            }
        });
    }

    public void reset() {
        new SECommand(this) {
            public void run() {
                mPreRotate = new SERotate(0, 0, 0, 1);
                mPreQuat = mPreRotate.toQuat();
                mPreTranslate = new SEVector3f(0, 0, 0);
                mPreScale = new SEVector3f(1, 1, 1);
                mModelObjectGroup.setLocalRotate(mPreRotate);
                mModelObjectGroup.setLocalScale(mPreScale);
                mModelObjectGroup.setLocalTranslate(mPreTranslate);
            }
        }.execute();
    }

    @Override
    public SECameraData getCameraData() {
        return mSECameraData;
    }

    public void add3DMaxModelToScene(String resource, final Runnable finish) {
        mModelInfo = HomeUtils.getModelInfo(resource);
        final SEObject modelObject = new SEObject(this, mModelInfo.mName);
        mModelInfo.load3DMAXModel(this, getContentObject(), modelObject, false, new Runnable() {
            public void run() {
                getContentObject().addChild(modelObject, false);
                modelObject.setNeedGenerateMirror(true);
                mModelObjectGroup = new SEObject(ASEScene.this, mModelInfo.mName + "_model");
                mModelObjectGroup.setHasBeenAddedToEngine(true);
                if (mModelInfo.mCompensationTrans != null) {
                    mModelObjectGroup.setLocalTranslate(mModelInfo.mCompensationTrans.mTranslate);
                    mPreTranslate = mModelInfo.mCompensationTrans.mTranslate;
                    mModelObjectGroup.setLocalRotate(mModelInfo.mCompensationTrans.mRotate);
                    mPreRotate = mModelInfo.mCompensationTrans.mRotate;
                    mPreQuat = mPreRotate.toQuat();
                    mModelObjectGroup.setLocalScale(mModelInfo.mCompensationTrans.mScale);
                    mPreScale = mModelInfo.mCompensationTrans.mScale;
                } else {
                    mPreTranslate = new SEVector3f();
                    mPreScale = new SEVector3f(1, 1, 1);
                    mPreRotate = new SERotate(0, 0, 0, 1);
                    mPreQuat = mPreRotate.toQuat();
                }
                addDesk();
                if (finish != null) {
                    finish.run();
                }

            }

        });
    }

    private void addDesk() {
        final ModelInfo modelInfo = findDeskModelInfo();
        final SEObject deskObject = new SEObject(this, modelInfo.mName);
        modelInfo.load3DMAXModel(this, getContentObject(), deskObject, false, new Runnable() {
            public void run() {
                getContentObject().addChild(deskObject, false);
                deskObject.setLocalTranslate(new SEVector3f(0, 0, -118));
                SEObject deskModel = new SEObject(ASEScene.this, modelInfo.mName + "_model");
                deskModel.setHasBeenAddedToEngine(true);
                if (modelInfo.mCompensationTrans != null) {
                    deskModel.setLocalTranslate(modelInfo.mCompensationTrans.mTranslate);
                    deskModel.setLocalRotate(modelInfo.mCompensationTrans.mRotate);
                    deskModel.setLocalScale(modelInfo.mCompensationTrans.mScale);
                }

                String mirrordensity = modelInfo.getSpecialAttribute(getContext(), "mirrordensity");
                if (!TextUtils.isEmpty(mirrordensity)) {
                    float density = Float.parseFloat(mirrordensity);
                    deskObject.setMirrorColorDesity_JNI(density);
                }

            }
        });
    }

    private ModelInfo findDeskModelInfo() {
        ModelInfo modelInfo = null;
        ContentResolver resolver = getContext().getContentResolver();
        String where = ModelColumns.OBJECT_NAME + "='group_desk_2'";
        Cursor modelCursor = resolver.query(ModelColumns.CONTENT_URI, null, where, null, null);
        if (modelCursor == null) {
            return null;
        }
        if (modelCursor.moveToFirst()) {
            modelInfo = ModelInfo.CreateFromDB(modelCursor);
        }
        modelCursor.close();
        return modelInfo;
    }

    private void catchImage(final Runnable finish) {
        new SECommand(this) {
            public void run() {
                if (mModelInfo != null) {
                    String fileName = mModelInfo.mAssetsPath + File.separator + "cover.png";
                    File file = new File(fileName);
                    String fileName2 = mModelInfo.mAssetsPath + File.separator + "cover.jpg";
                    File file2 = new File(fileName2);
                    if (!file.exists() && !file2.exists()) {
                        SERenderView renderView = (SERenderView) SESceneManager.getInstance().getGLSurfaceView();
                        Bitmap bitmap = renderView.getBitmap(getSceneWidth(), getSceneHeight());
                        HomeUtils.saveBitmap(bitmap, fileName, Bitmap.CompressFormat.PNG);
                    }
                }
                if (finish != null) {
                    finish.run();
                }

            }
        }.execute();
    }

}
