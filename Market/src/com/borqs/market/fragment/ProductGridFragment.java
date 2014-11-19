package com.borqs.market.fragment;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.borqs.market.LocalImageManager;
import com.borqs.market.R;
import com.borqs.market.adapter.ProductGridAdapter;
import com.borqs.market.api.ApiUtil;
import com.borqs.market.json.Product;
import com.borqs.market.json.Product.ProductType;
import com.borqs.market.json.ProductJSONImpl;
import com.borqs.market.net.RequestListener;
import com.borqs.market.net.WutongException;
import com.borqs.market.utils.BLog;
import com.borqs.market.utils.DataConnectionUtils;
import com.borqs.market.utils.IntentUtil;

public class ProductGridFragment extends BasicFragment implements OnScrollListener {

    private static final String TAG = "ProductGridFragment";
    private static final String TAG_DATAS = "TAG_DATAS";
    private static final String TAG_PRODUCT_TYPE = "TAG_PRODUCT_TYPE";
    private static final String TAG_VERSION_CODE = "TAG_VERSION_CODE";
    private static final String TAG_PACKAGE_NAME = "TAG_PACKAGE_NAME";
    protected GridView mGridView;
    protected View layout_more;
    private ProductGridAdapter mAdapter;
    private ArrayList<Product> mDatas = new ArrayList<Product>();
    private ApiUtil mApiUtil;
    private int mPage = 0;
    private final int count = 10;
    private boolean hasMore = true;

    protected String product_type = ProductType.THEME;
    protected String packageName;
    protected int appVersionCode = 0;
    private LocalImageManager mLocalImageManager;

    public static Bundle getArguments(String product_type, int appVersionCode, String packageName) {
        Bundle args = new Bundle();
        args.putString(ARGUMENTS_KEY_TYPE, product_type);
        args.putInt(ARGUMENTS_KEY_VERSION, appVersionCode);
        args.putString(ARGUMENTS_KEY_PKG, packageName);
        return args;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        product_type = args.getString(ARGUMENTS_KEY_TYPE);
        appVersionCode = args.getInt(ARGUMENTS_KEY_VERSION);
        packageName = args.getString(ARGUMENTS_KEY_PKG);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        BLog.d(TAG, "onCreateView()");
        super.onCreateView(inflater, container, savedInstanceState);
        // mConvertView = inflater.inflate(R.layout.product_grid_layout,
        // container, false);
        // mLoadingLayout = mConvertView.findViewById(R.id.loading_layout);
        mGridView = (GridView) mConvertView.findViewById(R.id.grid_favorite);
        layout_more = mConvertView.findViewById(R.id.layout_more);
        mLocalImageManager = new LocalImageManager(new Handler());
        mLocalImageManager.setThreadNumber(3);
        mLocalImageManager.onResume();      
        mAdapter = new ProductGridAdapter(mActivity, mLocalImageManager, mDatas);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnScrollListener(this);
        return mConvertView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        BLog.d(TAG, "onActivityCreated()");
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mDatas = savedInstanceState.getParcelableArrayList(TAG_DATAS);
            this.appVersionCode = savedInstanceState.getInt(TAG_VERSION_CODE, 0);
            this.product_type = savedInstanceState.getString(TAG_PRODUCT_TYPE);
            this.packageName = savedInstanceState.getString(TAG_PACKAGE_NAME);

            BLog.d(TAG, "savedInstanceState != null versioncode=" + appVersionCode);
            BLog.d(TAG, "savedInstanceState != null product_type=" + product_type);
            BLog.d(TAG, "savedInstanceState != null packageName=" + packageName);
        }
        if (mDatas != null && mDatas.size() > 0) {
            mPage = (mDatas.size() - 1) / count;
            BLog.d(TAG, "notifyDataChange");
            notifyDataChange();
        } else if (hasMore) {
            BLog.d(TAG, "getProductList....");
            getProductList();
        }

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mDatas != null && mDatas.get(position) != null) {
                    IntentUtil.startProductDetailActivity(mActivity, mDatas.get(position).product_id,
                            mDatas.get(position).version_code, mDatas.get(position).name,
                            mDatas.get(position).supported_mod);
                }
            }

        });
    }

    public void onRefresh() {
        hasMore = true;
        mPage = 0;
        getProductList();
    }

    public void loadMore() {
        mPage++;
        getProductList();
    }

    private void notifyDataChange() {
        if (mAdapter == null) {
            if (mLocalImageManager != null) {
                mAdapter = new ProductGridAdapter(mActivity, mLocalImageManager, mDatas);
                mGridView.setAdapter(mAdapter);
            }
        } else {
            mAdapter.notifyDataSetChanged(mDatas);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(TAG_DATAS, mDatas);
        outState.putString(TAG_PRODUCT_TYPE, product_type);
        outState.putString(TAG_PACKAGE_NAME, packageName);
        outState.putInt(TAG_VERSION_CODE, appVersionCode);

        BLog.d(TAG, "onSaveInstanceState()   versioncode=" + appVersionCode);
        BLog.d(TAG, "onSaveInstanceState()   product_type=" + product_type);
        BLog.d(TAG, "onSaveInstanceState()   packageName=" + packageName);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        BLog.d(TAG, "onDestroyView()");
        mLocalImageManager.ondestory();
        super.onDestroyView();
    }

    private boolean isLoading;
    private final Object mLocked = new Object();

    private void getProductList() {
        if (isLoading) {
            BLog.v(TAG, "is Loading ...");
            return;
        }

        if (mPage == 0)
            begin();

        if (!DataConnectionUtils.testValidConnection(mActivity)) {
            if (mPage == 0) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showLoadMessage(R.string.dlg_msg_no_active_connectivity, R.drawable.biz_pic_empty_view, true);
                    }
                }, 500);
            } else {
                Toast.makeText(mActivity, R.string.dlg_msg_no_active_connectivity, Toast.LENGTH_SHORT).show();
            }
            return;
        }

        synchronized (mLocked) {
            isLoading = true;
        }

        BLog.v(TAG, "begin getProductList");
        // mApiUtil = ApiUtil.getInstance();
        mApiUtil = new ApiUtil();
        mApiUtil.getProductList(mActivity.getApplicationContext(), mPage, count, product_type, appVersionCode,
                packageName, null, null, new RequestListener() {

                    @Override
                    public void onIOException(IOException e) {
                        BLog.d(TAG, "getProductList onIOException  " + e.getMessage());
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
                            ArrayList<Product> productList = ProductJSONImpl.createProductList(obj.optJSONArray("data"));

                            BLog.v(TAG, "finish getProductList=" + productList.size());

                            if (mPage == 0 && mDatas != null) {
                                mDatas.clear();
                            }
                            if (productList != null && productList.size() > 0) {
                                mDatas.addAll(productList);
                            } else {
                                hasMore = false;
                            }
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
        mApiUtil = null;
        System.gc();
    }

    private final static int LOAD_END = 0;
    private final static String RESULT = "RESULT";

    private class MainHandler extends Handler {
        public MainHandler() {
            super();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

            case LOAD_END: {
                mPage = (mDatas.size() - 1) / count;
                end();
                boolean suc = msg.getData().getBoolean(RESULT);
                if (suc) {
                    notifyDataChange();
                } else {
                    showLoadMessage(R.string.msg_loadi_failed);
                }
                break;
            }
            }
        }
    }

    @Override
    protected int getInflatelayout() {
        return R.layout.product_grid_layout;
    }

    // int mLastItemtPosition = 0;
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // if(scrollState == OnScrollListener.SCROLL_STATE_IDLE
        // && view.on) {
        //
        // }

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (mDatas == null || mDatas.size() == 0) {
            layout_more.setVisibility(View.GONE);
            return;
        }
        // mLastItemtPosition = firstVisibleItem + visibleItemCount -1;
        if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount % count == 0 && hasMore) {
            layout_more.setVisibility(View.VISIBLE);
            loadMore();
        } else {
            layout_more.setVisibility(View.GONE);
        }

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

    @Override
    public void onLoging() {
        // TODO Auto-generated method stub

    }

}
