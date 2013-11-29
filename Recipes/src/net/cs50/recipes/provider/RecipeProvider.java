package net.cs50.recipes.provider;

import net.cs50.recipes.util.SelectionBuilder;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;

public class RecipeProvider extends ContentProvider {

    RecipeDatabase mDbHelper;

    /**
     * Content authority for this provider.
     */
    private static final String AUTHORITY = RecipeContract.CONTENT_AUTHORITY;

    // The constants below represent individual URI routes, as IDs. Every URI pattern recognized by
    // this ContentProvider is defined using sUriMatcher.addURI(), and associated with one of these
    // IDs.
    //
    // When a incoming URI is run through sUriMatcher, it will be tested against the defined
    // URI patterns, and the corresponding route ID will be returned.
    /**
     * URI ID for route: /recipes
     */
    public static final int ROUTE_RECIPES = 1;

    /**
     * URI ID for route: /recipes/{ID}
     */
    public static final int ROUTE_RECIPES_ID = 2;

    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(AUTHORITY, RecipeContract.Recipe.TABLE_NAME, ROUTE_RECIPES);
        sUriMatcher.addURI(AUTHORITY, RecipeContract.Recipe.TABLE_NAME + "/*", ROUTE_RECIPES_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new RecipeDatabase(getContext());
        return true;
    }

    /**
     * Determine the mime type for entries returned by a given URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
        case ROUTE_RECIPES:
            return RecipeContract.Recipe.CONTENT_TYPE;
        case ROUTE_RECIPES_ID:
            return RecipeContract.Recipe.CONTENT_ITEM_TYPE;
        default:
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * Perform a database query by URI.
     *
     * <p>Currently supports returning all entries (/entries) and individual entries by ID
     * (/entries/{ID}).
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        SelectionBuilder builder = new SelectionBuilder();
        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch) {
        case ROUTE_RECIPES_ID:
            // Return a single entry, by ID.
            String id = uri.getLastPathSegment();
            builder.where(BaseColumns._ID + "=?", id);
        case ROUTE_RECIPES:
            // Return all known entries.
            builder.table(RecipeContract.Recipe.TABLE_NAME)
            .where(selection, selectionArgs);
            Cursor c = builder.query(db, projection, sortOrder);
            // Note: Notification URI must be manually set here for loaders to correctly
            // register ContentObservers.
            Context ctx = getContext();
            assert ctx != null;
            c.setNotificationUri(ctx.getContentResolver(), uri);
            return c;
        default:
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * Insert a new entry into the database.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        assert db != null;
        final int match = sUriMatcher.match(uri);
        Uri result;
        switch (match) {
        case ROUTE_RECIPES:
            long id = db.insertOrThrow(RecipeContract.Recipe.TABLE_NAME, null, values);
            result = Uri.parse(RecipeContract.Recipe.CONTENT_URI + "/" + id);
            break;
        case ROUTE_RECIPES_ID:
            throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
        default:
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return result;
    }

    /**
     * Delete an entry by database by URI.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        switch (match) {
        case ROUTE_RECIPES:
            count = builder.table(RecipeContract.Recipe.TABLE_NAME)
            .where(selection, selectionArgs)
            .delete(db);
            break;
        case ROUTE_RECIPES_ID:
            String id = uri.getLastPathSegment();
            count = builder.table(RecipeContract.Recipe.TABLE_NAME)
                    .where(BaseColumns._ID + "=?", id)
                    .where(selection, selectionArgs)
                    .delete(db);
            break;
        default:
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    /**
     * Update an etry in the database by URI.
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        switch (match) {
        case ROUTE_RECIPES:
            count = builder.table(RecipeContract.Recipe.TABLE_NAME)
            .where(selection, selectionArgs)
            .update(db, values);
            break;
        case ROUTE_RECIPES_ID:
            String id = uri.getLastPathSegment();
            count = builder.table(RecipeContract.Recipe.TABLE_NAME)
                    .where(BaseColumns._ID + "=?", id)
                    .where(selection, selectionArgs)
                    .update(db, values);
            break;
        default:
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    /**
     * SQLite backend for @{link FeedProvider}.
     *
     * Provides access to an disk-backed, SQLite datastore which is utilized by FeedProvider. This
     * database should never be accessed by other parts of the application directly.
     */
    static class RecipeDatabase extends SQLiteOpenHelper {
        /** Schema version. */
        public static final int DATABASE_VERSION = 1;

        /** Filename for SQLite file. */
        public static final String DATABASE_NAME = "recipes.db";

        private static final String TYPE_TEXT = " TEXT";
        private static final String TYPE_INTEGER = " INTEGER";
        private static final String COMMA_SEP = ",";

        /** SQL statement to create "entry" table. */
        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + RecipeContract.Recipe.TABLE_NAME + " (" +
                		RecipeContract.Recipe._ID + " INTEGER PRIMARY KEY," + 
                        RecipeContract.Recipe.COLUMN_NAME_RECIPE_ID + TYPE_TEXT + COMMA_SEP +
                        RecipeContract.Recipe.COLUMN_NAME_NAME + TYPE_TEXT + COMMA_SEP +
                        RecipeContract.Recipe.COLUMN_NAME_IMAGES + TYPE_TEXT + COMMA_SEP +
                        RecipeContract.Recipe.COLUMN_NAME_INSTRUCTIONS + TYPE_TEXT + COMMA_SEP +
                        RecipeContract.Recipe.COLUMN_NAME_INGREDIENTS + TYPE_TEXT + COMMA_SEP +
                        RecipeContract.Recipe.COLUMN_NAME_TAGS + TYPE_TEXT + COMMA_SEP +
                        RecipeContract.Recipe.COLUMN_NAME_USER_ID + TYPE_TEXT + COMMA_SEP +
                        RecipeContract.Recipe.COLUMN_NAME_CREATED_AT + TYPE_INTEGER + COMMA_SEP +
                        RecipeContract.Recipe.COLUMN_NAME_MODIFIED_AT + TYPE_INTEGER +
                ")";

        /** SQL statement to drop "entry" table. */
        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + RecipeContract.Recipe.TABLE_NAME;

        public RecipeDatabase(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }
    }
}
