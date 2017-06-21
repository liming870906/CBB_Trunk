package com.tingtingfm.cbb.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.common.utils.DensityUtils;
import com.tingtingfm.cbb.common.utils.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * 录音布局
 * Created by liming on 17/4/13.
 */

public class RecordLayoutView extends RelativeLayout {
    //左侧图片控件
    private ImageView ivLeftIcon;
    //左侧文本，中间文本控件
    private TextView tvLeftContent, tvCenterContent;
    //场景对象
    private Context context;
    //枚举对象
    private ClickStatus mClickStatus;
    //录音对象
    private MediaRecorder mRecorder;
    //日志标记
    private static final String LOG_TAG = "RecordLayoutView";
    //音频播放控件
    private MediaPlayer mPlayer;
    //构造器对象
    private Builder mBuilder;
    //更新时间线程
    private TimerThread mTimerThread;
    //实现记录标记
    private int mTimerCount;
    //线程间通讯对象
    private CallBackHandler mHalder;
    //动画
    private AnimationDrawable mDrawable;
    //通讯标记
    private static final int STOP_REOCRD_AUDIO_TAG = 0X20001;
    private static final int UPDATE_RECORD_TIME_TAG = 0X20002;

    /**
     * 按钮状态
     */
    enum ClickStatus {
        START_RECORD, STOP_RECORD, PLAY_AUDIO, PAUSE_AUDIO;
    }

    public enum FunctionalStatus {
        CLEAR_AUDIO_STATUS, REOCRD_STATUS, PLAY_STATUS, LOAD_AUDIO_FAILURE
    }

    /**
     * 构造方法
     *
     * @param context
     */
    public RecordLayoutView(Context context) {
        this(context, null);
    }

    /**
     * 构造方法
     *
     * @param context
     * @param attrs
     */
    public RecordLayoutView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * 构造方法
     *
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public RecordLayoutView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //获得上线文对象；
        this.context = context;
        //设置初始化方法
        init();
    }

    /**
     * 初始化方法
     */
    private void init() {
        //声明线程通讯对象
        mHalder = new CallBackHandler();
        //初始化计时数据
        mTimerCount = 0;
        //设置初始化状态标记
        this.mClickStatus = ClickStatus.START_RECORD;
        //设置控件背景资源
        this.setBackgroundResource(R.drawable.record_layout_view_shape);
        //初始化动画
        initAnimationDrawable();
        //初始化ImageView控件
        initImageView();
        //初始化左侧文本控件
        initLeftTextView();
        //初始化中间文本控件
        initCenterTextView();
        //添加到布局中
        this.addView(ivLeftIcon);
        this.addView(tvLeftContent);
        this.addView(tvCenterContent);
    }

    @SuppressLint("NewApi")
    private void initAnimationDrawable() {
        mDrawable = new AnimationDrawable();
        mDrawable.addFrame(getResources().getDrawable(R.drawable.play_record_01, null), 400);
        mDrawable.addFrame(getResources().getDrawable(R.drawable.play_record_02, null), 400);
        mDrawable.addFrame(getResources().getDrawable(R.drawable.play_record_03, null), 400);
        mDrawable.setOneShot(false);
    }

    /**
     * 初始化中央文本控件
     */
    private void initCenterTextView() {
        //声明控件
        tvCenterContent = new TextView(context);
        //设置控件大小
        LayoutParams _params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //设置对齐方式
        _params.addRule(ALIGN_PARENT_RIGHT, TRUE);
        _params.addRule(CENTER_VERTICAL, TRUE);
        _params.setMargins(0, 0, DensityUtils.dp2px(getContext(), 20.0f), 0);
        //设置图片大小
        tvCenterContent.setLayoutParams(_params);
        tvCenterContent.setTextSize(DensityUtils.px2sp(getContext(), 40.0f));
        tvCenterContent.setTextColor(Color.parseColor("#ff888888"));
        //设置文本
        tvCenterContent.setText("0''/120''");
    }

    private void initLeftTextView() {
        //声明控件
        tvLeftContent = new TextView(context);
        //设置控件大小
        LayoutParams _params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //设置对齐方式
        _params.addRule(RIGHT_OF, ivLeftIcon.getId());
        _params.addRule(CENTER_VERTICAL, TRUE);
        //设置文本大小
        tvLeftContent.setLayoutParams(_params);
        tvLeftContent.setTextSize(DensityUtils.px2sp(getContext(), 46.0f));
        tvLeftContent.setTextColor(Color.parseColor("#ff333333"));
        //设置文本
        tvLeftContent.setText("开始录音");
    }

    @SuppressLint("NewApi")
    private void initImageView() {
        //声明控件
        ivLeftIcon = new ImageView(context);
        //设置控件ID
        ivLeftIcon.setId(generateViewId());
        //设置控件大小
        LayoutParams _params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //设置对齐方式
        _params.addRule(ALIGN_PARENT_LEFT, TRUE);
        _params.addRule(CENTER_VERTICAL, TRUE);
        _params.setMargins(DensityUtils.dp2px(getContext(), 14.0f), 0, DensityUtils.dp2px(getContext(), 12.0f), 0);
        //设置图片大小
        ivLeftIcon.setLayoutParams(_params);
        //设置图片位置
        ivLeftIcon.setImageResource(R.drawable.start_record);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP://点击抬起
                //控制文本显示内容
                controlTextContent();
                break;
        }
        return true;
    }

    /**
     * 控制显示文本内容
     */
    private void controlTextContent() {
        switch (mClickStatus) {
            case START_RECORD://开始录音
                mClickStatus = ClickStatus.STOP_RECORD;
                setAudioStatusCallBack(FunctionalStatus.REOCRD_STATUS);
                tvLeftContent.setText("完成");
                ivLeftIcon.setImageResource(R.drawable.stop_record);
                onRecord(true);
                break;
            case STOP_RECORD://停止录音
                mClickStatus = ClickStatus.PLAY_AUDIO;
                setAudioStatusCallBack(FunctionalStatus.PLAY_STATUS);
                tvLeftContent.setVisibility(View.INVISIBLE);
                ivLeftIcon.setImageResource(R.drawable.play_record_03);
                onRecord(false);
                this.setBackgroundResource(R.drawable.rlv_audio_countrl_bg);
                mHalder.sendEmptyMessage(-1);
                break;
            case PLAY_AUDIO://播放音频
                mClickStatus = ClickStatus.PAUSE_AUDIO;
                initAnimationDrawable();
                ivLeftIcon.setImageDrawable(mDrawable);
                onPlay(true);
                mDrawable.start();
                break;
            case PAUSE_AUDIO://暂停音频
                mClickStatus = ClickStatus.PLAY_AUDIO;
                onPlay(false);
                mDrawable.setOneShot(true);
                break;
        }
    }

    /**
     * 开始录音
     *
     * @param start
     */
    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    /**
     * 播放录音
     *
     * @param start
     */
    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    /**
     * 开始录音
     */
    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(mBuilder.getmOutPutFormat());
        mRecorder.setOutputFile(mBuilder.getmPath() + File.separatorChar + mBuilder.getmFileName());
        mRecorder.setAudioEncoder(mBuilder.getmAudioEncoder());
        try {
            mRecorder.prepare();
            mRecorder.start();
            mTimerThread = new TimerThread();
            mTimerThread.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    /**
     * 停止录音
     */
    private void stopRecording() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
        if (mTimerThread != null) {
            mTimerThread.setCancle(true);
            mTimerThread = null;
        }
    }

    /**
     * 开始播放
     */
    private void startPlaying() {
        if (mPlayer == null) {
            initMediaPalyer();
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    //开始播放
                    mPlayer.start();
                }
            });
            mPlayer.prepareAsync();
        }
    }

    public long getMediaDuration() {
        if(isHaveFile()){
            try {
                MediaPlayer _player = new MediaPlayer();
                _player.setDataSource(mBuilder.getmPath() + File.separatorChar + mBuilder.getmFileName());
                _player.prepare();
                return _player.getDuration();
            } catch (IOException e) {
                e.printStackTrace();
                return 0 ;
            }
        }else{
           return 0 ;
        }
    }

    private void initMediaPalyer() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mBuilder.getmPath() + File.separatorChar + mBuilder.getmFileName());
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mClickStatus = ClickStatus.PLAY_AUDIO;
                    mDrawable.setOneShot(true);
                }
            });
            mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    setAudioStatusCallBack(FunctionalStatus.LOAD_AUDIO_FAILURE);
                    return false;
                }
            });
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    /**
     * 停止播放
     */
    public void stopPlaying() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    /**
     * 清除音频
     */
    public void clearAuido() {
        stopPlaying();
        mTimerCount = 0;
        //修改音频播放状态
        mClickStatus = ClickStatus.START_RECORD;
        //清除文件
        deleteFile();
        //设置控件背景资源
        this.setBackgroundResource(R.drawable.record_layout_view_shape);
        //修改文本信息
        //设置图片位置
        ivLeftIcon.setImageResource(R.drawable.start_record);
        tvLeftContent.setText("开始录音");
        tvCenterContent.setTextSize(DensityUtils.px2sp(getContext(), 40.0f));
        tvCenterContent.setTextColor(Color.parseColor("#888888"));
        tvCenterContent.setText("0''/120''");
        tvLeftContent.setVisibility(View.VISIBLE);
        tvCenterContent.setVisibility(View.VISIBLE);
        //清除音频
        setAudioStatusCallBack(FunctionalStatus.CLEAR_AUDIO_STATUS);
        //调用更新方法
        invalidate();
    }

    /**
     * 删除录音文件
     */
    public void deleteFile() {
        if (isHaveFile()) {
            File _file = new File(mBuilder.getmPath() + File.separator + mBuilder.getmFileName());
            if (_file.exists()) {
                _file.delete();
            }
        }
    }

    public boolean isHaveFile() {
        return FileUtils.isExistsFile(mBuilder.getmPath() + File.separator + mBuilder.getmFileName());
    }

    /**
     * 文件路径
     *
     * @return
     */
    public String getFilePath() {
        return mBuilder.getmPath() + File.separator + mBuilder.getmFileName();
    }

    /*
    * 内部Builder构建器
    * */
    public static class Builder {
        //音频文件路径
        private String mPath;
        //音频文件名称
        private String mFileName;
        //音频文件格式
        private int mOutPutFormat;
        //输出编码
        private int mAudioEncoder;
        //回调接口
        private OnViewAudioStatusListener mListener;

        /**
         * @param mPath
         * @return
         */
        public Builder setPath(String mPath) {
            this.mPath = mPath;
            return this;
        }

        public Builder setFileName(String mFileName) {
            this.mFileName = mFileName;
            return this;
        }

        public Builder setOutPutFormat(int format) {
            mOutPutFormat = format;
            return this;
        }

        public Builder setAudioEncoder(int encoder) {
            mAudioEncoder = encoder;
            return this;
        }

        public Builder setOnViewAudioStatusListener(OnViewAudioStatusListener listener) {
            mListener = listener;
            return this;
        }

        public String getmPath() {
            return mPath;
        }

        public String getmFileName() {
            return mFileName;
        }

        public int getmOutPutFormat() {
            return mOutPutFormat;
        }

        public int getmAudioEncoder() {
            return mAudioEncoder;
        }

        public OnViewAudioStatusListener getmListener() {
            return mListener;
        }
    }

    /**
     * 设置构造器对象
     *
     * @param builder
     */
    public void setOption(Builder builder) {
        this.mBuilder = builder;
    }

    /**
     * 计时器
     */
    class TimerThread extends Thread {
        //结束线程标记
        private boolean isCancle;

        /**
         * 构造方法
         */
        public TimerThread() {
            //默认初始状态
            isCancle = false;
        }

        @Override
        public void run() {
            while (!isCancle) {
                if (mTimerCount >= 120) {
                    //停止音频录制
                    mHalder.sendEmptyMessage(STOP_REOCRD_AUDIO_TAG);
                } else {
                    //更新控件显示秒数
                    mHalder.sendEmptyMessage(UPDATE_RECORD_TIME_TAG);
                }
                //更新时间
                mTimerCount++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 设置取消标记
         *
         * @param cancle
         */
        public void setCancle(boolean cancle) {
            isCancle = cancle;
        }
    }

    /**
     * 通讯类
     */
    class CallBackHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STOP_REOCRD_AUDIO_TAG:
                    //控制文本显示
                    controlTextContent();
                    tvCenterContent.setTextSize(DensityUtils.px2sp(getContext(), 46.0f));
                    tvCenterContent.setTextColor(Color.parseColor("#8e9396"));
                    //设置文本
                    tvCenterContent.setText(mTimerCount >= 120 ? "120''" : mTimerCount + "''");
                    break;
                case UPDATE_RECORD_TIME_TAG:
                    tvCenterContent.setTextSize(DensityUtils.px2sp(getContext(), 40.0f));
                    tvCenterContent.setTextColor(Color.parseColor("#888888"));
                    String _contentStr = "<font color='#333333'>" + mTimerCount + "''</font>/120''";
                    //设置文本
                    tvCenterContent.setText(Html.fromHtml(_contentStr));
                    break;
                default:
                    tvCenterContent.setTextSize(DensityUtils.px2sp(getContext(), 46.0f));
                    tvCenterContent.setTextColor(Color.parseColor("#8e9396"));
                    //设置文本
                    tvCenterContent.setText(mTimerCount >= 120 ? "120''" : mTimerCount + "''");
                    break;
            }
            //刷新控件
            invalidate();
        }
    }

    /**
     * 判断音乐是否播放，true播放，false停止
     *
     * @return
     */
    public boolean isPlaying() {
        if (mPlayer != null) {
            if (mPlayer.isPlaying())
                return true;
            else
                return false;
        } else {
            return false;
        }
    }

    public interface OnViewAudioStatusListener {
        void currentViewStatus(FunctionalStatus status);
    }

    /**
     * 设置音频状态回调方法
     *
     * @param status
     */
    public void setAudioStatusCallBack(FunctionalStatus status) {
        if (mBuilder != null && mBuilder.getmListener() != null) {
            mBuilder.getmListener().currentViewStatus(status);
        }
    }
}
