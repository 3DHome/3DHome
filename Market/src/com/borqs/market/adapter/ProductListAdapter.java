package com.borqs.market.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.borqs.market.LocalImageManager;
import com.borqs.market.R;
import com.borqs.market.json.Product;

public class ProductListAdapter extends BaseAdapter {

    private ArrayList<Product> mDatas = new ArrayList<Product>();
    private Activity mActivity;
    private Context mContext;
    private int mColumn;
    private int itemMinWidth;
    private boolean online;
    private LocalImageManager mLocalImageManager;

    public ProductListAdapter(Activity activity, LocalImageManager localImageManager, ArrayList<Product> datas,
            boolean online, boolean isPortrait) {
        mActivity = activity;
        mLocalImageManager = localImageManager;
        mContext = activity.getApplicationContext();
        calculateColumns();
        mDatas.clear();
        mDatas.addAll(datas);
        this.online = online;
    }

    private void calculateColumns() {
        final Resources res = mContext.getResources();
        int product_item_padding = res.getDimensionPixelSize(R.dimen.product_item_padding);
        int displayWidthPix = res.getDisplayMetrics().widthPixels - 2 * product_item_padding;

        itemMinWidth = res.getDimensionPixelSize(R.dimen.product_item_image_portrait_width);
        if (displayWidthPix <= itemMinWidth) {
            mColumn = 1;
            itemMinWidth = displayWidthPix;
        } else {
            mColumn = displayWidthPix / itemMinWidth;
            itemMinWidth += (displayWidthPix % itemMinWidth) / mColumn;
        }
    }

    public int getCount() {
        return mDatas != null ? (mDatas.size() / mColumn + (mDatas.size() % mColumn == 0 ? 0 : 1)) : 0;
    }

    public ArrayList<Product> getItem(int position) {
        if (position >= getCount()) {
            return null;
        } else {
            ArrayList<Product> prds = new ArrayList<Product>();
            prds.add(0, mDatas.get(position * mColumn));
            for (int index = 1; index < mColumn; index++) {
                int newPosition = position * mColumn + index;
                if (mDatas.size() > newPosition) {
                    prds.add(index, mDatas.get(newPosition));
                }
            }
            return prds;
        }
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (online) {
            ProductItemView holder = null;
            if (convertView == null) {
                holder = new ProductItemView(mActivity, mLocalImageManager, mColumn, itemMinWidth);
            } else {
                holder = (ProductItemView) convertView.getTag();
                holder.setmColumn(mColumn, itemMinWidth);
            }
            holder.initUI(position, getItem(position));
            return holder.convertView;
        } else {
            ProductLocalItemView holder = null;
            if (convertView == null) {
                holder = new ProductLocalItemView(mActivity, mLocalImageManager, mColumn, itemMinWidth);
            } else {
                holder = (ProductLocalItemView) convertView.getTag();
                holder.setmColumn(mColumn, itemMinWidth);
            }

            holder.initUI(position, getItem(position));
            return holder.convertView;
        }
    }

    public void notifyDataSetChanged(ArrayList<Product> datas) {
        calculateColumns();
        mDatas.clear();
        mDatas.addAll(datas);
        notifyDataSetChanged();
    }

}
