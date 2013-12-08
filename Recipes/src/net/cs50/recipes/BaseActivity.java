package net.cs50.recipes;

import net.cs50.recipes.accounts.AccountService;
import net.cs50.recipes.util.SyncUtils;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

// primary base activity -- deals with sync and account management
public abstract class BaseActivity extends FragmentActivity {

    public static final String PREFS_NAME = "prefs";

    private static String mAccessToken;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayShowTitleEnabled(false);

        SyncUtils.attachAccountManager((AccountManager) getSystemService(Context.ACCOUNT_SERVICE));

        Account[] accounts = SyncUtils.getAccountManager().getAccountsByType(
                AccountService.ACCOUNT_TYPE);
        if (accounts.length == 0) {
            SyncUtils.createSyncAccount(this);
        }

    }

    public static String getAccessToken() {
        return mAccessToken;
    }

    public static void setAccessToken(String token) {
        mAccessToken = token;
    }

    public void hideKeyboard(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}
