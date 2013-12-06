package net.cs50.recipes;

import net.cs50.recipes.accounts.AccountService;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public abstract class BaseActivity extends FragmentActivity {
	
	private AccountManager mAccountManager;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    getActionBar().setDisplayShowTitleEnabled(false);
	    
	    // Create account, if needed
    	mAccountManager = (AccountManager) this.getSystemService(Context.ACCOUNT_SERVICE);
    	Account[] accounts = mAccountManager.getAccountsByType(AccountService.ACCOUNT_TYPE);
    	if (accounts.length == 0)
    	{
    		SyncUtils.CreateSyncAccount(this);
    	}
	}
	
	public Account getCurrentAccount()
	{
		Account[] accounts = mAccountManager.getAccountsByType(AccountService.ACCOUNT_TYPE);
		return accounts[0];
	}
}
