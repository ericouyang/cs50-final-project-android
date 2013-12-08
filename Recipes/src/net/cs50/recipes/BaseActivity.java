package net.cs50.recipes;

import net.cs50.recipes.accounts.AccountService;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public abstract class BaseActivity extends FragmentActivity {

    private final String TAG = "BaseActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayShowTitleEnabled(false);

        SyncUtils.attachAccountManager((AccountManager) getSystemService(Context.ACCOUNT_SERVICE));

        Account[] accounts = SyncUtils.getAccountManager().getAccountsByType(
                AccountService.ACCOUNT_TYPE);
        if (accounts.length == 0) {
            SyncUtils.CreateSyncAccount(this);
        }
    }

    public void hideKeyboard(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}
