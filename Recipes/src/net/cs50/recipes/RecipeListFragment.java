/*
 * Copyright 2013 The Android Open Source Project
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

import java.util.ArrayList;
import java.util.List;

import net.cs50.recipes.provider.RecipeContract;
import net.cs50.recipes.types.Recipe;
import net.cs50.recipes.util.RecipeHelper;
import net.cs50.recipes.util.RecipeHelper.Category;
import net.cs50.recipes.util.RecipeHelper.RecipeAdapter;
import net.cs50.recipes.util.RecipeHelper.RecipeLoader;
import android.accounts.Account;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SpinnerAdapter;

/**
 * List fragment containing a list of Atom entry objects (articles) stored in the local database.
 *
 * <p>Database access is mediated by a content provider, specified in
 * {@link com.example.android.network.sync.basicsyncadapter.provider.FeedProvider}. This content
 * provider is
 * automatically populated by  {@link SyncService}.
 *
 * <p>Selecting an item from the displayed list displays the article in the default browser.
 *
 * <p>If the content provider doesn't return any data, then the first sync hasn't run yet. This sync
 * adapter assumes data exists in the provider once a sync has run. If your app doesn't work like
 * this, you should add a flag that notes if a sync has run, so you can differentiate between "no
 * available data" and "no initial sync", and display this in the UI.
 *
 * <p>The ActionBar displays a "Refresh" button. When the user clicks "Refresh", the sync adapter
 * runs immediately. An indeterminate ProgressBar element is displayed, showing that the sync is
 * occurring.
 */
public class RecipeListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<Recipe>> 
{

    private static final String TAG = "RecipeListFragment";

    private RecipeAdapter mAdapter;

    private List<Recipe> mRecipes;
    
    private RecipeLoader mRecipeLoader;
    
    private Menu mOptionsMenu;
    
    /**
     * Handle to a SyncObserver. The ProgressBar element is visible until the SyncObserver reports
     * that the sync is complete.
     *
     * <p>This allows us to delete our SyncObserver once the application is no longer in the
     * foreground.
     */
    private Object mSyncObserverHandle;

    private OnNavigationListener mOnNavigationListener;
    
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RecipeListFragment() {}

    public static RecipeListFragment findOrCreateFragment(FragmentManager fm, int containerId) {
        Log.i(TAG, "attempting to reload old fragment");
        
        RecipeListFragment fragment = (RecipeListFragment) fm.findFragmentByTag(TAG);
        if (fragment == null) {
            Log.i(TAG, "no old fragment, creating a new one");
            fragment = new RecipeListFragment();
            fm.beginTransaction().add(containerId, fragment, TAG).commit();
        }
        
        return fragment;
    }

    public void updateCategory(Category category)
    {
    	mRecipeLoader.setCategory(category);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setRetainInstance(true);
        setHasOptionsMenu(true);
        
        mRecipes = new ArrayList<Recipe>();
    }

    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        mAdapter = new RecipeHelper.RecipeAdapter(getActivity(), R.layout.recipe_list_item, mRecipes);
    	
        setListAdapter(mAdapter);
        
        getLoaderManager().initLoader(0, null, this);
        
    	setEmptyText(getText(R.string.loading));
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        mObserver.onStatusChanged(0);
        
        // Watch for sync state changes
        final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING |
                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
        mSyncObserverHandle = ContentResolver.addStatusChangeListener(mask, mObserver);
        
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSyncObserverHandle != null) {
            ContentResolver.removeStatusChangeListener(mSyncObserverHandle);
            mSyncObserverHandle = null;
        }
    }

    /**
     * Create the ActionBar.
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mOptionsMenu = menu;
        inflater.inflate(R.menu.main, menu);
        
        mOnNavigationListener = new OnNavigationListener() {
			  // Get the same strings provided for the drop-down's ArrayAdapter
			  String[] strings = getResources().getStringArray(R.array.home_filters_list);

			  @Override
			  public boolean onNavigationItemSelected(int position, long itemId) {
				String selectedString = strings[position];
				
				Log.i(TAG, "Updating to " + selectedString);
				
				if (selectedString.equals("Latest"))
				{
					updateCategory(RecipeHelper.Category.LATEST);
				}
				if (selectedString.equals("Top Recipes"))
				{
					updateCategory(RecipeHelper.Category.TOP);
				}
			    return true;
			  }
			};
			
	    SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.home_filters_list,
	            android.R.layout.simple_spinner_dropdown_item);
	    
	    getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
	    
	    getActivity().getActionBar().setListNavigationCallbacks(mSpinnerAdapter, mOnNavigationListener);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Log.i(TAG, item.getTitle() + " clicked");
    	
    	switch (item.getItemId()) {
	    	case R.id.menu_add:
	            Intent i = new Intent(getActivity(), CreateActivity.class);
	            startActivity(i);
	            return true;
    		case R.id.menu_refresh:
	        	Log.i(TAG, "Refreshing...");
	        	
	        	Account account = SyncUtils.getCurrentAccount();
	        	
	        	// Test the ContentResolver to see if the sync adapter is active or pending.
	            // Set the state of the refresh button accordingly.
	            boolean syncActive = ContentResolver.isSyncActive(
	            		account, RecipeContract.CONTENT_AUTHORITY);
	            boolean syncPending = ContentResolver.isSyncPending(
	            		account, RecipeContract.CONTENT_AUTHORITY);
	            
	        	setRefreshActionButtonState(syncActive || syncPending);
	            SyncUtils.TriggerRefresh(SyncUtils.getCurrentAccount());
	            return true;
    	}
    	
        return super.onOptionsItemSelected(item);
    }
    /**
     * Query the content provider for data.
     *
     * <p>Loaders do queries in a background thread. They also provide a ContentObserver that is
     * triggered when data in the content provider changes. When the sync adapter updates the
     * content provider, the ContentObserver responds by resetting the loader and then reloading
     * it.
     */
    @Override
    public Loader<List<Recipe>> onCreateLoader(int i, Bundle bundle) {

        Log.i(TAG, "loader created");
        
        mRecipeLoader = new RecipeHelper.RecipeLoader(getActivity(), RecipeHelper.Category.LATEST);
        
        return mRecipeLoader;
    }
	
    
    /**
     * Move the Cursor returned by the query into the ListView adapter. This refreshes the existing
     * UI with the data in the Cursor.
     */
    @Override
    public void onLoadFinished(Loader<List<Recipe>> recipeLoader, List<Recipe> recipes) {
    	mAdapter.setData(recipes);
    	mAdapter.notifyDataSetChanged();
    	
    	setRefreshActionButtonState(false);
    }
	
    /**
     * Called when the ContentObserver defined for the content provider detects that data has
     * changed. The ContentObserver resets the loader, and then re-runs the loader. In the adapter,
     * set the Cursor value to null. This removes the reference to the Cursor, allowing it to be
     * garbage-collected.
     */
    
    @Override
    public void onLoaderReset(Loader<List<Recipe>> recipeLoader) {
        mAdapter.notifyDataSetInvalidated();
    }

    

    /**
     * Load an article in the default browser when selected by the user.
     */
    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Get a URI for the selected item, then start an Activity that displays the URI. Any
        // Activity that filters for ACTION_VIEW and a URI can accept this. In most cases, this will
        // be a browser.

        // Get the item at the selected position, in the form of a Cursor.
        Recipe recipe = mAdapter.getItem(position);
        Uri recipeUrl = RecipeContract.BASE_CONTENT_URI.buildUpon()
                .appendPath(RecipeContract.Recipe.TABLE_NAME)
                .appendPath(Integer.toString(recipe.getId()))
                .build();

        Intent i = new Intent(getActivity(), ViewRecipeActivity.class);
        i.setData(recipeUrl);
        startActivity(i);
    }

    /**
     * Crate a new anonymous SyncStatusObserver. It's attached to the app's ContentResolver in
     * onResume(), and removed in onPause(). If status changes, it sets the state of the Refresh
     * button. If a sync is active or pending, the Refresh button is replaced by an indeterminate
     * ProgressBar; otherwise, the button itself is displayed.
     */
    private SyncStatusObserver mObserver = new SyncStatusObserver() {
        @Override
        public void onStatusChanged(int which) {
        	mRecipeLoader.onContentChanged();
        }
    };
    
    /**
     * Set the state of the Refresh button. If a sync is active, turn on the ProgressBar widget.
     * Otherwise, turn it off.
     *
     * @param refreshing True if an active sync is occurring, false otherwise
     */
    public void setRefreshActionButtonState(boolean refreshing) {
    	if (mOptionsMenu == null) {
            return;
        }

        final MenuItem refreshItem = mOptionsMenu.findItem(R.id.menu_refresh);
        if (refreshItem != null) {
            if (refreshing) {
                refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
            } else {
                refreshItem.setActionView(null);
            }
        }
    }
}