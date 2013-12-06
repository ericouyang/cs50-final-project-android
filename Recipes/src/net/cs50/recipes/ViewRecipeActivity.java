package net.cs50.recipes;

import net.cs50.recipes.types.Recipe;
import net.cs50.recipes.util.ImageHelper;
import net.cs50.recipes.util.RecipeHelper;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ViewRecipeActivity extends BaseActivity {
	
	final Context context = this;
    private TextView mRecipeNameView;
    private ImageView mRecipeImageView;
    private TextView mRecipeUserName;
    private TextView mRecipeNoms;
    private ListView mRecipeComments;
    private TextView mRecipeCreatedAt;
    private ListView mRecipeIngredients;
    private ListView mRecipeInstructions;
    private Recipe recipe;
    private Button commentButton;
    
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recipe);
        
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        
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
        
        mRecipeNameView = (TextView) findViewById(R.id.view_recipe_name);
        mRecipeImageView = (ImageView) findViewById(R.id.view_recipe_image);
        mRecipeUserName = (TextView) findViewById(R.id.view_recipe_user_name);
        mRecipeNoms = (TextView) findViewById(R.id.view_recipe_noms);
        mRecipeComments = (ListView) findViewById(R.id.view_recipe_comments);
        mRecipeCreatedAt = (TextView) findViewById(R.id.view_recipe_created_at);
        mRecipeIngredients = (ListView) findViewById(R.id.view_recipe_ingredients);
        mRecipeInstructions = (ListView) findViewById(R.id.view_recipe_instructions);
        
        recipe = RecipeHelper.getRecipe(resUri, this);
        
        mRecipeNameView.setText(recipe.getName());
        mRecipeUserName.setText("Fred!"); /*
        mRecipeNoms.setText(recipe.getName());
        mRecipeComments.setText(recipe.getComments());
        
        long createdAt = recipe.getCreatedAt();
        mRecipeCreatedAt.setText(String.valueOf(createdAt));
        
        List<String> ingredients = recipe.getIngredients();
        mRecipeIngredients.setText(recipe.getIngredients());
        
        List<String> instructions = recipe.getInstructions();
        mRecipeInstructions.setText(recipe.getInstructions()); */
        
        ImageHelper.loadBitmap(recipe.getImage(0), mRecipeImageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_view_recipe, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       
        // Handle action buttons
        switch(item.getItemId()) {
        
        case R.id.menu_comment:
        	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
    				context);
     
    			// set title
    			//alertDialogBuilder.setTitle("Comment on Recipe");
    			
    			// Use an EditText view to get user input.
    	         final EditText input = new EditText(this);
    	         input.setId(0);
    	         alertDialogBuilder.setView(input);
    	         
    	         
    			// set dialog message
    			alertDialogBuilder
    				.setMessage("Comment on recipe:")
    				.setCancelable(false)
    				.setPositiveButton("Send",new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog,int id) {
    						// if this button is clicked, close
    						// current activity
    						recipe.addComment("test comment");
    						ViewRecipeActivity.this.finish();
    					}
    				  })
    				.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
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
    				return true;
    		
        default:
            return super.onOptionsItemSelected(item);
        }
    }


}
