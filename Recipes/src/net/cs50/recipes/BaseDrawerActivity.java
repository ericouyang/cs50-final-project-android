package net.cs50.recipes;

import net.cs50.recipes.accounts.AuthenticatorActivity;
import net.cs50.recipes.util.RecipeHelper;
import net.cs50.recipes.util.SyncUtils;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/*
 * Abstract class which creates the navigation drawer for activities which need one.
 */
public abstract class BaseDrawerActivity extends BaseActivity {

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mDrawerItems;

    private Menu mMenu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTitle = mDrawerTitle = getTitle();
        
        mDrawerItems = getResources().getStringArray(R.array.drawer_items);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item,
                mDrawerItems));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // set up action bat to use navigation drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this,
	        mDrawerLayout,
	        R.drawable.ic_drawer, 
	        R.string.drawer_open,
	        R.string.drawer_close
        ) {
            @Override
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(0);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mMenu = menu;
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// toggle the drawer using the action bar's icon position
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // click listener for nav drawer list view
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    // select the item of drawer at given position
    private void selectItem(int position) {
        String item = mDrawerItems[position];
        
        if (item.equals("About")) {
            getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            
            // change to about page
            AboutFragment.findOrCreateFragment(getSupportFragmentManager(), R.id.content_frame);
        } else if (item.equals("Home")) {
        	getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        
            // change the home page
        	Bundle args = new Bundle();
            args.putString(RecipeListFragment.KEY_CATEGORY, RecipeHelper.Category.LATEST.toString());
            RecipeListFragment.findOrCreateFragment(getSupportFragmentManager(),
                    R.id.content_frame, args);
            
        } else if (item.equals("My Recipes")) {
            getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

            Bundle args = new Bundle();
            args.putString(RecipeListFragment.KEY_CATEGORY,
                    RecipeHelper.Category.MY_RECIPES.toString());

            RecipeListFragment.findOrCreateFragment(getSupportFragmentManager(),
                    R.id.content_frame, args);
        } else if (item.equals("Logout")) {
            SyncUtils.getAccountManager().removeAccount(SyncUtils.getCurrentAccount(),
                    new AccountManagerCallback<Boolean>() {

                        @Override
                        public void run(AccountManagerFuture<Boolean> arg0) {
                            if (SyncUtils.getCurrentAccount() == null) {
                                Intent k = new Intent(getBaseContext(), AuthenticatorActivity.class);
                                startActivity(k);
                            }
                        }

                    }, null);
        }

        mDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during onPostCreate() and
     * onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // pass config changes to drawer toggle
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
}
