package cn.talianshe.android.utils;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.talianshe.android.net.GlobalParams;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

public class PhotoCropUtil {


    public static RectF getPhotoRect(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options); // 此时返回的bitmap为null
        Log.i("PhotoCropUtil", "Bitmap Height == " + options.outHeight);
        return new RectF(0, 0, options.outWidth, options.outHeight);
    }

    public static String centerCropPhoto(String imgPath, float scale) {
        try {
            String newPath = getNewFileSavePath();
            RectF rect = getPhotoRect(imgPath);
            if (rect.height() * scale == rect.width()) {
                return imgPath;
            }
            float width = rect.width();
            float newHeight = width * scale;
            float startY = (rect.height() - newHeight) / 2;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(imgPath, options);
            Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, (int) startY, (int) width, (int) newHeight);
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(newPath));
            if(!bitmap.isRecycled()){
                bitmap.recycle();
            }
            if(!newBitmap.isRecycled()){
                newBitmap.recycle();
            }
            return newPath;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return imgPath;
    }

    public static String compressImage(Context context, String imgPath) {
        try {
            List<File> newFiles = Luban.with(context).load(imgPath).setTargetDir(getImgStorePath(context)).get();
            imgPath = newFiles.get(0).getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return imgPath;
    }

    public static String centerCropPhoto(Context context, String imgPath, float scale) {
        try {
            List<File> newFiles = Luban.with(context).load(imgPath).setTargetDir(getImgStorePath(context)).get();
            imgPath = newFiles.get(0).getAbsolutePath();
            RectF rect = getPhotoRect(imgPath);
            if (rect.height() * scale == rect.width()) {
                return imgPath;
            }
            String newPath = getNewFileSavePath();
            float width = rect.width();
            float newHeight = width * scale;
            float startY = (rect.height() - newHeight) / 2;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(imgPath, options);
            Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, (int) startY, (int) width, (int) newHeight);
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(newPath));
            if(!bitmap.isRecycled()){
                bitmap.recycle();
            }
            if(!newBitmap.isRecycled()){
                newBitmap.recycle();
            }
            return newPath;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return imgPath;
    }

    private static void compressImage(Context context, List<String> imgPaths, OnCompressListener listener) {
        String storageDir = getImgStorePath(context);
        Luban.with(context)
                .load(imgPaths)                                   // 传人要压缩的图片列表
                .ignoreBy(100)                                  // 忽略不压缩图片的大小
                .setTargetDir(storageDir)                        // 设置压缩后文件存储位置
                .setCompressListener(listener).launch();    //启动压缩
    }

    @NonNull
    private static String getImgStorePath(Context context) {
        File storageDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), GlobalParams.TEMP_PIC_DIR);
        if(!storageDir.exists())
            storageDir.mkdir();
        return storageDir.getAbsolutePath();
    }

    //获取裁剪图片保存的新路径
    private static String getNewFileSavePath() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
//        mCurrentPhotoPath = image.getAbsolutePath();
        return image.getAbsolutePath();
    }
}
