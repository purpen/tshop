package com.taihuoniao.shop.activity;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.taihuoniao.shop.R;
import com.taihuoniao.shop.R.id;
import com.taihuoniao.shop.R.layout;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;


class Constants {
	public static final String APP_KEY      = "1708367618";     // 应用的APP_KEY     
	public static final String REDIRECT_URL = "http://m.taihuoniao.com/auth/voke";// 应用的回调页     
	public static final String SCOPE =           // 应用申请的高级权限             
			"email,direct_messages_read,direct_messages_write,"
			+ "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
			+ "follow_app_official_microblog," + "invitation_write";
} 
 
public class StartActivity extends Activity {
	private Tencent mTencent;
	private AuthInfo mAuthInfo;
	Oauth2AccessToken mAccessToken;
	TextView log;
	boolean is_direct_to_main = true;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		is_direct_to_main = getIntent().getBooleanExtra(LoginActivity.DIRECT_TO_MAIN, true);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_start);
		log = (TextView)findViewById(R.id.text_log);

	}
	class AuthListener implements WeiboAuthListener {    
		@Override 
		public void onComplete(Bundle values) {   
			mAccessToken = Oauth2AccessToken.parseAccessToken(values); // 从 Bundle 中解析 Token         
			if (mAccessToken.isSessionValid()) {  
				Toast.makeText(StartActivity.this, "新浪微博成功授权", 5000);
					//保存Token     .........         
			} else {// 当您注册的应用程序签名不正确时，就会收到错误Code，请确保签名正确             
				String code = values.getString("code", "");
				Toast.makeText(StartActivity.this, "新浪微博授权失败，" + code, 5000);	
			}
		}	
		public void onWeiboException(WeiboException e){
			Toast.makeText(StartActivity.this,e.getMessage(), 5000);
		}
		public void onCancel(){
			Toast.makeText(StartActivity.this,"用户取消了微博授权", 5000);			
		}
	};
	SsoHandler mSsoHandler;
	public void weibo(View v){
		mAuthInfo = new AuthInfo(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
		mSsoHandler = new SsoHandler(StartActivity.this, mAuthInfo);
		mSsoHandler. authorizeClientSso(new AuthListener());
	}
	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {     
		super.onActivityResult(requestCode, resultCode, data);     
		if (mSsoHandler != null) {         
			mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
		}
		if (requestCode == LOGIN_REQUEST){
			if(resultCode == RESULT_OK){
				setResult(RESULT_OK);
				finish();
			}
		}
	}
//////////////////////////////////////////////////////////////////////////////////////////
	private class BaseUiListener implements IUiListener {
		 @Override
		 public void onComplete(Object response) {
		     //V2.0版本，参数类型由JSONObject 改成了Object,具体类型参考api文档
			 doComplete((JSONObject)response);			 
		 }
		 protected void doComplete(JSONObject values) {
			 Toast.makeText(StartActivity.this, values.toString(), 5000);		 				 
		 }
		 @Override
		 public void onError(UiError e) {
			 String msg_text = "onError:" + "code:" + e.errorCode + ", msg:" + e.errorMessage + ", detail:" + e.errorDetail;
			 Toast.makeText(StartActivity.this, msg_text, 5000);
		 }
		 @Override
		 public void onCancel() {
			 String msg_text = "onCancel";
			 Toast.makeText(StartActivity.this, msg_text, 5000);		 	
		 }
	}
	
	
	public void qq(View v){
		mTencent = Tencent.createInstance(Constants.APP_KEY, this.getApplicationContext());
		if (!mTencent.isSessionValid())
		{
			String SCOPE = "get_user_info,add_t";
			mTencent.login(this, SCOPE, new BaseUiListener());
		}		
	}	
	public static final int LOGIN_REQUEST = 1;//
	public void login(View v){
		Intent intent = new Intent();
		intent.putExtra(LoginActivity.DIRECT_TO_MAIN, is_direct_to_main);
		intent.setClass(this, LoginActivity.class);
		startActivityForResult(intent,LOGIN_REQUEST);
		finish();
	}
	public void register(View v){
		Intent intent = new Intent();
		intent.setClass(this, RegisterActivity.class);
		startActivity(intent);
		finish();
	}	
}
