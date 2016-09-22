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
import android.graphics.Typeface;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.borqs.market.BasicActivity;
import com.borqs.market.R;
import com.borqs.market.account.AccountSession;
import com.borqs.market.adapter.ScreenShotAdapter;
import com.borqs.market.api.ApiUtil;
import com.borqs.market.db.DownLoadHelper;
import com.borqs.market.db.DownLoadProvider;
import com.borqs.market.db.DownloadInfoColumns;
import com.borqs.market.db.PlugInColumns;
import com.borqs.market.json.Product;
import com.borqs.market.json.ProductJSONImpl;
import com.borqs.market.listener.DownloadListener;
import com.borqs.market.listener.ViewListener;
import com.borqs.market.net.RequestListener;
import com.borqs.market.net.WutongException;
import com.borqs.market.net.WutongParameters;
import com.borqs.market.utils.BLog;
import com.borqs.market.utils.DataConnectionUtils;
import com.borqs.market.utils.DownloadUtils;
import com.borqs.market.utils.IntentUtil;
import com.borqs.market.utils.MD5;
import com.borqs.market.utils.MarketConfiguration;
import com.borqs.market.utils.MarketUtils;
import com.borqs.market.utils.QiupuHelper;
import com.iab.engine.MarketBillingResult;
import com.iab.engine.MarketPurchaseListener;

public class ProductBasicDetailFragment extends BasicFragment implements
        OnClickListener, DownloadListener {

    private static final String TAG = "ProductBasicDetailFragment";
    protected static int     TOTAL_COUNT = 3;
    private static final String BOTTOM_LAYOUT_VISABLE = "BOTTOM_LAYOUT_VISABLE";

    protected View pager_layout;
    private ScreenShotAdapter mAdapter;
    protected Product mData = null;
    private ApiUtil mApiUtil;
    private boolean isBottomVisable = true;

    protected ViewPager mPager;
    protected LinearLayout mPage;
    protected LinearLayout mOpertionGroupView;
    protected View mDownloadOrDeleteLayout;
    protected Button btn_download;
    protected ImageView btn_delete;
    private ProgressBar downloadProgress;
    private TextView downloadSize;
    private TextView tv_comment;
    private TextView downloadPrecent;
    private View downloadCancel;
    private View processView;
    private View content_container;
    private View bottomLayout;
    
    protected DownloadManager dm;
    protected DownLoadHelper downLoadHelper;
    private String product_id = null;
    private int version_code = 0;
    private PlugInObserver plugInObserver;
    private DownloadOberserver downLoadObserver;
    protected long downLoadID = -1;
    private String jsonString = null;
    protected String supported_mod;
    protected int current_position = 0;

    protected static final int PRODUCT_STATUS_PURCHASE = 0;
    protected static final int PRODUCT_STATUS_DOWNLOAD = 1;
    protected static final int PRODUCT_STATUS_DOWNLOADING = 2;
    protected static final int PRODUCT_STATUS_DOWNLOADED = 3;
    protected static final int PRODUCT_STATUS_NEED_UPDATE = 4;
    protected static final int PRODUCT_STATUS_INSTALLED = 5;
    protected static final int PRODUCT_STATUS_APPLYED = 6;
    protected int PRODUCT_STATUS = PRODUCT_STATUS_PURCHASE;
    protected static final String FLAG_DATA = "FLAG_DATA";
    private boolean mIsFristLoad = true;
    private boolean isBelongSystem = false;
    
    public static Bundle getArguments(String supported_mod) {
        Bundle args = new Bundle();
        args.putString(ARGUMENTS_KEY_MOD, supported_mod);
        return args;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        supported_mod = args.getString(ARGUMENTS_KEY_MOD,Product.SupportedMod.PORTRAIT);
    }

    String action_view_comments;
    Typeface fontFace;
    @Override
    protected void initView() {
        action_view_comments = mContext.getString(R.string.action_view_comments);
        fontFace = Typeface.createFromAsset(mContext.getAssets(),"fonts/Roboto-Light.ttf");
        pager_layout = mConvertView.findViewById(R.id.pager_layout);
        mPager = (ViewPager) mConvertView.findViewById(R.id.mPager);
        mPager.setOffscreenPageLimit(TOTAL_COUNT);
//        mPager.setPageMargin(20);
        mOpertionGroupView = (LinearLayout) mConvertView.findViewById(R.id.opertion_group_view);
        mPage = (LinearLayout) mConvertView.findViewById(R.id.pages);
        mDownloadOrDeleteLayout = mConvertView.findViewById(R.id.delete_or_download);
        btn_download = (Button) mConvertView.findViewById(R.id.btn_download);
        btn_delete = (ImageView) mConvertView.findViewById(R.id.btn_delete);
        btn_download.setOnClickListener(this);
        btn_delete.setOnClickListener(this);

        downloadCancel = mConvertView.findViewById(R.id.download_cancel);
        downloadProgress = (ProgressBar)mConvertView.findViewById(R.id.download_progress);
        downloadSize = (TextView)mConvertView.findViewById(R.id.download_size);
        downloadPrecent = (TextView)mConvertView.findViewById(R.id.download_precent);
        processView = mConvertView.findViewById(R.id.process_view);
        content_container = mConvertView.findViewById(R.id.content_container);
        bottomLayout = mConvertView.findViewById(R.id.bottom);
        
        tv_comment = ((TextView)mConvertView.findViewById(R.id.tv_comment));
        tv_comment.setTypeface(fontFace);
        btn_download.setTypeface(fontFace);
        downloadSize.setTypeface(fontFace);
        downloadPrecent.setTypeface(fontFace);
        
        PhotoOnPageChangeListener pageChangeListener = new PhotoOnPageChangeListener();
        mPager.setOnPageChangeListener(pageChangeListener);

        pager_layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // dispatch the events to the ViewPager, to solve the problem that we can swipe only the middle view.
                return mPager.dispatchTouchEvent(event);
            }
        });
        downloadCancel.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if((PRODUCT_STATUS == PRODUCT_STATUS_DOWNLOADING) && downLoadID != -1) {
                    DownloadUtils.cancleDownload(mActivity, downLoadID,mData.product_id);
                    downLoadHelper.cancleDownloadFile(mData.product_id);
                    refreshProductStatus(queryProductStatus(mData.product_id));
                }
                
            }
        });
        String commentStr = String.format(action_view_comments, String.valueOf(mData== null? 0 :mData.comment_count));
        tv_comment.setText(commentStr);
        tv_comment.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                IntentUtil.startCommentListIntent(getActivity(), mData.product_id, mData.rating, mData.version_code);
                
            }
        });
    }
    
    @Override
    protected void begin() {
        super.begin();
        content_container.setVisibility(View.GONE);
    }
    
    @Override
    protected void end() {
        super.end();
        content_container.setVisibility(View.VISIBLE);
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        parseIntent(activity.getIntent());
        downLoadHelper = DownLoadHelper.getInstance(activity.getApplicationContext());
        dm = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    protected void parseIntent(Intent intent) {
        product_id = intent.getStringExtra(IntentUtil.EXTRA_KEY_ID);
        version_code = intent.getIntExtra(IntentUtil.EXTRA_KEY_VERSION,0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if(loading_layout != null) loading_layout.setBackgroundResource(R.color.transparent);
        plugInObserver = new PlugInObserver(new Handler());
        registerContentObserver(DownLoadProvider.getContentURI(mActivity.getApplicationContext(), DownLoadProvider.TABLE_PLUGIN), true, plugInObserver);
        QiupuHelper.registerDownloadListener(ProductBasicDetailFragment.class.getName(), this);

        if (savedInstanceState != null) {
            mData = savedInstanceState.getParcelable(FLAG_DATA);
            current_position = savedInstanceState.getInt("current_position");
            isBottomVisable = savedInstanceState.getBoolean(BOTTOM_LAYOUT_VISABLE);
            if(isBottomVisable) {
                bottomLayout.setVisibility(View.VISIBLE);
                ((ViewListener)getActivity()).show(false);
            }else {
                bottomLayout.setVisibility(View.GONE);
                ((ViewListener)getActivity()).hide(false);
            }
        }else {
            mData = DownLoadHelper.queryLocalProductInfo(mActivity.getApplicationContext(), product_id);
        }
        
        if(MarketConfiguration.getSystemThemeDir() != null) {
            String dir = MarketConfiguration.getSystemThemeDir().getPath();
            isBelongSystem = mData != null && !TextUtils.isEmpty(mData.installed_file_path) && mData.installed_file_path.startsWith(dir);
        }
        
        if(mData != null) {
            isBelongSystem = Product.isBelongSystem(mData.installed_file_path);
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
        	if(mIsFristLoad) {
        		getProductDetail(false);
        	}
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
    
    protected void refreshProductStatus(int status) {
        PRODUCT_STATUS = status;
        btn_download.setEnabled(true);
        if(isBelongSystem) {
            btn_delete.setVisibility(View.GONE);
        }else {
            btn_delete.setVisibility(View.VISIBLE);
        }
        if (PRODUCT_STATUS == PRODUCT_STATUS_PURCHASE) {
            if (mData.isFree) {
                btn_download.setText(R.string.action_download);
            } else {
                btn_download.setText(getString(R.string.action_purchase) + "(" + mData.price + ")");
            }
        } else if (PRODUCT_STATUS == PRODUCT_STATUS_DOWNLOAD) {
            btn_download.setText(R.string.action_download);
        } else if (PRODUCT_STATUS == PRODUCT_STATUS_DOWNLOADING) {
            btn_download.setText(R.string.action_downloading);
            mDownloadOrDeleteLayout.setVisibility(View.GONE);
            processView.setVisibility(View.VISIBLE);
            int[] bytesAndStatus = DownloadUtils.getBytesAndStatus(mActivity,downLoadID);
            updateView(bytesAndStatus[0], (int)mData.size, bytesAndStatus[2]);
            return;
        } else if (PRODUCT_STATUS == PRODUCT_STATUS_DOWNLOADED) {
            btn_download.setEnabled(false);
            btn_download.setText(R.string.action_install);
        } else if (PRODUCT_STATUS == PRODUCT_STATUS_INSTALLED) {
            if(Product.isSupportApply(mData.type)) {
                btn_download.setText(R.string.action_apply);
            }else {
                btn_download.setText(R.string.action_downloaded);
                btn_download.setEnabled(false);
            }
        } else if (PRODUCT_STATUS == PRODUCT_STATUS_APPLYED) {
            btn_download.setText(R.string.state_applied);
            btn_download.setEnabled(false);
        } else if (PRODUCT_STATUS == PRODUCT_STATUS_NEED_UPDATE) {
            btn_download.setText(R.string.action_update);
        }
        processView.setVisibility(View.GONE);
        mDownloadOrDeleteLayout.setVisibility(View.VISIBLE);
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
            mAdapter = new ScreenShotAdapter(getChildFragmentManager(),mData);
            mPager.setAdapter(mAdapter);
        } else {
            mAdapter.alertData(mData);
        }
        BLog.d(TAG,"page count = "+mAdapter.getCount());
        refreshProductStatus(queryProductStatus(product_id));

        mPage.removeAllViews();
        for (int i = 0; i < mAdapter.getCount(); i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            ImageView dot = new ImageView(getActivity());
            if (i == mPager.getCurrentItem()) {
                dot.setBackgroundResource(R.drawable.indicator_focus);
            } else {
                dot.setBackgroundResource(R.drawable.indicator_normal);
            }
            mPage.addView(dot, params);
        }
        String commentStr = String.format(action_view_comments, String.valueOf(mData== null? 0 :mData.comment_count));
        tv_comment.setText(commentStr);
    }

    protected int queryProductStatus(String productID) {
        Cursor themeCursor = downLoadHelper.queryTheme(productID);
        if (themeCursor != null && themeCursor.getCount() > 0) {
            themeCursor.moveToFirst();
            mData.current_version_name = themeCursor.getString(themeCursor.getColumnIndex(PlugInColumns.VERSION_NAME));
            mData.current_version_code = themeCursor.getInt(themeCursor.getColumnIndex(PlugInColumns.VERSION_CODE));
            boolean isApplyed = themeCursor.getInt(themeCursor.getColumnIndex(PlugInColumns.IS_APPLY)) == 1;
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
                return PRODUCT_STATUS_APPLYED;
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
//                                } else {
//                                    return PRODUCT_STATUS_DOWNLOADED;
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
        if (mData.purchased) {
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
                                    getActivity().finish();
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
        QiupuHelper.unregisterDownloadListener(ProductBasicDetailFragment.class.getName());
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
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                	if(mDownloadOrDeleteButton != null) {
//                		mDownloadOrDeleteButton.setVisibility(View.GONE);
//                	}
//                    showLoadMessage(R.string.dlg_msg_no_active_connectivity, R.drawable.biz_pic_empty_view,true);
//                }
//            }, 500);
            return;
        }
        synchronized (mLocked) {
            isLoading = true;
        }

        mApiUtil = ApiUtil.getInstance();
        mApiUtil.getProductDetail(mActivity, product_id, mData==null? version_code : mData.version_code, supported_mod,
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

    public void purchase() {
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
        
        if(mData.isFree || mData.purchased) {
            synchronized (mLocked) {
                isLoading = true;
            }
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
                        Message mds = mHandler.obtainMessage(PAYING_FAILED);
                        mds.getData().putString(MESSAGE, result.response);
                        mHandler.sendMessage(mds);
                    }
                }
            });
        }
    }

    private void onPayed(MarketBillingResult result) {
        refreshProductStatus(PRODUCT_STATUS_DOWNLOADING);
        mApiUtil = ApiUtil.getInstance();
        WutongParameters params = new WutongParameters();
        String originalJson = null;
        if(result != null) {
            if(result.billingType == MarketBillingResult.TYPE_IAB) {
                params.add("google_iab_order", result.orderId);
                originalJson = result.originalJson;
            }else {
                params.add("cmcc_mm_order", result.orderId);
                params.add("cmcc_mm_trade", result.tradeId);
            }
        }
        mApiUtil.productPurchase(mActivity, product_id, mData==null? version_code : mData.version_code, params, originalJson,
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
                mIsFristLoad = false;
                if(getActivity() == null) return;
                boolean suc = msg.getData().getBoolean(RESULT);
                if (suc) {
                    refreshUI();
//                } else {
//                    showLoadMessage(R.string.msg_loadi_failed);
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
//                            int[] bytesAndStatus = DownloadUtils.getBytesAndStatus(mActivity,downLoadID);
//                            updateView(bytesAndStatus[0], bytesAndStatus[1], bytesAndStatus[2]);
                            refreshProductStatus(PRODUCT_STATUS_DOWNLOADING);
//                      
                            downLoadHelper.insert(mData, null, downLoadID, jsonString);
                            btn_download.setText(R.string.action_downloading);
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
        if (id == R.id.btn_download) {
            if (PRODUCT_STATUS == PRODUCT_STATUS_PURCHASE
                    || PRODUCT_STATUS == PRODUCT_STATUS_DOWNLOAD) {
                purchase();
            } else if (PRODUCT_STATUS == PRODUCT_STATUS_DOWNLOADING) {
                // TODO cancle download
//            } else if (PRODUCT_STATUS == PRODUCT_STATUS_DOWNLOADED) {
//                IntentUtil.sendBroadCastforInstall(mActivity, mData.localUrl,mData.product_id);
            } else if (PRODUCT_STATUS == PRODUCT_STATUS_INSTALLED) {
                if(Product.isSupportApply(mData.type)) {
                    IntentUtil.sendBroadCastforApplyed(mActivity, mData.localUrl,mData.product_id);
//                }else {
//                    //删除
//                    showConfirmDialog();
                }
            } else if (PRODUCT_STATUS == PRODUCT_STATUS_NEED_UPDATE) {
                purchase();
                // TODO update version
            } else if (PRODUCT_STATUS == PRODUCT_STATUS_APPLYED) {
//                showConfirmDialog();
            }
        }else if (id == R.id.btn_delete){
            //删除
            showConfirmDialog();
        }
    }

    @Override
    public void downloadSuccess(String productID, String fileUri) {
        if (mData != null && productID.equals(mData.product_id))
            mData.localUrl = fileUri;
        btn_download.setText(R.string.action_downloaded);
        refreshProductStatus(PRODUCT_STATUS_DOWNLOADED);
    }

    @Override
    public void downloadFailed(String productID) {
        // TODO Auto-generated method stub
    }

    @Override
    protected int getInflatelayout() {
        return R.layout.product_detail_fragment;
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
//                    refreshProductStatus(PRODUCT_STATUS_APPLYED);
                    if(getActivity() != null) {
                        getActivity().finish();
                    }
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
            if(getActivity() != null && ViewListener.class.isInstance(getActivity())) {
                if(bottomLayout != null) {
                    if(position == 0) {
                        ((ViewListener)getActivity()).show(true);
                        showBottom();
                    }else if(position == 1 && current_position == 0) {
                        ((ViewListener)getActivity()).hide(true);
                        hideBottom();
                    }
                }
            }
            for (int i = 0; i < mPage.getChildCount(); i++) {
                ImageView dot = (ImageView) mPage.getChildAt(i);
                if (i == position) {
                    dot.setBackgroundResource(R.drawable.indicator_focus);
                } else {
                    dot.setBackgroundResource(R.drawable.indicator_normal);
                }
            }
            
            current_position = position;
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
    
    public void hideBottom() {
        if(bottomLayout.getVisibility() == View.VISIBLE) {
            Animation am1 = AnimationUtils.loadAnimation(getActivity(),R.anim.slide_out_down_self);
            bottomLayout.startAnimation(am1);
            bottomLayout.setVisibility(View.GONE);
        }
    }
    public void showBottom() {
        if(bottomLayout.getVisibility() != View.VISIBLE) {
            Animation am1 = AnimationUtils.loadAnimation(getActivity(),R.anim.slide_in_up_self);
            bottomLayout.startAnimation(am1);
            bottomLayout.setVisibility(View.VISIBLE);
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
            downloadPrecent.setVisibility(View.VISIBLE);
            downloadCancel.setVisibility(View.VISIBLE);

            if (totalBytes < 0) {
                downloadProgress.setIndeterminate(true);
                downloadPrecent.setText("0%");
                downloadSize.setText("0M/" + DownloadUtils.getAppSize(totalBytes));
            } else {
                downloadProgress.setIndeterminate(false);
                downloadProgress.setMax(totalBytes);
                downloadProgress.setProgress(downLoadedBytes);
                downloadPrecent.setText(DownloadUtils.getNotiPercent(downLoadedBytes, totalBytes));
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
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(FLAG_DATA, mData);
        outState.putInt("current_position", current_position);
        outState.putBoolean(BOTTOM_LAYOUT_VISABLE, bottomLayout.getVisibility() == View.VISIBLE);
        super.onSaveInstanceState(outState);
    }
}
