package com.tingtingfm.cbb.common.utils;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;

import com.tingtingfm.cbb.R;
import com.tingtingfm.cbb.bean.ManuscriptInfo;
import com.tingtingfm.cbb.common.db.DBManuscriptManager;

import java.util.ArrayList;

/**
 * Created by tianhu on 2017/1/6.
 */

public class ManuscriptUtils {

    /**
     * 将音频样式转换为audio标签
     * @param str html文本
     * @param filePath 本地路径
     * @param url  网络路径
     * @param audioNname 音频名称
     * @return
     */
    public static String convertAudioFlag(String str, String filePath, String url, String audioNname) {
        String startStr = "";
        while (true) {
            int inputPo = str.indexOf("<img");//音频节点开始位置
            if (inputPo == -1) {
                return startStr + str;
            }
            //input头文本
            startStr += str.substring(0, inputPo);//startStr 存储替换节点前所有数据
            str = str.substring(inputPo);//str为节点后的所有数据

            int nameStartPos = str.indexOf("name=");//获取名字开始位置
            String nameStr = str.substring(nameStartPos + 6);
            int nameEndPos = nameStr.indexOf("\"");//获取名字结束位置
            //获取name字段
            String audioName = nameStr.substring(0, nameEndPos);//获取名字字段进行替换为网络地址
            //结束标签位置
            int endInputPos = nameStr.indexOf(">");   //音频节点结束位置

            if (audioName.equals(filePath)) {
                //input结束标签后内容保存
                str = nameStr.substring(endInputPos + 1);
                String brStr = str.substring(0,4);
                if(brStr.equals("<br>")){
                    str= str.substring(4);
                }
                startStr += getAudioFlag(url) + audioNname + "</p>";
            } else {
                //结束标签位置
                startStr += str.substring(0, endInputPos);
                str = str.substring(endInputPos + 1);

            }

        }
    }

    /**
     * 返回audio标签数据
     * @param str
     * @return
     */
    private static String getAudioFlag(String str) {
        String audioFlag =
                "<p><audio src=\"" + str +
                        "\" controls=\"\" preload=\"metadata\">" +
                        "您的浏览器不支持audio标签！" +
                        "</audio>&nbsp;";
        return audioFlag;
    }

    /**
     * 稿件上传中，杀死进程，在次进入，将所有上传中状态改为上传失败。
     */
    public static void updateManuscriptState(final Context context) {
        new Thread() {
            @Override
            public void run() {
                DBManuscriptManager dbManager = DBManuscriptManager.getInstance(context);
                ArrayList<ManuscriptInfo> mInfos = dbManager.getUploadManuscriptDatas();
                for (ManuscriptInfo m : mInfos) {
                    m.setUploadState(3);
                    dbManager.updataManuscriptUploadState(m);
                }
            }
        }.start();
    }
}
