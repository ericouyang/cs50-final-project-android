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

public class RecipeParser {

    private RecipeParser() {
    }

    public static List<Recipe> parse(InputStream in) throws IOException, JSONException
    {
        List<Recipe> list = new LinkedList<Recipe>();
        JSONArray recipes = (JSONArray) new JSONTokener(getString(in)).nextValue();
        for (int i = 0, len = recipes.length(); i < len; i++) {
            JSONObject recipe = recipes.getJSONObject(i);
            String recipeId = recipe.getString("id");
            String name = recipe.getString("name");
            long createdAt = recipe.getLong("createdAt");
            long updatedAt = recipe.getLong("updatedAt");
            Recipe r = new Recipe(recipeId, name, createdAt, updatedAt);
            list.add(r);
        }
        return list;
    }

    private static String getString(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = in.read(buffer, 0, 4096)) > 0) {
            out.write(buffer, 0, read);
        }
        return new String(out.toByteArray());
    }
}
