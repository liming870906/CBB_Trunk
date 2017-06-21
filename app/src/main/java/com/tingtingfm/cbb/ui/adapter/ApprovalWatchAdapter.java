package com.tingtingfm.cbb.ui.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.bean.ApprovalInfo;
import com.tingtingfm.cbb.bean.ApprovalWatchInfo;
import com.tingtingfm.cbb.common.utils.DisplayImageOptionsUtils;
import com.tingtingfm.cbb.common.utils.ToastUtils;
import com.tingtingfm.cbb.ui.activity.AbstractActivity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by tianhu on 2017/4/20.
 * 审批查看列表
 */

public class ApprovalWatchAdapter extends CommonAdapter<ApprovalWatchInfo> {

    //多媒体对象
    private MediaPlayer mPlayer;
    //缓存点击位置是否选中
    private Map<Integer, Boolean> cache;
    //旋转动画
    private Animation mRotateAnim;
    //设置初始化位置
    private int position = -1;
    //记录控件标记
    private final String tag = "com.tingtingfm.cbb.approval.common.adapter_position:";
    //下滑刷新容器
    private PullToRefreshListView mListView;

    /**
     * 初始化方法
     * @param context 上下文
     * @param layoutId  布局ID
     * @param listview  容器控件对象
     */
    public ApprovalWatchAdapter(Context context, int layoutId,PullToRefreshListView listview) {
        super(context, layoutId);
        //初始化缓存对象
        cache = new HashMap<>();
        mListView = listview;
        //声明旋转动画
        mRotateAnim = AnimationUtils.loadAnimation(context,R.anim.rotate);
        //添加循环标准——匀速
        mRotateAnim.setInterpolator(new LinearInterpolator());
    }

    @Override
    public void covert(final ViewHolder holder, final ApprovalWatchInfo messageInfo) {
        //添加缓存
        cache.put(holder.getPosition(), false);
        //获得头像控件
        ImageView ivIcon = holder.getView(R.id.watch_img);
        //头像设置
        DisplayImageOptionsUtils.getInstance().displaySetImage(messageInfo.getWatchInfo().getUrl(), ivIcon, true);
        //设置名称
        holder.setText(R.id.watch_name, messageInfo.getWatchInfo().getApprovalName());
        //设置时间
        holder.setText(R.id.watch_approval_time, messageInfo.getWatchInfo().getOperationTime());
        //获得显示文本控件
        TextView approvalText = holder.getView(R.id.watch_approval_text);
        //判断文本是否为Null
        if (!TextUtils.isEmpty(messageInfo.getWatchInfo().getApprovalText())) {
            //显示文本控件
            approvalText.setVisibility(View.VISIBLE);
            //设置文本内容
            approvalText.setText(messageInfo.getWatchInfo().getApprovalText());
        } else {
            //隐藏文本控件
            approvalText.setVisibility(View.GONE);
        }
        //获得语音控件
        RelativeLayout audioLayout = holder.getView(R.id.watch_audio_rlayout);
        //获得语音时长控件
        TextView audioTime = holder.getView(R.id.watch_audio_time);
        //语音播放标记控件
        final ImageView audioImg = holder.getView(R.id.watch_approval_img1);
        //缓存控件
        audioImg.setTag(tag+holder.getPosition());
        //判断播放音频内容是否为null
        if (!TextUtils.isEmpty(messageInfo.getWatchInfo().getAudioUrl())) {
            //显示语音控件
            audioLayout.setVisibility(View.VISIBLE);
            //设置时长
            audioTime.setText(messageInfo.getWatchInfo().getAudioTime() + "''");
        } else {
            //隐藏语音控件
            audioLayout.setVisibility(View.GONE);
        }
        //添加点击事件监听
        audioLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获得当前点击位置
                position = holder.getPosition();
                //引用关闭其他动画方法
                closeOtherViewAnimation();
                try {
                    //判断是否点击的缓存
                    if (!cache.get(position)) {
                        //设置点击标记
                        cache.put(position, true);
                        //设置旋转图片
                        audioImg.setImageResource(R.drawable.common_rotate);
                        //开启旋转动画
                        audioImg.startAnimation(mRotateAnim);
                        //关闭播放
                        stopPlayer();
                        //开始播放
                        startPlayer(messageInfo, audioImg);
                    } else {
                        //设置取消标记
                        cache.put(position, false);
                        //关闭播放
                        stopPlayer();
                        //设置默认图片
                        audioImg.setImageResource(R.drawable.approval_audio3);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //待审状态，“等待一审，等待二审”
        TextView textView = holder.getView(R.id.watch_approval_state);
        String str = null;
        if (messageInfo.getWatchInfo().getApprovalLayer() == 1) {
            str = context.getString(R.string.manuscript_approval_one);
        } else if (messageInfo.getWatchInfo().getApprovalLayer() == 2) {
            str = context.getString(R.string.manuscript_approval_two);
        } else if (messageInfo.getWatchInfo().getApprovalLayer() == 3) {
            str = context.getString(R.string.manuscript_approval_three);
        }
        if (messageInfo.getWatchInfo().getApprovalOperation() == 0) {
            str = context.getString(R.string.manuscript_approvalback, str);
            textView.setTextColor(context.getResources().getColor(R.color.color_da4453));
        } else if (messageInfo.getWatchInfo().getApprovalOperation() == 1) {
            str = context.getString(R.string.manuscript_approvalok, str);
            textView.setTextColor(context.getResources().getColor(R.color.color_37bc9b));
        }
        textView.setText(str);
    }

    /**
     * 开始播放
     * @param messageInfo  实体对象
     * @param audioImg  多媒体控件
     * @throws IOException
     */
    private void startPlayer(ApprovalWatchInfo messageInfo, final ImageView audioImg) throws IOException {
        //声明多媒体对象
        mPlayer = new MediaPlayer();
        //设置多媒体资源数据
        mPlayer.setDataSource(context, Uri.parse(messageInfo.getWatchInfo().getAudioUrl()));
        //设置错误监听器
        mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                //停止动画
                audioImg.clearAnimation();
                //设置默认图片
                audioImg.setImageResource(R.drawable.approval_audio3);
                //加载失败提示
                ToastUtils.showToast(context, context.getString(R.string.manuscript_approval_audio_faile));
                //引用关闭其他动画方法
                closeOtherViewAnimation();
                return false;
            }
        });
        //设置准备监听器
        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //关闭动画
                audioImg.clearAnimation();
                //加载完成播放
                audioImg.setImageResource(R.drawable.watch_audio_animlist);
                //获得帧动画对象
                AnimationDrawable animationDrawable = (AnimationDrawable) audioImg.getDrawable();
                //开启多媒体音乐
                mPlayer.start();
                //开启动画
                animationDrawable.start();
            }
        });
        //异步准备方法
        mPlayer.prepareAsync();
    }

    /**
     * 停止媒体资源，并释放
     */
    public void stopPlayer() {
        //判断多媒体对象是否存在，是否播放
        if (mPlayer != null && mPlayer.isPlaying()) {
            //停止播放
            mPlayer.stop();
            //释放资源
            mPlayer.release();
            //设置空
            mPlayer = null;
        }
    }

    /**
     * 关闭播放之外的其他控件动画
     */
    private void closeOtherViewAnimation() {
        Iterator<Map.Entry<Integer,Boolean>> iterator = cache.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<Integer,Boolean> entry = iterator.next();
            int _key = entry.getKey();
            if(position != _key){
                cache.put(_key,false);
                ImageView _ivAudio = (ImageView) mListView.findViewWithTag(tag+_key);
                if(_ivAudio!= null){
                    _ivAudio.clearAnimation();
                    _ivAudio.setImageResource(R.drawable.approval_audio3);
                }
            }
        }
    }
}