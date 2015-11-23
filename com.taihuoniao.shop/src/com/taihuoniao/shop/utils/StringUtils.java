package com.taihuoniao.shop.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

/** 
 * 鐎涙顑佹稉鍙夋惙娴ｆ粌浼愰崗宄板瘶
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class StringUtils 
{
	private final static Pattern emailer = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
	//private final static SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	//private final static SimpleDateFormat dateFormater2 = new SimpleDateFormat("yyyy-MM-dd");
	
	private final static ThreadLocal<SimpleDateFormat> dateFormater = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
	};

	private final static ThreadLocal<SimpleDateFormat> dateFormater2 = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd");
		}
	};
	
	/**
	 * 鐏忓棗鐡х粭锔胯鏉烆兛缍呴弮銉︽埂缁鐎�
	 * @param sdate
	 * @return
	 */
	public static Date toDate(String sdate) {
		try {
			return dateFormater.get().parse(sdate);
		} catch (ParseException e) {
			return null;
		}
	}
	
	/**
	 * 娴犮儱寮告總鐣屾畱閺傜懓绱￠弰鍓с仛閺冨爼妫�
	 * @param sdate
	 * @return
	 */
	public static String friendly_time(String sdate) {
		Date time = toDate(sdate);
		if(time == null) {
			return "Unknown";
		}
		String ftime = "";
		Calendar cal = Calendar.getInstance();
		
		//閸掋倖鏌囬弰顖氭儊閺勵垰鎮撴稉锟芥径锟�
		String curDate = dateFormater2.get().format(cal.getTime());
		String paramDate = dateFormater2.get().format(time);
		if(curDate.equals(paramDate)){
			int hour = (int)((cal.getTimeInMillis() - time.getTime())/3600000);
			if(hour == 0)
				ftime = Math.max((cal.getTimeInMillis() - time.getTime()) / 60000,1)+"閸掑棝鎸撻崜锟�";
			else 
				ftime = hour+"鐏忓繑妞傞崜锟�";
			return ftime;
		}
		
		long lt = time.getTime()/86400000;
		long ct = cal.getTimeInMillis()/86400000;
		int days = (int)(ct - lt);		
		if(days == 0){
			int hour = (int)((cal.getTimeInMillis() - time.getTime())/3600000);
			if(hour == 0)
				ftime = Math.max((cal.getTimeInMillis() - time.getTime()) / 60000,1)+"閸掑棝鎸撻崜锟�";
			else 
				ftime = hour+"鐏忓繑妞傞崜锟�";
		}
		else if(days == 1){
			ftime = "閺勩劌銇�";
		}
		else if(days == 2){
			ftime = "閸撳秴銇�";
		}
		else if(days > 2 && days <= 10){ 
			ftime = days+"婢垛晛澧�";			
		}
		else if(days > 10){			
			ftime = dateFormater2.get().format(time);
		}
		return ftime;
	}
	
	/**
	 * 閸掋倖鏌囩紒娆忕暰鐎涙顑佹稉鍙夋闂傚瓨妲搁崥锔胯礋娴犲﹥妫�
	 * @param sdate
	 * @return boolean
	 */
	public static boolean isToday(String sdate){
		boolean b = false;
		Date time = toDate(sdate);
		Date today = new Date();
		if(time != null){
			String nowDate = dateFormater2.get().format(today);
			String timeDate = dateFormater2.get().format(time);
			if(nowDate.equals(timeDate)){
				b = true;
			}
		}
		return b;
	}
	
	/**
	 * 閸掋倖鏌囩紒娆忕暰鐎涙顑佹稉鍙夋Ц閸氾妇鈹栭惂鎴掕閵嗭拷
	 * 缁岃櫣娅ф稉鍙夋Ц閹稿洨鏁辩粚鐑樼壐閵嗕礁鍩楃悰銊ь儊閵嗕礁娲栨潪锔绢儊閵嗕焦宕茬悰宀�顑佺紒鍕灇閻ㄥ嫬鐡х粭锔胯
	 * 閼汇儴绶崗銉ョ摟缁楋缚瑕嗘稉绨剈ll閹存牜鈹栫�涙顑佹稉璇х礉鏉╂柨娲杢rue
	 * @param input
	 * @return boolean
	 */
	public static boolean isEmpty( String input ) 
	{
		if ( input == null || "".equals( input ) )
			return true;
		
		for ( int i = 0; i < input.length(); i++ ) 
		{
			char c = input.charAt( i );
			if ( c != ' ' && c != '\t' && c != '\r' && c != '\n' )
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * 閸掋倖鏌囬弰顖欑瑝閺勵垯绔存稉顏勬値濞夋洜娈戦悽闈涚摍闁喕娆㈤崷鏉挎絻
	 * @param email
	 * @return
	 */
	public static boolean isEmail(String email){
		if(email == null || email.trim().length()==0) 
			return false;
	    return emailer.matcher(email).matches();
	}
	/**
	 * 鐎涙顑佹稉鑼舵祮閺佸瓨鏆�
	 * @param str
	 * @param defValue
	 * @return
	 */
	public static int toInt(String str, int defValue) {
		try{
			return Integer.parseInt(str);
		}catch(Exception e){}
		return defValue;
	}
	/**
	 * 鐎电钖勬潪顒佹殻閺侊拷
	 * @param obj
	 * @return 鏉烆剚宕插鍌氱埗鏉╂柨娲� 0
	 */
	public static int toInt(Object obj) {
		if(obj==null) return 0;
		return toInt(obj.toString(),0);
	}
	/**
	 * 鐎电钖勬潪顒佹殻閺侊拷
	 * @param obj
	 * @return 鏉烆剚宕插鍌氱埗鏉╂柨娲� 0
	 */
	public static long toLong(String obj) {
		try{
			return Long.parseLong(obj);
		}catch(Exception e){}
		return 0;
	}
	/**
	 * 鐎涙顑佹稉鑼舵祮鐢啫鐨甸崐锟�
	 * @param b
	 * @return 鏉烆剚宕插鍌氱埗鏉╂柨娲� false
	 */
	public static boolean toBool(String b) {
		try{
			return Boolean.parseBoolean(b);
		}catch(Exception e){}
		return false;
	}
}
