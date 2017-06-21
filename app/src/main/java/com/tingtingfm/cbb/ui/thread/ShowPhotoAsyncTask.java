package com.tingtingfm.cbb.ui.thread;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.tingtingfm.cbb.common.utils.ScreenUtils;

import java.io.IOException;

public class ShowPhotoAsyncTask extends AsyncTask<String, Void, Bitmap> {
    public static final int MESSAGE_PHOTO_LOAD_END = 0x1001;
    public static final int MESSAGE_PHOTO_LOAD_START = 0x1002;

    private Handler mHandler;

    public ShowPhotoAsyncTask(Handler handler) {
        mHandler = handler;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mHandler.obtainMessage(MESSAGE_PHOTO_LOAD_START).sendToTarget();
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        return getReversalBitmap(params[0]);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        Message message = mHandler.obtainMessage();
        message.what = MESSAGE_PHOTO_LOAD_END;
        message.obj = bitmap;
        message.sendToTarget();
    }

    private Bitmap getReversalBitmap(String path) {
        Bitmap bm = getCompressBitmap(path);

        String postfix = path.substring(path.lastIndexOf(".")).toLowerCase();
        if (postfix.endsWith("jpg")) {
            int digree = 0;
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(path);
            } catch (IOException e) {
                e.printStackTrace();
                exif = null;
            }
            if (exif != null) {
                // 读取图片中相机方向信息
                int ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);
                // 计算旋转角度
                switch (ori) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        digree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        digree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        digree = 270;
                        break;
                    default:
                        digree = 0;
                        break;
                }
            }

            if (digree != 0) {
                // 旋转图片
                Matrix m = new Matrix();
                m.postRotate(digree);
                bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
                        bm.getHeight(), m, true);
            }
        }

        return bm;
    }

    private Bitmap getCompressBitmap(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(path, options);

        int bitmapHeight = options.outHeight;
        int bitmapWidth = options.outWidth;
        int height = ScreenUtils.getScreenHeight();
        int width = ScreenUtils.getScreenWidth();

        System.out.println("bitmapHeight: " + bitmapHeight + " bitmapWidth: " + bitmapWidth
                + " height: " + height + " width: " + width);
        int inSampleSize = 1;
        if (bitmapHeight > height || bitmapWidth > width) {
            inSampleSize = Math.max(bitmapHeight / height, bitmapWidth / width);
        }

        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
//        options.inDither = false;
//        options.inPreferredConfig = null;

        return BitmapFactory.decodeFile(path, options);
    }
}