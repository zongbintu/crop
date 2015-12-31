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
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

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
            final Activity context = handler.getContext();
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
                    if ("false".equals(handler.getCropParams().crop)) {
//                        handler.onPhotoCropped(uriFormat(handler.getContext(), handler.getCropParams().uri));
                        Uri uri = uriFormat(context, handler.getCropParams().uri);
                        if (!new File(uri.getPath()).exists()) {
                            handler.onCropFailed(context.getString(R.string.msg_error_file_not_found));
                            return;
                        } else {
                            try {
                                String fileName = BitmapUtil.saveFile(context, BitmapUtil.compressImage(uri.getPath(), 800f, 480f, 100));
                                uri = Uri.fromFile(new File(fileName));
                            } catch (IOException e) {
                                Log.e(TAG, "save bitmap file", e);
                            }
                        }
                        handler.onPhotoCropped(uri);
                        return;
                    } else {
                        if (isKitKat())
                            handler.getCropParams().uri = uriFormat(context, handler.getCropParams().uri);
                        intent = buildCropFromUriIntent(handler);
                    }
                    break;
                case REQUEST_CAMERA:
                    if ("false".equals(handler.getCropParams().crop)) {
//                        handler.onPhotoCropped(uriFormat(handler.getContext(), handler.getCropParams().uri));
                        Uri uri = uriFormat(context, handler.getCropParams().uri);
                        try {
                            String fileName = BitmapUtil.saveFile(context, BitmapUtil.compressImage(uri.getPath(), 800f, 480f, 100));
                            uri = Uri.fromFile(new File(fileName));
                        } catch (IOException e) {
                            Log.e(TAG, "save bitmap file", e);
                        }
                        handler.onPhotoCropped(uri);
                        return;
                    }
                    intent = buildCropFromUriIntent(handler);
                    break;
            }
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

    /**
     * 删除所有缓存crop文件
     *
     * @param context
     * @return
     */
    public static boolean cleanAllCropCache(Context context) {
        final String dir = getCropCacheDir(context);
        File file = new File(dir);
        boolean flag = false;
        if (file.exists())
            flag = deleteDirectory(file);
        else
            flag = true;
        return flag;
    }

    private static boolean deleteDirectory(File dir) {
        if (!dir.exists() && !dir.isDirectory()) {
            return false;
        }
        File[] files = dir.listFiles();
        boolean flag = true;
        for (File file : files) {
            if (file.isFile())
                flag = file.delete();
            else
                flag = deleteDirectory(file);
            if (!flag) break;
        }
        if (!flag) return false;
        //删除当前目录
        if (dir.delete()) {
            return true;
        } else {
            return false;
        }
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
        handler.getCropParams().cropResult = Uri.fromFile(new File(getCropFilePath(handler.getContext())));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, handler.getCropParams().cropResult);
        return intent;
    }

    /**
     * 生成Crop文件路径
     *
     * @param context
     * @return
     */
    public static String getCropFilePath(Context context) {
        final String dir = getCropCacheDir(context);
        File file = new File(dir);
        if (!file.isDirectory() && !file.exists())
            file.mkdirs();
        String path = dir + new Date().getTime() + ".jpg";
        file = null;
        return path;
    }

    /**
     * 获取crop缓存路径
     *
     * @param context
     * @return
     */
    private static String getCropCacheDir(Context context) {
        File file = context.getExternalCacheDir();
        if (file == null)
            file = context.getCacheDir();
        return file.getPath() + File.separatorChar + "crop" + File.separator;
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

    /**
     * 版本是否大于等于KitKat
     *
     * @return
     */
    public static boolean isKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    //以下是关键，原本uri返回的是file:///...来着的，android4.4返回的是content:///...
    @SuppressLint("NewApi")
    public static Uri uriFormat(final Context context, final Uri uri) {

        final boolean isKitKat = isKitKat();

        String path = null;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    path = Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                path = getDataColumn(context, contentUri, null, null);
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

                path = getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri))
                path = uri.getLastPathSegment();

            path = getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            path = uri.getPath();
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            String url = getPath(handler.getContext(), handler.getCropParams().uri);
//            return Uri.fromFile(new File(url));
//        }
        File file = new File(path);
        if (!TextUtils.isEmpty(path) && file.exists())
            if (isKitKat)
                return Uri.fromFile(file);
            else
                return Uri.parse(path);
        else
            return uri;
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
