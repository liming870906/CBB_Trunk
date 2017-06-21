package com.tingtingfm.cbb.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.tingtingfm.cbb.R;

/**
 * Created by lqsir on 2017/1/19.
 */

public class UploadLoadView extends FrameLayout{
    ImageView mImageView;
    Context context;

    private int noUploadId;
    private int uploadSuccessId;
    private int uploadFailId;
    private int uploadingId;
    private Animation operatingAnim;


    public UploadLoadView(Context context) {
        super(context);
        initView(context);
    }

    public UploadLoadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public UploadLoadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        this.context = context;
        View.inflate(context, R.layout.view_upload_load, this);
        mImageView = (ImageView) findViewById(R.id.view_upload_load_main);

        noUploadId = R.drawable.audio_preview_upload;
        uploadSuccessId = R.drawable.audio_preview_upload_sucess;
        uploadFailId = R.drawable.audio_preview_upload_failure;
        uploadingId = R.drawable.audio_preview_uploading;

        initAnim(context);
    }

    private void initAnim(Context context) {
        operatingAnim = AnimationUtils.loadAnimation(context, R.anim.rotate);
        LinearInterpolator lin = new LinearInterpolator();
        operatingAnim.setInterpolator(lin);
    }

    public void setUploadStatus(int status) {
        mImageView.clearAnimation();
        switch (status) {
            case 1:
                mImageView.setImageResource(noUploadId);
                break;
            case 2:
                mImageView.setImageResource(uploadSuccessId);
                break;
            case 3:
                mImageView.setImageResource(uploadFailId);
                break;
            case 4:
                mImageView.setAnimation(operatingAnim);
                mImageView.setImageResource(uploadingId);
                break;
            default:
                mImageView.setImageResource(noUploadId);
                break;
        }
    }

}
