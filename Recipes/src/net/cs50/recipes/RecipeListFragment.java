package net.cs50.recipes;

import java.util.ArrayList;
import java.util.List;

import net.cs50.recipes.models.Recipe;
import net.cs50.recipes.provider.RecipeContract;
import net.cs50.recipes.util.RecipeHelper;
import net.cs50.recipes.util.SyncUtils;
import net.cs50.recipes.util.RecipeHelper.Category;
import net.cs50.recipes.util.RecipeHelper.RecipeAdapter;
import net.cs50.recipes.util.RecipeHelper.RecipeLoader;
import android.accounts.Account;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
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
 * List fragment for recipes
 * implemented as a fragment for flexibility reasons
 * 
 * selection of item brings user to the view article acitivity (via an intent)
 */
public class RecipeListFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<List<Recipe>> {

    private static final String TAG = "RecipeListFragment";

    // internal key for the bundle which configures the setup of the fragment
    public static final String KEY_CATEGORY = "category";

    // the list adapter for this list
    private RecipeAdapter mAdapter;

    // list of recipe objects
    private List<Recipe> mRecipes;

    // loader to handle async calls for recipes
    private RecipeLoader mRecipeLoader;

    private Menu mOptionsMenu;

    // handle to sync observer
    private Object mSyncObserverHandle;

    private OnNavigationListener mOnNavigationListener;

    private boolean mShowButtons = true;

    private CreateDialog createDialog;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon
     * screen orientation changes).
     */
    public RecipeListFragment() {
    }

    // helper static method which returns either a previously created fragment or makes a new one
    public static RecipeListFragment findOrCreateFragment(FragmentManager fm, int containerId,
            Bundle bundle) {
        Log.i(TAG, "attempting to reload old fragment");

        RecipeListFragment fragment = (RecipeListFragment) fm.findFragmentByTag(TAG);
        if (fragment == null) {
            Log.i(TAG, "no old fragment, creating a new one");
            fragment = new RecipeListFragment();
            fm.beginTransaction().replace(containerId, fragment, TAG).commit();
            
            // set arguments to be used later
            fragment.setArguments(bundle);
        } else {
        	
        	// update the category for this fragment
            fragment.updateCategory(Category.valueOf(bundle.getString(KEY_CATEGORY)));
        }
        
        return fragment;
    }

    // setup the listview for a particular category
    public void updateCategory(Category category) {
        mRecipeLoader.setCategory(category);
        if (category == RecipeHelper.Category.MY_RECIPES) {
            mShowButtons = false;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ensure persistence of data across fragment states
        setRetainInstance(true);
        
        // allow us to register option menu handles
        setHasOptionsMenu(true);

        // create an empty arraylist, to be populated later by loader
        mRecipes = new ArrayList<Recipe>();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // setup list adapter for recipes list
        mAdapter = new RecipeHelper.RecipeAdapter(getActivity(), R.layout.recipe_list_item,
                mRecipes);
        setListAdapter(mAdapter);

        // set up loader
        getLoaderManager().initLoader(0, null, this);
        setEmptyText(getText(R.string.loading));
    }

    @Override
    public void onResume() {
        super.onResume();

        mObserver.onStatusChanged(0);

        // Watch for sync state changes
        final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING
                | ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
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
        inflater.inflate(R.menu.main, menu);

        mOptionsMenu = menu;

        // on change of navigation's spinner (which allows user to select category for list)
        mOnNavigationListener = new OnNavigationListener() {
            // Get the same strings provided for the drop-down's ArrayAdapter
            String[] strings = getResources().getStringArray(R.array.home_filters_list);

            @Override
            public boolean onNavigationItemSelected(int position, long itemId) {
                String selectedString = strings[position];

                Log.i(TAG, "Updating to " + selectedString);

                if (selectedString.equals("Latest")) {
                    updateCategory(RecipeHelper.Category.LATEST);
                }
                if (selectedString.equals("Top Recipes")) {
                    updateCategory(RecipeHelper.Category.TOP);
                }
                return true;
            }
        };

        SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.home_filters_list, android.R.layout.simple_spinner_dropdown_item);

        getActivity().getActionBar().setListNavigationCallbacks(mSpinnerAdapter,
                mOnNavigationListener);
    }

    // handle action bar item clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, item.getTitle() + " clicked");

        switch (item.getItemId()) {
        case R.id.menu_add:
            createDialog = new CreateDialog();
            createDialog.setFragmentContext(this);
            createDialog.show(getFragmentManager(), CreateDialog.TAG);
            return true;
        case R.id.menu_refresh:
            Log.i(TAG, "Refreshing...");

            Account account = SyncUtils.getCurrentAccount();

            // Test the ContentResolver to see if the sync adapter is active or pending.
            // Set the state of the refresh button accordingly.
            boolean syncActive = ContentResolver.isSyncActive(account,
                    RecipeContract.CONTENT_AUTHORITY);
            boolean syncPending = ContentResolver.isSyncPending(account,
                    RecipeContract.CONTENT_AUTHORITY);

            setRefreshActionButtonState(syncActive || syncPending);
            SyncUtils.triggerRefresh(SyncUtils.getCurrentAccount());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // we receive result from an activity that we've called when creating a recipe
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Intent intent = new Intent(getActivity(), CreateActivity.class);
            switch (CreateDialog.Action.values()[requestCode]) {
            case IMAGE_CAPTURE:
                intent.setData(Uri.fromFile(createDialog.getImageFile()));
                break;
            case IMAGE_SELECT:
                intent.setData(data.getData());
                break;
            }
            startActivity(intent);
        }
        createDialog = null;
        super.onActivityResult(requestCode, resultCode, data);
    }

    // perform query of database on background thread
    @Override
    public Loader<List<Recipe>> onCreateLoader(int i, Bundle bundle) {
        Log.i(TAG, "loader created");

        Category category = Category.valueOf(getArguments().getString(KEY_CATEGORY));

        mRecipeLoader = new RecipeHelper.RecipeLoader(getActivity(), category);

        if (category == RecipeHelper.Category.MY_RECIPES) {
            mShowButtons = false;
        }
        return mRecipeLoader;
    }

    // handle changes in recipes list by updating adapter
    @Override
    public void onLoadFinished(Loader<List<Recipe>> recipeLoader, List<Recipe> recipes) {
        mAdapter.setData(recipes);
        mAdapter.notifyDataSetChanged();

        setRefreshActionButtonState(false);
    }

    // called when the observer detects changes in data (an "invalidated" data set)
    @Override
    public void onLoaderReset(Loader<List<Recipe>> recipeLoader) {
        mAdapter.notifyDataSetInvalidated();
    }

    // set up an intent for the recipe to be viewed in detail
    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Get the item at the selected position and construct the URL we'll use for our intent
        Recipe recipe = mAdapter.getItem(position);
        Uri recipeUrl = RecipeContract.BASE_CONTENT_URI.buildUpon()
                .appendPath(RecipeContract.Recipe.TABLE_NAME)
                .appendPath(Integer.toString(recipe.getId())).build();

        // create intent and start view recipe activity
        Intent i = new Intent(getActivity(), ViewRecipeActivity.class);
        i.setData(recipeUrl);
        startActivity(i);
    }

    // observer for changes in the recipe loader
    private SyncStatusObserver mObserver = new SyncStatusObserver() {
        @Override
        public void onStatusChanged(int which) {
            mRecipeLoader.onContentChanged();
        }
    };

    // set the refresh action buton state
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