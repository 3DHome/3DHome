package com.borqs.market.fragment;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.borqs.market.LocalImageManager;
import com.borqs.market.R;
import com.borqs.market.LocalImageManager.ImageUpdateListener;

public class WallpaperPhotoFragment extends Fragment {
    public static final String TAG = "PhotoFragment";
    private static final String TAG_URL = "TAG_URL";
    private static final String ARGUMENTS_KEY_URL = "ARGUMENTS_KEY_URL";
    private String url;
    private LocalImageManager mLocalImageManager;

    public interface ClickListener {
        void onclick();
    }

    public static Bundle getArguments(String url) {
        Bundle args = new Bundle();
        args.putString(ARGUMENTS_KEY_URL, url);
        return args;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        url = args.getString(ARGUMENTS_KEY_URL);
    }

    View layout_process;
    public ImageView img_shoot;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.url = savedInstanceState.getString(TAG_URL);
        }
        mLocalImageManager = new LocalImageManager(new Handler());
        mLocalImageManager.onResume();
        View view = inflater.inflate(R.layout.wallpaper_photo_fragment, null);
        img_shoot = (ImageView) view.findViewById(R.id.img_shoot);
        if (!TextUtils.isEmpty(url)) {
            downloadPhoto(url, img_shoot);
        }
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
        super.onSaveInstanceState(outState);
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
