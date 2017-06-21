package com.tingtingfm.cbb.common.crash;

import android.content.Context;

import com.tingtingfm.cbb.common.log.TTLog;
import com.tingtingfm.cbb.common.utils.StorageUtils;
import com.tingtingfm.cbb.common.utils.TimeUtils;
import com.tingtingfm.cbb.common.utils.TimeUtils.TimeFormat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipAndDump {
    public final static int DEFAULT_BUF_SIZE = 4096;
    
    /**
     * 默认存储的文件个数<br>
     * 字段或域定义：<code>DEFAULT_BUF_NUM</code>
     */
    private final static int DEFAULT_BUF_NUM = 20;

    private static String getLogFileName() {
		return "err-tingting.log";
	}
    
    /**
     * 将当日日志文件进行压缩<br>
     * @return 如果有压缩则返回压缩后的文件名，否则返回null
     */
    public static String checkFileSize(Context context) {
    	File filePath = StorageUtils.getIndividualErrorLogDirectory(context);
//        deleteEarlyFile(filePath);
        
		/* 压缩的文件名以当前时间命名，避免重复，压缩完成后删除此文件 */
		final String name = TimeUtils.getTimeForSpecialFormat(TimeFormat.TimeFormat1) + ".zip";
		try {
			zipStream(filePath.getPath() + "/" + name, "err.log", filePath.getPath() + "/" + getLogFileName());
			File deleteFile = StorageUtils.getErrorFile(context);
			if (deleteFile.exists()) {
				deleteFile.delete();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return name;
    }

	/**
	 * @param filePath
	 */
	private static void deleteEarlyFile(File filePath) {
		/* 文件过多时，找出最早的文件，删除掉 */
        final File path = filePath;
        TTLog.d("getFile file_path=" + path);
        final File[] files = path.listFiles();
        final int file_num = files.length;
        if (file_num >= DEFAULT_BUF_NUM) {
            File early_file = files[0];

            for (int index = 1; index < file_num; index++) {
                final File cmp_file = files[index];
                if (early_file.lastModified() > cmp_file.lastModified()) {
                    early_file = cmp_file;
                }
            }

            early_file.delete();
        }
	}
    
    /**
     * 压缩文件
     * ZipAndDump.zipStream()<BR>
     */
    private static void zipStream(String outFile, String zename, String inputFile) throws IOException {
    	TTLog.d("zipStream");

        final FileOutputStream zfos = new FileOutputStream(outFile, false);
        final ZipOutputStream zos = new ZipOutputStream(zfos);
        zos.setLevel(Deflater.BEST_COMPRESSION);
        
        final ZipEntry zipEntry = new ZipEntry(zename);
        zos.putNextEntry(zipEntry);

        final FileInputStream zfis = new FileInputStream(new File(inputFile));
        dump(zos, zfis);

        zfis.close();

        /* the zos will close the zofs */
        zos.close();
    }

    public static void dump(OutputStream os, InputStream is) throws IOException {
        int len = 0;
        final byte[] buffer = new byte[DEFAULT_BUF_SIZE];
        while ((len = is.read(buffer, 0, DEFAULT_BUF_SIZE)) != -1) {
            os.write(buffer, 0, len);
        }
    }
    
    public synchronized static boolean getErrorReportSwitch(Context context) {
        final File file = new File(context.getApplicationInfo().dataDir + "/databases/ersc");
        
        if(file.exists() == true) {
            return false;
        }
        
        return true;
    }
    
    public synchronized static void setErrorReportSwitch(Context context, boolean value) throws IOException {
        final File file = new File(context.getApplicationInfo().dataDir + "/databases/ersc");
        if (value == true) {
            file.delete();
        } else {
            file.createNewFile();
        }
    }
}
