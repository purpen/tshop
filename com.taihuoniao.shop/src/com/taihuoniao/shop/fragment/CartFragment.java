package com.taihuoniao.shop.fragment;

import java.text.DecimalFormat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.taihuoniao.shop.R;
import com.taihuoniao.shop.ShopApp;
import com.taihuoniao.shop.ShopApp.ResultData;
import com.taihuoniao.shop.ShopApp.ShopHttpParams;
import com.taihuoniao.shop.ShopApp.ShoppingOrder;
import com.taihuoniao.shop.ShopApp.UserAddress;
import com.taihuoniao.shop.ShopAppContentProvider;
import com.taihuoniao.shop.ShopAppContentProvider.CartItem;
import com.taihuoniao.shop.ShopUtils;
import com.taihuoniao.shop.activity.BaseStyleActivity;
import com.taihuoniao.shop.activity.ProductViewActivity;
import com.taihuoniao.shop.activity.ShoppingOrderActivity;
import com.taihuoniao.shop.widget.SwipeListView;
import com.taihuoniao.shop.widget.SwipeListView.OnDeleteItemListener;

/**
 * A simple {@link Fragment} subclass.
 * 
 */
public class CartFragment extends BaseStyleFragment {
	private SwipeListView list;
	private Button cartCheckout;
	private TextView total_cost;
	private JSONArray jArray;
	
	public CartFragment() {
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_cart, container, false);
		list = (SwipeListView)v.findViewById(R.id.list);
		cartCheckout = (Button)v.findViewById(R.id.cartCheckout);
		total_cost = (TextView)v.findViewById(R.id.total_cost);
		list.setOnItemClickListener(mOnItemClick);
		list.setOnDeleteItemListener( new OnDeleteItemListener() {
			@Override
			public void onDeleteItem(int position, View content) {
				CartItem item = (CartItem)content.getTag();
				Uri uri = ShopAppContentProvider.CartItem.URI.buildUpon().appendPath(""+item._id).build();
				getActivity().getContentResolver().delete(uri, null, null);
				InitCheckout();
			}
		});
		
		cartCheckout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onCheckout();
			}
		});
		return v;
	}
	OnItemClickListener mOnItemClick = new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			CartItem item = (CartItem)view.getTag();
			Intent intent = new Intent();
			intent.setClass(getActivity(), ProductViewActivity.class);
			intent.putExtra(ShopAppContentProvider.ProductItem._ID, item.product_id);
			getActivity().startActivity(intent);
		}
	};	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	@Override
	public void onResume() {
		super.onResume();
		getActivity().setTitle(R.string.title_cart);	
		final BaseStyleActivity bsa = (BaseStyleActivity)getActivity();
		bsa.setBackAction(null);		
		InitCheckout();
	}
	public void InitCheckout(){
		Cursor cursor = getActivity().getContentResolver().query(ShopAppContentProvider.CartItem.URI, null, null, null, null);
		double total = 0;
		jArray = new JSONArray();
		boolean cartEnable = false;
		if(cursor != null){
			while(cursor.moveToNext()){
				CartItem item = new CartItem();
				item.ReadFromCursor(cursor);	
				JSONObject jObject = new JSONObject();
				try{
					jObject.put("sku_id", item._id);
					jObject.put("product_id", item.product_id);
					jObject.put("n", item.quantity);
					jArray.put(jObject);
				}catch(JSONException e){
					e.printStackTrace();
				}
				total += cursor.getInt(cursor.getColumnIndex(CartItem.SALE_PRICE)) * cursor.getInt(cursor.getColumnIndex(CartItem.QUANTITY));
				cartEnable = true;	
			}
			mAdapter = new CartItemList(getActivity(),cursor,true);
			list.setAdapter(mAdapter);
		}
		DecimalFormat df=new DecimalFormat("#.##");
		total_cost.setText("￥"+df.format(total));
		cartCheckout.setEnabled(cartEnable);
	}
	
	public void onCheckout(){
		cartCheckout.setEnabled(false);
		ShopHttpParams hp = ShopApp.self().doShoppingCheckout(jArray.toString());
		sendUrlRequest(hp);
	}
	
	CartItemList mAdapter = null;
	private class CartItemList extends CursorAdapter{
		LayoutInflater mInflater;

		public CartItemList(Context context, Cursor c, boolean autoRequery) {
			super(context, c, autoRequery);
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return mInflater.inflate(R.layout.cart_item, parent,false);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			View v = view;
			CartItem item = new CartItem();
			item.ReadFromCursor(cursor);
			ImageView image = (ImageView)v.findViewById(R.id.image);
			TextView title = (TextView)v.findViewById(R.id.title);
			TextView count = (TextView)v.findViewById(R.id.count);
			TextView price = (TextView)v.findViewById(R.id.price);
			TextView sku_mode = (TextView)v.findViewById(R.id.sku_mode);
			if(item.mode != null && !item.mode.isEmpty()){
				sku_mode.setText(item.mode);
			}
			
			ShopApp.self().showImageAsyn(image, item.cover);
			if(item.title != null){
				title.setText(item.title);
			}
			count.setText(""+item.quantity);
			DecimalFormat df=new DecimalFormat("#.##");
			price.setText("￥"+df.format(item.sale_price));	
			v.setTag(item);
		}		
	}
	@Override
	public void onUrlRequestReturn(ShopHttpParams hp, ResultData result) {
		super.onUrlRequestReturn(hp, result);
		if(hp.url.startsWith(ShopUtils.getShoppingCheckoutUrl())){
			cartCheckout.setEnabled(true);				
		}		
	}
	
	void putSkuModeToOrder(ShoppingOrder order){
		Cursor cursor = getActivity().getContentResolver().query(ShopAppContentProvider.CartItem.URI, null, null, null, null);
		while(cursor!= null && cursor.moveToNext()){
			CartItem item = new CartItem();
			item.ReadFromCursor(cursor);
			for(int i=0; order.shopping_items != null && i<order.shopping_items.length; i++){
				if(order.shopping_items[i].sku.equals(""+item._id)){
					order.shopping_items[i].mode = item.mode;
				}
			}
		}
		if(cursor != null)
			cursor.close();
	}
	@Override
	public void onUrlSuccess(ShopHttpParams hp, ResultData result) {
		if(hp.url.startsWith(ShopUtils.getShoppingCheckoutUrl())){
			JSONObject data = result.data;
			ShoppingOrder order = new ShoppingOrder();
			order.readJsonData(data);
			if(order.shopping_items != null){
				putSkuModeToOrder(order);
			}
			Intent intent = new Intent();
			intent.putExtra(ShoppingOrderActivity.ORDER_OBJECT, order);
			intent.putExtra("clearCart", true);//如果订单提交了，那么要清理购物车
			intent.setClass(getActivity(), ShoppingOrderActivity.class);
			getActivity().startActivity(intent);
			
		}
	}	
}
