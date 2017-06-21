package com.tingtingfm.cbb.common.upload.operatioin;

import com.tingtingfm.cbb.bean.MediaInfo;
import com.tingtingfm.cbb.common.upload.config.UploadConfiguration;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by lqsir on 2017/4/18.
 */

public class UploadWriteUtils {
    public static void writeParams(Map<String, String> params, DataOutputStream dos, UploadConfiguration configuration) throws IOException {
        if (params != null && params.size() > 0) {
            StringBuffer sb = new StringBuffer();
            Iterator<String> it = params.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                String value = params.get(key);
                sb.append(configuration.getPrefix());
                sb.append(configuration.getBoundary());
                sb.append(configuration.getLine_end());
                sb.append("Content-Disposition: form-data; name=\"")
                        .append(key)
                        .append("\"")
                        .append(configuration.getLine_end())
                        .append(configuration.getLine_end());
                sb.append(value);
                sb.append(configuration.getLine_end());
            }

            dos.write(sb.toString().getBytes());
        }
    }

    public static void writeFile(int sliceNo, String filePath, DataOutputStream dos, UploadConfiguration configuration) throws Exception {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("file no exist");
        }

        //上传文件类型+文件名
        writeFileTypeAndName(sliceNo + ".split", dos, configuration);

        /** 上传文件 */
        writeFileData(sliceNo, file, dos, configuration);

        byte[] end_data = (configuration.getPrefix()
                + configuration.getBoundary()
                + configuration.getPrefix()
                + configuration.getLine_end()).getBytes();
        dos.write(end_data);
        dos.flush();
    }

    private static void writeFileTypeAndName(String fileName, DataOutputStream dos, UploadConfiguration configuration) throws IOException {
        StringBuffer sb = new StringBuffer();
        sb.append(configuration.getPrefix())
                .append(configuration.getBoundary())
                .append(configuration.getLine_end());

        String name = "split";
        sb.append("Content-Disposition:form-data; name=\"" + name
                + "\"; filename=\"" + fileName
                + "\"" + configuration.getLine_end());

        sb.append(configuration.getLine_end());

        dos.write(sb.toString().getBytes());
    }

    private static void writeFileData(int sliceNo, File file, DataOutputStream dos, UploadConfiguration configuration) throws Exception {
        int singleSize = MediaInfo.SINGLE_SLICE_COUNT;
        long fileSize = file.length();
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
        randomAccessFile.seek((sliceNo - 1) * MediaInfo.SINGLE_SLICE_COUNT);

        int maxCount = (sliceNo * singleSize <= fileSize)
                ? singleSize
                : (int) (fileSize - (sliceNo - 1) * singleSize);

        System.out.println("The " + sliceNo + " slice, the slice size : " + maxCount);

        byte[] bytes = new byte[1024 * 128];
        int len = 0;
        int count = 0;
        while ((count < maxCount && (len = randomAccessFile.read(bytes)) != -1)) {
            dos.write(bytes, 0, len);
            count += len;
        }

        randomAccessFile.close();
        dos.write(configuration.getLine_end().getBytes());
    }
}
