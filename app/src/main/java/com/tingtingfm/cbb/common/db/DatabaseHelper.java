package com.tingtingfm.cbb.common.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author liming
 */
final class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "tingting_cbb_database";
    static final int DB_VERSION = 2;//1.0版本为 1

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //创建音频录音表
        createAudioRecordTable(db);
        //创建图片表
        createMaterialImageTable(db);
        //创建视频表
        createMaterialVideoTable(db);
        //创建稿件表
        createManuscriptTable(db);
        //创建稿件管理-草稿音频表
        createManuscriptAudio(db);

        onUpgrade(db, 1, DB_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql;
        switch (oldVersion) {
            case 1:
                sql = "ALTER TABLE " + TableConstant.TABLE_IMAGE_TABLE_NAME + " ADD COLUMN " + TableConstant.TABLE_IMAGE_SLICE_COUNT + " INTEGER";
                db.execSQL(sql);
                sql = "ALTER TABLE " + TableConstant.TABLE_IMAGE_TABLE_NAME + " ADD COLUMN " + TableConstant.TABLE_IMAGE_SLICE_ID + " INTEGER";
                db.execSQL(sql);
                sql = "ALTER TABLE " + TableConstant.TABLE_IMAGE_TABLE_NAME + " ADD COLUMN " + TableConstant.TABLE_IMAGE_SLICE_SUCCESS + " TEXT";
                db.execSQL(sql);

                sql = "ALTER TABLE " + TableConstant.TABLE_AUDIO_RECORD_TABLE_NAME + " ADD COLUMN " + TableConstant.TABLE_AUDIO_RECORD_SLICE_COUNT + " INTEGER";
                db.execSQL(sql);
                sql = "ALTER TABLE " + TableConstant.TABLE_AUDIO_RECORD_TABLE_NAME + " ADD COLUMN " + TableConstant.TABLE_AUDIO_RECORD_SLICE_ID + " INTEGER";
                db.execSQL(sql);
                sql = "ALTER TABLE " + TableConstant.TABLE_AUDIO_RECORD_TABLE_NAME + " ADD COLUMN " + TableConstant.TABLE_AUDIO_RECORD_SLICE_SUCCESS + " TEXT";
                db.execSQL(sql);

                sql = "ALTER TABLE " + TableConstant.TABLE_VIDEO_TABLE_NAME + " ADD COLUMN " + TableConstant.TABLE_VIDEO_SLICE_COUNT + " INTEGER";
                db.execSQL(sql);
                sql = "ALTER TABLE " + TableConstant.TABLE_VIDEO_TABLE_NAME + " ADD COLUMN " + TableConstant.TABLE_VIDEO_SLICE_ID + " INTEGER";
                db.execSQL(sql);
                sql = "ALTER TABLE " + TableConstant.TABLE_VIDEO_TABLE_NAME + " ADD COLUMN " + TableConstant.TABLE_VIDEO_SLICE_SUCCESS + " TEXT";
                db.execSQL(sql);

                sql = "ALTER TABLE " + TableConstant.TABLE_MANUSCRIPT_AUDIO_TABLE_NAME + " ADD COLUMN " + TableConstant.TABLE_MANUSCRIPT_SLICE_COUNT + " INTEGER";
                db.execSQL(sql);
                sql = "ALTER TABLE " + TableConstant.TABLE_MANUSCRIPT_AUDIO_TABLE_NAME + " ADD COLUMN " + TableConstant.TABLE_MANUSCRIPT_SLICE_ID + " INTEGER";
                db.execSQL(sql);
                sql = "ALTER TABLE " + TableConstant.TABLE_MANUSCRIPT_AUDIO_TABLE_NAME + " ADD COLUMN " + TableConstant.TABLE_MANUSCRIPT_SLICE_SUCCESS + " TEXT";
                db.execSQL(sql);

                //添加稿件插入音频上传后，音频网络地址，上传成功标志状态。
                sql = "ALTER TABLE " + TableConstant.TABLE_MANUSCRIPT_AUDIO_TABLE_NAME + " ADD COLUMN " + TableConstant.TABLE_AUDIO_RECORD_IS_UPLOAD + " INTEGER";
                db.execSQL(sql);
                sql = "ALTER TABLE " + TableConstant.TABLE_MANUSCRIPT_AUDIO_TABLE_NAME + " ADD COLUMN " + TableConstant.TABLE_MANUSCRIPT_AUDIO_NET_PATH + " TEXT";
                db.execSQL(sql);
                break;

        }
    }

    /**
     * 音频录音表
     *
     * @param db
     */
    public void createAudioRecordTable(SQLiteDatabase db) {
        String _sql = "CREATE TABLE IF NOT EXISTS "
                + TableConstant.TABLE_AUDIO_RECORD_TABLE_NAME + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TableConstant.TABLE_AUDIO_RECORD_MEDIA_ID + " INTEGER , "
                + TableConstant.TABLE_AUDIO_RECORD_DATA + " TEXT , "
                + TableConstant.TABLE_AUDIO_RECORD_SIZE + " INTEGER , "
                + TableConstant.TABLE_AUDIO_RECORD_DISPLAY_NAME + " TEXT , "
                + TableConstant.TABLE_AUDIO_RECORD_MIME_TYPE + " TEXT , "
                + TableConstant.TABLE_AUDIO_RECORD_TITLE + " TEXT , "
                + TableConstant.TABLE_AUDIO_RECORD_DATE_ADDED + " INTEGER , "
                + TableConstant.TABLE_AUDIO_RECORD_UPLOAD_STATUS + " INTEGER , "
                + TableConstant.TABLE_AUDIO_RECORD_DURATION + " INTEGER , "
                + TableConstant.TABLE_AUDIO_RECORD_INTERVIEW_PERSION + " TEXT , "
                + TableConstant.TABLE_AUDIO_RECORD_INTERVIEW_EVENT + " TEXT , "
                + TableConstant.TABLE_AUDIO_RECORD_INTERVIEW_KEYWORD + " TEXT , "
                + TableConstant.TABLE_AUDIO_RECORD_LONGITUDE + " double , "
                + TableConstant.TABLE_AUDIO_RECORD_LATITUDE + " double , "
                + TableConstant.TABLE_AUDIO_RECORD_PLACE + " TEXT , "
                + TableConstant.TABLE_AUDIO_RECORD_IS_UPLOAD + " INTEGER , "
                + TableConstant.TABLE_AUDIO_RECORD_USER_ID + " INTEGER , "
                + TableConstant.TABLE_AUDIO_RECORD_DATE_MODIFIED + " INTEGER);";
        db.execSQL(_sql);
    }

    public void createMaterialImageTable(SQLiteDatabase db) {
        String _sql = "CREATE TABLE IF NOT EXISTS "
                + TableConstant.TABLE_IMAGE_TABLE_NAME + " ("
                + TableConstant.TABLE_IMAGE_ID + " INTEGER , "
                + TableConstant.TABLE_IMAGE_MEDIA_ID + " INTEGER , "
                + TableConstant.TABLE_IMAGE_DISPLAY_NAME + " TEXT , "
                + TableConstant.TABLE_IMAGE_MIME_TYPE + " TEXT , "
                + TableConstant.TABLE_IMAGE_USER_ID + " INTEGER , "
                + TableConstant.TABLE_IMAGE_UPLOAD_STATUS + " INTEGER);";
        db.execSQL(_sql);
    }

    public void createMaterialVideoTable(SQLiteDatabase db) {
        String _sql = "CREATE TABLE IF NOT EXISTS "
                + TableConstant.TABLE_VIDEO_TABLE_NAME + " ("
                + TableConstant.TABLE_VIDEO_ID + " INTEGER , "
                + TableConstant.TABLE_VIDEO_MEDIA_ID + " INTEGER , "
                + TableConstant.TABLE_VIDEO_DISPLAY_NAME + " TEXT , "
                + TableConstant.TABLE_VIDEO_MIME_TYPE + " TEXT , "
                + TableConstant.TABLE_VIDEO_USER_ID + " INTEGER , "
                + TableConstant.TABLE_VIDEO_UPLOAD_STATUS + " INTEGER);";
        db.execSQL(_sql);
    }

    /**
     * 创建稿件表
     *
     * @param db
     */
    private void createManuscriptTable(SQLiteDatabase db) {
        String _sql = "CREATE TABLE IF NOT EXISTS "
                + TableConstant.TABLE_MANUSCRIPT_TABLE_NAME + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TableConstant.TABLE_MANUSCRIPT_TITLE + " TEXT , "
                + TableConstant.TABLE_MANUSCRIPT_NET_ID + " INTEGER , "
                + TableConstant.TABLE_MANUSCRIPT_ALIAS_TITLE + " INTEGER , "
                + TableConstant.TABLE_MANUSCRIPT_AUTHER + " TEXT , "
                + TableConstant.TABLE_MANUSCRIPT_HTML_TEXT + " TEXT , "
                + TableConstant.TABLE_MANUSCRIPT_MANUS_TEXT + " TEXT , "
                + TableConstant.TABLE_MANUSCRIPT_CREATE_TIME + " TEXT , "
                + TableConstant.TABLE_MANUSCRIPT_USER_ID + " TEXT , "
                + TableConstant.TABLE_MANUSCRIPT_ISSUBMIT + " INTEGER , "
                + TableConstant.TABLE_MANUSCRIPT_CHARCOUNT + " INTEGER , "
                + TableConstant.TABLE_MANUSCRIPT_TEXTEDIT + " INTEGER , "
                + TableConstant.TABLE_MANUSCRIPT_POSTILNUM + " INTEGER , "
                + TableConstant.TABLE_MANUSCRIPT_UPLOAD_STATE + " INTEGER ,"
                + TableConstant.TABLE_MANUSCRIPT_POSTILNUM + " INTEGER , "
                + TableConstant.TABLE_MANUSCRIPT_CLAIMSTATE + " TEXT , "
                + TableConstant.TABLE_MANUSCRIPT_APPROVESTATE + " INTEGER , "
                + TableConstant.TABLE_MANUSCRIPT_PROCESSID + " INTEGER , "
                + TableConstant.TABLE_MANUSCRIPT_ISMYCLAIM + " INTEGER , "
                + TableConstant.TABLE_MANUSCRIPT_ISOKCLAIM + " INTEGER , "
                + TableConstant.TABLE_MANUSCRIPT_MODIFYTIME + " TEXT);";
        db.execSQL(_sql);
    }

    /**
     * 创建稿件管理-草稿音频表
     *
     * @param db
     */
    private void createManuscriptAudio(SQLiteDatabase db) {
        String _sql = "CREATE TABLE IF NOT EXISTS "
                + TableConstant.TABLE_MANUSCRIPT_AUDIO_TABLE_NAME + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TableConstant.TABLE_MANUSCRIPT_ID + " INTEGER , "
                + TableConstant.TABLE_AUDIO_RECORD_MEDIA_ID + " INTEGER , "
                + TableConstant.TABLE_AUDIO_RECORD_DATA + " TEXT , "
                + TableConstant.TABLE_AUDIO_RECORD_SIZE + " INTEGER , "
                + TableConstant.TABLE_AUDIO_RECORD_DISPLAY_NAME + " TEXT , "
                + TableConstant.TABLE_AUDIO_RECORD_MIME_TYPE + " TEXT , "
                + TableConstant.TABLE_AUDIO_RECORD_TITLE + " TEXT , "
                + TableConstant.TABLE_AUDIO_RECORD_DATE_ADDED + " INTEGER , "
                + TableConstant.TABLE_AUDIO_RECORD_UPLOAD_STATUS + " INTEGER , "
                + TableConstant.TABLE_AUDIO_RECORD_DURATION + " INTEGER , "
                + TableConstant.TABLE_AUDIO_RECORD_DATE_MODIFIED + " INTEGER);";
        db.execSQL(_sql);
    }
}
