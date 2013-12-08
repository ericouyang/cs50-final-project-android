package net.cs50.recipes;

import net.cs50.recipes.util.RecipeHelper;
import android.app.ActionBar;
import android.os.Bundle;
import android.util.Log;

// primary activity -- configure to be the main view
public class MainActivity extends BaseDrawerActivity {

    private static String TAG = "MainActivity";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "Main activity created");

        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        // set up fragment
        Bundle args = new Bundle();
        args.putString(RecipeListFragment.KEY_CATEGORY, RecipeHelper.Category.LATEST.toString());
        RecipeListFragment.findOrCreateFragment(getSupportFragmentManager(), R.id.content_frame,
                args);

    }

}
