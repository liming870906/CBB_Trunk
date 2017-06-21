package com.tingtingfm.cbb.ui.activity;

import android.content.Intent;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.bean.MediaInfo;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.configuration.GlobalVariableManager;
import com.tingtingfm.cbb.common.db.DBAudioRecordManager;
import com.tingtingfm.cbb.common.helper.LocationHelper;
import com.tingtingfm.cbb.common.upload.UploadManager;
import com.tingtingfm.cbb.common.utils.FileUtils;
import com.tingtingfm.cbb.common.utils.NetUtils;
import com.tingtingfm.cbb.ui.view.SpaceEditText;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;

public class AudioInformationActivity extends BaseActivity {
    @BindView(R.id.et_audio_info_audio_name)
    SpaceEditText etAudioName;
    @BindView(R.id.et_audio_info_caifang_event)
    SpaceEditText etCFEvent;
    @BindView(R.id.et_audio_info_caifang_person)
    SpaceEditText etCFPersion;
    @BindView(R.id.et_audio_info_keyword)
    SpaceEditText etKeyword;
    @BindView(R.id.tv_audio_info_audion_name_count)
    TextView tvNameCount;
    @BindView(R.id.tv_audio_info_caifang_event_count)
    TextView tvEventCount;
    @BindView(R.id.tv_audio_info_caifang_person_count)
    TextView tvPersonCount;
    @BindView(R.id.tv_audio_info_keyword_count)
    TextView tvKeywordCount;
    private String mOldAudioName, mNewAudioName, mCFPerson, mCFEvent, mKeyword, mAudioPath;
    private int mGotoType;
    private int mAudioID;
    private String mMP3 = ".mp3";
    //音频文件夹
    private static final String TAG = "AudioInformationActivity=====>";
    //获得经纬度

    private double mLongitude, mLatitude;
    //地址详情
    private String mPlace = "";
    private int mMediaID;
    private boolean isSame;
    private MediaInfo mMediaInfo;

    @Override
    protected View initContentView() {
        return LayoutInflater.from(this).inflate(R.layout.activity_audio_information, null);
    }

    @Override
    protected void handleCreate() {
        //开启定位
        LocationHelper.getInstance().getLocationClient().start();

        //初始化数据
        initData();
        //改变标题信息及设置显示标题按钮
        showTitleLayout();
        //初始化内容显示控件
        infoShowContentView();
        //添加监听器方法
        addListener();
    }

    private void addListener() {
        //添加改变数据监听器
        etAudioName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                setEditTextContent(etAudioName);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String name = etAudioName.getText().toString();
                tvNameCount.setText(String.valueOf(18 - name.length()));
            }
        });
        etCFEvent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                setEditTextContent(etCFEvent);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String event = etCFEvent.getText().toString();
                tvEventCount.setText(String.valueOf(100 - event.length()));
            }
        });
        etCFPersion.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                setEditTextContent(etCFPersion);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String person = etCFPersion.getText().toString();
                tvPersonCount.setText(String.valueOf(18 - person.length()));
            }
        });
        etKeyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                setEditTextContent(etKeyword);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String keyword = etKeyword.getText().toString();
                tvKeywordCount.setText(String.valueOf(18 - keyword.length()));
            }
        });
    }

    private void setEditTextContent(EditText etView) {
        String editable = etView.getText().toString().trim();
        String str = stringFilter(editable.toString()).trim();
        if (!editable.equals(str)) {
            etView.setText(str);
            //设置新的光标所在位置
            etView.setSelection(str.length());
        }
    }

    /**
     * 初始化内容系那是界面
     */
    private void infoShowContentView() {
        //设置文件名称显示
        etAudioName.setText(mOldAudioName);
        etCFEvent.setText(mCFEvent);
        etCFPersion.setText(mCFPerson);
        etKeyword.setText(mKeyword);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        //获得进入页面类型
        mGotoType = getIntent().getIntExtra(Constants.GOTO_TYPE_KEY, Constants.GOTO_TYPE_RECORD);
        //获得文件名称
        mAudioPath = getIntent().getStringExtra(Constants.AUDIO_INFO_AUDIO_FILE_PATH_KEY);
        mMediaInfo = getIntent().getParcelableExtra(Constants.MEDIA_AUDIO_KEY);
        if(mMediaInfo != null){
            mOldAudioName = mMediaInfo.getTitle();
            mCFPerson = mMediaInfo.getInterview_persion();
            mCFEvent = mMediaInfo.getInterview_event();
            mKeyword = mMediaInfo.getInterview_keyword();
            mAudioID = mMediaInfo.getId();
            mMediaID = mMediaInfo.getMedia_id();
        }
//        mOldAudioName = getIntent().getStringExtra(Constants.AUDIO_INFO_AUDIO_NAME_KEY);
//        mCFPerson = getIntent().getStringExtra(Constants.AUDIO_INFO_CF_PERSON_KEY);
//        mCFEvent = getIntent().getStringExtra(Constants.AUDIO_INFO_CF_EVENT_KEY);
//        mKeyword = getIntent().getStringExtra(Constants.AUDIO_INFO_AUDIO_KEYWORD_KEY);
//        mAudioID = getIntent().getIntExtra(Constants.AUDIO_INFO_AUDIO_ID_KEY, -1);
        if (!TextUtils.isEmpty(mOldAudioName)) {
            tvNameCount.setText(String.valueOf(18 - mOldAudioName.length()));
        } else {
            tvNameCount.setText("18");
        }
        if (!TextUtils.isEmpty(mCFPerson)) {
            tvPersonCount.setText(String.valueOf(18 - mCFPerson.length()));
        } else {
            tvPersonCount.setText("18");
        }
        if (!TextUtils.isEmpty(mCFEvent)) {
            tvEventCount.setText(String.valueOf(100 - mCFEvent.length()));
        } else {
            tvEventCount.setText("100");
        }
        if (!TextUtils.isEmpty(mKeyword)) {
            tvKeywordCount.setText(String.valueOf(18 - mKeyword.length()));
        } else {
            tvKeywordCount.setText("18");
        }
    }

    /**
     * 设置标题信息
     */
    private void showTitleLayout() {
        switch (mGotoType) {
            case Constants.GOTO_TYPE_PREVIEW://音频预览页面进入
                setRightView3Visibility(View.VISIBLE);
                mLeftView1.setVisibility(View.GONE);
                mLeftView3.setVisibility(View.VISIBLE);
                setRightView3Content(R.string.audio_info_title_right_sure);
                setLeftView3Content(R.string.audio_info_title_left_cancel);
                setCenterViewContent(R.string.audio_info_preview_title);
                break;
            case Constants.GOTO_TYPE_RECORD://录音页面进入
                setRightView3Visibility(View.VISIBLE);
                setRightView3Content(R.string.audio_info_title_right_sure);
                mLeftView1.setVisibility(View.GONE);
                setCenterViewContent(R.string.audio_info_record_title);
                break;
        }
    }

    @Override
    protected void processMessage(Message msg) {

    }

    @Override
    protected void onLeftView3Click() {
        super.onLeftView3Click();
        //取消点击按钮
    }

    @Override
    protected void onRightView3Click() {
        super.onRightView3Click();
        //确定点击按钮
        //判断文件名称是否为null
        mNewAudioName = etAudioName.getText().toString().trim();
        if (TextUtils.isEmpty(mNewAudioName)) {
            showToast(R.string.audio_info_audio_name_tips);
            return;
        }
        isSame = isDataSame();
        mCFPerson = etCFPersion.getText().toString().trim();
        mCFEvent = etCFEvent.getText().toString().trim();
        mKeyword = etKeyword.getText().toString().trim();
        // TODO: 17/1/19 存储数据
        saveAudioFile();
    }

    private boolean isDataSame(){
        boolean b1 = isBoolean(mOldAudioName,mNewAudioName);
        boolean b2 = isBoolean(mCFPerson,etCFPersion.getText().toString().trim());
        boolean b3 = isBoolean(mCFEvent,etCFEvent.getText().toString().trim());
        boolean b4 = isBoolean(mKeyword,etKeyword.getText().toString().trim());
        if(b1 && b2 && b3 && b4){
            return true;
        }
        return false;
    }

    private boolean isBoolean(String s1, String s2){
        boolean b1 = TextUtils.isEmpty(s1);
        boolean b2 = TextUtils.isEmpty(s2);
        if(!b1){
            return s1.equals(s2);
        }else if(!b2){
            return s2.equals(s1);
        }else{
            return true;
        }
    }
    /**
     * 保存音频
     */
    private void saveAudioFile() {
        if(mMediaID > 0 && isSame){
            showToast(R.string.audio_info_save_toast_tips);
            finish();
            return ;
        }
        if (mOldAudioName.equals(mNewAudioName)) {
            updateAudioInfoDB();
            //判断保存是否成功
            feedbackInfo();
            //提示
            showToast(R.string.audio_info_save_toast_tips);
            //关闭页面
            finish();
            return;
        }
        //判断文件是否存在
        boolean _isExists = FileUtils.isExistsFile(mAudioPath + File.separator + mNewAudioName + mMP3);
        //文件不存在
        if (!_isExists) {
            //保存文件
            saveRecordAudio();
            //判断保存是否成功
            feedbackInfo();
            //提示
            showToast(R.string.audio_info_save_toast_tips);
            //关闭窗口
            finish();
        } else {
            //提示——录音保存失败
            showToast(R.string.audio_preview_file_save_tips);
        }
    }

    /**
     * 反馈信息
     */
    private void feedbackInfo() {
        if (mGotoType == Constants.GOTO_TYPE_PREVIEW) {
            Intent intent = new Intent();
            intent.putExtra(Constants.AUDIO_INFO_AUDIO_NAME_KEY, mNewAudioName);
            intent.putExtra(Constants.AUDIO_INFO_CF_PERSON_KEY, mCFPerson);
            intent.putExtra(Constants.AUDIO_INFO_CF_EVENT_KEY, mCFEvent);
            intent.putExtra(Constants.AUDIO_INFO_AUDIO_KEYWORD_KEY, mKeyword);
            setResult(RESULT_OK, intent);
        }
    }

    private boolean saveRecordAudio() {
        File _file = new File(mAudioPath + File.separator + mOldAudioName + mMP3);
        boolean isSave = _file.renameTo(new File(mAudioPath + File.separator + mNewAudioName + mMP3));
        //判断是否存储成功
        if (isSave) {
            //添加录音音频数据到数据库
            updateAudioInfoDB();
        }
        return isSave;
    }

    /**
     * 更新音频信息
     */
    private void updateAudioInfoDB() {
        if (mGotoType == Constants.GOTO_TYPE_RECORD) {
            BDLocation location = LocationHelper.getInstance().getBDLocation();
            if (location != null) {
                mLongitude = location.getLongitude();
                mLatitude = location.getLatitude();
                mPlace = getAddress(location.getAddrStr());
            }

            DBAudioRecordManager.getInstance(this).updateAudioRecordInfo(mAudioID,
                    mNewAudioName,
                    mCFPerson,
                    mCFEvent,
                    mKeyword,
                    mAudioPath + File.separator + mNewAudioName + mMP3,
                    mLongitude,
                    mLatitude,
                    mPlace,
                    0);
        } else {
            DBAudioRecordManager.getInstance(this).updateAudioRecordInfo(mAudioID,
                    mNewAudioName,
                    mCFPerson,
                    mCFEvent,
                    mKeyword,
                    mAudioPath + File.separator + mNewAudioName + mMP3,
                    1);
        }
        MediaInfo _mediaInfo = DBAudioRecordManager.getInstance(this).queryAudioRecord(mAudioID);
        if (_mediaInfo != null) {
            long fileSize = Math.abs(_mediaInfo.getSize());
            int maxFileSize = GlobalVariableManager.MAXWIFIFILESIZE;
            int netStatus = NetUtils.getNetConnectType();
            if (GlobalVariableManager.isOpen100 && netStatus == 2) {
                maxFileSize = GlobalVariableManager.MAX4GFILESIZE;
            }

            //文件未上传，直接上传
            //文件已上传，根据条件判断能否上传
            if (_mediaInfo.getUpload_status() == Constants.UPLOAD_STATUS_SUCCESS
                    || fileSize <= maxFileSize) {
                //添加现在队列
                UploadManager.getInstance().addUpload(_mediaInfo);
            }
        }
    }

//    /**
//     * 判断文件是否存在
//     *
//     * @param fileName
//     * @return
//     */
//    private boolean isExistsFile(String fileName) {
//        File _file = new File(mAudioPath + File.separator + fileName);
//        if (_file.exists()) {
//            return true;
//        }
//        return false;
//    }


    /**
     * 系统返回按钮
     */
    @Override
    public void onBackPressed() {
        if (mGotoType == Constants.GOTO_TYPE_PREVIEW) {
            onLeftView3Click();
        }
    }

    /**
     * 过滤图标字符串
     *
     * @param string
     * @return
     */
    public String stringFilter(String string) {
        Pattern p = Pattern.compile("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
                Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(string);
        return m.replaceAll("").trim();
    }

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
}
