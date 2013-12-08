package net.cs50.recipes.sync;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.cs50.recipes.models.Recipe;
import net.cs50.recipes.provider.RecipeContract;
import net.cs50.recipes.util.HttpHelper;
import net.cs50.recipes.util.RecipeHelper;
//import com.example.android.network.sync.basicsyncadapter.provider.FeedContract;

import org.json.JSONException;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

/**
 * Defines a sync adapter for Nom!
 * 
 * Used by SyncService to set up appropriate syncing environment
 */
class SyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String TAG = "SyncAdapter";

    private final ContentResolver mContentResolver;

    // default projection
    private static final String[] PROJECTION = RecipeContract.Recipe.PROJECTION_ALL_FIELDS;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
            ContentProviderClient provider, SyncResult syncResult) {
        Log.i(TAG, "Beginning network synchronization");
        try {

            String uri = RecipeContract.RECIPES_URI;
            InputStream stream = null;

            try {
                Log.i(TAG, "Streaming data from network: " + uri);
                stream = HttpHelper.getStream(uri);
                updateLocalData(stream, syncResult);
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        } catch (MalformedURLException e) {
            Log.wtf(TAG, "Feed URL is malformed", e);
            syncResult.stats.numParseExceptions++;
            return;
        } catch (IOException e) {
            Log.e(TAG, "Error reading from network: " + e.toString());
            syncResult.stats.numIoExceptions++;
            return;
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON: " + e.toString());
            syncResult.stats.numParseExceptions++;
            return;
        } catch (RemoteException e) {
            Log.e(TAG, "Error updating database: " + e.toString());
            syncResult.databaseError = true;
            return;
        } catch (OperationApplicationException e) {
            Log.e(TAG, "Error updating database: " + e.toString());
            syncResult.databaseError = true;
            return;
        }
        Log.i(TAG, "Network synchronization complete");
    }

    /**
     * Read JSON from stream and stores it into content provider
     * 
     * Based off of the Android Example for Content Providers
     * 
     * Compares retrieved data with database data to merge data sets
     * 
 	 */
    public void updateLocalData(final InputStream stream, final SyncResult syncResult)
            throws IOException, JSONException, RemoteException, OperationApplicationException {
        final ContentResolver contentResolver = getContext().getContentResolver();

        Log.i(TAG, "Parsing stream as JSON");
        final List<Recipe> recipes = RecipeHelper.parse(stream);
        Log.i(TAG, "Parsing complete. Found " + recipes.size() + " entries");

        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

        // Build hash table of incoming entries
        Map<String, Recipe> recipeMap = new HashMap<String, Recipe>();
        for (Recipe r : recipes) {
            recipeMap.put(r.getRecipeId(), r);
        }

        // Get list of all items
        Log.i(TAG, "Fetching local entries for merge");
        Uri uri = RecipeContract.Recipe.CONTENT_URI; 
        Cursor c = contentResolver.query(uri, PROJECTION, null, null, null);
        assert c != null;
        Log.i(TAG, "Found " + c.getCount() + " local entries. Computing merge solution...");

        // Find stale data
        int id;
        String recipeId;
        long updatedAt;
        while (c.moveToNext()) {
            syncResult.stats.numEntries++;
            id = c.getInt(RecipeContract.Recipe.PROJECTION_ALL_FIELDS_COLUMN_ID);
            recipeId = c.getString(RecipeContract.Recipe.PROJECTION_ALL_FIELDS_COLUMN_RECIPE_ID);
            updatedAt = c.getLong(RecipeContract.Recipe.PROJECTION_ALL_FIELDS_COLUMN_UPDATED_AT);
            Recipe match = recipeMap.get(recipeId);
            if (match != null) {
                // Check to see if the entry needs to be updated
                Uri existingUri = RecipeContract.Recipe.CONTENT_URI.buildUpon()
                        .appendPath(Integer.toString(id)).build();
                if (match.getUpdatedAt() != updatedAt) {
                    // Update existing record
                    Log.i(TAG, "Scheduling update: " + existingUri);
                    batch.add(ContentProviderOperation
                            .newUpdate(existingUri)
                            .withValue(RecipeContract.Recipe.COLUMN_NAME_NAME, match.getName())
                            .withValue(RecipeContract.Recipe.COLUMN_NAME_INGREDIENTS,
                                    match.getIngredientsJSONString())
                            .withValue(RecipeContract.Recipe.COLUMN_NAME_INSTRUCTIONS,
                                    match.getInstructionsJSONString())
                            .withValue(RecipeContract.Recipe.COLUMN_NAME_COMMENTS,
                                    match.getCommentsJSONString())
                            .withValue(RecipeContract.Recipe.COLUMN_NAME_LIKES, match.getNumLikes())
                            .withValue(RecipeContract.Recipe.COLUMN_NAME_PRIMARY_IMAGE_URL,
                                    match.getImage(0))
                            .withValue(RecipeContract.Recipe.COLUMN_NAME_UPDATED_AT,
                                    match.getUpdatedAt()).build());
                    syncResult.stats.numUpdates++;
                } else {
                    Log.i(TAG, "No action: " + existingUri);
                }
                // Remove from recipe map to prevent insert later.
                recipeMap.remove(recipeId);
            } else {
                // Entry doesn't exist. Remove it from the database.
                Uri deleteUri = RecipeContract.Recipe.CONTENT_URI.buildUpon()
                        .appendPath(Integer.toString(id)).build();
                Log.i(TAG, "Scheduling delete: " + deleteUri);
                batch.add(ContentProviderOperation.newDelete(deleteUri).build());
                syncResult.stats.numDeletes++;
            }
        }
        c.close();

        // Add new items
        for (Recipe r : recipeMap.values()) {
            Log.i(TAG, "Scheduling insert: recipeId=" + r.getRecipeId());
            batch.add(ContentProviderOperation
                    .newInsert(RecipeContract.Recipe.CONTENT_URI)
                    .withValue(RecipeContract.Recipe.COLUMN_NAME_RECIPE_ID, r.getRecipeId())
                    .withValue(RecipeContract.Recipe.COLUMN_NAME_NAME, r.getName())
                    .withValue(RecipeContract.Recipe.COLUMN_NAME_INGREDIENTS,
                            r.getIngredientsJSONString())
                    .withValue(RecipeContract.Recipe.COLUMN_NAME_INSTRUCTIONS,
                            r.getInstructionsJSONString())
                    .withValue(RecipeContract.Recipe.COLUMN_NAME_COMMENTS,
                            r.getCommentsJSONString())
                    .withValue(RecipeContract.Recipe.COLUMN_NAME_LIKES, r.getNumLikes())
                    .withValue(RecipeContract.Recipe.COLUMN_NAME_PRIMARY_IMAGE_URL, r.getImage(0))
                    .withValue(RecipeContract.Recipe.COLUMN_NAME_CREATED_AT, r.getCreatedAt())
                    .withValue(RecipeContract.Recipe.COLUMN_NAME_UPDATED_AT, r.getUpdatedAt())
                    .build());
            syncResult.stats.numInserts++;
        }

        Log.i(TAG, "Merge solution ready. Applying batch update");
        mContentResolver.applyBatch(RecipeContract.CONTENT_AUTHORITY, batch);
        mContentResolver.notifyChange(RecipeContract.Recipe.CONTENT_URI, // Updated content
                null, // No local observer
                false); 
    }
}
