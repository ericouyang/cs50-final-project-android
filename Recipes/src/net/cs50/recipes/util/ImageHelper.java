package net.cs50.recipes.util;

import java.io.InputStream;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

public class ImageHelper {

    private static final String TAG = "ImageHelper";
    private static final String REMOTE_URL = "http://ericouyang.com:1337/";

    private static LruCache<String, Bitmap> mImageMemoryCache;
    static {
        // based on http://developer.android.com/training/displaying-bitmaps/cache-bitmap.html

        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mImageMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };

        Log.i(TAG, "Image Cache Created");
    }

    private ImageHelper()
    {
    }

    public static void loadBitmap(String url, ImageView imageView) {
        final Bitmap bitmap = getBitmapFromMemCache(url);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            // imageView.setImageResource(R.drawable.image_placeholder);
            DownloadImageTask task = new DownloadImageTask(imageView);
            task.execute(url);
        }
    }

    public static void addBitmapToMemoryCache(String url, Bitmap bitmap) {
        if (getBitmapFromMemCache(url) == null) {
            mImageMemoryCache.put(url, bitmap);
        }
    }

    public static Bitmap getBitmapFromMemCache(String url) {
        return mImageMemoryCache.get(url);
    }

    // based on http://stackoverflow.com/questions/2471935/how-to-load-an-imageview-by-url-in-android
    public static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        private static final String TAG = "DownloadImageTask";

        ImageView mImageView;

        public DownloadImageTask(ImageView imageView) {
            Log.i(TAG, "new download image task");
            mImageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap image = null;
            try {
                // see also http://stackoverflow.com/questions/1945201/android-image-caching

                InputStream in = new URL(REMOTE_URL + url).openStream();

                image = BitmapFactory.decodeStream(in);

                if (image != null) {
                    addBitmapToMemoryCache(url, image);
                }

            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return image;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            mImageView.setImageBitmap(result);
        }
    }
}
