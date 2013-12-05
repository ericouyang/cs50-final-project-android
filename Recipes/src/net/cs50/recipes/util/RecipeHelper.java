package net.cs50.recipes.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.cs50.recipes.provider.RecipeContract;
import net.cs50.recipes.types.Recipe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class RecipeHelper {

	private static String TAG = "RecipeHelper";
	
	public enum Category {
		TOP, LATEST, MY_RECIPES
	}
	
    private RecipeHelper() {
    }

    public static List<Recipe> parse(InputStream in) throws IOException, JSONException
    {
        List<Recipe> list = new LinkedList<Recipe>();
        JSONArray recipes = (JSONArray) new JSONTokener(HttpHelper.getString(in)).nextValue();
        for (int i = 0, len = recipes.length(); i < len; i++) {
            JSONObject recipe = recipes.getJSONObject(i);
            String recipeId = recipe.getString("id");
            String name = recipe.getString("name");
            long createdAt = recipe.getLong("createdAt");
            long updatedAt = recipe.getLong("updatedAt");
            
            Recipe r = new Recipe(recipeId, name, createdAt, updatedAt);
            
            JSONArray images = recipe.optJSONArray("images");
            if (images != null)
            {
            	for (int j = 0, imagesLen = images.length(); j < imagesLen; j++)
            	{
            		JSONObject image = images.getJSONObject(j);
    				r.addImage(image.getString("filename"));
            	}
            }
            
            
            JSONArray comments = recipe.optJSONArray("comments");
            if (comments != null)
            {
            	for (int j = 0, commentsLen = comments.length(); j < commentsLen; j++)
            	{
            		JSONObject comment = comments.getJSONObject(j);
    				r.addComment(comment.getString("content"), comment.getString("userId"), comment.getLong("createdAt"));
            	}
            }
            
            JSONArray ingredients = recipe.optJSONArray("ingredients");
            if (ingredients != null)
            {
            	for (int j = 0, ingredientsLen = ingredients.length(); j < ingredientsLen; j++)
            	{
        			r.addIngredient(ingredients.getString(j));
            	}
            }
            
            JSONArray instructions = recipe.optJSONArray("instructions");
            if (instructions != null)
            {
            	for (int j = 0, instructionsLen = instructions.length(); j < instructionsLen; j++)
            	{
            		r.addInstruction(instructions.getString(j));
            	}
            }
            list.add(r);
        }
        return list;
    }
    
    public static List<Recipe> query(Category c, Context context) {
    	switch (c) {
    	case TOP:
    	case LATEST:
    	case MY_RECIPES:
    	}
    	
    	Uri recipeUrl = RecipeContract.BASE_CONTENT_URI.buildUpon()
                .appendPath(RecipeContract.Recipe.TABLE_NAME)
                .build();
    	
    	Cursor cursor = context.getContentResolver().query(
    			recipeUrl,
                RecipeContract.Recipe.PROJECTION_ALL_FIELDS,
                null,
                null,
                null);// The sort order for the returned rows

        List<Recipe> recipes = new ArrayList<Recipe>();
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
            	Recipe r = createRecipeFromCursor(cursor);
            	if (r != null)
            		recipes.add(r);
            }
        } else {

            // Insert code here to report an error if the cursor is null or the provider threw an exception.
        }
        
        return recipes;
    }
    
    public static Recipe getRecipe(Uri recipeUrl, Context context)
    {
    	
    	Cursor cursor = context.getContentResolver().query(
    			recipeUrl,
                RecipeContract.Recipe.PROJECTION_ALL_FIELDS,
                null,
                null,
                null);
    	
    	cursor.moveToFirst();
    	
    	return createRecipeFromCursor(cursor);
    }
    
    public static Recipe createRecipeFromCursor(Cursor cursor)
    {
    	if (cursor == null)
    	{
    		return null;
    	}
    	int id = cursor.getInt(RecipeContract.Recipe.PROJECTION_ALL_FIELDS_COLUMN_ID);
    	String recipeId = cursor.getString(RecipeContract.Recipe.PROJECTION_ALL_FIELDS_COLUMN_RECIPE_ID);
    	String name = cursor.getString(RecipeContract.Recipe.PROJECTION_ALL_FIELDS_COLUMN_NAME);
    	String primaryImageURL = cursor.getString(RecipeContract.Recipe.PROJECTION_ALL_FIELDS_COLUMN_PRIMARY_IMAGE_URL);
    	String ingredientsJSONString = cursor.getString(RecipeContract.Recipe.PROJECTION_ALL_FIELDS_COLUMN_INGREDIENTS);
    	String instructionsJSONString = cursor.getString(RecipeContract.Recipe.PROJECTION_ALL_FIELDS_COLUMN_INGREDIENTS);
    	long createdAt = cursor.getLong(RecipeContract.Recipe.PROJECTION_ALL_FIELDS_COLUMN_CREATED_AT);
    	long updatedAt = cursor.getLong(RecipeContract.Recipe.PROJECTION_ALL_FIELDS_COLUMN_UPDATED_AT);
    	
    	Recipe r = new Recipe(id, recipeId, name, createdAt, updatedAt);
    	if (!primaryImageURL.isEmpty())
    		r.addImage(primaryImageURL);
    	
    	JSONArray ingredientsJSONArray = null;
    	JSONArray instructionsJSONArray = null;
    	try
    	{
    		ingredientsJSONArray = new JSONArray(ingredientsJSONString);
    		if (ingredientsJSONArray != null)
        	{
        		for (int i = 0; i < ingredientsJSONArray.length(); i++)
            	{
            		r.addIngredient(ingredientsJSONArray.getString(i));
            	}
        	}
    		
    		instructionsJSONArray = new JSONArray(instructionsJSONString);
    		if (instructionsJSONArray != null)
        	{
        		for (int i = 0; i < instructionsJSONArray.length(); i++)
            	{
            		r.addInstruction(instructionsJSONArray.getString(i));
            	}
        	}
    	}
    	catch (JSONException e)
    	{
    		Log.e(TAG, "Error parsing JSON: " + e.toString());
    	}
    	
    	return r;
    }
}
