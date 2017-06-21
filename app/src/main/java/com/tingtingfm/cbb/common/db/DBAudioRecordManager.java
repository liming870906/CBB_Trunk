package com.tingtingfm.cbb.common.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.tingtingfm.cbb.bean.MediaInfo;
import com.tingtingfm.cbb.bean.UploadFirstResponse;
import com.tingtingfm.cbb.common.configuration.AccoutConfiguration;
import com.tingtingfm.cbb.common.utils.DataConvertUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 数据库管理类.
 *
 * @author liming
 */
public class DBAudioRecordManager {
    private static volatile DBAudioRecordManager instance;

    private AtomicInteger mOpenCounter = new AtomicInteger();
    private SQLiteDatabase mDb;
    private DatabaseHelper helper;

    /**
     * 构造方法
     *
     * @param context
     */
    private DBAudioRecordManager(Context context) {
        helper = new DatabaseHelper(context);
    }

    /**
     * 获得数据库管理对象
     *
     * @param context
     * @return
     */
    public static DBAudioRecordManager getInstance(Context context) {
        if (instance == null) {
            synchronized (DBAudioRecordManager.class) {
                if (instance == null) {
                    instance = new DBAudioRecordManager(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    /**
     * 获得数据库版本
     *
     * @return
     */
    public int getDB_VERSION() {
        return DatabaseHelper.DB_VERSION;
    }

    /**
     * 获得数据库操作对象
     *
     * @return
     */
    private synchronized SQLiteDatabase openDatabase() {
        if (mOpenCounter.incrementAndGet() == 1) {
            // Opening new database
            mDb = helper.getWritableDatabase();
        }
        return mDb;
    }

    /**
     * 关闭数据库
     */
    private synchronized void closeDatabase() {
        if (mOpenCounter.decrementAndGet() == 0) {
            // Closing database
            if (null != mDb) {
                mDb.close();
            }
        }
    }

    /**
     * 关闭游标
     *
     * @param cursor
     */
    private void closeCursor(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
    }

    /**
     * 添加录制音频信息
     *
     * @param info
     * @return
     */
    public boolean addAudioRecord(MediaInfo info) {
        boolean flag = false;
        try {
            openDatabase();
            ContentValues values = new ContentValues();
            values.put(TableConstant.TABLE_AUDIO_RECORD_MEDIA_ID, info.getMedia_id());
            values.put(TableConstant.TABLE_AUDIO_RECORD_DATA, info.getAbsolutePath());
            values.put(TableConstant.TABLE_AUDIO_RECORD_SIZE, info.getSize());
            values.put(TableConstant.TABLE_AUDIO_RECORD_DISPLAY_NAME, info.getFullName());
            values.put(TableConstant.TABLE_AUDIO_RECORD_MIME_TYPE, info.getMime_type());
            values.put(TableConstant.TABLE_AUDIO_RECORD_TITLE, info.getTitle());
            values.put(TableConstant.TABLE_AUDIO_RECORD_DATE_ADDED, info.getDate_added());
            values.put(TableConstant.TABLE_AUDIO_RECORD_DATE_MODIFIED, info.getDate_modified());
            values.put(TableConstant.TABLE_AUDIO_RECORD_UPLOAD_STATUS, info.getUpload_status());
            values.put(TableConstant.TABLE_AUDIO_RECORD_DURATION, info.getDuration());
            values.put(TableConstant.TABLE_AUDIO_RECORD_USER_ID, info.getUser_id());
            values.put(TableConstant.TABLE_AUDIO_RECORD_LONGITUDE, info.getLongitude());
            values.put(TableConstant.TABLE_AUDIO_RECORD_LATITUDE, info.getLatitude());
            values.put(TableConstant.TABLE_AUDIO_RECORD_PLACE, info.getPlace());


            flag = mDb.replace(TableConstant.TABLE_AUDIO_RECORD_TABLE_NAME, null, values) != -1 ? true : false;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return flag;
    }

    /**
     * 删除所有录制音频信息
     *
     * @return
     */
    public boolean deleteAllAudioRecord() {
        boolean result = false;
        try {
            openDatabase();
            result = mDb.delete(TableConstant.TABLE_AUDIO_RECORD_TABLE_NAME, null, null) > 0 ? true : false;
        } catch (Exception e) {
        } finally {
            closeDatabase();
        }
        return result;
    }

    /**
     * 删除某条录制音频信息
     *
     * @param name
     * @param mimeType
     * @return
     */
    public boolean deleteAudioRecord(String name, String mimeType) {
        boolean result = false;
        try {
            openDatabase();
            result = mDb.delete(TableConstant.TABLE_AUDIO_RECORD_TABLE_NAME, TableConstant.TABLE_AUDIO_RECORD_DISPLAY_NAME + "=? and " + TableConstant.TABLE_AUDIO_RECORD_MIME_TYPE + "=?", new String[]{name, mimeType}) > 0 ? true : false;
        } catch (Exception e) {
        } finally {
            closeDatabase();
        }
        return result;
    }

    /**
     * 查询所有录制音频信息
     *
     * @return
     */
    public ArrayList<MediaInfo> queryAllAudioRecord() {
        ArrayList<MediaInfo> _list = new ArrayList<>();
        Cursor mCursor = null;
        try {
            openDatabase();
            mCursor = mDb.query(TableConstant.TABLE_AUDIO_RECORD_TABLE_NAME, null, null, null, null, null, "_id desc");
            if (mCursor != null && mCursor.getCount() > 0) {
                for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                    int _user_id = mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_USER_ID));
                    if (_user_id == AccoutConfiguration.getLoginInfo().getUserid()) {
                        MediaInfo _info = new MediaInfo();
                        _info.setId(mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_ID)));
                        _info.setMedia_id(mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_MEDIA_ID)));
                        _info.setAbsolutePath(mCursor.getString(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_DATA)));
                        _info.setSize(mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_SIZE)));
                        _info.setFullName(mCursor.getString(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_DISPLAY_NAME)));
                        _info.setMime_type(mCursor.getString(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_MIME_TYPE)));
                        _info.setTitle(mCursor.getString(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_TITLE)));
                        _info.setDate_added(mCursor.getLong(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_DATE_ADDED)));
                        _info.setDate_modified(mCursor.getLong(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_DATE_MODIFIED)));
                        _info.setUpload_status(mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_UPLOAD_STATUS)));
                        _info.setDuration(mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_DURATION)));
                        _info.setInterview_persion(mCursor.getString(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_INTERVIEW_PERSION)));
                        _info.setInterview_event(mCursor.getString(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_INTERVIEW_EVENT)));
                        _info.setInterview_keyword(mCursor.getString(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_INTERVIEW_KEYWORD)));
                        _info.setLongitude(mCursor.getDouble(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_LONGITUDE)));
                        _info.setLatitude(mCursor.getDouble(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_LATITUDE)));
                        _info.setPlace(mCursor.getString(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_PLACE)));
                        _info.setIsUpdateAudioInfo(mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_IS_UPLOAD)));
                        _info.setUser_id(_user_id);
                        _info.setSliceId(mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_SLICE_ID)));
                        _info.setSliceCount(mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_SLICE_COUNT)));
                        _info.setSuccessIds(mCursor.getString(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_SLICE_SUCCESS)));
                        _list.add(0, _info);
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            closeCursor(mCursor);
            closeDatabase();
        }

        return _list;
    }

    /**
     * 查询所有录制音频信息
     *
     * @return
     */
    public MediaInfo queryAudioRecord(int id) {
        MediaInfo _info = null;
        Cursor mCursor = null;
        try {
            openDatabase();
            mCursor = mDb.query(TableConstant.TABLE_AUDIO_RECORD_TABLE_NAME, null, "_id=?", new String[]{String.valueOf(id)}, null, null, "_id desc");
            if (mCursor != null && mCursor.getCount() > 0) {
                if (mCursor.moveToFirst()) {
                    _info = new MediaInfo();
                    _info.setId(mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_ID)));
                    _info.setMedia_id(mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_MEDIA_ID)));
                    _info.setAbsolutePath(mCursor.getString(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_DATA)));
                    _info.setSize(mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_SIZE)));
                    _info.setFullName(mCursor.getString(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_DISPLAY_NAME)));
                    _info.setMime_type(mCursor.getString(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_MIME_TYPE)));
                    _info.setTitle(mCursor.getString(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_TITLE)));
                    _info.setDate_added(mCursor.getLong(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_DATE_ADDED)));
                    _info.setDate_modified(mCursor.getLong(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_DATE_MODIFIED)));
                    _info.setUpload_status(mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_UPLOAD_STATUS)));
                    _info.setDuration(mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_DURATION)));
                    _info.setInterview_persion(mCursor.getString(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_INTERVIEW_PERSION)));
                    _info.setInterview_event(mCursor.getString(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_INTERVIEW_EVENT)));
                    _info.setInterview_keyword(mCursor.getString(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_INTERVIEW_KEYWORD)));
                    _info.setLongitude(mCursor.getDouble(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_LONGITUDE)));
                    _info.setLatitude(mCursor.getDouble(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_LATITUDE)));
                    _info.setPlace(mCursor.getString(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_PLACE)));
                    _info.setIsUpdateAudioInfo(mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_IS_UPLOAD)));
                    _info.setUser_id(mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_USER_ID)));
                }
            }
        } catch (Exception e) {
        } finally {
            closeCursor(mCursor);
            closeDatabase();
        }

        return _info;
    }

    /**
     * 查询所有录制音频信息
     *
     * @return
     */
    public int queryAudioRecordInfo(String name) {
        int _id = 0;
        Cursor mCursor = null;
        try {
            openDatabase();
            mCursor = mDb.query(TableConstant.TABLE_AUDIO_RECORD_TABLE_NAME,
                    null,
                    TableConstant.TABLE_AUDIO_RECORD_TITLE + "=?",
                    new String[]{name}, null, null, "_id desc");
            if (mCursor != null && mCursor.getCount() > 0) {
                for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                    _id = mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_ID));
                }
            }
        } catch (Exception e) {
        } finally {
            closeCursor(mCursor);
            closeDatabase();
        }
        return _id;
    }

    public boolean queryAudioRecordInfo(String name, int userId){
        boolean isReslut = false;
        Cursor mCursor = null;
        try {
            openDatabase();
            mCursor = mDb.query(TableConstant.TABLE_AUDIO_RECORD_TABLE_NAME,
                    null,
                    TableConstant.TABLE_AUDIO_RECORD_TITLE + "=? and "+TableConstant.TABLE_AUDIO_RECORD_USER_ID+"=?",
                    new String[]{name,String.valueOf(userId)}, null, null, null);
            if (mCursor != null && mCursor.getCount() > 0) {
                for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                    isReslut = mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_ID)) > 0 ? true : false;
                }
            }
        } catch (Exception e) {
        } finally {
            closeCursor(mCursor);
            closeDatabase();
        }
        return isReslut;
    }

    public void updateAudioRecord(MediaInfo infos) {
        updateAudioRecord(DataConvertUtils.getMediaInfos(infos));
    }

    /**
     * 更新多媒体数据
     *
     * @param infos
     */
    public boolean updateAudioRecord(List<MediaInfo> infos) {
        int result = 0;
        try {
            openDatabase();
            for (MediaInfo info : infos) {
                ContentValues values = new ContentValues();
                values.put(TableConstant.TABLE_AUDIO_RECORD_DISPLAY_NAME, info.getFullName());
                values.put(TableConstant.TABLE_AUDIO_RECORD_TITLE, info.getTitle());
                values.put(TableConstant.TABLE_AUDIO_RECORD_MEDIA_ID, info.getMedia_id());
                values.put(TableConstant.TABLE_AUDIO_RECORD_DATE_MODIFIED, info.getDate_modified());
                values.put(TableConstant.TABLE_AUDIO_RECORD_DATA, info.getAbsolutePath());
                values.put(TableConstant.TABLE_AUDIO_RECORD_UPLOAD_STATUS, info.getUpload_status());
                values.put(TableConstant.TABLE_AUDIO_RECORD_SLICE_ID, info.getSliceId());
                values.put(TableConstant.TABLE_AUDIO_RECORD_SLICE_COUNT, info.getSliceCount());
                values.put(TableConstant.TABLE_AUDIO_RECORD_SLICE_SUCCESS, info.getSuccessIds());
                values.put(TableConstant.TABLE_AUDIO_RECORD_IS_UPLOAD,info.getIsUpdateAudioInfo());
                //更新数据
                result = mDb.update(TableConstant.TABLE_AUDIO_RECORD_TABLE_NAME, values, "_id=?", new String[]{String.valueOf(info.getId())});
            }
        } catch (Exception e) {
        } finally {
            closeDatabase();
        }
        return result == 0 ? false : true;
    }

    /**
     * 更新音频信息
     *
     * @param id
     * @param name
     * @param person
     * @param event
     * @param keyword
     * @return
     */
    public boolean updateAudioRecordInfo(int id, String name, String person, String event, String keyword, String data, double longitude, double latitude, String place, int isUpload) {
        int result = 0;
        try {
            openDatabase();
            ContentValues values = new ContentValues();
            values.put(TableConstant.TABLE_AUDIO_RECORD_DISPLAY_NAME, name + ".mp3");
            values.put(TableConstant.TABLE_AUDIO_RECORD_TITLE, name);
            values.put(TableConstant.TABLE_AUDIO_RECORD_INTERVIEW_PERSION, person);
            values.put(TableConstant.TABLE_AUDIO_RECORD_INTERVIEW_EVENT, event);
            values.put(TableConstant.TABLE_AUDIO_RECORD_INTERVIEW_KEYWORD, keyword);
            values.put(TableConstant.TABLE_AUDIO_RECORD_DATA, data);
            values.put(TableConstant.TABLE_AUDIO_RECORD_LONGITUDE, longitude);
            values.put(TableConstant.TABLE_AUDIO_RECORD_LATITUDE, latitude);
            values.put(TableConstant.TABLE_AUDIO_RECORD_PLACE, place);
            values.put(TableConstant.TABLE_AUDIO_RECORD_IS_UPLOAD, isUpload);

            //更新数据
            result = mDb.update(TableConstant.TABLE_AUDIO_RECORD_TABLE_NAME, values, "_id=?", new String[]{String.valueOf(id)});
        } catch (Exception e) {
        } finally {
            closeDatabase();
        }
        return result == 0 ? false : true;
    }

    /**
     * 更新音频信息
     *
     * @param id
     * @param name
     * @param person
     * @param event
     * @param keyword
     * @return
     */
    public boolean updateAudioRecordInfo(int id,
                                         String name,
                                         String person,
                                         String event,
                                         String keyword,
                                         String data,
                                         int isUpload) {
        int result = 0;
        try {
            openDatabase();
            ContentValues values = new ContentValues();
            values.put(TableConstant.TABLE_AUDIO_RECORD_DISPLAY_NAME, name + ".mp3");
            values.put(TableConstant.TABLE_AUDIO_RECORD_TITLE, name);
            values.put(TableConstant.TABLE_AUDIO_RECORD_INTERVIEW_PERSION, person);
            values.put(TableConstant.TABLE_AUDIO_RECORD_INTERVIEW_EVENT, event);
            values.put(TableConstant.TABLE_AUDIO_RECORD_INTERVIEW_KEYWORD, keyword);
            values.put(TableConstant.TABLE_AUDIO_RECORD_DATA, data);
            values.put(TableConstant.TABLE_AUDIO_RECORD_IS_UPLOAD, isUpload);

            //更新数据
            result = mDb.update(TableConstant.TABLE_AUDIO_RECORD_TABLE_NAME, values, "_id=?", new String[]{String.valueOf(id)});
        } catch (Exception e) {
        } finally {
            closeDatabase();
        }
        return result == 0 ? false : true;
    }

    public UploadFirstResponse getUploadInfo(int id) {
        UploadFirstResponse response = null;

        Cursor mCursor = null;
        try {
            openDatabase();
            mCursor = mDb.query(TableConstant.TABLE_AUDIO_RECORD_TABLE_NAME,
                    null,
                    TableConstant.TABLE_AUDIO_RECORD_ID + "= ? and " + TableConstant.TABLE_AUDIO_RECORD_USER_ID + "=?",
                    new String[]{
                            String.valueOf(id),
                            String.valueOf(AccoutConfiguration.getLoginInfo().getUserid())
                    },
                    null, null, null);

            if (mCursor != null && mCursor.getCount() > 0) {
                for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                    int sliceId = mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_SLICE_ID));
                    int sliceCount = mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_SLICE_COUNT));
                    String successIds = mCursor.getString(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_SLICE_SUCCESS));
                    response = new UploadFirstResponse(sliceId, sliceCount, successIds);
                }
            }
        } catch (Exception e) {
        } finally {
            closeCursor(mCursor);
            closeDatabase();
        }

        return response;
    }
}
