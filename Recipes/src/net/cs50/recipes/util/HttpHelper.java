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

import net.cs50.recipes.provider.RecipeContract;
import net.cs50.recipes.types.Recipe;

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

public class HttpHelper {
    private static final String TAG = "HttpHelper";

    private static final String BASE_URL = "http://nom.hrvd.io/";

    private static final String CREATE_USER_URI = "user/create";

    private static final String AUTHORIZE_USER_URI = "authorize/";

    private static final String RECIPE_URI = "recipe";

    // Network connection timeout, in milliseconds.
    private static final int NET_CONNECT_TIMEOUT_MILLIS = 15000; // 15 seconds

    // Network read timeout, in milliseconds.
    private static final int NET_READ_TIMEOUT_MILLIS = 10000; // 10 seconds

    private static String mAuthToken = null;

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

    public static InputStream getStream(String uri, String requestMethod,
            List<NameValuePair> params, boolean includeAuthToken) throws IOException {
        Log.i(TAG, "Uri: " + uri);
        Log.i(TAG, "Request Method: " + requestMethod);
        Log.i(TAG, "Include Auth Token: " + includeAuthToken);
        URL url = new URL(BASE_URL + uri);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(NET_READ_TIMEOUT_MILLIS /* milliseconds */);
        conn.setConnectTimeout(NET_CONNECT_TIMEOUT_MILLIS /* milliseconds */);
        conn.setRequestMethod(requestMethod);
        conn.setDoInput(true);

        if (params != null || includeAuthToken) {
            conn.setDoOutput(true);

            if (includeAuthToken) {
                if (params == null) {
                    params = new ArrayList<NameValuePair>(1);
                }
                params.add(new BasicNameValuePair("access_token", getAuthToken()));
            }

            Log.i(TAG, "Params: " + params.toString());

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(URLEncodedUtils.format(params, "UTF-8"));
            writer.flush();
            writer.close();
            os.close();
        }

        conn.connect();

        Log.i(TAG, conn.getHeaderFields().toString());

        return new BufferedInputStream(conn.getInputStream());
    }

    public static InputStream getStream(String uri, String requestMethod) throws IOException {
        return getStream(uri, requestMethod, null);
    }

    public static InputStream getStream(String uri, String requestMethod, List<NameValuePair> params)
            throws IOException {
        return getStream(uri, requestMethod, params, false);
    }

    public static InputStream getStream(String uri, String requestMethod, boolean includeAuthToken)
            throws IOException {
        return getStream(uri, requestMethod, null, includeAuthToken);
    }

    public static InputStream getStream(String uri) throws IOException {
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

    private static String getAuthToken() {
        return "577af53fc6c3b4ee55ba597b3830998b77e6dda2";
        /*
         * if (mAuthToken != null) { return mAuthToken; } return SyncUtils.getCurrentAuthToken();
         */
    }

    public static boolean uploadRecipe(ContentResolver cr, Bitmap bitmap, String name,
            List<String> ingredients, List<String> instructions) {
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
            InputStream is = getStream(RECIPE_URI + "/create", "POST", params, true);

            JSONObject o = new JSONObject(getString(is));
            is.close();

            Recipe r = RecipeHelper.getRecipe(o);

            return uploadImage(cr, r.getRecipeId(), bitmap);
        } catch (Exception e) {
            Log.e(TAG, "Error occured while uploading recipe", e);
            return false;
        }
    }

    private static final String ATTACHMENT_NAME = "image";
    private static final String ATTACHMENT_FILE_NAME = "image.png";
    private static final String CRLF = "\r\n";
    private static final String TWO_HYPHENS = "--";
    private static final String BOUNDARY = "----NomAndroid";

    // http://sunil-android.blogspot.com/2013/03/image-upload-on-server.html
    // http://stackoverflow.com/questions/11766878/sending-files-using-post-with-httpurlconnection
    private static boolean uploadImage(ContentResolver cr, String id, Bitmap image) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.close();
            byte[] bytes = stream.toByteArray();

            HttpURLConnection httpUrlConnection = null;
            URL url = new URL(BASE_URL + RECIPE_URI + "/" + id + "/uploadImages?access_token="
                    + getAuthToken());
            httpUrlConnection = (HttpURLConnection) url.openConnection();
            httpUrlConnection.setUseCaches(false);
            httpUrlConnection.setDoOutput(true);

            httpUrlConnection.setRequestMethod("POST");
            httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
            httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
            httpUrlConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary="
                    + BOUNDARY);

            DataOutputStream request = new DataOutputStream(httpUrlConnection.getOutputStream());

            request.writeBytes(TWO_HYPHENS + BOUNDARY + CRLF);
            request.writeBytes("Content-Disposition: form-data; name=\"" + ATTACHMENT_NAME
                    + "\";filename=\"" + ATTACHMENT_FILE_NAME + "\"" + CRLF);
            request.writeBytes(CRLF);

            request.write(bytes);

            request.writeBytes(CRLF);
            request.writeBytes(TWO_HYPHENS + BOUNDARY + TWO_HYPHENS + CRLF);

            request.flush();
            request.close();

            Log.i(TAG, httpUrlConnection.getHeaderFields().toString());

            InputStream is = new BufferedInputStream(httpUrlConnection.getInputStream());

            JSONObject o = new JSONObject(getString(is));
            is.close();

            Recipe r = RecipeHelper.getRecipe(o);

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

            cr.insert(RecipeContract.Recipe.CONTENT_URI, cv);
            cr.notifyChange(RecipeContract.Recipe.CONTENT_URI, null, false);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error occured while uploading image", e);
            return false;
        }
    }

    public static class AddRecipeAsyncTask extends AsyncTask<Object, Void, Boolean> {
        @SuppressWarnings("unchecked")
        @Override
        protected Boolean doInBackground(Object... params) {
            return uploadRecipe((ContentResolver) params[0], (Bitmap) params[1],
                    (String) params[2], (List<String>) params[3], (List<String>) params[4]);
        }
    }

    public static class AddCommentAsyncTask extends AsyncTask<Object, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Object... params) {
            return addComment((ContentResolver) params[0], (Uri) params[1], (String) params[2],
                    (String) params[3]);
        }
    }

    public static class LikeAsyncTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            return like(params[0]);
        }
    }

    public static class UnlikeAsyncTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            return unlike(params[0]);
        }
    }

}
