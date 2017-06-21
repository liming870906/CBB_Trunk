package com.tingtingfm.cbb.bean;

/**
 * Created by tianhu on 2017/5/12.
 */

public class ManuscriptAudioInfo {
    private String audioPath;//音频路径
    private String imagePath;//音频图片路径

    public ManuscriptAudioInfo() {

    }

    public ManuscriptAudioInfo(String audioPath, String imagePath) {
        this.audioPath = audioPath;
        this.imagePath = imagePath;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @Override
    public String toString() {
        return "ManuscriptAudioInfo{" +
                "audioPath='" + audioPath + '\'' +
                ", imagePath='" + imagePath + '\'' +
                '}';
    }
}
