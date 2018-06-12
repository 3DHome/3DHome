package com.borqs.market.adapter;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.view.ViewGroup;

import com.borqs.market.fragment.RecommendPhotoFragment;
import com.borqs.market.json.Recommend;

public class RecommendPhotoAdapter  extends FragmentPagerAdapter {
    
    private ArrayList<Recommend> mDatas = null;
    private String supported_mod;
    private String category;
    private FragmentManager fm;
    public RecommendPhotoAdapter(FragmentManager fm,ArrayList<Recommend> datas, String supported_mod, String category) {
        super(fm);
        mDatas = datas;
        this.supported_mod = supported_mod;
        this.category = category;
        this.fm = fm;
    }

    @Override
    public int getCount() {
        return mDatas == null? 0 : mDatas.size();
    }

    @Override
    public Fragment getItem(int position) {
        if(mDatas != null && mDatas.size() > position) {
            Bundle data = new Bundle();
            data.putParcelable(RecommendPhotoFragment.KEY_DATA, mDatas.get(position));
            data.putString(RecommendPhotoFragment.KEY_MOD, supported_mod);
            data.putString(RecommendPhotoFragment.KEY_CATEGORY, category);
            RecommendPhotoFragment f = new RecommendPhotoFragment();
            f.setArguments(data);
            return f;
        }
        return null;
    }
    
    public void notifyDataSetChanged(ArrayList<Recommend> mDatas) {
        this.mDatas = mDatas;
        super.notifyDataSetChanged();
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
    }
}
