package net.cs50.recipes.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.cs50.recipes.R;
import net.cs50.recipes.provider.RecipeContract;
import net.cs50.recipes.types.Recipe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RecipeHelper {

    private static String TAG = "RecipeHelper";

    public enum Category {
        TOP, LATEST, MY_RECIPES
    }

    private RecipeHelper() {
    }

    public static List<Recipe> parse(InputStream in) throws IOException, JSONException {
        List<Recipe> list = new LinkedList<Recipe>();
        JSONArray recipes = (JSONArray) new JSONTokener(HttpHelper.getString(in)).nextValue();
        for (int i = 0, len = recipes.length(); i < len; i++) {
            JSONObject recipe = recipes.getJSONObject(i);
            list.add(getRecipe(recipe));
        }
        return list;
    }

    public static Recipe getRecipe(JSONObject recipe) throws JSONException {
        String recipeId = recipe.getString("id");
        String name = recipe.getString("name");
        int likes = recipe.getInt("votes");
        Log.i(TAG, "num likes " + likes);
        long createdAt = recipe.getLong("createdAt");
        long updatedAt = recipe.getLong("updatedAt");

        Recipe r = new Recipe(recipeId, name, likes, createdAt, updatedAt);

        JSONArray images = recipe.optJSONArray("images");
        if (images != null) {
            for (int j = 0, imagesLen = images.length(); j < imagesLen; j++) {
                JSONObject image = images.getJSONObject(j);
                r.addImage(image.getString("filename"));
            }
        }

        JSONArray comments = recipe.optJSONArray("comments");
        if (comments != null) {
            for (int j = 0, commentsLen = comments.length(); j < commentsLen; j++) {
                JSONObject comment = comments.getJSONObject(j);
                r.addComment(comment.getString("content"), comment.getString("userId"),
                        comment.getString("userName"), comment.getLong("createdAt"));
            }
        }

        JSONArray ingredients = recipe.optJSONArray("ingredients");
        if (ingredients != null) {
            for (int j = 0, ingredientsLen = ingredients.length(); j < ingredientsLen; j++) {
                r.addIngredient(ingredients.getString(j));
            }
        }

        JSONArray instructions = recipe.optJSONArray("instructions");
        if (instructions != null) {
            for (int j = 0, instructionsLen = instructions.length(); j < instructionsLen; j++) {
                r.addInstruction(instructions.getString(j));
            }
        }

        return r;
    }

    public static List<Recipe> query(Category c, Context context) {
        Log.i(TAG, "query for " + c);

        String sortOrder = null;
        String selection = null;
        switch (c) {
        case TOP:
            sortOrder = RecipeContract.Recipe.COLUMN_NAME_LIKES + " desc";
            break;
        case MY_RECIPES:
            selection = RecipeContract.Recipe.COLUMN_NAME_USER_ID + "=1";
        case LATEST:
            sortOrder = RecipeContract.Recipe.COLUMN_NAME_CREATED_AT + " desc";
            break;
        }

        Uri recipeUrl = RecipeContract.BASE_CONTENT_URI.buildUpon()
                .appendPath(RecipeContract.Recipe.TABLE_NAME).build();

        Cursor cursor = context.getContentResolver().query(recipeUrl,
                RecipeContract.Recipe.PROJECTION_ALL_FIELDS, selection, null, sortOrder);

        List<Recipe> recipes = new ArrayList<Recipe>();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Recipe r = createRecipeFromCursor(cursor);
                if (r != null) {
                    recipes.add(r);
                }
            }
        } else {

            // Insert code here to report an error if the cursor is null or the provider threw an
            // exception.
        }

        return recipes;
    }

    public static Recipe getRecipe(Uri recipeUrl, Context context) {

        Cursor cursor = context.getContentResolver().query(recipeUrl,
                RecipeContract.Recipe.PROJECTION_ALL_FIELDS, null, null, null);

        cursor.moveToFirst();

        return createRecipeFromCursor(cursor);
    }

    public static Recipe createRecipeFromCursor(Cursor cursor) {
        if (cursor == null) {
            return null;
        }
        int id = cursor.getInt(RecipeContract.Recipe.PROJECTION_ALL_FIELDS_COLUMN_ID);
        String recipeId = cursor
                .getString(RecipeContract.Recipe.PROJECTION_ALL_FIELDS_COLUMN_RECIPE_ID);
        String name = cursor.getString(RecipeContract.Recipe.PROJECTION_ALL_FIELDS_COLUMN_NAME);
        String primaryImageURL = cursor
                .getString(RecipeContract.Recipe.PROJECTION_ALL_FIELDS_COLUMN_PRIMARY_IMAGE_URL);
        String ingredientsJSONString = cursor
                .getString(RecipeContract.Recipe.PROJECTION_ALL_FIELDS_COLUMN_INGREDIENTS);
        String instructionsJSONString = cursor
                .getString(RecipeContract.Recipe.PROJECTION_ALL_FIELDS_COLUMN_INSTRUCTIONS);
        int likes = cursor.getInt(RecipeContract.Recipe.PROJECTION_ALL_FIELDS_COLUMN_LIKES);
        String commentsJSONString = cursor
                .getString(RecipeContract.Recipe.PROJECTION_ALL_FIELDS_COLUMN_COMMENTS);
        long createdAt = cursor
                .getLong(RecipeContract.Recipe.PROJECTION_ALL_FIELDS_COLUMN_CREATED_AT);
        long updatedAt = cursor
                .getLong(RecipeContract.Recipe.PROJECTION_ALL_FIELDS_COLUMN_UPDATED_AT);

        Recipe r = new Recipe(id, recipeId, name, likes, createdAt, updatedAt);
        if (primaryImageURL != null && !primaryImageURL.isEmpty()) {
            r.addImage(primaryImageURL);
        }

        JSONArray ingredientsJSONArray = null;
        JSONArray instructionsJSONArray = null;
        JSONArray commentsJSONArray = null;
        try {
            if (ingredientsJSONString != null) {
                ingredientsJSONArray = new JSONArray(ingredientsJSONString);
                if (ingredientsJSONArray != null) {
                    for (int i = 0; i < ingredientsJSONArray.length(); i++) {
                        r.addIngredient(ingredientsJSONArray.getString(i));
                    }
                }
            }
            if (instructionsJSONString != null) {
                instructionsJSONArray = new JSONArray(instructionsJSONString);
                if (instructionsJSONArray != null) {
                    for (int i = 0; i < instructionsJSONArray.length(); i++) {
                        r.addInstruction(instructionsJSONArray.getString(i));
                    }
                }
            }
            if (commentsJSONString != null) {
                commentsJSONArray = new JSONArray(commentsJSONString);
                if (commentsJSONArray != null) {
                    for (int i = 0; i < commentsJSONArray.length(); i++) {
                        JSONObject comment = commentsJSONArray.getJSONObject(i);
                        r.addComment(comment.getString("content"), comment.getString("userId"),
                                comment.getString("userName"), comment.getLong("createdAt"));
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON: " + e.toString());
        }

        return r;
    }

    public static class RecipeAdapter extends ArrayAdapter<Recipe> {
        public RecipeAdapter(Context context, int resource, List<Recipe> recipes) {
            super(context, resource, recipes);
        }

        public void setData(List<Recipe> data) {
            clear();
            if (data != null) {
                addAll(data);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Recipe recipe = getItem(position);

            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.recipe_list_item, parent, false);

            TextView userNameView = (TextView) rowView
                    .findViewById(R.id.recipe_list_item_user_name);
            TextView createdAtView = (TextView) rowView
                    .findViewById(R.id.recipe_list_item_created_at);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.recipe_list_item_image);
            TextView nameView = (TextView) rowView.findViewById(R.id.recipe_list_item_name);
            TextView numNomsView = (TextView) rowView.findViewById(R.id.recipe_list_item_noms);
            TextView numCommentsView = (TextView) rowView
                    .findViewById(R.id.recipe_list_item_comments);

            String primaryImageURL = recipe.getImage(0);

            userNameView.setText(recipe.getUserName());
            createdAtView.setText(recipe.getCreatedAtTime().format("%b %d"));
            if (primaryImageURL != null) {
                ImageHelper.loadBitmap(primaryImageURL, imageView);
            }

            nameView.setText(recipe.getName());
            numNomsView.setText(recipe.getNumLikes() + " noms");
            numCommentsView.setText(recipe.getNumComments() + " comments");

            return rowView;
        }
    }

    // http://www.androiddesignpatterns.com/2012/08/implementing-loaders.html
    public static class RecipeLoader extends AsyncTaskLoader<List<Recipe>> {

        private List<Recipe> mData;
        private Category mCategory;

        public RecipeLoader(Context context, Category category) {
            // Loaders may be used across multiple Activitys (assuming they aren't
            // bound to the LoaderManager), so NEVER hold a reference to the context
            // directly. Doing so will cause you to leak an entire Activity's context.
            // The superclass constructor will store a reference to the Application
            // Context instead, and can be retrieved with a call to getContext().

            super(context);
            mCategory = category;
        }

        public void setCategory(Category category) {
            mCategory = category;
            onContentChanged();
        }

        /****************************************************/
        /** (1) A task that performs the asynchronous load **/
        /****************************************************/

        @Override
        public List<Recipe> loadInBackground() {
            // This method is called on a background thread and should generate a
            // new set of data to be delivered back to the client.
            return RecipeHelper.query(mCategory, getContext());
        }

        /********************************************************/
        /** (2) Deliver the results to the registered listener **/
        /********************************************************/

        @Override
        public void deliverResult(List<Recipe> data) {
            if (isReset()) {
                // The Loader has been reset; ignore the result and invalidate the data.
                releaseResources(data);
                return;
            }

            // Hold a reference to the old data so it doesn't get garbage collected.
            // We must protect it until the new data has been delivered.
            List<Recipe> oldData = mData;
            mData = data;

            if (isStarted()) {
                // If the Loader is in a started state, deliver the results to the
                // client. The superclass method does this for us.

                super.deliverResult(data);
            }

            // Invalidate the old data as we don't need it any more.
            if (oldData != null && oldData != data) {
                releaseResources(oldData);
            }
        }

        /*********************************************************/
        /** (3) Implement the Loader’s state-dependent behavior **/
        /*********************************************************/

        @Override
        protected void onStartLoading() {
            if (mData != null) {
                // Deliver any previously loaded data immediately.
                deliverResult(mData);
            }

            // Begin monitoring the underlying data source.
            /*
             * if (mObserver == null) { mObserver = new SampleObserver(); // TODO: register the
             * observer }
             */

            if (takeContentChanged() || mData == null) {
                // When the observer detects a change, it should call onContentChanged()
                // on the Loader, which will cause the next call to takeContentChanged()
                // to return true. If this is ever the case (or if the current data is
                // null), we force a new load.
                forceLoad();
            }
        }

        @Override
        protected void onStopLoading() {
            // The Loader is in a stopped state, so we should attempt to cancel the
            // current load (if there is one).
            cancelLoad();

            // Note that we leave the observer as is. Loaders in a stopped state
            // should still monitor the data source for changes so that the Loader
            // will know to force a new load if it is ever started again.
        }

        @Override
        protected void onReset() {
            // Ensure the loader has been stopped.
            onStopLoading();

            // At this point we can release the resources associated with 'mData'.
            if (mData != null) {
                releaseResources(mData);
                mData = null;
            }

            // The Loader is being reset, so we should stop monitoring for changes.
            /*
             * if (mObserver != null) { // TODO: unregister the observer mObserver = null; }
             */
        }

        @Override
        public void onCanceled(List<Recipe> data) {
            // Attempt to cancel the current asynchronous load.
            super.onCanceled(data);

            // The load has been canceled, so we should release the resources
            // associated with 'data'.
            releaseResources(data);
        }

        private void releaseResources(List<Recipe> data) {
            // For a simple List, there is nothing to do. For something like a Cursor, we
            // would close it in this method. All resources associated with the Loader
            // should be released here.
        }

        /*********************************************************************/
        /** (4) Observer which receives notifications when the data changes **/
        /*********************************************************************/

        // NOTE: Implementing an observer is outside the scope of this post (this example
        // uses a made-up "SampleObserver" to illustrate when/where the observer should
        // be initialized).

        // The observer could be anything so long as it is able to detect content changes
        // and report them to the loader with a call to onContentChanged(). For example,
        // if you were writing a Loader which loads a list of all installed applications
        // on the device, the observer could be a BroadcastReceiver that listens for the
        // ACTION_PACKAGE_ADDED intent, and calls onContentChanged() on the particular
        // Loader whenever the receiver detects that a new application has been installed.
        // Please don’t hesitate to leave a comment if you still find this confusing! :)
    }

}
