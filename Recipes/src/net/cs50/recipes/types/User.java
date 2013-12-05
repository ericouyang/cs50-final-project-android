package net.cs50.recipes.types;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

public class User {
	private final int mId;
	private final String mUserId;
	private String mFirstName;
	private String mLastName;
	private String mProfileImageURL;
	private final long mCreatedAt;
	private long mUpdatedAt;
	
	public User()
	{
		this(0, "", "", "", "", 0, 0);
	}
	
	public User(int id, String userId, String firstName, String lastName, String profileImageURL, long createdAt, long updatedAt)
	{
		mId = id;
		mUserId = userId;
		mFirstName = firstName;
		mLastName = lastName;
		mProfileImageURL = profileImageURL;
		mCreatedAt = createdAt;
		mUpdatedAt = updatedAt;
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
