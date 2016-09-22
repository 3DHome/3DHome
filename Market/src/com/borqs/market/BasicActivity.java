package com.borqs.market;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.borqs.market.account.AccountListener;
import com.borqs.market.account.AccountLoader;
import com.borqs.market.account.AccountLoaderFactory;
import com.borqs.market.account.AccountObserver;
import com.borqs.market.account.AccountSession;
import com.borqs.market.json.Product;
import com.borqs.market.utils.BLog;
import com.borqs.market.utils.IntentUtil;
import com.borqs.market.utils.MarketUtils;
import com.borqs.market.view.LightProgressDialog;
import com.iab.engine.MarketBilling;
import com.iab.engine.MarketBillingFactory;
import com.iab.engine.MarketPurchaseListener;

public class BasicActivity extends FragmentActivity implements AccountListener{
    
    private static final String TAG = "BasicActivity";
    protected final FragmentActivity thiz = this;
    private AccountLoader accountLoader;
    private MarketBilling marketBilling = null;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        AccountObserver.registerAccountListener(this.getClass().getName(),this);
        if(!AccountSession.isLogin) {
            accountLoader = AccountLoaderFactory.createAccountLoader(this);
            AccountSession.loadAccount(getBaseContext());
        }
        marketBilling = MarketBillingFactory.createMarketBilling(this);
        
    }
    
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        final View parent = getWindow().getDecorView();
        setTitleResource(parent, R.layout.title_bar_item);
    }
    
    protected LinearLayout title_container;
    protected void setTitleResource(View parent, int resId) {
        title_container = (LinearLayout) parent.findViewById(R.id.titlebar_container);
        if (title_container != null) {
            View content = LayoutInflater.from(this).inflate(resId, null);
            content.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            title_container.addView(content);
            initHeadViews(parent);
        } else {
            BLog.v(TAG, "why I am null, *****************");
        }
    }
    
    private View layout_right;
    private View layout_back;
//    private ImageView action_back;
    private ImageView action_right;
    private TextView title;
    protected void initHeadViews(View parent) {
        BLog.v(TAG, "initHeadViews");

//        action_back = (ImageView) parent.findViewById(action_back);
        action_right =  (ImageView)parent.findViewById(R.id.action_right);
        title = (TextView) parent.findViewById(R.id.title_bar_title);
        layout_right = parent.findViewById(R.id.layout_right);
        layout_back = parent.findViewById(R.id.layout_back);
        
        setActionBack(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
               finish();
                
            }
        });
    }
    
    public void showRightAction(boolean show) {
        if(action_right != null) {
            action_right.setVisibility(show?View.VISIBLE:View.GONE);
        }
    }
    public void showRightAction(int resId, boolean show) {
        if(action_right != null) {
            action_right.setImageResource(resId);
            action_right.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
    
    public void setActionBack(View.OnClickListener listener) {
        if(layout_back != null) {
            layout_back.setOnClickListener(listener);
        }
    }
    
    public void setActionRight(View.OnClickListener listener) {
        if(layout_right != null) {
            layout_right.setOnClickListener(listener);
        }
    }
    
    public void setTitle(int resId) {
        if(title != null) {
            title.setText(resId);
        }
    }
    
    public void setTitle(String str) {
        if(title != null) {
            title.setText(str);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        AccountObserver.unRegisterAccountListener(this.getClass().getName());
        accountLoader = null;
        cancelListenBackToScene();
        if(marketBilling != null) {
            marketBilling.depose();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(accountLoader != null) {
            accountLoader.onActivityResult(requestCode, resultCode, data);
        }
        if(marketBilling != null) {
            marketBilling.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    public void login() {
        loginProgressDialog = LightProgressDialog.create(this, R.string.lable_being_login);
        loginProgressDialog.setCanceledOnTouchOutside(false);
        if(accountLoader == null) {
            accountLoader = AccountLoaderFactory.createAccountLoader(this);
        }
        accountLoader.login();
    }

    @Override
    public void onLogin() {
        if(loginProgressDialog != null) {
            loginProgressDialog.dismiss();
        }
        loginProgressDialog = null;
    }

    @Override
    public void onLogout() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onCancelLogin() {
        if(loginProgressDialog != null) {
            loginProgressDialog.dismiss();
        }
        loginProgressDialog = null;
        
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        cancelListenBackToScene();
    }

    @Override
    protected void onPause() {
        super.onPause();
        listenBackToScene();
    }

    private BroadcastReceiver mReceiver;
    private void cancelListenBackToScene() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    private void listenBackToScene() {
        if (mReceiver == null) {
            mReceiver = new BackToSceneReceiver();
        }
        registerReceiver(mReceiver, new IntentFilter(MarketUtils.ACTION_MARKET_THEME_APPLY));
        registerReceiver(mReceiver, new IntentFilter(MarketUtils.ACTION_BACK_TO_SCENE));
    }
    private class BackToSceneReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MarketUtils.ACTION_MARKET_THEME_APPLY.equals(action) ||
                    MarketUtils.ACTION_BACK_TO_SCENE.equals(action)) {
                try {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            finish();
                        }
                    });
                } catch(Exception e) {

                }
            }
        }

    }

    public void goBackToScene() {
        IntentUtil.sendBroadBackToScene(getApplicationContext());
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu optionMenu) {
        getMenuInflater().inflate(R.menu.basic_menu, optionMenu);
        return super.onCreateOptionsMenu(optionMenu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(ProductTabActivity.class.isInstance(this)) {
            menu.findItem(R.id.menu_refresh).setVisible(true);
        }else if(ProductLocalDetailActivity.class.isInstance(this)) {
            menu.findItem(R.id.menu_refresh).setVisible(true);
        }else if(ProductDetailActivity.class.isInstance(this)) {
            menu.findItem(R.id.menu_refresh).setVisible(true);
        }else if(UserShareListActivity.class.isInstance(this)) {
            menu.findItem(R.id.menu_refresh).setVisible(true);
        }else if(PartitionsListActivity.class.isInstance(this)) {
            menu.findItem(R.id.menu_refresh).setVisible(true);
        }else if(MarketHomeActivity.class.isInstance(this)) {
            menu.findItem(R.id.menu_refresh).setVisible(true);
        }else if(ProductListActivity.class.isInstance(this)) {
            menu.findItem(R.id.menu_refresh).setVisible(true);
        }
        if(AccountSession.isLogin()) {
            menu.findItem(R.id.menu_logout).setVisible(true);
            menu.findItem(R.id.menu_login).setVisible(false);
        }else {
            menu.findItem(R.id.menu_logout).setVisible(false);
            menu.findItem(R.id.menu_login).setVisible(true);
        }
        
//        menu.findItem(R.id.menu_refresh).setVisible(false);
        menu.findItem(R.id.menu_logout).setVisible(false);
        menu.findItem(R.id.menu_login).setVisible(false);
        menu.findItem(R.id.menu_export_wallpaper).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

  AlertDialog loginProgressDialog = null;
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
      int itemId = item.getItemId();
      if (itemId == android.R.id.home) {
          finish();
      } else if (itemId == R.id.menu_login) {
          login();
      } else if (itemId == R.id.menu_logout) {
          AccountSession.logout(getBaseContext());
      }
      return super.onOptionsItemSelected(item);
  }
  
  public void purchase(Product product, MarketPurchaseListener listener) {
      if(marketBilling == null) {
          marketBilling = MarketBillingFactory.createMarketBilling(this);
      }
      marketBilling.purchase(this,product, listener);
  }

  public void consumeAsync() {
      if(marketBilling == null) {
          marketBilling = MarketBillingFactory.createMarketBilling(this);
      }
      marketBilling.consumeAsync(this.getBaseContext());
  }

  @Override
  public void onLoging() {
      if(loginProgressDialog != null) {
          loginProgressDialog.show();
      }
        
   }
  
}
