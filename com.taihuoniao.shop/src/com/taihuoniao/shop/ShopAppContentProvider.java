package com.taihuoniao.shop;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;
public class ShopAppContentProvider extends ContentProvider {
	private static final int DATABASE_VERSION = 4;	
	public static final String AUTHORITY = "ShopApp";	
	public static final String DISTRICT_TABLE_NAME = "District";
	public static final Uri DISTRICT_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + DISTRICT_TABLE_NAME);
	
	public static class District implements BaseColumns{
		public static final String NAME = "name";
		public static final String PARENT = "parent";
	}
	public static int tryInt(JSONObject row,String name){
		int ret = 0;
		try{
			ret = row.getInt(name);
		}catch(JSONException e){
			//e.printStackTrace();
		}
		return ret;
	}
	public static double tryDouble(JSONObject row,String name){
		double ret = 0;
		try{
			ret = row.getDouble(name);
		}catch(JSONException e){
			//e.printStackTrace();
		}
		return ret;
	}	
	public static long tryLong(JSONObject row,String name){
		long ret = 0;
		try{
			ret = row.getLong(name);
		}catch(JSONException e){
			//e.printStackTrace();
		}
		return ret;
	}	
	public static String tryString(JSONObject row,String name){
		String ret = "";
		try{
			ret = row.getString(name);
		}catch(JSONException e){
			e.printStackTrace();
		}
		return ret;
	}	
	public static JSONObject tryJSONObject(JSONObject row,String name){
		JSONObject ret=null;
		try{
			ret = row.getJSONObject(name);
		}catch(JSONException e){
			e.printStackTrace();
		}
		return ret;
	}	
	public static String[] tryStringArray(JSONObject row,String name){
		String[] ret = null;
		try{
			JSONArray array = row.getJSONArray(name);
			ret = new String[array.length()];
			for(int i=0; i<array.length(); i++){
				ret[i] = array.getString(i);
			}
		}catch(JSONException e){
			e.printStackTrace();
		}
		return ret;
	}	
	public static String Array2String(String[] array){
		StringBuilder sb = new StringBuilder();
		if(array != null){
			for(int i=0; i<array.length;i++){
				if(sb.length() != 0)
					sb.append("|");
				sb.append(array[i]);
			}
		}
		return sb.toString();
	}
	public static String[] String2Array(String values){
		if(values != null)
			return values.split("\\|");
		return null;
	}	
	
	public static final String PRODUCTCATEGORY_TABLE_NAME = "ProductCategory";
	public static final Uri PRODUCTCATEGORY_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PRODUCTCATEGORY_TABLE_NAME);
	public static class ProductCategory implements BaseColumns{
		public static final String NAME = "name";
		public static final String TITLE = "title";
		public static final String TOTAL_COUNT = "total_count";
		public static final String APP_COVER_URL = "app_cover_url";				
		public int _id;
		public String name;
		public String title;
		public int total_count;
		public String app_cover_url;
		public void ReadJasonData(JSONObject row){
			try{
				_id = row.getInt(_ID);
				name = row.getString(NAME);
				title = row.getString(TITLE);
				total_count = row.getInt(TOTAL_COUNT);				
				app_cover_url = row.getString(APP_COVER_URL);
			}catch(JSONException e){
				//e.printStackTrace();
			}			
		}
		public void UpdateToDataBase(Context context){
			Uri uri = ContentUris.appendId(ShopAppContentProvider.PRODUCTCATEGORY_CONTENT_URI.buildUpon(),_id).build();
			ContentValues values = new ContentValues();
			//id放入到uri中，就不用put到values中了
			values.put(NAME,name);
			values.put(TITLE,title);
			values.put(TOTAL_COUNT,total_count);
			values.put(APP_COVER_URL, app_cover_url);
			context.getContentResolver().update(uri, values, null, null);			
		}
		public void ReadFromCursor(Cursor cursor){
			_id = cursor.getInt(cursor.getColumnIndex(_ID));
			name = cursor.getString(cursor.getColumnIndex(NAME));
			title = cursor.getString(cursor.getColumnIndex(TITLE));
			total_count = cursor.getInt(cursor.getColumnIndex(TOTAL_COUNT));
			app_cover_url = cursor.getString(cursor.getColumnIndex(APP_COVER_URL));
		}
	}
	public static class ProductItem implements BaseColumns{
		public static final String TABLE_NAME = "ProductItem";
		public static final Uri URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
		//
		public static final String TITLE = "title";
		public static final String ADVANTAGE = "advantage";
		public static final String SALE_PRICE = "sale_price";
		public static final String MARKET_PRICE = "market_price";
		public static final String PRESALE_PEOPLE = "presale_people";
		public static final String PRESALE_PERCENT = "presale_percent";
		public static final String CATEGORY_ID = "category_id";
		public static final String SUMMARY = "summary";
		public static final String PRESALE_FINISH_TIME = "presale_finish_time";		
		public static final String CAN_SALED = "can_saled";				
		public static final String TOPIC_COUNT = "topic_count";
		public static final String COVER_URL = "cover_url";
		public static final String SMALL_AVATAR_URL = "small_avatar_url";
		public static final String STICK = "stick";
		//product view
		public static final String TAOBAO_IID = "taobao_iid";
		public static final String CONTENT = "content";
		public static final String TAGS ="tags";
		public static final String VIDEO = "video";
		public static final String COVER_ID = "cover_id";
		public static final String ASSET = "asset";
		public static final String ASSET_COUNT = "asset_count";
		public static final String IS_TRY = "is_try";
		//
		public static final String IS_FAVORITE = "is_favorite";
		public static final String IS_LOVE = "is_love";
		public static final String CONTENT_VIEW_URL = "content_view_url";
		public static final String SKUS = "skus";
		public static final String SNATCHED = "snatched";
		public static String getCreateTableSQL(){
			String sql = "CREATE TABLE " + TABLE_NAME + " (" +
	        		ProductItem._ID + " INTEGER PRIMARY KEY," +
	        		ProductItem.TITLE + " TEXT," +
	        		ProductItem.ADVANTAGE + " TEXT," +
	        		ProductItem.SALE_PRICE + " DOUBLE," +
	        		ProductItem.MARKET_PRICE + " DOUBLE," +
	        		ProductItem.PRESALE_PEOPLE + " INTEGER," +
	        		ProductItem.PRESALE_PERCENT + " DOUBLE," +
	        		ProductItem.PRESALE_FINISH_TIME + " INTEGER," +
	        		ProductItem.SUMMARY + " TEXT," +
	        		ProductItem.COVER_URL + " TEXT," +
	        		ProductItem.SMALL_AVATAR_URL + " TEXT," +	
	        		ProductItem.CATEGORY_ID + " INTEGER ," +
	        		ProductItem.CAN_SALED + " INTEGER ," +
	        		ProductItem.STICK + " INTEGER ," +
	        		//product view 
	        		ProductItem.TAOBAO_IID + " INTEGER ," +
	        		ProductItem.CONTENT + " TEXT ," +
	        		ProductItem.TAGS + " TEXT ," +
	        		ProductItem.VIDEO + " TEXT ," +
	        		ProductItem.COVER_ID + " TEXT ," +
	        		ProductItem.ASSET + " TEXT ," +
	        		ProductItem.IS_TRY + " INTEGER ," +
	        		ProductItem.IS_FAVORITE + " INTEGER ," +
	        		ProductItem.IS_LOVE + " INTEGER ," +
	        		ProductItem.CONTENT_VIEW_URL + " TEXT ," +
	        		ProductItem.SKUS + " TEXT ," +	   
	        		ProductItem.SNATCHED + " INTEGER ," +
	        		ProductItem.TOPIC_COUNT + " INTEGER DEFAULT 0" +	        		
	                   ");";
			return sql;
		}
		
		public int _id;
		public String title;
		public String advantage;
		public double sale_price;
		public double market_price;
		public int presale_people;
		public double presale_percent;
		public int designer_id;
		public int category_id;
		public int stage;
		public int vote_favor_count;
		public int vote_oppose_count;
		public String summary;
		public int succeed;
		public long voted_finish_time;
		public long presale_finish_time;
		public long snatched_time;
		public int inventory;
		public int can_saled;
		public int topic_count;
		public double presale_money;
		public int snatched;
		public int presale_goals;
		public String cover_url;
		public String small_avatar_url;
		public int stick;
	//product view		
		//title     string
		//advantage     str      优势
		//summary     str     简述
		public int taobao_iid;
		public String content;
		public String[] tags;//
		public String video;
		public String cover_id;
		public String[] asset;//图片地址
		public int asset_count;//图片数量
		public int is_try;//是否试用
		//
		public int is_favorite;
		public int is_love;
		public String content_view_url;
		public List<SKU>skus = new ArrayList<SKU>();
		public static class SKU{
		//{"_id":1020885052,"product_id":1020885051,"name":"","mode":"red","price":0.01,"quantity":15,"sold":25,"limited_count":0,"sync_count":0,"summary":"","bad_count":0,"bad_tag":"","revoke_count":0,"shelf":0,"stage":9,"status":0,"created_on":1423058796,"updated_on":1446451910}
			public int _id;
			public int product_id;
			public String mode;
			public double price;
			public long quantity;
			public long sold;
			public long limited_count;
			public String summary;
			public int stage;
			public int status;
			public long created_on;
			public void ReadJasonData(JSONObject item){
				_id = tryInt(item,"_id");
				product_id = tryInt(item,"_id");
				mode = tryString(item,"mode");
				price = tryDouble(item,"price");
				quantity = tryLong(item,"quantity");
				sold = tryLong(item,"sold");
				limited_count = tryLong(item,"limited_count");
				summary = tryString(item,"summary");
				stage = tryInt(item,"stage");
				status = tryInt(item,"status");
				created_on = tryLong(item,"created_on");						
			}
			
		}
		public Designer designer;
		public static class Designer{
			public String _id;
			public String screen_name;
			public String city;
			public String profile_job;
			public String big_avatar_url;
			public void ReadJasonData(JSONObject item){
				_id = tryString(item,"_id");
				screen_name = tryString(item,"screen_name");
				city = tryString(item,"city");
				JSONObject profile = tryJSONObject(item,"profile_job");
				if(profile != null){
					profile_job = tryString(profile,"job");
				}
				big_avatar_url = tryString(item,"big_avatar_url");
				
			}
		}	
/*		
		{"success":true,"is_error":false,"status":"0","message":"\u8bf7\u6c42\u6210\u529f",
		"data":{
			"_id":1020885051,"taobao_iid":null,"title":"\u6d4b\u8bd5sku","advantage":"\u65e0","summary":"\u6d4b\u8bd5",
			"content":null,"tags":["\u6d4b\u8bd5"],"video":[],
			"cover_id":"54d2273f3ffca265238b46c6","asset":["http:\/\/frbird.qiniudn.com\/product\/150204\/54d2271d3ffca264238b46c8-1-bi.jpg"],
			"asset_count":1,"category_id":30,"user_id":10,
			"designer_id":1,"cost_price":0,"market_price":188,"sale_price":88,"hot_price":0,"inventory":6,
			"sale_count":31,"sync_count":0,"attributes":{"width":0,"height":0,"weight":0,"color":0},
			"meta":{"unit":null},"sku_count":0,"mode_count":0,"presale_inventory":0,"presale_count":0,"presale_people":0,"presale_money":0,"presale_goals":0,
			"presale_start_time":false,"presale_finish_time":143999,"voted_start_time":false,"voted_finish_time":143999,"snatched":0,"snatched_time":1427375820,
			"appoint_count":0,"trial":0,"view_count":432,"favorite_count":2,"love_count":2,"comment_count":16,"comment_star":77,"topic_count":2,
			"vote_favor_count":0,"vote_oppose_count":0,"score_count":0,"score_average":0,
			"score":{"usability":0,"design":0,"creativity":0,"content":0},"stage":9,"process_voted":0,"process_presaled":0,"process_saled":1,"approved":1,
			"succeed":0,"published":1,"stick":1,"okcase":0,"state":0,"deleted":0,"random":1210370927,"created_on":1423058780,"updated_on":1447324677,
			"view_url":"http:\/\/dev.taihuoniao.com\/shop\/1020885051.html","snatched_price":0.1,"snatched_count":4,"last_editor_id":10,
			"exchanged":0,"max_bird_coin":0,"min_bird_coin":0,"exchange_price":0,"exchange_count":0,"short_title":"\u6d4b\u8bd5sku","featured":1,
			"user":{"_id":10,"account":"admin@taihuoniao.com","avatar":{"big":"avatar\/151009\/561786363ffca2ad608b474c","medium":"avatar\/151009\/561786363ffca2ad608b474c","small":"avatar\/151009\/561786363ffca2ad608b474c","mini":"avatar\/151009\/561786363ffca2ad608b474c"},"city":"\u5317\u4eac","counter":{"message_count":0,"notice_count":0,"alert_count":7,"fans_count":0,"comment_count":33,"people_count":0},"created_on":1400729499,"email":"","fans_count":2,"first_login":0,"follow_count":0,"from_site":1,"love_count":0,"nickname":"\u592a\u706b\u9e1f","permission":[],"product_count":1,"profile":{"realname":"\u592a\u706b\u9e1f","phone":"1111","address":"bbbbb","job":"\u5b75\u5316\u5668","zip":"123","weixin":"123","im_qq":"123"},"qq_uid":null,"role_id":8,"sex":0,"sina_uid":null,"state":2,"summary":"\u4eba\u751f\u662f\u573a\u5927\u8bbe\u8ba1","tags":["\u521b\u65b0\u8bbe\u8ba1","\u667a\u80fd\u4ea7\u54c1","\u53ef\u7a7f\u6234\u4ea7\u54c1","\u539f\u521b\u4ea7\u54c1","\u5b75\u5316\u5668"],"topic_count":65,"identify":{"d3in_tag":1},"wx_open_id":"","wx_union_id":null,"mentor":50,"kind":1,"id":10,"true_nickname":"\u592a\u706b\u9e1f","screen_name":"\u592a\u706b\u9e1f","big_avatar_url":"http:\/\/frbird.qiniudn.com\/avatar\/151009\/561786363ffca2ad608b474c-avb.jpg","medium_avatar_url":"http:\/\/frbird.qiniudn.com\/avatar\/151009\/561786363ffca2ad608b474c-avm.jpg","small_avatar_url":"http:\/\/frbird.qiniudn.com\/avatar\/151009\/561786363ffca2ad608b474c-avs.jpg","mini_avatar_url":"http:\/\/frbird.qiniudn.com\/avatar\/151009\/561786363ffca2ad608b474c-avn.jpg","home_url":"http:\/\/dev.taihuoniao.com\/user\/10\/","view_follow_url":"http:\/\/dev.taihuoniao.com\/user\/10\/follow\/","view_fans_url":"http:\/\/dev.taihuoniao.com\/user\/10\/fans\/","is_ok":true,"is_chief":true,"is_customer":true,"is_editor":true,"is_admin":true,"can_edit":true,"can_admin":true,"mentor_info":{"id":50,"name":"\u5b98\u65b9\u8ba4\u8bc1"},"last_char":"0","ext_state":{"_id":10,"rank_id":6,"next_rank_id":7,"rank_point":133,"updated_on":1447330529,"user_rank":{"_id":6,"rank_id":6,"next_rank_id":7,"title":"\u9e1f\u5217\u5175","point_type":"exp","point_amount":350,"note":null,"updated_on":1427693033,"next_rank":{"_id":7,"rank_id":7,"next_rank_id":8,"title":"\u9e1f\u4e0b\u58eb","point_type":"exp","point_amount":480,"note":null,"updated_on":1427693033,"next_rank":{"_id":8,"rank_id":8,"next_rank_id":9,"title":"\u9e1f\u4e0b\u58eb","point_type":"exp","point_amount":630,"note":null,"updated_on":1427693033,"next_rank":{"_id":9,"rank_id":9,"next_rank_id":10,"title":"\u9e1f\u4e0b\u58eb","point_type":"exp","point_amount":800,"note":null,"updated_on":1427693033,"next_rank":{"_id":10,"rank_id":10,"next_rank_id":11,"title":"\u9e1f\u4e2d\u58eb","point_type":"exp","point_amount":990,"note":null,"updated_on":1427693033,"next_rank":{"_id":11,"rank_id":11,"next_rank_id":12,"title":"\u9e1f\u4e2d\u58eb","point_type":"exp","point_amount":1200,"note":null,"updated_on":1427693033,"next_rank":{"_id":12,"rank_id":12,"next_rank_id":13,"title":"\u9e1f\u4e2d\u58eb","point_type":"exp","point_amount":1430,"note":null,"updated_on":1427693033,"next_rank":{"_id":13,"rank_id":13,"next_rank_id":14,"title":"\u9e1f\u519b\u58eb","point_type":"exp","point_amount":1680,"note":null,"updated_on":1427693033,"next_rank":{"_id":14,"rank_id":14,"next_rank_id":15,"title":"\u9e1f\u519b\u58eb","point_type":"exp","point_amount":1950,"note":null,"updated_on":1427693033,"next_rank":{"_id":15,"rank_id":15,"next_rank_id":16,"title":"\u9e1f\u519b\u58eb","point_type":"exp","point_amount":2240,"note":null,"updated_on":1427693033,,
			"designer":null,"cover":{"_id":{"$id":"54d2273f3ffca265238b46c6"},"parent_id":1020885051,"filepath":"product\/150204\/54d2271d3ffca264238b46c8-1","filename":"2014-11-26 15:01:26 \u7684\u5c4f\u5e55\u622a\u56fe.png","size":209139,"width":1366,"height":768,"desc":null,"thumbnails":{"mini":{"view_url":"http:\/\/frbird.qiniudn.com\/product\/150204\/54d2271d3ffca264238b46c8-1-s.jpg"},"tiny":{"view_url":"http:\/\/frbird.qiniudn.com\/product\/150204\/54d2271d3ffca264238b46c8-1-ti.jpg"},"small":{"view_url":"http:\/\/frbird.qiniudn.com\/product\/150204\/54d2271d3ffca264238b46c8-1-sm.jpg"},"medium":{"view_url":"http:\/\/frbird.qiniudn.com\/product\/150204\/54d2271d3ffca264238b46c8-1-me.jpg"},"large":{"view_url":"http:\/\/frbird.qiniudn.com\/product\/150204\/54d2271d3ffca264238b46c8-1-la.jpg"},"big":{"view_url":"http:\/\/frbird.qiniudn.com\/product\/150204\/54d2271d3ffca264238b46c8-1-bi.jpg"},"huge":{"view_url":"http:\/\/frbird.qiniudn.com\/product\/150204\/54d2271d3ffca264238b46c8-1-hu.jpg"},"massive":{"view_url":"http:\/\/frbird.qiniudn.com\/product\/150204\/54d2271d3ffca264238b46c8-1-ma.jpg"},"resp":{"view_url":"http:\/\/frbird.qiniudn.com\/product\/150204\/54d2271d3ffca264238b46c8-1-resp.jpg"},"hd":{"view_url":"http:\/\/frbird.qiniudn.com\/product\/150204\/54d2271d3ffca264238b46c8-1-hd.jpg"},"md":{"view_url":"http:\/\/frbird.qiniudn.com\/product\/150204\/54d2271d3ffca264238b46c8-1-m.jpg"},"hm":{"view_url":"http:\/\/frbird.qiniudn.com\/product\/150204\/54d2271d3ffca264238b46c8-1-hm.jpg"},"ava":{"view_url":"http:\/\/frbird.qiniudn.com\/product\/150204\/54d2271d3ffca264238b46c8-1-ava.jpg"}},"asset_type":10,"id":"54d2273f3ffca265238b46c6","fileurl":"http:\/\/frbird.qiniudn.com\/product\/150204\/54d2271d3ffca264238b46c8-1","__extend__":true},
			"category":{"_id":30,"domain":1,
						"gid":0,"is_open":1,"name":"heartbeat","order_by":5,"pid":0,"reply_count":211,"state":0,"summary":"","tags":[""],
						"title":"\u8fd0\u52a8\u5065\u5eb7",
						"total_count":111,"tags_s":"","view_url":"http:\/\/dev.taihuoniao.com\/fever\/c30\/","__extend__":true
						},
			"mm_view_url":"http:\/\/dev.taihuoniao.com\/wechat\/1020885051.html","wap_view_url":"http:\/\/t.taihuoniao.com\/shop\/1020885051.html",
			"subject_view_url":"http:\/\/dev.taihuoniao.com\/topic\/subject?id=1020885051&page=1","vote_view_url":"http:\/\/dev.taihuoniao.com\/fever\/1020885051.html",
			"presale_view_url":"http:\/\/dev.taihuoniao.com\/sale\/1020885051.html","comment_view_url":"http:\/\/dev.taihuoniao.com\/shop\/view\/1020885051\/1",
			"tags_s":"\u6d4b\u8bd5","vote_count":0,"stage_label":"\u70ed\u552e\u4e2d","hotsale":1,"strip_summary":"\u6d4b\u8bd5","expert_assess":false,"presale_percent":0,
			"voted_finished":true,"presale_finished":true,"snatched_start":true,"can_saled":true,"is_try":0,"newest":0,"hot":0,"stars":5,"stars_value":9.625,"__extend__":true,
			"content_view_url":"http:\/\/dev.taihuoniao.com\/view\/product_show?id=1020885051&current_user_id=477411","is_favorite":0,"is_love":0,
			"cover_url":"http:\/\/frbird.qiniudn.com\/product\/150204\/54d2271d3ffca264238b46c8-1-bi.jpg",
			"skus":[
				{"_id":1020885052,"product_id":1020885051,"name":"","mode":"red","price":0.01,"quantity":15,"sold":25,"limited_count":0,"sync_count":0,"summary":"","bad_count":0,"bad_tag":"","revoke_count":0,"shelf":0,"stage":9,"status":0,"created_on":1423058796,"updated_on":1446451910},
				{"_id":1020885053,"product_id":1020885051,"name":"","mode":"green","price":1.1,"quantity":8,"sold":3,"limited_count":0,"sync_count":0,"summary":"","bad_count":0,"bad_tag":"","revoke_count":0,"shelf":0,"stage":9,"status":0,"created_on":1423058814,"updated_on":1445308018}],
			"skus_count":2,"current_user_id":477411},"current_user_id":477411}		
*/		
		public void ReadJasonDataFPV(JSONObject row){
			try{
				_id = row.getInt(_ID);
				title = row.getString(TITLE);
				advantage = row.getString(ADVANTAGE);
				//sale_price = row.getDouble(SALE_PRICE);
				summary = row.getString(SUMMARY);
				taobao_iid = tryInt(row,TAOBAO_IID);
				content = row.getString(CONTENT);
				tags = tryStringArray(row,TAGS);
				video = row.getString(VIDEO);
				cover_id = row.getString(COVER_ID);
				asset = tryStringArray(row,ASSET);
				is_try = row.getInt(IS_TRY);	
				is_favorite = row.getInt(IS_FAVORITE);
				is_love = row.getInt(IS_LOVE);
				content_view_url = row.getString(CONTENT_VIEW_URL);
				can_saled = row.getBoolean(CAN_SALED)?1:0;
				sale_price = row.getDouble(SALE_PRICE);
				cover_url = row.getString(COVER_URL);
				JSONArray skus = row.getJSONArray("skus");
				this.skus.clear();
				if(skus != null && skus.length()>0){
					for(int i=0; i<skus.length(); i++){
						SKU sku = new SKU();
						JSONObject item = skus.getJSONObject(i);
						sku.ReadJasonData(item);
						this.skus.add(sku);
					}
				}
				snatched = tryInt(row, SNATCHED);
				//designer
				JSONObject designerJSONObject = row.getJSONObject("designer");
				designer = new Designer();
				if(designerJSONObject != null){
					designer.ReadJasonData(designerJSONObject);
				}else{
					JSONObject user = row.getJSONObject("user");
					if(user != null){
						designer.screen_name = tryString(user, "nickname");						
						JSONObject avatar = user.getJSONObject("avatar");
						if(avatar != null){
							designer.big_avatar_url = ShopUtils.getBaseUrl()+ "//" + tryString(avatar, "big");
						}
					}
				}
			}catch(JSONException e){
				e.printStackTrace();
			}						
		}
		
		
		public void UpdateToDataBaseFPV(Context context){
			Uri uri = ContentUris.appendId(ShopAppContentProvider.ProductItem.URI.buildUpon(),_id).build();
			ContentValues values = new ContentValues();
			//id放入到uri中，就不用put到values中了
			values.put(TITLE,title);
			values.put(ADVANTAGE, advantage);
			values.put(SUMMARY,summary);
			values.put(TAOBAO_IID, taobao_iid);
			values.put(CONTENT, content);
			values.put(VIDEO, video);
			values.put(COVER_ID, cover_id);
			values.put(IS_TRY, is_try);
			values.put(TAGS, Array2String(tags));
			values.put(ASSET, Array2String(asset));
			values.put(IS_FAVORITE, is_favorite);
			values.put(IS_LOVE, is_love);
			values.put(CONTENT_VIEW_URL, content_view_url);
			JSONArray array =new JSONArray();
			for(SKU sku:skus){
				array.put(sku);
			}
			values.put(SKUS,array.toString());
			
			context.getContentResolver().update(uri, values, null, null);			
		}
		public void ReadFromCursorFPV(Cursor cursor){
			_id = cursor.getInt(cursor.getColumnIndex(_ID));
			title = cursor.getString(cursor.getColumnIndex(TITLE));
			advantage = cursor.getString(cursor.getColumnIndex(ADVANTAGE));
			sale_price = cursor.getFloat(cursor.getColumnIndex(SALE_PRICE));
			summary = cursor.getString(cursor.getColumnIndex(SUMMARY));
			category_id = cursor.getInt(cursor.getColumnIndex(CATEGORY_ID));
			can_saled = cursor.getInt(cursor.getColumnIndex(CAN_SALED));
			topic_count = cursor.getInt(cursor.getColumnIndex(TOPIC_COUNT));
			cover_url = cursor.getString(cursor.getColumnIndex(COVER_URL));
			stick = cursor.getInt(cursor.getColumnIndex(STICK));
			//
			taobao_iid = cursor.getInt(cursor.getColumnIndex(TAOBAO_IID));
			content = cursor.getString(cursor.getColumnIndex(CONTENT));
			video = cursor.getString(cursor.getColumnIndex(VIDEO));
			cover_id = cursor.getString(cursor.getColumnIndex(COVER_ID));
			is_try = cursor.getInt(cursor.getColumnIndex(IS_TRY));
			tags = String2Array(cursor.getString(cursor.getColumnIndex(TAGS)));
			asset = String2Array(cursor.getString(cursor.getColumnIndex(ASSET)));
			is_favorite = cursor.getInt(cursor.getColumnIndex(IS_FAVORITE));
			is_love = cursor.getInt(cursor.getColumnIndex(IS_LOVE));
			content_view_url = cursor.getString(cursor.getColumnIndex(CONTENT_VIEW_URL));
			String jsonSKUS  = cursor.getString(cursor.getColumnIndex(SKUS));
			skus.clear();
			try{
				JSONArray array =new JSONArray(jsonSKUS);
				for(int i=0;i<array.length(); i++){
					SKU sku = new SKU();
					JSONObject item = array.getJSONObject(i);
					sku.ReadJasonData(item);
					this.skus.add(sku);
				}				
			}catch(JSONException e){
				e.printStackTrace();
			}
			
		}		
		
		public void ReadJasonData(JSONObject row){
/*
			{"success":true,"is_error":false,"status":"0","message":"\u8bf7\u6c42\u6210\u529f","data":{"total_rows":62,"rows":[{
				"_id":1091139060,
				"title":"10\u5143\u6d4b\u8bd5\u5546\u54c1",
				"advantage":"10\u5143\u6d4b\u8bd5\u5546\u54c1",
				"sale_price":10,
				"market_price":10,
				"presale_people":0,
				"presale_percent":0,
				"cover_id":"55fedc253ffca2cb568b615c",
				"designer_id":10,
				"category_id":50,
				"stage":9,
				"vote_favor_count":0,
				"vote_oppose_count":0,
				"summary":"10\u5143\u6d4b\u8bd5\u5546\u54c1",
				"succeed":0,
				"voted_finish_time":143999,
				"presale_finish_time":143999,
				"snatched_time":false,
				"inventory":99,
				"can_saled":true,
				"topic_count":0,
				"presale_money":0,
				"snatched":0,
				"presale_goals":0,
				"cover_url":"http:\/\/frbird.qiniudn.com\/product\/150921\/55fedc103ffca2ca568b618b-1-me.jpg",
				"username":"\u592a\u706b\u9e1f",
				"small_avatar_url":"http:\/\/frbird.qiniudn.com\/avatar\/151009\/561786363ffca2ad608b474c-avs.jpg",
				"content_view_url":"http:\/\/dev.taihuoniao.com\/view\/product_show?id=1091139060&current_user_id=477411"},
			*/
			try{
				_id = row.getInt(_ID);
				title = row.getString(TITLE);
				advantage = row.getString(ADVANTAGE);
				sale_price = row.getDouble(SALE_PRICE);
				market_price = row.getDouble(MARKET_PRICE);
				presale_people = row.getInt(PRESALE_PEOPLE);
				presale_percent = row.getDouble(PRESALE_PERCENT);
				category_id = row.getInt(CATEGORY_ID);
				summary = row.getString(SUMMARY);
				presale_finish_time = tryLong(row,PRESALE_FINISH_TIME);
				topic_count = row.getInt(TOPIC_COUNT);				
				cover_url = row.getString(COVER_URL);
				small_avatar_url = row.getString(SMALL_AVATAR_URL);
				stick = row.getInt(STICK);
			}catch(JSONException e){
				e.printStackTrace();
			}			
		}		
		
		public void UpdateToDataBase(Context context){
			Uri uri = ContentUris.appendId(ShopAppContentProvider.ProductItem.URI.buildUpon(),_id).build();
			ContentValues values = new ContentValues();
			//id放入到uri中，就不用put到values中了
			values.put(TITLE,title);
			values.put(ADVANTAGE, advantage);
			values.put(SALE_PRICE, sale_price);
			values.put(MARKET_PRICE, market_price);
			values.put(PRESALE_PEOPLE, presale_people);
			values.put(PRESALE_FINISH_TIME, presale_finish_time);
			values.put(PRESALE_PERCENT, presale_percent);
			values.put(CATEGORY_ID, category_id);
			values.put(SUMMARY,summary);
			values.put(TOPIC_COUNT,topic_count);
			values.put(CAN_SALED, can_saled);
			values.put(COVER_URL, cover_url);
			values.put(SMALL_AVATAR_URL, small_avatar_url);
			values.put(STICK, stick);
			context.getContentResolver().update(uri, values, null, null);			
		}
		public void ReadFromCursor(Cursor cursor){
			_id = cursor.getInt(cursor.getColumnIndex(_ID));
			title = cursor.getString(cursor.getColumnIndex(TITLE));
			advantage = cursor.getString(cursor.getColumnIndex(ADVANTAGE));
			sale_price = cursor.getFloat(cursor.getColumnIndex(SALE_PRICE));
			market_price = cursor.getFloat(cursor.getColumnIndex(MARKET_PRICE));
			presale_people = cursor.getInt(cursor.getColumnIndex(PRESALE_PEOPLE));
			presale_finish_time = cursor.getLong(cursor.getColumnIndex(PRESALE_FINISH_TIME));
			presale_percent = cursor.getFloat(cursor.getColumnIndex(PRESALE_PERCENT));
			summary = cursor.getString(cursor.getColumnIndex(SUMMARY));
			category_id = cursor.getInt(cursor.getColumnIndex(CATEGORY_ID));
			can_saled = cursor.getInt(cursor.getColumnIndex(CAN_SALED));
			topic_count = cursor.getInt(cursor.getColumnIndex(TOPIC_COUNT));
			cover_url = cursor.getString(cursor.getColumnIndex(COVER_URL));
			stick = cursor.getInt(cursor.getColumnIndex(STICK));
		}		
		
	}	
	//SKU来自ProductView          			{"_id":1020885052,"product_id":1020885051,"name":"","mode":"red","price":0.01,"quantity":15,"sold":25,"limited_count":0,"sync_count":0,"summary":"","bad_count":0,"bad_tag":"","revoke_count":0,"shelf":0,"stage":9,"status":0,"created_on":1423058796,"updated_on":1446451910}
	//ShoppingItem来自ShoppingOrder 			{"sku":"1080959165","product_id":"1080959165","quantity":1,"price":799,"sale_price":799,"title":"LISA\u667a\u80fd\u5b55\u8868\u2014\u60a8\u7684\u5b55\u671f\u8d34\u8eab\u95fa\u871c ","cover":"http:\/\/frbird.qiniudn.com\/product\/140809\/53e5d22d989a6a5d598b621f-1-s.jpg","view_url":"http:\/\/dev.taihuoniao.com\/shop\/view-1080959165-1.html","subtotal":799}
	//来自ShoppingOrderList	                 "sku":1020885051,"product_id":1020885051,"quantity":1,"price":88,"sale_price":88,"name":"\u6d4b\u8bd5sku","cover_url":"http:\/\/frbird.qiniudn.com\/product\/150204\/54d2271d3ffca264238b46c8-1-s.jpg"
	public static class CartItem implements BaseColumns{
		public static final String PRODUCT_ID = "product_id";
		public static final String PRICE = "price";
		public static final String SALE_PRICE = "sale_price";
		public static final String TITLE = "title";
		public static final String COVER = "cover";
		public static final String QUANTITY = "quantity";
		public static final String NAME = "name";
		public static final String MODE = "mode";
		
		public int _id;
		public int product_id;
		public double price;
		public double sale_price;
		public String title;
		public String cover;
		public int quantity;
		public String name;
		public String mode;
		
		public static final String TABLE_NAME = "CartItem";
		public static final Uri URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
		public static String getCreateTableSQL(){
			return "CREATE TABLE " + TABLE_NAME + " (" +
					_ID + " INTEGER PRIMARY KEY," +	
	        		PRODUCT_ID+ " INTEGER," +	        		
	        		PRICE + " DOUBLE," +
	        		SALE_PRICE + " DOUBLE," +
	        		TITLE + " TEXT," +	     
	        		COVER + " TEXT," +
	        		QUANTITY + " INTEGER ," +
	        		NAME + " TEXT," +	
	        		MODE + " TEXT " +
	                   ");";
		}				
//		public void readJsonData(JSONObject item){
//			try{
//				_id = item.getInt("sku");
//				product_id = ShopAppContentProvider.tryString(item,"product_id");
//				price = ShopAppContentProvider.tryDouble(item,"price");
//				sale_price = ShopAppContentProvider.tryDouble(item,"sale_price");
//				title = ShopAppContentProvider.tryString(item,"title");
//				cover = ShopAppContentProvider.tryString(item,"cover");
//				quantity = ShopAppContentProvider.tryInt(item,"quantity");
//				mode = ShopAppContentProvider.tryString(item,"mode");
//			}catch(JSONException e){
//				e.printStackTrace();
//			}
//		}
		public void UpdateToDataBase(Context context){
			Uri uri = URI.buildUpon().appendPath(""+_id).build();
			ContentValues values = new ContentValues();
			//sku放入到uri中，就不用put到values中了
			values.put(PRODUCT_ID, product_id);
			values.put(PRICE, price);
			values.put(SALE_PRICE, sale_price);
			values.put(TITLE, title);
			values.put(COVER, cover);
			values.put(QUANTITY,quantity);
			values.put(NAME, name);
			values.put(MODE, mode);
			context.getContentResolver().update(uri, values, null, null);			
		}
		public void ReadFromCursor(Cursor cursor){
			_id = cursor.getInt(cursor.getColumnIndex(_ID));
			product_id = cursor.getInt(cursor.getColumnIndex(PRODUCT_ID));
			price = cursor.getDouble(cursor.getColumnIndex(PRICE));
			sale_price = cursor.getDouble(cursor.getColumnIndex(SALE_PRICE));
			title = cursor.getString(cursor.getColumnIndex(TITLE));
			cover = cursor.getString(cursor.getColumnIndex(COVER));
			quantity = cursor.getInt(cursor.getColumnIndex(QUANTITY));
			name = cursor.getString(cursor.getColumnIndex(NAME));
			mode = cursor.getString(cursor.getColumnIndex(MODE));
		}	
	}
	
	public static final String CONFIGS_TABLE_NAME = "Configs";
	public static final Uri CONFIGS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + CONFIGS_TABLE_NAME);
	public static class Configs implements BaseColumns{
		public static final String NAME = "name";
		public static final String VALUE = "value";
	}
	private static final int OLC_ALL = 0;
	private static final int OLC_DISTRICTS = 1;
	private static final int OLC_PRODUCTCATEGORYS = 2;
	private static final int OLC_PRODUCTITEMS = 3;
	private static final int OLC_DISTRICT_ID = 4;
	private static final int OLC_PRODUCTCATEGORY_ID = 5;
	private static final int OLC_PRODUCTITEM_ID = 6;
	private static final int OLC_CONFIGS = 7;
	private static final int OLC_CONFIGS_NAME = 8;
	private static final int OLC_SHOPPINGITEMS = 9;
	private static final int OLC_SHOPPINGITEM_NAME = 10;
	
	
	private static final UriMatcher sURLMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sURLMatcher.addURI(AUTHORITY, null, OLC_ALL);
		sURLMatcher.addURI(AUTHORITY, DISTRICT_TABLE_NAME, OLC_DISTRICTS);
		sURLMatcher.addURI(AUTHORITY, PRODUCTCATEGORY_TABLE_NAME, OLC_PRODUCTCATEGORYS);
		sURLMatcher.addURI(AUTHORITY, ProductItem.TABLE_NAME, OLC_PRODUCTITEMS);
		sURLMatcher.addURI(AUTHORITY, DISTRICT_TABLE_NAME + "/#", OLC_DISTRICT_ID);
		sURLMatcher.addURI(AUTHORITY, PRODUCTCATEGORY_TABLE_NAME + "/#", OLC_PRODUCTCATEGORY_ID);	
		sURLMatcher.addURI(AUTHORITY, ProductItem.TABLE_NAME + "/#", OLC_PRODUCTITEM_ID);
		sURLMatcher.addURI(AUTHORITY, CONFIGS_TABLE_NAME, OLC_CONFIGS);
		sURLMatcher.addURI(AUTHORITY, CONFIGS_TABLE_NAME + "/*", OLC_CONFIGS_NAME);
		sURLMatcher.addURI(AUTHORITY, CartItem.TABLE_NAME, OLC_SHOPPINGITEMS);
		sURLMatcher.addURI(AUTHORITY, CartItem.TABLE_NAME + "/*", OLC_SHOPPINGITEM_NAME);		
	}
	private static SQLiteDatabase sqlDB;
	private static DatabaseHelper dbHelper;
	private static final String DATABASE_NAME = AUTHORITY + ".db";
	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context){
			super(context,DATABASE_NAME,null,DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("DROP TABLE IF EXISTS " + DISTRICT_TABLE_NAME);			
	        db.execSQL("CREATE TABLE " + DISTRICT_TABLE_NAME + " (" +
	        		District._ID + " INTEGER PRIMARY KEY," +
	        		District.NAME + " TEXT," +
	        		District.PARENT + " INTEGER DEFAULT 0" +        		
	                   ");");
			db.execSQL("DROP TABLE IF EXISTS " + PRODUCTCATEGORY_TABLE_NAME);	        
	        db.execSQL("CREATE TABLE " + PRODUCTCATEGORY_TABLE_NAME + " (" +
	        		ProductCategory._ID + " INTEGER PRIMARY KEY," +
	        		ProductCategory.NAME + " TEXT," +
	        		ProductCategory.TITLE + " TEXT," +
	        		ProductCategory.APP_COVER_URL + " TEXT," +
	        		ProductCategory.TOTAL_COUNT + " INTEGER DEFAULT 0" +	        		
	                   ");");	
	        
	        db.execSQL("DROP TABLE IF EXISTS " + ProductItem.TABLE_NAME);
	        db.execSQL(ProductItem.getCreateTableSQL());	     	        
	        db.execSQL("DROP TABLE IF EXISTS " + CartItem.TABLE_NAME);
	        db.execSQL(CartItem.getCreateTableSQL());
	        db.execSQL("DROP TABLE IF EXISTS " + CONFIGS_TABLE_NAME);
	        db.execSQL("CREATE TABLE " + CONFIGS_TABLE_NAME + " (" +
	                Configs._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
	        		Configs.NAME + " TEXT UNIQUE," +
	                Configs.VALUE + " TEXT" +
	        			");");
	        
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {			
			onCreate(db);			
		}
		@Override
		public void onDowngrade(SQLiteDatabase db, int oldVersion,
				int newVersion) {
			onUpgrade(db,oldVersion,newVersion);
		}
	}
	
	public ShopAppContentProvider() {
		
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {		
        int match = sURLMatcher.match(uri);
        String table = null;
        String where_insert = null;
        int ret = 0;
        switch (match) {
		case OLC_DISTRICTS:
			table = DISTRICT_TABLE_NAME;
			break;
		case OLC_PRODUCTCATEGORYS:
			table = PRODUCTCATEGORY_TABLE_NAME;
			break;
		case OLC_PRODUCTITEMS:
			table = ProductItem.TABLE_NAME;
			break;			
		case OLC_DISTRICT_ID:
			table = DISTRICT_TABLE_NAME;
            where_insert = District._ID+"=" + uri.getLastPathSegment();
			break;
		case OLC_PRODUCTCATEGORY_ID:
			table = PRODUCTCATEGORY_TABLE_NAME;
            where_insert = ProductCategory._ID+"=" + uri.getLastPathSegment();			
			break;
		case OLC_PRODUCTITEM_ID:
			table = ProductItem.TABLE_NAME;
            where_insert = ProductItem._ID+"=" + uri.getLastPathSegment();			
			break;			
		case OLC_CONFIGS:
			table = CONFIGS_TABLE_NAME;
			break;
		case OLC_CONFIGS_NAME:    
			table = CONFIGS_TABLE_NAME;

            where_insert = "name=" + DatabaseUtils.sqlEscapeString(uri.getLastPathSegment());
            break;
		case OLC_SHOPPINGITEMS:
			table = CartItem.TABLE_NAME;
			break;
		case OLC_SHOPPINGITEM_NAME:    
			table = CartItem.TABLE_NAME;
            where_insert = CartItem._ID+"=" + DatabaseUtils.sqlEscapeString(uri.getLastPathSegment());
            break;            
        default:
        	break;
        }
        if (table != null){
        	sqlDB = dbHelper.getWritableDatabase();
        	if(where_insert == null)
        		ret = sqlDB.delete(table, selection, selectionArgs);
        	else
        		ret = sqlDB.delete(table, where_insert, null);
        }
        return ret;
	}

	@Override
	public String getType(Uri uri) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {//不支持插入方法，请直接使用update接口
		throw new UnsupportedOperationException("Not yet implemented");
//        int match = sURLMatcher.match(uri);
//        String table = null;
//        switch (match) {
//		case OLC_DISTRICTS:
//			table = DISTRICT_TABLE_NAME;
//			break;
//		case OLC_PRODUCTCATEGORYS:
//			table = PRODUCTCATEGORY_TABLE_NAME;
//			break;
//		case OLC_PRODUCTITEMS:
//			table = PRODUCTITEM_TABLE_NAME;
//			break;  
//		case OLC_CONFIGS:
//			table = CONFIGS_TABLE_NAME;
//			break;
//        }
//        if (table != null){
//    		sqlDB = dbHelper.getWritableDatabase();
//    		long rowId = sqlDB.insert(table, null, values);      
//    		if (rowId > 0) {
//    			Uri rowUri = ContentUris.appendId(uri.buildUpon(), rowId).build();
//    			getContext().getContentResolver().notifyChange(rowUri, null);
//    			return rowUri;
//    		}    		
//        }
//		return null;		
	}

	@Override
	public boolean onCreate() {
		dbHelper = new DatabaseHelper(getContext());
		return (dbHelper == null) ? false : true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		int match = sURLMatcher.match(uri);
		//Log.i("","query " + uri.toString() + " sURLMatcher.match：" + match);
		switch(match){
		case OLC_DISTRICTS:
			qb.setTables(DISTRICT_TABLE_NAME);
			break;
		case OLC_PRODUCTCATEGORYS:
			qb.setTables(PRODUCTCATEGORY_TABLE_NAME);
			break;
		case OLC_PRODUCTITEMS:
			qb.setTables(ProductItem.TABLE_NAME);
			break;
		case OLC_DISTRICT_ID:
			qb.setTables(DISTRICT_TABLE_NAME);
            qb.appendWhere(District._ID+"=" + uri.getLastPathSegment());
			break;
		case OLC_PRODUCTCATEGORY_ID:
			qb.setTables(PRODUCTCATEGORY_TABLE_NAME);
			qb.appendWhere(ProductCategory._ID + "=" + uri.getLastPathSegment());		
			break;	
		case OLC_PRODUCTITEM_ID:
			qb.setTables(ProductItem.TABLE_NAME);
			qb.appendWhere(ProductItem._ID + "=" + uri.getLastPathSegment());	
			break;			
		case OLC_CONFIGS:
			qb.setTables(CONFIGS_TABLE_NAME);
			break;
		case OLC_CONFIGS_NAME:
			qb.setTables(CONFIGS_TABLE_NAME);
            qb.appendWhere("name=" + DatabaseUtils.sqlEscapeString(uri.getLastPathSegment()));
            break;
		case OLC_SHOPPINGITEMS:
			qb.setTables(CartItem.TABLE_NAME);
			break;
		case OLC_SHOPPINGITEM_NAME:    
			qb.setTables(CartItem.TABLE_NAME);
			qb.appendWhere("name=" + DatabaseUtils.sqlEscapeString(uri.getLastPathSegment()));
            break;             
		default:
			return null;
		}
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {
        int match = sURLMatcher.match(uri);
        String table = null;
        String where_insert = null;
        switch (match) {
		case OLC_DISTRICTS:
			table = DISTRICT_TABLE_NAME;
			break;
		case OLC_PRODUCTCATEGORYS:
			table = PRODUCTCATEGORY_TABLE_NAME;
			break;
		case OLC_PRODUCTITEMS:
			table = ProductItem.TABLE_NAME;
			break;			
		case OLC_DISTRICT_ID:
			table = DISTRICT_TABLE_NAME;
            if (where != null || whereArgs != null) {
                throw new UnsupportedOperationException(
                        "Cannot update URL " + uri + " with a where clause");
            }
            where_insert = District._ID+"=" + uri.getLastPathSegment();
			break;
		case OLC_PRODUCTCATEGORY_ID:
			table = PRODUCTCATEGORY_TABLE_NAME;
            if (where != null || whereArgs != null) {
                throw new UnsupportedOperationException(
                        "Cannot update URL " + uri + " with a where clause");
            }
            where_insert = ProductCategory._ID+"=" + uri.getLastPathSegment();			
			break;
		case OLC_PRODUCTITEM_ID:
			table = ProductItem.TABLE_NAME;
            if (where != null || whereArgs != null) {
                throw new UnsupportedOperationException(
                        "Cannot update URL " + uri + " with a where clause");
            }
            where_insert = ProductItem._ID+"=" + uri.getLastPathSegment();			
			break;			
		case OLC_CONFIGS:
			table = CONFIGS_TABLE_NAME;
			break;
		case OLC_CONFIGS_NAME:    
			table = CONFIGS_TABLE_NAME;
            if (where != null || whereArgs != null) {
                throw new UnsupportedOperationException(
                        "Cannot update URL " + uri + " with a where clause");
            }
            where_insert = "name=" + DatabaseUtils.sqlEscapeString(uri.getLastPathSegment());
            break;
		case OLC_SHOPPINGITEMS:
			table = CartItem.TABLE_NAME;
			break;
		case OLC_SHOPPINGITEM_NAME:    
			table = CartItem.TABLE_NAME;
            if (where != null || whereArgs != null) {
                throw new UnsupportedOperationException(
                        "Cannot update URL " + uri + " with a where clause");
            }
            where_insert = CartItem._ID+"=" + DatabaseUtils.sqlEscapeString(uri.getLastPathSegment());
            break;            
        default:
        	break;
        }
        if (table != null){
        	if(where_insert != null){//保证插入一个
        		sqlDB = dbHelper.getWritableDatabase();
	            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
				qb.setTables(table);			
	            qb.appendWhere(where_insert);
	    		Cursor cursor = qb.query(sqlDB, null, null, null, null, null, null);
	    		if (cursor == null || !cursor.moveToNext()){//没有内容，就插入一个
	        		if(cursor != null)
	        			cursor.close();
	        		if(match == OLC_CONFIGS_NAME){
	        			values.put(ShopAppContentProvider.Configs.NAME,uri.getLastPathSegment());
	        		}else if(match == OLC_DISTRICT_ID){
	        			values.put(ShopAppContentProvider.District._ID,uri.getLastPathSegment());
	        		}else if(match == OLC_PRODUCTCATEGORY_ID){
	        			values.put(ShopAppContentProvider.ProductCategory._ID,uri.getLastPathSegment());
	        		}else if(match == OLC_PRODUCTITEM_ID){
	        			values.put(ShopAppContentProvider.ProductItem._ID,uri.getLastPathSegment());
	        		}else if(match == OLC_SHOPPINGITEM_NAME){
	        			values.put(ShopAppContentProvider.CartItem._ID,uri.getLastPathSegment());
	        		}
	        		long rowid = sqlDB.insert(table, null, values);
	        		return rowid>=0?1:0;
	    		}else{
	    			if(match == OLC_SHOPPINGITEM_NAME){
	    				int quantity = cursor.getInt(cursor.getColumnIndex(CartItem.QUANTITY)) + (Integer)values.get(CartItem.QUANTITY);
	    				values.put(CartItem.QUANTITY,quantity);
	    			}	    			
	        		if(cursor != null)
	        			cursor.close();
		    		int count = sqlDB.update(table, values, where_insert, null);
		    		if (count > 0) {
		    			getContext().getContentResolver().notifyChange(uri, null);
		    		}    		
		    		return count;	    			
	    		}
        	}else{//一般性update
	    		int count = sqlDB.update(table, values, where, whereArgs);
	    		if (count > 0) {
	    			getContext().getContentResolver().notifyChange(uri, null);
	    		}    		
	    		return count;
        	}
        }
		return 0;
	}
}
