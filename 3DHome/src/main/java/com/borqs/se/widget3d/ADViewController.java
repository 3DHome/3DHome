package com.borqs.se.widget3d;

import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.borqs.market.api.PurchaseListener;
import com.borqs.market.utils.MarketUtils;
import com.borqs.se.engine.SESceneManager;
import com.borqs.se.engine.SECamera.CameraChangedListener;
import com.borqs.se.home3d.HomeScene;
import com.borqs.se.home3d.HomeUtils;
import com.borqs.se.home3d.ModelInfo;
import com.borqs.se.home3d.HomeManager;
import com.borqs.se.home3d.ProviderUtils.ModelColumns;
import com.borqs.se.home3d.SettingsActivity;

public class ADViewController implements CameraChangedListener {
    private HomeManager mHomeManager;
    private ADViewIntegrated mADViewIntegrated;
    private Context mContext;
    private HomeScene mHomeScene;
    private static ADViewController mADViewController;

//    private List<ModelInfo> mFlyers;
    private int mCurrentFlyerIndex;
    private Flyer mCurrentFlyer;
    private boolean mIsFly;
    private boolean mHasGivenMoneyToUs;
    private long mPreRequestADTime;

    private ADViewController() {
        mHomeManager = HomeManager.getInstance();
        mContext = mHomeManager.getContext();
        mCurrentFlyerIndex = 0;
        mHasGivenMoneyToUs = hasGivenMoneyToUs(mContext) || hasGivenMoneyToUs2(mContext);
        if (mHasGivenMoneyToUs) {
            SettingsActivity.setHasGivenMoneyToUs(mContext, true);
        }
        if (!mHasGivenMoneyToUs) {
            MarketUtils.hasPurchase(mContext, "com.borqs.se.object.fighter", new PurchaseListener() {

                @Override
                public void onComplete(boolean hasPurchased) {
                    if (hasPurchased) {
                        SESceneManager.getInstance().runInUIThread(new Runnable() {

                            @Override
                            public void run() {
                                SettingsActivity.setHasGivenMoneyToUs(mContext, true);
                                mHasGivenMoneyToUs = true;
                                SESceneManager.getInstance().runInUIThread(new Runnable() {
                                    public void run() {
                                        deleteAD();
                                    }
                                });
                                if (mIsFly && mCurrentFlyer != null) {
                                    mCurrentFlyer.setHasGivenMoneyToUs(true);
                                }
                            }
                        });
                    }
                }

                @Override
                public void onError(Exception e) {
                    // TODO Auto-generated method stub

                }

            });
        }
    }

    public static ADViewController getInstance() {
        if (mADViewController == null) {
            mADViewController = new ADViewController();
        }
        return mADViewController;
    }

    public void onPause() {
        if (mADViewIntegrated != null) {
            mADViewIntegrated.onPause();
        }
    }

    /**
     * 在3DHome回来的时候申请加载ADMOB，假如用户已经付钱了那么不加载广告
     */
    public void onResume() {
        if (!mHasGivenMoneyToUs) {
            mHasGivenMoneyToUs = hasGivenMoneyToUs(mContext);
        }
        if (!mHasGivenMoneyToUs) {
            if (!initADIntegrated()) {
                if (mADViewIntegrated != null && mIsFly) {
                    mADViewIntegrated.onResume();
                }
            }
        } else {
            deleteAD();
            if (mIsFly && mCurrentFlyer != null) {
                mCurrentFlyer.setHasGivenMoneyToUs(true);
            }
        }
    }

    public void onDestory() {
        deleteAD();
    }

    public void setHomeScene(HomeScene scene) {
        mHomeScene = scene;
    }

    private boolean initADIntegrated() {
        if (mADViewIntegrated == null && !mHomeManager.withoutAD()) {
            mADViewIntegrated = new ADViewIntegrated(mContext);
            LinearLayout.LayoutParams adParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            mADViewIntegrated.setFocusable(true);
            mADViewIntegrated.setLayoutParams(adParams);
            mADViewIntegrated.setOrientation(LinearLayout.VERTICAL);
            mADViewIntegrated.setTranslationY(-200);
            mADViewIntegrated.setAdListener(new ADViewIntegrated.AdListener() {
                public void onFailedToReceiveAd(String error) {
                    if (HomeUtils.DEBUG) {
                        Log.d("ADMOB##########################################", "onFailedToReceiveAd = " + error);
                    }
                    if (!mIsFly) {
                        stopCatchImage();
                    }
                }

                public void onReceiveAd() {
                    if (HomeUtils.DEBUG) {
                        Log.d("ADMOB##########################################", "onReceiveAd success");
                    }
                    if (!mIsFly) {
                        stopCatchImage();
                    }

                }

                public void onRequestAd() {
                    mPreRequestADTime = System.currentTimeMillis();
                    if (HomeUtils.DEBUG) {
                        Log.d("ADMOB##########################################", "onRequestAd time = "
                                + mPreRequestADTime);
                    }

                }

                public void onDestoryAd() {
                    if (HomeUtils.DEBUG) {
                        Log.d("ADMOB##########################################", "onDestoryAd");
                    }

                }

            });
            mHomeManager.getWorkSpace().addView(mADViewIntegrated);
            return true;
        }
        return false;
    }

    private void deleteAD() {
        if (mADViewIntegrated != null) {
            mADViewIntegrated.onDestory();
            mHomeManager.getWorkSpace().removeView(mADViewIntegrated);
            mADViewIntegrated = null;
        }
    }

    public void loadAD() {
        if (mADViewIntegrated != null) {
            if (HomeUtils.DEBUG) {
                Log.d("ADMOB controller ############################", "try to request load AD");
            }
            mADViewIntegrated.requestLoadAD();
        }
    }

    public boolean hasLoadedADMod() {
        return mADViewIntegrated != null && mADViewIntegrated.hasAD();
    }

    private ModelInfo selectFlyer() {
        ModelInfo modelInfo = null;
        // To find type "Airship" model from the current scene
        List<ModelInfo> modelInfos = HomeManager.getInstance().getModelManager().findModelInfoByType("Airship");
        // Cycle select sky objects
        if (!modelInfos.isEmpty()) {
            if (mCurrentFlyerIndex >= modelInfos.size()) {
                mCurrentFlyerIndex = 0;
            }
            modelInfo = modelInfos.get(mCurrentFlyerIndex);
            mCurrentFlyerIndex++;
        }
        return modelInfo;
    }

    /**
     * will call back by flyer while its animation finished
     */
    public void notifyFinish() {
        stopCatchImage();
        mIsFly = false;
        if (mCurrentFlyer != null) {
            mCurrentFlyer.getParent().removeChild(mCurrentFlyer, true);
        }
    }

    @Override
    public void onCameraChanged() {
        if ((mHomeScene.getSightValue() == 1) && !mIsFly) {
            // choose one flyer and begin to fly if move camera sight to sky
            mIsFly = true;
            final ModelInfo modelInfo = selectFlyer();
            if (modelInfo != null) {
                ObjectInfo objInfo = new ObjectInfo();
                objInfo.mIndex = (int) System.currentTimeMillis();
                objInfo.setModelInfo(modelInfo);
                objInfo.mSceneName = mHomeScene.getSceneName();
                mCurrentFlyer = (Flyer) objInfo.CreateNormalObject(mHomeScene);
                mHomeScene.getContentObject().addChild(mCurrentFlyer, false);
                HomeManager.getInstance().getModelManager()
                        .create(mHomeScene.getContentObject(), mCurrentFlyer, new Runnable() {
                            public void run() {
                                mCurrentFlyer.initStatus();
                                mCurrentFlyer.fly(mHasGivenMoneyToUs);
                            }

                        });
            }
        } else if (mIsFly && mHomeScene.getSightValue() < 0.25f && mCurrentFlyer != null) {
            // stop and destroy flyer after 2s if move camera sight to wall
            mCurrentFlyer.stopFlyDelayed(2000);
        } else if (mIsFly && mHomeScene.getSightValue() >= 0.25f && mCurrentFlyer != null) {
            // cancel stop and destroy flyer if try to move camera sight to sky
            mCurrentFlyer.cancelStopFly();
        }
    }

    public void requestCatchImage(long delay) {
        if (mADViewIntegrated != null) {
            mADViewIntegrated.requestCatchImage(delay);
        }
    }

    public void stopCatchImage() {
        if (mADViewIntegrated != null) {
            mADViewIntegrated.stopCatchImage();
        }
    }

    public void doClick() {
        if (mADViewIntegrated != null) {
            mADViewIntegrated.doClick();
        }
    }

    public void setImageName(String imageName) {
        if (mADViewIntegrated != null) {
            mADViewIntegrated.setImageName(imageName);
        }
    }

    public void setImageSize(int w, int h) {
        if (mADViewIntegrated != null) {
            mADViewIntegrated.setImageSize(w, h);
        }
    }

    private static boolean hasGivenMoneyToUs(Context context) {
        return SettingsActivity.hasGivenMoneyToUs(context);
    }

    private static boolean hasGivenMoneyToUs2(Context context) {
        boolean isRemove = false;
        Cursor cursor = context.getContentResolver().query(ModelColumns.CONTENT_URI,
                new String[] { ModelColumns.PRODUCT_ID }, ModelColumns.PRODUCT_ID + " = 'com.borqs.se.object.fighter'",
                null, null);
        if (cursor != null && cursor.getCount() > 0) {
            isRemove = true;
        }
        if (cursor != null) {
            cursor.close();
        }
        return isRemove;
    }
}
