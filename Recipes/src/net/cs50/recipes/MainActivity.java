package net.cs50.recipes;

import net.cs50.recipes.util.RecipeHelper;
import android.app.ActionBar;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;


public class MainActivity extends BaseDrawerActivity {
	
	private static String TAG = "MainActivity";
	private RecipeListFragment mFragment;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    Log.i(TAG, "Main activity created");
	    
	    getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
	    
	    Bundle args = new Bundle();
	    args.putString(RecipeListFragment.KEY_CATEGORY, RecipeHelper.Category.LATEST.toString());
	    
	    mFragment =
        		RecipeListFragment.findOrCreateFragment(getSupportFragmentManager(), R.id.content_frame, args);
	    
	}

}
