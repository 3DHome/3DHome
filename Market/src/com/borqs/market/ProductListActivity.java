package com.borqs.market;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;

import com.borqs.market.fragment.ProductListFragment;
import com.borqs.market.utils.BLog;
import com.borqs.market.utils.MarketUtils;

public class ProductListActivity extends BasicActivity {
    private final String TAG = "ProductListActivity";

    private FragmentManager mFragmentManager;
    private ProductListFragment productFragment;
    private final String TAG_FRAGMENT = "TAG_FRAGMENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BLog.d(TAG, "onCreate(savedInstanceState)");
        setContentView(R.layout.product_list_activity);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        parseIntent();
        if(MarketUtils.CATEGORY_THEME.equals(categoryStr)) {
            getActionBar().setTitle(R.string.top_navigation_theme);
        }else if(MarketUtils.CATEGORY_OBJECT.equals(categoryStr)){
            getActionBar().setTitle(R.string.top_navigation_object);
        }
        else if(MarketUtils.CATEGORY_WALLPAPER.equals(categoryStr)){
            getActionBar().setTitle(R.string.top_navigation_wallpaper);
        }

        productFragment = new ProductListFragment();
        productFragment.setArguments(ProductListFragment.getArguments(categoryStr, supported_mod, showHeaderView, orderBy));
        mFragmentManager = getSupportFragmentManager();
        mFragmentManager.beginTransaction()
                .add(R.id.fragment_container, productFragment, TAG_FRAGMENT)
                .commitAllowingStateLoss();

    }

    private String categoryStr;
    private String supported_mod;
    private String orderBy;
    private boolean showHeaderView;

    private void parseIntent() {
        categoryStr = getIntent().getStringExtra(MarketUtils.EXTRA_CATEGORY);
        supported_mod = getIntent().getStringExtra(MarketUtils.EXTRA_MOD);
        orderBy = getIntent().getStringExtra(MarketUtils.EXTRA_ORDER_BY);
        showHeaderView = getIntent().getBooleanExtra(MarketUtils.EXTRA_SHOW_HEADVIEW,true);
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

    public interface ActionListener {
        void onrefresh();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFragmentManager.beginTransaction().remove(productFragment);
        productFragment = null;
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
}
