package com.tingtingfm.cbb.bean;


import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;

/**
 * Created by liming on 16/12/28.
 */

public class MediaInfo implements Parcelable {
    public static final int SINGLE_SLICE_COUNT = 5 * 1024 * 1024;
    //仅稿件管理-音频属性
    private int manuscriptId;
    //仅稿件管理-网络音频路径
    private String audioNetPath;

    //标记ID
    private int id;
    //服务器返回ID 如没有返回默认为-1
    private int media_id;
    //媒体资源地址（绝对路径）
    private String absolutePath;
    //媒体文件大小
    private long size;
    //媒体文件名称（如：xxx.jpg)
    private String fullName;
    //媒体类型——参考常量类中
    private String mime_type;
    //媒体标题——没有扩展名
    private String title;
    //媒体文件添加时间
    private long date_added;
    //媒体文件修改时间
    private long date_modified;
    //上传状态——参考常量类中
    private int upload_status;
    //音频文件时长
    private int duration;
    //采访人员
    private String interview_persion;
    //采访事件
    private String interview_event;
    //关键词
    private String interview_keyword;
    //经度
    private double longitude;
    //纬度
    private double latitude;
    //定位地点
    private String place;
    //是否上传
    private int isUpdateAudioInfo;
    //账户ID
    private int user_id;

    private int sliceId;
    private int sliceCount;
    private String successIds;

    private int extraInteger;
    private String extraString;

    public MediaInfo() {
    }

    /**
     * @param id
     * @param media_id
     * @param absolutePath
     * @param size
     * @param fullName
     * @param mime_type
     * @param title
     * @param date_added
     * @param date_modified
     * @param upload_status
     */
    public MediaInfo(int id,
                     int media_id,
                     String absolutePath,
                     long size,
                     String fullName,
                     String mime_type,
                     String title,
                     long date_added,
                     long date_modified,
                     int upload_status,
                     int duration,
                     int user_id,
                     int sliceId,
                     int sliceCount,
                     String successIds) {
        this.id = id;
        this.media_id = media_id;
        this.absolutePath = absolutePath;
        this.size = size;
        this.fullName = fullName;
        this.mime_type = mime_type;
        this.title = title;
        this.date_added = date_added;
        this.date_modified = date_modified;
        this.upload_status = upload_status;
        this.duration = duration;
        this.user_id = user_id;
        this.sliceId = sliceId;
        this.sliceCount = sliceCount;
        this.successIds = successIds;
    }

    protected MediaInfo(Parcel in) {
        manuscriptId = in.readInt();
        id = in.readInt();
        media_id = in.readInt();
        absolutePath = in.readString();
        size = in.readLong();
        fullName = in.readString();
        mime_type = in.readString();
        title = in.readString();
        date_added = in.readLong();
        date_modified = in.readLong();
        upload_status = in.readInt();
        duration = in.readInt();
        interview_persion = in.readString();
        interview_event = in.readString();
        interview_keyword = in.readString();
        longitude = in.readDouble();
        latitude = in.readDouble();
        place = in.readString();
        isUpdateAudioInfo = in.readInt();
        user_id = in.readInt();
        sliceId = in.readInt();
        sliceCount = in.readInt();
        successIds = in.readString();
        extraInteger = in.readInt();
        extraString = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(manuscriptId);
        dest.writeInt(id);
        dest.writeInt(media_id);
        dest.writeString(absolutePath);
        dest.writeLong(size);
        dest.writeString(fullName);
        dest.writeString(mime_type);
        dest.writeString(title);
        dest.writeLong(date_added);
        dest.writeLong(date_modified);
        dest.writeInt(upload_status);
        dest.writeInt(duration);
        dest.writeString(interview_persion);
        dest.writeString(interview_event);
        dest.writeString(interview_keyword);
        dest.writeDouble(longitude);
        dest.writeDouble(latitude);
        dest.writeString(place);
        dest.writeInt(isUpdateAudioInfo);
        dest.writeInt(user_id);
        dest.writeInt(sliceId);
        dest.writeInt(sliceCount);
        dest.writeString(successIds);
        dest.writeInt(extraInteger);
        dest.writeString(extraString);
    }

    @Override
    public int describeContents() {
        return 0;
    }


    public static final Creator<MediaInfo> CREATOR = new Creator<MediaInfo>() {
        @Override
        public MediaInfo createFromParcel(Parcel in) {

            return new MediaInfo(in);
        }

        @Override
        public MediaInfo[] newArray(int size) {
            return new MediaInfo[size];
        }
    };

    public String getAudioNetPath() {
        return audioNetPath;
    }

    public void setAudioNetPath(String audioNetPath) {
        this.audioNetPath = audioNetPath;
    }

    public int getManuscriptId() {
        return manuscriptId;
    }

    public void setManuscriptId(int manuscriptId) {
        this.manuscriptId = manuscriptId;
    }

    public int getMedia_id() {
        return media_id;
    }

    public void setMedia_id(int media_id) {
        this.media_id = media_id;
    }

    public int getUpload_status() {
        return upload_status;
    }

    public void setUpload_status(int upload_status) {
        this.upload_status = upload_status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getMime_type() {
        return mime_type;
    }

    public void setMime_type(String mime_type) {
        this.mime_type = mime_type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getDate_added() {
        return date_added;
    }

    public void setDate_added(long date_added) {
        this.date_added = date_added;
    }

    public long getDate_modified() {
        return date_modified;
    }

    public void setDate_modified(long date_modified) {
        this.date_modified = date_modified;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getInterview_persion() {
        return interview_persion;
    }

    public void setInterview_persion(String interview_persion) {
        this.interview_persion = getValue(interview_persion);
    }

    public String getInterview_event() {
        return interview_event;
    }

    public void setInterview_event(String interview_event) {
        this.interview_event = getValue(interview_event);
    }

    public String getInterview_keyword() {
        return interview_keyword;
    }

    public void setInterview_keyword(String interview_keyword) {
        this.interview_keyword = getValue(interview_keyword);
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = getValue(place);
    }

    public int getIsUpdateAudioInfo() {
        return isUpdateAudioInfo;
    }

    public void setIsUpdateAudioInfo(int isUpdateAudioInfo) {
        this.isUpdateAudioInfo = isUpdateAudioInfo;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getSliceId() {
        return sliceId;
    }

    public void setSliceId(int sliceId) {
        this.sliceId = sliceId;
    }

    public void setSliceCount(int sliceCount) {
        this.sliceCount = sliceCount;
    }

    public int getSliceCount() {
        return sliceCount;
    }

    public String getSuccessIds() {
        return successIds;
    }

    public void setSuccessIds(String successIds) {
        this.successIds = successIds;
    }

    public int getExtraInteger() {
        return extraInteger;
    }

    public void setExtraInteger(int extraInteger) {
        this.extraInteger = extraInteger;
    }

    public String getExtraString() {
        return extraString;
    }

    public void setExtraString(String extraString) {
        this.extraString = extraString;
    }

    private String getValue(String val) {
        String value = val;
        if (TextUtils.isEmpty(val)) {
            value = "";
        }

        if ("null".equals(val)) {
            value = "";
        }

        return value;
    }

    public long getFileSize() {
        if (TextUtils.isEmpty(absolutePath)) {
            return 0;
        }

        File file = new File(absolutePath);

        return file.length();
    }

    public int getDefaultSliceCount() {
        long size = getFileSize();
        int count = (int) (size / SINGLE_SLICE_COUNT);

        if (count == 0) {
            return 1;
        } else if (count * SINGLE_SLICE_COUNT == size) {
            return count;
        } else {
            return count + 1;
        }
    }

    public String getFileMd5() {
        if (TextUtils.isEmpty(absolutePath)) {
            return "";
        }

        File file = new File(absolutePath);
        MessageDigest md5 = null;
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            md5 = MessageDigest.getInstance("MD5");
            md5.update(byteBuffer);
//            BigInteger bi = new BigInteger(1, md5.digest());
//            md5Value = bi.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return bytesToHexString(md5.digest());
    }

    public String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public String getFileSuffix() {
        return fullName.substring(fullName.lastIndexOf(".") + 1);
    }

    /**
     * 检查是否上传音频信息(不包含音频文件)
     *
     * @return
     */
    public boolean checkUploadInfo() {
        String fileSuffix = getFileSuffix();
        if (fileSuffix.equals("mp3") && media_id > 0 && isUpdateAudioInfo == 1) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof MediaInfo) {
            MediaInfo mediaInfo = (MediaInfo) o;
            boolean isEquals = false;
            if (this.id == mediaInfo.getId()) {
                isEquals = true;
            }

//            if (checkUploadInfo()) {
//                isEquals = false;
//            }

            return isEquals;
        }
        return false;
    }

    @Override
    public String toString() {
        return "MediaInfo{" +
                "manuscriptId=" + manuscriptId +
                ", audioNetPath='" + audioNetPath + '\'' +
                ", id=" + id +
                ", media_id=" + media_id +
                ", absolutePath='" + absolutePath + '\'' +
                ", size=" + size +
                ", fullName='" + fullName + '\'' +
                ", mime_type='" + mime_type + '\'' +
                ", title='" + title + '\'' +
                ", date_added=" + date_added +
                ", date_modified=" + date_modified +
                ", upload_status=" + upload_status +
                ", duration=" + duration +
                ", interview_persion='" + interview_persion + '\'' +
                ", interview_event='" + interview_event + '\'' +
                ", interview_keyword='" + interview_keyword + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", place='" + place + '\'' +
                ", isUpdateAudioInfo=" + isUpdateAudioInfo +
                ", user_id=" + user_id +
                ", sliceId=" + sliceId +
                ", sliceCount=" + sliceCount +
                ", successIds='" + successIds + '\'' +
                ", extraInteger=" + extraInteger +
                ", extraString='" + extraString + '\'' +
                '}';
    }
}
