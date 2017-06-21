package com.tingtingfm.cbb.ui.adapter;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.LruCache;
import android.widget.ImageView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 视频图片方法
 * Created by liming on 17/4/21.
 */

public class VideoImageLoader {
    private static volatile VideoImageLoader manager = null;
    private LruCache<String,Bitmap> imageCache;
    // 固定2个线程来执行任务
    private ExecutorService executorService = Executors.newFixedThreadPool(3);
    private Handler handler = new Handler();
    private VideoImageLoader() {
        int _maxMemory = (int) Runtime.getRuntime().maxMemory();
        int _cacheSize = _maxMemory/8;
        imageCache = new LruCache<String,Bitmap>(_cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
    }

    public static VideoImageLoader getInstance(){
        VideoImageLoader cache = manager;
        if(cache == null){
            synchronized (VideoImageLoader.class){
                cache = manager;
                if(cache == null){
                    cache = new VideoImageLoader();
                    manager = cache;
                }
            }
        }
        return cache;
    }

    public interface ImageCallBack{
        void imageLoaded(Bitmap bitmap);
    }

    /**
     * 加载数据
     * @param filePath
     * @param imageView
     */
    public void loadImage(final String filePath, final ImageView imageView){
        Bitmap _bitmap = loadDrawable(filePath, new ImageCallBack() {
            @Override
            public void imageLoaded(Bitmap bitmap) {
                if(filePath.equals(imageView.getTag())){
                    if(bitmap != null){
                        imageView.setImageBitmap(bitmap);
                    }else{
                        // TODO: 17/4/21 没有图片资源
                        imageView.setImageResource(0);
                    }
                }
            }
        });
        if(_bitmap != null){
            if(imageView.getTag().equals(filePath)){
                imageView.setImageBitmap(_bitmap);
            }
        }else{
            // TODO: 17/4/21 没有图片资源
            imageView.setImageResource(0);
        }
    }

    public Bitmap loadDrawable(final String filePath, final ImageCallBack callBack){
        if(imageCache.get(filePath) != null){
            return imageCache.get(filePath);
        }
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                final Bitmap _bitmap = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Video.Thumbnails.MINI_KIND);
                //存入map
                imageCache.put(filePath, _bitmap);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callBack.imageLoaded(_bitmap);
                    }
                });
            }
        });
        return null;
    }
}
