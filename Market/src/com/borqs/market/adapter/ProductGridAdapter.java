package com.borqs.market.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.borqs.market.LocalImageManager;
import com.borqs.market.R;
import com.borqs.market.LocalImageManager.ImageUpdateListener;
import com.borqs.market.json.Product;
import com.borqs.market.utils.BLog;

import butterknife.ButterKnife;

public class ProductGridAdapter extends BaseAdapter {

    private ArrayList<Product> mDatas;
    private LayoutInflater inflater;
    private Context mContext;
    private LocalImageManager mLocalImageManager;

    public ProductGridAdapter(Context context, LocalImageManager localImageManager, ArrayList<Product> datas) {
        mContext = context;
        mLocalImageManager = localImageManager;
        mDatas = datas;
        inflater = LayoutInflater.from(context);
        notifyGridColumnChanged(mContext.getResources().getInteger(R.integer.gridview_number));
    }

    public int getCount() {
        return mDatas != null ? mDatas.size() : 0;
    }

    public Product getItem(int position) {
        if (position >= getCount()) {
            return null;
        } else {
            return mDatas.get(position);
        }
    }

    public long getItemId(int position) {
        return position;
    }

    int image_width = 0;
    int image_height = 0;

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.product_item_view, null);
            holder = new ViewHolder();
            holder.imageCover = ButterKnife.findById(convertView, R.id.img_cover);
            holder.imageCover.setLayoutParams(new RelativeLayout.LayoutParams(image_width, image_height));
            holder.textName = ButterKnife.findById(convertView, R.id.tv_name);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        initUI(position, holder);

        return convertView;
    }

    private void initUI(int position, final ViewHolder holder) {
        if (getItem(position) == null) {
            BLog.e("data is null");
            return;
        }
        Product product = getItem(position);
        final ImageView cover = holder.imageCover;
        ImageUpdateListener l = (ImageUpdateListener)cover.getTag();
        if (l == null || !l.getImageName().equals(product.logo_url)) {
            mLocalImageManager.removeImageUpdateListener(l);
            final String imageName = product.logo_url;
            l = new ImageUpdateListener() {
                public String getImageName() {
                    return imageName;
                }

                public void onImageUpdated(Bitmap image) {
                    if (image != null) {
                        cover.setImageBitmap(image);
                    } else {
                        cover.setImageResource(R.drawable.picture_loading);
                    }

                }
            };
        }
        mLocalImageManager.addImageUpdateListener(image_width, image_height, l);
        cover.setTag(l);
        holder.textName.setText(product.name);
    }

    static class ViewHolder {
        public ImageView imageCover;
        public TextView textName;
    }

    public void notifyDataSetChanged(ArrayList<Product> datas) {
        mDatas = datas;
        super.notifyDataSetChanged();
    }

    public void notifyGridColumnChanged(int columnNum) {
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        image_width = width / columnNum;
        image_height = image_width;
    }
}
