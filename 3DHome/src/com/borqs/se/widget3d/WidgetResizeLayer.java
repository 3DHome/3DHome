package com.borqs.se.widget3d;

import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.borqs.se.R;
import com.borqs.se.engine.SECamera;
import com.borqs.se.engine.SEEmptyAnimation;
import com.borqs.se.engine.SEObject;
import com.borqs.se.engine.SEScaleAnimation;
import com.borqs.se.engine.SESceneManager;
import com.borqs.se.engine.SETranslateAnimation;
import com.borqs.se.engine.SEVector.SEVector3f;
import com.borqs.se.home3d.HomeScene;
import com.borqs.se.home3d.WidgetResizeFrame;
import com.borqs.se.widget3d.ObjectInfo.ObjectSlot;

public class WidgetResizeLayer implements ResizeLayerInterface {
    private NormalObject mWidgetObject;
    private HomeScene mScene;
    private SECamera mCamera;
    private Wall mWall;
    private House mHouse;
    private RectF mResizeRect;
    private Rect mFinallyResizeRect;
    private WidgetResizeFrame mResizeRectObject;
    private boolean mOnResize = false;
    private SEScaleAnimation mShowResizeRectAnim;

    private enum START_POSITION {
        LEFT_TOP, RIGHT_TOP, LEFT_BOTTOM, RIGHT_BOTTOM
    }

    private START_POSITION mStartPostion = START_POSITION.RIGHT_BOTTOM;
    private float mTouchParaX;
    private float mTouchParaY;
    private int mPreTouchX;
    private int mPreTouchY;
    private int mMinX = 1;
    private int mMinY = 1;

    public WidgetResizeLayer(HomeScene scene, NormalObject widgetObject) {
        mScene = scene;
        mCamera = scene.getCamera();
        mWidgetObject = widgetObject;
        mWall = (Wall) widgetObject.getParent();
        mHouse = (House) mWall.getParent();
    }

    public void startResize() {
        if (mShowResizeRectAnim != null) {
            mShowResizeRectAnim.stop();
        }
        mScene.setStatus(HomeScene.STATUS_ON_WIDGET_RESIZE, true);
        mFinallyResizeRect = new Rect();
        ObjectSlot objectSlot = mWidgetObject.getObjectSlot();
        mFinallyResizeRect.set(objectSlot.left(), objectSlot.top(), objectSlot.right(), objectSlot.bottom());
        mResizeRect = new RectF(mFinallyResizeRect);
        mResizeRectObject = new WidgetResizeFrame(mScene, mWidgetObject.mName + "_rect");
        float w = getWidthOfWidget(mResizeRect.width());
        float h = getHeightOfWidget(mResizeRect.height());
        mResizeRectObject.setSize(128, 128);
        mResizeRectObject.reSize(w, h);
        mResizeRectObject.setBackground(R.drawable.resize_bg);
        mWall.addChild(mResizeRectObject, true);
        mResizeRectObject.setLocalTranslate(getResizeRectPostion(mResizeRect));
        SEVector3f fromScale = new SEVector3f(0, 0, 0);
        SEVector3f toScale = new SEVector3f(1, 1, 1);
        mShowResizeRectAnim = new SEScaleAnimation(mScene, mResizeRectObject, fromScale, toScale, 15);
        mShowResizeRectAnim.execute();
        mOnResize = true;
    }

    public void endResize() {
        if (mOnResize) {
            mOnResize = false;
            if (mShowResizeRectAnim != null) {
                mShowResizeRectAnim.stop();
            }
            SEVector3f fromScale = mResizeRectObject.getUserScale().clone();
            SEVector3f toScale = new SEVector3f(0, 0, 0);
            mShowResizeRectAnim = new SEScaleAnimation(mScene, mResizeRectObject, fromScale, toScale, 15) {
                @Override
                public void onFinish() {
                    mWall.removeChild(mResizeRectObject, true);
                    mResizeRectObject = null;
                }
            };
            mShowResizeRectAnim.execute();
            mScene.setStatus(HomeScene.STATUS_ON_WIDGET_RESIZE, false);
        }
    }

    public boolean handleResizeTouchEvent(MotionEvent ev) {
        int touchX = (int) ev.getX();
        int touchY = (int) ev.getY();
        switch (ev.getAction()) {
        case MotionEvent.ACTION_DOWN:
            mPreTouchX = touchX;
            mPreTouchY = touchY;
            RectF screenRect = toScreenRect(mResizeRect);
            mTouchParaX = mResizeRect.width() / screenRect.width();
            mTouchParaY = mResizeRect.height() / screenRect.height();
            if (!touchInResizeRect(screenRect, touchX, touchY)) {
                mScene.getDragLayer().finishResize();
            } else {
                if (touchX > screenRect.centerX()) {
                    if (touchY > screenRect.centerY()) {
                        mStartPostion = START_POSITION.RIGHT_BOTTOM;
                    } else {
                        mStartPostion = START_POSITION.RIGHT_TOP;
                    }
                } else {
                    if (touchY > screenRect.centerY()) {
                        mStartPostion = START_POSITION.LEFT_BOTTOM;
                    } else {
                        mStartPostion = START_POSITION.LEFT_TOP;
                    }
                }
            }
            break;
        case MotionEvent.ACTION_MOVE:
            float newLeft = mResizeRect.left;
            float newRight = mResizeRect.right;
            float newTop = mResizeRect.top;
            float newBottom = mResizeRect.bottom;
            switch (mStartPostion) {
            case LEFT_TOP:
                newLeft = newLeft + (touchX - mPreTouchX) * mTouchParaX;
                newTop = newTop + (touchY - mPreTouchY) * mTouchParaY;
                if (newRight - newLeft < mMinX - 0.2f) {
                    newLeft = newRight - mMinX + 0.2f;
                } else if (newLeft < 0) {
                    newLeft = 0;
                }
                if (newBottom - newTop < mMinY - 0.2f) {
                    newTop = newBottom - mMinY + 0.2f;
                } else if (newTop < 0) {
                    newTop = 0;
                }
                break;
            case RIGHT_TOP:
                newRight = newRight + (touchX - mPreTouchX) * mTouchParaX;
                newTop = newTop + (touchY - mPreTouchY) * mTouchParaY;
                if (newRight - newLeft < mMinX - 0.2f) {
                    newRight = newLeft + mMinX - 0.2f;
                } else if (newRight > mHouse.mCellCountX) {
                    newRight = mHouse.mCellCountX;
                }
                if (newBottom - newTop < mMinY - 0.2f) {
                    newTop = newBottom - mMinY + 0.2f;
                } else if (newTop < 0) {
                    newTop = 0;
                }
                break;
            case LEFT_BOTTOM:
                newLeft = newLeft + (touchX - mPreTouchX) * mTouchParaX;
                newBottom = newBottom + (touchY - mPreTouchY) * mTouchParaY;
                if (newRight - newLeft < mMinX - 0.2f) {
                    newLeft = newRight - mMinX + 0.2f;
                } else if (newLeft < 0) {
                    newLeft = 0;
                }
                if (newBottom - newTop < mMinY - 0.2f) {
                    newBottom = newTop + mMinY - 0.2f;
                } else if (newBottom > mHouse.mCellCountY) {
                    newBottom = mHouse.mCellCountY;
                }
                break;
            case RIGHT_BOTTOM:
                newRight = newRight + (touchX - mPreTouchX) * mTouchParaX;
                newBottom = newBottom + (touchY - mPreTouchY) * mTouchParaY;
                if (newRight - newLeft < mMinX - 0.2f) {
                    newRight = newLeft + mMinX - 0.2f;
                } else if (newRight > mHouse.mCellCountX) {
                    newRight = mHouse.mCellCountX;
                }
                if (newBottom - newTop < mMinY - 0.2f) {
                    newBottom = newTop + mMinY - 0.2f;
                } else if (newBottom > mHouse.mCellCountY) {
                    newBottom = mHouse.mCellCountY;
                }
                break;
            }
            mResizeRect.set(newLeft, newTop, newRight, newBottom);
            mResizeRectObject.setLocalTranslate(getResizeRectPostion(mResizeRect));
            float w = getWidthOfWidget(mResizeRect.width());
            float h = getHeightOfWidget(mResizeRect.height());
            mResizeRectObject.reSize(w, h);
            mPreTouchX = touchX;
            mPreTouchY = touchY;
            break;
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            Rect newResizeRect = new Rect(Math.round(mResizeRect.left), Math.round(mResizeRect.top),
                    Math.round(mResizeRect.right), Math.round(mResizeRect.bottom));
            Rect finallyResizeRect = getMaxCanSlotRect(mFinallyResizeRect, newResizeRect, mStartPostion);
            mResizeRect.set(finallyResizeRect);

            SEVector3f fromLocation = mResizeRectObject.getLocalTranslate();
            SEVector3f toLocation = getResizeRectPostion(finallyResizeRect);
            final float oldW = mResizeRectObject.getWidth();
            final float oldH = mResizeRectObject.getHeight();
            final float newW = getWidthOfWidget(finallyResizeRect.width());
            final float newH = getHeightOfWidget(finallyResizeRect.height());
            SEEmptyAnimation sizeAnimation = new SEEmptyAnimation(mScene, 0, 1, 15) {
                @Override
                public void onAnimationRun(float value) {
                    float w = oldW + (newW - oldW) * value;
                    float h = oldH + (newH - oldH) * value;
                    if (mResizeRectObject != null)
                        mResizeRectObject.reSize(w, h);
                }
            };
            sizeAnimation.execute();
            SETranslateAnimation move = new SETranslateAnimation(mScene, mResizeRectObject, fromLocation, toLocation,
                    15);
            move.setIsLocal(true);
            move.execute();
            if (!mFinallyResizeRect.equals(finallyResizeRect)) {
                mFinallyResizeRect = finallyResizeRect;
                mWidgetObject.onSizeAndPositionChanged(mFinallyResizeRect);
                SESceneManager.getInstance().removeMessage(HomeScene.MSG_TYPE_UPDATE_SHELF);
                SESceneManager.getInstance().handleMessage(HomeScene.MSG_TYPE_UPDATE_SHELF, null);
            }

            break;
        }
        return true;
    }

    private boolean touchInResizeRect(RectF screenRect, int x, int y) {
        float offset = mCamera.getWidth() / 10;
        RectF newScreenRect = new RectF(screenRect.left - offset, screenRect.top - offset, screenRect.right + offset,
                screenRect.bottom + offset);
        return newScreenRect.contains(x, y);
    }

    private RectF toScreenRect(RectF src) {
        RectF des = new RectF();
        float[] leftTop = mResizeRectObject.localToScreenCoordinate_JNI(new float[] {
                -mResizeRectObject.getWidth() / 2, 0, mResizeRectObject.getHeight() / 2 });
        float[] rightBottom = mResizeRectObject.localToScreenCoordinate_JNI(new float[] {
                mResizeRectObject.getWidth() / 2, 0, -mResizeRectObject.getHeight() / 2 });
        des.set(leftTop[0], leftTop[1], rightBottom[0], rightBottom[1]);
        return des;

    }

    private float getWidthOfWidget(float spanX) {
        return mHouse.getWallCellWidth(spanX);
    }

    private float getHeightOfWidget(float spanY) {
        return mHouse.getWallCellHeight(spanY);
    }

    private SEVector3f getResizeRectPostion(RectF rect) {
        float gridSizeX = mHouse.mCellWidth + mHouse.mWidthGap;
        float gridSizeY = mHouse.mCellHeight + mHouse.mHeightGap;
        float offsetX = (rect.left + rect.width() / 2.f) * gridSizeX - mHouse.mCellCountX
                * gridSizeX / 2.f
                + (mHouse.mWallPaddingLeft - mHouse.mWallPaddingRight) / 2;
        float offsetZ = mHouse.mCellCountY * gridSizeY / 2.f - (rect.top + rect.height() / 2.f)
                * gridSizeY
                + (mHouse.mWallPaddingBottom - mHouse.mWallPaddingTop) / 2;
        return new SEVector3f(offsetX, -100, offsetZ);
    }

    private SEVector3f getResizeRectPostion(Rect rect) {
        float gridSizeX = mHouse.mCellWidth + mHouse.mWidthGap;
        float gridSizeY = mHouse.mCellHeight + mHouse.mHeightGap;
        float offsetX = (rect.left + rect.width() / 2.f) * gridSizeX - mHouse.mCellCountX
                * gridSizeX / 2.f
                + (mHouse.mWallPaddingLeft - mHouse.mWallPaddingRight) / 2;
        float offsetZ = mHouse.mCellCountY * gridSizeY / 2.f - (rect.top + rect.height() / 2.f)
                * gridSizeY
                + (mHouse.mWallPaddingBottom - mHouse.mWallPaddingTop) / 2;
        return new SEVector3f(offsetX, -100, offsetZ);
    }

    private Rect getMaxCanSlotRect(Rect from, Rect to, START_POSITION startPostion) {
        int sizeX = mHouse.mCellCountX;
        int sizeY = mHouse.mCellCountY;
        boolean[][] fullSlot = new boolean[sizeY][sizeX];
        for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {
                fullSlot[y][x] = true;
            }
        }
        for (SEObject child : mWall.getChildObjects()) {
            if (child instanceof NormalObject && child != mWidgetObject) {
                NormalObject normalObject = (NormalObject) child;
                ObjectSlot objectSlot = normalObject.getObjectInfo().mObjectSlot;
                int startY = objectSlot.mStartY;
                int startX = objectSlot.mStartX;
                float sX = objectSlot.mSpanX;
                float sY = objectSlot.mSpanY;
                for (int y = startY; y < startY + sY; y++) {
                    for (int x = startX; x < startX + sX; x++) {
                        fullSlot[y][x] = false;
                    }
                }
            }

        }
        Rect maxRect = new Rect(from);
        switch (startPostion) {
        case LEFT_TOP:
            if (to.left != maxRect.left || to.top != maxRect.top) {
                if (to.left >= maxRect.left) {
                    maxRect.left = to.left;
                }
                if (to.top >= maxRect.top) {
                    maxRect.top = to.top;
                }
                if (to.left < maxRect.left) {
                    for (int x = maxRect.left - 1; x >= to.left; x--) {
                        boolean canPlace = true;
                        for (int y = maxRect.top; y <= maxRect.bottom - 1; y++) {
                            if (!fullSlot[y][x]) {
                                canPlace = false;
                                break;
                            }
                        }
                        if (!canPlace) {
                            break;
                        } else {
                            maxRect.left = x;
                        }
                    }
                }
                if (to.top < maxRect.top) {
                    for (int y = maxRect.top - 1; y >= to.top; y--) {
                        boolean canPlace = true;
                        for (int x = maxRect.left; x <= maxRect.right - 1; x++) {
                            if (!fullSlot[y][x]) {
                                canPlace = false;
                                break;
                            }
                        }
                        if (!canPlace) {
                            break;
                        } else {
                            maxRect.top = y;
                        }
                    }
                }
            }
            break;
        case RIGHT_TOP:
            if (to.right != maxRect.right || to.top != maxRect.top) {
                if (to.right <= maxRect.right) {
                    maxRect.right = to.right;
                }
                if (to.top >= maxRect.top) {
                    maxRect.top = to.top;
                }
                if (to.right > maxRect.right) {
                    for (int x = maxRect.right; x <= to.right - 1; x++) {
                        boolean canPlace = true;
                        for (int y = maxRect.top; y <= maxRect.bottom - 1; y++) {
                            if (!fullSlot[y][x]) {
                                canPlace = false;
                                break;
                            }
                        }
                        if (!canPlace) {
                            break;
                        } else {
                            maxRect.right = x + 1;
                        }
                    }
                }
                if (to.top < maxRect.top) {
                    for (int y = maxRect.top - 1; y >= to.top; y--) {
                        boolean canPlace = true;
                        for (int x = maxRect.left; x <= maxRect.right - 1; x++) {
                            if (!fullSlot[y][x]) {
                                canPlace = false;
                                break;
                            }
                        }
                        if (!canPlace) {
                            break;
                        } else {
                            maxRect.top = y;
                        }
                    }
                }
            }
            break;
        case LEFT_BOTTOM:
            if (to.left != maxRect.left || to.bottom != maxRect.bottom) {
                if (to.left >= maxRect.left) {
                    maxRect.left = to.left;
                }
                if (to.bottom <= maxRect.bottom) {
                    maxRect.bottom = to.bottom;
                }
                if (to.left < maxRect.left) {
                    for (int x = maxRect.left - 1; x >= to.left; x--) {
                        boolean canPlace = true;
                        for (int y = maxRect.top; y <= maxRect.bottom - 1; y++) {
                            if (!fullSlot[y][x]) {
                                canPlace = false;
                                break;
                            }
                        }
                        if (!canPlace) {
                            break;
                        } else {
                            maxRect.left = x;
                        }
                    }
                }
                if (to.bottom > maxRect.bottom) {
                    for (int y = maxRect.bottom - 1; y < to.bottom; y++) {
                        boolean canPlace = true;
                        for (int x = maxRect.left; x <= maxRect.right - 1; x++) {
                            if (!fullSlot[y][x]) {
                                canPlace = false;
                                break;
                            }
                        }
                        if (!canPlace) {
                            break;
                        } else {
                            maxRect.bottom = y + 1;
                        }
                    }
                }
            }
            break;
        case RIGHT_BOTTOM:
            if (to.right != maxRect.right || to.bottom != maxRect.bottom) {
                if (to.right <= maxRect.right) {
                    maxRect.right = to.right;
                }
                if (to.bottom <= maxRect.bottom) {
                    maxRect.bottom = to.bottom;
                }
                if (to.right > maxRect.right) {
                    for (int x = maxRect.right; x <= to.right - 1; x++) {
                        boolean canPlace = true;
                        for (int y = maxRect.top; y <= maxRect.bottom - 1; y++) {
                            if (!fullSlot[y][x]) {
                                canPlace = false;
                                break;
                            }
                        }
                        if (!canPlace) {
                            break;
                        } else {
                            maxRect.right = x + 1;
                        }
                    }
                }
                if (to.bottom > maxRect.bottom) {
                    for (int y = maxRect.bottom; y <= to.bottom - 1; y++) {
                        boolean canPlace = true;
                        for (int x = maxRect.left; x <= maxRect.right - 1; x++) {
                            if (!fullSlot[y][x]) {
                                canPlace = false;
                                break;
                            }
                        }
                        if (!canPlace) {
                            break;
                        } else {
                            maxRect.bottom = y + 1;
                        }
                    }
                }
            }
            break;
        }
        return maxRect;
    }

}
