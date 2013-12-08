package net.cs50.recipes.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class CommentContract {
    private CommentContract() {
    }

    /**
     * Content provider authority.
     */
    public static final String CONTENT_AUTHORITY = "net.cs50.recipes";

    /**
     * Base URI. (content://com.example.android.network.sync.basicsyncadapter)
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String RECIPES_URI = "comment";

    /**
     * Columns supported by "recipes" records.
     */
    public static class Comment implements BaseColumns {
        /**
         * MIME type for lists of recipes.
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.cs50.comments";
        /**
         * MIME type for individual recipes.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.cs50.comment";

        /**
         * Table name where records are stored for "recipe" resources.
         */
        public static final String TABLE_NAME = "comments";

        /**
         * Fully qualified URI for "recipe" resources.
         */
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME)
                .build();

        public static final String COLUMN_NAME_RECIPE_ID = "recipe_id";

        public static final String COLUMN_NAME_CONTENT = "content";

        public static final String COLUMN_NAME_USER_ID = "user_id";

        public static final String COLUMN_NAME_CREATED_AT = "created_at";

        public static final String COLUMN_NAME_UPDATED_AT = "updated_at";

        /**
         * Projections
         */
        public static final String[] PROJECTION_ALL_FIELDS = { _ID, COLUMN_NAME_RECIPE_ID,
                COLUMN_NAME_CONTENT, COLUMN_NAME_USER_ID, COLUMN_NAME_CREATED_AT,
                COLUMN_NAME_UPDATED_AT };

        // Constants representing column positions from PROJECTION_ALL_FIELDS
        public static final int PROJECTION_ALL_FIELDS_COLUMN_ID = 0;
        public static final int PROJECTION_ALL_FIELDS_COLUMN_RECIPE_ID = 1;
        public static final int PROJECTION_ALL_FIELDS_COLUMN_CONTENT = 2;
        public static final int PROJECTION_ALL_FIELDS_COLUMN_USER_ID = 3;
        public static final int PROJECTION_ALL_FIELDS_COLUMN_CREATED_AT = 4;
        public static final int PROJECTION_ALL_FIELDS_COLUMN_UPDATED_AT = 5;
    }
}
