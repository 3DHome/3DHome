package com.borqs.market;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.MenuItem;

import com.borqs.market.fragment.ProductListFragment;
import com.borqs.market.fragment.ProductLocalListFragment;
import com.borqs.market.utils.MarketConfiguration;
import com.borqs.market.utils.MarketUtils;

public class ProductTabActivity extends BasicActivity implements ActionBar.TabListener {

    private FragmentManager mFragmentManager;
    private final String TAG_FRAGMENT_ONLINE = "TAG_FRAGMENT_ONLINE";
    private final String TAG_FRAGMENT_LOCAL = "TAG_FRAGMENT_LOCAL";
    private String supported_mod;

    private ActionBar.Tab tabLocal = null;
    private ActionBar.Tab tabOnline = null;
    private MyAdapter mAdapter;
    private ViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        parseIntent();

        if (MarketUtils.CATEGORY_THEME.equals(categoryStr)) {
            getActionBar().setTitle(R.string.top_navigation_theme);
        } else if (MarketUtils.CATEGORY_OBJECT.equals(categoryStr)) {
            getActionBar().setTitle(R.string.top_navigation_object);
        } else if (MarketUtils.CATEGORY_WALLPAPER.equals(categoryStr)) {
            getActionBar().setTitle(R.string.top_navigation_wallpaper);
        }
        tabOnline = getActionBar().newTab();
        tabOnline.setText(R.string.tab_online);
        tabOnline.setTabListener(this);
        getActionBar().addTab(tabOnline, 0);

        tabLocal = getActionBar().newTab();
        tabLocal.setText(R.string.tab_local);
        tabLocal.setTabListener(this);
        getActionBar().addTab(tabLocal, 1);

        mFragmentManager = getSupportFragmentManager();
        if (savedInstanceState != null) {
            onLineFragment = (ProductListFragment) mFragmentManager
                    .getFragment(savedInstanceState, TAG_FRAGMENT_ONLINE);
            localFragment = (ProductLocalListFragment) mFragmentManager.getFragment(savedInstanceState,
                    TAG_FRAGMENT_LOCAL);
            tab_index = savedInstanceState.getInt("tab_index");
        }
        mAdapter = new MyAdapter(getSupportFragmentManager());

        mPager = findById(R.id.pager);
        mPager.setAdapter(mAdapter);

        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                tab_index = position;
                getActionBar().setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                // TODO Auto-generated method stub
            }
        });
    }

    private ProductLocalListFragment localFragment = null;
    private ProductListFragment onLineFragment = null;

    class MyAdapter extends FragmentPagerAdapter {
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                if (onLineFragment == null) {
                    onLineFragment = new ProductListFragment();
                    onLineFragment.setArguments(ProductListFragment.getArguments(categoryStr, supported_mod, true,
                            orderBy));
                }
                return onLineFragment;
            } else {
                if (localFragment == null) {
                    localFragment = new ProductLocalListFragment();
                    localFragment.setArguments(ProductLocalListFragment.getArguments(categoryStr, supported_mod));
                }
                return localFragment;
            }
        }
    }

    private String categoryStr;
    private String orderBy;

    private void parseIntent() {
        supported_mod = getIntent().getStringExtra(MarketUtils.EXTRA_MOD);
        if (TextUtils.isEmpty(MarketConfiguration.PACKAGE_NAME)) {
            throw new IllegalArgumentException("package name is null");
        }
        categoryStr = getIntent().getStringExtra(MarketUtils.EXTRA_CATEGORY);
        orderBy = getIntent().getStringExtra(MarketUtils.EXTRA_ORDER_BY);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mFragmentManager.putFragment(outState, TAG_FRAGMENT_ONLINE, onLineFragment);
        mFragmentManager.putFragment(outState, TAG_FRAGMENT_LOCAL, localFragment);
        outState.putInt("tab_index", tab_index);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            onLineFragment = (ProductListFragment) mFragmentManager
                    .getFragment(savedInstanceState, TAG_FRAGMENT_ONLINE);
            localFragment = (ProductLocalListFragment) mFragmentManager.getFragment(savedInstanceState,
                    TAG_FRAGMENT_LOCAL);
            tab_index = savedInstanceState.getInt("tab_index");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_refresh) {
            if (tab_index == 0) {
                if (onLineFragment != null) {
                    onLineFragment.onRefresh();
                }
            } else if (tab_index == 1) {
                if (localFragment != null) {
                    localFragment.onRefresh();
                }
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
        onLineFragment = null;
        localFragment = null;
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub

    }

    int tab_index = 0;

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        if (mPager != null) {
            tab_index = tab.getPosition();
            mPager.setCurrentItem(tab_index, true);
        }
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub

    }
}
