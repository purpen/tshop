package com.taihuoniao.shop.activity;

import com.taihuoniao.shop.R;
import com.taihuoniao.shop.R.drawable;
import com.taihuoniao.shop.R.id;
import com.taihuoniao.shop.R.layout;
import com.taihuoniao.shop.R.string;
import com.taihuoniao.shop.ShopApp;
import com.taihuoniao.shop.ShopApp.ResultData;
import com.taihuoniao.shop.ShopApp.ShopHttpParams;
import com.taihuoniao.shop.fragment.CartFragment;
import com.taihuoniao.shop.fragment.HomeFragment;
import com.taihuoniao.shop.fragment.ShopFragment;
import com.taihuoniao.shop.fragment.UserFragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends BaseStyleActivity{
	private static final String HOME_TAG = "home";
	private static final String SHOP_TAG = "shop";
	private static final String USER_TAG = "user";
	private static final String CART_TAG = "cart";
	RadioGroup radio_group;
	FragmentTabHost tabHost;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);      
		showBackButton(false);
		tabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        tabHost.setup(this,getSupportFragmentManager(),android.R.id.tabcontent);
        tabHost.addTab(tabHost.newTabSpec(HOME_TAG).setIndicator(getTabItemView(R.drawable.icon_home,getString(R.string.home))),HomeFragment.class,null);
        tabHost.addTab(tabHost.newTabSpec(SHOP_TAG).setIndicator(getTabItemView(R.drawable.icon_shop,getString(R.string.shop))),ShopFragment.class,null);
        tabHost.addTab(tabHost.newTabSpec(USER_TAG).setIndicator(getTabItemView(R.drawable.icon_user,getString(R.string.user))),UserFragment.class,null);
        tabHost.addTab(tabHost.newTabSpec(CART_TAG).setIndicator(getTabItemView(R.drawable.icon_cart,getString(R.string.cart))),CartFragment.class,null);
        tabHost.getTabWidget().setDividerDrawable(null);
        tabHost.setOnTabChangedListener(new OnTabChangeListener() {			
			@Override
			public void onTabChanged(String tag) {
				if(tag.equalsIgnoreCase(HOME_TAG)){
				}
			}
		});
		radio_group = (RadioGroup)findViewById(R.id.radio_group);
		radio_group.check(R.id.home);
		radio_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch(checkedId){
				case R.id.home:
					tabHost.setCurrentTabByTag(HOME_TAG);
					Toast.makeText(MainActivity.this, HOME_TAG, 5000).show();
					break;
				case R.id.shop:
					tabHost.setCurrentTabByTag(SHOP_TAG);
					Toast.makeText(MainActivity.this, SHOP_TAG, 5000).show();
					break;	
				case R.id.user:
					tabHost.setCurrentTabByTag(USER_TAG);
					Toast.makeText(MainActivity.this, USER_TAG, 5000).show();
					break;					
				case R.id.cart:
					tabHost.setCurrentTabByTag(CART_TAG);
					Toast.makeText(MainActivity.this, CART_TAG, 5000).show();
					break;										
				}
			}
		});
	}
    private View getTabItemView(int imageId,String text){  
        View view = this.getLayoutInflater().inflate(R.layout.tab_item_view, null);        
        ImageView imageView = (ImageView) view.findViewById(R.id.imageview);  
        imageView.setImageResource(imageId);          
        TextView textView = (TextView) view.findViewById(R.id.textview);          
        textView.setText(text);      
        return view;
   } 
	@Override
	public void onUrlSuccess(ShopHttpParams hp, ResultData result) {
		ShopApp.self().showToast(this, result.message);
	}
	
	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {     
		super.onActivityResult(requestCode, resultCode, data);     
		if (requestCode == StartActivity.LOGIN_REQUEST){
			if(resultCode == RESULT_OK){
				
			}
		}
	}
	public void testLogin(){
		if(!ShopApp.self().isLogined()){
			Intent intent = new Intent();
			intent.setClass(this, StartActivity.class);
			int requestCode = StartActivity.LOGIN_REQUEST;
			startActivityForResult(intent, requestCode);
		}
	}	
	private long lastPressTime = 0;
	private long TIME_DELAY_QUIT = 2000;
	@Override
	public void onBackPressed() {
		if(clickBackButton()){
			return;
		}
		if(lastPressTime == 0 || System.currentTimeMillis() - lastPressTime > TIME_DELAY_QUIT ){
			//很奇怪，toast的显示时间是不够TIME_DELAY_QUIT的时间的，估计是toast的显示包含了动画时间
			Toast.makeText(this, "再次按下返回键退出！", (int)TIME_DELAY_QUIT + 3000).show();
		}else{
			super.onBackPressed();
		}
		lastPressTime = System.currentTimeMillis();		
	}
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// Handle action bar item clicks here. The action bar will
//		// automatically handle clicks on the Home/Up button, so long
//		// as you specify a parent activity in AndroidManifest.xml.
//		int id = item.getItemId();
//		if (id == R.id.action_settings) {
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}
}
