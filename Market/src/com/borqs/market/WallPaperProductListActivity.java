package com.borqs.market;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.borqs.market.fragment.WallpaperProductGridFragment;
import com.borqs.market.utils.BLog;
import com.borqs.market.utils.MarketUtils;

public class WallPaperProductListActivity extends BasicActivity {
    private final String TAG = "WallPaperProductListActivity";

    private FragmentManager mFragmentManager;
    private WallpaperProductGridFragment productFragment;
    private final String TAG_FRAGMENT = "TAG_FRAGMENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BLog.d(TAG, "onCreate(savedInstanceState)");
        setContentView(R.layout.default_fragment_activity);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        // getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        parseIntent();
        
        getActionBar().setTitle(R.string.top_navigation_wallpaper);
        
        productFragment = new WallpaperProductGridFragment();
        productFragment.setArguments(WallpaperProductGridFragment.getArguments(categoryStr));
        mFragmentManager = getSupportFragmentManager();
        mFragmentManager.beginTransaction()
                .add(R.id.fragment_container, productFragment, TAG_FRAGMENT)
                .commitAllowingStateLoss();

    }

    private String categoryStr;

    private void parseIntent() {
        categoryStr = MarketUtils.CATEGORY_WALLPAPER;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        BLog.d(TAG, "onSaveInstanceState(outState)");
        mFragmentManager.putFragment(outState, TAG_FRAGMENT, productFragment);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        BLog.d(TAG, "onRestoreInstanceState(outState)");
        if (savedInstanceState != null) {
            mFragmentManager.getFragment(savedInstanceState, TAG_FRAGMENT);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_refresh) {
            if (productFragment != null) {
                productFragment.onRefresh();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public interface ActionListener {
        void onrefresh();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFragmentManager.beginTransaction().remove(productFragment);
        productFragment = null;
    }
}
