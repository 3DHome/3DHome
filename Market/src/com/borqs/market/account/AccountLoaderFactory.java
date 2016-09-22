package com.borqs.market.account;

import android.app.Activity;

import com.borqs.market.account.google.GoogleAccountLoader;

public class AccountLoaderFactory {

    public static AccountLoader createAccountLoader(Activity activity) {
        return new GoogleAccountLoader(activity);
//        return new PhoneAccountLoader(activity);
    }

}
