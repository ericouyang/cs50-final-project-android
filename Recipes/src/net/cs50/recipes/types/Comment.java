package net.cs50.recipes.types;

public class Comment {
	private final int mId;
	private String mContent;
	private final Recipe mRecipe;
	private final User mUser;
	private final long mCreatedAt;
	private long mModifiedAt;
	
	public Comment(int id, String content, Recipe recipe, User user, long createdAt, long modifiedAt)
	{
		mId = id;
		mContent = content;
		mRecipe = recipe;
		mUser = user;
		mCreatedAt = createdAt;
		mModifiedAt = modifiedAt;
	}

	public String getContent() {
		return mContent;
	}

	public void setContent(String content) {
		mContent = content;
	}

	public long getModifiedAt() {
		return mModifiedAt;
	}

	public int getId() {
		return mId;
	}

	public Recipe getRecipe() {
		return mRecipe;
	}

	public User getUser() {
		return mUser;
	}

	public long getCreatedAt() {
		return mCreatedAt;
	}
}
