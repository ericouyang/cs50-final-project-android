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

import java.util.List;

import net.cs50.recipes.accounts.AccountService;
import net.cs50.recipes.provider.RecipeContract;
import net.cs50.recipes.types.Recipe;
import net.cs50.recipes.util.RecipeHelper;
import net.cs50.recipes.util.RecipeHelper.RecipeAdapter;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

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
public class RecipeListFragment extends ListFragment
implements LoaderManager.LoaderCallbacks<List<Recipe>> 
{

    private static final String TAG = "RecipeListFragment";

    private RecipeAdapter mAdapter;

    private List<Recipe> mRecipes;
    
    /**
     * Handle to a SyncObserver. The ProgressBar element is visible until the SyncObserver reports
     * that the sync is complete.
     *
     * <p>This allows us to delete our SyncObserver once the application is no longer in the
     * foreground.
     */
    private Object mSyncObserverHandle;


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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mRecipes = RecipeHelper.query(RecipeHelper.Category.LATEST, getActivity());
        
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    /**
     * Create SyncAccount at launch, if needed.
     *
     * <p>This will create a new account with the system for our application, register our
     * {@link SyncService} with it, and establish a sync schedule.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Log.i(TAG, "attaching fragment");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.i(TAG, "view created");

        mAdapter = new RecipeHelper.RecipeAdapter(getActivity(), R.layout.recipe_list_item, mRecipes);
        setListAdapter(mAdapter);
        setEmptyText(getText(R.string.loading));

        getLoaderManager().initLoader(0, null, this);
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
        // We only have one loader, so we can ignore the value of i.
        // (It'll be '0', as set in onCreate().)
        
        return new RecipeHelper.RecipeLoader(getActivity(), RecipeHelper.Category.LATEST);
        /*
        return new CursorLoader(getActivity(),  // Context
                RecipeContract.Recipe.CONTENT_URI, // URI
                PROJECTION,                // Projection
                null,                           // Selection
                null,                           // Selection args
                RecipeContract.Recipe.COLUMN_NAME_CREATED_AT + " desc"); // Sort
        */
    }
	
    
    /**
     * Move the Cursor returned by the query into the ListView adapter. This refreshes the existing
     * UI with the data in the Cursor.
     */
    
    @Override
    public void onLoadFinished(Loader<List<Recipe>> recipeLoader, List<Recipe> recipes) {
    	mAdapter.notifyDataSetChanged();
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


        /*
        // Get the link to the article represented by the item.
        String articleUrlString = c.getString(COLUMN_URL_STRING);
        if (articleUrlString == null) {
            Log.e(TAG, "Attempt to launch entry with null link");
            return;
        }

        Log.i(TAG, "Opening URL: " + articleUrlString);
        // Get a Uri object for the URL string
        Uri articleURL = Uri.parse(articleUrlString);
        Intent i = new Intent(Intent.ACTION_VIEW, articleURL);
        startActivity(i);

         */
    }

    /**
     * Crfate a new anonymous SyncStatusObserver. It's attached to the app's ContentResolver in
     * onResume(), and removed in onPause(). If status changes, it sets the state of the Refresh
     * button. If a sync is active or pending, the Refresh button is replaced by an indeterminate
     * ProgressBar; otherwise, the button itself is displayed.
     */
    private SyncStatusObserver mObserver = new SyncStatusObserver() {
        /** Callback invoked with the sync adapter status changes. */
        @Override
        public void onStatusChanged(int which) {

            getActivity().runOnUiThread(new Runnable() {
                /**
                 * The SyncAdapter runs on a background thread. To update the UI, onStatusChanged()
                 * runs on the UI thread.
                 */
                @Override
                public void run() {
                    // Create a handle to the account that was created by
                    // SyncService.CreateSyncAccount(). This will be used to query the system to
                    // see how the sync status has changed.
                    //Account account = AccountService.GetAccount();
                    //if (account == null) {
                        // GetAccount() returned an invalid value. This shouldn't happen, but
                        // we'll set the status to "not refreshing".
                    //    setRefreshActionButtonState(false);
                    //    return;
                    //}
                	
                    
                }
            });
        }
    };
}