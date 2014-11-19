package com.borqs.market.fragment;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.borqs.market.BasicActivity;
import com.borqs.market.R;
import com.borqs.market.account.AccountSession;
import com.borqs.market.adapter.WallPapercreenShotAdapter;
import com.borqs.market.api.ApiUtil;
import com.borqs.market.db.DownLoadHelper;
import com.borqs.market.db.DownLoadProvider;
import com.borqs.market.db.DownloadInfoColumns;
import com.borqs.market.db.PlugInColumns;
import com.borqs.market.json.Product;
import com.borqs.market.json.ProductJSONImpl;
import com.borqs.market.listener.DownloadListener;
import com.borqs.market.net.RequestListener;
import com.borqs.market.net.WutongException;
import com.borqs.market.net.WutongParameters;
import com.borqs.market.utils.BLog;
import com.borqs.market.utils.DataConnectionUtils;
import com.borqs.market.utils.DownloadUtils;
import com.borqs.market.utils.IntentUtil;
import com.borqs.market.utils.MarketUtils;
import com.borqs.market.utils.QiupuHelper;
import com.iab.engine.MarketBillingResult;
import com.iab.engine.MarketPurchaseListener;
import com.support.StaticUtil;

public class WallpaperProductDetailFragment extends BasicFragment implements
        OnClickListener, DownloadListener {

    private static final String TAG = "ProductDetailFragment";
    private static final String FLAG_DATA = "FLAG_DATA";
    private static int     TOTAL_COUNT = 3;

    protected View pager_layout;
    private WallPapercreenShotAdapter mAdapter;
    private Product mData = null;
    private ApiUtil mApiUtil;

    protected ViewPager mPager;
    protected LinearLayout mPage;
    protected LinearLayout mOpertionGroupView;
    protected Button mDownloadOrDeleteButton;
    private ProgressBar downloadProgress;
    private TextView downloadSize;
    private TextView downloadPercent;
    private View downloadCancel;
    private View processView;

    protected DownloadManager dm;
    protected DownLoadHelper downLoadHelper;
    private String product_id = null;
    private int version_code = 1;
    private PlugInObserver plugInObserver;
    private DownloadOberserver downLoadObserver;
    private long downLoadID = -1;
    private String jsonString = null;
    protected String supported_mod;

    protected static final int PRODUCT_STATUS_PURCHASE = 0;
    protected static final int PRODUCT_STATUS_DOWNLOAD = 1;
    protected static final int PRODUCT_STATUS_DOWNLOADING = 2;
    protected static final int PRODUCT_STATUS_DOWNLOADED = 3;
    protected static final int PRODUCT_STATUS_NEED_UPDATE = 4;
    protected static final int PRODUCT_STATUS_INSTALLED = 5;
    protected static final int PRODUCT_STATUS_APPLIED = 6;
    protected int PRODUCT_STATUS = PRODUCT_STATUS_PURCHASE;

    public static Bundle getArguments(String supported_mod) {
        Bundle args = new Bundle();
        args.putString(ARGUMENTS_KEY_MOD, supported_mod);
        return args;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        supported_mod = args.getString(ARGUMENTS_KEY_MOD);
    }

    @Override
    protected void initView() {
        pager_layout = mConvertView.findViewById(R.id.pager_layout);
        mPager = (ViewPager) mConvertView.findViewById(R.id.mPager);
        mPager.setOffscreenPageLimit(TOTAL_COUNT);
        mPager.setPageMargin(20);
        mOpertionGroupView = (LinearLayout) mConvertView.findViewById(R.id.opertion_group_view);
        mPage = (LinearLayout) mConvertView.findViewById(R.id.pages);
        mDownloadOrDeleteButton = (Button) mConvertView.findViewById(R.id.delete_or_download);
        mDownloadOrDeleteButton.setOnClickListener(this);

        downloadCancel = mConvertView.findViewById(R.id.download_cancel);
        downloadProgress = (ProgressBar)mConvertView.findViewById(R.id.download_progress);
        downloadSize = (TextView)mConvertView.findViewById(R.id.download_size);
        downloadPercent = (TextView)mConvertView.findViewById(R.id.download_precent);
        processView = mConvertView.findViewById(R.id.process_view);

        PhotoOnPageChangeListener pageChangeListener = new PhotoOnPageChangeListener();
        mPager.setOnPageChangeListener(pageChangeListener);

        pager_layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // dispatch the events to the ViewPager, to solve the problem that we can swipe only the middle view.
                return mPager.dispatchTouchEvent(event);
            }
        });
        downloadCancel.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if((PRODUCT_STATUS == PRODUCT_STATUS_DOWNLOADING) && downLoadID != -1) {
                    DownloadUtils.cancleDownload(mActivity, downLoadID,mData.product_id);
                    downLoadHelper.cancleDownloadFile(mData.product_id);
                    refreshProductStatus(queryProductStatus(mData.product_id));
                }
                
            }
        });
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        parseIntent(activity.getIntent());
        downLoadHelper = DownLoadHelper.getInstance(activity.getApplicationContext());
        dm = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    private void parseIntent(Intent intent) {
        product_id = intent.getStringExtra(IntentUtil.EXTRA_KEY_ID);
        version_code = intent.getIntExtra(IntentUtil.EXTRA_KEY_VERSION,1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        plugInObserver = new PlugInObserver(new Handler());
        registerContentObserver(DownLoadProvider.getContentURI(getActivity().getApplicationContext(), DownLoadProvider.TABLE_PLUGIN), true, plugInObserver);
        QiupuHelper.registerDownloadListener(WallpaperProductDetailFragment.class.getName(), this);

        if (savedInstanceState != null) {
            mData = savedInstanceState.getParcelable(FLAG_DATA);
        }
        return mConvertView;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        downLoadObserver = new DownloadOberserver();
        registerContentObserver(DownloadUtils.CONTENT_URI, true, downLoadObserver);
        if (mData != null) {
            refreshUI();
        } else {
            getProductDetail(true);
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        if(downLoadObserver != null) {
            unregisterContentObserver(downLoadObserver);
            downLoadObserver = null; 
        }
    }
 
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(FLAG_DATA, mData);
        super.onSaveInstanceState(outState);
    }
    
    private boolean wallPaperUnExist(String ProductId) {
    	String path = Environment.getExternalStorageDirectory().getPath() + "/borqs/apps/freehdhome";
        File dirRoot = new File(path);
        if (!dirRoot.exists()) {
        	return true;
        }
        String localPath ;
        if(dirRoot.isDirectory()) {
        	localPath = path + "/" + ProductId.toLowerCase();
            File tmpPathFile = new File(localPath);
            if(!tmpPathFile.exists()) {
               return true;
            }
        }
        return false;
    }
    
    private void refreshProductStatus(int status) {
        PRODUCT_STATUS = status;
        if (PRODUCT_STATUS == PRODUCT_STATUS_PURCHASE) {
            if (mData.isFree) {
                mDownloadOrDeleteButton.setText(R.string.action_download);
            } else {
                mDownloadOrDeleteButton.setText(getString(R.string.action_purchase) + "(" + mData.price + ")");
            }
        } else if (PRODUCT_STATUS == PRODUCT_STATUS_DOWNLOAD) {
            mDownloadOrDeleteButton.setText(R.string.action_download);
        } else if (PRODUCT_STATUS == PRODUCT_STATUS_DOWNLOADING) {
            mDownloadOrDeleteButton.setText(R.string.action_downloading);
            mDownloadOrDeleteButton.setVisibility(View.GONE);
            processView.setVisibility(View.VISIBLE);
            return;
        } else if (PRODUCT_STATUS == PRODUCT_STATUS_DOWNLOADED) {
            mDownloadOrDeleteButton.setText(R.string.action_install);
        } else if (PRODUCT_STATUS == PRODUCT_STATUS_INSTALLED) {
            if(isSupportApply(mData.type)) {
                mDownloadOrDeleteButton.setText(R.string.action_apply);
            }else {
                mDownloadOrDeleteButton.setText(R.string.action_delete);
            }
        } else if (PRODUCT_STATUS == PRODUCT_STATUS_APPLIED) {
//            mDownloadOrDeleteButton.setText(R.string.action_delete);
            mDownloadOrDeleteButton.setText(R.string.wallpaper_state_applied);
        } else if (PRODUCT_STATUS == PRODUCT_STATUS_NEED_UPDATE) {
            mDownloadOrDeleteButton.setText(R.string.action_update);
        }
        processView.setVisibility(View.GONE);
        mDownloadOrDeleteButton.setVisibility(View.VISIBLE);
    }
    
    private boolean isSupportApply(String category) {
        if(Product.ProductType.THEME.equals(category)) {
            return true;
        }
        if(Product.ProductType.WALL_PAPER.equals(category)) {
            return true;
        }
        return false;
    }

    protected void refreshUI() {
        if(isDetached()) {
            BLog.d(TAG, "fragment is detached!");
            return;
        } 
        if (mData == null) {
            getProductDetail(true);
            return;
        }
        if (mAdapter == null) {
            mAdapter = new WallPapercreenShotAdapter(getFragmentManager(),mData);
            mPager.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
        BLog.d(TAG,"page count = "+mAdapter.getCount());
        refreshProductStatus(queryProductStatus(product_id));

        mPage.removeAllViews();
        for (int i = 0; i < mAdapter.getCount(); i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            ImageView dot = new ImageView(getActivity());
            if (i == 0) {
                dot.setBackgroundResource(R.drawable.indicator_focus);
            } else {
                dot.setBackgroundResource(R.drawable.indicator_normal);
            }
            mPage.addView(dot, params);
        }
    }

    private int queryProductStatus(String productID) {
    	final boolean notExist = wallPaperUnExist(productID);
    	if(notExist) {
    		return PRODUCT_STATUS_DOWNLOAD;
    	}
        Cursor themeCursor = downLoadHelper.queryTheme(productID);
        if (themeCursor != null && themeCursor.getCount() > 0) {
            themeCursor.moveToFirst();
            mData.current_version_name = themeCursor.getString(themeCursor.getColumnIndex(PlugInColumns.VERSION_NAME));
            mData.current_version_code = themeCursor.getInt(themeCursor.getColumnIndex(PlugInColumns.VERSION_CODE));
            boolean isApplyed = themeCursor.getInt(themeCursor.getColumnIndex(PlugInColumns.IS_APPLY)) == 1 ? true : false;
            themeCursor.close();
            if (mData.version_code > mData.current_version_code) {
                Cursor cursor = downLoadHelper.queryDownloadFile(productID);
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    int downStatus = cursor.getInt(cursor.getColumnIndex(DownloadInfoColumns.DOWNLOAD_STATUS));
                    if (downStatus == DownloadInfoColumns.DOWNLOAD_STATUS_DOWNLOADING) {
                        downLoadID = cursor.getLong(cursor.getColumnIndex(DownloadInfoColumns.DOWNLOAD_ID));
                        cursor.close();
                        return PRODUCT_STATUS_DOWNLOADING;
                    } 
                }
                return PRODUCT_STATUS_NEED_UPDATE;
            }
            if(isApplyed) {
                return PRODUCT_STATUS_APPLIED;
            }else {
                return PRODUCT_STATUS_INSTALLED;
            }
        }
        if (themeCursor != null)
            themeCursor.close();

        Cursor cursor = downLoadHelper.queryDownloadFile(productID);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            int downStatus = cursor.getInt(cursor.getColumnIndex(DownloadInfoColumns.DOWNLOAD_STATUS));
            int current_version_code = cursor.getInt(cursor.getColumnIndex(DownloadInfoColumns.VERSION_CODE));
            long download_size = cursor.getLong(cursor.getColumnIndex(DownloadInfoColumns.SIZE));
            if (downStatus == DownloadInfoColumns.DOWNLOAD_STATUS_DOWNLOADING) {
                downLoadID = cursor.getLong(cursor.getColumnIndex(DownloadInfoColumns.DOWNLOAD_ID));
                cursor.close();
                return PRODUCT_STATUS_DOWNLOADING;
            } else if (downStatus == DownloadInfoColumns.DOWNLOAD_STATUS_COMPLETED) {
                    String fileuri = cursor.getString(cursor
                            .getColumnIndex(DownloadInfoColumns.LOCAL_PATH));
                    File dFile = new File(URI.create(fileuri));
                    if (dFile.exists() && dFile.length() == download_size) {
                        if (mData != null)
                            mData.localUrl = fileuri;
                        if (mData.version_code > current_version_code) {
                            //服务器已经有新版本，本地缓存可以删除了
                            downLoadHelper.deleteFile(productID, version_code);
                            dFile.delete();
                            return PRODUCT_STATUS_DOWNLOAD;
                        } else {
                            return PRODUCT_STATUS_DOWNLOADED;
                        }
                    }
                    downLoadHelper.deleteFile(productID, version_code);
                    cursor.close();
//                }
            }
        }
        if(cursor != null) {
            cursor.close();
        }
        /*if (!notExist && PRODUCT_STATUS != PRODUCT_STATUS_DOWNLOADING) {
            return PRODUCT_STATUS_INSTALLED;
        } else */if (mData.purchased) {
            return PRODUCT_STATUS_DOWNLOAD;
        } else {
            return PRODUCT_STATUS_PURCHASE;
        }
    }

    void showConfirmDialog() {
        new AlertDialog.Builder(mActivity)
                .setTitle(R.string.action_delete)
                .setMessage(getString(R.string.delete_file_message))
                .setPositiveButton(getString(R.string.label_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int whichButton) {
//                                showIndeterminate(R.string.action_delete);
                                    IntentUtil.sendBroadCastDelete(
                                                    getActivity(),
                                                    mData.product_id);
                            }
                        })
                .setNegativeButton(getString(R.string.label_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int whichButton) {
                            }
                        }).create().show();

    }

    @Override
    public void onDestroyView() {
        QiupuHelper.unregisterDownloadListener(WallpaperProductDetailFragment.class.getName());
        unregisterContentObserver(plugInObserver);
        super.onDestroyView();
    }

    private boolean isLoading;
    private final Object mLocked = new Object();

    private void getProductDetail(boolean isVisbleLoadingUI) {
        if (isLoading) {
            BLog.v(TAG, "is Loading ...");
            return;
        }

        BLog.v(TAG, "begin getProductDetail");
        if(isVisbleLoadingUI) begin();
        if (!DataConnectionUtils.testValidConnection(mActivity)) {
//            Toast.makeText(mActivity, R.string.dlg_msg_no_active_connectivity,
//                    Toast.LENGTH_SHORT).show();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showLoadMessage(R.string.dlg_msg_no_active_connectivity, R.drawable.biz_pic_empty_view,true);
                }
            }, 500);
            return;
        }
        synchronized (mLocked) {
            isLoading = true;
        }

        mApiUtil = ApiUtil.getInstance();
        mApiUtil.getProductDetail(mActivity, product_id, version_code,supported_mod,
                new RequestListener() {

                    @Override
                    public void onIOException(IOException e) {
                        BLog.v(TAG,
                                "getProductDetail onIOException  "
                                        + e.getMessage());
                        synchronized (mLocked) {
                            isLoading = false;
                        }

                        Message mds = mHandler.obtainMessage(LOAD_END);
                        mds.getData().putBoolean(RESULT, false);
                        mHandler.sendMessage(mds);

                    }

                    @Override
                    public void onError(WutongException e) {
                        BLog.d(TAG, "onError  " + e.getMessage());
                        synchronized (mLocked) {
                            isLoading = false;
                        }

                        Message mds = mHandler.obtainMessage(LOAD_END);
                        mds.getData().putBoolean(RESULT, false);
                        mHandler.sendMessage(mds);

                    }

                    @Override
                    public void onComplete(String response) {
                        JSONObject obj;
                        try {
                            obj = new JSONObject(response);
                            if (!obj.has("data"))
                                return;
                            jsonString = obj.optJSONObject("data").toString();
                            mData = new ProductJSONImpl(obj
                                    .optJSONObject("data"));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        synchronized (mLocked) {
                            isLoading = false;
                        }

                        Message mds = mHandler.obtainMessage(LOAD_END);
                        mds.getData().putBoolean(RESULT, true);
                        mHandler.sendMessage(mds);

                    }
                });
    }

    private void purchase() {
        if (isLoading) {
            BLog.v(TAG, "is Loading ...");
            return;
        }
        
        if(!MarketUtils.hasSDcard()) {
            Toast.makeText(mActivity, R.string.msg_no_sdcard,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!DataConnectionUtils.testValidConnection(mActivity)) {
            Toast.makeText(mActivity, R.string.dlg_msg_no_active_connectivity,
                    Toast.LENGTH_SHORT).show();
//            showLoadMessage(R.string.dlg_msg_no_active_connectivity);
            return;
        }
        if(!AccountSession.isLogin() && !mData.isFree) {
            BasicActivity bac =  (BasicActivity)getActivity();
            bac.login();
            return;
        }
        synchronized (mLocked) {
            isLoading = true;
        }
        
        if(mData.isFree || mData.purchased) {
            onPayed(null);
        }else {
            doPaying();
        }
    }
    
    private void doPaying() {
        if(mActivity != null && BasicActivity.class.isInstance(mActivity)) {
            ((BasicActivity)mActivity).purchase(mData, new MarketPurchaseListener() {
                
                @Override
                public void onBillingFinished(boolean isSuccess, MarketBillingResult result) {
                    if(isSuccess) {
                        onPayed(result);
                    }else {
                        synchronized (mLocked) {
                            isLoading = false;
                        }

                        Message mds = mHandler.obtainMessage(PAYING_FAILED);
                        mds.getData().putString(MESSAGE, result.response);
                        mHandler.sendMessage(mds);
                    }
                }
            });
        }
    }

    private void onPayed(MarketBillingResult result) {
        mApiUtil = ApiUtil.getInstance();
        WutongParameters params = new WutongParameters();
        String originalJson = null;
        if(result != null) {
            if(result.billingType == result.TYPE_IAB) {
                params.add("google_iab_order", result.orderId);
                originalJson = result.originalJson;
            }else {
                params.add("cmcc_mm_order", result.orderId);
                params.add("cmcc_mm_trade", result.tradeId);
            }
        }
        mApiUtil.productPurchase(mActivity, product_id, version_code,params, originalJson,
                new RequestListener() {

                    @Override
                    public void onIOException(IOException e) {
                        BLog.v(TAG,
                                "productPurchase onIOException  "
                                        + e.getMessage());
                        synchronized (mLocked) {
                            isLoading = false;
                        }

                        Message mds = mHandler.obtainMessage(PURCHASE_END);
                        mds.getData().putBoolean(RESULT, false);
                        mHandler.sendMessage(mds);

                    }

                    @Override
                    public void onError(WutongException e) {
                        BLog.v(TAG,
                                "productPurchase onError  " + e.getMessage());
                        synchronized (mLocked) {
                            isLoading = false;
                        }

                        Message mds = mHandler.obtainMessage(PURCHASE_END);
                        mds.getData().putBoolean(RESULT, false);
                        mHandler.sendMessage(mds);

                    }

                    @Override
                    public void onComplete(String response) {
                        JSONObject obj;
                        String url = null;
                        try {
                            obj = new JSONObject(response);
                            if (!obj.has("data"))
                                return;
                            if (obj.optJSONObject("data") != null) {
                                url = obj.optJSONObject("data").optString("url");
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        synchronized (mLocked) {
                            isLoading = false;
                        }

                        Message mds = mHandler.obtainMessage(PURCHASE_END);
                        mds.getData().putBoolean(RESULT, true);
                        mds.getData().putString(URL, url);
                        mHandler.sendMessage(mds);

                    }
                });
    }

    @Override
    protected void createHandler() {
        mHandler = new MainHandler();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private final static int LOAD_END = 0;
    private final static int PURCHASE_END = 1;
    private final static int DELETE_END = 2;
    public static final int MSG_ON_CHANGE = 3;
    public static final int PAYING_FAILED = 5;
    private final static String RESULT = "RESULT";
    private final static String URL = "URL";
    private final static String MESSAGE = "MESSAGE";

    private class MainHandler extends Handler {
        public MainHandler() {
            super();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case LOAD_END: {
                end();
                if(getActivity() == null) return;
                boolean suc = msg.getData().getBoolean(RESULT);
                if (suc) {
                    refreshUI();
                } else {
                    showLoadMessage(R.string.msg_loadi_failed);
                }
                break;
            }
            case DELETE_END: {
                // end();
                dismissProgress();
                if(getActivity() == null) return;
                boolean suc = msg.getData().getBoolean(RESULT);
                if (suc) {
                    if (!TextUtils.isEmpty(mData.localUrl)) {
                        File f = new File(mData.localUrl);
                        f.delete();
                    }
                    downLoadHelper.deleteFile(product_id,
                            version_code);
                    refreshProductStatus(PRODUCT_STATUS_DOWNLOAD);
                } else {

                }
                break;
            }
            case MSG_ON_CHANGE: {
                if((PRODUCT_STATUS == PRODUCT_STATUS_DOWNLOADING) && downLoadID != -1) {
                    int[] bytesAndStatus = DownloadUtils.getBytesAndStatus(mActivity,downLoadID);
                    updateView(bytesAndStatus[0], bytesAndStatus[1], bytesAndStatus[2]);
                }
                break;
            }
            case PURCHASE_END: {
                if(getActivity() == null) return;
                boolean suc = msg.getData().getBoolean(RESULT);
                if (suc) {
                    if(mActivity != null && BasicActivity.class.isInstance(mActivity)) {
                        ((BasicActivity)mActivity).consumeAsync();
                    }
                    String url = msg.getData().getString(URL);
                    if (TextUtils.isEmpty(url)) {
                        BLog.e("url is null");
                    } else {
                        mData.purchased = true;
                        DownloadUtils downloadUtils = new DownloadUtils(mActivity.getApplicationContext());
                        downLoadID = downloadUtils.download(url, mData.name, mData.product_id);
                        if(downLoadID != -1) {
                            int[] bytesAndStatus = DownloadUtils.getBytesAndStatus(mActivity,downLoadID);
                            updateView(bytesAndStatus[0], bytesAndStatus[1], bytesAndStatus[2]);
                            refreshProductStatus(PRODUCT_STATUS_DOWNLOADING);
//                      
                            downLoadHelper.insert(mData, null, downLoadID, jsonString);
                            mDownloadOrDeleteButton.setText(R.string.action_downloading);
                        }else {
                            Toast.makeText(mActivity, R.string.download_failed,
                                  Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
//                    Toast.makeText(mActivity, R.string.purchase_failed,
//                            Toast.LENGTH_SHORT).show();

                }
                break;
            }
            case PAYING_FAILED: {
                if(getActivity() == null) return;
                String respone = msg.getData().getString(MESSAGE);
                Toast.makeText(mActivity, respone, Toast.LENGTH_LONG).show();
                refreshProductStatus(PRODUCT_STATUS_PURCHASE);
                break;
            }
            }
        }
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        if (id == R.id.delete_or_download) {
            if (PRODUCT_STATUS == PRODUCT_STATUS_PURCHASE
                    || PRODUCT_STATUS == PRODUCT_STATUS_DOWNLOAD) {
                purchase();
                StaticUtil.staticWallpaper(mContext, "WALLPAPER_DOWNLOAD");
//                StaticUtil.onEvent(mActivity, "WALLPAPER_DOWNLOAD", "wallpaper download");
            } else if (PRODUCT_STATUS == PRODUCT_STATUS_DOWNLOADING) {
                // TODO cancle download
            } else if (PRODUCT_STATUS == PRODUCT_STATUS_DOWNLOADED) {
                IntentUtil.sendBroadCastforInstall(mActivity, mData.localUrl,mData.product_id);
            } else if (PRODUCT_STATUS == PRODUCT_STATUS_INSTALLED) {
                if(isSupportApply(mData.type)) {
                    mDownloadOrDeleteButton.setText(R.string.action_wallpaper_setting);
                    IntentUtil.sendBroadCastforApplyed(mActivity, mData.localUrl,mData.product_id);
                }else {
                    //删除
                    showConfirmDialog();
                }
            } else if (PRODUCT_STATUS == PRODUCT_STATUS_NEED_UPDATE) {
                purchase();
                // TODO update version
            } else if (PRODUCT_STATUS == PRODUCT_STATUS_APPLIED) {
//                showConfirmDialog();
            }
        }
    }

    @Override
    public void downloadSuccess(String productId, String fileUri) {
        if (mData != null)
            mData.localUrl = fileUri;
        mDownloadOrDeleteButton.setText(R.string.action_downloaded);
        refreshProductStatus(PRODUCT_STATUS_DOWNLOADED);
    }

    @Override
    public void downloadFailed(String productId) {
        // TODO Auto-generated method stub
    }

    @Override
    protected int getInflatelayout() {
        return R.layout.wallpaper_detail_fragment;
    }

    class PlugInObserver extends ContentObserver {
        public PlugInObserver(Handler handler) {
            super(handler);
        }

        // 当监听到数据发生了变化就调用这个方法，并将新添加的数据查询出来
        public void onChange(boolean selfChange) {
            if(mData == null || downLoadHelper == null) return;
            Cursor cursor = downLoadHelper.queryTheme(mData.product_id);
            if (cursor.moveToFirst()) {
                int isApply = cursor.getInt(cursor
                        .getColumnIndex(PlugInColumns.IS_APPLY));
                if (isApply == 1) {
                    if (PRODUCT_STATUS_APPLIED != PRODUCT_STATUS) {
                        mActivity.setResult(Activity.RESULT_OK);
                        mActivity.finish();
                    }
                    refreshProductStatus(PRODUCT_STATUS_APPLIED);
                }else {
                    refreshProductStatus(PRODUCT_STATUS_INSTALLED);
                }
            } else {
                Message mds = mHandler.obtainMessage(DELETE_END);
                mds.getData().putBoolean(RESULT, true);
                mHandler.sendMessage(mds);
            }
        }
    }

    @Override
    public void onRefresh() {
        getProductDetail(true);
    }
    
    public class PhotoOnPageChangeListener implements OnPageChangeListener {

        @Override
        public void onPageSelected(int position) {
            for (int i = 0; i < mPage.getChildCount(); i++) {
                ImageView dot = (ImageView) mPage.getChildAt(i);
                if (i == position) {
                    dot.setBackgroundResource(R.drawable.indicator_focus);
                } else {
                    dot.setBackgroundResource(R.drawable.indicator_normal);
                }
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            // to refresh frameLayout
            if (pager_layout != null) {
                pager_layout.invalidate();
            }
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }

    @Override
    public void onLogin() {
       getProductDetail(false);
    }

    @Override
    public void onLogout() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onCancelLogin() {
        // TODO Auto-generated method stub
        
    }
    
    class DownloadOberserver extends ContentObserver {
        
        public DownloadOberserver() {
            super(mHandler);
        }

        @Override
        public void onChange(boolean selfChange) {
            mHandler.sendEmptyMessage(MSG_ON_CHANGE);
        }
        
    }

    public void updateView(int downLoadedBytes, int totalBytes, int status) {
        if (DownloadUtils.isDownloading(status)) {
            downloadProgress.setVisibility(View.VISIBLE);
            downloadProgress.setMax(0);
            downloadProgress.setProgress(0);
            downloadSize.setVisibility(View.VISIBLE);
            downloadPercent.setVisibility(View.VISIBLE);
            downloadCancel.setVisibility(View.VISIBLE);

            if (totalBytes < 0) {
                downloadProgress.setIndeterminate(true);
                downloadPercent.setText("0%");
                downloadSize.setText("0M/0M");
            } else {
                downloadProgress.setIndeterminate(false);
                downloadProgress.setMax(totalBytes);
                downloadProgress.setProgress(downLoadedBytes);
                downloadPercent.setText(DownloadUtils.getNotiPercent(downLoadedBytes, totalBytes));
                downloadSize.setText(DownloadUtils.getAppSize(downLoadedBytes) + "/" + DownloadUtils.getAppSize(totalBytes));
            }
        } else {
            downloadProgress.setMax(0);
            downloadProgress.setProgress(0);
            processView.setVisibility(View.GONE);

            if (status == DownloadManager.STATUS_FAILED) {
            } else if (status == DownloadManager.STATUS_SUCCESSFUL) {
            } else {
            }
        }
        
    }

    @Override
    public void onLoging() {
        // TODO Auto-generated method stub
        
    }
}
