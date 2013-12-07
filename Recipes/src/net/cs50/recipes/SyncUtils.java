/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.cs50.recipes;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncStatusObserver;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import net.cs50.recipes.accounts.AccountService;
import net.cs50.recipes.provider.RecipeContract;

/**
 * Static helper methods for working with the sync framework.
 */
public class SyncUtils {
    private static final long SYNC_FREQUENCY = 60 * 60;  // 1 hour (in seconds)
    private static final String CONTENT_AUTHORITY = RecipeContract.CONTENT_AUTHORITY;

    /**
     * Create an entry for this application in the system account list, if it isn't already there.
     *
     * @param context Context
     */
    public static void CreateSyncAccount(Context context) {
    	
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        
        AccountManagerFuture<Bundle> future = accountManager.addAccount(AccountService.ACCOUNT_TYPE, AccountService.AUTH_TOKEN_TYPE, null, null, (Activity) context, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    Bundle result = future.getResult();
                    
                    String accountName = result.getString(AccountManager.KEY_ACCOUNT_NAME);
                    String accountType = result.getString(AccountManager.KEY_ACCOUNT_TYPE);	
                    
                    Account account = new Account(accountName, accountType);
                    
                    // Inform the system that this account supports sync
                    ContentResolver.setIsSyncable(account, CONTENT_AUTHORITY, 1);
                    // Inform the system that this account is eligible for auto sync when the network is up
                    ContentResolver.setSyncAutomatically(account, CONTENT_AUTHORITY, true);
                    // Recommend a schedule for automatic synchronization. The system may modify this based
                    // on other scheduled syncs and network utilization.
                    ContentResolver.addPeriodicSync(
                            account, CONTENT_AUTHORITY, new Bundle(),SYNC_FREQUENCY);
                    
                    Log.d("SyncUtils", "AddNewAccount Bundle is " + result);

                    TriggerRefresh(account);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, null);
    }

    /**
     * Helper method to trigger an immediate sync ("refresh").
     *
     * <p>This should only be used when we need to preempt the normal sync schedule. Typically, this
     * means the user has pressed the "refresh" button.
     *
     * Note that SYNC_EXTRAS_MANUAL will cause an immediate sync, without any optimization to
     * preserve battery life. If you know new data is available (perhaps via a GCM notification),
     * but the user is not actively waiting for that data, you should omit this flag; this will give
     * the OS additional freedom in scheduling your sync request.
     */
    public static void TriggerRefresh(Account account) {
        Bundle b = new Bundle();
        // Disable sync backoff and ignore sync preferences. In other words...perform sync NOW!
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(
                account,      // Sync account
                RecipeContract.CONTENT_AUTHORITY, // Content authority
                b);                                      // Extras
    }
    
}
