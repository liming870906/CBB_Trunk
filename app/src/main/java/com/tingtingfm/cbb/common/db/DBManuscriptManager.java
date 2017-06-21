package com.tingtingfm.cbb.common.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.tingtingfm.cbb.bean.ManuscriptInfo;
import com.tingtingfm.cbb.bean.MediaInfo;
import com.tingtingfm.cbb.bean.UploadFirstResponse;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.configuration.PreferencesConfiguration;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tianhu on 17/01/10.
 */

public class DBManuscriptManager {
    private static volatile DBManuscriptManager instance;
    private AtomicInteger mOpenCounter = new AtomicInteger();
    private SQLiteDatabase mDb;
    private DatabaseHelper helper;
    /**
     * 用户标识
     */
    private static String userId;
    /**
     * 构造方法
     *
     * @param context
     */
    private DBManuscriptManager(Context context) {
        helper = new DatabaseHelper(context);
    }

    /**
     * 获得数据库管理对象
     *
     * @param context
     * @return
     */
    public static DBManuscriptManager getInstance(Context context) {
        if (instance == null) {
            synchronized (DBAudioRecordManager.class) {
                if (instance == null) {
                    instance = new DBManuscriptManager(context.getApplicationContext());
                }
            }
        }
        userId = PreferencesConfiguration.getSValues(Constants.USER_ID);
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

    ///********* 草稿信息保存 start ************///

    /**
     * 添加稿件
     *
     * @param mInfo
     * @return  返回当前插入行id
     */
    public int addManuscriptInfo(ManuscriptInfo mInfo) {
        int flag = -1;
        try {
            openDatabase();
            ContentValues values = new ContentValues();
            values.put(TableConstant.TABLE_MANUSCRIPT_TITLE, mInfo.getTitle());
            values.put(TableConstant.TABLE_MANUSCRIPT_ALIAS_TITLE, mInfo.getAliasTitle());
            values.put(TableConstant.TABLE_MANUSCRIPT_AUTHER, mInfo.getAuther());
            values.put(TableConstant.TABLE_MANUSCRIPT_HTML_TEXT, mInfo.getHtmlText());
            values.put(TableConstant.TABLE_MANUSCRIPT_CREATE_TIME, mInfo.getCreateTime());
            values.put(TableConstant.TABLE_MANUSCRIPT_MANUS_TEXT, mInfo.getManuscriptText());
            values.put(TableConstant.TABLE_MANUSCRIPT_CHARCOUNT, mInfo.getCharCount());
            values.put(TableConstant.TABLE_MANUSCRIPT_USER_ID,userId);
            flag = (int) mDb.insert(TableConstant.TABLE_MANUSCRIPT_TABLE_NAME, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return flag;
    }

    /**
     * 删除稿件
     *
     * @param id 稿件id
     * @return
     */
    public boolean deleteManuscriptInfo(int id) {
        boolean flag = false;
        try {
            openDatabase();
            flag = mDb.delete(TableConstant.TABLE_MANUSCRIPT_TABLE_NAME, "_id =?", new String[]{"" + id}) > 0 ? true : false;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return flag;
    }

    /**
     * 更新搞件
     *
     * @param info 稿件对象
     * @return
     */
    public boolean updataManuscriptInfo(ManuscriptInfo info) {
        boolean result = false;
        try {
            openDatabase();
            ContentValues values = new ContentValues();
            values.put(TableConstant.TABLE_MANUSCRIPT_TITLE, info.getTitle());
            values.put(TableConstant.TABLE_MANUSCRIPT_ALIAS_TITLE, info.getAliasTitle());
            values.put(TableConstant.TABLE_MANUSCRIPT_AUTHER, info.getAuther());
            values.put(TableConstant.TABLE_MANUSCRIPT_HTML_TEXT, info.getHtmlText());
            values.put(TableConstant.TABLE_MANUSCRIPT_MANUS_TEXT, info.getManuscriptText());
            values.put(TableConstant.TABLE_MANUSCRIPT_CHARCOUNT, info.getCharCount());
            values.put(TableConstant.TABLE_MANUSCRIPT_TEXTEDIT, info.getTextEdit());
            result = mDb.update(TableConstant.TABLE_MANUSCRIPT_TABLE_NAME, values, "_id=?", new String[]{info.getId() + ""}) > 0 ? true : false;
        } catch (Exception e) {
        } finally {
            closeDatabase();
        }
        return result;
    }

    /**
     * 更新搞件net_id
     *
     * @param info 稿件对象
     * @return
     */
    public boolean updataManuscriptNetId(ManuscriptInfo info) {
        boolean result = false;
        try {
            openDatabase();
            ContentValues values = new ContentValues();
            values.put(TableConstant.TABLE_MANUSCRIPT_NET_ID, info.getServerId());
//            values.put(TableConstant.TABLE_MANUSCRIPT_UPLOAD_STATE, info.getUploadState());
            result = mDb.update(TableConstant.TABLE_MANUSCRIPT_TABLE_NAME, values, "_id=?", new String[]{info.getId() + ""}) > 0 ? true : false;
        } catch (Exception e) {
        } finally {
            closeDatabase();
        }
        return result;
    }

    /**
     * 更新上传状态
     *
     * @param info 稿件对象
     * @return
     */
    public boolean updataManuscriptUploadState(ManuscriptInfo info) {
        boolean result = false;
        try {
            openDatabase();
            ContentValues values = new ContentValues();
            values.put(TableConstant.TABLE_MANUSCRIPT_UPLOAD_STATE, info.getUploadState());
            result = mDb.update(TableConstant.TABLE_MANUSCRIPT_TABLE_NAME, values, "_id=?", new String[]{info.getId() + ""}) > 0 ? true : false;
        } catch (Exception e) {
        } finally {
            closeDatabase();
        }
        return result;
    }

    /**
     * 更新标记文本内容更改过
     *
     * @param info 稿件对象
     * @return
     */
    public boolean updataManuscriptTextEdit(ManuscriptInfo info) {
        boolean result = false;
        try {
            openDatabase();
            ContentValues values = new ContentValues();
            values.put(TableConstant.TABLE_MANUSCRIPT_TEXTEDIT, info.getTextEdit());
            result = mDb.update(TableConstant.TABLE_MANUSCRIPT_TABLE_NAME, values, "_id=?", new String[]{info.getId() + ""}) > 0 ? true : false;
        } catch (Exception e) {
        } finally {
            closeDatabase();
        }
        return result;
    }

    /**
     * 更新是否提交后
     *
     * @param info 稿件对象
     * @return
     */
    public boolean updataManuscriptSubmitState(ManuscriptInfo info) {
        boolean result = false;
        try {
            openDatabase();
            ContentValues values = new ContentValues();
//            values.put(TableConstant.TABLE_MANUSCRIPT_UPLOAD_STATE, info.getUploadState());
            values.put(TableConstant.TABLE_MANUSCRIPT_ISSUBMIT, info.getIsSubmit());
            result = mDb.update(TableConstant.TABLE_MANUSCRIPT_TABLE_NAME, values, "_id=?", new String[]{info.getId() + ""}) > 0 ? true : false;
        } catch (Exception e) {
        } finally {
            closeDatabase();
        }
        return result;
    }

    /**
     * 通过id查询稿件
     *
     * @param id 稿件id
     * @return
     */
    public ManuscriptInfo findManuscriptInfo(int id) {
        ManuscriptInfo info = null;
        Cursor mCursor = null;
        try {
            openDatabase();
            mCursor = mDb.query(TableConstant.TABLE_MANUSCRIPT_TABLE_NAME, null, "_id=?", new String[]{id + ""}, null, null, null);
            if (mCursor != null && mCursor.getCount() > 0) {
                mCursor.moveToFirst();
                info = getManuscriptInfo(mCursor);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        } finally {
            closeCursor(mCursor);
            closeDatabase();
        }
        return info;
    }


    /**
     * 获取搞件数据
     * @param mCursor
     * @return
     */
    private ManuscriptInfo getManuscriptInfo(Cursor mCursor) {
        ManuscriptInfo info = new ManuscriptInfo();
        info.setId(mCursor.getInt(mCursor.getColumnIndex("_id")));
        info.setServerId(mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_MANUSCRIPT_NET_ID)));
        info.setTitle(mCursor.getString(mCursor.getColumnIndex(TableConstant.TABLE_MANUSCRIPT_TITLE)));
        info.setAliasTitle(mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_MANUSCRIPT_ALIAS_TITLE)));
        info.setAuther(mCursor.getString(mCursor.getColumnIndex(TableConstant.TABLE_MANUSCRIPT_AUTHER)));
        info.setHtmlText(mCursor.getString(mCursor.getColumnIndex(TableConstant.TABLE_MANUSCRIPT_HTML_TEXT)));
        info.setUploadState(mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_MANUSCRIPT_UPLOAD_STATE)));
        info.setCreateTime(mCursor.getString(mCursor.getColumnIndex(TableConstant.TABLE_MANUSCRIPT_CREATE_TIME)));
        info.setIsSubmit(mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_MANUSCRIPT_ISSUBMIT)));
        info.setManuscriptText(mCursor.getString(mCursor.getColumnIndex(TableConstant.TABLE_MANUSCRIPT_MANUS_TEXT)));
        info.setCharCount(mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_MANUSCRIPT_CHARCOUNT)));
        info.setTextEdit(mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_MANUSCRIPT_TEXTEDIT)));
        return info;
    }

    /**
     * 通过title查询稿件
     *
     * @param title 稿件标题
     * @return
     */
    public ManuscriptInfo findManuscriptInfo(String title) {
        ManuscriptInfo info = null;
        Cursor mCursor = null;
        try {
            openDatabase();
            mCursor = mDb.query(TableConstant.TABLE_MANUSCRIPT_TABLE_NAME, null,
                    TableConstant.TABLE_MANUSCRIPT_TITLE+"=? AND userId = ?",
                    new String[]{title,userId}, null, null, null);
            if (mCursor != null && mCursor.getCount() > 0) {
                mCursor.moveToFirst();
                info = getManuscriptInfo(mCursor);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        } finally {
            closeCursor(mCursor);
            closeDatabase();
        }
        return info;
    }

    /**
     * 稿件搜索模糊查询
     * @param title 稿件标题
     * @return
     */
    public ArrayList<ManuscriptInfo> findManuscriptByTitle(String title){
        ArrayList<ManuscriptInfo> list = new ArrayList<ManuscriptInfo>();
        Cursor mCursor = null;
        try {
            openDatabase();
            mCursor =  mDb.rawQuery("SELECT * FROM "+TableConstant.TABLE_MANUSCRIPT_TABLE_NAME
                    +" WHERE "+TableConstant.TABLE_MANUSCRIPT_TITLE+" LIKE '%"+ title +"%' "+
                    " AND userId = "+userId+
                    " ORDER BY _id desc", null);
            if (null != mCursor && mCursor.getCount() > 0) {
                while (mCursor.moveToNext()) {
                    list.add(getManuscriptInfo(mCursor));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeCursor(mCursor);
            closeDatabase();
        }
        return list;
    }


    /**
     * 获取所有搞件
     */
    public ArrayList<ManuscriptInfo> getManuscriptInfos() {
        ArrayList<ManuscriptInfo> list = new ArrayList<ManuscriptInfo>();
        Cursor mCursor = null;
        try {
            openDatabase();
            mCursor = mDb.query(TableConstant.TABLE_MANUSCRIPT_TABLE_NAME, null, "userId=?",
                    new String[]{userId},
                    "", "", "_id desc");
            if (null != mCursor && mCursor.getCount() > 0) {
                while (mCursor.moveToNext()) {
                    list.add(getManuscriptInfo(mCursor));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeCursor(mCursor);
            closeDatabase();
        }
        return list;
    }

    /**
     * 获取所有上传中稿件data
     * @return
     */
    public ArrayList<ManuscriptInfo> getUploadManuscriptDatas() {
        ArrayList<ManuscriptInfo> list = new ArrayList<ManuscriptInfo>();
        Cursor mCursor = null;
        try {
            openDatabase();
            mCursor = mDb.query(TableConstant.TABLE_MANUSCRIPT_TABLE_NAME, null, "uploadState=?",
                    new String[]{2+""},
                    "", "", "");
            if (null != mCursor && mCursor.getCount() > 0) {
                while (mCursor.moveToNext()) {
                    list.add(getManuscriptInfo(mCursor));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeCursor(mCursor);
            closeDatabase();
        }
        return list;
    }
    /**
     * 查询已有名称，最大别名值
     * @param title  稿件标题
     * @param id  稿件id
     * @return
     */
    public int  getCurrentManuscriptInfos(String title,int id) {
        int sliasNum = -1;
        Cursor mCursor = null;
        try {
            openDatabase();
            mCursor = mDb.query(TableConstant.TABLE_MANUSCRIPT_TABLE_NAME, null,
                    TableConstant.TABLE_MANUSCRIPT_TITLE+" = ? AND _id != ? AND userId = ?",
                    new String[]{title,""+id,userId}, null, null, null);
            if (mCursor != null && mCursor.getCount() > 0) {
                while (mCursor.moveToNext()) {
                    int aliasTitle = mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_MANUSCRIPT_ALIAS_TITLE));
                    if(aliasTitle > sliasNum){
                        sliasNum =aliasTitle;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        } finally {
            closeCursor(mCursor);
            closeDatabase();
        }
        return sliasNum;
    }

    /********* 草稿信息保存 end ****************/

    /********* 草稿音频数据保存 start *************/

    /**
     * 添加音频数据
     * @param mediaInfo 音频对象
     */
    public int saveAudioInfo(MediaInfo mediaInfo) {
        int flag = -1;
        try {
            openDatabase();
            ContentValues values = new ContentValues();
            values.put(TableConstant.TABLE_MANUSCRIPT_ID, mediaInfo.getManuscriptId());
            values.put(TableConstant.TABLE_AUDIO_RECORD_MEDIA_ID, mediaInfo.getMedia_id());
            values.put(TableConstant.TABLE_AUDIO_RECORD_DATA, mediaInfo.getAbsolutePath());
            values.put(TableConstant.TABLE_AUDIO_RECORD_SIZE, mediaInfo.getSize());
            values.put(TableConstant.TABLE_AUDIO_RECORD_DISPLAY_NAME, mediaInfo.getFullName());
            values.put(TableConstant.TABLE_AUDIO_RECORD_MIME_TYPE, mediaInfo.getMime_type());
            values.put(TableConstant.TABLE_AUDIO_RECORD_TITLE, mediaInfo.getTitle());
            values.put(TableConstant.TABLE_AUDIO_RECORD_DATE_ADDED, mediaInfo.getDate_added());
            values.put(TableConstant.TABLE_AUDIO_RECORD_DATE_MODIFIED, mediaInfo.getDate_modified());
            values.put(TableConstant.TABLE_AUDIO_RECORD_UPLOAD_STATUS, mediaInfo.getUpload_status());
            values.put(TableConstant.TABLE_AUDIO_RECORD_DURATION, mediaInfo.getDuration());
            
            flag = (int) mDb.insert(TableConstant.TABLE_MANUSCRIPT_AUDIO_TABLE_NAME, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return flag;
    }

    /**
     * 删除某稿件中所有音频数据
     * @param manuscriptId 稿件id
     */
    public boolean deleteManuAllAudioInfo(int manuscriptId){
        boolean flag = false;
        try {
            openDatabase();
            flag = mDb.delete(TableConstant.TABLE_MANUSCRIPT_AUDIO_TABLE_NAME,
                    TableConstant.TABLE_MANUSCRIPT_ID+" =? ", new String[]{"" + manuscriptId}) > 0 ? true : false;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return flag;
    }

    /**
     * 删除音频数据
     * @param id 音频id
     */
    public boolean deleteAudioInfo(int id) {
        boolean flag = false;
        try {
            openDatabase();
            flag = mDb.delete(TableConstant.TABLE_MANUSCRIPT_AUDIO_TABLE_NAME, "_id =?", new String[]{"" + id}) > 0 ? true : false;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return flag;
    }

    /**
     * 查找音频数据
     * @param id 稿件id
     */
    public ArrayList<MediaInfo> findAudioInfo(int id){
        ArrayList<MediaInfo> list = new ArrayList<MediaInfo>();
        Cursor mCursor = null;
        try {
            openDatabase();
            mCursor = mDb.query(TableConstant.TABLE_MANUSCRIPT_AUDIO_TABLE_NAME, null,
                    TableConstant.TABLE_MANUSCRIPT_ID+" = ? ",
                    new String[]{id+""}, null, null, null);

            if (null != mCursor && mCursor.getCount() > 0) {
                while (mCursor.moveToNext()) {
                    list.add(getMediaInfoInfo(mCursor));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeCursor(mCursor);
            closeDatabase();
        }
        return list;
    }

    /**
     * 获取搞件数据
     * @param mCursor
     * @return
     */
    private MediaInfo getMediaInfoInfo(Cursor mCursor) {
        MediaInfo info = new MediaInfo();
        info.setManuscriptId(mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_MANUSCRIPT_ID)));
        info.setId(mCursor.getInt(mCursor.getColumnIndex("_id")));
        info.setTitle(mCursor.getString(mCursor.getColumnIndex(TableConstant.TABLE_MANUSCRIPT_TITLE)));
        info.setAbsolutePath(mCursor.getString(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_DATA)));
        info.setDate_added(mCursor.getLong(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_DATE_ADDED)));
        info.setDate_modified(mCursor.getLong(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_DATE_MODIFIED)));
        info.setFullName(mCursor.getString(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_DISPLAY_NAME)));
        info.setMedia_id(mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_MEDIA_ID)));
        info.setMime_type(mCursor.getString(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_MIME_TYPE)));
        info.setSize(mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_SIZE)));
        info.setDuration(mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_DURATION)));
        info.setUpload_status(mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_UPLOAD_STATUS)));
        info.setIsUpdateAudioInfo(mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_AUDIO_RECORD_IS_UPLOAD)));
        info.setAudioNetPath(mCursor.getString(mCursor.getColumnIndex(TableConstant.TABLE_MANUSCRIPT_AUDIO_NET_PATH)));
        info.setSliceId(mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_MANUSCRIPT_SLICE_ID)));
        info.setSliceCount(mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_MANUSCRIPT_SLICE_COUNT)));
        info.setSuccessIds(mCursor.getString(mCursor.getColumnIndex(TableConstant.TABLE_MANUSCRIPT_SLICE_SUCCESS)));
        return info;
    }

    /**
     * 获取指定稿件下的音频信息
     * @param manuscriptId 稿件id
     * @return
     */
    public ArrayList<MediaInfo> getMediaInfoInfos(int manuscriptId) {
        ArrayList<MediaInfo> list = new ArrayList<MediaInfo>();
        Cursor mCursor = null;
        try {
            openDatabase();
            mCursor = mDb.query(TableConstant.TABLE_MANUSCRIPT_AUDIO_TABLE_NAME, null, "manuscript_id=?", new String[]{manuscriptId + ""}, null, null, null);
            if (null != mCursor && mCursor.getCount() > 0) {
                while (mCursor.moveToNext()) {
                    list.add(getMediaInfoInfo(mCursor));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeCursor(mCursor);
            closeDatabase();
        }
        return list;
    }

    /**
     * 音频上传成功，更新音频上传状态
     * @param audiofile 音频对象
     */
    public boolean MediaInfoUploadState(MediaInfo audiofile) {
        boolean result = false;
        try {
            openDatabase();
            ContentValues values = new ContentValues();
            values.put(TableConstant.TABLE_AUDIO_RECORD_IS_UPLOAD, audiofile.getIsUpdateAudioInfo());
            values.put(TableConstant.TABLE_MANUSCRIPT_AUDIO_NET_PATH, audiofile.getAudioNetPath());
            result = mDb.update(TableConstant.TABLE_MANUSCRIPT_AUDIO_TABLE_NAME, values, "_id=?", new String[]{audiofile.getId() + ""}) > 0 ? true : false;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return result;
    }

    /**
     * 更新稿件音频数据
     * @param info 音频对象
     * @return
     */
    public boolean updateAudioInfo(MediaInfo info) {
        boolean result = false;
        try {
            openDatabase();
            ContentValues values = new ContentValues();
            values.put(TableConstant.TABLE_MANUSCRIPT_SLICE_COUNT, info.getSliceCount());
            values.put(TableConstant.TABLE_MANUSCRIPT_SLICE_ID, info.getSliceId());
            values.put(TableConstant.TABLE_MANUSCRIPT_SLICE_SUCCESS, info.getSuccessIds());
            result = mDb.update(TableConstant.TABLE_MANUSCRIPT_AUDIO_TABLE_NAME, values,
                    " _id = ?", new String[]{info.getId() + ""}) > 0 ? true : false;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return result;
    }

    /**
     * 获取稿件音频数据
     * @param id 音频id
     * @return
     */
    public UploadFirstResponse getUploadInfo(int id) {
        UploadFirstResponse response = null;

        Cursor mCursor = null;
        try {
            openDatabase();
            mCursor = mDb.query(TableConstant.TABLE_MANUSCRIPT_AUDIO_TABLE_NAME,
                    null,
                    "_id = ?",
                    new String[] {
                            String.valueOf(id)
                    },
                    null, null, null);

            if (mCursor != null && mCursor.getCount() > 0) {
                for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                    int sliceId = mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_MANUSCRIPT_SLICE_ID));
                    int sliceCount = mCursor.getInt(mCursor.getColumnIndex(TableConstant.TABLE_MANUSCRIPT_SLICE_COUNT));
                    String successIds = mCursor.getString(mCursor.getColumnIndex(TableConstant.TABLE_MANUSCRIPT_SLICE_SUCCESS));
                    response = new UploadFirstResponse(sliceId, sliceCount, successIds);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeCursor(mCursor);
            closeDatabase();
        }

        return response;
    }


    /********* 草稿音频数据保存 end *************/
}
