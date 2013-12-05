package net.cs50.recipes.util;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

public class HttpHelper {
	private static final String TAG = "HttpHelper";
	
    private static final String BASE_URL = "http://ericouyang.com:1337/";

    private static final String CREATE_USER_URI = "user/create";
    
    private static final String AUTHORIZE_USER_URI = "authorize";
    
    // Network connection timeout, in milliseconds.
    private static final int NET_CONNECT_TIMEOUT_MILLIS = 15000;  // 15 seconds

    // Network read timeout, in milliseconds.
    private static final int NET_READ_TIMEOUT_MILLIS = 10000;  // 10 seconds
    
	public static String signUp(String firstName, String lastName, String username, String password)
			throws Exception 
	{
		List<NameValuePair> params = new ArrayList<NameValuePair>(4);
		params.add(new BasicNameValuePair("username", username));
		params.add(new BasicNameValuePair("firstName", firstName));
		params.add(new BasicNameValuePair("lastName", lastName));
		params.add(new BasicNameValuePair("password", password));
        
		String authToken = null;
		try
		{
			InputStream stream = getStream(CREATE_USER_URI, "POST", params);
            
            JSONObject user = (JSONObject) new JSONTokener(getString(stream)).nextValue();
            authToken = user.getString("access_token");
            
        } catch (ClientProtocolException e) {
        	e.printStackTrace();
        } catch (IOException e) {
        	e.printStackTrace();
        }

        return authToken;
    }
	
	public static String authorize(String username, String password)
	{
		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
		params.add(new BasicNameValuePair("username", username));
		params.add(new BasicNameValuePair("password", password));
        
		String authToken = null;
		try
		{
			InputStream stream = getStream(AUTHORIZE_USER_URI, "POST", params);
            
            JSONObject user = (JSONObject) new JSONTokener(getString(stream)).nextValue();
            authToken = user.getString("access_token");
            
            Log.i(TAG, authToken);
            
        } catch (JSONException e) {
        	Log.e(TAG, "Error parsing JSON: " + e.toString());
        } catch (ClientProtocolException e) {
        	e.printStackTrace();
        } catch (IOException e) {
        	e.printStackTrace();
        }

        return authToken;
	}
	
	public static InputStream getStream(String uri, String requestMethod, List<NameValuePair> params, boolean includeAuthToken)
		throws IOException
	{
		URL url = new URL(BASE_URL + uri);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(NET_READ_TIMEOUT_MILLIS /* milliseconds */);
        conn.setConnectTimeout(NET_CONNECT_TIMEOUT_MILLIS /* milliseconds */);
        conn.setRequestMethod(requestMethod);
        conn.setDoInput(true);
        
        if (params != null || includeAuthToken)
        {
        	conn.setDoOutput(true);
        	
        	if (includeAuthToken)
        	{
        		if (params == null)
        		{
        			params = new ArrayList<NameValuePair>(1);
        		}
        		params.add(new BasicNameValuePair("auth_token", getAuthToken()));
        	}
        	
        	
        	OutputStream os = conn.getOutputStream();
        	BufferedWriter writer = new BufferedWriter(
        	        new OutputStreamWriter(os, "UTF-8"));
        	writer.write(URLEncodedUtils.format(params, "UTF-8"));
        	writer.flush();
        	writer.close();
        	os.close();
        }
        
        conn.connect();
        return conn.getInputStream();
	}
	
	public static InputStream getStream(String uri, String requestMethod, List<NameValuePair> params) 
		throws IOException
	{
		return getStream(uri, requestMethod, params, false);
	}
	
	public static InputStream getStream(String uri, String requestMethod) throws IOException
	{
		return getStream(uri, requestMethod, null);
	}
	
	public static InputStream getStream(String uri) throws IOException
	{
		return getStream(uri, "GET");
	}
	
	public static String getString(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = in.read(buffer, 0, 4096)) > 0) {
            out.write(buffer, 0, read);
        }
        return new String(out.toByteArray());
    }
	
	private static String getAuthToken()
	{
		return "";
	}
}
