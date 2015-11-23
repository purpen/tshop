package com.taihuoniao.shop.activity;

import com.taihuoniao.shop.R;
import com.taihuoniao.shop.ShopApp;
import com.taihuoniao.shop.ShopApp.ResultData;
import com.taihuoniao.shop.ShopApp.ShopHttpParams;
import com.taihuoniao.shop.ShopApp.UserAddress;
import com.taihuoniao.shop.ShopAppContentProvider;
import com.taihuoniao.shop.R.id;
import com.taihuoniao.shop.R.layout;
import com.taihuoniao.shop.ShopAppContentProvider.District;
import com.taihuoniao.shop.ShopUtils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class AddUserAddressActivity extends BaseStyleActivity {
	protected static final String TAG = "AddUserAddressActivity";
	EditText realnameEdit;
	EditText phoneEdit;
	EditText mail_codeEdit;
	Spinner addr_province;
	Spinner addr_city;
	EditText addr_detailEdit;
	CompoundButton is_default;
	Button save_addr;
	int mProvince = -1;
	int mDistrict = -1;
	DistrictAdapter mProvinceAdapter;
	DistrictAdapter mDistrictAdapter;
	UserAddress mUserAddress;
	public static final String ADDR_ID = "addr_id";
	public static final String ADDR_DEFAULT = "addr_is_default";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_user_address);
		showBackButton(true);
		realnameEdit = (EditText)findViewById(R.id.realname);
		phoneEdit = (EditText)findViewById(R.id.phone);
		mail_codeEdit = (EditText)findViewById(R.id.mail_code);
		addr_province = (Spinner)findViewById(R.id.addr_province);
		addr_city = (Spinner)findViewById(R.id.addr_city);
		addr_detailEdit = (EditText)findViewById(R.id.addr_detail);		
		save_addr = (Button)findViewById(R.id.save_addr);
		is_default = (CompoundButton)findViewById(R.id.is_default);
		String addr_id = getIntent().getStringExtra(ADDR_ID);
		mUserAddress = ShopApp.self().getUserAddress(addr_id);
		if(mUserAddress != null){
			realnameEdit.setText(mUserAddress.name);
			phoneEdit.setText(mUserAddress.phone);
			mail_codeEdit.setText(mUserAddress.zip);
			addr_detailEdit.setText(mUserAddress.address);
			is_default.setChecked(mUserAddress.is_default == 1);
			is_default.setSaveEnabled(true);
			mProvince = mUserAddress.province;
			mDistrict = mUserAddress.city;
		}else{
			mUserAddress = new UserAddress();
			is_default.setEnabled(false);
		}
	}
	@Override
	protected void onResume() {
		super.onResume();
		if(!showProvinces()){
			UpdateProvinces();
		}
	}
	public void onSaveAddr(View v){
		final String realname = realnameEdit.getText().toString();
		final String phone = phoneEdit.getText().toString();
		final String mail_code = mail_codeEdit.getText().toString();
		final String province = ""+mProvince;
		final String city = ""+mDistrict;
		final String address = addr_detailEdit.getText().toString();
		save_addr.setEnabled(false);
		if(is_default.isChecked() && mUserAddress._id != null && !mUserAddress._id.isEmpty()){
			ShopHttpParams hp = ShopApp.self().setDefaultAddress(mUserAddress._id);
			sendUrlRequest(hp);			
		}		
		ShopHttpParams hp = ShopApp.self().editAddress(realname, phone, province, city, address, mail_code, mUserAddress._id,is_default.isChecked()?1:0);
		sendUrlRequest(hp);

	}
	private boolean showProvinces(){
		Cursor cursor = this.getContentResolver().query(ShopAppContentProvider.DISTRICT_CONTENT_URI,null,ShopAppContentProvider.District.PARENT + "=0",null, null);
		int selection = 0;
		while(cursor.moveToNext()){
			int id = cursor.getInt(cursor.getColumnIndex(ShopAppContentProvider.District._ID));
			if(id == mProvince){
				selection = cursor.getPosition();
				break;
			}
		}
		mProvinceAdapter = new DistrictAdapter(this,cursor);
		addr_province.setAdapter(mProvinceAdapter);
		addr_province.setSelection(selection);
		addr_province.setOnItemSelectedListener(
                new OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                    	mProvince = (Integer)view.getTag();
                    	if(!showDistricts())
                    		UpdateDistricts();
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                    	
                    }
                });		
		return cursor.getCount()>0;
	}
	private boolean showDistricts(){
		Cursor cursor = this.getContentResolver().query(ShopAppContentProvider.DISTRICT_CONTENT_URI,null,ShopAppContentProvider.District.PARENT + "=" + mProvince,null, null);
		int selection = 0;
		while(cursor.moveToNext()){
			int id = cursor.getInt(cursor.getColumnIndex(ShopAppContentProvider.District._ID));
			if(id == mDistrict){
				selection = cursor.getPosition();
				break;
			}
		}
		mDistrictAdapter = new DistrictAdapter(this,cursor);		
        addr_city.setAdapter(mDistrictAdapter);  
        addr_city.setSelection(selection);
        addr_city.setOnItemSelectedListener(
                new OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                    	mDistrict = (Integer)view.getTag();
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                    	
                    }
                });
       return cursor.getCount()>0;
	}	
	private void UpdateProvinces(){
		ShopHttpParams hp = ShopApp.doGetProvinces();
		sendUrlRequest(hp);
		hp = ShopApp.doGetDistricts(-1);
		sendUrlRequest(hp);
	}
	private void UpdateDistricts(){
		ShopHttpParams hp = ShopApp.doGetDistricts(mProvince);
		sendUrlRequest(hp);
	}
	@Override
	public void onUrlSuccess(ShopHttpParams hp, ResultData result) {
		if(hp.url.startsWith(ShopUtils.getEditUserAddressUrl())){			
			save_addr.setEnabled(true);
			Intent intent = new Intent();
			intent.putExtra(ADDR_ID, mUserAddress._id);
			setResult(RESULT_OK, intent); //intent为A传来的带有Bundle的intent，当然也可以自己定义新的Bundle			
			AddUserAddressActivity.this.finish();
		}else if(hp.url.startsWith(ShopUtils.getProvincesUrl())){
			ShopApp.self().parseDistrict(result);
			showProvinces();
		}else if(hp.url.startsWith(ShopUtils.getDistrictsUrl())){
			Log.e(TAG,"onUrlSuccess parseDistrict start");
			ShopApp.self().parseDistrict(result);
			Log.e(TAG,"onUrlSuccess parseDistrict end");
			showDistricts();
		}
	}
	@Override 
	public void onUrlFailure(ShopHttpParams hp,ResultData result){
		super.onUrlFailure(hp, result);
		if(hp.url.startsWith(ShopUtils.getEditUserAddressUrl())){
			save_addr.setEnabled(true);
		}
	}
	
	private static class DistrictAdapter extends CursorAdapter {

		protected LayoutInflater mInflater;
		public DistrictAdapter(Context context,Cursor cursor) {
			super(context, cursor,true);
	        mInflater = (LayoutInflater) context.getSystemService(
	                Context.LAYOUT_INFLATER_SERVICE);
		}
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = mInflater.inflate(android.R.layout.simple_spinner_item, parent, false); 
			return view;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			TextView text = (TextView)view.findViewById(android.R.id.text1);			
			String name = cursor.getString(cursor.getColumnIndex(ShopAppContentProvider.District.NAME));
			int id = cursor.getInt(cursor.getColumnIndex(ShopAppContentProvider.District._ID));
			text.setText(name);
			view.setTag(id);
		}
	    @Override
	    public View getDropDownView(int position, View convertView, ViewGroup parent) {
	    	return super.getDropDownView(position, convertView, parent);
	        //return createViewFromResource(position, convertView, parent, mDropDownResource);
	    }
		int mDropDownResource = android.R.layout.simple_spinner_dropdown_item;
	    public void setDropDownViewResource(int resource) {
	        this.mDropDownResource = resource;
	    }
		int mFieldId = 0;
	    private View createViewFromResource(int position, View convertView, ViewGroup parent,
	            int resource) {
	        View view;
	        TextView text;

	        if (convertView == null) {
	            view = mInflater.inflate(resource, parent, false);
	        } else {
	            view = convertView;
	        }

	        try {
	            if (mFieldId == 0) {
	                //  If no custom field is assigned, assume the whole resource is a TextView
	                text = (TextView) view;
	            } else {
	                //  Otherwise, find the TextView field within the layout
	                text = (TextView) view.findViewById(mFieldId);
	            }
	        } catch (ClassCastException e) {
	            Log.e("ArrayAdapter", "You must supply a resource ID for a TextView");
	            throw new IllegalStateException(
	                    "ArrayAdapter requires the resource ID to be a TextView", e);
	        }

	        if (mDataValid) {
	            mCursor.moveToPosition(position);
	            String name = mCursor.getString(mCursor.getColumnIndex(ShopAppContentProvider.District.NAME));
	            int id = mCursor.getInt(mCursor.getColumnIndex(ShopAppContentProvider.District._ID));
		        text.setText(name);
		        view.setTag(id);
	        }

	        return view;
	    }		
	}	
}
