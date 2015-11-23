package com.taihuoniao.shop.activity;

import org.apache.http.params.HttpParams;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.taihuoniao.shop.R;
import com.taihuoniao.shop.ShopApp;
import com.taihuoniao.shop.ShopApp.ResultData;
import com.taihuoniao.shop.ShopApp.ShopHttpParams;
import com.taihuoniao.shop.ShopUtils;

public class LoginActivity extends BaseStyleActivity {
	TextView log;
	Button button;
	EditText editUser;
	EditText editPass;
	boolean is_direct_to_main = true;
	public static final String DIRECT_TO_MAIN = "direct_to_main";
	@Override
	public void onUrlSuccess(ShopHttpParams hp, ResultData result) {
		Intent intent = new Intent();
		if(is_direct_to_main){
			intent.setClass(LoginActivity.this, MainActivity.class);
			LoginActivity.this.startActivity(intent);
		}else{
			setResult(RESULT_OK);
		}
		LoginActivity.this.finish();		
	}
	public void onUrlFailure(ShopHttpParams hp, ResultData result) {
		button.setEnabled(true);
		super.onUrlFailure(hp, result);
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		is_direct_to_main = getIntent().getBooleanExtra(DIRECT_TO_MAIN, true);
		if(ShopApp.self().isLogined() && is_direct_to_main){
			Intent intent = new Intent();
			intent.setClass(LoginActivity.this, MainActivity.class);
			LoginActivity.this.startActivity(intent);
			LoginActivity.this.finish();
			return;
		}		
		setContentView(R.layout.activity_login);
		this.setBackActivtyClass(StartActivity.class);
		log = (TextView)findViewById(R.id.textViewLog);
		editUser = (EditText)findViewById(R.id.user);
		editPass = (EditText)findViewById(R.id.pass);
		button = (Button)findViewById(R.id.login);
		button.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				final String user = editUser.getEditableText().toString();
				final String pass = editPass.getEditableText().toString();
				ShopHttpParams hp = ShopApp.doLogin(user, pass);
				button.setEnabled(false);
				sendUrlRequest(hp);				
			}
		});
		findViewById(R.id.fogot).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(LoginActivity.this, FogottenActivity.class);
				LoginActivity.this.startActivity(intent);
			}
		});
	}
	
}
