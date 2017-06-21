package com.tingtingfm.cbb.ui.activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.view.View;

import com.edmodo.cropper.CropImageView;
import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.common.cache.MediaDataManager;
import com.tingtingfm.cbb.common.configuration.AccoutConfiguration;
import com.tingtingfm.cbb.ui.thread.ShowPhotoAsyncTask;
import com.tingtingfm.cbb.ui.view.CropPanelView;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by think on 2017/1/10.
 */

public class CropPhotoActivity extends BaseActivity {
    @BindView(R.id.crop_layout)
    CropImageView mImageView;

    private int flag = 8;//初始标记，标记图片裁减样式是否已变化

    public final int MIN_CLICK_DELAY_TIME = 1000;
    private long lastClickTime = 0;
    /**
     * 初始化一些View操作
     *
     * @return
     */
    @Override
    protected View initContentView() {
        return getContentView(R.layout.activity_crop_photo);
    }

    /**
     * 逻辑操作，如：请求数据，加载界面...
     */
    @Override
    protected void handleCreate() {
        getLeftView1().setVisibility(View.GONE);
        setCenterViewContent(R.string.material_manage_photo_crop);
        setLeftView3Content(R.string.cancel);

//        mImageView.setImageBitmap(getBitmap(getIntent().getStringExtra("filePath")));

        new ShowPhotoAsyncTask(basicHandler).execute(getIntent().getStringExtra("filePath"));
    }

    /**
     * Handler消息处理
     * msg.what[0-9]:表示裁切图片的9种样式
     *
     * @param msg
     */
    @Override
    protected void processMessage(Message msg) {
        switch (msg.what) {
            case 0:
                if (flag != 0) {
                    flag = 0;
                    mImageView.setAspectRatio(3, 2);
                    mImageView.setFixedAspectRatio(true);
                    mImageView.invalidate();
                }
                break;
            case 1:
                if (flag != 1) {
                    flag = 1;
                    mImageView.setAspectRatio(3, 4);
                    mImageView.setFixedAspectRatio(true);
                    mImageView.invalidate();
                }
                break;
            case 2:
                if (flag != 2) {
                    flag = 2;
                    mImageView.setAspectRatio(4, 3);
                    mImageView.setFixedAspectRatio(true);
                    mImageView.invalidate();
                }
                break;
            case 3:
                if (flag != 3) {
                    flag = 3;
                    mImageView.setAspectRatio(4, 6);
                    mImageView.setFixedAspectRatio(true);
                    mImageView.invalidate();
                }
                break;
            case 4:
                if (flag != 4) {
                    flag = 4;
                    mImageView.setAspectRatio(5, 7);
                    mImageView.setFixedAspectRatio(true);
                    mImageView.invalidate();
                }
                break;
            case 5:
                if (flag != 5) {
                    flag = 5;
                    mImageView.setAspectRatio(8, 10);
                    mImageView.setFixedAspectRatio(true);
                    mImageView.invalidate();
                }
                break;
            case 6:
                if (flag != 6) {
                    flag = 6;
                    mImageView.setAspectRatio(16, 9);
                    mImageView.setFixedAspectRatio(true);
                    mImageView.invalidate();
                }
                break;
            case 7:
                if (flag != 7) {
                    flag = 7;
                    mImageView.setAspectRatio(9, 16);
                    mImageView.setFixedAspectRatio(true);
                    mImageView.invalidate();
                }
                break;
            case 8:
                if (flag != 8) {
                    flag = 8;
                    mImageView.setFixedAspectRatio(false);
                    mImageView.invalidate();
                    mImageView.invalidate();
                }
                break;
            case 9:
                if (flag != 9) {
                    flag = 9;
                    mImageView.setAspectRatio(1, 1);
                    mImageView.setFixedAspectRatio(true);
                    mImageView.invalidate();
                }
                break;
            case ShowPhotoAsyncTask.MESSAGE_PHOTO_LOAD_END:
                dismissDlg();
                Bitmap bitmap = (Bitmap) msg.obj;

                mImageView.setImageBitmap(bitmap);
                break;
            case ShowPhotoAsyncTask.MESSAGE_PHOTO_LOAD_START:
                showLoadDialog();
                break;
        }
    }

    @OnClick(R.id.crop_txt_scale)
    public void onClickScale() {
        CropPanelView cropPanelView = new CropPanelView(this, basicHandler);
        cropPanelView.show();
    }

    @OnClick(R.id.crop_txt_save)
    public void onClickSave() {
        long currentTime  = SystemClock.currentThreadTimeMillis();
        System.out.println("currentTime = " + currentTime + " lastClickTime: " + lastClickTime);
        if (currentTime - lastClickTime < MIN_CLICK_DELAY_TIME) {
            return;
        }

        lastClickTime = currentTime;
        if (flag == 8 && !mImageView.isChange()) {
            showToast(R.string.material_manage_photo_no_crop);
            return;
        }

        Bitmap bitmap = mImageView.getCroppedImage();
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "", "");

        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(getRealPathFromURI(Uri.parse(path))))));

        //添加到本地存储记录中
        MediaDataManager.getInstance().addMediaInfo(
                MediaDataManager
                        .getInstance()
                        .queryImageDataForUri(CropPhotoActivity.this, Uri.parse(path), AccoutConfiguration.getLoginInfo().getUserid()));
        Intent materialIntent = new Intent(CropPhotoActivity.this, MaterialManageActivity.class);
        materialIntent.putExtra("Fragment_Type",1);
        materialIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(materialIntent);
    }

    /**
     * content://media/external/images/media/85316
     * //通过本地路经 content://得到URI路径
     * @param contentUri
     * @return
     */
    public String getRealPathFromURI(Uri contentUri) {
        Cursor cursor = null;
        String locationPath = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            locationPath = cursor.getString(column_index);
        } catch (Exception e) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return locationPath;
    }

    private Bitmap getBitmap(String filePath) {
        Bitmap bm = BitmapFactory.decodeFile(filePath);
        String postfix = filePath.substring(filePath.lastIndexOf(".")).toLowerCase();
        if (postfix.endsWith("jpg")) {
            int digree = 0;
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(filePath);
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
}
