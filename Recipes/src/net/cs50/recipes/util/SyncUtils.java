package net.cs50.recipes.util;

import net.cs50.recipes.accounts.AccountService;
import net.cs50.recipes.provider.RecipeContract;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

/**
 * Helper methods for working with the sync framework.
 */
public class SyncUtils {
    private static final String TAG = "SyncUtils";

    private static final long SYNC_FREQUENCY = 60 * 60; // 1 hour (in seconds)
    private static final String CONTENT_AUTHORITY = RecipeContract.CONTENT_AUTHORITY;

    private static AccountManager mAccountManager;

    private static String mAuthToken = null;

    // attach account manager for future use
    public static void attachAccountManager(AccountManager accountManager) {
        mAccountManager = accountManager;
    }

    // statically get the account manager, which is attached with above function
    public static AccountManager getAccountManager() {
        return mAccountManager;
    }

    // gets the first account with our app's ACCOUNT_TYPE
    public static Account getCurrentAccount() {
        Account[] accounts = mAccountManager.getAccountsByType(AccountService.ACCOUNT_TYPE);
        Log.i(TAG, "num accounts " + accounts.length);
        if (accounts.length > 0)
            return accounts[0];
        return null;
    }

    // make a sync account for the given application context
    public static void createSyncAccount(Context context) {

        AccountManager accountManager = (AccountManager) context
                .getSystemService(Context.ACCOUNT_SERVICE);

        // add new sync account asynchronously
        AccountManagerFuture<Bundle> future = accountManager.addAccount(
                AccountService.ACCOUNT_TYPE, AccountService.AUTH_TOKEN_TYPE, null, null,
                (Activity) context, new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        try {
                            Bundle result = future.getResult();

                            String accountName = result.getString(AccountManager.KEY_ACCOUNT_NAME);
                            String accountType = result.getString(AccountManager.KEY_ACCOUNT_TYPE);

                            Account account = new Account(accountName, accountType);

                            // this new account is syncable 
                            ContentResolver.setIsSyncable(account, CONTENT_AUTHORITY, 1);
                            
                            // set up auto sync
                            ContentResolver.setSyncAutomatically(account, CONTENT_AUTHORITY, true);
                            ContentResolver.addPeriodicSync(account, CONTENT_AUTHORITY,
                                    new Bundle(), SYNC_FREQUENCY);

                            // first pull of data for this acount
                            triggerRefresh(account);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, null);
    }

    // run a sync now!
    public static void triggerRefresh(Account account) {
        Bundle b = new Bundle();
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(account, 
                RecipeContract.CONTENT_AUTHORITY, 
                b); // extra parameters
    }

}
