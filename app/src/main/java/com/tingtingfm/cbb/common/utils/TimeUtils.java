package com.tingtingfm.cbb.common.utils;

import android.text.TextUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 时间工具类
 * @author lqsir
 *
 */
public class TimeUtils {
	
	public enum TimeFormat {
		TimeFormat1("yyyy.MM.dd"),
		TimeFormat2("yyyy-MM-dd"),
		TimeFormat3("yyyyMMddHHmmss"),
		TimeFormat4("yyyy-MM-dd HH:mm:ss"),
		TimeFormat5("yy:MM:dd:HH:mm:ss"),
		TimeFormat6("yyyy:MM:dd"),
		TimeFormat7("MM月dd日"),
		TimeFormat8("HH:mm:ss"),
		TimeFormat9("yyyyMMdd"),
		TimeFormat10("yyyy年MM月dd日"),
		TimeFormat11("yyyyMMdd-HHmmss");

		String format;
		
		TimeFormat(String value) {
			format = value;
		}
		
		public String getValue() {
			return format;
		}
	}
	
	/**
	 * 将给定的时间值转换成H:M:S，当小时为0时，只显示M:S
	 * @param time    要转换的时间值
	 * @return    H:M:S/M:S
	 */
	public static String converToHms(int time) {
		int h = 0, m = 0, s = 0;
		s = time % 60;
		time = time / 60;
		m = time % 60;
		h = time / 60;
		return h == 0 ? getDoubleNum(m) + ":" + getDoubleNum(s) 
				: getDoubleNum(h) + ":" + getDoubleNum(m) + ":" + getDoubleNum(s);
	}
	/**
	 * 将给定的时间值转换成H:M:S，当小时为0时，只显示M:S
	 * @param time    要转换的时间值
	 * @return    H:M:S/M:S
	 */
	public static String converToHms(long time) {
		int h = 0, m = 0, s = 0;
		s = (int) (time % 60);
		time = time / 60;
		m = (int) (time % 60);
		h = (int) (time / 60);
		return getDoubleNum(h) + ":" + getDoubleNum(m) + ":" + getDoubleNum(s);
	}
	public static String converToms(long time) {
		if(time <= 0 ){
			return "00:00";
		}
		time = time/1000;
		int h = 0, m = 0, s = 0;
		s = (int) (time % 60);
		time = time / 60;
		m = (int) (time % 60);
		h = (int) (time / 60);
		if(h > 0 ){
			return getDoubleNum(h) + ":" + getDoubleNum(m) + ":" + getDoubleNum(s);
		}else{
			return getDoubleNum(m) + ":" + getDoubleNum(s);
		}
	}



	private static String getDoubleNum(int value) {
		return value > 99 ? value+"" : value > 9 ? value + "" : (value == 0 ? "00" : "0" + value);
	}
	
	public static int getTotalTime(String st) {
		int totalTime = 0;
		String[] times = st.split(":");
		for (int i = times.length; i > 0; i--) {
			totalTime += Integer.valueOf(times[i - 1]) * Math.pow(60, times.length - i);
		}
		
		return totalTime;
	}
	
    /**
     * 根据提供的格式返回对应的时间字符串
     * @param format {@link TimeFormat}
     * @return
     */
    public static String getTimeForSpecialFormat(TimeFormat format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format.getValue(), Locale.CHINA);
        return sdf.format(Calendar.getInstance().getTime());
    }
    
    public static String getTimeForSpecialFormat(TimeFormat format, Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(format.getValue(), Locale.CHINA);
        return sdf.format(date);
    }
    
    /**
     * 给定的日期是否是今天
     * @param date 给定的日期
     * @return true 表示今天 flase 表示非今天
     * @author lqsir
     */
	public static boolean isTodayForDate(String date) {
		return getTimeForSpecialFormat(TimeFormat.TimeFormat10).equals(date);
	}

	/**
	 * 比较给定的2个时间值是否相等
	 * @param value
	 * @param date
	 * @return
	 */
	public static boolean isEqualsForDates(long value, String date) {
		return getTimeForFormatAndDate(TimeFormat.TimeFormat2, value).equals(date);
	}

    
    /**
     * 以给定的格式、给定的时间值来返回相应结果
     * @param value 当前时间秒值
     * @return 返回当前秒值的年月日
     * @author lqsir
     */
    public static String getTimeForFormatAndDate(TimeFormat format, long value) {
    	if (value == 0L)
    		return "";
    	
        Calendar c= Calendar.getInstance();
        c.setTimeInMillis(value * 1000);
        return getTimeForSpecialFormat(format, c.getTime());
    }

    /**
     * 返回当前小时+分钟
     * @return
     */
	public static final String getTimeByCalendar() {
		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY);// 小时
		int minute = cal.get(Calendar.MINUTE);// 分
		
		return hour + ":" + getDoubleNum(minute);
	}
	
	
	public static String millisToString(long millis) {
		String value = "00:00:00";

		if (millis <= 0)
			return value;

		millis = java.lang.Math.abs(millis);

		millis /= 1000;
		int sec = (int) (millis % 60);
		millis /= 60;
		int min = (int) (millis % 60);
		millis /= 60;
		int hours = (int) millis;

		String time;
		DecimalFormat format = (DecimalFormat) NumberFormat.getInstance(Locale.US);
		format.applyPattern("00");

		time = format.format(hours) + ":" + format.format(min) + ":" + format.format(sec);

		return time;
	}

	/**
	 * 显示倒计时，只有分+秒
	 * @param millis
	 * @return
	 */
	public static String countdownString(long millis) {
		String value = "00:00";
		boolean negative = millis <= 0;

		if (negative)
			return value;

		millis = java.lang.Math.abs(millis);

		millis /= 1000;
		int sec = (int) (millis % 60);
		millis /= 60;
		int min = (int) (millis % 60);
		millis /= 60;
		int hours = (int) millis;
		min = hours * 60 + min;

		String time;
		DecimalFormat format = (DecimalFormat) NumberFormat.getInstance(Locale.US);
		format.applyPattern("00");

		time = format.format(min) + ":" + format.format(sec);

		return time;
	}

	private static String timeToStr(String st) {
		return getTimeForSpecialFormat(TimeFormat.TimeFormat6) + ":" + st + ":00";
	}

	public static long getMillis(String str) {
		String[] tt = str.split(":");
		int h = Integer.valueOf(tt[0]);
		int s = Integer.valueOf(tt[1]);

		return (h * 3600 + s *60) * 1000;
	}

	public static long getMillis2(String str) {
		if(!TextUtils.isEmpty(str)){
			String[] tt = str.split(":");
			int h = 0;
			int m = 0;
			int s = 0;
			if (tt.length == 2) {
				h = Integer.valueOf(tt[0]);
				m = Integer.valueOf(tt[1]);
			} else {
				h = Integer.valueOf(tt[0]);
				m = Integer.valueOf(tt[1]);
				s = Integer.valueOf(tt[2]);
			}
			return (h * 3600 + m * 60 + s);
		}
		return 0;
	}

	/**
	 * 得到当天到当前时刻的秒数
	 * @return
	 */
	public static int getCurrentSecondForDay() {
		Calendar calendar = Calendar.getInstance(Locale.CHINA);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);

		System.out.println(hour + ":" + minute + ":" + second);
		return hour * 3600 + minute * 60 + second;
	}

    /**
     * 返回当时分如5分 返回 25分    如39分 返回 21分。
     * @param time
     * @return
     */
    private static int getTime(int time){
        int timea = 0;
        if (time < 30) {
            timea = 30 - time;
        } else if (time >= 30) {
            timea = 60 - time;
        }
        return timea;
    }

	/**
	 * 时间戳 转为 小时
	 * @param serverTime
	 * @return
	 */
	public static int timeStampToHour(String serverTime){
		SimpleDateFormat sdf = new SimpleDateFormat("HH");
		Date date = new Date(Long.parseLong(serverTime + "000"));
		return Integer.parseInt(sdf.format(date));
	}

	/***
	 *  判断是否 在本周内
	 * @param time
	 * @return
	 */
	public static boolean isThisWeek(String time) {
		if(TextUtils.isEmpty(time))
			return false;
		Calendar calendar = Calendar.getInstance();
		int currentWeek = calendar.get(Calendar.WEEK_OF_YEAR);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		try {
			date = sdf.parse(time);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		calendar.setTime(date);
		int paramWeek = calendar.get(Calendar.WEEK_OF_YEAR);
		if (paramWeek == currentWeek) {
			return true;
		}
		return false;
	}

	/**
	 * 获取月日
	 * @param dateTime
	 * @return
	 */
	public static String getMonthDay(String dateTime){
		if(TextUtils.isEmpty(dateTime))
			return "";

		SimpleDateFormat sdff = new SimpleDateFormat(TimeFormat.TimeFormat4.getValue(), Locale.CHINA);
		SimpleDateFormat sdf = new SimpleDateFormat("M月d日");
		Date d = new Date();
		try {
			d = sdff.parse(dateTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return sdf.format(d);
	}
	/**
	 * 获取年月日
	 * @return
	 */
	public static String getYearMonthDayHMS(){
		SimpleDateFormat sdff = new SimpleDateFormat(TimeFormat.TimeFormat3.getValue(), Locale.CHINA);
		Date d = new Date();
		return sdff.format(d);
	}

	/**
	 * 获得年月日-时分秒
	 * @return
     */
	public static String getYMDHMS(){
		SimpleDateFormat sdff = new SimpleDateFormat(TimeFormat.TimeFormat11.getValue(), Locale.CHINA);
		Date d = new Date();
		return sdff.format(d);
	}

	/**
	 * 获取年月日
	 * @return  2017-01-11 12:01:11
	 */
	public static String getYearMonthDayHMS1(){
		SimpleDateFormat sdff = new SimpleDateFormat(TimeFormat.TimeFormat4.getValue(), Locale.CHINA);
		Date d = new Date();
		return sdff.format(d);
	}

	/**
	 * 获取年月日
	 * @return  2017-01-11 12:01:11
	 */
	public static String getYearMonthDayHMS1(long time){
		SimpleDateFormat sdff = new SimpleDateFormat(TimeFormat.TimeFormat4.getValue(), Locale.CHINA);
		Date d = new Date(time);
		return sdff.format(d);
	}

	/**
	 * 获取年月日
	 * @return
	 */
	public static String getYearMonthDayHMS(long time){
		SimpleDateFormat sdff = new SimpleDateFormat(TimeFormat.TimeFormat10.getValue(), Locale.CHINA);
		Date d = new Date(time*1000);
		return sdff.format(d);
	}


	/**
	 * 获取小时 分钟 HH:MM
	 * @param dateTime
	 * @return
	 */
	public static String getHourM(String dateTime){
		if(TextUtils.isEmpty(dateTime))
		 return "";
		SimpleDateFormat sdff = new SimpleDateFormat(TimeFormat.TimeFormat4.getValue(), Locale.CHINA);
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		try {
			Date date = sdff.parse(dateTime);
			return sdf.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return "";
	}


	/**
	 * 传入时间日期  返回 时分秒
	 * @param dateTime 2016-05-07 12:00:23
	 * @return
	 */
	public static String getHMSTime(String dateTime){
		if(TextUtils.isEmpty(dateTime))
			return "";
		SimpleDateFormat sdff = new SimpleDateFormat(TimeFormat.TimeFormat4.getValue(), Locale.CHINA);
		SimpleDateFormat format = new SimpleDateFormat(TimeFormat.TimeFormat8.getValue(), Locale.CHINA);
		try {
			Date date = sdff.parse(dateTime);
			return format.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 传入时间日期  返回 时分秒
	 * @param dateTime 2016-05-07 12:00:23
	 * @return  20160507120023
	 */
	public static String getYMDHMS(String dateTime){
		if(TextUtils.isEmpty(dateTime))
			return "";
		SimpleDateFormat sdff = new SimpleDateFormat(TimeFormat.TimeFormat4.getValue(), Locale.CHINA);
		SimpleDateFormat format = new SimpleDateFormat(TimeFormat.TimeFormat3.getValue(), Locale.CHINA);
		try {
			Date date = sdff.parse(dateTime);
			return format.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 传入时间日期  返回  日期
	 * @param dateTime 2016-05-07 12:00:23
	 * @return
	 */
	public static String getDate(String dateTime){
		if(TextUtils.isEmpty(dateTime))
			return "";
		SimpleDateFormat sdff = new SimpleDateFormat(TimeFormat.TimeFormat4.getValue(), Locale.CHINA);
		SimpleDateFormat format = new SimpleDateFormat(TimeFormat.TimeFormat2.getValue(), Locale.CHINA);
		try {
			Date date = sdff.parse(dateTime);
			return format.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return "";
	}

	/***
	 * 判断开始 时间 是否已经处理 开始播放状态 了
	 * @param dateTime 2016-05-07 12:00:23
	 * @return
	 */
	public static boolean isStartPlay(String dateTime){
		if(TextUtils.isEmpty(dateTime))
			return false;
		SimpleDateFormat sdff = new SimpleDateFormat(TimeFormat.TimeFormat4.getValue(), Locale.CHINA);
		try {
			Date date = sdff.parse(dateTime);
			long current = System.currentTimeMillis();
			return current >= date.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return false;
	}

    /**
     * 预约提醒用到。
     * 给二个日期，判断参数一日期在参数二的前面，后面。过滤掉过期数据，如果在前面，表示过期。
     *
     * @param date1 活动结束日期 时间
     * @param date2 当前日期 时间
     * @return 返回值 -1，1，0 。用于数据按日期时间排序，进行提醒。
     */
    public static int isOverDue(String date1, String date2){
		int overdue = 0;
		if(!TextUtils.isEmpty(date1.trim()) && !TextUtils.isEmpty(date2.trim())){
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date d1 = sdf.parse(date1);
				Date d2 = sdf.parse(date2);
				return d1.compareTo(d2);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return overdue;
	}

	/**
	 * 返回给定日期时间的秒值
	 * @param dataTime
	 * @return
	 */
	public static long getTime(String dataTime){
		long seconValue = 0;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date d1 = sdf.parse(dataTime);
			seconValue = d1.getTime();
			d1.getSeconds();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return seconValue;
	}


	/**
	 * 获取当前时间日期
	 * @return yyyy-MM-dd HH:mm:ss格式日期
	 */
	public static String getCurrentDateTime(){
		long l = System.currentTimeMillis();
		Date date = new Date(l);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dateFormat.format(date);
	}

	/**
	 * 两个日期相差几天
	 * @param sdate
	 * @return
	 */
	public static int friendly_time(String sdate) {
		Date time = toDate(sdate);
		if(time == null) {
			return 0;
		}
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd");
		String curDate = formatter.format(new java.util.Date());// 获取当前时间
		Date cur =  toDate(curDate);
		//Calendar cal = Calendar.getInstance();

		long lt = time.getTime()/86400000;
		//long ct = cal.getTimeInMillis()/86400000;
		long ct = cur.getTime()/86400000;
		int days = (int)(lt - ct);
		return days;
	}

	/**
	 * 将字符串转位日期类型
	 * @param sdate
	 * @return
	 */
	public static Date toDate(String sdate) {
		try {
			return dateFormater2.get().parse(sdate);
		} catch (ParseException e) {
			return null;
		}
	}

	private final static ThreadLocal<SimpleDateFormat> dateFormater2 = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy.MM.dd");
		}
	};
}
