package com.tingtingfm.cbb.ui.activity;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.bean.CommonResourceInfo;
import com.tingtingfm.cbb.bean.MediaGroupInfo;
import com.tingtingfm.cbb.bean.MediaInfo;
import com.tingtingfm.cbb.common.cache.MediaDataManager;
import com.tingtingfm.cbb.common.configuration.AccoutConfiguration;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.configuration.GlobalVariableManager;
import com.tingtingfm.cbb.common.configuration.UrlManager;
import com.tingtingfm.cbb.common.db.DBAudioRecordManager;
import com.tingtingfm.cbb.common.db.DBMaterialImageManager;
import com.tingtingfm.cbb.common.db.DBMaterialVideoManager;
import com.tingtingfm.cbb.common.helper.HttpRequestHelper;
import com.tingtingfm.cbb.common.net.BaseRequestCallback;
import com.tingtingfm.cbb.common.net.RequestEntity;
import com.tingtingfm.cbb.common.upload.DBOperationUtils;
import com.tingtingfm.cbb.common.upload.UploadListener;
import com.tingtingfm.cbb.common.upload.UploadManager;
import com.tingtingfm.cbb.common.utils.FileUtils;
import com.tingtingfm.cbb.common.utils.NetUtils;
import com.tingtingfm.cbb.response.CheckMaterialResponse;
import com.tingtingfm.cbb.ui.eventbus.MediaDataEvent;
import com.tingtingfm.cbb.ui.eventbus.MessageEvent;
import com.tingtingfm.cbb.ui.fragment.MaterialAudioFragment;
import com.tingtingfm.cbb.ui.fragment.MaterialOtherFragment;
import com.tingtingfm.cbb.ui.thread.IDataLoadListener;
import com.tingtingfm.cbb.ui.thread.DataTaskThread;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;


/**
 * 素材管理
 *
 * @author liming
 */
public class MaterialManageActivity extends BaseActivity
        implements UploadListener,
        IDataLoadListener {

    //日志标记
    private static final String TAG = "MaterialManageActivity === >";
    //请求权限成功
    private static final int PERMISSION_REQUEST_READ_CONTACTS = 1;
    @BindView(R.id.ll_material_manager_bottom_layout)
    LinearLayout llBottomLayout;
    @BindView(R.id.iv_material_manager_upload)
    ImageView ivUpload;
    @BindView(R.id.iv_material_manager_send)
    ImageView ivSend;
    @BindView(R.id.iv_material_manager_delete)
    ImageView ivDelete;
    @BindView(R.id.tv_material_manager_upload)
    TextView tvUpload;
    @BindView(R.id.tv_material_manager_send)
    TextView tvSend;
    @BindView(R.id.tv_material_manager_delete)
    TextView tvDelete;
    @BindView(R.id.btn_material_manager_sure)
    Button btnMaterialSure;
    @BindView(R.id.fl_material_manager_content_layout)
    FrameLayout flContentLayout;
    @BindView(R.id.ll_material_manager_title_content)
    LinearLayout llTitleContentLayout;
    @BindView(R.id.btn_material_manager_title_audio)
    Button btnTitleAudio;
    @BindView(R.id.btn_material_manager_title_phone)
    Button btnTitlePhone;
    @BindView(R.id.material_manager_title_right3)
    TextView tvTitleRight;
    @BindView(R.id.tv_material_manager_title_content)
    TextView tvTitleContent;
    @BindView(R.id.material_manager_title_left1)
    ImageView ivTitleLeft;
    //设置选择标记
    private boolean isChoose = false;
    private Deque<UpdateMediaInfo> successDatas = new ArrayDeque<UpdateMediaInfo>();
    private Deque<UpdateMediaInfo> failDatas = new ArrayDeque<UpdateMediaInfo>();
    private ArrayList<MediaGroupInfo> mOtherData;
    private ArrayList<MediaInfo> mAudioData;
    //发送数据
    private ArrayList<MediaInfo> mUploadMedias;
    //设置选择导航标记——true显示上传、发送、删除导航；false显示确定导航

    //过滤路径
    private String pathDCIM = Environment.getExternalStorageDirectory() + "/DCIM";
    private String pathPictures = Environment.getExternalStorageDirectory() + "/Pictures";
    //设置碎片标记
    private String[] mFragmentTags = new String[]{"FRAGMENT_AUDIO", "FRAGMENT_OTHER"};
    //声明碎片管理器
    private FragmentManager mManager;
    //声明碎片事务
    private FragmentTransaction mTransaction;
    //碎片对象
    private Fragment mFragmentAudio, mFragmentOther;
    //判断界面标记
    private FragmentTag mTag;

    enum FragmentTag {
        FRAGMENT_AUDIO_TAG, FRAGMENT_OTHER_TAG;
    }

    @Override
    protected View initContentView() {
        return LayoutInflater.from(MaterialManageActivity.this).inflate(R.layout.activity_material_manage, null);
    }

    @Override
    protected void handleCreate() {
        EventBus.getDefault().register(this);
        successDatas.clear();
        failDatas.clear();
        //获得标记
        mTag = getIntent().getIntExtra("Fragment_Type", 0) == 0 ? FragmentTag.FRAGMENT_AUDIO_TAG : FragmentTag.FRAGMENT_OTHER_TAG;
        //初始化碎片
        initFragment();
        //初始化管理器
        initFragmentManager();
        //去掉标题
        setTitleVisiable(View.GONE);
        //设置标题
        updateTitleTagView();
        //声明数据存储对象
        mOtherData = MediaDataManager.getInstance().getmMediaGroupInfos();
        mAudioData = MediaDataManager.getInstance().getmMediaInfos();
        //声明上传集合
        mUploadMedias = new ArrayList<>();
        //设置标题右侧文本
        tvTitleRight.setText(R.string.material_manage_title_right_choose);
        tvTitleContent.setText(R.string.material_manage_nothing_title);
        //设置底部导航
        llBottomLayout.setVisibility(View.GONE);
        //开启加载数据线程
        new DataTaskThread().setLoadListener(this).crateNewMaterialRunnable().start();
        //添加监听
        UploadManager.getInstance().addUploadListener(this);
    }

    /**
     * 初始化碎片
     */
    private void initFragment() {
        mFragmentAudio = new MaterialAudioFragment();
        mFragmentOther = new MaterialOtherFragment();
    }

    /**
     * 初始化碎片
     */
    private void initFragmentManager() {
        mManager = getSupportFragmentManager();
        mTransaction = mManager.beginTransaction();
        if (mTag == FragmentTag.FRAGMENT_AUDIO_TAG) {
            mTransaction.replace(R.id.fl_material_manager_content_layout, mFragmentAudio, mFragmentTags[0]);
        } else {
            mTransaction.replace(R.id.fl_material_manager_content_layout, mFragmentOther, mFragmentTags[1]);
        }
        mTransaction.commit();
    }

    /**
     * 添加标题分类点击时间监听器
     *
     * @param view
     */
    @OnClick({R.id.btn_material_manager_title_audio, R.id.btn_material_manager_title_phone})
    public void onContentTitleViewClick(View view) {
        mTransaction = mManager.beginTransaction();
        switch (view.getId()) {
            case R.id.btn_material_manager_title_audio://点击音频分类
                mTag = FragmentTag.FRAGMENT_AUDIO_TAG;
                mTransaction.replace(R.id.fl_material_manager_content_layout, mFragmentAudio, mFragmentTags[0]);
                break;
            case R.id.btn_material_manager_title_phone://点击相册分类
                mTag = FragmentTag.FRAGMENT_OTHER_TAG;
                mTransaction.replace(R.id.fl_material_manager_content_layout, mFragmentOther, mFragmentTags[1]);
                break;
        }
        mTransaction.commit();
        updateTitleTagView();
    }

    private void updateTitleTagView() {
        switch (mTag) {
            case FRAGMENT_AUDIO_TAG:
                btnTitleAudio.setBackgroundResource(R.drawable.material_title_tag_left_bg);
                btnTitlePhone.setBackgroundColor(Color.TRANSPARENT);
                btnTitleAudio.setTextColor(getResources().getColor(R.color.color_697FB4));
                btnTitlePhone.setTextColor(getResources().getColor(R.color.white));
                break;
            case FRAGMENT_OTHER_TAG:
                btnTitleAudio.setBackgroundColor(Color.TRANSPARENT);
                btnTitlePhone.setBackgroundResource(R.drawable.material_title_tag_right_bg);
                btnTitleAudio.setTextColor(getResources().getColor(R.color.white));
                btnTitlePhone.setTextColor(getResources().getColor(R.color.color_697FB4));
                break;
        }
    }

    @OnClick({R.id.ll_material_manager_upload, R.id.ll_material_manager_send, R.id.ll_material_manager_delete})
    public void onBottomViewClick(View view) {
        int netStatus = NetUtils.getNetConnectType();

        if (mUploadMedias != null && mUploadMedias.size() > 0) {
            switch (view.getId()) {
                case R.id.ll_material_manager_upload:
                    if (!isUploadMediaHaveNoUploadFile()) {
                        break;
                    }
                    int singleFileSize = GlobalVariableManager.MAXWIFIFILESIZE;
                    if (GlobalVariableManager.isOpen100 && netStatus == 2) {
                        //手机网络
                        singleFileSize = GlobalVariableManager.MAX4GFILESIZE;
                    }

                    ArrayList<MediaInfo> mediaInfos = getListForSize(singleFileSize, mUploadMedias);
                    boolean isGreater500 = false;
                    int size = mediaInfos.size();

                    if (singleFileSize == GlobalVariableManager.MAX4GFILESIZE) {
                        ArrayList<MediaInfo> greater500List = getListForSize(GlobalVariableManager.MAXWIFIFILESIZE, mUploadMedias);
                        if (greater500List != null && greater500List.size() > 0) {
                            size = greater500List.size();
                            isGreater500 = true;
                        }
                    }

                    //判断是否提示对话框
                    if (size > 0) {
                        //移除大于指定大小的文件
                        for (MediaInfo _info : mediaInfos) {
                            mUploadMedias.remove(_info);
                        }

                        //显示提示框
                        showMore500MDialog(size,
                                !isGreater500 && singleFileSize == GlobalVariableManager.MAX4GFILESIZE
                                        ? R.string.material_manage_upload_size100_message
                                        : R.string.material_manage_more_count_text, true);
                    } else {
                        //添加上传
                        UploadManager.getInstance().addUpload(getNoUploadMedias(mUploadMedias));
                        //发送清理数据通知
                        basicHandler.sendEmptyMessage(Constants.MATERIAL_UPDATE_UPLOAD_CLICK_TAG);
                    }
                    break;
                case R.id.ll_material_manager_send:
                    sendMateriales();
                    break;
                case R.id.ll_material_manager_delete:
                    showDeleteMaterialDialog();
                    break;
            }
        }
    }


    /**
     * 筛选未上传文件
     *
     * @return
     */
    private ArrayList<MediaInfo> getNoUploadMedias(ArrayList<MediaInfo> infos) {
        ArrayList<MediaInfo> _infos = new ArrayList<>();
        for (MediaInfo _info : infos) {
            if (_info.getUpload_status() != Constants.UPLOAD_STATUS_SUCCESS) {
                _infos.add(_info);
            }
        }
        return _infos;
    }

    /**
     * 显示大于500MB对话框
     */
    private void showMore500MDialog(int count, int messageResId, final boolean isUpload) {
        showOneButtonDialog(true, "", getString(messageResId, count), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isUpload) {
                    //上传数据
                    //添加上传
                    UploadManager.getInstance().addUpload(getNoUploadMedias(mUploadMedias));
                    //发送清理数据通知
                    basicHandler.sendEmptyMessage(Constants.MATERIAL_UPDATE_UPLOAD_CLICK_TAG);
                }
            }
        });
    }

    /**
     * 过滤给定的集合中文件大小>500M的数据
     *
     * @return
     */
    private ArrayList<MediaInfo> getListForSize(int fileSize, ArrayList<MediaInfo> pMedias) {
        ArrayList<MediaInfo> _infos = new ArrayList<>();
        for (MediaInfo _info : pMedias) {
            long _size = Math.abs(_info.getSize());
            //大于500M
            if (_size > fileSize) {
                _infos.add(_info);
            }
        }
        return _infos;
    }

    @Override
    protected void processMessage(Message msg) {
        switch (msg.what) {
            case Constants.MATERIAL_CHOOSE_MEDIA_DATA_TAG:
                //更新标题
                updateTitleView();
                //更新底部控件
                uploadBottomView();

                break;
            case Constants.MATERIAL_RESTART_ALL_DATA_TAG:
                //清除数据
                mUploadMedias.clear();
                //改变标题
                changeTitleShowView();
                //改变底部隐藏状态
                setBottomViewVisible();
                //更新标题
                updateTitleView();
                EventBus.getDefault().post(new MessageEvent().Obtion(Constants.MATERIAL_LOAD_CHOOSE_DATA_TAG, isChoose));
                break;
            case Constants.MATERIAL_UPDATE_UPLOAD_CLICK_TAG:
                mUploadMedias.clear();
                changeTitleShowView();
                setBottomViewVisible();
                //更新标题
                updateTitleView();
                EventBus.getDefault().post(new MessageEvent().Obtion(Constants.MATERIAL_LOAD_CHOOSE_DATA_TAG, isChoose));
                break;
        }
    }

    @OnClick(R.id.material_manager_title_right3)
    public void onTitleRightClick() {
        if (mTag == FragmentTag.FRAGMENT_AUDIO_TAG) {
            if (MediaDataManager.getInstance().getmMediaInfos().size() <= 0) {
                return;
            }
        } else if (mTag == FragmentTag.FRAGMENT_OTHER_TAG) {
            if (MediaDataManager.getInstance().getmMediaGroupInfos().size() <= 0) {
                return;
            }
        }
        if (isChoose) {
            mUploadMedias.clear();
            basicHandler.sendEmptyMessage(Constants.MATERIAL_CHOOSE_MEDIA_DATA_TAG);
        }
        changeTitleShowView();
        setBottomViewVisible();
        EventBus.getDefault().post(new MessageEvent().Obtion(Constants.MATERIAL_LOAD_CHOOSE_DATA_TAG, isChoose));
    }

    private void setBottomSureBtnVisible() {
        //底部显示内容
        btnMaterialSure.setVisibility(isChoose ? View.VISIBLE : View.GONE);
        //设置默认显示
        if (btnMaterialSure.getVisibility() == View.VISIBLE) {
            btnMaterialSure.setTextColor(getResources().getColor(R.color.color_c1c1c1));
            btnMaterialSure.setBackgroundColor(getResources().getColor(R.color.white));
        }
    }

    /**
     * 更新标题
     */
    private void changeTitleShowView() {
        //修改选择状态
        isChoose = !isChoose;
        //更新右侧内容
        tvTitleRight.setText(isChoose ? R.string.material_manage_title_right_cancle : R.string.material_manage_title_right_choose);
        //更新标题
        tvTitleContent.setVisibility(isChoose ? View.VISIBLE : View.GONE);
        //标记布局
        llTitleContentLayout.setVisibility(isChoose ? View.GONE : View.VISIBLE);
        //设置返回按钮显示隐藏
        ivTitleLeft.setVisibility(isChoose ? View.GONE : View.VISIBLE);
    }

    /**
     * 设置底部功能列表是否显示
     */
    private void setBottomViewVisible() {
        //底部显示内容
        llBottomLayout.setVisibility(isChoose ? View.VISIBLE : View.GONE);
        //判断底部是否显示
        if (llBottomLayout.getVisibility() == View.VISIBLE) {
            ivUpload.setImageResource(R.drawable.material_upload_default);
            ivSend.setImageResource(R.drawable.material_send_default);
            ivDelete.setImageResource(R.drawable.material_delete_default);
            tvUpload.setTextColor(getResources().getColor(R.color.color_bac2cd));
            tvSend.setTextColor(getResources().getColor(R.color.color_bac2cd));
            tvDelete.setTextColor(getResources().getColor(R.color.color_bac2cd));
        }
    }

    /**
     * 更新底部控件
     */
    private void uploadBottomView() {
        if (mUploadMedias.size() > 0) {
            if (isUploadMediaHaveNoUploadFile()) {
                ivUpload.setImageResource(R.drawable.material_upload);
                tvUpload.setTextColor(getResources().getColor(R.color.color_737373));
            } else {
                ivUpload.setImageResource(R.drawable.material_upload_default);
                tvUpload.setTextColor(getResources().getColor(R.color.color_bac2cd));
            }
            ivSend.setImageResource(R.drawable.material_send_selector);
            ivDelete.setImageResource(R.drawable.material_delete_selector);
            tvSend.setTextColor(getResources().getColorStateList(R.color.tv_color_b8737373_seletor));
            tvDelete.setTextColor(getResources().getColorStateList(R.color.tv_color_b8737373_seletor));
        } else {
            ivUpload.setImageResource(R.drawable.material_upload_default);
            ivSend.setImageResource(R.drawable.material_send_default);
            ivDelete.setImageResource(R.drawable.material_delete_default);
            tvUpload.setTextColor(getResources().getColor(R.color.color_bac2cd));
            tvSend.setTextColor(getResources().getColor(R.color.color_bac2cd));
            tvDelete.setTextColor(getResources().getColor(R.color.color_bac2cd));
        }
    }

    private boolean isUploadMediaHaveNoUploadFile() {

        for (MediaInfo info : mUploadMedias) {
            if (info.getUpload_status() == Constants.UPLOAD_STATUS_DEFAULT || info.getUpload_status() == Constants.UPLOAD_STATUS_FAILURE) {
                return true;
            }
        }
        return false;
    }

    /**
     * 更新标题控件
     */
    private void updateTitleView() {
        if (mUploadMedias.size() > 0) {
            //设置标题
            tvTitleContent.setText(getString(R.string.material_manage_choose_title, mUploadMedias.size()));
        } else {
            //设置标题
            tvTitleContent.setText(R.string.material_manage_nothing_title);
        }
    }

    /**
     * 查询音频数据
     */
    private ArrayList<MediaInfo> queryAudioData() {
        return DBAudioRecordManager.getInstance(this).queryAllAudioRecord();
    }

    /**
     * 查询视频数据
     */
    private ArrayList<MediaInfo> queryVideoData() {
        ArrayList<MediaInfo> _infos = new ArrayList<>();
        Uri _videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Cursor _cursor = null;
        try {
            ContentResolver _provider = this.getContentResolver();
            _cursor = _provider.query(_videoUri, null, null,
                    null, MediaStore.Video.Media.DATE_ADDED);
            if (_cursor != null && _cursor.getCount() > 0) {
                while (_cursor.moveToNext()) {
                    int _id = _cursor.getInt(_cursor.getColumnIndex(MediaStore.Video.Media._ID));
                    String _data = _cursor.getString(_cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                    long _size = _cursor.getInt(_cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
                    String _displayName = _cursor.getString(_cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                    String _mimeType = _cursor.getString(_cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE));
                    String _title = _cursor.getString(_cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                    long _dateAdded = _cursor.getLong(_cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED));
                    long _dateModified = _cursor.getLong(_cursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED));
                    int _duration = _cursor.getInt(_cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                    if (FileUtils.isExistsFile(_data)) {
                        CommonResourceInfo resourceInfo = DBMaterialVideoManager.getInstance(this)
                                .queryVideoInfo(_id, _displayName, _mimeType,
                                        AccoutConfiguration.getLoginInfo().getUserid());
                        MediaInfo _info = new MediaInfo(_id, resourceInfo.media_id, _data, _size,
                                _displayName, _mimeType, _title,
                                _dateAdded, _dateModified, resourceInfo.uploadStatus,
                                _duration, resourceInfo.user_id, resourceInfo.sliceId,
                                resourceInfo.sliceCount, resourceInfo.successIds);
                        _infos.add(_info);
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            if (_cursor != null) {
                _cursor.close();
            }
        }

        return _infos;
    }

    /**
     * 查询图片数据
     */
    private ArrayList<MediaInfo> queryImageData() {
        ArrayList<MediaInfo> _infos = new ArrayList<>();
        Uri _imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor _cursor = null;
        try {
            ContentResolver _provider = this.getContentResolver();
            _cursor = _provider.query(_imageUri, null, MediaStore.Images.Media.MIME_TYPE + "=? or "
                            + MediaStore.Images.Media.MIME_TYPE + "=?",
                    new String[]{"image/jpeg", "image/png"}, MediaStore.Images.Media.DATE_ADDED);
            if (_cursor != null && _cursor.getCount() > 0) {
                while (_cursor.moveToNext()) {
                    int _id = _cursor.getInt(_cursor.getColumnIndex(MediaStore.Images.Media._ID));
                    String _data = _cursor.getString(_cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    long _size = _cursor.getInt(_cursor.getColumnIndex(MediaStore.Images.Media.SIZE));
                    String _displayName = _cursor.getString(_cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                    String _mimeType = _cursor.getString(_cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE));
                    String _title = _cursor.getString(_cursor.getColumnIndex(MediaStore.Images.Media.TITLE));
                    long _dateAdded = _cursor.getLong(_cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
                    long _dateModified = _cursor.getLong(_cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED));
                    //判断文件是否存在
                    if (FileUtils.isExistsFile(_data) && (_data.contains(pathDCIM) || _data.contains(pathPictures))) {
                        //查询数据资源
                        CommonResourceInfo resourceInfo = DBMaterialImageManager.getInstance(this)
                                .queryImageInfo(_id, _displayName, _mimeType, AccoutConfiguration.getLoginInfo().getUserid());
                        MediaInfo _info = new MediaInfo(_id, resourceInfo.media_id, _data,
                                _size, _displayName, _mimeType, _title,
                                _dateAdded, _dateModified, resourceInfo.uploadStatus, 0,
                                resourceInfo.user_id, resourceInfo.sliceId, resourceInfo.sliceCount, resourceInfo.successIds);
                        _infos.add(_info);
                    }
                }
            }

        } catch (Exception e) {
        } finally {
            if (_cursor != null) {
                _cursor.close();
            }
        }
        return _infos;
    }

    /**
     * 显示删除素材提示框
     */
    private void showDeleteMaterialDialog() {
        showTwoButtonDialog(true, true, "", getString(R.string.material_manage_delete_material_dialog_title),
                null, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //产出素材
                        deleteMaterial(mUploadMedias);
                        //发送更新消息
                        basicHandler.sendEmptyMessage(Constants.MATERIAL_RESTART_ALL_DATA_TAG);
                    }
                });
    }

    /**
     * 删除选中的素材
     *
     * @param infos
     */
    private void deleteMaterial(ArrayList<MediaInfo> infos) {
        if (infos != null && infos.size() > 0) {
            for (MediaInfo info : infos) {
                if (info.getMime_type().startsWith(Constants.MIME_TYPE_IMAGE)) {
                    DBMaterialImageManager.getInstance(this).deleteMaterialImage(info.getFullName(), info.getMime_type());
                    //删除系统数据库信息
                    deleteSystemMediaInfo(1, info);
                } else if (info.getMime_type().startsWith(Constants.MIME_TYPE_AUDIO)) {
                    DBAudioRecordManager.getInstance(this).deleteAudioRecord(info.getFullName(), info.getMime_type());
                } else if (info.getMime_type().startsWith(Constants.MIME_TYPE_VIDEO)) {
                    DBMaterialVideoManager.getInstance(this).deleteMaterialVideo(info.getFullName(), info.getMime_type());
                    //删除系统数据库信息
                    deleteSystemMediaInfo(0, info);
                }
                if (info.getMime_type().startsWith(Constants.MIME_TYPE_AUDIO)) {
                    MediaDataManager.getInstance().deleteMediaInfo(info, MediaDataManager.MediaType.MEDIA_AUDIO_TYPE);
                } else {
                    MediaDataManager.getInstance().deleteMediaInfo(info, MediaDataManager.MediaType.MEDIA_OTHER_TYPE);
                }
                deleteOwnFile(info.getAbsolutePath());
            }
        }
    }

    /**
     * 删除系统图片信息图片信息
     *
     * @param type
     * @param info
     */
    public void deleteSystemMediaInfo(int type, MediaInfo info) {
        //获得多媒体素材地址
        String _path = info.getAbsolutePath();
        //访问地址
        Uri _uri = null;
        //访问条件
        String _where = null;
        switch (type) {
            case 1:
                _uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                _where = MediaStore.Images.Media.DATA + "='" + _path + "'";
                break;
            default:
                _uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                _where = MediaStore.Video.Media.DATA + "='" + _path + "'";
                break;
        }

        //判断路径是否为null
        if (!TextUtils.isEmpty(_path)) {
            //获得访问系统数据库对象
            ContentResolver _resolver = getContentResolver();
            //删除多媒体数据
            _resolver.delete(_uri, _where, null);
        }
        //发送更新
        sendScanFile(_path);
    }

    private void sendScanFile(String _path) {
        //发送广播
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File file = new File(_path);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        sendBroadcast(intent);
    }

    /**
     * 删除文件
     */
    public void deleteOwnFile(final String data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File _file = new File(data);
                if (_file.exists()) {
                    _file.delete();
                }
            }
        }).start();
    }

//    /**
//     * 判断文件是否存在
//     *
//     * @param data
//     * @return
//     */
//    public boolean isExistsFile(String data) {
//        File file = new File(data);
//        if (file.exists()) {
//            return true;
//        }
//        return false;
//    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //发送消息完成加载数据
        EventBus.getDefault().post(new MessageEvent().Obtion(Constants.MATERIAL_LOAD_CHOOSE_DATA_TAG, isChoose));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        UploadManager.getInstance().removeUploadListener(this);
    }

    /**
     * 发送素材
     */
    private void sendMateriales() {
        //判断素材上限是否为5个
        if (mUploadMedias != null && mUploadMedias.size() > 5) {
            //提示上限
            showToast(R.string.material_manage_send_max_file_count_tips);
            //返回
            return;
        }
        boolean _isUploadFlag = true;
        Upload:
        for (MediaInfo info : mUploadMedias) {
            if (info.getUpload_status() != Constants.UPLOAD_STATUS_SUCCESS) {
                _isUploadFlag = false;
                break Upload;
            }
        }
        if (!_isUploadFlag) {
            showToast(R.string.material_manage_send_no_upload_tips);
            return;
        }

        //检验是否有删除数据
        StringBuilder _sb = new StringBuilder();
        //拼接参数
        for (MediaInfo info : mUploadMedias) {
            _sb.append(info.getMedia_id() + ",");
        }

        String _id_list = _sb.subSequence(0, _sb.length() - 1).toString();
        //引用发送请求
        checkMaterialRequest(_id_list);
    }

    /**
     * 发送检查请求
     *
     * @param id_list
     */
    private void checkMaterialRequest(final String id_list) {
        //请求接口进行登录
        RequestEntity entity = new RequestEntity(UrlManager.CHECK_MATERIALS);
        entity.addParams("id_list", id_list);
        HttpRequestHelper.post(entity, new BaseRequestCallback<CheckMaterialResponse>() {
            @Override
            public void onStart() {
                showLoadDialog();
            }

            @Override
            public void onSuccess(CheckMaterialResponse response) {
                ArrayList<Integer> _ids = response.getData();
                if (_ids.size() == 0) {
                    // TODO: 17/1/5  界面跳转
                    startActivity(new Intent(MaterialManageActivity.this, SendActivity.class)
                            .putExtra("id_list", id_list));
                    //发送清理数据通知
                    basicHandler.sendEmptyMessage(Constants.MATERIAL_UPDATE_UPLOAD_CLICK_TAG);
                } else {
                    // TODO: 17/1/5  显示对话框
                    showWebDeleteDialog(_ids);
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

    /**
     * Web删除素材提示框
     */
    private void showWebDeleteDialog(ArrayList<Integer> ids) {
        final ArrayList<MediaInfo> mediainfos = new ArrayList<>();
        StringBuilder _sb = new StringBuilder();
        for (int id : ids) {
            for (MediaInfo _info : mUploadMedias) {
                if (_info.getMedia_id() == id) {
                    _sb.append(_info.getFullName() + ",");
//                    _info.setUpload_status(Constants.UPLOAD_STATUS_DEFAULT);
                    mediainfos.add(_info);
                }
            }
        }

        if (_sb.length() > 1) {
            String _message = _sb.substring(0, _sb.length() - 1);
            showOneButtonDialog(true, "", getString(R.string.audio_preview_make_web_delete_tips, _message), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    for (MediaInfo info : mediainfos) {
                        info.setUpload_status(Constants.UPLOAD_STATUS_DEFAULT);
                        info.setSliceId(0);
                        info.setSliceCount(0);
                        info.setSuccessIds("");
                        info.setMedia_id(-1);
                        info.setIsUpdateAudioInfo(0);
                        if (info.getMime_type().startsWith(Constants.MIME_TYPE_AUDIO)) {
                            info.setIsUpdateAudioInfo(0);
                        }

                        DBOperationUtils.updateMaterialDb(info);
                    }
                    basicHandler.sendEmptyMessage(Constants.MATERIAL_CHOOSE_MEDIA_DATA_TAG);
                    EventBus.getDefault().post(new MessageEvent().Obtion(Constants.MATERIAL_LOAD_CHOOSE_DATA_TAG, isChoose));
                }
            });
        }
    }

    @Override
    public void start(List<Integer> ids) {
        if (ids.size() != 0) {
            updateUploadMediaInfo(ids, Constants.UPLOAD_STATUS_LOADING);
            EventBus.getDefault().post(new MessageEvent().Obtion(Constants.MATERIAL_LOAD_CHOOSE_DATA_TAG, isChoose));
        }
    }

    private void updateUploadMediaInfo(List<Integer> ids, int uploadStatus) {
        for (int id : ids) {
            next:
            for (int i = 0; i < mOtherData.size(); i++) {
                MediaGroupInfo _mediaGroupInfo = mOtherData.get(i);
                ArrayList<MediaInfo> _infos = _mediaGroupInfo.getMediaInfos();
                for (MediaInfo info : _infos) {
                    if (info.getId() == id) {
                        info.setUpload_status(uploadStatus);
                        break next;
                    }
                }
            }
            for (MediaInfo info : mAudioData) {
                if (info.getId() == id) {
                    info.setUpload_status(uploadStatus);
                    break;
                }
            }
        }
    }


    private void updateUploadFailInfo() {
        while (failDatas.size() > 0) {
            UpdateMediaInfo mediaInfo = failDatas.pollLast();
            if (mediaInfo == null) {
                return;
            }

            next:
            for (int i = 0; i < mOtherData.size(); i++) {
                MediaGroupInfo _mediaGroupInfo = mOtherData.get(i);
                ArrayList<MediaInfo> _infos = _mediaGroupInfo.getMediaInfos();
                for (MediaInfo info : _infos) {
                    if (info.getId() == mediaInfo.id) {
                        info.setUpload_status(Constants.UPLOAD_STATUS_FAILURE);
                        break next;
                    }
                }
            }
            for (MediaInfo info : mAudioData) {
                if (info.getId() == mediaInfo.id) {
                    info.setUpload_status(Constants.UPLOAD_STATUS_FAILURE);
                    break;
                }
            }
            EventBus.getDefault().post(new MessageEvent().Obtion(Constants.MATERIAL_LOAD_CHOOSE_DATA_TAG, isChoose));
        }
    }

    @Override
    public void fail(int id) {
        failDatas.add(new UpdateMediaInfo(id));
        updateUploadFailInfo();
    }

    @Override
    public void success(int id, int mediaId) {
        successDatas.add(new UpdateMediaInfo(id, mediaId));
        updateUploadSuccessInfo();
    }

    private void updateUploadSuccessInfo() {
        while (successDatas.size() > 0) {
            UpdateMediaInfo mediaInfo = successDatas.pollLast();
            if (mediaInfo == null) {
                return;
            }

            next:
            for (int i = 0; i < mOtherData.size(); i++) {
                MediaGroupInfo _mediaGroupInfo = mOtherData.get(i);
                ArrayList<MediaInfo> _infos = _mediaGroupInfo.getMediaInfos();
                for (MediaInfo info : _infos) {
                    if (info.getId() == mediaInfo.id) {
                        info.setMedia_id(mediaInfo.mediaId);
                        info.setUpload_status(Constants.UPLOAD_STATUS_SUCCESS);
                        break next;
                    }
                }
            }
            for (MediaInfo info : mAudioData) {
                if (info.getId() == mediaInfo.id) {
                    info.setMedia_id(mediaInfo.mediaId);
                    info.setUpload_status(Constants.UPLOAD_STATUS_SUCCESS);
                    break;
                }
            }
            EventBus.getDefault().post(new MessageEvent().Obtion(Constants.MATERIAL_LOAD_CHOOSE_DATA_TAG, isChoose));
        }
    }

    @Override
    public void startLoad() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showLoadDialog();
            }
        });
    }

    @Override
    public void loadMaterialData() {
        loadAudioMaterialData();
        loadOtherMaterialData();
        //发送消息完成加载数据
        EventBus.getDefault().post(new MessageEvent().Obtion(Constants.MATERIAL_LOAD_DATA_TAG));
    }

    private void loadOtherMaterialData() {
        ArrayList<MediaInfo> _mediaInfos = new ArrayList<>();
        _mediaInfos.addAll(queryImageData());
        _mediaInfos.addAll(queryVideoData());
        //根据时间排序
        Collections.sort(_mediaInfos, new MediaComparator());
        //组装数据
        MediaDataManager.getInstance().assemblyData(_mediaInfos);
    }

    @NonNull
    private void loadAudioMaterialData() {
        //获得音频数据
        ArrayList<MediaInfo> _audios = queryAudioData();
        //根据时间排序
        Collections.sort(_audios, new MediaComparator());
        //组装数据
        MediaDataManager.getInstance().setmMediaInfos(_audios);
    }

    @Override
    public void stopLoad() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateUploadSuccessInfo();
                updateUploadFailInfo();
                dismissDlg();
            }
        });
    }

    @OnClick(R.id.btn_material_manager_sure)
    public void clickMaterialAudioSureView(View view) {
        if (mUploadMedias != null && mUploadMedias.size() > 0) {
            //添加素材确定按钮
            resultData();
        }
    }

    @OnClick(R.id.material_manager_title_left1)
    public void onTitleLeftClick() {
        //点击返回
        onLeftView1Click();
    }

    @Override
    protected void onLeftView1Click() {
        setResult(-1, null);
        super.onLeftView1Click();
    }

    @Override
    public void onBackPressed() {
        setResult(-1, null);
        super.onBackPressed();
    }

    //将选中数据返回--tianhu
    private void resultData() {
        if (null != mUploadMedias && mUploadMedias.size() > 0) {
            //超过500文件个数
            ArrayList<MediaInfo> _More500MFiles = getListForSize(GlobalVariableManager.MAXWIFIFILESIZE, mUploadMedias);
            //判断是否提示对话框
            if (_More500MFiles.size() > 0) {
                //显示提示框
                showMore500MDialog(_More500MFiles.size(), R.string.material_manage_more_count_text2, false);
            } else {
                Intent intent = new Intent();
                intent.putParcelableArrayListExtra("ObjList", mUploadMedias);
                setResult(0, intent);
                finish();
            }
        }
    }


    public static class UpdateMediaInfo {
        int id;
        int mediaId;

        public UpdateMediaInfo(int id) {
            this.id = id;
        }

        public UpdateMediaInfo(int id, int mediaId) {
            this.id = id;
            this.mediaId = mediaId;
        }
    }

    class MediaComparator implements Comparator<MediaInfo> {

        @Override
        public int compare(MediaInfo o1, MediaInfo o2) {
            long time1 = o1.getDate_added();
            long time2 = o2.getDate_added();
            if (time2 < time1) {
                return -1;
            } else if (time2 == time1) {
                return 0;
            } else {
                return 1;
            }
        }
    }

    public void onEventMainThread(MediaDataEvent media) {
        MediaInfo info = media.getMediaInfo();
        boolean isCheck = media.isCheck();
        //判断是否选中数据
        if (isCheck) {
            //添加数据
            mUploadMedias.add(info);
        } else {
            //移除数据
            mUploadMedias.remove(info);
        }
        //发送消息
        basicHandler.sendEmptyMessage(Constants.MATERIAL_CHOOSE_MEDIA_DATA_TAG);
    }
}
