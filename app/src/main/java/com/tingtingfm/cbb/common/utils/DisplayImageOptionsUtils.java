package com.tingtingfm.cbb.common.utils;

import android.text.TextUtils;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.TTApplication;

public class DisplayImageOptionsUtils {
    private static volatile DisplayImageOptionsUtils mOptions = null;
    private final DisplayImageOptions setOptionsRounded;
    private  DisplayImageOptions options;

    private DisplayImageOptionsUtils() {

        options = new DisplayImageOptions.Builder()
//                .showImageOnLoading(TTApplication.getAppContext().getResources().getDrawable(R.drawable.main_person_head))
//                .showImageForEmptyUri(TTApplication.getAppContext().getResources().getDrawable(R.drawable.main_person_head))
//                .showImageOnFail(TTApplication.getAppContext().getResources().getDrawable(R.drawable.main_person_head))
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .considerExifParams(true)
                .displayer(new RoundedBitmapDisplayer(0))
                .build();

        //设置头像option
        setOptionsRounded = new DisplayImageOptions.Builder()
//                .showImageOnLoading(TTApplication.getAppContext().getResources().getDrawable(R.drawable.main_person_head))
                .showImageForEmptyUri(TTApplication.getAppContext().getResources().getDrawable(R.drawable.main_person_head))
                .showImageOnFail(TTApplication.getAppContext().getResources().getDrawable(R.drawable.main_person_head))
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .considerExifParams(true)
                .displayer(new RoundedBitmapDisplayer(1000))
                .build();
    }

    public static DisplayImageOptionsUtils getInstance() {
        if (mOptions == null) {
            synchronized (DisplayImageOptionsUtils.class) {
                if (mOptions == null) {
                    mOptions = new DisplayImageOptionsUtils();
                }
            }
        }

        return mOptions;
    }


    /**
     * 发送页面图标显示
     * @param imageUrl 给定地址
     * @param view   给定View
     * @param isRounded true, false show normal bitmap
     */
    public void displayImage(String imageUrl, final ImageView view, boolean isRounded) {
        view.setImageResource(R.drawable.main_person_head);
        if(!TextUtils.isEmpty(imageUrl)){
            if(isRounded){
                ImageLoader.getInstance().displayImage(imageUrl,view, setOptionsRounded);
            }else{
                ImageLoader.getInstance().displayImage(imageUrl, view, options);
            }
        }
    }

    /**
     * 设置个人信息页图。options不一样。
     * @param imageUrl 给定地址
     * @param view   给定View
     * @param isRounded true, false show normal bitmap
     */
    public void displaySetImage(String imageUrl, final ImageView view, boolean isRounded) {
        if (!TextUtils.isEmpty(imageUrl)) {
            if (isRounded) {
                ImageLoader.getInstance().displayImage(imageUrl, view, setOptionsRounded);
            } else {
                ImageLoader.getInstance().displayImage(imageUrl, view, options);
            }
        } else {
            view.setImageResource(R.drawable.main_person_head);
        }
    }
}