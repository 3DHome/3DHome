package com.borqs.se.widget3d;

import android.view.MotionEvent;
import android.view.VelocityTracker;

import com.borqs.se.engine.SECommand;
import com.borqs.se.engine.SETransParas;
import com.borqs.se.home3d.HomeManager;
import com.borqs.se.home3d.HomeScene;
import com.borqs.se.home3d.ScaleGestureDetector;
import com.borqs.se.widget3d.ObjectInfo.ObjectSlot;

public class NodeRoot extends VesselObject implements ScaleGestureDetector.OnScaleGestureListener {
    private int mCheckSceneStatus;
    private VelocityTracker mVelocityTracker;
    private HomeScene mHomeScene;

    public NodeRoot(HomeScene scene, String name, int index) {
        super(scene, name, index);
        mHomeScene = scene;
        setPressType(PRESS_TYPE.NONE);
        mCheckSceneStatus = HomeScene.STATUS_APP_MENU + HomeScene.STATUS_HELPER_MENU + HomeScene.STATUS_MOVE_OBJECT
                + HomeScene.STATUS_ON_DESK_SIGHT + HomeScene.STATUS_ON_SKY_SIGHT + HomeScene.STATUS_ON_WIDGET_SIGHT;

    }

    @Override
    public void initStatus() {
        super.initStatus();
        setOnLongClickListener(null);
        setOnClickListener(null);
        setVesselLayer(new DragDeskLayer(getHomeScene(), this));
        HomeManager.getInstance().setScaleListener(this);
    }

    @Override
    public void onActivityResume() {
        super.onActivityResume();
        HomeManager.getInstance().setScaleListener(this);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mHomeScene.getStatus(HomeScene.STATUS_ON_SCALL);
    }

    private void trackVelocity(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        final VelocityTracker velocityTracker = mVelocityTracker;
        velocityTracker.computeCurrentVelocity(1000);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        trackVelocity(event);
        return super.onTouchEvent(event);
    }

    /** 下面四个值代表的意思是相机距离房间中心点的最小/大值以及相机在最小位置的张角和最大位置的张角 **/
    private float mMinCameraRadius;
    private float mMaxCameraRadius;

    public boolean onScaleBegin(ScaleGestureDetector detector) {
        if (!canScall()) {
            return false;
        }
        mHomeScene.setStatus(HomeScene.STATUS_ON_SCALL, true);
        mMinCameraRadius = getHomeSceneInfo().getCameraMinRadius();
        mMaxCameraRadius = getHomeSceneInfo().getCameraMaxRadius();
        mHomeScene.stopAllAnimation();
        return true;
    }

    public void onScaleEnd(ScaleGestureDetector detector) {
        mHomeScene.setStatus(HomeScene.STATUS_ON_SCALL, false);
        float curCameraRadius = getCamera().getRadius();
        float endCameraRadius = curCameraRadius;
        if (curCameraRadius < mMinCameraRadius) {
            endCameraRadius = mMinCameraRadius;
        } else if (curCameraRadius > mMaxCameraRadius) {
            endCameraRadius = mMaxCameraRadius;
        }
        if (canScall()) {
            final float endFov = getHomeSceneInfo().getCameraFovByRadius(endCameraRadius);
            mHomeScene.playSetRadiusAndFovAnim(endCameraRadius, endFov, null);
        }

    }

    public boolean onScale(ScaleGestureDetector detector) {
        float scale = detector.getScaleFactor();
        float currentCameraRadius = getCamera().getRadius();
        float targetCameraRadius = currentCameraRadius + (1 - scale) * 500;

        if (targetCameraRadius < mMinCameraRadius - 50f) {
            targetCameraRadius = mMinCameraRadius - 50f;
        } else if (targetCameraRadius > mMaxCameraRadius + 50f) {
            targetCameraRadius = mMaxCameraRadius + 50f;
        }
        if (!canScall()) {
            return false;
        }
        final float r = targetCameraRadius;
        final float fov = getHomeSceneInfo().getCameraFovByRadius(r);
        new SECommand(getScene()) {
            public void run() {
                if (canScall()) {
                    getCamera().setRadiusAndFov(r, fov);
                }
            }
        }.execute();
        return true;
    }

    private boolean canScall() {
        return (mHomeScene.getStatus() & mCheckSceneStatus) <= 0;
    }

    @Override
    public SETransParas getTransParasInVessel(NormalObject needPlaceObj, ObjectSlot objectSlot) {
        return null;
    }
}
