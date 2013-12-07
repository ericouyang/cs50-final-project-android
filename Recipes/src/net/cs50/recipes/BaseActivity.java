package net.cs50.recipes;

import net.cs50.recipes.accounts.AccountService;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public abstract class BaseActivity extends FragmentActivity {
	
	private final String TAG = "BaseActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    getActionBar().setDisplayShowTitleEnabled(false);
	    
	    SyncUtils.attachAccountManager((AccountManager) this.getSystemService(Context.ACCOUNT_SERVICE));
	    
    	Account[] accounts = SyncUtils.getAccountManager().getAccountsByType(AccountService.ACCOUNT_TYPE);
    	if (accounts.length == 0)
    	{
    		SyncUtils.CreateSyncAccount(this);
    	}
	}
}
