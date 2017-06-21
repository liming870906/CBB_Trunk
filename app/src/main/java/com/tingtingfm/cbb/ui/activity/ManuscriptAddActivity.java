package com.tingtingfm.cbb.ui.activity;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Build;
import android.os.Message;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.bean.ManuscriptAudioInfo;
import com.tingtingfm.cbb.bean.ManuscriptInfo;
import com.tingtingfm.cbb.bean.MediaInfo;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.configuration.GlobalVariableManager;
import com.tingtingfm.cbb.common.configuration.PreferencesConfiguration;
import com.tingtingfm.cbb.common.db.DBManuscriptManager;
import com.tingtingfm.cbb.common.log.TTLog;
import com.tingtingfm.cbb.common.utils.ManuscriptInterfaceUtils;
import com.tingtingfm.cbb.common.utils.NetUtils;
import com.tingtingfm.cbb.common.utils.ScreenUtils;
import com.tingtingfm.cbb.common.utils.StorageUtils;
import com.tingtingfm.cbb.common.utils.TimeUtils;
import com.tingtingfm.cbb.common.utils.ToastUtils;
import com.tingtingfm.cbb.ui.serve.ManuscriptServiceHelper;
import com.tingtingfm.cbb.ui.view.SpaceEditText;
import com.wasabeef.richeditor.AudiobackgrounUtil;
import com.wasabeef.richeditor.RichEditor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.OnClick;


public class ManuscriptAddActivity extends BaseActivity {

    @BindView(R.id.editor)
    RichEditor mEditor;
    @BindView(R.id.editor_onlyShow)
    RichEditor mEditorOnlyShow;
    @BindView(R.id.richeditor_title)
    SpaceEditText titleEditText;
    @BindView(R.id.richeditor_title_TextView)
    TextView titleTextView;
    @BindView(R.id.richeditor_auther)
    SpaceEditText autherEditText;
    @BindView(R.id.richeditor_auther_textView)
    TextView autherTextView;
    @BindView(R.id.richeditor_current_num)
    TextView numTextView;
    @BindView(R.id.richeditor_send)
    TextView sendTextView;
    @BindView(R.id.richeditro_sub_layout)
    LinearLayout subLayout;
    @BindView(R.id.main_saft)
    LinearLayout saftLayout;
    @BindView(R.id.richeditor_send_layout)
    LinearLayout sendLayout;
    @BindView(R.id.richeditor_title_hint)
    TextView titleHint;

    @BindView(R.id.richeditor_layout)
    LinearLayout bottomlayout;
    @BindView(R.id.richeditor_postilLayout)
    LinearLayout postilLayout;
    @BindView(R.id.richeditor_postilTxtv)
    TextView postilTextView;
    @BindView(R.id.richeditor_recall_layout)
    LinearLayout recallLayout;
    @BindView(R.id.richeditor_recall)
    TextView recallTextView;
    @BindView(R.id.richeditor_quickApprove_layout)
    LinearLayout quickApproveLayout;
    @BindView(R.id.richeditor_quickApprove)
    TextView quickApproveTextView;

    /**
     * 文本大小更新常量
     */
    private final int MSG_TEXT_NUM = 0X11110;
    /**
     * 文本2万常量
     */
    private final int MAX_TEXT_LEGNTH = 20000;
    /**
     * 添加音频常量
     */
    private final int SHOW_AUDIO_1 = 20001;
    /**
     * 定时器
     */
    private Timer timer;
    /**
     * 稿件数据库帮助类
     */
    private DBManuscriptManager dbManager;
    /**
     * html文本
     */
    private String currentHtmlText = "";
    /**
     * 纯文本
     */
    private String manuscriptText = "";
    /**
     * 稿件对象
     */
    private ManuscriptInfo currentObj;
    /**
     * 名字为空，默认为当前时间
     */
    private String timeStr;
    /**
     * 创建文件当前时间  20170112121316 (同为草稿音频缓存文件夹名)
     */
    private String createTime;
    /**
     * 音频缓存目录
     */
    private String saveAudioDir;
    /**
     * 是否编辑过
     */
    private boolean isEdit = false, contentIsModify = false;
    /**
     * 稿件服务通信帮助者
     */
    private ManuscriptServiceHelper serviceHelper;

    /**
     * 插入音频标识(false插入音频,true从音频预览页添加音频)
     */
    private boolean audioAddFlag = false;
    /**
     * 稿件是否为新建。(从搞件管理页进来，不是新建)
     */
    private boolean isHaveData = false;
    /**
     * 是否已提交
     */
    private boolean isRunSubmit = false;
    /**
     * 对话框显示，同时单击了返回，则删除当前数据。进行下一个数据
     */
    private boolean dialogShow;
    /**
     * 单击返回
     */
    private boolean isBack;
    /**
     * 当前焦点，区分标题，记者，文本区域
     */
    private int focusItem;
    /**
     * 粘贴板管理者
     */
    private ClipboardManager cb;
    /**
     * 长按标识
     */
    private static boolean longClick;
    /**
     * 是否有文本内容保存过
     */
    private boolean haveData = false;
    /**
     * html文本临时变量
     */
    private StringBuffer htmlText = null;
    /**
     * 插入音频信息集合
     */
    private ArrayList<ManuscriptAudioInfo> manuscriptAudioInfos = new ArrayList<ManuscriptAudioInfo>();
    /**
     * webView页面宽度
     */
    private int webViewWidth;
    /**
     * 保存当前上传状态
     */
    private int uploadState = -1;

    /**
     * 设置页面布局
     *
     * @return
     */
    @Override
    protected View initContentView() {
        return getContentView(R.layout.activity_richeditor);
    }

    /**
     * 初始界面UI,数据
     */
    @Override
    protected void handleCreate() {
        //软键盘，打开，关闭监听
        addOnSoftKeyBoardVisibleListener(this);
        dbManager = DBManuscriptManager.getInstance(this);
        setCurrentObjData();
        addTextChangeListener(titleEditText, 0);
        addTextChangeListener(autherEditText, 1);

        setOnfoucsChangeListener(titleEditText, 0);
        setOnfoucsChangeListener(autherEditText, 1);

        cb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        cb.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                synchronized (ManuscriptAddActivity.this) {
                    if (focusItem == 2 && null != cb.getPrimaryClip() && longClick) {
                        TTLog.e("longClick: " + longClick + "  ClipChanged");
                        longClick = false;
                        setClipText();
                    }
                }
            }
        });
        mEditorSetInitData();
        //获取webView显示的宽度
        webViewWidth = ScreenUtils.getScreenWidth() - ((int) getResources().getDimension(R.dimen.dp_13) * 2);
        setEditorActionListener();
    }

    /**
     * 设置焦点监听
     *
     * @param view
     * @param flag
     */
    private void setOnfoucsChangeListener(View view, final int flag) {
        view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    focusItem = flag;
                    if (focusItem == 2) {
                        saftLayout.setVisibility(View.VISIBLE);
                    } else {
                        saftLayout.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    /**
     * 只要显示界面，走此方法。进行页面数据更新。
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getRootLayout().setBackgroundColor(getResources().getColor(R.color.white));
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.color_697fb4));
        }
        serviceHelper = ManuscriptServiceHelper.getInstance(basicHandler);
        serviceHelper.bindService();
        serviceHelper.setPageFlag(0);
        setCenterViewContent(R.string.manuscript_temp);
        setClipText();
        pauseMusic();
        setViewShow();
    }

    /**
     * 设置界面下面按键显示
     */
    private void setViewShow() {
        if (currentObj != null) {
            switch (currentObj.getApproveState()) {
                case 0://未提审
                    sendTextView.setText(getString(R.string.manuscript_submit));
                    break;
                case 1://等待一审
                case 2://等待二审
                case 3://等待三审
                    sendLayout.setVisibility(View.GONE);
                    bottomlayout.setVisibility(View.VISIBLE);
                    postilLayout.setVisibility(View.VISIBLE);
                    recallLayout.setVisibility(View.VISIBLE);
                    switch (currentObj.getApproveState()) {
                        case 1://撤回
                            break;
                        case 2://不可撤回,样式为灰色
                            setViewTextColorAndBackground(recallTextView, recallLayout);
                            break;
                        case 3://不可撤回,样式为灰色
                            setViewTextColorAndBackground(recallTextView, recallLayout);
                            break;
                    }
                    break;
                case 4://终审完成(已审)
                    sendTextView.setText(getString(R.string.manuscript_look));
                    break;
                case 5://审批退回
                    sendLayout.setVisibility(View.GONE);
                    bottomlayout.setVisibility(View.VISIBLE);
                    postilLayout.setVisibility(View.VISIBLE);
                    recallLayout.setVisibility(View.VISIBLE);//发记者稿库
                    recallTextView.setText(getString(R.string.manuscript_submit));
                    break;
                case 6://待审
                    sendLayout.setVisibility(View.GONE);
                    bottomlayout.setVisibility(View.VISIBLE);
                    postilLayout.setVisibility(View.VISIBLE);
                    recallLayout.setVisibility(View.VISIBLE);//我要改稿
                    recallTextView.setText(getString(R.string.manuscript_myModify));
                    quickApproveLayout.setVisibility(View.VISIBLE);//快速审批
                    quickApproveTextView.setText(getString(R.string.manuscript_quickApprove));
                    break;
            }
        }
    }

    /**
     * 设置文字为灰色,layout背景为灰色。
     *
     * @param view1
     */
    private void setViewTextColorAndBackground(View view1, View view2) {
        if (view1 instanceof TextView) {
            ((TextView) view1).setTextColor(getResources().getColor(R.color.color_b7bcc3));
        }
        if (view2 instanceof LinearLayout) {
            view2.setBackground(getResources().getDrawable(R.drawable.manuscript_postil_background1));
        }
    }

    /**
     * 将剪切板内容改变纯文本。(稿件需要：网页复制内容只能是纯文本，不含图片等)
     */
    private void setClipText() {
        if (null != cb.getPrimaryClip() && null != cb.getPrimaryClip().getItemAt(0)) {
            String text = getShowText(cb.getPrimaryClip().getItemAt(0).getText().toString());
            ClipData clip = ClipData.newHtmlText("simpletext", text, text);
            cb.setPrimaryClip(clip);
        }
    }


    /**
     * 处理网页复制过文本，特殊符号进行转换。
     *
     * @param text
     * @return
     */
    private String getShowText(String text) {
        return text.replace("\n", "<br>").replace("\r", "<br>").replace("&gt", ">").replace("&lt", "<");
    }

    /**
     * 页面消失运行生命周期
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (null != serviceHelper)
            serviceHelper.setManuscriptId(0);
        longClick = false;
        hideSoftInput(mEditor);
    }

    /**
     * 开定时器，数据每5秒进行保存数据
     *
     * @param frist
     */
    private void openTimer(boolean frist) {
        if (null == timer) {
            int openTime = 0;
            if (frist) {
                openTime = 5000;
            } else {
                openTime = 10;
            }
            timer = new Timer();
            timer.schedule(new MyTask(), openTime, 5000);
        }
    }

    /**
     * 设置当前已有的数据信息
     */
    private void setCurrentObjData() {
        Intent intent = getIntent();
        ManuscriptInfo manusInfo = (ManuscriptInfo) intent.getSerializableExtra("manuInfo");

        if (null != manusInfo) {
            haveData = true;
            currentObj = manusInfo;
            titleEditText.setText(currentObj.getTitle());
            autherEditText.setText(currentObj.getAuther());
            //初始化文本字数
            if (!TextUtils.isEmpty(currentObj.getManuscriptText())) {
                numTextView.setText(String.valueOf(currentObj.getManuscriptText().length()));
            }
            currentHtmlText = currentObj.getHtmlText();
            TTLog.e("manuscriptAdd  get text : " + currentHtmlText);
            if (!TextUtils.isEmpty(currentHtmlText)) {
                mEditor.setHtmlText(currentHtmlText);
                mEditorOnlyShow.setHtmlText(currentHtmlText);
            } else {
                mEditor.setPlaceholder(getString(R.string.manuscript_input));
                mEditorOnlyShow.setHtmlText(getString(R.string.manuscript_input));
            }
            manuscriptText = currentObj.getManuscriptText();
            createTime = TimeUtils.getYMDHMS(currentObj.getCreateTime());
            createCacheDie();
            setSendBackground();
            isHaveData = true;
            if (currentObj.getIsSubmit() != 1) {
                setSubmitBackground();
            }
        } else {
            isHaveData = false;
            ArrayList<MediaInfo> mediaInfos = getIntent().getParcelableArrayListExtra("ObjList");
            if (null == mediaInfos) {
                mEditor.setPlaceholder(getString(R.string.manuscript_input));
            } else {
                audioAddFlag = true;
                saveAudioData(mediaInfos);
            }
            titleHint.setVisibility(View.VISIBLE);
            //设置记者名称
            autherEditText.setText(PreferencesConfiguration.getSValues(Constants.REAL_NAME));
            //初始化文本字数
            numTextView.setText(String.valueOf(0));
            setSubmitBackground();
        }
    }

    /**
     * 设置是否可以提审背景
     */
    private void setSendBackground() {
        if (null != currentObj) {
            if (currentObj.getIsSubmit() == 1) {
                sendLayout.setBackgroundResource(R.drawable.manuscript_send_background);
                textNoEdit(currentObj.getTitle(), currentObj.getAuther());
            } else {
                sendLayout.setBackgroundResource(R.drawable.b697fb4_526592_bg_selector);
            }
        }
    }

    /**
     * 标题，记者不能进行编辑
     *
     * @param title
     * @param auther
     */
    private void textNoEdit(String title, String auther) {
        mEditorOnlyShow.setVisibility(View.VISIBLE);
        mEditorOnlyShow.setFocusable(false);
        mEditor.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mEditorOnlyShow.getVisibility() == View.VISIBLE) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        titleEditText.setVisibility(View.GONE);
        titleTextView.setVisibility(View.VISIBLE);
        titleTextView.setText(title);
        autherEditText.setVisibility(View.GONE);
        autherTextView.setText(auther);
        autherTextView.setVisibility(View.VISIBLE);
    }

    /**
     * 文本可以编辑view显示
     */
    private void editViewShow() {
        mEditorOnlyShow.setVisibility(View.GONE);
        titleEditText.setVisibility(View.VISIBLE);
        titleTextView.setVisibility(View.GONE);
        autherEditText.setVisibility(View.VISIBLE);
        autherTextView.setVisibility(View.GONE);
    }

    /**
     * 设置webview初始化数据
     */
    private void mEditorSetInitData() {
        mEditor.setContext(this);
//        mEditor.setEditorHeight(200);
        mEditor.setEditorFontSize(16);
        mEditor.setEditorFontColor(getResources().getColor(R.color.color_444444));
        mEditorOnlyShow.setEditorFontSize(16);
        mEditorOnlyShow.setEditorFontColor(getResources().getColor(R.color.color_444444));
        mEditor.setOnTextChangeListener(new RichEditor.OnTextChangeListener() {
            @Override
            public void onTextChange(String text) {
                haveData = true;
                if ((null != currentObj && currentObj.getIsSubmit() == 1)) {
                    if (!currentHtmlText.equals(text)) {
                        mEditor.setHtmlText(currentHtmlText);
                    }
                } else {
                    currentHtmlText = text;
                }
                setSubmitBackground();
            }

            @Override
            public void onCurrentText(String text) {
                manuscriptText = text;
                isEdit = true;
                contentIsModify = true;
                updateTextEdit();
                openTimer(false);
                basicHandler.sendEmptyMessage(MSG_TEXT_NUM);
            }
        });
        setOnfoucsChangeListener(mEditor, 2);
        mEditor.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                longClick = true;
                TTLog.e("longClick: " + longClick);
                return false;
            }
        });
    }

    /**
     * 标题不支持换行
     */
    private void setEditorActionListener() {
        titleEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return (event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });
    }

    /**
     * 设置写稿页面，最底部，提交审批背景颜色
     */
    private void setSubmitBackground() {
        if (!TextUtils.isEmpty(currentHtmlText)) {
            if (currentHtmlText.length() > 0) {
                sendLayout.setBackgroundResource(R.drawable.b697fb4_526592_bg_selector);
            } else {
                sendLayout.setBackgroundResource(R.drawable.manuscript_send_background);
            }
        }
    }

    /**
     * 标题，记者，内容进行编辑时，更新数据库状态：1为编辑过，0为未编辑过
     */
    private void updateTextEdit() {
        setContentEdit(1);
        //正在上传，不进行处理(显示上传中)。未上传，进行处理（修改上传状态）
        if (null != currentObj && currentObj.getUploadState() != 2) {
            currentObj.setUploadState(0);
            dbManager.updataManuscriptUploadState(currentObj);
        }
    }

    /**
     * 监听软键盘状态
     *
     * @param activity
     */
    public void addOnSoftKeyBoardVisibleListener(Activity activity) {
        final View decorView = activity.getWindow().getDecorView();
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                decorView.getWindowVisibleDisplayFrame(rect);
                int displayHight = rect.bottom - rect.top;
                int hight = decorView.getHeight();
                boolean visible = (double) displayHight / hight < 0.8;
                if (visible) {
                    subLayout.setVisibility(View.GONE);
                    if (focusItem == 2) {
                        saftLayout.setVisibility(View.VISIBLE);
                    } else {
                    }
                } else {
                    saftLayout.setVisibility(View.GONE);
                    subLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * 插入音频，提交审批单击事件
     *
     * @param view
     */
    @OnClick({R.id.action_insert, R.id.richeditor_send_layout, R.id.richeditor_recall_layout
            , R.id.richeditor_quickApprove_layout, R.id.richeditor_postilLayout})
    public void clickButton(View view) {
        switch (view.getId()) {
            case R.id.richeditor_postilLayout:
                ToastUtils.showToast(this, "查看批注");
                break;
            case R.id.richeditor_quickApprove_layout://快速审批
                ToastUtils.showToast(this, "快速审批");
                break;
            case R.id.richeditor_recall_layout:
                if (null != currentObj) {
                    switch (currentObj.getApproveState()) {
                        case 1: //等待一审，可撤回
                            ToastUtils.showToast(this, "可撤回");
                            break;
                        case 2: //等待二审，不可撤回
                            ToastUtils.showToast(this, "不可撤回");
                            break;
                        case 3: //等待三审，不可撤回
                            ToastUtils.showToast(this, "不可撤回");
                            break;
                        case 5: //审批退回-发记者稿库
                            ToastUtils.showToast(this, "审批退回-发记者稿库");
                            break;
                        case 6: //待审-
//                            if(是否认领){ //没认领，(我要改稿)
//
//                            }else{//认领(审批)
//
//                            }
                            editViewShow();
                            quickApproveLayout.setVisibility(View.GONE);
                            recallTextView.setText(getString(R.string.main_approval));
                            currentObj.setIsSubmit(0);
                            break;
                    }
                }
                break;
            case R.id.richeditor_send_layout://提交审批
                deleteMoreData();
                if (null != currentObj && !TextUtils.isEmpty(currentHtmlText)) {
                    if (currentObj.getApproveState() == 0) {//发记者稿库
                        ToastUtils.showToast(this, "发记者稿库");
                        //已提交审批，不做处理
                        if (currentObj.getIsSubmit() == 1) {
                            break;
                        }
                        //无网络处理
                        if (!NetUtils.isNetConnected()) {
                            showToast(R.string.manuscript_net_no);
                            break;
                        }
                        //正上传，进行提示，返回。
                        if (currentObj.getUploadState() == 2) {
                            showToast(R.string.manuscript_uploading);
                        } else {
                            //纯文本内容不能超2万字
                            if (manuscriptText.length() <= MAX_TEXT_LEGNTH) {
                                if (!isRunSubmit) {
                                    isRunSubmit = true;
                                    saveCurrentManuscriptData();
                                    //文件音频大于100，进行提示
                                    if (isUploadForSpecial4G100M()) {
                                        showOneButtonDialog(true, "", getString(R.string.manuscript_upload_size100M_message), null);
                                        isRunSubmit = false;
                                    } else {
                                        ManuscriptInterfaceUtils.getManuscriptSubmitInfo(this, currentObj.getServerId(), basicHandler);
                                    }
                                }
                            } else {
                                ToastUtils.showToast(this, R.string.manuscript_submit_max);
                            }
                        }
                    } else if (currentObj.getApproveState() == 4) {
                        ToastUtils.showToast(this, "查看批注");
                    }
                } else {
                    ToastUtils.showToast(this, R.string.manuscript_inputs);
                }
                break;
            case R.id.action_insert:
//                Touchfalg = -1;
                mEditor.setWebViewHeight();
                mEditor.getDivHeight();
                Intent in = new Intent(this, ChooseAudioMaterialActivity.class);
                this.startActivityForResult(in, 0);
                break;
        }
    }

    /**
     * 设置内容已编辑同步数据库
     */
    private void setContentEdit(int editValue) {
        if (null != currentObj) {
            currentObj.setTextEdit(editValue);
            dbManager.updataManuscriptTextEdit(currentObj);
        }
    }

    /**
     * 右上角，未上传事件方法
     */
    @Override
    protected void onRightView1Click() {
        //判断内容是否为空，为空不能上传
        if (null != currentObj && !TextUtils.isEmpty(currentHtmlText)) {
            //未提审，未上传(0),上传失败(3)进行处理.已上传(1),上传中(2)-不在进行上传。
            if (currentObj.getIsSubmit() == 0 && (currentObj.getUploadState() == 0 || currentObj.getUploadState() == 3)) {
                //网络处理
                if (NetUtils.isNetConnected()) {
                    //内容不能超二万字
                    if (manuscriptText.length() <= MAX_TEXT_LEGNTH) {
                        upLoadManuscript();
                        hideSoftInput(mLeftView1);
                    } else {
                        ToastUtils.showToast(this, R.string.manuscript_uoload_max);
                    }
                } else {
                    showToast(R.string.manuscript_net_no);
                }
            }
        } else {
            ToastUtils.showToast(this, R.string.manuscript_inputs);
        }
    }

    /**
     * 上传稿件
     */
    private void upLoadManuscript() {
        uploadState = currentObj.getUploadState();
        deleteMoreData();
        saveCurrentManuscriptData();
        if (isUploadForSpecial4G100M()) {
            showOneButtonDialog(true, "", getString(R.string.manuscript_upload_size100M_message), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
        } else {
            if (null != currentObj) {
                setContentEdit(0);
                currentObj.setUploadState(2);
                serviceHelper.startUpload(currentObj);
            }
        }
    }

    /**
     * 返回事件
     */
    @Override
    protected void onLeftView1Click() {
        if (!dialogShow) {
            isBack = true;
            backSaveData();
        }
    }

    /**
     * 返回事件
     */
    @Override
    public void onBackPressed() {
        if (!dialogShow) {
            isBack = true;
            backSaveData();
        }
    }

    //上传稿件
    private void uploadData() {
        if (NetUtils.isNetConnected()) {
            upLoadManuscript();
        } else {//无网络，不上传返回列表(状态失败)
            setUploadStateAddFinish();
        }
    }

    /**
     * 设置当前稿件为上传失败状态，返回列表
     */
    private void setUploadStateAddFinish() {
        currentObj.setUploadState(3);
        dbManager.updataManuscriptUploadState(currentObj);
        finish();
    }

    /**
     * 返回时，数据处理
     */
    private void backSaveData() {
        mEditor.clearFocus();
        deleteMoreData();
        if (isEdit) {
            initTimer();
            if (isEdit) {
                saveCurrentManuscriptData();
            }
        }
        if (null != currentObj) {
            if (currentObj.getIsSubmit() == 1) {//云端稿件
                if(contentIsModify){
                    uploadData();
                }else{
                    setUploadStateAddFinish();
                }
            } else {//本地稿件
                uploadData();
            }
        }
    }

    /**
     * 进行音频选择面，回来后数据处理方法
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 0 && null != data) {
            ArrayList<MediaInfo> mediaInfos = data.getParcelableArrayListExtra("ObjList");
            if (null != mediaInfos) {
                audioAddFlag = false;
                saveAudioData(mediaInfos);
            }
        }
    }

    /**
     * 创建音频缓存目录。
     */
    private void createCacheDie() {
        saveAudioDir = StorageUtils.getSDCardStorageDirectory(this).getPath() + Constants.AUDIO_CACHE_DIR + File.separator + createTime;
        File file = new File(saveAudioDir);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 将文件复制到草稿文件目录下
     *
     * @param mediaInfos
     */
    private void saveAudioData(ArrayList<MediaInfo> mediaInfos) {
        if (null != currentObj) {
            if (mediaInfos.size() > 0) {
                //计算空间大小
                int spaceSize = (int) StorageUtils.getSurplusSapce1(saveAudioDir) / 1024 / 1024;
                if (spaceSize > 500) { //大于500兆
                    int fileNum = 0;
                    for (int i = 0; i < mediaInfos.size(); i++) {
                        File file = new File(mediaInfos.get(i).getAbsolutePath());
                        fileNum += file.length();
                    }
                    fileNum = (fileNum / 1014 / 1024);
                    if (spaceSize > fileNum) {  //添加内容，小于当前空间，可以进行插入
                        copyFileInsert(mediaInfos);
                    } else {//添加内容大于当前空间，进行空间提示
                        showSpaceInfo();
                    }
                } else {//小于500兆 进行空间提示
                    showSpaceInfo();
                }
            }
        } else {
            //当前保存，进行保存后。在进行音频缓存
            saveCurrentManuscriptData();
            saveAudioData(mediaInfos);
        }
    }

    /**
     * 对话框显示空间不足--“存储空间不足，无法添加音频，请释放一些存储空间后重试”
     */
    private void showSpaceInfo() {
        showOneButtonDialog("", getString(R.string.manuscript_no_space), null);
        if (null != currentObj && (audioAddFlag || !haveData)) {
            dbManager.deleteManuscriptInfo(currentObj.getId());
            currentObj = null;
        }
    }


    /**
     * 拷贝文件，插入音频图标
     *
     * @param mediaInfos
     */
    private void copyFileInsert(final ArrayList<MediaInfo> mediaInfos) {
        if (audioAddFlag) {
            htmlText = new StringBuffer();
        }
        manuscriptAudioInfos.clear();
        showLoadDialog(R.string.manuscript_execute);
        new Thread() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < mediaInfos.size(); i++) {
                        MediaInfo audioInfo = mediaInfos.get(i);
                        //复制音频(相同不进行在次复制)
                        //有，返回存储目录
                        String audioSavePath = saveAudioDir + File.separator + audioInfo.getFullName();
                        File file = new File(audioSavePath);
                        boolean isSaveFalg = false;
                        if (!file.exists()) {//已有音频，则不在进行复制文件，直接可使用。
                            isSaveFalg = copyFile(audioInfo, audioSavePath);
                        } else {
                            isSaveFalg = true;
                        }

                        if (isSaveFalg) {
                            audioInfo.setAbsolutePath(audioSavePath);
                            audioInfo.setManuscriptId(currentObj.getId());
                            //将音频信息保存到数据库。此草稿id关连
                            int flag = dbManager.saveAudioInfo(audioInfo);
                            //显示音频信息。
                            if (flag != -1) {
                                String audioName = audioInfo.getFullName();
                                if (audioAddFlag && null != htmlText) {
                                    htmlText.append("<div><img name=\"" + audioSavePath + "\"  src=\"" + AudiobackgrounUtil.getAudioBackground(ManuscriptAddActivity.this, TimeUtils.getYMDHMS(currentObj.getCreateTime()), audioName, TimeUtils.converToHms(audioInfo.getDuration() / 1000), webViewWidth) + "\" onclick=\"inputClick(this)\" style=\"width: 100%; " +
                                            "height: 49.6px;  margin-top: 12px; margin-bottom: 14px; vertical-align:middle; border: 0px;\"/><div><br>");
                                } else {
                                    String imagePath = AudiobackgrounUtil.getAudioBackground(ManuscriptAddActivity.this, TimeUtils.getYMDHMS(currentObj.getCreateTime()), audioName, TimeUtils.converToHms(audioInfo.getDuration() / 1000), webViewWidth);
                                    manuscriptAudioInfos.add(new ManuscriptAudioInfo(audioSavePath, imagePath));
                                }
                            }
                        }
                    }
                    basicHandler.sendEmptyMessage(SHOW_AUDIO_1);
                } catch (Exception e) {
                    if (null != currentObj)
                        e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 将目标音频保存到缓存保存路径
     *
     * @param info
     * @return
     */
    private boolean copyFile(MediaInfo info, String savePath) {
        boolean isSave = false;
        //目标音频路径
        if (!TextUtils.isEmpty(info.getAbsolutePath())) {
            //进行复制
            StorageUtils.copyFile(new File(info.getAbsolutePath()), new File(savePath));
            isSave = true;
        }
        return isSave;
    }

    @Override
    protected void processMessage(Message msg) {
        switch (msg.what) {
            case SHOW_AUDIO_1:
                if (audioAddFlag && !TextUtils.isEmpty(htmlText.toString())) {//从素村过来
                    mEditor.setHtmlText(htmlText.toString());//显示音频内容。
                    currentHtmlText = htmlText.toString();
                    isEdit = true;//设置为编辑过
                    contentIsModify = false;
                    openTimer(true);//打开定时器，进行保存
                    mEditor.focusEditor();
                    focusItem = 2;
                    htmlText = null;
                    setSubmitBackground();
                } else {
                    if (manuscriptAudioInfos.size() > 0) {
                        for (ManuscriptAudioInfo m : manuscriptAudioInfos) {
                            mEditor.insertTodo(m.getAudioPath(), m.getImagePath());
                        }
                    }
                }
                dismissDlg();
                if (!audioAddFlag) {
                    basicHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mEditor.setScrollY();
                        }
                    }, 500);
                }
                audioAddFlag = false;
                break;
            case ManuscriptServiceHelper.SERVICE_HELPER_SUBMITED:
                showDialogA(msg.arg1);
                break;
            case ManuscriptServiceHelper.SERVICE_HELPER_UPDATENETID:
                if (null != currentObj && currentObj.getId() == msg.arg2)
                    currentObj.setServerId(msg.arg1);
                break;
            case Constants.UPLOAD_FAIL:
                isRunSubmit = false;
                ToastUtils.showToast(ManuscriptAddActivity.this, R.string.message_detail_fail);
                break;
            case ManuscriptInterfaceUtils.MANUSCRIPT_INFO:
                isRunSubmit = false;
                if (msg.arg2 == 0) {
                    Intent intent = new Intent(this, ManuscriptProcessActivity.class);
                    intent.putExtra("LocalId", currentObj.getId());
                    startActivity(intent);
                } else {
                    showDialogA(currentObj.getId());
                }
                break;
            case ManuscriptServiceHelper.SERVICE_HELPER_JG:
                if (null != currentObj && msg.arg2 == currentObj.getId()) {
                    if (null != getmRightView1().getAnimation())
                        getmRightView1().clearAnimation();

                    if (currentObj.getTextEdit() == 1) {//上传后，在编辑过。回来显示可上传图标。
                        currentObj.setUploadState(0);
                        currentObj.setTextEdit(0);
                    } else {//未编辑过，显示成功，失败图标
                        if (msg.arg1 == 1) {
                            currentObj.setUploadState(1);
                        } else {
                            currentObj.setUploadState(3);
                        }
                    }
                }
                break;
            case ManuscriptServiceHelper.SERVICE_HELPER_UPLOADING:
                if (null != serviceHelper) {
                    //设置右上角，加载中(有动画效果)，未加载。
                }
                break;
            case ManuscriptServiceHelper.SERVICE_HELPER_DIALOG:
                if (currentObj.getId() == msg.arg1) {
                    if (!isBack) {
                        dialogShow = true;
                        //进行提示  -- 取消，确定后进行上传
                        showTwoButtonDialog(getString(R.string.manuscript_reset), "", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialogShow = false;
                                if (uploadState != -1) {
                                    currentObj.setUploadState(uploadState);
                                    dbManager.updataManuscriptUploadState(currentObj);
                                }
                                serviceHelper.cancelSubmit(currentObj.getId(), -1);
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialogShow = false;
                                TTLog.e("manuscript 对话框确定，进行上传");
                                serviceHelper.startUpload();
                            }
                        });
                    } else if (null != serviceHelper) {
                        serviceHelper.cancelSubmit(currentObj.getId(), -1);
                    }
                }
                break;
            case MSG_TEXT_NUM:
                if (null != manuscriptText) {
                    numTextView.setText(String.valueOf(manuscriptText.length()));
                }
                break;
        }
    }

    /**
     * 弹出其它位置已提审对话框
     *
     * @param manuscriptId
     */
    private void showDialogA(int manuscriptId) {
        if (currentObj.getId() == manuscriptId) {
            TTLog.e("manuscript 其它位置已提审对话框");
//            if(uploadState != -1){
//                currentObj.setUploadState(uploadState);
//                dbManager.updataManuscriptUploadState(currentObj);
//            }
//            setRightViewBackground();
            //进行提示  -- 取消，确定后进行上传
            showOneButtonDialog(getString(R.string.manuscript_submited), "", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        }
    }


    /**
     * 删除没有使用的音频,(内容添加三个音频，返回删除一个音频。需要将搞件内容只没有音频信息的音频删除掉)
     * 删除没有使用的图片
     */
    private void deleteMoreData() {
        if (!TextUtils.isEmpty(saveAudioDir) && !TextUtils.isEmpty(currentHtmlText)) {//搞件文件夹路径
            //删除数据库冗余音频数据及音频文件
            deleteManuscriptAudioData();
            //删除冗余没有使用到的图片
            deleteManuscriptAudioImage();
        }
    }

    //删除数据库冗余音频数据及音频文件
    private void deleteManuscriptAudioData() {
        //获取稿件所有音频数据
        ArrayList<MediaInfo> mediaInfos = dbManager.getMediaInfoInfos(currentObj.getId());
        for (MediaInfo m : mediaInfos) {
            String audioPath = m.getAbsolutePath();
            if (!currentHtmlText.contains(audioPath)) {
                //删除稿件音频数据库数据
                dbManager.deleteAudioInfo(m.getId());
                //删除对应在在的音频文件
                File fileDir = new File(audioPath);
                if (fileDir.exists()) {
                    fileDir.delete();
                }
            }
        }
    }

    //稿件删除多余没有使用到的图片
    private void deleteManuscriptAudioImage() {
        String imgPath = AudiobackgrounUtil.getMascriptImgPath(this);
        File file = new File(imgPath + TimeUtils.getYMDHMS(currentObj.getCreateTime()));
        if (file.exists()) {
            File[] imgFiles = file.listFiles();
            for (int i = 0; i < imgFiles.length; i++) {
                File imgfile = imgFiles[i];
                String imgPathStr = imgfile.getPath();
                if (!currentHtmlText.contains(imgPathStr)) {
                    imgfile.delete();
                }
            }
        }
    }

    /**
     * 标题，记者名称输入文字监听。改变过返回进行保存
     *
     * @param editText
     * @param flag     1 title  0 auther
     */
    private void addTextChangeListener(EditText editText, final int flag) {
        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                haveData = true;
                textChanged();
                if (flag == 0) {
                    if (text.length() > 0) {
                        titleHint.setVisibility(View.GONE);
                    } else {
                        titleHint.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    //标题，记者变换更新
    private void textChanged() {
        isEdit = true;
        contentIsModify = true;
        updateTextEdit();
        openTimer(false);
    }

    /**
     * 定时任务，保存数据
     */
    class MyTask extends TimerTask {
        public void run() {
            if (isEdit) {
                //保存当前文档
                saveCurrentManuscriptData();
            }
        }
    }

    /**
     * 保存当前文档
     */
    private void saveCurrentManuscriptData() {
        String title = titleEditText.getText().toString().trim();
        String auther = autherEditText.getText().toString();
        escapeCode();
        if (null == currentObj) {
            //标题为空，取当前时间
            ManuscriptInfo maInfo = new ManuscriptInfo();
            timeStr = TimeUtils.getYearMonthDayHMS1();
            maInfo.setCreateTime(timeStr);
            createTime = TimeUtils.getYMDHMS(maInfo.getCreateTime());
            if (TextUtils.isEmpty(title)) {
                title = getString(R.string.manuscript_not_title);
            }
            createCacheDie();
            maInfo.setTitle(title);
            maInfo.setAuther(auther);
            TTLog.e("manuscriptAdd  add text : " + currentHtmlText);
            maInfo.setHtmlText(currentHtmlText == null ? "" : currentHtmlText);
            maInfo.setManuscriptText(manuscriptText == null ? "" : manuscriptText);
            maInfo.setCharCount(manuscriptText == null ? 0 : manuscriptText.length());
            int id = dbManager.addManuscriptInfo(maInfo);
            if (id > 0) {
                maInfo.setId(id);
            }
            currentObj = maInfo;
            if (null != serviceHelper)
                serviceHelper.setManuscriptId(currentObj.getId());
        } else {
            if (TextUtils.isEmpty(title)) {//1.3，标题为空时，显示"无标题"。去提1.2名字重复逻辑。
                title = getString(R.string.manuscript_not_title);
            }
            currentObj.setTitle(title);
            currentObj.setAuther(auther);
            TTLog.e("manuscriptAdd  update text : " + currentHtmlText);
            currentObj.setHtmlText(currentHtmlText == null ? "" : currentHtmlText);
            currentObj.setManuscriptText(manuscriptText == null ? "" : manuscriptText);
            currentObj.setCharCount(manuscriptText == null ? 0 : manuscriptText.length());
//            currentObj.setId(0);  //不需要更新
//            currentObj.setCreateTime("");//不需要更新
            dbManager.updataManuscriptInfo(currentObj);
        }
        isEdit = false;
    }

    /**
     * 将html内容中特殊字符进行转义
     */
    private void escapeCode() {
        if (!TextUtils.isEmpty(currentHtmlText)) {
            currentHtmlText = currentHtmlText.replace("\\", "&#092;");
            currentHtmlText = currentHtmlText.replace("&nbsp;", "&#160;");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        initTimer();
        currentObj = null;
    }

    //清楚定时器
    private void initTimer() {
        if (null != timer) {
            timer.cancel();
            timer = null;
        }
    }


    //停止播放器停止播放
    private void pauseMusic() {
        AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        boolean mAudioFouus = mAudioManager.requestAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
            }
        }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    /**
     * 控制，流量下音频1大于100M，进行提示方法
     *
     * @return
     */
    private boolean isUploadForSpecial4G100M() {
        boolean isMore100M = false;
        if (GlobalVariableManager.isOpen100) {
            int netStatus = NetUtils.getNetConnectType();
            List<MediaInfo> audios = dbManager.findAudioInfo(currentObj.getId());
            for (MediaInfo info : audios) {
                if (netStatus == 2 && info.getSize() > GlobalVariableManager.MAX4GFILESIZE) {
                    isMore100M = true;
                    break;
                }
            }
        }

        return isMore100M;
    }
}
