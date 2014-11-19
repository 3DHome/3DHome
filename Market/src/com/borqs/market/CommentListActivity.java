package com.borqs.market;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.borqs.market.fragment.CommentListFragment;
import com.borqs.market.utils.IntentUtil;

public class CommentListActivity extends BasicActivity {

    private FragmentManager mFragmentManager;
    private CommentListFragment fragment;
    private final String TAG_FRAGMENT = "TAG_FRAGMENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.default_fragment_activity);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        parseIntent();
        
        getActionBar().setTitle(R.string.title_comment);
        
        fragment = new CommentListFragment();
        fragment.setArguments(CommentListFragment.getArguments(product_id, rating, version_code));
        mFragmentManager = getSupportFragmentManager();
        mFragmentManager.beginTransaction()
                .add(R.id.fragment_container, fragment, TAG_FRAGMENT)
                .commitAllowingStateLoss();

    }

    private float rating;
    private String product_id;
    private int version_code;

    private void parseIntent() {
        rating = getIntent().getFloatExtra(IntentUtil.EXTRA_KEY_RATING, 0);
        version_code = getIntent().getIntExtra(IntentUtil.EXTRA_KEY_VERSION, 0);
        product_id = getIntent().getStringExtra(IntentUtil.EXTRA_KEY_ID);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mFragmentManager.putFragment(outState, TAG_FRAGMENT, fragment);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
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
        mFragmentManager.beginTransaction().remove(fragment);
        fragment = null;
    }

    
}
