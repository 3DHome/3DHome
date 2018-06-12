package com.borqs.market.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.res.Configuration;
import android.database.ContentObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.borqs.market.LocalImageManager;
import com.borqs.market.R;
import com.borqs.market.adapter.ProductListAdapter;
import com.borqs.market.db.DownLoadHelper;
import com.borqs.market.db.DownLoadProvider;
import com.borqs.market.json.Product;
import com.borqs.market.json.Product.ProductType;
import com.borqs.market.json.Product.SupportedMod;
import com.borqs.market.listener.DownloadListener;
import com.borqs.market.utils.BLog;
import com.borqs.market.utils.MarketConfiguration;
import com.borqs.market.utils.QiupuHelper;

public class ProductLocalListFragment extends BasicFragment implements DownloadListener {

    private static final String TAG = ProductLocalListFragment.class.getSimpleName();
    private static final String TAG_DATAS = "TAG_DATAS";
    private static final String TAG_PRODUCT_TYPE = "TAG_PRODUCT_TYPE";
    private LayoutInflater mLayoutInflater;
    protected FrameLayout products_layout;
    protected ListView mListView;
    private View layout_tips;
    private ProductListAdapter mAdapter;
    private ArrayList<Product> mDatas = new ArrayList<Product>();
    private AsyncTask<Void, Void, List<Product>> mScanLocalProductTask;

    protected String product_type = ProductType.THEME;
    protected String supported_mod;
    boolean isportrait = false;
    boolean hasShowTipsMap = false;
    boolean hasShowTips = false;
    private LocalImageManager mLocalImageManager;

    public static Bundle getArguments(String product_type, String supported_mod) {
        Bundle args = new Bundle();
        args.putString(ARGUMENTS_KEY_TYPE, product_type);
        args.putString(ARGUMENTS_KEY_MOD, supported_mod);
        return args;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        product_type = args.getString(ARGUMENTS_KEY_TYPE);
        supported_mod = args.getString(ARGUMENTS_KEY_MOD);
        if (!TextUtils.isEmpty(supported_mod)) {
            if (supported_mod.contains(SupportedMod.PORTRAIT)) {
                isportrait = true;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mLocalImageManager = new LocalImageManager(new Handler());
        mLocalImageManager.setThreadNumber(3);
        mLocalImageManager.onResume();
        mLayoutInflater = inflater;
        QiupuHelper.registerDownloadListener(downloadListener_key, this);
        plugInObserver = new PlugsInObserver();
        registerContentObserver(
                DownLoadProvider.getContentURI(getActivity().getApplicationContext(), DownLoadProvider.TABLE_PLUGIN),
                true, plugInObserver);

        mListView = (ListView) mConvertView.findViewById(R.id.listview);

        products_layout = (FrameLayout) mConvertView.findViewById(R.id.products_layout);
        if (!Product.isSupportApply(product_type)) {
            products_layout.setBackgroundResource(R.drawable.local_fragment_bg_repeat);
        }
        mAdapter = new ProductListAdapter(mActivity,mLocalImageManager, mDatas, false, isportrait);
        mListView.setAdapter(mAdapter);
        return mConvertView;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged(mDatas);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        BLog.d(TAG, "onActivityCreated()");
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mDatas = savedInstanceState.getParcelableArrayList(TAG_DATAS);
            this.product_type = savedInstanceState.getString(TAG_PRODUCT_TYPE);
        }
        if (mDatas != null && mDatas.size() > 0) {
            notifyDataChange();
        } else {
            getProductList();
        }

    }

    public void onRefresh() {
        getProductList();
    }

    private void notifyDataChange() {
        if (mAdapter == null) {
            mAdapter = new ProductListAdapter(mActivity, mLocalImageManager, mDatas, false, isportrait);
            mListView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged(mDatas);
        }
        showTips(500);
    }

    /**
     * 显示提示：“长按物体拖动到房间”
     */
    private void showTips(long delayMillis) {
        // 显示大图片提示
        if (ProductType.OBJECT.equals(product_type) && !hasShowTipsMap
                && !MarketConfiguration.hasShowedObjectUserGuide(mContext)) {
            final View guideView = mLayoutInflater.inflate(R.layout.object_user_guide, null);
            guideView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));
            products_layout.addView(guideView);
            guideView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    products_layout.removeView(guideView);
                    MarketConfiguration.setShowObjectUserGuide(mContext, true);
                }
            });
        }
        hasShowTipsMap = true;
        if (ProductType.OBJECT.equals(product_type) && !hasShowTips
                && !MarketConfiguration.hasShowedObjectUserTips(mContext)) {
            if (layout_tips == null) {
                layout_tips = mConvertView.findViewById(R.id.layout_tips);
                View layout_tips_close = mConvertView.findViewById(R.id.tv_close);
                layout_tips_close.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        hideTips(0);
                        MarketConfiguration.setShowObjectUserTips(mContext, true);
                    }
                });
            }
            layout_tips.postDelayed(new Runnable() {

                @Override
                public void run() {
                    if (getActivity() != null && !getActivity().isFinishing()) {
                        Animation am = AnimationUtils.loadAnimation(getActivity().getBaseContext(),
                                android.R.anim.fade_in);
                        layout_tips.startAnimation(am);
                        layout_tips.setVisibility(View.VISIBLE);
                    }
                }
            }, delayMillis);
            hasShowTips = true;
        }
    }

    /**
     * 隐藏提示：“长按物体拖动到房间”
     */
    private void hideTips(long delayMillis) {
        layout_tips.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (getActivity() != null && !getActivity().isFinishing()) {
                    Animation am = AnimationUtils
                            .loadAnimation(getActivity().getBaseContext(), android.R.anim.fade_out);
                    layout_tips.startAnimation(am);
                    layout_tips.setVisibility(View.GONE);
                }
            }
        }, delayMillis);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(TAG_DATAS, mDatas);
        outState.putString(TAG_PRODUCT_TYPE, product_type);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        mLocalImageManager.ondestory();
        QiupuHelper.unregisterDownloadListener(downloadListener_key);
        if (plugInObserver != null) {
            unregisterContentObserver(plugInObserver);
            plugInObserver = null;
        }
        super.onDestroyView();
    }

    private boolean isLoading;
    private final Object mLocked = new Object();
    private List<Product> mTmpData;
    private void getProductList() {
        if (isLoading) {
            BLog.v(TAG, "is Loading ...");
            return;
        }

        begin();

        synchronized (mLocked) {
            isLoading = true;
        }

        BLog.v(TAG, "begin getLocalProductList");
        mScanLocalProductTask = new AsyncTask<Void, Void, List<Product>>() {
            @Override
            protected List<Product> doInBackground(Void... params) {
                return DownLoadHelper.queryLocalProductList(mActivity.getApplicationContext(), supported_mod,
                        product_type);
            }

            @Override
            protected void onPostExecute(List<Product> productList) {
                synchronized (mLocked) {
                    isLoading = false;
                }
                mTmpData = productList;
                Message mds = mHandler.obtainMessage(LOAD_END);

                mHandler.sendMessage(mds);
            }
        };
        mScanLocalProductTask.execute(null, null, null);

    }

    @Override
    protected void createHandler() {
        mHandler = new MainHandler();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mDatas != null && mDatas.size() > 0) {
            for (int i = 0; i < mDatas.size(); i++) {
                mDatas.get(i).despose();
            }
            mDatas.clear();
        }
        mAdapter = null;
        if (mScanLocalProductTask != null) {
            mScanLocalProductTask.cancel(true);
        }
        System.gc();
    }

    private final static int LOAD_END = 1;
    private final static int MSG_ON_CHANGE = 2;

    private class MainHandler extends Handler {
        public MainHandler() {
            super();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case LOAD_END:
                end();
                mDatas.clear();
                if (mTmpData != null) {
                    mDatas.addAll(mTmpData);
                    mTmpData = null;
                }
                notifyDataChange();
                if (mDatas.size() == 0) {
                    showLoadMessage(R.string.empty_list, false);
                }
                break;
            case MSG_ON_CHANGE:
                getProductList();
                break;
            }
        }
    }

    @Override
    protected int getInflatelayout() {
        return R.layout.product_list_fragment;
    }

    @Override
    public void onLogin() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onLogout() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onCancelLogin() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void initView() {
        // TODO Auto-generated method stub

    }

    private final String downloadListener_key = TAG + product_type;

    private PlugsInObserver plugInObserver;

    class PlugsInObserver extends ContentObserver {
        public PlugsInObserver() {
            super(mHandler);
        }

        // 当监听到数据发生了变化就调用这个方法，并将新添加的数据查询出来
        public void onChange(boolean selfChange) {
            mHandler.sendEmptyMessage(MSG_ON_CHANGE);
        }
    }

    @Override
    public void downloadSuccess(String productID, String fileUri) {
        // TODO Auto-generated method stub

    }

    @Override
    public void downloadFailed(String productID) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onLoging() {
        // TODO Auto-generated method stub

    }
}
