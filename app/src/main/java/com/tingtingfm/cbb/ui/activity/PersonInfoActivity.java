package com.tingtingfm.cbb.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kevin.crop.UCrop;
import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.bean.PersonInfo;
import com.tingtingfm.cbb.common.configuration.AccoutConfiguration;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.configuration.PreferencesConfiguration;
import com.tingtingfm.cbb.common.configuration.UrlManager;
import com.tingtingfm.cbb.common.helper.HttpRequestHelper;
import com.tingtingfm.cbb.common.log.TTLog;
import com.tingtingfm.cbb.common.net.BaseRequestCallback;
import com.tingtingfm.cbb.common.net.RequestEntity;
import com.tingtingfm.cbb.common.utils.DisplayImageOptionsUtils;
import com.tingtingfm.cbb.common.utils.NetUtils;
import com.tingtingfm.cbb.common.utils.UploadUtils;
import com.tingtingfm.cbb.response.EditUserResponse;
import com.tingtingfm.cbb.response.PersonInfoResponse;
import com.tingtingfm.cbb.ui.activity.cream.CropActivity;
import com.tingtingfm.cbb.ui.view.SelectPicPopupWindow;

import java.io.File;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by tianhu on 2016/12/26.
 */

public class PersonInfoActivity extends BaseActivity implements View.OnClickListener {
    @BindView(R.id.person_head_imgView)
    ImageView personHead;
    @BindView(R.id.person_name)
    TextView nameTextView;
    @BindView(R.id.person_id)
    TextView idTextView;
    @BindView(R.id.person_phone)
    TextView phoneTextView;
    @BindView(R.id.person_mail)
    TextView mailTextView;
    @BindView(R.id.person_department)
    TextView departmentTextView;
    @BindView(R.id.person_role)
    TextView roleTextView;

    @BindView(R.id.person_root_llayout)
    LinearLayout rootLayout;

    public static final int NONE = 0;
    public static final int PHOTOHRAPH = 1;// 拍照
    public static final int PHOTOZOOM = 2; // 缩放
    public static final int PHOTORESOULT = 3;// 结果
    private final int CROP_MAX_WIDTH = 200;
    private final int CROP_MAX_HEIGHT = 200;
    public static final String IMAGE_UNSPECIFIED = "image/*";

    private final String face_Image = "/face_Image";

    private String face_filePath;
    //显示信息
    private final int SHOW_INTO = 0x2001;
    //帐号信息
    private PersonInfo personInfo;
    private SelectPicPopupWindow menuWindow;

    private static final int GALLERY_REQUEST_CODE = 0;    // 相册选图标记
    private static final int CAMERA_REQUEST_CODE = 1;    // 相机拍照标记
    protected static final int REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101;
    protected static final int REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 102;
    private AlertDialog mAlertDialog;
    // 拍照临时图片
    private String mTempPhotoPath;
    // 剪切后图像文件
    private Uri mDestinationUri;
    private String id;
    private String url;
    private String localType;

    @Override
    protected View initContentView() {
        return getContentView(R.layout.person_info_activity);
    }

    @Override
    protected void handleCreate() {
        setCenterViewContent(R.string.person_info);
        if (NetUtils.isNetConnected()) {
            getAccountInfo();
        } else {
            showToast(R.string.login_not_net);
            rootLayout.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mDestinationUri = Uri.fromFile(new File(getCacheDir(), "cropImage.jpeg"));
        mTempPhotoPath = Environment.getExternalStorageDirectory() + File.separator + "photo.jpeg";
    }

    //获取个人信息
    private void getAccountInfo() {
        //请求接口进行登录
        RequestEntity entity = new RequestEntity(UrlManager.GETUSERINFO);
        HttpRequestHelper.post(entity, new BaseRequestCallback<PersonInfoResponse>() {
            @Override
            public void onStart() {
                showLoadDialog();
                TTLog.i("lqsir ---onStart");
            }

            @Override
            public void onSuccess(PersonInfoResponse response) {
                if (response.getErrno() == 0 && null != response.getData()) {
                    if(response.getData().getIs_disabled() == 0){
                        personInfo = response.getData();
                        AccoutConfiguration.updateAccoutInfo(personInfo);
                        basicHandler.sendEmptyMessage(SHOW_INTO);
                    }else{
                        showToast(R.string.login_disabled);
                        //清空帐号信息
                        AccoutConfiguration.removeLoginInfo();
                        //保留登录Activity,删除其它Activity
                        ActivityStack.getInstance().popAllActivityExcept(PersonInfoActivity.class);
                        //打开登录界面
                        Intent intent = new Intent(PersonInfoActivity.this, CbbActivity.class);
                        startActivity(intent);
                        PersonInfoActivity.this.finish();
                    }
                } else {
                    showToast(R.string.login_accPass_err);
                }
            }

            @Override
            public void onFail(int code, String errorMessage) {
                showToast(errorMessage);
            }

            @Override
            public void onCancel() {
                dismissDlg();
                TTLog.i("lqsir --- onCancel");
            }
        });
    }

    @Override
    protected void processMessage(Message msg) {
        switch (msg.what) {
            case Constants.UPLOAD_SUCCESS:
                Bundle bundle = msg.getData();
                if (null != bundle) {
                    id = bundle.getString("id");
                    url = bundle.getString("url");
                    localType = bundle.getString("locaType");
                    saveInfo(url);//与帐号绑定信息
                }
                break;
            case Constants.UPLOAD_FAIL:
                dismissDlg();
                showToast(R.string.person_upload_fail);
                break;
            case SHOW_INTO:
                if (null != personInfo) {
                    //设置显示信息
                    setAccountInfo(personInfo);
                }
                break;
        }
    }

    //编辑个人信息，将上头像与帐号进行绑定
    private void saveInfo(final String urlStr) {
        //请求接口进行绑定
        if (!TextUtils.isEmpty(urlStr)) {
            RequestEntity entity = new RequestEntity(UrlManager.EDIT_USER_INFO);
            entity.addParams("face_url", urlStr);
            HttpRequestHelper.post(entity, new BaseRequestCallback<EditUserResponse>() {

                @Override
                public void onStart() {
                    TTLog.i("lqsir ---onStart");
                }

                @Override
                public void onSuccess(EditUserResponse response) {
                    TTLog.i("lqsir --- " + response.getData().toString());
                    if (null != response.getData() && response.getData().getSucc() == 1) {
                        PreferencesConfiguration.setSValues(Constants.FACE_URL, urlStr);
                        setHead(urlStr);
                    } else {
                        showToast(R.string.login_accPass_err);
                    }
                }

                @Override
                public void onFail(int code, String errorMessage) {
                    showToast(errorMessage);
                }

                @Override
                public void onCancel() {
                    dismissDlg();
                    TTLog.i("lqsir --- onCancel");
                }

            });
        } else {
            showToast(R.string.person_upload_fail);
        }
    }

    //设置帐号信息
    private void setAccountInfo(PersonInfo personInfo) {
        //设置头像
        setHead(personInfo.getFace_url());
        //名字
        if (!TextUtils.isEmpty(personInfo.getRealname()))
            nameTextView.setText(personInfo.getRealname());
        //id
        if (!TextUtils.isEmpty(String.valueOf(personInfo.getUserid())))
            idTextView.setText(String.valueOf(personInfo.getUserid()));
        //手机
        if (!TextUtils.isEmpty(personInfo.getMobile()))
            phoneTextView.setText(personInfo.getMobile());
        //邮箱
        if (!TextUtils.isEmpty(personInfo.getEmail()))
            mailTextView.setText(personInfo.getEmail());
        //部门
        if (!TextUtils.isEmpty(personInfo.getDepartment()))
            departmentTextView.setText(personInfo.getDepartment());
        //职务
        if (!TextUtils.isEmpty(personInfo.getRole()))
            roleTextView.setText(personInfo.getRole());
    }

    //设置头像
    private void setHead(String urlStr) {
        DisplayImageOptionsUtils.getInstance().displaySetImage(urlStr, personHead, true);
    }

    @OnClick(R.id.person_head_imgView)
    public void sethead() {
        //显示窗口
        menuWindow = new SelectPicPopupWindow(PersonInfoActivity.this, this);
        menuWindow.showAtLocation(rootLayout, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
    }

    @Override
    public void onClick(View v) {
        menuWindow.dismiss();
        switch (v.getId()) {
            case R.id.btn_take_photo:
                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN // Permission was added in API Level 16
                        && ActivityCompat.checkSelfPermission(PersonInfoActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            getString(R.string.permission_write_storage_rationale),
                            REQUEST_STORAGE_WRITE_ACCESS_PERMISSION);
                } else {*/
                    Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    //下面这句指定调用相机拍照后的照片存储的路径
                    takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mTempPhotoPath)));
                    startActivityForResult(takeIntent, CAMERA_REQUEST_CODE);
//                }
                break;
            case R.id.btn_pick_photo:
                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN // Permission was added in API Level 16
                        && ActivityCompat.checkSelfPermission(PersonInfoActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                            getString(R.string.permission_read_storage_rationale),
                            REQUEST_STORAGE_READ_ACCESS_PERMISSION);
                } else {*/
                    Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    // 如果限制上传到服务器的图片类型时可以直接写如："image/jpeg 、 image/png等的类型"
                    pickIntent.setType("image/*");
                    startActivityForResult(pickIntent, GALLERY_REQUEST_CODE);
//                }
                break;
            default:
                break;
        }
    }

    /**
     * 请求权限
     * <p>
     * 如果权限被拒绝过，则提示用户需要权限
     */
    /*@TargetApi(Build.VERSION_CODES.M)
    protected void requestPermission(final String permission, String rationale, final int requestCode) {
        if (shouldShowRequestPermissionRationale(permission)) {
            showTwoButtonDialog(getString(R.string.permission_title_rationale),rationale,null,new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    requestPermissions(new String[]{permission}, requestCode);
                }
            });
        } else {
            requestPermissions(new String[]{permission}, requestCode);
        }
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAMERA_REQUEST_CODE:   // 调用相机拍照
                    File temp = new File(mTempPhotoPath);
                    startCropActivity(Uri.fromFile(temp));
                    break;
                case GALLERY_REQUEST_CODE:  // 直接从相册获取
                    startCropActivity(data.getData());
                    break;
                case UCrop.REQUEST_CROP:    // 裁剪图片结果
                    handleCropResult(data);
                    break;
                case UCrop.RESULT_ERROR:    // 裁剪图片错误
                    handleCropError(data);
                    break;
            }
        }

    }

    /**
     * 裁剪图片方法实现
     *
     * @param uri
     */
    public void startCropActivity(Uri uri) {
        UCrop.of(uri, mDestinationUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(512, 512)
                .withTargetActivity(CropActivity.class)
                .start(PersonInfoActivity.this);
    }

    /**
     * 处理剪切成功的返回值
     *
     * @param result
     */
    private void handleCropResult(Intent result) {
        if (!NetUtils.isNetConnected()) {
            showToast(R.string.login_not_net);
            return;
        }

        deleteTempPhotoFile();
        final Uri resultUri = UCrop.getOutput(result);//图片路径
        if (null != resultUri) {
            showLoadDialog();
            //上传头像
            UploadUtils upload = new UploadUtils(resultUri.getPath().toString(), "pic", "1","user","0", UrlManager.UPLOAD, basicHandler);
            upload.start();
        } else {
            showToast(R.string.permission_not_cut);
        }
    }

    /**
     * 处理剪切失败的返回值
     *
     * @param result
     */
    private void handleCropError(Intent result) {
        deleteTempPhotoFile();
        final Throwable cropError = UCrop.getError(result);
        if (cropError != null) {
            showToast(cropError.getMessage());
        } else {
            showToast(R.string.permission_not_cut);
        }
    }

    /**
     * 删除拍照临时文件
     */
    private void deleteTempPhotoFile() {
        File tempFile = new File(mTempPhotoPath);
        if (tempFile.exists() && tempFile.isFile()) {
            tempFile.delete();
        }
    }

}
