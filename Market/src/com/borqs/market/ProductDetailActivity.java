package com.borqs.market;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.borqs.market.fragment.ProductDetailFragment;
import com.borqs.market.fragment.ProductDetailFragment.RefreshListener;
import com.borqs.market.listener.ViewListener;
import com.borqs.market.utils.IntentUtil;

public class ProductDetailActivity extends BasicActivity implements RefreshListener,ViewListener {
    ProductDetailFragment detailFragment = null;
//    private static final String FRAGMENT_TAG = "FRAGMENT_TAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.product_detail_activity);
//        getActionBar().setDisplayHomeAsUpEnabled(true);
        String product_name = getIntent().getStringExtra(IntentUtil.EXTRA_KEY_NAME);
        String supportMod = getIntent().getStringExtra(IntentUtil.EXTRA_MOD);
        if(!TextUtils.isEmpty(product_name)) {
//            getActionBar().setTitle(product_name);
            setTitle(product_name);
        }
        if(savedInstanceState == null) {
            detailFragment = new ProductDetailFragment();
            detailFragment.setArguments(ProductDetailFragment.getArguments(supportMod));
            getSupportFragmentManager().beginTransaction()
            .add(R.id.fragment_container, detailFragment).commitAllowingStateLoss();
        }else {
            detailFragment = (ProductDetailFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        }
    }
    @Override
    public void refreshTitle(String title) {
        if(!TextUtils.isEmpty(title)) {
//            getActionBar().setTitle(title);
            setTitle(title);
        }
        
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_refresh) {
            if (detailFragment != null) {
                detailFragment.onRefresh();
            }
        }
        return super.onOptionsItemSelected(item);
    }
//    @Override
//    public void onPageSelected(int position) {
//        if(position == 0) {
//            if(title_container != null) {
//                if(title_container.getVisibility() != View.VISIBLE) {
//                    Animation am = AnimationUtils.loadAnimation(thiz,R.anim.slide_in_down_self);
//                    title_container.startAnimation(am);
//                    title_container.setVisibility(View.VISIBLE);
//                }
//            }
//        }else if(position == 1) {
//            if(title_container != null) {
//                if(title_container.getVisibility() == View.VISIBLE) {
//                    Animation am = AnimationUtils.loadAnimation(thiz,R.anim.slide_out_up_self);
//                    title_container.startAnimation(am);
//                    title_container.setVisibility(View.GONE);
//                }
//            }
//        }
//        
//    }
    @Override
    public void show(boolean showAnimation) {
        if(title_container != null && title_container.getVisibility() != View.VISIBLE) {
            if(showAnimation) {
                Animation am = AnimationUtils.loadAnimation(thiz,R.anim.slide_in_down_self);
                title_container.startAnimation(am);
            }
            title_container.setVisibility(View.VISIBLE);
        }
        
    }
    @Override
    public void hide(boolean showAnimation) {
        if(title_container != null && title_container.getVisibility() == View.VISIBLE) {
            if(showAnimation) {
                Animation am = AnimationUtils.loadAnimation(thiz,R.anim.slide_out_up_self);
                title_container.startAnimation(am);
            }
              title_container.setVisibility(View.GONE);
          }
    }
    @Override
    public void showOrHide(boolean showAnimation) {
        if(title_container != null) {
            if(title_container.getVisibility() == View.VISIBLE) {
                if(showAnimation) {
                    Animation am = AnimationUtils.loadAnimation(thiz,R.anim.slide_out_up_self);
                    title_container.startAnimation(am);
                }
              title_container.setVisibility(View.GONE);
              if(detailFragment != null) {
                  detailFragment.hideBottom();
              }
            }else {
                if(showAnimation) {
                    Animation am = AnimationUtils.loadAnimation(thiz,R.anim.slide_in_down_self);
                    title_container.startAnimation(am);
                }
              title_container.setVisibility(View.VISIBLE);
              if(detailFragment != null) {
                  detailFragment.showBottom();
              }
            }
       }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        outState.putInt(FRAGMENT_TAG, detailFragment.hashCode());
        super.onSaveInstanceState(outState);
    }
        
}