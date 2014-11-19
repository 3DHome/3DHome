package com.borqs.market.fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.borqs.market.R;
import com.borqs.market.adapter.RecommendPhotoAdapter;
import com.borqs.market.api.ApiUtil;
import com.borqs.market.json.Product;
import com.borqs.market.json.ProductJSONImpl;
import com.borqs.market.json.Recommend;
import com.borqs.market.json.RecommendJSONImpl;
import com.borqs.market.net.RequestListener;
import com.borqs.market.net.WutongException;
import com.borqs.market.utils.BLog;
import com.borqs.market.utils.MarketConfiguration;
import com.borqs.market.view.ViewPagerInListView;

public class ProductListFragment extends ProductBasicListFragment {

    private static final String TAG = ProductListFragment.class.getSimpleName();
    protected View recommend_header_view;
    protected View recommend_container;
    protected ViewPagerInListView mPager;
    protected LinearLayout mPagesNumber;
    protected TextView tv_recommend_name;
    private RecommendPhotoAdapter recommendPhotoAdapter;
    private ArrayList<Recommend> mRecommends = new ArrayList<Recommend>();

    private final String TAG_RECOMMEND_DATAS = "TAG_RECOMMEND_DATAS";

    @Override
    protected void initHeaderView(LayoutInflater inflater) {
        super.initHeaderView(inflater);

        recommend_header_view = inflater.inflate(R.layout.recommend_header_view, null);
        recommend_container = recommend_header_view.findViewById(R.id.recommend_container);
        mPager = (ViewPagerInListView) recommend_header_view.findViewById(R.id.mPager);
        mPagesNumber = (LinearLayout) recommend_header_view.findViewById(R.id.pages_number);
        tv_recommend_name = (TextView) recommend_header_view.findViewById(R.id.tv_recommend_name);
        PhotoOnPageChangeListener pageChangeListener = new PhotoOnPageChangeListener();
        mPager.setOnPageChangeListener(pageChangeListener);
        mPagesNumber.removeAllViews();
        recommendPhotoAdapter = new RecommendPhotoAdapter(getChildFragmentManager(), mRecommends, supported_mod,
                product_type);
        mPager.setAdapter(recommendPhotoAdapter);
        if (mRecommends != null && mRecommends.size() > 0) {
            if (mRecommends.get(0).type == Recommend.TYPE_USER_SHARE) {
                tv_recommend_name.setText(R.string.tab_user_share);
            } else {
                tv_recommend_name.setText(mRecommends.get(0).name);
            }
        }
        for (int i = 0; i < recommendPhotoAdapter.getCount(); i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            ImageView dot = new ImageView(getActivity());
            if (i == mPager.getCurrentItem()) {
                dot.setBackgroundResource(R.drawable.indicator_focus);
            } else {
                dot.setBackgroundResource(R.drawable.indicator_normal);
            }
            mPagesNumber.addView(dot, params);
        }
        initRecommendContainer();
    }

    private void initRecommendContainer() {
        int width = mPager.getResources().getDisplayMetrics().widthPixels;
        int height = mPager.getResources().getDisplayMetrics().heightPixels;
        mPager.getLayoutParams().height = width > height ? height / 2 : width / 2;
        if (mRecommends != null && mRecommends.size() > 0) {
            recommend_container.setVisibility(View.VISIBLE);
            if (mListView.getHeaderViewsCount() == 0) {
                mListView.addHeaderView(recommend_header_view);
            }
        } else {
            recommend_container.setVisibility(View.GONE);
            if (mListView.getHeaderViewsCount() > 0) {
                mListView.removeHeaderView(recommend_header_view);
            }
        }
    }

    public class PhotoOnPageChangeListener implements OnPageChangeListener {

        @Override
        public void onPageSelected(int position) {
            if (mRecommends != null && mRecommends.size() > 0) {
                if (mRecommends.get(position).type == Recommend.TYPE_USER_SHARE) {
                    tv_recommend_name.setText(R.string.tab_user_share);
                } else {
                    tv_recommend_name.setText(mRecommends.get(position).name);
                }
            }
            for (int i = 0; i < mPager.getChildCount(); i++) {
                ImageView dot = (ImageView) mPagesNumber.getChildAt(i);
                if (i == position) {
                    dot.setBackgroundResource(R.drawable.indicator_focus);
                } else {
                    dot.setBackgroundResource(R.drawable.indicator_normal);
                }
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        mListView.invalidateViews();
        super.onConfigurationChanged(newConfig);
        notifyDataChange();
    }

    @Override
    protected void getDatasList() {
        super.getDatasList();
        if (showRecommend) {
            getRecommendList();
        }
        getProductList();
    }

    private List<Product> mTmpData;

    public List<Product> getTmpData() {
        return mTmpData;
    }

    private void getProductList() {
        mApiUtil.getProductList(mActivity.getApplicationContext(), mPage, count, product_type,
                MarketConfiguration.VERSION_CODE, MarketConfiguration.PACKAGE_NAME, supported_mod, orderBy,
                new RequestListener() {

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
                            totalCount = obj.optInt("total");
                            ArrayList<Product> productList = ProductJSONImpl.createProductList(obj.optJSONArray("data"));
                            mTmpData = new ArrayList<Product>();
                            mTmpData.addAll(productList);
                            BLog.v(TAG, "finish getProductList=" + productList.size());
                           
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

    private boolean isLoadingRecommend;
    private final Object mLockedRecommend = new Object();

    private void getRecommendList() {
        if (mPage > 0)
            return;
        if (isLoadingRecommend) {
            BLog.v(TAG, "is Loading Recommend ...");
            return;
        }

        synchronized (mLockedRecommend) {
            isLoadingRecommend = true;
        }
        if (mApiUtil == null) {
            mApiUtil = new ApiUtil();
        }
        mApiUtil.getRecommendList(mActivity.getApplicationContext(), product_type, new RequestListener() {
            @Override
            public void onIOException(IOException e) {
                BLog.d(TAG, "getRecommendList onIOException  " + e.getMessage());
                synchronized (mLockedRecommend) {
                    isLoadingRecommend = false;
                }

                Message mds = mHandler.obtainMessage(LOAD_RECOMMEND_END);
                mds.getData().putBoolean(RESULT, false);
                mHandler.sendMessage(mds);

            }

            @Override
            public void onError(WutongException e) {
                BLog.d(TAG, "onError  " + e.getMessage());
                synchronized (mLockedRecommend) {
                    isLoadingRecommend = false;
                }

                Message mds = mHandler.obtainMessage(LOAD_RECOMMEND_END);
                mds.getData().putBoolean(RESULT, false);
                mHandler.sendMessage(mds);

            }

            @Override
            public void onComplete(String response) {
                JSONObject obj;
                ArrayList<Recommend> rmdList = null;
                try {
                    obj = new JSONObject(response);
                    if (!obj.has("data"))
                        return;
                    rmdList = RecommendJSONImpl.createRecommendList(obj.optJSONArray("data"));
                    BLog.v(TAG, "finish getRecommendList=" + rmdList.size());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                synchronized (mLockedRecommend) {
                    isLoadingRecommend = false;
                }

                Message mds = mHandler.obtainMessage(LOAD_RECOMMEND_END);
                mds.getData().putBoolean(RESULT, true);
                mds.getData().putParcelableArrayList(DATA, rmdList);
                mHandler.sendMessage(mds);

            }
        });
    }

    @Override
    protected void createHandler() {
        mHandler = new MainHandler();
    }

    private final static int LOAD_RECOMMEND_END = 0x00100;
    private final static String DATA = "DATA";

    private class MainHandler extends Handler {
        public MainHandler() {
            super();
        }

        @Override
        public void handleMessage(Message msg) {
            if (getActivity() == null)
                return;
            switch (msg.what) {
            case LOAD_END:
                if (mPage == 0) {
                    if (!isLoading && !isLoadingRecommend) {
                           end();
                        boolean suc = msg.getData().getBoolean(RESULT);
                        if (suc) {
                            if (mPage == 0 && mDatas != null) {
                                mDatas.clear();
                            }
                            List<Product> temDatas = getTmpData();
                            if (temDatas != null && temDatas.size() > 0) {
                                mDatas.addAll(temDatas);
                                if (mDatas.size() >= totalCount) {
                                    hasMore = false;
                                } else {
                                    hasMore = true;
                                }
                            }
                            mPage = (mDatas.size() - 1) / count;
                            notifyDataChange();
                            if (mDatas.size() == 0) {
                                showLoadMessage(R.string.empty_list, false);
                            }
                        } else {
                            showLoadMessage(R.string.msg_loadi_failed);
                        }
                    }
                } else {
                    end();
                    layout_more.setVisibility(View.GONE);
                    boolean suc = msg.getData().getBoolean(RESULT);
                    if (suc) {
                        if (mPage == 0 && mDatas != null) {
                            mDatas.clear();
                        }
                        List<Product> temDatas = getTmpData();
                        if (temDatas != null && temDatas.size() > 0) {
                            mDatas.addAll(temDatas);
                            if (mDatas.size() >= totalCount) {
                                hasMore = false;
                            } else {
                                hasMore = true;
                            }
                        }
                        mPage = (mDatas.size() - 1) / count;
                        notifyDataChange();
                        if (mDatas.size() == 0) {
                            showLoadMessage(R.string.empty_list, false);
                        }
                    } else {
                        showLoadMessage(R.string.msg_loadi_failed);
                    }
                }
                break;
            case LOAD_RECOMMEND_END:
                boolean suc = msg.getData().getBoolean(RESULT);
                if (suc) {
                    mRecommends = msg.getData().getParcelableArrayList(DATA);
                    if (mRecommends == null) {
                        mRecommends = new ArrayList<Recommend>();
                    }
                    if (recommendPhotoAdapter == null) {
                        recommendPhotoAdapter = new RecommendPhotoAdapter(getChildFragmentManager(), mRecommends,
                                supported_mod, product_type);
                        mPager.setAdapter(recommendPhotoAdapter);
                    } else {
                        recommendPhotoAdapter.notifyDataSetChanged(mRecommends);
                    }
                    if (showRecommend) {
                        mPagesNumber.removeAllViews();
                        for (int i = 0; i < recommendPhotoAdapter.getCount(); i++) {
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            ImageView dot = new ImageView(getActivity());
                            if (i == mPager.getCurrentItem()) {
                                dot.setBackgroundResource(R.drawable.indicator_focus);
                            } else {
                                dot.setBackgroundResource(R.drawable.indicator_normal);
                            }
                            mPagesNumber.addView(dot, params);
                        }
                        if (mRecommends != null && mRecommends.size() > 0) {
                            if (mRecommends.get(0).type == Recommend.TYPE_USER_SHARE) {
                                tv_recommend_name.setText(R.string.tab_user_share);
                            } else {
                                tv_recommend_name.setText(mRecommends.get(0).name);
                            }
                        }
                        initRecommendContainer();
                    }
                } else {
                    showLoadMessage(R.string.msg_loadi_failed);
                }
                break;

            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (mAdapter == null)
            return;
        int itemsLastIndex = mAdapter.getCount();
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && visibleLastIndex == itemsLastIndex && hasMore) {
            if (isLoading) {
                layout_more.setVisibility(View.VISIBLE);
            }
            if (isLoading || isLoadingRecommend) {
                BLog.v(TAG, "is Loading ...");
                return;
            }
        }
        super.onScrollStateChanged(view, scrollState);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(TAG_RECOMMEND_DATAS, mRecommends);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mRecommends = savedInstanceState.getParcelableArrayList(TAG_RECOMMEND_DATAS);
        }
        super.onActivityCreated(savedInstanceState);
    }
}
