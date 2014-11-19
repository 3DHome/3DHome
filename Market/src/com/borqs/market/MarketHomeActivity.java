package com.borqs.market;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import com.borqs.market.fragment.ProductListFragment;
import com.borqs.market.json.Product.ProductType;
import com.borqs.market.utils.BLog;
import com.borqs.market.utils.MarketUtils;

public class MarketHomeActivity extends BasicActivity implements
        ActionBar.TabListener {
    private final String TAG = "MarketHomeActivity";

    // static final int NUM_ITEMS = 2;
    static final String TAG_THEME = "TAG_THEME";
    static final String TAG_OBJECT = "TAG_OBJECT";

    private MyAdapter mAdapter;
    private ViewPager mPager;
    // private ActionBar.Tab tabTheme = null;
    // private ActionBar.Tab tabObj = null;
    private String[] theme_category;
    private ArrayList<ActionBar.Tab> tabList = new ArrayList<ActionBar.Tab>();
    private Map<String, ProductListFragment> fragmentMap = new HashMap<String, ProductListFragment>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BLog.d(TAG, "onCreate(savedInstanceState)");
        setContentView(R.layout.activity_main);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        parseIntent();
        theme_category = getResources().getStringArray(R.array.theme_category);

        if (theme_category != null && theme_category.length > 0) {
            for (int i = 0; i < theme_category.length; i++) {
                ActionBar.Tab tab = getActionBar().newTab();
                tab.setText(theme_category[i]);
                tab.setTabListener(this);
                getActionBar().addTab(tab, i);
                tabList.add(i, tab);
            }
        }
        // tabTheme = getSupportActionBar().newTab();
        // tabTheme.setText(R.string.top_navigation_theme);
        // tabTheme.setTabListener(this);
        // getSupportActionBar().addTab(tabTheme, Category.THEME.ordinal());
        //
        // tabObj = getSupportActionBar().newTab();
        // tabObj.setText(R.string.top_navigation_object);
        // tabObj.setTabListener(this);
        // getSupportActionBar().addTab(tabObj, Category.OBJECT.ordinal());
        getActionBar().setSelectedNavigationItem(tab_index);

        mAdapter = new MyAdapter(getSupportFragmentManager());

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mPager.setCurrentItem(tab_index);

        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                tab_index = position;
                // if (position == Category.THEME.ordinal()) {
                // getSupportActionBar().selectTab(tabTheme);
                // } else {
                // getSupportActionBar().selectTab(tabObj);
                // }
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

    private String supported_mod;
    private int tab_index = 0;

    private void parseIntent() {
        String categoryStr = getIntent().getStringExtra(
                MarketUtils.EXTRA_CATEGORY);
        supported_mod = getIntent().getStringExtra(MarketUtils.EXTRA_MOD);
        if (MarketUtils.CATEGORY_THEME.equals(categoryStr)) {
            tab_index = 0;
        } else if (MarketUtils.CATEGORY_OBJECT.equals(categoryStr)) {
            tab_index = 1;
        } else {
            tab_index = 0;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        BLog.d(TAG, "onSaveInstanceState(outState)");
        // getSupportFragmentManager().putFragment(outState, TAG_THEME,
        // themeFragment);
        // getSupportFragmentManager().putFragment(outState, TAG_OBJECT,
        // objecttFragment);

        // if(fragmentList != null && fragmentList.size() >0) {
        // for(int index = 0;index < fragmentList.size();index++) {
        // ProductGridFragment f = fragmentList.get(index);
        // if (f != null) {
        // getSupportFragmentManager().putFragment(outState,
        // generateFragmentTag(index),
        // f);
        // }
        // }
        if (fragmentMap != null && theme_category != null
                && theme_category.length > 0) {
            for (int index = 0; index < theme_category.length; index++) {
                ProductListFragment f = fragmentMap.get(theme_category);
                if (f != null) {
                    getSupportFragmentManager().putFragment(outState,
                            generateFragmentTag(theme_category[index]), f);
                }
            }
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        BLog.d(TAG, "onRestoreInstanceState(outState)");
        // if (themeFragment == null) {
        // themeFragment = (ProductGridFragment) getSupportFragmentManager()
        // .getFragment(savedInstanceState, TAG_THEME);
        // }
        //
        // if (objecttFragment == null) {
        // objecttFragment = (ProductGridFragment) getSupportFragmentManager()
        // .getFragment(savedInstanceState, TAG_OBJECT);
        // }

        // if(savedInstanceState != null && theme_category != null &&
        // theme_category.length >0) {
        // if(fragmentList == null) fragmentList = new
        // ArrayList<ProductGridFragment>();
        // fragmentList.clear();
        // for(int index = 0;index < theme_category.length;index++) {
        // fragmentList.add(index,
        // (ProductGridFragment) getSupportFragmentManager()
        // .getFragment(savedInstanceState, generateFragmentTag(index))
        // );
        // }
        // }

        if (savedInstanceState != null && theme_category != null
                && theme_category.length > 0) {
            if (fragmentMap == null)
                fragmentMap = new HashMap<String, ProductListFragment>();
            fragmentMap.clear();
            for (int index = 0; index < theme_category.length; index++) {
                fragmentMap
                        .put(theme_category[index],
                                (ProductListFragment) getSupportFragmentManager()
                                        .getFragment(
                                                savedInstanceState,
                                                generateFragmentTag(theme_category[index])));
            }
        }
    }

    private String generateFragmentTag(String key) {
        return "TAG_" + key;
    }

    private ProductListFragment getFragment(int position) {
        ProductListFragment f = null;
        if (fragmentMap == null) {
            fragmentMap = new HashMap<String, ProductListFragment>();
        }
        if (fragmentMap.containsKey(theme_category[position])) {
            f = fragmentMap.get(theme_category[position]);
        } else {
            f = new ProductListFragment();
            f.setArguments(ProductListFragment.getArguments(ProductType.getProductType(position), supported_mod, true, null));
            fragmentMap.put(theme_category[position], f);
        }

        return f;
    }

    // private ProductGridFragment themeFragment = null;
    // private ProductGridFragment objecttFragment = null;

    class MyAdapter extends FragmentPagerAdapter {
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return theme_category == null ? 0 : theme_category.length;
        }

        @Override
        public Fragment getItem(int position) {
            // if (position == 0) {
            // if (themeFragment == null) {
            // themeFragment = new ProductGridFragment(ProductType.THEME,
            // app_version, package_name);
            // }
            // return themeFragment;
            // } else {
            // if (objecttFragment == null) {
            // objecttFragment = new ProductGridFragment(
            // ProductType.OBJECT, app_version, package_name);
            // }
            // return objecttFragment;
            // }
            ProductListFragment f = null;
            if (fragmentMap == null) {
                fragmentMap = new HashMap<String, ProductListFragment>();
            }
            if (fragmentMap.containsKey(theme_category[position])) {
                f = fragmentMap.get(theme_category[position]);
            } else {
                f = new ProductListFragment();
                f.setArguments(ProductListFragment.getArguments(ProductType.getProductType(position), supported_mod, true, null));
                fragmentMap.put(theme_category[position], f);
            }

            return f;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_refresh) {
            ProductListFragment fragment = getFragment(tab_index);
            if (fragment != null) {
                fragment.onRefresh();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public interface ActionListener {
        void onrefresh();
    }

    @Override
    public void onTabSelected(Tab tab, android.app.FragmentTransaction ft) {
        if (mPager != null) {
            tab_index = tab.getPosition();
            mPager.setCurrentItem(tab_index, true);
        }
        
    }

    @Override
    public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onTabReselected(Tab tab, android.app.FragmentTransaction ft) {
        // TODO Auto-generated method stub
        
    }
}
