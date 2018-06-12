package com.borqs.se.widget3d;

import java.util.List;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.animation.DecelerateInterpolator;
import com.borqs.se.engine.SEAnimFinishListener;
import com.borqs.se.engine.SECamera;
import com.borqs.se.engine.SEEmptyAnimation;
import com.borqs.se.engine.SEObject;
import com.borqs.se.engine.SETranslateAnimation;
import com.borqs.se.engine.SEVector.SEVector2f;
import com.borqs.se.engine.SEVector.SEVector2i;
import com.borqs.se.engine.SEVector.SEVector3f;
import com.borqs.se.widget3d.VesselLayer.ACTION;
import com.borqs.se.home3d.HomeScene;
import com.borqs.se.home3d.HomeManager;

public class DragLayer {
    private SEObject mMotionTarget;
    private HomeScene mHomeScene;
    private SECamera mCamera;
    private NormalObject mOnDragObject;
    private SEVector2i mCompensateTouch;
    private boolean mIsOnDrag;
    private boolean mIsOnObjectResize;
    private VesselLayer mCurVesselLayer;
    private VelocityTracker mVelocityTracker;
    private List<VesselLayer> mChildLayers;
    private boolean mCanChangeLayer;
    private ResizeLayerInterface mResizeLayer;
    private SEEmptyAnimation mFlyAnimation;
    private boolean mBlock;

    public DragLayer(HomeScene scene) {
        mHomeScene = scene;
        mIsOnDrag = false;
        mIsOnObjectResize = false;
        mBlock = false;
        mCamera = scene.getCamera();
    }

    public void block(boolean block) {
        mBlock = block;
    }

    public void startDrag(NormalObject onDragObject, float touchX, float touchY) {
        if (onDragObject != null) {
            mCompensateTouch = onDragObject.getAdjustTouch();
            if (mCompensateTouch == null) {
                return;
            }
            mOnDragObject = onDragObject;
            mChildLayers = mHomeScene.getDragDropLayerList();
            boolean isDesktopObj = false;
            if (mOnDragObject.getObjectInfo().mIsNativeObject
                    && mOnDragObject.getObjectInfo().mModelInfo.mSlotType == ObjectInfo.SLOT_TYPE_DESKTOP) {
                isDesktopObj = true;
            }
            mCanChangeLayer = mOnDragObject instanceof AppObject || isDesktopObj;
            for (VesselLayer vesselLayer : mChildLayers) {
                if (vesselLayer.canHandleSlot(mOnDragObject, mCompensateTouch.getX() + touchX, mCompensateTouch.getY()
                        + touchY)) {
                    mCurVesselLayer = vesselLayer;
                    loadAndShowRecycle(mCurVesselLayer);
                    mCurVesselLayer.setOnLayerModel(mOnDragObject, true);
                    onDragObjectEvent_II(ACTION.BEGIN, mCompensateTouch.getX() + touchX, mCompensateTouch.getY()
                            + touchY);
                    break;
                }
            }
            mIsOnDrag = true;
        }
    }

    public boolean isOnDrag() {
        return mIsOnDrag;
    }

    public boolean isOnResize() {
        return mIsOnObjectResize;
    }

    public void finishDrag() {
        if (!mIsOnDrag) {
            return;
        }
        mOnFly = false;
        if (mCurVesselLayer != null) {
            mCurVesselLayer.setOnLayerModel(mOnDragObject, false);
            mCurVesselLayer = null;
        }
        if (mRecycle != null) {
            mRecycle.getParent().removeChild(mRecycle, true);
            HomeManager.getInstance().getModelManager().unRegister(mRecycle);
            mRecycle = null;
        }
        mOnDragObject.setPressed(false);
        if (mFlyAnimation != null) {
            mFlyAnimation.stop();
        }
        mOnDragObject = null;
        mIsOnDrag = false;
    }

    public void changeOnDragObject(NormalObject onDragObject) {
        mOnDragObject = onDragObject;
    }

    public void finishResize() {
        if (!mIsOnObjectResize) {
            return;
        }
        mOnDragObject = null;
        mIsOnObjectResize = false;
        if (mResizeLayer != null) {
            mResizeLayer.endResize();
            mResizeLayer = null;
        }
    }

    public void startResize(NormalObject onDragObject) {
        if (onDragObject != null) {
            mOnDragObject = onDragObject;
            mIsOnObjectResize = true;
            mResizeLayer = new WidgetResizeLayer(mHomeScene, onDragObject);
            mResizeLayer.startResize();

        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mIsOnDrag || mIsOnObjectResize;
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        if (mBlock) {
            return true;
        }
        final int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            if (mMotionTarget != null) {
                mMotionTarget = null;
            }
            if (!onInterceptTouchEvent(event)) {
                SEObject root = mHomeScene.getContentObject();
                if (root != null && root.dispatchTouchEvent(event)) {
                    mMotionTarget = root;
                    return true;
                }
            }
        }

        final SEObject target = mMotionTarget;
        if (target == null) {
            return onTouchEvent(event);
        }
        boolean isUpOrCancel = (action == MotionEvent.ACTION_UP) || (action == MotionEvent.ACTION_CANCEL);
        if (!isUpOrCancel && onInterceptTouchEvent(event)) {
            event.setAction(MotionEvent.ACTION_CANCEL);
            if (!target.dispatchTouchEvent(event)) {
                // target didn't handle ACTION_CANCEL. not much we can do
                // but they should have.
            }
            mMotionTarget = null;
            return true;
        }
        if (isUpOrCancel) {
            if (mIsOnDrag) {
                if (!mOnFly) {
                    mOnFly = true;
                    onDragObjectEvent_II(ACTION.FINISH, event.getX() + mCompensateTouch.getX(), event.getY()
                            + mCompensateTouch.getY());
                }
                event.setAction(MotionEvent.ACTION_CANCEL);
                if (!target.dispatchTouchEvent(event)) {
                    // target didn't handle ACTION_CANCEL. not much we can do
                    // but they should have.
                }
                return true;
            }
            mMotionTarget = null;
        }

        return target.dispatchTouchEvent(event);

    }

    public boolean onTouchEvent(MotionEvent event) {
        trackVelocity(event);
        if (mIsOnDrag && !mOnFly) {
            onDragObjectEvent(event);
        } else if (mIsOnObjectResize) {
            if (mResizeLayer != null) {
                mResizeLayer.handleResizeTouchEvent(event);
            }
        }
        return true;
    }

    private void trackVelocity(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        final VelocityTracker velocityTracker = mVelocityTracker;
        velocityTracker.computeCurrentVelocity(1000);
    }

    private float mTouchX;
    private float mTouchY;
    private boolean mOnFly;

    public boolean onDragObjectEvent(MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            mTouchX = event.getX();
            mTouchY = event.getY();
            if (mFlyAnimation != null) {
                mFlyAnimation.stop();
            }
            break;
        case MotionEvent.ACTION_MOVE:
            mTouchX = event.getX();
            mTouchY = event.getY();
            onDragObjectEvent_II(ACTION.MOVE, mTouchX + mCompensateTouch.getX(), mTouchY + mCompensateTouch.getY());
            break;
        case MotionEvent.ACTION_UP:
            mTouchX = event.getX();
            mTouchY = event.getY();
        case MotionEvent.ACTION_CANCEL:
            mOnFly = true;
            onDragObjectEvent_II(ACTION.UP, mTouchX + mCompensateTouch.getX(), mTouchY + mCompensateTouch.getY());
            float velocityX = mVelocityTracker.getXVelocity();
            float velocityY = mVelocityTracker.getYVelocity();
            if (Math.abs(velocityX) <= 100) {
                velocityX = 0;
            } else {
                velocityX = velocityX * 0.05f;
            }
            if (Math.abs(velocityY) <= 100) {
                velocityY = 0;
            } else {
                velocityY = velocityY * 0.05f;
            }

            final SEVector2f startPoint = new SEVector2f(mTouchX, mTouchY);
            final SEVector2f direct = new SEVector2f(velocityX, velocityY);
            int animTimes = (int) direct.getLength() / 20;
            if (animTimes < 1) {
                animTimes = 1;
            }
            if (mFlyAnimation != null) {
                mFlyAnimation.stop();
            }
            mFlyAnimation = new SEEmptyAnimation(mHomeScene, 0, 1, animTimes) {
                @Override
                public void onAnimationRun(float value) {
                    SEVector2f curPoint = startPoint.add(direct.mul(value));
                    float newX = curPoint.getX() + mCompensateTouch.getX();
                    float newY = curPoint.getY() + mCompensateTouch.getY();
                    if (newX < 0) {
                        newX = 0;
                        stop();
                        return;
                    } else if (newX > mCamera.getWidth()) {
                        newX = mCamera.getWidth();
                        stop();
                        return;
                    }

                    if (newY < 0) {
                        newY = 0;
                        stop();
                        return;
                    } else if (newY > mCamera.getHeight()) {
                        newY = mCamera.getHeight();
                        stop();
                        return;
                    }
                    onDragObjectEvent_II(ACTION.FLY, newX, newY);
                }
            };
            mFlyAnimation.setAnimFinishListener(new SEAnimFinishListener() {
                public void onAnimationfinish() {
                    SEVector2f curPoint = startPoint.add(direct);
                    float newX = curPoint.getX() + mCompensateTouch.getX();
                    float newY = curPoint.getY() + mCompensateTouch.getY();
                    if (newX < 0) {
                        newX = 0;
                    } else if (newX > mCamera.getWidth()) {
                        newX = mCamera.getWidth();
                    }
                    if (newY < 0) {
                        newY = 0;
                    } else if (newY > mCamera.getHeight()) {
                        newY = mCamera.getHeight();
                    }
                    onDragObjectEvent_II(ACTION.FINISH, newX, newY);
                }
            });
            mFlyAnimation.setInterpolator(new DecelerateInterpolator());
            mFlyAnimation.execute();
            break;
        }
        return true;
    }

    private float mBeginX;
    private float mBeginY;

    public boolean onDragObjectEvent_II(ACTION event, float newX, float newY) {
        // judge whether user want to edit object or move move object
        if (event == ACTION.BEGIN) {
            mBeginX = newX;
            mBeginY = newY;
        } else {
            if (!mOnDragObject.mSlotChanged) {
                mOnDragObject.mSlotChanged = Math.sqrt((newX - mBeginX) * (newX - mBeginX) + (newY - mBeginY)
                        * (newY - mBeginY)) > 30 * mHomeScene.getScreenDensity();
            }
        }
        if (mCanChangeLayer) {
            float lineGround = mCamera.worldToScreenCoordinate(new SEVector3f(0, mHomeScene.getHouse().mWallRadius, 0))
                    .getY();
            int preSlotType = mOnDragObject.getObjectInfo().mSlotType;
            if (newY > lineGround) {
                if (preSlotType != ObjectInfo.SLOT_TYPE_DESKTOP) {
                    mOnDragObject.changeSlotType(ObjectInfo.SLOT_TYPE_DESKTOP);
                }
            } else {
                if (preSlotType == ObjectInfo.SLOT_TYPE_DESKTOP) {
                    mOnDragObject.changeSlotType(ObjectInfo.SLOT_TYPE_WALL);
                }
            }
            for (VesselLayer vesselLayer : mChildLayers) {
                if (vesselLayer.canHandleSlot(mOnDragObject, newX, newY)) {
                    if (vesselLayer != mCurVesselLayer) {
                        if (mCurVesselLayer != null) {
                            mCurVesselLayer.setOnLayerModel(mOnDragObject, false);
                        }
                        mCurVesselLayer = vesselLayer;
                        loadAndShowRecycle(mCurVesselLayer);
                        mCurVesselLayer.setOnLayerModel(mOnDragObject, true);
                    }
                }
            }
        }
        if (mCurVesselLayer == null) {
            return true;
        }
        mOnDragObject.setPressed(true);
        mCurVesselLayer.onObjectMoveEvent(event, newX, newY);
        updateRecycleColor();
        return true;
    }

    private void updateRecycleColor() {
        if (mCurVesselLayer != null) {
            if (mCurVesselLayer.isInRecycle()) {
                if (mRecycle != null && !mHasSetRecycleColor) {
                    mRecycle.setUseUserColor(1, 0, 0);
                    mHasSetRecycleColor = true;
                }
            } else {
                if (mRecycle != null && mHasSetRecycleColor) {
                    mRecycle.clearUserColor();
                    mHasSetRecycleColor = false;
                }
            }
        }
    }

    private SETranslateAnimation mMoveRecycleAnimation;
    private SEObject mRecycle;
    private boolean mHasSetRecycleColor;

    private void loadAndShowRecycle(VesselLayer currentLayer) {
        SEVector3f srcTranslate;
        SEVector3f desTranslate;
        float scale = 0.66f;
        Rect boundOfRecycle = new Rect();
        float y = mHomeScene.getHouse().mWallRadius * scale;
        srcTranslate = new SEVector3f(0, y, 500);
        desTranslate = new SEVector3f(0, y, 0);
        SEVector2i locationBottom = mHomeScene.getCamera().worldToScreenCoordinate(desTranslate);
        boundOfRecycle.left = (int) (mCamera.getWidth() / 2 - 80 * mHomeScene.getScreenDensity());
        boundOfRecycle.right = (int) (mCamera.getWidth() / 2 + 80 * mHomeScene.getScreenDensity());
        boundOfRecycle.bottom = locationBottom.getY();
        boundOfRecycle.top = (int) (boundOfRecycle.bottom - 50 * mHomeScene.getScreenDensity());
        currentLayer.setBoundOfRecycle(boundOfRecycle);
        loadAndShowRecycle(srcTranslate, desTranslate, scale);
    }

    private void loadAndShowRecycle(SEVector3f srcTranslate, SEVector3f desTranslate, float scale) {
        if (mMoveRecycleAnimation != null) {
            mMoveRecycleAnimation.stop();
        }
        if (mRecycle != null) {
            SEVector3f mySrcTranslate = mRecycle.getUserTranslate();
            if (desTranslate.subtract(mySrcTranslate).getLength() > 1) {
                mMoveRecycleAnimation = new SETranslateAnimation(mHomeScene, mRecycle, mySrcTranslate, desTranslate, 5);
                mMoveRecycleAnimation.execute();
            }
            return;
        }
        int index = (int) System.currentTimeMillis();
        mRecycle = new SEObject(mHomeScene, "recycle", index);
        boolean result = HomeManager.getInstance().getModelManager()
                .createQuickly(mHomeScene.getContentObject(), mRecycle);
        if (result) {
            mRecycle.setUserTranslate(srcTranslate);
            mRecycle.setUserScale(new SEVector3f(scale, scale, scale));
            mMoveRecycleAnimation = new SETranslateAnimation(mHomeScene, mRecycle, srcTranslate, desTranslate, 10);
            mMoveRecycleAnimation.setInterpolator(new DecelerateInterpolator());
            mMoveRecycleAnimation.execute();
        } else {
            mRecycle = null;
        }
    }

}
