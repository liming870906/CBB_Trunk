package com.tingtingfm.cbb.common.cache;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.tingtingfm.cbb.bean.CommonResourceInfo;
import com.tingtingfm.cbb.bean.MediaGroupInfo;
import com.tingtingfm.cbb.bean.MediaInfo;
import com.tingtingfm.cbb.common.db.DBMaterialImageManager;
import com.tingtingfm.cbb.common.utils.TimeUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by liming on 17/2/16.
 */

public class MediaDataManager {
    private static volatile MediaDataManager manager = null;
    private ArrayList<MediaGroupInfo> mMediaGroupInfos;
    private ArrayList<MediaInfo> mMediaInfos;
    /**
     * 构造方法
     */
    private MediaDataManager() {
        mMediaGroupInfos = new ArrayList<>();
        mMediaInfos = new ArrayList<>();
    }

    public enum MediaType{
        MEDIA_AUDIO_TYPE,MEDIA_OTHER_TYPE;
    }
    /**
     * 初始化方法
     *
     * @return
     */
    public static MediaDataManager getInstance() {
        MediaDataManager cache = manager;
        if (cache == null) {
            synchronized (MediaDataManager.class) {
                cache = manager;
                if (cache == null) {
                    cache = new MediaDataManager();
                    manager = cache;
                }
            }
        }
        return cache;
    }

    /**
     * 组装数据
     */
    public void assemblyData(ArrayList<MediaInfo> list) {
        //判断是否有数据
        if (list == null || list.size() == 0) {
            return;
        }
        //清空数据
        mMediaGroupInfos.clear();
        //加载数据
        MediaGroupInfo _mediaGroupInfo = new MediaGroupInfo();
        String _date = null;
        for (MediaInfo info : list) {
                String _time = TimeUtils.getYearMonthDayHMS(info.getDate_added());
                if (_date != null && !_date.equals(_time)) {
                    addMediaGroupInfo(_mediaGroupInfo);
                    _mediaGroupInfo = new MediaGroupInfo();
                }
                //设置时间
                _date = _time;
                _mediaGroupInfo.setDate(_date);
                _mediaGroupInfo.addMediaInfo(info);
        }
        addMediaGroupInfo(_mediaGroupInfo);
    }

    /**
     * 添加数据
     *
     * @param info
     */
    private void addMediaGroupInfo(MediaGroupInfo info) {
        mMediaGroupInfos.add(info);
    }

    /**
     * 添加多媒体
     *
     * @param info
     */
    public void addMediaInfo(MediaInfo info) {
        //插入多媒体数据的添加时间（日期）
        String _info_data = TimeUtils.getYearMonthDayHMS(info.getDate_added());
        //设置添加标记
        boolean _flag = false;
        //迭代数据
        for (MediaGroupInfo groupInfo : mMediaGroupInfos) {
            String _date = groupInfo.getDate();
            //判断时间是否相同
            if (_info_data.equals(_date)) {
                _flag = true;
                //获得多媒体集合
                ArrayList<MediaInfo> _info = groupInfo.getMediaInfos();
                //添加数据
                _info.add(0, info);
                break;
            }
        }
        if (!_flag) {
            //声明多媒体分组对象
            MediaGroupInfo _groupInfo = new MediaGroupInfo();
            //设置时间字符串
            _groupInfo.setDate(_info_data);
            //添加多媒体集合
            _groupInfo.addMediaInfo(info);
            //添加数据
            mMediaGroupInfos.add(getInsertIndex(_info_data),_groupInfo);
        }
    }

    /**
     * 插入位置
     * @return
     */
    private int getInsertIndex(String date) {
        //时间数据格式
        DateFormat _format = new SimpleDateFormat("yyyy年MM月dd日");
        try {
            //添加时间
            Date date1 = _format.parse(date);
            //循环遍历容器集合
            for (MediaGroupInfo groupInfo : mMediaGroupInfos){
                //获得容器中对象时间
                Date date2 = _format.parse(groupInfo.getDate());
                //比较时间大小
                if (date2.getTime() > date1.getTime()){
                    //返回位置
                    return mMediaGroupInfos.indexOf(groupInfo);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 删除多媒体信息
     * @param info 多媒体对象
     * @param type 多媒体类型
     */
    public void deleteMediaInfo(MediaInfo info, MediaType type){
        switch (type){
            case MEDIA_AUDIO_TYPE:
                mMediaInfos.remove(info);
                break;
            case MEDIA_OTHER_TYPE:
                deleteMediaInfo(info);
                break;
        }
    }
    /**
     * 删除多媒体对象
     *
     * @param info
     */
    public void deleteMediaInfo(MediaInfo info) {
        //多媒体素材的添加时间（日期）
        String _info_data = TimeUtils.getYearMonthDayHMS(info.getDate_added());
        //迭代数据
        for (MediaGroupInfo groupInfo : mMediaGroupInfos) {
            String _date = groupInfo.getDate();
            //判断时间是否相同
            if (_info_data.equals(_date)) {
                //获得多媒体集合
                ArrayList<MediaInfo> _info = groupInfo.getMediaInfos();
                //添加数据
                _info.remove(info);
                //判读是否还有数据
                if (_info.size() == 0) {
                    //移除多媒体分类对象
                    mMediaGroupInfos.remove(groupInfo);
                }
                break;
            }
        }
    }

    /**
     * 删除多媒体信息
     * @param infos
     */
    public void deleteMediaInfo(ArrayList<MediaInfo> infos){
        if(infos == null || infos.size() == 0){
            return ;
        }
        for (MediaInfo info : infos){
            deleteMediaInfo(info);
        }
    }

    /**
     * 更新多媒体信息
     * @param info  多媒体信息
     * @param type  多媒体类型
     */
    public void updateMediaInfo(MediaInfo info,MediaType type){
        switch (type){
            case MEDIA_AUDIO_TYPE:
                int index = mMediaInfos.indexOf(info);
                if (index != -1) {
                    mMediaInfos.set(index, info);
                }
                break;
            case MEDIA_OTHER_TYPE:
                updateMediaInfo(info);
                break;
        }
    }

    /**
     * 更新图片、视频多媒体数据信息
     * @param info
     */
    public void updateMediaInfo(MediaInfo info){
        //插入多媒体数据的添加时间（日期）
        String _info_data = TimeUtils.getYearMonthDayHMS(info.getDate_added());
        //迭代数据
        for (MediaGroupInfo groupInfo : mMediaGroupInfos) {
            String _date = groupInfo.getDate();
            //判断时间是否相同
            if(_info_data.equals(_date)){
                //获得多媒体集合
                ArrayList<MediaInfo> _info = groupInfo.getMediaInfos();
                int index = _info.indexOf(info);
                if(index < _info.size() && index >= 0){
                    //更新数据
                    _info.set(index,info);
                }
                break ;
            }
        }
    }

    /**
     * 获得图片、视频多媒体信息集合
     * @return
     */
    public ArrayList<MediaGroupInfo> getmMediaGroupInfos() {
        return mMediaGroupInfos;
    }

    /**
     * 查询图片数据信息
     * @param context 上下文对象
     * @param uri       图片uri地址
     * @param userId    用户ID
     * @return
     */
    public MediaInfo queryImageDataForUri(Context context, Uri uri,int userId) {
        MediaInfo info = new MediaInfo();
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                int _id = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                String _data = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                long _size = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.SIZE));
                String _displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                String _mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE));
                String _title = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.TITLE));
                long _dateAdded = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
                long _dateModified = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED));
                //查询数据资源
                CommonResourceInfo resourceInfo = DBMaterialImageManager.getInstance(context).queryImageInfo(_id, _displayName, _mimeType,userId);
                //添加MediaInfo数据
                info.setId(_id);
                info.setUser_id(resourceInfo.user_id);
                info.setMedia_id(resourceInfo.media_id);
                info.setAbsolutePath(_data);
                info.setSize(_size);
                info.setFullName(_displayName);
                info.setMime_type(_mimeType);
                info.setTitle(_title);
                info.setDate_added(_dateAdded);
                info.setDate_modified(_dateModified);
                info.setUpload_status(resourceInfo.uploadStatus);
                info.setDuration(0);
            }
            cursor.close();
        }
        return info;
    }

    /**
     * 清楚多媒体信息
     */
    public void clearData(){
        mMediaGroupInfos.clear();
        mMediaInfos.clear();
    }

    /**
     * 设置音频信息资源
     * @param mediaInfos
     */
    public void setmMediaInfos(ArrayList<MediaInfo> mediaInfos){
        if(mediaInfos == null) return ;
        this.mMediaInfos.clear();
        this.mMediaInfos.addAll(mediaInfos);
    }

    /**
     * 获得音频多媒体信息
     * @return
     */
    public ArrayList<MediaInfo> getmMediaInfos() {
        return mMediaInfos;
    }
}
