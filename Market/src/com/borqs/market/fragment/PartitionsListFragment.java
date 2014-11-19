package com.borqs.market.fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Message;

import com.borqs.market.json.Product;
import com.borqs.market.json.ProductJSONImpl;
import com.borqs.market.net.RequestListener;
import com.borqs.market.net.WutongException;
import com.borqs.market.utils.BLog;
import com.borqs.market.utils.MarketConfiguration;

public class PartitionsListFragment extends ProductBasicListFragment {

    private static final String TAG = PartitionsListFragment.class.getSimpleName();
    private String partitions_id = null;

    public static Bundle getArguments(String product_type, String supported_mod, String partitions_id) {
        Bundle args = ProductBasicListFragment.getArguments(product_type, supported_mod, false, null);
        args.putString(ARGUMENTS_KEY_ID, partitions_id);
        return args;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        partitions_id = args.getString(ARGUMENTS_KEY_ID);
    }

    @Override
    protected void getDatasList() {
        super.getDatasList();
        getUserShareList();
    }

    List<Product> mTmpDatas;

    public List<Product> getTmpData() {
        return mTmpDatas;
    }

    private void getUserShareList() {
        mApiUtil.getPartitionsList(mActivity.getApplicationContext(), mPage, count, MarketConfiguration.VERSION_CODE,
                partitions_id, supported_mod, new RequestListener() {

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
                            mTmpDatas = productList;
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
}
