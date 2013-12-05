package net.cs50.recipes.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import net.cs50.recipes.types.Recipe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class RecipeHelper {

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
}
