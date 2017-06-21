package com.tingtingfm.cbb.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.bean.ManuscriptInfo;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.utils.NetUtils;
import com.tingtingfm.cbb.ui.view.RecordLayoutView;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 查看批注管理页面
 * @author liming
 */
public class ApprovalActivity extends BaseActivity implements RecordLayoutView.OnViewAudioStatusListener{

    @BindView(R.id.rlv_record)
    RecordLayoutView mRecordView;
    @BindView(R.id.start_approval_text)
    EditText etApprovalContent;
    @BindView(R.id.btn_approval_text)
    Button btnApprovalText;
    @BindView(R.id.btn_clear_record)
    Button btnRecordDelete;
    @BindView(R.id.btn_approval_pass)
    Button btnApprovalPass;
    @BindView(R.id.btn_approval_back)
    Button btnApprovalBack;
    @BindView(R.id.start_approval_text_num)
    TextView tvApprovalTextCount;
    private boolean isStartRecord = false;
    private ManuscriptInfo mManuscriptInfo;

    @Override
    protected View initContentView() {
        return getContentView(R.layout.activity_start_approval);
    }

    @Override
    protected void handleCreate() {
        initRecordView();
        setCenterViewContent(R.string.main_approval);
        //获得稿件对象
        mManuscriptInfo = (ManuscriptInfo) getIntent().getSerializableExtra(Constants.KEY_MANUSCRIPT_INFO);
        //设置文本初始值
        tvApprovalTextCount.setText(getString(R.string.manuscript_approval_text_count,0));
        //引用添加监听器方法
        addListener();
    }

    /**
     * 添加监听器方法
     */
    private void addListener() {
        //监听文本内容
        etApprovalContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO: 2017/6/15 获得文本字数
                if((start + count) <= 200){
                    //设置文本显示字数
                    tvApprovalTextCount.setText(getString(R.string.manuscript_approval_text_count,(start + count)));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        btnApprovalText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //显示提示
                showToast(R.string.manuscript_approval_toast_content);
            }
        });
    }

    @Override
    protected void processMessage(Message msg) {

    }

    @OnClick(R.id.btn_clear_record)
    public void clearRecord(View view) {
        mRecordView.clearAuido();
    }

    @OnClick({R.id.btn_approval_pass,R.id.btn_approval_back})
    public void controlGotoActivity(View view){
        if(!NetUtils.isNetConnected()){
            showToast(R.string.login_not_net);
            return ;
        }else if(isStartRecord){
            showToast(R.string.manuscript_approval_toast_content);
            return;
        }
        switch (view.getId()){
            case R.id.btn_approval_pass:
                showGotoActivityDialog(R.string.manuscript_approval_dialog_message_btn_pass,1);
                break;
            case R.id.btn_approval_back:
                showGotoActivityDialog(R.string.manuscript_approval_dialog_message_btn_back,0);
                break;
        }
    }

    private void showGotoActivityDialog(int resId, final int type){
        showTwoButtonDialog(true, getString(R.string.dialog_title_default), getString(resId), null, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showToast("Duration:"+mRecordView.getMediaDuration());
                //跳转提交审批页面
//                gotoApprovalProcessActivity(type);
            }
        });
    }

    /**
     * 跳转提交审批页面
     * @param type
     */
    private void gotoApprovalProcessActivity(int type) {
        //初始化意图
        Intent intent = new Intent();
        //稿件对象
        intent.putExtra(Constants.KEY_MANUSCRIPT_INFO,mManuscriptInfo);
        //时长
        intent.putExtra(Constants.KEY_APPROVATE_AUDIO_TIME,0L);
        //获得文本审批
        String _content = etApprovalContent.getText().toString().trim();
        //判断文本审批是否为Null
        if(!TextUtils.isEmpty(_content)){
            //添加文本审批内容
            intent.putExtra(Constants.KEY_APPROVATE_TEXT_CONTENT,_content);
        }
        //判断录音文件是否存在
        if(mRecordView.isHaveFile()){
            //添加路径
            intent.putExtra(Constants.KEY_APPROVATE_AUDIO_PATH,mRecordView.getFilePath());
            //添加时长
            intent.putExtra(Constants.KEY_APPROVATE_AUDIO_TIME,mRecordView.getMediaDuration());
        }
        switch (type){
            case 1://通过
                intent.putExtra(Constants.KEY_ACTION_TYPE,1);
                break;
            case 0://退回
                intent.putExtra(Constants.KEY_ACTION_TYPE,1);
                break;
        }
        startActivity(intent);
    }

    /**
     * 初始化控件配置
     */
    private void initRecordView() {
        RecordLayoutView.Builder builder = new RecordLayoutView.Builder()
                .setFileName("audio110101.m4a")
                .setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                .setOutPutFormat(MediaRecorder.OutputFormat.MPEG_4)
                .setOnViewAudioStatusListener(this)
                .setPath(Environment.getExternalStorageDirectory().getPath());
        mRecordView.setOption(builder);
        //清除文本信息
        mRecordView.deleteFile();
    }

    @Override
    public void currentViewStatus(RecordLayoutView.FunctionalStatus status) {
        switch (status){
            case CLEAR_AUDIO_STATUS:
                //清除音频
                Log.i("info","StartApprovalActivity==>CLEAR_AUDIO_STATUS");
                btnRecordDelete.setVisibility(View.GONE);
                break;
            case REOCRD_STATUS:
                //录音状态
                Log.i("info","StartApprovalActivity==>REOCRD_STATUS");
                isStartRecord = true;
                btnApprovalText.setVisibility(View.VISIBLE);
                break;
            case PLAY_STATUS:
                //播放状态——显示删除按钮
                Log.i("info","StartApprovalActivity==>PLAY_STATUS");
                isStartRecord = false;
                btnApprovalText.setVisibility(View.GONE);
                btnRecordDelete.setVisibility(View.VISIBLE);
                break;
            case LOAD_AUDIO_FAILURE:
                //加载失败
                showToast(R.string.manuscript_approval_audio_faile);
                break;
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        onLeftView1Click();
    }

    @Override
    protected void onLeftView1Click() {
        //收取软键盘
        hideSoftInput(mLeftView1);
        //判断问价是否正在录音
        if(isStartRecord){
            //正在录音提示
            showToast(R.string.manuscript_approval_toast_content);
        }else{
            //没有录音或者停止录音
            if(mRecordView.isHaveFile() || !TextUtils.isEmpty(etApprovalContent.getText().toString())){
                //提示
                showTwoButtonDialog(true, getString(R.string.dialog_title_default), getString(R.string.manuscript_approval_dialog_message_back), null, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        if(mRecordView.isHaveFile()){
                            mRecordView.clearAuido();
                        }
                    }
                });
            }else{
                //判断文本批注和语音批注是否存在
                super.onLeftView1Click();
            }
        }
    }
}
