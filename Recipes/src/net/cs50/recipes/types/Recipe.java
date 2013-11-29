package net.cs50.recipes.types;

public class Recipe {
	private final int mId;
	private final String mRecipeId;
	private final String mName;
	private final User mUser;
	private final long mCreatedAt;
	private long mUpdatedAt;
    
	public Recipe(int id, String name, long createdAt, long updatedAt)
	{
		mId = id;
		mRecipeId = "";
		mName = name;
		mUser = new User();
		mCreatedAt = createdAt;
		mUpdatedAt = updatedAt;
	}
	
	public Recipe(String recipeId, String name, long createdAt, long updatedAt)
	{
		mId = 0;
		mRecipeId = recipeId;
		mName = name;
		mUser = new User();
		mCreatedAt = createdAt;
		mUpdatedAt = updatedAt;
	}
	
	public Recipe(int id, String recipeId, String name, User user, long createdAt, long updatedAt)
	{
		mId = id;
		mRecipeId = recipeId;
		mName = name;
		mUser = user;
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
}
