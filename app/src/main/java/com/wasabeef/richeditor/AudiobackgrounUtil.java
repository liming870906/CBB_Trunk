package com.wasabeef.richeditor;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.common.utils.DensityUtils;
import com.tingtingfm.cbb.common.utils.StorageUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by admin on 2016/12/16.
 */

public class AudiobackgrounUtil {
    public static final String audioImgPath = "/cc_audiofile";

    /**
     * 给定音频名字，时间。获取音频背景文件(根据设计UI样式)
     *
     * @param audioName
     * @param times
     * @return
     */
    public static String getAudioBackground(Context context, String fileName, String audioName, String times,int webViewWidth) {
        String outPath = null;
        try {
            int width = (int) context.getResources().getDimension(R.dimen.dp_270);
            int height = (int) context.getResources().getDimension(R.dimen.dp_49_6);
            Paint paint = new Paint();
            Bitmap bitmap = Bitmap.createBitmap(webViewWidth, height, Bitmap.Config.ARGB_4444);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE);

            //画音频图标
            int margin = (int) context.getResources().getDimension(R.dimen.dp_7_3);
            canvas.drawBitmap(getDefultImage(context, "audio_img.png"), margin, margin, paint);
            //画音频名称文字
            drawTitle(context, canvas,
                    audioName,
                    paint,
                    DensityUtils.px2dp(context, context.getResources().getDimension(R.dimen.text_size_46)),//字体
                    context.getResources().getColor(R.color.color_444444),//颜色
                    (int) context.getResources().getDimension(R.dimen.dp_48_7),//距离左边
                    (int) context.getResources().getDimension(R.dimen.dp_15),//距离上边
                    (int) context.getResources().getDimension(R.dimen.dp_120_3));//宽度

            //画音频时间文字
            int timeWidth = (int) context.getResources().getDimension(R.dimen.dp_204_7);
            if(!TextUtils.isEmpty(times)){
                String[] ti = times.split(":");
                if (ti.length > 2) {
                    timeWidth = (int) context.getResources().getDimension(R.dimen.dp_186);
                } else {
                    timeWidth = (int) context.getResources().getDimension(R.dimen.dp_204_7);
                }
            }
            paint.setAntiAlias(true);
            drawTitle(context, canvas,
                    times,
                    paint,
                    DensityUtils.px2dp(context, context.getResources().getDimension(R.dimen.text_size_40)),//字体
                    context.getResources().getColor(R.color.color_697FB4),//颜色
                    timeWidth,//距离左边
                    (int) context.getResources().getDimension(R.dimen.dp_15_3),//距离上边
                    (int) context.getResources().getDimension(R.dimen.dp_66));//宽度

            //更多图标
            int moreMarginleft = (int) context.getResources().getDimension(R.dimen.dp_248);//距离左边
            int moreMarginTop = (int) context.getResources().getDimension(R.dimen.dp_18);//距离上边
            canvas.drawBitmap(getDefultImage(context, "audio_img_more.png"), moreMarginleft, moreMarginTop, paint);

            //画边框
            drawRoundRect(context, paint, canvas, width, height);

            //保存图片
            String dirPath = getMascriptImgPath(context) + fileName;
            File file = new File(dirPath);
            if (!file.exists()) {
                file.mkdirs();
            }
            outPath = dirPath + File.separator + System.currentTimeMillis() + ".jpg";
            FileOutputStream mFOS = new FileOutputStream(outPath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, mFOS);// (0 - 100)压缩文件
            if (null != bitmap) {
                bitmap.recycle();
                bitmap = null;
                canvas = null;
            }
            mFOS.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outPath;
    }

    /**
     * 获取稿件图片存放路径
     *
     * @param context
     */
    public static String getMascriptImgPath(Context context) {

        return StorageUtils.getSDCardStorageDirectory(context).toString() + audioImgPath + File.separator;
    }
    /**
     * 删除该稿件下所有图片
     *
     * @param context
     * @param fileName
     */
    public static void deleteAllImg(Context context, String fileName) {
        String imgFilePath = getMascriptImgPath(context) + fileName;
        File file = new File(imgFilePath);
        if (file.exists()) {
            File[] imgFiles = file.listFiles();
            for (int i = 0; i < imgFiles.length; i++) {
                imgFiles[i].delete();
            }
            file.delete();
        }
    }

    //画边框
    private static void drawRoundRect(Context con, Paint paint, Canvas canvas, int width, int height) {
        paint.setStyle(Paint.Style.FILL);//充满
        paint.setColor(con.getResources().getColor(R.color.color_c5c5c5));
        paint.setStyle(Paint.Style.STROKE);//设置空心
        paint.setAntiAlias(true);// 设置画笔的锯齿效果
        paint.setStrokeWidth(con.getResources().getDimension(R.dimen.dp_0_3));
        RectF oval3 = new RectF(1, 1, width - 1, height - 1);// 设置个新的长方形
        canvas.drawRoundRect(oval3, 5, 5, paint);//第二个参数是x半径，第三个参数是y半径
    }

    private static Bitmap getDefultImage(Context con, String fileName) {
        Bitmap image = null;
        AssetManager am = con.getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
            return image;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;

    }

    private static void drawTitle(Context context, Canvas canvas, String audioName, Paint paint
            ,int textSize, int textColor, int marginleft, int marginTop,int width) {
        TextView titleView = new TextView(context);
        titleView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        titleView.setWidth(width);
        titleView.setLines(1);
        titleView.setEllipsize(TextUtils.TruncateAt.END);
        titleView.setTextSize(textSize);
        titleView.setTextColor(textColor);
        titleView.setText(audioName);
        Bitmap titleBitmap = convertViewToBitmap(titleView);
        canvas.drawBitmap(titleBitmap, marginleft, marginTop, paint);
    }

    public static Bitmap convertViewToBitmap(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }
}
