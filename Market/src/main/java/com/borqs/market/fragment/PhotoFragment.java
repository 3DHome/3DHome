package com.borqs.market.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.borqs.market.LocalImageManager;
import com.borqs.market.LocalImageManager.ImageUpdateListener;
import com.borqs.market.R;
import com.borqs.market.json.Product.ProductType;
import com.borqs.market.listener.ViewListener;


public class PhotoFragment extends Fragment {
    public static final String TAG = "PhotoFragment";
    private static final String TAG_URL = "TAG_URL";
    private static final String TAG_CATEGORY = "TAG_CATEGORY";

    protected static String ARGUMENTS_KEY_URL = "ARGUMENTS_KEY_URL";
    protected static String ARGUMENTS_KEY_CATEGORY = "ARGUMENTS_KEY_CATEGORY";
    private String url;
    private String category;
    private int width;
//    private int height;
    private LocalImageManager mLocalImageManager;

    public interface ClickListener {
        void onclick();
    }

    public static Bundle getArguments(String url, String category) {
        Bundle args = new Bundle();
        args.putString(ARGUMENTS_KEY_URL, url);
        args.putString(ARGUMENTS_KEY_CATEGORY, category);
        return args;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        url = args.getString(ARGUMENTS_KEY_URL);
        category = args.getString(ARGUMENTS_KEY_CATEGORY);
    }

    View layout_process;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        DisplayMetrics dm = getActivity().getApplicationContext().getResources().getDisplayMetrics();
        int h = dm.heightPixels;
        int w = dm.widthPixels;
        width = w;
//        height = h;
    }

    public ImageView img_shoot;
    public View shoot_layout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.url = savedInstanceState.getString(TAG_URL);
            this.category = savedInstanceState.getString(TAG_CATEGORY);
        }
        mLocalImageManager = new LocalImageManager(new Handler());
        mLocalImageManager.onResume();
        View view = inflater.inflate(R.layout.photo_fragment, null);
        shoot_layout = view.findViewById(R.id.shoot_layout);
        img_shoot = (ImageView) view.findViewById(R.id.img_shoot);

        if (!TextUtils.isEmpty(url)) {
            downloadPhoto(url, img_shoot);
        }
        if (ProductType.OBJECT.equals(category)) {
            shoot_layout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        } else {
            shoot_layout.setBackgroundColor(getResources().getColor(android.R.color.black));
        }
        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (getActivity() != null && ViewListener.class.isInstance(getActivity())) {
                    ((ViewListener) getActivity()).showOrHide(true);
                }

            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        mLocalImageManager.ondestory();
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(TAG_URL, url);
        outState.putString(TAG_CATEGORY, category);
        super.onSaveInstanceState(outState);
    }

    private void downloadPhoto(String url, final ImageView imageView) {
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
                        imageView.setImageResource(R.drawable.picture_loading);
                    }

                }
            };
        }
        mLocalImageManager.addImageUpdateListener(width, l);
        imageView.setTag(l);
    }

}
