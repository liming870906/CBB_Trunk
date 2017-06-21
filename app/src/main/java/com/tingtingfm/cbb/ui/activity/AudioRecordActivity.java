package com.tingtingfm.cbb.ui.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.czt.mp3recorder.MP3Recorder;
import com.shuyu.waveview.AudioWaveView;
import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.bean.MediaInfo;
import com.tingtingfm.cbb.common.configuration.AccoutConfiguration;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.configuration.GlobalVariableManager;
import com.tingtingfm.cbb.common.configuration.PreferencesConfiguration;
import com.tingtingfm.cbb.common.db.DBAudioRecordManager;
import com.tingtingfm.cbb.common.dialog.TTAlertDialog;
import com.tingtingfm.cbb.common.helper.LocationHelper;
import com.tingtingfm.cbb.common.upload.UploadManager;
import com.tingtingfm.cbb.common.utils.NetUtils;
import com.tingtingfm.cbb.common.utils.ScreenUtils;
import com.tingtingfm.cbb.common.utils.StorageUtils;
import com.tingtingfm.cbb.common.utils.TimeUtils;
import com.tingtingfm.cbb.ui.receiver.HeadsetReceiver;
import com.tingtingfm.cbb.ui.receiver.PhoneStateReceiver;
import com.tingtingfm.cbb.ui.receiver.SimStateReceiver;
import com.tingtingfm.cbb.ui.view.SlidingButton;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.OnClick;

import static com.tingtingfm.cbb.common.configuration.PreferencesConfiguration.getSValues;

/**
 * 录音页面
 *
 * @author liming
 */
public class AudioRecordActivity extends AbstractActivity
        implements HeadsetReceiver.HeadsetStatusListener,
        SlidingButton.OnUnLockListener,
        SensorEventListener,
        PhoneStateReceiver.PhoneStateChangeListener,
        SimStateReceiver.OnSimStateCallBackListener {


    /**
     * 录音状态枚举
     */
    enum RecordStatus {
        RECORD_UNSTART,//开始录音
        RECORD_RECORDING,//录音中
        RECORD_PAUSE//暂停录音
    }

    //界面控件
    @BindView(R.id.activity_audio_record)
    RelativeLayout rlAudioRecordLayout;
    @BindView(R.id.ib_audio_record_control)
    ImageButton ibRecordControl;
    @BindView(R.id.ib_audio_record_save)
    ImageView ibREcordSave;
    @BindView(R.id.tv_audio_record_save_text)
    TextView tvAudioSaveText;
    @BindView(R.id.tv_audio_record_control_text)
    TextView tvAudioControlText;
    @BindView(R.id.iv_audio_record_left_wheel)
    ImageView ivLeftWheel;
    @BindView(R.id.iv_audio_record_right_wheel)
    ImageView ivRightWheel;
    @BindView(R.id.tv_audio_record_file_name)
    TextView tvFileName;
    @BindView(R.id.tv_audio_record_file_size)
    TextView tvFileSize;
    @BindView(R.id.tv_audio_record_remain_size)
    TextView tvRemainSize;
    @BindView(R.id.tv_audio_record_input_source)
    TextView tvInputSource;
    @BindView(R.id.wavesfv)
    AudioWaveView audioWave;
    @BindView(R.id.ll_audio_record_layout_default)
    LinearLayout llLayoutDefault;
    @BindView(R.id.tv_audio_record_input_source_default)
    TextView tvInputSourceDefault;
    @BindView(R.id.tv_audio_record_remain_size_default)
    TextView tvRemainSizeDefault;
    @BindView(R.id.sb_audio_record_lock)
    SlidingButton sbAudioRecordLock;
    @BindView(R.id.fl_audio_record_lock_layout)
    FrameLayout flLockLayout;
    @BindView(R.id.tv_audio_record_time)
    TextView tvAudioRecordTime;
    @BindView(R.id.tv_audio_record_millisecond)
    TextView tvAudioRecordMilliSecond;
    @BindView(R.id.fl_audio_record_layout)
    FrameLayout flRecordLayout;
    //锁屏
    PowerManager pm;
    PowerManager.WakeLock mWakeLock;
    //声明音频管理器
    private AudioManager mAudioManager;
    //声明耳机广播接收器方法
    private HeadsetReceiver mHeadsetReceiver;
    //声明SIM卡状态监听器
    private SimStateReceiver mSimStateReceiver;
    //方向传感器
    private Sensor mOrientationSensor;
    //传感器管理器
    private SensorManager mSensorManager;
    //录音类对象
    MP3Recorder mRecorder;
    //控制按钮标记
    private RecordStatus mRecordStatus = RecordStatus.RECORD_UNSTART;
    //声明补间动画
    private Animation mRotateAnim;
    //声明线程对象
    private Thread mUpdateDataThread, mUpdateTimeThread, mUpdateRaimSizeThread;
    //定义终止更新线程状态标记
    private boolean isCancleThread = true;
    private boolean isUpdateRaimCancle = true;
    //定义更新状态标记
    private boolean isUpdateThreadFlag = false;
    //定义音频时间——单位秒
    private long mTimeSecond = 0L;
    //设置是否有麦克风权限
    private boolean isMicPermission = true;
    //录音临时文件绝对路径
    private String mTemporaryFilePath;
    //录音存放目录
    private String mAudioPath;
    //录音文件名称
    private String mFileName;
    //扩展名
    private String mMP3 = ".mp3";
    //定义屏幕方向标记
    private boolean isOrientation = true;
    //音频ID
    private int mAudioId;
    //添加多媒体文件
    private MediaInfo mMediaInfo;
    //Sim卡状态
    private int mSimStatus = -1;

    /**
     * 手机状态改变是暂停音乐
     */
    @Override
    public void pausePhoneStateChange() {
        if (mRecordStatus == RecordStatus.RECORD_RECORDING) {
            //暂停音频并停止UI
            resolvePauseUI();
            //录音暂停
            mRecorder.setPause(true);
            //隐藏滑动锁
            if (flLockLayout.getVisibility() != View.GONE) {
                //隐藏滑动锁
                flLockLayout.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void playPhoneStateChange() {
    }

    @Override
    protected View initContentView() {
        return getContentView(R.layout.activity_audio_record);
    }

    @Override
    protected void handleCreate() {
        if (PreferencesConfiguration.getBValues(Constants.SETTING_BRIGHT)) {
            flLockLayout.setKeepScreenOn(true);
        }
        //引用初始化系统管理方法
        initSystemManager();
        //初始化常亮方法
        initLightLock();
        //初始化录音文件
        initRecordFile();
        //初始化动画
        initAnim();
        //更新音频采集源UI
        updateNormalUi();
        //设置绘制图标（标尺）
        audioWave.setBitmapRuler(BitmapFactory.decodeResource(getResources(), R.drawable.record_ruler));
        //录音进度线
        audioWave.setBitmapProgressTag(BitmapFactory.decodeResource(getResources(), R.drawable.record_progress_tag));
        //开始绘制
        audioWave.startView();
        //声明接收器对象
        mHeadsetReceiver = new HeadsetReceiver();
        //声明Sim卡状态监听器
        mSimStateReceiver = new SimStateReceiver();
        //线程结束标记
        isUpdateRaimCancle = false;
        //声明更新线程对象
        mUpdateDataThread = new UpdateShowDataThread();
        mUpdateTimeThread = new UpdateAudioTimeThread();
        mUpdateRaimSizeThread = new UpdateRaimSizeThread();
        mUpdateRaimSizeThread.start();
        //引用添加监听器方法
        addListener();
    }

    /**
     * 初始化常亮锁
     */
    private void initLightLock() {
        if (PreferencesConfiguration.getBValues(Constants.SETTING_BRIGHT)) {
            pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "My Tag");
            //是否需计算锁的数量
            mWakeLock.setReferenceCounted(false);
        }
    }

    /**
     * 初始化系统管理
     */
    private void initSystemManager() {
        //获得传感器管理器
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //获得音频管理器对象
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //获得方向传感器
        mOrientationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
    }

    /**
     * 初始化录音文件
     */
    private void initRecordFile() {
        //获得录音绝对路径
        mAudioPath = StorageUtils.getOwnAudioRecordDirectory(this).getPath() + File.separator;
        //拼接论事文件路径
        mTemporaryFilePath = mAudioPath + "audio_temporary_file.mp3";
        //清除临时录音文件
        StorageUtils.deleteFile(mTemporaryFilePath);
        //初始化文件和时间
//        initFileAndData();
        //获得文件名称
        mFileName = TimeUtils.getYMDHMS();
//        mFileName = getSValues(Constants.SYSTEM_DATA) + getSValues(Constants.RECORD_FILE_DEFAULT_KEY);
    }

    /**
     * 初始化动画
     */
    private void initAnim() {
        //设置动画
        mRotateAnim = AnimationUtils.loadAnimation(this, R.anim.anim_record_tape_wheel_rotate);
        //设置匀速
        mRotateAnim.setInterpolator(new LinearInterpolator());
    }

    private void updateNormalUi() {
        String temp = "";

        //设置显示文本
        temp = StorageUtils.getSurplusSapce(Environment.getExternalStorageDirectory().getPath());
        tvRemainSize.setText(getString(R.string.audio_record_remain_size, temp));

        //获得耳机状态
        temp = mAudioManager.isWiredHeadsetOn()
                ? getString(R.string.audio_record_out_mic)
                : getString(R.string.audio_record_in_mic);
        tvInputSource.setText(getString(R.string.audio_record_input_source, temp));
        tvInputSourceDefault.setText(getString(R.string.audio_record_input_source, temp));

        //设置显示文本
        temp = StorageUtils.getSurplusSapce(Environment.getExternalStorageDirectory().getPath());
        tvRemainSizeDefault.setText(getString(R.string.audio_record_remain_size, temp));

        //判断文件是否超出规则
//        isFileOutOfRange = isFileNameOutOfRange();
        //设置文件名
        tvFileName.setText(getString(R.string.audio_record_file_name, mFileName));
        //文件大小
        tvFileSize.setText(getString(R.string.audio_record_file_size, "0.00M"));
    }

    //开始录音动画
    private void startRecordAnim() {
        ivLeftWheel.startAnimation(mRotateAnim);
        ivRightWheel.startAnimation(mRotateAnim);
    }

    //停止录音动画
    private void stopRecordAnim() {
        //暂停动画
        ivLeftWheel.clearAnimation();
        ivRightWheel.clearAnimation();
    }

    /**
     * 接受消息方法
     *
     * @param msg 消息实体
     */
    @Override
    protected void processMessage(Message msg) {
        //获得消息标记  what
        switch (msg.what) {
            case Constants.UPDATE_FILE_SIZE_TAG:
                //文件大小
                tvFileSize.setText(getString(R.string.audio_record_file_size, msg.getData().getString(Constants.FILE_SIZE_KEY)));
                break;
            case Constants.UPDATE_RAIM_SIZE_TAG:
                //设置显示文本
                tvRemainSize.setText(getString(R.string.audio_record_remain_size, msg.getData().getString(Constants.RAIM_SIZE_KEY)));
                tvRemainSizeDefault.setText(getString(R.string.audio_record_remain_size, msg.getData().getString(Constants.RAIM_SIZE_KEY)));
                break;
            case Constants.UPDATE_RECORD_AUDIO_TIME_TAG:
                //更新时间
                updateTimeView(msg.getData());
                break;
            case Constants.RECORD_RAIM_SIZE_TAG:
                controllRecordShow();
//                //保存文件
//                saveAuidoFile(false, false);
                //显示空间不足对话框
                showRaimSpaceDialog();
                break;
            case Constants.UPDATE_RECORD_MILLI_SECOND:
                updateMilliSecondView(msg.arg1, msg.arg2);
                break;
            default:
                break;
        }
    }

    /**
     * 控件不足对话框
     */
    private void showRaimSpaceDialog() {
        showOneButtonDialog(getString(R.string.audio_record_raim_dialog_title),
                getString(R.string.audio_record_raim_dialog_message), null);
    }

    /**
     * 显示默认布局
     */
    private void showDefaultLayout() {
        //设置控制状态——播放状态
        mRecordStatus = RecordStatus.RECORD_UNSTART;
        //移除动画
        stopRecordAnim();

        mUpdateDataThread = new UpdateShowDataThread();
        mUpdateTimeThread = new UpdateAudioTimeThread();
        //计时
        mTimeSecond = 0L;

        changePlayViewShowStatus();

        //显示控制文本
        tvAudioControlText.setText(R.string.audio_record_start);

        //默认时间
        tvAudioRecordTime.setText(R.string.audio_record_show_time);
        tvAudioRecordMilliSecond.setText(R.string.audio_record_show_millisecond);
    }

    /**
     * 设置播放状态
     */
    private void changePlayViewShowStatus() {
        switch (mRecordStatus) {
            case RECORD_PAUSE:
                //修改——暂停暂停状态
                ibRecordControl.setImageResource(R.drawable.record_start);
                tvAudioControlText.setText(R.string.audio_record_pause);
                break;
            case RECORD_RECORDING:
                //隐藏默认布局
                if (llLayoutDefault.getVisibility() != View.GONE) {
                    llLayoutDefault.setVisibility(View.GONE);
                }

                //显示锁定布局
                if (flLockLayout.getVisibility() != View.VISIBLE) {
                    flLockLayout.setVisibility(View.VISIBLE);
                }

                //显示保存图标及文本
                ibREcordSave.setVisibility(View.VISIBLE);
                tvAudioSaveText.setVisibility(View.VISIBLE);
                //修改控制按钮图标及文本为录音中
                ibRecordControl.setImageResource(R.drawable.record_recording);
                tvAudioControlText.setText(R.string.audio_record_recording);
                break;
            case RECORD_UNSTART:
                //隐藏滑动锁
                flLockLayout.setVisibility(View.GONE);
                //隐藏存储状态
                ibREcordSave.setVisibility(View.GONE);
                tvAudioSaveText.setVisibility(View.GONE);
                //修改控制按钮显示状态
                ibRecordControl.setImageResource(R.drawable.record_start);
                break;
        }
    }

    /**
     * 返回按钮方法
     */
    @Override
    public void onBackPressed() {
        if (!isMicPermission || mRecordStatus == RecordStatus.RECORD_UNSTART) {
            isUpdateRaimCancle = true;
            finish();
            return;
        }
        if (mRecordStatus == RecordStatus.RECORD_RECORDING) {
            //暂停录音
            controllRecordShow();
        }

        //保存文件
        saveAuidoFile(true, false);
        //提示信息
        showToast(R.string.audio_record_back_save_tips);
        super.onBackPressed();
    }

    /**
     * 操作按钮点击方法（返回，录音，保存）
     *
     * @param view
     */
    @OnClick({R.id.ib_audio_record_back, R.id.ib_audio_record_control, R.id.ib_audio_record_save})
    public void controlClick(View view) {
        //显示锁定布局
        if (flLockLayout.getVisibility() == View.VISIBLE) {
            return;
        }
        switch (view.getId()) {
            case R.id.ib_audio_record_back://返回按钮
                onBackPressed();
                break;
            case R.id.ib_audio_record_control://录音控制按钮
                if (!SimStateReceiver.isListener()) {
                    SimStateReceiver.addOnSimStateCallBackListener(this);
                }
                controllRecordShow();
                break;
            case R.id.ib_audio_record_save://保存按钮
                //获得控制状态
                if (mRecordStatus == RecordStatus.RECORD_RECORDING) {
                    //暂停录音
                    controllRecordShow();
                }
                //保存文件
                saveAuidoFile(true, true);
                break;
            default:
                break;
        }
    }

    /**
     * 保存文件
     */
    private void saveAuidoFile(boolean isClose, boolean isShowDialog) {
        if (isMicPermission) {
            //判断是否弹出提示框
            if (isShowDialog) {
                new TTAlertDialog.Builder(AudioRecordActivity.this)
                        .setTitle(R.string.audio_record_dialog_title)
                        .setMessage(R.string.audio_record_dialog_message)
                        .setNegativeButton(R.string.audio_record_dialog_sure, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                resolveStopRecord();
                                //文件名字为超出范围——保存文件
                                boolean isSave = saveRecordAudio(mFileName);
                                //添加标记
//                                increaseFileName();
                                if (isSave) {
                                    //跳转音频信息页面
                                    gotoAudioInfo();
                                } else {
                                    showToast(R.string.audio_record_save_failure_tips);
                                    dismissDlg();
                                    isUpdateRaimCancle = true;
                                    finish();
                                }
                            }
                        })
                        .setPositiveButton(R.string.audio_record_dialog_cancle, null)
                        .create()
                        .show();
            } else {
                resolveStopRecord();
                //文件名字为超出范围——保存文件
                saveRecordAudio(mFileName);
                //判断文件对象是否存在
                if (mMediaInfo != null) {
                    long fileSize = Math.abs(mMediaInfo.getSize());
                    int maxFileSize = GlobalVariableManager.MAXWIFIFILESIZE;
                    int netStatus = NetUtils.getNetConnectType();
                    if (GlobalVariableManager.isOpen100 && netStatus == 2) {
                        maxFileSize = GlobalVariableManager.MAX4GFILESIZE;
                    }

                    //根据当前手机网络条件来确定上传的素材最大大小
                    if (fileSize <= maxFileSize) {
                        //添加上传
                        UploadManager.getInstance().addUpload(mMediaInfo);
                    }
                }

                //添加标记
//                increaseFileName();
                //关闭线程
                isCancleThread = true;
                if (isClose)
                    isUpdateRaimCancle = true;
                //关闭页面
                finish();
            }
        } else {
            if (isClose)
                isUpdateRaimCancle = true;
            //关闭页面
            finish();
        }
    }

    /**
     * 跳转音频信息页面
     */
    private void gotoAudioInfo() {
        //创建意图对象
        Intent intent = new Intent(this, AudioInformationActivity.class);
        //添加数据
        intent.putExtra(Constants.GOTO_TYPE_KEY, Constants.GOTO_TYPE_RECORD);
        intent.putExtra(Constants.AUDIO_INFO_AUDIO_FILE_PATH_KEY, getFilePath(mMediaInfo.getAbsolutePath()));
        intent.putExtra(Constants.MEDIA_AUDIO_KEY, mMediaInfo);
//        intent.putExtra(Constants.AUDIO_INFO_AUDIO_ID_KEY, mAudioId);
//        intent.putExtra(Constants.AUDIO_INFO_AUDIO_NAME_KEY, mFileName);
        //跳转音频信息页面
        startActivity(intent);
        isUpdateRaimCancle = true;
        //关闭页面
        finish();
    }

    private String getFilePath(String mAudioPath) {
        File _file = new File(mAudioPath);
        if (_file.exists()) {
            return _file.getParentFile().getPath();
        } else {
            return mAudioPath.substring(0, mAudioPath.lastIndexOf("/"));
        }
    }

    /**
     * 控制返回和保存状态
     *
     * @return
     */
    private boolean controlBackAndSaveStatu() {
        //判断当前录音状态——录音状态
        if (RecordStatus.RECORD_RECORDING == mRecordStatus) {
            //暂停录音
            controllRecordShow();
        } else if (mRecordStatus == RecordStatus.RECORD_PAUSE) {
            //不做处理
        } else if (mRecordStatus == RecordStatus.RECORD_UNSTART) {
            //关闭线程标记
            isCancleThread = true;
            //关闭界面
            finish();
            //跳出方法
            return true;
        }
        return false;
    }

    /**
     * 开始方法
     */
    @Override
    protected void onStart() {
        super.onStart();
        //引用耳机注册方法
        registerHeadsetStatusReceiver();
    }

    /**
     * 停止方法
     */
    @Override
    protected void onStop() {
        super.onStop();
        //引用耳机取消注册方法
        unregisterHeadsetStatusReceiver();
    }

    /**
     * 销毁Activity方法
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        PhoneStateReceiver.removeOnPhoneStateListener(this);
    }

    /**
     * 控制录音显示方法
     */
    private void controllRecordShow() {
        switch (mRecordStatus) {
            case RECORD_UNSTART:
                if (StorageUtils.isSurplusSapce(Environment.getExternalStorageDirectory().getPath())) {
                    //提示无法录音
                    showToast(R.string.audio_record_raim_size_tip);
                    //返回
                    return;
                }
                //开始录音
                resolveRecord();
                audioWave.startDraw();
                break;
            case RECORD_RECORDING:
                resolvePause();
                audioWave.pauseDraw();
                break;
            case RECORD_PAUSE:
                if (StorageUtils.isSurplusSapce(Environment.getExternalStorageDirectory().getPath())) {
                    //提示无法录音
                    showToast(R.string.audio_record_raim_size_tip);
                    //返回
                    return;
                }
                //开始录音
                resolvePause();
                audioWave.startDraw();
                break;
            default:
                break;
        }
    }

    /**
     * 添加监听器方法
     */
    private void addListener() {
        mHeadsetReceiver.setHeadsetStatusListener(this);
        sbAudioRecordLock.setOnUnLockListener(this);
        PhoneStateReceiver.setOnPhoneStateListener(this);
        SimStateReceiver.addOnSimStateCallBackListener(this);
    }

    /**
     * 获得耳机状态方法
     *
     * @param isStatus
     */
    @Override
    public void onHeadsetStatus(boolean isStatus) {
        //获得耳机状态
        tvInputSource.setText(getString(R.string.audio_record_input_source, isStatus ? getString(R.string.audio_record_out_mic) : getString(R.string.audio_record_in_mic)));
        tvInputSourceDefault.setText(getString(R.string.audio_record_input_source, isStatus ? getString(R.string.audio_record_out_mic) : getString(R.string.audio_record_in_mic)));
    }

    /**
     * 注册耳机广播接收器方法
     */
    private void registerHeadsetStatusReceiver() {
        if (mHeadsetReceiver != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.HEADSET_PLUG");
            registerReceiver(mHeadsetReceiver, filter);
        } else {
            mHeadsetReceiver = new HeadsetReceiver();
            mHeadsetReceiver.setHeadsetStatusListener(this);
            registerHeadsetStatusReceiver();
        }
    }

    /**
     * 取消广播接收器方法
     */
    private void unregisterHeadsetStatusReceiver() {
        if (mHeadsetReceiver != null) {
            unregisterReceiver(mHeadsetReceiver);
            mHeadsetReceiver = null;
        }
    }

    /**
     * 开始录音
     */
    private void resolveRecord() {
        //开始线程标记
        isCancleThread = false;
        //开启线程
        mUpdateDataThread.start();
        mUpdateTimeThread.start();

        mRecorder = new MP3Recorder(new File(mTemporaryFilePath));
        int size = ScreenUtils.getScreenWidth() / 2;//控件默认的间隔是1
        mRecorder.setDataList(audioWave.getRecList(), size);
        mRecorder.setErrorHandler(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == MP3Recorder.ERROR_TYPE) {
                    showOneButtonDialog(getString(R.string.audio_record_permission_dialog_title),
                            getString(R.string.audio_record_permission_dialog_message),
                            null);
                    resolveError();
                }
            }
        });

        try {
            mRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(AudioRecordActivity.this, "录音出现异常", Toast.LENGTH_SHORT).show();
            resolveError();
            return;
        }

        resolveRecordUI();

        changePlayViewShowStatus();
    }

    /**
     * 停止录音
     */
    private void resolveStopRecord() {
        resolveStopUI();
        if (mRecorder != null && mRecorder.isRecording()) {
            mRecorder.setPause(false);
            mRecorder.stop();
            audioWave.stopView();
        }
    }

    /**
     * 录音异常
     */
    private void resolveError() {
        resolveNormalUI();
        if (mRecorder != null && mRecorder.isRecording()) {
            mRecorder.stop();
            audioWave.stopView();
        }
    }

    /**
     * 暂停
     */
    private void resolvePause() {
        if (mRecordStatus == RecordStatus.RECORD_UNSTART) {
            return;
        }

        if (mRecorder.isPause()) {
            resolveRecordUI();
            mRecorder.setPause(false);
        } else {
            resolvePauseUI();
            mRecorder.setPause(true);
        }
    }

    /**
     * 还原初始化UI
     */
    private void resolveNormalUI() {
        //关闭权限
        isMicPermission = false;
        //开始更新标记
        isUpdateThreadFlag = false;
        //开始线程标记
        isCancleThread = true;
        //还原布局
        showDefaultLayout();
    }

    /**
     * 还原录音UI
     */
    private void resolveRecordUI() {
        isMicPermission = true;
        //设置状态录音中
        mRecordStatus = RecordStatus.RECORD_RECORDING;
        //开始更新标记
        isUpdateThreadFlag = true;

        startRecordAnim();

        changePlayViewShowStatus();

        //重新设置Lock
        sbAudioRecordLock.reset();
    }

    /**
     * 暂停UI
     */
    private void resolvePauseUI() {
        mRecordStatus = RecordStatus.RECORD_PAUSE;
        //关闭更新标记
        isUpdateThreadFlag = false;

        stopRecordAnim();

        changePlayViewShowStatus();
    }

    /**
     * 停止录音UI
     */
    private void resolveStopUI() {
        //关闭权限
        isMicPermission = false;
        //开始更新标记
        isUpdateThreadFlag = false;
        //开始线程标记
        isCancleThread = true;
    }

    /**
     * 保存录音
     */
    private boolean saveRecordAudio(String pFileName) {
        File _file = new File(mTemporaryFilePath);
        boolean isSave = _file.renameTo(new File(mAudioPath + pFileName + mMP3));
        //判断是否存储成功
        if (isSave) {
            //添加录音音频数据到数据库
            addAudioRecordDB(pFileName);
        }
        return isSave;
    }

    /**
     * 设置解锁方法
     *
     * @param lock
     */
    @Override
    public void setUnLocked(boolean lock) {
        if (lock) {
            //显示锁定布局
            flLockLayout.setVisibility(View.GONE);
        }
    }

//    /**
//     * 初始化文本和数据
//     */
//    private void initFileAndData() {
//        //获得存储系统时间
//        String saveTime = getSValues(Constants.SYSTEM_DATA);
//        String systemTime = TimeUtils.getTimeForSpecialFormat(TimeUtils.TimeFormat.TimeFormat9);
//        //判断是否为Null
//        if (TextUtils.isEmpty(saveTime) || !systemTime.equals(saveTime)) {
//            //存储系统时间
//            PreferencesConfiguration.setSValues(Constants.SYSTEM_DATA, systemTime);
//            //文件名称——默认后缀 001
//            PreferencesConfiguration.setSValues(Constants.RECORD_FILE_DEFAULT_KEY, Constants.RECORD_FILE_DEFAULT_NAME);
//        }
//    }

//    /**
//     * 增加文件名称后缀，并且不能待遇999断
//     */
//    private void increaseFileName() {
//        //获得文件后缀
//        String _defaultValue = PreferencesConfiguration.getSValues(Constants.RECORD_FILE_DEFAULT_KEY);
//        //判断文件后缀不是null 或者""
//        if (!TextUtils.isEmpty(_defaultValue)) {
//            //强制装换转换为数字
//            int _defaultNumber = Integer.parseInt(_defaultValue);
//            //小于999加1
//            _defaultNumber += 1;
//            //将后缀转换为字符串
//            String _newValue = String.valueOf(_defaultNumber);
//            //判断字符串长度
//            switch (_newValue.length()) {
//                case 1:// 一位数
//                    _newValue = "00" + _newValue;
//                    break;
//                case 2:// 两位数
//                    _newValue = "0" + _newValue;
//                    break;
//                default:// 三位数以上
////                        _newValue = _newValue;
//                    break;
//            }
//            //存储文件名称后缀
//            PreferencesConfiguration.setSValues(Constants.RECORD_FILE_DEFAULT_KEY, _newValue);
//        }
//    }

    /**
     * 更新时间
     *
     * @param bundle
     */
    private void updateTimeView(Bundle bundle) {
        //获得时间
        String _time = bundle.getString(Constants.RECORD_AUDIO_TIME_KEY);
        //时间是否为null
        if (!TextUtils.isEmpty(_time)) {
            //设置时间
            tvAudioRecordTime.setText(_time);
        }
    }

    /**
     * 毫秒更新显示
     *
     * @param time
     */
    private void updateMilliSecondView(long time, long milliSecond) {
        String _milliStr;
        if (milliSecond < 10) {
            _milliStr = ".0" + milliSecond;
        } else {
            _milliStr = "." + milliSecond;
        }
        String _timeStr = TimeUtils.converToms(time);
        //设置时间
        tvAudioRecordTime.setText(_timeStr);
        tvAudioRecordMilliSecond.setText(_milliStr);
    }

//    /**
//     * 判断文件是否存在
//     *
//     * @param fileName
//     * @return
//     */
//    private boolean isExistsFile(String fileName) {
//        //获得文件对象
//        File _file = new File(mAudioPath + fileName);
//        //判断文件是否存在
//        if (_file.exists()) {
//            return true;
//        }
//        return false;
//    }

    /**
     * 添加音频录音数据至数据库
     *
     * @param fileName 文件名称
     */
    private void addAudioRecordDB(String fileName) {
        //获得文件名称对象
        File file = new File(mAudioPath + fileName + mMP3);
        //判断文件是否存在
        if (file.exists()) {
            //声明对媒体对象
            MediaInfo _info = new MediaInfo();
            //添加默认服务器ID
            _info.setMedia_id(-1);
            //用户ID
            _info.setUser_id(AccoutConfiguration.getLoginInfo().getUserid());
            //文件大小
            _info.setSize(file.length());
            //文件名称（带扩展名）
            _info.setFullName(fileName + mMP3);
            //文件名称
            _info.setTitle(fileName);
            //文本类型
            _info.setMime_type(Constants.MIME_TYPE_AUDIO_MP3);
            //数据地址
            _info.setAbsolutePath(mAudioPath + fileName + mMP3);
            //添加文本的时间
            _info.setDate_added(System.currentTimeMillis() / 1000);
            //更新文本的时间
            _info.setDate_modified(System.currentTimeMillis());
            //更新文本上传状态
            _info.setUpload_status(Constants.UPLOAD_STATUS_DEFAULT);
            //添加音频文件时间
//            _info.setDuration((int) (mTimeSecond));
            _info.setDuration(MediaPlayer.create(this, Uri.fromFile(new File(mAudioPath + fileName + mMP3))).getDuration());
            //设置定位信息
            BDLocation location = LocationHelper.getInstance().getBDLocation();
            if (location != null) {
                _info.setLatitude(location.getLatitude());
                _info.setLongitude(location.getLongitude());
                _info.setPlace(getAddress(location.getAddrStr()));
            }
            //添加音频数据到数据库中
            DBAudioRecordManager.getInstance(this).addAudioRecord(_info);
            //查询音频文件ID
            mAudioId = DBAudioRecordManager.getInstance(this).queryAudioRecordInfo(_info.getTitle());
            //更新_info的ID
            _info.setId(mAudioId);
            //设置成员对象
            mMediaInfo = _info;
        }
    }

    /**
     * 触感器变化方法
     *
     * @param sensorEvent
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //获得手机Y轴数据
        float y = sensorEvent.values[SensorManager.DATA_Y];
        if (y < -40 && y > -140) {
            //竖屏
            if (!isOrientation) {
                //旋转屏幕——正向显示
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                isOrientation = true;
            }
        } else if (y > 40 && y < 140) {
            //竖屏倒置——反向显示
            if (isOrientation) {
                //旋转屏幕
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                isOrientation = false;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    /**
     * 恢复方法
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (mWakeLock != null) mWakeLock.acquire();
        //初始化设置-录音时屏幕倒转标记
        if (PreferencesConfiguration.getBValues(Constants.SETTING_REVERSAL)) {
            //添加传感器注册监听方法
            mSensorManager.registerListener(this, mOrientationSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    /**
     * 暂停方法
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mWakeLock != null) mWakeLock.release();
        if (PreferencesConfiguration.getBValues(Constants.SETTING_REVERSAL)) {
            //取消传感器监听器
            mSensorManager.unregisterListener(this);
        }
    }

    /**
     * 地址是否怒空字符串
     *
     * @param address
     * @return 地址信息，如果为没有地址返回空字符串
     */
    private String getAddress(String address) {
        String add = "";
        if (!TextUtils.isEmpty(address)) {
            add = address;
        }

        if ("null".equals(add)) {
            add = "";
        }

        return add;
    }

    /**
     * 更新显示数据线程
     */
    class UpdateShowDataThread extends Thread {
        @Override
        public void run() {
            while (!isCancleThread) {
                if (isUpdateThreadFlag) {

                    //判断控件对否小于0.5G
                    if (StorageUtils.isSurplusSapce(Environment.getExternalStorageDirectory().getPath())) {
                        //声明消息,添加空间不足标记，并发送消息
                        Message.obtain(basicHandler, Constants.RECORD_RAIM_SIZE_TAG).sendToTarget();
                    }
                    //获得当前文本大小
                    String _fileSize = StorageUtils.getTagetFileSize(mTemporaryFilePath);
                    //生成数据
                    Bundle _data = new Bundle();
                    //添加数据
                    _data.putString(Constants.FILE_SIZE_KEY, _fileSize);
                    //发送消息
                    Message msg = Message.obtain(basicHandler);
                    //添加标记
                    msg.what = Constants.UPDATE_FILE_SIZE_TAG;
                    //添加数据
                    msg.setData(_data);
                    //发送消息
                    msg.sendToTarget();
                }
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 更新音频时间线程
     */
    class UpdateAudioTimeThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!isCancleThread) {
                if (isUpdateThreadFlag) {
                    Message.obtain(basicHandler, Constants.UPDATE_RECORD_MILLI_SECOND, (int) (mTimeSecond), (int) (mTimeSecond % 1000 / 10)).sendToTarget();
                    mTimeSecond += 76;
                }
                try {
                    Thread.sleep(76);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 检查剩余存储空间
     */
    class UpdateRaimSizeThread extends Thread {
        @Override
        public void run() {
            while (!isUpdateRaimCancle) {
                //获得剩余空间大小
                String _raimSize = StorageUtils.getSurplusSapce(Environment.getExternalStorageDirectory().getPath());
                //生成数据
                Bundle _data = new Bundle();
                //添加数据
                _data.putString(Constants.RAIM_SIZE_KEY, _raimSize);
                //发送消息
                Message msg = Message.obtain(basicHandler);
                //添加标记
                msg.what = Constants.UPDATE_RAIM_SIZE_TAG;
                //添加数据
                msg.setData(_data);
                //发送消息
                msg.sendToTarget();

                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void callBack(int simState) {
        if (simState == mSimStatus) {
            return;
        }
        mSimStatus = simState;
        if (simState == 0) {
            flLockLayout.setVisibility(View.INVISIBLE);
            if (mRecordStatus == RecordStatus.RECORD_RECORDING) {
                SimStateReceiver.removeaddOnSimStateCallBackListener();
                controllRecordShow();
            }
        }
    }
}
