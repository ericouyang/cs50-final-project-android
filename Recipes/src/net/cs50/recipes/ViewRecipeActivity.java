package net.cs50.recipes;

import net.cs50.recipes.provider.RecipeContract;
import net.cs50.recipes.util.ImageHelper;
import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;


public class ViewRecipeActivity extends Activity {

    private Cursor mCursor;
    private TextView mRecipeNameView;
    private ImageView mRecipeImageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recipe);
        Uri resUri = getIntent().getData();

        mCursor = getContentResolver().query(
                resUri,
                RecipeContract.Recipe.PROJECTION_ALL_FIELDS,
                null,
                null,
                null);// The sort order for the returned rows

        mRecipeNameView = (TextView) findViewById(R.id.view_recipe_name);
        mRecipeImageView = (ImageView) findViewById(R.id.view_recipe_image);

        mCursor.moveToFirst();

        mRecipeNameView.setText(mCursor.getString(RecipeContract.Recipe.PROJECTION_ALL_FIELDS_COLUMN_NAME));

        String imageUrl = mCursor.getString(RecipeContract.Recipe.PROJECTION_ALL_FIELDS_COLUMN_PRIMARY_IMAGE_URL);
        ImageHelper.loadBitmap(imageUrl, mRecipeImageView);
    }




}
