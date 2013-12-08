package net.cs50.recipes.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

public class ImageHelper {

    public static final int IMAGE_LENGTH = 1024;

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

    private ImageHelper() {
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

    public static Bitmap imageFromUri(Context context, Uri uri, int length) throws IOException {
        BitmapFactory.Options sizeOptions = new BitmapFactory.Options();
        sizeOptions.inJustDecodeBounds = true;
        sizeOptions.inDensity = 0;
        sizeOptions.inPurgeable = true;
        sizeOptions.inInputShareable = true;
        InputStream sizeStream = context.getContentResolver().openInputStream(uri);
        BitmapFactory.decodeStream(sizeStream, null, sizeOptions);
        sizeStream.close();

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        bitmapOptions.inDensity = 0;
        bitmapOptions.inPurgeable = true;
        bitmapOptions.inInputShareable = true;
        bitmapOptions.inSampleSize = computeInSampleSize(sizeOptions, length, length);
        InputStream bitmapStream = context.getContentResolver().openInputStream(uri);
        Bitmap loaded = BitmapFactory.decodeStream(bitmapStream, null, bitmapOptions);
        bitmapStream.close();
        try {
            int orientation = rotationForImage(context, uri);
            if (orientation != 0) {
                Matrix matrix = new Matrix();
                matrix.preRotate(orientation);
                Bitmap best;
                int width = loaded.getWidth(), height = loaded.getHeight();
                if (width >= loaded.getHeight()) {
                    best = Bitmap.createBitmap(loaded, width / 2 - height / 2, 0, height, height,
                            matrix, true);

                } else {
                    best = Bitmap.createBitmap(loaded, 0, height / 2 - width / 2, width, width,
                            matrix, true);
                }
                if (loaded != best) {
                    loaded.recycle();
                }
                return best;
            }
        } catch (Exception ignored) {
        }
        return loaded;
    }

    public static int rotationForImage(Context context, Uri uri) {
        if (uri.getScheme().equals("content")) {
            String[] projection = { MediaStore.Images.ImageColumns.ORIENTATION };
            Cursor localCursor = context.getContentResolver().query(uri, projection, null, null,
                    null);
            if (localCursor.moveToFirst()) {
                return localCursor.getInt(0);
            }
        } else if (uri.getScheme().equals("file")) {
            try {
                float f = exifOrientationToDegrees(new ExifInterface(uri.getPath())
                        .getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_NORMAL));
                return (int) f;
            } catch (IOException localIOException) {
            }
        }
        return 0;
    }

    public static float exifOrientationToDegrees(int orientation) {
        switch (orientation) {
        case ExifInterface.ORIENTATION_ROTATE_90:
            return 90.0F;
        case ExifInterface.ORIENTATION_ROTATE_180:
            return 180.0F;
        case ExifInterface.ORIENTATION_ROTATE_270:
            return 270.0F;
        default:
            return 0.0F;
        }
    }

    public static int computeInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static String getMediaPath(Context context, Uri uri) {
        String[] projections = { MediaColumns.DATA };
        Cursor cursor = context.getContentResolver().query(uri, projections, null, null, null);
        String path = null;
        if (cursor != null) {
            int i = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            path = cursor.getString(i);
            cursor.close();
        }
        return path;
    }

    // based on
    // http://stackoverflow.com/questions/2471935/how-to-load-an-imageview-by-url-in-android
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
