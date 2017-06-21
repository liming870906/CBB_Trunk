package com.tingtingfm.cbb.ui.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.TTApplication;
import com.tingtingfm.cbb.bean.LoginInfo;
import com.tingtingfm.cbb.bean.PersonInfo;
import com.tingtingfm.cbb.common.configuration.AccoutConfiguration;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.configuration.PreferencesConfiguration;
import com.tingtingfm.cbb.common.configuration.UrlManager;
import com.tingtingfm.cbb.common.helper.HttpRequestHelper;
import com.tingtingfm.cbb.common.helper.LocationHelper;
import com.tingtingfm.cbb.common.log.TTLog;
import com.tingtingfm.cbb.common.net.BaseRequestCallback;
import com.tingtingfm.cbb.common.net.RequestEntity;
import com.tingtingfm.cbb.common.upload.ScanTaskThread;
import com.tingtingfm.cbb.common.utils.BitmapUtils;
import com.tingtingfm.cbb.common.utils.DeviceUtils;
import com.tingtingfm.cbb.common.utils.DisplayImageOptionsUtils;
import com.tingtingfm.cbb.common.utils.NetUtils;
import com.tingtingfm.cbb.common.utils.StorageUtils;
import com.tingtingfm.cbb.common.utils.TimeUtils;
import com.tingtingfm.cbb.response.MessageResponse;
import com.tingtingfm.cbb.response.PersonInfoResponse;
import com.tingtingfm.cbb.ui.activity.ActivityStack;
import com.tingtingfm.cbb.ui.activity.ApprovalManageActivity;
import com.tingtingfm.cbb.ui.activity.AudioRecordActivity;
import com.tingtingfm.cbb.ui.activity.CbbActivity;
import com.tingtingfm.cbb.ui.activity.ManuscriptActivity;
import com.tingtingfm.cbb.ui.activity.ManuscriptAddActivity;
import com.tingtingfm.cbb.ui.activity.MaterialManageActivity;
import com.tingtingfm.cbb.ui.activity.MessageActivity;
import com.tingtingfm.cbb.ui.activity.SettingActivity;
import com.tingtingfm.cbb.ui.serve.ManuscriptServiceHelper;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by admin on 2017/3/21.
 */

public class MainFragment extends BaseFragment {

    @BindView(R.id.main_headIcon)
    ImageView headIcon;
    @BindView(R.id.main_name)
    TextView nameTextView; //登录用户名
    @BindView(R.id.main_compere) // 部门
            TextView compereTextView;
    @BindView(R.id.main_record)
    LinearLayout recordButton;
    @BindView(R.id.main_photograph)
    LinearLayout photographButton;
    @BindView(R.id.main_shoot)
    LinearLayout shootButton;
    @BindView(R.id.main_writing)
    LinearLayout writingButton;
    @BindView(R.id.main_bottom_material)
    LinearLayout materailButton;
    @BindView(R.id.main_bottom_manuscript)
    LinearLayout manuscriptButton;
    @BindView(R.id.main_bottom_message)
    RelativeLayout messageButton;

    //消息数字view
    @BindView(R.id.main_message_num)
    TextView messageNumTextView;
    private final int MEG_SHOW_NUM = 0x0101;//显示消息数

    @Override
    protected View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_main, null);
    }

    @Override
    protected void handleCreate() {
        setAccountInfo();
        new ScanTaskThread().start();

        LocationHelper.getInstance().getLocationClient().start();
    }

    @Override
    protected void processMessage(Message msg) {
        switch (msg.what) {
            case MEG_SHOW_NUM:
                if (msg.arg1 == 0) {
                    messageNumTextView.setVisibility(View.GONE);
                } else {
                    messageNumTextView.setText((msg.arg1 > 99 ? 99 : msg.arg1) + "");
                    messageNumTextView.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (NetUtils.isNetConnected()) {
            getAccountInfo();
            getMessageNum();
        } else {
//            showToast(R.string.login_not_net);
        }
    }

    @OnClick({R.id.main_record, R.id.main_photograph, R.id.main_shoot, R.id.main_writing
            , R.id.main_bottom_material, R.id.main_bottom_manuscript, R.id.main_bottom_message
            , R.id.main_headIcon, R.id.main_bottom_approval})
    public void buttonClickEvent(View view) {
        switch (view.getId()) {
            case R.id.main_headIcon:
                Intent intent = new Intent(mActivity, SettingActivity.class);
                startActivity(intent);
                break;
            case R.id.main_record: //录音
                //界面跳转
                startActivity(new Intent(mActivity, AudioRecordActivity.class));
//                ToastUtils.showToast(MainActivity.this, R.string.main_record);
                break;
            case R.id.main_photograph://照相
                //获得相片文件存放位置uri
                Uri _photo_file_uri = getPhotoFileUri();
                //判断相片位置uri是否为null
                if (_photo_file_uri != null) {
                    //开启系统相机
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraIntent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, 0);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, _photo_file_uri);

                    startActivityForResult(cameraIntent, Constants.TAKE_PHOTO);
                }
                break;
            case R.id.main_shoot://摄影
                //调用视频意图
                Intent _video_intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                //保存路径
                // TODO: 2017/4/6  (添加保存路径代码后，在华为荣耀，Meta7上摄像，保存后无数据，初步估计是路径问题）
//                _video_intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/Camera/")));
                //分辨率0最低，1最高
//                _video_intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                //开启系统摄像机
                startActivityForResult(_video_intent, Constants.CAMERA_TUBE);
                break;
            case R.id.main_writing://写稿
                startActivity(new Intent(mActivity, ManuscriptAddActivity.class));
                break;
            case R.id.main_bottom_material://素材
                //跳转到素材管理页面
                startActivity(new Intent(mActivity, MaterialManageActivity.class));
                break;
            case R.id.main_bottom_manuscript://稿件
                startActivity(new Intent(mActivity, ManuscriptActivity.class));
                break;
            case R.id.main_bottom_approval://审批
                startActivity(new Intent(mActivity, ApprovalManageActivity.class));
                break;
            case R.id.main_bottom_message://信息
                Intent megIn = new Intent(mActivity, MessageActivity.class);
                startActivity(megIn);
                break;
        }
    }

    /**
     * 获得图片文件URI地址
     *
     * @return
     */
    private Uri getPhotoFileUri() {
        //获得文件
        File _file = getPhotoFile();
        //判断文件是否为null
        if (_file != null) {
            //获得图片URI
            Uri _uri = Uri.fromFile(_file);
            return _uri;
        }
        return null;
    }

    /**
     * 获得文件对象
     *
     * @return
     */
    private File getPhotoFile() {
        //创建图片File对象_此文件为临时文件
        File _file = new File(StorageUtils.getCacheDirectory(mActivity), "temp.jpg");
        try {
            //创建文件
            _file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return _file;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.TAKE_PHOTO) {//拍照
            //获得临时文件
            Bitmap _bitmap = BitmapFactory.decodeFile(StorageUtils.getCacheDirectory(mActivity) + "/temp.jpg");
            int degree = BitmapUtils.readPictureDegree(StorageUtils.getCacheDirectory(mActivity) + "/temp.jpg");
            if ("samsung".equals(DeviceUtils.getBrand())) {
                if (degree == 90) {
                    _bitmap = BitmapUtils.rotateBitmap(_bitmap, 90.0f);
                } else if (degree == 180) {
                    _bitmap = BitmapUtils.rotateBitmap(_bitmap, 180.0f);
                }
            }
            if (_bitmap != null) {
                //将图片保存到系统图库，并返回数据库中指定的路径
                String urlStr = MediaStore.Images.Media.insertImage(mActivity.getContentResolver(), _bitmap, TimeUtils.getYearMonthDayHMS(), "cbb_photo");
                if (!TextUtils.isEmpty(urlStr)) {
                    Uri _uri = Uri.parse(urlStr);
                    //发送更新SD卡通知
                    mActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, _uri));
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //设置帐号信息
    private void setAccountInfo() {
        LoginInfo loginInfo = AccoutConfiguration.getLoginInfo();
        if (!TextUtils.isEmpty(loginInfo.getRealname())) {
            //设置帐号用户名
            nameTextView.setText(loginInfo.getRealname());
        }

        if (!TextUtils.isEmpty(loginInfo.getDepartment()) || !TextUtils.isEmpty(loginInfo.getRole())) {
            //设置部门
            compereTextView.setText(loginInfo.getDepartment() + " - " + loginInfo.getRole());
        }
        //头像设置
        DisplayImageOptionsUtils.getInstance().displaySetImage(loginInfo.getFace_url(), headIcon, true);
    }

    //获取个人信息
    private void getAccountInfo() {
        //请求接口进行登录
        RequestEntity entity = new RequestEntity(UrlManager.GETUSERINFO);
        HttpRequestHelper.post(entity, new BaseRequestCallback<PersonInfoResponse>() {
            @Override
            public void onStart() {
                TTLog.i("lqsir ---onStart");
            }

            @Override
            public void onSuccess(PersonInfoResponse response) {
                if (response.getErrno() == 0 && null != response.getData()) {
                    if (response.getData().getIs_disabled() == 0) {
                        PersonInfo personInfo = response.getData();
                        AccoutConfiguration.updateAccoutInfo(personInfo);
                        //设置显示信息
                        setAccountInfo();
                    } else {
                        showToast(R.string.login_disabled);
                        //清空帐号信息
                        AccoutConfiguration.removeLoginInfo();
                        //保留登录Activity,删除其它Activity
                        ActivityStack.getInstance().popAllActivityExcept(CbbActivity.class);
                        enterLoginActivity();
                    }
                } else {
//                    showToast(R.string.login_accPass_err);
                }
            }

            @Override
            public void onFail(int code, String errorMessage) {
//                showToast(errorMessage);
            }

            @Override
            public void onCancel() {
                TTLog.i("lqsir --- onCancel");
            }
        });
    }

    private void enterLoginActivity() {
        CbbActivity activity = (CbbActivity) mActivity;
        activity.showFragment(CbbActivity.FLAG_LOAGINFRAGMENT);
    }

    /**
     * 获取消息数
     */
    private void getMessageNum() {
        //请求接口进行登录
        RequestEntity entity = new RequestEntity(UrlManager.GET_NOREAD_NUM);
        entity.addParams("admin_id", PreferencesConfiguration.getSValues(Constants.USER_ID));
        HttpRequestHelper.post(entity, new BaseRequestCallback<MessageResponse>() {
            @Override
            public void onStart() {
                TTLog.i("lqsir ---onStart");
            }

            @Override
            public void onSuccess(MessageResponse response) {
                TTLog.i("lqsir --- " + response.getData().toString());
                if (response.getErrno() == 0 && null != response.getData()) {
                    String firstTime = PreferencesConfiguration.getSValues(Constants.FIRST_START_TIME);
                    //登录接口没走。直接进入首页，请求未读消息数接口，保存第一次接口请求时间。
                    if (TextUtils.isEmpty(firstTime)) {
                        PreferencesConfiguration.setSValues(Constants.FIRST_START_TIME, response.getServer_time() + "");
                    }
                    Message message = fragmentHandler.obtainMessage();
                    message.what = MEG_SHOW_NUM;
                    message.arg1 = response.getData().getNum();
                    fragmentHandler.sendMessage(message);
                }
            }

            @Override
            public void onFail(int code, String errorMessage) {
//                showToast(errorMessage);
            }

            @Override
            public void onCancel() {
                TTLog.i("lqsir --- onCancel");
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ManuscriptServiceHelper.getInstance(null).stopService(TTApplication.getAppContext());
    }
}
