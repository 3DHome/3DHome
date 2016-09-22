package com.borqs.market.account;


public interface AccountListener {
    void onLogin();
    void onLoging();
    void onLogout();
	void onCancelLogin();
}
