package com.borqs.market.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.view.ViewGroup;

import com.borqs.market.fragment.PhotoFragment;
import com.borqs.market.fragment.ProductDescriptionFragment;
import com.borqs.market.json.Product;

public class ScreenShotAdapter  extends FragmentPagerAdapter {
    
    private Product mData = null;
    private ProductDescriptionFragment desFragment;
    private FragmentManager fm;
    public ScreenShotAdapter(FragmentManager fm,Product data) {
        super(fm);
        this.fm = fm;
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
            if(desFragment == null) {
                desFragment = ProductDescriptionFragment.newInstance(mData);
            }
            return desFragment;
        }else {
            PhotoFragment f = new PhotoFragment();
            f.setArguments(PhotoFragment.getArguments(mData.screenshots.get(position - 1),mData.type));
            return f;
        }
    }

    public void alertData(Product data) {
    	mData = data;
    	notifyDataSetChanged();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public void destroyItem(ViewGroup container,int position,Object object){
      super.destroyItem(container, position, object);
      FragmentTransaction bt = fm.beginTransaction();
      bt.remove((Fragment)object);
      bt.commitAllowingStateLoss();
      object = null;
      if(position == 0) {
          desFragment = null;
      }
    }
    
}
