package com.tingtingfm.cbb.common.upload;

import android.text.TextUtils;

import com.tingtingfm.cbb.bean.MediaInfo;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.configuration.PreferencesConfiguration;
import com.tingtingfm.cbb.common.utils.AppUtils;
import com.tingtingfm.cbb.common.utils.DeviceUtils;
import com.tingtingfm.cbb.common.utils.encrypt.DesECBUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lqsir on 2017/4/17.
 */

public class ParamsHelper {
    private static Map<String, String> generateParams(Map<String, String> params) {
        final Map<String, String> requestParams = new HashMap<>();
        requestParams.put("client", "android_" + DeviceUtils.getTelephoneSerialNum());
        requestParams.put("version", "android_" + AppUtils.getVersionName());
        requestParams.put("session_key", PreferencesConfiguration.getSValues(Constants.SESSION_KEY));

        requestParams.putAll(params);

        requestParams.put("api_sign", DesECBUtil.getSecreteToken(requestParams));

        return requestParams;
    }

    public static Map<String, String> getStartUploadParams(MediaInfo info) {
        Map<String, String> params = new HashMap<>();
        params.put("location_type", "resource");
        params.put("size", info.getFileSize() + "");
        params.put("file_name", info.getFullName());
        params.put("split_counts", info.getDefaultSliceCount() + "");

        return ParamsHelper.generateParams(params);
    }

    public static Map<String, String> getSliceUploadParams(int identity, int slice) {
        Map<String, String> params = new HashMap<>();
        params.put("type", "5");
        params.put("serial_number", String.valueOf(slice));
        params.put("identity", String.valueOf(identity));

        return ParamsHelper.generateParams(params);
    }

    public static Map<String, String> getEndUploadParams(final MediaInfo info, int identity) {
        int type = getUploadType(info.getMime_type());
        final Map<String, String> params = new HashMap<>();
        params.put("material_type", getResourceType(info.getMime_type()));
        params.put("location_type", info.getManuscriptId() == 0 ? "resource" : "manuscript");
        params.put("identity", String.valueOf(identity));
        params.put("size", info.getFileSize() + "");
        params.put("file_name", info.getFullName());
        params.put("split_counts", info.getDefaultSliceCount() + "");
        params.put("file_md5", info.getFileMd5());

        if (info.getManuscriptId() != 0) {
            params.put("manuscript_id", info.getExtraInteger() + "");
        }
        //录音信息
        if (type == 2 && info.getManuscriptId() == 0) {
            params.put("keyword", getValue(info.getInterview_keyword()));
            params.put("longitude", info.getLongitude() + "");
            params.put("latitude", info.getLatitude() + "");
            params.put("place", getValue(info.getPlace()));
            params.put("audio_save_time", info.getDate_added() + "");
            params.put("people_interviewed", getValue(info.getInterview_persion()));
            params.put("event", getValue(info.getInterview_event()));
        }
        return ParamsHelper.generateParams(params);
    }

    public static Map<String, String> getAudioParams(MediaInfo info) {
        final Map<String, String> params = new HashMap<>();
        params.put("id", info.getMedia_id() + "");
        params.put("name", info.getFullName());
        params.put("people_interviewed", getValue(info.getInterview_persion()));
        params.put("event", getValue(info.getInterview_event()));
        params.put("keyword", getValue(info.getInterview_keyword()));

        return ParamsHelper.generateParams(params);
    }

    private static String getValue(String value) {
        return TextUtils.isEmpty(value) ? "" : value;
    }

    /**
     * 根据音频文件类型返回上传类型
     *
     * @param type 音频文件类型
     * @return 上传类型
     */
    public static int getUploadType(String type) {
        if (type.contains("image/")) {
            return 1;
        } else if (type.contains("audio/")) {
            return 2;
        } else if (type.contains("video/")) {
            return 3;
        }

        return 1;
    }

    public static String getResourceType(String type) {
        String material_type = "pic";
        switch (getUploadType(type)) {
            case 1:
                material_type = "pic";
                break;
            case 2:
                material_type = "audio";
                break;
            case 3:
                material_type = "video";
                break;
        }

        return material_type;
    }
}
