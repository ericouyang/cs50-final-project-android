package net.cs50.recipes;

import java.util.List;

import net.cs50.recipes.types.Comment;
import net.cs50.recipes.types.Recipe;
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
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

public class ViewRecipeActivity extends BaseActivity {
    final String TAG = "ViewRecipeActivity";

    private TextView mRecipeNameView;
    private ImageView mRecipeImageView;
    private TextView mRecipeUserName;
    private TextView mRecipeNoms;
    private ListView mRecipeComments;
    private TextView mRecipeCreatedAt;
    private ListView mRecipeIngredients;
    private ListView mRecipeInstructions;
    private Recipe recipe;
    private ArrayAdapter<Comment> commentsAdapter;
    private ShareActionProvider mShareActionProvider;

    private Uri recipeUri;

    private static final int GROUP_INGREDIENTS = 0;
    private static final int GROUP_INSTRUCTIONS = 1;
    private static final int GROUP_COMMENTS = 2;

    private static final String TITLE_INGREDIENTS = "Ingredients";
    private static final String TITLE_INSTRUCTIONS = "Instructions";
    private static final String TITLE_COMMENTS = "Comments";

    LayoutInflater inflater;

    EditText titleText;
    ExpandableListView detailsListView;
    ExpandableListAdapter listAdapter;

    private List<String> ingredients;
    private List<String> instructions;
    private List<Comment> comments;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recipe);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        recipeUri = getIntent().getData();

        mRecipeNameView = (TextView) findViewById(R.id.view_recipe_name);
        mRecipeImageView = (ImageView) findViewById(R.id.view_recipe_image);
        mRecipeUserName = (TextView) findViewById(R.id.view_recipe_user_name);
        mRecipeNoms = (TextView) findViewById(R.id.view_recipe_noms);
        mRecipeCreatedAt = (TextView) findViewById(R.id.view_recipe_created_at);

        recipe = RecipeHelper.getRecipe(recipeUri, this);

        mRecipeNameView.setText(recipe.getName());
        mRecipeUserName.setText(recipe.getUserName());
        mRecipeNoms.setText(recipe.getNumLikes() + " noms");

        comments = recipe.getComments();

        mRecipeCreatedAt.setText(recipe.getCreatedAtTime().format("%b %d"));

        ingredients = recipe.getIngredients();

        instructions = recipe.getInstructions();

        String primaryImageURL = recipe.getImage(0);
        if (primaryImageURL != null) {
            ImageHelper.loadBitmap(primaryImageURL, mRecipeImageView);
        }

        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        titleText = (EditText) findViewById(R.id.text_create_title);

        detailsListView = (ExpandableListView) findViewById(R.id.view_list_details);
        detailsListView.setScrollContainer(false);
        listAdapter = new ExpandableListAdapter();
        detailsListView.setAdapter(listAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_view_recipe, menu);
        MenuItem item = menu.findItem(R.id.menu_share);

        mShareActionProvider = (ShareActionProvider) item.getActionProvider();
        mShareActionProvider.setShareIntent(getDefaultShareIntent());
        return super.onCreateOptionsMenu(menu);
    }

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

        case R.id.menu_nom:
            String text;
            if (recipe.toggleLike() == true) {
                text = "You have nom'ed this recipe!";
            } else {
                text = "You have un-nom'ed this recipe.";
            }

            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();

            return true;

        case R.id.menu_comment:
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            // Use an EditText view to get user input.
            final EditText input = new EditText(this);
            input.setId(0);
            alertDialogBuilder.setView(input);

            // set dialog message
            alertDialogBuilder.setMessage("Comment on recipe:").setCancelable(false)
                    .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            // if this button is clicked, close
                            // current activity
                            String inputString = input.getText().toString();

                            recipe.addComment(inputString);
                            Log.i(TAG, inputString);
                            listAdapter.notifyDataSetChanged();

                            new HttpHelper.AddCommentAsyncTask().execute(getContentResolver(),
                                    recipeUri, recipe.getRecipeId(), inputString);

                            String text = "You have commented on this recipe!";

                            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
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

        @Override
        public View getChildView(int groupPosition, final int childPosition, boolean isLastChild,
                View convertView, ViewGroup parent) {
            switch (groupPosition) {
            case GROUP_INGREDIENTS:
            case GROUP_INSTRUCTIONS:
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.view_list_item, null);
                }
                final String itemText = (String) getChild(groupPosition, childPosition);
                TextView listItem = (TextView) convertView.findViewById(R.id.text_list_item);
                listItem.setText(itemText);
                break;
            case GROUP_COMMENTS:
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.view_list_item, null);
                }
                final Comment comment = (Comment) getChild(groupPosition, childPosition);
                TextView commentItem = (TextView) convertView.findViewById(R.id.text_list_item);
                commentItem.setText(comment.getContent() + " by " + comment.getUserName() + " on "
                        + comment.getCreatedAt());
                break;
            }

            return convertView;
        }

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

        @Override
        public Object getGroup(int groupPosition) {
            switch (groupPosition) {
            case GROUP_INGREDIENTS:
                return TITLE_INGREDIENTS;
            case GROUP_INSTRUCTIONS:
                return TITLE_INSTRUCTIONS;
            case GROUP_COMMENTS:
                return TITLE_COMMENTS;
            default:
                throw new IllegalArgumentException();
            }
        }

        @Override
        public int getGroupCount() {
            return 3;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

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
            return true;
        }
    }

}
