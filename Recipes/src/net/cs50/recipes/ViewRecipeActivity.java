package net.cs50.recipes;

import java.util.List;

import net.cs50.recipes.models.Comment;
import net.cs50.recipes.models.Recipe;
import net.cs50.recipes.util.HttpHelper;
import net.cs50.recipes.util.ImageHelper;
import net.cs50.recipes.util.RecipeHelper;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

public class ViewRecipeActivity extends BaseActivity {
    final String TAG = "ViewRecipeActivity";

    // the recipe we're viewing
    private Recipe recipe;
    private Uri recipeUri;
    
    // fields for the recipe view
    private TextView mRecipeNameView;
    private ImageView mRecipeImageView;
    private TextView mRecipeUserName;
    private TextView mRecipeCreatedAt;
    
    // lists of data for this recipes
    private List<String> ingredients;
    private List<String> instructions;
    private List<Comment> comments;
    
    // provider so that we can share the recipe
    private ShareActionProvider mShareActionProvider;

    // groups for the expandable list view and corresponding titles
    private static final int GROUP_INGREDIENTS = 0;
    private static final int GROUP_INSTRUCTIONS = 1;
    private static final int GROUP_COMMENTS = 2;
    private static final String TITLE_INGREDIENTS = "Ingredients";
    private static final String TITLE_INSTRUCTIONS = "Instructions";
    private static final String TITLE_COMMENTS = "Comments";

    // layout inflater
    private LayoutInflater inflater;

    // store instance reference to list view and adapter
    private ExpandableListView detailsListView;
    private ExpandableListAdapter listAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_recipe);

        // store instance reference to recipe being viewed
        recipeUri = getIntent().getData();
        recipe = RecipeHelper.getRecipe(recipeUri, this);
        
        // setup actionbar
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // set up expandable list view for instructions, ingredients, and comments
        detailsListView = (ExpandableListView) findViewById(R.id.view_list_details);
        View header = inflater.inflate(R.layout.view_list_header, null);
        detailsListView.addHeaderView(header);
        listAdapter = new ExpandableListAdapter();
        detailsListView.setAdapter(listAdapter);
        
        // assign views to instance variables
        mRecipeNameView = (TextView) header.findViewById(R.id.view_recipe_name);
        mRecipeImageView = (ImageView) header.findViewById(R.id.view_recipe_image);
        mRecipeUserName = (TextView) header.findViewById(R.id.view_recipe_user_name);
        mRecipeCreatedAt = (TextView) header.findViewById(R.id.view_recipe_created_at);
       
        // set view to data
        mRecipeNameView.setText(recipe.getName());
        mRecipeUserName.setText(recipe.getUserName());
        mRecipeCreatedAt.setText(recipe.getCreatedAtTime().format("%b %d"));

        // set lists to data
        ingredients = recipe.getIngredients();
        instructions = recipe.getInstructions();
        comments = recipe.getComments();

        // async call to load bitmap into view
        String primaryImageURL = recipe.getImage(0);
        if (primaryImageURL != null) {
            ImageHelper.loadBitmap(primaryImageURL, mRecipeImageView);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_view_recipe, menu);
        MenuItem item = menu.findItem(R.id.menu_share);

        // set up share action button
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();
        mShareActionProvider.setShareIntent(getDefaultShareIntent());
        return super.onCreateOptionsMenu(menu);
    }

    // helper method to set up intent to share recipe
    private Intent getDefaultShareIntent() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = "Check out the " + recipe.getName() + " recipe on the nom! app!";
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, recipe.getName());
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);

        return sharingIntent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

        // user clicked on the "nom!" (like) button
        case R.id.menu_nom:
            String text;
            if (recipe.toggleLike() == true) {
                text = "You have nom'ed this recipe!";
            } else {
                text = "You have un-nom'ed this recipe.";
            }

            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();

            return true;

    	// user clicked on the comment button
        case R.id.menu_comment:
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            final EditText input = new EditText(this);
            input.setId(0);
            alertDialogBuilder.setView(input);

            // setup dialog message for commenting on recipe
            alertDialogBuilder.setMessage("Comment on recipe:").setCancelable(true)
                    .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                        	// get string from text field
                            String inputString = input.getText().toString();

                            // add comment to recipe object and notify of change to list adapter
                            recipe.addComment(inputString);
                            listAdapter.notifyDataSetChanged();

                            // async task for pinging server to add comment to recipe 
                            new HttpHelper.AddCommentAsyncTask().execute(getContentResolver(),
                                    recipeUri, recipe.getRecipeId(), inputString);

                            // make toast to tell user that comment was succesful
                            String text = "You have commented on this recipe!";
                            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            // close dialog box when cancel button clicked
                            dialog.cancel();
                        }
                    });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show the dialog box
            alertDialog.show();
            return true;

        default:
            return super.onOptionsItemSelected(item);
        }
    }

    // set up expandable list adapter for the recipe details (instructions, ingredients, comments)
    public class ExpandableListAdapter extends BaseExpandableListAdapter {

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            switch (groupPosition) {
            case GROUP_INGREDIENTS:
                return ingredients.get(childPosition);
            case GROUP_INSTRUCTIONS:
                return instructions.get(childPosition);
            case GROUP_COMMENTS:
                return comments.get(childPosition);
            default:
                throw new IllegalArgumentException();
            }
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        // setup the specific row for the list
        @Override
        public View getChildView(int groupPosition, final int childPosition, boolean isLastChild,
                View convertView, ViewGroup parent) {
        	
        	if (convertView == null) {
                convertView = inflater.inflate(R.layout.view_list_item, null);
            }
            
            TextView listItem = (TextView) convertView.findViewById(R.id.text_list_item);
            
            switch (groupPosition) {
            case GROUP_INGREDIENTS:
            case GROUP_INSTRUCTIONS:
            	String itemText = (String) getChild(groupPosition, childPosition);
                listItem.setText(itemText);
                break;
            case GROUP_COMMENTS:
            	Comment comment = (Comment) getChild(groupPosition, childPosition);
            	
            	// customize content a bit for comment
            	listItem.setText("\"" + comment.getContent() + "\" -" + comment.getUserName());
                break;
            }

            return convertView;
        }

        // get total number of items in the list
        @Override
        public int getChildrenCount(int groupPosition) {
            switch (groupPosition) {
            case GROUP_INGREDIENTS:
                return ingredients.size();
            case GROUP_INSTRUCTIONS:
                return instructions.size();
            case GROUP_COMMENTS:
                return comments.size();
            default:
                throw new IllegalArgumentException();
            }
        }

        // what's the group at the given position
        @Override
        public Object getGroup(int groupPosition) {
            switch (groupPosition) {
            case GROUP_INGREDIENTS:
                return TITLE_INGREDIENTS;
            case GROUP_INSTRUCTIONS:
                return TITLE_INSTRUCTIONS;
            case GROUP_COMMENTS:
                return TITLE_COMMENTS + " (" + comments.size() + ")";
            default:
                throw new IllegalArgumentException();
            }
        }

        // how many groups?
        @Override
        public int getGroupCount() {
            return 3;
        }

        // let the group id be its relative position
        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        // get the specific view for the group
        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                ViewGroup parent) {
            String groupTitle = (String) getGroup(groupPosition);

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.view_list_group, null);
            }

            TextView textListGroup = (TextView) convertView.findViewById(R.id.text_list_group);
            textListGroup.setText(groupTitle);

            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
        	// no action on the list elements required, just for displaying
            return false;
        }
    }

}
