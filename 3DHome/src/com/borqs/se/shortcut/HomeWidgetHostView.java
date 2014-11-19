package com.borqs.se.shortcut;

import com.borqs.se.R;
import com.borqs.se.engine.SECommand;
import com.borqs.se.engine.SESceneManager;
import com.borqs.se.engine.SEUtils;
import com.borqs.se.home3d.HomeManager;
import com.borqs.se.widget3d.WidgetObject;

import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

public class HomeWidgetHostView extends AppWidgetHostView {
    private static final int MSG_UPDATE_IMAGE = 0;
    private LayoutInflater mInflater;
    private WidgetObject mWidgetObject;
    private Handler mHandler;
    private Canvas mMyCanvas;
    private boolean mHasReleased;
    private int mImageW, mImageH;
    private String mImageKey;
    private SESceneManager mSESceneManager;

    public HomeWidgetHostView(Context context) {
        super(context);
        mSESceneManager = SESceneManager.getInstance();
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_UPDATE_IMAGE:
                    mUpdateBitmap.run();
                    break;
                }
            }

        };
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
        if (mHandler != null) {
            mHandler.removeMessages(MSG_UPDATE_IMAGE);
            mHandler.sendEmptyMessage(MSG_UPDATE_IMAGE);
        }
    }

    @Override
    public ViewParent invalidateChildInParent(int[] location, Rect dirty) {
        mHandler.removeMessages(MSG_UPDATE_IMAGE);
        mHandler.sendEmptyMessage(MSG_UPDATE_IMAGE);
        return super.invalidateChildInParent(location, dirty);
    }

    public void setWidgetObject(WidgetObject obj) {
        mWidgetObject = obj;
        mImageKey = mWidgetObject.getName() + "_imageKey";
    }

    public WidgetObject getWidgetObject() {
        return mWidgetObject;
    }

    public void setImageSize(int w, int h) {
        mImageW = w;
        mImageH = h;
        mSESceneManager.removeTextureImage(mImageKey);
        mMyCanvas = null;
    }

    private Canvas tryToGetCanvas() {
        if (mMyCanvas != null) {
            return mMyCanvas;
        }
        int newW = SEUtils.higherPower2(mImageW);
        int newH = SEUtils.higherPower2(mImageH);
        try {
            Bitmap textureBitmap = Bitmap.createBitmap(newW, newH, Bitmap.Config.ARGB_8888);
            mSESceneManager.addTextureImage(mImageKey, textureBitmap);
            mMyCanvas = new Canvas(textureBitmap);
            mMyCanvas.translate((newW - mImageW) * 0.5f, (newH - mImageH) * 0.5f);
            return mMyCanvas;
        } catch (Exception e) {

        }
        return null;
    }

    private Runnable mUpdateBitmap = new Runnable() {
        public void run() {
            if (mHasReleased) {
                return;
            }
            Canvas canvas = tryToGetCanvas();
            if (canvas == null) {
                return;
            }
            setDrawingCacheEnabled(true);
            Bitmap catchBitmap = getDrawingCache();
            Bitmap textureBitmap = mSESceneManager.getTextureImage(mImageKey);           
            if (catchBitmap != null && !catchBitmap.isRecycled()) {
                Rect srcRect = new Rect(0, 0, mImageW, mImageH);
                Rect desRect = new Rect(0, 0, mImageW, mImageH);
                Bitmap temp = Bitmap.createScaledBitmap(catchBitmap, mImageW, mImageH, true);
                if (catchBitmap != temp) {
                    catchBitmap.recycle();
                }
                if (temp != null) {
                    textureBitmap.eraseColor(0);
                    canvas.drawBitmap(temp, srcRect, desRect, null);
                    temp.recycle();
                    setDrawingCacheEnabled(false);
                    new SECommand(HomeManager.getInstance().getHomeScene()) {
                        public void run() {
                            mSESceneManager.changeTextureImage(mImageKey);
                        }
                    }.execute();
                }
            }
        }
    };

    @Override
    protected View getErrorView() {
        return mInflater.inflate(R.layout.appwidget_error, this, false);
    }

    @Override
    public int getDescendantFocusability() {
        return ViewGroup.FOCUS_BLOCK_DESCENDANTS;
    }

    public void releaseBitMap() {
        mHasReleased = true;
        mSESceneManager.removeTextureImage(mImageKey);
    }
}
