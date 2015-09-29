package com.tu.crop;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;

/**
 * Created with Android Studio. User: ryan@xisue.com Date: 10/1/14 Time: 11:08
 * AM Desc: CropHelper Revision: - 10:00 2014/10/03 Basic utils. - 11:30
 * 2014/10/03 Add static methods for generating crop intents. - 15:00 2014/10/03
 * Finish the logic of handling crop intents. - 12:20 2014/10/04 Add
 * "scaleUpIfNeeded" crop options for scaling up cropped images if the size is
 * too small.
 */
public class CropHelper {

    public static final String TAG = "CropHelper";

    /**
     * request code of Activities or Fragments You will have to change the
     * values of the request codes below if they conflict with your own.
     */
    public static final int REQUEST_GALLERY = 126;
    public static final int REQUEST_CROP = 127;
    public static final int REQUEST_CAMERA = 128;

    public static final String CROP_CACHE_FILE_NAME = "crop_cache_file.jpg";

    public static Uri buildUri() {
        return Uri.fromFile(Environment.getExternalStorageDirectory())
                .buildUpon().appendPath(CROP_CACHE_FILE_NAME).build();
    }

    public static void handleResult(CropHandler handler, int requestCode,
                                    int resultCode, Intent data) {
        if (handler == null)
            return;

        if (resultCode == Activity.RESULT_CANCELED) {
            handler.onCropCancel();
        } else if (resultCode == Activity.RESULT_OK) {
            CropParams cropParams = handler.getCropParams();
            if (cropParams == null) {
                handler.onCropFailed("CropHandler's params MUST NOT be null!");
                return;
            }
            Intent intent = null;
            switch (requestCode) {
                case REQUEST_CROP:
                    Log.d(TAG, "Photo cropped!");
                    if (handler.getCropParams().returnData) {
                        handler.onPhotoCropped(handler.getCropParams().uri);
                    } else {
                        Uri uri = data.getData();
                        //4.4以下getData为null，getAction()为该uri.4.4及以上getData为正确uri
//                        data.getAction();
                        if (uri == null) {
                            uri = cropParams.cropResult;
                        }
                        handler.onPhotoCropped(uri);
                    }
                    return;
                case REQUEST_GALLERY:
                    handler.getCropParams().uri = data.getData();
                    handler.getCropParams().uri = uriFormat(handler);
                    intent = buildCropFromUriIntent(handler);
                    break;
                case REQUEST_CAMERA:
                    intent = buildCropFromUriIntent(handler);
                    break;
            }
            Activity context = handler.getContext();
            if (context != null) {
                context.startActivityForResult(intent, REQUEST_CROP);
            } else {
                handler.onCropFailed("CropHandler's context MUST NOT be null!");
            }
        }
    }

    public static boolean clearCachedCropFile(Uri uri) {
        if (uri == null)
            return false;

        File file = new File(uri.getPath());
        if (file.exists()) {
            boolean result = file.delete();
            if (result)
                Log.i(TAG, "Cached crop file cleared.");
            else
                Log.e(TAG, "Failed to clear cached crop file.");
            return result;
        } else {
            Log.w(TAG,
                    "Trying to clear cached crop file but it does not exist.");
        }
        return false;
    }

    public static Intent buildCropFromUriIntent(CropHandler handler) {
        return buildCropIntent("com.android.camera.action.CROP", handler);
    }

    public static Intent buildGalleryIntent() {
        return new Intent(Intent.ACTION_GET_CONTENT).setType("image/*");
    }

    public static Intent buildCaptureIntent(Uri uri) {
        return new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(
                MediaStore.EXTRA_OUTPUT, uri);
    }

    public static Intent buildCropIntent(String action, CropHandler handler) {
        CropParams params = handler.getCropParams();
        Intent intent = new Intent(action, null)
                .setDataAndType(params.uri, params.type)
                .putExtra("crop", params.crop)
                .putExtra("aspectX", params.aspectX)
                .putExtra("aspectY", params.aspectY)
                .putExtra("outputX", params.outputX)
                .putExtra("outputY", params.outputY)
                .putExtra("return-data", params.returnData)
                .putExtra("outputFormat", params.outputFormat)
                .putExtra("noFaceDetection", params.noFaceDetection);
        handler.getCropParams().cropResult = crateUri(handler.getContext());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, handler.getCropParams().cropResult);
        return intent;
    }

    private static Uri crateUri(final Context context) {
        File file = context.getExternalCacheDir();
        if (file == null)
            file = context.getCacheDir();
        String path = file.getPath() + File.separatorChar + new Date().getTime() + ".jpg";
        file = null;
        return Uri.fromFile(new File(path));
    }

    /**
     * 为ImageView设置crop
     *
     * @param params
     * @param imageView
     * @param data
     */
    public static void setImageViewCrop(CropParams params, ImageView imageView, Intent data) {

        Bitmap photo = null;
        if (data != null) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                photo = extras.getParcelable("data");
            }
        }
        if (photo == null)//加载本地截图
        {
            Bitmap bm = BitmapFactory.decodeFile(params.uri.getPath());
            imageView.setImageBitmap(bm);
        } else//加载接收的图片数据
        {
            imageView.setImageBitmap(photo);
        }
    }

    public static Bitmap decodeUriAsBitmap(Context context, Uri uri) {
        if (context == null || uri == null)
            return null;

        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeStream(context.getContentResolver()
                    .openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }

    public static Uri uriFormat(CropHandler handler) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String url = getPath(handler.getContext(), handler.getCropParams().uri);
            return Uri.fromFile(new File(url));
        }
        return handler.getCropParams().uri;
    }

    //以下是关键，原本uri返回的是file:///...来着的，android4.4返回的是content:///...
    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}
