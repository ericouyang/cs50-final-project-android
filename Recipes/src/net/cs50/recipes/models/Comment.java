package net.cs50.recipes.models;

// class to represent comment in memory 
public class Comment {
    private int mId;
    private String mContent;
    private final String mUserId;
    private final String mUserName;
    private final long mCreatedAt;
    private long mUpdatedAt;

    public Comment(String content, String userId, String userName, long createdAt) {
        mId = 0;
        mContent = content;
        mUserId = userId;
        mUserName = userName;
        mCreatedAt = createdAt;
        mUpdatedAt = 0;
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

    public String getUserId() {
        return mUserId;
    }

    public String getUserName() {
        return mUserName;
    }

    public long getCreatedAt() {
        return mCreatedAt;
    }

    public String toString() {
        return mContent;
    }

}
