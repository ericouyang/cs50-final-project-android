package net.cs50.recipes.types;

public class Recipe {
	private final int mId;
	private final String mRecipeId;
	private final String mName;
	private final User mUser;
	private final long mCreatedAt;
	private long mModifiedAt;
    
	public Recipe(int id, String recipeId, String name, User user, long createdAt, long modifiedAt)
	{
		mId = id;
		mRecipeId = recipeId;
		mName = name;
		mUser = user;
		mCreatedAt = createdAt;
		mModifiedAt = modifiedAt;
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

	public long getModifiedAt() {
		return mModifiedAt;
	}
}
