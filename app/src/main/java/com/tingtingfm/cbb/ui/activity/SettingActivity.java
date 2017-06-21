package com.tingtingfm.cbb.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.bean.LoginInfo;
import com.tingtingfm.cbb.bean.MediaInfo;
import com.tingtingfm.cbb.common.cache.MediaDataManager;
import com.tingtingfm.cbb.common.configuration.AccoutConfiguration;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.configuration.PreferencesConfiguration;
import com.tingtingfm.cbb.common.db.DBAudioRecordManager;
import com.tingtingfm.cbb.common.log.TTLog;
import com.tingtingfm.cbb.common.update.UpdateManager;
import com.tingtingfm.cbb.common.utils.AppUtils;
import com.tingtingfm.cbb.common.utils.DisplayImageOptionsUtils;
import com.tingtingfm.cbb.common.utils.StorageUtils;
import com.tingtingfm.cbb.ui.activity.webview.HelpActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by tianhu on 2016/12/23.
 */

public class SettingActivity extends BaseActivity {
    private final int EXIT_OK = 0x1101;
    @BindView(R.id.setting_self_rlayout)
    RelativeLayout selfRlayout;
    @BindView(R.id.setting_bright_rlayout)
    RelativeLayout birghtRlayout;
    @BindView(R.id.setting_reversal_rlayout)
    RelativeLayout reversalRlayout;
    @BindView(R.id.setting_update_rlayout)
    RelativeLayout updateRlayout;
    @BindView(R.id.setting_help_rlayout)
    RelativeLayout helpRlayout;
    @BindView(R.id.setting_exit_button)
    Button exitButton;
    @BindView(R.id.setting_cbb)
    TextView versionTextView;
    //头像
    @BindView(R.id.setting_head_imageView)
    ImageView headImageView;
    //名字
    @BindView(R.id.setting_name)
    TextView accountTextView;
    //部门
    @BindView(R.id.setting_role)
    TextView roleTextView;

    //屏幕倒转开关
    @BindView(R.id.setting_reversal_onOff)
    ImageView reversalOnOff;
    //屏幕长亮开关
    @BindView(R.id.setting_bright_onOff)
    ImageView brightOnOff;

    @BindView(R.id.restore_audio_info)
    Button mRestoreAudioData;

    @Override
    protected View initContentView() {
        return getContentView(R.layout.setting_activity);
    }

    @Override
    protected void handleCreate() {
        setCenterViewContent(R.string.setting_text);
        if (true||TTLog.getAbleLogging() || PreferencesConfiguration.getBValues("restoreAudio")) {
            mRestoreAudioData.setVisibility(View.GONE);
        }
        initSetData();
    }

    //初始化"录音时屏幕倒转"默认开启
    private void initSetData() {
        if(TextUtils.isEmpty(PreferencesConfiguration.getSValues(Constants.SETTINGS_SCREEN_INVERSION))){
            //应用是否运行过。运行过就不在进行初始化数据
            PreferencesConfiguration.setSValues(Constants.SETTINGS_SCREEN_INVERSION,"inversion_true");
            //初始化设置-录音时屏幕倒转标记
            PreferencesConfiguration.setBValues(Constants.SETTING_REVERSAL,true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LoginInfo loginInfo = AccoutConfiguration.getLoginInfo();
        DisplayImageOptionsUtils.getInstance().displaySetImage(loginInfo.getFace_url(), headImageView, true);
        if (!TextUtils.isEmpty(loginInfo.getRealname())) {
            //设置帐号用户名
            accountTextView.setText(loginInfo.getRealname());
        }
        if (!TextUtils.isEmpty(loginInfo.getDepartment()) || !TextUtils.isEmpty(loginInfo.getRole())) {
            //设置部门
            roleTextView.setText(loginInfo.getDepartment() + " - " + loginInfo.getRole());
        }
        setOnOff();
        //设置版本号
        versionTextView.setText(getString(R.string.person_version, AppUtils.getVersionName()));
    }

    //设置默认onOff状态
    private void setOnOff() {
        boolean reversalWIfi = PreferencesConfiguration.getBValues(Constants.SETTING_REVERSAL);
        setReversalOnOff(reversalWIfi);
        boolean brightWIfi = PreferencesConfiguration.getBValues(Constants.SETTING_BRIGHT);
        setBirhgtOnOff(brightWIfi);
    }

    @Override
    protected void processMessage(Message msg) {
        switch (msg.what) {
            case EXIT_OK://帐号退出
                MediaDataManager.getInstance().clearData();
                //清空帐号信息
                AccoutConfiguration.removeLoginInfo();
                //保留登录Activity,删除其它Activity
                ActivityStack.getInstance().popAllActivityExcept(SettingActivity.class);
                //打开登录界面
                Intent intent = new Intent(this, CbbActivity.class);
                startActivity(intent);
                SettingActivity.this.finish();
                break;
        }
    }

    @OnClick({R.id.setting_self_rlayout, R.id.setting_bright_onOff, 
            R.id.setting_reversal_onOff, R.id.setting_update_rlayout, R.id.setting_help_rlayout,
            R.id.setting_exit_button, R.id.setting_make_rlayout})
    public void clickRlayout(View view) {
        switch (view.getId()) {
            //个人信息
            case R.id.setting_self_rlayout:
                Intent personIn = new Intent(SettingActivity.this, PersonInfoActivity.class);
                startActivity(personIn);
                break;

            //录音时屏幕倒转
            case R.id.setting_reversal_onOff:
                boolean reversalWIfi = PreferencesConfiguration.getBValues(Constants.SETTING_REVERSAL);
                PreferencesConfiguration.setBValues(Constants.SETTING_REVERSAL, !reversalWIfi);
                setReversalOnOff(!reversalWIfi);
                break;
            //录音时屏幕长亮
            case R.id.setting_bright_onOff:
                boolean brightWIfi = PreferencesConfiguration.getBValues(Constants.SETTING_BRIGHT);
                PreferencesConfiguration.setBValues(Constants.SETTING_BRIGHT, !brightWIfi);
                setBirhgtOnOff(!brightWIfi);
                break;
            //检查更新
            case R.id.setting_update_rlayout:
                UpdateManager.getInstance().checkUpdate(true);
                break;
            //帮助及反馈
            case R.id.setting_help_rlayout:
                Intent helpIn = new Intent(SettingActivity.this, HelpActivity.class);
                startActivity(helpIn);
                break;
            //配置制作网帐号
            case R.id.setting_make_rlayout:
                Intent makeIn = new Intent(SettingActivity.this, MakeActivity.class);
                startActivity(makeIn);
                break;
            //退出帐号
            case R.id.setting_exit_button:
                showTwoButtonDialog(true, true, "", getString(R.string.person_is_exit),
                        null, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                basicHandler.sendEmptyMessage(EXIT_OK);
                            }
                        });
                break;
        }
    }

    //设置屏幕长亮
    private void setBirhgtOnOff(boolean brightWIfi) {
        if (brightWIfi) {
            brightOnOff.setImageResource(R.drawable.setting_open);
        } else {
            brightOnOff.setImageResource(R.drawable.setting_close);
        }
    }

    //设置屏幕倒转
    private void setReversalOnOff(boolean reversalWIfi) {
        if (reversalWIfi) {
            reversalOnOff.setImageResource(R.drawable.setting_open);
        } else {
            reversalOnOff.setImageResource(R.drawable.setting_close);
        }
    }

    /**
     * 该功能属于隐藏功能，只为了提供给测试恢复录音数据使用
     */
    @OnClick(R.id.restore_audio_info)
    void restoreAudioDataFunction() {
        new AsyncTask<Void, Integer, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                File audioFiles = StorageUtils.getSDCardStorageDirectory(SettingActivity.this);
                //获得文件名称对象
                List<File> fileLists =  new ArrayList<File>();
                File[] files = audioFiles.listFiles();
                for (File file :files){
                    if(!file.isDirectory()){
                        fileLists.add(file);
                    }
                }
//                List<File> fileLists =  Arrays.asList(files);
                Collections.sort(fileLists, new Comparator<File>() {
                    @Override
                    public int compare(File lhs, File rhs) {
                        long time1 = lhs.lastModified();
                        long time2 = rhs.lastModified();
                        if (time2 < time1) {
                            return -1;
                        } else if (time2 == time1) {
                            return 0;
                        } else {
                            return 1;
                        }
                    }
                });

                MediaPlayer player;
                for (File file : fileLists) {
                    System.out.println("file = " + file.toString() + " modified: " + file.lastModified());
                    //判断文件是否存在
                    if (file.exists()) {
                        player = MediaPlayer.create(SettingActivity.this, Uri.fromFile(file));
                        //声明对媒体对象
                        MediaInfo _info = new MediaInfo();
                        //添加默认服务器ID
                        _info.setMedia_id(-1);
                        //用户ID
                        _info.setUser_id(AccoutConfiguration.getLoginInfo().getUserid());
                        //文件大小
                        _info.setSize(file.length());
                        //文件名称（带扩展名）
                        _info.setFullName(file.getName());
                        //文件名称
                        _info.setTitle(getFileName(file));
                        //文本类型
                        _info.setMime_type(Constants.MIME_TYPE_AUDIO_MP3);
                        //数据地址
                        _info.setAbsolutePath(file.getAbsolutePath());
                        //添加文本的时间
                        _info.setDate_added(file.lastModified() / 1000);
                        //更新文本的时间
                        _info.setDate_modified(file.lastModified());
                        //更新文本上传状态
                        _info.setUpload_status(Constants.UPLOAD_STATUS_DEFAULT);
                        //添加音频文件时间
                        _info.setDuration(player.getDuration());
                        //设置用户ID
                        _info.setUser_id(AccoutConfiguration.getLoginInfo().getUserid());
                        //添加音频数据到数据库中
                        DBAudioRecordManager.getInstance(SettingActivity.this).addAudioRecord(_info);
                    }
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showLoadDialog();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                PreferencesConfiguration.setBValues("restoreAudio", true);
                mRestoreAudioData.setVisibility(View.GONE);
                dismissDlg();
            }
        }.execute();
    }
    private String getFileName(File file){
        String _fileName = file.getName();
        return _fileName.substring(0,_fileName.lastIndexOf("."));
    }
}
