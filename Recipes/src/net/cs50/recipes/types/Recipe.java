package net.cs50.recipes.types;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

public class Recipe {
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
	
    public Recipe(String recipeId, String name, long createdAt, long updatedAt)
    {
    	mId = 0;
    	mRecipeId = recipeId;
    	mName = name;
    	mImageURLs = new ArrayList<String>();
    	mIngredients = new ArrayList<String>();
    	mInstructions = new ArrayList<String>();
    	mComments = new ArrayList<Comment>();
    	mUser = null;
    	mLikes = 0;
    	mCurrentUserLiked = false;
    	mCreatedAt = createdAt;
    	mUpdatedAt = updatedAt;
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

	public long getCreatedAt() {
		return mCreatedAt;
	}

	public long getUpdatedAt() {
		return mUpdatedAt;
	}
	
	public boolean addImage(String imageURL)
	{
		return mImageURLs.add(imageURL);
	}
	
	public String getImage(int i)
	{
		if (i < mImageURLs.size() && i >= 0)
		{
			return mImageURLs.get(i);
		}
		return "";
	}
	
	public boolean addIngredient(String ingredient)
	{
		return mIngredients.add(ingredient);
	}
	
	public boolean addInstruction(String instruction)
	{
		return mInstructions.add(instruction);
	}
	
	public Comment addComment(String content, String userId, long createdAt)
	{
		Comment comment = new Comment(content, userId, createdAt);
		if (mComments.add(comment))
			return comment;
		return null;
	}
	
	public List<String> getIngredients()
	{
		return mIngredients;
	}
	
	public String getIngredientsJSONString()
	{
		return new JSONArray(mIngredients).toString();
	}
	
	public List<String> getInstructions()
	{
		return mInstructions;
	}
	
	public String getInstructionsJSONString()
	{
		return new JSONArray(mInstructions).toString();
	}
	
	public Comment addComment(String content)
	{
		Comment comment = new Comment(content);
		if (mComments.add(comment))
			return comment;
		return null;
	}
	
	public boolean toggleLike()
	{
		mCurrentUserLiked = !mCurrentUserLiked;
		return mCurrentUserLiked;
	}
}
