package com.borqs.market.fragment;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.borqs.market.LocalImageManager;
import com.borqs.market.R;
import com.borqs.market.LocalImageManager.ImageUpdateListener;
import com.borqs.market.json.Recommend;
import com.borqs.market.utils.BLog;
import com.borqs.market.utils.IntentUtil;
import com.borqs.market.utils.MarketUtils;

public class RecommendPhotoFragment extends Fragment {
    public static final String TAG = RecommendPhotoFragment.class.getSimpleName();
    public final static String KEY_DATA = "KEY_DATA";
    public final static String KEY_MOD = "KEY_MOD";
    public final static String KEY_CATEGORY = "KEY_CATEGORY";
    private Recommend mData;
    private String supported_mod;
    private String category;
    private LocalImageManager mLocalImageManager;

    public interface ClickListener {
        void onclick();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mData = (Recommend) args.getParcelable(KEY_DATA);
        supported_mod = args.getString(KEY_MOD);
        category = args.getString(KEY_CATEGORY);
    }

    View layout_process;
    public ImageView img_recommend;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mLocalImageManager = new LocalImageManager(new Handler());
        mLocalImageManager.onResume();
        View view = inflater.inflate(R.layout.recommend_imageview, null);
        img_recommend = (ImageView) view.findViewById(R.id.img_recommend);
        return view;
    }

    @Override
    public void onDestroyView() {
        mLocalImageManager.ondestory();
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        BLog.d(TAG, "onActivityCreated()");
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mData = (Recommend) savedInstanceState.getParcelable(KEY_DATA);
            supported_mod = savedInstanceState.getString(KEY_MOD);
            category = savedInstanceState.getString(KEY_CATEGORY);
        }
        if (mData != null) {
            initPad(mData, img_recommend);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(KEY_DATA, mData);
        outState.putString(KEY_MOD, supported_mod);
        outState.putString(KEY_CATEGORY, category);
        super.onSaveInstanceState(outState);
    }

    private void initPad(Recommend recommend, ImageView img_recommend) {
        if (recommend.type == Recommend.TYPE_USER_SHARE) {
            img_recommend.setImageResource(R.drawable.picture_loading);
            img_recommend.setOnClickListener(getOnClickListener(recommend));
        }

        if (!TextUtils.isEmpty(recommend.promotion_image)) {
            img_recommend.setImageResource(R.drawable.picture_loading);
            downloadPhoto(recommend.promotion_image, img_recommend);
            img_recommend.setOnClickListener(getOnClickListener(recommend));
        }
    }

    private View.OnClickListener getOnClickListener(final Recommend recommend) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recommend.type == Recommend.TYPE_SINGLE_PRODUCT) {
                    IntentUtil.startProductDetailActivity(getActivity(), recommend.target, 0, null, supported_mod);
                } else if (recommend.type == Recommend.TYPE_PARTITION) {
                    MarketUtils.startPartitionseListIntent(getActivity().getApplicationContext(), category,
                            supported_mod, recommend.name, recommend.target);
                } else if (recommend.type == Recommend.TYPE_TAG) {
                    // TODO
                } else if (recommend.type == Recommend.TYPE_USER_SHARE) {
                    MarketUtils
                            .startUserShareListIntent(getActivity().getApplicationContext(), category, supported_mod);
                } else if (recommend.type == Recommend.TYPE_ORDER_BY) {
                    MarketUtils.startProductListIntent(getActivity().getApplicationContext(), category, false,
                            supported_mod, false, recommend.target);
                }
            }
        };
    }

    private void downloadPhoto(String url, final ImageView imageView) {
        Resources resources = imageView.getResources();
        int width = (int) (300 * resources.getDisplayMetrics().density);

        ImageUpdateListener l = (ImageUpdateListener) imageView.getTag();
        if (l == null || !l.getImageName().equals(url)) {
            mLocalImageManager.removeImageUpdateListener(l);
            final String imageName = url;
            l = new ImageUpdateListener() {
                public String getImageName() {
                    return imageName;
                }

                public void onImageUpdated(Bitmap image) {
                    if (image != null) {
                        imageView.setImageBitmap(image);
                    } else {
                        imageView.setImageBitmap(null);
                    }

                }
            };
        }
        mLocalImageManager.addImageUpdateListener(width, l);
        imageView.setTag(l);
    }

}
