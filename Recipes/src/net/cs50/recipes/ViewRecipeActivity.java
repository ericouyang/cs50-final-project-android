package net.cs50.recipes;

import net.cs50.recipes.provider.RecipeContract;
import net.cs50.recipes.types.Recipe;
import net.cs50.recipes.util.ImageHelper;
import net.cs50.recipes.util.RecipeHelper;
import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.content.Context;
import android.content.DialogInterface;
import android.app.AlertDialog;

public class ViewRecipeActivity extends Activity {
	
	final Context context = this;
    //private Cursor mCursor;
    private TextView mRecipeNameView;
    private ImageView mRecipeImageView;
    private TextView mRecipeUserName;
    
    private Button commentButton;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recipe);
        
        /*
        commentButton = (Button) findViewById(R.id.buttonAlert);
        
		// add button listener
		commentButton.setOnClickListener(new OnClickListener() {
 
		@Override
		public void onClick(View arg0) {
 
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);
 
			// set title
			alertDialogBuilder.setTitle("Your Title");
 
			// set dialog message
			alertDialogBuilder
				.setMessage("Click yes to exit!")
				.setCancelable(false)
				.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						// if this button is clicked, close
						// current activity
						ViewRecipeActivity.this.finish();
					}
				  })
				.setNegativeButton("No",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						// if this button is clicked, just close
						// the dialog box and do nothing
						dialog.cancel();
					}
				});
 
				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
 
				// show it
				alertDialog.show();
			}

		
		});
        */
        
        Uri resUri = getIntent().getData();

        /*
        mCursor = getContentResolver().query(
                resUri,
                RecipeContract.Recipe.PROJECTION_ALL_FIELDS,
                null,
                null,
                null);// The sort order for the returned rows

		mCursor.moveToFirst();
		*/
        
        mRecipeNameView = (TextView) findViewById(R.id.view_recipe_name);
        mRecipeImageView = (ImageView) findViewById(R.id.view_recipe_image);
        mRecipeUserName = (TextView) findViewById(R.id.view_recipe_user_name);
        
        Recipe recipe = RecipeHelper.getRecipe(resUri, this);
        
        mRecipeNameView.setText(recipe.getName());
        mRecipeUserName.setText("Fred!");
        
        ImageHelper.loadBitmap(recipe.getImage(0), mRecipeImageView);
    }




}
