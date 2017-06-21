package com.tingtingfm.cbb.common.utils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.alibaba.fastjson.JSON;
import com.tingtingfm.cbb.common.configuration.Constants;
import com.tingtingfm.cbb.common.configuration.PreferencesConfiguration;
import com.tingtingfm.cbb.common.log.TTLog;
import com.tingtingfm.cbb.common.utils.encrypt.DesECBUtil;
import com.tingtingfm.cbb.response.UploadFaceResponse;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * 
 * @author tianhu 2014-11-11
 * 
 */
public class UploadUtils extends Thread {
	private final String locaType;
	private final String manuscId;
	private String face_filePath;
	private String mfileKey;
	private Handler mhandler;
	private String mtype, mUrl;
	private static final String BOUNDARY = UUID.randomUUID().toString(); // 随机生成
																			// 边界标识
	private static final String PREFIX = "--";
	private static final String LINE_END = "\r\n";
	private static final String CONTENT_TYPE = "multipart/form-data"; // 内容类型
	private int requestTime = 0;

	public UploadUtils(String filePath, String fileKey, String type, String locationType,String manuscriptId, String Url, Handler handler) {
		face_filePath = filePath;//文件
		mfileKey = fileKey;//audio 上传文件字段名
		mtype = type;//1图片 2音频 3视频 4稿件
		locaType =locationType; //所属类型：resource资源中心, manuscript稿件,user用户
		manuscId = manuscriptId;  //如果是类型是稿件，则必须传manuscript_id，如果不传或为0，则返回失败
		mUrl = Url;
		mhandler = handler;
	}

	@Override
	public void run() {
		super.run();
		// 组装post参数
		final Map<String, String> params = new HashMap<String, String>();
		params.put("client", "android_" + DeviceUtils.getTelephoneSerialNum());
		params.put("version", "android_" + AppUtils.getVersionName());
		params.put("session_key", PreferencesConfiguration.getSValues(Constants.SESSION_KEY));
        params.put("type", mtype);
		params.put("location_type", locaType);
		params.put("manuscript_id", manuscId);
		params.put("api_sign", DesECBUtil.getSecreteToken(params));
		toUploadFile(mfileKey, mUrl, params, new File(face_filePath));
	}

	private void toUploadFile(String fileKey, String RequestURL, Map<String, String> param, File... file) {
		requestTime = 0;

		long requestTime = System.currentTimeMillis();
		long responseTime = 0;

		try {
			URL url = new URL(RequestURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10 * 1000);
			conn.setConnectTimeout(10 * 1000);
			conn.setDoInput(true); // 允许输入流
			conn.setDoOutput(true); // 允许输出流
			conn.setUseCaches(false); // 不允许使用缓存
			conn.setRequestMethod("POST"); // 请求方式
			conn.setRequestProperty("Charset", "utf-8"); // 设置编码
			conn.setRequestProperty("connection", "keep-alive");
			conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
			conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
			// conn.setRequestProperty("Content-Type",
			// "application/x-www-form-urlencoded");

			/**
			 * 当文件不为空，把文件包装并且上传
			 */
			DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
			StringBuffer sb = null;

			/***
			 * 以下是用于上传参数
			 */
			if (param != null && param.size() > 0) {
				Iterator<String> it = param.keySet().iterator();
				while (it.hasNext()) {
					sb = null;
					sb = new StringBuffer();
					String key = it.next();
					String value = param.get(key);
					sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
					sb.append("Content-Disposition: form-data; name=\"").append(key).append("\"").append(LINE_END).append(LINE_END);
					sb.append(value).append(LINE_END);
					dos.write(sb.toString().getBytes());
					// dos.flush();
				}
			}

			for (File f : file) {

				sb = null;
				sb = new StringBuffer();
				/**
				 * 这里重点注意： name里面的值为服务器端需要key 只有这个key 才可以得到对应的文件
				 * filename是文件的名字，包含后缀名的 比如:abc.png
				 */
				sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
				sb.append("Content-Disposition:form-data; name=\"" + fileKey + "\"; filename=\"" + f.getName() + "\"" + LINE_END);
				if (fileKey.equals("pic")) {
					sb.append("Content-Type:image/pjpeg" + LINE_END); // 这里配置的Content-type很重要的
				} else {
					sb.append("Content-Type:audio/mpeg" + LINE_END); // 这里配置的Content-type很重要的
				}
				sb.append(LINE_END);

				dos.write(sb.toString().getBytes());
				/** 上传文件 */
				InputStream is = new FileInputStream(f);
				byte[] bytes = new byte[1024];
				int len = 0;
				while ((len = is.read(bytes)) != -1) {
					//Thread.sleep(100);
					dos.write(bytes, 0, len);
				}
				is.close();

				dos.write(LINE_END.getBytes());
			}
			byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
			dos.write(end_data);
			dos.flush();

			/**
			 * 获取响应码 200=成功 当响应成功，获取响应的流
			 */
			int res = conn.getResponseCode();
			responseTime = System.currentTimeMillis();
			this.requestTime = (int) ((responseTime - requestTime) / 1000);
			if (res == 200) {
				InputStream input = conn.getInputStream();
				StringBuffer sb1 = new StringBuffer();
				int ss;
				while ((ss = input.read()) != -1) {
					sb1.append((char) ss);
				}

				UploadFaceResponse response = JSON.parseObject(sb1.toString(), UploadFaceResponse.class);
				if (response.data != null) {
					Message mMes = mhandler.obtainMessage();
					mMes.what = Constants.UPLOAD_SUCCESS;
					Bundle bundle = new Bundle();
					bundle.putString("id", response.data.getId()+"");
					bundle.putString("url", response.data.getUrl());
					bundle.putString("locaType", response.data.getLocation_type());
					mMes.setData(bundle);
					mhandler.sendMessage(mMes);
					TTLog.i("Comment_test", "----upload voice success" + response.data.getUrl());
				} else {
					mhandler.sendEmptyMessage(Constants.UPLOAD_FAIL);
				}
				return;
			} else {
				mhandler.sendEmptyMessage(Constants.UPLOAD_FAIL);
				return;
			}
		} catch (Exception e) {
			mhandler.sendEmptyMessage(Constants.UPLOAD_FAIL);
			e.printStackTrace();
			return;
		}
	}
}
