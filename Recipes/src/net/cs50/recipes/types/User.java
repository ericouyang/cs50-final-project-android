package net.cs50.recipes.types;

public class User {
	private final int mId;
	private final String mUserId;
	private String mFirstName;
	private String mLastName;
	private final long mCreatedAt;
	private long mUpdatedAt;
	
	public User()
	{
		mId = 0;
		mUserId = "";
		mFirstName = "";
		mLastName = "";
		mCreatedAt = 0;
		mUpdatedAt = 0;
	}
	
	public User(int id, String userId, String firstName, String lastName, long createdAt, long updatedAt)
	{
		mId = id;
		mUserId = userId;
		mFirstName = firstName;
		mLastName = lastName;
		mCreatedAt = createdAt;
		mUpdatedAt = mUpdatedAt;
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

	public long getUpdatedAt() {
		return mUpdatedAt;
	}

}
