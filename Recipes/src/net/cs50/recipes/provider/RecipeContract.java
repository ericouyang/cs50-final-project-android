package net.cs50.recipes.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

// Constants for recipe content provider
public class RecipeContract {
    private RecipeContract() {
    }

    // Content provider authority.
    public static final String CONTENT_AUTHORITY = "net.cs50.recipes";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String RECIPES_URI = "recipe";

    // columns for recipe records
    public static class Recipe implements BaseColumns {
    	
        // MIME type for lists of recipes.
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.cs50.recipes";
        // MIME type for individual recipes.
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.cs50.recipe";

        // table name where recipes are stored
        public static final String TABLE_NAME = "recipes";

        // uri for recipes
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME)
                .build();

        // column name constants
        public static final String COLUMN_NAME_RECIPE_ID = "recipe_id";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_IMAGES = "images";
        public static final String COLUMN_NAME_TAGS = "tags";
        public static final String COLUMN_NAME_INSTRUCTIONS = "instructions";
        public static final String COLUMN_NAME_INGREDIENTS = "ingredients";
        public static final String COLUMN_NAME_PRIMARY_IMAGE_URL = "primary_image_url";
        public static final String COLUMN_NAME_USER_ID = "user_id";
        public static final String COLUMN_NAME_LIKES = "likes";
        public static final String COLUMN_NAME_COMMENTS = "comments";
        public static final String COLUMN_NAME_CREATED_AT = "created_at";
        public static final String COLUMN_NAME_UPDATED_AT = "updated_at";

        // default projection
        public static final String[] PROJECTION_ALL_FIELDS = { _ID, COLUMN_NAME_RECIPE_ID,
                COLUMN_NAME_NAME, COLUMN_NAME_IMAGES, COLUMN_NAME_INSTRUCTIONS,
                COLUMN_NAME_INGREDIENTS, COLUMN_NAME_TAGS, COLUMN_NAME_PRIMARY_IMAGE_URL,
                COLUMN_NAME_USER_ID, COLUMN_NAME_LIKES, COLUMN_NAME_COMMENTS,
                COLUMN_NAME_CREATED_AT, COLUMN_NAME_UPDATED_AT };

        // Constants representing column positions from PROJECTION_ALL_FIELDS
        public static final int PROJECTION_ALL_FIELDS_COLUMN_ID = 0;
        public static final int PROJECTION_ALL_FIELDS_COLUMN_RECIPE_ID = 1;
        public static final int PROJECTION_ALL_FIELDS_COLUMN_NAME = 2;
        public static final int PROJECTION_ALL_FIELDS_COLUMN_IMAGES = 3;
        public static final int PROJECTION_ALL_FIELDS_COLUMN_INSTRUCTIONS = 4;
        public static final int PROJECTION_ALL_FIELDS_COLUMN_INGREDIENTS = 5;
        public static final int PROJECTION_ALL_FIELDS_COLUMN_TAGS = 6;
        public static final int PROJECTION_ALL_FIELDS_COLUMN_PRIMARY_IMAGE_URL = 7;
        public static final int PROJECTION_ALL_FIELDS_COLUMN_USER_ID = 8;
        public static final int PROJECTION_ALL_FIELDS_COLUMN_LIKES = 9;
        public static final int PROJECTION_ALL_FIELDS_COLUMN_COMMENTS = 10;
        public static final int PROJECTION_ALL_FIELDS_COLUMN_CREATED_AT = 11;
        public static final int PROJECTION_ALL_FIELDS_COLUMN_UPDATED_AT = 12;
    }
}