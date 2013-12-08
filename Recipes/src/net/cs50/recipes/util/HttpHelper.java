package net.cs50.recipes.util;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.cs50.recipes.BaseActivity;
import net.cs50.recipes.models.Recipe;
import net.cs50.recipes.provider.RecipeContract;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

// helper class to perform HTTP requests
public class HttpHelper {
    private static final String TAG = "HttpHelper";

    // url paths
    private static final String BASE_URL = "http://nom.hrvd.io/";
    private static final String CREATE_USER_URI = "user/create";
    private static final String AUTHORIZE_USER_URI = "authorize/";
    private static final String RECIPE_URI = "recipe";

    // Network connection timeout, in milliseconds.
    private static final int NET_CONNECT_TIMEOUT_MILLIS = 15000; // 15 seconds

    // Network read timeout, in milliseconds.
    private static final int NET_READ_TIMEOUT_MILLIS = 10000; // 10 seconds

    private static String mAuthToken = null;

    /* app currently does not support user signups via the mobile app
    public static String signUp(String firstName, String lastName, String username, String password)
            throws Exception {
        List<NameValuePair> params = new ArrayList<NameValuePair>(4);
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("firstName", firstName));
        params.add(new BasicNameValuePair("lastName", lastName));
        params.add(new BasicNameValuePair("password", password));

        String authToken = null;
        try {
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
	*/
    
    // authorize the user, return the auth token
    public static String authorize(String username, String password) {
        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("password", password));

        String authToken = null;
        try {
            InputStream stream = getStream(AUTHORIZE_USER_URI, "POST", params);

            JSONObject res = (JSONObject) new JSONTokener(getString(stream)).nextValue();
            if (res.has("access_token")) {
                authToken = res.getString("access_token");

                mAuthToken = authToken;
                Log.i(TAG, authToken);
            }

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON: " + e.toString());
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return authToken;
    }

    // like the given recipe
    public static boolean like(String id) {
        try {
            InputStream stream = getStream(RECIPE_URI + "/" + id + "/like", "POST", true);

            if (getString(stream).equals("true")) {
                return true;
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    // unlike the given recipe
    public static boolean unlike(String id) {
        try {
            InputStream stream = getStream(RECIPE_URI + "/" + id + "/like", "DELETE", true);

            if (getString(stream).equals("true")) {
                return true;
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
    
    // add a given comment a notify data change via content resolver
    public static boolean addComment(ContentResolver cr, Uri recipeUri, String recipeId,
            String content) {
        List<NameValuePair> params = new ArrayList<NameValuePair>(1);
        params.add(new BasicNameValuePair("content", content));

        try {
            InputStream stream = getStream(RECIPE_URI + "/" + recipeId + "/addComment", "POST",
                    params, true);

            JSONArray array = new JSONArray(getString(stream));
            stream.close();

            ContentValues cv = new ContentValues();
            cv.put(RecipeContract.Recipe.COLUMN_NAME_COMMENTS, array.toString());

            cr.update(recipeUri, cv, null, null);
            cr.notifyChange(RecipeContract.Recipe.CONTENT_URI, null, false);
        } catch (Exception e) {
            Log.e(TAG, "Error occured in addComment", e);
        }

        return false;
    }

    // get HTTP stream (cascading function, see below)
    public static InputStream getStream(String uri, String requestMethod,
            List<NameValuePair> params, boolean includeAuthToken) throws IOException {
    	
    	// helpful logging for debugging
        Log.i(TAG, "Uri: " + uri);
        Log.i(TAG, "Request Method: " + requestMethod);
        Log.i(TAG, "Include Auth Token: " + includeAuthToken);
        
        // setup URL connection
        URL url = new URL(BASE_URL + uri);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(NET_READ_TIMEOUT_MILLIS);
        conn.setConnectTimeout(NET_CONNECT_TIMEOUT_MILLIS);
        conn.setRequestMethod(requestMethod);
        conn.setDoInput(true);

        // set up request parameters, if needed
        if (params != null || includeAuthToken) {
            conn.setDoOutput(true);

            if (includeAuthToken) {
            	// add auth token to parameters to be sent
                if (params == null) {
                    params = new ArrayList<NameValuePair>(1);
                }
                params.add(new BasicNameValuePair("access_token", BaseActivity.getAccessToken()));
            }

            Log.i(TAG, "Params: " + params.toString());

            // write parameters to output stream
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(URLEncodedUtils.format(params, "UTF-8"));
            writer.flush();
            writer.close();
            os.close();
        }

        // connect to server
        conn.connect();

        Log.i(TAG, conn.getHeaderFields().toString());

        // return input stream for further processing
        return new BufferedInputStream(conn.getInputStream());
    }

    // simplified version of getStream(), allows for a specific request method
    public static InputStream getStream(String uri, String requestMethod) throws IOException {
        return getStream(uri, requestMethod, null);
    }

    // simplified version of getStream(), allows for setting request method and parameters
    public static InputStream getStream(String uri, String requestMethod, List<NameValuePair> params)
            throws IOException {
        return getStream(uri, requestMethod, params, false);
    }

   // simplified version of getStream(), allows for specific request method and the inclusion of an auth token
    public static InputStream getStream(String uri, String requestMethod, boolean includeAuthToken)
            throws IOException {
        return getStream(uri, requestMethod, null, includeAuthToken);
    }

   // simplified version of getStream(), just perform a GET request on URL
    public static InputStream getStream(String uri) throws IOException {
        return getStream(uri, "GET");
    }

    // convert InputString to String
    public static String getString(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = in.read(buffer, 0, 4096)) > 0) {
            out.write(buffer, 0, read);
        }
        return new String(out.toByteArray());
    }

    // perform a POST request to upload recipe
    public static boolean uploadRecipe(ContentResolver cr, Bitmap bitmap, String name,
            List<String> ingredients, List<String> instructions) {
    	
    	// setup params
        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
        params.add(new BasicNameValuePair("userId", Integer.toString(1)));
        params.add(new BasicNameValuePair("name", name));
        for (int i = 0; i < ingredients.size(); i++) {
            params.add(new BasicNameValuePair("ingredients[" + i + "]", ingredients.get(i)));
        }
        for (int i = 0; i < instructions.size(); i++) {
            params.add(new BasicNameValuePair("instructions[" + i + "]", instructions.get(i)));
        }

        try {
        	// perform POST
            InputStream is = getStream(RECIPE_URI + "/create", "POST", params, true);

            JSONObject o = new JSONObject(getString(is));
            is.close();

            Recipe r = RecipeHelper.getRecipe(o);

            // next step: upload image
            return uploadImage(cr, r.getRecipeId(), bitmap);
        } catch (Exception e) {
            Log.e(TAG, "Error occured while uploading recipe", e);
            return false;
        }
    }

    // constants for performing our form upload
    private static final String ATTACHMENT_NAME = "image";
    private static final String ATTACHMENT_FILE_NAME = "image.png";
    private static final String CRLF = "\r\n";
    private static final String TWO_HYPHENS = "--";
    private static final String BOUNDARY = "----NomAndroid";

    // upload image -- based on tutorials below:
    // http://sunil-android.blogspot.com/2013/03/image-upload-on-server.html
    // http://stackoverflow.com/questions/11766878/sending-files-using-post-with-httpurlconnection
    private static boolean uploadImage(ContentResolver cr, String id, Bitmap image) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.close();
            byte[] bytes = stream.toByteArray();

            // setup HTTPURLConnection
            HttpURLConnection httpUrlConnection = null;
            URL url = new URL(BASE_URL + RECIPE_URI + "/" + id + "/uploadImages?access_token="
                    + BaseActivity.getAccessToken());
            httpUrlConnection = (HttpURLConnection) url.openConnection();
            httpUrlConnection.setUseCaches(false);
            httpUrlConnection.setDoOutput(true);
            httpUrlConnection.setRequestMethod("POST");
            httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
            httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
            httpUrlConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary="
                    + BOUNDARY);

            // begin output
            DataOutputStream request = new DataOutputStream(httpUrlConnection.getOutputStream());

            // begin content
            request.writeBytes(TWO_HYPHENS + BOUNDARY + CRLF);
            request.writeBytes("Content-Disposition: form-data; name=\"" + ATTACHMENT_NAME
                    + "\";filename=\"" + ATTACHMENT_FILE_NAME + "\"" + CRLF);
            request.writeBytes(CRLF);

            // write image to stream
            request.write(bytes);

            // end content
            request.writeBytes(CRLF);
            request.writeBytes(TWO_HYPHENS + BOUNDARY + TWO_HYPHENS + CRLF);

            request.flush();
            request.close();

            Log.i(TAG, httpUrlConnection.getHeaderFields().toString());

            // get response from server
            InputStream is = new BufferedInputStream(httpUrlConnection.getInputStream());

            JSONObject o = new JSONObject(getString(is));
            is.close();

            Recipe r = RecipeHelper.getRecipe(o);

            // set up content to be inserted into local database via content provider
            ContentValues cv = new ContentValues();
            cv.put(RecipeContract.Recipe.COLUMN_NAME_RECIPE_ID, r.getRecipeId());
            cv.put(RecipeContract.Recipe.COLUMN_NAME_NAME, r.getName());
            cv.put(RecipeContract.Recipe.COLUMN_NAME_INGREDIENTS, r.getIngredientsJSONString());
            cv.put(RecipeContract.Recipe.COLUMN_NAME_INSTRUCTIONS, r.getInstructionsJSONString());
            cv.put(RecipeContract.Recipe.COLUMN_NAME_COMMENTS, r.getCommentsJSONString());
            cv.put(RecipeContract.Recipe.COLUMN_NAME_LIKES, r.getNumLikes());
            cv.put(RecipeContract.Recipe.COLUMN_NAME_PRIMARY_IMAGE_URL, r.getImage(0));
            cv.put(RecipeContract.Recipe.COLUMN_NAME_CREATED_AT, r.getCreatedAt());
            cv.put(RecipeContract.Recipe.COLUMN_NAME_UPDATED_AT, r.getUpdatedAt());

            // insert new recipe
            cr.insert(RecipeContract.Recipe.CONTENT_URI, cv);
            
            // notify listeners that change has occurred
            cr.notifyChange(RecipeContract.Recipe.CONTENT_URI, null, false);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error occured while uploading image", e);
            return false;
        }
    }

    // task to add recipe asynchronously
    public static class AddRecipeAsyncTask extends AsyncTask<Object, Void, Boolean> {
        @SuppressWarnings("unchecked")
        @Override
        protected Boolean doInBackground(Object... params) {
            return uploadRecipe((ContentResolver) params[0], (Bitmap) params[1],
                    (String) params[2], (List<String>) params[3], (List<String>) params[4]);
        }
    }

    // task to add comment asynchronously
    public static class AddCommentAsyncTask extends AsyncTask<Object, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Object... params) {
            return addComment((ContentResolver) params[0], (Uri) params[1], (String) params[2],
                    (String) params[3]);
        }
    }

    // task to like recipe asynchronously
    public static class LikeAsyncTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            return like(params[0]);
        }
    }

    // task to unlike recipe asynchronously
    public static class UnlikeAsyncTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            return unlike(params[0]);
        }
    }

}
