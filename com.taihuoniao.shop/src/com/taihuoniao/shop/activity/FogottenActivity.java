package com.taihuoniao.shop.activity;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.taihuoniao.shop.R;
import com.taihuoniao.shop.ShopApp;
import com.taihuoniao.shop.R.id;
import com.taihuoniao.shop.R.layout;
import com.taihuoniao.shop.ShopApp.ResultData;
import com.taihuoniao.shop.ShopApp.ShopHttpParams;
import com.taihuoniao.shop.ShopUtils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class FogottenActivity extends BaseStyleActivity {

	TextView log;
	Button btnRegister;
	Button btnSendVertifyCode;
	EditText editUser;
	EditText editPass;
	EditText editCode;
	@Override
	public void onUrlSuccess(ShopHttpParams hp, ResultData result) {
		if(hp.url.startsWith(ShopUtils.getSendVerifyCodeUrl())){
			ShopApp.self().showToast(this, "发送验证码成功!");
		}else if(hp.url.startsWith(ShopUtils.getResetPassUrl())){
			ShopApp.self().showToast(this, "重置成功!");
		}else{
			ShopApp.self().showToast(this, result.message);
		}
	}	
	@Override
	public void onUrlRequestReturn(ShopHttpParams hp, ResultData result){
		super.onUrlFailure(hp, result);
		if(hp.url.startsWith(ShopUtils.getSendVerifyCodeUrl())){
			btnSendVertifyCode.setEnabled(true);			
		}else if(hp.url.startsWith(ShopUtils.getResetPassUrl())){
			btnSendVertifyCode.setEnabled(true);
			btnRegister.setEnabled(true);
		}		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fogotten);
		log = (TextView)findViewById(R.id.textViewLog);
		btnRegister = (Button)findViewById(R.id.register);
		btnSendVertifyCode = (Button)findViewById(R.id.vertify);
		editUser = (EditText)findViewById(R.id.user);
		editPass = (EditText)findViewById(R.id.pass);
		editCode = (EditText)findViewById(R.id.code);		
		this.showBackButton(true);
	}
	public void onSendVertifyCode(View v){
		final String user = editUser.getEditableText().toString();
		if(user.length() != 11){
			Toast.makeText(this, "请输入11位手机号码", 5000);
			return;
		}
		btnSendVertifyCode.setEnabled(false);
		btnRegister.setEnabled(false);
		ShopHttpParams hp = ShopApp.doSendVerifyCode(user);
		sendUrlRequest(hp);		
	}
	public void onResetPass(View v){
		final String user = editUser.getEditableText().toString();
		final String pass = editPass.getEditableText().toString();
		final String code = editCode.getEditableText().toString();
		if(user.length() < 11){
			Toast.makeText(this, "请输入11位手机号码", 5000);
			return;
		}	
		if(pass.length() < 5){
			Toast.makeText(this, "密码必须大于5位", 5000);
			return;
		}		
		if(code.length() < 3){
			Toast.makeText(this, "请输入验证码", 5000);
			return;
		}		
		btnSendVertifyCode.setEnabled(false);
		btnRegister.setEnabled(false);
		ShopHttpParams hp = ShopApp.doResetPass(user, pass, code);
		sendUrlRequest(hp);
	}
}
