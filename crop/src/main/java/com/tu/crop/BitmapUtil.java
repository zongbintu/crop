package com.tu.crop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * Created by Tu on 15/12/29.
 */
public class BitmapUtil {
    /**
     * 按质量压缩图片
     * @param srouceBitmap
     * @param size   文件压缩后大小(K)
     * @return  Bitmap
     */
    public static Bitmap compressImage(Bitmap srouceBitmap, int size) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        srouceBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > size) {    //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            srouceBitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    /**
     * 指定文件大小比例缩放
     * @param srcPath   源文件
     * @param targetHeight  缩放高度    现在主流手机比较多是800*480分辨率
     * @param targetWidth   缩放宽度
     * @param size  文件大小(K)
     * @return  Bitmap
     */
    public static Bitmap compressImage(String srcPath,float targetHeight,float targetWidth,int size) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, crateCompressOptions(newOpts,targetHeight, targetWidth));
        if (null != bitmap)
            //压缩好比例大小后再进行质量压缩
            return compressImage(bitmap, size);
        return bitmap;
    }

    /**
     * 指定高度、宽度比例缩放
     * @param sourceBitmap  源图
     * @param targetHeight  缩放高度    现在主流手机比较多是800*480分辨率
     * @param targetWidth   缩放宽度
     * @param size  文件大小(K)
     * @return  Bitmap
     */
    private Bitmap compressImage(Bitmap sourceBitmap,float targetHeight,float targetWidth,int size) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        sourceBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        //判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
        if( baos.toByteArray().length / 1024>1024) {
            //重置baos即清空baos
            baos.reset();
            //这里压缩50%，把压缩后的数据存放到baos中
            sourceBitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        }
        //设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        bitmap = BitmapFactory.decodeStream(isBm, null, crateCompressOptions(newOpts,targetHeight, targetWidth));
        //压缩好比例大小后再进行质量压缩
        return compressImage(bitmap,size);
    }
    /**
     * 根据指定大小比例生成Options
     * @param options
     * @param targetHeight
     * @param targetWidth
     * @return
     */
    private static BitmapFactory.Options crateCompressOptions(BitmapFactory.Options options,float targetHeight,float targetWidth){
        options.inJustDecodeBounds = false;
        int w = options.outWidth;
        int h = options.outHeight;
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > targetWidth) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (options.outWidth / targetWidth);
        } else if (w < h && h > targetHeight) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (options.outHeight / targetHeight);
        }
        if (be <= 0)
            be = 1;
        //设置缩放比例
        options.inSampleSize = be;
        return options;
    }
    /**
     * 保存文件
     * @param context
     * @param bm
     * @return filename
     * @throws IOException
     */
    public static String saveFile(Context context,Bitmap bm) throws IOException {
        String fileName = CropHelper.getCropFilePath(context);
        File file = new File(fileName);
        File dirFile = file.getParentFile();
        if(!dirFile.exists())
            dirFile.mkdir();
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        bos.flush();
        bos.close();
        return fileName;
    }
}
