package com.tingtingfm.cbb.ui.eventbus;

import com.tingtingfm.cbb.bean.MediaInfo;

/**
 * Created by liming on 17/4/18.
 */

public class MediaDataEvent {
    private MediaInfo mediaInfo;
    private boolean isCheck;

    public MediaDataEvent(MediaInfo mediaInfo, boolean isCheck) {
        this.mediaInfo = mediaInfo;
        this.isCheck = isCheck;
    }

    public MediaInfo getMediaInfo() {
        return mediaInfo;
    }

    public boolean isCheck() {
        return isCheck;
    }
}
