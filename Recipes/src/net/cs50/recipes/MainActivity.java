package net.cs50.recipes;

import net.cs50.recipes.provider.RecipeContract;
import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


public class MainActivity extends BaseDrawerActivity {
	
	private static String TAG = "MainActivity";
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    Log.i(TAG, "Main activity created");
	    
        RecipeListFragment fragment =
        		RecipeListFragment.findOrCreateFragment(getSupportFragmentManager(), R.id.content_frame);
	}
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Log.i(TAG, item.getTitle() + " clicked");
    	
    	switch (item.getItemId()) {
    		case R.id.menu_add:
	            Intent i = new Intent(this, CreateActivity.class);
	            startActivity(i);
	            return true;
    		case R.id.menu_refresh:
	        	Log.i(TAG, "Refreshing...");
	        	setRefreshActionButtonState(item);
	            SyncUtils.TriggerRefresh(getCurrentAccount());
	            return true;
    	}
    	
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Set the state of the Refresh button. If a sync is active, turn on the ProgressBar widget.
     * Otherwise, turn it off.
     *
     * @param refreshing True if an active sync is occurring, false otherwise
     */
    public void setRefreshActionButtonState(MenuItem item) {
        if (item != null && item.getItemId() == R.id.menu_refresh) {
        	Log.i(TAG, "set refresh state");
        	
        	Account account = getCurrentAccount();
        	
        	Log.i(TAG, account.toString());
        	// Test the ContentResolver to see if the sync adapter is active or pending.
            // Set the state of the refresh button accordingly.
            boolean syncActive = ContentResolver.isSyncActive(
            		account, RecipeContract.CONTENT_AUTHORITY);
            boolean syncPending = ContentResolver.isSyncPending(
            		account, RecipeContract.CONTENT_AUTHORITY);
            
            if (syncActive || syncPending) {
                item.setActionView(R.layout.actionbar_indeterminate_progress);
            } else {
                item.setActionView(null);
            }
        }
    }
}
