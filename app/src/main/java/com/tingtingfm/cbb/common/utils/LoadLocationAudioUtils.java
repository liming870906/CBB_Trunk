package com.tingtingfm.cbb.common.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import com.tingtingfm.cbb.bean.MediaInfo;
import com.tingtingfm.cbb.common.configuration.AccoutConfiguration;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.configuration.PreferencesConfiguration;
import com.tingtingfm.cbb.common.db.DBAudioRecordManager;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by liming on 2017/5/16.
 */

public class LoadLocationAudioUtils {
    public static void loadLocationAudioInfo(final Context context) {
        if (!PreferencesConfiguration.getBValues((Constants.RECOVERY_AUDIO_IS_FINISH))) {
            new Thread(){
                @Override
                public void run() {
                    //生成存储对象
                    Map<Integer, List<File>> _audioData = new HashMap<Integer, List<File>>();
                    //获得根目录../CBB/audio目录
                    File audioFiles = StorageUtils.getSDCardStorageDirectory(context);
                    //获得audio目录下的所有文件及文件夹
                    String[] _fileList = audioFiles.list();
                    //循环
                    for (int i = 0; i < _fileList.length; i++) {
                        //生成文件或文件夹
                        File _readFile = new File(audioFiles.getPath() + File.separator + _fileList[i]);
                        //判断是否为文件夹
                        if (_readFile.isDirectory()) {
                            System.out.println("文件夹：" + _readFile.getAbsolutePath());
                            if (isNumber(_fileList[i])) {
                                //获得所有文件
                                File[] _files = _readFile.listFiles();
                                //数据防止集合中
                                List<File> _fileArrs = Arrays.asList(_files);
                                //存放数据
                                _audioData.put(Integer.valueOf(_fileList[i]), _fileArrs);
                            }
                        }
                    }
                    //遍历容器
                    Iterator<Map.Entry<Integer, List<File>>> iterator = _audioData.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<Integer, List<File>> entry = iterator.next();
                        int _userId = entry.getKey();
                        List<File> _files = entry.getValue();
                        MediaPlayer player;
                        for (File file : _files) {
                            if("audio_temporary_file.mp3".equals(file.getName())){
                                FileUtils.deleteFile(file);
                                break;
                            }
                            System.out.println("file = " + file.toString() + " modified: " + file.lastModified());
                            //判断文件是否存在
                            if (file.exists()) {
                                player = MediaPlayer.create(context, Uri.fromFile(file));
                                //声明对媒体对象
                                MediaInfo _info = new MediaInfo();
                                //添加默认服务器ID
                                _info.setMedia_id(-1);
                                //用户ID
                                _info.setUser_id(AccoutConfiguration.getLoginInfo().getUserid());
                                //文件大小
                                _info.setSize(file.length());
                                //文件名称（带扩展名）
                                _info.setFullName(file.getName());
                                //文件名称
                                _info.setTitle(getFileName(file));
                                //文本类型
                                _info.setMime_type(Constants.MIME_TYPE_AUDIO_MP3);
                                //数据地址
                                _info.setAbsolutePath(file.getAbsolutePath());
                                //添加文本的时间
                                _info.setDate_added(file.lastModified() / 1000);
                                //更新文本的时间
                                _info.setDate_modified(file.lastModified());
                                //更新文本上传状态
                                _info.setUpload_status(Constants.UPLOAD_STATUS_DEFAULT);
                                //添加音频文件时间
                                _info.setDuration(player.getDuration());
                                //设置用户ID
                                _info.setUser_id(_userId);
                                if (!DBAudioRecordManager.getInstance(context).queryAudioRecordInfo(_info.getTitle(), _userId)) {
                                    //添加音频数据到数据库中
                                    DBAudioRecordManager.getInstance(context).addAudioRecord(_info);
                                    System.out.println(_info.toString());
                                }
                            }
                        }
                    }
                    PreferencesConfiguration.setBValues(Constants.RECOVERY_AUDIO_IS_FINISH,true);
                }
            }.start();
        }
    }

    /**
     * 获得去掉扩展名的文件名称
     * @param file
     * @return
     */
    public static String getFileName(File file) {
        String _fileName = file.getName();
        return _fileName.substring(0, _fileName.lastIndexOf("."));
    }

    /**
     * 判断字符串是否数值
     *
     * @param str
     * @return true:是数值 ；false：不是数值
     * @author:WD_SUHUAFU
     */
    public static boolean isNumber(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher match = pattern.matcher(str);
        return match.matches();
    }
}
