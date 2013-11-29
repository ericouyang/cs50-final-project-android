package net.cs50.recipes;

import java.util.Locale;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class MainActivity extends FragmentActivity {
	
	private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mDrawerItems;
    
    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_main);

        mTitle = mDrawerTitle = getTitle();
        mDrawerItems = getResources().getStringArray(R.array.drawer_items);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mDrawerItems));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(0);
        }
        
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        // Create the adapter that will return a fragment for each of the three
    	// primary sections of the app.
    	mSectionsPagerAdapter = new SectionsPagerAdapter(
    		getSupportFragmentManager());
    	
    	// Set up the ViewPager with the sections adapter.
    	mViewPager = (ViewPager) findViewById(R.id.pager);
    	mViewPager.setAdapter(mSectionsPagerAdapter);

    	// When swiping between different sections, select the corresponding
    	// tab. We can also use ActionBar.Tab#select() to do this if we have
    	// a reference to the Tab.
    	mViewPager
    		.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
    		    @Override
    		    public void onPageSelected(int position) {
    			actionBar.setSelectedNavigationItem(position);
    		    }
    		});
    	
    	// Create a tab listener that is called when the user changes tabs.
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
        	
        	@Override
            public void onTabSelected(Tab tab, android.app.FragmentTransaction ft) {
            	// When the given tab is selected, switch to the corresponding page in
        		// the ViewPager.
        		mViewPager.setCurrentItem(tab.getPosition());
            }

			@Override
			public void onTabReselected(Tab tab,
					android.app.FragmentTransaction ft) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTabUnselected(Tab tab,
					android.app.FragmentTransaction ft) {
				// TODO Auto-generated method stub
				
			}
        };
        
    	// For each of the sections in the app, add a tab to the action bar.
    	for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
    	    // Create a tab with text corresponding to the page title defined by
    	    // the adapter. Also specify this Activity object, which implements
    	    // the TabListener interface, as the callback (listener) for when
    	    // this tab is selected.
    	    actionBar.addTab(actionBar.newTab()
    		    .setText(mSectionsPagerAdapter.getPageTitle(i))
    		    .setTabListener(tabListener));
    	}
    	
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        
        Fragment fragment = new RecipeListFragment();
        fragmentTransaction.add(R.id.content_frame, fragment);
        fragmentTransaction.commit();
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         // The action bar home/up action should open or close the drawer.
         // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch(item.getItemId()) {
        /*
        case R.id.action_websearch:
            // create intent to perform web search for this planet
            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
            intent.putExtra(SearchManager.QUERY, getActionBar().getTitle());
            // catch event that there's no activity to handle intent
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, R.string.app_not_available, Toast.LENGTH_LONG).show();
            }
            return true;
        */
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
    	/*
        // update the main content by replacing fragments
        Fragment fragment = new PlanetFragment();
        Bundle args = new Bundle();
        args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
        fragment.setArguments(args);

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mDrawerItems[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
        */
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
		    super(fm);
		}
	
		@Override
		public Fragment getItem(int position) {
		    // getItem is called to instantiate the fragment for the given page.
		    // Return a DummySectionFragment (defined as a static inner class
		    // below) with the page number as its lone argument.
		    Fragment fragment = new DummySectionFragment();
		    Bundle args = new Bundle();
		    args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
		    fragment.setArguments(args);
		    return fragment;
		}
	
		@Override
		public int getCount() {
		    // Show 3 total pages.
		    return 3;
		}
	
		@Override
		public CharSequence getPageTitle(int position) {
		    Locale l = Locale.getDefault();
		    switch (position) {
		    case 0:
			return getString(R.string.title_section1).toUpperCase(l);
		    case 1:
			return getString(R.string.title_section2).toUpperCase(l);
		    case 2:
			return getString(R.string.title_section3).toUpperCase(l);
		    }
		    return null;
		}
	    }
	
	    /**
	     * A dummy fragment representing a section of the app, but that simply
	     * displays dummy text.
	     */
	    public static class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";
	
		public DummySectionFragment() {
		}
	
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		    View rootView = inflater.inflate(R.layout.fragment_home_dummy,
			    container, false);
		    TextView dummyTextView = (TextView) rootView
			    .findViewById(R.id.section_label);
		    dummyTextView.setText(Integer.toString(getArguments().getInt(
			    ARG_SECTION_NUMBER)));
		    return rootView;
		}
    }
    
    /**
     * Fragment that appears in the "content_frame", shows a planet
     */
    /*
    public static class PlanetFragment extends Fragment {
        public static final String ARG_PLANET_NUMBER = "planet_number";

        public PlanetFragment() {
            // Empty constructor required for fragment subclasses
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_planet, container, false);
            int i = getArguments().getInt(ARG_PLANET_NUMBER);
            String planet = getResources().getStringArray(R.array.planets_array)[i];

            int imageId = getResources().getIdentifier(planet.toLowerCase(Locale.getDefault()),
                            "drawable", getActivity().getPackageName());
            ((ImageView) rootView.findViewById(R.id.image)).setImageResource(imageId);
            getActivity().setTitle(planet);
            return rootView;
        }
    }
    */
}
