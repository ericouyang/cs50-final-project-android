/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.cs50.recipes;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.cs50.recipes.provider.RecipeContract;
import net.cs50.recipes.types.Recipe;
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
 * Define a sync adapter for the app.
 * 
 * <p>
 * This class is instantiated in {@link SyncService}, which also binds SyncAdapter to the system.
 * SyncAdapter should only be initialized in SyncService, never anywhere else.
 * 
 * <p>
 * The system calls onPerformSync() via an RPC call through the IBinder object supplied by
 * SyncService.
 */
class SyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String TAG = "SyncAdapter";

    /**
     * Content resolver, for performing database operations.
     */
    private final ContentResolver mContentResolver;

    /**
     * Project used when querying content provider. Returns all known fields.
     */
    private static final String[] PROJECTION = RecipeContract.Recipe.PROJECTION_ALL_FIELDS;

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
    }

    /**
     * Called by the Android system in response to a request to run the sync adapter. The work
     * required to read data from the network, parse it, and store it in the content provider is
     * done here. Extending AbstractThreadedSyncAdapter ensures that all methods within SyncAdapter
     * run on a background thread. For this reason, blocking I/O and other long-running tasks can be
     * run <em>in situ</em>, and you don't have to set up a separate thread for them. .
     * 
     * <p>
     * This is where we actually perform any work required to perform a sync.
     * {@link AbstractThreadedSyncAdapter} guarantees that this will be called on a non-UI thread,
     * so it is safe to peform blocking I/O here.
     * 
     * <p>
     * The syncResult argument allows you to pass information back to the method that triggered the
     * sync.
     */
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
                // Makes sure that the InputStream is closed after the app is
                // finished using it.
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
     * Read XML from an input stream, storing it into the content provider.
     * 
     * <p>
     * This is where incoming data is persisted, committing the results of a sync. In order to
     * minimize (expensive) disk operations, we compare incoming data with what's already in our
     * database, and compute a merge. Only changes (insert/update/delete) will result in a database
     * write.
     * 
     * <p>
     * As an additional optimization, we use a batch operation to perform all database writes at
     * once.
     * 
     * <p>
     * Merge strategy: 1. Get cursor to all items in feed<br/>
     * 2. For each item, check if it's in the incoming data.<br/>
     * a. YES: Remove from "incoming" list. Check if data has mutated, if so, perform database
     * UPDATE.<br/>
     * b. NO: Schedule DELETE from database.<br/>
     * (At this point, incoming database only contains missing items.)<br/>
     * 3. For any items remaining in incoming list, ADD to database.
     * 
     * @throws OperationApplicationException
     * @throws RemoteException
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
        Uri uri = RecipeContract.Recipe.CONTENT_URI; // Get all entries
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
        mContentResolver.notifyChange(RecipeContract.Recipe.CONTENT_URI, // URI where data was
                                                                         // modified
                null, // No local observer
                false); // IMPORTANT: Do not sync to network
        // This sample doesn't support uploads, but if *your* code does, make sure you set
        // syncToNetwork=false in the line above to prevent duplicate syncs.
    }

    /**
     * Given a string representation of a URL, sets up a connection and gets an input stream.
     */
    /*
     * private InputStream downloadUrl(final URL url) throws IOException { HttpURLConnection conn =
     * (HttpURLConnection) url.openConnection(); conn.setReadTimeout(NET_READ_TIMEOUT_MILLIS);
     * conn.setConnectTimeout(NET_CONNECT_TIMEOUT_MILLIS); conn.setRequestMethod("GET");
     * conn.setDoInput(true); // Starts the query conn.connect(); return conn.getInputStream(); }
     */
}
