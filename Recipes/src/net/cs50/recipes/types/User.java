package net.cs50.recipes.types;

public class User {
	private final int mId;
	private final String mUserId;
	private String mFirstName;
	private String mLastName;
	private final long mCreatedAt;
	private long mModifiedAt;
	
	public User(int id, String userId, String firstName, String lastName, long createdAt, long modifiedAt)
	{
		mId = id;
		mUserId = userId;
		mFirstName = firstName;
		mLastName = lastName;
		mCreatedAt = createdAt;
		mModifiedAt = modifiedAt;
	}

	public int getId() {
		return mId;
	}

	public String getUserId() {
		return mUserId;
	}

	public String getFirstName() {
		return mFirstName;
	}

	public void setFirstName(String firstName) {
		mFirstName = firstName;
	}

	public String getLastName() {
		return mLastName;
	}

	public void setLastName(String lastName) {
		mLastName = lastName;
	}
	
	public long getCreatedAt() {
		return mCreatedAt;
	}

	public long getModifiedAt() {
		return mModifiedAt;
	}

}
