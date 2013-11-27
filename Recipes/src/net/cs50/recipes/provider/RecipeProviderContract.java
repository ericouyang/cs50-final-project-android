package net.cs50.recipes.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Field and table name constants for
 * {@link com.example.android.network.sync.basicsyncadapter.provider.FeedProvider}.
 */
public class RecipeProviderContract {
    private RecipeProviderContract() {
    }

    /**
     * Content provider authority.
     */
    public static final String CONTENT_AUTHORITY = "net.cs50.recipes";

    /**
     * Base URI. (content://com.example.android.network.sync.basicsyncadapter)
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Path component for "recipe"-type resources..
     */
    private static final String PATH_RECIPES = "recipes";

    /**
     * Columns supported by "recipes" records.
     */
    public static class Recipe implements BaseColumns {
        /**
         * MIME type for lists of recipes.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.cs50.recipes";
        /**
         * MIME type for individual recipes.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.cs50.recipe";

        /**
         * Fully qualified URI for "recipe" resources.
         */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_RECIPES).build();

        /**
         * Table name where records are stored for "recipe" resources.
         */
        public static final String TABLE_NAME = "recipes";
        
        /**
         * External ID. (Note: Not to be confused with the database primary key, which is _ID.
         */
        public static final String COLUMN_NAME_RECIPE_ID = "recipe_id";
        
        /**
         * Recipe name
         */
        public static final String COLUMN_NAME_NAME = "name";
        
        /**
         * Article hyperlink. Corresponds to the rel="alternate" link in the
         * Atom spec.
         */
        public static final String COLUMN_NAME_LINK = "link";
        
        /**
         * Date article was published.
         */
        public static final String COLUMN_NAME_CREATED = "createdAt";
    }
}