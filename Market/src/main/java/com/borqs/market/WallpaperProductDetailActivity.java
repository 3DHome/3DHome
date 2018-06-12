package com.borqs.market;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import com.borqs.market.fragment.WallpaperProductDetailFragment;
import com.borqs.market.utils.IntentUtil;

public class WallpaperProductDetailActivity extends BasicActivity {
    WallpaperProductDetailFragment detailFragment = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.wallpaper_detail_activity);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        String product_name = getIntent().getStringExtra(IntentUtil.EXTRA_KEY_NAME);
        String supportMod = getIntent().getStringExtra(IntentUtil.EXTRA_MOD);
        if(TextUtils.isEmpty(product_name)) {
            getActionBar().setTitle(R.string.themes_market);
        }else {
            getActionBar().setTitle(product_name);
        }
        detailFragment = new WallpaperProductDetailFragment();
        detailFragment.setArguments(WallpaperProductDetailFragment.getArguments(supportMod));

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, detailFragment).commitAllowingStateLoss();
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
}