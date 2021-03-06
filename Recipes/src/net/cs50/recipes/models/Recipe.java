package net.cs50.recipes.models;

import java.util.ArrayList;
import java.util.List;

import net.cs50.recipes.R;
import net.cs50.recipes.util.HttpHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import android.text.format.Time;
import android.util.Log;

// class to represent recipe in memory
public class Recipe {
    private final String TAG = "Recipe";
    
    private int mId;
    private final String mRecipeId;
    private final String mName;
    private List<String> mImageURLs;
    private List<String> mIngredients;
    private List<String> mInstructions;
    private List<Comment> mComments;
    private final User mUser;
    private int mLikes;
    private boolean mCurrentUserLiked;
    private final long mCreatedAt;
    private long mUpdatedAt;
    
    private final String DEFAULT_USER_NAME = "John Harvard";

    public Recipe(int id, String recipeId, String name, int likes, long createdAt, long updatedAt) {
        Log.i("Recipe", "num likes " + likes);
        mId = id;
        mRecipeId = recipeId;
        mName = name;
        mImageURLs = new ArrayList<String>();
        mIngredients = new ArrayList<String>();
        mInstructions = new ArrayList<String>();
        mComments = new ArrayList<Comment>();
        mUser = null;
        mLikes = likes;
        mCurrentUserLiked = false;
        mCreatedAt = createdAt;
        mUpdatedAt = updatedAt;
    }

    public Recipe(String recipeId, String name, int likes, long createdAt, long updatedAt) {
        this(0, recipeId, name, likes, createdAt, updatedAt);
    }

    public Recipe(String recipeId, String name, long createdAt, long updatedAt) {
        this(0, recipeId, name, 0, createdAt, updatedAt);
    }

    public int getId() {
        return mId;
    }

    public String getRecipeId() {
        return mRecipeId;
    }

    public String getName() {
        return mName;
    }

    public String getUserName() {
        return DEFAULT_USER_NAME;
    }

    public long getCreatedAt() {
        return mCreatedAt;
    }

    public Time getCreatedAtTime() {
        Time createdAt = new Time();
        createdAt.set(mCreatedAt);

        return createdAt;
    }

    public long getUpdatedAt() {
        return mUpdatedAt;
    }

    public boolean addImage(String imageURL) {
        return mImageURLs.add(imageURL);
    }

    public String getImage(int i) {
        if (i < mImageURLs.size() && i >= 0) {
            return mImageURLs.get(i);
        }
        return null;
    }

    public boolean addIngredient(String ingredient) {
        return mIngredients.add(ingredient);
    }

    public boolean addInstruction(String instruction) {
        return mInstructions.add(instruction);
    }

    public Comment addComment(String content, String userId, String userName, long createdAt) {
        Log.i(TAG, "Adding comment " + content);
        Comment comment = new Comment(content, userId, userName, createdAt);
        if (mComments.add(comment)) {
            return comment;
        }
        return null;
    }

    public List<String> getIngredients() {
        return mIngredients;
    }

    public String getIngredientsJSONString() {
        return new JSONArray(mIngredients).toString();
    }

    public List<String> getInstructions() {
        return mInstructions;
    }

    public String getInstructionsJSONString() {
        return new JSONArray(mInstructions).toString();
    }

    public Comment addComment(String content) {
        Comment comment = new Comment(content, "", DEFAULT_USER_NAME, 0);
        if (mComments.add(comment)) {
            return comment;
        }
        return null;
    }

    public int getNumLikes() {
        return mLikes;
    }

    public List<Comment> getComments() {
        return mComments;
    }

    // construct json string representing comments in memory
    public String getCommentsJSONString() {
        try {
            JSONArray array = new JSONArray();
            for (int i = 0; i < mComments.size(); i++) {
                Comment comment = mComments.get(i);
                JSONObject JSONComment = new JSONObject();
                JSONComment.put("content", comment.getContent());
                JSONComment.put("userId", comment.getUserId());
                JSONComment.put("userName", comment.getUserName());
                JSONComment.put("createdAt", comment.getUpdatedAt());

                array.put(JSONComment);
            }

            Log.i(TAG, "Generated JSON String " + array.toString());

            return array.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error constructing JSON String for comments");
        }
        return "";
    }

    public int getNumComments() {
        return mComments.size();
    }

    // toggle like on recipe
    public boolean toggleLike() {
        boolean changePerformed;

        Log.i(TAG, "Current User Liked " + mCurrentUserLiked);
        if (mCurrentUserLiked) {
            changePerformed = HttpHelper.like(mRecipeId);
        } else {
            changePerformed = HttpHelper.unlike(mRecipeId);
        }
        if (changePerformed) {
            mCurrentUserLiked = !mCurrentUserLiked;
        }

        Log.i(TAG, "Change user liked " + changePerformed);
        return mCurrentUserLiked;
    }
}
