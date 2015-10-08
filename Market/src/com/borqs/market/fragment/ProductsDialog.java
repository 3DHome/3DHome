package com.borqs.market.fragment;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.borqs.market.R;
import com.borqs.market.json.Product.ProductType;
import com.borqs.market.utils.MarketUtils;

import butterknife.ButterKnife;

public class ProductsDialog extends BasicDialogFragment implements View.OnClickListener{
    private final int FRAGMENT_COUNT = 2;
    private final int POSITION_ONLINE = 0;
    private final int POSITION_LOCAL = 1;
    private MarketUtils.OnDragListener OnDragListener;
    private float mPreTouchX;
    private float mPreTouchY;
    private String mSelectObjectID;
    private boolean mOnDrag = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        product_type = args.getString(ARGUMENTS_KEY_TYPE);
        supported_mod = args.getString(ARGUMENTS_KEY_MOD);
    }

    public static Bundle getArguments(String product_type, String supported_mod) {
        Bundle args = new Bundle();
        args.putString(ARGUMENTS_KEY_TYPE, product_type);
        args.putString(ARGUMENTS_KEY_MOD, supported_mod);
        return args;
    }

    protected String product_type = ProductType.THEME;
    protected String supported_mod;
    private MyAdapter mAdapter;
    private ViewPager mPager;
    private TextView lableOnline;
    private TextView lableLocal;
    private TextView title_bar_title;
    private View tabOnline;
    private View tabLocal;
    private View layout_back;
    private View layout_right;

    private ProductLocalListFragment localFragment = null;
    private ProductListFragment onLineFragment = null;

    @Override
    protected void initView(LayoutInflater inflater) {
        mPager = ButterKnife.findById(mConvertView, R.id.pager);
        lableOnline = ButterKnife.findById(mConvertView, R.id.lable_online);
        lableLocal = ButterKnife.findById(mConvertView, R.id.lable_local);
        title_bar_title = ButterKnife.findById(mConvertView, R.id.title_bar_title);
        tabOnline = ButterKnife.findById(mConvertView, R.id.tab_online);
        tabLocal = ButterKnife.findById(mConvertView, R.id.tab_local);
        layout_back = ButterKnife.findById(mConvertView, R.id.layout_back);
        layout_right = ButterKnife.findById(mConvertView, R.id.layout_right);
        tabOnline.setOnClickListener(this);
        tabLocal.setOnClickListener(this);
        layout_back.setOnClickListener(this);
        layout_right.setOnClickListener(this);
        
        mAdapter = new MyAdapter(getChildFragmentManager());
        mPager.setAdapter(mAdapter);
        mPager.setCurrentItem(POSITION_LOCAL);
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if(position == POSITION_ONLINE) {
                    lableOnline.setPressed(true);
                    lableLocal.setPressed(false);
                    tabOnline.setBackgroundResource(R.drawable.tab_selected);
//                    tabLocal.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    tabLocal.setBackgroundResource(R.drawable.tab_indicator_ab_holo);
                }else if(position == POSITION_LOCAL) {
                    lableOnline.setPressed(false);
                    lableLocal.setPressed(true);
//                    tabOnline.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    tabOnline.setBackgroundResource(R.drawable.tab_indicator_ab_holo);
                    tabLocal.setBackgroundResource(R.drawable.tab_selected);
                }
                
            }
        });
        
        if(MarketUtils.CATEGORY_THEME.equals(product_type)) {
            title_bar_title.setText(R.string.top_navigation_theme);
        }else if(MarketUtils.CATEGORY_OBJECT.equals(product_type)){
            title_bar_title.setText(R.string.top_navigation_object);
        }else if(MarketUtils.CATEGORY_WALLPAPER.equals(product_type)){
            title_bar_title.setText(R.string.top_navigation_wallpaper);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return mConvertView;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Dialog dialog = super.onCreateDialog(savedInstanceState);
        Dialog dialog = new Dialog(getActivity(), R.style.HelperDialogStyle2) {

            @Override
            public boolean dispatchTouchEvent(MotionEvent ev) {
                mPreTouchX = ev.getX();
                mPreTouchY = ev.getY();
                if (mOnDrag && OnDragListener != null) {
                    if (ev.getAction() == MotionEvent.ACTION_CANCEL || ev.getAction() == MotionEvent.ACTION_UP) {
                        dismiss();
                    }
                    return OnDragListener.onDrag(ev);
                } else {
                    return super.dispatchTouchEvent(ev);
                }
            }

        };
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        IntentFilter filter = new IntentFilter(MarketUtils.ACTION_MARKET_LONG_CLICK);
        mApplicationContext.registerReceiver(mReceiver, filter);
        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        mApplicationContext.unregisterReceiver(mReceiver);
        super.onDismiss(dialog);
    }

    class MyAdapter extends FragmentPagerAdapter {
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return FRAGMENT_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == POSITION_ONLINE) {
                if (onLineFragment == null) {
                    onLineFragment = new ProductListFragment();
                    onLineFragment.setArguments(ProductListFragment
                            .getArguments(product_type, supported_mod, true,
                                    null));
                }
                return onLineFragment;
            } else {
                if (localFragment == null) {
                    localFragment = new ProductLocalListFragment();
                    localFragment.setArguments(ProductLocalListFragment
                            .getArguments(product_type, supported_mod));
                }
                return localFragment;
            }
        }
    }

    @Override
    public void onLogin() {
        // TODO Auto-generated method stub
    }

    @Override
    public void onLoging() {
        // TODO Auto-generated method stub
    }

    @Override
    public void onLogout() {
        // TODO Auto-generated method stub
    }

    @Override
    public void onCancelLogin() {
        // TODO Auto-generated method stub
    }

    @Override
    protected void createHandler() {
        // TODO Auto-generated method stub
    }

    @Override
    protected int getInflatelayout() {
        return R.layout.product_list_dialog;
    }

    @Override
    public void onRefresh() {
        // TODO Auto-generated method stub
    }

    @Override
    public void onClick(View v) {
       int id = v.getId();
       if(id == R.id.tab_online) {
           if(mPager.getCurrentItem() != POSITION_ONLINE) {
               mPager.setCurrentItem(0,true);
               lableOnline.setPressed(true);
               lableLocal.setPressed(false);
           }
       }else if(id == R.id.tab_local) {
           if(mPager.getCurrentItem() != POSITION_LOCAL) {
               mPager.setCurrentItem(1,true);
               lableOnline.setPressed(false);
               lableLocal.setPressed(true);
           }
       }else if(id == R.id.layout_back) {
           dismiss();
       }else if(id == R.id.layout_right) {
           if (mPager.getCurrentItem() == POSITION_ONLINE) {
               if (onLineFragment != null) {
                   onLineFragment.onRefresh();
               }
           } else {
               if (localFragment != null) {
                   localFragment.onRefresh();
               }
           }
       }
        
    }
    
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mConvertView.setVisibility(View.GONE);
            mOnDrag = true;
            mSelectObjectID = intent.getStringExtra(MarketUtils.EXTRA_PRODUCT_ID);
            OnDragListener = MarketUtils.getOnDragListener();
            if (OnDragListener != null) {
                OnDragListener.onStartDrag(mSelectObjectID, mPreTouchX, mPreTouchY);
            }
        }
    };
}
