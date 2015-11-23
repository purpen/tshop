package com.taihuoniao.shop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Application;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.BaseColumns;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.taihuoniao.shop.activity.LoginActivity;
import com.taihuoniao.shop.activity.StartActivity;
import com.taihuoniao.shop.utils.AsynImageLoader;
import com.taihuoniao.shop.utils.FileUtils;

public class ShopApp extends Application {
	public static final String TAG = "ShopApp";
	private static ShopApp sApp;
	private AsynImageLoader mAsynImageLoader;  
	private int mUserId = -1;
	private String mNickName;
	private String mUuid;
	private static final String USER_ID_KEY = "id";
	private static final String NICK_NAME_KEY = "nickname";
	//"profile":{"realname":"\u7530\u5e05","phone":"18701680973","address":"\u6211\u7684\u5fc3\u91cc","job":"aaaa"}
	public static class UserAddress{
		public String realname="";
		public String phone="";
		public String address="";
		public String job="";
		public String name;
		public int province;
		public int city;
		public String zip;
		public String _id = null;
		public int is_default;
	};

	private UserInfo mUserInfo = new UserInfo();
	private UserAddress mUserAddresses[];

	public ShopApp() {
		if (sApp == null){
			sApp = this;
		}
		mAsynImageLoader = new AsynImageLoader();		
	}
	public static ShopApp self(){
		return sApp;
	}	
	@Override
	public void onCreate() {
		super.onCreate();
		InitUserId();		
	}
	public void showImageAsyn(ImageView imageView, String url){
		mAsynImageLoader.showImageAsyn(imageView, url, R.drawable.loading);
	}	
	public void showImageAsyn(ImageView imageView, String url, int resId){
		mAsynImageLoader.showImageAsyn(imageView, url, resId);
	}
	public boolean isLogined(){
		return getUserId()<=0?false:true;
	}

	public void InitUserId(){
		SharedPreferences sp = getSharedPreferences(null,MODE_PRIVATE);
		mUserId = sp.getInt(USER_ID_KEY, -1);
		mNickName = sp.getString(NICK_NAME_KEY,"");
		TelephonyManager telephonyManager = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);		
		mUuid = Build.ID + Build.SERIAL + telephonyManager.getDeviceId() + Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);
	}
	public void setUserId(int id,String name){
		mUserId = id;
		mNickName = name;
		SharedPreferences sp = this.getSharedPreferences(null,MODE_PRIVATE);
		Editor edit = sp.edit();
		edit.putInt(USER_ID_KEY,mUserId);
		edit.putString(NICK_NAME_KEY, mNickName);
		edit.commit();
	}
	public int getUserId(){
		return mUserId;
	}
	public String getNickName(){
		return mNickName;
	}
	public UserInfo getUserInfo(){
		return mUserInfo;
	}
	public UserAddress[] getUserAddresses(){
		return mUserAddresses;
	}
	public UserAddress getUserAddress(String _id){
		if(mUserAddresses != null){
			for(int i=0; i<mUserAddresses.length; i++){
				if(mUserAddresses[i]._id.equals(_id))
					return mUserAddresses[i];
			}
		}
		return null;
	}
	
	private String httpPost(String url,HttpEntity post) throws UnsupportedEncodingException,ClientProtocolException,IOException {
		HttpClient httpClient = new DefaultHttpClient();  
		String strResult = "";
		//
		    HttpPost postMethod = new HttpPost(url);  
		    postMethod.setEntity(post); //将参数填入POST Entity中  
		                  
		    HttpResponse response = httpClient.execute(postMethod); //执行POST方法  
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){  
                //取得返回的字符串  
                strResult = EntityUtils.toString(response.getEntity(),"utf-8");
                parseIsLogined(strResult);
            }    
            InputStreamReader isr = new InputStreamReader(post.getContent());
            BufferedReader br = new BufferedReader(isr);
            Log.i(TAG, "httpPost Url:" + url);
            Log.i(TAG, "httpPost Post:" + br.readLine());
		    Log.i(TAG, "resCode = " + response.getStatusLine().getStatusCode()); //获取响应码  
		    Log.i(TAG, "result = " + strResult); //获取响应内容  		                
		//
		return strResult;		
	}
	//
	private static final String CLIENT_SECRET="545d9f8aac6b7a4d04abffe5";
	private static final String CLIENT_KEY="1415289600";
	private static class SortByName implements Comparator<BasicNameValuePair> {
		 public int compare(BasicNameValuePair a, BasicNameValuePair b) {
			 return a.getName().compareTo(b.getName());
		 }
	}	
	private BasicNameValuePair client_param = new BasicNameValuePair("client_id", CLIENT_KEY);
	private void addSign(List<BasicNameValuePair> params){	
		if(params.contains(client_param))
			return;//已经添加了签名了，不需要再次添加
		params.add(client_param);  
		params.add(new BasicNameValuePair("uuid", mUuid));
		params.add(new BasicNameValuePair("channel", "0"));
		params.add(new BasicNameValuePair("time", ""+System.currentTimeMillis()));

		Collections.sort(params,new SortByName());	
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<params.size(); i++){
			if(i > 0)				
				sb.append("&");
			BasicNameValuePair pair = params.get(i);
			sb.append(pair.getName() + "=" + pair.getValue());
		}
		sb.append(CLIENT_SECRET);
		sb.append(CLIENT_KEY);
		String md5 = MD5.GetMD5Code(sb.toString());
		String sign = MD5.GetMD5Code(md5);
		
		params.add(new BasicNameValuePair("sign", sign));
	}
	
	public ResultData doCommonAction(ShopHttpParams hp){
		ResultData result = new ResultData();
		addSign(hp.params);
		try{
			UrlEncodedFormEntity post = new UrlEncodedFormEntity(hp.params, "utf-8");		  			
			String strResult = httpPost(hp.url,post);
			if(!strResult.isEmpty())
				result.parseCommonResult(strResult);
		} catch (UnsupportedEncodingException e) {
			result.message = e.getMessage();
		    e.printStackTrace();  
		} catch (ClientProtocolException e) {
			result.message = e.getMessage();
		    e.printStackTrace();  
		} catch (IOException e) {
			result.message = e.getMessage();
		    e.printStackTrace();
		}		
		return result;		
	}	
	
	public static class ShopHttpParams{
		public String url;
		public List<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();
		public String tag;
		public void put(String name,String value){
			if(params == null)
				params = new LinkedList<BasicNameValuePair>();
			params.add(new BasicNameValuePair(name, ""+value));
		}
	};
	
	public static class ResultData{
		public boolean success;
		public String message;
		public JSONObject data;
		public String strResult;
		public Object object;
		ResultData(){
			success = false;
		}
		public void parseCommonResult(String strResult){
			success = false;
			this.strResult = strResult;
			JSONTokener jsonParser = new JSONTokener(strResult);
			//操作完成: {"success":true,"is_error":false,"status":"0","message":"\u6b22\u8fce\u56de\u6765.","data":{"is_login":true}}		
			try{
				JSONObject retobj = (JSONObject) jsonParser.nextValue();
				success = retobj.getBoolean("success");
				message = retobj.getString("message");
				data = retobj.getJSONObject("data");
			}catch(JSONException e){
				e.printStackTrace();
			}
		}		
	};	
	
	public static interface HttpCallBackInterface {
		void onUrlRequestReturn(ShopHttpParams hp,ResultData result);
	}
	
	protected class BaseHttpAsyncTask extends AsyncTask<Void, Void, ResultData>{
		private ShopHttpParams hp;
		private HttpCallBackInterface callback;
		public BaseHttpAsyncTask(ShopHttpParams hp,HttpCallBackInterface callback) {
			this.hp = hp;
			this.callback = callback;
		}
		@Override
		protected ResultData doInBackground(Void...argParams) {
			ResultData result = doCommonAction(hp);
			return result;
		}

		@Override
		protected void onPostExecute(ResultData result) {
			super.onPostExecute(result);
			if(callback != null)
				callback.onUrlRequestReturn(hp,result);
		}
	}
	
	public void sendUrlRequest(ShopHttpParams hp,HttpCallBackInterface callback) {
		BaseHttpAsyncTask task = new BaseHttpAsyncTask(hp,callback);
		task.execute((Void)null);
	}
	
	public static ShopHttpParams doLogin(String user,String pass){
		ShopHttpParams hp = new ShopHttpParams();
		hp.put("mobile", user);  
		hp.put("password", pass);
		hp.put("from_to", "2");//2 表示android
		hp.url = ShopUtils.getLoginUrl();
		return hp;
	}
	public static ShopHttpParams doLogout(){
		ShopHttpParams hp = new ShopHttpParams();
		hp.put("from_to", "2");//2 表示android
		hp.url = ShopUtils.getLogoutUrl();
		return hp;
	}
	
	public void parseIsLogined(String result){
		JSONTokener jsonParser = new JSONTokener(result);
		try{
			JSONObject retobj = (JSONObject) jsonParser.nextValue();
			boolean success = retobj.getBoolean("success");
			if(success){
				mUserId = retobj.getInt("current_user_id");
			}
		}catch(JSONException e){
			e.printStackTrace();
		}	
	}	
	
	public boolean parseLogin(String result){
		JSONTokener jsonParser = new JSONTokener(result);
		//操作完成: {"success":true,"is_error":false,"status":"0","message":"\u6b22\u8fce\u56de\u6765.","data":{"is_login":true,"id":20448,"account":"15001120509","nickname":"\u65e0\u53cc","last_login":null,"current_login":null,"visit":null,"is_admin":null}}		
		boolean success = false;
		try{
			JSONObject retobj = (JSONObject) jsonParser.nextValue();
			success = retobj.getBoolean("success");
			if(success){				
				JSONObject data = retobj.getJSONObject("data");
				mUserId = data.getInt("id");
				mNickName = data.getString("nickname");	
				setUserId(mUserId,mNickName);
			}
		}catch(JSONException e){
			e.printStackTrace();
		}
		return success;
	}
	
	public ShopHttpParams doGetUserInfo(){
		ShopHttpParams hp = new ShopHttpParams();
		//params.add(new BasicNameValuePair("id", ""+mUserId));
		hp.url = ShopUtils.getUserInfoUrl();
		return hp;
	}	
	
	public static class UserInfo{
		public int _id;
		public String account="";
		public String nickname="";
		public int state;
		public int first_login;
		public String city="";
		public int sex;//1 为男，2为女
		public String summar="";
		public long create_on;
		public String email="";
		public String phone="";
		public String job="";
		public String avatar="";
		public UserAddress profile;
		boolean readJasonData(JSONObject data){
			boolean success = false;
			try{
				_id = data.getInt("_id");
				account = data.getString("account");
				nickname = data.getString("nickname");
				state = data.getInt("state");
				first_login = data.getInt("first_login");
				////
				JSONObject profile = data.getJSONObject("profile");
				if(profile != null){
					this.profile = new UserAddress();
					this.profile.realname = profile.getString("realname");
					this.profile.phone = profile.getString("phone");
					this.profile.address = profile.getString("address");
					this.profile.job = profile.getString("job");
				}
				////
				city = data.getString("city");
				sex = data.getInt("sex");
				summar = data.getString("summary");
				create_on = data.getLong("created_on");					
				email = data.getString("email");
				phone = data.getString("phone");				
				job = data.getString("job");
				avatar = data.getString("avatar");	
				success = true;
			}catch(JSONException e){
				e.printStackTrace();
			}		
			return success;
		}
	};
	public boolean parseUserInfo(ResultData result){
		//操作完成: {"success":true,"is_error":false,"status":"0","message":"\u8bf7\u6c42\u6210\u529f",
		//"data":{"_id":20448,"account":"15001120509","nickname":"\u65e0\u53cc","state":2,"first_login":0,
		//"profile":{"realname":"\u7530\u5e05","phone":"18701680973","address":"\u6211\u7684\u5fc3\u91cc","job":"aaaa"},
		//"city":"\u5317\u4eac","sex":1,"summary":"\u4f60\u4eec\u5462","created_on":1416283525,"email":"tianshuaiorc@sina.com","phone":"18701680973","job":"aaaa",
		//"avatar":"http:\/\/frbird.qiniudn.com\/avatar\/150326\/5514037d3ffca25d6f8b4b28-avm.jpg"}		
		boolean success = mUserInfo.readJasonData(result.data);
		if (!mUserInfo.nickname.equalsIgnoreCase(mNickName))
			setUserId(mUserId, mUserInfo.nickname);
			result.object = mUserInfo;
			success = true;
		return success;
	}
	
	public static ShopHttpParams doSendVerifyCode(String user){
		ShopHttpParams hp = new ShopHttpParams();
		hp.put("mobile", user);
		hp.url = ShopUtils.getSendVerifyCodeUrl();
		return hp;
	}
	public static ShopHttpParams doRegister(String user,String pass,String code){
		ShopHttpParams hp = new ShopHttpParams();
		hp.put("mobile", user);
		hp.put("password", pass);
		hp.put("verify_code", code);
		hp.url = ShopUtils.getRegisterUrl();
		return hp;
	}	
	public static ShopHttpParams doResetPass(String mobile,String content,String verify_code){
		ShopHttpParams hp = new ShopHttpParams();
		hp.put("mobile", mobile);
		hp.put("content", content);
		hp.put("verify_code", verify_code);
		hp.url = ShopUtils.getResetPassUrl();
		return hp;
	}	
	public ShopHttpParams doUpdateUserInfo(String nickname,String sex,String city,String job,String phone){
		ShopHttpParams hp = new ShopHttpParams();
		hp.put("current_user_id", ""+getUserId());
		hp.put("nickname", nickname);
		hp.put("sex", sex);
		hp.put("city", city);
		hp.put("job", job);
		hp.put("phone", phone);		
		hp.url = ShopUtils.getUpdateUserInfoUrl();
		return hp;
	}
	
	public ShopHttpParams doUpdateUserAvatar(File file){
		byte []avatar = null;
		try{
			avatar = FileUtils.toBytes(new FileInputStream(file));					
		}catch(IOException e){
			e.printStackTrace();
			return null;
		}
		ShopHttpParams hp = new ShopHttpParams();
		hp.put("current_user_id", ""+getUserId());
		hp.put("type", "3");//1商品，2话题，3头像
		addSign(hp.params);//比较特殊的
		hp.put("tmp", Base64.encodeToString(avatar,Base64.DEFAULT));	
		hp.url = ShopUtils.getUploadUrl();
		return hp;//
	}
	
	public ShopHttpParams doUserAddress(){
		ShopHttpParams hp = new ShopHttpParams();
		hp.put("current_user_id", ""+getUserId());
		hp.url = ShopUtils.getUserAddressUrl();
		return hp;
	}
	public ShopHttpParams doGetUserAddress(){
		ShopHttpParams hp = new ShopHttpParams();
		hp.put("current_user_id", ""+getUserId());
		hp.url = ShopUtils.getUserAddressUrl();
		return hp;	
	}	
	
	public boolean parseUserAddress(ResultData result){
		//{"success":true,"is_error":false,"status":"0","message":"\u8bf7\u6c42\u6210\u529f",
		//"data":{"total_rows":1,
		//"rows":[
		//{"_id":"561d005a3ffca222438b4bf9",
		//"user_id":477411,
		//"name":"\u521a\u521a\u597d",
		//"phone":"13422886692",
		//"province":0,
		//"city":0,
		//"area":null,
		//"address":"\u5e7f\u544a\u6b4c\u65b9\u6cd5",
		//"zip":0,
		//"is_default":1,
		//"province_name":null,
		//"city_name":null}],"total_page":1,"current_page":1,"pager":"","next_page":0,"prev_page":0}}
		boolean success = false;
		try{		
			JSONObject data = result.data;
			int addr_count= data.getInt("total_rows");
			UserAddress [] uas = new UserAddress[addr_count];
			JSONArray rows = data.getJSONArray("rows");
			for(int i=0; i < rows.length() && i < uas.length; i++){
				JSONObject row = rows.getJSONObject(i);
				if(uas[i] == null){
					uas[i] = new UserAddress();
				}
				uas[i].name = row.getString("name");
				uas[i].phone = row.getString("phone");
				uas[i].province = row.getInt("province");
				uas[i].city = row.getInt("city");
				uas[i].address = row.getString("address");
				uas[i].zip = row.getString("zip");
				uas[i]._id = row.getString("_id");
				uas[i].is_default = row.getInt("is_default");
			}
			result.object = uas;
			mUserAddresses = uas;
			success = true;
		}catch(JSONException e){
			e.printStackTrace();
		}
		return success;
	}
	
	public ShopHttpParams editAddress(String name,String phone,String province,String city,String address,String zip,String _id,int is_default){
		ShopHttpParams hp = new ShopHttpParams();
		hp.put("current_user_id", ""+getUserId());
		hp.put("name", name);
		hp.put("phone", phone);
		hp.put("province", province);
		hp.put("city", city);
		hp.put("address", address);
		hp.put("zip", zip);
		hp.put("is_default",""+is_default);
		if(_id != null && !_id.isEmpty()){
			hp.put("_id", ""+_id);
		}
		hp.url = ShopUtils.getEditUserAddressUrl();
		return hp;
	}
	public ShopHttpParams delAddress(String _id){
		ShopHttpParams hp = new ShopHttpParams();
		hp.put("id","" + _id);
		hp.url = ShopUtils.getDelUserAddressUrl();
		return hp;
	}
	public ShopHttpParams setDefaultAddress(String target_id){
		ShopHttpParams hp = new ShopHttpParams();
		hp.put("id", target_id);
		hp.url = ShopUtils.getSetDefaultAddressUrl();
		return hp;
	}	
	

	private void UpdateDistrict(String name,int _id,int parent){
		//Uri uri = ShopAppContentProvider.CONFIGS_CONTENT_URI.buildUpon().appendPath(newSegment).build();
		Uri uri = ContentUris.appendId(ShopAppContentProvider.DISTRICT_CONTENT_URI.buildUpon(),_id).build();
		ContentValues values = new ContentValues();
		values.put(ShopAppContentProvider.District.NAME,name);
		//values.put(ShopAppContentProvider.District._ID,_id);
		values.put(ShopAppContentProvider.District.PARENT,parent);
		this.getContentResolver().update(uri, values, null, null);		
	}
	
	public String getDistrict(int districtId){
		Uri uri = ContentUris.appendId(ShopAppContentProvider.DISTRICT_CONTENT_URI.buildUpon(),districtId).build();		
		Cursor cursor = this.getContentResolver().query(uri,null,null,null, null);
		String ret = null;
		if(cursor != null && cursor.moveToNext()){
			ret = cursor.getString(cursor.getColumnIndex(ShopAppContentProvider.District.NAME));
			cursor.close();
			cursor = null;
		}
		Log.i(TAG,"getDistrict id:" + districtId + " ret:" + ret);
		return ret;
	}
	
	public boolean parseDistrict(ResultData result){
		//result = {"success":true,"is_error":false,"status":"0","message":"\u8bf7\u6c42\u6210\u529f",
		//"data":{"total_rows":34,"rows":[{"_id":1,"city":"\u5317\u4eac\u5e02","parent_id":0,"child":0,"layer":1,"sort":1,"status":1,"__extend__":true},{"_id":2,"city":"\u5929\u6d25\u5e02","parent_id":0,"child":0,"layer":1,"sort":2,"status":1,"__extend__":true},{"_id":3,"city":"\u4e0a\u6d77\u5e02","parent_id":0,"child":0,"layer":1,"sort":3,"status":1,"__extend__":true},{"_id":4,"city":"\u91cd\u5e86\u5e02","parent_id":0,"child":0,"layer":1,"sort":4,"status":1,"__extend__":true},{"_id":5,"city":"\u6cb3\u5317\u7701","parent_id":0,"child":0,"layer":1,"sort":5,"status":1,"__extend__":true},{"_id":6,"city":"\u5c71\u897f\u7701","parent_id":0,"child":0,"layer":1,"sort":6,"status":1,"__extend__":true},{"_id":7,"city":"\u53f0\u6e7e\u7701","parent_id":0,"child":0,"layer":1,"sort":7,"status":1,"__extend__":true},{"_id":8,"city":"\u8fbd\u5b81\u7701","parent_id":0,"child":0,"layer":1,"sort":8,"status":1,"__extend__":true},{"_id":9,"city":"\u5409\u6797\u7701","parent_id":0,"child":0,"layer":1,"sort":9,"status":1,"__extend__":true},{"_id":10,"city":"\u9ed1\u9f99\u6c5f\u7701","parent_id":0,"child":0,"layer":1,"sort":10,"status":1,"__extend__":true},{"_id":11,"city":"\u6c5f\u82cf\u7701","parent_id":0,"child":0,"layer":1,"sort":11,"status":1,"__extend__":true},{"_id":12,"city":"\u6d59\u6c5f\u7701","parent_id":0,"child":0,"layer":1,"sort":12,"status":1,"__extend__":true},{"_id":13,"city":"\u5b89\u5fbd\u7701","parent_id":0,"child":0,"layer":1,"sort":13,"status":1,"__extend__":true},{"_id":14,"city":"\u798f\u5efa\u7701","parent_id":0,"child":0,"layer":1,"sort":14,"status":1,"__extend__":true},{"_id":15,"city":"\u6c5f\u897f\u7701","parent_id":0,"child":0,"layer":1,"sort":15,"status":1,"__extend__":true},{"_id":16,"city":"\u5c71\u4e1c\u7701","parent_id":0,"child":0,"layer":1,"sort":16,"status":1,"__extend__":true},{"_id":17,"city":"\u6cb3\u5357\u7701","parent_id":0,"child":0,"layer":1,"sort":17,"status":1,"__extend__":true},{"_id":18,"city":"\u6e56\u5317\u7701","parent_id":0,"child":0,"layer":1,"sort":18,"status":1,"__extend__":true},{"_id":19,"city":"\u6e56\u5357\u7701","parent_id":0,"child":0,"layer":1,"sort":19,"status":1,"__extend__":true},{"_id":20,"city":"\u5e7f\u4e1c\u7701","parent_id":0,"child":0,"layer":1,"sort":20,"status":1,"__extend__":true},{"_id":21,"city":"\u7518\u8083\u7701","parent_id":0,"child":0,"layer":1,"sort":21,"status":1,"__extend__":true},{"_id":22,"city":"\u56db\u5ddd\u7701","parent_id":0,"child":0,"layer":1,"sort":22,"status":1,"__extend__":true},{"_id":23,"city":"\u8d35\u5dde\u7701","parent_id":0,"child":0,"layer":1,"sort":23,"status":1,"__extend__":true},{"_id":24,"city":"\u6d77\u5357\u7701","parent_id":0,"child":0,"layer":1,"sort":24,"status":1,"__extend__":true},{"_id":25,"city":"\u4e91\u5357\u7701","parent_id":0,"child":0,"layer":1,"sort":25,"status":1,"__extend__":true},{"_id":26,"city":"\u9752\u6d77\u7701","parent_id":0,"child":0,"layer":1,"sort":26,"status":1,"__extend__":true},{"_id":27,"city":"\u9655\u897f\u7701","parent_id":0,"child":0,"layer":1,"sort":27,"status":1,"__extend__":true},{"_id":28,"city":"\u5e7f\u897f\u58ee\u65cf\u81ea\u6cbb\u533a","parent_id":0,"child":0,"layer":1,"sort":28,"status":1,"__extend__":true},{"_id":29,"city":"\u897f\u85cf\u81ea\u6cbb\u533a","parent_id":0,"child":0,"layer":1,"sort":29,"status":1,"__extend__":true},{"_id":30,"city":"\u5b81\u590f\u56de\u65cf\u81ea\u6cbb\u533a","parent_id":0,"child":0,"layer":1,"sort":30,"status":1,"__extend__":true},{"_id":31,"city":"\u65b0\u7586\u7ef4\u543e\u5c14\u81ea\u6cbb\u533a","parent_id":0,"child":0,"layer":1,"sort":31,"status":1,"__extend__":true},{"_id":32,"city":"\u5185\u8499\u53e4\u81ea\u6cbb\u533a","parent_id":0,"child":0,"layer":1,"sort":32,"status":1,"__extend__":true},{"_id":33,"city":"\u6fb3\u95e8\u7279\u522b\u884c\u653f\u533a","parent_id":0,"child":0,"layer":1,"sort":33,"status":1,"__extend__":true},{"_id":34,"city":"\u9999\u6e2f\u7279\u522b\u884c\u653f\u533a","parent_id":0,"child":0,"layer":1,"sort":34,"status":1,"__extend
		Log.e(TAG,"parseDistrict begin");
		boolean success = false;
		try{
			JSONObject data = result.data;
			int total_rows = data.getInt("total_rows");
			JSONArray rows = data.getJSONArray("rows");
			for(int i=0; i<rows.length(); i++){
				JSONObject row = rows.getJSONObject(i);
				int _id = row.getInt("_id");
				String city = row.getString("city");
				int parent_id = row.getInt("parent_id");
				UpdateDistrict(city,_id,parent_id);
			}
			success = true;
		}catch(JSONException e){
			e.printStackTrace();
		}	
		Log.e(TAG,"parseDistrict end");
		return success;
	}
	
	public static ShopHttpParams doGetProvinces(){
		ShopHttpParams hp = new ShopHttpParams();
		hp.url = ShopUtils.getProvincesUrl();
		return hp;		
	}
	
	public static ShopHttpParams doGetDistricts(int province){
		ShopHttpParams hp = new ShopHttpParams();
		if(province >= 0){
			hp.put("id", ""+province);
		}		
		hp.url = ShopUtils.getDistrictsUrl();
		return hp;	
	}
	
	public static final class PageData implements Cloneable{
		public int total_page = -1;
		public int total_rows;
		public int current_page = 0;
		public int current_rows = 0;
		public int page = 1;
		public long lastTime = 0;
		public static final int size = 10;
		public static final int refresh_time = 5000;//最多5秒内刷新同一个页面
		public Object object = null;
		public void addPageParams(List<BasicNameValuePair> params){
			params.add(new BasicNameValuePair("page", ""+page));
			params.add(new BasicNameValuePair("size", ""+size));
		}
		//设置要获取的页面，合法的pageIndex必须  >=1
		public void setPage(int pageIndex){
			long currentTime = System.currentTimeMillis();
			if(current_page == 0){//没有获取过数据,或者pageIndex，那么必须从第一页开始获取
				page = 1;
			}else if(pageIndex <=0){//不合法的pageIndex，设置下一页
				if(page < total_page){
					page++;
				}				
			}else if(pageIndex <= total_page){//合法的页面
				page = pageIndex;			
			}else{//不能获取超过最后一页
				page = total_page;
			}
		}
		
		@Override
		public PageData clone(){
			PageData data;
			try{
				data = (PageData)super.clone();
			}catch(CloneNotSupportedException e){
				e.printStackTrace();
				data = new PageData();
				data.total_page = total_page;
				data.total_rows = total_rows;
				data.current_page = current_page;
				data.current_rows = current_rows;
				data.page = page;
				data.lastTime = lastTime;				
			}
			return data;
		}
	}
	
	private PageData mPageDataProductCategory = new PageData();
	public boolean parseProductCategory(ResultData result){
		boolean success=false;
		try{
			JSONObject data = result.data;
			mPageDataProductCategory.total_rows = data.getInt("total_rows");
			JSONArray rows = data.getJSONArray("rows");
			ShopAppContentProvider.ProductCategory[] categorys = new ShopAppContentProvider.ProductCategory[rows.length()];
			//这里注意，如果更新了内容
			for(int i=0; i<rows.length(); i++){
				JSONObject row = rows.getJSONObject(i);
				ShopAppContentProvider.ProductCategory d = new ShopAppContentProvider.ProductCategory();							
				d.ReadJasonData(row);
				d.UpdateToDataBase(this);
				categorys[i] = d;
				Log.i("","row:"+i + " id" + d._id + " title:" + d.title + " app_cover_url:"+ d.app_cover_url);
			}
			mPageDataProductCategory.total_page = data.getInt("total_page");
			mPageDataProductCategory.current_page = data.getInt("current_page");
			PageData page = mPageDataProductCategory.clone();
			page.object = categorys;
			result.object = page;
			success = true;
		}catch(JSONException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
		return success;
	}
	public ShopHttpParams doGetProductCategory(int pageIndex){
		ShopHttpParams hp = new ShopHttpParams();
		mPageDataProductCategory.setPage(pageIndex);
		mPageDataProductCategory.addPageParams(hp.params);//添加分页参数
		hp.url = ShopUtils.getProductCategoryUrl();
		return hp;		
	}
	HashMap<String, PageData>mapPageDataProductItem = new HashMap<String, PageData>();
	public boolean parseProductList(ResultData result,String tag){
//		{"success":true,"is_error":false,"status":"0","message":"\u8bf7\u6c42\u6210\u529f","data":{"total_rows":62,"rows":[{
		PageData pageData = mapPageDataProductItem.get(tag);
		boolean success = false;
		try{
			JSONObject data = result.data;
			pageData.total_rows = data.getInt("total_rows");
			JSONArray rows = data.getJSONArray("rows");
			ShopAppContentProvider.ProductItem items[] = new ShopAppContentProvider.ProductItem[rows.length()];
			//这里注意，如果更新了内容
			for(int i=0; i<rows.length(); i++){
				JSONObject row = rows.getJSONObject(i);
				ShopAppContentProvider.ProductItem d = new ShopAppContentProvider.ProductItem();				
				d.ReadJasonData(row);
				d.UpdateToDataBase(this);
				items[i] = d;
				Log.i("","row:"+i + " id" + d._id + " title:" + d.title + " cover_url:"+ d.cover_url);
			}
			pageData.total_page = data.getInt("total_page");
			pageData.current_page = data.getInt("current_page");	
			PageData page = pageData.clone();
			page.object = items;
			result.object = page;
			success = true;
		}catch(JSONException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
		return success;
	}	
	
	public ShopHttpParams doGetProductList(int pageIndex,int category_id,int stick){
		ShopHttpParams hp = new ShopHttpParams();
		if(category_id >= 0)
			hp.put("category_id", ""+category_id);
		if(stick >= 0)
			hp.put("stick", ""+stick);
		String key = "category_id="+category_id+"&stick="+stick;
		hp.tag = key;
		PageData pageData = mapPageDataProductItem.get(key);
		if(pageData == null){
			pageData = new PageData();
			mapPageDataProductItem.put(key, pageData);
		}
		mPageDataProductCategory.setPage(pageIndex);	
		mPageDataProductCategory.addPageParams(hp.params);//添加分页参数
		hp.url = ShopUtils.getProductListUrl();
		return hp;		
	}
	
	public static void showToast(Context context,String text){
		Toast.makeText(context, text, 5000).show();
	}
	
	public boolean parseProductView(ResultData result){
//		{"success":true,"is_error":false,"status":"0","message":"\u8bf7\u6c42\u6210\u529f","data":{"total_rows":62,"rows":[{		
		boolean success = false;
		try{
			JSONObject data = result.data;
			ShopAppContentProvider.ProductItem d = new ShopAppContentProvider.ProductItem();	
			result.object = d;				
			d.ReadJasonDataFPV(data);
			d.UpdateToDataBaseFPV(this);
			result.object = d;
			success = true;
		}catch(Exception e){
			e.printStackTrace();
		}
		return success;
	}	
	public ShopHttpParams doProductView(int id){
		ShopHttpParams hp = new ShopHttpParams();
		hp.put("id", ""+id);
		hp.url = ShopUtils.getProductViewUrl();
		return hp;
	}
	public ShopHttpParams doProductCommonAction(int id,String url){
		ShopHttpParams hp = new ShopHttpParams();
		hp.put("id", ""+id);
		hp.url = url;
		return hp;		
	}
	public ShopHttpParams doShoppingCartAction(int sku){
		ShopHttpParams hp = new ShopHttpParams();
		hp.put("sku", ""+sku);
		hp.url = ShopUtils.getShoppingCartUrl();
		return hp;
	}
	
	
	//{"success":true,"is_error":false,"status":"0","message":"\u8bf7\u6c42\u6210\u529f!",
	//"data":{"is_nowbuy":1,"pay_money":799,
//			"order_info":{"rid":"115102902832","user_id":477411,
//				"dict":{"payment_method":"a","transfer":"a","transfer_time":"a","summary":"","invoice_type":0,"freight":0,"card_money":0,"coin_money":0,"invoice_caty":"p","invoice_content":"d",
//					"items":[{"sku":"1080959165","product_id":"1080959165","quantity":1,"price":799,"sale_price":799,"title":"LISA\u667a\u80fd\u5b55\u8868\u2014\u60a8\u7684\u5b55\u671f\u8d34\u8eab\u95fa\u871c ","cover":"http:\/\/frbird.qiniudn.com\/product\/140809\/53e5d22d989a6a5d598b621f-1-s.jpg","view_url":"http:\/\/dev.taihuoniao.com\/shop\/view-1080959165-1.html","subtotal":799}],
//					"total_money":799,"items_count":1,"addbook_id":"561d005a3ffca222438b4bf9"},
//				"expired":1446144468,"created_on":1446108468,"updated_on":1446108468,"_id":2832},
//			"current_user_id":477411},
	//"current_user_id":477411}
	/*{"success":true,"is_error":false,"status":"0","message":"\u8bf7\u6c42\u6210\u529f!",
	 * "data":{"order_info":{"rid":"115111602903","user_id":477411,
	 * 			"dict":{"payment_method":"a","transfer":"a","transfer_time":"a","summary":"","invoice_type":0,"freight":0,"card_money":0,"coin_money":0,"invoice_caty":1,"invoice_content":"d",
	 * 					"items":[
	 * 							{"sku":1020885052,"product_id":1020885051,"quantity":1,"price":0.01,"sale_price":0.01,"title":"\u6d4b\u8bd5sku (red)","cover":"http:\/\/frbird.qiniudn.com\/product\/150204\/54d2271d3ffca264238b46c8-1-s.jpg","view_url":"http:\/\/dev.taihuoniao.com\/shop\/1020885051.html","subtotal":0.01},
	 * 							{"sku":1020885053,"product_id":1020885051,"quantity":1,"price":1.1,"sale_price":1.1,"title":"\u6d4b\u8bd5sku (green)","cover":"http:\/\/frbird.qiniudn.com\/product\/150204\/54d2271d3ffca264238b46c8-1-s.jpg","view_url":"http:\/\/dev.taihuoniao.com\/shop\/1020885051.html","subtotal":1.1}
	 * 							],
	 * 					"total_money":1.11,"items_count":2
	 * },"expired":1447705175,"created_on":1447669175,"updated_on":1447669175,"_id":2903},"is_nowbuy":0,"pay_money":1.11,"current_user_id":477411},"current_user_id":477411}	
	 */
	public static class ShoppingOrder implements Serializable{
		public double pay_money;
		public String rid;
		public int rrid;
		public int is_nowbuy;
		public int is_presaled = 0;
		public String summary;
		public String payment_method;
		public String transfer_time;	
		public ShoppingItem[] shopping_items;
		public double total_money;
		public String addbook_id;	
		public String realname;
		public String address;
		public int status;
		public long expired_time;
		public void readJsonData(JSONObject data){
			try{
				is_nowbuy = ShopAppContentProvider.tryInt(data,"is_nowbuy");
				pay_money = ShopAppContentProvider.tryDouble(data,"pay_money");
				JSONObject order_info = data.getJSONObject("order_info");
				rid = ShopAppContentProvider.tryString(order_info,"rid");
				rrid = ShopAppContentProvider.tryInt(order_info,"_id");
				JSONObject dict = order_info.getJSONObject("dict");
				summary = ShopAppContentProvider.tryString(dict,"summary");
				payment_method = ShopAppContentProvider.tryString(dict,"payment_method");
				transfer_time = ShopAppContentProvider.tryString(dict,"transfer_time");
				JSONArray items = dict.getJSONArray("items");
				shopping_items = new ShoppingItem[items.length()];
				for(int i=0; i<items.length(); i++){
					shopping_items[i] = new ShoppingItem();
					shopping_items[i].readJsonData(items.getJSONObject(i));
				}
				total_money = ShopAppContentProvider.tryInt(dict,"total_money");
				addbook_id = ShopAppContentProvider.tryString(dict,"addbook_id");
			}catch(JSONException e){
				e.printStackTrace();
			}	
		}
	}
	//SKU来自ProductView          			{"_id":1020885052,"product_id":1020885051,"name":"","mode":"red","price":0.01,"quantity":15,"sold":25,"limited_count":0,"sync_count":0,"summary":"","bad_count":0,"bad_tag":"","revoke_count":0,"shelf":0,"stage":9,"status":0,"created_on":1423058796,"updated_on":1446451910}
	//ShoppingItem来自ShoppingOrder 			{"sku":"1080959165","product_id":"1080959165","quantity":1,"price":799,"sale_price":799,"title":"LISA\u667a\u80fd\u5b55\u8868\u2014\u60a8\u7684\u5b55\u671f\u8d34\u8eab\u95fa\u871c ","cover":"http:\/\/frbird.qiniudn.com\/product\/140809\/53e5d22d989a6a5d598b621f-1-s.jpg","view_url":"http:\/\/dev.taihuoniao.com\/shop\/view-1080959165-1.html","subtotal":799}
	//ShoppingItem来自ShoppingOrderList	     "sku":1020885051,"product_id":1020885051,"quantity":1,"price":88,"sale_price":88,"name":"\u6d4b\u8bd5sku","cover_url":"http:\/\/frbird.qiniudn.com\/product\/150204\/54d2271d3ffca264238b46c8-1-s.jpg"
	public static class ShoppingItem implements BaseColumns,Serializable{
		
		public String sku;
		public String product_id;
		public double price;
		public double sale_price;
		public String title;
		public String cover;
		public String view_url;
		public int quantity;
		public double subtotal;
		public String cover_url;
		public String name;
		public String mode;
		public int count;
				
		public void readJsonData(JSONObject item){
			try{
				sku = item.getString("sku");
				product_id = ShopAppContentProvider.tryString(item,"product_id");
				price = ShopAppContentProvider.tryDouble(item,"price");
				sale_price = ShopAppContentProvider.tryDouble(item,"sale_price");
				title = ShopAppContentProvider.tryString(item,"title");
				cover = ShopAppContentProvider.tryString(item,"cover");
				view_url = ShopAppContentProvider.tryString(item,"view_url");
				quantity = ShopAppContentProvider.tryInt(item,"quantity");
				subtotal = ShopAppContentProvider.tryDouble(item,"subtotal");
				mode = ShopAppContentProvider.tryString(item,"mode");
			}catch(JSONException e){
				e.printStackTrace();
			}
		}
	
	}	
	
	public ShopHttpParams doShoppingNowBuyAction(int sku,int n){
		ShopHttpParams hp = new ShopHttpParams();
		hp.put("sku", ""+sku);
		hp.put("n", ""+n);
//{"success":true,"is_error":false,"status":"0","message":"\u8bf7\u6c42\u6210\u529f!",
//"data":{"is_nowbuy":1,"pay_money":799,
//		"order_info":{"rid":"115102902832","user_id":477411,
//			"dict":{"payment_method":"a","transfer":"a","transfer_time":"a","summary":"","invoice_type":0,"freight":0,"card_money":0,"coin_money":0,"invoice_caty":"p","invoice_content":"d",
//				"items":[{"sku":"1080959165","product_id":"1080959165","quantity":1,"price":799,"sale_price":799,"title":"LISA\u667a\u80fd\u5b55\u8868\u2014\u60a8\u7684\u5b55\u671f\u8d34\u8eab\u95fa\u871c ","cover":"http:\/\/frbird.qiniudn.com\/product\/140809\/53e5d22d989a6a5d598b621f-1-s.jpg","view_url":"http:\/\/dev.taihuoniao.com\/shop\/view-1080959165-1.html","subtotal":799}],
//				"total_money":799,"items_count":1,"addbook_id":"561d005a3ffca222438b4bf9"},
//			"expired":1446144468,"created_on":1446108468,"updated_on":1446108468,"_id":2832},
//		"current_user_id":477411},
//"current_user_id":477411}		
		hp.url = ShopUtils.getShoppingNowBuyUrl();
		return hp;		
	}

	public ShopHttpParams doShoppingConfirm(int rrid,String addbook_id,int is_nowbuy,int is_presaled,String summary,String payment_method,String transfer_time){
		ShopHttpParams hp = new ShopHttpParams();
		hp.put("rrid", ""+rrid);
		hp.put("addbook_id", addbook_id);
		hp.put("is_nowbuy", ""+is_nowbuy);
		hp.put("is_presaled", ""+is_presaled);
		hp.put("summary", summary);
		hp.put("payment_method", payment_method);
		hp.put("transfer_time", transfer_time);
		hp.url = ShopUtils.getShoppingConfirmUrl();
		return hp;
	}
	public ShopHttpParams doShoppingPayed(String rid,String payaway){
		ShopHttpParams hp = new ShopHttpParams();
		hp.put("rid", rid);
		hp.put("payaway", payaway);
		hp.url = ShopUtils.getShoppingPayedUrl();
		return hp;
	}
	public ShopHttpParams getProductCommentList(int target_id,PageData page){
		ShopHttpParams hp = new ShopHttpParams();
		hp.put("target_id", ""+target_id);
		hp.url = ShopUtils.getProductCommentsUrl();
		page.addPageParams(hp.params);		
		return hp;
	}
	public static class ProductComment{
		public String strId;
		public int star;
		public String content;
		public String created_on;
		public UserInfo userInfo;		
	}
	
	public boolean parseProductCommentList(ResultData result){
		boolean success = false;
		//"data":{"total_rows":1,
		//"rows":[{"_id":{"$id":"5641b5e83ffca223438b6430"},"user_id":477411,"target_id":"1080959165","target_user_id":0,"sku_id":0,"star":5,"content":"\u731c\u731c\u731c\u731c","reply":[],"type":4,"sub_type":1,"love_count":0,"invented_love_count":0,"is_reply":0,"reply_id":null,"reply_user_id":0,"floor":13,"deleted":0,"created_on":"5\u5206\u949f  \u524d","updated_on":1447146984,
		//"user":{"_id":477411,"account":"13422886692","nickname":"\u4e5f\u8bb8","state":2,"from_site":5,
		//	"profile":{"job":"null","phone":"13422886692","address":null,"realname":null},
		//"email":null,"role_id":1,"permission":[],"mentor":0,"sina_uid":null,"qq_uid":null,"wx_open_id":null,"wx_union_id":null,"follow_count":0,"fans_count":0,"love_count":0,"topic_count":0,"product_count":0,"favorite_count":0,"first_login":1,
		//	"avatar":{"big":"avatar\/151110\/5641a7b23ffca2985b8b4908","medium":"avatar\/151110\/5641a7b23ffca2985b8b4908","small":"avatar\/151110\/5641a7b23ffca2985b8b4908","mini":"avatar\/151110\/5641a7b23ffca2985b8b4908"},
		//"city":"\u793e\u533a","sex":1,"tags":[],"summary":null,"counter":{"message_count":0,"notice_count":0,"alert_count":0,"fans_count":0,"comment_count":0,"people_count":0},"identify":{"d3in_volunteer":0,"d3in_vip":0,"d3in_tag":0},"identify_info":{"position":0,"user_name":null},"quality":0,"kind":0,"symbol":0,"created_on":1442927014,"id":477411,"true_nickname":"\u4e5f\u8bb8","screen_name":"\u4e5f\u8bb8",
		//"big_avatar_url":"http:\/\/frbird.qiniudn.com\/avatar\/151110\/5641a7b23ffca2985b8b4908-avb.jpg",
		//"medium_avatar_url":"http:\/\/frbird.qiniudn.com\/avatar\/151110\/5641a7b23ffca2985b8b4908-avm.jpg",
		//"small_avatar_url":"http:\/\/frbird.qiniudn.com\/avatar\/151110\/5641a7b23ffca2985b8b4908-avs.jpg",
		//"mini_avatar_url":"http:\/\/frbird.qiniudn.com\/avatar\/151110\/5641a7b23ffca2985b8b4908-avn.jpg",
		//"home_url":"http:\/\/dev.taihuoniao.com\/user\/477411\/","view_follow_url":"http:\/\/dev.taihuoniao.com\/user\/477411\/follow\/","view_fans_url":"http:\/\/dev.taihuoniao.com\/user\/477411\/fans\/",
		try{
			JSONObject data = result.data;
			PageData page = new PageData();
			page.total_rows = data.getInt("total_rows");
			JSONArray rows = data.getJSONArray("rows");
			ProductComment comments[] = new ProductComment[rows.length()];
			//这里注意，如果更新了内容
			for(int i=0; i<rows.length(); i++){
				JSONObject row = rows.getJSONObject(i);
				ProductComment d = new ProductComment();
				JSONObject id = row.getJSONObject("_id");
				if(id!=null)
					d.strId = ShopAppContentProvider.tryString(id, "$id");
				d.star = ShopAppContentProvider.tryInt(row, "start");
				d.content = ShopAppContentProvider.tryString(row, "content");
				d.created_on = ShopAppContentProvider.tryString(row, "created_on");
				comments[i] = d;
				JSONObject user = row.getJSONObject("user");
				d.userInfo = new UserInfo();
				d.userInfo.nickname = ShopAppContentProvider.tryString(user, "nickname");
				d.userInfo.avatar = ShopAppContentProvider.tryString(user, "medium_avatar_url");
			}
			page.total_page = data.getInt("total_page");
			page.current_page = data.getInt("current_page");
			page.object = comments;
			result.object = page;
			success = true;
		}catch(JSONException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}	
		return success;
	}
	public ShopHttpParams doProductComment(int target_id,String content,int star){
		ShopHttpParams hp = new ShopHttpParams();
		hp.put("target_id", ""+target_id);
		hp.put("content", content);
		hp.put("star", ""+star);
		hp.url = ShopUtils.getProductCommentUrl();
		return hp;
	}	
//		
//	{"success":true,"is_error":false,"status":"0","message":"\u8bf7\u6c42\u6210\u529f",
//	"data":{
//			"total_rows":21,
//			"rows":[{
//						"_id":"563b1b323ffca21e438b60d2","rid":"115110502889",
//						"items":[{
//								"sku":1020885051,"product_id":1020885051,"quantity":1,"price":88,"sale_price":88,"name":"\u6d4b\u8bd5sku","cover_url":"http:\/\/frbird.qiniudn.com\/product\/150204\/54d2271d3ffca264238b46c8-1-s.jpg"
//							}],
//						"items_count":1,"total_money":88,"pay_money":88,"card_money":0,"coin_money":0,"freight":0,"discount":0,"user_id":477411,
//						"addbook_id":"561d005a3ffca222438b4bf9",
//						"express_info":{
//									"name":"\u521a\u521a\u597d","phone":"13422886692","area":null,"address":"\u5e7f\u544a\u6b4c\u65b9\u6cd5","zip":0,"email":null,"province":null,"city":null
//									},
//						"invoice_type":0,"invoice_caty":"p","invoice_title":"","invoice_content":"d","payment_method":"a",
//						"express_caty":"","express_no":"","sended_date":0,"card_code":"","is_presaled":0,"expired_time":1446886962,
//						"from_site":7,"status":1
//					},
//					{
//						"_id":"5638a69d3ffca21e438b5f88","rid":"115110302888",
//						"items":[{"sku":1071045007,"product_id":1071045007,"quantity":1,"price":0.01,"sale_price":0.01,"name":"\u6d4b\u8bd5\u4ea7\u54c1","cover_url":"http:\/\/frbird.qiniudn.com\/product\/150730\/55b9fb6c3ffca2235f8b4f73-1-s.jpg"
//					}],
//					"items_count":1,"total_money":0.01,"pay_money":0.01,"card_money":0,"coin_money":0,"freight":0,"discount":0,"user_id":477411,"addbook_id":"561d005a3ffca222438b4bf9",
//					"express_info":{
//							"name":"\u521a\u521a\u597d","phone":"13422886692","area":null,"address":"\u5e7f\u544a\u6b4c\u65b9\u6cd5","zip":0,"email":null,"province":null,"city":null},"invoice_type":0,"invoice_caty":"p","invoice_title":"","invoice_content":"d","payment_method":"a","express_caty":"","express_no":"","sended_date":0,"card_code":"","is_presaled":0,"expired_time":1446726045,"from_site":7,"status":10},{"_id":"5638a6663ffca222438b5f36","rid":"115110302887","items":[{"sku":1080959165,"product_id":1080959165,"quantity":1,"price":799,"sale_price":799,"name":"LISA\u667a\u80fd\u5b55\u8868\u2014\u60a8\u7684\u5b55\u671f\u8d34\u8eab\u95fa\u871c ","cover_url":"http:\/\/frbird.qiniudn.com\/product\/140809\/53e5d22d989a6a5d598b621f-1-s.jpg"}],"items_count":1,"total_money":799,"pay_money":799,"card_money":0,"coin_money":0,"freight":0,"discount":0,"user_id":477411,"addbook_id":"561d005a3ffca222438b4bf9","express_info":{"name":"\u521a\u521a\u597d","phone":"13422886692","area":null,"address":"\u5e7f\u544a\u6b4c\u65b9\u6cd5","zip":0,"email":null,"province":null,"city":null},"invoice_type":0,"invoice_caty":"p","invoice_title":"","invoice_content":"d","payment_method":"a","express_caty":"","express_no":"","sended_date":0,"card_code":"","is_presaled":0,"expired_time":1446725990,"from_site":7,"status":1},{"_id":"5638a5e93ffca221438b5fa4","rid":"115110302886","items":[{"sku":1080959165,"product_id":1080959165,"quantity":1,"price":799,"sale_price":799,"name":"LISA\u667a\u80fd\u5b55\u8868\u2014\u60a8\u7684\u5b55\u671f\u8d34\u8eab\u95fa\u871c ","cover_url":"http:\/\/frbird.qiniudn.com\/product\/140809\/53e5d22d989a6a5d598b621f-1-s.jpg"}],"items_count":1,"total_money":799,"pay_money":799,"card_money":0,"coin_money":0,"freight":0,"discount":0,"user_id":477411,"addbook_id":"561d005a3ffca222438b4bf9","express_info":{"name":"\u521a\u521a\u597d","phone":"13422886692","area":null,"address":"\u5e7f\u544a\u6b4c\u65b9\u6cd5","zip":0,"email":null,"province":null,"city":null},"invoice_type":0,"invoice_caty":"p","invoice_title":"","invoice_content":"d","payment_method":"a","express_caty":"","express_no":"","sended_date":0,"card_code":"","is_presaled":0,"expired_time":1446725865,"from_site":7,"status":1},{"_id":"5638a4513ffca222438b5f32","rid":"115110302885","items":[{"sku":1080959165,"product_id":1080959165,"quantity":1,"price":799,"sale_price":799,"name":"LISA\u667a\u80fd\u5b55\u8868\u2014\u60a8\u7684\u5b55\u671f\u8d34\u8eab\u95fa\u871c ","cover_url":"http:\/\/frbird.qiniudn.com\/product\/140809\/53e5d22d989a6a5d598b621f-1-s.jpg"}],"items_count":1,"total_money":799,"pay_money":799,"card_money":0,"coin_money":0,"freight":0,"discount":0,"user_id":477411,"addbook_id":"561d005a3ffca222438b4bf9","express_info":{"name":"\u521a\u521a\u597d","phone":"13422886692","area":null,"address":"\u5e7f\u544a\u6b4c\u65b9\u6cd5","zip":0,"email":null,"province":null,"city":null},"invoice_type":0,"invoice_caty":"p","invoice_title":"","invoice_content":"d","payment_method":"a","express_caty":"","express_no":"","sended_date":0,"card_code":"","is_presaled":0,"expired_time":1446725457,"from_site":7,"status":1},{"_id":"5638a3783ffca223438b5f09","rid":"115110302884","items":[{"sku":1080959165,"product_id":1080959165,"quantity":1,"price":799,"sale_price":799,"name":"LISA\u667a\u80fd\u5b55\u8868\u2014\u60a8\u7684\u5b55\u671f\u8d34\u8eab\u95fa\u871c ","cover_url":"http:\/\/frbird.qiniudn.com\/product\/140809\/53e5d22d989a6a5d598b621f-1-s.jpg"}],"items_count":1,"total_money":799,"pay_money":799,"card_money":0,"coin_money":0,"freight":0,"discount":0,"user_id":477411,"addbook_id":"561d005a3ffca222438b4bf9","express_info":{"name":"\u521a\u521a\u597d","phone":"13422886692","area":null,"address":"\u5e7f\u544a\u6b4c\u65b9\u6cd5","zip":0,"email":null,"province":null,"city":null},"invoice_type":0,"invoice_caty":"p","invoice_title":"","invoice_content":"d","payment_method":"a","express_caty":"","express_no":"","sended_date":0,"card_code":"","is_presaled":0,"expired_time":1446725240,"from_site":7,"status":1},{"_id":"5638a2363ffca21f438b5f7d","rid":"115110302883","items":[{"sku":1080959165,"product_id":1080959165,"quantity":1,"price":799,"sale_price":799,"name":"LISA\u667a\u80fd\u5b55\u8868\u2014\u60a8\u7684\u5b55\u671f\u8d34\u8eab\u95fa\u871c ","cover_url":"http:\/\/frbird.qiniudn.com\/product\/140809\/53e5d22d989a6a5d598b621f-1-s.jpg"}],"items_count":1,"total_money":799,"pay_money":799,"card_money":0,"coin_money":0,"freight":0,"discount":0,"user_id":477411,"addbook_id":"561d005a3ffca222438b4bf9","express_info":{"name":"\u521a\u521a\u597d","phone":"13422886692","area":null,"address":"\u5e7f\u544a\u6b4c\u65b9\u6cd5","zip":0,"email":null,"province":null,"city":null},"invoice_type":0,"invoice_caty":"p","invoice_title":"","invoice_content":"d","payment_method":"a","express_caty":"","express_no":"","sended_date":0,"card_code":"","is_presaled":0,"expired_time":1446724918,"from_site":7,"status":1},{"_id":"5638a0ec3ffca221438b5f9d","rid":"115110302882","items":[{"sku":1080959165,"product_id":1080959165,"quantity":1,"price":799,"sale_price":799,"name":"LISA\u667a\u80fd\u5b55\u8868\u2014\u60a8\u7684\u5b55\u671f\u8d34\u8eab\u95fa\u871c ","cover_url":"http:\/\/frbird.qiniudn.com\/product\/140809\/53e5d22d989a6a5d598b621f-1-s.jpg"}],"items_count":1,"total_money":799,"pay_money":799,"card_money":0,"coin_money":0,"freight":0,"discount":0,"user_id":477411,"addbook_id":"561d005a3ffca222438b4bf9","express_info":{"name":"\u521a\u521a\u597d","phone":"13422886692","area":null,"address":"\u5e7f\u544a\u6b4c\u65b9\u6cd5","zip":0,"email":null,"province":null,"city":null},"invoice_type":0,"invoice_caty":"p","invoice_title":"","invoice_content":"d","payment_method":"a","express_caty":"","express_no":"","sended_date":0,"card_code":"","is_presaled":0,"expired_time":1446724588,"from_site":7,"status":1},{"_id":"5638a0a13ffca21d438b5f26","rid":"115110302881","items":[{"sku":1080959165,"product_id":1080959165,"quantity":1,"price":799,"sale_price":799,"name":"LISA\u667a\u80fd\u5b55\u8868\u2014\u60a8\u7684\u5b55\u671f\u8d34\u8eab\u95fa\u871c ","cover_url":"http:\/\/frbird.qiniudn.com\/product\/140809\/53e5d22d989a6a5d598b621f-1-s.jpg"}],"items_count":1,"total_money":799,"pay_money":799,"card_money":0,"coin_money":0,"freight":0,"discount":0,"user_id":477411,"addbook_id":"561d005a3ffca222438b4bf9","express_info":{"name":"\u521a\u521a\u597d","phone":"13422886692","area":null,"address":"\u5e7f\u544a\u6b4c\u65b9\u6cd5","zip":0,"email":null,"province":null,"city":null},"invoice_type":0,"invoice_caty":"p","invoice_title":"","invoice_content":"d","payment_method":"a","express_caty":"","express_no":"",
//		462
//		"sended_date":0,"card_code":"","is_presaled":0,"expired_time":1446724513,"from_site":7,"status":1},{"_id":"5638a01e3ffca223438b5f05","rid":"115110302880","items":[{"sku":1080959165,"product_id":1080959165,"quantity":1,"price":799,"sale_price":799,"name":"LISA\u667a\u80fd\u5b55\u8868\u2014\u60a8\u7684\u5b55\u671f\u8d34\u8eab\u95fa\u871c ","cover_url":"http:\/\/frbird.qiniudn.com\/product\/140809\/53e5d22d989a6a5d598b621f-1-s.jpg"}],"items_count":1,"total_money":799,"pay_money":799,"card_money":0,"coin_money":0,"freight":0,"discount":0,"user_id":477411,"addbook_id":"561d005a3ffca222438b4bf9","express_info":{"name":"\u521a\u521a\u597d","phone":"13422886692","area":null,"address":"\u5e7f\u544a\u6b4c\u65b9\u6cd5","zip":0,"email":null,"province":null,"city":null},
//		"invoice_type":0,"invoice_caty":"p","invoice_title":"","invoice_content":"d","payment_method":"a","express_caty":"","express_no":"","sended_date":0,"card_code":"","is_presaled":0,"expired_time":1446724382,"from_site":7,"status":1}],
//		"total_page":3,"current_page":1,"pager":"","next_page":2,"prev_page":0,"current_user_id":477411},"current_user_id":477411}	
	
	public boolean parseShoppingOrderList(ResultData result){
		boolean success = false;
		try{
			JSONObject data = result.data;
			PageData page = new PageData();
			page.total_rows = data.getInt("total_rows");
			JSONArray rows = data.getJSONArray("rows");
			List<ShoppingOrder> orderList = new LinkedList<ShoppingOrder>();
			//这里注意，如果更新了内容
			for(int i=0; i<rows.length(); i++){
				JSONObject row = rows.getJSONObject(i);
				ShoppingOrder d = new ShoppingOrder();
				d.rid = ShopAppContentProvider.tryString(row, "rid");
				JSONArray shop_items = row.getJSONArray("items");
				if(shop_items!=null){
					d.shopping_items = new ShoppingItem[shop_items.length()];
				}
				for(int j=0;j<shop_items.length(); j++){
					d.shopping_items[j] = new ShoppingItem();
					JSONObject item = shop_items.getJSONObject(j);
					d.shopping_items[j].sku = ShopAppContentProvider.tryString(item,"sku");
					d.shopping_items[j].name = ShopAppContentProvider.tryString(item,"name");
					d.shopping_items[j].sale_price = ShopAppContentProvider.tryDouble(item,"sale_price");
					d.shopping_items[j].price = ShopAppContentProvider.tryDouble(item,"price");
					d.shopping_items[j].quantity = ShopAppContentProvider.tryInt(item,"quantity");
					d.shopping_items[j].cover_url = ShopAppContentProvider.tryString(item,"cover_url");
					d.shopping_items[j].product_id = ShopAppContentProvider.tryString(item,"product_id");
				}
				d.expired_time =  ShopAppContentProvider.tryLong(row, "expired_time");
				d.status = ShopAppContentProvider.tryInt(row, "status");
				d.pay_money = ShopAppContentProvider.tryDouble(row, "pay_money");
				d.total_money = ShopAppContentProvider.tryDouble(row, "total_money");
				d.addbook_id = ShopAppContentProvider.tryString(row, "addbook_id");
				JSONObject express_info = row.getJSONObject("express_info");
				if(express_info != null){
					d.realname = ShopAppContentProvider.tryString(express_info, "name");
					d.address = ShopAppContentProvider.tryString(express_info, "address");					
				}
				orderList.add(d);
			}
			page.total_page = data.getInt("total_page");
			page.current_page = data.getInt("current_page");
			page.object = orderList;
			result.object = page;
			success = true;
		}catch(JSONException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}	
		return success;
	}
	public ShopHttpParams getShoppingOrderList(int status,PageData page){
		ShopHttpParams hp = new ShopHttpParams();
		if(status >= 0){//１.待付款;   2.待发货;3.已发货;4.确认收货;5.申请退款; 6.退款成功; 9,过期订单; 9,取消订单;
			hp.put("status", ""+status);
		}//没有，则是全部获取
		hp.url = ShopUtils.getShoppingOrderListUrl();
		page.addPageParams(hp.params);
		return hp;
	}	
	public ShopHttpParams cancelOrder(String rid){
		ShopHttpParams hp = new ShopHttpParams();
		hp.put("rid", rid);
		hp.url = ShopUtils.getCancelOrderUrl();
		return hp;
	}
	
	public ShopHttpParams doShoppingCheckout(String array){
		ShopHttpParams hp = new ShopHttpParams();
		hp.put("array", array);
		hp.url = ShopUtils.getShoppingCheckoutUrl();
		return hp;
	}
	//测试登陆，如果没有登陆则返回false
	public boolean testLogin(Context context){
		if(!isLogined()){
			Intent intent = new Intent();
			intent.setClass(context, StartActivity.class);
			intent.putExtra(LoginActivity.DIRECT_TO_MAIN, false);
			context.startActivity(intent);
			return false;
		}
		return true;
	}	
}
