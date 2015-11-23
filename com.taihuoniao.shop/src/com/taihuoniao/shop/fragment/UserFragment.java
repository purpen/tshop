package com.taihuoniao.shop.fragment;

import com.taihuoniao.shop.R;
import com.taihuoniao.shop.ShopApp;
import com.taihuoniao.shop.R.drawable;
import com.taihuoniao.shop.R.id;
import com.taihuoniao.shop.R.layout;
import com.taihuoniao.shop.R.string;
import com.taihuoniao.shop.ShopApp.ShopHttpParams;
import com.taihuoniao.shop.ShopApp.UserInfo;
import com.taihuoniao.shop.ShopUtils;
import com.taihuoniao.shop.activity.BaseStyleActivity;
import com.taihuoniao.shop.activity.LoginActivity;
import com.taihuoniao.shop.activity.ShoppingOrderListActivity;
import com.taihuoniao.shop.activity.StartActivity;
import com.taihuoniao.shop.activity.UserInfoActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * 
 */
public class UserFragment extends BaseStyleFragment {
	
	View user_info;
	ImageView avatar;
	TextView nickname;
	TextView address;
	View order;
	View subject;
	View favorite;
	View message;
	View logout;
	public UserFragment() {
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_user, container, false);
		user_info = v.findViewById(R.id.user_info);
		avatar = (ImageView)v.findViewById(R.id.avatar);
		nickname = (TextView)v.findViewById(R.id.nickname);
		address = (TextView)v.findViewById(R.id.address);
		order = v.findViewById(R.id.order);
		subject = v.findViewById(R.id.subject);
		favorite = v.findViewById(R.id.favorite);
		message = v.findViewById(R.id.message);
		logout = v.findViewById(R.id.logout);
		user_info.setOnClickListener(mOnClick);
		order.setOnClickListener(mOnClick);
		subject.setOnClickListener(mOnClick);
		message.setOnClickListener(mOnClick);
		logout.setOnClickListener(mOnClick);
		readInfo();
		return v;
	}
	OnClickListener mOnClick = new OnClickListener() {			
		@Override
		public void onClick(View v) {		
			if(v == logout){
				ShopApp.self().setUserId(0, "");
				ShopHttpParams hp = ShopApp.self().doLogout();
				sendUrlRequest(hp);				
			}else if (v == user_info){
				FragmentActivity t = getActivity();
				Intent intent = new Intent();
				intent.setClass(t,UserInfoActivity.class);
				t.startActivity(intent);
			}else if(v==order){
				FragmentActivity t = getActivity();
				Intent intent = new Intent();
				intent.setClass(t,ShoppingOrderListActivity.class);
				t.startActivity(intent);				
			}
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		getActivity().setTitle(R.string.title_user);
		final BaseStyleActivity bsa = (BaseStyleActivity)getActivity();
		bsa.setBackAction(null);
		ShopHttpParams hp = ShopApp.self().doGetUserInfo();
		sendUrlRequest(hp);		
	}
	private static final String CITY_KEY="city";
	private static final String AVATAR_KEY="avatar";
	private String mCity;
	private String mAvatar;
	
	public void readInfo(){
		SharedPreferences sp = this.getActivity().getPreferences(Context.MODE_PRIVATE);
		mCity = sp.getString(CITY_KEY, "");
		mAvatar = sp.getString(AVATAR_KEY, "");		
		nickname.setText(ShopApp.self().getNickName());
		address.setText(mCity);
		ShopApp.self().showImageAsyn(avatar, mAvatar, R.drawable.image_loading_background);
	}
	
	public void saveInfo(){
		ShopApp.UserInfo ui = ShopApp.self().getUserInfo();
		if (!ui.city.equalsIgnoreCase(mCity) || !ui.avatar.equalsIgnoreCase(mAvatar)){
			mCity = ui.city;
			mAvatar = ui.avatar;
			SharedPreferences sp = this.getActivity().getPreferences(Context.MODE_PRIVATE);
			Editor edit = sp.edit();
			edit.putString(CITY_KEY, mCity);
			edit.putString(AVATAR_KEY, mAvatar);
			edit.commit();
		}
		nickname.setText(ShopApp.self().getNickName());
		address.setText(mCity);
		ShopApp.self().showImageAsyn(avatar, mAvatar, R.drawable.image_loading_background);		
	}
	
	public void testLogin(){
		if(!ShopApp.self().isLogined()){
			Intent intent = new Intent();
			intent.setClass(getActivity(), StartActivity.class);
			intent.putExtra(LoginActivity.DIRECT_TO_MAIN, false);
			getActivity().startActivity(intent);
		}
	}
	public void onUrlSuccess(ShopHttpParams hp, com.taihuoniao.shop.ShopApp.ResultData result){
		if(hp.url.startsWith(ShopUtils.getUserInfoUrl())){
			ShopApp.self().parseUserInfo(result);
			saveInfo();
		}else if(hp.url.startsWith(ShopUtils.getLogoutUrl())){
			getActivity().finish();
		}
	};
	public void onUrlFailure(ShopHttpParams hp, com.taihuoniao.shop.ShopApp.ResultData result){
		if(hp.url.startsWith(ShopUtils.getUserInfoUrl())){
			testLogin();
			ShopApp.self().showToast(getActivity(), result.message);
		}else if(hp.url.startsWith(ShopUtils.getLogoutUrl())){
			getActivity().finish();
		}		
	};
	
}
