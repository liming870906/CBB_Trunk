package com.piterwilson.audio;

/**
 * Created by liming on 17/3/22.
 */

public interface DecodeWaveListener {
    void decodeStart();//开始解码
    void decodeIsOver();//解码结束
    void decodeDissmisDialog();// 取消加载弹窗

    void maxDuration(long duration);
}
