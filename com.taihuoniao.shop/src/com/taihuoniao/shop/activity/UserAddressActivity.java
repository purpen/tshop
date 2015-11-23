package com.taihuoniao.shop.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.taihuoniao.shop.R;
import com.taihuoniao.shop.ShopApp;
import com.taihuoniao.shop.ShopApp.ResultData;
import com.taihuoniao.shop.ShopApp.ShopHttpParams;
import com.taihuoniao.shop.ShopApp.UserAddress;
import com.taihuoniao.shop.ShopUtils;
import com.taihuoniao.shop.widget.SwipeListView;
import com.taihuoniao.shop.widget.SwipeListView.OnDeleteItemListener;

public class UserAddressActivity extends BaseStyleActivity  {	
	SwipeListView list;
	boolean initDistrict = false;
	boolean chooseAddress = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_address);
		list = (SwipeListView)findViewById(R.id.list);
		showBackButton(true);
		Intent intent = getIntent();
		chooseAddress = intent.getBooleanExtra("chooseAddress", false);
		list.setOnItemClickListener(mOnItemClick);
		list.setOnDeleteItemListener( new OnDeleteItemListener() {
			@Override
			public void onDeleteItem(int position, View content) {
				UserAddress ua = (UserAddress)content.getTag();
				ShopHttpParams hp = ShopApp.self().delAddress(ua._id);
				sendUrlRequest(hp);
				list.setEnabled(false);
			}
		});
		updateAddress();		
	}
	
	OnItemClickListener mOnItemClick = new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			UserAddress ua =  (UserAddress)view.getTag();
			if(chooseAddress){				
				Intent intent = new Intent();
				intent.putExtra(ADDR_POSITION, position);
				setResult(RESULT_OK, intent); //intent为A传来的带有Bundle的intent，当然也可以自己定义新的Bundle
				finish();//此处一定要调用finish()方法
			}else{
				startEditAddressActivity(ua._id);
			}
		}
	};
	public static final String ADDR_POSITION = "addr_position";

	private void startEditAddressActivity(String addr_id){
		Intent intent = new Intent();
		intent.putExtra(AddUserAddressActivity.ADDR_ID, addr_id);
		intent.setClass(this, AddUserAddressActivity.class);	
		int requestCode = ADD_ADDRESS_SUCCESS;//必须>=0
		startActivityForResult(intent, requestCode);		
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == ADD_ADDRESS_SUCCESS){
			if(resultCode == RESULT_OK)
				updateAddress();
		}else if(requestCode == EDIT_ADDRESS_SUCCESS){
			if(resultCode == RESULT_OK)
				updateAddress();
		}
	}
	
	private static final int ADD_ADDRESS_SUCCESS = 1;
	private static final int EDIT_ADDRESS_SUCCESS = 2;
	public void onAddAddress(View v){
		startEditAddressActivity(null);
	}
	@Override
	protected void onResume() {
		super.onResume();
	}

	
	private void updateAddress(){
		ShopHttpParams hp = ShopApp.self().doUserAddress();
		sendUrlRequest(hp);
	}
	private void getDistrict(){
		if(initDistrict == false){
			initDistrict = true;
//			ShopHttpParams hp = ShopApp.self().doGetDistricts(-1);
//			sendUrlRequest(hp);
			DistrictTask task = new DistrictTask();
			task.execute((Void)null);
		}
	}
	private class DistrictTask extends AsyncTask<Void,Void,ResultData>{
		@Override
		protected ResultData doInBackground(Void... params) {
			ShopHttpParams hp = ShopApp.self().doGetProvinces();
			ResultData result = ShopApp.self().doCommonAction(hp);
			ShopApp.self().parseDistrict(result);			
			hp = ShopApp.self().doGetDistricts(-1);
			result = ShopApp.self().doCommonAction(hp);
			ShopApp.self().parseDistrict(result);
			return result;
		}
		@Override
		protected void onPostExecute(ResultData result) {			
			super.onPostExecute(result);
			showAddress();
		}
	}	
	
	private void showAddress(){
		AddressAdapter adapter = new AddressAdapter(ShopApp.self().getUserAddresses(),this.getLayoutInflater());
		list.setAdapter(adapter);
	}
	class AddressAdapter extends BaseAdapter{
		UserAddress []mUserAddresses;
		LayoutInflater mInflater;
		
		AddressAdapter(UserAddress []addr,LayoutInflater inflater){
			mUserAddresses = addr;
			mInflater = inflater;
		}		
		@Override
		public int getCount() {
			return mUserAddresses.length;
		}

		@Override
		public Object getItem(int i) {
			if( i>= 0 && i < mUserAddresses.length)
				return mUserAddresses[i];
			return null;
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View v, ViewGroup parent) {
			
			if (v == null){
				v = mInflater.inflate(R.layout.address_item, parent,false);
			}
			TextView realname = (TextView)v.findViewById(R.id.realname);
			TextView address = (TextView)v.findViewById(R.id.address);
			TextView phone = (TextView)v.findViewById(R.id.phone);
			CompoundButton addr_default = (CompoundButton)v.findViewById(R.id.addr_default);
			if( i>= 0 && i < mUserAddresses.length){
				UserAddress ua =  mUserAddresses[i];
				realname.setText(ua.name);
				String strProvince = ShopApp.self().getDistrict(ua.province);
				String strCity = ShopApp.self().getDistrict(ua.city);
				addr_default.setChecked(ua.is_default == 1);
				addr_default.setVisibility(ua.is_default == 1?View.VISIBLE:View.INVISIBLE);
				if(strProvince == null || strProvince.isEmpty() || strCity == null || strCity.isEmpty()){
					getDistrict();
					address.setText(ua.province + " " + ua.city + " " + ua.address);
				}else{
					address.setText(strProvince + " " + strCity + " " + ua.address);
				}
				phone.setText(ua.phone);
				v.setTag(ua);
			}
			return v;
		}
	};

	@Override
	public void onUrlSuccess(ShopHttpParams hp, ResultData result) {
		if(hp.url.startsWith(ShopUtils.getUserAddressUrl())){
			ShopApp.self().parseUserAddress(result);
			showAddress();			
		}else if(hp.url.startsWith(ShopUtils.getDistrictsUrl())){
			ShopApp.self().parseDistrict(result);
			showAddress();
		}else if(hp.url.startsWith(ShopUtils.getDelUserAddressUrl())){
			updateAddress();
			list.setEnabled(true);
		}
	}
	public void onUrlFailure(ShopHttpParams hp, ResultData result) {
		showAddress();
	}	
}
