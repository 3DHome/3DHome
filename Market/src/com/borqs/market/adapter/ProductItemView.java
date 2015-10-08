package com.borqs.market.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.borqs.market.LocalImageManager;
import com.borqs.market.R;
import com.borqs.market.LocalImageManager.ImageUpdateListener;
import com.borqs.market.json.Product;
import com.borqs.market.utils.IntentUtil;

import butterknife.ButterKnife;

public class ProductItemView {
    public static final String TAG = ProductItemView.class.getSimpleName();
    public View convertView;
    public LinearLayout itemContainer;
    public int childCount;

    public int image_portrait_width;
    public int image_portrait_height;
    public int height;
    public int mColumn;
    private Activity mActivity;
    private Context mContext;
    private LayoutInflater inflater;
    private LocalImageManager mLocalImageManager;

    public ProductItemView(Activity activity, LocalImageManager localImageManager, int column, int itemMinWidth) {
        super();
        mActivity = activity;
        mLocalImageManager = localImageManager;
        mContext = mActivity.getBaseContext();
        mColumn = column;
        inflater = LayoutInflater.from(mContext);

        calculateWidth(itemMinWidth);

        convertView = inflater.inflate(R.layout.list_item_view, null);
        itemContainer = ButterKnife.findById(convertView, R.id.item_container);
        convertView.setTag(this);
    }

    private void calculateWidth(int itemMinWidth) {
        image_portrait_width = itemMinWidth;
        image_portrait_height = itemMinWidth * 3 / 2;
        height = image_portrait_height;
    }

    public void setmColumn(int mColumn, int itemMinWidth) {
        calculateWidth(itemMinWidth);
        this.mColumn = mColumn;
    }

    public void initUI(int position, ArrayList<Product> prds) {
        if (itemContainer.getChildCount() != mColumn) {
            itemContainer.removeAllViews();
        }
        if (prds != null && prds.size() > 0) {
            int prdSize = prds.size();
            for (int i = 0; i < mColumn; i++) {
                View child = null;
                Holder holder = null;
                if (itemContainer.getChildCount() > i) {
                    child = itemContainer.getChildAt(i);
                }
                if (child == null) {
                    child = inflater.inflate(R.layout.product_view, null);
                    holder = new Holder();
                    holder.layout_bottom = ButterKnife.findById(child, R.id.layout_bottom);
                    holder.imageCover = ButterKnife.findById(child, R.id.img_cover);
                    holder.layout_product_view = ButterKnife.findById(child, R.id.layout_product_view);
                    holder.textName = ButterKnife.findById(child, R.id.tv_name);

                    child.setTag(holder);
                    itemContainer.addView(child, i, new LinearLayout.LayoutParams(image_portrait_width, height, 0));
                } else {
                    holder = (Holder) child.getTag();
                }
                if (i < prdSize) {
                    final Product product = prds.get(i);
                    child.setVisibility(View.VISIBLE);
                    if (!TextUtils.isEmpty(product.product_id) && product.product_id.startsWith("USP_")) {
                        if (TextUtils.isEmpty(product.name)) {
                            holder.textName.setText(product.author);
                        } else {
                            holder.textName.setText(product.name);
                        }
                    } else {
                        holder.textName.setText(product.name);
                    }

                    if (Product.isSupportApply(product.type)) {
                        holder.layout_bottom.setBackgroundResource(R.drawable.bg_item_normal);
                    } else {
                        holder.layout_bottom.setBackgroundResource(R.drawable.lable_object_bg);
                    }

                    final ImageView cover = holder.imageCover;
                    ImageUpdateListener l = (ImageUpdateListener)cover.getTag();
                    if (l == null || !l.getImageName().equals(product.cover_url)) {
                        mLocalImageManager.removeImageUpdateListener(l);
                        final String imageName = product.cover_url;
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
                    mLocalImageManager.addImageUpdateListener(image_portrait_width, l);
                    cover.setTag(l);
                    holder.layout_product_view.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            IntentUtil.startProductDetailActivity(mActivity, product.product_id, product.version_code,
                                    product.name, product.supported_mod);
                        }
                    });
                } else {
                    child.setVisibility(View.INVISIBLE);
                }
            }
        }
        if (itemContainer.getChildCount() > prds.size()) {
            for (int i = prds.size(); i < itemContainer.getChildCount(); i++) {
                itemContainer.getChildAt(i).setVisibility(View.INVISIBLE);
            }
        }

    }

    static class Holder {
        public View layout_bottom;
        public View layout_product_view;
        public ImageView imageCover;
        public TextView textName;
    }

}
