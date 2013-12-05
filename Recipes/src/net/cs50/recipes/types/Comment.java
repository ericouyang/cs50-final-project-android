package net.cs50.recipes.types;

public class Comment {
	private int mId;
	private String mContent;
	private final User mUser;
	private final String mUserId;
	private final long mCreatedAt;
	private long mUpdatedAt;
	
	public Comment(String content)
	{
		this(0, content, null, 0, 0);
	}
	
	public Comment(String content, User user, long createdAt, long updatedAt)
	{
		this(0, content, user, createdAt, updatedAt);
	}
	
	public Comment(String content, String userId, long createdAt)
	{
		mId = 0;
		mContent = content;
		mUser = null;
		mUserId = userId;
		mCreatedAt = createdAt;
		mUpdatedAt = 0;
	}
	
	public Comment(int id, String content, User user, long createdAt, long updatedAt)
	{
		mId = id;
		mContent = content;
		mUser = user;
		if (user != null)
			mUserId = user.getUserId();
		else
			mUserId = "";
		mCreatedAt = createdAt;
		mUpdatedAt = updatedAt;
	}

	public String getContent() {
		return mContent;
	}

	public void setContent(String content) {
		mContent = content;
	}

	public long getUpdatedAt() {
		return mUpdatedAt;
	}

	public int getId() {
		return mId;
	}

	public User getUser() {
		return mUser;
	}

	public long getCreatedAt() {
		return mCreatedAt;
	}
}
