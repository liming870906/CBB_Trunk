package com.tingtingfm.cbb.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.playengine.PlayerEngine;
import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.bean.MediaInfo;
import com.tingtingfm.cbb.common.cache.MediaDataManager;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.configuration.GlobalVariableManager;
import com.tingtingfm.cbb.common.configuration.UrlManager;
import com.tingtingfm.cbb.common.db.DBAudioRecordManager;
import com.tingtingfm.cbb.common.helper.HttpRequestHelper;
import com.tingtingfm.cbb.common.net.BaseRequestCallback;
import com.tingtingfm.cbb.common.net.RequestEntity;
import com.tingtingfm.cbb.common.upload.DBOperationUtils;
import com.tingtingfm.cbb.common.upload.UploadListener;
import com.tingtingfm.cbb.common.upload.UploadManager;
import com.tingtingfm.cbb.common.utils.NetUtils;
import com.tingtingfm.cbb.common.utils.StorageUtils;
import com.tingtingfm.cbb.common.utils.TimeUtils;
import com.tingtingfm.cbb.response.CheckMaterialResponse;
import com.tingtingfm.cbb.response.SendRBCNetResponse;
import com.tingtingfm.cbb.ui.play.MediaCore;
import com.tingtingfm.cbb.ui.receiver.PhoneStateReceiver;
import com.tingtingfm.cbb.ui.view.UploadLoadView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * 音频预览界面
 */
public class AudioPreviewActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener,
        UploadListener,
        PhoneStateReceiver.PhoneStateChangeListener,
        PlayerEngine.OnPreparedListener,
        PlayerEngine.OnCompletionListener,
        PlayerEngine.OnErrorListener,
        PlayerEngine.OnBufferingUpdateListener,
        PlayerEngine.OnSeekCompleteListener,
        PlayerEngine.OnInfoListener {

    //日志标记
    private static final String TAG = "AudioPreviewActivity======>";

    private static final int PREVIEW_UPDATE_SEEKBAR = 0x1;

    @BindView(R.id.ll_audio_preview_added_layout)
    LinearLayout llAddedLayout;
    @BindView(R.id.ll_audio_preview_upload_status_layout)
    LinearLayout llUploadLayout;
    @BindView(R.id.ll_audio_preview_make_web_layout)
    LinearLayout llMakeWebLayout;
    @BindView(R.id.ll_audio_preview_send_layout)
    LinearLayout llSendLayout;
    @BindView(R.id.ll_audio_preview_delete_layout)
    LinearLayout llDeleteLayout;
    @BindView(R.id.iv_audio_preview_upload_status)
    UploadLoadView ivUploadStatus;
    @BindView(R.id.tv_audio_preview_file_size)
    TextView tvFileSize;
    @BindView(R.id.btn_audio_preview_seek)
    Button btnSeek;
//    @BindView(R.id.dv_audio_preview_wave)
//    DrawView mDrawWaveView;
//    @BindView(R.id.waveview)
//    WaveView audioWave;
    @BindView(R.id.ib_audio_preview_start_or_pause)
    ImageButton ibPlayControl;
    @BindView(R.id.tv_audio_preview_time)
    TextView tvAudioTime;
    @BindView(R.id.tv_audio_preview_time_sum)
    TextView tvAudioTimeSum;
    @BindView(R.id.sb_audio_preview_progress)
    SeekBar sbProgress;
    @BindView(R.id.tv_audio_preview_upload_text)
    TextView tvUploadText;
    @BindView(R.id.tv_audio_preview_content_audio_name)
    TextView tvAudioNameLabel;
    @BindView(R.id.tv_audio_preview_content_audio_time)
    TextView tvAudioTimeLable;
    @BindView(R.id.tv_audio_preview_content_audio_file_size)
    TextView tvFileSizeLable;
    //多媒体对象
    private MediaInfo mMediaInfo;
    //文件扩展名
    private String mMP3 = ".mp3";
    //设置旋转动画
    private Animation mRotateAnim;
    //设置倍速标记
    private int mSpeedFalg = Constants.MEDIA_AUDIO_SEEK_1X;
    //设置播放状态
    //播放开始时间和结束时间
    private long maxDuratioin;
    private long currentDuration, currentSeekDuration;
    private String mTotalTime;
    //预览音频文件
    private File mFile;
    private boolean seekBarTouch;
    private boolean isFirstDraw = true;
    private boolean isPlayOver = false;

    private boolean isCreatePlayerEngine = false;
    private PlayerEngine playerEngine;
    private boolean isPlay = false;
    private boolean isCanClick = true;
    private boolean isStartPlay;

    @Override
    protected View initContentView() {
        return getContentView(R.layout.activity_audio_preview);
    }

    @Override
    protected void handleCreate() {
        playerEngine = MediaCore.getExistingInstance().getPlayerEngine();
        sbProgress.setOnSeekBarChangeListener(this);
        //设置初始总时间
        mTotalTime = getString(R.string.audio_preview_default_show_time);
        //显示右侧图标
        setRightView1Visibility(View.VISIBLE);
        //设置右侧图标
        setRightView1Background(R.drawable.audio_preview_reset_name_selector);

        initAnim();

        //获得数据
        mMediaInfo = (MediaInfo) getIntent().getParcelableExtra(Constants.MEDIA_AUDIO_KEY);
        if (mMediaInfo != null) {
            mTotalTime = TimeUtils.converToms(mMediaInfo.getDuration());
            initUI();
            initMediaPlayer();
        }

        PhoneStateReceiver.setOnPhoneStateListener(this);
    }

    private void initAnim() {
        //声明动画
        mRotateAnim = AnimationUtils.loadAnimation(this, R.anim.anim_record_tape_wheel_rotate);
        //设置连续旋转
        mRotateAnim.setInterpolator(new LinearInterpolator());
    }

    private void initUI() {
        if (mMediaInfo != null) {
            setCenterViewContent(mMediaInfo.getFullName());
            //设置上传状态
            changeUploadStatusView(mMediaInfo.getUpload_status());
            //设置文件大小
            tvFileSize.setText(getString(R.string.audio_preview_file_size,
                    StorageUtils.getTagetFileSize(mMediaInfo.getAbsolutePath())));
            tvFileSizeLable.setText(getString(R.string.audio_preview_text_content_audio_file_size,
                    StorageUtils.getTagetFileSize(mMediaInfo.getAbsolutePath())));
            tvAudioTimeLable.setText(getString(R.string.audio_preview_text_content_audio_time,mTotalTime));
            tvAudioNameLabel.setText(getString(R.string.audio_preview_text_content_audio_name,mMediaInfo.getFullName()));
        }
        updateAudioPlayTimeView();
    }

    private void sendUpdateSeekBarMessage() {
        if (isPlay && basicHandler != null) {
            basicHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (playerEngine != null) {
                        playerEngine.SetSoundSpeed(mSpeedFalg == Constants.MEDIA_AUDIO_SEEK_1X ? 0 : 1);
                    }

                    if (basicHandler != null) {
                        basicHandler.obtainMessage(PREVIEW_UPDATE_SEEKBAR).sendToTarget();
                    }
                }
            }, 300);
        }
    }

    @Override
    protected void processMessage(Message msg) {
        switch (msg.what) {
            case PREVIEW_UPDATE_SEEKBAR:
                updateProgressView();
                updateAudioPlayTimeView();
                sendUpdateSeekBarMessage();
                break;
            case Constants.AUDIO_PREVIEW_UPLOAD_UPLOAD_STATUS_TAG:
                //更新多媒体状态
                updateMediaStatus(msg.arg1);
                //更新数据库状态
                DBOperationUtils.updateMaterialDb(mMediaInfo);
                break;
        }
    }

    /**
     * 更新进度条
     */
    private void updateProgressView() {
        if (playerEngine != null && isPlay) {
            long duration = playerEngine.getCurrentPosition();

            if (isStartPlay && duration == 0) {
                return;
            }

            currentDuration = currentSeekDuration = duration;
            if (currentDuration > 0 && !seekBarTouch) {
                sbProgress.setProgress((int) currentDuration);
            }
            isStartPlay = true;

        }
    }

    /**
     * 更新播放时间显示
     */
    private void updateAudioPlayTimeView() {
        //处理类似12600毫秒显示12秒的情况
        if (currentDuration + 600 > maxDuratioin) {
            currentDuration = currentSeekDuration = maxDuratioin;
        }
        tvAudioTime.setText(getString(R.string.audio_preview_play_time, TimeUtils.converToms(currentDuration)));
        tvAudioTimeSum.setText(getString(R.string.audio_preview_play_time_sum, mTotalTime));
    }

    /**
     * 底部功能栏点击方法
     *
     * @param view
     */
    @OnClick({R.id.ll_audio_preview_upload_status_layout, R.id.ll_audio_preview_make_web_layout, R.id.ll_audio_preview_added_layout, R.id.ll_audio_preview_send_layout, R.id.ll_audio_preview_delete_layout})
    public void onBottomView(View view) {
        switch (view.getId()) {
            case R.id.ll_audio_preview_upload_status_layout:    //上传状态

                //多媒体对象为null——或则长传状态为：上传中和已上传点击无响应
                if (mMediaInfo == null || mMediaInfo.getUpload_status() == Constants.UPLOAD_STATUS_LOADING || mMediaInfo.getUpload_status() == Constants.UPLOAD_STATUS_SUCCESS)
                    return;

                int uploadStatus = mMediaInfo.getUpload_status();
                if (uploadStatus == Constants.UPLOAD_STATUS_DEFAULT
                        || uploadStatus == Constants.UPLOAD_STATUS_FAILURE) {
                    if (mMediaInfo != null) {
                        uploadMaterial(Math.abs(mMediaInfo.getSize()));
                    }
                }
                break;
            case R.id.ll_audio_preview_make_web_layout: //制作网
                if (mMediaInfo != null) {
                    //判断是否上传成功
                    if (mMediaInfo.getUpload_status() != Constants.UPLOAD_STATUS_SUCCESS) {
                        //提示：请把素材上传后，再传制作网
                        showToast(R.string.audio_preview_make_web_tips);
                        return;
                    }
                    //判断素材是否被删除
                    checkMaterialRequest(Constants.AUDIO_PREVIEW_MAKE_WEB_TYPE);
                }
                break;
            case R.id.ll_audio_preview_added_layout:    //添加
                pauseAudio();
                if (mMediaInfo != null) {
                    long _size = Math.abs(mMediaInfo.getSize());
                    //大于500M
                    if (_size > GlobalVariableManager.MAXWIFIFILESIZE) {
                        showOneButtonDialog(true, "", getString(R.string.audio_preview_add_dialog_text), null);
                    } else {
                        //封装数据
                        ArrayList<MediaInfo> data = new ArrayList<>();
                        data.add(mMediaInfo);
                        //跳转写稿页面
                        Intent intent = new Intent(AudioPreviewActivity.this, ManuscriptAddActivity.class);
                        intent.putParcelableArrayListExtra("ObjList", data);
                        startActivity(intent);
                    }
                }

                break;
            case R.id.ll_audio_preview_send_layout: //发送
                pauseAudio();

                sendMaterial();
                break;
            case R.id.ll_audio_preview_delete_layout:   //删除
                //显示删除提示框
                showDeleteAudioDialog();
                break;
        }
    }

    private void uploadMaterial(long fileSize) {
        int maxFileSize = GlobalVariableManager.MAXWIFIFILESIZE;
        int netStatus = NetUtils.getNetConnectType();
        if (GlobalVariableManager.isOpen100 && netStatus == 2) {
            maxFileSize = GlobalVariableManager.MAX4GFILESIZE;
        }

        if (fileSize > maxFileSize) {
            boolean isGreater500 = false;
            if (maxFileSize == GlobalVariableManager.MAX4GFILESIZE
                    && fileSize > GlobalVariableManager.MAXWIFIFILESIZE) {
                isGreater500 = true;
            }
            showOneButtonDialog(true, "",
                   !isGreater500 && maxFileSize == GlobalVariableManager.MAX4GFILESIZE
                    ? getString(R.string.material_upload_size100_message)
                           : getString(R.string.audio_preview_upload_dialog_text), null);
        } else {
            //添加到下载队列
            UploadManager.getInstance().addUpload(mMediaInfo);
            MediaDataManager.getInstance().updateMediaInfo(mMediaInfo, MediaDataManager.MediaType.MEDIA_AUDIO_TYPE);
        }
    }
    /**
     * 发送方法
     */
    private void sendMaterial() {
        // TODO: 17/1/18 发送方法
        if (mMediaInfo.getUpload_status() != Constants.UPLOAD_STATUS_SUCCESS) {
            showToast(R.string.material_manage_send_no_upload_tips);
            return;
        }
        //引用发送请求
        checkMaterialRequest(Constants.AUDIO_PREVIEW_SEND_TYPE);
    }

    /**
     * 播放速度点击方法
     *
     * @param view
     */
    @OnClick(R.id.btn_audio_preview_seek)
    public void clickChangePlaySpeed(View view) {
        switch (mSpeedFalg) {
            case Constants.MEDIA_AUDIO_SEEK_1X:  //当前播放1X速度
                //设置倍速标记
                mSpeedFalg = Constants.MEDIA_AUDIO_SEEK_1_5X;
                //显示倍速文本
                btnSeek.setText(R.string.audio_preview_seek_1_5);
                //判断播放对象是否存在
                if (isPlay) {
                    //设置播放速率
                    playerEngine.SetSoundSpeed(mSpeedFalg == Constants.MEDIA_AUDIO_SEEK_1X ? 0 : 1);
                }
                break;
            case Constants.MEDIA_AUDIO_SEEK_1_5X://当前播放1.5X速度
                //设置倍速标记
                mSpeedFalg = Constants.MEDIA_AUDIO_SEEK_1X;
                //显示倍速文本
                btnSeek.setText(R.string.audio_preview_seek_1);
                //判断播放对象是否存在
                if (isPlay) {
                    playerEngine.SetSoundSpeed(mSpeedFalg == Constants.MEDIA_AUDIO_SEEK_1X ? 0 : 1);
                }
                break;
        }
    }

    /**
     * 播放、暂停方法
     *
     * @param view
     */
    @OnClick(R.id.ib_audio_preview_start_or_pause)
    public void clickPlayOrPause(View view) {
        // TODO: 2017/4/1 当前播放进度有主动改变时，采用SeekTo
        if (isCreatePlayerEngine) {
            if (isPlay) {
                //正在播放，暂停
                pauseAudio();
            } else {
                if (currentDuration == 0
                        || currentDuration == maxDuratioin
                        || currentSeekDuration > currentDuration
                        || currentSeekDuration < currentDuration) {
                    if (currentSeekDuration == maxDuratioin) {
                        currentSeekDuration = currentDuration = 0;
                        isStartPlay = false;
                    }

                    playAudio((int) currentSeekDuration, true);
                } else {
                    playAudio((int) currentDuration, false);
                }
            }
        }
    }

    /**
     * 播放音频，@param startPosition 开始播放的时间
     */
    private void playAudio(int startPosition, boolean isSeek) {
        //判断多媒体类是否为null
        if (playerEngine == null) {
            //不做播放操作，返回该方法
            return;
        }
        try {
            //更新播放
            if (isSeek) {
                playerEngine.seekTo(startPosition);
            }
            playerEngine.start();
            System.out.println(" playAudio   mSpeedFalg = " + mSpeedFalg);
            isPlay = true;
            changePlayControlView();
            sendUpdateSeekBarMessage();
//            controlDrawWaveAnim(isPlay);
        } catch (Exception e) {
            return;
        }
    }

    private void pauseAudio() {
        if (isPlay) {
            playerEngine.pause();
        }

        isPlay = false;
        changePlayControlView();
//        controlDrawWaveAnim(isPlay);
    }

    @OnClick(R.id.ib_audio_preview_jia_3s)
    public void clickPlayJia3s(View view) {
        if (!isPlay) {//暂停状态
            long progress = currentSeekDuration + 3000;
            if (maxDuratioin != 0 && progress >= maxDuratioin) {
                //快进到了文件尾
                currentSeekDuration = maxDuratioin;
                isStartPlay = false;
            } else {
                currentSeekDuration = progress;
            }
            sbProgress.setProgress((int) currentSeekDuration);
            setCurrentTimeTV(currentSeekDuration);
        } else {//播放状态
            if (isCanClick) {
                isCanClick = false;
                long progress = currentDuration + 3000;
                if (maxDuratioin != 0 && progress >= maxDuratioin) {
                    isStartPlay = false;
                    //结束
                    pauseAudio();
                    //快进到了文件尾
                    currentDuration = currentSeekDuration = maxDuratioin;
                    sbProgress.setProgress((int) currentDuration);
                    setCurrentTimeTV(currentDuration);
                } else {
                    playerEngine.seekTo((int) progress);
                }

                basicHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isCanClick = true;
                    }
                }, 100);
            }
        }
    }

    @OnClick(R.id.ib_audio_preview_jian_3s)
    public void clickPlayJian3s(View view) {
        if (!isPlay) {//暂停状态
            long progress = currentSeekDuration - 3000;
            if (maxDuratioin != 0 && progress <= 0) {
                isStartPlay = false;
                //快退到了文件头
                currentSeekDuration = 0;
            } else {
                currentSeekDuration = progress;
            }
            sbProgress.setProgress((int) currentSeekDuration);
            setCurrentTimeTV(currentSeekDuration);
        } else {//播放状态
            if (isCanClick) {
                isCanClick = false;
                long progress = currentDuration - 3000;
                if (maxDuratioin != 0 && progress <= 0) {
                    isStartPlay = false;
                    progress = 0;
                }
                playerEngine.seekTo((int) progress);

                basicHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isCanClick = true;
                    }
                }, 100);
            }
        }
    }

    @Override
    protected void onRightView1Click() {
        super.onRightView1Click();
        pauseAudio();
        //显示提示对话框
        gotoAudioInfo();
    }

    /**
     * 跳转到音频信息页面
     */
    private void gotoAudioInfo() {
        Intent intent = new Intent(this, AudioInformationActivity.class);
        intent.putExtra(Constants.GOTO_TYPE_KEY, Constants.GOTO_TYPE_PREVIEW);
        intent.putExtra(Constants.AUDIO_INFO_AUDIO_FILE_PATH_KEY,getFilePath(mMediaInfo.getAbsolutePath()));
        intent.putExtra(Constants.MEDIA_AUDIO_KEY,mMediaInfo);
//        intent.putExtra(Constants.AUDIO_INFO_AUDIO_ID_KEY, mMediaInfo.getId());
//        intent.putExtra(Constants.AUDIO_INFO_AUDIO_NAME_KEY, mMediaInfo.getTitle());
//        intent.putExtra(Constants.AUDIO_INFO_CF_PERSON_KEY, mMediaInfo.getInterview_persion());
//        intent.putExtra(Constants.AUDIO_INFO_CF_EVENT_KEY, mMediaInfo.getInterview_event());
//        intent.putExtra(Constants.AUDIO_INFO_AUDIO_KEYWORD_KEY, mMediaInfo.getInterview_keyword());
        startActivityForResult(intent, Constants.GOTO_TYPE_PREVIEW);
    }
    private String getFilePath(String mAudioPath) {
        File _file = new File(mAudioPath);
        if (_file.exists()) {
            return _file.getParentFile().getPath();
        }else{
            return mAudioPath.substring(0,mAudioPath.lastIndexOf("/"));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == Constants.GOTO_TYPE_PREVIEW) {
            //更新数据
            String _etContent = data.getStringExtra(Constants.AUDIO_INFO_AUDIO_NAME_KEY);
            //设置文件名称（带扩展名）
            mMediaInfo.setFullName(_etContent + mMP3);
            //设置文件名称去掉扩展名
            mMediaInfo.setTitle(_etContent);
            //修改文件路径
            mMediaInfo.setAbsolutePath(getFilePath(mMediaInfo.getAbsolutePath()) + File.separator + _etContent + mMP3);
            mMediaInfo.setInterview_persion(data.getStringExtra(Constants.AUDIO_INFO_CF_PERSON_KEY));
            mMediaInfo.setInterview_event(data.getStringExtra(Constants.AUDIO_INFO_CF_EVENT_KEY));
            mMediaInfo.setInterview_keyword(data.getStringExtra(Constants.AUDIO_INFO_AUDIO_KEYWORD_KEY));
            //修改标题
            setCenterViewContent(mMediaInfo.getFullName());
            tvAudioNameLabel.setText(getString(R.string.audio_preview_text_content_audio_name,mMediaInfo.getFullName()));
            //更新数据
            MediaDataManager.getInstance().updateMediaInfo(mMediaInfo, MediaDataManager.MediaType.MEDIA_AUDIO_TYPE);
        }
    }

    /**
     * 控制播放状态
     */
    private void changePlayControlView() {
        //获得图片资源
        int drawableID = isPlay ? R.drawable.audio_preview_pause : R.drawable.audio_preview_start;
        //设置图片资源
        ibPlayControl.setImageResource(drawableID);
    }

//    public void controlDrawWaveAnim(boolean isPlay){
//        if(isPlay){
//            mDrawWaveView.startAnim();
//        }else{
//            mDrawWaveView.stopAnim();
//        }
//    }

    /**
     * 修改底部栏上传状态
     */
    private void changeUploadStatusView(int uploadStatus) {
        ivUploadStatus.setUploadStatus(uploadStatus);
        //设置
        switch (uploadStatus) {
            case Constants.UPLOAD_STATUS_DEFAULT:   //默认未上传状态
                tvUploadText.setText(R.string.audio_preview_upload);
                break;
            case Constants.UPLOAD_STATUS_FAILURE:   //上传失败状态
                tvUploadText.setText(R.string.audio_preview_upload_failure);
                break;
            case Constants.UPLOAD_STATUS_LOADING:   //上传中状态
                tvUploadText.setText(R.string.audio_preview_uploading);
                break;
            case Constants.UPLOAD_STATUS_SUCCESS:   //上传成功状态
                tvUploadText.setText(R.string.audio_preview_upload_success);
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        seekBarTouch = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        seekBarTouch = false;
        long duration = seekBar.getProgress();
        if (duration == 0 || duration == maxDuratioin) {
            isStartPlay = false;
        }

        if (isPlay) {
            playerEngine.seekTo((int) duration);
        } else {
            currentSeekDuration = duration;
            setCurrentTimeTV(currentSeekDuration);
        }
    }

    @Override
    public void onBackPressed() {
        onLeftView1Click();
    }

    @Override
    protected void onLeftView1Click() {
        if (playerEngine != null) {
            playerEngine.reset();
            playerEngine = null;
        }
        super.onLeftView1Click();
    }

    /**
     * 显示删除音频提示框
     */
    private void showDeleteAudioDialog() {
        showTwoButtonDialog(true, true,
                null,
                getString(R.string.audio_preview_delete_audio_title),
                null,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pauseAudio();

                        //产出素材
                        deletePreviewAudio(mMediaInfo);
                        //关闭Activity
                        onBackPressed();
                    }
                });
    }

    /**
     * 删除展示的音频
     *
     * @param info
     */
    private void deletePreviewAudio(MediaInfo info) {
        deleteFile(info.getAbsolutePath());
        if (info.getMime_type().startsWith(Constants.MIME_TYPE_AUDIO)) {
            DBAudioRecordManager.getInstance(this).deleteAudioRecord(info.getFullName(), info.getMime_type());
        }
        //删除数据
        MediaDataManager.getInstance().deleteMediaInfo(info, MediaDataManager.MediaType.MEDIA_AUDIO_TYPE);
    }

    /**
     * 删除文件
     */
    public boolean deleteFile(String data) {
        File _file = new File(data);
        if (_file.exists()) {
            return _file.delete();
        }
        return false;
    }

    /**
     * 发送前先检查该文件在服务端是否存在的请求
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
                    switch (pType) {
                        case Constants.AUDIO_PREVIEW_MAKE_WEB_TYPE:
                            //发送制作网
                            sendMakeWebRequest();
                            break;
                        case Constants.AUDIO_PREVIEW_SEND_TYPE:
                            dismissDlg();
                            // TODO: 17/1/18 跳转发送页面
                            startActivity(new Intent(AudioPreviewActivity.this, SendActivity.class)
                                    .putExtra("id_list", String.valueOf(mMediaInfo.getMedia_id())));
                            break;
                    }
                } else {
                    dismissDlg();
                    showOneButtonDialog(true, "", AudioPreviewActivity.this.getString(R.string.audio_preview_make_web_delete_tips, mMediaInfo.getFullName()), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //设置上传状态
                            mMediaInfo.setUpload_status(Constants.UPLOAD_STATUS_DEFAULT);
                            mMediaInfo.setIsUpdateAudioInfo(0);
                            mMediaInfo.setSliceId(0);
                            mMediaInfo.setSliceCount(0);
                            mMediaInfo.setSuccessIds("");
                            mMediaInfo.setMedia_id(0);
                            //更新数据
                            MediaDataManager.getInstance().updateMediaInfo(mMediaInfo, MediaDataManager.MediaType.MEDIA_AUDIO_TYPE);
                            //更新数据库
                            DBOperationUtils.updateMaterialDb(mMediaInfo);
                            //发送更新底部导航栏上传状态
                            Message.obtain(basicHandler, Constants.AUDIO_PREVIEW_UPLOAD_UPLOAD_STATUS_TAG, mMediaInfo.getUpload_status(), 0).sendToTarget();
                        }
                    });
                }
            }

            @Override
            public void onFail(int code, String errorMessage) {
                dismissDlg();
                showToast(errorMessage);
            }

            @Override
            public void onCancel() {

            }
        });
    }

    /**
     * 发送制作网
     */
    private void sendMakeWebRequest() {
        //请求接口
        RequestEntity entity = new RequestEntity(UrlManager.SEND_RBC_NET);
        entity.addParams("id", String.valueOf(mMediaInfo.getMedia_id()));
        HttpRequestHelper.post(entity, new BaseRequestCallback<SendRBCNetResponse>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(SendRBCNetResponse response) {
                switch (response.getData().getStatus()) {
                    case Constants.SEND_MAKE_WEB_SUCCESS://成功
                    case Constants.SEND_MAKE_WEB_FAILE://失败
                    case Constants.SEND_MAKE_WEB_HANDLE://进行中
                    default:
                        showToast(response.getData().getMsg());
                        break;
                    case Constants.SEND_MAKE_WEB_NOT_WEB_USER://没有账号
                        pauseAudio();

                        Intent makeIn = new Intent(AudioPreviewActivity.this, MakeActivity.class);
                        startActivity(makeIn);
                        break;
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

    @Override
    public void start(List<Integer> ids) {
        if (ids.contains(mMediaInfo.getId())) {
            updateMediaStatus(Constants.UPLOAD_STATUS_LOADING);
        }
    }

    @Override
    public void fail(int id) {
        if (id == mMediaInfo.getId()) {
            updateMediaStatus(Constants.UPLOAD_STATUS_FAILURE);
        }
    }

    @Override
    public void success(int id, int mediaId) {
        if (id == mMediaInfo.getId()) {
            mMediaInfo.setMedia_id(mediaId);
            updateMediaStatus(Constants.UPLOAD_STATUS_SUCCESS);
        }
    }

    private void updateMediaStatus(final int status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //更新过媒体对象
                mMediaInfo.setUpload_status(status);
                //点击：上传和上传失败切换至上传中并开启动画
                changeUploadStatusView(mMediaInfo.getUpload_status());
                //更新上传状态
                MediaDataManager.getInstance().updateMediaInfo(mMediaInfo, MediaDataManager.MediaType.MEDIA_AUDIO_TYPE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        UploadManager.getInstance().addUploadListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        UploadManager.getInstance().removeUploadListener(this);
    }

    @Override
    public void pausePhoneStateChange() {
        // TODO: 17/3/15 接收电话赞赢音频
        pauseAudio();
    }

    @Override
    public void playPhoneStateChange() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PhoneStateReceiver.removeOnPhoneStateListener(this);
    }

    private void initMediaPlayer() {
        if (!isCreatePlayerEngine) {
            do {
                //first call,because setDataSource may err
                playerEngine.setOnErrorListener(this);
                playerEngine.setDataSource(mMediaInfo.getAbsolutePath());
                playerEngine.setOnPreparedListener(this);
                playerEngine.setOnCompletionListener(this);
                playerEngine.setOnBufferingUpdateListener(this);
                playerEngine.setOnInfoListener(this);
                playerEngine.setDownBufLen(4 * 1024 * 1024);
                playerEngine.SetSoundSpeed(mSpeedFalg == Constants.MEDIA_AUDIO_SEEK_1X ? 0 : 1);
                playerEngine.prepareAsync();

                isCreatePlayerEngine = true;
            } while (false);
        }
    }

    @Override
    public void onBufferingUpdate(PlayerEngine engine, int i) {

    }

    @Override
    public void onCompletion(PlayerEngine engine) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                System.out.println("onCompletion()");
                basicHandler.removeMessages(PREVIEW_UPDATE_SEEKBAR);
                //暂停方法
                pauseAudio();
                isPlay = false;
                isStartPlay = false;
                changePlayControlView();

                if (playerEngine != null) {
                    playerEngine.reset();
                }
                isCreatePlayerEngine = false;
                currentDuration = currentSeekDuration = 0;
                initMediaPlayer();
                sbProgress.setProgress((int) currentDuration);
                setCurrentTimeTV(currentDuration);
//                mDrawWaveView.relsetView();
            }
        });
    }

    @Override
    public boolean onError(PlayerEngine engine, int i, int i1) {
        return false;
    }

    @Override
    public boolean onInfo(PlayerEngine engine, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(final PlayerEngine engine) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isStartPlay = false;
                //文件总数长
                maxDuratioin = engine.getDuration();
                //默认显示总时长
                mTotalTime = TimeUtils.converToms(maxDuratioin);
                tvAudioTimeSum.setText(getString(R.string.audio_preview_play_time_sum, mTotalTime));
                //设置进度条
                sbProgress.setMax((int) maxDuratioin);

                System.out.println("maxDuratioin = " + maxDuratioin);
            }
        });
    }

    @Override
    public void onSeekComplete(PlayerEngine engine) {

    }

    private void setCurrentTimeTV(long currentDuration) {
        tvAudioTime.setText(getString(R.string.audio_preview_play_time, TimeUtils.converToms(currentDuration)));
    }
}
