package com.tingtingfm.cbb.common.utils.encrypt;


import android.text.TextUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/***
 * 转码   MD5工具类
 * @author spring sky
 * Email:vipa1888@163.com
 * QQ:840950105
 *
 */
public final class ConvertUtil {

	public final static char[] BToA = "0123456789abcdef".toCharArray();

	private ConvertUtil() {

	}

	/**
	 * 把16进制字符串转换成字节数组
	 * 
	 * @param hex
	 * @return
	 */
	public static byte[] hexStringToByte(String hex) {
		int len = (hex.length() / 2);
		byte[] result = new byte[len];
		char[] achar = hex.toCharArray();
		for (int i = 0; i < len; i++) {
			int pos = i * 2;
			result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
		}
		return result;
	}

	private static byte toByte(char c) {
		byte b = (byte) "0123456789ABCDEF".indexOf(c);
		return b;
	}

	/**
	 * 把字节数组转换成16进制字符串
	 * 
	 * @param bArray
	 * @return
	 */
	public static final String bytesToHexString(byte[] bArray) {
		if(bArray == null )
		{
			return "";
		}
		StringBuffer sb = new StringBuffer(bArray.length);
		String sTemp;
		for (int i = 0; i < bArray.length; i++) {
			sTemp = Integer.toHexString(0xFF & bArray[i]);
			if (sTemp.length() < 2)
				sb.append(0);
			sb.append(sTemp.toUpperCase());
		}
		return sb.toString();
	}

	/**
	 * 把字节数组转换为对象
	 * 
	 * @param bytes
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static final Object bytesToObject(byte[] bytes) throws IOException,
			ClassNotFoundException {
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		ObjectInputStream oi = new ObjectInputStream(in);
		Object o = oi.readObject();
		oi.close();
		return o;
	}

	/**
	 * 把可序列化对象转换成字节数组
	 * 
	 * @param s
	 * @return
	 * @throws IOException
	 */
	public static final byte[] objectToBytes(Serializable s) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream ot = new ObjectOutputStream(out);
		ot.writeObject(s);
		ot.flush();
		ot.close();
		return out.toByteArray();
	}

	public static final String objectToHexString(Serializable s)
			throws IOException {
		return bytesToHexString(objectToBytes(s));
	}

	public static final Object hexStringToObject(String hex)
			throws IOException, ClassNotFoundException {
		return bytesToObject(hexStringToByte(hex));
	}

	/**
	 * @函数功能: BCD码转为10进制串(阿拉伯数据)
	 * @输入参数: BCD码
	 * @输出结果: 10进制串
	 */
	public static String bcd2Str(byte[] bytes) {
		StringBuffer temp = new StringBuffer(bytes.length * 2);

		for (int i = 0; i < bytes.length; i++) {
			temp.append((byte) ((bytes[i] & 0xf0) >>> 4));
			temp.append((byte) (bytes[i] & 0x0f));
		}
		return temp.toString().substring(0, 1).equalsIgnoreCase("0") ? temp
				.toString().substring(1) : temp.toString();
	}

	/**
	 * @函数功能: 10进制串转为BCD码
	 * @输入参数: 10进制串
	 * @输出结果: BCD码
	 */
	public static byte[] str2Bcd(String asc) {
		int len = asc.length();
		int mod = len % 2;

		if (mod != 0) {
			asc = "0" + asc;
			len = asc.length();
		}

		byte abt[] = new byte[len];
		if (len >= 2) {
			len = len / 2;
		}

		byte bbt[] = new byte[len];
		abt = asc.getBytes();
		int j, k;

		for (int p = 0; p < asc.length() / 2; p++) {
			if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9')) {
				j = abt[2 * p] - '0';
			} else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {
				j = abt[2 * p] - 'a' + 0x0a;
			} else {
				j = abt[2 * p] - 'A' + 0x0a;
			}

			if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9')) {
				k = abt[2 * p + 1] - '0';
			} else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {
				k = abt[2 * p + 1] - 'a' + 0x0a;
			} else {
				k = abt[2 * p + 1] - 'A' + 0x0a;
			}

			int a = (j << 4) + k;
			byte b = (byte) a;
			bbt[p] = b;
		}
		return bbt;
	}

	public static String BCD2ASC(byte[] bytes) {
		StringBuffer temp = new StringBuffer(bytes.length * 2);

		for (int i = 0; i < bytes.length; i++) {
			int h = ((bytes[i] & 0xf0) >>> 4);
			int l = (bytes[i] & 0x0f);
			temp.append(BToA[h]).append(BToA[l]);
		}
		return temp.toString();
	}

	/**
	 * 两字符数组异或
	 */
	public static byte[] byteArrXor(byte[] arr1, byte[] arr2, int len){
		byte[] dest = new byte[len];
		
		if((arr1.length < len) || (arr2.length < len)){
			return null;
		}
		
		for(int i = 0;i < len;i++){
			dest[i] = (byte)(arr1[i] ^ arr2[i]);
		}
		
		return dest;
	}
	

	/**
	 * MD5加密字符串，返回加密后的16进制字符串
	 * @param origin
	 * @return
	 */
	public static String MD5EncodeToHex(String origin) {
		return bytesToHexString(MD5Encode(origin));
	}

	/**
	 * MD5加密字符串，返回加密后的字节数组
	 * 
	 * @param origin
	 * @return
	 */
	public static byte[] MD5Encode(String origin) {
		return MD5Encode(origin.getBytes());
	}

	/**
	 * MD5加密字节数组，返回加密后的字节数组
	 * 
	 * @param bytes
	 * @return
	 */
	public static byte[] MD5Encode(byte[] bytes) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
			return md.digest(bytes);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}
	

	/**
	 * 根据提供的下载次数，返回相关值
	 * 
	 * <pre>
	 * 下载量最多显示4位，超出4位，显示XXXX万，其中XXXX四位数包含
	 * 小数点，小数点后最多显示1位；
	 * 例：1.1万、10.1万、100.1万、1000万
	 * 下载量超出8位时，显示XXXX亿，规则同上
	 * 
	 * </pre>
	 * 
	 * @param downloadNum
	 * @return
	 */
	public static String downloadNumToStr(int downloadNum) {
		if (downloadNum < 10000) {
			return downloadNum +"";
		} else if (downloadNum > 9999 && downloadNum < 100000000) {
			int x = downloadNum / 10000;
			int y = (downloadNum % 10000) / 1000;
			if (x > 999) {
				return x + "万";
			} else {
				return x + "." + y + "万";
			}
		} else {
			int x = downloadNum / 100000000;
			int y = (downloadNum % 100000000) / 10000000;
			if (x > 99999999) {
				return x + "亿";
			} else {
				return x + "." + y + "亿";
			}
		}
	}

	/**
	 * 
	 * <pre>
	 * 大小显示3位数，并包含小数点，小数点后最多保留2位；
	 * 例：159M、15.9M、1.59M、0.59M
	 * </pre>
	 * 
	 * @param length
	 * @return
	 */
	public static String downloadLengthToStr(int length) {
		double v = (double) length / 1024;
		int i = length % 1024;
		if (i == 0) {
			return String.valueOf(((int) v)) + "M";
		} else {
			return String.format("%.2f", Double.parseDouble(String.valueOf(v))) + "M";
		}
	}
	
	/**
	 * 根据给定的长度去截取字符串
	 * @param str 给定的字符串
	 * @param count 显示的长度
	 * @return 返回操作后的字符串
	 */
	public static String subLengthStr(String str, int count) {
		if (TextUtils.isEmpty(str))
			return "";

		String temp = str.toString().trim();
		if (temp.length() <= count) {
			return temp;
		}
		
		return temp.substring(0, count).toString() + "...";
	}
}
