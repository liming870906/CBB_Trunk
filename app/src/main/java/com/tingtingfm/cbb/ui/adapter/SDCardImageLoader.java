package com.tingtingfm.cbb.ui.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.LruCache;
import android.widget.ImageView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by liming on 16/12/29.
 */

public class SDCardImageLoader {
    public static final int sampleScale = 256;
    //缓存
    private LruCache<String, Bitmap> imageCache;
    // 固定2个线程来执行任务
    private ExecutorService executorService = Executors.newFixedThreadPool(3);
    private Handler handler = new Handler();

//    private int screenW, screenH;
    private static volatile SDCardImageLoader manager = null;

    /**
     * 初始化方法
     * @return SDCardImageLoader对象
     */
    public static SDCardImageLoader getInstance() {
        SDCardImageLoader cache = manager;
        if (cache == null) {
            synchronized (SDCardImageLoader.class) {
                cache = manager;
                if (cache == null) {
                    cache = new SDCardImageLoader();
                    manager = cache;
                }
            }
        }
        return cache;
    }

    private SDCardImageLoader(/*int screenW, int screenH*/) {
//        this.screenW = screenW;
//        this.screenH = screenH;

        // 获取应用程序最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 8;

        // 设置图片缓存大小为程序最大可用内存的1/8
        imageCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
    }

    private Bitmap loadDrawable(final int smallRate, final String filePath,
                                final ImageCallback callback) {
        // 如果缓存过就从缓存中取出数据
        if (imageCache.get(filePath) != null) {
            return imageCache.get(filePath);
        }

        // 如果缓存没有则读取SD卡
        executorService.submit(new Runnable() {
            public void run() {
                try {
                    BitmapFactory.Options opt = new BitmapFactory.Options();
                    opt.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(filePath, opt);

                    // 获取到这个图片的原始宽度和高度
                    int picWidth = opt.outWidth;
                    int picHeight = opt.outHeight;

                    //读取图片失败时直接返回
                    if (picWidth == 0 || picHeight == 0) {
                        return;
                    }

                    //初始压缩比例
                    opt.inSampleSize = smallRate;
                    // 根据屏的大小和图片大小计算出缩放比例
                    if (picWidth >= picHeight) {
                        if (picHeight >= sampleScale) {
                            opt.inSampleSize = picHeight / sampleScale;
                        } else {
                            opt.inSampleSize = sampleScale / picHeight;
                        }
                    } else {
                        if (picWidth >= sampleScale) {
                            opt.inSampleSize = picWidth / sampleScale;
                        } else {
                            opt.inSampleSize = sampleScale / picWidth;
                        }
                    }

                    //这次再真正地生成一个有像素的，经过缩放了的bitmap
                    opt.inJustDecodeBounds = false;
                    final Bitmap bmp = BitmapFactory.decodeFile(filePath, opt);
                    int width = bmp.getWidth();
                    int height = bmp.getHeight();
                    int dstWidth = width;
                    if (width > height) {
                        dstWidth = height;
                    }
                    System.out.println("width: " + width + " --- height: " + height);
                    final Bitmap bmpCopy = Bitmap.createBitmap(bmp, 0, 0, dstWidth, dstWidth, null, false);
                    System.out.println("++++++++++++++++width: " + bmpCopy.getWidth() + " --- height: " + bmpCopy.getHeight());
                    //存入map
                    imageCache.put(filePath, bmpCopy);

                    handler.post(new Runnable() {
                        public void run() {
                            callback.imageLoaded(bmpCopy);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return null;
    }

    /**
     * 异步读取SD卡图片，并按指定的比例进行压缩（最大不超过屏幕像素数）
     *
     * @param smallRate 压缩比例，不压缩时输入1，此时将按屏幕像素数进行输出
     * @param filePath  图片在SD卡的全路径
     * @param imageView 组件
     */
    public void loadImage(int smallRate, final String filePath, final ImageView imageView) {

        Bitmap bmp = loadDrawable(smallRate, filePath, new ImageCallback() {

            @Override
            public void imageLoaded(Bitmap bmp) {
                if (filePath.equals(imageView.getTag())) {
                    if (bmp != null) {
                        imageView.setImageBitmap(bmp);
                    } else {
                        imageView.setImageResource(0);
                    }
                }
            }
        });

        if (bmp != null) {
            if (imageView.getTag().equals(filePath)) {
                imageView.setImageBitmap(bmp);
            }
        } else {
            imageView.setImageResource(0);
        }

    }


    /**
     * 对外界开放的回调接口
      */
    public interface ImageCallback {
        /**
         * 设置目标对象的图像资源
          */
        void imageLoaded(Bitmap imageDrawable);
    }
}
