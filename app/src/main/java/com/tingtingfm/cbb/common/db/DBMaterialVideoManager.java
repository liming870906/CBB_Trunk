package com.tingtingfm.cbb.common.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.tingtingfm.cbb.bean.CommonResourceInfo;
import com.tingtingfm.cbb.bean.MediaInfo;
import com.tingtingfm.cbb.bean.UploadFirstResponse;
import com.tingtingfm.cbb.common.configuration.AccoutConfiguration;
import com.tingtingfm.cbb.common.configuration.Constants;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by liming on 16/12/29.
 */

public class DBMaterialVideoManager {
    private static volatile DBMaterialVideoManager instance;

    private AtomicInteger mOpenCounter = new AtomicInteger();
    private SQLiteDatabase mDb;
    private DatabaseHelper helper;

    /**
     * 构造方法
     *
     * @param context
     */
    private DBMaterialVideoManager(Context context) {
        helper = new DatabaseHelper(context);
    }

    /**
     * 获得数据库管理对象
     *
     * @param context
     * @return
     */
    public static DBMaterialVideoManager getInstance(Context context) {
        if (instance == null) {
            synchronized (DBAudioRecordManager.class) {
                if (instance == null) {
                    instance = new DBMaterialVideoManager(context.getApplicationContext());
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
     * 添加视频信息
     *
     * @param name
     * @param mimeType
     * @param uploadStatus
     * @return
     */
    public boolean addMaterialVideo(int id, int mediaId, String name, String mimeType, int uploadStatus, int userId) {
        boolean flag = false;
        try {
            openDatabase();
            ContentValues values = new ContentValues();
            values.put(TableConstant.TABLE_VIDEO_ID, id);
            values.put(TableConstant.TABLE_VIDEO_MEDIA_ID, mediaId);
            values.put(TableConstant.TABLE_VIDEO_DISPLAY_NAME, name);
            values.put(TableConstant.TABLE_VIDEO_MIME_TYPE, mimeType);
            values.put(TableConstant.TABLE_VIDEO_UPLOAD_STATUS, uploadStatus);
            values.put(TableConstant.TABLE_VIDEO_USER_ID, userId);

            flag = mDb.replace(TableConstant.TABLE_VIDEO_TABLE_NAME, null, values) != -1 ? true : false;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return flag;
    }

    /**
     * 删除所有视频信息
     *
     * @return
     */
    public boolean deleteAllMaterialVideo() {
        boolean result = false;
        try {
            openDatabase();
            result = mDb.delete(TableConstant.TABLE_VIDEO_TABLE_NAME, null, null) > 0 ? true : false;
        } catch (Exception e) {
        } finally {
            closeDatabase();
        }
        return result;
    }

    /**
     * 删除某条图片信息
     *
     * @param name
     * @param mimeType
     * @return
     */
    public boolean deleteMaterialVideo(String name, String mimeType) {
        boolean result = false;
        try {
            openDatabase();
            result = mDb.delete(TableConstant.TABLE_VIDEO_TABLE_NAME, TableConstant.TABLE_VIDEO_DISPLAY_NAME + "=? and " + TableConstant.TABLE_VIDEO_MIME_TYPE + "=?", new String[]{name, mimeType}) > 0 ? true : false;
        } catch (Exception e) {
        } finally {
            closeDatabase();
        }
        return result;
    }

    /**
     * 查询所有视频信息
     *
     * @return
     */
    public CommonResourceInfo queryVideoInfo(int id, String name, String mimeType, int userId) {
        CommonResourceInfo resourceInfo = new CommonResourceInfo();
        Cursor mCursor = null;
        try {
            openDatabase();
            mCursor = mDb.query(TableConstant.TABLE_VIDEO_TABLE_NAME,
                    null,
                    TableConstant.TABLE_VIDEO_DISPLAY_NAME + "=? and " + TableConstant.TABLE_VIDEO_MIME_TYPE + "=? and " + TableConstant.TABLE_VIDEO_USER_ID + "=?",
                    new String[]{name, mimeType, String.valueOf(userId)},
                    null, null, "_id desc");
            if (mCursor != null && mCursor.getCount() > 0) {
                for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                    resourceInfo.uploadStatus = mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_VIDEO_UPLOAD_STATUS));
                    resourceInfo.media_id = mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_VIDEO_MEDIA_ID));
                    resourceInfo.user_id = mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_VIDEO_USER_ID));
                    resourceInfo.sliceId = mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_VIDEO_SLICE_ID));
                    resourceInfo.sliceCount = mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_VIDEO_SLICE_COUNT));
                    resourceInfo.successIds = mCursor.getString(mCursor.getColumnIndex(TableConstant.TABLE_VIDEO_SLICE_SUCCESS));
                }
            }
        } catch (Exception e) {
        } finally {
            closeCursor(mCursor);
            closeDatabase();
        }
        //没有数据
        if (resourceInfo.uploadStatus == -1) {
            //添加数据
            addMaterialVideo(id, resourceInfo.media_id, name, mimeType, Constants.UPLOAD_STATUS_DEFAULT, userId);
            //设置返回值
            resourceInfo.uploadStatus = Constants.UPLOAD_STATUS_DEFAULT;
            resourceInfo.user_id = userId;
        }
        return resourceInfo;
    }

    /**
     * 更新服务器ID
     *
     * @param info
     * @return
     */
    public boolean updateVideoMediaID(MediaInfo info) {
        int result = 0;
        try {
            openDatabase();
            ContentValues values = new ContentValues();
            values.put(TableConstant.TABLE_VIDEO_MEDIA_ID, info.getMedia_id());
            values.put(TableConstant.TABLE_VIDEO_UPLOAD_STATUS, info.getUpload_status());
            values.put(TableConstant.TABLE_VIDEO_SLICE_ID, info.getSliceId());
            values.put(TableConstant.TABLE_VIDEO_SLICE_COUNT, info.getSliceCount());
            values.put(TableConstant.TABLE_VIDEO_SLICE_SUCCESS, info.getSuccessIds());
            //更新数据
            result = mDb.update(TableConstant.TABLE_VIDEO_TABLE_NAME, values, "_id=?", new String[]{String.valueOf(info.getId())});
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
            mCursor = mDb.query(TableConstant.TABLE_VIDEO_TABLE_NAME,
                    null,
                    TableConstant.TABLE_AUDIO_RECORD_ID + "= ? and " + TableConstant.TABLE_AUDIO_RECORD_USER_ID + "=?",
                    new String[] {
                            String.valueOf(id),
                            String.valueOf(AccoutConfiguration.getLoginInfo().getUserid())
                    },
                    null, null, null);

            if (mCursor != null && mCursor.getCount() > 0) {
                for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                    int sliceId = mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_VIDEO_SLICE_ID));
                    int sliceCount = mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_VIDEO_SLICE_COUNT));
                    String successIds = mCursor.getString(mCursor.getColumnIndex(TableConstant.TABLE_VIDEO_SLICE_SUCCESS));
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
