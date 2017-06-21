package com.tingtingfm.cbb.ui.activity;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.TTApplication;
import com.tingtingfm.cbb.bean.MediaInfo;
import com.tingtingfm.cbb.common.cache.MediaDataManager;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.configuration.GlobalVariableManager;
import com.tingtingfm.cbb.common.configuration.UrlManager;
import com.tingtingfm.cbb.common.db.DBMaterialImageManager;
import com.tingtingfm.cbb.common.helper.HttpRequestHelper;
import com.tingtingfm.cbb.common.net.BaseRequestCallback;
import com.tingtingfm.cbb.common.net.RequestEntity;
import com.tingtingfm.cbb.common.upload.DBOperationUtils;
import com.tingtingfm.cbb.common.upload.UploadListener;
import com.tingtingfm.cbb.common.upload.UploadManager;
import com.tingtingfm.cbb.common.utils.NetUtils;
import com.tingtingfm.cbb.response.CheckMaterialResponse;
import com.tingtingfm.cbb.ui.thread.ShowPhotoAsyncTask;
import com.tingtingfm.cbb.ui.view.PreviewPhotoView;
import com.tingtingfm.cbb.ui.view.UploadLoadView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by lqsir on 2017/1/3.
 */

public class PreviewPhotoActivity extends BaseActivity implements UploadListener {
    @BindView(R.id.preview_imageview)
    PreviewPhotoView imageView;

    @BindView(R.id.preview_img_upload)
    UploadLoadView imgUpload;
    @BindView(R.id.preview_txt_upload)
    TextView txtUpload;
    @BindView(R.id.preview_layout_upload)
    LinearLayout layoutUpload;
    @BindView(R.id.preview_txt_crop)
    TextView txtCrop;
    @BindView(R.id.preview_txt_send)
    TextView txtSend;
    @BindView(R.id.preview_txt_delete)
    TextView txtDelete;
    @BindView(R.id.preview_load)
    ProgressBar mProgressBar;

    MediaInfo mMediaInfo;
    String imagePath;
    Bitmap mBitmap;

    /**
     * 初始化一些View操作
     *
     * @return
     */
    @Override
    protected View initContentView() {
        return getContentView(R.layout.activity_preview_photo);
    }

    /**
     * 逻辑操作，如：请求数据，加载界面...
     */
    @Override
    protected void handleCreate() {
        mMediaInfo = getIntent().getParcelableExtra("mediaInfo");
        imagePath = mMediaInfo.getAbsolutePath();
        setCenterViewContent(mMediaInfo.getFullName());
        new ShowPhotoAsyncTask(basicHandler).execute(imagePath);
    }

    /**
     * 设置图片的上传状态
     */
    private void updateUploadStatus(final int uploadStatus) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imgUpload.setUploadStatus(uploadStatus);
                switch (uploadStatus) {
                    case Constants.UPLOAD_STATUS_DEFAULT:
                        txtUpload.setText(R.string.audio_preview_upload);
                        break;
                    case Constants.UPLOAD_STATUS_SUCCESS://成功
                        txtUpload.setText(R.string.audio_preview_upload_success);
                        break;
                    case Constants.UPLOAD_STATUS_FAILURE://失败
                        txtUpload.setText(R.string.audio_preview_upload_failure);
                        break;
                    case Constants.UPLOAD_STATUS_LOADING:
                        txtUpload.setText(R.string.audio_preview_uploading);
                        break;
                }
            }
        });
    }

    /**
     * Handler消息处理
     *
     * @param msg
     */
    @Override
    protected void processMessage(Message msg) {
        switch (msg.what) {
            case ShowPhotoAsyncTask.MESSAGE_PHOTO_LOAD_END:
                mBitmap = (Bitmap) msg.obj;
                mProgressBar.setVisibility(View.GONE);
                imageView.setImageBitmap(mBitmap);
                break;
            case ShowPhotoAsyncTask.MESSAGE_PHOTO_LOAD_START:
                mProgressBar.setVisibility(View.VISIBLE);
                break;
        }
    }

    @OnClick(R.id.preview_layout_upload)
    public void clickUpload() {
        int status = mMediaInfo.getUpload_status();
        if (status == Constants.UPLOAD_STATUS_DEFAULT
                || status == Constants.UPLOAD_STATUS_FAILURE) {
            uploadMaterial(Math.abs(mMediaInfo.getSize()));
        }
    }

    private void uploadMaterial(long fileSize) {
        int maxFileSize = GlobalVariableManager.MAXWIFIFILESIZE;
        int netStatus = NetUtils.getNetConnectType();
        if (GlobalVariableManager.isOpen100 && netStatus == 2) {
            maxFileSize = GlobalVariableManager.MAX4GFILESIZE;
        }

        if (fileSize > maxFileSize) {
            showOneButtonDialog(true, "",
                    maxFileSize == GlobalVariableManager.MAX4GFILESIZE
                            ? getString(R.string.material_upload_size100_message)
                            : getString(R.string.audio_preview_upload_dialog_text), null);
        } else {
            //添加到下载队列
            UploadManager.getInstance().addUpload(mMediaInfo);
            MediaDataManager.getInstance().updateMediaInfo(mMediaInfo);
        }
    }

    @OnClick(R.id.preview_txt_crop)
    public void clickCrop() {
        Intent intent = new Intent();
        intent.setClass(this, CropPhotoActivity.class);
        intent.putExtra("filePath", imagePath);
        startActivity(intent);
    }

    @OnClick(R.id.preview_txt_send)
    public void clickSend() {
        //未上传提示
        if (mMediaInfo.getUpload_status() != 2) {
            showToast(R.string.material_manage_send_no_upload_tips);
            return;
        }

        //TODO 已上传后，判断服务器是否已经删除该资源
        checkMaterialRequest(Constants.AUDIO_PREVIEW_SEND_TYPE);
    }

    /**
     * 发送检查请求
     */
    private void checkMaterialRequest(final int pType) {
        //请求接口
        RequestEntity entity = new RequestEntity(UrlManager.CHECK_MATERIALS);
        entity.addParams("id_list", String.valueOf(mMediaInfo.getMedia_id()));
        HttpRequestHelper.post(entity, new BaseRequestCallback<CheckMaterialResponse>() {
            @Override
            public void onStart() {
                showLoadDialog();
            }

            @Override
            public void onSuccess(CheckMaterialResponse response) {
                ArrayList<Integer> _ids = response.getData();
                if (_ids.size() == 0) {
                    // TODO: 17/1/18 跳转发送页面
                    startActivity(new Intent(PreviewPhotoActivity.this, SendActivity.class)
                            .putExtra("id_list", String.valueOf(mMediaInfo.getMedia_id())));
                } else {
                    showOneButtonDialog(true, "", PreviewPhotoActivity.this.getString(R.string.audio_preview_make_web_delete_tips, mMediaInfo.getFullName()), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //设置上传状态
                            mMediaInfo.setUpload_status(Constants.UPLOAD_STATUS_DEFAULT);
                            mMediaInfo.setSliceId(0);
                            mMediaInfo.setSliceCount(0);
                            mMediaInfo.setSuccessIds("");
                            mMediaInfo.setMedia_id(0);
                            //更新数据
                            MediaDataManager.getInstance().updateMediaInfo(mMediaInfo);
                            //更新数据库状态
                            DBOperationUtils.updateMaterialDb(mMediaInfo);
                            //修改底部导航上传状态
                            updateUploadStatus(Constants.UPLOAD_STATUS_DEFAULT);
                        }
                    });
                }
            }

            @Override
            public void onFail(int code, String errorMessage) {
                showToast(errorMessage);
            }

            @Override
            public void onCancel() {
                dismissDlg();
            }
        });
    }

    @OnClick(R.id.preview_txt_delete)
    public void clickDelete() {
        showTwoButtonDialog(true, true, null, getString(R.string.material_manage_delete_photo),
                null, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface anInterface, int i) {
                        if (!TextUtils.isEmpty(imagePath)) {
                            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                            ContentResolver mContentResolver = PreviewPhotoActivity.this.getContentResolver();
                            String where = MediaStore.Images.Media.DATA + "='" + imagePath + "'";
                            //删除图片
                            mContentResolver.delete(uri, where, null);
                        }

                        //发送广播
                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        File file = new File(imagePath);
                        Uri uri = Uri.fromFile(file);
                        intent.setData(uri);
                        PreviewPhotoActivity.this.sendBroadcast(intent);

                        //删除本地关联记录
                        DBMaterialImageManager.getInstance(TTApplication.getAppContext())
                                .deleteMaterialImage(mMediaInfo.getId());

                        //删除本地存储记录
                        MediaDataManager.getInstance().deleteMediaInfo(mMediaInfo);

                        Intent materialIntent = new Intent(PreviewPhotoActivity.this, MaterialManageActivity.class);
                        materialIntent.putExtra("Fragment_Type", 1);
                        materialIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(materialIntent);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        UploadManager.getInstance().addUploadListener(this);
        updateUploadStatus(mMediaInfo.getUpload_status());
    }

    @Override
    protected void onPause() {
        super.onPause();
        UploadManager.getInstance().removeUploadListener(this);
    }

    @Override
    public void start(List<Integer> ids) {
        System.out.println("PreviewPhotoActivity.start " + ids.toString());
        if (ids.contains(mMediaInfo.getId())) {
            mMediaInfo.setUpload_status(Constants.UPLOAD_STATUS_LOADING);
            updateUploadStatus(Constants.UPLOAD_STATUS_LOADING);
        }
    }

    @Override
    public void fail(int id) {
        System.out.println("PreviewPhotoActivity.fail " + id);
        if (id == mMediaInfo.getId()) {
            mMediaInfo.setUpload_status(Constants.UPLOAD_STATUS_FAILURE);
            updateUploadStatus(Constants.UPLOAD_STATUS_FAILURE);
            //更新上传状态
            MediaDataManager.getInstance().updateMediaInfo(mMediaInfo);
        }
    }

    @Override
    public void success(int id, int mediaId) {
        System.out.println("PreviewPhotoActivity.success " + id);
        if (id == mMediaInfo.getId()) {
            mMediaInfo.setUpload_status(Constants.UPLOAD_STATUS_SUCCESS);
            mMediaInfo.setMedia_id(mediaId);
            updateUploadStatus(Constants.UPLOAD_STATUS_SUCCESS);
            //更新上传状态
            MediaDataManager.getInstance().updateMediaInfo(mMediaInfo);
        }
    }
}
