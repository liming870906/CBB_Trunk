/**
 * 
 */
package com.tingtingfm.cbb.common.utils;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.FileNotFoundException;

/**
 * @author liqiang
 * @data 2014年9月27日
 *
 */
public class ImageUtils {

	/**
	 * 把 Uri 转成 bitmap
	 * @param activity
	 * @param uri
	 * @return
	 */
	public static Bitmap changeUri4Bitmap(Activity activity, Uri uri) {
		Bitmap photo = null;
		// 解决三星手机或其它手机截图时超过160以上的为大图情况 通过uri获取
		int dw = activity.getWindowManager().getDefaultDisplay().getWidth();
		int dh = activity.getWindowManager().getDefaultDisplay().getHeight() / 2;

		BitmapFactory.Options factory = new BitmapFactory.Options();
		factory.inJustDecodeBounds = true; // 当为true时 允许查询图片不为
											// 图片像素分配内存
		try {
			photo = BitmapFactory.decodeStream(activity.getContentResolver()
					.openInputStream(uri), null, factory);
			int hRatio = (int) Math.ceil(factory.outHeight / (float) dh); // 图片是高度的几倍
			int wRatio = (int) Math.ceil(factory.outWidth / (float) dw); // 图片是宽度的几倍
			// 缩小到 1/ratio的尺寸和 1/ratio^2的像素
			if (hRatio > 1 || wRatio > 1) {
				if (hRatio > wRatio) {
					factory.inSampleSize = hRatio;
				} else
					factory.inSampleSize = wRatio;
			}
			factory.inJustDecodeBounds = false;
			photo = BitmapFactory.decodeStream(activity.getContentResolver()
					.openInputStream(uri), null, factory);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return photo;
	}

}
