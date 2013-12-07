package net.cs50.recipes;

import android.os.Bundle;
import android.util.Log;


public class MainActivity extends BaseDrawerActivity {
	
	private static String TAG = "MainActivity";
	private RecipeListFragment mFragment;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    Log.i(TAG, "Main activity created");
	    
	    mFragment =
        		RecipeListFragment.findOrCreateFragment(getSupportFragmentManager(), R.id.content_frame);
	    
	}

}
