package com.taihuoniao.shop.activity;

import java.text.DecimalFormat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.taihuoniao.shop.R;
import com.taihuoniao.shop.ShopApp;
import com.taihuoniao.shop.ShopApp.ResultData;
import com.taihuoniao.shop.ShopApp.ShopHttpParams;
import com.taihuoniao.shop.ShopApp.ShoppingItem;
import com.taihuoniao.shop.ShopApp.ShoppingOrder;
import com.taihuoniao.shop.ShopApp.UserAddress;
import com.taihuoniao.shop.ShopAppContentProvider;
import com.taihuoniao.shop.ShopAppContentProvider.CartItem;

public class ShoppingOrderActivity extends BaseStyleActivity {
	
	private TextView price;
	private ViewGroup addressGroup;
	private ListView list;
	private Button confirm;
	private boolean clearCart = false;
	private int curAddress = -1;
	private boolean updateDistrict = false;
	public static final String ORDER_OBJECT = "order";
	private ShoppingOrder order;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		order = (ShoppingOrder)intent.getSerializableExtra(ORDER_OBJECT);
		clearCart = intent.getBooleanExtra("clearCart", false);
		setContentView(R.layout.activity_shopping_order);
		showBackButton(true);
		price = (TextView)findViewById(R.id.total_cost);
		list = (ListView)findViewById(R.id.list);
		addressGroup = (ViewGroup)findViewById(R.id.addressGroup);
		addressGroup.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startChooseAddressActivity();	
			}
		});
		confirm = (Button)findViewById(R.id.confirm);
		confirm.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				UserAddress[] uas = ShopApp.self().getUserAddresses(); 
				if(uas != null &&curAddress>=0&&curAddress<uas.length){
					confirm.setEnabled(false);
					ShoppingConfirmTask task = new ShoppingConfirmTask();
					task.execute();
				}else{
					ShopApp.self().showToast(ShoppingOrderActivity.this, "缺少收货地址！");
				}
			}
		});
		displayShoppingList();
	}
	@Override
	protected void onResume() {
		super.onResume();
		if(!displayAddress()){
			AddressTask task = new AddressTask();
			task.execute((Void)null);			
		}	
	}
	private void startChooseAddressActivity(){
		Intent intent=new Intent();
		intent.setClass(ShoppingOrderActivity.this, UserAddressActivity.class);
		intent.putExtra("chooseAddress", true);//选择地址
		int requestCode = 0;//必须>=0
		startActivityForResult(intent, requestCode);
	}
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) { // //resultCode为回传的标记，一般回传的是RESULT_OK作为成功的处理
		   case RESULT_OK:
			curAddress = data.getIntExtra(UserAddressActivity.ADDR_POSITION,curAddress);
			showCurAddr();
		    break;
		default:
		    break;
		}
	}
	private void showCurAddr(){
		int i = curAddress;
		UserAddress[] uas = ShopApp.self().getUserAddresses();
		if( i>= 0 && i < uas.length){			
			TextView realname = (TextView)addressGroup.findViewById(R.id.realname);
			TextView address = (TextView)addressGroup.findViewById(R.id.address);
			TextView phone = (TextView)addressGroup.findViewById(R.id.phone);
			TextView province = (TextView)addressGroup.findViewById(R.id.province);
			TextView city = (TextView)addressGroup.findViewById(R.id.city);
			CompoundButton addr_default = (CompoundButton)addressGroup.findViewById(R.id.addr_default);

			UserAddress ua =  uas[i];
			realname.setText(ua.name);
			address.setText(ua.address);
			phone.setText(ua.phone);
			addr_default.setChecked(ua.is_default == 1);
			addr_default.setVisibility(ua.is_default == 1?View.VISIBLE:View.INVISIBLE);
			
			if(ua.province != 0 && !updateDistrict){
				String strProvince = ShopApp.self().getDistrict(ua.province);
				if(strProvince == null || strProvince.isEmpty()){
					DistrictTask task = new DistrictTask();
					task.execute(0);
				}else{
					province.setText(strProvince);							
				}
			}else{
				province.setText(""+ua.province);
			}
			if(ua.city != 0 && !updateDistrict){
				String strCity = ShopApp.self().getDistrict(ua.city);
				if(strCity == null || strCity.isEmpty()){
					DistrictTask task = new DistrictTask();
					task.execute(ua.city);
				}else{
					city.setText(strCity);
				}
			}else{
				city.setText(""+ua.city);
			}
		}		
	}
	private boolean displayAddress(){
		boolean findDefault = false;
		UserAddress[] uas = ShopApp.self().getUserAddresses();
		if(uas != null){
			if(curAddress == -1 || curAddress > uas.length){
				curAddress = 0;
				for(int i=0; i<uas.length; i++){
					if(uas[i] != null && uas[i].is_default == 1){
						curAddress = i;
						findDefault = true;
						break;
					}
				}				
			}
		}
		showCurAddr();
		return findDefault;	
	}
	
	private class DistrictTask extends AsyncTask<Integer,Void,ResultData>{
		@Override
		protected ResultData doInBackground(Integer... params) {
			int districtParentId = params[0];
			ShopHttpParams hp;
			if(districtParentId == 0)
				hp = ShopApp.self().doGetProvinces();
			else
				hp = ShopApp.self().doGetDistricts(districtParentId);
			ResultData result = ShopApp.self().doCommonAction(hp);
			ShopApp.self().parseDistrict(result);
			return result;
		}
		@Override
		protected void onPostExecute(ResultData result) {			
			super.onPostExecute(result);
			updateDistrict = true;
			displayAddress();
		}
	}
	
	private class ShoppingConfirmTask extends AsyncTask<Void,Void,ResultData>{
		@Override
		protected ResultData doInBackground(Void... params) {
			UserAddress[] uas = ShopApp.self().getUserAddresses();
			ShopHttpParams hp = ShopApp.self().doShoppingConfirm(order.rrid, uas[curAddress]._id, order.is_nowbuy,order.is_presaled,order.summary, order.payment_method, order.transfer_time);
			ResultData result = ShopApp.self().doCommonAction(hp);
			return result;
		}
		@Override
		protected void onPostExecute(ResultData result) {		
			super.onPostExecute(result);
			confirm.setEnabled(true);
			if(!result.success){
				ShopApp.showToast(ShoppingOrderActivity.this, result.message);
				return;
			}
			if(clearCart)
				ShoppingOrderActivity.this.getContentResolver().delete(CartItem.URI, null, null);
			//{"success":true,"is_error":false,"status":"0","message":"\u4e0b\u5355\u6210\u529f!",
			//"data":{"rid":"115110202864","pay_money":799,"is_snatched":0,"current_user_id":477411},"current_user_id":477411}	
			try{
				JSONObject data = result.data;
				String rid = data.getString("rid");
				double pay_money = data.getDouble("pay_money");
				Intent intent = new Intent();
				intent.putExtra("rid",rid);
				intent.putExtra("pay_money",pay_money);
				intent.setClass(ShoppingOrderActivity.this, ShoppingConfirmActivity.class);
				ShoppingOrderActivity.this.startActivity(intent);
				ShoppingOrderActivity.this.finish();				
			}catch(JSONException e){
				e.printStackTrace();
			}
		}
	}
	private void displayInfo(){
	
	}	
	private class AddressTask extends AsyncTask<Void,Void,ResultData>{
		@Override
		protected ResultData doInBackground(Void... params) {
			ShopHttpParams hp = ShopApp.self().doGetUserAddress();
			ResultData result = ShopApp.self().doCommonAction(hp);
			ShopApp.self().parseUserAddress(result);
			return result;
		}
		@Override
		protected void onPostExecute(ResultData result) {			
			super.onPostExecute(result);
			displayAddress();
		}
	}
	
	private void displayShoppingList(){
		DecimalFormat df=new DecimalFormat("#.##");
		price.setText("￥"+df.format(order.pay_money));
		if(mAdapter == null){
			mAdapter = new ShoppingListAdapter();
			list.setAdapter(mAdapter);
		}else
			mAdapter.notifyDataSetChanged();
	}
	ShoppingListAdapter mAdapter = null;
	private class ShoppingListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			if(order.shopping_items != null)
				return order.shopping_items.length;
			return 0;
		}

		@Override
		public Object getItem(int position) {
			if(order.shopping_items != null && position < order.shopping_items.length)
				return order.shopping_items[position];
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View v, ViewGroup parent) {
			if (v == null){
				v = ShoppingOrderActivity.this.getLayoutInflater().inflate(R.layout.shopping_item, parent,false);
			}
			ImageView image = (ImageView)v.findViewById(R.id.image);
			TextView title = (TextView)v.findViewById(R.id.title);
			TextView sku_mode = (TextView)v.findViewById(R.id.sku_mode);
			TextView count = (TextView)v.findViewById(R.id.count);
			TextView price = (TextView)v.findViewById(R.id.price);
			if( order.shopping_items!=null && position < order.shopping_items.length){
				ShoppingItem item = order.shopping_items[position];
				ShopApp.self().showImageAsyn(image, item.cover);
				title.setText(item.name + item.title);
				//category.setText(item.sku);
				count.setText(""+item.quantity);
				DecimalFormat df=new DecimalFormat("#.##");
				price.setText("￥"+df.format(item.sale_price));
				if(item.mode!=null && !item.mode.isEmpty()){
					sku_mode.setText(item.mode);
				}
				
			}
			return v;			
		}		
	}
	@Override
	public void onUrlSuccess(ShopHttpParams hp, ResultData result) {
		
	}
}
