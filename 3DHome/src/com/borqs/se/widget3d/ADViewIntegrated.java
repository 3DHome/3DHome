package com.borqs.se.widget3d;

import com.borqs.se.R;
import com.borqs.se.engine.SECommand;
import com.borqs.se.engine.SEObject;
import com.borqs.se.engine.SESceneManager;
import com.borqs.se.engine.SEUtils;
import com.borqs.se.home3d.HomeManager;
import com.borqs.se.home3d.HomeUtils;
import com.borqs.se.home3d.SettingsActivity;
import com.google.ads.Ad;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdRequest.ErrorCode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class ADViewIntegrated extends LinearLayout {
    private static final int MSG_UPDATE_IMAGE = 1;
    private static final int MSG_RELEASE_IMAGE = 2;
    private static final int MSG_TRY_TO_LOAD_ADMOB = 3;
    public static final long MIN_REQUEST_INTERVAL = 60000;
    private int mTryCount = 0;
    private AdListener mAdListener;
    private com.google.ads.AdView mAdmod;
    private boolean mHasAD = false;
    private int mImageW, mImageH;
    private Handler mHandler;
    private Bitmap mMyBitmap;
    private Canvas mMyCanvas;
    private String mImageName;
    private boolean mIsEnableCatchImage = false;
    private boolean mHasBeenDestoryed = false;
    private Bitmap mCrossBitmap;
    private WebView mWebView;

    public ADViewIntegrated(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    public ADViewIntegrated(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public ADViewIntegrated(Context context) {
        super(context);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                case MSG_UPDATE_IMAGE:
                    if (!mHasBeenDestoryed)
                        mUpdateBitmap.run();
                    break;
                case MSG_RELEASE_IMAGE:
                    releaseBitmap();
                    break;
                case MSG_TRY_TO_LOAD_ADMOB:
                    if (!mHasAD) {
                        if (isNetworkingAvailable() && mTryCount < 5) {
                            loadOneAD();
                            mTryCount++;
                        } else {
                            removeMessages(MSG_TRY_TO_LOAD_ADMOB);
                            sendEmptyMessageDelayed(MSG_TRY_TO_LOAD_ADMOB, MIN_REQUEST_INTERVAL);
                        }
                    }
                    break;
                }
            }

        };
        BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.remove_ad);
        mCrossBitmap = bitmapDrawable.getBitmap();
    }

    public void setAdListener(AdListener l) {
        mAdListener = l;
    }

    public View showChild(int index) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (i == index) {
                getChildAt(i).setVisibility(View.VISIBLE);
            } else {
                getChildAt(i).setVisibility(View.INVISIBLE);
            }
        }
        requestLayout();
        return getChildAt(index);

    }

    /**
     * 申请加载一个广告，但是必须要注意距离上一个申请不等小于30S
     */
    public void requestLoadAD() {
        if (mAdmod != null && !mOnLoadingAD) {
            if (!mHasAD) {
                mTryCount = 0;
                mHandler.removeMessages(MSG_TRY_TO_LOAD_ADMOB);
                mHandler.sendEmptyMessageDelayed(MSG_TRY_TO_LOAD_ADMOB, MIN_REQUEST_INTERVAL);
            } else {
                long currrentTime = System.currentTimeMillis();
                if (Math.abs(currrentTime - mRequestADTime) > MIN_REQUEST_INTERVAL) {
                    loadOneAD();
                }
            }
        }
    }

    private boolean mHasBeenPaused = false;

    public void onResume() {
        if (getWebView() != null && mHasBeenPaused && !mHasBeenDestoryed) {
            if (HomeUtils.DEBUG)
                Log.d("ADMOB##########################################", "resume ADVIEW");
            mHasBeenPaused = false;
            getWebView().resumeTimers();
            getWebView().onResume();
        }
    }

    public void onPause() {
        if (getWebView() != null && !mHasBeenPaused && !mHasBeenDestoryed) {
            if (HomeUtils.DEBUG)
                Log.d("ADMOB##########################################", "pause ADVIEW");
            mHasBeenPaused = true;
            getWebView().pauseTimers();
            getWebView().onPause();
        }
    }

    private long mRequestADTime = 0;

    private void loadOneAD() {
        if (mAdmod != null) {
            mRequestADTime = System.currentTimeMillis();
            if (mAdListener != null) {
                mAdListener.onRequestAd();
            }
            mOnLoadingAD = true;
            mAdmod.loadAd(new AdRequest());
        }
    }

    private void stopLoadAD() {
        if (mAdmod != null) {
            mAdmod.stopLoading();
        }
    }

    public boolean hasAD() {
        return mHasAD;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeMessages(MSG_TRY_TO_LOAD_ADMOB);
        mHasBeenDestoryed = true;
        destoryAD();
    }

    private void destoryAD() {
        if (mAdmod != null) {
            if (mAdListener != null) {
                mAdListener.onDestoryAd();
            }
            mAdmod.destroy();
            mAdmod.setAdListener(null);
        }
    }

    public interface AdListener {
        public void onFailedToReceiveAd(String arg0);

        public void onReceiveAd();
        
        public void onRequestAd();
        
        public void onDestoryAd();
    }

    @Override
    public ViewParent invalidateChildInParent(int[] location, Rect dirty) {
        if (mIsEnableCatchImage) {
            mHandler.removeMessages(MSG_UPDATE_IMAGE);
            mHandler.sendEmptyMessage(MSG_UPDATE_IMAGE);
        }
        return super.invalidateChildInParent(location, dirty);
    }

    public void requestCatchImage(long delay) {
        if (HomeUtils.DEBUG)
            Log.d("ADMOB##########################################", "requestCatchImage");
        mHandler.post(new Runnable() {
            public void run() {
                onResume();
            }
        });
        mIsEnableCatchImage = true;
        mHandler.removeMessages(MSG_UPDATE_IMAGE);
        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_IMAGE, delay);
    }

    public void stopCatchImage() {
        if (HomeUtils.DEBUG)
            Log.d("ADMOB##########################################", "stopCatchImage");
        mHandler.post(new Runnable() {
            public void run() {
                onPause();
            }
        });
        mIsEnableCatchImage = false;
        mHandler.removeMessages(MSG_UPDATE_IMAGE);
        mHandler.sendEmptyMessage(MSG_RELEASE_IMAGE);
    }

    public void onDestory() {
        onResume();
        mIsEnableCatchImage = false;
        mHandler.removeMessages(MSG_UPDATE_IMAGE);
        mHandler.sendEmptyMessage(MSG_RELEASE_IMAGE);
    }

    private boolean mHasApplyImage = false;

    public void setImageName(String imageName) {
        mHasApplyImage = false;
        mImageName = imageName;
    }

    public void setImageSize(int w, int h) {
        mImageW = w;
        mImageH = h;
    }

    private Canvas tryToGetCanvas() {
        if (mMyCanvas != null && !mMyBitmap.isRecycled()) {
            return mMyCanvas;
        }
        int newW = SEUtils.higherPower2(mImageW);
        int newH = SEUtils.higherPower2(mImageH);
        try {
            mMyBitmap = Bitmap.createBitmap(newW, newH, Bitmap.Config.ARGB_8888);
        } catch (Exception e) {
            mMyBitmap = null;
        }
        if (mMyBitmap != null) {
            mMyCanvas = new Canvas(mMyBitmap);
            mMyCanvas.translate((newW - mImageW) * 0.5f, (newH - mImageH) * 0.5f);
            return mMyCanvas;
        }
        return null;
    }

    private int mImageData = 0;
    private Object mLock = new Object();

    private Runnable mUpdateBitmap = new Runnable() {
        public void run() {
            if (getChildAt(0) == null) {
                return;
            }
            Canvas canvas = tryToGetCanvas();
            if (canvas == null) {
                return;
            }
            getChildAt(0).setDrawingCacheEnabled(true);
            Bitmap catchBitmap = getChildAt(0).getDrawingCache();
            mMyBitmap.eraseColor(0);
            if (catchBitmap != null && !catchBitmap.isRecycled()) {
                Rect src = new Rect(0, 0, catchBitmap.getWidth(), catchBitmap.getHeight());
                Rect des = new Rect(0, 0, mImageW, mImageH);
                canvas.drawBitmap(catchBitmap, src, des, null);
                getChildAt(0).setDrawingCacheEnabled(false);
                // if engine has not used the previous image, release the
                // previous image
                synchronized (mLock) {
                    if (mImageData != 0) {
                        SEObject.releaseImageData_JNI(mImageData);
                        mImageData = 0;
                    }
                }
                if (mCrossBitmap != null
                        && !getContext().getString(R.string.channel_amazon).equalsIgnoreCase(
                                SettingsActivity.getChannelCode(getContext()))) {
                    canvas.drawBitmap(mCrossBitmap, 0, 0, null);
                }
                int imageData = SEObject.loadImageData_JNI(mMyBitmap);
                synchronized (mLock) {
                    mImageData = imageData;
                }
                new SECommand(HomeManager.getInstance().getHomeScene()) {
                    public void run() {
                        synchronized (mLock) {
                            if (!mHasApplyImage) {
                                mHasApplyImage = true;
                                SEObject.applyImage_JNI(mImageName, mImageName);
                            }
                            SEObject.addImageData_JNI(mImageName, mImageData);
                            mImageData = 0;
                        }
                    }
                }.execute();
            }
        }
    };

    private Runnable mOpenADTask = new Runnable() {
        public void run() {
            if (getChildAt(0) == null) {
                return;
            }
            try {
                float pointX = getChildAt(0).getWidth() - 50;
                float pointY = getChildAt(0).getHeight() / 2;
                long now = SystemClock.uptimeMillis();
                MotionEvent eventDowne = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, pointX, pointY, 0);
                getChildAt(0).dispatchTouchEvent(eventDowne);
                MotionEvent eventUp = MotionEvent.obtain(now, now, MotionEvent.ACTION_UP, pointX, pointY, 0);
                getChildAt(0).dispatchTouchEvent(eventUp);
            } catch (Exception e) {
            }
        }
    };

    public void doClick() {
        mHandler.removeCallbacks(mOpenADTask);
        mHandler.post(mOpenADTask);
    }

    private void releaseBitmap() {
        if (mMyBitmap != null && !mMyBitmap.isRecycled()) {
            mMyBitmap.recycle();
        }
    }

    /*************************************************************************** admob ********************/
    private boolean mOnLoadingAD = false;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        try {
            initADMod();
        } catch (Exception e) {

        }
    }

    private void initADMod() {
        if (mAdmod != null) {
            return;
        }
        AdSize adSize;
        DisplayMetrics metric = new DisplayMetrics();
        SESceneManager.getInstance().getGLActivity().getWindowManager().getDefaultDisplay().getMetrics(metric);
        if (metric.widthPixels >= 1500) {
            adSize = AdSize.IAB_BANNER;
        } else {
            adSize = AdSize.BANNER;
        }
        
        mAdmod = new com.google.ads.AdView(SESceneManager.getInstance().getGLActivity(), adSize, "a150d168b3b6e1f");
        RelativeLayout.LayoutParams myLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        mAdmod.setLayoutParams(myLayoutParams);
        addView(mAdmod);
        mAdmod.loadAd(new AdRequest());
        mRequestADTime = System.currentTimeMillis();
        mOnLoadingAD = true;
        if (mAdListener != null) {
            mAdListener.onRequestAd();
        }
        mAdmod.setAdListener(new com.google.ads.AdListener() {

            public void onDismissScreen(Ad arg0) {

            }

            public void onFailedToReceiveAd(Ad arg0, ErrorCode error) {
                stopLoadAD();
                mOnLoadingAD = false;
                if (mAdListener != null) {
                    mAdListener.onFailedToReceiveAd(error.toString());
                }
                if (!mHasAD) {
                    mHandler.removeMessages(MSG_TRY_TO_LOAD_ADMOB);
                    mHandler.sendEmptyMessageDelayed(MSG_TRY_TO_LOAD_ADMOB, MIN_REQUEST_INTERVAL * (mTryCount + 1));
                }

            }

            public void onLeaveApplication(Ad arg0) {

            }

            public void onPresentScreen(Ad arg0) {

            }

            public void onReceiveAd(Ad arg0) {
                stopLoadAD();
                mOnLoadingAD = false;
                mHasAD = true;
                if (mAdListener != null) {
                    mAdListener.onReceiveAd();
                }
            }

        });
    }

    private WebView findWebView(ViewGroup viewGroup) {
        int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof WebView) {
                return (WebView) view;
            } else if (view instanceof ViewGroup) {
                ViewGroup v = (ViewGroup) view;
                WebView webView = findWebView(v);
                if (webView != null) {
                    return webView;
                }
            }
        }
        return null;

    }

    private boolean isNetworkingAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeInfo = cm.getActiveNetworkInfo();
        if (null != activeInfo) {
            return true;
        }

        return false;
    }

    private WebView getWebView() {
        if (mWebView == null) {
            if (mAdmod != null) {
                mWebView = findWebView(mAdmod);
            }
        }
        return mWebView;
    }
}
