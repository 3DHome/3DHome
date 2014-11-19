package com.borqs.se.widget3d;

import android.appwidget.AppWidgetProviderInfo;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.widget.FrameLayout.LayoutParams;

import com.borqs.se.engine.SEAnimFinishListener;
import com.borqs.se.engine.SEObject;
import com.borqs.se.engine.SEObjectFactory;
import com.borqs.se.engine.SESceneManager;
import com.borqs.se.engine.SETransParas;
import com.borqs.se.engine.SEScene.SCENE_CHANGED_TYPE;
import com.borqs.se.engine.SEVector.AXIS;
import com.borqs.se.engine.SEVector.SERect3D;
import com.borqs.se.engine.SEVector.SEVector3f;
import com.borqs.se.home3d.HomeScene;
import com.borqs.se.home3d.HomeManager;
import com.borqs.se.shortcut.HomeWidgetHostView;
import com.borqs.se.widget3d.ObjectInfo.ObjectSlot;

public class WidgetObject extends NormalObject {
    private HomeWidgetHostView mWidget;
    protected SEObject mIconObject;
    public ObjectSlot mPreSlot;
    private HomeManager mHomeManager;
    private int mWidth;
    private int mHeight;
    private Rect mWidgetToScreenRect;
    private House mHouse;
    private boolean mHasCreated = false;

    public WidgetObject(HomeScene scene, String name, int index) {
        super(scene, name, index);
        setOnClickListener(null);
        mHomeManager = HomeManager.getInstance();
    }

    /**
     * the image Max width should smaller than 512 and Max height should smaller
     * than 1024
     */
    private int[] resizeImage(int w, int h) {
        int[] imageSize = new int[2];
        float maxW = getWidthOfWidget(4);
        float maxH = getHeightOfWidget(4);
        float scaleOfW = 512 / maxW;
        float scaleOfH = 1024 / maxH;
        float scaleOfImage = scaleOfW > scaleOfH ? scaleOfH : scaleOfW;
        imageSize[0] = (int) (w * scaleOfImage);
        imageSize[1] = (int) (h * scaleOfImage);
        return imageSize;
    }

    @Override
    public void initStatus() {
        super.initStatus();
        setCanChangeBind(false);
        setCanBeResized(true);
        if (getParent().getParent() instanceof House) {
            mHouse = (House) getParent().getParent();
        } else {
            mHouse = getHomeScene().getHouse();
        }
        setOnLongClickListener(new SEObject.OnTouchListener() {
            public void run(SEObject obj) {
                SESceneManager.getInstance().runInUIThread(new Runnable() {
                    public void run() {
                        mWidget.clearFocus();
                        mWidget.setPressed(false);
                    }
                });
                hideBackground();
                setPressType(PRESS_TYPE.ALPHA);
                SETransParas startTranspara = new SETransParas();
                startTranspara.mTranslate = getAbsoluteTranslate();
                float angle = getUserRotate().getAngle();
                SEObject parent = getParent();
                while (parent != null) {
                    angle = angle + parent.getUserRotate().getAngle();
                    parent = parent.getParent();
                }
                startTranspara.mRotate.set(angle, 0, 0, 1);
                setStartTranspara(startTranspara);
                setOnMove(true);
            }
        });
        if (!mHasCreated) {
            AppWidgetProviderInfo providerInfo = mHomeManager.getAppWidgetManager().getAppWidgetInfo(
                    getObjectInfo().mAppWidgetId);
            if (providerInfo == null || getObjectSlot().mSpanX <= 0 || getObjectSlot().mSpanY <= 0) {
                getParent().removeChild(this, true);
                return;
            }
            String imageKey = mName + "_imageKey";
            String imageName = mName + "_imageName";
            mWidth = (int) getWidthOfWidget(getObjectSlot().mSpanX);
            mHeight = (int) getHeightOfWidget(getObjectSlot().mSpanY);
            SERect3D rect = new SERect3D(new SEVector3f(1, 0, 0), new SEVector3f(0, 0, 1));
            rect.setSize(mWidth, mHeight, 1);
            mIconObject = new SEObject(getScene(), mName + "_icon");
            SEObjectFactory
                    .createRectangle(mIconObject, rect, IMAGE_TYPE.BITMAP, imageName, imageKey, null, null, true);
            int[] imageSize = resizeImage(mWidth, mHeight);
            mIconObject.setImageValidAreaSize(imageSize[0], imageSize[1]);
            // Opengl中Bitmap应该经过上下像素反转再使用，以前会在引擎端自动反转Bitmap，这种方式性能低，现在我们直接反转贴图坐标也可以做到
            mIconObject.revertTextureImage();
            mIconObject.setBlendSortAxis(AXIS.Y);
            addChild(mIconObject, true);
            mIconObject.setIsEntirety(false);
        }

        bind();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
        case MotionEvent.ACTION_DOWN:
            setPressType(PRESS_TYPE.NONE);
            getHomeScene().setStatus(HomeScene.STATUS_ON_WIDGET_TOUCH, true);
            mWidgetToScreenRect = new Rect();
            float[] leftTop = localToScreenCoordinate_JNI(new float[] { -mWidth / 2f, 0, mHeight / 2f });
            float[] rightBottom = localToScreenCoordinate_JNI(new float[] { mWidth / 2f, 0, -mHeight / 2f });
            mWidgetToScreenRect.set((int) leftTop[0], (int) leftTop[1], (int) rightBottom[0], (int) rightBottom[1]);
            break;
        case MotionEvent.ACTION_MOVE:
            getHomeScene().setStatus(HomeScene.STATUS_ON_WIDGET_TOUCH, true);
            removeLongClick();
            break;
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            getHomeScene().setStatus(HomeScene.STATUS_ON_WIDGET_TOUCH, false);
            break;
        }

        final MotionEvent event = MotionEvent.obtain(ev);
        float scale = getScaleParasOfWidget();
        int widgetW = (int) (mWidth * scale);
        int widgetH = (int) (mHeight * scale);

        float scaleX = widgetW / (float) mWidgetToScreenRect.width();
        float scaleY = widgetH / (float) mWidgetToScreenRect.height();
        float touchX = (ev.getX() - mWidgetToScreenRect.left) * scaleX;
        float touchY = (ev.getY() - mWidgetToScreenRect.top) * scaleY;
        event.setLocation(touchX, touchY);
        SESceneManager.getInstance().runInUIThread(new Runnable() {
            public void run() {
                try {
                    if (mWidget != null) {
                        mWidget.dispatchTouchEvent(event);
                    }
                } catch (Exception e) {

                }
            }
        });
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onSizeAndPositionChanged(Rect sizeRect) {
        removeChild(mIconObject, true);
        String imageKey = mName + "_imageKey";
        String imageName = mName + "_imageName";
        getObjectSlot().mSpanX = sizeRect.width();
        getObjectSlot().mSpanY = sizeRect.height();
        getObjectSlot().mStartX = sizeRect.left;
        getObjectSlot().mStartY = sizeRect.top;
        getObjectInfo().updateSlotDB();
        mWidth = (int) getWidthOfWidget(getObjectSlot().mSpanX);
        mHeight = (int) getHeightOfWidget(getObjectSlot().mSpanY);
        SERect3D rect = new SERect3D(new SEVector3f(1, 0, 0), new SEVector3f(0, 0, 1));
        rect.setSize(mWidth, mHeight, 1);
        mIconObject = new SEObject(getScene(), mName + "_icon");
        SEObjectFactory.createRectangle(mIconObject, rect, IMAGE_TYPE.BITMAP, imageName, imageKey, null, null, true);
        int[] imageSize = resizeImage(mWidth, mHeight);
        mIconObject.setImageValidAreaSize(imageSize[0], imageSize[1]);
        mIconObject.revertTextureImage();
        addChild(mIconObject, true);
        mIconObject.setIsEntirety(false);
        mIconObject.setBlendSortAxis(AXIS.Y);
        if (getParent() != null && (getParent() instanceof VesselObject)) {
            VesselObject vessel = (VesselObject) getParent();
            SETransParas transParas = vessel.getTransParasInVessel(this, getObjectSlot());
            if (transParas != null) {
                getUserTransParas().set(transParas);
                setUserTransParas();
            }
        }
        requestWidgetLayout();
    }

    @Override
    public boolean handleBackKey(SEAnimFinishListener l) {
        if (getHomeScene().getDragLayer().isOnResize()) {
            getHomeScene().getDragLayer().finishResize();
            return true;
        }
        return false;
    }

    private float getWidthOfWidget(float spanX) {
        return mHouse.getWallCellWidth(spanX);
    }

    private float getHeightOfWidget(float spanY) {
        return mHouse.getWallCellHeight(spanY);
    }

    @Override
    public void showBackground() {
        if (hasBeenReleased()) {
            return;
        }
    }

    @Override
    public void hideBackground() {
    }

    @Override
    public void onSlotSuccess() {
        super.onSlotSuccess();
        if (hasBeenReleased()) {
            return;
        }
    }

    public void bind() {
        if (!hasInit()) {
            return;
        }
        SESceneManager.getInstance().runInUIThread(new Runnable() {
            public void run() {
                if (mWidget != null) {
                    mWidget.releaseBitMap();
                    mHomeManager.getWorkSpace().removeView(mWidget);
                }
                AppWidgetProviderInfo providerInfo = mHomeManager.getAppWidgetManager().getAppWidgetInfo(
                        getObjectInfo().mAppWidgetId);
                HomeWidgetHostView hostView = (HomeWidgetHostView) mHomeManager.getAppWidgetHost().createView(
                        getContext(), getObjectInfo().mAppWidgetId, providerInfo);
                float scale = getScaleParasOfWidget();
                int widgetW = (int) (mWidth * scale);
                int widgetH = (int) (mHeight * scale);
                LayoutParams params = new LayoutParams(widgetW, widgetH);
                hostView.setLayoutParams(params);
                setWidgetHostView(hostView);
                int[] imageSize = resizeImage(mWidth, mHeight);
                hostView.setImageSize(imageSize[0], imageSize[1]);
                hostView.setTag(WidgetObject.this);
                mWidget.setTranslationY(-mWidget.getLayoutParams().height);
                mHomeManager.getWorkSpace().addView(hostView);
            }
        });
    }

    @Override
    public void onSceneChanged(SCENE_CHANGED_TYPE changeType) {
        super.onSceneChanged(changeType);
        if (changeType == SCENE_CHANGED_TYPE.NEW_SCENE) {
            SESceneManager.getInstance().runInUIThread(new Runnable() {
                public void run() {
                    if (mWidget != null) {
                        mWidget.releaseBitMap();
                        mHomeManager.getWorkSpace().removeView(mWidget);
                    }
                }
            });
        } else if (changeType == SCENE_CHANGED_TYPE.NEW_CONFIG) {
            if (mWidget != null) {
                Rect rect = new Rect(getObjectSlot().mStartX, getObjectSlot().mStartY, getObjectSlot().mStartX
                        + getObjectSlot().mSpanX, getObjectSlot().mStartY + getObjectSlot().mSpanY);
                onSizeAndPositionChanged(rect);
            }
        }
    }

    private void requestWidgetLayout() {
        SESceneManager.getInstance().runInUIThread(new Runnable() {
            public void run() {
                if (mWidget == null) {
                    return;
                }
                float scale = getScaleParasOfWidget();
                int widgetW = (int) (mWidth * scale);
                int widgetH = (int) (mHeight * scale);
                LayoutParams params = new LayoutParams(widgetW, widgetH);
                mWidget.setLayoutParams(params);
                mWidget.layout(0, 0, params.width, params.height);
                setWidgetHostView(mWidget);
                int[] imageSize = resizeImage(mWidth, mHeight);
                mWidget.setImageSize(imageSize[0], imageSize[1]);
                mWidget.setTag(WidgetObject.this);
                mWidget.setTranslationY(-mWidget.getLayoutParams().height);
                mWidget.requestLayout();
            }
        });
    }

    private float getScaleParasOfWidget() {
        if (getHomeScene().isScreenLarge()) {
            return 1;
        }
        return (getCamera().getWidth() + 16) / mHouse.mWallWidth;
    }

    @Override
    public void onRelease() {
        super.onRelease();
        unbind();
    }

    private void unbind() {
        if (!hasInit()) {
            return;
        }
        SESceneManager.getInstance().runInUIThread(new Runnable() {
            public void run() {
                if (mWidget != null) {
                    mWidget.releaseBitMap();
                    mHomeManager.getWorkSpace().removeView(mWidget);
                    mHomeManager.getAppWidgetHost().deleteAppWidgetId(getObjectInfo().mAppWidgetId);
                }
            }
        });
    }

    public void setWidgetHostView(HomeWidgetHostView view) {
        mWidget = view;
        mWidget.setWidgetObject(this);
    }

}