package com.tingtingfm.cbb.common.utils;

import com.tingtingfm.cbb.bean.MediaInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据转换帮助类
 * Created by lqsir on 2017/1/19.
 */

public class DataConvertUtils {
    public static List<MediaInfo> getMediaInfos(MediaInfo info) {
        List<MediaInfo> infos = new ArrayList<MediaInfo>();
        infos.add(info);

        return infos;
    }
}
