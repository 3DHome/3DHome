package com.borqs.market;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;

import com.borqs.market.fragment.PartitionsListFragment;
import com.borqs.market.utils.BLog;
import com.borqs.market.utils.MarketUtils;

public class PartitionsListActivity extends BasicActivity {
    private final String TAG = PartitionsListActivity.class.getSimpleName();

    private FragmentManager mFragmentManager;
    private PartitionsListFragment productFragment;
    private final String TAG_FRAGMENT = "TAG_FRAGMENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_list_activity);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        parseIntent();
        getActionBar().setTitle(name);

        productFragment = new PartitionsListFragment();
        productFragment.setArguments(PartitionsListFragment.getArguments(categoryStr, supported_mod, partitions_id));
        mFragmentManager = getSupportFragmentManager();
        mFragmentManager.beginTransaction()
                .add(R.id.fragment_container, productFragment, TAG_FRAGMENT)
                .commitAllowingStateLoss();

    }

    private String partitions_id;
    private String categoryStr;
    private String supported_mod;
    private String name;

    private void parseIntent() {
        categoryStr = getIntent().getStringExtra(MarketUtils.EXTRA_CATEGORY);
        supported_mod = getIntent().getStringExtra(MarketUtils.EXTRA_MOD);
        name = getIntent().getStringExtra(MarketUtils.EXTRA_NAME);
        partitions_id = getIntent().getStringExtra(MarketUtils.EXTRA_ID);
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
