package com.borqs.market.wallpaper;

import android.os.Bundle;
import android.view.MenuItem;

import com.borqs.market.BasicActivity;
import com.borqs.market.R;
import com.borqs.market.fragment.WallpaperExportFragment;
import com.borqs.market.utils.IntentUtil;

public class WallpaperExportActivity extends BasicActivity {
    WallpaperExportFragment detailFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.wallpaper_detail_activity);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        String product_name = getIntent().getStringExtra(IntentUtil.EXTRA_KEY_NAME);
        getActionBar().setTitle(product_name);

        detailFragment = new WallpaperExportFragment();
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