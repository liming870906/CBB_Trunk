package com.tingtingfm.cbb.common.utils.encrypt;


import android.text.TextUtils;

import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


/***
 * DES ECB对称加密 解密
 * @author lqsir
 */
public class DesECBUtil {
	private static String key = "#**$(UdS";
	private static String ENCODING = "UTF-8";
	
	/**
	 * 给定的内容按固定的Key来加密
	 * @param encryptString 需要加密的内容
	 * @return 返回加密后的内容
	 * @author lqsir
	 */
	public static String encryptDES(String encryptString) {
		try {
			return encryptDES(encryptString, key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

    /**
	 * 给定的内容按固定的Key来解密
	 * @param decryptString 需要解密的内容
	 * @return 返回解密后的内容
	 * @author lqsir
	 */
	public static String decryptDES(String decryptString) {
		try {
			return decryptDES(decryptString, key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * 加密数据
	 * @param encryptString  注意：这里的数据长度只能为8的倍数
	 * @param encryptKey
	 * @return
	 * @throws Exception
	 * YA0yKVwqgJuuuCD6QVYe7g==
	 */
	public static String encryptDES(String encryptString, String encryptKey) throws Exception {
		SecretKeySpec key = new SecretKeySpec(encryptKey.getBytes(), "DES");
		Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] encryptedData = cipher.doFinal(encryptString.getBytes("utf-8"));
		return ConvertUtil.bytesToHexString(encryptedData);
	}
	
	/**
	 * 自定义一个key
	 * @param keyRule
	 */
	public static byte[] getKey(String keyRule) {
		byte[] keyByte = keyRule.getBytes();
		// 创建一个空的八位数组,默认情况下为0
		byte[] byteTemp = new byte[16];
		// 将用户指定的规则转换成八位数组
		for (int i = 0; i < keyByte.length; i++) {
			byteTemp[i] = keyByte[i];
		}
		return byteTemp;
	}
	
	/***
	 * 解密数据
	 * @param decryptString
	 * @param decryptKey
	 * @return
	 * @throws Exception
	 */
	public static String decryptDES(String decryptString, String decryptKey) throws Exception {
		SecretKeySpec key = new SecretKeySpec(decryptKey.getBytes(), "DES");
		Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte decryptedData[] = cipher.doFinal(ConvertUtil.hexStringToByte(decryptString));
		return new String(decryptedData);
	}
	
	public static String getSecreteToken(Map<String, String> maps) {
    	String result = "";
    	try {
	    	Map<String, String> posts = maps;
	        List<String> keys = new ArrayList<String>();
	        for (String str : posts.keySet()) {
	            keys.add(str);
	        }
	        Collections.sort(keys);
	
	        StringBuilder sb = new StringBuilder();
	        for (int i = 0; i < keys.size(); i++) {
	            sb.append(URLEncoder.encode(keys.get(i), ENCODING));
	            sb.append("=");
                String value = posts.get(keys.get(i));
				if (!TextUtils.isEmpty(value)) {
					sb.append(URLEncoder.encode(value, ENCODING));
				}
	            sb.append("&");
	        }
	        
	        result = sb.toString().substring(0, sb.toString().length() -1);
            result = dispatchStr(result);
            result += "_" + "bw(*ez$@]a.bokLi";
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
        return encryption(result);
    }

	public static String encryption(String plainText) {
		String re_md5 = new String();
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(plainText.getBytes());
			byte b[] = md.digest();

			int i;

			StringBuffer buf = new StringBuffer();
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset] & 0xff;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}

			re_md5 = buf.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return re_md5;
	}

	/**
	 * 对字符做处理，URLEncoder.encode对*不做转义，对空格做+号转义，*需要替换成%2A, +号需要替换成%20
	 * Uri.encode *需要替换成%2A
	 *
	 * @param value
	 * @return
	 */
	public static String dispatchStr(String value) {
		return value.replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
	}
}
