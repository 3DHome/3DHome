package com.borqs.market.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.borqs.market.fragment.ProductDescriptionFragment;
import com.borqs.market.fragment.WallpaperPhotoFragment;
import com.borqs.market.json.Product;

public class WallPapercreenShotAdapter  extends FragmentPagerAdapter {
    
    private Product mData = null;
    public WallPapercreenShotAdapter(FragmentManager fm,Product data) {
        super(fm);
        mData = data;
    }

    @Override
    public int getCount() {
        int count = 1;
        if (mData != null && mData.screenshots != null) {
            count = mData.screenshots.size();
            return count==0?1:count + 1;
        }
        return count;
    }

    @Override
    public Fragment getItem(int position) {
        if(position == 0) {
            return ProductDescriptionFragment.newInstance(mData);
        }else {
            WallpaperPhotoFragment f = new WallpaperPhotoFragment();
            f.setArguments(WallpaperPhotoFragment.getArguments(mData.screenshots.get(position - 1)));
            return f;
        }
    }

}
